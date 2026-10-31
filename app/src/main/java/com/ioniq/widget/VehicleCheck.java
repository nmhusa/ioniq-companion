package com.ioniq.widget;
import org.json.JSONObject;
/** Cached observations only. Missing or unfamiliar values never mean clear. */
final class VehicleCheck {
 int known,unknown,alerts; final StringBuilder detail=new StringBuilder();
 static Boolean flag(Object value){
  if(Boolean.TRUE.equals(value)||"true".equalsIgnoreCase(String.valueOf(value))||"1".equals(String.valueOf(value)))return true;
  if(Boolean.FALSE.equals(value)||"false".equalsIgnoreCase(String.valueOf(value))||"0".equals(String.valueOf(value)))return false;
  return null;
 }
 private void add(JSONObject s,String path,String label,String active,String clear){
  Object value=s;for(String key:path.split("\\.")){value=value instanceof JSONObject?((JSONObject)value).opt(key):null;}
  Boolean state=flag(value);if(state==null)unknown++;else {known++;if(state)alerts++;}
  detail.append(label).append(": ").append(state==null?"Unavailable":state?active:clear).append("\n");
 }
 static VehicleCheck read(JSONObject s){
  VehicleCheck c=new VehicleCheck();String[] keys={"frontLeft","frontRight","backLeft","backRight"};String[] names={"Front left","Front right","Rear left","Rear right"};
  for(int i=0;i<4;i++)c.add(s,"doorOpen."+keys[i],names[i]+" door","Open","Closed");
  for(int i=0;i<4;i++)c.add(s,"windowOpen."+keys[i],names[i]+" window","Open","Closed");
  c.add(s,"hoodOpen","Hood","Open","Closed");c.add(s,"trunkOpen","Trunk","Open","Closed");
  String[] tires={"FrontLeft","FrontRight","RearLeft","RearRight"};
  for(int i=0;i<4;i++)c.add(s,"tirePressureLamp.tirePressureWarningLamp"+tires[i],names[i]+" tire warning","Warning","No warning reported");
  c.add(s,"tirePressureLamp.tirePressureWarningLampAll","General tire warning","Warning","No warning reported");
  c.add(s,"washerFluidStatus","Washer fluid warning","Warning","No warning reported");return c;
 }
 static boolean recent(JSONObject s){try{long age=java.time.Duration.between(java.time.OffsetDateTime.parse(s.optString("dateTime","")).toInstant(),java.time.Instant.now()).getSeconds();return age>=0&&age<=900;}catch(Exception e){return false;}}
 String summary(){return alerts>0?alerts+" reported alert"+(alerts==1?"":"s"):known==0?"Unavailable":unknown>0?"Partial report":"No alerts reported";}
}
