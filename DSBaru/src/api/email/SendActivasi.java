package api.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class SendActivasi {
	String refTrx="";
	String kelas="";
	String trxType="";
	HttpServletRequest request;
	
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
	public SendActivasi(String refTrx,String kelas, String trxType,HttpServletRequest request) {
		// TODO Auto-generated constructor stub
		this.refTrx=refTrx;
		this.kelas=kelas;
		this.trxType=trxType;
		this.request=request;
	}
	
	
	public void kirim(String nama, String jk, String email, String namamitra, String link, String idmitra, String no_reg) {
		URL url = null;
		try {
			url = new URL(DSAPI.EMAIL_API+"sendMailPreregisterMitra.html");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LogSystem.info(request, "Send POST to email " + url.toString(),kelas, refTrx, trxType);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("no_reg", no_reg);
        params.put("nama", nama);
        params.put("jk", jk);
        params.put("email", email);
        params.put("nama_mitra", namamitra);
        params.put("link", link);
        params.put("id_mitra", idmitra);
       
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
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        conn.setConnectTimeout(30000);
	        
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        StringBuffer respon = new StringBuffer();
	        String output;
	        while ((output = in.readLine()) != null) {
				respon.append(output);
			}
	        in.close();
	        
	        LogSystem.info(request, "Response email : " + respon.toString(), kelas, refTrx, trxType);
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			
			LogSystem.info(request, "Timeout ke email ",kelas, refTrx, trxType);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
	}
}
