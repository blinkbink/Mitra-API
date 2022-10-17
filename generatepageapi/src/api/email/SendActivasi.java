package api.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import id.co.keriss.consolidate.util.DSAPI;

public class SendActivasi {
	public SendActivasi() {
		// TODO Auto-generated constructor stub
	}
	public void kirim(String nama, String jk, String email, String namamitra, String link, String idmitra, String no_reg) {
		URL url = null;
		try {
			url = new URL(DSAPI.EMAIL_API+"sendMailPreregisterMitra.html");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("no_reg", no_reg);
        params.put("nama", nama);
        params.put("jk", jk);
        params.put("email", email);
        params.put("nama_mitra", namamitra);
        params.put("link", link);
        params.put("id_mitra", idmitra);
       
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
	        
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

	        for (int c; (c = in.read()) >= 0;)
	            System.out.print((char)c);
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println("timeout ke email");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
