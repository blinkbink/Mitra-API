package id.co.keriss.consolidate.action.km;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.util.FileProcessor;

public class InfoDoc extends ActionSupport {
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	User user = (User) context.getSession().getAttribute (USER);
	DB db = getDB(context);	
	HttpServletRequest  request  = context.getRequest();
	HttpServletResponse  resp  = context.getResponse();
	User userTrx= user!=null?new UserManager(db).findById(user.getId()):null;
		
	try{

        DocumentsDao ddao=new DocumentsDao(db);
        List<Documents> trx=ddao.findByUserto(String.valueOf(user.getId()));
        context.put("trans",trx);
        String method = null;
		method = request.getParameter("frmProcess");
		String jsonResult = null;
        
		if(method.equals("getFile") && user!=null){
			 System.out.println("#### Download File ####");	
			 String fileName = request.getParameter("name"); //this will return `data.xls`
			 String path = request.getContextPath() +request.getParameter("path");
			    //using the File(parent, child) constructor for File class
			    File file = new File(path, fileName);
			    //verify if the file exists
			    if (file.exists()) {
			    	// reads input file from an absolute path
			        FileInputStream inStream = new FileInputStream(file);
			         
			        // modifies response
			        resp .setContentType("application/pdf");
			        resp .setContentLength((int) file.length());
			         
			        // forces download
			        String headerKey = "Content-Disposition";
			        String headerValue = String.format("attachment; filename=\"%s\"", request.getParameter("filename"));
			        resp.setHeader(headerKey, headerValue);
			         
			        // obtains response's output stream
			        OutputStream outStream = resp.getOutputStream();
			              
			    	int bytes;
					while ((bytes = inStream.read()) != -1) {
						outStream.write(bytes);
					}
			        
			        inStream.close();
			        outStream.close();     
			    	
			    	
			    } else {
			        //handle a response to do nothing
			    	System.out.println("File Tidak Ada");
			    }
		}
		
		if(method.equals("saveFile") && user!=null){
			 FileItem fileSave = null;
			 File fileTo;
			
			  try {
				  String description = request.getParameter("description");
				 
				  boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				  if (!isMultipart) {
					}
					// multipart form
					else {
						// Create a new file upload handler
						ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
						// parse requests
						List<FileItem> fileItems = upload.parseRequest(request);
						for (FileItem fileItem : fileItems) {
							if (fileItem.isFormField()) {

							}else {
								System.out.println("filenya   " + fileItem.getContentType());
								if (fileItem.getSize() > 0)
									fileSave = fileItem;
								
							}
						}
					}
				  
				  if (fileSave != null) {
					  
					  FileProcessor fProc=new FileProcessor();
					  fProc.uploadFile(request, db, userTrx, fileSave);
				  }
				  
				  /*
		            // Part list (multi files).
		            for (Part part : request.getParts()) {
		                String fileName = extractFileName(part);
		                if (fileName != null && fileName.length() > 0) {
		                    // File data
		                	Documents dc = new Documents();
		                    InputStream is = part.getInputStream();
		                    Blob blob = Hibernate.createBlob(is);
		                    Date date = new Date();
		                    dc.setUserdata(userTrx.getUserdata());
		                    dc.setWaktu_buat(date);
		                    dc.setFile(blob);
		                    dc.setFile_name(fileName);
		                    dc.setStatus('F');
		                    new DocumentsDao(db).create(dc);
		                }
		            }
		            */
				  
			  }catch (Exception e) {
		       e.printStackTrace();
		       request.setAttribute("errorMessage", "Error: " + e.getMessage());
		      } 
	    trx=ddao.findByUserto(String.valueOf(user.getId()));
	    context.put("trans",trx);	
			
		JSONObject jo =new JSONObject();
		jo.put("status", "OK");	
		System.out.println(jo.toString());
		jsonResult=jo.toString();
	}
        
        
        
	}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
            context.getSyslog().error (e);
           // context.put("content", content);
		}
	}

	    
	
}
