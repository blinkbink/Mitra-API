package id.co.keriss.consolidate.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption {

	
	public static String md5(String password) {
		String res=null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        md.update(password.getBytes());

        byte byteData[] = md.digest();

        
        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<byteData.length;i++) {
    		String hex=Integer.toHexString(0xff & byteData[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
    	res=hexString.toString();
		return res;
	}
}
