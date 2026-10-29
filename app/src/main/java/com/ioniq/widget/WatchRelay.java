package com.ioniq.widget;
import android.content.*;
import com.google.android.gms.wearable.Wearable;
import com.ioniq.common.WatchProtocol;
import org.json.*;
import java.nio.charset.StandardCharsets;
/** Commands are transient messages; only results and duplicate-request receipts are persisted. */
final class WatchRelay {
 private static String activeId="";
 private static SharedPreferences prefs(Context c){return SecurePrefs.get(c,"watch_bridge");}
 static boolean enabled(Context c){return prefs(c).getBoolean("enabled",false);}
 private static String scope(Context c){try{JSONObject a=Vault.read(c);String vin=a.optString("vin"),email=a.optString("email");return vin.isEmpty()||email.isEmpty()?"":email+"\n"+vin;}catch(Exception e){return "";}}
 static synchronized boolean enable(Context c){String scope=scope(c);if(scope.isEmpty())return false;return prefs(c).edit().clear().putBoolean("enabled",true).putString("scope",scope).putString("session",java.util.UUID.randomUUID().toString()).commit();}
 static synchronized void disable(Context c){if(!prefs(c).edit().clear().commit())throw new IllegalStateException("Could not disable watch controls.");activeId="";}
 static synchronized String session(Context c){SharedPreferences p=prefs(c);return enabled(c)&&!scope(c).isEmpty()&&scope(c).equals(p.getString("scope",""))?p.getString("session",""):"";}
 static void reply(Context c,String node,String path,JSONObject data){if(node==null||node.isEmpty())return;try{Wearable.getMessageClient(c).sendMessage(node,path,data.toString().getBytes(StandardCharsets.UTF_8)).addOnFailureListener(e->{});}catch(Exception ignored){}}
 private static JSONObject records(Context c)throws Exception{return new JSONObject(prefs(c).getString("records","{}"));}
 private static void save(Context c,JSONObject records)throws Exception{if(!prefs(c).edit().putString("records",records.toString()).commit())throw new Exception("Watch receipt could not be saved");}
 static JSONObject response(String id,String state,String detail){JSONObject j=new JSONObject();try{j.put("id",id).put("state",state).put("detail",detail);}catch(Exception ignored){}return j;}
 static synchronized JSONObject snapshot(Context c,String query)throws Exception{
  String session=session(c);JSONObject out=new JSONObject().put("query",query).put("enabled",!session.isEmpty()).put("session",session);
  if(session.isEmpty())return out.put("detail","Enable Watch controls in the phone app’s Settings & help.");
  JSONObject s=new JSONObject(SecurePrefs.get(c,"status").getString("json","{}"));
  return out.put("title",VehicleIdentity.title(c)).put("battery",StatusText.battery(s)).put("range",StatusText.range(s)).put("locked",StatusText.lock(s)).put("charging",StatusText.charging(s)).put("report",s.optString("dateTime","Unavailable"))
   .put("fetched",SecurePrefs.get(c,"status").getLong("fetched",0)).put("temp",c.getSharedPreferences("ui",0).getInt("temp",72)).put("unlockProtected",UnlockGuard.enabled(c)).put("busy",CommandService.running||!activeId.isEmpty());
 }
 /** Returns a saved/rejection result; null means exactly one caller may dispatch this new request. */
 static synchronized JSONObject claim(Context c,String node,JSONObject request,long now)throws Exception{
  String id=request.optString("id"),action=request.optString("action"),session=request.optString("session");
  if(!WatchProtocol.validId(id)||!WatchProtocol.allowed(action))return response(id,"blocked","Unsupported watch request. Update both apps.");
  if(session.isEmpty()||!session.equals(session(c)))return response(id,"blocked","Phone account or watch permission changed. Sync the watch again.");
  JSONObject all=records(c),old=all.optJSONObject(id);
  if(old!=null)return result(c,node,id,session);
  java.util.ArrayList<String> remove=new java.util.ArrayList<>();java.util.Iterator<String> keys=all.keys();while(keys.hasNext()){String k=keys.next();if(all.getJSONObject(k).optLong("created")<now-3600000&&!k.equals(activeId))remove.add(k);}for(String k:remove)all.remove(k);
  if(all.length()>=128){disable(c);return response(id,"blocked","Watch request limit reached. Re-enable Watch controls on the phone.");}
  String rejected=null;
  if(!WatchProtocol.fresh(request.optLong("sent"),now))rejected="Request expired. Nothing was sent. Check watch and phone clocks.";
  else if(CommandService.running||!activeId.isEmpty())rejected="Another command is in progress. Nothing else was sent.";
  else if("unlock".equals(action)&&UnlockGuard.enabled(c))rejected="Unlock protection is on. Use Unlock doors on your phone to authenticate.";
  if(rejected!=null){all.put(id,new JSONObject(request.toString()).put("node",node).put("created",now).put("state","blocked").put("detail",rejected));save(c,all);return response(id,"blocked",rejected);}
  JSONObject record=new JSONObject(request.toString()).put("node",node).put("created",now).put("state","queued").put("detail","Phone received your request.");all.put(id,record);save(c,all);activeId=id;return null;
 }
 static synchronized boolean mayRun(Context c,Intent intent){try{
  String id=intent.getStringExtra("watch_id"),session=intent.getStringExtra("watch_session"),node=intent.getStringExtra("watch_node");
  JSONObject r=records(c).optJSONObject(id);return r!=null&&id.equals(activeId)&&session!=null&&!session.isEmpty()&&session.equals(session(c))&&node.equals(r.optString("node"))&&intent.getAction().equals(r.optString("action"))&&WatchProtocol.fresh(r.optLong("sent"),System.currentTimeMillis());
 }catch(Exception e){return false;}}
 static synchronized JSONObject result(Context c,String node,String id,String session)throws Exception{
  if(session.isEmpty()||!session.equals(session(c)))return response(id,"blocked","Watch connection changed. Sync again; check any earlier command on the phone.");
  JSONObject all=records(c),r=all.optJSONObject(id);if(r==null||!node.equals(r.optString("node")))return response(id,"unknown","No recorded result. Check the phone before trying again.");
  if(WatchProtocol.pending(r.optString("state"))&&!id.equals(activeId)){r.put("state","unknown").put("detail","Phone restarted before confirmation. Check the official app before retrying.");save(c,all);}
  return response(id,r.optString("state"),r.optString("detail"));
 }
 static synchronized void update(Context c,Intent intent,String state,String detail){String id=intent.getStringExtra("watch_id");if(id==null)return;
  try{JSONObject all=records(c),r=all.optJSONObject(id);if(r!=null){r.put("state",state).put("detail",detail);save(c,all);}}catch(Exception ignored){}
  if(!WatchProtocol.pending(state)&&id.equals(activeId))activeId="";
  reply(c,intent.getStringExtra("watch_node"),WatchProtocol.RESULT,response(id,state,detail));
 }
}
