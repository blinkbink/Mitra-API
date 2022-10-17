package api.log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KMSRSAEncryption;
import id.co.keriss.consolidate.util.LogSystem;

public class ActivityLog {
	
	private HttpURLConnection httpConn;
	private DataOutputStream req;
	private final String boundary = "*****";
	private final String crlf = "\r\n";
	private final String twoHyphens = "--";
	String kodeTrx="";
	String trxType="LogDB.Activity";
	String kelas="api.ActivityLog";
	HttpServletRequest request=null;
	String path_app = this.getClass().getName();
	String mitra_req;
	String email_req;
	String CATEGORY;
	long start;
	
	public ActivityLog(HttpServletRequest req, String kTrx, String mitra_req, String email_req, long start, String CATEGORY) {
		// TODO Auto-generated constructor stub
		kodeTrx=kTrx;
		request=req;
		this.mitra_req = mitra_req;
		this.email_req = email_req;
		this.CATEGORY = CATEGORY;
		this.start = start;
		
	}
	
	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() 
	{
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

	public String POST(String activity, String result, String information, String user, String document_id, String certificate, String password, String otp, String nik) {
		String url = DSAPI.DB_LOG + "/create/activitylog";
		String rst = null;
		try {

			JSONObject jf = new JSONObject();
			ArrayList ar = new ArrayList();
			
			ar.add(password);
			ar.add(otp);
			
			jf.put("activity", activity);
			jf.put("result", result);
			jf.put("information", information);
			jf.put("user", user);
			jf.put("document_id", document_id);
			jf.put("certificate_cn", certificate);
			jf.put("nik", nik);
			jf.put("auth", ar);
			
			
			LogSystem.info(request, jf.toString(), kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			rst = activityLogService(url, jf);
			
		} catch (Exception e) {
			e.printStackTrace();
			LogSystem.error(request, e.getMessage(), kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}
		return rst;
	}
	
	public String GET(String user, String id) {
		String url = DSAPI.DB_LOG + "/retrieve/activitylog";
		String rst = null;
		try {

			JSONObject jf = new JSONObject();
			jf.put("user", user);
			
			LogSystem.info(request, jf.toString(), kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			rst = activityLogService(url, jf);
			
		} catch (Exception e) {
			e.printStackTrace();
			LogSystem.error(request, e.getMessage(), kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}
		return rst;
	}
	
	
	public String activityLogService(String requestURL, JSONObject jo) throws IOException, NoSuchAlgorithmException, KeyManagementException {

		try {
		    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
	            } };
		
	            SSLContext sc = SSLContext.getInstance("SSL");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            HostnameVerifier allHostsValid = new HostnameVerifier() {
	                public boolean verify(String hostname, SSLSession session) {  return true;  }
	            };

	            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	   
	            LogSystem.info(request, "request to URL : " + requestURL, kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

	            URL obj = new URL(requestURL);
	            BufferedReader in = null;
	            
	            if(requestURL.contains("https")) {
	                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
	                con.setRequestMethod("POST");
	                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	                con.setRequestProperty("Content-Type", "application/json");
	                con.setConnectTimeout(3000);
	                con.setDoOutput(true);

	                if(jo!=null) {
	                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	                    wr.write(jo.toString().getBytes());
	                    wr.flush();
	                    wr.close();
	                }

	                int responseCode = con.getResponseCode();
	                LogSystem.info(request, "Sending "+con.getRequestMethod()+" request to URL : " + requestURL, kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	                LogSystem.info(request, "Response Code : " + responseCode, kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

	                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            }else {
	                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	                con.setRequestMethod("POST");
	                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	                con.setRequestProperty("Content-Type", "application/json");
	                con.setConnectTimeout(3000);
	                con.setDoOutput(true);

	                if(jo!=null) {
	                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	                    wr.write(jo.toString().getBytes());
	                    wr.flush();
	                    wr.close();
	                }

	                int responseCode = con.getResponseCode();
	                LogSystem.info(request, "Sending "+con.getRequestMethod()+" request to URL : " + requestURL, kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	                LogSystem.info(request, "Response Code : " + responseCode, kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

	                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            }
		    String output;
	            StringBuffer response = new StringBuffer();
	            while ((output = in.readLine()) != null) {
	                response.append(output);
	            }
	            in.close();
	            LogSystem.info(request, "Response Message : " + response.toString(), kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	            return response.toString();
	    	} catch (Exception e) {
	    		LogSystem.error(request, requestURL + " Message : " + e, kodeTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	            e.printStackTrace();
	            return null;
		}
	}
}
