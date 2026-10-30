package com.ioniq.widget;
import org.json.JSONObject;
/** Reads only the auxiliary battery field from the existing cached US status. */
final class AuxBattery {
 static double percent(JSONObject status){
  JSONObject battery=status.optJSONObject("battery");Object raw=battery==null?null:battery.opt("batSoc");
  if(!(raw instanceof Number)&&!(raw instanceof String))return Double.NaN;
  try{double n=Double.parseDouble(raw.toString().trim());return Double.isFinite(n)&&n>=0&&n<=100?n:Double.NaN;}catch(Exception e){return Double.NaN;}
 }
 static long age(JSONObject s,long now){try{long at=java.time.OffsetDateTime.parse(s.optString("dateTime")).toInstant().toEpochMilli();return at>now+300000?-1:Math.max(0,now-at);}catch(Exception e){return -1;}}
 static String value(JSONObject s,long now){double p=percent(s);return Double.isNaN(p)?"Unknown":String.format(java.util.Locale.US,"%.0f%%",p)+(age(s,now)>86400000?" · old":"");}
 static int color(JSONObject s,long now){double p=percent(s);if(Double.isNaN(p)||age(s,now)<0||age(s,now)>86400000)return 0xffbcd6e3;return p<50?0xffff8c87:p<70?0xffffcf72:0xffbcd6e3;}
 static String details(JSONObject s,long now){double p=percent(s);long age=age(s,now);String time=s.optString("dateTime","");String freshness=age<0?"Report age unavailable":age<60000?"Reported less than a minute ago":age<3600000?"Reported "+age/60000+" minutes ago":age<86400000?"Reported "+age/3600000+" hours ago":"Reported "+age/86400000+" days ago (old reading)";
  return (Double.isNaN(p)?"Hyundai did not provide a usable 12-volt charge reading in this report.":"Last reported 12-volt charge: "+String.format(java.util.Locale.US,"%.0f%%",p))+"\n\n"+freshness+(time.isEmpty()?"":"\nVehicle report: "+time)+"\n\nThis is an estimated charge level, not a battery-health test or a guarantee the car will start. It updates with the normal vehicle status refresh, not continuously.\n\nApp color guides: amber below 70%, red below 50%. These are app display thresholds, not Hyundai fault limits. Old readings (over 24 hours) or readings with unknown age use a neutral color.";
 }
}
