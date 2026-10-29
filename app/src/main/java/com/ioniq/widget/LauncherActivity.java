package com.ioniq.widget;
/** The only exported activity deliberately discards all incoming extras and data. */
public final class LauncherActivity extends android.app.Activity {
 @Override public void onCreate(android.os.Bundle state){super.onCreate(state);startActivity(new android.content.Intent(this,MainActivity.class));finish();}
}
