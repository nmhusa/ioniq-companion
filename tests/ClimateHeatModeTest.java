package com.ioniq.widget;
public final class ClimateHeatModeTest {
 public static void main(String[] args){
  check(false,false,0);check(true,false,2);check(false,true,3);check(true,true,1);
  System.out.println("4 climate heat-mode checks passed");
 }
 private static void check(boolean rear,boolean wheel,int expected){if(ClimateHeatMode.encode(rear,wheel)!=expected)throw new AssertionError("Incorrect rear/wheel encoding");}
}
