package com.ioniq.widget;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
final class CacheCipher {
 static byte[] seal(SecretKey key,String domain,byte[] data)throws Exception {
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key);c.updateAAD(domain.getBytes(StandardCharsets.UTF_8));
  byte[] iv=c.getIV(), encrypted=c.doFinal(data);return ByteBuffer.allocate(1+iv.length+encrypted.length).put((byte)iv.length).put(iv).put(encrypted).array();
 }
 static byte[] open(SecretKey key,String domain,byte[] data)throws Exception {
  if(data.length<29||data[0]!=12)throw new java.security.GeneralSecurityException("Invalid encrypted cache");
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,java.util.Arrays.copyOfRange(data,1,13)));c.updateAAD(domain.getBytes(StandardCharsets.UTF_8));return c.doFinal(data,13,data.length-13);
 }
}
