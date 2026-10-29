package com.ioniq.watch;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import com.google.android.gms.wearable.*;
import com.ioniq.common.WatchProtocol;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** No Hyundai networking or credentials: this UI sends transient messages to the paired phone. */
public final class WatchActivity extends Activity implements MessageClient.OnMessageReceivedListener {
 private static final String[] PAGES={"DOORS","CLIMATE","CHARGING","FIND CAR","STATUS"};
 private static final String[][] ACTIONS={{"lock","unlock"},{"climate","climate_stop"},{"charge_start","charge_stop"},{"lights","horn_lights"}};
 private static final String[][] LABELS={{"Lock doors","Unlock doors"},{"Climate on","Climate off"},{"Start charge","Stop charge"},{"Flash lights","Horn & lights"}};
 private static final int[][] COLORS={{0xff71d7bd,0xffffcf72},{0xffff8585,0xff80bfff},{0xffb6dc76,0xffffb675},{0xffffcf72,0xffff8585}};
 private final Handler handler=new Handler(Looper.getMainLooper());private LinearLayout root,body;private TextView title,pageLabel,feedback,hint;
 private final ArrayList<Button> controls=new ArrayList<>();private JSONObject snapshot=new JSONObject(),last=new JSONObject();
 private String phone="",session="",query="",detail="Open the phone app and enable Watch controls.";
 private int page;private long lastSync,lastConnect,queryAt;private boolean visible,connecting,enabled,dragging;private float downX,downY;
 private final Runnable tick=new Runnable(){public void run(){if(!visible)return;
  if(WatchProtocol.pending(last.optString("state"))){if(System.currentTimeMillis()-last.optLong("sent")>90000){setResult("unknown","Confirmation is taking too long. Check the phone before trying again.");}else readResult();}
  if(System.currentTimeMillis()-lastSync>30000&&!connecting&&System.currentTimeMillis()-lastConnect>10000)connect();
  updateEnabled();handler.postDelayed(this,5000);
 }};
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff09151e);getWindow().setNavigationBarColor(0xff09151e);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  if(state!=null)page=Math.max(0,Math.min(4,state.getInt("page")));
  try{last=new JSONObject(getSharedPreferences("watch_result",0).getString("last","{}"));if(last.has("detail"))detail=last.optString("detail");}catch(Exception ignored){}
  build();
 }
 @Override protected void onSaveInstanceState(Bundle out){out.putInt("page",page);super.onSaveInstanceState(out);}
 @Override protected void onResume(){super.onResume();visible=true;Wearable.getMessageClient(this).addListener(this).addOnSuccessListener(v->{if(visible)connect();}).addOnFailureListener(e->{detail="Google Play services / Wear OS connection unavailable.";showFeedback();});handler.post(tick);}
 @Override protected void onPause(){visible=false;handler.removeCallbacks(tick);Wearable.getMessageClient(this).removeListener(this);super.onPause();}
 private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(false);return t;}
 private void build(){
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding(dp(22),dp(16),dp(22),dp(16));root.setBackgroundColor(0xff09151e);setContentView(root);
  title=text(snapshot.optString("title","IONIQ"),16,0xffdceaf1);title.setTypeface(null,Typeface.BOLD);title.setMaxLines(1);root.addView(title,new LinearLayout.LayoutParams(-1,dp(21)));
  pageLabel=text(PAGES[page]+" · "+(page+1)+" / 5",10,0xff94cfdf);root.addView(pageLabel,new LinearLayout.LayoutParams(-1,dp(16)));
  body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setGravity(Gravity.CENTER);root.addView(body,new LinearLayout.LayoutParams(-1,0,1));controls.clear();
  if(page<4){for(int i=0;i<2;i++){final String action=ACTIONS[page][i];String label=LABELS[page][i];if("climate".equals(action))label+=" · "+snapshot.optInt("temp",72)+"°F";
    Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTextSize(13);b.setTypeface(null,Typeface.BOLD);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(dp(3),0,dp(3),0);b.setMaxLines(2);
    int color=COLORS[page][i];GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{blend(color,0.40f),0xff14232d});bg.setCornerRadius(dp(22));bg.setStroke(dp(2),color);b.setBackground(bg);b.setElevation(dp(3));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(3),0,dp(3));body.addView(b,lp);controls.add(b);b.setOnClickListener(v->activate(action));
   }}else{
    TextView status=text(snapshot.optString("battery","—%")+" · "+snapshot.optString("range","Range unavailable")+"\n"+snapshot.optString("locked","Lock unknown")+" · "+snapshot.optString("charging","Unknown"),13,0xffa4edda);status.setMaxLines(3);body.addView(status,new LinearLayout.LayoutParams(-1,0,1));
    Button sync=new Button(this);sync.setText("Sync phone status");sync.setAllCaps(false);sync.setTextSize(11);sync.setMinHeight(0);sync.setMinimumHeight(0);sync.setPadding(0,0,0,0);body.addView(sync,new LinearLayout.LayoutParams(-1,dp(40)));sync.setOnClickListener(v->connect());
   }
  feedback=text("",10,0xffb9cbd6);feedback.setPadding(dp(20),0,dp(20),0);feedback.setMaxLines(3);feedback.setEllipsize(android.text.TextUtils.TruncateAt.END);root.addView(feedback,new LinearLayout.LayoutParams(-1,dp(34)));feedback.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Command / report details").setMessage(detail+"\n\nVehicle report: "+snapshot.optString("report","Unavailable")+"\nStatus is cached on the phone; it may be old.").setPositiveButton("Done",null).show());
  hint=text("↑ Swipe pages ↓",9,0xff92a9b6);root.addView(hint,new LinearLayout.LayoutParams(-1,dp(12)));showFeedback();updateEnabled();
 }
 private int blend(int c,float f){return Color.rgb(Math.round(Color.red(c)*f),Math.round(Color.green(c)*f),Math.round(Color.blue(c)*f));}
 @Override public boolean dispatchTouchEvent(android.view.MotionEvent e){
  if(e.getActionMasked()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();dragging=false;}
  float dx=e.getX()-downX,dy=e.getY()-downY;
  if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&!dragging&&Math.abs(dy)>dp(16)&&Math.abs(dy)>Math.abs(dx)*1.4f){dragging=true;MotionEvent cancel=MotionEvent.obtain(e);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();}
  if(dragging){if(e.getActionMasked()==MotionEvent.ACTION_UP){if(Math.abs(dy)>dp(32)){int next=Math.max(0,Math.min(4,page+(dy<0?1:-1)));if(next!=page){page=next;root.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);build();}}dragging=false;}return true;}
  return super.dispatchTouchEvent(e);
 }
 private boolean busy(){return WatchProtocol.pending(last.optString("state"));}
 private void updateEnabled(){boolean ready=enabled&&!session.isEmpty()&&!phone.isEmpty()&&System.currentTimeMillis()-lastSync<60000&&!busy()&&!snapshot.optBoolean("busy");for(Button b:controls){b.setEnabled(ready);b.setAlpha(ready?1f:0.45f);}}
 private void showFeedback(){if(feedback==null)return;String state=last.optString("state");String brief=busy()?("sending".equals(state)?"Sending to Hyundai…":"Waiting for phone…"):"confirmed".equals(state)?"Last command confirmed · tap details":("unknown".equals(state)||"error".equals(state)||"blocked".equals(state))?detail:enabled?"Ready · saved status":"Phone setup needed · tap details";
  feedback.setText(brief);feedback.setTextColor("confirmed".equals(state)?0xff92e4cd:busy()?0xffffcf72:0xffb9cbd6);}
 private void connect(){if(connecting)return;connecting=true;lastConnect=System.currentTimeMillis();
  Wearable.getCapabilityClient(this).getCapability(WatchProtocol.CAPABILITY,CapabilityClient.FILTER_REACHABLE).addOnSuccessListener(info->{connecting=false;if(!visible)return;
   Set<Node> nodes=info.getNodes();if(nodes.size()!=1){enabled=false;session="";phone="";detail=nodes.isEmpty()?"Phone unavailable. Install/update the phone app, pair the watch, and enable Watch controls.":"Multiple phone companions found. Keep only the intended phone connected.";showFeedback();updateEnabled();return;}
   phone=nodes.iterator().next().getId();query=UUID.randomUUID().toString();queryAt=System.currentTimeMillis();try{JSONObject request=new JSONObject().put("query",query);Wearable.getMessageClient(this).sendMessage(phone,WatchProtocol.STATUS,bytes(request)).addOnFailureListener(e->{enabled=false;detail="Could not reach the phone. No command was sent.";showFeedback();updateEnabled();});}catch(Exception ignored){}
   final String issuedQuery=query;final long issuedAt=queryAt;handler.postDelayed(()->{if(query.equals(issuedQuery)&&lastSync<issuedAt&&visible){enabled=false;detail="No reply from phone. Open the phone app, then Sync phone status.";showFeedback();updateEnabled();}},10000);
   if(last.has("id"))readResult();
  }).addOnFailureListener(e->{connecting=false;enabled=false;detail="Phone connection unavailable. Check pairing and Google Play services.";showFeedback();updateEnabled();});
 }
 private static byte[] bytes(JSONObject data){return data.toString().getBytes(StandardCharsets.UTF_8);}
 private void activate(String action){if(!enabled||busy()||System.currentTimeMillis()-lastSync>=60000){connect();return;}
  KeyguardManager k=getSystemService(KeyguardManager.class);if(k!=null&&k.isDeviceLocked()){detail="Unlock the watch first.";showFeedback();return;}
  if("unlock".equals(action)&&snapshot.optBoolean("unlockProtected")){new AlertDialog.Builder(this).setTitle("Unlock protection").setMessage("Use Unlock doors in the phone app to authenticate. Watch controls cannot bypass this setting.").setPositiveButton("Done",null).show();return;}
  if("horn_lights".equals(action)){new AlertDialog.Builder(this).setTitle("Sound horn?").setMessage("This sounds the car horn and flashes its lights.").setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->send(action)).show();return;}send(action);
 }
 private boolean saveLast(){return getSharedPreferences("watch_result",0).edit().putString("last",last.toString()).commit();}
 private void setResult(String state,String message){try{last.put("state",state).put("detail",message);}catch(Exception ignored){}detail=message;saveLast();showFeedback();updateEnabled();}
 private void send(String action){if(busy()||!enabled||System.currentTimeMillis()-lastSync>=60000)return;
  try{last=new JSONObject().put("id",UUID.randomUUID().toString()).put("action",action).put("session",session).put("node",phone).put("sent",System.currentTimeMillis()).put("state","queued").put("detail","Waiting for phone…");if(!saveLast()){setResult("error","Watch storage unavailable. Nothing was sent.");return;}root.performHapticFeedback(HapticFeedbackConstants.CONFIRM);detail="Waiting for phone…";showFeedback();updateEnabled();
   Wearable.getMessageClient(this).sendMessage(phone,WatchProtocol.COMMAND,bytes(last)).addOnFailureListener(e->setResult("unknown","Delivery is uncertain. Check the phone before trying again."));
  }catch(Exception e){setResult("error","Could not send. Check the phone.");}
 }
 private void readResult(){if(!last.has("id"))return;try{JSONObject request=new JSONObject().put("id",last.getString("id")).put("session",last.getString("session"));Wearable.getMessageClient(this).sendMessage(last.getString("node"),WatchProtocol.RESULT,bytes(request)).addOnFailureListener(e->{});}catch(Exception ignored){}}
 @Override public void onMessageReceived(MessageEvent event){if(event.getData().length>8192)return;try{JSONObject data=new JSONObject(new String(event.getData(),StandardCharsets.UTF_8));runOnUiThread(()->{if(!visible)return;
   if(WatchProtocol.STATUS.equals(event.getPath())&&phone.equals(event.getSourceNodeId())&&query.equals(data.optString("query"))){snapshot=data;session=data.optString("session");enabled=data.optBoolean("enabled");lastSync=System.currentTimeMillis();if(!enabled)detail=data.optString("detail","Enable Watch controls on the phone.");else if(!last.has("id"))detail="Connected. Status is the phone’s saved Hyundai report.";build();}
   else if(WatchProtocol.RESULT.equals(event.getPath())&&last.optString("node").equals(event.getSourceNodeId())&&last.optString("id").equals(data.optString("id"))){boolean wasPending=busy();String state=data.optString("state","unknown");if(!busy()&&WatchProtocol.pending(state)&&!"unknown".equals(last.optString("state")))return;setResult(state,data.optString("detail","Check phone for details."));if(!WatchProtocol.pending(state)&&wasPending){root.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);handler.postDelayed(()->{if(visible)connect();},750);}}
  });}catch(Exception ignored){}}
}
