package test;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.Cipher;


public class TestKey {

	public static void main(String [] args) throws Exception {
		String Data="ini testing";
		System.setProperty("protect", "module");
		FileInputStream in=new FileInputStream("/opt/jks/aes.keystore");

		final KeyStore tmpKs = KeyStore.getInstance("nCipher.sworld", "nCipherKM");
		tmpKs.load(in, null);
		Key key=tmpKs.getKey("aeskey", null);
		Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        System.out.println("enc:"+encryptedValue);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decVal = c.doFinal(Base64.getDecoder().decode(encryptedValue));
        System.out.println("dec:"+new String(decVal));

        
	}
}
