package com.ioniq.widget;
import android.content.Intent;
import com.google.android.gms.wearable.*;
import com.ioniq.common.WatchProtocol;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
/** Google Play services verifies same-package, same-signature Data Layer peers. */
public final class WatchListenerService extends WearableListenerService {
 @Override public void onMessageReceived(MessageEvent event){
  byte[] data=event.getData();if(data.length>4096)return;
  String node=event.getSourceNodeId(),path=event.getPath();
  try{JSONObject request=new JSONObject(new String(data,StandardCharsets.UTF_8));
   if(WatchProtocol.STATUS.equals(path)){String query=request.optString("query");if(WatchProtocol.validId(query))WatchRelay.reply(this,node,path,WatchRelay.snapshot(this,query));}
   else if(WatchProtocol.RESULT.equals(path)){String id=request.optString("id");if(WatchProtocol.validId(id))WatchRelay.reply(this,node,path,WatchRelay.result(this,node,id,request.optString("session")));}
   else if(WatchProtocol.COMMAND.equals(path)){
    JSONObject refusal=WatchRelay.claim(this,node,request,System.currentTimeMillis());if(refusal!=null){WatchRelay.reply(this,node,WatchProtocol.RESULT,refusal);return;}
    Intent command=new Intent(this,CommandService.class).setAction(request.getString("action")).putExtra("watch_id",request.getString("id")).putExtra("watch_node",node).putExtra("watch_session",request.getString("session"));
    try{startForegroundService(command);}catch(Exception e){WatchRelay.update(this,command,"blocked","Android blocked background control. Open the phone app, then try again. Nothing was sent.");}
   }
  }catch(Exception ignored){/* No raw payloads, credentials or exception data are logged. */}
 }
}
