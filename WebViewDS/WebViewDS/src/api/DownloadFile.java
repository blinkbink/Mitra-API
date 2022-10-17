package api;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import org.hibernate.Session;
import id.co.keriss.consolidate.util.LogSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

public class DownloadFile extends HttpServlet {

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		User userRecv;

//		System.out.println("masukkkkkkkkkkkkkkkk");
        String filePath = null;

		 DB db = new DB();
        try {
            db.open ();
            Session hs = db.session();
	        
			userRecv=new UserManager(db).findById((long) 5790);
			int count = 21;
	//		HttpServletRequest  request  = context.getRequest();
	//		HttpServletResponse  response  = context.getResponse();
			String jsonString=null;
			byte[] dataFile=null;
			String res="";
			List<FileItem> fileItems = null;
			try{
					boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	
					// no multipart form
					if (!isMultipart) {
	
					}
					// multipart form
					else {
						// Create a new file upload handler
						ServletFileUpload upload = new ServletFileUpload(
								new DiskFileItemFactory());
	
						// parse requests
						 fileItems = upload.parseRequest(request);
	
						// Process the uploaded items
						for (FileItem fileItem : fileItems) {
							// a regular form field
							if (fileItem.isFormField()) {
								if(fileItem.getFieldName().equals("jsonfield")){
									jsonString=fileItem.getString();
								}
								
							}
							else {
								
								 if(fileItem.getFieldName().equals("file")){
									 dataFile=fileItem.get();
								 }
								// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());
	
							}
						}
					}
				 String process=request.getRequestURI().split("/")[2];
	//	         System.out.println("PATH :"+request.getRequestURI());
	//	         System.out.println("RECEIVE :"+jsonString);
	//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
		         LogSystem.request(request,fileItems);
				 if(jsonString==null) return;	         
		         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
		         JSONObject jo = new JSONObject();
		         
		         
		         try{
		 			UserManager um=new UserManager(db);
		 			User usr= um.findByUsername(jsonRecv.get("userid").toString());
		 			userRecv=null;
	//	 			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
		 			DigiSign ds=new DigiSign();
		 			if(um==null){
		 				res="06"; //username/ password salah
		 			}
		 			else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
		 				System.out.println(usr.getLogin().getPassword());
		 				System.out.println(jsonRecv.get("pwd"));
		 				res="06";
		 			}
		 			else{
		 				List<DocumentAccess> listDoc= null;
		 				DocumentsAccessDao docDao=new DocumentsAccessDao(db);
		 				if(!process.equals("DWL01.html"))listDoc =docDao.findAccessByEEMitraDocID(String.valueOf(usr.getId()), jsonRecv.get("document_id").toString());
		 				else listDoc =docDao.findDocEEuser(jsonRecv.get("document_id").toString(),String.valueOf(usr.getId()),jsonRecv.get("userid").toString());
		 				System.out.println("SIZE :"+listDoc.size());
	
		 				if(listDoc.size()>0) {
			 				Documents fileDoc=listDoc.get(0).getDocument();
							/*change to eeuser*/
//			 				User author=new UserManager(db).findByUserID(String.valueOf(fileDoc.getUserdata().getId()));

			 				User author=fileDoc.getEeuser();
			 				filePath=fileDoc.getPath()+fileDoc.getSigndoc();
			 				JSONArray lWaiting= new JSONArray();
			 				JSONArray lSigned= new JSONArray();
		
//			 				for(DocumentAccess doc:listDoc) {
//			 					JSONObject docObj=new JSONObject();
//								/*change to eeuser*/
////			 					User userDoc=new UserManager(db).findByUserID(String.valueOf(doc.getUserdata().getId()));
//
//			 					User userDoc=doc.getEeuser();
//			 					if(userDoc==null) {
//									docObj.put("name", doc.getName());
//									docObj.put("email", doc.getEmail());
//
//			 					}else {
//								docObj.put("name", userDoc.getName());
//								docObj.put("email", userDoc.getNick());
//			 					}
//			 					if(doc.getType().equals("sign") && !doc.isFlag()) {
//			 						lWaiting.put(docObj);
//			 							
//			 					}if(doc.getType().equals("sign") && doc.isFlag()) {
//			 						lSigned.put(docObj);
//			 					}
//			 					
//			 				}
//			 				if(lWaiting!=null && lWaiting.length()>0)jo.put("waiting", lWaiting);
//			 				if(lSigned!=null && lSigned.length()>0)jo.put("signed", lSigned);
			 				res="00";
			 				userRecv=usr;
		 				}
		 				else {
		 					res="NF";
		 				}
		 			}
	
	//	 			TrxDs trx=new TrxDs();
	//	 			trx.setMessage(jsonRecv.toString());
	//	 			trx.setMsg_from(k.getUserdata());
	//	 			trx.setMsg_time(new Date());
	//	 			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
	//	 			else trx.setStatus(new StatusKey("EVR"));
	//	 			trx.setType('1');
	//	 			if(userRecv!=null){
	//	 				trx.setMsg_to(userRecv.getUserdata());
	//	 				new TrxDSDao(db).create(trx);
	//	 			}
		 			
		 		}
		 		catch (Exception e) {
		 			// TODO: handle exception
		 			res="06";
		 			LogSystem.error(getClass(), e);
		 		}
	//	         
	//	         if(process.equals("DS01.html")){
	//	        	 jo=register(jsonRecv);
	//	         }else if(process.equals("DS02.html")){
	//	        	 jo=checkPubKey(jsonRecv);
	//	         }else if(process.equals("TRX.html") && jsonRecv.get("trx").equals("01")){
	//	        	 jo=checkSign(jsonRecv);
	//	         }else if(process.equals("TRX.html") && jsonRecv.get("trx").equals("02")){
	//	        	 jo=setSign(jsonRecv);
	//	         }else if(process.equals("TRX01.html")){
	//	        	 jo=checkFile(jsonRecv,dataFile);
	//	         }else if(process.equals("LGN.html")){
	//	        	 jo=login(jsonRecv,dataFile);
	//	         }
	//			if(jo!=null)res= jo.toString();
	//			else res="<b>ERROR 404</b>";
	//	        
	//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
	//
	//			res=new JSONObject().put("result", "00").toString();
	//			context.put("trxjson", res);
		         
		         
		         System.out.println("DEBUG :" +jo.toString());
	
				if((jo.has("waiting")==false && filePath!=null)|| (process.equals("DWL01.html") && filePath!=null)) {
					// reads input file from an absolute path
					filePath=filePath.replace("..", "../../DigitalSignature");
					
			        File downloadFile = new File(filePath);
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
			        System.out.println("MIME type: " + mimeType);
			         
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
				}else {
					jo.put("result", res);
					
			        OutputStream outStream = response.getOutputStream();
			        outStream.write(new JSONObject().put("JSONFile", jo).toString().getBytes());
			        
			        // System.out.println("SEND :"+new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo);
	
				}
	
			}catch (Exception e) {
	            LogSystem.error(getClass(), e);
	//			error (context, e.getMessage());
	//            context.getSyslog().error (e);
			}
			  
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
		// TODO Auto-generated method stub
		doPost(request, response);

	}

	
}
