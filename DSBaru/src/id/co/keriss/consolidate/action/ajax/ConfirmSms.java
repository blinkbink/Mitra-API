package id.co.keriss.consolidate.action.ajax;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class ConfirmSms implements DSAPI {
	String kelas="id.co.keriss.consolidate.action.ajax.ConfirmSms";
	private final String USER_AGENT = "Mozilla/5.0";

//	public static void main(String args[]) {
//
//		ConfirmSms http = new ConfirmSms();
//		// Sending post request
//		Long id = 1;
//		try {
//			http.sendingPostRequest("324512","081318696374", );
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
////			LogSystem.error(getClass(), e);
//			e.printStackTrace();
//		}
//	}

	// HTTP Post request
	public String sendingPostRequest(String kode,String phone, Long idmitra,HttpServletRequest  request, String refTrx, String trxType) {
		String rst = "Timeout";
		try{
			String text = URLEncoder.encode(
				    "{"+kode+"} merupakan nomor verifikasi Anda. nomor ini rahasia, mohon tidak memberikan nomor verifikasi ini kepada siapapun.",
				    "UTF-8");
		String dest = URLEncoder.encode(phone, "UTF-8");
		String url = SMS_API+"/service/SMSService.html?text=" + text + "&dest=" + dest + "id_mitra=" + idmitra;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// Setting basic post request
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setRequestProperty("Content-Type", "application/json");
		// String postJsonData = "{'text':'c54s123','dest' : '081212611881'}";
		con.setConnectTimeout(5000);//set timeout to 5 seconds
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		// wr.writeBytes(postJsonData);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		LogSystem.info(request, "Sending 'POST' request to URL : " + url, kelas, refTrx, trxType);
		// System.out.println("Post Data : " + postJsonData);
		LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();

		// printing result from response
		LogSystem.info(request, "Response : " + response.toString(), kelas, refTrx, trxType);
		String sr = response.toString();
		JSONObject jo = new JSONObject(sr);
		rst = jo.getString("result");
		System.out.println(rst);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
		
		return rst;
	}

}
