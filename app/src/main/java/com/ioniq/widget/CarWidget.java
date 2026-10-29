package com.ioniq.widget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.widget.RemoteViews;
import org.json.JSONObject;
public class CarWidget extends AppWidgetProvider {
 static android.app.PendingIntent open(Context c,String action,int code){return PendingIntent.getActivity(c,code,new Intent(c,MainActivity.class).setAction("com.ioniq."+action).putExtra("action",action),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public static void render(Context c){
  AppWidgetManager m=AppWidgetManager.getInstance(c);int[] ids=m.getAppWidgetIds(new ComponentName(c,CarWidget.class));
  for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);
   v.setTextViewText(R.id.vehicle_name,VehicleIdentity.title(c)+" · US");
   try {JSONObject s=new JSONObject(SecurePrefs.get(c,"status").getString("json","{}"));v.setTextViewText(R.id.battery,StatusText.battery(s));v.setTextViewText(R.id.detail,StatusText.details(s));v.setTextViewText(R.id.time,StatusText.time(s)+(SecurePrefs.get(c,"status").contains("error")?" • Refresh failed":""));}catch(Exception ignored){}
   v.setOnClickPendingIntent(R.id.root,open(c,"open",0));v.setOnClickPendingIntent(R.id.refresh,open(c,"refresh",1));v.setOnClickPendingIntent(R.id.lock,open(c,"lock",2));v.setOnClickPendingIntent(R.id.climate,open(c,"climate",3));m.updateAppWidget(id,v);
  }
 }
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c);RefreshJob.schedule(c);}
 @Override public void onDisabled(Context c){((android.app.job.JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(61);}
}
