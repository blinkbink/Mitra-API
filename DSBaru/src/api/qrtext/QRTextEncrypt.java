package api.qrtext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.hierynomus.security.MessageDigest;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import sun.misc.BASE64Encoder;

public class QRTextEncrypt {
	String refTrx="";
	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
        
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		}
	};
	public QRTextEncrypt(String refTrx) {
		// TODO Auto-generated constructor stub
		this.refTrx=refTrx;
	}
	
	
	public JSONObject kirim(String textQR, Long mitra, String document_id, HttpServletRequest  request) {
		URL url = null;
		JSONObject resp=null;
		try {
			url = new URL(DSAPI.QRTEXT);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        Map<String,Object> params = new LinkedHashMap<>();
        JSONObject params=new JSONObject();
        java.security.MessageDigest digest = null;
		try {
			digest = java.security.MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String key = DSAPI.QRTEXTKEY+textQR;
        byte[] hash=digest.digest(key.getBytes(StandardCharsets.UTF_8));
        try {
			params.put("key", new BASE64Encoder().encode(hash));
			params.put("mitra", mitra);
	        params.put("QR", textQR);
	        params.put("id_doc_mitra", document_id);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
       
        LogSystem.info(request, params.toString(), "QRTEXT", refTrx, "SEND-DOC");
        /*
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            try {
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        byte[] postDataBytes = null;
		try {
			postDataBytes = postData.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
        //HttpURLConnection conn = null;
        HttpsURLConnection conn = null;
		try {
			//conn = (HttpURLConnection)url.openConnection();
			SSLContext sc = null;
	        try {
	        	sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	 
	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	        	@Override
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
	 
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	 
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        //conn.setRequestProperty("Content-Length", params.toString().length());
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(params.toString().getBytes("UTF-8"));
	        conn.setConnectTimeout(30000);
	        
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        String response="";
	        for (int c; (c = in.read()) >= 0;) {
	        	System.out.print((char)c);
	        	response=response+(char)c;
	        }
	            
	        in.close();
	        LogSystem.info(request, response, "QRTextEncrypt", refTrx, "SEND-DOC");
	        try {
				resp = new JSONObject(response);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println("timeout ke QRTEXT");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
	       
		}
		return resp;
		
	}
	
	
}
