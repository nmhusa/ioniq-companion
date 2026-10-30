package com.ioniq.widget;
import android.Manifest;import android.app.*;import android.bluetooth.*;import android.content.*;import android.content.pm.PackageManager;import android.graphics.Color;import android.os.*;import android.widget.*;import java.util.*;
/** Single setup screen for automatic lock on the car's own Bluetooth disconnect. */
public class ParkingActivity extends Activity {
 private LinearLayout page;private Button car,delay,toggle;private TextView status;private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable refresh=new Runnable(){public void run(){render();handler.postDelayed(this,1000);}};
 private android.content.SharedPreferences prefs(){return BluetoothLockService.prefs(this);}
 private void text(String value){TextView t=new TextView(this);t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(16);t.setPadding(0,18,0,18);page.addView(t);}
 private Button button(String value,Runnable action){Button b=new Button(this);b.setText(value);b.setAllCaps(false);page.addView(b);b.setOnClickListener(v->action.run());return b;}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(32,32,32,32);page.setBackgroundColor(0xff0b161f);scroll.addView(page);setContentView(scroll);
  text("BLUETOOTH AUTO-LOCK");
  text("Set up once. Your car disconnects → countdown → one lock attempt. No button press each trip.");
  car=button("Select car Bluetooth",()->choose());
  delay=button("Delay",()->new AlertDialog.Builder(this).setTitle("Lock delay").setItems(new String[]{"10 seconds","15 seconds","30 seconds","60 seconds","120 seconds"},(d,n)->{BluetoothLockService.stop(this);prefs().edit().putInt("bt_delay",new int[]{10,15,30,60,120}[n]).apply();render();Toast.makeText(this,"Delay saved. Enable auto-lock again.",Toast.LENGTH_SHORT).show();}).show());
  toggle=button("Enable auto-lock",()->{if(BluetoothLockService.enabled(this)&&BluetoothLockService.instance!=null){BluetoothLockService.stop(this);render();}else enable();});
  status=new TextView(this);status.setTextColor(0xff8fe4cb);status.setTextSize(15);page.addView(status);
  text("Select the car itself, not your Android Auto dongle. Reconnecting cancels a pending lock. Turning Bluetooth off stops auto-lock. A lost Bluetooth connection can also happen while driving; it is not proof you parked or closed the doors.");
  text("Monitoring uses a persistent notification with Turn off / cancel. It resumes after a phone restart when Android permits, but never restores an old countdown. Check the result notification: Internet and Hyundai service are required. A command already sent cannot be recalled.");
  render();
 }
 private void render(){if(car==null)return;car.setText("Car · "+prefs().getString("bt_name","Select Bluetooth device"));delay.setText("Lock delay · "+BluetoothLockService.delay(this)+" seconds");toggle.setText(BluetoothLockService.enabled(this)?(BluetoothLockService.instance!=null?"Turn auto-lock off":"Resume auto-lock"):"Enable auto-lock");if(status!=null)status.setText(prefs().getString("bt_status","Off"));}
 private boolean permissions(boolean notifications){
  ArrayList<String> list=new ArrayList<>();if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)list.add(Manifest.permission.BLUETOOTH_CONNECT);
  if(notifications&&Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)list.add(Manifest.permission.POST_NOTIFICATIONS);
  if(!list.isEmpty()){requestPermissions(list.toArray(new String[0]),72);return false;}return true;
 }
 private void choose(){if(!permissions(false))return;try{BluetoothAdapter a=getSystemService(BluetoothManager.class).getAdapter();if(a==null||!a.isEnabled()){Toast.makeText(this,"Turn Bluetooth on and pair the car in Android settings first.",Toast.LENGTH_LONG).show();return;}
  ArrayList<BluetoothDevice> devices=new ArrayList<>(a.getBondedDevices());String[] names=new String[devices.size()];
  if(devices.isEmpty()){Toast.makeText(this,"Pair your car in Android Bluetooth settings first.",Toast.LENGTH_LONG).show();return;}
  for(int i=0;i<names.length;i++)names[i]=devices.get(i).getName()+" · "+devices.get(i).getAddress();
  new AlertDialog.Builder(this).setTitle("Select the CAR, not the dongle").setItems(names,(d,n)->{BluetoothLockService.stop(this);BluetoothDevice selected=devices.get(n);prefs().edit().putString("bt_device",selected.getAddress()).putString("bt_name",selected.getName()).apply();render();}).show();
 }catch(SecurityException e){Toast.makeText(this,"Allow Nearby devices access.",Toast.LENGTH_LONG).show();}}
 private void enable(){if(!permissions(true))return;
  if(prefs().getString("bt_device","").isEmpty()){choose();return;}
  String scope=BluetoothLockService.scope(this);if(scope.isEmpty()){Toast.makeText(this,"Connect your Hyundai account and select the car first.",Toast.LENGTH_LONG).show();return;}
  NotificationManager nm=getSystemService(NotificationManager.class);boolean allowed=nm.areNotificationsEnabled();
  for(String id:new String[]{"bluetooth_lock","bluetooth_lock_results"}){NotificationChannel c=nm.getNotificationChannel(id);if(c!=null&&c.getImportance()==NotificationManager.IMPORTANCE_NONE)allowed=false;}
  if(!allowed){startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()));return;}
  BluetoothAdapter adapter=getSystemService(BluetoothManager.class).getAdapter();if(adapter==null||!adapter.isEnabled()){Toast.makeText(this,"Turn Bluetooth on first.",Toast.LENGTH_LONG).show();return;}
  if(!prefs().edit().putString("bt_scope",scope).putBoolean("bt_enabled",true).commit()){Toast.makeText(this,"Could not save auto-lock settings.",Toast.LENGTH_LONG).show();return;}
  try{startForegroundService(new Intent(this,BluetoothLockService.class));}catch(RuntimeException e){BluetoothLockService.stop(this);Toast.makeText(this,"Android could not start monitoring.",Toast.LENGTH_LONG).show();}render();
 }
 @Override public void onRequestPermissionsResult(int request,String[] p,int[] results){super.onRequestPermissionsResult(request,p,results);Toast.makeText(this,"Tap the setup option again after allowing permissions.",Toast.LENGTH_SHORT).show();}
 @Override protected void onResume(){super.onResume();handler.post(refresh);}
 @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
