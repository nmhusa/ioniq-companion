package com.ioniq.widget;
import android.content.Context;
import org.json.*;
/** Local encrypted account vehicle list and per-VIN display names. No authentication data. */
final class VehicleGarage {
 static String account(Context c){try{return Vault.read(c).optString("email","");}catch(Exception e){return "";}}
 static String vin(Context c){try{return Vault.read(c).optString("vin","");}catch(Exception e){return "";}}
 static String scope(Context c){try{JSONObject a=Vault.read(c);String email=a.optString("email"),vin=a.optString("vin");return email.isEmpty()||vin.isEmpty()?"":email+"\n"+vin;}catch(Exception e){return "";}}
 static void save(Context c,JSONArray source)throws Exception{
  String email=account(c);if(email.isEmpty())return;JSONArray clean=new JSONArray();java.util.HashSet<String> seen=new java.util.HashSet<>();
  for(int i=0;i<source.length()&&clean.length()<50;i++){JSONObject item=source.optJSONObject(i),v=item==null?null:item.optJSONObject("vehicleDetails");if(v==null||v.optString("vin").isEmpty()||v.optString("regid").isEmpty()||!seen.add(v.optString("vin")))continue;
   JSONObject keep=new JSONObject();for(String key:new String[]{"vin","regid","modelYear","modelCode","modelName","trimName","trimDescription","exteriorColorCode","exteriorColor","exteriorColorName","paintCode"})keep.put(key,v.optString(key,""));clean.put(new JSONObject().put("vehicleDetails",keep));}
  SecurePrefs.get(c,"garage").edit().putString("account",email).putString("vehicles",clean.toString()).commit();
 }
 static JSONArray vehicles(Context c){try{String email=account(c);if(!email.isEmpty()&&email.equals(SecurePrefs.get(c,"garage").getString("account","")))return new JSONArray(SecurePrefs.get(c,"garage").getString("vehicles","[]"));}catch(Exception ignored){}return new JSONArray();}
 static String nickname(Context c,String vin){if(vin.isEmpty()||account(c).isEmpty())return "";return SecurePrefs.get(c,"vehicle_names").getString(account(c)+"\n"+vin,"");}
 static void rename(Context c,String name)throws Exception{String vin=vin(c),email=account(c);if(vin.isEmpty()||email.isEmpty())throw new Exception("Connect and select a vehicle first.");name=name.trim();if(name.length()>40)throw new Exception("Use 40 characters or fewer.");if(!SecurePrefs.get(c,"vehicle_names").edit().putString(email+"\n"+vin,name).commit())throw new Exception("Could not save the name.");}
 static String label(Context c,JSONObject v){String vin=v.optString("vin"),nick=nickname(c,vin),model=ModelName.title(ModelName.number(v.optString("modelCode"),v.optString("modelName")));return (nick.isEmpty()?model:nick+" · "+model)+" · "+v.optString("modelYear")+" · …"+vin.substring(Math.max(0,vin.length()-6));}
 static void clear(Context c){SecurePrefs.get(c,"vehicle_appearance").edit().clear().commit();SecurePrefs.get(c,"garage").edit().clear().commit();SecurePrefs.get(c,"vehicle_names").edit().clear().commit();}
}
