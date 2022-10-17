package apiMitraBackup;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.LogSystem;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class StatusDocMitraWRC extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		//System.out.println("DATA DEBUG :"+(i++));
		LogSystem.info(request, "DATA DEBUG :"+(i++));
		Mitra mitra=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {
					JSONObject jo=new JSONObject();
					jo.put("res", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					
					return;
				}
				// multipart form
				else {
					TokenMitraDao tmd=new TokenMitraDao(getDB(context));
					String token=request.getHeader("authorization");
					if(token!=null) {
						String[] split=token.split(" ");
						if(split.length==2) {
							if(split[0].equals("Bearer"))token=split[1];
						}
						TokenMitra tm=tmd.findByToken(token.toLowerCase());
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada : " + token);
							mitra=tm.getMitra();
						} else {
							LogSystem.info(request, "Token tidak ada");
							JSONObject jo=new JSONObject();
							jo.put("res", "55");
							jo.put("notif", "token salah");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							
							return;
						}
					}
					
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
						
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI());
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         JSONObject jo = null;
	        
	         jo = checkDoc(mitra, useradmin, jsonRecv, request, context);
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo);



		}catch (Exception e) {
            LogSystem.error(getClass(), e);
		}
	}
	
	JSONObject checkDoc(Mitra mitratoken, User useradmin, JSONObject jsonRecv, HttpServletRequest  request,JPublishContext context) throws JSONException {
		DB db = getDB(context);

        JSONObject jo=new JSONObject();
        JSONArray lWaiting= new JSONArray();
		JSONArray lSigned= new JSONArray();
		String status = "waiting";
        String res="06";
        Documents id_doc=null;
        boolean kirim=false;
        String filePath = null;
        DocumentsDao docDao = new DocumentsDao(db);
        
        try{
        	ApiVerification aVerf = new ApiVerification(db);

        	boolean vrf=false;
	        if(mitratoken!=null) {
	        	vrf=true;
	        }
	        else {
	        	vrf=aVerf.verification(jsonRecv);
	        }
        	
			if(vrf){
				User usr=null;
	        	Mitra mitra=null;
	        	if(mitratoken==null && useradmin==null) {
		        	if(aVerf.getEeuser().isAdmin()==false) {
		        		jo.put("result", "12");
		                jo.put("notif", "userid anda tidak diijinkan.");
		                return jo;
		        	}
		        	usr = aVerf.getEeuser();
		    		mitra = usr.getMitra();
	        	}
	        	else {
	        		usr=useradmin;
	        		mitra=mitratoken;
	        	}
	        	
				List<DocumentAccess> listDoc= null;
 				DocumentsAccessDao docAcDao=new DocumentsAccessDao(db);
 				DocumentsDao dd = new DocumentsDao(db);
// 				List<Documents> ld=dd.findByUserDocID(usr.getId(), jsonRecv.get("document_id").toString());
 				List<Documents> ld=dd.findByDocID(jsonRecv.get("document_id").toString());
 				if(ld.size()>0) {
 					Documents document=ld.get(0);
 					if(document.getEeuser().getMitra().getId()!=mitra.getId()) {
 						jo.put("result", "12");
		                jo.put("notif", "Mitra tidak diijinkan untuk akses dokumen.");
		                return jo;
 					}
 					//listDoc =docAcDao.findAccessByEEMitraDocID(String.valueOf(usr.getId()), jsonRecv.get("document_id").toString());
 	 				listDoc=docAcDao.findByDoc(document.getId().toString());
 					System.out.println("SIZE :"+listDoc.size());
 	 				if(listDoc.size()>0) {
 	 					Documents fileDoc=listDoc.get(0).getDocument();
 	 					User author=fileDoc.getEeuser();
 		 				filePath=fileDoc.getPath()+fileDoc.getSigndoc();
 		 				
 		 				
 		 				for(DocumentAccess doc:listDoc) {
 		 					JSONObject docObj=new JSONObject();
 							/*change to eeuser*/
 		// 					User userDoc=new UserManager(db).findByUserID(String.valueOf(doc.getUserdata().getId()));
 		
 		 					User userDoc=doc.getEeuser();
 		 					if(userDoc==null) {
 								docObj.put("name", doc.getName());
 								docObj.put("email", doc.getEmail());
 		
 		 					}else {
	 							docObj.put("name", userDoc.getName());
	 							docObj.put("email", userDoc.getNick());
 		 					}
 		 					if(doc.getType().equals("sign") && !doc.isFlag()) {
 		 						lWaiting.put(docObj);
 		 							
 		 					}if(doc.getType().equals("sign") && doc.isFlag()) {
 		 						lSigned.put(docObj);
 		 					}
 		 					
 		 				}
 		 				if(lWaiting!=null && lWaiting.length()>0) {
 		 					jo.put("waiting", lWaiting);
 		 					jo.put("status", "waiting");
 		 				}
 		 				if(lSigned!=null && lSigned.length()>0) {
 		 					jo.put("signed", lSigned);
 		 					if(lWaiting==null || lWaiting.length()==0) {
 		 						jo.put("status", "complete");
 		 					}
 		 				}
 		 				
 	 				}
 	 				res="00";
 				}
 				else {
 					//jo=aVerf.setResponFailed(jo);
 					res="05";
 					jo.put("notif", "Document_id tidak ditemukan");
 				}
 				
			}
			else {
				//jo=aVerf.setResponFailed(jo);
				res="12";
				jo.put("notif", "Userid atau password salah");
			}
        }
        catch (Exception e) {
			// TODO: handle exception
			res="06";
			LogSystem.error(getClass(), e);
		}
        if(!jo.has("result"))jo.put("result", res);
 	   return jo;
	}
	
		
		
}
