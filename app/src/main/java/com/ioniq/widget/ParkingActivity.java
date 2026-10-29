package com.ioniq.widget;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.widget.*;
import java.util.*;

public class ParkingActivity extends Activity {
 private LinearLayout page;
 private TextView status;
 private final Handler handler = new Handler(Looper.getMainLooper());
 private final Runnable refresh = new Runnable() { public void run() {
  if (status != null) status.setText((ParkingService.active ? "Monitoring · " : "Stopped · ") + SecurePrefs.get(ParkingActivity.this,"parking").getString("status","No parking session started."));
  handler.postDelayed(this,1000);
 }};
 private void text(String value) { TextView t=new TextView(this); t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(17);t.setPadding(0,16,0,16);page.addView(t); }
 private void button(String value,Runnable action) { Button b=new Button(this);b.setText(value);b.setAllCaps(false);page.addView(b);b.setOnClickListener(v->action.run()); }
 @Override public void onCreate(Bundle saved) {
  super.onCreate(saved);ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(32,32,32,32);page.setBackgroundColor(0xff0b161f);scroll.addView(page);setContentView(scroll);
  text("PARK & RETURN · Preview");
  text("Start monitoring before your drive, then select your paired car or dongle. A Bluetooth disconnect can mark parking only when a recent, accurate, slow-moving GPS fix is available. Wireless Android Auto may use Wi-Fi instead; use I’ve parked here if no parking cue appears.");
  status=new TextView(this);status.setTextColor(0xff8fe4cb);status.setTextSize(18);page.addView(status);
  button("1 · Select car / dongle Bluetooth",()->chooseDevice());
  button("2 · Departure distance",()->new AlertDialog.Builder(this).setTitle("Departure distance · current "+ParkingStart.radius(this)+" m").setItems(new String[]{"20 m (66 ft)","30 m (98 ft) · default","50 m (164 ft)","100 m (328 ft)","150 m (492 ft)","200 m (656 ft)"},(d,n)->{
   ParkingService.stop(this);SecurePrefs.get(ParkingActivity.this,"parking").edit().putBoolean("close_distance_v2",true).putInt("radius",new int[]{20,30,50,100,150,200}[n]).apply();Toast.makeText(this,"Distance saved. Start a new session.",Toast.LENGTH_LONG).show();}).show());
  button("3 · Start monitoring before driving",()->start(false));
  button("I’ve parked here · arm this location",()->start(true));
  button("Stop / cancel parking session",()->ParkingService.stop(this));
  text("Departure default: 30 m plus GPS uncertainty. 20/30 m settings require GPS accuracy within 5 m and 5 seconds of continued separation; longer settings retain 20 seconds. Actual lock distance/time also depends on walking speed and Hyundai network delay. One lock attempt per parking session; no automatic retries. Return reminders use a smaller inner radius so they cannot fire while you are still walking away. Unlock only happens when you tap it. Internet and Hyundai service are required.");
  text("Allow precise location while using the app, Nearby devices, and notifications. Monitoring continues with a visible notification after you leave this screen. Start again after reboot, force-stop, or Android stops the app. Samsung battery restrictions can interrupt monitoring. Location stays on this phone; this feature uses more battery while active.");
  text("Keep your key available during testing. Confirm locking in the Hyundai app. Loss of GPS, permissions or an unsuitable parking fix prevents automatic locking. Stop cancels pending work, but cannot recall a command already sent.");
 }
 private boolean permissions() { return ParkingStart.permissions(this,true); }
 @Override public void onRequestPermissionsResult(int request,String[] p,int[] results){super.onRequestPermissionsResult(request,p,results);if(request==71)Toast.makeText(this,"After allowing permissions, tap your setup button again.",Toast.LENGTH_LONG).show();}
 private void chooseDevice() {
  if(!permissions())return;
  try {
   BluetoothAdapter adapter=getSystemService(BluetoothManager.class).getAdapter();
   if(adapter==null||!adapter.isEnabled()){Toast.makeText(this,"Turn Bluetooth on and pair the dongle in Android settings first.",Toast.LENGTH_LONG).show();return;}
   ArrayList<BluetoothDevice> devices=new ArrayList<>(adapter.getBondedDevices());
   if(devices.isEmpty()){Toast.makeText(this,"No paired Bluetooth devices. Use Android Bluetooth settings to pair first.",Toast.LENGTH_LONG).show();return;}
   String[] names=new String[devices.size()];for(int n=0;n<names.length;n++)names[n]=devices.get(n).getName()+" · "+devices.get(n).getAddress();
   new AlertDialog.Builder(this).setTitle("Choose only your car / dongle").setItems(names,(d,n)->{ParkingService.stop(this);SecurePrefs.get(ParkingActivity.this,"parking").edit().putString("device",devices.get(n).getAddress()).putString("status","Device selected. Start monitoring before driving.").apply();}).show();
  }catch(SecurityException e){Toast.makeText(this,"Nearby devices permission is required.",Toast.LENGTH_LONG).show();}
 }
 private void start(boolean parked) { ParkingStart.start(this,parked); }
 @Override protected void onResume(){super.onResume();handler.post(refresh);}
 @Override protected void onPause(){super.onPause();handler.removeCallbacks(refresh);}
}
