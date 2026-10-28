package com.ioniq.widget;
import android.app.job.*;
import android.content.*;
public class RefreshJob extends JobService {
 // One queue serializes credential changes, reads, and commands within the process.
 static final java.util.concurrent.ExecutorService QUEUE=java.util.concurrent.Executors.newSingleThreadExecutor();
 static void schedule(Context c){
  JobScheduler j=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  if(j.getPendingJob(61)==null)j.schedule(new JobInfo.Builder(61,new ComponentName(c,RefreshJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(60*60*1000L).build());
 }
 private volatile boolean stopped;
 @Override public boolean onStartJob(JobParameters p){stopped=false;QUEUE.execute(()->{
  try{if(!Vault.read(this).optString("vin").isEmpty())VehicleMileage.refresh(this,false);}catch(Exception e){getSharedPreferences("status",0).edit().putBoolean("error",true).apply();}
  CarWidget.render(this);if(!stopped)jobFinished(p,false);
 });return true;}
 @Override public boolean onStopJob(JobParameters p){stopped=true;return false;}
}
