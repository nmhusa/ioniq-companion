package com.ioniq.widget;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;

/** Uses existing public API methods so local Hyundai credentials/code remain untouched. */
final class VehicleMileage {
 private static MileageReading reading(JSONObject vehicle){
  Object raw=vehicle.opt("odometer");int unit=3;String date=vehicle.optString("odometerUpdateDate","");
  if(raw instanceof JSONObject){JSONObject o=(JSONObject)raw;raw=o.opt("value");unit=o.optInt("unit",3);}
  return raw==null||raw==JSONObject.NULL?null:MileageReading.parse(String.valueOf(raw),unit,date);
 }
 private static void store(Context c,String vin,MileageReading reading,boolean stale)throws JSONException{
  JSONObject data=new JSONObject().put("vin",vin).put("value",reading.value).put("unit",reading.unit).put("reportTime",reading.reportTime).put("stale",stale);
  c.getSharedPreferences("mileage",0).edit().putString("json",data.toString()).apply();
 }
 static JSONObject refresh(Context c,boolean force)throws Exception{
  HyundaiApi api=new HyundaiApi(c);JSONObject status=api.status(force);
  String vin=Vault.read(c).optString("vin","");if(vin.isEmpty())return status;
  MileageReading fallback=reading(status);
  SharedPreferences prefs=c.getSharedPreferences("mileage",0);
  // A partial/failed details response must not erase the last valid mileage.
  try{
   JSONArray vehicles=api.vehicles();
   for(int i=0;i<vehicles.length();i++){
    JSONObject item=vehicles.optJSONObject(i);JSONObject details=item==null?null:item.optJSONObject("vehicleDetails");
    if(details==null||!vin.equals(details.optString("vin")))continue;
    MileageReading selected=reading(details);
    if(selected!=null){store(c,vin,selected,false);return status;}
    break;
   }
  }catch(Exception ignored){ /* Keep status refresh successful even if mileage is unavailable. */ }
  if(fallback!=null)store(c,vin,fallback,false);
  else {
   JSONObject previous=new JSONObject(prefs.getString("json","{}"));
   if(vin.equals(previous.optString("vin")))prefs.edit().putString("json",previous.put("stale",true).toString()).apply();
   else prefs.edit().clear().apply();
  }
  return status;
 }
 static void attach(Context c,JSONObject status){
  try{
   String vin=Vault.read(c).optString("vin","");
   JSONObject saved=new JSONObject(c.getSharedPreferences("mileage",0).getString("json","{}"));
   if(!vin.isEmpty() && vin.equals(saved.optString("vin"))){
    status.put("odometer",new JSONObject().put("value",saved.getDouble("value")).put("unit",saved.getInt("unit")));
    status.put("mileageReport",MileageReading.reportLabel(saved.optString("reportTime"))+(saved.optBoolean("stale")?" · last known; refresh unavailable":""));
   }else{
    MileageReading fallback=reading(status);
    if(fallback!=null){status.put("odometer",new JSONObject().put("value",fallback.value).put("unit",fallback.unit));status.put("mileageReport",MileageReading.reportLabel(fallback.reportTime));}
   }
  }catch(Exception ignored){ /* Unknown stays unavailable; never substitute another vehicle. */ }
 }
}
