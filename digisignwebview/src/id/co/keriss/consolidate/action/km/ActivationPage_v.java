package id.co.keriss.consolidate.action.km;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.ActivationSession;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class ActivationPage_v extends ActionSupport implements DSAPI {
	@SuppressWarnings("unchecked")
	
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	
		long start = System.currentTimeMillis();
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="ACTPAGEV"+sdfDate2.format(tgl).toString();
	//super.logout (context, cfg); // just in case - force logout
	DB db = null;
	try { 
		db = getDB(context);
		
	} 
	catch(Exception e) {
			e.printStackTrace();
			//LogSystem.error(getClass(), e);
	}
	HttpServletRequest  request  = context.getRequest();
	JSONObject result = new JSONObject();
	String CATEGORY = "ACTIVATION";
	String userid = null;
	String pwd = null;
	String email = null;
	boolean exist = true;
	User dataUser = null;
	Mitra mitra=null;
	String jsonString=null;
	String path_app = this.getClass().getName();
	String jsondata = request.getParameter("jsonfield");
	String mitra_req="";
	String email_req="";
	
	//System.out.println(jsondata);
	try {
		LogSystem.info(request, "Json data : "+jsondata, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	} catch (JSONException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	try {			
			try {
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				if (!isMultipart) {
					jsonString = jsondata;
				}
				else {
					LogSystem.info(request, "Multipart", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
					List<FileItem> fileItems=null;
					LogSystem.info(request, "Check", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					try {
						fileItems = upload.parseRequest(request);
					}catch(Exception e)
					{
						LogSystem.error(getClass(), e);
					}

					for (FileItem fileItem : fileItems) {
						if (fileItem.isFormField()) {

							if (fileItem.getFieldName().equals("jsonfield")) {
								jsonString = fileItem.getString();
								LogSystem.info(request, jsonString, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							}
						} 
					}
				}
				
				String encryptedAct = request.getParameter("act").toString();
				
				AESEncryption AES = new AESEncryption();
				String hasildecrypt = AES.decryptSession(encryptedAct);
	
				JSONObject jsonDecrypt = new JSONObject(hasildecrypt);
				
				if(jsonDecrypt.has("refTrx"))
				{
				 refTrx = refTrx+"/"+jsonDecrypt.getString("refTrx");
				}
				 
				LogSystem.info(request, "Masuk Activation with email Verif WebView", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				
				LogSystem.info(request, "ACT :"+encryptedAct, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				LogSystem.info(request, "DCRYPT :"+hasildecrypt, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				
				
				if (!jsonDecrypt.has("sessionKey")) 
				{ 
					 LogSystem.error(request,"Parameter Session key tidak ditemukan", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s"); 
					 context.getResponse().sendError(403);
					 return; 
				}
				if (!jsonDecrypt.has("sessionID")) 
				{ 
					 LogSystem.error(request,"Parameter Session ID tidak ditemukan", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s"); 
					 context.getResponse().sendError(403);
				 	 return; 
				}
				String session_key=jsonDecrypt.getString("sessionKey");
				String session_id=jsonDecrypt.getString("sessionID");
				LogSystem.info(request, "id :"+session_id, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				LogSystem.info(request, "key :"+session_key, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				
				UserManager user = new UserManager(db);
			    ActivationSession preregisterid = user.findPreregistrationBySession_Keyid(session_key, session_id);
			    email = preregisterid.getPreregistration().getEmail();
			    
			    email_req = email;
			    mitra_req = preregisterid.getMitra().getName();
			    LogSystem.info(request, "EMAIL USER = " +email, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    
			    context.put("email", email);
			    
			    
			    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
				Date dateh = new Date();
				Date today = dateh;
				Date expire = preregisterid.getExpire_time();
				boolean used = preregisterid.isUsed();
				if(today.after(expire) ) {
					 
					 LogSystem.error(request, "Session has already expired", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        	 context.getResponse().sendError(408);
					 return;
				}
				if (used == true) {
					 LogSystem.error(request, "Session has already Used", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        	 context.getResponse().sendError(401);
					
					 return;
				}
			    
			    
				dataUser = user.findByUsername(userid);
				ActivationSession session = null;
				long mitraid ;
				try { 
					 	session = user.findMitraActivationSession(session_key,session_id);
					 	mitraid = session.getMitra().getId();
					 	LogSystem.info(request, "Mitra ID :"+mitraid, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 	mitra = user.findMitrafromActivationSession(mitraid);
				} 
				catch(Exception e) {
						e.printStackTrace();
				}
				
		    	PreRegistrationDao prDao = new PreRegistrationDao(db);
		    	UserdataDao usd = new UserdataDao(db);
		    	UserManager eeuser = new UserManager(db);
		    	User emaileeuser = eeuser.findByEmail(email);
		    	

		    	if(emaileeuser != null)
		    	{
		    		LogSystem.info(request, "Email sudah melakukan aktivasi", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    		context.getResponse().sendError(401);
					return;
		    	}
		    	
		    	PreRegistration data = prDao.findByEmail(email);
		    	
		    	Userdata ktp = usd.findByKtp(data.getNo_identitas());
		    	
		    	if(ktp != null)
		    	{
		    		LogSystem.info(request, "NIK sudah aktivasi dengan email lain", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    		context.getResponse().sendError(403);
		    	}
		    	
		    	if(data != null)
		    	{
		    		String response_email = data.getEmail();
		    		String response_ittd = data.getImageTtd();
		    		String responseNohp = data.getNo_handphone();
		    		String idmitra =    AESEncryption.encryptDoc(Long.toString((data.getMitra().getId())));
		    		
		    		Long preid = data.getId();
		    		context.put("i_ttd", response_ittd);
		    		context.put("preid",AESEncryption.encryptIdpreregis(String.valueOf(preid)));
		    		context.put("nohp", responseNohp);
		    		context.put("idmitra", idmitra);
		    		context.put("sessionid", session_id);
		    		context.put("sessionkey", session_key);
		    		JSONObject dUser=new JSONObject();
		    		dUser.put("etype", AESEncryption.encryptIdpreregis(response_email));
		    		dUser.put("ptype", AESEncryption.encryptIdpreregis(responseNohp));
					dUser.put("type", idmitra);
					dUser.put("rtype", AESEncryption.encryptIdpreregis(String.valueOf(preid)));
					context.put("usertype", dUser);
					context.put("refTrx", refTrx);
		    		LogSystem.info(request, "Email info : " + response_email, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    	}
		    	else
		    	{
		    		LogSystem.info(request, "Email info (kosong belum registrasi) atau Kosong", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    		context.getResponse().sendError(404);
					return;
		    	}

		}
		catch(Exception e)
		{
			LogSystem.info(request, "Detail Error : ", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			e.printStackTrace();
			context.getResponse().sendError(403);
			return;
		}
			
			result.put("rc", "00");
			context.put("result", result.get("rc"));	
			
	}
	catch(Exception e)
	{
		try {
			LogSystem.info(request, "Error info : "+ e, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		e.printStackTrace();
		try {
			context.getResponse().sendError(403);
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
		Random rand = new Random(); 
		int value = rand.nextInt(50000); 
		context.put("rand", value);
		
		context.put("domainwv", "https://"+DOMAINAPIWV);
	}
}
