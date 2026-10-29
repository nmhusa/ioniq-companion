package com.ioniq.widget;
public final class ClientCredentialTest {
 public static void main(String[] args) {
  String expected="Synthetic-credential-å";
  byte[] raw=expected.getBytes(java.nio.charset.StandardCharsets.UTF_8);
  int[] masked=new int[raw.length],pad=new int[raw.length];
  for(int i=0;i<raw.length;i++){pad[i]=(i*37+129)&255;masked[i]=(raw[i]&255)^pad[i];}
  if(!expected.equals(ClientCredential.decode(masked,pad)))throw new AssertionError();
  if(!ClientCredential.decode(new int[0],new int[0]).isEmpty())throw new AssertionError();
  try{ClientCredential.decode(new int[1],new int[0]);throw new AssertionError();}catch(IllegalArgumentException ok){}
  System.out.println("3 credential decoder checks passed");
 }
}
