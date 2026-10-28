package com.ioniq.widget;
import android.content.*;
final class NamedClimate {
 static final String[] NAMES={"Winter","Summer","Custom"};
 static void initialize(Context c){SharedPreferences p=c.getSharedPreferences("ui",0);if(!p.contains("preset_Custom_temp"))p.edit().putInt("preset_Custom_temp",p.getInt("temp",72)).putBoolean("preset_Custom_front_defrost",p.getBoolean("front_defrost",false)).putBoolean("preset_Custom_rear_defrost",p.getBoolean("rear_defrost",false)).putBoolean("preset_Custom_steering_heat",p.getBoolean("steering_heat",false)).apply();}
 static String active(Context c){return c.getSharedPreferences("ui",0).getString("preset_name","Custom");}
 static int temp(Context c,String name){SharedPreferences p=c.getSharedPreferences("ui",0);return p.getInt("preset_"+name+"_temp",name.equals("Winter")?74:name.equals("Summer")?70:p.getInt("temp",72));}
 static boolean flag(Context c,String name,String key){SharedPreferences p=c.getSharedPreferences("ui",0);return p.getBoolean("preset_"+name+"_"+key,name.equals("Custom")?p.getBoolean(key,false):name.equals("Winter")&&!key.equals("front_defrost"));}
 static void use(Context c,String name){save(c,name,temp(c,name),flag(c,name,"front_defrost"),flag(c,name,"rear_defrost"),flag(c,name,"steering_heat"));}
 static void save(Context c,String name,int temp,boolean front,boolean rear,boolean wheel){
  if(temp<62||temp>90)throw new IllegalArgumentException("Temperature out of range");
  c.getSharedPreferences("ui",0).edit().putString("preset_name",name).putInt("temp",temp)
   .putBoolean("front_defrost",front).putBoolean("rear_defrost",rear).putBoolean("steering_heat",wheel)
   .putInt("preset_"+name+"_temp",temp).putBoolean("preset_"+name+"_front_defrost",front)
   .putBoolean("preset_"+name+"_rear_defrost",rear).putBoolean("preset_"+name+"_steering_heat",wheel).apply();
 }
}
