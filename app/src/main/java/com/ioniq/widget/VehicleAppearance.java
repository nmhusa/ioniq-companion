package com.ioniq.widget;
import android.content.Context;
import android.widget.ImageView;
import org.json.*;
final class VehicleAppearance {
 static JSONObject selection(Context c){try{String scope=VehicleGarage.scope(c);return scope.isEmpty()?new JSONObject():new JSONObject(SecurePrefs.get(c,"vehicle_appearance").getString(scope,"{}"));}catch(Exception e){return new JSONObject();}}
 static void save(Context c,String scope,String group,String color)throws Exception{
  if(scope.isEmpty()||!scope.equals(VehicleGarage.scope(c)))throw new Exception("Vehicle changed. Reopen its appearance settings.");
  android.content.SharedPreferences.Editor e=SecurePrefs.get(c,"vehicle_appearance").edit();
  if(color.isEmpty())e.remove(scope);else{JSONObject g=PaintCatalog.group(c,group);int model=VehicleIdentity.read(c).optInt("model");if(g==null||(model!=0&&model!=g.optInt("model"))||PaintCatalog.color(g,color)==null)throw new Exception("Choose a color for this vehicle.");e.putString(scope,new JSONObject().put("group",group).put("color",color).toString());}
  if(!e.commit())throw new Exception("Could not save appearance.");
 }
 static String asset(Context c){try{JSONObject p=VehicleIdentity.read(c),s=selection(c),g=PaintCatalog.group(c,s.optString("group")),color=null;
  if(g!=null&&(p.optInt("model")==0||g.optInt("model")==p.optInt("model")))color=PaintCatalog.color(g,s.optString("color"));
  if(color==null)color=PaintCatalog.match(PaintCatalog.defaultGroup(c,p),p.optJSONArray("paintHints"));return color==null?"":color.getString("asset");
 }catch(Exception e){return "";}}
 static void show(Context c,ImageView image,String asset,int model){
  if(!asset.isEmpty())try(java.io.InputStream in=c.getAssets().open(asset)){android.graphics.Bitmap bitmap=android.graphics.BitmapFactory.decodeStream(in);if(bitmap!=null){image.setImageBitmap(bitmap);return;}}catch(Exception ignored){}
  image.setImageResource(model==5?R.drawable.car_profile_5:model==6?R.drawable.car_profile:model==9?R.drawable.car_profile_9:R.drawable.ic_launcher_car);
 }
}
