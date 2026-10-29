package com.ioniq.widget;
import java.nio.charset.StandardCharsets;
/** Extraction deterrent only. Both halves ship in the APK; this is not secret storage. */
final class ClientCredential {
 static String decode(int[] masked,int[] pad) {
  if(masked.length!=pad.length)throw new IllegalArgumentException("Credential data mismatch");
  byte[] value=new byte[masked.length];
  for(int i=0;i<value.length;i++)value[i]=(byte)(masked[i]^pad[i]);
  try{return new String(value,StandardCharsets.UTF_8);}finally{java.util.Arrays.fill(value,(byte)0);}
 }
}
