package com.ioniq.widget;
import org.json.JSONObject;
public class AuxBatteryTest {
 static int checks;static void check(boolean b){checks++;if(!b)throw new AssertionError("Auxiliary battery check "+checks);}
 public static void main(String[] args)throws Exception{
  long now=java.time.Instant.parse("2026-10-30T18:00:00Z").toEpochMilli();JSONObject s=new JSONObject();check(Double.isNaN(AuxBattery.percent(s)));s.put("evStatus",new JSONObject().put("batteryStatus",90));check(Double.isNaN(AuxBattery.percent(s)));
  for(Object v:new Object[]{0,100,85," 67 ",50.5}){s.put("battery",new JSONObject().put("batSoc",v));check(!Double.isNaN(AuxBattery.percent(s)));}
  for(Object v:new Object[]{-1,101,"NaN","Infinity","",true,JSONObject.NULL,new JSONObject()}){s.put("battery",new JSONObject().put("batSoc",v));check(Double.isNaN(AuxBattery.percent(s)));check(AuxBattery.value(s,now).equals("Unknown"));}
  s.put("battery",new JSONObject().put("batSoc",40)).put("dateTime","2026-10-30T17:00:00Z");check(AuxBattery.color(s,now)==0xffff8c87);check(AuxBattery.details(s,now).contains("1 hours ago"));s.getJSONObject("battery").put("batSoc",60);check(AuxBattery.color(s,now)==0xffffcf72);s.getJSONObject("battery").put("batSoc",80);check(AuxBattery.color(s,now)==0xffbcd6e3);
  s.put("dateTime","2026-10-28T17:00:00Z");check(AuxBattery.value(s,now).contains("old"));check(AuxBattery.color(s,now)==0xffbcd6e3);s.put("dateTime","not a date");check(AuxBattery.age(s,now)==-1);s.put("dateTime","2027-01-01T00:00:00Z");check(AuxBattery.age(s,now)==-1);check(AuxBattery.details(s,now).contains("not Hyundai fault limits"));
  System.out.println("Passed "+checks+" auxiliary battery checks");
 }
}
