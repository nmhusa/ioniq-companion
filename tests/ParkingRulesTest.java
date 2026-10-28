package com.ioniq.widget;
public class ParkingRulesTest {
 private static int checked;
 private static void expect(ParkingRules.Event expected, ParkingRules.Event actual){checked++;if(expected!=actual)throw new AssertionError("Expected "+expected+", got "+actual);}
 public static void main(String[] args){
  ParkingRules r=new ParkingRules(100,0);
  expect(ParkingRules.Event.NONE,r.sample(1000,110,20,true,1)); // error margin prevents early lock
  expect(ParkingRules.Event.COUNTDOWN,r.sample(2000,130,20,true,1));
  expect(ParkingRules.Event.NONE,r.sample(15000,130,20,false,1)); // bad GPS resets dwell
  expect(ParkingRules.Event.COUNTDOWN,r.sample(22000,130,20,true,1));
  expect(ParkingRules.Event.NONE,r.sample(42000,130,20,true,10)); // driving cannot trigger lock
  expect(ParkingRules.Event.COUNTDOWN,r.sample(43000,130,20,true,1));
  expect(ParkingRules.Event.NONE,r.sample(63000,90,20,true,1)); // approach cancels countdown
  expect(ParkingRules.Event.COUNTDOWN,r.sample(64000,140,20,true,1));
  expect(ParkingRules.Event.LOCK,r.sample(84000,140,20,true,1));
  expect(ParkingRules.Event.NONE,r.sample(120000,200,20,true,1)); // single attempt, even failure
  expect(ParkingRules.Event.NONE,r.sample(150000,50,20,true,1)); // return uncertainty
  expect(ParkingRules.Event.RETURN,r.sample(160000,30,20,true,1));
  expect(ParkingRules.Event.NONE,r.sample(170000,30,20,true,1)); // single return reminder
  ParkingRules timed=new ParkingRules(100,0);
  expect(ParkingRules.Event.EXPIRED,timed.sample(900001,150,10,true,1));
  expect(ParkingRules.Event.EXPIRED,r.sample(86400001,1000,10,true,1));
  ParkingRules gap=new ParkingRules(100,0);
  expect(ParkingRules.Event.COUNTDOWN,gap.sample(1000,140,20,true,1));gap.interrupt();
  expect(ParkingRules.Event.COUNTDOWN,gap.sample(31000,140,20,true,1));
  expect(ParkingRules.Event.NONE,gap.sample(51000,600,20,true,1));
  System.out.println("Passed "+checked+" parking-rule checks.");
 }
}
