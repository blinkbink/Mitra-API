package id.co.keriss.consolidate.util;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {
	 private static final String ALGO = "AES";
	 private static final String ALGO2 = "AES";
	    private static final byte[] keyValue = 
	        new byte[] { 'c', '5', 'A', 'P', 'A', 'Y', 'M',	'e', 'n', 'T', 'a','e', 'S', 'K', '3', 'y' };

	    private static final byte[] keyDoc = 
		        new byte[] { 'B', 'a', 'N', 'U', 'J', '0', 'M',	'B', 'L', '0', 'a','e', 'S', 'K', '3', 'y' };

	    private static final byte[] keyValue2 = 
		        new byte[] { 'd', 'A', 'A', '2', 'c', 'Y', 'B',	'e', 'n', 'T', 'm','J', 'S', 't', 'G', 'y' };

	    private static final byte[] keyToken = "UHXpV1a45r3E6Wp3".getBytes();

	    public static String encrypt(String Data) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = org.bouncycastle.util.encoders.Base64.toBase64String(encVal);
	        return encryptedValue;
	    }

	    public static String decrypt(String encryptedData) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = org.bouncycastle.util.encoders.Base64.decode(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
	    public static String encryptBilling(String Data) throws Exception {
	        Key key = new SecretKeySpec(keyDoc, ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = org.bouncycastle.util.encoders.Base64.toBase64String(encVal);
	        return encryptedValue;
	    }

	    public static String decryptBilling(String encryptedData) throws Exception {
	        Key key = new SecretKeySpec(keyDoc, ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = org.bouncycastle.util.encoders.Base64.decode(encryptedData);
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
	        String encryptedValue = org.bouncycastle.util.encoders.Base64.toBase64String(encVal);
	        return encryptedValue;
	    }

	    public static String decryptDoc(String encryptedData) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue =org.bouncycastle.util.encoders.Base64.decode(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
	    public static String decryptLinkMitra(String keyString,String encryptedData) throws Exception {
	        Key key = new SecretKeySpec(keyString.getBytes(), ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = org.bouncycastle.util.encoders.Base64.decode(encryptedData);
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
	        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
	        return encryptedValue;
	    }

	    public static String decryptID(String encryptedData) throws Exception {
	        Key key = generateIdKey();
	        Cipher c = Cipher.getInstance(ALGO2);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = org.bouncycastle.util.encoders.Base64.decode(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    public static String decryptDocWeb(String encryptedData) throws Exception {
	        Key key =  new SecretKeySpec(keyToken, ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = org.bouncycastle.util.encoders.Base64.decode(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    public static String encryptDocWeb(String Data) throws Exception {
	        Key key = new SecretKeySpec(keyToken, ALGO);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
	        return encryptedValue;
	    }
}
