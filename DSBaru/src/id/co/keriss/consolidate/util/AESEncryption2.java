package id.co.keriss.consolidate.util;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AESEncryption2 {
	 //private static final String ALGO = "AES";
//	    private static final byte[] keyValue = 
//	        new byte[] { 'c', '5', 'A', 'P', 'A', 'Y', 'M',	'e', 'n', 'T', 'a','e', 'S', 'K', '3', 'y' };
//
//	    private static final byte[] keyDoc = 
//		        new byte[] { 'B', 'a', 'N', 'U', 'J', '0', 'M',	'B', 'L', '0', 'a','e', 'S', 'K', '3', 'y' };

	    
	    public static String encrypt(String Data, byte[] keyValue) throws Exception {
                
	        Key key = generateKey(keyValue);
              
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encVal = c.doFinal(Data.getBytes());
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }

            private static final String ALGO = "AES";
	    
            public static String decrypt(String encryptedData, byte[] keyValue) throws Exception {
	        Key key = generateKey(keyValue);
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
           
	    private static Key generateKey(byte[] keyValue) throws Exception {
	        Key key = new SecretKeySpec(keyValue, ALGO);
	        return key;
	    }
	    
	    

	    
	    
	    
}
