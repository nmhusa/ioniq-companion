package com.ioniq.widget;
import android.app.*;
import android.content.*;
import android.os.*;
import android.hardware.biometrics.*;
public final class UnlockActivity extends Activity {
 private CancellationSignal cancellation;private boolean completed;private String mode;
 @Override public void onCreate(Bundle state){super.onCreate(state);mode=getIntent().getStringExtra("mode");
  if(state!=null){finish();return;} // Recreated activities never resume a previously approved operation.
  KeyguardManager keyguard=getSystemService(KeyguardManager.class);
  if(!keyguard.isDeviceSecure()){android.widget.Toast.makeText(this,"Set a phone screen-lock PIN or password first.",android.widget.Toast.LENGTH_LONG).show();finish();return;}
  if(Build.VERSION.SDK_INT>=30){cancellation=new CancellationSignal();new BiometricPrompt.Builder(this).setTitle("IONIQ Companion")
   .setSubtitle("unlock".equals(mode)?"Confirm unlocking your car":"Confirm changing unlock protection")
   .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG|BiometricManager.Authenticators.DEVICE_CREDENTIAL).build()
   .authenticate(cancellation,getMainExecutor(),new BiometricPrompt.AuthenticationCallback(){
    @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult r){approved();}
    @Override public void onAuthenticationError(int code,CharSequence message){finish();}
   });
  }else{Intent prompt=keyguard.createConfirmDeviceCredentialIntent("IONIQ Companion","Confirm to continue");if(prompt==null)finish();else startActivityForResult(prompt,42);}
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==42){if(result==RESULT_OK)approved();else finish();}}
 private void approved(){if(completed||isFinishing())return;completed=true;
  if("unlock".equals(mode)){startForegroundService(new Intent(this,CommandService.class).setAction("unlock").putExtra("unlock_permit",UnlockGuard.grant()));}
  else if("enable".equals(mode)||"disable".equals(mode)){boolean on="enable".equals(mode);if(SecurePrefs.get(this,"security").edit().putBoolean("protect_unlock",on).commit()){ActionWidget.renderAll(this);android.widget.Toast.makeText(this,on?"Unlock protection enabled":"Unlock protection disabled",android.widget.Toast.LENGTH_SHORT).show();}}
  finish();
 }
 @Override protected void onDestroy(){if(cancellation!=null)cancellation.cancel();super.onDestroy();}
}
