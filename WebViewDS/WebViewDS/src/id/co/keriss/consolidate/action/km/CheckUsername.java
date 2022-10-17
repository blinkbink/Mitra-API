package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.util.LogSystem;
import java.io.IOException;
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
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		String kelas = "id.co.keriss.consolidate.action.km.CheckUsername";
		String trxType = "CHKUSERNAME";
		String refTrx = "";
		String username = "";
		
		try{
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);

	         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	         List<FileItem> fileItems=null;
	         try {
	        	 fileItems = upload.parseRequest(request);
	         }catch(Exception e)
	         {
				LogSystem.info(request, e.toString(), kelas, refTrx, trxType);
	         }
	         
	         for (FileItem fileItem : fileItems) 
	         {
					if (fileItem.isFormField()) 
					{
						if (fileItem.getFieldName().equals("username")) 
						{
							username = fileItem.getString();
							
						}
						if (fileItem.getFieldName().equals("refTrx")) 
						{
							refTrx = fileItem.getString();
							
						}
				
					} 
			}
	         
	         LogSystem.info(request, username, kelas, refTrx, trxType);
	         
	         try {
	        	 Login login = new Login();
	        	 LoginDao LDAO = new LoginDao(db);
	        
	        	 if(LDAO.getByUsername(username)==null) {
	        		 context.put("trxjson", 100);
	        	 } else {
	        		 context.put("trxjson", 200);
	        	 }
	        	 
	         } catch (Exception e) {
	        	 LogSystem.info(request, "Error Check Username", kelas, refTrx, trxType);
	        	e.printStackTrace();
	     		context.getResponse().sendError(01);
	     		return;
	         }
	                 	         
		}catch (Exception e) {
            LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
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
