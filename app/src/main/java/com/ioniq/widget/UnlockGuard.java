package com.ioniq.widget;
import android.content.Context;
final class UnlockGuard {
 private static final OneShotPermit permit=new OneShotPermit();
 static boolean enabled(Context c){return SecurePrefs.get(c,"security").getBoolean("protect_unlock",false);}
 static String grant(){return permit.issue(android.os.SystemClock.elapsedRealtime());}
 static boolean consume(String token){return permit.consume(token,android.os.SystemClock.elapsedRealtime());}
}
