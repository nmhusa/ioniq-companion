package com.ioniq.widget;
import android.content.*;
import android.security.keystore.*;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.*;
import java.util.*;
import org.json.*;
/** Encrypted cache storage. Legacy plaintext is removed only after a successful encrypted commit. */
final class SecurePrefs implements SharedPreferences {
 private static final Map<String,SecurePrefs> INSTANCES=new HashMap<>();
 private final SharedPreferences disk;
 private final String name;
 private Map<String,Object> values;
 private final Set<OnSharedPreferenceChangeListener> listeners=new HashSet<>();
 static synchronized SharedPreferences get(Context c,String name){
  SecurePrefs p=INSTANCES.get(name);if(p==null){p=new SecurePrefs(c.getApplicationContext(),name);INSTANCES.put(name,p);}return p;
 }
 private static SecretKey key()throws Exception{
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);String alias="ioniq.cache.v1";
  if(!ks.containsAlias(alias)){KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}
  return (SecretKey)ks.getKey(alias,null);
 }
 private SecurePrefs(Context c,String name){this.name=name;disk=c.getSharedPreferences("encrypted_"+name,0);
  SharedPreferences legacy=c.getSharedPreferences(name,0);
  try{
   if(disk.contains("payload")){values=decode(CacheCipher.open(key(),name,Base64.decode(disk.getString("payload",""),Base64.NO_WRAP)));}
   else{values=new HashMap<>(legacy.getAll());if(!write(values))throw new Exception();}
   if(!legacy.getAll().isEmpty()&&!legacy.edit().clear().commit())throw new Exception();
  }catch(Exception e){throw new IllegalStateException("Protected local data could not be opened. Restart the phone and try again.");}
 }
 private static byte[] encode(Map<String,Object> map)throws Exception{
  JSONObject root=new JSONObject();for(Map.Entry<String,Object> e:map.entrySet()){
   Object v=e.getValue();String type=v instanceof String?"s":v instanceof Integer?"i":v instanceof Long?"l":v instanceof Float?"f":v instanceof Boolean?"b":"set";
   root.put(e.getKey(),new JSONObject().put("t",type).put("v",v instanceof Set?new JSONArray((Set<?>)v):v));
  }return root.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
 }
 private static Map<String,Object> decode(byte[] bytes)throws Exception{
  JSONObject root=new JSONObject(new String(bytes,java.nio.charset.StandardCharsets.UTF_8));Map<String,Object> map=new HashMap<>();Iterator<String> it=root.keys();
  while(it.hasNext()){String k=it.next();JSONObject e=root.getJSONObject(k);Object v;switch(e.getString("t")){
   case "s":v=e.getString("v");break;case "i":v=e.getInt("v");break;case "l":v=e.getLong("v");break;case "f":v=(float)e.getDouble("v");break;case "b":v=e.getBoolean("v");break;
   case "set":Set<String> set=new HashSet<>();JSONArray a=e.getJSONArray("v");for(int n=0;n<a.length();n++)set.add(a.getString(n));v=set;break;default:throw new Exception();
  }map.put(k,v);}return map;
 }
 private boolean write(Map<String,Object> map){try{return disk.edit().putString("payload",Base64.encodeToString(CacheCipher.seal(key(),name,encode(map)),Base64.NO_WRAP)).commit();}catch(Exception e){return false;}}
 public synchronized Map<String,?> getAll(){Map<String,Object> copy=new HashMap<>();for(Map.Entry<String,Object> e:values.entrySet())copy.put(e.getKey(),e.getValue() instanceof Set?new HashSet<>((Set<?>)e.getValue()):e.getValue());return copy;}
 public synchronized String getString(String k,String d){return values.containsKey(k)?(String)values.get(k):d;}
 @SuppressWarnings("unchecked") public synchronized Set<String> getStringSet(String k,Set<String> d){return values.containsKey(k)?new HashSet<>((Set<String>)values.get(k)):d;}
 public synchronized int getInt(String k,int d){return values.containsKey(k)?(Integer)values.get(k):d;}
 public synchronized long getLong(String k,long d){return values.containsKey(k)?(Long)values.get(k):d;}
 public synchronized float getFloat(String k,float d){return values.containsKey(k)?(Float)values.get(k):d;}
 public synchronized boolean getBoolean(String k,boolean d){return values.containsKey(k)?(Boolean)values.get(k):d;}
 public synchronized boolean contains(String k){return values.containsKey(k);}
 public Editor edit(){return new Edit();}
 public synchronized void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){listeners.add(l);}
 public synchronized void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){listeners.remove(l);}
 private final class Edit implements Editor{
  private final Map<String,Object> changes=new HashMap<>();private boolean clear;
  public Editor putString(String k,String v){changes.put(k,v);return this;}
  public Editor putStringSet(String k,Set<String> v){changes.put(k,v==null?null:new HashSet<>(v));return this;}
  public Editor putInt(String k,int v){changes.put(k,v);return this;}public Editor putLong(String k,long v){changes.put(k,v);return this;}
  public Editor putFloat(String k,float v){changes.put(k,v);return this;}public Editor putBoolean(String k,boolean v){changes.put(k,v);return this;}
  public Editor remove(String k){changes.put(k,null);return this;}public Editor clear(){clear=true;return this;}
  public boolean commit(){Set<String> changed;Set<OnSharedPreferenceChangeListener> notify;
   synchronized(SecurePrefs.this){Map<String,Object> next=clear?new HashMap<>():new HashMap<>(values);changed=new HashSet<>(changes.keySet());if(clear)changed.addAll(values.keySet());
    for(Map.Entry<String,Object> e:changes.entrySet()){if(e.getValue()==null)next.remove(e.getKey());else next.put(e.getKey(),e.getValue());}
    if(!write(next))return false;values=next;notify=new HashSet<>(listeners);
   }for(OnSharedPreferenceChangeListener l:notify)for(String k:changed)l.onSharedPreferenceChanged(SecurePrefs.this,k);return true;
  }
  public void apply(){if(!commit())throw new IllegalStateException("Protected local data could not be saved.");}
 }
}
