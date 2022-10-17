package id.co.keriss.consolidate.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonParser;

import jdk.nashorn.internal.parser.JSONParser;


public class KmsService implements DSAPI {
	String rst = "GAGAL";
	String USER_AGENT = "Mozilla/5.0";
	//Logger log = Logger.getLogger(ContextConstants.LOGGER);
	private HttpsURLConnection httpConn;
	private DataOutputStream req;
	private final String boundary = "*****";
	private final String crlf = "\r\n";
	private final String twoHyphens = "--";
	String kodeTrx="";
	String trxType="KMS";
	String kelas="util.KmsService";
	HttpServletRequest request=null;
	
	public KmsService(HttpServletRequest req, String kTrx) {
		// TODO Auto-generated constructor stub
		kodeTrx=kTrx;
		request=req;
	}
	
	public JSONObject checkSertifikat(long eeuser, String level, String mitra) {
		String url = DSAPI.KMS_HOST_BANYAK + "/certChecking.html";
		LogSystem.info(request, "URL : " + url, kelas, kodeTrx, trxType);
		JSONObject rst = new JSONObject();
		try {
			JSONObject jo = new JSONObject();
			JSONObject jf = new JSONObject();
			jf.put("eeuser", eeuser);
			LogSystem.info(request, "levelnya : " + level, kelas, kodeTrx, trxType);
			jf.put("level", level);
			LogSystem.info(request, "Mitranya : " + mitra, kelas, kodeTrx, trxType);
			if(mitra != "")
			{
				jf.put("mitra", mitra);
			}
			LogSystem.info(request, "Request cek sertifikat : " + jf.toString(), kelas, kodeTrx, trxType);
			jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
//			jo.put("JSONFile", KMSRSAEncryption.encryptWithPub(jf.toString()));
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
			
			LogSystem.info(request, "Response cek sertifikat : " + rst.toString(), kelas, kodeTrx, trxType);
		} catch (Exception e) {
			LogSystem.error(getClass(), e);
		}
		return rst;
	}
	
	public JSONObject createSertifikat(Long eeuser, Boolean renewal, String level) {
//		String url = ContextConstants.EJBCA + "/generateCert.html";
		String url = DSAPI.KMS_HOST_BANYAK + "/generateCert.html";
		LogSystem.info(request, "URL : " + url, kelas, kodeTrx, trxType);
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
			
			LogSystem.info(request, "Request create sertifikat : " + jf.toString(), kelas, kodeTrx, trxType);
			jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
//			jo.put("JSONFile", KMSRSAEncryption.encryptWithPub(jf.toString()));			
			jo.put("encrypt-mode", "CBC");
			LogSystem.info(request, "URL : " + url, kelas, kodeTrx, trxType);
			if(baru)
			{
				kmsService(url);
				addFormField("jsonfield", jo.toString());
				res = finish();
				rst = res.getString("result");
			}
			else
			{
				kmsServiceTextPlain(url);
				textPlain(jo.toString());
				res = finishTextPlain();
				rst = res.getString("result");
			}
			
			
			LogSystem.info(request, "Response create sertifikat : " + res.toString(), kelas, kodeTrx, trxType);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return rst;
		return res;
	}

	public String changeEmail(Long eeuser, String email) {
		String url = DSAPI.KMS_HOST_BANYAK + "/revokeCert.html";
		LogSystem.info(request, "URL : " + url, kelas, kodeTrx, trxType);
		// {"JSONFile":{"eeuser": "15065", "newEmail":"fiki.arfiandi@yahoo.com"}}
		JSONObject res = new JSONObject();
		String rst="";

		try {
			JSONObject jo = new JSONObject();
			JSONObject jf = new JSONObject();
			jf.put("eeuser", eeuser);
			jf.put("newEmail", email);
			LogSystem.info(request, "Request revoke sertifikat : " + jf.toString(), kelas, kodeTrx, trxType);
			jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
//			jo.put("JSONFile", KMSRSAEncryption.encryptWithPub(jf.toString()));
			jo.put("encrypt-mode", "CBC");
			if(baru)
			{
				kmsService(url);
				addFormField("jsonfield", jo.toString());
				res = finish();
				rst = res.getString("result");
			}
			else
			{
				kmsServiceTextPlain(url);
				textPlain(jo.toString());
				rst = finishTextPlain().getString("result");
			}

			LogSystem.info(request, "Response revoke sertifikat : " + res.toString(), kelas, kodeTrx, trxType);
		} catch (Exception e) {
			LogSystem.error(request, e.getMessage(), kelas, kodeTrx, trxType);
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

			//log.info("[RESP] tes" + sr);
			
			//LogSystem.res
			JSONObject res1 = new JSONObject(sr);
			LogSystem.response(request, res1, kelas, kodeTrx, trxType);
			String dataResp="";
			try {
				dataResp = AESEncryption.decryptDocSign(res1.getString("JSONFile"));
//				dataResp = KMSRSAEncryption.decryptWithPub(res1.getString("JSONFile"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			res = new JSONObject(dataResp);
			//log.info("[DATA] " + dataResp);
			LogSystem.info(request, res.toString(), kelas, kodeTrx, trxType);
			response = res.getString("result");
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}
		return res;
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

			//log.info("[RESP] tes" + sr);
			
			//LogSystem.res
			JSONObject res1 = new JSONObject(sr);
			LogSystem.info(request, "Response : " + res1.toString(), kelas, kodeTrx, trxType);
			String dataResp = AESEncryption.decryptDocSign(res1.getString("JSONFile"));
//			String dataResp = KMSRSAEncryption.decryptWithPub(res1.getString("JSONFile"));
			res = new JSONObject(dataResp);
			//log.info("[DATA] " + dataResp);
			LogSystem.info(request, dataResp, kelas, kodeTrx, trxType);
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

}

