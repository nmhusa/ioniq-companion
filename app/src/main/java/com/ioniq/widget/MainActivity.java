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
 private LinearLayout page;private TextView status,message;private boolean busy=false;private int temperature=72;
 private final java.util.ArrayList<Button> buttons=new java.util.ArrayList<>();
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density);}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(8),0,dp(8));page.addView(t);return t;}
 private android.graphics.drawable.GradientDrawable surface(int fill,int stroke){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(20));d.setStroke(dp(1),stroke);return d;}
 private Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setAllCaps(false);b.setBackground(surface(0xff172939,0xff294253));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,dp(5));page.addView(b,lp);b.setOnClickListener(v->{if(!busy)action.run();});buttons.add(b);return b;}
 private void controlRow(int left,int right){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);page.addView(row);for(int i:new int[]{left,right}){Button b=new Button(this);b.setText(ActionWidget.LABELS[i]);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(Color.WHITE);b.setCompoundDrawablesWithIntrinsicBounds(0,ActionWidget.ICONS[i],0,0);b.setCompoundDrawablePadding(dp(10));b.setGravity(Gravity.CENTER);b.setBackground(getDrawable(ActionWidget.BACKGROUNDS[i]));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(148),1);lp.setMargins(dp(4),dp(6),dp(4),dp(6));row.addView(b,lp);buttons.add(b);final String a=ActionWidget.ACTIONS[i];b.setOnClickListener(v->{if(!busy)confirm(a);});}}
 private void pinWidgets(){String[] choices={"Vehicle dashboard","Lock doors","Unlock doors","Climate on","Climate off","Start charging","Stop charging"};new AlertDialog.Builder(this).setTitle("Add a home-screen widget").setItems(choices,(dialog,n)->{android.appwidget.AppWidgetManager m=android.appwidget.AppWidgetManager.getInstance(this);Class<?> type=n==0?CarWidget.class:ActionWidget.TYPES[n-1];if(m.isRequestPinAppWidgetSupported())m.requestPinAppWidget(new ComponentName(this,type),null,null);else message.setText("Long-press your home screen → Widgets → IONIQ 6 Companion.");}).show();}
 @Override public void onCreate(Bundle b){super.onCreate(b);temperature=getSharedPreferences("ui",0).getInt("temp",72);build();handleIntent(getIntent());if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},20);}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handleIntent(i);}
 private void handleIntent(Intent i){String a=i.getStringExtra("action");i.removeExtra("action");if("refresh".equals(a))refresh(false);else if("lock".equals(a)||"climate".equals(a))confirm(a);}
 private void build(){
  buttons.clear();ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(24),dp(24),dp(24),dp(24));page.setBackgroundColor(Color.rgb(11,22,31));scroll.addView(page);setContentView(scroll);
  text("YOUR ELECTRIC DRIVE",11,0xff72C9FF);text("IONIQ 6",38,Color.WHITE);text("2023 • HYUNDAI US • PREVIEW 0.6",12,Color.rgb(141,227,207));
  status=text("No vehicle data yet",22,Color.WHITE);status.setBackground(surface(0xff142D39,0xff315565));status.setPadding(dp(18),dp(22),dp(18),dp(22));message=text("Ready when you are. Your car’s report time appears above.",14,0xffAFC5D4);showStatus();
  text("DOORS",12,0xff53D8B0);controlRow(0,1);
  button("Refresh saved Hyundai status",()->refresh(false));
  button("Request fresh status from car",()->new AlertDialog.Builder(this).setMessage("Contact the car for a new status report?").setPositiveButton("Refresh",(d,w)->refresh(true)).setNegativeButton("Cancel",null).show());
  text("CLIMATE PRESET",12,Color.rgb(141,227,207));
  TextView temp=text(temperature+" °F",24,Color.WHITE);SeekBar seek=new SeekBar(this);seek.setMax(28);seek.setProgress(temperature-62);page.addView(seek);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){temperature=62+p;temp.setText(temperature+" °F");getSharedPreferences("ui",0).edit().putInt("temp",temperature).apply();ActionWidget.renderAll(MainActivity.this);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
  controlRow(2,3);text("CHARGING",12,0xffD7EB70);controlRow(4,5);
  text("MAKE IT YOURS",12,0xffB49AFF);button("＋  Add a home-screen widget",()->pinWidgets());
  button("Connect / change Bluelink account",()->loginForm());
  button("Disconnect and erase local data",()->new AlertDialog.Builder(this).setMessage("Remove your saved login and vehicle status from this phone?").setPositiveButton("Disconnect",(d,w)->run(()->{Vault.clear(this);return "Disconnected. Local login and status removed.";})).setNegativeButton("Cancel",null).show());
  text("Credentials are encrypted with Android Keystore. Requests go directly to Hyundai. Background refresh reads Hyundai’s cached report about hourly when Android allows; the car’s timestamp shows its actual age. The six individual action widgets send commands immediately with one tap. Climate on uses your saved temperature. A confirmed command is not a live sensor reading.",12,Color.LTGRAY);
 }
 @Override protected void onResume(){super.onResume();if(status!=null)showStatus();String last=getSharedPreferences("actions",0).getString("last_result","");if(message!=null&&!last.isEmpty()&&!busy)message.setText(last);}
 private void showStatus(){try{JSONObject s=new JSONObject(getSharedPreferences("status",0).getString("json","{}"));status.setText(StatusText.battery(s)+"\n"+StatusText.details(s)+"\n"+StatusText.time(s));}catch(Exception ignored){}}
 private interface Task{String go()throws Exception;}
 private void run(Task task){if(busy)return;busy=true;for(Button b:buttons)b.setEnabled(false);message.setText("Working… Please wait.");RefreshJob.QUEUE.execute(()->{String result;try{result=task.go();}catch(Exception e){result=(e instanceof java.net.SocketTimeoutException)?"Network timeout. Command outcome may be unknown; check the official app before retrying.":e.getMessage();if(result==null)result="Request failed. Check your connection and the official app.";}final String answer=result;CarWidget.render(this);runOnUiThread(()->{busy=false;if(isDestroyed())return;for(Button b:buttons)b.setEnabled(true);message.setText(answer);showStatus();});});}
 private void refresh(boolean force){run(()->{new HyundaiApi(this).status(force);RefreshJob.schedule(this);return "Status retrieved. Check the vehicle report timestamp.";});}
 private void confirm(String a){String name=a.equals("climate")?"Start climate at "+temperature+" °F":a.replace('_',' ');new AlertDialog.Builder(this).setTitle(name).setMessage("Send this command to your selected Hyundai? Completion can take a minute.").setPositiveButton("Send",(d,w)->{final int chosen=temperature;run(()->new HyundaiApi(this).command(a,chosen));}).setNegativeButton("Cancel",null).show();}
 private EditText field(LinearLayout box,String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setInputType(type);e.setSingleLine(true);e.setSaveEnabled(false);e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);box.addView(e);return e;}
 private void loginForm(){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),0,dp(20),0);
  EditText email=field(box,"Bluelink email",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
  EditText pass=field(box,"Bluelink password",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
  EditText pin=field(box,"Remote-service PIN",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Hyundai US sign-in").setView(box).setPositiveButton("Connect",null).setNegativeButton("Cancel",null).create();
  dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String e=email.getText().toString().trim(),p=pass.getText().toString(),n=pin.getText().toString();if(e.isEmpty()||p.isEmpty()||n.isEmpty()){email.setError("Fill in all three fields");return;}dialog.dismiss();run(()->{
   JSONObject data=new JSONObject().put("email",e).put("password",p).put("pin",n);Vault.save(this,data);getSharedPreferences("status",0).edit().clear().commit();
   HyundaiApi api=new HyundaiApi(this);JSONArray list=api.vehicles();if(list.length()==0)return "No enrolled vehicles found.";
   runOnUiThread(()->chooseVehicle(list));return "Signed in. Select your vehicle.";
  });}));dialog.show();
 }
 private void chooseVehicle(JSONArray list){
  if(isFinishing()||isDestroyed())return;
  String[] names=new String[list.length()];try{for(int i=0;i<names.length;i++){JSONObject v=list.getJSONObject(i).getJSONObject("vehicleDetails");String vin=v.getString("vin");names[i]=v.optString("modelYear")+" "+v.optString("modelCode")+" • …"+vin.substring(Math.max(0,vin.length()-6));}}catch(Exception e){message.setText("Unexpected vehicle list format.");return;}
  new AlertDialog.Builder(this).setTitle("Choose your IONIQ 6").setItems(names,(d,index)->run(()->{HyundaiApi api=new HyundaiApi(this);api.select(list.getJSONObject(index).getJSONObject("vehicleDetails"));api.status(false);RefreshJob.schedule(this);return "Vehicle connected. You can now add the widget.";})).show();
 }
}
