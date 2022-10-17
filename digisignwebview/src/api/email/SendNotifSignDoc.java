package api.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class SendNotifSignDoc {

	Date dateSign;
	String nama;
	String jk;
	String email;
	String nama_pengirim;
	String jk_pengirim;
	String nama_doc;
	String link;
	String idmitra;
	String refTrx="";
	HttpServletRequest request;
	String path_app =this.getClass().getName();
	String mitra_req;
	String CATEGORY;
	long start;

	public SendNotifSignDoc(HttpServletRequest request, String refTrx)
	{
		this.refTrx=refTrx;
		this.request=request;
	}

	public void kirim(Date dateSign, String nama, String jk, String email, String nama_pengirim, String jk_pengirim,
			String nama_doc, String link, String idmitra, String mitra_req, String CATEGORY, long start) {

		this.dateSign = dateSign;
		this.nama = nama;
		this.jk = jk;
		this.email = email;
		this.nama_pengirim = nama_pengirim;
		this.jk_pengirim = jk_pengirim;
		this.nama_doc = nama_doc;
		this.link = link;
		this.idmitra = idmitra;

		sendMails sm = new sendMails();
		Thread tm = new Thread(sm);
		tm.start();
	}

	class sendMails implements Runnable {

		@Override
		public void run() {
			
			
			
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

			URL url = null;
			try {
				url = new URL(DSAPI.EMAIL_API + "sendMailNotifSign.html");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, Object> params = new LinkedHashMap<>();

			// Format tanggal
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
			String strDate = dateFormat.format(dateSign);

			// String oldDateString = "2018-01-17 11:34:10.489000";
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
//			LogSystem.info(request, "Date Format : " + sdf.format(d) + " WIB", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

			params.put("tanggal", sdf.format(d) + " WIB");
			params.put("nama_penerima", nama);
			params.put("jk_penerima", jk);
			params.put("email", email);
			params.put("nama_ttd", nama_pengirim);
			params.put("jk_ttd", jk_pengirim);
			params.put("nama_dokumen", nama_doc);
			params.put("link", link);
			params.put("id_mitra", idmitra);
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
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
//        HttpURLConnection conn = null;
			HttpsURLConnection conn = null;
			try {
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

				public boolean verify(String arg0, SSLSession arg1) {
					// TODO Auto-generated method stub
					return true;
				}
	        
	        };

				// Install the all-trusting host verifier
//	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//	        System.out.println("CEDEBUG");
				LogSystem.info(request, "URL : " + url, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				conn = (HttpsURLConnection) url.openConnection();
//			conn = (HttpURLConnection)url.openConnection();
//			System.out.println("KEDEBUG");
				conn.setRequestMethod("POST");
//			System.out.println("KEDEBUG2");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//	        System.out.println("KEDEBUG3");
				conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//	        System.out.println("KEDEBUG4");
				conn.setDoOutput(true);
//	        System.out.println("KEDEBUG5");
				conn.getOutputStream().write(postDataBytes);
//			System.out.println("BEDEBUG");
				conn.setConnectTimeout(5000);
//	        System.out.println("DEBUG");
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//	        System.out.println("BLEDEBUG");
				String output;
				StringBuffer respon = new StringBuffer();

				while ((output = in.readLine()) != null) {
					respon.append(output);
				}
				in.close();

				String sr = respon.toString();
				
				LogSystem.info(request, "Response " + sr, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			} catch (java.net.SocketTimeoutException t) {
				// return false;
				t.printStackTrace();
				
				try {
					LogSystem.info(request, "timeout ke email", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				conn.disconnect();

			}
		}
	}
}