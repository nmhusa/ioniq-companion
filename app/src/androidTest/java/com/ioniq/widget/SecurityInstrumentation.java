package com.ioniq.widget;
import android.app.Instrumentation;
import android.content.*;
import android.content.pm.*;
import android.os.Bundle;
import java.util.*;
public final class SecurityInstrumentation extends Instrumentation {
 private int checks;
 private void check(boolean condition){checks++;if(!condition)throw new AssertionError("Check "+checks+" failed");}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  Context c=getTargetContext();String store="security_test_"+System.nanoTime();
  SharedPreferences legacy=c.getSharedPreferences(store,0);
  check(legacy.edit().putString("private","VIN-ONLY-TEST-44.0,-73.0").putInt("int",3).putLong("long",9999999999L).putBoolean("bool",true).putFloat("float",1.5f).putStringSet("set",new HashSet<>(Arrays.asList("a","b"))).commit());
  SharedPreferences secure=SecurePrefs.get(c,store);
  check(legacy.getAll().isEmpty());check(secure.getString("private","").contains("VIN-ONLY-TEST"));check(secure.getInt("int",0)==3);check(secure.getLong("long",0)==9999999999L);check(secure.getBoolean("bool",false));check(secure.getFloat("float",0)==1.5f);check(secure.getStringSet("set",Collections.emptySet()).size()==2);
  SharedPreferences disk=c.getSharedPreferences("encrypted_"+store,0);String encoded=disk.getString("payload","");check(!encoded.contains("VIN-ONLY-TEST"));check(disk.getAll().size()==1);
  SharedPreferences.Editor a=secure.edit().putString("a","one"),b=secure.edit().putString("b","two");check(a.commit());check(b.commit());check(secure.contains("a")&&secure.contains("b"));check(!disk.getString("payload","").equals(encoded));
  // A second instance must decrypt the persisted data with Android Keystore (not an in-memory round-trip).
  java.lang.reflect.Constructor<SecurePrefs> constructor=SecurePrefs.class.getDeclaredConstructor(Context.class,String.class);constructor.setAccessible(true);
  SharedPreferences reopened=constructor.newInstance(c,store);check(reopened.getLong("long",0)==9999999999L);check(reopened.getString("b","").equals("two"));
  check(secure.edit().remove("private").commit());check(!secure.contains("private"));check(secure.edit().clear().commit());check(secure.getAll().isEmpty());
  // Authentication-tag failure must never silently fall back to a legacy plaintext value.
  String corrupt=store+"_corrupt";c.getSharedPreferences(corrupt,0).edit().putString("private","DO-NOT-FALL-BACK").commit();c.getSharedPreferences("encrypted_"+corrupt,0).edit().putString("payload","invalid").commit();
  boolean rejected=false;try{SecurePrefs.get(c,corrupt);}catch(IllegalStateException expected){rejected=true;}check(rejected);
  PackageManager pm=c.getPackageManager();check(!pm.getActivityInfo(new ComponentName(c,MainActivity.class),0).exported);check(!pm.getActivityInfo(new ComponentName(c,UnlockActivity.class),0).exported);check(pm.getActivityInfo(new ComponentName(c,LauncherActivity.class),0).exported);check(!pm.getServiceInfo(new ComponentName(c,CommandService.class),0).exported);
  // A stale widget PendingIntent without a fresh authentication permit must send nothing.
  SharedPreferences settings=SecurePrefs.get(c,"security"),actions=SecurePrefs.get(c,"actions");settings.edit().putBoolean("protect_unlock",true).commit();
  Map<String,?> before=actions.getAll();c.startForegroundService(new Intent(c,CommandService.class).setAction("unlock"));android.os.SystemClock.sleep(1000);
  check(!CommandService.running);check(before.equals(actions.getAll()));settings.edit().clear().commit();
  watchChecks(c);
  result.putString("security_result","PASS: "+checks+" Android storage and component checks");finish(-1,result);
 }catch(Throwable e){result.putString("security_result","FAIL: "+e.getClass().getSimpleName()+" after "+checks+" checks");finish(0,result);}}
 private org.json.JSONObject request(String session,String action,long now)throws Exception{return new org.json.JSONObject().put("id",java.util.UUID.randomUUID().toString()).put("session",session).put("action",action).put("sent",now);}
 private void watchChecks(Context c)throws Exception{
  org.json.JSONObject saved=Vault.read(c);boolean protectedUnlock=UnlockGuard.enabled(c);
  try{WatchRelay.disable(c);Vault.save(c,new org.json.JSONObject().put("email","test@example.invalid").put("vin","TESTVIN-NOT-A-CAR"));
   long now=System.currentTimeMillis();check(WatchRelay.claim(c,"test-node",request("","lock",now),now)!=null);
   check(WatchRelay.enable(c));String session=WatchRelay.session(c);check(com.ioniq.common.WatchProtocol.validId(session));
   String snapshot=WatchRelay.snapshot(c,java.util.UUID.randomUUID().toString()).toString();check(!snapshot.contains("test@example.invalid")&&!snapshot.contains("TESTVIN-NOT-A-CAR"));
   check(WatchRelay.claim(c,"test-node",request(session,"erase",now),now)!=null);check(WatchRelay.claim(c,"test-node",request(session,"lock",now-60001),now)!=null);
   org.json.JSONObject first=request(session,"lock",now);String id=first.getString("id");check(WatchRelay.claim(c,"test-node",first,now)==null);
   check(WatchRelay.claim(c,"test-node",first,now).getString("state").equals("queued"));check(WatchRelay.claim(c,"test-node",request(session,"unlock",now),now)!=null);
   check(WatchRelay.result(c,"other-node",id,session).getString("state").equals("unknown"));
   Intent command=new Intent(c,CommandService.class).setAction("lock").putExtra("watch_id",id).putExtra("watch_session",session).putExtra("watch_node","test-node");
   check(WatchRelay.mayRun(c,command));check(!WatchRelay.mayRun(c,new Intent(command).setAction("unlock")));check(!WatchRelay.mayRun(c,new Intent(command).putExtra("watch_node","other-node")));
   WatchRelay.update(c,command,"confirmed","Synthetic test; no vehicle request");check(!WatchRelay.mayRun(c,command));check(WatchRelay.claim(c,"test-node",first,now).getString("state").equals("confirmed"));
   SecurePrefs.get(c,"security").edit().putBoolean("protect_unlock",true).commit();org.json.JSONObject blockedUnlock=request(session,"unlock",now);check(WatchRelay.claim(c,"test-node",blockedUnlock,now).getString("state").equals("blocked"));
   SecurePrefs.get(c,"security").edit().putBoolean("protect_unlock",false).commit();check(WatchRelay.claim(c,"test-node",blockedUnlock,now).getString("state").equals("blocked"));org.json.JSONObject interrupted=request(session,"lock",now);check(WatchRelay.claim(c,"test-node",interrupted,now)==null);
   java.lang.reflect.Field active=WatchRelay.class.getDeclaredField("activeId");active.setAccessible(true);active.set(null,"");check(WatchRelay.claim(c,"test-node",interrupted,now).getString("state").equals("unknown"));
   WatchRelay.disable(c);check(WatchRelay.session(c).isEmpty());check(WatchRelay.enable(c));check(!session.equals(WatchRelay.session(c)));check(WatchRelay.claim(c,"test-node",first,now).getString("state").equals("blocked"));
   Vault.save(c,new org.json.JSONObject().put("email","test@example.invalid").put("vin","DIFFERENT-VEHICLE"));check(WatchRelay.session(c).isEmpty());
  }finally{WatchRelay.disable(c);Vault.save(c,saved);SecurePrefs.get(c,"security").edit().putBoolean("protect_unlock",protectedUnlock).commit();}
 }

}
