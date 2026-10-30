package com.ioniq.widget;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
final class ClimatePreset {
 static JSONObject body(Context c,int temp)throws Exception {
  SharedPreferences p=c.getSharedPreferences("ui",0);
  return SeatClimate.apply(c,new JSONObject().put("airCtrl",1).put("defrost",p.getBoolean("front_defrost",false))
   .put("airTemp",new JSONObject().put("value",String.valueOf(temp)).put("unit",1))
   .put("heating1",ClimateHeatMode.encode(p.getBoolean("rear_defrost",false),p.getBoolean("steering_heat",false))));
 }
 static String summary(Context c){
  SharedPreferences p=c.getSharedPreferences("ui",0);java.util.ArrayList<String> enabled=new java.util.ArrayList<>();
  if(p.getBoolean("front_defrost",false))enabled.add("Front defrost");
  if(p.getBoolean("rear_defrost",false))enabled.add("Rear defrost");
  if(p.getBoolean("steering_heat",false))enabled.add("Steering heat");
  String seats=SeatClimate.summary(c);if(!seats.isEmpty())enabled.add(seats);
  return enabled.isEmpty()?"Defrost & steering heat · off":android.text.TextUtils.join(" · ",enabled);
 }
}
