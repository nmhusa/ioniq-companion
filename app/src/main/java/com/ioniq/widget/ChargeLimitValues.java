package com.ioniq.widget;
final class ChargeLimitValues {
 static final int AC=1,DC=0;
 static boolean valid(int value){return value>=50&&value<=100&&value%10==0;}
 static int pack(int ac,int dc){if(!valid(ac)||!valid(dc))throw new IllegalArgumentException("Choose AC and DC limits from 50–100% in 10% steps.");return ac*1000+dc;}
 static int ac(int packed){int value=packed/1000;if(!valid(value)||!valid(packed%1000))throw new IllegalArgumentException("Invalid charge limits");return value;}
 static int dc(int packed){ac(packed);return packed%1000;}
}
