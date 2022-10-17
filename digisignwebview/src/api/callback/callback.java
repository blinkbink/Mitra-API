package api.callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import id.co.keriss.consolidate.util.LogSystem;

public class callback {
	
	private HttpServletRequest request;
	private String refTrx="";
	
	
	
	public callback(HttpServletRequest request, String refTrx)
	{
		this.request=request;
		this.refTrx=refTrx;
	}
	
	static TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
        
	     
	  }
	};
	public JSONObject call(String urlCallback, String mitra_req, String email_req, String CATEGORY, long start) throws JSONException, IOException {
		
		URL url = null;
		String response=null;
		String path_app =this.getClass().getName();
		try {
			// Install the all-trusting trust manager
            SSLContext sc;
			try {
				sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
     
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
              
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			}
            };
     
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
     
			url = new URL(urlCallback);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jo=new JSONObject();
		

//        HttpsURLConnection conn = null;
        HttpURLConnection conn = null;
        
		try {
			if (urlCallback.contains("https"))
			{
				conn = (HttpsURLConnection) url.openConnection();
			}
			else
			{
				conn = (HttpURLConnection) url.openConnection();
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
	
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setConnectTimeout(60000);
			conn.connect();
			
			LogSystem.info(request, "Response Code : "+String.valueOf(conn.getResponseCode()), refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

	        try{
	        	  InputStream is = conn.getInputStream();
	    	      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    	      StringBuilder str = new StringBuilder();
	    	      String line = null;
	    	      while((line = reader.readLine()) != null){
	    	          str.append(line + "\n");
	    	      }
	    	      is.close();
	    	      String result = str.toString();
	    	      
	    	      jo.put("code", conn.getResponseCode());
//	    	      jo.put("message", result);

	        }catch (Exception e) {
	        	 jo.put("code", conn.getResponseCode());
	    	     jo.put("message", e.toString());
				// TODO: handle exception
			}
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			jo.put("code", 201);
   	      	jo.put("message", t.toString());
			t.printStackTrace();
			LogSystem.error(request, t.toString(), refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			jo.put("code", 201);
   	      	jo.put("message", e.toString());
			e.printStackTrace();
			LogSystem.error(request, e.toString(), refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} finally {
			conn.disconnect();
		}
		return jo;
		
	}
}
