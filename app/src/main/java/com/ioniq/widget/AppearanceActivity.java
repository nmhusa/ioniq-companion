package com.ioniq.widget;
import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
import org.json.*;
/** Local-only appearance editor; no car command or Hyundai request. */
public final class AppearanceActivity extends Activity {
 private String scope,selected="";private JSONObject profile,group;private ImageView preview;private RadioGroup colors;private TextView note;private boolean rendering;
 private final java.util.ArrayList<JSONObject> options=new java.util.ArrayList<>();
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 private TextView text(String label,int size){TextView t=new TextView(this);t.setText(label);t.setTextSize(size);t.setTextColor(0xffb8d4df);t.setPadding(0,dp(8),0,dp(8));return t;}
 @Override public void onCreate(Bundle b){super.onCreate(b);scope=VehicleGarage.scope(this);profile=VehicleIdentity.read(this);if(scope.isEmpty()){Toast.makeText(this,"Connect and select a vehicle first.",Toast.LENGTH_LONG).show();finish();return;}
  try{
   LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(20),dp(12),dp(20),dp(12));page.setBackgroundColor(0xff0b161f);setContentView(page);
   page.addView(text("Car appearance",24));preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.FIT_CENTER);preview.setContentDescription("Selected vehicle paint preview");page.addView(preview,new LinearLayout.LayoutParams(-1,dp(150)));
   JSONObject saved=VehicleAppearance.selection(this),initial=PaintCatalog.group(this,saved.optString("group"));selected=saved.optString("color");if(initial==null)initial=PaintCatalog.defaultGroup(this,profile);
   JSONArray all=PaintCatalog.groups(this);java.util.ArrayList<String> labels=new java.util.ArrayList<>();int active=0;
   for(int i=0;i<all.length();i++){JSONObject g=all.getJSONObject(i);if(profile.optInt("model")==0||g.optInt("model")==profile.optInt("model")){if(initial!=null&&g.getString("id").equals(initial.getString("id")))active=options.size();options.add(g);labels.add(g.getString("label"));}}
   if(options.isEmpty())throw new Exception("No appearance palettes available.");
   Spinner palettes=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);palettes.setAdapter(adapter);palettes.setContentDescription("Model year and body style palette");page.addView(palettes,new LinearLayout.LayoutParams(-1,dp(52)));
   note=text("",12);page.addView(note);ScrollView scroll=new ScrollView(this);colors=new RadioGroup(this);colors.setOrientation(LinearLayout.VERTICAL);scroll.addView(colors);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
   LinearLayout actions=new LinearLayout(this);for(String label:new String[]{"Cancel","Save"}){Button button=new Button(this);button.setText(label);button.setAllCaps(false);actions.addView(button,new LinearLayout.LayoutParams(0,dp(52),1));button.setOnClickListener(v->{if(label.equals("Cancel")){finish();return;}try{VehicleAppearance.save(this,scope,group.getString("id"),selected);finish();}catch(Exception e){new AlertDialog.Builder(this).setMessage(e.getMessage()).setPositiveButton("OK",null).show();}});}page.addView(actions);
   group=options.get(active);render();palettes.setSelection(active);palettes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int position,long id){JSONObject next=options.get(position);if(next!=group){group=next;selected="";render();}}});
  }catch(Exception e){new AlertDialog.Builder(this).setMessage("Appearance options could not be loaded.").setPositiveButton("Close",(d,w)->finish()).setOnCancelListener(d->finish()).show();}
 }
 private void render(){try{rendering=true;colors.removeAllViews();boolean sameYear=group.optString("year").equals(profile.optString("year"));note.setText((sameYear?"US factory palette. ":"Reference palette — availability unverified for your car’s year. ")+"Trim availability varies. Images are representative; wheels and paint finish may differ.");
  RadioButton automatic=choice("Automatic / original image","");colors.addView(automatic);JSONArray list=group.getJSONArray("colors");boolean found=false;
  for(int i=0;i<list.length();i++){JSONObject color=list.getJSONObject(i);RadioButton radio=choice(color.getString("name"),color.getString("id"));colors.addView(radio);if(color.getString("id").equals(selected)){radio.setChecked(true);found=true;}}
  if(!found){selected="";automatic.setChecked(true);}rendering=false;refreshPreview();
 }catch(Exception e){rendering=false;note.setText("Could not load this palette.");}}
 private RadioButton choice(String label,String value){RadioButton r=new RadioButton(this);r.setText(label);r.setTextColor(Color.WHITE);r.setMinHeight(dp(48));r.setButtonTintList(android.content.res.ColorStateList.valueOf(0xff95e4cf));r.setOnCheckedChangeListener((button,checked)->{if(checked&&!rendering){selected=value;refreshPreview();}});return r;}
 private void refreshPreview(){try{JSONObject color=selected.isEmpty()?PaintCatalog.match(PaintCatalog.defaultGroup(this,profile),profile.optJSONArray("paintHints")):PaintCatalog.color(group,selected);VehicleAppearance.show(this,preview,color==null?"":color.getString("asset"),profile.optInt("model"));}catch(Exception ignored){}}
}
