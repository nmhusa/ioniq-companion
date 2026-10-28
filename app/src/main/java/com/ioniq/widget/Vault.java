package com.ioniq.widget;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONObject;
final class Vault {
 private static final String ALIAS="ioniq6.credentials";
 private static SecretKey key() throws Exception {
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
  if(!ks.containsAlias(ALIAS)) { KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore"); g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()); g.generateKey(); }
  return (SecretKey)ks.getKey(ALIAS,null);
 }
 static synchronized void save(Context c, JSONObject data) throws Exception {
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE,key());
  String enc=Base64.encodeToString(cipher.doFinal(data.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)),Base64.NO_WRAP);
  c.getSharedPreferences("vault",0).edit().putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).putString("data",enc).commit();
 }
 static synchronized JSONObject read(Context c) throws Exception {
  android.content.SharedPreferences s=c.getSharedPreferences("vault",0); String data=s.getString("data",null); if(data==null)return new JSONObject();
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(s.getString("iv",""),Base64.NO_WRAP)));
  return new JSONObject(new String(cipher.doFinal(Base64.decode(data,Base64.NO_WRAP)),java.nio.charset.StandardCharsets.UTF_8));
 }
 static synchronized void clear(Context c) { c.getSharedPreferences("vault",0).edit().clear().commit(); c.getSharedPreferences("status",0).edit().clear().commit(); c.getSharedPreferences("actions",0).edit().clear().commit();ActionWidget.renderAll(c); }
}
