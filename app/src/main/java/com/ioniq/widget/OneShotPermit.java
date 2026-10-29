package com.ioniq.widget;
final class OneShotPermit {
 private String value;private long issued;
 synchronized String issue(long now){value=java.util.UUID.randomUUID().toString();issued=now;return value;}
 synchronized boolean consume(String token,long now){if(token==null||!token.equals(value))return false;value=null;return now>=issued&&now-issued<=15000;}
}
