package api.dukcapil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.DSAPI;

public class checkingData {
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
	public JSONObject check(String nik, String nama, String tempatlahir, String tgllahir, String alamat, String fotowajah, String mitra) {
		URL url = null;
		JSONObject response=null;
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
     
        //con = (HttpsURLConnection) obj.openConnection();
//Setting basic post request
          //con.setRequestMethod("POST");
			//url = new URL("https://DS-Support:7070/DSSv1-0/idVerf.html");
			url = new URL(DSAPI.DUKCAPIL_API+"idVerf.html");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jo=new JSONObject();
		
		try {
			jo.put("nik", nik);
			jo.put("nama", nama);
			jo.put("tempat_lahir", tempatlahir);
	        jo.put("tanggal_lahir", tgllahir);
	        jo.put("alamat", alamat);
	        jo.put("fotowajah", fotowajah);
	        jo.put("mitra", mitra);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

        //HttpURLConnection conn = null;
        HttpsURLConnection conn = null;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			String jos=jo.toString();
			conn.setRequestMethod("POST");
			  conn.setDoInput(true);
			  conn.setDoOutput(true);
			  conn.setRequestProperty("Connection", "Keep-Alive");
			  conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			  conn.setRequestProperty("Accept","application/json");
			  conn.setConnectTimeout(60000);
			  conn.connect();
	        //conn.getOutputStream().write(postDataBytes);
	        conn.getOutputStream().write(jos.getBytes());
	        
	        //Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        /*
	        for (int c; (c = in.read()) >= 0;)
	            System.out.print((char)c);
	        */
	        
	        //System.out.println(conn.getResponseMessage());
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
	        	        //System.out.println("result = "+result);
	        	        response=new JSONObject(result);
	        }catch (Exception e) {
				// TODO: handle exception
			}
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println("timeout ke verifikasi dukcapil");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
		
	}
}
