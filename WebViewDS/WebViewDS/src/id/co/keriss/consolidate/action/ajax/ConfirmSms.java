package id.co.keriss.consolidate.action.ajax;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class ConfirmSms implements DSAPI {

	private final String USER_AGENT = "Mozilla/5.0";

	// HTTP Post request
	public JSONObject sendingPostRequest(String kode, String phone, Long idmitra, String refTrx, HttpServletRequest request) {
		String rst = "Timeout";
		JSONObject jo = null;
		HttpsURLConnection  con=null;
		String kelas="ajax.ConfirmSms";
		String trxType="SND-OTP";
		try {
			String textLog=URLEncoder.encode("{xxxxxx} merupakan nomor verifikasi Anda. nomor ini rahasia, mohon tidak memberikan nomor verifikasi ini kepada siapapun.",
					"UTF-8");
			String text = URLEncoder.encode("{" + kode
					+ "} merupakan nomor verifikasi Anda. nomor ini rahasia, mohon tidak memberikan nomor verifikasi ini kepada siapapun.",
					"UTF-8");
			String dest = URLEncoder.encode(phone, "UTF-8");
			String id_mitra = URLEncoder.encode(Long.toString(idmitra), "UTF-8");
			
			String url = SMS_API + "/service/SMSService.html?text=" + text + "&dest=" + dest + "&id_mitra=" + id_mitra;
			LogSystem.response(request, SMS_API + "/service/SMSService.html?text=" + textLog + "&dest=" + dest + "&id_mitra=" + id_mitra, kelas, refTrx, trxType);

			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}

			} };

			
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
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
     		URL obj = new URL(url);
			con = (HttpsURLConnection) obj.openConnection();

			// Setting basic post request
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/json");
			// String postJsonData = "{'text':'c54s123','dest' : '081212611881'}";
			con.setConnectTimeout(5000);// set timeout to 5 seconds
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			// wr.writeBytes(postJsonData);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			LogSystem.info(request, "Sending 'POST' request to URL : " + url, kelas, refTrx, trxType);
			LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);	
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer response = new StringBuffer();

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();
			con.disconnect();
			// printing result from response
			LogSystem.info(request, response.toString(), kelas, refTrx, trxType);
			String sr = response.toString();			
			jo = new JSONObject(sr);
			LogSystem.response(request, jo, kelas, refTrx, trxType);
			
			rst = jo.getString("result");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		} finally {
			con.disconnect();
		}

		// return rst;
		return jo;
	}
	
	
	
	public JSONObject sendMailOTPMail(String kode,String email, String id_mitra, String nama)  throws Exception {
		URL url = null;
		String rst = "Timeout";
		JSONObject jo = null;
		try {
			url = new URL(EMAIL_API+"otp.html");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    Map<String,Object> params = new LinkedHashMap<>();
	    
	    params.put("otp", kode);
	    if (id_mitra!="Personal") {
	    	params.put("id_mitra", id_mitra);
	    }
	    params.put("email", email);
	    params.put("nama", nama);
	   
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
	 // Install the all-trusting trust manager
	    SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
			
	  

	    // Create all-trusting host name verifier
	    HostnameVerifier allHostsValid = new HostnameVerifier() {
	    	@Override
	        public boolean verify(String hostname, SSLSession session) {
	            return true;
	        }
	    };

	    // Install the all-trusting host verifier
	    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    
	    byte[] postDataBytes = null;
		try {
			postDataBytes = postData.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    HttpURLConnection conn;
		try {
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        conn.setConnectTimeout(30000);
	        
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        
	        String output;
			StringBuffer response = new StringBuffer();
			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();
			conn.disconnect();
			System.out.println(response.toString());
			String sr = response.toString();
			jo = new JSONObject(sr);
			rst = jo.getString("result");
			System.out.println(rst);
//			
//	        for (int c; (c = in.read()) >= 0;)
//	            System.out.print((char)c);
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println("timeout ke email");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}

}
