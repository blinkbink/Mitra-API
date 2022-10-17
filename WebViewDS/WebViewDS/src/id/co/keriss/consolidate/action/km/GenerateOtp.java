package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class GenerateOtp extends ActionSupport {
	User userRecv;
	String kelas="action.km.GenerateOtp";
	String trxType="GEN-OTP";
	String refTrx="";
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		JSONObject jo = new JSONObject();
		
		String nohp = "";
		String type = "";
		
		try{
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);

	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
	         //System.out.println("Check");
	         
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("nohp")) 
						{
							nohp = fileItem.getString();
							
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
	         
	         LogSystem.info(request, "Check", kelas, refTrx, trxType);
	         LogSystem.info(request, nohp, kelas, refTrx, trxType);
	         LogSystem.info(request, type, kelas, refTrx, trxType);
	         LogSystem.info(request, refTrx, kelas, refTrx, trxType);
	         
	         LogSystem.info(request, "Encrypted Preregistration: " + AESEncryption.decryptDoc(nohp), kelas, refTrx, trxType);
	         String encryptedPreregistration = AESEncryption.decryptDoc(nohp);
	         
    		 LogSystem.info(request, "Decrypted Preregistration: " + encryptedPreregistration, kelas, refTrx, trxType);
    		 
    		 PreRegistrationDao prDao = new PreRegistrationDao(db);
    		 PreRegistration data = prDao.findById(Long. parseLong(encryptedPreregistration));
    		 
    		 if(data == null)
	         {
	        	 jo.put("rc", "05");
		         jo.put("notif", "Aktivasi tidak dapat dilanjutkan, hubungi Digisign");
		         context.put("trxjson", jo.toString());
		         return;
	         }
    		 
        	 ConfirmCode ccode=new ConfirmCode();
        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
        	 String dataMedia = "";
        	 
        	 if(type.equals("email"))
        	 {
        		 dataMedia = data.getEmail();
        	 }
        	 else
        	 {
        		 dataMedia = data.getNo_handphone();
        	 }
        	 
        	 ConfirmCode Exist = OTPDAO.findByPhone(dataMedia);
        	 
        	 if(Exist != null)
        	 {
        		
        		 LogSystem.info(request, "Code masih belum terpakai, kirim yang lama", kelas, refTrx, trxType);
        		 LogSystem.info(request, "Code :"+Exist.getCode(), kelas, refTrx, trxType);
	        	 LogSystem.info(request, "Tujuan :"+dataMedia, kelas, refTrx, trxType);

	        	 
        		 ConfirmSms sms = new ConfirmSms();
        		 JSONObject respsms = null;
        		 
        		 if(type.equals("email"))
        		 {
        			 respsms = sms.sendMailOTPMail(Exist.getCode(), data.getEmail(), data.getMitra().getId().toString(), data.getNama());
        		 }
        		 else
        		 {
        			 respsms = sms.sendingPostRequest(Exist.getCode(), data.getNo_handphone(), data.getMitra().getId(), refTrx, request);
        		 }
		         
		         if(respsms.getString("result").equals("OK") || respsms.getString("result").equals("sukses"))
		         {
		        	 jo.put("rc", "00");
			         jo.put("notif", "Berhasil kirim OTP");
			         context.put("trxjson", jo.toString());
			         return;
		         }
		         else
		         {
	        		 LogSystem.error(request, "Error " + respsms.getString("info"), kelas, refTrx, trxType);
		        	 jo.put("rc", "05");
			         jo.put("notif", "Gagal kirim OTP");
			         context.put("trxjson", jo.toString());
			         return;  
		         }
        	 }
        	 else
        	 {
        		 LogSystem.info(request, "Pakai code baru", kelas, refTrx, trxType);
		         ccode.setStatus("no");
		         ccode.setWaktu_buat(new Date());
		         ccode.setMsisdn(dataMedia);
		         Random rand=new Random();
		         int number = rand.nextInt(999999);
		         String code=String.format("%06d", number);
		         ccode.setCode(code);
		         
		         try {
		        	 ccd.create(ccode);
		         }catch(Exception e)
		         {

		        	 LogSystem.error(getClass(), e, kelas, refTrx, trxType);
	        		 jo.put("rc", "05");
			         jo.put("notif", "Gagal Kirim OTP");
			         context.put("trxjson", jo.toString());
			         return;
		         }
		         
		         if(ccode.getId()>0) {
		        	 ConfirmSms sms = new ConfirmSms();
		        	 LogSystem.info(request, "Code :"+code, kelas, refTrx, trxType);
		        	 LogSystem.info(request, "Tujuan :"+dataMedia, kelas, refTrx, trxType);
		        	 LogSystem.info(request, encryptedPreregistration, kelas, refTrx, trxType);
		        	 
		        	 JSONObject respsms = null;
		        	 
		        	 if(type.equals("email"))
	        		 {
	        			 respsms = sms.sendMailOTPMail(code, data.getEmail(), data.getMitra().getId().toString(), data.getNama());
	        		 }
	        		 else
	        		 {
	        			 respsms = sms.sendingPostRequest(code, data.getNo_handphone(),  data.getMitra().getId(), refTrx, request);
	        		 }
			         
		        	 if(respsms.getString("result").equals("OK") || respsms.getString("result").equals("sukses"))
			         {
			        	 jo.put("rc", "00");
				         jo.put("notif", "Berhasil kirim OTP");
				         context.put("trxjson", jo.toString());
				         return;
			         }
			         else
			         {
		        		 LogSystem.error(request, "Error " + respsms.getString("info"));
			        	 jo.put("rc", "05");
				         jo.put("notif", "Gagal kirim OTP");
				         context.put("trxjson", jo.toString());
				         return;  
			         }
		         }
        	 }
	                 	         
		}catch (Exception e) {
			 LogSystem.error(getClass(), e);
    		 try {
				jo.put("rc", "05");
				jo.put("notif", "Gagal Kirim OTP");
				context.put("trxjson", jo.toString());
			   	return;
			} catch (JSONException e1) {
				LogSystem.error(getClass(), e1);
			}
	         
		}
		
	}
}
