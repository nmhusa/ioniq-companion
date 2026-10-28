package com.ioniq.widget;
import android.content.Context;
import android.location.Location;
import org.json.*;
final class CarLocations {
 static final String VEHICLE="vehicle",PARKING="parking";
 static String vin(Context c){try{return Vault.read(c).optString("vin","");}catch(Exception e){return "";}}
 static GeoPoint point(JSONObject data)throws Exception {JSONObject coord=data.getJSONObject("coord");return GeoPoint.parse(coord.getString("lat"),coord.getString("lon"));}
 static void saveVehicle(Context c,String expectedVin,JSONObject data)throws Exception {
  point(data);if(expectedVin.isEmpty()||!expectedVin.equals(vin(c)))throw new Exception("Selected vehicle changed. Location not saved.");
  JSONObject saved=new JSONObject(data.toString()).put("vin",expectedVin).put("retrieved",System.currentTimeMillis());
  c.getSharedPreferences("locations",0).edit().putString(VEHICLE,saved.toString()).commit();
 }
 static void saveParking(Context c,Location location){try{
  String selected=vin(c);if(selected.isEmpty())return;
  GeoPoint p=new GeoPoint(location.getLatitude(),location.getLongitude());
  JSONObject data=new JSONObject().put("vin",selected).put("saved",System.currentTimeMillis()).put("accuracy",location.getAccuracy())
   .put("coord",new JSONObject().put("lat",p.latitude).put("lon",p.longitude));
  c.getSharedPreferences("locations",0).edit().putString(PARKING,data.toString()).apply();
 }catch(Exception ignored){}}
 static JSONObject read(Context c,String source){try{JSONObject data=new JSONObject(c.getSharedPreferences("locations",0).getString(source,"{}"));String selected=vin(c);if(selected.isEmpty()||!selected.equals(data.optString("vin")))return null;point(data);return data;}catch(Exception e){return null;}}
 static String deviceTime(long time){return time<=0?"Unavailable":java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.SHORT).format(new java.util.Date(time));}
 static String description(JSONObject data,String source){
  if(PARKING.equals(source))return "Saved parking spot (phone)\nSaved: "+deviceTime(data.optLong("saved"))+"\nGPS accuracy: ±"+Math.round(data.optDouble("accuracy",0))+" m\nThis is where your phone marked parking, not a location reported by the car.";
  String time=data.optString("time","");if(time.isEmpty()||time.equals("null"))time="Unavailable";
  return "Hyundai-reported location\nVehicle timestamp: "+time+"\nRetrieved: "+deviceTime(data.optLong("retrieved"))+"\nReported coordinates may be old. Retrieval time is not the time the car was at this spot.";
 }
 static void clear(Context c){c.getSharedPreferences("locations",0).edit().clear().commit();}
}
