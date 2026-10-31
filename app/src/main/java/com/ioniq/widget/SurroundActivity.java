package com.ioniq.widget;
import android.app.*;import android.os.*;import android.graphics.*;import android.view.*;import android.widget.*;
public final class SurroundActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());private TextView status,time,closure;private ImageView image;private Button load,request;private LinearLayout tabs;private int selected;private SurroundSnapshot shown;private float down;
 private final Runnable tick=new Runnable(){public void run(){SurroundSession.bind(SurroundActivity.this);render();handler.postDelayed(this,500);}};
 @Override public void onCreate(Bundle saved){super.onCreate(saved);if(saved!=null)selected=saved.getInt("view",0);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(16),dp(16),dp(12));root.setBackgroundColor(0xff0b1720);setContentView(root);
  Button back=button("‹  Surroundings",()->finish());root.addView(back);time=text(root,16);status=text(root,15);closure=text(root,14);
  tabs=new LinearLayout(this);HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.addView(tabs);root.addView(scroll);
  String[] names={"Overview","Front","Rear","Left","Right"};for(int i=0;i<names.length;i++){final int n=i;tabs.addView(button(names[i],()->{selected=n;display();}));}
  image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setContentDescription("Saved surround-view snapshot");root.addView(image,new LinearLayout.LayoutParams(-1,0,1));
  image.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){down=e.getX();return true;}if(e.getAction()==MotionEvent.ACTION_UP){float delta=e.getX()-down;if(Math.abs(delta)>dp(50)&&shown!=null&&shown.split()){selected=(selected+(delta<0?1:4))%5;display();}else v.performClick();return true;}return true;});
  image.setOnClickListener(v->{if(shown==null)return;ImageView large=new ImageView(this);large.setAdjustViewBounds(true);large.setImageBitmap(shown.view(selected));new AlertDialog.Builder(this).setView(large).setPositiveButton("Close",null).show();});
  load=button("Load latest snapshot",()->SurroundSession.start(this,false));request=button("Request new snapshot",()->new AlertDialog.Builder(this).setTitle("Request camera images?").setMessage("Park the vehicle, switch it off and ensure nobody is using it. Remote capture may activate vehicle equipment. Images may take about two minutes and are not live. Never use these snapshots to judge whether it is safe to move the car.").setNegativeButton("Cancel",null).setPositiveButton("Request",(d,w)->SurroundSession.start(this,true)).show());root.addView(load);root.addView(request);
 }
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(0xffa4edda);b.setOnClickListener(v->action.run());return b;}
 private TextView text(LinearLayout root,int size){TextView t=new TextView(this);t.setTextColor(0xffbdd0db);t.setTextSize(size);t.setPadding(0,dp(5),0,dp(5));root.addView(t);return t;}
 private void render(){status.setText(SurroundSession.message);load.setEnabled(!SurroundSession.busy);request.setEnabled(!SurroundSession.busy);if(shown!=SurroundSession.snapshot){shown=SurroundSession.snapshot;display();}}
 private void display(){if(shown==null){image.setImageDrawable(null);time.setText("No snapshot loaded");closure.setText("");tabs.setVisibility(View.GONE);return;}time.setText("Captured: "+(shown.timestamp.isEmpty()?"time unavailable":shown.timestamp)+" · not live");closure.setText(shown.closure+(shown.split()?"":"\nUnrecognized layout: showing full image."));tabs.setVisibility(shown.split()?View.VISIBLE:View.GONE);image.setImageBitmap(shown.view(selected));for(int i=0;i<tabs.getChildCount();i++)tabs.getChildAt(i).setAlpha(i==selected?1f:0.55f);}
 @Override protected void onResume(){super.onResume();handler.post(tick);}
 @Override protected void onPause(){handler.removeCallbacks(tick);super.onPause();}
 @Override protected void onSaveInstanceState(Bundle out){out.putInt("view",selected);super.onSaveInstanceState(out);}
}
