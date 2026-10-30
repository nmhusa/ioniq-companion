package com.ioniq.widget;
import android.content.Context;
import org.json.JSONObject;
final class VehicleIdentity {
 static void save(Context c,JSONObject details)throws Exception{
  String vin=Vault.read(c).optString("vin","");
  if(vin.isEmpty()||!vin.equals(details.optString("vin")))return;
  JSONObject p=new JSONObject().put("vin",vin).put("model",ModelName.number(details.optString("modelCode"),details.optString("modelName"))).put("year",details.optString("modelYear",""));
  p.put("variant",ModelName.variant(details.optString("modelCode")+" "+details.optString("modelName")+" "+details.optString("trimName")+" "+details.optString("trimDescription")));
  org.json.JSONArray hints=new org.json.JSONArray();for(String key:new String[]{"exteriorColorCode","exteriorColor","exteriorColorName","paintCode"}){Object value=details.opt(key);if(value instanceof String&&((String)value).length()<=64)hints.put(value);}p.put("paintHints",hints);
  SecurePrefs.get(c,"vehicle_identity").edit().putString("json",p.toString()).apply();
 }
 static JSONObject read(Context c){
  try{String vin=Vault.read(c).optString("vin","");JSONObject p=new JSONObject(SecurePrefs.get(c,"vehicle_identity").getString("json","{}"));if(!vin.isEmpty()&&vin.equals(p.optString("vin")))return p;}catch(Exception ignored){}
  return new JSONObject();
 }
 static boolean needsLookup(Context c){try{return !Vault.read(c).optString("vin","").isEmpty()&&!read(c).has("vin");}catch(Exception e){return false;}}
 static String title(Context c){String name=VehicleGarage.nickname(c,VehicleGarage.vin(c));return name.isEmpty()?ModelName.title(read(c).optInt("model")):name;}
}
