package com.ioniq.widget;
import android.content.*;
/** Restart opted-in monitoring only; never restores a pending lock. */
public final class BluetoothLockBoot extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){
  if(i==null||!(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction())))return;
  try{if(BluetoothLockService.enabled(c))c.startForegroundService(new Intent(c,BluetoothLockService.class));}
  catch(RuntimeException e){BluetoothLockService.prefs(c).edit().putString("bt_status","Monitoring paused by Android · open auto-lock settings").apply();}
 }
}
