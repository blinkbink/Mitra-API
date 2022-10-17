package id.co.keriss.consolidate.action.km;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.util.LogSystem;

public class CheckOTP extends ActionSupport {
	User userRecv;
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="OTP"+sdfDate2.format(tgl).toString();
	String kelas="id.co.keriss.consolidate.action.km.CheckOTP";
	String trxType="C-OTP";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		
		String nohp = "";
		String otp = "";
		
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
	        	LogSystem.error(getClass(), e,kelas, refTrx, trxType);
				//System.out.println(e);
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("nohp")) 
						{
							nohp = fileItem.getString();
							LogSystem.info(request, nohp, kelas, refTrx, trxType);

						}
						if (fileItem.getFieldName().equals("otpcode")) 
						{
							otp = fileItem.getString();
							LogSystem.info(request, nohp, kelas, refTrx, trxType);
						}
					} 
			}
	         
	         try {
	        	 ConfirmCode OTP = new ConfirmCode();
	        	 ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
	        	 ConfirmCode dataOTP = OTPDAO.findByNewCode(otp, nohp);
	        	 
	        	 //System.out.println(dataOTP.getCode());
				 LogSystem.info(request, dataOTP.getCode(), kelas, refTrx, trxType);
	        	 
	        	 if (dataOTP != null)
	        	 {
	        		 context.put("trxjson", 200);
	        		 try {
	        			 dataOTP.setStatus("yes");
		        		 OTPDAO.update(dataOTP);
	        		 }
	        		 catch(Exception e)
	        		 {
	        			 e.printStackTrace();
	        		 }
	        	 }
	        	 else
	        	 {
	        		 context.put("trxjson", 01);
	        		 context.getResponse().sendError(01);
	        	 }
	        	 
	         } catch (Exception e) {
	        	//System.out.println("Error Check OTP");
	        	LogSystem.info(request, "Error check OTP", kelas, refTrx, trxType);
	        	e.printStackTrace();
	     		context.getResponse().sendError(02);
	     		return;
	         }
	                 	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e, kelas, refTrx, trxType);
            e.printStackTrace();
            try {
				context.getResponse().sendError(03);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            return;
		}
		
		
	}
}
