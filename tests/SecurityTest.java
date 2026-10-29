package com.ioniq.widget;
import javax.crypto.*;import java.util.*;import java.nio.charset.StandardCharsets;
public final class SecurityTest {
 static void check(boolean v){if(!v)throw new AssertionError();}
 interface Attempt{void run()throws Exception;}
 static void rejects(Attempt a)throws Exception{try{a.run();}catch(java.security.GeneralSecurityException e){return;}throw new AssertionError("Tampering accepted");}
 public static void main(String[] args)throws Exception{
  KeyGenerator g=KeyGenerator.getInstance("AES");g.init(256);javax.crypto.SecretKey key=g.generateKey();byte[] plain="VIN-test and location 44,-73".getBytes(StandardCharsets.UTF_8);
  byte[] first=CacheCipher.seal(key,"locations",plain),second=CacheCipher.seal(key,"locations",plain);
  check(!Arrays.equals(first,second));check(Arrays.equals(plain,CacheCipher.open(key,"locations",first)));
  rejects(()->CacheCipher.open(key,"status",first));rejects(()->CacheCipher.open(g.generateKey(),"locations",first));
  byte[] tampered=first.clone();tampered[tampered.length-1]^=1;rejects(()->CacheCipher.open(key,"locations",tampered));rejects(()->CacheCipher.open(key,"locations",new byte[0]));
  OneShotPermit p=new OneShotPermit();check(!p.consume(null,0));String token=p.issue(100);check(!p.consume("fake",101));check(p.consume(token,102));check(!p.consume(token,103));token=p.issue(100);check(!p.consume(token,15101));
  String old=p.issue(100);String fresh=p.issue(101);check(!p.consume(old,102));check(p.consume(fresh,103));
  String privateText="password=SECRET token=PRIVATE https://private.example";
  check(!ErrorText.message(new Exception(privateText)).contains("SECRET"));check(!ErrorText.message(new orgLikeException(privateText)).contains("PRIVATE"));
  String error=ErrorText.message(new Exception("Remote command failed (HTTP 502). "+privateText));check(error.contains("502")&&!error.contains("SECRET"));
  check(ErrorText.message(new Exception("Sign-in failed (HTTP 401). details")).contains("Reconnect"));
  System.out.println("17 security checks passed");
 }
 static class orgLikeException extends RuntimeException{orgLikeException(String s){super(s);}}
}
