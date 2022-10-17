package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.util.LogSystem;
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

public class CheckUsername extends ActionSupport {
	User userRecv;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="CUSERNAME"+sdfDate2.format(tgl).toString();
		String username = "";
		String username_lower = "";
		String CATEGORY = "ACTIVATION";
		String mitra_req="";
		String email_req="";
		String path_app=this.getClass().getName();
		
		try{
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);

	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
//	         LogSystem.info(request, "Check", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
	        	 LogSystem.info(request, e.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         }
	         
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("username")) 
						{
							username = fileItem.getString();
							username_lower = fileItem.getString().toLowerCase();
							
						}
						if (fileItem.getFieldName().equals("refTrx")) 
						{
							refTrx = fileItem.getString();
						}
				
					} 
			}
	         
	         LogSystem.info(request, "receive check username ", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         
	         LogSystem.info(request, username, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	         LogSystem.request(request, fileItems, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         
	         try {
	        	 Login login = new Login();
	        	 LoginDao LDAO = new LoginDao(db);
	        	 
	        	 if(LDAO.getByUsername(username)==null && LDAO.getByUsername(username_lower)==null) {
	        		 LogSystem.info(request, "Dapat digunakan ", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 context.put("trxjson", 100);
	        	 } else {
	        		 context.put("trxjson", 200);
	        		 LogSystem.info(request, "Tidak dapat digunakan ", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	 }
	        	 
	         } catch (Exception e) {
	        	 LogSystem.info(request, "Error Check Username", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	e.printStackTrace();
	     		context.getResponse().sendError(01);
	     		return;
	         }
	                 	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
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
