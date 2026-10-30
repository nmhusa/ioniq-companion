package com.ioniq.widget;
import android.content.*;
import android.widget.*;
import org.json.*;
final class SeatClimate {
 static JSONObject caps(Context c){JSONObject p=VehicleIdentity.read(c);JSONObject caps=p.optJSONObject("seatCapabilities");return caps==null?new JSONObject():caps;}
 static String generation(Context c,JSONObject auth){JSONObject p=VehicleIdentity.read(c);int generation=p.optJSONObject("seatCapabilities")==null?0:p.optJSONObject("seatCapabilities").optInt("generation");return auth.optString("vin").equals(p.optString("vin"))&&generation>0?String.valueOf(generation):auth.optInt("year",2023)>=2025?"3":"2";}
 static JSONObject saved(Context c,String name){try{String scope=VehicleGarage.scope(c);return scope.isEmpty()?new JSONObject():new JSONObject(SecurePrefs.get(c,"seat_presets").getString(scope+"\n"+name,"{}"));}catch(Exception e){return new JSONObject();}}
 static JSONObject apply(Context c,JSONObject body)throws Exception{JSONObject seats=SeatOptions.command(caps(c),saved(c,NamedClimate.active(c)));if(seats!=null)body.put("seatHeaterVentInfo",seats).put("igniOnDuration",5);return body;}
 static String summary(Context c){try{JSONObject cap=caps(c),options=saved(c,NamedClimate.active(c));if(!options.optBoolean("enabled")||!SeatOptions.any(cap))return "";JSONArray values=options.optJSONArray("values");StringBuilder s=new StringBuilder("Seats · 5 min climate duration");for(int i=0;i<4;i++)if(SeatOptions.levels(cap,i).length()>0)s.append("\n").append(SeatOptions.NAMES[i]).append(": ").append(SeatOptions.label(values==null?2:values.optInt(i,2)));return s.toString();}catch(Exception e){return "";}}
 static final class Editor {
  final Context context;final String scope,name;final JSONObject caps;final CheckBox enabled;final Spinner[] pickers=new Spinner[4];
  Editor(Context c,LinearLayout box,String preset){context=c;scope=VehicleGarage.scope(c);name=preset;caps=caps(c);JSONObject saved=saved(c,preset);
   if(!SeatOptions.any(caps)){enabled=null;return;}
   enabled=new CheckBox(c);enabled.setText("Include seat heating / cooling");enabled.setChecked(saved.optBoolean("enabled"));box.addView(enabled);TextView note=new TextView(c);note.setText("Applied when Climate on runs this preset. Other supported seats use the settings below. Climate duration: 5 minutes.");box.addView(note);
   JSONArray values=saved.optJSONArray("values");for(int i=0;i<4;i++){JSONArray levels=SeatOptions.levels(caps,i);if(levels.length()==0)continue;TextView label=new TextView(c);label.setText(SeatOptions.NAMES[i]);box.addView(label);Spinner picker=new Spinner(c);pickers[i]=picker;java.util.ArrayList<String> names=new java.util.ArrayList<>();int active=0;for(int n=0;n<levels.length();n++){int code=levels.optInt(n);names.add(SeatOptions.label(code));if(values!=null&&values.optInt(i,2)==code)active=n;}picker.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,names));picker.setSelection(active);picker.setEnabled(enabled.isChecked());box.addView(picker);}
   enabled.setOnCheckedChangeListener((b,on)->{for(Spinner picker:pickers)if(picker!=null)picker.setEnabled(on);});
  }
  void save()throws Exception{if(scope.isEmpty()||!scope.equals(VehicleGarage.scope(context)))throw new Exception("Vehicle changed. Reopen its climate preset.");if(enabled==null)return;JSONArray values=new JSONArray();for(int i=0;i<4;i++)values.put(pickers[i]==null?0:SeatOptions.levels(caps,i).optInt(pickers[i].getSelectedItemPosition(),2));JSONObject choice=new JSONObject().put("enabled",enabled.isChecked()).put("values",values);SeatOptions.command(caps(context),choice);if(!SecurePrefs.get(context,"seat_presets").edit().putString(scope+"\n"+name,choice.toString()).commit())throw new Exception("Could not save seat settings.");}
 }
}
