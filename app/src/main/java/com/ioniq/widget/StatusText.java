package com.ioniq.widget;
import org.json.*;
final class StatusText {
 static String battery(JSONObject s){JSONObject e=s.optJSONObject("evStatus");double n=e==null?Double.NaN:e.optDouble("batteryStatus",Double.NaN);return Double.isNaN(n)||n<0||n>100?"—%":String.format(java.util.Locale.US,"%.0f%%",n);}
 private static String distance(JSONObject r){String value=r.optString("value","");if(value.isEmpty()||"null".equals(value))return "Unavailable";int u=r.optInt("unit",-1);return value+(u==1?" km":u==3?" mi":" (unit unavailable)");}
 static String range(JSONObject s){try{return distance(s.getJSONObject("evStatus").getJSONArray("drvDistance").getJSONObject(0).getJSONObject("rangeByFuel").getJSONObject("evModeRange"));}catch(Exception e){return "Unavailable";}}
 static String odometer(JSONObject s){JSONObject o=s.optJSONObject("odometer");return o==null?"Unavailable":distance(o);}
 static String charging(JSONObject s){JSONObject e=s.optJSONObject("evStatus");if(e==null)return "Charging unknown";String charge=e.optString("batteryCharge","");if("true".equalsIgnoreCase(charge))return "Charging";if(!"false".equalsIgnoreCase(charge))return "Charging unknown";return e.optInt("batteryPlugin")>0?"Plugged in":"Unplugged";}
 static String lock(JSONObject s){String value=s.optString("doorLock","");return "true".equalsIgnoreCase(value)?"Locked":"false".equalsIgnoreCase(value)?"Unlocked":"Lock unknown";}
 static String details(JSONObject s){return range(s)+"  •  "+charging(s)+"  •  "+lock(s);}
 static String time(JSONObject s){return "Vehicle report: "+s.optString("dateTime","time unavailable");}
}
