package com.ioniq.widget;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.TextView;
import org.json.JSONObject;
/** Scalable car-battery silhouette; live text remains accessible and selectable by TalkBack. */
final class AuxBatteryView extends TextView {
 private final BatteryShape shape=new BatteryShape();
 AuxBatteryView(Context c){super(c);setTextSize(12);setGravity(Gravity.CENTER);setPadding(dp(8),dp(15),dp(8),dp(7));setMinHeight(dp(64));setFocusable(true);setBackground(shape);setElevation(dp(2));show(new JSONObject(),System.currentTimeMillis());}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 void show(JSONObject report,long now){double p=AuxBattery.percent(report);boolean old=AuxBattery.age(report,now)>86400000;int color=AuxBattery.color(report,now);setText("12V\n"+(Double.isNaN(p)?"Unknown":String.format(java.util.Locale.US,"%.0f%%",p))+(old&&!Double.isNaN(p)?"\nold":""));setTextColor(color);shape.color=color;shape.level=Double.isNaN(p)?-1:(float)(p/100);shape.invalidateSelf();setContentDescription("12-volt car battery: "+AuxBattery.value(report,now)+". Tap for report age and details.");}
 private final class BatteryShape extends Drawable {
  final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);int color=0xffbcd6e3;float level=-1;
  @Override public void draw(Canvas canvas){Rect b=getBounds();float left=b.left+dp(2),right=b.right-dp(2),top=b.top+dp(10),bottom=b.bottom-dp(2),width=right-left;RectF body=new RectF(left,top,right,bottom);
   paint.setStyle(Paint.Style.FILL);paint.setShader(null);paint.setColor(color);canvas.drawRoundRect(new RectF(left+width*.17f,b.top+dp(2),left+width*.34f,top+dp(1)),dp(2),dp(2),paint);canvas.drawRoundRect(new RectF(left+width*.66f,b.top+dp(2),left+width*.83f,top+dp(1)),dp(2),dp(2),paint);
   paint.setShader(new LinearGradient(0,top,0,bottom,new int[]{0xff27434c,0xff10232e},null,Shader.TileMode.CLAMP));canvas.drawRoundRect(body,dp(7),dp(7),paint);paint.setShader(null);
   if(level>=0){canvas.save();Path clip=new Path();clip.addRoundRect(body,dp(7),dp(7),Path.Direction.CW);canvas.clipPath(clip);paint.setColor((color&0x00ffffff)|0x26000000);canvas.drawRect(left,bottom-(bottom-top)*level,right,bottom,paint);canvas.restore();}
   paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));paint.setColor(color);canvas.drawRoundRect(body,dp(7),dp(7),paint);
   paint.setStrokeWidth(dp(1));float y=top+dp(7);canvas.drawLine(left+dp(6),y,left+dp(12),y,paint);canvas.drawLine(right-dp(12),y,right-dp(6),y,paint);canvas.drawLine(right-dp(9),y-dp(3),right-dp(9),y+dp(3),paint);
  }
  @Override public void setAlpha(int alpha){} @Override public void setColorFilter(ColorFilter filter){} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
}
