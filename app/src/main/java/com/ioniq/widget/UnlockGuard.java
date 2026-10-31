package com.ioniq.widget;
import android.content.Context;
import android.content.SharedPreferences;
final class UnlockGuard {
 private static final OneShotPermit permit=new OneShotPermit();
 // One-time upgrade deliberately restores protection even for legacy opt-outs.
 static synchronized boolean enabled(Context c){
  SharedPreferences p=SecurePrefs.get(c,"security");
  if(p.getInt("unlock_policy",0)<1){
   p.edit().putBoolean("protect_unlock",true).putInt("unlock_policy",1).commit();
   return true; // Fail closed even if persistence fails.
  }
  return p.getBoolean("protect_unlock",true);
 }
 // Called only after device authentication by UnlockActivity.
 static synchronized boolean setAuthenticated(Context c,boolean on){return SecurePrefs.get(c,"security").edit().putBoolean("protect_unlock",on).putInt("unlock_policy",1).commit();}
 static String grant(){return permit.issue(android.os.SystemClock.elapsedRealtime());}
 static boolean consume(String token){return permit.consume(token,android.os.SystemClock.elapsedRealtime());}
}
