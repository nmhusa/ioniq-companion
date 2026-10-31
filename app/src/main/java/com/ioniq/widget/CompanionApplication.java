package com.ioniq.widget;
public final class CompanionApplication extends android.app.Application {
 @Override public void onCreate(){super.onCreate();UnlockGuard.enabled(this);for(String store:new String[]{"status","locations","mileage","vehicle_identity","actions","parking"})SecurePrefs.get(this,store);}
}
