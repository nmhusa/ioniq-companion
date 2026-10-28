package com.ioniq.widget;
import org.json.*;
final class StatusText {
 static String battery(JSONObject s){JSONObject e=s.optJSONObject("evStatus");return e!=null&&e.has("batteryStatus")?e.optString("batteryStatus")+"%":"—%";}
 static String details(JSONObject s){
  JSONObject e=s.optJSONObject("evStatus");String range="Range unavailable";
  try{JSONObject r=e.getJSONArray("drvDistance").getJSONObject(0).getJSONObject("rangeByFuel").getJSONObject("evModeRange");range=r.getString("value")+(r.optInt("unit",3)==1?" km":" mi");}catch(Exception ignored){}
  String charge=e==null?"Charging unknown":!e.has("batteryCharge")?"Charging unknown":e.optBoolean("batteryCharge")?"Charging":e.optInt("batteryPlugin")>0?"Plugged in":"Unplugged";
  String lock=!s.has("doorLock")?"Lock unknown":s.optBoolean("doorLock")?"Locked":"Unlocked";
  return range+"  •  "+charge+"  •  "+lock;
 }
 static String time(JSONObject s){return "Vehicle report: "+s.optString("dateTime","time unavailable");}
}
