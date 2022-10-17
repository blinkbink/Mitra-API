package api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

import id.co.keriss.consolidate.util.LogSystem;

public class FileGetter extends HttpServlet {


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("masuk");
        String filePath = null;
    	User userRecv;

		 DB db = new DB();
        try {
            db.open ();
            Session hs = db.session();
          
	        
			int count = 21;
	//		HttpServletRequest  request  = context.getRequest();
	//		HttpServletResponse  response  = context.getResponse();
			String res="";
		    String[] uri=request.getRequestURI().split("/");
	
			String path="/opt/data-DS/UploadFile/";
			path+=uri[3]+"/original/"+uri[4];
		        
			LogSystem.request(request);
	
	        File downloadFile = new File(path);
	        if(!downloadFile.isFile()) {
	        	User us=new UserManager(db).findById(Long.valueOf(uri[3]));
	        	path="/opt/data-DS/UploadFile/";
	 			path+=us.getUserdata().getId()+"/original/"+uri[4];
	 			downloadFile = new File(path);
	        }
	        FileInputStream inStream = new FileInputStream(downloadFile);
	         
	        // if you want to use a relative path to context root:
	//	        String relativePath = request.getRealPath("");
	//	        System.out.println("relativePath = " + relativePath);
	         
	        // obtains ServletContext
	        ServletContext contextServ = getServletContext();
	         
	        // gets MIME type of the file
	        String mimeType = contextServ.getMimeType(filePath);
	        if (mimeType == null) {        
	            // set to binary type if MIME mapping not found
	            mimeType = "application/pdf";
	        }
//	        System.out.println("MIME type: " + mimeType);
	         
	        // modifies response
	        response.setContentType(mimeType);
	        response.setContentLength((int) downloadFile.length());
	         
	        // forces download
	        String headerKey = "Content-Disposition";
	        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
	        response.setHeader(headerKey, headerValue);
	         
	        // obtains response's output stream
	        OutputStream outStream = response.getOutputStream();
	         
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	         
	        while ((bytesRead = inStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	         
			LogSystem.info(request, "application/pdf download");
	
	        inStream.close();
	        outStream.close();      
        
		} catch (Exception e) {
            LogSystem.error(getClass(), e);

        }finally {
        	try {
        		db.close();
        	}
        	catch (Exception e) {
                LogSystem.error(getClass(), e);
			}
        	
        }
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
        doPost(request, response);
	}
	

}
