package com.ioniq.widget;
/** In-memory only: a restart cannot resurrect a disconnected-car countdown. */
final class BluetoothLockRules {
 private long generation,due;private boolean connected,countdown,queued,sent;
 synchronized void connected(){generation++;connected=true;countdown=false;queued=false;sent=false;}
 synchronized boolean disconnected(long now,int seconds){
  if(!connected)return false;
  connected=false;countdown=true;queued=false;sent=false;generation++;due=now+seconds*1000L;return true;
 }
 synchronized void cancel(){generation++;connected=false;countdown=false;queued=false;sent=false;}
 synchronized long remaining(long now){return countdown?Math.max(0,due-now):-1;}
 synchronized long claim(long now){
  if(!countdown||now<due)return -1;
  countdown=false;
  if(now>due+60000){generation++;return -1;}
  queued=true;return generation;
 }
 synchronized boolean valid(long token,long now){return token==generation&&queued&&!connected&&!sent&&now>=due&&now<=due+60000;}
 synchronized boolean consume(long token,long now){if(!valid(token,now))return false;queued=false;sent=true;return true;}
 synchronized void finish(long token){if(token==generation){queued=false;countdown=false;}}
 synchronized boolean matches(long token){return token==generation;}
 synchronized boolean isConnected(){return connected;}
}
