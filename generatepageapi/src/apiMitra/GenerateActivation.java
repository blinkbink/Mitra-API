package apiMitra;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.DSA;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import api.email.SendSuksesRegistrasi;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Activation;
import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.UserdataManual;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class GenerateActivation extends ActionSupport implements DSAPI{
@SuppressWarnings("unchecked")
	
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	Date idTrx=new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//super.logout (context, cfg); // just in case - force logout
	DB db = getDB(context);
	String stringDate = sdfDate2.format(idTrx).toString();
	String refTrx="GEN-ACT-PAGE"+ stringDate;
	HttpServletRequest  request  = context.getRequest();
	Logger log = LogManager.getLogger("digisignlogger");
	JSONObject result = new JSONObject();
	String userid = null;
	String pwd = null;
	String email = null;
	boolean exist = true;
	User dataUser = null;
	Mitra mitra=null;
	String jsonString=null;
	String jsondata = request.getParameter("jsonfield");

	try {

		TokenMitraDao tmd = null;
		TokenMitra tm = null;
		String token = null;
		try {
			tmd=new TokenMitraDao(getDB(context));
			token=request.getHeader("authorization");
			if(token!=null) {
				String[] split=token.split(" ");
				if(split.length==2) {
					if(split[0].equals("Bearer"))token=split[1];
				}
			}
			tm=tmd.findByToken(token.toLowerCase());
		}catch(Exception e)
		{
			JSONObject jo=new JSONObject();
			jo.put("result", "91");
			jo.put("notif", "System timeout. silahkan coba kembali.");
			context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			LogSystem.error(request, e.toString(), refTrx);
			return;
		}
			
		if(tm!=null) {
			LogSystem.info(request, "Token ada", refTrx);
			mitra=tm.getMitra();
		} else {
			LogSystem.error(request, "Token tidak ada", refTrx);
			JSONObject jo=new JSONObject();
			jo.put("result", "55");
			jo.put("notif", "token salah");
			context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			LogSystem.info(request, "JSON Data : " + jo.toString(), refTrx);
			return;
		}
			
			try {
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				if (!isMultipart) {
//					LogSystem.info(request, "Bukan multipart");
//					Logger.getLogger(getClass()).info(LogSystem.getGENACTLog(request, "Bukan Multipart", idTrx, "LOG"));
					LogSystem.info(request, "Bukan Multipart", refTrx);
					jsonString = jsondata;
				}

				else {
					ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
					List<FileItem> fileItems=null;
					//System.out.println("Check");
					try {
						fileItems = upload.parseRequest(request);
					}catch(Exception e)
					{
//						Logger.getLogger(getClass()).error(LogSystem.getGENACTLog(request, ExceptionUtils.getStackTrace(e), idTrx, "ERROR"));
						LogSystem.error(request, e.toString(), refTrx);
					}

					for (FileItem fileItem : fileItems) {
						if (fileItem.isFormField()) {

							if (fileItem.getFieldName().equals("jsonfield")) {
								jsonString = fileItem.getString();
//								LogSystem.info(request, jsonString);
//								Logger.getLogger(getClass()).info(LogSystem.getGENACTLog(request, jsonString, idTrx, "RCV"));
								LogSystem.info(request, jsonString, refTrx);

								//System.out.println(jsonString);
							}
						} 
					}
				}
				
				UserManager user = new UserManager(db);
				JSONObject adata=new JSONObject(jsonString).getJSONObject("JSONFile");
				
				userid = adata.getString("userid").toLowerCase();
			    email = adata.getString("email_user").toLowerCase();
			    
			    
				dataUser = user.findByUsername(userid);
				
				LogSystem.info(request, "Check User id : " +  dataUser, refTrx);

				if(dataUser != null)
				{
					LogSystem.info(request, "User Authentication : "+ dataUser.getName(), refTrx);
					LogSystem.info(request, "UserID : " + dataUser.getNick().toString(), refTrx);

					ApiVerification aVerf = new ApiVerification(db);
			        boolean vrf = false;
					LogSystem.info(request, "Is Admin : " + dataUser.isAdmin(), refTrx);

			        if(mitra!=null && dataUser.isAdmin()) {
			        	//Check mitra dan token
			        	if(dataUser.getMitra().getId()!=mitra.getId()) {
							LogManager.getLogger(getClass()).info(LogSystem.getGENACTLog(request, "Token dan mitra tidak valid", idTrx, "LOG"));
							LogSystem.info(request, "Token dan mitra tidak valid", refTrx);

			        		result.put("result", "12");
			        		result.put("notif", "Token dan mitra tidak cocok");
							 context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
							LogSystem.info(request, result.toString(), refTrx);
							return;
			        	}
			        	vrf = true;
			        }
			        else {
			        	result.put("result", "05");
			        	result.put("notif", "Userid  bukan admin");
						 context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
						LogSystem.info(request, result.toString(), refTrx);

						return;
			        }
//			        LogSystem.info(request, "Verifikasi userID & Password : " + vrf);
			       
			    	PreRegistrationDao prDao = new PreRegistrationDao(db);
			    	UserdataDao usd = new UserdataDao(db);
			    	UserManager eeuser = new UserManager(db);
			    	User emaileeuser = eeuser.findByEmail(email);
			    	

			    	if(emaileeuser != null)
			    	{
//			    		LogSystem.info(request, "Email sudah melakukan aktivasi");
						LogSystem.info(request, "Email sudah melakukan aktivasi", refTrx);

			    		result.put("result", "14");
			    		result.put("notif", "Email sudah melakukan aktivasi");
//			    		context.put("trxjson", result.toString());
						 context.put("trxjson", new JSONObject().put("JSONFile", result).toString());

						LogSystem.info(request, result.toString(), refTrx);

						return;
			    	}
			    	
			    	PreRegistration data = prDao.findByEmail(email.toLowerCase());
			    	
			    	if (data == null)
			    	{
			    		if(prDao.emailPerusahaan(email.toLowerCase()) != null)
			    		{
			    			result.put("result", "03");
				    		result.put("notif", "Tidak dapat melanjutkan aktivasi email perusahaan");
				    		
				    		LogSystem.info(request, result.toString(), refTrx);
				    		
				    		context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
				    		
				    		return;
			    		}
			    		result.put("result", "06");
			    		result.put("notif", "General Error");

   					 	context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
   					 	
   					 	LogSystem.info(request, "Email tidak ada didalam daftar aktivasi", refTrx);

						LogSystem.info(request, result.toString(), refTrx);

						return;
			    	}
			    	
			    	if(data.getType() != '1')
			    	{
			    		result.put("result", "03");
			    		result.put("notif", "Tidak dapat melanjutkan aktivasi");

   					 	context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
   					 	LogSystem.info(request, "Tidak dapat melanjutkan aktivasi, akun prereg dengan type " + data.getType(), refTrx);

						LogSystem.info(request, result.toString(), refTrx);

						return;
			    	}
			    	
			    	Userdata ktp = usd.findByKtp(data.getNo_identitas());
			    	
			    	if(ktp != null)
			    	{
						LogSystem.info(request, "NIK sudah aktivasi dengan email lain", refTrx);
			    		result.put("result", "14");
			    		result.put("notif", "Email sudah melakukan aktivasi");
			    		context.put("trxjson", new JSONObject().put("JSONFile", result).toString());

						LogSystem.info(request, result.toString(), refTrx);
						return;
			    	}
			    	
			    	
			    	if(data != null)
			    	{
			    		if(data.getMitra().getId() != dataUser.getMitra().getId())
				    	{
//				    		LogSystem.info(request, "Mitra tidak diijinkan melakukan aktivasi pada user ini");
							LogSystem.info(request, "Mitra tidak diijinkan melakukan aktivasi pada user ini", refTrx);
				    		result.put("result", "12");
				    		result.put("notif", "Mitra tidak diijinkan melakukan aktivasi pada email user ini");
							 context.put("trxjson", new JSONObject().put("JSONFile", result).toString());

							LogSystem.info(request, result.toString(), refTrx);
							return;
				    	}
			    		
			    		Activation act=new Activation();
			    		Date current=new Date();
			    		Date expire=new Date(current.getTime()+(1000*7*60));
			    		act.setCreate_time(current);
			    		act.setExpire_time(expire);
			    		String dat=current.getTime()+";"+data.getEmail()+";"+data.getId()+";"+data.getNo_identitas();
			    		String sesKey=id.co.keriss.consolidate.util.SHA256.shaHex(dat);
			    		act.setSession_key(sesKey);
			    		act.setUsed(false);
			    		act.setMitra(data.getMitra());
			    		act.setPreregistration(data);
			    		
			    		
			    		Session ss=db.session();
			    		Transaction t=ss.beginTransaction();
			    		Long idSes=(Long) ss.save(act);
			    		ss.save(act);
			    		t.commit();
			    		
			    		JSONObject link=new JSONObject();
						 link.put("sessionKey", sesKey);
						 link.put("sessionID", idSes.toString());
						 link.put("refTrx", stringDate);
						 
						 JSONObject jo=new JSONObject();
						 jo.put("result", "00");
						 jo.put("link", "https://"+DOMAINAPIWV+"/activationpage.html?act="+URLEncoder.encode(AESEncryption.encryptId(link.toString()),"UTF-8"));
			    		
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						 LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
						 return;
			    	}
			    	else
			    	{
						LogSystem.info(request, "Email info (kosong belum registrasi) atau Kosong", refTrx);
						result.put("result", "05");
			    		result.put("notif", "Email tidak ada atau belum melakukan registrasi");
						LogSystem.info(request, result.toString(), refTrx);
						context.put("trxjson", new JSONObject().put("JSONFile", result).toString());

						return;

			    	}
			    	
				}
				else
				{
					
		        	result.put("result", "05");
		        	result.put("notif", "User tidak ditemukan");
					context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
					LogSystem.info(request, result.toString(), refTrx);
					return;

				}
		}
		catch(Exception e)
		{
			
			LogSystem.error(request, "GET JSON API = " + e.toString(), refTrx);
			e.printStackTrace();
//			context.getResponse().sendError(403);
        	result.put("result", "06");
        	result.put("notif", "General Error");
        	result.put("error", e.getMessage());
			 context.put("trxjson", new JSONObject().put("JSONFile", result).toString());
			LogManager.getLogger(getClass()).info(LogSystem.getGENACTLog(request, result.toString(), idTrx, "SND"));
			LogSystem.info(request, "Response : " + result.toString(), refTrx);
			return;

		}	
			
	}
	catch(Exception e)
	{
		LogSystem.error(request, e.toString(), refTrx);
		e.printStackTrace();
		try {
			result.put("result", "06");
        	result.put("notif", "General Error");
        	result.put("error", e.getMessage());
			 context.put("trxjson", new JSONObject().put("JSONFile", result).toString());

			LogSystem.info(request, result.toString(), refTrx);
			return;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		context.put("trxjson", result.toString());
	}

}
}
