package com.ioniq.widget;
public final class BluetoothLockRulesTest {
 static int n;static void check(boolean b){n++;if(!b)throw new AssertionError("Check "+n);}
 public static void main(String[] a){
  BluetoothLockRules r=new BluetoothLockRules();
  check(!r.disconnected(1000,30));check(r.claim(999999)<0);
  r.connected();check(r.isConnected());check(r.disconnected(1000,30));check(!r.disconnected(2000,30));check(r.remaining(2000)==29000);check(r.claim(30999)<0);
  long token=r.claim(31000);check(token>=0);check(r.claim(31000)<0);check(r.valid(token,31000));
  r.connected();check(!r.consume(token,31001));check(r.disconnected(32000,60));
  token=r.claim(92000);check(token>=0);check(r.consume(token,92000));check(!r.consume(token,92000));check(!r.disconnected(93000,30));r.finish(token);
  r.connected();r.disconnected(100000,120);check(r.claim(219999)<0);token=r.claim(220000);r.cancel();check(!r.consume(token,220001));
  r.connected();r.disconnected(300000,30);check(r.claim(390001)<0);
  r.connected();r.disconnected(400000,30);token=r.claim(430000);check(!r.consume(token,490001));
  r=new BluetoothLockRules();check(!r.disconnected(500000,30));check(r.claim(600000)<0);
  r.connected();r.disconnected(700000,30);token=r.claim(730000);r.finish(token);check(!r.consume(token,730001));
  for(int seconds:new int[]{10,15}){
   r=new BluetoothLockRules();r.connected();check(r.disconnected(1000,seconds));
   long due=1000+seconds*1000L;check(r.claim(due-1)<0);token=r.claim(due);check(token>=0);check(r.consume(token,due));check(!r.consume(token,due));
   r=new BluetoothLockRules();r.connected();r.disconnected(1000,seconds);r.connected();check(r.claim(due)<0);
   r.disconnected(due+1000,seconds);token=r.claim(due+1000+seconds*1000L);r.connected();check(!r.consume(token,due+1000+seconds*1000L));
  }
  System.out.println(n+" Bluetooth lock checks passed");
 }
}
