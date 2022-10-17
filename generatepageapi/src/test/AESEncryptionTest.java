package test;

import org.jpos.ee.DB;

import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;

public class AESEncryptionTest {
//      public String decryptID(String encryptedData) throws Exception {
//        String keyAes="pO9C7jhGuNR6PNLu";
//          Key key = new SecretKeySpec(keyAes.getBytes(), "AES");
//          Cipher c = Cipher.getInstance("AES");
//          c.init(Cipher.DECRYPT_MODE, key);
//          byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
//          byte[] decValue = c.doFinal(decordedValue);
//          String decryptedValue = new String(decValue);
//          return decryptedValue;
//      }
      public static void main(String args[]) throws Exception {
    	  
    	  String text="Cij6Jz9ui76E5Ky/qJR\n"
    	  		+ "qt4VQRv2PlzzrSTH27tgP+NDjvthK0H/DPOiYsvpXdD2/qVzSqXaSyHy+\n"
    	  		+ "qO53NyBdcAC4slHUBZnIirHiFrCXT8bh7UIXyMD6gIqm7DttfQJvKvGiA/K2V1CFF395gQsvtvdI\n"
    	  		+ "TcA5p6J+Go0RehZxMeIH1SqrzqSTN+urJ1UFEGECB07sDJAMSQKUYexfu8aE8w==";
			String data=AESEncryption.decryptLinkMitra("RBazsYSDTuShYbUG",text);

//    	  AESEncryptionTest aestest=new AESEncryptionTest();
//    	  String url="3W9GTCmPCBZIGUKo3AQg2piiuAOqQkHKNHSREiVEn4sb4baPH2Zql%2FqvoPzKBvXyfc%2FZoAQb8k3l%0Axwcb2dsDTlDgEh3x%2FvP1LA61RKSfdenUXz6mhYWoihMnNvFWDpFwWH0NN%2FpEiP%2FO7pJVwGExbw%3D%3D";
//    	  String decodeUrl=java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
    	  System.out.println(data);
    	  
    	  DB db=new DB();
    	  db.open();
  			TokenMitra tm=new TokenMitraDao(db).findByName(DSAPI.ACC_MITRA);
  			db.close();
      	  System.out.println(tm.getAes_key());

      }
}
