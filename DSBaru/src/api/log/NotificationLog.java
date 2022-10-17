//package api.log;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.Reader;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.StringJoiner;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import javax.servlet.http.HttpServletRequest;
//
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//
//import id.co.keriss.consolidate.DS.FaceRecognition;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.KMSRSAEncryption;
//import id.co.keriss.consolidate.util.LogSystem;
//
//public class NotificationLog {
//	
//	private HttpsURLConnection httpConn;
//	private DataOutputStream req;
//	private final String boundary = "*****";
//	private final String crlf = "\r\n";
//	private final String twoHyphens = "--";
//	String kodeTrx="";
//	String trxType="KMS";
//	String kelas="consolidate.util.KmsService";
//	HttpServletRequest request=null;
//	
//	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() 
//	{
//        @Override
//        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//            // TODO Auto-generated method stub
//
//        }
//        @Override
//        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//            // TODO Auto-generated method stub
//
//        }
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            // TODO Auto-generated method stub
//            return null;
//        }
//    }
//	};
//
//	public JSONObject POST(String user, String id) {
//		String url = DSAPI.DB_LOG + "retrieve/notificationlog";
//		JSONObject rst = new JSONObject();
//		try {
//
//			JSONObject jf = new JSONObject();
//			jf.put("user", user);
//			jf.put("last_id", id);
//			
//			LogSystem.info(request, jf.toString(), kelas, kodeTrx, trxType);
//			
//			notificationLogService(url);
//			sendData(jf.toString());
//			rst = finishNotificationLog();
//		} catch (Exception e) {
//			LogSystem.error(request, e.getMessage(), kelas, kodeTrx, trxType);
//		}
//		return rst;
//	}
//	
//	public JSONObject GET(String user, String id) {
//		String url = DSAPI.DB_LOG + "retrieve/notificationlog";
//		JSONObject rst = new JSONObject();
//		try {
//
//			JSONObject jf = new JSONObject();
//			jf.put("user", user);
//			jf.put("last_id", id);
//			
//			LogSystem.info(request, jf.toString(), kelas, kodeTrx, trxType);
//			
//			notificationLogService(url);
//			sendData(jf.toString());
//			rst = finishNotificationLog();
//		} catch (Exception e) {
//			LogSystem.error(request, e.getMessage(), kelas, kodeTrx, trxType);
//		}
//		return rst;
//	}
//	
//	
//	public void notificationLogService(String requestURL) throws IOException, NoSuchAlgorithmException, KeyManagementException {
//
//		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
//
//			@Override
//			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
//					throws java.security.cert.CertificateException {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
//					throws java.security.cert.CertificateException {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//		} };
//
//		// creates a unique boundary based on time stamp
//		URL url = new URL(requestURL);
//
//		// Install the all-trusting trust manager
//		SSLContext sc = SSLContext.getInstance("SSL");
//		sc.init(null, trustAllCerts, new java.security.SecureRandom());
//		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//		// Create all-trusting host name verifier
//		HostnameVerifier allHostsValid = new HostnameVerifier() {
//			@Override
//			public boolean verify(String hostname, SSLSession session) {
//				return true;
//			}
//		};
//
//		// Install the all-trusting host verifier
//		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//
//		httpConn = (HttpsURLConnection) url.openConnection();
//		httpConn.setUseCaches(false);
//		httpConn.setDoOutput(true); // indicates POST method
//		httpConn.setDoInput(true);
//		httpConn.setRequestMethod("POST");
//		httpConn.setRequestProperty("Connection", "Keep-Alive");
//		httpConn.setRequestProperty("Cache-Control", "no-cache");
//		httpConn.setRequestProperty("Content-Type", "application/json");
//		httpConn.setConnectTimeout(30000);
//		req = new DataOutputStream(httpConn.getOutputStream());
//	}
//
//	
//	public void sendData(String value) throws IOException {
//		req.writeBytes(value);
//		req.flush();
//	}	
//	
//	public JSONObject finishNotificationLog() throws IOException, JSONException {
//		String response = "";
//		JSONObject res = null;
//
//		req.flush();
//		req.close();
//
//		// checks server's status code first
//		int status = httpConn.getResponseCode();
//		if (status == HttpURLConnection.HTTP_OK) {
//			InputStream responseStream = new BufferedInputStream(httpConn.getInputStream());
//
//			BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
//			String output;
//			StringBuffer respon = new StringBuffer();
//
//			while ((output = in.readLine()) != null) {
//				respon.append(output);
//			}
//			in.close();
//
//			String sr = respon.toString();
//
//			//log.info("[RESP] tes" + sr);
//			
//			//LogSystem.res
//			JSONObject res1 = new JSONObject(sr);
//			LogSystem.response(request, res1, kelas, kodeTrx, trxType);
//			String dataResp = KMSRSAEncryption.decryptWithPub(res1.getString("JSONFile"));
//			res = new JSONObject(dataResp);
//			//log.info("[DATA] " + dataResp);
//			LogSystem.info(request, res.toString(), kelas, kodeTrx, trxType);
//			response = res.getString("result");
//			httpConn.disconnect();
//		} else {
//			throw new IOException("Server returned non-OK status: " + status);
//		}
//		return res;
//	}
//
//}
