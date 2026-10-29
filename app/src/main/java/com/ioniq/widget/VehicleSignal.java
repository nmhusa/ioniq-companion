package com.ioniq.widget;
// US request paths and polling types verified against Hyundai-Kia-Connect/HyundaiBlueLinkApiUSA.py.
final class VehicleSignal {
 static boolean isSignal(String action){return "lights".equals(action)||"horn_lights".equals(action);}
 static String path(String action){if("lights".equals(action))return "ac/v2/rcs/rhl/light";if("horn_lights".equals(action))return "ac/v2/rcs/rhl/hnl";throw new IllegalArgumentException("Unsupported vehicle signal");}
 static String serviceType(String action){return "lights".equals(action)?"LIGHTS_ONLY":"horn_lights".equals(action)?"HORN_AND_LIGHTS":"REMOTE_POLL";}
 static String title(String action){return "lights".equals(action)?"Flash lights":"Horn & lights";}
}
