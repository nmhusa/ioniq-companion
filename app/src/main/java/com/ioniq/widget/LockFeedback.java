package com.ioniq.widget;
/** Guidance only: never infers Utility Mode or changes the saved vehicle lock state. */
final class LockFeedback {
 static String describe(String action,String result){
  if(!"lock".equals(action)||result!=null&&result.startsWith("Hyundai reports command completed"))return result;
  return "Lock not confirmed. Check the doors before leaving.\n\nIf the car is on or in Utility Mode, switch it off before trying again. Other causes include connectivity or account problems. No automatic retry.\n\nDetails: "+(result==null?"No confirmation received.":result);
 }
}
