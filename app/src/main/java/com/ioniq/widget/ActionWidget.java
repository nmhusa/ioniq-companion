package com.ioniq.widget;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.RemoteViews;
public abstract class ActionWidget extends AppWidgetProvider {
 protected abstract String action();
 static final String[] ACTIONS={"lock","unlock","climate","climate_stop","charge_start","charge_stop"};
 static final String[] LABELS={"Lock doors","Unlock doors","Climate on","Climate off","Start charging","Stop charging"};
 static final int[] ICONS={R.drawable.ic_lock,R.drawable.ic_unlock,R.drawable.ic_climate,R.drawable.ic_climate_stop,R.drawable.ic_charge_start,R.drawable.ic_charge_stop};
 static final int[] BACKGROUNDS={R.drawable.tile_lock,R.drawable.tile_unlock,R.drawable.tile_climate,R.drawable.tile_climate_stop,R.drawable.tile_charge_start,R.drawable.tile_charge_stop};
 static final Class<?>[] TYPES={LockWidget.class,UnlockWidget.class,ClimateWidget.class,ClimateStopWidget.class,ChargeStartWidget.class,ChargeStopWidget.class};
 static int index(String a){for(int i=0;i<ACTIONS.length;i++)if(ACTIONS[i].equals(a))return i;return -1;}
 static void renderAll(Context c){for(int i=0;i<ACTIONS.length;i++)render(c,i);}
 static void render(Context c,int i){AppWidgetManager m=AppWidgetManager.getInstance(c);for(int id:m.getAppWidgetIds(new ComponentName(c,TYPES[i]))){
  String label=LABELS[i];if(ACTIONS[i].equals("climate"))label+=" · "+c.getSharedPreferences("ui",0).getInt("temp",72)+"°F";
  android.content.SharedPreferences s=c.getSharedPreferences("actions",0);String result=s.getString(ACTIONS[i],"Tap to send");if(result.equals("Sending…")&&!CommandService.running)result="Unconfirmed";long t=s.getLong(ACTIONS[i]+"_time",0);
  if(t>0)result+=" · "+android.text.format.DateFormat.getTimeFormat(c).format(new java.util.Date(t));
  Intent intent=new Intent(c,CommandService.class).setAction(ACTIONS[i]);
  PendingIntent tap=PendingIntent.getForegroundService(c,i+100,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  RemoteViews compact=layout(c,i,true,label,result,tap),large=layout(c,i,false,label,result,tap),views;
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
 private static RemoteViews layout(Context c,int i,boolean compact,String label,String result,PendingIntent tap){
  RemoteViews v=new RemoteViews(c.getPackageName(),compact?R.layout.action_widget_compact:R.layout.action_widget);
  v.setImageViewResource(R.id.action_icon,ICONS[i]);
  if(!compact){v.setInt(R.id.action_root,"setBackgroundResource",BACKGROUNDS[i]);v.setTextViewText(R.id.action_label,label);v.setTextViewText(R.id.action_result,result);}
  v.setContentDescription(R.id.action_root,label+". "+result+". Double tap to send command.");
  v.setOnClickPendingIntent(R.id.action_root,tap);return v;
 }
 @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,android.os.Bundle options){render(c,index(action()));}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c,index(action()));}
}
