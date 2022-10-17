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
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.util.LogSystem;

public class CheckUsername extends ActionSupport {
	User userRecv;
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="UNM"+sdfDate2.format(tgl).toString();
	String kelas="id.co.keriss.consolidate.action.km.CheckUsername";
	String trxType="C-UNM";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		
		String username = "";
		
		try{
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);

	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
	         LogSystem.info(request, "Check", kelas, refTrx, trxType);
	         //System.out.println("Check");
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
	        	LogSystem.error(getClass(), e, kelas, refTrx, trxType);
				//System.out.println(e);
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("username")) 
						{
							username = fileItem.getString();
							//System.out.println(username);
							LogSystem.info(request, username, kelas, refTrx, trxType);
						}
				
					} 
			}
	         
	         try {
	        	 Login login = new Login();
	        	 LoginDao LDAO = new LoginDao(db);
	        	 
	        	 Login Username = LDAO.getByUsername(username);
	        	 
	        	 if(Username != null)
	        	 {
	        		 context.put("trxjson", 200);
	        	 }
	        	 else
	        	 {
	        		 context.put("trxjson", 100);
	        	 }
	        	 
	         } catch (Exception e) {
	        	//System.out.println("Error Check Username");
	        	LogSystem.info(request, "Error check username", kelas, refTrx, trxType);
	        	e.printStackTrace();
	     		context.getResponse().sendError(01);
	     		return;
	         }
	                 	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e, kelas, refTrx, trxType);
            e.printStackTrace();
            try {
				context.getResponse().sendError(02);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            return;
		}
		
		
	}
}
