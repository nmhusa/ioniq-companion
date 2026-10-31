package com.ioniq.widget;
import android.content.Context;
import android.os.*;
/** Only memory-held images, scoped to the selected account and vehicle. */
final class SurroundSession {
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static String scope="",baseline="";private static long generation,deadline;
 static volatile SurroundSnapshot snapshot;static volatile String message="Load the latest saved snapshot, or request a new one.";
 static volatile boolean busy;private static boolean capture;private static Context context;
 static synchronized void bind(Context c){String current=VehicleGarage.scope(c);if(!current.equals(scope)){scope=current;generation++;busy=false;capture=false;snapshot=null;message="Load the latest saved snapshot, or request a new one.";}context=c.getApplicationContext();}
 static synchronized void start(Context c,boolean fresh){bind(c);if(busy)return;if(scope.isEmpty()){message="Connect and select a vehicle first.";return;}
  if(fresh){long last=SecurePrefs.get(c,"surround_requests").getLong(scope,0);long now=System.currentTimeMillis();if(now-last<180000){message="A capture was recently requested. Wait three minutes between requests; use Load latest to check for its result.";return;}}
  busy=true;capture=fresh;deadline=SystemClock.elapsedRealtime()+180000;long id=++generation;message=fresh?"Preparing request…":"Loading latest snapshot…";step(id,true);
 }
 private static void step(long id,boolean first){RefreshJob.QUEUE.execute(()->{
  Context c;String selected;boolean fresh;
  synchronized(SurroundSession.class){if(id!=generation||!busy)return;c=context;selected=scope;fresh=capture;}
  try{
   if(!selected.equals(VehicleGarage.scope(c)))throw new Exception("Vehicle changed. Camera request cancelled.");
   HyundaiApi api=new HyundaiApi(c);SurroundSnapshot next=SurroundSnapshot.parse(api.surroundDetails());
   synchronized(SurroundSession.class){if(id!=generation||!selected.equals(VehicleGarage.scope(c)))return;
    if(first&&fresh){baseline=next==null?"":next.timestamp;if(next!=null)snapshot=next;}
    else if(!fresh){snapshot=next;busy=false;message=next==null?"No saved camera snapshot returned. Your vehicle may not support remote surround view.":"Saved snapshot loaded—not live.";return;}
    else if(next!=null&&!next.timestamp.isEmpty()&&!next.timestamp.equals(baseline)){snapshot=next;busy=false;message="New snapshot received—not live.";return;}
   }
   if(first&&fresh){
    synchronized(SurroundSession.class){if(id!=generation||!selected.equals(VehicleGarage.scope(c)))return;
     // Persist cooldown before sending: process death or an ambiguous POST must not trigger a duplicate.
     if(!SecurePrefs.get(c,"surround_requests").edit().putLong(selected,System.currentTimeMillis()).commit())throw new Exception("Could not record the request. Nothing was sent.");
    }
    api.surroundCapture();
   }
   synchronized(SurroundSession.class){if(id!=generation)return;if(SystemClock.elapsedRealtime()>=deadline){busy=false;message="No new snapshot confirmed within three minutes. No capture retried. Use Load latest later.";return;}message="Preparing images—may take about 2 minutes. You can return to the app; keep it open while waiting.";}
   main.postDelayed(()->step(id,false),10000);
  }catch(Exception e){synchronized(SurroundSession.class){if(id==generation){busy=false;message="Surround view: "+ErrorText.message(e)+" No capture was retried.";}}}
 });}
}
