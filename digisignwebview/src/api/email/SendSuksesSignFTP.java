package api.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import id.co.keriss.consolidate.util.DSAPI;

public class SendSuksesSignFTP {
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
	public void kirim(Date dateSign, String nama, String jk, String email, String list_doc, String idmitra) {
	//public void kirim(String nama, String jk, String email, String nama_pengirim, String jk_pengirim, String nama_doc, String link, String idmitra) {
			
		URL url = null;
		try {
			url = new URL(DSAPI.EMAIL_API+"sendMailListNotifCompleted.html");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Map<String,Object> params = new LinkedHashMap<>();
     
        //Format tanggal
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(dateSign);
        
        //String oldDateString = "2018-01-17 11:34:10.489000";
        String oldDateString = dateFormat.format(dateSign);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        Date d = null;
       
        try {
			d = sdf.parse(oldDateString);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       
        sdf.applyPattern("MMM dd, yyyy hh:mm:ss");
        System.out.println("Date Format : " + sdf.format(d) + " WIB");
        
        
        //params.put("tanggal", sdf.format(d) + " WIB");
        params.put("nama_penerima", nama);
        params.put("jk_penerima", jk);
        params.put("email", email);
        params.put("list_dokumen", list_doc);    
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

//        HttpURLConnection conn=null;
        HttpsURLConnection conn=null;
		try {
//			conn = (HttpURLConnection)url.openConnection();
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
	        
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

	        for (int c; (c = in.read()) >= 0;)
	            System.out.print((char)c);
	        in.close();
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println("timeout ke email");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
	       
		}
	}
}
