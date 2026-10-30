package com.ioniq.widget;
import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.content.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
public class MainActivity extends Activity {
 private ImageView carImage;private TextView vehicleTitle,vehicleSubtitle;private int shownModel=-1;
 private Button walkAway;private TextView parkingStatus;private String reportDetails="",resultDetails="";
 private final android.os.Handler parkingHandler=new android.os.Handler(android.os.Looper.getMainLooper());
 private final Runnable parkingTick=new Runnable(){public void run(){updateParkingControl();parkingHandler.postDelayed(this,1000);}};
 private void updateParkingControl(){
  if(walkAway!=null){walkAway.setText("Auto-lock · "+(BluetoothLockService.enabled(this)?(BluetoothLockService.instance==null?"Paused":"On"):"Off")+"  ›");walkAway.setEnabled(!busy);}
  if(parkingStatus!=null){String detail=SecurePrefs.get(this,"parking").getString("bt_status","Set up once · no button press each trip");parkingStatus.setText(BluetoothLockService.enabled(this)&&BluetoothLockService.instance==null?"Monitoring paused · tap settings to resume":detail);}
 }
 private void showResult(String value){
  resultDetails=value==null?"":value;
  String compact=resultDetails;
  if(compact.contains("Hyundai reports command completed."))compact="Command completed · details";
  message.setText(compact);message.setContentDescription(resultDetails.isEmpty()?"":"Latest result: "+resultDetails+". Tap for details.");message.setVisibility(compact.isEmpty()?View.GONE:View.VISIBLE);
 }
 private void reportDialog(){new AlertDialog.Builder(this).setTitle("Vehicle report").setMessage(reportDetails+"\n\nThis is Hyundai’s vehicle report time, not the time you tapped Refresh. Cached reports can be older than your latest command.").setPositiveButton("Done",null).show();}
 private String reportLabel(String raw){
  try{java.time.Instant at=java.time.OffsetDateTime.parse(raw).toInstant();long age=java.time.Duration.between(at,java.time.Instant.now()).getSeconds();if(age>=0){if(age<60)return "Reported just now · details";if(age<3600)return "Reported "+age/60+" min ago · details";if(age<86400)return "Reported "+age/3600+" hr ago · details";return "Reported "+age/86400+" days ago · details";}}catch(Exception ignored){}
  return raw.isEmpty()?"Report unavailable · details":"Vehicle report · details";
 }
 private void refreshOptions(){new AlertDialog.Builder(this).setTitle("Refresh status").setItems(new String[]{"Read saved Hyundai report","Request fresh report from car"},(d,n)->{if(n==0)refresh(false);else new AlertDialog.Builder(this).setMessage("Contact the car for a new status report?").setPositiveButton("Refresh",(x,w)->refresh(true)).setNegativeButton("Cancel",null).show();}).show();}

 private LinearLayout page;private TextView status,message,heroBattery,heroLock,heroRange,heroMileage,heroCharging;private boolean busy=false;private int temperature=72;
 private final java.util.ArrayList<Button> buttons=new java.util.ArrayList<>();
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density);}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(8),0,dp(8));page.addView(t);return t;}
 private android.graphics.drawable.GradientDrawable surface(int fill,int stroke){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(20));d.setStroke(dp(1),stroke);return d;}
 private Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setAllCaps(false);b.setBackground(surface(0xff172939,0xff294253));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,dp(5));page.addView(b,lp);b.setOnClickListener(v->{if(!busy)action.run();});buttons.add(b);return b;}
 private void controlRow(int left,int right){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);page.addView(row);for(int i:new int[]{left,right}){Button b=new Button(this);b.setText(new String[]{"Lock","Unlock","Climate on","Climate off","Start charge","Stop charge"}[i]);b.setContentDescription(ActionWidget.LABELS[i]);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(Color.WHITE);android.graphics.drawable.Drawable icon=getDrawable(ActionWidget.ICONS[i]);icon.setBounds(0,0,dp(42),dp(42));b.setCompoundDrawables(null,icon,null,null);b.setCompoundDrawablePadding(dp(6));b.setPadding(dp(8),dp(10),dp(8),dp(10));b.setMinHeight(dp(104));b.setGravity(Gravity.CENTER);b.setBackground(getDrawable(ActionWidget.BACKGROUNDS[i]));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(4),dp(6),dp(4),dp(6));row.addView(b,lp);buttons.add(b);final String a=ActionWidget.ACTIONS[i];b.setOnClickListener(v->{if(!busy)confirm(a);});}}
 private TextView badge(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(12),dp(8),dp(12),dp(8));t.setBackground(surface(0xe6102533,0xff365565));return t;}
 private void carHeader(){
  FrameLayout hero=new FrameLayout(this);hero.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{0xff254958,0xff0e1d2a,0xff122a39}));hero.setPadding(dp(6),dp(6),dp(6),dp(6));page.addView(hero,new LinearLayout.LayoutParams(-1,dp(getResources().getConfiguration().fontScale>1.2f?320:280)));
  ImageView car=new ImageView(this);carImage=car;car.setImageResource(R.drawable.ic_launcher_car);car.setScaleType(ImageView.ScaleType.FIT_CENTER);car.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);FrameLayout.LayoutParams pic=new FrameLayout.LayoutParams(-1,dp(210),Gravity.CENTER);hero.addView(car,pic);
  LinearLayout charge=new LinearLayout(this);charge.setOrientation(LinearLayout.VERTICAL);heroBattery=badge("—%",30,0xffb4f4df);charge.addView(heroBattery);heroCharging=badge("Charging unknown",11,0xffbcd6e3);charge.addView(heroCharging);FrameLayout.LayoutParams batteryPos=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.START);batteryPos.setMargins(dp(8),dp(8),0,0);hero.addView(charge,batteryPos);
  heroLock=badge("Lock unknown",14,0xffa4edda);FrameLayout.LayoutParams lockPos=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.END);lockPos.setMargins(0,dp(8),dp(8),0);hero.addView(heroLock,lockPos);
  LinearLayout metrics=new LinearLayout(this);metrics.setOrientation(LinearLayout.HORIZONTAL);heroRange=badge("RANGE\nUnavailable",14,Color.WHITE);heroMileage=badge("ODOMETER\nUnavailable",14,Color.WHITE);LinearLayout.LayoutParams metric=new LinearLayout.LayoutParams(0,-2,1);metric.setMargins(dp(4),0,dp(4),0);metrics.addView(heroRange,metric);LinearLayout.LayoutParams metric2=new LinearLayout.LayoutParams(0,-2,1);metric2.setMargins(dp(4),0,dp(4),0);metrics.addView(heroMileage,metric2);FrameLayout.LayoutParams bottom=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);bottom.setMargins(dp(4),0,dp(4),dp(8));hero.addView(metrics,bottom);
 }
 private void cautionStrip(){
  View tape=new View(this){private final android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
   @Override protected void onDraw(android.graphics.Canvas canvas){super.onDraw(canvas);canvas.drawColor(0xffffd54f);paint.setColor(0xff202020);float h=getHeight(),stripe=dp(16);for(float x=-h;x<getWidth()+h;x+=stripe*2){android.graphics.Path p=new android.graphics.Path();p.moveTo(x,0);p.lineTo(x+stripe,0);p.lineTo(x+stripe+h,h);p.lineTo(x+h,h);p.close();canvas.drawPath(p,paint);}}
  };tape.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(10));lp.setMargins(0,dp(22),0,dp(12));page.addView(tape,lp);
 }
 private void pinWidgets(){String[] choices={"Vehicle dashboard","Lock doors","Unlock doors","Climate on","Climate off","Start charging","Stop charging"};new AlertDialog.Builder(this).setTitle("Add a home-screen widget").setItems(choices,(dialog,n)->{android.appwidget.AppWidgetManager m=android.appwidget.AppWidgetManager.getInstance(this);Class<?> type=n==0?CarWidget.class:ActionWidget.TYPES[n-1];if(m.isRequestPinAppWidgetSupported())m.requestPinAppWidget(new ComponentName(this,type),null,null);else showResult("Long-press your home screen → Widgets → IONIQ Companion.");}).show();}
 @Override public void onCreate(Bundle b){super.onCreate(b);NamedClimate.initialize(this);temperature=getSharedPreferences("ui",0).getInt("temp",72);build();boolean launchedWithAction=(getIntent().hasExtra("action")||getIntent().hasExtra("widget_result"));handleIntent(getIntent());if(!launchedWithAction&&VehicleIdentity.needsLookup(this))refresh(false);if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},20);}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handleIntent(i);}
 private void handleIntent(Intent i){String widget=i.getStringExtra("widget_result");i.removeExtra("widget_result");if(ActionWidget.index(widget)>=0){widgetResult(widget);return;}String a=i.getStringExtra("action");i.removeExtra("action");if("refresh".equals(a))refresh(false);else if("lock".equals(a)||"climate".equals(a))confirm(a);}
 private void widgetResult(String action){
  android.content.SharedPreferences p=SecurePrefs.get(this,"actions");String stored=p.getString(action,"");
  boolean own=CommandService.running&&action.equals(CommandService.activeAction);
  WidgetFeedback.State state=WidgetFeedback.state(stored,own,false);
  String detail=p.getString(action+"_detail",WidgetFeedback.description(state));
  if(WidgetFeedback.pending(stored)&&!own)detail="The app stopped before confirmation was recorded. The command outcome is unknown. Check the official app before retrying.";
  long at=p.getLong(action+"_time",0);String time=at>0?"\n\nUpdated: "+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(at)):"";
  new AlertDialog.Builder(this).setTitle(ActionWidget.LABELS[ActionWidget.index(action)])
   .setMessage(detail+time+"\n\nThis is a command result, not live vehicle status.")
   .setPositiveButton("Done",null).setNeutralButton("Refresh details",(d,w)->widgetResult(action)).show();
 }
 private void build(){
  buttons.clear();ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(20),dp(16),dp(20),dp(24));page.setBackgroundColor(Color.rgb(11,22,31));scroll.addView(page);setContentView(scroll);
  vehicleTitle=text("IONIQ Companion",30,Color.WHITE);vehicleSubtitle=text("HYUNDAI US",12,Color.rgb(141,227,207));
  carHeader();
  LinearLayout summaryParent=page;LinearLayout summary=new LinearLayout(this);summary.setOrientation(LinearLayout.VERTICAL);summary.setGravity(Gravity.CENTER_VERTICAL);summary.setMinimumHeight(dp(48));summary.setPadding(0,dp(2),0,dp(2));summaryParent.addView(summary);page=summary;
  status=text("No vehicle data yet",12,0xffAFC5D4);status.setPadding(0,dp(2),0,dp(2));
  message=text("",13,0xffAFC5D4);message.setPadding(0,dp(2),0,dp(2));message.setMaxLines(1);message.setEllipsize(android.text.TextUtils.TruncateAt.END);
  summary.setFocusable(true);summary.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Vehicle & command details").setMessage(reportDetails+"\n\nThis is Hyundai’s vehicle report time, not the time you tapped Refresh."+(resultDetails.isEmpty()?"":"\n\nLatest result:\n"+resultDetails)).setPositiveButton("Done",null).setNeutralButton("Dismiss result",(d,w)->{showResult("");SecurePrefs.get(this,"actions").edit().remove("last_result").apply();}).show());
  page=summaryParent;showResult("");showStatus();
  text("DOORS",12,0xff53D8B0).setPadding(0,dp(2),0,dp(2));controlRow(0,1);
  text("CLIMATE",12,Color.rgb(141,227,207)).setPadding(0,dp(4),0,dp(2));controlRow(2,3);
  Button presets=button("Climate presets · "+NamedClimate.active(this)+" · "+temperature+" °F",()->{});
  presets.setOnClickListener(v->{if(!busy)chooseClimate(presets);});
  locationControls();
  walkAway=button("Auto-lock",()->startActivity(new Intent(this,ParkingActivity.class)));
  walkAway.setTextColor(0xff8fe4cb);walkAway.setTextSize(13);walkAway.setBackgroundColor(Color.TRANSPARENT);
  walkAway.setContentDescription("Bluetooth automatic locking status and settings");parkingStatus=null;updateParkingControl();
  button("Refresh status",()->refreshOptions());
  LinearLayout controls=page;
  LinearLayout charging=new LinearLayout(this);charging.setOrientation(LinearLayout.VERTICAL);charging.setVisibility(View.GONE);
  Button chargingToggle=button("Charging ▾",()->{});
  chargingToggle.setBackground(getDrawable(R.drawable.charging_bar));chargingToggle.setTextColor(0xffEEFFF3);chargingToggle.setTypeface(null,android.graphics.Typeface.BOLD);chargingToggle.setPadding(dp(12),0,dp(12),dp(5));
  chargingToggle.setOnClickListener(v->{boolean open=charging.getVisibility()!=View.VISIBLE;charging.setVisibility(open?View.VISIBLE:View.GONE);chargingToggle.setSelected(open);chargingToggle.setText(open?"Charging ▴":"Charging ▾");});
  controls.addView(charging);page=charging;controlRow(4,5);button("Charge limits",()->chargeLimits());page=controls;
  LinearLayout dashboard=page;
  LinearLayout settings=new LinearLayout(this);settings.setOrientation(LinearLayout.VERTICAL);settings.setVisibility(View.GONE);
  Button settingsToggle=button("Settings & help ▾",()->{});
  settingsToggle.setBackground(getDrawable(R.drawable.settings_bar));settingsToggle.setTextColor(0xffF5EEFF);settingsToggle.setTypeface(null,android.graphics.Typeface.BOLD);settingsToggle.setPadding(dp(12),0,dp(12),dp(5));
  settingsToggle.setOnClickListener(v->{boolean open=settings.getVisibility()!=View.VISIBLE;settings.setVisibility(open?View.VISIBLE:View.GONE);settingsToggle.setSelected(open);settingsToggle.setText(open?"Settings & help ▴":"Settings & help ▾");});
  dashboard.addView(settings);page=settings;
  button("Bluetooth auto-lock settings",()->startActivity(new Intent(this,ParkingActivity.class)));
  button("＋  Add a home-screen widget",()->pinWidgets());

  button("Watch controls",()->watchSettings());
  button("Unlock security",()->new AlertDialog.Builder(this).setTitle("Protect car unlocking").setMessage("Currently "+(UnlockGuard.enabled(this)?"ON":"OFF")+". Require a fingerprint or phone screen-lock PIN before unlocking from the app or widget. Locking and climate stay one tap.").setPositiveButton(UnlockGuard.enabled(this)?"Disable protection":"Enable protection",(d,w)->startActivity(new Intent(this,UnlockActivity.class).putExtra("mode",UnlockGuard.enabled(this)?"disable":"enable"))).setNegativeButton("Cancel",null).show());
  cautionStrip();
  text("⚠  ACCOUNT SETTINGS — PROCEED WITH CARE",14,0xffffd54f);
  Button account=button("⚠  Connect / change Bluelink account",()->new AlertDialog.Builder(this).setTitle("Change your Bluelink connection?").setMessage("Continuing opens sign-in. Submitting a new login replaces this phone’s saved account and vehicle selection and stops Bluetooth auto-lock. You will need your Bluelink email, password and remote-service PIN. Cancelling leaves your current connection unchanged.").setNegativeButton("Keep current account",null).setPositiveButton("Continue to sign-in",(d,w)->loginForm()).show());
  account.setTextColor(0xffffd54f);
  Button erase=button("⚠  Disconnect and erase local data",()->new AlertDialog.Builder(this).setTitle("Erase this phone’s saved connection?").setMessage("This removes your saved Bluelink login, PIN, selected vehicle, status and command history. Bluetooth auto-lock will stop and its settings will be erased. You must sign in again to control the car. This does not delete your Hyundai account. This action cannot be undone.").setNegativeButton("Cancel — keep my data",null).setPositiveButton("Erase and disconnect",(d,w)->{BluetoothLockService.stop(this);run(()->{WatchRelay.disable(this);Vault.clear(this);CarLocations.clear(this);SecurePrefs.get(this,"mileage").edit().clear().commit();SecurePrefs.get(this,"vehicle_identity").edit().clear().commit();SecurePrefs.get(this,"parking").edit().clear().commit();return "Disconnected. Saved login, status and parking settings removed.";});}).show());
  erase.setTextColor(0xffffd54f);
  cautionStrip();
  text("Credentials are encrypted with Android Keystore. Requests go directly to Hyundai. Background refresh reads Hyundai’s cached report about hourly when Android allows; the car’s timestamp shows its actual age. Action widgets send commands with one tap, except unlocking when unlock protection is enabled. Climate on uses your saved temperature. A confirmed command is not a live sensor reading.",12,Color.LTGRAY);
  text("IONIQ Companion · 0.8.8",12,0xffAFC5D4);page=dashboard;
 }
 @Override protected void onPause(){super.onPause();parkingHandler.removeCallbacks(parkingTick);}
 @Override protected void onResume(){super.onResume();parkingHandler.removeCallbacks(parkingTick);parkingHandler.post(parkingTick);if(status!=null)showStatus();ActionWidget.renderAll(this);String last=SecurePrefs.get(this,"actions").getString("last_result","");if(message!=null&&!last.isEmpty()&&!busy)showResult(last);}
 private void updateVehicleIdentity(){
  JSONObject profile=VehicleIdentity.read(this);int model=profile.optInt("model");
  if(vehicleTitle!=null)vehicleTitle.setText(ModelName.title(model));
  String year=profile.optString("year","");
  if(vehicleSubtitle!=null)vehicleSubtitle.setText((year.isEmpty()?"":year+" • ")+"HYUNDAI US");
  if(carImage!=null&&shownModel!=model){carImage.setImageResource(model==5?R.drawable.car_profile_5:model==6?R.drawable.car_profile:R.drawable.ic_launcher_car);shownModel=model;}
 }
 private void showStatus(){updateVehicleIdentity();try{JSONObject s=new JSONObject(SecurePrefs.get(this,"status").getString("json","{}"));VehicleMileage.attach(this,s);reportDetails=StatusText.time(s)+"\nOdometer report: "+s.optString("mileageReport","unavailable");status.setText(reportLabel(s.optString("dateTime","")));if(heroBattery!=null){heroBattery.setText(StatusText.battery(s));heroLock.setText(StatusText.lock(s));heroCharging.setText(StatusText.charging(s));heroRange.setText("RANGE\n"+StatusText.range(s));heroMileage.setText("ODOMETER\n"+StatusText.odometer(s));heroLock.setTextColor("Unlocked".equals(StatusText.lock(s))?0xffffcf72:0xffa4edda);}}catch(Exception ignored){}}
 private void watchSettings(){
  boolean on=WatchRelay.enabled(this);new AlertDialog.Builder(this).setTitle("Galaxy Watch controls · "+(on?"ON":"OFF"))
   .setMessage("Allow the matching, signed watch app to send one-tap commands through this phone. Bluelink credentials stay here. Unlock protection, if enabled, requires unlocking from the phone. Turning this off does not cancel a command already sent.")
   .setNegativeButton("Cancel",null).setPositiveButton(on?"Disable":"Enable",(d,w)->{if(on){WatchRelay.disable(this);showResult("Watch controls disabled.");}else showResult(WatchRelay.enable(this)?"Watch controls enabled. Open IONIQ Companion on your watch.":"Connect and select your vehicle first.");}).show();
 }
 private void locationControls(){
  LinearLayout parent=page,row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);parent.addView(row);page=row;
  Button find=button("⌖  Find my car",()->findCar());LinearLayout.LayoutParams wide=new LinearLayout.LayoutParams(0,dp(52),1);wide.setMargins(0,dp(5),dp(6),dp(5));find.setLayoutParams(wide);
  Button details=button("⋯",()->locationOptions());details.setContentDescription("Find my car options: lights, horn, location details and saved parking");LinearLayout.LayoutParams small=new LinearLayout.LayoutParams(dp(52),dp(52));small.setMargins(0,dp(5),0,dp(5));details.setLayoutParams(small);page=parent;
 }
 private void findCar(){if(CarLocations.read(this,CarLocations.VEHICLE)!=null)openCarMap();else fetchCarMap();}
 private void openCarMap(){
  JSONObject data=CarLocations.read(this,CarLocations.VEHICLE);if(data==null){showResult("No saved Hyundai location. Use location details to refresh or open your parking spot.");return;}
  try{openLocation(CarLocations.point(data).mapUrl());}catch(Exception e){showResult("Saved location is invalid. Use location details to refresh.");}
 }
 private void fetchCarMap(){run(()->{
  String selected=CarLocations.vin(this);
  try{JSONObject location=new HyundaiApi(this).findCar();CarLocations.saveVehicle(this,selected,location);}
  catch(Exception e){runOnUiThread(()->{if(isFinishing()||isDestroyed()||!selected.equals(CarLocations.vin(this)))return;JSONObject parking=CarLocations.read(this,CarLocations.PARKING);if(parking!=null)new AlertDialog.Builder(this).setTitle("Use saved parking spot?").setMessage("Hyundai location could not be updated. You can open the separately saved phone parking spot instead.").setNegativeButton("Cancel",null).setPositiveButton("View parking spot",(d,w)->locationDetails(CarLocations.PARKING)).show();});throw e;}
  runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()&&selected.equals(CarLocations.vin(this)))openCarMap();});return "Location retrieved. Timestamp and refresh are under ⋯.";
 });}
 private void locationOptions(){
  java.util.ArrayList<String> sources=new java.util.ArrayList<>();
  java.util.ArrayList<CharSequence> labels=new java.util.ArrayList<>();
  for(String source:new String[]{CarLocations.VEHICLE,CarLocations.PARKING}){JSONObject data=CarLocations.read(this,source);if(data!=null){sources.add(source);labels.add(CarLocations.description(data,source));}}
  AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle("Find my car");
  labels.add(0,signalLabel("Horn & lights",0xffff8585));labels.add(0,signalLabel("Flash lights",0xffffcf72));
  dialog.setItems(labels.toArray(new CharSequence[0]),(d,n)->{if(n<2)confirmSignal(n==0?"lights":"horn_lights");else locationDetails(sources.get(n-2));});
  dialog.setPositiveButton("Update & open map",(d,w)->fetchCarMap()).setNegativeButton("Close",null);
  if(!sources.isEmpty())dialog.setNeutralButton("Clear saved",(d,w)->new AlertDialog.Builder(this).setTitle("Clear saved locations?").setMessage("Remove the saved car and parking coordinates from this phone? Bluetooth auto-lock settings are unchanged.").setNegativeButton("Cancel",null).setPositiveButton("Clear",(x,y)->{CarLocations.clear(this);locationOptions();}).show());
  dialog.show();
 }
 private CharSequence signalLabel(String label,int color){
  android.text.SpannableString styled=new android.text.SpannableString(label);
  styled.setSpan(new android.text.style.ForegroundColorSpan(color),0,label.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  styled.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0,label.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  return styled;
 }
 private void confirmSignal(String action){
  if(busy||CommandService.running){showResult("Another request is in progress. Wait for its result first.");return;}
  String title=VehicleSignal.title(action);
  new AlertDialog.Builder(this).setTitle(title+"?").setMessage(("horn_lights".equals(action)?"This will sound the horn and flash the lights on your selected car. Please consider people nearby.":"This will flash the lights on your selected car.")+"\n\nHyundai controls the duration. The request is sent once; there is no automatic retry.")
   .setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{if(busy||CommandService.running){showResult("Another request is in progress. Nothing else was sent.");return;}run(()->{String result=title+": "+new HyundaiApi(this).command(action,temperature);SecurePrefs.get(this,"actions").edit().putString("last_result",result).apply();return result;});}).show();
 }
 private void locationDetails(String source){
  JSONObject data=CarLocations.read(this,source);if(data==null){locationOptions();return;}
  try{GeoPoint point=CarLocations.point(data);new AlertDialog.Builder(this).setTitle(source.equals(CarLocations.PARKING)?"Saved parking spot":"Hyundai location").setMessage(CarLocations.description(data,source)+"\n\nOpen the map or walking directions in Google Maps. This shares the selected coordinates with Google.")
   .setPositiveButton("Open map",(d,w)->openLocation(point.mapUrl()))
   .setNeutralButton("Walk here",(d,w)->openLocation(point.walkUrl())).setNegativeButton("Back",(d,w)->locationOptions()).show();
  }catch(Exception e){showResult("Saved location is invalid. Request an updated location.");}
 }
 private void openLocation(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));}catch(ActivityNotFoundException e){showResult("Install a map app or browser to open this location.");}}
 private interface Task{String go()throws Exception;}
 private void run(Task task){if(busy)return;busy=true;for(Button b:buttons)b.setEnabled(false);updateParkingControl();showResult("Working…");RefreshJob.QUEUE.execute(()->{String result;try{result=task.go();}catch(Exception e){result=ErrorText.message(e);if(result==null)result="Request failed. Check your connection and the official app.";}final String answer=result;CarWidget.render(this);runOnUiThread(()->{busy=false;if(isDestroyed())return;for(Button b:buttons)b.setEnabled(true);showResult(answer);showStatus();});});}
 private void refresh(boolean force){run(()->{VehicleMileage.refresh(this,force);RefreshJob.schedule(this);return "Status refreshed";});}
 private void presetApplied(Button button){temperature=getSharedPreferences("ui",0).getInt("temp",72);button.setText("Climate presets · "+NamedClimate.active(this)+" · "+temperature+" °F");ActionWidget.renderAll(this);}
 private void chooseClimate(Button button){
  String[] options=new String[4];for(int i=0;i<3;i++)options[i]="Use "+NamedClimate.NAMES[i]+" · "+NamedClimate.temp(this,NamedClimate.NAMES[i])+" °F";options[3]="Edit presets…";
  new AlertDialog.Builder(this).setTitle("Climate presets").setItems(options,(d,n)->{if(n<3){NamedClimate.use(this,NamedClimate.NAMES[n]);presetApplied(button);}else new AlertDialog.Builder(this).setTitle("Edit preset").setItems(NamedClimate.NAMES,(x,i)->editClimate(button,NamedClimate.NAMES[i])).show();}).setNegativeButton("Cancel",null).show();
 }
 private void editClimate(Button button,String name){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(8),dp(20),dp(8));
  TextView label=new TextView(this);label.setText(NamedClimate.temp(this,name)+" °F");box.addView(label);
  SeekBar slider=new SeekBar(this);slider.setMax(28);slider.setProgress(NamedClimate.temp(this,name)-62);box.addView(slider);
  slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){label.setText((p+62)+" °F");}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
  String[] keys={"front_defrost","rear_defrost","steering_heat"},labels={"Front defrost","Rear defrost","Steering-wheel heater"};CheckBox[] checks=new CheckBox[3];
  for(int i=0;i<3;i++){checks[i]=new CheckBox(this);checks[i].setText(labels[i]);checks[i].setChecked(NamedClimate.flag(this,name,keys[i]));box.addView(checks[i]);}
  new AlertDialog.Builder(this).setTitle(name+" preset").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save & use",(d,w)->{NamedClimate.save(this,name,62+slider.getProgress(),checks[0].isChecked(),checks[1].isChecked(),checks[2].isChecked());presetApplied(button);}).show();
 }
 private void chargeLimits(){
  JSONObject saved;try{saved=new JSONObject(SecurePrefs.get(this,"status").getString("json","{}"));}catch(Exception e){saved=new JSONObject();}
  int oldAc=ChargeLimits.read(saved,ChargeLimitValues.AC),oldDc=ChargeLimits.read(saved,ChargeLimitValues.DC);
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(8),dp(20),dp(8));
  TextView report=new TextView(this);report.setText("Last report: AC "+ChargeLimits.display(oldAc)+" · DC "+ChargeLimits.display(oldDc)+"\n"+StatusText.time(saved));box.addView(report);
  String[] choices={"Choose limit","50%","60%","70%","80%","90%","100%"};Spinner[] pickers=new Spinner[2];
  for(int i=0;i<2;i++){TextView label=new TextView(this);label.setText(i==0?"AC charging":"DC fast charging");box.addView(label);pickers[i]=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices);pickers[i].setAdapter(adapter);int current=i==0?oldAc:oldDc;pickers[i].setSelection(current<0?0:(current-50)/10+1);box.addView(pickers[i]);}
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Charge limits").setView(box).setNegativeButton("Cancel",null).setNeutralButton("Read latest",(d,w)->run(()->{new HyundaiApi(this).status(false);runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())chargeLimits();});return "Hyundai report refreshed";})).setPositiveButton("Review",null).create();
  dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{int ai=pickers[0].getSelectedItemPosition(),di=pickers[1].getSelectedItemPosition();if(ai==0||di==0){report.setText("Choose both AC and DC limits before continuing.");return;}int ac=40+ai*10,dc=40+di*10;dialog.dismiss();new AlertDialog.Builder(this).setTitle("Set charging limits?").setMessage("AC: "+ac+"%\nDC fast charging: "+dc+"%\n\nSend these limits to your selected Hyundai?").setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->run(()->new HyundaiApi(this).command("limits",ChargeLimitValues.pack(ac,dc)))).show();}));dialog.show();
 }
 private void confirm(String a){if("unlock".equals(a)&&UnlockGuard.enabled(this)){startActivity(new Intent(this,UnlockActivity.class).putExtra("mode","unlock"));return;}String name=a.equals("climate")?"Start climate at "+temperature+" °F":a.replace('_',' ');new AlertDialog.Builder(this).setTitle(name).setMessage((a.equals("climate")?ClimatePreset.summary(this)+"\n\n":"")+"Send this command to your selected Hyundai? Completion can take a minute.").setPositiveButton("Send",(d,w)->{final int chosen=temperature;run(()->new HyundaiApi(this).command(a,chosen));}).setNegativeButton("Cancel",null).show();}
 private EditText field(LinearLayout box,String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setInputType(type);e.setSingleLine(true);e.setSaveEnabled(false);e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);box.addView(e);return e;}
 private void loginForm(){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),0,dp(20),0);
  EditText email=field(box,"Bluelink email",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
  EditText pass=field(box,"Bluelink password",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
  EditText pin=field(box,"Remote-service PIN",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Hyundai US sign-in").setView(box).setPositiveButton("Connect",null).setNegativeButton("Cancel",null).create();
  dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String e=email.getText().toString().trim(),p=pass.getText().toString(),n=pin.getText().toString();if(e.isEmpty()||p.isEmpty()||n.isEmpty()){email.setError("Fill in all three fields");return;}dialog.dismiss();BluetoothLockService.stop(this);run(()->{
   JSONObject data=new JSONObject().put("email",e).put("password",p).put("pin",n);WatchRelay.disable(this);Vault.save(this,data);CarLocations.clear(this);SecurePrefs.get(this,"mileage").edit().clear().commit();SecurePrefs.get(this,"vehicle_identity").edit().clear().commit();SecurePrefs.get(this,"status").edit().clear().commit();
   HyundaiApi api=new HyundaiApi(this);JSONArray list=api.vehicles();if(list.length()==0)return "No enrolled vehicles found.";
   runOnUiThread(()->chooseVehicle(list));return "Signed in. Select your vehicle.";
  });}));dialog.show();
 }
 private void chooseVehicle(JSONArray list){
  if(isFinishing()||isDestroyed())return;
  String[] names=new String[list.length()];try{for(int i=0;i<names.length;i++){JSONObject v=list.getJSONObject(i).getJSONObject("vehicleDetails");String vin=v.getString("vin");names[i]=v.optString("modelYear")+" "+v.optString("modelCode")+" • …"+vin.substring(Math.max(0,vin.length()-6));}}catch(Exception e){showResult("Unexpected vehicle list format.");return;}
  new AlertDialog.Builder(this).setTitle("Choose your vehicle").setItems(names,(d,index)->{BluetoothLockService.stop(this);run(()->{HyundaiApi api=new HyundaiApi(this);CarLocations.clear(this);WatchRelay.disable(this);api.select(list.getJSONObject(index).getJSONObject("vehicleDetails"));VehicleIdentity.save(this,list.getJSONObject(index).getJSONObject("vehicleDetails"));VehicleMileage.refresh(this,false);RefreshJob.schedule(this);return "Vehicle connected. You can now add the widget.";});}).show();
 }
}
