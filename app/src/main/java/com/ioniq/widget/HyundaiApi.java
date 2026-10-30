package com.ioniq.widget;
// US protocol adapted from Andy Fase's MIT-licensed egmp-bluelink-scriptable.
import android.content.Context;
import java.net.URL;
import android.net.Uri;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TimeZone;
import javax.net.ssl.HttpsURLConnection;
import org.json.*;
final class HyundaiApi {
 private final Context context; private final JSONObject auth;
 HyundaiApi(Context c) throws Exception { context=c.getApplicationContext(); auth=Vault.read(context); }
 private static Map<String,String> headers(String... pairs) { Map<String,String> result=new java.util.HashMap<>(); for(int i=0;i<pairs.length;i+=2)result.put(pairs[i],pairs[i+1]);return result; }
 private static class Reply { JSONObject json; String tid; Reply(JSONObject j,String t){json=j;tid=t;} }
 private Reply request(String path,String method,JSONObject body,boolean token,boolean car,Map<String,String> extra) throws Exception {
  HttpsURLConnection h=(HttpsURLConnection)new URL("https://api.telematics.hyundaiusa.com/"+path).openConnection();
  try {
   h.setInstanceFollowRedirects(false); h.setConnectTimeout(15000); h.setReadTimeout(25000); h.setRequestMethod(method);
   if(body!=null)h.setRequestProperty("Content-Type","application/json"); h.setRequestProperty("Accept","application/json, text/plain, */*"); h.setRequestProperty("Accept-Language","en-US,en;q=0.9");
   h.setRequestProperty("from","SPA"); h.setRequestProperty("to","ISS"); h.setRequestProperty("language","0");
   int hours=TimeZone.getDefault().getOffset(System.currentTimeMillis())/3600000;
   h.setRequestProperty("offset",String.format(java.util.Locale.US,"%+d",hours));
   h.setRequestProperty("client_id","m66129Bb-em93-SPAHYN-bZ91-am4540zp19920");
   if(BuildConfig.CLIENT_MASKED.length==0)throw new Exception("This build has no Hyundai client credential. See BUILD_GUIDE.md for local configuration.");
   h.setRequestProperty("clientSecret",ClientCredential.decode(BuildConfig.CLIENT_MASKED,BuildConfig.CLIENT_PAD));
   h.setRequestProperty("username",auth.optString("email")); h.setRequestProperty("blueLinkServicePin",auth.optString("pin"));
   h.setRequestProperty("brandIndicator","H"); h.setRequestProperty("User-Agent","okhttp/3.14.9");
   if(token)h.setRequestProperty("accessToken",auth.getString("access"));
   if(car){ h.setRequestProperty("VIN",auth.getString("vin")); h.setRequestProperty("APPCLOUD-VIN",auth.getString("vin")); h.setRequestProperty("registrationId",auth.getString("regid")); h.setRequestProperty("gen",SeatClimate.generation(context,auth)); }
   for(Map.Entry<String,String> e:extra.entrySet())h.setRequestProperty(e.getKey(),e.getValue());
   if(body!=null){h.setDoOutput(true);try(java.io.OutputStream out=h.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
   int code=h.getResponseCode();
   if(code<200||code>=300) {
    String stage=path.equals("v2/ac/oauth/token")?"Sign-in":path.equals("v2/ac/oauth/token/refresh")?"Session refresh":path.startsWith("ac/v2/enrollment/")?"Vehicle list":path.endsWith("vehicleStatus")?"Vehicle status":path.endsWith("getRunningStatus")?"Command confirmation":"Remote command";
    throw new Exception(stage+" failed (HTTP "+code+"). "+(code==429?"Please wait before trying again.":code==401||code==403?"Hyundai rejected authentication. Reconnect your account.":"Hyundai did not complete this request.")+(stage.startsWith("Remote")||stage.startsWith("Command")?" Outcome may be unknown; check the official app before retrying.":""));
   }
   String raw; try(java.io.InputStream in=h.getInputStream()){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream(); byte[] buf=new byte[4096]; int n; while((n=in.read(buf))!=-1){out.write(buf,0,n);if(out.size()>2000000)throw new Exception("Unexpectedly large server response");} raw=out.toString("UTF-8");}
   JSONObject j=raw.trim().startsWith("{")?new JSONObject(raw):new JSONObject();
   if("ERROR".equalsIgnoreCase(j.optString("status")))throw new Exception("Hyundai reported an error. Check the official app.");
   return new Reply(j,h.getHeaderField("tmsTid"));
  } finally {h.disconnect();}
 }
 private void ensureToken() throws Exception {
  if(auth.optString("email").isEmpty())throw new Exception("Connect your Bluelink account first.");
  if(auth.optLong("expires")>System.currentTimeMillis()+60000&&!auth.optString("access").isEmpty())return;
  JSONObject data=new JSONObject(); String path;
  if(!auth.optString("refresh").isEmpty()){path="v2/ac/oauth/token/refresh";data.put("refresh_token",auth.getString("refresh"));}
  else {path="v2/ac/oauth/token";data.put("username",auth.getString("email")).put("password",auth.getString("password"));}
  JSONObject t=request(path,"POST",data,false,false,headers()).json;
  if(t.optString("access_token").isEmpty())throw new Exception("No access token returned. This login may require an updated API or MFA flow.");
  auth.put("access",t.getString("access_token"));auth.put("refresh",t.optString("refresh_token",auth.optString("refresh")));auth.put("expires",System.currentTimeMillis()+t.optLong("expires_in",1800)*1000);Vault.save(context,auth);
 }
 JSONArray vehicles() throws Exception { ensureToken(); return request("ac/v2/enrollment/details/"+Uri.encode(auth.getString("email"), "@"),"GET",null,true,false,headers()).json.getJSONArray("enrolledVehicleDetails"); }
 void select(JSONObject vehicle) throws Exception {auth.put("vin",vehicle.getString("vin")).put("regid",vehicle.getString("regid")).put("year",vehicle.optInt("modelYear",2023));Vault.save(context,auth);SecurePrefs.get(context,"status").edit().clear().commit();}
 private void requireVehicle() throws Exception { if(auth.optString("vin").isEmpty()||auth.optString("regid").isEmpty())throw new Exception("Setup is incomplete. Tap Connect / change Bluelink account and select your vehicle before using Refresh or controls."); }
 JSONObject status(boolean force) throws Exception {
  requireVehicle();
  ensureToken(); JSONObject s=request("ac/v2/rcs/rvs/vehicleStatus","GET",null,true,true,headers("refresh",force?"true":"false")).json.getJSONObject("vehicleStatus");
  SecurePrefs.get(context,"status").edit().putString("json",s.toString()).putLong("fetched",System.currentTimeMillis()).remove("error").commit(); return s;
 }
 JSONObject findCar() throws Exception { requireVehicle();ensureToken();return request("ac/v2/rcs/rfc/findMyCar","GET",null,true,true,headers()).json; }
 String command(String action,int temp) throws Exception {
  requireVehicle();
  ensureToken(); String path; JSONObject body=null;
  switch(action){
   case "lock": path="ac/v2/rcs/rdo/off";break;
   case "unlock": path="ac/v2/rcs/rdo/on";break;
   case "charge_start":path="ac/v2/evc/charge/start";break;
   case "charge_stop":path="ac/v2/evc/charge/stop";break;
   case "climate_stop":path="ac/v2/evc/fatc/stop";break;
   case "climate":path="ac/v2/evc/fatc/start";body=ClimatePreset.body(context,temp);break;
   case "limits":path="ac/v2/evc/charge/targetsoc/set";body=ChargeLimits.body(temp);break;
   case "lights":case "horn_lights":path=VehicleSignal.path(action);body=new JSONObject().put("userName",auth.getString("email")).put("vin",auth.getString("vin"));break;
   default:throw new Exception("Unsupported command");
  }
  if(action.equals("lock")||action.equals("unlock")||action.startsWith("charge_"))body=new JSONObject().put("userName",auth.getString("email")).put("vin",auth.getString("vin"));
  Reply reply=request(path,"POST",body,true,true,headers());
  if(reply.tid==null||reply.tid.isEmpty())return "Request sent; completion is unconfirmed. Check the official app before retrying.";
  for(int i=0;i<12;i++){
   Thread.sleep(2500);
   JSONObject j=request("ac/v2/rmt/getRunningStatus","GET",null,true,true,headers("tid",reply.tid,"login_id",auth.getString("email"),"service_type",VehicleSignal.serviceType(action))).json;
   if("SUCCESS".equals(j.optString("status"))) {try{status(false);}catch(Exception ignored){} return "Hyundai reports command completed. Vehicle status may take time to update.";}
  }
  return "Request sent; confirmation timed out. Check the official app before retrying.";
 }
}
