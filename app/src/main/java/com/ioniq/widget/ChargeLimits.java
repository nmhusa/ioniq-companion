package com.ioniq.widget;
import org.json.*;
final class ChargeLimits {
 static JSONObject body(int packed)throws JSONException {
  return new JSONObject().put("targetSOClist",new JSONArray()
   .put(new JSONObject().put("plugType",ChargeLimitValues.DC).put("targetSOClevel",ChargeLimitValues.dc(packed)))
   .put(new JSONObject().put("plugType",ChargeLimitValues.AC).put("targetSOClevel",ChargeLimitValues.ac(packed))));
 }
 static int read(JSONObject status,int plug){
  try{JSONArray items=status.getJSONObject("evStatus").getJSONObject("reservChargeInfos").getJSONArray("targetSOClist");
   for(int i=items.length()-1;i>=0;i--){JSONObject item=items.getJSONObject(i);if(item.optInt("plugType",-1)==plug){double value=item.optDouble("targetSOClevel",Double.NaN);return value==(int)value&&ChargeLimitValues.valid((int)value)?(int)value:-1;}}
  }catch(JSONException ignored){}return -1;
 }
 static String display(int value){return value<0?"Unavailable":value+"%";}
}
