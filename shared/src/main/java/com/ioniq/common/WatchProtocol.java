package com.ioniq.common;
public final class WatchProtocol {
 private WatchProtocol(){}
 public static final String CAPABILITY="ioniq_phone_controls_v1",STATUS="/ioniq/watch/status",COMMAND="/ioniq/watch/command",RESULT="/ioniq/watch/result";
 public static final String[] ACTIONS={"lock","unlock","climate","climate_stop","charge_start","charge_stop","lights","horn_lights"};
 public static boolean allowed(String action){for(String a:ACTIONS)if(a.equals(action))return true;return false;}
 public static boolean validId(String id){return id!=null&&id.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");}
 public static boolean fresh(long sent,long now){return sent>0&&sent>=now-60000&&sent<=now+10000;}
 public static boolean pending(String state){return "queued".equals(state)||"sending".equals(state);}
}
