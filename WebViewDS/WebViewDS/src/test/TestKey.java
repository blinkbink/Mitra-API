package test;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;

import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.KMSRSAEncryption;


public class TestKey {

	public static void main(String [] args) throws Exception {
		JSONObject jo = new JSONObject();
		JSONObject jf = new JSONObject();
		ArrayList docacc = new ArrayList();
		docacc.add("484621");
		
		jf.put("user", "957");
		jf.put("document_access", docacc);
		jf.put("document_id", "299436");
		jf.put("outfile", "/file2/data-DS/UploadFile/957/original/APIPDFBOXTEST1.pdf");
		System.out.println(jf.toString());
		jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
		System.out.println(jo.toString());
	}
}
