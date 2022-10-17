package id.co.keriss.consolidate.action.ajax;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.action.ContextConstants;

public class SendMailApi {

	String rst = "Timeout";
	// {692015}
	String USER_AGENT = "Mozilla/5.0";
	Logger log = LogManager.getLogger(ContextConstants.LOGGER);
	String emailHost =  ContextConstants.EMAIL_HOST ;
	String hostLink =  ContextConstants.HOST_LINK;
	
	public void sendMailSuccessRegister(String email, String nama, String jeniskelamin, String idmit) {

		String dest;
		try {
			dest = URLEncoder.encode(email, "UTF-8");

			String name = URLEncoder.encode(nama, "UTF-8");
			String jk = URLEncoder.encode(jeniskelamin, "UTF-8");
			String id_mitra = URLEncoder.encode(idmit, "UTF-8");
			String url =emailHost+"/sendMailRegisterSuccess.html?link="
					+ hostLink+"&nama=" + name + "&jk=" + jk + "&email=" + dest + "&id_mitra="
					+ id_mitra;

			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error(getClass(),e);
		}
	}
	
	public void sendMailRegister(String email, String nama, String jeniskelamin) {

		try {
			String dest = URLEncoder.encode(email, "UTF-8");
			String jk = URLEncoder.encode(jeniskelamin, "UTF-8");
			String name = URLEncoder.encode(nama, "UTF-8");
			String url = emailHost + "/sendMailRegister.html?link=" + hostLink + "&nama=" + name + "&jk=" + jk
					+ "&email=" + dest;
			
			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error(getClass(),e);
		}
	}
	
	public void sendSuccessActivation(String email, String nama, char j, long id_mitra) {

		try {
			String jeniskelamin =  Character.toString(j);
			String dest = URLEncoder.encode(email, "UTF-8");
			String jk = URLEncoder.encode(jeniskelamin, "UTF-8");
			String name = URLEncoder.encode(nama, "UTF-8");
			String idMitra = URLEncoder.encode(Long.toString(id_mitra), "UTF-8");
			
			String url = emailHost + "sendMailRegisterSuccess.html?link=" + hostLink + "&nama=" + name + "&jk=" + jk
					+ "&email=" + dest + "&id_mitra=" + idMitra;
			
			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error(getClass(),e);
		}
	}

	public void mailOtp(String kode, String email, String nama,String idmit) {

		try {
			String text = URLEncoder.encode(kode, "UTF-8");
			String dest = URLEncoder.encode(email, "UTF-8");
			String name = URLEncoder.encode(nama, "UTF-8");
			String id_mitra = URLEncoder.encode(idmit, "UTF-8");
			String url = emailHost+ "otp.html?otp=" + text + "&email=" + dest + "&nama=" + name+ "&id_mitra=" + id_mitra;
		
			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error(getClass(),e);
		}
	}
	
	public void mailDocument_signed( String nama_dokumen, String link,String nama_ttd,String email,String jk_ttd,String nama_penerima,String jk_penerima,String idmit) {

		try {
		
			String namedok = URLEncoder.encode(nama_dokumen, "UTF-8");
			String linkdok = URLEncoder.encode(link, "UTF-8");
			String nama_sign = URLEncoder.encode(nama_ttd, "UTF-8");
			String jk_sign = URLEncoder.encode(jk_ttd, "UTF-8");
			String nama_recive = URLEncoder.encode(nama_penerima, "UTF-8");
			String jk_recive = URLEncoder.encode(jk_penerima, "UTF-8");
			String emailsign = URLEncoder.encode(email, "UTF-8");
			String id_mitra = URLEncoder.encode(idmit, "UTF-8");
			
			String url = emailHost+"sendMailNotifSign.html?nama_dokumen="+namedok+"&link="+linkdok+"&nama_ttd="+nama_sign+"&email="+emailsign+"&jk_ttd="+jk_sign+"&nama_penerima="+nama_recive+"&jk_penerima="+jk_recive+"&id_mitra="+id_mitra;
			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void mailDocument_complete(String nama_dokumen, String link,String email,String nama_penerima,String jk_penerima,String idmit) {

		try {
		
			String nama_dok = URLEncoder.encode(nama_dokumen, "UTF-8");
			String linkdok = URLEncoder.encode(link, "UTF-8");
			String nama_recive= URLEncoder.encode(nama_penerima, "UTF-8");
			String jk_receive = URLEncoder.encode(jk_penerima, "UTF-8");
			String email_receive = URLEncoder.encode(email, "UTF-8");
			String id_mitra = URLEncoder.encode(idmit, "UTF-8");
			
			String url = emailHost+"sendMailNotifCompleted.html?nama_dokumen="+nama_dok+"&link="+linkdok+"&email="+email_receive+"&nama_penerima="+nama_recive+"&jk_penerima="+jk_receive+"&id_mitra="+id_mitra;
			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void mailDocument_received(String html, String nama_dokumen, String link,String nama,String email,String jk,String nama_pengirim,String jk_pengirim,String idmit) {

		try {
		
			// Tipe html 
			// String html = "sendMailFileaReqSign.html";
			// String html = "sendMailFile.html";
			
			String nama_dok = URLEncoder.encode(nama_dokumen, "UTF-8");
			String linkdok = URLEncoder.encode(link, "UTF-8");
			String nama_received = URLEncoder.encode(nama, "UTF-8");
			String email_received= URLEncoder.encode(email, "UTF-8");
			String jk_received = URLEncoder.encode(jk, "UTF-8");
			String nama_send = URLEncoder.encode(nama_pengirim, "UTF-8");
			String jk_send = URLEncoder.encode(jk_pengirim, "UTF-8");
			String id_mitra = URLEncoder.encode(idmit, "UTF-8");
			
			String url = emailHost+html+"?nama_dokumen="+nama_dok+"&link="+linkdok+"&nama="+nama_received+"&email="+email_received+"&jk="+jk_received+"&nama_pengirim="+nama_send+"&jk_pengirim="+jk_send+"&id_mitra="+id_mitra;
			sendingEmailApi mail = new sendingEmailApi(url);
			Thread th = new Thread(mail);
			th.start();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	class sendingEmailApi implements Runnable {

		String url = "";

		public sendingEmailApi(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				// TODO Auto-generated method stub
				// Setting basic post request
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				// con.setRequestProperty("Content-Type", "application/json");
				// String postJsonData = "{'text':'c54s123','dest' : '081212611881'}";
				con.setConnectTimeout(5000);// set timeout to 5 seconds
				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				// wr.writeBytes(postJsonData);
				wr.flush();
				wr.close();

				int responseCode = con.getResponseCode();
				log.info("Sending 'POST' request to URL : " + url);
				// log.info("Post Data : " + postJsonData);
				log.info("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String output;
				StringBuffer response = new StringBuffer();

				while ((output = in.readLine()) != null) {
					response.append(output);
				}
				in.close();

				// printing result from response
				log.info(response.toString());
				String sr = response.toString();
				JSONObject jo = new JSONObject(sr);
				rst = jo.getString("result");
				log.info(rst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e);
			}

		}

	}
}