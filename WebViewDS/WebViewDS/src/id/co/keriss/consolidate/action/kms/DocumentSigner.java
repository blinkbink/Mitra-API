package id.co.keriss.consolidate.action.kms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.mapping.Array;

import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KMSRSAEncryption;
import id.co.keriss.consolidate.util.LogSystem;

public class DocumentSigner implements DSAPI{

	private final String USER_AGENT = "DS-API";
	private final String kelas = "DocumentSigner";
	private final String trxType = "SGN";

	public JSONObject kirimMeterai(HttpServletRequest request, String refTrx, Long documentId, String docName, Long mitra, float llx, float lly, float urx, float ury,int page, String document, String location) {
		//public void kirim(String nama, String jk, String email, String nama_pengirim, String jk_pengirim, String nama_doc, String link, String idmitra) {
		JSONObject resp=new JSONObject();

		URL url;
		try {
			url = new URL(DSAPI.METERAI);
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return null;
		}
	
		LogSystem.info(request, "URL = "+ url, kelas, refTrx, trxType+"-METERAI");
        JSONObject data=new JSONObject();
        JSONObject req=new JSONObject();
        String charset = "UTF-8";
		try {
			data.put("document_id", documentId);
			data.put("document_name", docName);
			data.put("mitra_id", mitra);
			data.put("llx", llx);	
			data.put("lly", lly);
			data.put("urx", urx);
			data.put("ury", ury);
			data.put("page", page);
			
			if(location == null)
			{
				data.put("location", "");
			}
			else
			{
				data.put("location", location);
			}
			
			data.put("document_data", document);
			LogSystem.info(request, "isi data proses ke Meterai = "+StringUtils.left(data.toString(), 200), kelas, refTrx, trxType+"-METERAI");
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(50000);
            conn.setReadTimeout(50000);

            String input = data.toString();
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            
            LogSystem.info(request, "Response code "+ conn.getResponseCode(), kelas, refTrx, trxType+"-METERAI");
            
//            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + conn.getResponseCode());
//            }

            BufferedReader br = null;
            try {
            	br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            }catch(Exception e)
            {
            	br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
            }

            String output;
            String response="";
            while ((output = br.readLine()) != null) {
                response+=output;
            }
            conn.disconnect();

            JSONObject rspJSON= new JSONObject(response);
            
            return rspJSON;
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
	}

	// HTTP Post request
//	public JSONObject sendingPostRequest(String idAccess, String fileOut, String refTrx, HttpServletRequest request, String version) throws JSONException {
//		JSONObject res=null;
//		String kelas="kms.DocumentSigner";
//		String trxType="PRC-SGN";
//		try{
//			
//			String url = null;
//			if (version.equals("v1"))
//			{
//				url = KMS_HOST_OLD+"/docSign.html";
//			}
//			
//			if  (version.equals("v3"))
//			{
//				url = KMS_HOST+"/docSign.html";
//			}
//		
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpPost httpPost = new HttpPost(url);
//
//		JSONObject data=new JSONObject();
//		data.put("document_access", idAccess);
//		data.put("outfile", fileOut);
//		
//		JSONObject req=new JSONObject();
//		req.put("JSONFile",  AESEncryption.encryptDocSign(data.toString()));
//		
//		LogSystem.info(request, url, kelas, refTrx, trxType);
//		LogSystem.response(request,url+" // "+"{\"JSONFile\" : "+data.toString()+"}", kelas, refTrx, trxType);
//
//		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
//		                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//		                .addTextBody("jsonfield", req.toString());
//		HttpEntity multiPartEntity = builder.build();
//		httpPost.setEntity(multiPartEntity);
//
//		
//		HttpResponse rsp = httpclient.execute(httpPost);
//		
//		int responseCode = rsp.getStatusLine().getStatusCode();
//
//		BufferedReader in = new BufferedReader(new InputStreamReader(rsp.getEntity().getContent()));
//		String output;
//		StringBuffer response = new StringBuffer();
//
//		while ((output = in.readLine()) != null) {
//			response.append(output);
//		}
//		in.close();
//
//
//		String sr =response.toString();
//		
//		LogSystem.info(request, sr, kelas, refTrx, trxType);
//		LogSystem.info(request,url+" {"+responseCode+"}//"+sr, kelas, refTrx, trxType);
//		
//		JSONObject res1 = new JSONObject(sr);
//		
//		String dataResp=AESEncryption.decryptDocSign(res1.getString("JSONFile"));
//		res = new JSONObject(dataResp);
//		LogSystem.info(getClass(),"[DATA] "+url+" //"+dataResp);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//			
//		}
//		
//		return res;
//	}
	
	public JSONObject sendingPostRequest2(String idAccess, String fileOut, HttpServletRequest request, String refTrx, String version) throws JSONException {
		JSONObject res=null;
		try{
			
			String url = null;
			if (version.equals("v1"))
			{
				url = KMS_HOST_OLD+"/docSign.html";
			}
			
			if  (version.equals("v3"))
			{
				url = KMS_HOST+"/docSign.html";
			}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		JSONObject data=new JSONObject();
		data.put("document_access", idAccess);
		data.put("outfile", fileOut);
		
		JSONObject req=new JSONObject();
		//req.put("JSONFile",  KMSRSAEncryption.encryptWithPub(data.toString()));
		req.put("JSONFile",  AESEncryption.encryptDocSign(data.toString()));
		req.put("encrypt-mode", "CBC");

		//LogSystem.info(getClass(),"[POST] "+url+" // "+"{\"JSONFile\" : "+data.toString()+"}");
		LogSystem.info(request, "request KMS = "+req.toString(), "DocumentSigning", refTrx, "SGN-KMS");
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
		                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
		                .addTextBody("jsonfield", req.toString());
		HttpEntity multiPartEntity = builder.build();
		httpPost.setEntity(multiPartEntity);
		httpPost.setConfig(RequestConfig.custom().setConnectionRequestTimeout(10000).build());
		httpPost.setHeader("tokenId", refTrx);
		
		
		HttpResponse rsp = httpclient.execute(httpPost);
		
		int responseCode = rsp.getStatusLine().getStatusCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(rsp.getEntity().getContent()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();


		String sr =response.toString();
		
		//LogSystem.info(getClass(),"[RESP] "+url+" {"+responseCode+"}//"+sr);
		LogSystem.info(request, "response KMS = "+sr, "DocumentSigning", refTrx, "SGN-KMS");
		JSONObject res1 = new JSONObject(sr);
		
		//String dataResp=KMSRSAEncryption.decryptWithPub(res1.getString("JSONFile"));
		String dataResp=AESEncryption.decryptDocSign(res1.getString("JSONFile"));
		res = new JSONObject(dataResp);
		//LogSystem.info(getClass(),"[DATA] "+url+" //"+dataResp);
		LogSystem.info(request, "response KMS Decrypt = "+dataResp, "DocumentSigning", refTrx, "SGN-KMS");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
		
		
		return res;
	}
	
//	public JSONObject sendingPostRequest3(Long[] idAccess, String fileOut, HttpServletRequest request, String refTrx) {
//		JSONObject res=null;
//		try{
//			
//		String url = KMS_HOST+"/docSign.html";
//		
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpPost httpPost = new HttpPost(url);
//
//		JSONObject data=new JSONObject();
//		data.put("document_access", Arrays.toString(idAccess));
//		data.put("outfile", fileOut);
//		LogSystem.info(request, "isi data ke KMS = "+data.toString(), "DocumentSigning", refTrx, "SGN-KMS");
//		
//		JSONObject req=new JSONObject();
//		//req.put("JSONFile",  KMSRSAEncryption.encryptWithPub(data.toString()));
//		req.put("JSONFile",  AESEncryption.encryptDocSign(data.toString()));
//		req.put("encrypt-mode", "CBC");
//
//		//LogSystem.info(getClass(),"[POST] "+url+" // "+"{\"JSONFile\" : "+data.toString()+"}");
//		LogSystem.info(request, "request KMS = "+req.toString(), "DocumentSigning", refTrx, "SGN-KMS");
//		
//		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
//		                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//		                .addTextBody("jsonfield", req.toString());
//		HttpEntity multiPartEntity = builder.build();
//		httpPost.setEntity(multiPartEntity);
//		httpPost.setConfig(RequestConfig.custom().setConnectionRequestTimeout(10000).build());
//		
//		
//		HttpResponse rsp = httpclient.execute(httpPost);
//		
//		//int responseCode = rsp.getStatusLine().getStatusCode();
//
//		BufferedReader in = new BufferedReader(new InputStreamReader(rsp.getEntity().getContent()));
//		String output;
//		StringBuffer response = new StringBuffer();
//
//		while ((output = in.readLine()) != null) {
//			response.append(output);
//		}
//		in.close();
//
//
//		String sr =response.toString();
//		
//		//LogSystem.info(getClass(),"[RESP] "+url+" {"+responseCode+"}//"+sr);
//		LogSystem.info(request, "response KMS = "+sr, "DocumentSigning", refTrx, "SGN-KMS");
//		JSONObject res1 = new JSONObject(sr);
//		
//		//String dataResp=KMSRSAEncryption.decryptWithPub(res1.getString("JSONFile"));
//		String dataResp=AESEncryption.decryptDocSign(res1.getString("JSONFile"));
//		res = new JSONObject(dataResp);
//		//LogSystem.info(getClass(),"[DATA] "+url+" //"+dataResp);
//		LogSystem.info(request, "response KMS Decrypt = "+dataResp, "DocumentSigning", refTrx, "SGN-KMS");
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			LogSystem.error(getClass(), e);
//			try {
//				res.put("result", "91");
//				res.put("info", "timeut kms");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			return res;
//		}
//		
//		return res;
//	}
	
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
	
	public JSONObject kirimKMS(JSONArray idAccess, String fileOut, HttpServletRequest request, String refTrx, Long user, Long documentId, String version) throws JSONException {
		//public void kirim(String nama, String jk, String email, String nama_pengirim, String jk_pengirim, String nama_doc, String link, String idmitra) {
		JSONObject resp=new JSONObject();		
		String url = null;
		if (version.equals("v1"))
		{
			url = KMS_HOST_OLD+"/docSign.html";
		}
		
		if  (version.equals("v3"))
		{
			url = KMS_HOST+"/docSign.html";
		}
	        
	     
        JSONObject data=new JSONObject();
        JSONObject req=new JSONObject();
        String charset = "UTF-8";
		try {
			data.put("document_access", idAccess);
			data.put("outfile", fileOut);
			data.put("user", user);
			data.put("document_id", documentId);
			LogSystem.info(request, "isi data ke KMS = "+data.toString(), "DocumentSigning", refTrx, "SGN-KMS");
			
			
			//req.put("JSONFile",  KMSRSAEncryption.encryptWithPub(data.toString()));
			try {
				req.put("JSONFile",  AESEncryption.encryptDocSign(data.toString()));
				req.put("encrypt-mode", "CBC");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String sr="";
		try {
            id.co.keriss.consolidate.action.billing.MultipartUtility multipart = new id.co.keriss.consolidate.action.billing.MultipartUtility(url, charset,null, refTrx);
            multipart.addHeaderField("tokenId", refTrx);
            multipart.addFormField("jsonfield", req.toString() );
             
            List<String> response = multipart.finish();
             
//            System.out.println("SERVER REPLIED:");
             
            for (String line : response) {
//                System.out.println(line);
                sr+=line;
            }
            
            try {
				JSONObject received=new JSONObject(sr);
				LogSystem.info(request, "response KMS = "+received.toString(), "DocumentSigning", refTrx, "SGN-KMS");
				String dataResp=AESEncryption.decryptDocSign(received.getString("JSONFile"));
				resp=new JSONObject(dataResp);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resp;
			
	}
}
