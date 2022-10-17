package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ActivationSessionDao;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.ee.ActivationSession;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class CheckOTP extends ActionSupport {
	User userRecv;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="COTP"+sdfDate2.format(tgl).toString();
		String nohp = "";
		String otp = "";
		String data = "";
		String emailOTP = "";
		String sessionid = "";
		String path_app = this.getClass().getName();
		String CATEGORY = "OTP";
		String email_req ="";
		String mitra_req ="";
		
		try{
	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
				LogSystem.error(request, e.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("nohp")) 
						{
							nohp = fileItem.getString();
							LogSystem.info(request, nohp, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						}
						if (fileItem.getFieldName().equals("sessionid")) 
						{
							sessionid = fileItem.getString();
						}
						if (fileItem.getFieldName().equals("otpcode")) 
						{
							otp = fileItem.getString();
							LogSystem.info(request, otp, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						}
						
						if (fileItem.getFieldName().equals("data")) 
						{
							data = fileItem.getString();
							LogSystem.info(request, data, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							try {
								data = AESEncryption.decryptIdpreregis(data);
							}catch(Exception e)
							{
								 e.printStackTrace();
			        			 LogSystem.info(request, "Error decrypt data", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				        		 context.put("trxjson", 404); 
				        		 return;
							}
						}
						
						if (fileItem.getFieldName().equals("verEmail")) 
						{
							emailOTP = fileItem.getString();
						}
						
						if (fileItem.getFieldName().equals("refTrx")) 
						{
							refTrx = fileItem.getString();
						}
					} 
			}
	         
	         
	         ActivationSessionDao activationSessionDB = new ActivationSessionDao(db);
	         ActivationSession activationSession = new ActivationSession();
	         activationSession = activationSessionDB.findSessionId(sessionid);
	         
	         Date datey = new Date();
			 Date today = datey;
			 Date expire = activationSession.getExpire_time();
			 boolean used = activationSession.isUsed();
			 if(today.after(expire) ) {
				 LogSystem.error(request, "Session has already expired", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	 context.put("trxjson", 408);
	        	 return;
			 }
			 if (used == true) {
	        	 LogSystem.error(request, "Session has already expired", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	 context.put("trxjson", 401);
	        	 return;
			 }
			 
	         if(data != "")
	         {
	        	 try 
		         {
	        		 LogSystem.info(request, "OTP : " + emailOTP, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 if(emailOTP.equals(""))
	        		 {
	        			 LogSystem.info(request, "OTP null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 context.put("trxjson", 404); 
		        		 return;
	        		 }
	        		 
	        		 PreRegistrationDao dd=new PreRegistrationDao(db);
	        		 PreRegistration pr=dd.findById(Long.valueOf(data));
	        		 
	        		 if(pr == null)
	        		 {
	        			 LogSystem.info(request, "Data preregistration null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 context.put("trxjson", 404); 
		        		 return;
	        		 }
	        		 
		        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
		        	 ConfirmCode dataOTP = OTPDAO.findByNewCode(emailOTP, pr.getEmail());
		        	 
		        	 if (dataOTP != null)
		        	 {
		        		 LogSystem.info(request, dataOTP.getCode(), refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 context.put("trxjson", 200);
		        		 try {
		        			 LogSystem.info(request, "Kode verifikasi email valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        			 dataOTP.setStatus("yes");
			        		 OTPDAO.update(dataOTP);
		        		 }
		        		 catch(Exception e)
		        		 {	
		        			 LogSystem.info(request, "Gagal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        			 e.printStackTrace();
		        		 }
		        	 }
		        	 else
		        	 {
		        		 LogSystem.info(request, "Kode verifikasi email salah", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 context.put("trxjson", 404);
		        	 }
		         }catch(Exception e)
		         {
		        	LogSystem.info(request, "Gagal cek kode verifikasi email", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        	e.printStackTrace();
		        	context.put("trxjson", 402);
		     		return;
		         }
	         }
	         else
	         {
		         try {
		        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
		        	 ConfirmCode dataOTP = OTPDAO.findByNewCode(otp, nohp);
		        	 
		        	 if (dataOTP != null)
		        	 {
		        		 LogSystem.info(request, dataOTP.getCode(), refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 context.put("trxjson", 200);
		        		 try {
		        			 LogSystem.info(request,"Sukses", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        			 dataOTP.setStatus("yes");
			        		 OTPDAO.update(dataOTP);
		        		 }
		        		 catch(Exception e)
		        		 {
		        			 LogSystem.info(request,"Gagal " + e.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        			 e.printStackTrace();
		        		 }
		        	 }
		        	 else
		        	 {
		        		 LogSystem.info(request,"Not Found", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 context.put("trxjson", 404);
		        	 }
		         } catch (Exception e) {
		        	LogSystem.info(request,"Error Check OTP", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        	e.printStackTrace();
		        	context.put("trxjson", 402);
		     		return;
		         }
	         }
	                 	         
		}catch (Exception e) {
            LogSystem.error(request, e.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
            e.printStackTrace();
            try {
	        	context.put("trxjson", 404);
				context.getResponse().sendError(03);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            return;
		}		
	}
}
