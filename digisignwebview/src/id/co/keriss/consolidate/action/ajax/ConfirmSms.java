package id.co.keriss.consolidate.action.ajax;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

public class ConfirmSms implements DSAPI {

	private final String USER_AGENT = "Mozilla/5.0";
	
	private String refTrx="";
	private HttpServletRequest request; 

	public ConfirmSms(HttpServletRequest request, String refTrx)
	{
		this.request=request;
		this.refTrx=refTrx;
	}

	// HTTP Post request
	public JSONObject sendingPostRequest(String kode, String phone, Long idmitra, String mitra_req, String email_req) {
		long start = System.currentTimeMillis();
		String path_app = this.getClass().getName();
		String CATEGORY = "OTP";
		String rst = "Timeout";
		JSONObject jo = null;
		HttpsURLConnection  con=null;
		try {
			String text = URLEncoder.encode("{" + kode
					+ "} merupakan nomor verifikasi Anda. nomor ini rahasia, mohon tidak memberikan nomor verifikasi ini kepada siapapun.",
					"UTF-8");
			String dest = URLEncoder.encode(phone, "UTF-8");
			String id_mitra = URLEncoder.encode(Long.toString(idmitra), "UTF-8");
			
			String url = SMS_API + "/service/SMSService.html?text=" + text + "&dest=" + dest + "&id_mitra=" + id_mitra;

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
			LogSystem.info(request, "Sending 'POST' request to URL : " + url, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			// System.out.println("Post Data : " + postJsonData);
			LogSystem.info(request, "Response Code : " + responseCode, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer response = new StringBuffer();

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();
			con.disconnect();
			// printing result from response
			
			String sr = response.toString();
			jo = new JSONObject(sr);
			rst = jo.getString("result");
			LogSystem.info(request, "Response " + sr, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		} finally {
			con.disconnect();
		}

		// return rst;
		return jo;
	}

}
