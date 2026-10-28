package com.ioniq.widget;
public final class ChargeLimitValuesTest {
 public static void main(String[] args){
  if(ChargeLimitValues.AC!=1||ChargeLimitValues.DC!=0)throw new AssertionError("Hyundai plug types reversed");
  int count=1;
  for(int ac=50;ac<=100;ac+=10)for(int dc=50;dc<=100;dc+=10){int value=ChargeLimitValues.pack(ac,dc);if(ChargeLimitValues.ac(value)!=ac||ChargeLimitValues.dc(value)!=dc)throw new AssertionError("Limits mixed up");count++;}
  for(int bad:new int[]{-1,0,49,55,101,110}){try{ChargeLimitValues.pack(bad,80);throw new AssertionError("Invalid AC accepted");}catch(IllegalArgumentException expected){}try{ChargeLimitValues.pack(80,bad);throw new AssertionError("Invalid DC accepted");}catch(IllegalArgumentException expected){}count+=2;}
  try{ChargeLimitValues.ac(8055);throw new AssertionError("Malformed command accepted");}catch(IllegalArgumentException expected){}count++;
  System.out.println(count+" charge-limit checks passed");
 }
}
