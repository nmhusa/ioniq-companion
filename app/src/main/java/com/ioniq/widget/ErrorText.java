package com.ioniq.widget;
/** Never show or persist arbitrary exception payloads, URLs, or response bodies. */
final class ErrorText {
 static String message(Exception e){
  if(e instanceof java.net.SocketTimeoutException)return "Network timeout. Outcome may be unknown; check the official app before retrying.";
  String m=e.getMessage();
  if(m!=null){java.util.regex.Matcher code=java.util.regex.Pattern.compile("^(Sign-in|Session refresh|Vehicle list|Vehicle status|Command confirmation|Remote command) failed \\(HTTP ([0-9]{3})\\)\\..*$").matcher(m);
   if(code.matches())return code.group(1)+" failed (HTTP "+code.group(2)+"). "+(code.group(2).equals("401")||code.group(2).equals("403")?"Reconnect your Bluelink account.":code.group(2).equals("429")?"Wait before trying again.":"Check the official app before retrying; command outcome may be unknown.");
   if(m.startsWith("Setup is incomplete."))return "Select your vehicle in Connect / change Bluelink account first.";
   if(m.equals("Connect your Bluelink account first."))return m;
   if(m.startsWith("This build has no Hyundai client credential."))return "This build is missing its local Hyundai API configuration.";
  }
  return "Request could not be completed. Check your connection and the official app before retrying; command outcome may be unknown.";
 }
}
