package com.ioniq.widget;
import android.content.Context;
import org.json.JSONObject;
final class VehicleIdentity {
 static void save(Context c,JSONObject details)throws Exception{
  String vin=Vault.read(c).optString("vin","");
  if(vin.isEmpty()||!vin.equals(details.optString("vin")))return;
  JSONObject p=new JSONObject().put("vin",vin).put("model",ModelName.number(details.optString("modelCode"),details.optString("modelName"))).put("year",details.optString("modelYear",""));
  c.getSharedPreferences("vehicle_identity",0).edit().putString("json",p.toString()).apply();
 }
 static JSONObject read(Context c){
  try{String vin=Vault.read(c).optString("vin","");JSONObject p=new JSONObject(c.getSharedPreferences("vehicle_identity",0).getString("json","{}"));if(!vin.isEmpty()&&vin.equals(p.optString("vin")))return p;}catch(Exception ignored){}
  return new JSONObject();
 }
 static boolean needsLookup(Context c){try{return !Vault.read(c).optString("vin","").isEmpty()&&!read(c).has("vin");}catch(Exception e){return false;}}
 static String title(Context c){return ModelName.title(read(c).optInt("model"));}
}
