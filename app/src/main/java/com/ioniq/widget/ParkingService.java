package com.ioniq.widget;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;

/** User-started location foreground service; deliberately never restarts itself. */
public class ParkingService extends Service implements LocationListener {
 static volatile boolean active;
 private static volatile long generation;
 private static final String CHANNEL="parking", ALERTS="parking_alerts";
 private static final int NOTICE=710, RETURN_NOTICE=711, RESULT_NOTICE=712;
 private LocationManager locations;
 private volatile Location latest, anchor;
 private volatile boolean canSend;
 private ParkingRules rules;
 private boolean registered, awaitingFix, monitorBluetooth;
 private long startedAt, requestAt;
 private String device="";
 private final Handler handler=new Handler(Looper.getMainLooper());
 private NotificationManager notifications;
 private long session;
 private final Runnable watchdog=new Runnable(){public void run(){
  if(!active)return;
  if(SystemClock.elapsedRealtime()-startedAt>24*60*60*1000L){setStatus("Session expired. Start a new session.");stopSelf();return;}
  if(!permissionsValid() || !locations.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
   anchor=null;rules=null;awaitingFix=false;setStatus("Monitoring stopped: location, Bluetooth or notifications unavailable.");stopSelf();return;
  }
  if(latest==null || !fresh(latest)){canSend=false;if(rules!=null)rules.interrupt();setStatus("Waiting for an accurate GPS fix. Locking is paused.");}
  if(awaitingFix && SystemClock.elapsedRealtime()-requestAt>60000){awaitingFix=false;setStatus("Could not mark parking. Go outdoors, then tap I’ve parked here.");}
  handler.postDelayed(this,10000);
 }};
 private final BroadcastReceiver bluetooth=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
  if(!monitorBluetooth)return;
  String action=i.getAction();
  if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
   if(i.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)!=BluetoothAdapter.STATE_ON){anchor=null;rules=null;awaitingFix=false;setStatus("Bluetooth switched off. Parking cancelled; arm again when parked.");}return;
  }
  BluetoothDevice found=i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
  if(found==null || !found.getAddress().equals(device))return;
  if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
   generation++;session=generation;anchor=null;rules=null;awaitingFix=false;notifications.cancel(RETURN_NOTICE);setStatus("Car / dongle connected. Waiting for parking disconnect.");
  }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
   // Ignore duplicate disconnects after arming and require recent measured low speed.
   if(rules!=null)return;
   if(latest!=null && fresh(latest) && latest.hasSpeed() && latest.getSpeed()<1.5f)arm(latest);
   else setStatus("Disconnect detected, but no reliable parking fix. Tap I’ve parked here when parked.");
  }
 }};
 static void stop(Context c){generation++;active=false;c.stopService(new Intent(c,ParkingService.class));NotificationManager n=c.getSystemService(NotificationManager.class);n.cancel(RETURN_NOTICE);c.getSharedPreferences("parking",0).edit().putString("status","Stopped. Pending parking actions cancelled.").apply();}
 private boolean permissionsValid(){
  return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED && (!monitorBluetooth || Build.VERSION.SDK_INT<31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED) && (Build.VERSION.SDK_INT<33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) && notifications.areNotificationsEnabled() && notifications.getNotificationChannel(CHANNEL).getImportance()!=NotificationManager.IMPORTANCE_NONE && notifications.getNotificationChannel(ALERTS).getImportance()!=NotificationManager.IMPORTANCE_NONE;
 }
 private boolean fresh(Location l){long age=SystemClock.elapsedRealtimeNanos()-l.getElapsedRealtimeNanos();return age>=0 && age<30000000000L && l.hasAccuracy() && l.getAccuracy()<=(ParkingStart.radius(this)<=30?5:20) && !l.isFromMockProvider();}
 private PendingIntent open(){return PendingIntent.getActivity(this,710,new Intent(this,ParkingActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 private Notification.Builder base(String channel,String title,String text){return new Notification.Builder(this,channel).setSmallIcon(R.drawable.ic_lock).setContentTitle(title).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(open()).setVisibility(Notification.VISIBILITY_PRIVATE);}
 private Notification monitor(String text){PendingIntent stop=PendingIntent.getService(this,710,new Intent(this,ParkingService.class).setAction("stop"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);return base(CHANNEL,"IONIQ · Park & return",text).setOngoing(true).setOnlyAlertOnce(true).addAction(new Notification.Action.Builder(null,"Stop / cancel",stop).build()).build();}
 private void setStatus(String value){getSharedPreferences("parking",0).edit().putString("status",value).apply();if(active)notifications.notify(NOTICE,monitor(value));}
 @Override public void onCreate(){super.onCreate();notifications=getSystemService(NotificationManager.class);locations=getSystemService(LocationManager.class);notifications.createNotificationChannel(new NotificationChannel(CHANNEL,"Parking monitoring and cancellation",NotificationManager.IMPORTANCE_LOW));notifications.createNotificationChannel(new NotificationChannel(ALERTS,"Parking results and return reminder",NotificationManager.IMPORTANCE_DEFAULT));}
 @Override public int onStartCommand(Intent i,int flags,int id){
  if(i==null||"stop".equals(i.getAction())){stop(this);stopSelf();return START_NOT_STICKY;}
  // Called from a visible Activity, so while-in-use location permission is sufficient.
  monitorBluetooth="monitor".equals(i.getAction());
  active=true;canSend=false;session=++generation;startedAt=SystemClock.elapsedRealtime();anchor=null;rules=null;awaitingFix=false;
  startForeground(NOTICE,monitor("Starting parking monitoring…"));
  if(!permissionsValid()){setStatus("Enable precise location, Nearby devices and both notification channels, then start again.");stopSelf();return START_NOT_STICKY;}
  device=getSharedPreferences("parking",0).getString("device","");notifications.cancel(RETURN_NOTICE);
  try {
   if(!locations.isProviderEnabled(LocationManager.GPS_PROVIDER)){setStatus("Turn on phone location, then start again.");stopSelf();return START_NOT_STICKY;}
   if(registered && !monitorBluetooth){unregisterReceiver(bluetooth);registered=false;}
   if(monitorBluetooth && !registered){
    IntentFilter f=new IntentFilter();f.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);f.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    // These are protected system broadcasts, including ones sent by the Bluetooth process.
    if(Build.VERSION.SDK_INT>=33)registerReceiver(bluetooth,f,Context.RECEIVER_EXPORTED);else registerReceiver(bluetooth,f);
    registered=true;
   }
   locations.removeUpdates(this);locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,this,Looper.getMainLooper());
   awaitingFix="park".equals(i.getAction());requestAt=SystemClock.elapsedRealtime();
   setStatus(awaitingFix?"Stay beside your parked car. Finding an accurate GPS fix…":"Monitoring selected Bluetooth device. Waiting for a parking disconnect.");
   handler.removeCallbacks(watchdog);handler.postDelayed(watchdog,10000);
  }catch(SecurityException|IllegalArgumentException e){setStatus("Could not start monitoring. Check location and Nearby devices permissions.");stopSelf();}
  return START_NOT_STICKY;
 }
 private void arm(Location l){
  canSend=false;getSharedPreferences("parking",0).edit().putBoolean("attempted",false).apply();
  session=++generation;anchor=new Location(l);rules=new ParkingRules(ParkingStart.radius(this),SystemClock.elapsedRealtime());awaitingFix=false;
  CarLocations.saveParking(this,l);
  notifications.cancel(RETURN_NOTICE);setStatus("Parking saved on phone. Walk beyond "+rules.radius+" m plus GPS uncertainty to begin the "+(rules.countdownMillis()/1000)+"-second lock countdown.");
 }
 @Override public void onLocationChanged(Location l){
  if(!active)return;
  if(!fresh(l)){canSend=false;if(rules!=null)rules.interrupt();setStatus("GPS accuracy is insufficient (need ≤"+(ParkingStart.radius(this)<=30?5:20)+" m). Locking is paused.");return;}
  // Do not accept out-of-order fixes as new countdown samples.
  if(latest!=null && l.getElapsedRealtimeNanos()<=latest.getElapsedRealtimeNanos())return;
  if(latest!=null && l.getElapsedRealtimeNanos()-latest.getElapsedRealtimeNanos()>30000000000L && rules!=null)rules.interrupt();
  latest=new Location(l);
  if(awaitingFix){if(l.hasSpeed() && l.getSpeed()>1.5f){setStatus("Stop beside your parked car to save its location.");return;}arm(l);return;}
  if(rules==null||anchor==null)return;
  double distance=anchor.distanceTo(l), uncertainty=anchor.getAccuracy()+l.getAccuracy();
  canSend=l.hasSpeed() && l.getSpeed()<=2.5f && distance-uncertainty>=rules.radius && distance<=500;
  ParkingRules.Event event=rules.sample(SystemClock.elapsedRealtime(),distance,uncertainty,l.hasSpeed(),l.hasSpeed()?l.getSpeed():99);
  if(event==ParkingRules.Event.COUNTDOWN)setStatus("Beyond departure distance · "+(rules.countdownMillis()/1000)+"-second lock countdown. Stop / cancel to prevent locking.");
  else if(event==ParkingRules.Event.LOCK)sendLock();
  else if(event==ParkingRules.Event.RETURN)showReturn();
  else if(event==ParkingRules.Event.EXPIRED){setStatus("Parking window expired. Start a new session when parked.");stopSelf();}
  else if(!getSharedPreferences("parking",0).getBoolean("attempted",false))setStatus("Parking saved. Waiting for sustained movement beyond "+rules.radius+" m.");
 }
 private void sendLock(){
  final long token=session;
  setStatus("Lock queued · no automatic retry. Waiting for Hyundai.");
  getSharedPreferences("parking",0).edit().putBoolean("attempted",true).apply();
  RefreshJob.QUEUE.execute(()->{
   // Cancelled, reconnected, re-armed, signed out or stopped while queued: do not send.
   if(!active||generation!=token||!canSend||latest==null||!fresh(latest)||!permissionsValid())return;
   String result;
   try{result=new HyundaiApi(this).command("lock",72);}catch(Exception e){result="Lock not confirmed: "+(e.getMessage()==null?"request failed":e.getMessage());}
   final String answer=result;
   handler.post(()->{
    getSharedPreferences("actions",0).edit().putString("last_result","Walk-away lock: "+answer).apply();
    if(active&&generation==token)setStatus("Lock result: "+answer+" Waiting for return; no retries.");
    if(Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)notifications.notify(RESULT_NOTICE,base(ALERTS,"IONIQ · Walk-away lock result",answer).build());
   });
  });
 }
 private void showReturn(){
  canSend=false;generation++;
  setStatus("You’re near your saved parking location. Tap Unlock in the return notification if needed.");
  PendingIntent unlock=PendingIntent.getForegroundService(this,711,new Intent(this,CommandService.class).setAction("unlock"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  notifications.notify(RETURN_NOTICE,base(ALERTS,"Back near your IONIQ?","Tap Unlock to send a remote command. Nothing unlocks automatically.").setAutoCancel(true).addAction(new Notification.Action.Builder(null,"Unlock",unlock).build()).build());
 }
 @Override public void onProviderDisabled(String provider){if(LocationManager.GPS_PROVIDER.equals(provider)){if(rules!=null)rules.interrupt();setStatus("Location disabled. Locking is paused.");}}
 @Override public void onProviderEnabled(String provider){}
 @Override public void onStatusChanged(String provider,int status,Bundle extras){}
 @Override public void onDestroy(){active=false;generation++;handler.removeCallbacks(watchdog);try{if(locations!=null)locations.removeUpdates(this);}catch(SecurityException ignored){}if(registered)unregisterReceiver(bluetooth);notifications.cancel(RETURN_NOTICE);stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}
