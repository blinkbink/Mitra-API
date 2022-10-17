package api.System;

import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;

import api.email.SendMailApi;

public class HealthCheck extends ActionSupport {

	private static String basepathPreReg="/file2/data-DS/PreReg/";
	private static String basepathRegLog="/file2/data-DS/LogRegistrasi/";
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx="";
	String kelas="apiMitra.RegMitra";
	String trxType="REGD";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg)  {
		Date tgl= new Date();
		String refTrx="REGD"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();

		String uuid = UUID.randomUUID().toString().replace("-", "");

		LogSystem.info(request, "DATA DEBUG :"+(i++));
		try{
	
	        JSONObject jo = null;
	        jo=HealthCheck(context, request, uuid, refTrx);
	        
			context.put("trxjson", jo.toString());
	
			LogSystem.response(request, jo, kelas, refTrx, trxType);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	JSONObject HealthCheck(JPublishContext context,  HttpServletRequest  request, String uuid, String refTrx) throws JSONException, StaleObjectStateException, HibernateException, NoSuchAlgorithmException, KeyManagementException{

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

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() 
		{
	        @Override
	        public boolean verify(String hostname, SSLSession session) 
	        {
	        	return true;
	        }
		};

      	HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	      
		JSONObject jo=new JSONObject();
		ArrayList<String> alist=new ArrayList<String>();
		DB db = null;
		try {
			db = new DB();
			try {
				db = getDB(context);
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			LogSystem.info(request, "DB Connection status : " + db.session().isConnected(), kelas, refTrx, trxType+"/HealthCheck");
			
			//Check connection to DB
			LogSystem.info(request, "Check Database connection", kelas, refTrx, trxType+"/HealthCheck");
			try {
				UserManager user = new UserManager(db);
				LogSystem.info(request, "User check : " + user.findById(Long.valueOf(1)),kelas, refTrx, trxType+"/HealthCheck");
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("DB");
			}
			
			//Check koneksi Samba
			SMBClient client = new SMBClient();
			String server = DSAPI.FILESYS_SERVER_ADDRESS_NAS;
			String username = DSAPI.FILESYS_USERNAME_NAS;
			String pwd = DSAPI.FILESYS_PASSWORD_NAS;
			String domain=DSAPI.FILESYS_DOMAIN_NAS;
			try (Connection connection = client.connect(server)) {
		        AuthenticationContext ac = new AuthenticationContext(username, pwd.toCharArray(), domain);
		        Session session = connection.authenticate(ac);
		        
		        LogSystem.info(request, "Samba get connection : " + session.getConnection(), kelas, refTrx, trxType+"/HealthCheck");
			}catch(Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("Samba");
			}finally {
				client.close();
			}
			
			//Check Koneksi Billing
			LogSystem.info(request, "Check Billing connection", kelas, refTrx, trxType+"/HealthCheck");
			URL urlBilling = null;
			HttpsURLConnection urlConnBilling = null;
			try {
				urlBilling = new URL(DSAPI.KILLBILL+"Balance.html");
				urlConnBilling = (HttpsURLConnection) urlBilling.openConnection();
				urlConnBilling.connect();
		        LogSystem.info(request, "Response code : " + urlConnBilling.getResponseCode(), kelas, refTrx, trxType+"/HealthCheck");
		        if(HttpsURLConnection.HTTP_OK != urlConnBilling.getResponseCode())
		        {
		        	LogSystem.info(request, "Connection to billing not 200", kelas, refTrx, trxType+"/HealthCheck");
		        	alist.add("Billing");
		        }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("Billing");
			}finally {
				urlConnBilling.disconnect();
			}
			
			//Check Koneksi FaceRecognition
			LogSystem.info(request, "Check FR connection", kelas, refTrx, trxType+"/HealthCheck");
			try {
				FaceRecognition fRec=new FaceRecognition(refTrx, trxType+"/HealthCheck", request);
				JSONObject respFace=fRec.checkFace(Base64.encode("".getBytes()),Base64.encode("".getBytes()), Long.valueOf("000000000"), "00000000000");
				LogSystem.info(request, "Resp FR : "+respFace.toString(), kelas, refTrx, trxType+"/HealthCheck");
				
				if(respFace.has("connection"))
				{
					if(!respFace.getBoolean("connection"))
					{
						LogSystem.info(request, "FR Response : " + respFace.getBoolean("connection"), kelas, refTrx, trxType+"/HealthCheck");
						alist.add("FR"); 
					}
				}
			}catch(Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("FR");
			}
			
			//Check Koneksi Verifikasi
			LogSystem.info(request, "Check verifikasi connection", kelas, refTrx, trxType+"/HealthCheck");
			URL urlVerifikasi = null;
			HttpsURLConnection urlConnVerifikasi = null;
			try {
				JSONObject jose=new JSONObject();
				
				try {
					jose.put("nik", "");
					jose.put("fotowajah", "");
					jose.put("mitra", "");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				String jos=jose.toString();
//				urlVerifikasi = new URL(DSAPI.DUKCAPIL+"faceCheck.html");
				urlVerifikasi = new URL(DSAPI.DUKCAPIL+"facecheck");
				urlConnVerifikasi = (HttpsURLConnection) urlVerifikasi.openConnection();
				
				urlConnVerifikasi.setRequestMethod("POST");
				urlConnVerifikasi.setDoInput(true);
				urlConnVerifikasi.setDoOutput(true);
				urlConnVerifikasi.setRequestProperty("Connection", "Keep-Alive");
				urlConnVerifikasi.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
				urlConnVerifikasi.setRequestProperty("Accept","application/json");
				urlConnVerifikasi.connect();
				
				urlConnVerifikasi.getOutputStream().write(jos.getBytes());
		        LogSystem.info(request, "Response code : " + urlConnVerifikasi.getResponseCode(), kelas, refTrx, trxType+"/HealthCheck");
		        
		        InputStream is = null;
        		if (urlConnVerifikasi.getResponseCode() == 200) 
        		{
        			is = urlConnVerifikasi.getInputStream();
        		}
        		else
        		{
        			is = urlConnVerifikasi.getErrorStream();
        		}
    	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	        StringBuilder str = new StringBuilder();
    	        String line = null;
    	        while((line = reader.readLine()) != null){
    	            str.append(line + "\n");
    	        }
    	        is.close();
    	        String result = str.toString();
    	        
    	        JSONObject response=new JSONObject(result);
    	        LogSystem.info(request, "Response dukcapil " + response, kelas, refTrx, trxType+"/HealthCheck");
    	        int responseCode=200;
    	        if (response.has("connection"))
    	        {
    	        	if(!response.getBoolean("connection"))
    	        	{
    	        		responseCode=400;
    	        	}
    	        }
    	        
		        if(HttpsURLConnection.HTTP_OK != responseCode)
		        {
		        	LogSystem.info(request, "Connection to verifikasi not 200", kelas, refTrx, trxType+"/HealthCheck");
		        	alist.add("Verifikasi");
		        }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("Verifikasi");
			}finally {
				urlConnVerifikasi.disconnect();
			}
			
			//Check Koneksi dukcapil
			LogSystem.info(request, "Check dukcapil connection", kelas, refTrx, trxType+"/HealthCheck");
			URL urlDukcapil = null;
			HttpsURLConnection urlConnDukcapil = null;
			try {
				JSONObject jose=new JSONObject();
				
				try {
					jose.put("nik", "");
					jose.put("nama", "");
					jose.put("threshold", "90");
					jose.put("tgl_lhr", "");
					jose.put("mitra", "Digisign");
			        jose.put("version", "2.0");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				String jos=jose.toString();
//				urlDukcapil = new URL(DSAPI.DUKCAPIL+"dukcapil_nik_verf.html");
				urlDukcapil = new URL(DSAPI.DUKCAPIL+"dukcapil_nik_verf");
				urlConnDukcapil = (HttpsURLConnection) urlDukcapil.openConnection();
				
				urlConnDukcapil.setRequestMethod("POST");
				urlConnDukcapil.setDoInput(true);
				urlConnDukcapil.setDoOutput(true);
				urlConnDukcapil.setRequestProperty("Connection", "Keep-Alive");
				urlConnDukcapil.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
				urlConnDukcapil.setRequestProperty("Accept","application/json");
				urlConnDukcapil.connect();
				urlConnDukcapil.getOutputStream().write(jos.getBytes());
		        LogSystem.info(request, "Response code : " + urlConnDukcapil.getResponseCode(), kelas, refTrx, trxType+"/HealthCheck");

		        InputStream is = null;
        		if (urlConnDukcapil.getResponseCode() == 200) 
        		{
        			is = urlConnDukcapil.getInputStream();
        		}
        		else
        		{
        			is = urlConnDukcapil.getErrorStream();
        		}
    	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	        StringBuilder str = new StringBuilder();
    	        String line = null;
    	        while((line = reader.readLine()) != null){
    	            str.append(line + "\n");
    	        }
    	        is.close();
    	        String result = str.toString();
    	        
    	        JSONObject response=new JSONObject(result);
    	        LogSystem.info(request, "Response dukcapil " + response, kelas, refTrx, trxType+"/HealthCheck");
    	        int responseCode=200;
    	        if (response.has("connection"))
    	        {
    	        	if(!response.getBoolean("connection"))
    	        	{
    	        		responseCode=400;
    	        	}
    	        }
    	        
		        if(HttpsURLConnection.HTTP_OK != responseCode)
		        {
		        	LogSystem.info(request, "Connection to Dukcapil not 200", kelas, refTrx, trxType+"/HealthCheck");
		        	alist.add("Dukcapil");
		        }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("Dukcapil");
			}finally {
				urlConnDukcapil.disconnect();
			}

			//Check Koneksi KMS
			LogSystem.info(request, "Check KMS connection", kelas, refTrx, trxType+"/HealthCheck");
			URL urlKMS = null;
			HttpsURLConnection urlConnKMS = null;
			try {
				String url = DSAPI.KMS_HOST_BANYAK + "/certChecking.html";
				JSONObject jokms = new JSONObject();
				JSONObject jf = new JSONObject();
				
				jokms.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
				jokms.put("encrypt-mode", "CBC");
				
				KmsService kms = new KmsService(request, refTrx);
				
				kms.kmsService(url);
				kms.addFormField("jsonfield", jokms.toString());
				String rst = null;
				rst = kms.finish().getString("result");

		        LogSystem.info(request, "Response code : " + rst, kelas, refTrx, trxType+"/HealthCheck");
		        if(rst == null)
		        {
		        	LogSystem.info(request, "Connection to KMS not 200", kelas, refTrx, trxType+"/HealthCheck");
		        	alist.add("KMS");
		        }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("KMS");
			}

			
			//Check Koneksi SMS
			LogSystem.info(request, "Check SMS connection", kelas, refTrx, trxType+"/HealthCheck");
			URL urlSMS = null;
			HttpsURLConnection urlConnSMS = null;
			try {
				urlSMS = new URL(DSAPI.SMS_API+"/service/SMSService.html");
				urlConnSMS = (HttpsURLConnection) urlSMS.openConnection();
				urlConnSMS.connect();
		        LogSystem.info(request, "Response code : " + urlConnSMS.getResponseCode(), kelas, refTrx, trxType+"/HealthCheck");
		        if(HttpsURLConnection.HTTP_OK != urlConnSMS.getResponseCode())
		        {
		        	LogSystem.info(request, "Connection to SMS not 200", kelas, refTrx, trxType+"/HealthCheck");
		        	alist.add("SMS");
		        }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("SMS");
			}finally {
				urlConnSMS.disconnect();
			}

			//Check connection to email
			LogSystem.info(request, "Check API email connection start", kelas, refTrx, trxType+"/HealthCheck");
			
			try {
				Integer cekmail = new SendMailApi().pingMail(request, kelas, refTrx, trxType+"/HealthCheck");
				LogSystem.info(request, "Response cek email : " + cekmail, kelas, refTrx, trxType+"/HealthCheck");
	            if(cekmail != 200) 
	            {
	            	alist.add("Email");
	            }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, kelas, refTrx, trxType+"/HealthCheck");
				alist.add("Email");
			}
			LogSystem.info(request, "Check API email connection finish", kelas, refTrx, trxType+"/HealthCheck");
			
			if(alist.size() > 0)
			{
				jo.put("result", "05");
				jo.put("error", alist);
				context.getResponse().setStatus(503);
				return jo;
			}
			else
			{
				jo.put("result", "00");
				return jo;
			}
  	  	}catch(RuntimeException e)
  	  	{
  	  		alist.add("");
	  	  	jo.put("result", "05");
			jo.put("error", alist);
  	  		return jo;
  	  	} finally {
  	  		LogSystem.info(request, "Close session connection to db", kelas, refTrx, trxType+"/HealthCheck");
			db.session().close();
			LogSystem.info(request, "Close connection db", kelas, refTrx, trxType+"/HealthCheck");
			db.close();
			
  	  	}
	}
}