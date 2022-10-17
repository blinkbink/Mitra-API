package test;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.Cipher;


import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;


public class GenKey {

	public static void main(String [] args) throws Exception {
//////////////////////////////////////////Encrypt With Decrypt Engine////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//        System.out.println("ENCRYPT DECRYPT ENGINE \n");
////        String aes_key = "c5APAYMenTaeSK3y";//dari token mitra (MAKE SURE THE KEY WAS CORRECT)//BaNUJ0MBL0aeSK3y
//        String aes_key = "BpAhLjM97Kn9x1gt";//dari token mitra (MAKE SURE THE KEY WAS CORRECT)//BaNUJ0MBL0aeSK3y
//        String Final = "\"result\""+":"+"\"00\"";
//		String viewnya = "\"view_only\""+": true";
//		String notifikasi = "\"notif\""+":"+"\"Dokumen Telah Ditandatangani!\"";
//		String dokumen_id = "\"document_id\""+":"+"\"DocumentTest123\"";
//		String email_us = "\"email_user\""+":"+"\"testing@digisign.id\"";
////      String successresult = "{"+dokumen_id+","+viewnya+","+Final+","+notifikasi+","+email_us+"}";
////		{"result":"00","notif":"Proses Aktivasi Berhasil","email_user":"testing@tandatanganku.com","nik":"3275094801950033"}
////		String successresult = "{\"result\":\"00\", \"notif\":\"Proses Aktivasi Berhasil\",\"email_user\":\"test123@digisign.id\",\"nik\":\"1234567890987654\"}";
////		String successresult = "{\"result\":\"00\",\"notif\":\"Proses Aktivasi Berhasil\",\"email_user\":\"kofoz@getnada.com\",\"nik\":\"3273150208910008\"}";
////		String successresult = "{\"doc_id\":\"61224\",\"email_user\":\"dummy62@digisign.id\"}";
//		String successresult = "{\"document_id\":\""
//				+ "15949880850139013922"
//				+ "\",\"status\":\"0\",\"status_document\":\"complete\",\"result\":\"00\",\"notif\":\"Proses tanda tangan berhasil!\",\"email_user\":\""
//				+ "aahmad51600@gmail.com"
//				+ "\"}";
////		String successresult = "Fri Jan 24 01:01:17 WIB 2020|accounttest2@tandatanganku.com|41776fecfd7d98e80ddbd442aa5e306c";
//		System.out.println("STRING : "+successresult+"\n");
//        AESEncryption AES = new AESEncryption();
//		String encryptresult = AES.encryptKeyAndResultRedirect(aes_key,successresult);
//		String encoderencrypt = URLEncoder.encode(AESEncryption.encryptKeyAndResultRedirect(aes_key,successresult),"UTF-8");
//		System.out.println("ENCRYPT : "+encryptresult+"\n");
//		System.out.println("ENCODE ENCRYPT : https://tanifund.com/digisign/signed-off?msg="+encoderencrypt+"\n");
//		String decodedValue = decodeValue( encoderencrypt);
//		System.out.println("DECODE ENCODE ENCRYPT : "+decodedValue+"\n");
//		System.out.println("AES_KEY : "+aes_key+" (PASTIKAN AES KEY SESUAI) \n");
//		String decryptresult = AES.decryptKeyAndResultRedirect(aes_key,encryptresult);
//		System.out.println("DECRYPT : " +decryptresult+"\n");
//		String decodedecryptresult = AES.decryptKeyAndResultRedirect(aes_key,decodedValue);
//		System.out.println("DECRYPT WITH DECODE : " +decryptresult);
		
		
//////////////////////////////////////////DECRYPT_ONLY////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("ONLY DECRYPT ENGINE \n");
//        String aes_key = "95bSnrZStYXocwyp";//dari token mitra (MAKE SURE THE KEY WAS CORRECT)
//        String aes_key = "dAA2cYBenTmJStGy";// DIGISIGN AES KEY : dAA2cYBenTmJStGy
		String aes_key = "ndErtq7TKBfjUfmt";// DIGISIGN AES KEY 2 : c5APAYMenTaeSK3y|c5APAYMenTaeSK3y
        AESEncryption AES = new AESEncryption();
		String encryptresult ="fLAfei65Zmq8iR0w1x6OI4Y%2BHu7FDBhjbp5oVKv1sDKX3ZI8ozPzTmvinsS5M6jXVsmS5N%2FWg9HR%0AC0DkD29B0GLyAMIocACQXAzYivoDHIzuJn2YhTZpUZk2BthEyFh7%2FXEX5BrRvGBP4NUgdHUYPXyF%0AXYqsYKr6jq0iNOHXhFQHr4MPVHPXEWJc%2Bkef4OF4UA2aiNHmT6HrAMfd88mKjNCAKsui%2BLfJ%2F5ao%0AhC3S%2BvknOzABilEPi9A4N%2BIYMex8";//directly from link
		String decodedValue = decodeValue( encryptresult);
//		String decodedValue = "p9YjRaiUN1XvAF9NppNvrQmnJ87Le1a1+XvvVN5EcvfDc+rFgTPxUj5a82lWapPyTHShJWE2MP2G\\na+Iv9kFCjg==";
//		System.out.println("ENCRYPT : "+encryptresult+"\n");
		System.out.println("DECODE ENCRYPT : "+decodedValue+"\n");
		System.out.println("AES_KEY : "+aes_key+" (PASTIKAN AES KEY SESUAI) \n");
		String decryptresult = AES.decryptKeyAndResultRedirect(aes_key,decodedValue);
//		String decryptresult = AES.decryptDoc(decodedValue);		
		System.out.println("DECRYPT : "+decryptresult+"\n");
		
//////////////////////////////////////////Encoder Base64/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
//		System.out.println("Encoder Base64 ENGINE \n");
//		String Dstring = "OBthYdq7u2TcKucZgdQf+xg9GWl07TsDkYj3BKMrdCPxOXYSR9TBlVxTYCYFbVXumEDlNDroKNVt\nbiPkozOeG+XadbJl4CCgTpwm0rxRRVD8YRxe6g27S05MGorxR2GYBybWEBNRlQNZ+vecPqH2fAv5\nSxTqc61zGPhbTxs4s48W+2iJIFK3WiBs7VCTCEiEWwJ4lrzKudRHOcTqrR+PifgDwvSt6CdnNEny\nKI43JpvV6+tRbNZMYH9MXTSWdV3C";
//		String D_encoder = URLEncoder.encode(Dstring,"UTF-8");
//		System.out.println("Encode Base64: " +D_encoder);
        
		
//////////////////////////////////////////Decrypt CBC/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//System.out.println("Encrypt CBC \n");
//String string = "{\"document_access\":[200579,200578],\"outfile\":\"\\/file\\/data-DS\\/UploadFile\\/15063\\/original\\/DSSG20200709133915168200579.pdf\",\"user\":9,\"document_id\":95677}";
//String dencoder = AESEncryption.encryptDocSign(string);
//System.out.println("Encode Base64: " +dencoder);
//		
//System.out.println("Decrypt CBC \n");
//String Dstring = "YsIZWUgBPyZtwo9d3c8oNXsEov1JzjFODkYGBLdumD+2Yas8Zouad1XMbdQnGqCQSP9XQG/upJ561+M+AOMnefeaHe8hFHPm4ta4kgpiYBQSwvs3mwpDZh3x1A+oZsAghaJ+9A3cX4c3lHtOC3q5xjR5HpVDc7Sgs/VWmUMDjM4=";
//
//String D_encoder = AESEncryption.decryptDocSign(Dstring);
//System.out.println("Encode Base64: " +D_encoder);
		
		
		
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	}
	//
	public static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
