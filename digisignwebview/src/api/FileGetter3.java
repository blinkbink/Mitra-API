package api;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.hibernate.Session;

import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;
import id.sni.digisign.filetransfer.Samba;

import java.io.ByteArrayInputStream;
import java.io.File;
//import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

public class FileGetter3 extends HttpServlet {

	Logger log=LogManager.getLogger(FileGetter3.class);
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();		
        String filePath = null;
    	User userRecv;
    	
    	String path_app =this.getClass().getName();
    	
		 DB db = new DB();
		 InputStream inStream=null;
		 OutputStream outStream=null;
        try {
            db.open ();
            Session hs = db.session();
          
	        
			int count = 21;
	//		HttpServletRequest  request  = context.getRequest();
	//		HttpServletResponse  response  = context.getResponse();
			String res="";
		    String[] uri=request.getRequestURI().split("/");
		    boolean useopt = false;
			String path = "";
			AESEncryption aes = new AESEncryption();
			String uri3=AESEncryption.decrypt(request.getParameter("id"));
			String uri4=AESEncryption.decrypt(request.getParameter("doc"));
			String uri5=AESEncryption.decrypt(request.getParameter("tp"));
			String uri6=AESEncryption.decrypt(request.getParameter("ss"));
			String mitra_req=AESEncryption.decrypt(request.getParameter("mr"));
			String email_req=AESEncryption.decrypt(request.getParameter("er"));
			String CATEGORY=AESEncryption.decrypt(request.getParameter("ca"));
			
			String refTrx=request.getParameter("refTrx");
			boolean checkopt = uri5.startsWith("optfile");
			path="/"+uri5+"/data-DS/UploadFile/";
			String session[]=uri6.split("\\|");
			Date now=new Date();
			boolean exp=true;
			Date expDate=new Date(Long.valueOf(session[1]));
			if(!now.after(expDate)) {
				exp=false;
			}
			String namafileSession=AESEncryption.decrypt(session[0]);
			String logInfo="URI :"+request.getRequestURI()+"\\namefile="+namafileSession+"\\expDate="+expDate+"\\nowDate="+now+"\\expStatus="+exp;
			LogSystem.info(request, logInfo, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), "", "", CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			if(!namafileSession.equals(uri4)) {
				response.sendError(404);
			    return; 
			}
			if(exp) {
				response.sendError(419);
			    return; 
			}
			//path+=uri[3]+"/original/"+uri[4];
			path+=uri3+"/original/"+uri4;
			LogSystem.request(request, refTrx);
//			System.out.println("hasil path adalah = "+path);
			
//	        //File downloadFile = new File(path);
//	        if(!downloadFile.isFile()) {
//	        	User us=new UserManager(db).findById(Long.valueOf(uri3));
//	        	path="/opt/data-DS/UploadFile/";
//	 			path+=us.getUserdata().getId()+"/original/"+uri4;
//	 			//downloadFile = new File(path);
//	        }
//	        //inStream = new FileInputStream(downloadFile);
//			if(useopt) {
//				inStream =  new ByteArrayInputStream(smb.openfile(path));
//			}else {
//				inStream =  new ByteArrayInputStream(smb.openfile_NAS(path));
//			}
			
			Samba smb = new Samba(refTrx, request, mitra_req, email_req, CATEGORY, start);

//			
			byte[] encoded ;
			try {
				inStream =  new ByteArrayInputStream(smb.openfile(path));
			}catch (Exception e) {
	      	     log.error(LogSystem.getLog( ExceptionUtils.getStackTrace(e), uri5, "ERROR"));
  				 smb.close();
	      	     throw e;
			}finally {
				smb.close();
			}
			
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
//	        response.setContentLength((int) downloadFile.length());
//	         
//	        // forces download
//	        String headerKey = "Content-Disposition";
//	        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
//	        response.setHeader(headerKey, headerValue);
	         
	        // obtains response's output stream
	        outStream = response.getOutputStream();
	         
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	         
	        while ((bytesRead = inStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	         
//			LogSystem.info(request, "application/pdf download",refTrx);
			LogSystem.info(request, "application/pdf download", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), "", "", CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	             
        
		} catch (Exception e) {
            LogSystem.error(getClass(), e);
            e.printStackTrace();
        }finally {
        	try {
        		if(inStream!=null)inStream.close();
    	        if(outStream!=null)outStream.close();
        		db.close();
        		response.flushBuffer();
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
