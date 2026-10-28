package com.ioniq.widget;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import java.util.ArrayList;

/** Shared, one-tap start path. Android permission prompts occur only during setup. */
final class ParkingStart {
 static int radius(Context c){
  android.content.SharedPreferences p=c.getSharedPreferences("parking",0);
  if(!p.getBoolean("close_distance_v2",false)){
   int old=p.getInt("radius",100);
   p.edit().putInt("radius",old==100?30:old).putBoolean("close_distance_v2",true).apply();
  }
  return p.getInt("radius",30);
 }
 static boolean permissions(Activity a,boolean bluetooth){
  ArrayList<String> needed=new ArrayList<>();
  if(a.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){needed.add(Manifest.permission.ACCESS_COARSE_LOCATION);needed.add(Manifest.permission.ACCESS_FINE_LOCATION);}
  if(bluetooth && Build.VERSION.SDK_INT>=31 && a.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)needed.add(Manifest.permission.BLUETOOTH_CONNECT);
  if(Build.VERSION.SDK_INT>=33 && a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)needed.add(Manifest.permission.POST_NOTIFICATIONS);
  if(!needed.isEmpty()){a.requestPermissions(needed.toArray(new String[0]),71);Toast.makeText(a,"Allow permissions, then tap Walk-away lock again.",Toast.LENGTH_LONG).show();return false;}
  NotificationManager nm=a.getSystemService(NotificationManager.class);
  NotificationChannel monitor=nm.getNotificationChannel("parking"),alerts=nm.getNotificationChannel("parking_alerts");
  if(!nm.areNotificationsEnabled() || monitor!=null&&monitor.getImportance()==NotificationManager.IMPORTANCE_NONE || alerts!=null&&alerts.getImportance()==NotificationManager.IMPORTANCE_NONE){
   new AlertDialog.Builder(a).setMessage("Enable parking notifications so you can see and cancel a lock countdown.").setPositiveButton("Settings",(d,w)->a.startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,a.getPackageName()))).setNegativeButton("Cancel",null).show();return false;
  }
  return true;
 }
 static void start(Activity a,boolean parked){
  radius(a);
  if(!permissions(a,!parked))return;
  if(!parked && a.getSharedPreferences("parking",0).getString("device","").isEmpty()){Toast.makeText(a,"Select your car or dongle in Walk-away settings first.",Toast.LENGTH_LONG).show();return;}
  try{if(Vault.read(a).optString("vin").isEmpty()){Toast.makeText(a,"Connect and select your Hyundai first.",Toast.LENGTH_LONG).show();return;}}
  catch(Exception e){Toast.makeText(a,"Connect your Hyundai first.",Toast.LENGTH_LONG).show();return;}
  a.startForegroundService(new Intent(a,ParkingService.class).setAction(parked?"park":"monitor"));
  Toast.makeText(a,parked?"Stay beside the parked car until Parking saved appears.":"Monitoring selected Bluetooth device for parking.",Toast.LENGTH_LONG).show();
 }
}
