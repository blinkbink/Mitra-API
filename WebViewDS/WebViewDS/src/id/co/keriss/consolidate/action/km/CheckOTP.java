package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class CheckOTP extends ActionSupport {
	User userRecv;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		
		String nohp = "";
		String otp = "";
		String kelas="id.co.keriss.consolidate.action.km.CheckOTP";
		String refTrx = "";
		String typeTrx = "CHK-OTP";
		String type = "";
		
		try{


	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
				LogSystem.info(request, e.toString(), kelas, refTrx, typeTrx);
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("nohp")) 
						{
							nohp = fileItem.getString();
						}
						if (fileItem.getFieldName().equals("otpcode")) 
						{
							otp = fileItem.getString();
						}
						if (fileItem.getFieldName().equals("type")) 
						{
							type = fileItem.getString();
						}
						if (fileItem.getFieldName().equals("refTrx")) 
						{
							refTrx = fileItem.getString();
						}
					}
			}
	         
	         
	         if(type.equals("email"))
	         {
	        	 try {
		        	 
		        	 String encryptedPreregistration = AESEncryption.decryptDoc(nohp);
		    		 PreRegistrationDao prDao = new PreRegistrationDao(db);
		    		 PreRegistration data = prDao.findById(Long. parseLong(encryptedPreregistration));
	
		        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
		        	 ConfirmCode dataOTP = OTPDAO.findByNewCode(otp, data.getEmail());
		        	 
		        	 if (dataOTP != null)
		        	 {
		        		 LogSystem.info(request, dataOTP.getCode(), kelas, refTrx, typeTrx );
		        		 try {
		        			 JSONObject jo = new JSONObject();
		        			 jo.put("rc", "00");
		        			 jo.put("notif", "Success");
			        		 OTPDAO.update(dataOTP);
			        		 context.put("trxjson", jo.toString());
			        		 LogSystem.info(request, "Kode verifikasi email valid", kelas, refTrx, typeTrx);
			        		 return;
		        		 }
		        		 catch(Exception e)
		        		 {
		        			 e.printStackTrace();
		        		 }
		        	 }
		        	 else
		        	 {
		        		 JSONObject jo = new JSONObject();
	        			 jo.put("rc", "05");
	        			 jo.put("notif", "OTP Salah");
	        			 context.put("trxjson", jo.toString());
	        			 LogSystem.info(request, "Kode verifikasi email salah", kelas, refTrx, typeTrx);
	        			 return;
		        	 }
		        	 
		         } catch (Exception e) {
		        	 JSONObject jo = new JSONObject();
	    			 jo.put("rc", "05");
	    			 jo.put("notif", "OTP Salah");
	    			 LogSystem.info(request, "OTP Salah", kelas, refTrx, typeTrx);
	    			 context.put("trxjson", jo.toString());
	    			 return;
		         }
	         }
	         else
	         {
	        	 try {
		        	 
		        	 String encryptedPreregistration = AESEncryption.decryptDoc(nohp);
		    		 PreRegistrationDao prDao = new PreRegistrationDao(db);
		    		 PreRegistration data = prDao.findById(Long. parseLong(encryptedPreregistration));
		    		 
	
		        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
		        	 ConfirmCode dataOTP = OTPDAO.findByNewCode(otp, data.getNo_handphone());
		        	 
		        	 if (dataOTP != null)
		        	 {
		        		 LogSystem.info(request, dataOTP.getCode(), kelas, refTrx, typeTrx );
		        		 try {
		        			 JSONObject jo = new JSONObject();
		        			 jo.put("rc", "00");
		        			 jo.put("notif", "Success");
			        		 OTPDAO.update(dataOTP);
			        		 context.put("trxjson", jo.toString());
			        		 LogSystem.info(request, "OTP Valid", kelas, refTrx, typeTrx);
			        		 return;
		        		 }
		        		 catch(Exception e)
		        		 {
		        			 e.printStackTrace();
		        		 }
		        	 }
		        	 else
		        	 {
		        		 JSONObject jo = new JSONObject();
	        			 jo.put("rc", "05");
	        			 jo.put("notif", "OTP Salah");
	        			 context.put("trxjson", jo.toString());
	        			 LogSystem.info(request, "OTP Salah", kelas, refTrx, typeTrx);
	        			 return;
		        	 }
		        	 
		         } catch (Exception e) {
		        	 JSONObject jo = new JSONObject();
	    			 jo.put("rc", "05");
	    			 jo.put("notif", "OTP Salah");
	    			 LogSystem.info(request, "OTP Salah", kelas, refTrx, typeTrx);
	    			 context.put("trxjson", jo.toString());
	    			 return;
		         }
	         }
	        
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
            e.printStackTrace();
            try {
//				context.getResponse().sendError(03);
				 JSONObject jo = new JSONObject();
				 jo.put("rc", "05");
				 jo.put("notif", "OTP Salah");
				 LogSystem.info(request, "OTP Salah", kelas, refTrx, typeTrx);
				 context.put("trxjson", jo.toString());
				 return;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
            return;
		}	
	}
}
