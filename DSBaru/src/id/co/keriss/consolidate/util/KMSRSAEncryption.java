package id.co.keriss.consolidate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Encoder;

public class KMSRSAEncryption implements DSAPI {
	static Cipher cipher;
	
	 
	// https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
	public static PrivateKey getPrivate(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	// https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
	public static PublicKey getPublic(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	public void encryptFile(byte[] input, File output, PrivateKey key) 
		throws IOException, GeneralSecurityException {
		KMSRSAEncryption.cipher.init(Cipher.ENCRYPT_MODE, key);
		writeToFile(output, KMSRSAEncryption.cipher.doFinal(input));
	}

	public void decryptFile(byte[] input, File output, PublicKey key) 
		throws IOException, GeneralSecurityException {
		KMSRSAEncryption.cipher.init(Cipher.DECRYPT_MODE, key);
		writeToFile(output, KMSRSAEncryption.cipher.doFinal(input));
	}

	private void writeToFile(File output, byte[] toWrite)
			throws IllegalBlockSizeException, BadPaddingException, IOException {
		FileOutputStream fos = new FileOutputStream(output);
		fos.write(toWrite);
		fos.flush();
		fos.close();
	}

	public static String encryptText(String msg, Key key) 
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			UnsupportedEncodingException, IllegalBlockSizeException, 
			BadPaddingException, InvalidKeyException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
//		return encodeBase64String(cipher.doFinal(msg.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
	}

	public static String decryptText(String msg, Key key)
			throws InvalidKeyException, UnsupportedEncodingException, 
			IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(Base64.getDecoder().decode(msg)), "UTF-8");
	}

	public byte[] getFileInBytes(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		byte[] fbytes = new byte[(int) f.length()];
		fis.read(fbytes);
		fis.close();
		return fbytes;
	}
	
	
	public static String decryptWithPub(String data) {
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			PublicKey key = getPublic(ROOTDIR+"WEB-INF/KeyPair/Core_publicKey");
//			PublicKey key = getPublic("Core_publicKey");
//			PublicKey publicKey = getPublic("KeyPair/publicKey");
			String decrypted_msg= decryptText(data, key);
			return decrypted_msg;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public static String encryptWithPub(String data) {
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//			PrivateKey key = getPublic("../KeyPair/privateKey");
			PublicKey key = getPublic(ROOTDIR+"WEB-INF/KeyPair/Core_publicKey");
//			PublicKey key = getPublic("Core_publicKey");
			String decrypted_msg= encryptText(data, key);
			return decrypted_msg;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
