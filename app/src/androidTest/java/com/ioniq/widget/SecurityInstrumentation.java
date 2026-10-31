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
  SharedPreferences settings=SecurePrefs.get(c,"security"),actions=SecurePrefs.get(c,"actions");check(settings.edit().clear().commit());check(UnlockGuard.enabled(c));
  check(settings.getInt("unlock_policy",0)==1);
  check(settings.edit().clear().putBoolean("protect_unlock",false).commit());check(UnlockGuard.enabled(c));
  check(UnlockGuard.setAuthenticated(c,false));check(!UnlockGuard.enabled(c));check(!UnlockGuard.enabled(c));
  check(UnlockGuard.setAuthenticated(c,true));check(UnlockGuard.enabled(c));
  Map<String,?> before=actions.getAll();c.startForegroundService(new Intent(c,CommandService.class).setAction("unlock"));android.os.SystemClock.sleep(1000);
  check(!CommandService.running);check(before.equals(actions.getAll()));settings.edit().clear().commit();
  watchChecks(c);
  garageChecks(c);
  appearanceChecks(c);
  AuxBatteryTest.main(new String[0]);
  SeatOptionsTest.main(new String[0]);
  seatChecks(c);
  check(Double.isNaN(AuxBattery.percent(new org.json.JSONObject().put("evStatus",new org.json.JSONObject().put("batteryStatus",80)))));
  check(AuxBattery.percent(new org.json.JSONObject().put("battery",new org.json.JSONObject().put("batSoc",65)))==65);
  check(!pm.getServiceInfo(new ComponentName(c,BluetoothLockService.class),0).exported);
  check(!pm.getReceiverInfo(new ComponentName(c,BluetoothLockBoot.class),0).exported);
  check(!pm.getActivityInfo(new ComponentName(c,ParkingActivity.class),0).exported);
  check((pm.getServiceInfo(new ComponentName(c,BluetoothLockService.class),0).getForegroundServiceType() & ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)!=0);
  Intent stale=new Intent(c,CommandService.class).setAction("lock").putExtra("bt_token",123L).putExtra("bt_run","stale");
  check(!BluetoothLockService.mayRun(c,stale));check(!BluetoothLockService.consume(c,stale));
  before=actions.getAll();c.startForegroundService(stale);android.os.SystemClock.sleep(1000);
  check(!CommandService.running);check(before.equals(actions.getAll()));
  java.util.concurrent.atomic.AtomicReference<Throwable> uiError=new java.util.concurrent.atomic.AtomicReference<>();
  android.app.Activity setup=startActivitySync(new Intent(c,ParkingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
  runOnMainSync(()->{try{check(setup!=null);setup.finish();}catch(Throwable e){uiError.set(e);}});
  if(uiError.get()!=null)throw new AssertionError("Bluetooth setup screen failed");

  result.putString("security_result","PASS: "+checks+" Android storage and component checks");finish(-1,result);
 }catch(Throwable e){result.putString("security_result","FAIL: "+e.getClass().getSimpleName()+" after "+checks+" checks");finish(0,result);}}
 private android.view.View findText(android.view.View view,String label){if(view instanceof android.widget.TextView&&label.contentEquals(((android.widget.TextView)view).getText()))return view;if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++){android.view.View found=findText(group.getChildAt(i),label);if(found!=null)return found;}}return null;}
 private void seatChecks(Context c)throws Exception{
  org.json.JSONObject original=Vault.read(c);try{
   org.json.JSONObject auth=new org.json.JSONObject().put("email","seats@example.invalid").put("vin","SEAT-A");Vault.save(c,auth);
   org.json.JSONObject details=new org.json.JSONObject().put("vin","SEAT-A").put("evStatus","E").put("vehicleGeneration",3).put("seatConfigurations",new org.json.JSONObject().put("seatConfigs",new org.json.JSONArray().put(SeatOptionsTest.seat(1,"YES","YES","2,3,4,5,6,7,8"))));
   VehicleIdentity.save(c,details);check(SeatClimate.generation(c,auth).equals("3"));String key=VehicleGarage.scope(c)+"\n"+NamedClimate.active(c);SecurePrefs.get(c,"seat_presets").edit().putString(key,new org.json.JSONObject().put("enabled",true).put("values",new org.json.JSONArray().put(5).put(0).put(0).put(0)).toString()).commit();
   check(ClimatePreset.body(c,72).getJSONObject("seatHeaterVentInfo").getInt("drvSeatHeatState")==5);check(ClimatePreset.body(c,72).getInt("igniOnDuration")==5);
   Vault.save(c,new org.json.JSONObject().put("email","seats@example.invalid").put("vin","SEAT-B"));check(!ClimatePreset.body(c,72).has("seatHeaterVentInfo"));
   Vault.save(c,new org.json.JSONObject().put("email","different@example.invalid").put("vin","SEAT-A"));check(SeatClimate.saved(c,NamedClimate.active(c)).length()==0);
   Vault.save(c,auth);details.put("vehicleGeneration",2);VehicleIdentity.save(c,details);check(!ClimatePreset.body(c,72).has("seatHeaterVentInfo"));VehicleGarage.clear(c);check(SecurePrefs.get(c,"seat_presets").getAll().isEmpty());
  }finally{VehicleGarage.clear(c);SecurePrefs.get(c,"vehicle_identity").edit().clear().commit();Vault.save(c,original);}
 }
 private void appearanceChecks(Context c)throws Exception{
  org.json.JSONObject saved=Vault.read(c);try{
   org.json.JSONArray groups=PaintCatalog.groups(c);check(groups.length()==13);java.util.HashSet<String> images=new java.util.HashSet<>();
   for(int i=0;i<groups.length();i++){org.json.JSONObject g=groups.getJSONObject(i);org.json.JSONArray colors=g.getJSONArray("colors");for(int x=0;x<colors.length();x++){String asset=colors.getJSONObject(x).getString("asset");if(images.add(asset))try(java.io.InputStream in=c.getAssets().open(asset)){android.graphics.Bitmap bitmap=android.graphics.BitmapFactory.decodeStream(in);check(bitmap!=null&&bitmap.hasAlpha()&&bitmap.getWidth()>=500);check(android.graphics.Color.alpha(bitmap.getPixel(0,0))==0);bitmap.recycle();}}}check(images.size()==50);
   org.json.JSONObject auth=new org.json.JSONObject().put("email","paint@example.invalid").put("vin","PAINT-A");Vault.save(c,auth);VehicleGarage.clear(c);
   org.json.JSONObject details=new org.json.JSONObject().put("vin","PAINT-A").put("modelName","IONIQ 9").put("modelYear","2026").put("exteriorColorCode","NH5");VehicleIdentity.save(c,details);check(VehicleAppearance.asset(c).equals("paint_icons/i9_nh5.webp"));String scope=VehicleGarage.scope(c);
   VehicleAppearance.save(c,scope,"i9_2026_standard","i9_swp");check(VehicleAppearance.asset(c).equals("paint_icons/i9_swp.webp"));
   Vault.save(c,new org.json.JSONObject().put("email","paint@example.invalid").put("vin","PAINT-B"));VehicleIdentity.save(c,new org.json.JSONObject().put("vin","PAINT-B").put("modelName","IONIQ 6").put("modelYear","2026").put("exteriorColorCode","A2B"));check(VehicleAppearance.asset(c).isEmpty());check(PaintCatalog.defaultGroup(c,VehicleIdentity.read(c))==null);
   boolean rejected=false;try{VehicleAppearance.save(c,scope,"i9_2026_standard","i9_swp");}catch(Exception expected){rejected=true;}check(rejected);
   VehicleAppearance.save(c,VehicleGarage.scope(c),"i6_2025_standard","i6_w6h");check(VehicleAppearance.asset(c).equals("paint_icons/i6_w6h.webp"));
   Vault.save(c,auth);VehicleIdentity.save(c,details);check(VehicleAppearance.asset(c).equals("paint_icons/i9_swp.webp"));
   Vault.save(c,new org.json.JSONObject().put("email","other@example.invalid").put("vin","PAINT-A"));check(VehicleAppearance.selection(c).length()==0);Vault.save(c,auth);
   VehicleAppearance.save(c,scope,"","");check(VehicleAppearance.asset(c).equals("paint_icons/i9_nh5.webp"));details.put("exteriorColorCode","unknown");VehicleIdentity.save(c,details);check(VehicleAppearance.asset(c).isEmpty());
   check(PaintCatalog.match(PaintCatalog.group(c,"i9_2026_standard"),new org.json.JSONArray().put("NH5").put("SWP"))==null);
   check(PaintCatalog.match(PaintCatalog.group(c,"i5_2022_standard"),new org.json.JSONArray().put("A2B"))==null);
   check(PaintCatalog.match(PaintCatalog.group(c,"i5_2022_standard"),new org.json.JSONArray().put("Digital Teal")).getString("id").equals("i5_early_digital_teal"));
   VehicleAppearance.save(c,scope,"i9_2026_standard","i9_swp");VehicleGarage.clear(c);check(VehicleAppearance.selection(c).length()==0);
   check(!c.getPackageManager().getActivityInfo(new ComponentName(c,AppearanceActivity.class),0).exported);
   android.app.Activity editor=startActivitySync(new Intent(c,AppearanceActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();java.util.concurrent.atomic.AtomicReference<Throwable> error=new java.util.concurrent.atomic.AtomicReference<>();
   runOnMainSync(()->{try{
    android.view.View root=editor.getWindow().getDecorView();android.view.View paint=findText(root,"Snow White Pearl"),save=findText(root,"Save");check(paint!=null&&save!=null);paint.performClick();save.performClick();check(VehicleAppearance.asset(c).equals("paint_icons/i9_swp.webp"));
   }catch(Throwable e){error.set(e);}finally{editor.finish();}});if(error.get()!=null)throw new AssertionError("Appearance UI selection failed",error.get());
  }finally{VehicleGarage.clear(c);SecurePrefs.get(c,"vehicle_identity").edit().clear().commit();Vault.save(c,saved);}
 }
 private void garageChecks(Context c)throws Exception{
  org.json.JSONObject saved=Vault.read(c);java.util.concurrent.CountDownLatch release=new java.util.concurrent.CountDownLatch(1);
  try{
   org.json.JSONObject auth=new org.json.JSONObject().put("email","garage@example.invalid").put("vin","TEST-CAR-A").put("regid","reg-A");Vault.save(c,auth);VehicleGarage.clear(c);
   org.json.JSONObject first=new org.json.JSONObject().put("vin","TEST-CAR-A").put("regid","reg-A").put("modelName","IONIQ 6").put("modelYear","2023").put("private_unneeded","not stored");
   org.json.JSONObject second=new org.json.JSONObject().put("vin","TEST-CAR-B").put("regid","reg-B").put("modelCode","IONIQ 9").put("modelYear","2026");
   org.json.JSONArray cars=new org.json.JSONArray().put(new org.json.JSONObject().put("vehicleDetails",first)).put(new org.json.JSONObject().put("vehicleDetails",second));
   VehicleGarage.save(c,cars);check(VehicleGarage.vehicles(c).length()==2);check(!VehicleGarage.vehicles(c).toString().contains("private_unneeded"));
   VehicleIdentity.save(c,first);VehicleGarage.rename(c,"  Comet  ");check(VehicleIdentity.title(c).equals("Comet"));check(VehicleGarage.label(c,first).contains("Comet"));
   String scope=VehicleGarage.scope(c);new HyundaiApi(c).select(second);VehicleIdentity.save(c,second);check(!scope.equals(VehicleGarage.scope(c)));check(VehicleIdentity.title(c).equals("IONIQ 9"));check(VehicleIdentity.read(c).optInt("model")==9);VehicleGarage.rename(c,"Atlas");
   new HyundaiApi(c).select(first);VehicleIdentity.save(c,first);check(VehicleIdentity.title(c).equals("Comet"));check(VehicleGarage.nickname(c,"TEST-CAR-B").equals("Atlas"));VehicleGarage.rename(c,"");check(VehicleIdentity.title(c).equals("IONIQ 6"));
   Vault.save(c,new org.json.JSONObject().put("email","different@example.invalid").put("vin","TEST-CAR-B"));check(VehicleGarage.vehicles(c).length()==0);check(VehicleGarage.nickname(c,"TEST-CAR-B").isEmpty());
   Vault.save(c,auth);
   // Block the queue, enqueue a vehicle change, then receive a lock for the old vehicle.
   java.util.concurrent.CountDownLatch blocked=new java.util.concurrent.CountDownLatch(1);
   RefreshJob.QUEUE.execute(()->{blocked.countDown();try{release.await(10,java.util.concurrent.TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}});
   check(blocked.await(3,java.util.concurrent.TimeUnit.SECONDS));
   RefreshJob.QUEUE.execute(()->{try{new HyundaiApi(c).select(second);}catch(Exception e){throw new RuntimeException(e);}});
   c.startForegroundService(new Intent(c,CommandService.class).setAction("lock"));
   long until=android.os.SystemClock.elapsedRealtime()+3000;while(!CommandService.running&&android.os.SystemClock.elapsedRealtime()<until)android.os.SystemClock.sleep(50);check(CommandService.running);release.countDown();
   until=android.os.SystemClock.elapsedRealtime()+5000;while(CommandService.running&&android.os.SystemClock.elapsedRealtime()<until)android.os.SystemClock.sleep(50);check(!CommandService.running);check(SecurePrefs.get(c,"actions").getString("lock_detail","").equals("Vehicle changed. Nothing was sent."));
  }finally{release.countDown();VehicleGarage.clear(c);SecurePrefs.get(c,"vehicle_identity").edit().clear().commit();SecurePrefs.get(c,"actions").edit().clear().commit();Vault.save(c,saved);}
 }
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
