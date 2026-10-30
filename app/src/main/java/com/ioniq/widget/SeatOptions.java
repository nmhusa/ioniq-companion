package com.ioniq.widget;
import org.json.*;
/** US enrollment metadata. Never infer equipment or remote support from model year. */
final class SeatOptions {
 static final String[] NAMES={"Driver","Front passenger","Rear left","Rear right"};
 static final String[] KEYS={"drvSeatHeatState","astSeatHeatState","rlSeatHeatState","rrSeatHeatState"};
 static String label(int v){switch(v){case 2:return "Off";case 3:return "Cooling · low";case 4:return "Cooling · medium";case 5:return "Cooling · high";case 6:return "Heating · low";case 7:return "Heating · medium";case 8:return "Heating · high";default:return "Unavailable";}}
 static JSONObject capabilities(JSONObject details)throws Exception{
  JSONObject result=new JSONObject().put("generation",details.optInt("vehicleGeneration",0));JSONArray seats=new JSONArray();JSONObject config=details.optJSONObject("seatConfigurations");JSONArray input=config==null?null:config.optJSONArray("seatConfigs");
  boolean remote=details.optInt("vehicleGeneration",0)==3&&"E".equals(details.optString("evStatus"));
  for(int id=1;id<=4;id++){JSONArray levels=new JSONArray();JSONObject found=null;boolean duplicate=false;
   if(remote&&input!=null)for(int i=0;i<input.length();i++){JSONObject entry=input.optJSONObject(i);if(entry!=null&&entry.optInt("seatLocationID",-1)==id){if(found!=null)duplicate=true;found=entry;}}
   if(found!=null&&!duplicate){java.util.Set<String> supported=new java.util.HashSet<>(java.util.Arrays.asList(found.optString("supportedLevels").replace(" ","").split(",")));boolean heat="YES".equalsIgnoreCase(found.optString("heatingCapable")),cool="YES".equalsIgnoreCase(found.optString("ventCapable"));if(supported.contains("2")){levels.put(2);for(int code=3;code<=8;code++)if(supported.contains(String.valueOf(code))&&(code<=5?cool:heat))levels.put(code);}if(levels.length()<2)levels=new JSONArray();}
   seats.put(levels);
  }return result.put("seats",seats);
 }
 static JSONArray levels(JSONObject caps,int seat){JSONArray seats=caps.optJSONArray("seats"),levels=seats==null?null:seats.optJSONArray(seat);return levels==null?new JSONArray():levels;}
 static boolean any(JSONObject caps){for(int i=0;i<4;i++)if(levels(caps,i).length()>0)return true;return false;}
 static boolean allowed(JSONObject caps,int seat,int code){JSONArray list=levels(caps,seat);for(int i=0;i<list.length();i++)if(list.optInt(i,-1)==code)return true;return false;}
 static JSONObject command(JSONObject caps,JSONObject saved)throws Exception{
  if(!saved.optBoolean("enabled")||!any(caps))return null;JSONArray values=saved.optJSONArray("values");JSONObject result=new JSONObject();
  for(int i=0;i<4;i++){int code=values==null?2:values.optInt(i,2);if(levels(caps,i).length()>0){if(!allowed(caps,i,code))throw new Exception("Seat capabilities changed. Review your climate preset.");result.put(KEYS[i],code);}else result.put(KEYS[i],0);}return result;
 }
}
