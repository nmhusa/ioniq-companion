package com.ioniq.widget;
import android.app.job.*;
import android.content.*;
import android.os.*;
/** Local-only repaint to recover a stale pending badge after process termination. */
public class WidgetFeedbackJob extends JobService {
 private static final int ID=62;
 static void schedule(Context c){JobScheduler jobs=c.getSystemService(JobScheduler.class);if(jobs!=null)jobs.schedule(new JobInfo.Builder(ID,new ComponentName(c,WidgetFeedbackJob.class)).setMinimumLatency(60000).setOverrideDeadline(120000).build());}
 static void cancel(Context c){JobScheduler jobs=c.getSystemService(JobScheduler.class);if(jobs!=null)jobs.cancel(ID);}
 @Override public boolean onStartJob(JobParameters params){
  ActionWidget.renderAll(this);
  if(CommandService.running)new Handler(Looper.getMainLooper()).post(()->{if(CommandService.running)schedule(getApplicationContext());});
  return false;
 }
 @Override public boolean onStopJob(JobParameters params){return true;}
}
