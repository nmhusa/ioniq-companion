package com.ioniq.common;
public final class WatchProtocolTest {
 private static int n;private static void check(boolean b){n++;if(!b)throw new AssertionError("Check "+n);}
 public static void main(String[] args){
  for(String action:WatchProtocol.ACTIONS)check(WatchProtocol.allowed(action));
  for(String action:new String[]{null,"","erase","login","refresh","UNLOCK","unlock/anything"})check(!WatchProtocol.allowed(action));
  String id=java.util.UUID.randomUUID().toString();check(WatchProtocol.validId(id));check(!WatchProtocol.validId(null));check(!WatchProtocol.validId("../unlock"));check(!WatchProtocol.validId(id+"x"));
  check(WatchProtocol.fresh(100000,100000));check(WatchProtocol.fresh(40000,100000));check(!WatchProtocol.fresh(39999,100000));check(WatchProtocol.fresh(110000,100000));check(!WatchProtocol.fresh(110001,100000));check(!WatchProtocol.fresh(0,100000));check(!WatchProtocol.fresh(Long.MIN_VALUE,100000));check(!WatchProtocol.fresh(Long.MAX_VALUE,100000));
  check(WatchProtocol.pending("queued"));check(WatchProtocol.pending("sending"));check(!WatchProtocol.pending("unknown"));check(!WatchProtocol.pending("confirmed"));check(!WatchProtocol.pending(null));
  System.out.println(n+" watch protocol checks passed");
 }
}
