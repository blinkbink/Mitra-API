package id.co.keriss.consolidate.action.km;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;

public class GenerateOtp extends ActionSupport {
	User userRecv;
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="GOTP"+sdfDate2.format(tgl).toString();
	String kelas="id.co.keriss.consolidate.action.km.GenerateOtp";
	String trxType="GEN-OTP";
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		
		String nohp = "";
		String idmitra = "";
		try{
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);

	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
	         //System.out.println("Check");
	         LogSystem.info(request, "Check", kelas, refTrx, trxType);
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
				//System.out.println(e);
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("nohp")) 
						{
							nohp = fileItem.getString();
							//System.out.println(nohp);
							LogSystem.info(request, nohp, kelas, refTrx, trxType);
						}
						if (fileItem.getFieldName().equals("idmitra")) 
						{
							idmitra = fileItem.getString();
							LogSystem.info(request, nohp, kelas, refTrx, trxType);
						}
					}
			}
	         
	        // try {
	        	 ConfirmCode ccode=new ConfirmCode();
	        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
	        	 ConfirmCode Exist = OTPDAO.findByPhone(nohp);
	        	 
	        	 String encryptedIdMitra = AESEncryption.encryptDoc(idmitra);
	        	 
	        	 if(Exist != null)
	        	 {
	        		 LogSystem.info(request, "Encrypted IdMitra: " + AESEncryption.encryptDoc(idmitra), kelas, refTrx, trxType);
	        		 LogSystem.info(request, "Decrypted IdMitra: " + encryptedIdMitra, kelas, refTrx, trxType);
	        		 LogSystem.info(request, "Code masih belum terpakai, kirim yang lama", kelas, refTrx, trxType);
	        		 //System.out.println("Code masih belum terpakai, kirim yang lama");
	        		 ConfirmSms sms = new ConfirmSms();
			         sms.sendingPostRequest(Exist.getCode(), nohp, Long.parseLong(encryptedIdMitra),request, refTrx, trxType);
	        	 }
	        	 else
	        	 {
	        		 //System.out.println("Pakai code baru");
	        		 LogSystem.info(request, "Pakai code baru", kelas, refTrx, trxType);
			         ccode.setStatus("no");
			         ccode.setWaktu_buat(new Date());
			         ccode.setMsisdn(nohp);
			         Random rand=new Random();
			         int number = rand.nextInt(999999);
			         String code=String.format("%06d", number);
			         ccode.setCode(code);
			         
			         try {
			        	 ccd.create(ccode);
			         }catch(Exception e)
			         {
			        	 LogSystem.info(request, "Error generate OTP", kelas, refTrx, trxType);
			        	 //System.out.println("Error generate OTP");
			        	 e.printStackTrace();
			        	 context.getResponse().sendError(01);
			        	 return;
			         }
			         
			         if(ccode.getId()>0) {
			        	 ConfirmSms sms = new ConfirmSms();
				         sms.sendingPostRequest(code, nohp, Long.parseLong(encryptedIdMitra), request, refTrx, trxType);
			         }
	        	 }
	        	
//	         } catch (Exception e) {
//	        	System.out.println("Error Send OTP");
//	        	e.printStackTrace();
//	     		context.getResponse().sendError(02);
//	     		return;
//	         }
	                 	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e, kelas, refTrx, trxType);
            e.printStackTrace();
            return;
		}
		
		context.put("trxjson", 200);
	}
}
