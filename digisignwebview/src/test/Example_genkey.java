package test;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;


public class Example_genkey {

	    public String decryptID(String encryptedData) throws Exception {
	        encryptedData="dVmVdQFHRgjw3qnkVPe3uEkq1vffYw6CJoPwFO0RW+gI7Zh/JII+k7KiAdjkOdnCPh+wB6WHnbMGAEQbNimG/cLGgW76FRoQD1aDJs9cui0OjJduAwQ7slpRfRyfVKxHiv+iKRT6/7qIpr+jKPoO/A==";
	    	String keyAes="DssuLtu5ITSd2qHh";
	        Key key = new SecretKeySpec(keyAes.getBytes(), "AES");
	        Cipher c = Cipher.getInstance("AES");
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }
	    
}
