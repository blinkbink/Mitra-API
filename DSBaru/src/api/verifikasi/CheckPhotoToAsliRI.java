package api.verifikasi;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import sun.misc.BASE64Decoder;

public class CheckPhotoToAsliRI {
	static TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	  }
	};
	public JSONObject check(String nik, String fotowajah, String mitra, String refTrx, HttpServletRequest request) throws JSONException {
		URL url = null;
		JSONObject response=null;
		String trxType = "trxType";
		String kelas="apiMitra.CheckPhotoToAsliRI";
		try {
			// Install the all-trusting trust manager
            SSLContext sc;
			try {
				sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
     
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
              
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			}
            };
     
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
     
        //con = (HttpsURLConnection) obj.openConnection();
//Setting basic post request
          //con.setRequestMethod("POST");
			//url = new URL("https://DS-Support:7070/DSSv1-0/idVerf.html");
//			url = new URL(DSAPI.DUKCAPIL+"faceCheck.html");
			url = new URL(DSAPI.DUKCAPIL+"facecheck");
//			System.out.println(refTrx+" footo : " + fotowajah);
			//Check foto wajah tipe
//			String extension = "";
//			if (fotowajah.startsWith("/9j"))
//			{
//				extension = "jpg";
//			}
//			else
//			{
//				extension = "png";
//			}
//			
//			if(!extension.equals("jpg"))
//			{
//				System.out.println(refTrx+" Extensi : " + extension);
//				response = new JSONObject();
//				response.put("gagal", "819");
//				response.put("connection", true);
//				return response;
//			}
//			else
//			{
//				BASE64Decoder decoder = new BASE64Decoder();
//		        byte[] decodedBytes = decoder.decodeBuffer(fotowajah);
//				BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
//				
//				ByteArrayOutputStream tmp = new ByteArrayOutputStream();
//			    ImageIO.write(image, "jpg", tmp);
//			    tmp.close();
//			    
//			    Integer contentLength = tmp.size();
//			    System.out.println(refTrx+" Size selfie : " + contentLength);
//				if (contentLength > 635000)
//				{
//					response = new JSONObject();
//					response.put("gagal", "818");
//					response.put("connection", true);
//					return response;
//				}
//			}
			LogSystem.info(request, "URL : " + url.toString(),kelas, refTrx,trxType);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jo=new JSONObject();
		
		try {
			jo.put("nik", nik);
			//jo.put("nama", nama);
			//jo.put("tempat_lahir", tempatlahir);
	        //jo.put("tanggal_lahir", tgllahir);
	        //jo.put("alamat", alamat);
	        jo.put("fotowajah", fotowajah);
	        jo.put("mitra", mitra);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

        //HttpURLConnection conn = null;
        HttpsURLConnection conn = null;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			String jos=jo.toString();
			conn.setRequestMethod("POST");
			  conn.setDoInput(true);
			  conn.setDoOutput(true);
			  conn.setRequestProperty("Connection", "Keep-Alive");
			  conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			  conn.setRequestProperty("Accept","application/json");
			  conn.setConnectTimeout(60000);
			  conn.connect();
	        //conn.getOutputStream().write(postDataBytes);
	        conn.getOutputStream().write(jos.getBytes());
	        
	        //Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        /*
	        for (int c; (c = in.read()) >= 0;)
	            System.out.print((char)c);
	        */
	        System.out.println(refTrx+" Response code : " + conn.getResponseCode());
//	        if(conn.getResponseCode() != HttpsURLConnection.HTTP_OK)
//	        {
//	        	response = new JSONObject();
//	        	response.put("connection", true);
//	        	response.put("", value)
//	        	return response;
//	        }
	        try{
	        		InputStream is = null;
	        		if (conn.getResponseCode() == 200) 
	        		{
	        			is = conn.getInputStream();
	        		}
	        		else
	        		{
	        			is = conn.getErrorStream();
	        		}
        	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        	        StringBuilder str = new StringBuilder();
        	        String line = null;
        	        while((line = reader.readLine()) != null){
        	            str.append(line + "\n");
        	        }
        	        is.close();

        	        String result = str.toString();
        	        response=new JSONObject(result);
        	        System.out.println(refTrx+" Response : " + response);
	        }catch (Exception e) {
				// TODO: handle exception
	        	e.printStackTrace();
			}
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println(refTrx+" timeout ke verifikasi dukcapil");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		return response;
		
	}

}
