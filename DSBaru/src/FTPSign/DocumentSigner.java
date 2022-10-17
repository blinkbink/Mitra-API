package FTPSign;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

@SuppressWarnings({ "resource", "deprecation" })
public class DocumentSigner implements DSAPI{

	@SuppressWarnings("unused")
	private final String USER_AGENT = "DS-API";


	// HTTP Post request
	public JSONObject sendingPostRequest(String idAccess, String fileOut,CloseableHttpClient httpclient) throws JSONException {
		JSONObject res=null;
		try{
		Date start=new Date();	
		Date end=new Date();	
		String url = KMS_HOST_OLD+"/docSign.html";
		
		HttpPost httpPost = new HttpPost(url);

		JSONObject data=new JSONObject();
		data.put("document_access", idAccess);
		data.put("outfile", fileOut);
		
		JSONObject req=new JSONObject();
		KMSRSAEncryption2 krsa=new KMSRSAEncryption2();
		req.put("JSONFile",  krsa.encryptWithPub(data.toString()));

		LogSystem.info(getClass(),"[POST] "+url+" // "+"{\"JSONFile\" : "+data.toString()+"}");
		
		System.out.println(start+" data :"+data.toString());
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
		                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
		                .addTextBody("jsonfield", req.toString());
		HttpEntity multiPartEntity = builder.build();

		httpPost.setEntity(multiPartEntity);

		
		CloseableHttpResponse rsp = httpclient.execute(httpPost);
		
		int responseCode = rsp.getStatusLine().getStatusCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(rsp.getEntity().getContent()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();

		String sr =response.toString();
		end=new Date();
		LogSystem.info(getClass(),"[RESP] "+url+" {"+responseCode+"}//"+sr);
		JSONObject res1 = new JSONObject(sr);
		
		String dataResp=krsa.decryptWithPub(res1.getString("JSONFile"));
		res = new JSONObject(dataResp);
		LogSystem.info(getClass(),"[DATA] "+url+" //"+dataResp);
		System.out.println(end+" data :"+dataResp+" / "+((double)(end.getTime()-start.getTime())/1000));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
		
		return res;
	}
}
