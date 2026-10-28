package com.ioniq.widget;
import android.app.*;import android.content.*;import android.os.*;
public class CommandService extends Service {
 private static final String CHANNEL="vehicle_commands";static volatile boolean running;
 private Notification notification(String text){return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_lock).setContentTitle("IONIQ 6 · Remote control").setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(PendingIntent.getActivity(this,900,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE)).setOnlyAlertOnce(true).build();}
 @Override public void onCreate(){super.onCreate();((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(CHANNEL,"Vehicle commands",NotificationManager.IMPORTANCE_LOW));}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent==null){stopSelf();return START_NOT_STICKY;}String action=intent.getAction();int index=ActionWidget.index(action);
  if("unlock".equals(action))((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(711);
  if(index<0){if(!running)stopSelf();return START_NOT_STICKY;}
  if(running)return START_NOT_STICKY;
  running=true;startForeground(600,notification("Sending "+ActionWidget.LABELS[index]+"…"));
  getSharedPreferences("actions",0).edit().putString(action,"Sending…").putLong(action+"_time",System.currentTimeMillis()).apply();ActionWidget.renderAll(this);
  int temp=getSharedPreferences("ui",0).getInt("temp",72);
  RefreshJob.QUEUE.execute(()->{
   String result;String shortResult;
   try{result=new HyundaiApi(this).command(action,temp);shortResult=result.startsWith("Hyundai reports command completed")?"Confirmed":"Unconfirmed";}
   catch(Exception e){result=e.getMessage()==null?"Request failed. Check the official app before retrying.":e.getMessage();shortResult="Check app";}
   getSharedPreferences("actions",0).edit().putString(action,shortResult).putString("last_result",ActionWidget.LABELS[index]+": "+result).putLong(action+"_time",System.currentTimeMillis()).commit();
   ActionWidget.renderAll(this);CarWidget.render(this);final String answer=result;
   new Handler(Looper.getMainLooper()).post(()->{if(android.os.Build.VERSION.SDK_INT<33 || checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(600,notification(answer));stopForeground(STOP_FOREGROUND_DETACH);running=false;stopSelf();});
  });return START_NOT_STICKY;
 }
 @Override public IBinder onBind(Intent i){return null;}
}
