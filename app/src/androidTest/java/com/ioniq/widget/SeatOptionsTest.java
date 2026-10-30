package com.ioniq.widget;
import org.json.*;
public class SeatOptionsTest {
 static int checks;static void check(boolean b){checks++;if(!b)throw new AssertionError("Seat check "+checks);}
 static JSONObject seat(int id,String heat,String cool,String levels)throws Exception{return new JSONObject().put("seatLocationID",id).put("heatingCapable",heat).put("ventCapable",cool).put("supportedLevels",levels);}
 public static void main(String[] args)throws Exception{
  JSONObject d=new JSONObject().put("modelYear",2026).put("evStatus","E").put("vehicleGeneration",3);check(!SeatOptions.any(SeatOptions.capabilities(d)));
  JSONArray seats=new JSONArray().put(seat(1,"YES","YES","2,3,4,5,6,7,8")).put(seat(2,"YES","NO","2,6,7"));d.put("seatConfigurations",new JSONObject().put("seatConfigs",seats));JSONObject caps=SeatOptions.capabilities(d);check(SeatOptions.levels(caps,0).length()==7);check(SeatOptions.levels(caps,1).length()==3);check(SeatOptions.levels(caps,2).length()==0);check(!SeatOptions.allowed(caps,1,3));check(!SeatOptions.allowed(caps,1,8));
  JSONObject saved=new JSONObject().put("enabled",true).put("values",new JSONArray().put(5).put(6).put(8).put(8));JSONObject command=SeatOptions.command(caps,saved);check(command.getInt("drvSeatHeatState")==5);check(command.getInt("astSeatHeatState")==6);check(command.getInt("rlSeatHeatState")==0);check(command.getInt("rrSeatHeatState")==0);saved.put("enabled",false);check(SeatOptions.command(caps,saved)==null);saved.put("enabled",true).put("values",new JSONArray().put(5).put(8));boolean rejected=false;try{SeatOptions.command(caps,saved);}catch(Exception e){rejected=true;}check(rejected);
  for(Object gen:new Object[]{2,0,4,"unknown",JSONObject.NULL}){d.put("vehicleGeneration",gen);check(!SeatOptions.any(SeatOptions.capabilities(d)));}d.put("vehicleGeneration",3);d.put("evStatus","N");check(!SeatOptions.any(SeatOptions.capabilities(d)));d.put("evStatus","E");
  seats.put(seat(1,"YES","YES","2,6"));check(SeatOptions.levels(SeatOptions.capabilities(d),0).length()==0);
  for(String levels:new String[]{"","6,7,8","2","2,999","2,3,4,5"}){d.put("seatConfigurations",new JSONObject().put("seatConfigs",new JSONArray().put(seat(1,"YES","NO",levels))));check(!SeatOptions.any(SeatOptions.capabilities(d)));}
  check(SeatOptions.label(2).equals("Off"));check(SeatOptions.label(3).equals("Cooling · low"));check(SeatOptions.label(8).equals("Heating · high"));System.out.println("Passed "+checks+" seat capability and payload checks");
 }
}
