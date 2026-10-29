package com.ioniq.widget;
import android.app.Instrumentation;
import android.content.*;
import android.content.pm.*;
import android.os.Bundle;
import java.util.*;
public final class SecurityInstrumentation extends Instrumentation {
 private int checks;
 private void check(boolean condition){checks++;if(!condition)throw new AssertionError("Check "+checks+" failed");}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  Context c=getTargetContext();String store="security_test_"+System.nanoTime();
  SharedPreferences legacy=c.getSharedPreferences(store,0);
  check(legacy.edit().putString("private","VIN-ONLY-TEST-44.0,-73.0").putInt("int",3).putLong("long",9999999999L).putBoolean("bool",true).putFloat("float",1.5f).putStringSet("set",new HashSet<>(Arrays.asList("a","b"))).commit());
  SharedPreferences secure=SecurePrefs.get(c,store);
  check(legacy.getAll().isEmpty());check(secure.getString("private","").contains("VIN-ONLY-TEST"));check(secure.getInt("int",0)==3);check(secure.getLong("long",0)==9999999999L);check(secure.getBoolean("bool",false));check(secure.getFloat("float",0)==1.5f);check(secure.getStringSet("set",Collections.emptySet()).size()==2);
  SharedPreferences disk=c.getSharedPreferences("encrypted_"+store,0);String encoded=disk.getString("payload","");check(!encoded.contains("VIN-ONLY-TEST"));check(disk.getAll().size()==1);
  SharedPreferences.Editor a=secure.edit().putString("a","one"),b=secure.edit().putString("b","two");check(a.commit());check(b.commit());check(secure.contains("a")&&secure.contains("b"));check(!disk.getString("payload","").equals(encoded));
  // A second instance must decrypt the persisted data with Android Keystore (not an in-memory round-trip).
  java.lang.reflect.Constructor<SecurePrefs> constructor=SecurePrefs.class.getDeclaredConstructor(Context.class,String.class);constructor.setAccessible(true);
  SharedPreferences reopened=constructor.newInstance(c,store);check(reopened.getLong("long",0)==9999999999L);check(reopened.getString("b","").equals("two"));
  check(secure.edit().remove("private").commit());check(!secure.contains("private"));check(secure.edit().clear().commit());check(secure.getAll().isEmpty());
  // Authentication-tag failure must never silently fall back to a legacy plaintext value.
  String corrupt=store+"_corrupt";c.getSharedPreferences(corrupt,0).edit().putString("private","DO-NOT-FALL-BACK").commit();c.getSharedPreferences("encrypted_"+corrupt,0).edit().putString("payload","invalid").commit();
  boolean rejected=false;try{SecurePrefs.get(c,corrupt);}catch(IllegalStateException expected){rejected=true;}check(rejected);
  PackageManager pm=c.getPackageManager();check(!pm.getActivityInfo(new ComponentName(c,MainActivity.class),0).exported);check(!pm.getActivityInfo(new ComponentName(c,UnlockActivity.class),0).exported);check(pm.getActivityInfo(new ComponentName(c,LauncherActivity.class),0).exported);check(!pm.getServiceInfo(new ComponentName(c,CommandService.class),0).exported);
  result.putString("security_result","PASS: "+checks+" Android storage and component checks");finish(-1,result);
 }catch(Throwable e){result.putString("security_result","FAIL: "+e.getClass().getSimpleName()+" after "+checks+" checks");finish(0,result);}}
}
