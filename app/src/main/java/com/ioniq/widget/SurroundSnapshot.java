package com.ioniq.widget;
import android.graphics.*;
import android.util.Base64;
import org.json.*;
final class SurroundSnapshot {
 final Bitmap image;final int[] sizes;final String timestamp,closure;
 SurroundSnapshot(Bitmap b,int[] s,String t,String c){image=b;sizes=s;timestamp=t;closure=c;}
 static SurroundSnapshot parse(JSONObject response)throws Exception{
  JSONArray list=response.optJSONArray("svmDetails");
  if(list==null||list.length()==0)return null;
  JSONObject entry=list.optJSONObject(0),d=entry==null?null:entry.optJSONObject("svmDetail");
  if(d==null)return null;
  String encoded=d.optString("svmImage","");if(encoded.isEmpty())return null;
  if(encoded.length()>15000000)throw new Exception("Camera image is too large to display safely.");
  byte[] bytes;try{bytes=Base64.decode(encoded,Base64.DEFAULT);}catch(IllegalArgumentException e){throw new Exception("Hyundai returned an unreadable camera image.");}
  BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);
  if(o.outWidth<=0||o.outHeight<=0||o.outWidth>16000||o.outHeight>8000||(long)o.outWidth*o.outHeight>16000000)throw new Exception("Camera image dimensions are unsupported.");
  o.inJustDecodeBounds=false;Bitmap b=BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(b==null)throw new Exception("Camera image could not be decoded.");
  JSONArray raw=d.optJSONArray("imageSize");int[] sizes=null;if(raw!=null&&raw.length()>=5&&raw.length()<=32){sizes=new int[raw.length()];for(int i=0;i<sizes.length;i++)sizes[i]=raw.optInt(i,-1);}
  JSONObject gps=d.optJSONObject("gpsDetail");String stamp=gps==null?"":gps.optString("time","");
  JSONObject doors=d.optJSONObject("doorOpen");boolean known=true,open=false;
  for(String k:new String[]{"frontLeft","frontRight","backLeft","backRight"}){Boolean v=flag(doors==null?null:doors.opt(k));known&=v!=null;open|=Boolean.TRUE.equals(v);}
  Boolean trunk=flag(d.opt("trunkOpen"));known&=trunk!=null;open|=Boolean.TRUE.equals(trunk);
  return new SurroundSnapshot(b,sizes,stamp,open?"Door or trunk open at capture":known?"Doors & trunk closed at capture":"Door/trunk state unavailable at capture");
 }
 private static Boolean flag(Object v){if(v==null||v==JSONObject.NULL)return null;String s=v.toString();if("true".equalsIgnoreCase(s)||"1".equals(s))return true;if("false".equalsIgnoreCase(s)||"0".equals(s))return false;return null;}
 Bitmap view(int index){int[] box=SurroundLayout.rectangle(sizes,image.getWidth(),image.getHeight(),index);return box==null?image:Bitmap.createBitmap(image,box[0],box[1],box[2],box[3]);}
 boolean split(){return SurroundLayout.rectangle(sizes,image.getWidth(),image.getHeight(),0)!=null;}
}
