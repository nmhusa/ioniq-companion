package com.ioniq.widget;
import android.Manifest;import android.app.*;import android.bluetooth.*;import android.content.*;import android.content.pm.PackageManager;import android.os.*;
/** Opt-in continuous Bluetooth monitoring. Never persists or retries a pending command. */
public final class BluetoothLockService extends Service {
 static volatile BluetoothLockService instance;
 private static final String CHANNEL="bluetooth_lock",RESULTS="bluetooth_lock_results";
 private static final int NOTICE=720,RESULT=721;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final BluetoothLockRules rules=new BluetoothLockRules();
 private BluetoothAdapter adapter;private NotificationManager nm;private PowerManager.WakeLock wake;
 private boolean registered;private volatile boolean alive;private String device="",scope="",runId="";private int delay;private long eventGeneration;
 static SharedPreferences prefs(Context c){return SecurePrefs.get(c,"parking");}
 static boolean enabled(Context c){return prefs(c).getBoolean("bt_enabled",false);}
 static int delay(Context c){int n=prefs(c).getInt("bt_delay",15);return n==10||n==15||n==30||n==60||n==120?n:15;}
 static String scope(Context c){try{org.json.JSONObject v=Vault.read(c);String vin=v.optString("vin"),email=v.optString("email");return vin.isEmpty()||email.isEmpty()?"":email+"\n"+vin;}catch(Exception e){return "";}}
 static void stop(Context c){prefs(c).edit().putBoolean("bt_enabled",false).putString("bt_status","Off").commit();BluetoothLockService s=instance;if(s!=null){s.alive=false;s.rules.cancel();}c.stopService(new Intent(c,BluetoothLockService.class));}
 static boolean mayRun(Context c,Intent i){BluetoothLockService s=instance;return s!=null&&s.valid(i);}
 private boolean valid(Intent i){return alive&&"lock".equals(i.getAction())&&runId.equals(i.getStringExtra("bt_run"))&&enabled(this)&&scope.equals(scope(this))&&usable()&&rules.valid(i.getLongExtra("bt_token",-1),SystemClock.elapsedRealtime());}
 static boolean consume(Context c,Intent i){BluetoothLockService s=instance;return s!=null&&s.valid(i)&&s.rules.consume(i.getLongExtra("bt_token",-1),SystemClock.elapsedRealtime());}
 static void result(Context c,Intent i,String message){
  BluetoothLockService s=instance;if(s==null||!s.runId.equals(i.getStringExtra("bt_run")))return;
  s.handler.post(()->{boolean current=s.rules.matches(i.getLongExtra("bt_token",-1));s.rules.finish(i.getLongExtra("bt_token",-1));if(current)s.releaseWake();
   if(s.alive){if(current)s.status(s.rules.isConnected()?"Car connected · ready for next disconnect":"Last result: "+message+" · waiting for car connection");
    if(s.notificationsAllowed())s.nm.notify(RESULT,s.base(RESULTS,"IONIQ · Bluetooth lock result",message).setAutoCancel(true).build());}
  });
 }
 private boolean notificationsAllowed(){NotificationChannel a=nm.getNotificationChannel(CHANNEL),b=nm.getNotificationChannel(RESULTS);return (Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&nm.areNotificationsEnabled()&&a!=null&&b!=null&&a.getImportance()!=NotificationManager.IMPORTANCE_NONE&&b.getImportance()!=NotificationManager.IMPORTANCE_NONE;}
 private boolean usable(){try{return (Build.VERSION.SDK_INT<31||checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED)&&adapter!=null&&adapter.isEnabled()&&notificationsAllowed();}catch(SecurityException e){return false;}}
 private Notification.Builder base(String channel,String title,String detail){return new Notification.Builder(this,channel).setSmallIcon(R.drawable.ic_lock).setContentTitle(title).setContentText(detail).setStyle(new Notification.BigTextStyle().bigText(detail)).setVisibility(Notification.VISIBILITY_PRIVATE).setContentIntent(PendingIntent.getActivity(this,NOTICE,new Intent(this,ParkingActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));}
 private Notification monitor(String detail){return base(CHANNEL,"IONIQ · Bluetooth auto-lock",detail).setOngoing(true).setOnlyAlertOnce(true).addAction(new Notification.Action.Builder(null,"Turn off / cancel",PendingIntent.getService(this,NOTICE,new Intent(this,BluetoothLockService.class).setAction("stop"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE)).build()).build();}
 private void status(String text){prefs(this).edit().putString("bt_status",text).apply();if(alive&&notificationsAllowed())nm.notify(NOTICE,monitor(text));}
 private void releaseWake(){if(wake!=null&&wake.isHeld())wake.release();}
 private void fail(String text){stop(this);prefs(this).edit().putString("bt_status",text).commit();stopSelf();}
 private void connected(){rules.connected();releaseWake();status("Car connected · ready for disconnect");}
 private final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
  if(!alive)return;
  if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(i.getAction())){
   if(i.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)!=BluetoothAdapter.STATE_ON)fail("Bluetooth turned off · auto-lock stopped");return;
  }
  BluetoothDevice found=i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
  try{if(found==null||!device.equals(found.getAddress()))return;}catch(SecurityException e){fail("Nearby devices permission missing");return;}
  eventGeneration++;
  if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(i.getAction()))connected();
  else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(i.getAction())){
   if(!usable()){fail("Bluetooth or notifications unavailable · stopped");return;}
   if(rules.disconnected(SystemClock.elapsedRealtime(),delay)){
    wake.acquire((delay+65)*1000L);status("Car disconnected · locking in "+delay+" seconds");handler.removeCallbacks(tick);handler.post(tick);
   }
  }
 }};
 private final Runnable tick=new Runnable(){public void run(){
  if(!alive)return;
  if(!enabled(BluetoothLockService.this)||!scope.equals(scope(BluetoothLockService.this))||!usable()){fail("Account, Bluetooth or notification settings changed · stopped");return;}
  long now=SystemClock.elapsedRealtime(),remaining=rules.remaining(now);
  if(remaining>0)status("Locking in "+((remaining+999)/1000)+" seconds · reconnect or turn off to cancel");
  else if(remaining==0){
   long token=rules.claim(now);
   if(token<0){releaseWake();status("Countdown expired · no command sent; waiting for car connection");}
   else{
    status("Lock queued · no retries");
    try{startForegroundService(new Intent(BluetoothLockService.this,CommandService.class).setAction("lock").putExtra("bt_run",runId).putExtra("bt_token",token));}
    catch(RuntimeException e){rules.finish(token);releaseWake();status("Android blocked the lock request · open app; nothing sent");if(notificationsAllowed())nm.notify(RESULT,base(RESULTS,"IONIQ · Lock not sent","Android blocked background execution. No retry will occur.").build());}
   }
  }
  handler.postDelayed(this,rules.remaining(SystemClock.elapsedRealtime())>=0?1000:10000);
 }};
 @Override public void onCreate(){super.onCreate();nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Bluetooth auto-lock monitoring",NotificationManager.IMPORTANCE_LOW));nm.createNotificationChannel(new NotificationChannel(RESULTS,"Bluetooth auto-lock results",NotificationManager.IMPORTANCE_DEFAULT));BluetoothManager manager=getSystemService(BluetoothManager.class);adapter=manager==null?null:manager.getAdapter();wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"ioniq:bluetooth-lock-countdown");wake.setReferenceCounted(false);}
 @Override public int onStartCommand(Intent i,int flags,int id){
  if(i!=null&&"stop".equals(i.getAction())){stop(this);stopSelf();return START_NOT_STICKY;}
  // Foreground immediately even on a system restart; no previous countdown is loaded.
  try{startForeground(NOTICE,monitor("Starting Bluetooth auto-lock…"));}catch(RuntimeException e){fail("Android blocked monitoring · check Nearby devices permission");return START_NOT_STICKY;}
  if(alive)return START_STICKY;
  scope=scope(this);device=prefs(this).getString("bt_device","");delay=delay(this);
  if(!enabled(this)||scope.isEmpty()||!scope.equals(prefs(this).getString("bt_scope",""))||device.isEmpty()||!usable()){fail("Auto-lock could not start · check settings");return START_NOT_STICKY;}
  alive=true;instance=this;runId=java.util.UUID.randomUUID().toString();rules.cancel();
  try{
   IntentFilter f=new IntentFilter();f.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);f.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
   if(Build.VERSION.SDK_INT>=33)registerReceiver(receiver,f,Context.RECEIVER_EXPORTED);else registerReceiver(receiver,f);registered=true;
   status("Waiting for the selected car to connect");
   // Discover an already-connected car. Ignore a delayed snapshot after any live connection event.
   final long snapshotGeneration=eventGeneration;
   BluetoothProfile.ServiceListener listener=new BluetoothProfile.ServiceListener(){
    public void onServiceConnected(int profile,BluetoothProfile proxy){
     handler.post(()->{try{if(alive&&snapshotGeneration==eventGeneration)for(BluetoothDevice d:proxy.getConnectedDevices())if(device.equals(d.getAddress())){connected();break;}}catch(SecurityException e){fail("Nearby devices permission missing");}finally{adapter.closeProfileProxy(profile,proxy);}});
    }
    public void onServiceDisconnected(int profile){}
   };
   adapter.getProfileProxy(this,listener,BluetoothProfile.A2DP);adapter.getProfileProxy(this,listener,BluetoothProfile.HEADSET);
   handler.post(tick);
  }catch(RuntimeException e){fail("Bluetooth monitoring could not start · check permissions");return START_NOT_STICKY;}
  return START_STICKY;
 }
 @Override public void onDestroy(){alive=false;rules.cancel();if(instance==this)instance=null;handler.removeCallbacksAndMessages(null);releaseWake();if(registered)unregisterReceiver(receiver);stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}
