package com.ioniq.widget;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.RemoteViews;
public abstract class ActionWidget extends AppWidgetProvider {
 protected abstract String action();
 static final String[] ACTIONS={"lock","unlock","climate","climate_stop","charge_start","charge_stop"};
 static final String[] LABELS={"Lock doors","Unlock doors","Climate on","Climate off","Start charging","Stop charging"};
 static final String[] SHORT_LABELS={"Lock","Unlock","Climate on","Climate off","Charge","Stop"};
 static final int[] ICONS={R.drawable.ic_lock,R.drawable.ic_unlock,R.drawable.ic_climate,R.drawable.ic_climate_stop,R.drawable.ic_charge_start,R.drawable.ic_charge_stop};
 static final int[] BACKGROUNDS={R.drawable.tile_lock,R.drawable.tile_unlock,R.drawable.tile_climate,R.drawable.tile_climate_stop,R.drawable.tile_charge_start,R.drawable.tile_charge_stop};
 static final Class<?>[] TYPES={LockWidget.class,UnlockWidget.class,ClimateWidget.class,ClimateStopWidget.class,ChargeStartWidget.class,ChargeStopWidget.class};
 static int index(String a){for(int i=0;i<ACTIONS.length;i++)if(ACTIONS[i].equals(a))return i;return -1;}
 static void renderAll(Context c){for(int i=0;i<ACTIONS.length;i++)render(c,i);}
 static void render(Context c,int i){AppWidgetManager m=AppWidgetManager.getInstance(c);for(int id:m.getAppWidgetIds(new ComponentName(c,TYPES[i]))){
  String label=LABELS[i];if(ACTIONS[i].equals("climate"))label+=" · "+c.getSharedPreferences("ui",0).getInt("temp",72)+"°F";
  android.content.SharedPreferences s=SecurePrefs.get(c,"actions");String stored=s.getString(ACTIONS[i],"");long t=s.getLong(ACTIONS[i]+"_time",0);
  boolean active=CommandService.running,own=active&&ACTIONS[i].equals(CommandService.activeAction);
  WidgetFeedback.State state=WidgetFeedback.state(stored,own,active&&!own);
  String result=WidgetFeedback.description(state);if(t>0&&state!=WidgetFeedback.State.BUSY)result+=" · "+android.text.format.DateFormat.getTimeFormat(c).format(new java.util.Date(t));
  Intent intent=new Intent(c,CommandService.class).setAction(ACTIONS[i]);
  PendingIntent tap="unlock".equals(ACTIONS[i])&&UnlockGuard.enabled(c)?PendingIntent.getActivity(c,i+100,new Intent(c,UnlockActivity.class).setAction("protected_unlock").putExtra("mode","unlock"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE):PendingIntent.getForegroundService(c,i+100,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Intent detailIntent=new Intent(c,MainActivity.class).setAction("widget_result_"+ACTIONS[i]).putExtra("widget_result",ACTIONS[i]).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent details=PendingIntent.getActivity(c,i+200,detailIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  RemoteViews compact=layout(c,i,true,label,result,state,tap,details),large=layout(c,i,false,label,result,state,tap,details),views;
  if(android.os.Build.VERSION.SDK_INT>=31){
   java.util.Map<android.util.SizeF,RemoteViews> sizes=new java.util.LinkedHashMap<>();
   sizes.put(new android.util.SizeF(40,40),compact);sizes.put(new android.util.SizeF(110,130),large);
   views=new RemoteViews(sizes);
  }else{
   android.os.Bundle options=m.getAppWidgetOptions(id);
   views=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,40)>=110&&options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,40)>=130?large:compact;
  }
  m.updateAppWidget(id,views);
 }}
 private static RemoteViews layout(Context c,int i,boolean compact,String label,String result,WidgetFeedback.State state,PendingIntent tap,PendingIntent details){
  RemoteViews v=new RemoteViews(c.getPackageName(),compact?R.layout.action_widget_compact:R.layout.action_widget);
  v.setImageViewResource(R.id.action_icon,ICONS[i]);
  int background=state==WidgetFeedback.State.CONFIRMED?R.drawable.widget_feedback_success:
   state==WidgetFeedback.State.QUEUED||state==WidgetFeedback.State.SENDING||state==WidgetFeedback.State.BUSY?R.drawable.widget_feedback_busy:
   state==WidgetFeedback.State.ERROR||state==WidgetFeedback.State.UNCONFIRMED?R.drawable.widget_feedback_attention:R.drawable.widget_feedback_idle;
  int color=state==WidgetFeedback.State.CONFIRMED?0xffb4f4df:state==WidgetFeedback.State.IDLE?0xffe3edf3:0xffffdc8a;
  if(!compact){v.setInt(R.id.action_root,"setBackgroundResource",BACKGROUNDS[i]);v.setTextViewText(R.id.action_label,label);}
  else v.setInt(R.id.action_root,"setBackgroundResource",state==WidgetFeedback.State.IDLE?R.drawable.widget_feedback_touch:background);
  v.setInt(R.id.action_result,"setBackgroundResource",background);
  v.setTextColor(R.id.action_result,color);
  v.setTextViewText(R.id.action_result,compact?WidgetFeedback.badge(state,SHORT_LABELS[i]):result);
  v.setInt(R.id.action_icon,"setImageAlpha",state==WidgetFeedback.State.BUSY?120:255);
  v.setContentDescription(R.id.action_root,label+(i==2?". "+ClimatePreset.summary(c):"")+". "+result+". Activate icon to send. Status opens details.");
  v.setContentDescription(R.id.action_result,label+". "+result+". Open command details.");
  v.setOnClickPendingIntent(R.id.action_result,details);
  v.setOnClickPendingIntent(R.id.action_root,tap);return v;
 }
 @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,android.os.Bundle options){render(c,index(action()));}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c,index(action()));}
}
