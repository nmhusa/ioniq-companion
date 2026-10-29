package com.ioniq.watch;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;import java.lang.reflect.*;
public final class WatchInstrumentation extends Instrumentation {
 private int checks;private void check(boolean b){checks++;if(!b)throw new AssertionError("Check "+checks);}
 private Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 private void swipe(WatchActivity a,float from,float to){long t=SystemClock.uptimeMillis();for(int n=0;n<3;n++){MotionEvent e=MotionEvent.obtain(t,t+n*40,n==0?MotionEvent.ACTION_DOWN:n==1?MotionEvent.ACTION_MOVE:MotionEvent.ACTION_UP,210,from+(to-from)*n/2,0);a.dispatchTouchEvent(e);e.recycle();}}
 @Override public void onStart(){Bundle result=new Bundle();try{
  WatchActivity a=(WatchActivity)startActivitySync(new Intent(getTargetContext(),WatchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();SystemClock.sleep(1000);waitForIdleSync();
  java.util.concurrent.atomic.AtomicReference<Throwable> error=new java.util.concurrent.atomic.AtomicReference<>();runOnMainSync(()->{try{
   LinearLayout body=(LinearLayout)field(a,"body");check(body.getChildCount()==2);float density=a.getResources().getDisplayMetrics().density;
   for(int i=0;i<2;i++){Button b=(Button)body.getChildAt(i);check(!b.isEnabled());check(b.getHeight()>=Math.round(48*density));check(b.getTop()>=0&&b.getBottom()<=body.getHeight());}
   for(String key:new String[]{"enabled","phone","session","lastSync"}){Field f=WatchActivity.class.getDeclaredField(key);f.setAccessible(true);f.set(a,key.equals("enabled")?Boolean.TRUE:key.equals("lastSync")?Long.valueOf(System.currentTimeMillis()):"test-peer");}
   Method ready=WatchActivity.class.getDeclaredMethod("updateEnabled");ready.setAccessible(true);ready.invoke(a);check(((Button)body.getChildAt(0)).isEnabled());
   for(int i=1;i<=4;i++){swipe(a,310,160);check(((Integer)field(a,"page"))==i);}swipe(a,310,160);check(((Integer)field(a,"page"))==4);
   swipe(a,160,310);check(((Integer)field(a,"page"))==3);check(!((org.json.JSONObject)field(a,"last")).has("id"));
   a.recreate();
  }catch(Throwable e){error.set(e);}});if(error.get()!=null)throw error.get();waitForIdleSync();
  result.putString("watch_result","PASS: "+checks+" compact watch UI checks");finish(-1,result);
 }catch(Throwable e){result.putString("watch_result","FAIL: "+e.getClass().getSimpleName()+" after "+checks+" checks");finish(0,result);}}
}
