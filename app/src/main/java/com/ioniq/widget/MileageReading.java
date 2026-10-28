package com.ioniq.widget;

/** Units used by the US Hyundai API: 1 = kilometres, 3 = miles. */
final class MileageReading {
 final double value; final int unit; final String reportTime;
 private MileageReading(double value,int unit,String reportTime){this.value=value;this.unit=unit;this.reportTime=reportTime;}
 static MileageReading parse(String raw,int unit,String date){
  if(raw==null || (unit!=1 && unit!=3))return null;
  try{double value=Double.parseDouble(raw.trim());if(!Double.isFinite(value)||value<0)return null;return new MileageReading(value,unit,date==null?"":date.trim());}catch(NumberFormatException e){return null;}
 }
 static String reportLabel(String raw){
  if(raw==null||raw.trim().isEmpty()||"null".equals(raw))return "Update time unavailable";
  if(raw.matches("[0-9]{14}"))return raw.substring(0,4)+"-"+raw.substring(4,6)+"-"+raw.substring(6,8)+" "+raw.substring(8,10)+":"+raw.substring(10,12)+":"+raw.substring(12,14)+" (Hyundai time)";
  return raw;
 }
}
