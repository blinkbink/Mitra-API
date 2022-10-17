package id.co.keriss.consolidate.util;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AESEncryption {
	 private static final String ALGO = "AES";
	 private static final String ALGO2 = "AES";
	    private static final byte[] keyValue = 
	        new byte[] { 'c', '5', 'A', 'P', 'A', 'Y', 'M',	'e', 'n', 'T', 'a','e', 'S', 'K', '3', 'y' };

	    private static final byte[] keyDoc = 
		        new byte[] { 'B', 'a', 'N', 'U', 'J', '0', 'M',	'B', 'L', '0', 'a','e', 'S', 'K', '3', 'y' };

	    private static final byte[] keyValue2 = 
		        new byte[] { 'd', 'A', 'A', '2', 'c', 'Y', 'B',	'e', 'n', 'T', 'm','J', 'S', 't', 'G', 'y' };
	    
	    private static final byte[] keyDoc2 = 
		        new byte[] { 'Z', '1', 'a', 'x', 'd', '1', 'M',	'9', 'x', 'G', 'a','w', 'S', 'r', '1', 'Y' };

	    
	    public static String encrypt(String Data) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }

	    public static String decrypt(String encryptedData) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
	    public static String encryptBilling(String Data) throws Exception {
	        Key key = new SecretKeySpec(keyDoc, ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }

	    public static String decryptBilling(String encryptedData) throws Exception {
	        Key key = new SecretKeySpec(keyDoc, ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
	    private static Key generateKey() throws Exception {
	        Key key = new SecretKeySpec(keyValue, ALGO);
	        return key;
	    }
	    
	    public static String encryptDoc(String Data) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }

	    public static String decryptDoc(String encryptedData) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
	    private static Key generateDocKey() throws Exception {
	        Key key = new SecretKeySpec(keyDoc, ALGO);
	        return key;
	    }
	    
	    
	    private static Key generateIdKey() throws Exception {
	        Key key = new SecretKeySpec(keyValue2, ALGO);
	        return key;
	    }
	    
	    
	    public static String encryptId(String Data) throws Exception {
	        Key key = generateIdKey();
	        Cipher c = Cipher.getInstance(ALGO2);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }

	    public static String decryptID(String encryptedData) throws Exception {
	        Key key = generateIdKey();
	        Cipher c = Cipher.getInstance(ALGO2);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
	    private static Key generateIdKeySign() throws Exception {
	        Key key = new SecretKeySpec(keyDoc2, ALGO);
	        return key;
	    }
	    
	    public static String encryptDocSign(String Data) throws Exception {
	        Key key = generateIdKeySign();
	        Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        c.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(new byte[16]));
	        byte[] encVal = c.doFinal(Data.getBytes("UTF-8"));
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }
	    
	    public static String decryptDocSign(String encryptedData) throws Exception {
	        Key key = generateIdKeySign();
	        Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        c.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(new byte[16]));
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
}
