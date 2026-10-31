package com.ioniq.widget;
import android.app.*;import android.content.*;import android.os.*;
public class CommandService extends Service {
 private static final String CHANNEL="vehicle_commands";static volatile boolean running;static volatile String activeAction="";
 private Notification notification(String text){return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_lock).setContentTitle(VehicleIdentity.title(this)+" · Remote control").setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(PendingIntent.getActivity(this,900,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE)).setOnlyAlertOnce(true).build();}
 @Override public void onCreate(){super.onCreate();((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(CHANNEL,"Vehicle commands",NotificationManager.IMPORTANCE_LOW));}
 private void rejectStart(){if(!running){startForeground(600,notification("Request cancelled or unavailable. Nothing was sent."));stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent==null){rejectStart();return START_NOT_STICKY;}String action=intent.getAction();int index=ActionWidget.index(action);boolean watch=intent.hasExtra("watch_id");String label=index>=0?ActionWidget.LABELS[index]:VehicleSignal.title(action);
  if(index<0&&!(watch&&VehicleSignal.isSignal(action))){rejectStart();return START_NOT_STICKY;}
  boolean bluetooth=intent.hasExtra("bt_token");
  if(bluetooth&&!BluetoothLockService.mayRun(this,intent)){BluetoothLockService.result(this,intent,"Cancelled or expired · nothing sent");rejectStart();return START_NOT_STICKY;}
  if(watch&&!WatchRelay.mayRun(this,intent)){WatchRelay.update(this,intent,"blocked","Request expired or watch connection changed. Nothing was sent.");rejectStart();return START_NOT_STICKY;}
  if(running){if(bluetooth)BluetoothLockService.result(this,intent,"Another command is in progress · lock not sent; no retry");if(watch)WatchRelay.update(this,intent,"blocked","Another command is in progress. Nothing else was sent.");android.widget.Toast.makeText(this,"A command is already in progress. Nothing else was sent.",android.widget.Toast.LENGTH_SHORT).show();ActionWidget.renderAll(this);return START_NOT_STICKY;}
  final boolean authenticatedUnlock="unlock".equals(action)&&UnlockGuard.consume(intent.getStringExtra("unlock_permit"));
  if("unlock".equals(action)&&UnlockGuard.enabled(this)&&!authenticatedUnlock){if(watch)WatchRelay.update(this,intent,"blocked","Unlock protection is on. Authenticate using Unlock doors on the phone.");startForeground(600,notification("Unlock requires phone authentication. Open the app and tap Unlock doors."));stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return START_NOT_STICKY;}
  if("unlock".equals(action))((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(711);
  activeAction=action;running=true;startForeground(600,notification("Queued: "+label));
  SecurePrefs.get(this,"actions").edit().putString(action,"Queued…").putString(action+"_detail","Waiting for the app’s current request to finish.").putLong(action+"_time",System.currentTimeMillis()).apply();ActionWidget.renderAll(this);WidgetFeedbackJob.schedule(this);if(watch)WatchRelay.update(this,intent,"queued","Queued on phone…");
  final String commandScope=VehicleGarage.scope(this);
  int temp=getSharedPreferences("ui",0).getInt("temp",72);
  RefreshJob.QUEUE.execute(()->{
   SecurePrefs.get(this,"actions").edit().putString(action,"Sending…").putString(action+"_detail","Sending command / waiting for Hyundai confirmation.").putLong(action+"_time",System.currentTimeMillis()).commit();ActionWidget.renderAll(this);
   new Handler(Looper.getMainLooper()).post(()->{if(running)((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(600,notification("Sending "+label+"…"));});
   String result;String shortResult;
   try{
    if(commandScope.isEmpty()||!commandScope.equals(VehicleGarage.scope(this))){result="Vehicle changed. Nothing was sent.";shortResult="Blocked";}
    else if("unlock".equals(action)&&UnlockGuard.enabled(this)&&!authenticatedUnlock){result="Unlock protection is on. Authenticate on the phone. Nothing was sent.";shortResult="Blocked";}
    else if(bluetooth&&!BluetoothLockService.consume(this,intent)){result="Bluetooth lock cancelled or expired. Nothing was sent.";shortResult="Blocked";}
    else if(watch&&(!WatchRelay.mayRun(this,intent)||("unlock".equals(action)&&UnlockGuard.enabled(this)))){result="Watch request expired, connection changed, or unlock protection was enabled. Nothing was sent.";shortResult="Blocked";}
    else{if(watch)WatchRelay.update(this,intent,"sending","Sending to Hyundai…");result=new HyundaiApi(this).command(action,temp);shortResult=result.startsWith("Hyundai reports command completed")?"Confirmed":"Unconfirmed";}
   }
   catch(Exception e){result=ErrorText.message(e);shortResult="Check app";}
   if(!"Blocked".equals(shortResult))result=LockFeedback.describe(action,result);
   SecurePrefs.get(this,"actions").edit().putString(action,shortResult).putString(action+"_detail",result).putString("last_result",label+": "+result).putLong(action+"_time",System.currentTimeMillis()).commit();
   if(watch)WatchRelay.update(this,intent,"Confirmed".equals(shortResult)?"confirmed":"Blocked".equals(shortResult)?"blocked":"Unconfirmed".equals(shortResult)?"unknown":"error",result);
   if(bluetooth)BluetoothLockService.result(this,intent,result);
   final String answer=result;
   new Handler(Looper.getMainLooper()).post(()->{
    running=false;activeAction="";WidgetFeedbackJob.cancel(this);ActionWidget.renderAll(this);CarWidget.render(this);
    if(android.os.Build.VERSION.SDK_INT<33 || checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(600,notification(answer));
    stopForeground(STOP_FOREGROUND_DETACH);stopSelf();
   });
  });return START_NOT_STICKY;
 }
 @Override public IBinder onBind(Intent i){return null;}
}
