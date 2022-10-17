package id.co.keriss.consolidate.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

//import webdigi.action.ContextConstants;
//import webdigi.action.km.KMSRSAEncryption;
import id.co.keriss.consolidate.action.ContextConstants;
import id.co.keriss.consolidate.util.KMSRSAEncryption;;

public class KmsService implements DSAPI {
	
	
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
	String rst = "GAGAL";
	String USER_AGENT = "Mozilla/5.0";
	Logger log = LogManager.getLogger(ContextConstants.LOGGER);
	private HttpsURLConnection httpConn;
	private DataOutputStream req;
	private final String boundary = "*****";
	private final String crlf = "\r\n";
	private final String twoHyphens = "--";
	String kodeTrx="";
	String trxType="KMS";
	String kelas="consolidate.util.KmsService";
	HttpServletRequest request=null;
	String path_app =this.getClass().getName();
	String CATEGORY;
	long start;
	String mitra_req;
	String email_req;
	
	public KmsService(HttpServletRequest req, String kTrx, String category, long start, String mitra_req, String email_req) {
		// TODO Auto-generated constructor stub
		kodeTrx=kTrx;
		request=req;
		this.CATEGORY = category;
		this.start = start;
		this.mitra_req = mitra_req;
		this.email_req = email_req;
	}
	
	public JSONObject checkSertifikat(long eeuser, String level, String mitra) throws JSONException {
		String url = DSAPI.KMS_HOSTS + "/certChecking.html";
		LogSystem.info(request, "URL : " + url, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		JSONObject rst = new JSONObject();
		try {
			JSONObject jo = new JSONObject();
			JSONObject jf = new JSONObject();
			jf.put("eeuser", eeuser);
			jf.put("level", level);
			LogSystem.info(request, "Mitranya : " + mitra, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			if(mitra != "")
			{
				jf.put("mitra", mitra);
			}
			LogSystem.info(request, "Request cek sertifikat : " + jf.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
			jo.put("encrypt-mode", "CBC");
			if(baru)
			{
				kmsService(url);
				addFormField("jsonfield", jo.toString());
				rst = finish();
			}
			else
			{
				kmsServiceTextPlain(url);
				textPlain(jo.toString());
				rst = finishTextPlain();
			}

			LogSystem.info(request, "Response cek sertifikat : " + rst, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (Exception e) {
			LogSystem.error(request, e.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}
		return rst;
	}
	
	public JSONObject createSertifikat(Long eeuser, Boolean renewal, String level) throws JSONException {
//		String url = ContextConstants.EJBCA + "/generateCert.html";
		String url = KMS_HOSTS + "/generateCert.html";
		LogSystem.info(request, "URL : " + url, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		JSONObject res = new JSONObject();
		String rst="";
		try {
			// {"JSONFile":{"eeuser": "15250"}}
			JSONObject jo = new JSONObject();
			JSONObject jf = new JSONObject();
			jf.put("eeuser", eeuser);
			jf.put("level", level);
			if (renewal)
			{
				jf.put("renewal", renewal);
			}
			
			LogSystem.info(request, "Request create sertifikat : " + jf.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
			jo.put("encrypt-mode", "CBC");
			if(baru)
			{
				kmsService(url);
				addFormField("jsonfield", jo.toString());
				res = finish();
			}
			else
			{
				kmsServiceTextPlain(url);
				textPlain(jo.toString());
				res = finishTextPlain();
			}
			
			LogSystem.info(request, "Response create sertifikat : " + res, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (Exception e) {
			LogSystem.error(request, e.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}
		return res;
	}

	public JSONObject changeEmail(Long eeuser, String email) throws JSONException {
		String url = KMS_HOSTS + "/revokeCert.html";
		LogSystem.info(request, "URL : " + url, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		JSONObject rst = new JSONObject();
		try {
			JSONObject jo = new JSONObject();
			JSONObject jf = new JSONObject();
			jf.put("eeuser", eeuser);
			jf.put("newEmail", email);
			LogSystem.info(request, "Request revoke sertifikat : " +jf.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
			jo.put("encrypt-mode", "CBC");
			if(baru) 
			{
				kmsService(url);
				addFormField("jsonfield", jo.toString());
				rst = finish();
			}
			else
			{
				kmsServiceTextPlain(url);
				textPlain(jo.toString());
				rst = finishTextPlain();
			}
			
			LogSystem.info(request, "Response revoke sertifikat : " + rst.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (Exception e) {
			LogSystem.error(request, e.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}
		return rst;		
}


	public void kmsService(String requestURL) throws IOException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}

		} };

		// creates a unique boundary based on time stamp
		URL url = new URL(requestURL);

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

		httpConn = (HttpsURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestMethod("POST");
		httpConn.setRequestProperty("Connection", "Keep-Alive");
		httpConn.setRequestProperty("Cache-Control", "no-cache");
		httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);
		httpConn.setConnectTimeout(30000);
		req = new DataOutputStream(httpConn.getOutputStream());
	}

	/**
	 * Adds a form field to the request
	 *
	 * @param name  field name
	 * @param value field value
	 */
	public void addFormField(String name, String value) throws IOException {
		req.writeBytes(this.twoHyphens + this.boundary + this.crlf);
		req.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + this.crlf);
		req.writeBytes("Content-Type: text/plain; charset=UTF-8" + this.crlf);
		req.writeBytes(this.crlf);
		req.writeBytes(value + this.crlf);
		req.flush();
	}

	public JSONObject finish() throws IOException, JSONException {
		String response = "";
		JSONObject res = null;
		req.writeBytes(this.crlf);
		req.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);

		req.flush();
		req.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			InputStream responseStream = new BufferedInputStream(httpConn.getInputStream());

			BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
			String output;
			StringBuffer respon = new StringBuffer();

			while ((output = in.readLine()) != null) {
				respon.append(output);
			}
			in.close();

			String sr = respon.toString();

			LogSystem.info(request, "[RESP] " + sr, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			JSONObject res1 = new JSONObject(sr);

			String dataResp="";
			try {
				dataResp = AESEncryption.decryptDocSign(res1.getString("JSONFile"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			res = new JSONObject(dataResp);
//			LogSystem.response(request, res, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			response = res.getString("result");
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}
		return res;
	}
	
	public void kmsServiceTextPlain(String requestURL) throws IOException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}

		} };

		// creates a unique boundary based on time stamp
		URL url = new URL(requestURL);

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

		httpConn = (HttpsURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestMethod("POST");
		httpConn.setRequestProperty("Connection", "Keep-Alive");
		httpConn.setRequestProperty("Cache-Control", "no-cache");
		httpConn.setRequestProperty("Content-Type", "text/plain");
		httpConn.setConnectTimeout(30000);
		req = new DataOutputStream(httpConn.getOutputStream());
	}

	
	public void textPlain(String value) throws IOException {
		req.writeBytes(value);
		req.flush();
	}	
	
	public JSONObject finishTextPlain() throws Exception {
		String response = "";
		JSONObject res = null;

		req.flush();
		req.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			InputStream responseStream = new BufferedInputStream(httpConn.getInputStream());

			BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
			String output;
			StringBuffer respon = new StringBuffer();

			while ((output = in.readLine()) != null) {
				respon.append(output);
			}
			in.close();

			String sr = respon.toString();

			
			//LogSystem.res
			JSONObject res1 = new JSONObject(sr);
			LogSystem.info(request, res1.toString(), kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			String dataResp = AESEncryption.decryptDocSign(res1.getString("JSONFile"));
			res = new JSONObject(dataResp);
//			LogSystem.response(request, res, kodeTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			response = res.getString("result");
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}
		return res;
	}

}

