package com.ioniq.widget;
public final class VehicleSignalTest {
 static void check(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  check(VehicleSignal.path("lights").endsWith("/rhl/light"));check(!VehicleSignal.path("lights").equals(VehicleSignal.path("horn_lights")));
  check(VehicleSignal.path("horn_lights").endsWith("/rhl/hnl"));check(VehicleSignal.serviceType("lights").equals("LIGHTS_ONLY"));check(VehicleSignal.serviceType("horn_lights").equals("HORN_AND_LIGHTS"));
  for(String action:new String[]{"lock","unlock","climate","climate_stop","charge_start","charge_stop","limits"})check(VehicleSignal.serviceType(action).equals("REMOTE_POLL"));
  boolean rejected=false;try{VehicleSignal.path("unlock");}catch(IllegalArgumentException e){rejected=true;}check(rejected);
  System.out.println("13 signal-command checks passed");
 }
}
