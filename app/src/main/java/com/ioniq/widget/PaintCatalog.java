package com.ioniq.widget;
import android.content.Context;
import org.json.*;
/** Bundled US factory palettes. No VIN decoding or network requests. */
final class PaintCatalog {
 private static JSONArray cached;
 static synchronized JSONArray groups(Context c)throws Exception{
  if(cached==null)try(java.io.InputStream in=c.getAssets().open("paint_catalog.json")){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);cached=new JSONObject(out.toString("UTF-8")).getJSONArray("groups");}
  return cached;
 }
 static JSONObject group(Context c,String id)throws Exception{JSONArray a=groups(c);for(int i=0;i<a.length();i++)if(a.getJSONObject(i).getString("id").equals(id))return a.getJSONObject(i);return null;}
 static JSONObject defaultGroup(Context c,JSONObject profile)throws Exception{JSONArray a=groups(c);for(int i=0;i<a.length();i++){JSONObject g=a.getJSONObject(i);if(g.optInt("model")==profile.optInt("model")&&g.optString("year").equals(profile.optString("year"))&&g.optString("variant").equals(profile.optString("variant")))return g;}return null;}
 static JSONObject color(JSONObject group,String id)throws Exception{if(group==null)return null;JSONArray a=group.getJSONArray("colors");for(int i=0;i<a.length();i++)if(a.getJSONObject(i).getString("id").equals(id))return a.getJSONObject(i);return null;}
 private static String normalize(String value){return value.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]","");}
 static JSONObject match(JSONObject group,JSONArray hints)throws Exception{
  if(group==null||hints==null)return null;JSONObject found=null;JSONArray colors=group.getJSONArray("colors");
  for(int h=0;h<hints.length();h++){String hint=normalize(hints.optString(h));if(hint.isEmpty())continue;
   for(int i=0;i<colors.length();i++){JSONObject color=colors.getJSONObject(i);boolean matches=hint.equals(normalize(color.getString("name")))||(!color.optString("code").isEmpty()&&hint.equals(normalize(color.getString("code"))));JSONArray aliases=color.optJSONArray("aliases");if(aliases!=null)for(int a=0;a<aliases.length();a++)matches|=hint.equals(normalize(aliases.getString(a)));
    if(matches){if(found!=null&&!found.getString("id").equals(color.getString("id")))return null;found=color;}
   }
  }return found;
 }
}
