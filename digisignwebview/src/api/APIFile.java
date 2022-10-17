//package api;
//
//import id.co.keriss.consolidate.DS.DigiSign;
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.SmartphoneVerification;
//import id.co.keriss.consolidate.action.ajax.SendMailSSL;
//import id.co.keriss.consolidate.action.billing.BillingSystem;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.KeyDao;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.DocumentSummary;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.Key;
//import id.co.keriss.consolidate.ee.Userdata;
//import id.co.keriss.consolidate.util.FileProcessor;
//import id.co.keriss.consolidate.util.LogSystem;
//import id.co.keriss.consolidate.util.SignerModalku;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.SimpleDateFormat;
//import java.util.List;
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//import org.mortbay.log.Log;
//
//import com.anthonyeden.lib.config.Configuration;
//
//public class APIFile extends ActionSupport {
//
//	User userRecv;
//	SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		DB db = getDB(context);
//		//userRecv=new UserManager(db).findById((long) 5790);
////        User user = (User) context.getSession().getAttribute (USER);
////		int count = 21;
//		HttpServletRequest  request  = context.getRequest();
//		String jsonString=null;
//		String refTrx="";
//		FileItem dataFile=null;
//		String filename=null;
//		List<FileItem> fileItems =null;
//		try{
//				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//
//				// no multipart form
//				if (!isMultipart) {
//
//				}
//				// multipart form
//				else {
//					// Create a new file upload handler
//					ServletFileUpload upload = new ServletFileUpload(
//							new DiskFileItemFactory());
//
//					// parse requests
//				fileItems = upload.parseRequest(request);
//
//					// Process the uploaded items
//					for (FileItem fileItem : fileItems) {
//						// a regular form field
//						if (fileItem.isFormField()) {
//							if(fileItem.getFieldName().equals("jsonfield")){
//								jsonString=fileItem.getString();
//							}
//							
//						}
//						else {
//							
//							 if(fileItem.getFieldName().equals("file") || fileItem.getFieldName().equals("File")){
//								 dataFile=fileItem;
//								 filename=fileItem.getName();
//							 }
//							// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());
//
//						}
//					}
//				}
//			 String process=request.getRequestURI().split("/")[2];
////	         System.out.println("RECEIVE :"+jsonString);
//	         LogSystem.request(request,fileItems, refTrx);
////	         Logger ll=Logger.getLogger(this.getClass());
////	         ll.info("testtttttttttttttttttttttttttt");
////	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
////	         
//	         //Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
//			
//			 if(jsonString==null) return;	         
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         
//	         JSONObject jo = null;
////	         if(process.equals("SGN01.html")){
////	        	 	jo=signFile(jsonRecv, dataFile, filename);
////	         }
//	         if(process.equals("UPL01.html")){
//	        	 	jo=uploadFile(jsonRecv, dataFile, filename,request,context);
//	         }
//	         if(process.equals("LSTDOC.html")){
//	        	 	jo=listDoc(jsonRecv,context);
//	         }
//	         if(process.equals("LSTDOC2.html")){
//	        	 	jo=listDoc2(jsonRecv,context);
//	         }
//	         if(process.equals("DWLFILE.html")){
//	        	 	jo=dwlDoc(jsonRecv,context);
//	         }
//	         if(process.equals("SND01.html")){
//	                jo=sendReqDoc(jsonRecv,dataFile,request,context);
//	         }
//	         if(process.equals("SND-MODALKU01.html")){
//	                jo=sendReqDocModalku(jsonRecv,dataFile,request,1,context); //badan usaha
//	         }
//	         if(process.equals("SND-MODALKU02.html")){
//	                jo=sendReqDocModalku(jsonRecv,dataFile,request,2,context); //individu
//	         }
//	         if(process.equals("DTFILE.html")){
//	                jo=detailDoc(jsonRecv,context);
//	         }
//	         if(process.equals("DELDOC.html")) {
//	        	 	jo=deleteDocument(jsonRecv,context);
//	        	 
//	         }
//	         String res="";
//			if(jo!=null) {
//				res= new JSONObject().put("JSONFile", jo).toString();
//				LogSystem.response(request, jo,"");
//			}
//			else {
//				res="<b>ERROR 404</b>";
//				LogSystem.info(request, res,"");
//			}
//	        
////			Logger.getLogger("q2").info(request.getRequestURI()+ ", RESPONSE : "+res);
////			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			context.put("trxjson", res);
//	        context.getSyslog().info (" SEND: " + context.getRequest().getRequestURI()+" - "+res);
//
//			
//
//		}catch (Exception e) {
//
//			LogSystem.log.error(e);
//		}
//	}
//	
//	
//	JSONObject signFile(JSONObject jsonRecv, byte[] dataFile, String filename,JPublishContext context) throws JSONException{
//     
//		DB db = getDB(context);
//		JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			User usr= um.findByUsername(jsonRecv.get("userid").toString());
//			userRecv=null;
//			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			if(um==null){
//				res="06"; //username/ password salah
//			}
////			else if(!usr.getPassword().equals(jsonRecv.get("pwd"))){
////				System.out.println(usr.getPassword());
////				System.out.println(jsonRecv.get("pwd"));
////				res="06";
////			}
//			else if(k!=null){
//				ByteArrayOutputStream os=ds.signFile(dataFile,filename, k.getKey(), jsonRecv.get("key-pwd").toString());
////			    signature=ds.generateSignature(Base64.decode(message), k.getKey(), jsonRecv.get("pwd-key").toString());
//				if(os!=null){
//					  res="00";
//					  File file = new File(filename);
//	
//				          /* This logic will check whether the file
//					   * exists or not. If the file is not found
//					   * at the specified location it would create
//					   * a new file*/
//					  if (!file.exists()) {
//						 file.getParentFile().mkdirs();
//					     file.createNewFile();
//					  }
//	
//					  /*String content cannot be directly written into
//					   * a file. It needs to be converted into bytes
//					   */
//					  FileOutputStream fos = new FileOutputStream(file);
//				
//					  fos.write(os.toByteArray());
//					  fos.flush();
//					  System.out.println("File Written Successfully");
//			        jo.put("file-signed", signature);
//				}else{
//					jo.put("info", ds.getNotif());
//					res="07";
//				}
//				userRecv=usr;
//			}
//
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject uploadFile(JSONObject jsonRecv, FileItem dataFile, String filename,HttpServletRequest  request,JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				  User usr= sVerf.getEeuser();
//				  FileProcessor fProc=new FileProcessor();
//				  if(fProc.uploadFile(request, db, usr, dataFile)) {
// 					  res="00";
//					  System.out.println("Upload Successfully");
//					    
//				  }
//				  
//		          jo.put("document_id", new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename()).getId().toString());
//		          jo.put("file", fProc.getDc().getPath()+ fProc.getDc().getFile_name());
//		          jo.put("create-date",sdf.format(fProc.getDc().getWaktu_buat()));
//  
//				  userRecv=usr;
//				
//			}else {
//				jo=sVerf.setResponFailed(jo);
//			}
//			
//			
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result"))jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject dwlDoc(JSONObject jsonRecv, JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			User usr= um.findByUsername(jsonRecv.get("userid").toString());
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			if(um==null){
//				res="06"; //username/ password salah
//			}
//			else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
//				System.out.println(usr.getLogin().getPassword());
//				System.out.println(jsonRecv.get("pwd"));
//				res="06";
//			}
//			else{
////				Documents doc=null;
//				DocumentsDao docDao=new DocumentsDao(db);
//				Documents doc =docDao.findByUserID(String.valueOf(usr.getId()),Long.valueOf(jsonRecv.get("document_id").toString()));
////				JSONArray lDocArray= new JSONArray();
////				for(Documents doc:listDoc) {
////					JSONObject docObj=new JSONObject();
////					String stat=doc.getStatus()=='F'?"upload":"sign";
////					jo.put("document_id", doc.getId().toString());
//					jo.put("file", doc.getPath()+doc.getSigndoc());
////					jo.put("name", doc.getFile_name());
////					jo.put("create-date", doc.getId().toString());
////					jo.put("flag", stat);
////					lDocArray.put(docObj);
////				}
////				jo.put("document", lDocArray);
//				res="00";
////				userRecv=usr;
//			}
//
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        jo.put("result", res);
//	   return jo;
//	}
//	
//
//	JSONObject listDoc(JSONObject jsonRecv, JPublishContext context) throws JSONException{
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		DB db = getDB(context);
//
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				
//				List<Documents> listDoc= null;
//				DocumentsDao docDao=new DocumentsDao(db);
//				listDoc =docDao.findByUserto(String.valueOf(usr.getId()));
//				JSONArray lDocArray= new JSONArray();
//				for(Documents doc:listDoc) {
//					JSONObject docObj=new JSONObject();
//					boolean locationSign=true;
//					String stat=doc.isSign()==false?"upload":"sign";
//					if(stat.equals("upload")) {
//						if(doc.getStatus()=='T') {
//							stat="send";
//						}else {
//							if(new DocumentsAccessDao(db).findDraft(doc.getId().toString())) {
//								stat="draft";
//							}
//						}
//						
//					}
//					docObj.put("document_id", doc.getId().toString());
//					docObj.put("file", doc.getPath()+doc.getRename());
//					docObj.put("name", doc.getFile_name());
//					docObj.put("create-date", doc.getWaktu_buat());
//					docObj.put("flag", stat);
//					lDocArray.put(docObj);
//				}
//				jo.put("document", lDocArray);
//				
//				List<DocumentAccess> listDocAccess= null;
//				DocumentsAccessDao accessDao=new DocumentsAccessDao(db);
//				listDocAccess =accessDao.findByEEUser(String.valueOf(usr.getId()));
//				JSONArray lAcsArray= new JSONArray();
//				for(DocumentAccess acs:listDocAccess) {
//					/*change to eeuser*/
////					Userdata frDoc= acs.getDocument().getUserdata();
////					User userFrom=um.findByUserID(String.valueOf(frDoc.getId()));
//
//					User userFrom=acs.getEeuser();
//
//					JSONObject docObj=new JSONObject();
//					String stat=acs.getType().equals("sign") && !acs.isFlag()?"unsigned":acs.getType();
//					docObj.put("document_id", acs.getDocument().getId().toString());
//					docObj.put("file", acs.getDocument().getPath()+acs.getDocument().getRename());
//					docObj.put("name-req", userFrom.getUserdata().getNama());
//					docObj.put("email-req",  userFrom.getNick());
//					docObj.put("name", acs.getDocument().getFile_name());
//					docObj.put("create-date", acs.getDocument().getWaktu_buat());
//					docObj.put("flag", stat);
//					String docStat=accessDao.checkSignedDocument(acs.getDocument().getId())==true?"true":"false";
//					docObj.put("doc-status", docStat);
//					if(acs.getType().equals("sign") && acs.getPage()==0) {
//						continue;
//					}
//					if(acs.getDocument().getStatus()=='F') {
//						continue;
//					}
//					lAcsArray.put(docObj);
//				}
//				jo.put("other-doc", lAcsArray);
//				DocumentSummary dSum=docDao.getSummaryDocument(usr);
//				jo.put("waiting", String.valueOf(dSum.getWaiting()));
//				jo.put("completed", String.valueOf(dSum.getCompleted()));
//				jo.put("sign", String.valueOf(dSum.getNeedSign()));
//				BillingSystem bs=new BillingSystem(usr);  
//	            int blc=bs.getBalance();
//	            jo.put("credit", String.valueOf(blc));
//				userRecv=usr;
//				res="00";
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	
//	JSONObject listDoc2(JSONObject jsonRecv, JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				
//				List<Documents> listDoc= null;
//				DocumentsDao docDao=new DocumentsDao(db);
//				listDoc =docDao.findByUserto(String.valueOf(usr.getId()));
//				JSONArray lDocArray= new JSONArray();
//				for(Documents doc:listDoc) {
//					JSONObject docObj=new JSONObject();
//					boolean locationSign=true;
//					
//					docObj.put("document_id", doc.getId().toString());
//					docObj.put("file", doc.getPath()+doc.getRename());
//					docObj.put("name", doc.getFile_name());
//					docObj.put("create-date", doc.getWaktu_buat());
//				    String sign_flag="author";
//					String stat=doc.isSign()==false?"upload":"sign";
//					if(stat.equals("upload")) {
//						if(doc.getStatus()=='T') {
//							stat="send";
//							if(doc.getTypeShare().equals("sign")) {
//								stat="waiting";
//							}
//							sign_flag=doc.getStatusSign();
//						}else {
//							if(doc.getDocAccess().size()>0)
//								stat="draft";
//							else
//								stat="upload";
//						}
//						
//					}else {
//						
//						sign_flag="signed";
//					}
//					docObj.put("flag", stat);
//					docObj.put("sign_flag", sign_flag);
//
//					lDocArray.put(docObj);
//				}
//				jo.put("document", lDocArray);
//				
//				List<DocumentAccess> listDocAccess= null;
//				DocumentsAccessDao accessDao=new DocumentsAccessDao(db);
//				listDocAccess =accessDao.findOtherDoc(usr);
//				JSONArray lAcsArray= new JSONArray();
//				for(DocumentAccess acs:listDocAccess) {
//					/*change to eeuser*/
////					Userdata frDoc= acs.getDocument().getUserdata();
////					User userFrom=um.findByUserID(String.valueOf(frDoc.getId()));
//					if(acs.getEeuser()==null) {
//						acs.setEeuser(usr);
//						accessDao.update(acs);
//					}
//					User userFrom=acs.getDocument().getEeuser();
//
//					JSONObject docObj=new JSONObject();
//					String stat=acs.getType().equals("sign") && !acs.isFlag()?"unsigned":acs.getType();
//					docObj.put("document_id", acs.getDocument().getId().toString());
//					docObj.put("file", acs.getDocument().getPath()+acs.getDocument().getRename());
//					docObj.put("name-req", userFrom.getUserdata().getNama());
//					docObj.put("email-req",  userFrom.getNick());
//					docObj.put("name", acs.getDocument().getFile_name());
//					docObj.put("create-date", acs.getDocument().getWaktu_buat());
//					docObj.put("flag", stat);
//					String docStat=accessDao.checkSignedDocument(acs.getDocument().getId())==true?"true":"false";
//					docObj.put("doc-status", docStat);
//					if(acs.getType().equals("sign") && acs.getPage()==0) {
//						continue;
//					}
//					if(acs.getDocument().getStatus()=='F') {
//						continue;
//					}
//					lAcsArray.put(docObj);
//				}
//				jo.put("other-doc", lAcsArray);
//				DocumentSummary dSum=docDao.getSummaryDocument(usr);
//				jo.put("waiting", String.valueOf(dSum.getWaiting()));
//				jo.put("completed", String.valueOf(dSum.getCompleted()));
//				jo.put("sign", String.valueOf(dSum.getNeedSign()));
//				BillingSystem bs=new BillingSystem(usr);  
//	            int blc=bs.getBalance();
//	            String balance=String.valueOf(blc);
//	            if(blc<0) {
//	            	balance="-";
//	            }
//	            jo.put("credit", balance);
//				userRecv=usr;
//				res="00";
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	
//	JSONObject sendReqDoc(JSONObject jsonRecv,FileItem dataFile,HttpServletRequest  request, JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//        boolean kirim=false;
//        char pay;
//		try{
//			UserManager um=new UserManager(db);
////			User usr= um.findByUsername(jsonRecv.get("userid").toString());
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
//			pay=jsonRecv.get("type").toString().charAt(0);
//			if(sVerf.verification(jsonRecv)){
//				  User usr= sVerf.getEeuser();
//				  
//				
//				DocumentsDao docDao=new DocumentsDao(db);
//				Documents id_doc=null;
//				if(dataFile==null) {
//					id_doc=docDao.findByUserID(String.valueOf(usr.getId()),Long.valueOf(jsonRecv.get("document_id").toString()));
//				}
//				else {
//					  FileProcessor fProc=new FileProcessor();
//					  if(fProc.uploadFile(request, db, usr, dataFile,jsonRecv.get("document_id").toString())) {
//						  System.out.println("Upload Successfully");
//					      Log.info("Q2", "UPLOAD & REQ SIGN ["+usr.getNick()+"] , SUCCESS : "+ fProc.getDc().getPath()+ fProc.getDc().getFile_name());
//
//						    
//					  }
//					  
//					  id_doc=new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename());
//
//			          
//				}
//				
//				if(id_doc!=null) {
//					DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
//					int clear=dAccessDao.clearAkses(id_doc.getId());
//					System.out.println("clear access id: "+id_doc.getId()+", "+clear+" rows");
//					if(jsonRecv.get("send-to")!=null) {
//						
//						JSONArray sendList=jsonRecv.getJSONArray("send-to");
//						
//						for(int i=0; i<sendList.length(); i++) {
//							JSONObject obj=(JSONObject) sendList.get(i);
//
//							if(sendList.getString(i).equals(usr.getNick())) continue;
//							DocumentAccess da=new DocumentAccess();
////							User user= new UserManager(db).findByUsername(sendList.getString(i));
//							User user= new UserManager(db).findByUsername(obj.getString("email"));
//
//							/*change to eeuser*/
////							if(user!=null)da.setUserdata(user.getUserdata());
//
//							if(user!=null)da.setEeuser(user);
//							da.setDocument(id_doc);
//							da.setFlag(false);
//							da.setType("share");
//							da.setDate_sign(null);
//							da.setEmail(obj.getString("email").toLowerCase());
//							da.setName(obj.getString("name"));
//							dAccessDao.create(da);
//							//new SendMailSSL().sendMailFile(usr.getName(), sendList.getString(i));
////							MailSender mail=new MailSender(usr.getName(), obj.getString("email"));
////							Thread mailThr=new Thread(mail);
////							mailThr.start();
//							
//							kirim=true;
//
//						}
//						
//					}
//					
//					if(jsonRecv.get("req-sign")!=null) {
//						JSONArray sendList=jsonRecv.getJSONArray("req-sign");
//						for(int i=0; i<sendList.length(); i++) {
//							JSONObject obj=(JSONObject) sendList.get(i);
//							
//							DocumentAccess da=new DocumentAccess();
//							User user= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());
//							/*change to eeuser*/
////							if(user!=null)da.setUserdata(user.getUserdata());
//
//							if(user!=null)da.setEeuser(user);
//							da.setDocument(id_doc);
//							da.setFlag(false);
//							da.setType("sign");
//							da.setDate_sign(null);
//							da.setEmail(obj.getString("email").toLowerCase());
//							da.setName(obj.getString("name"));
//							dAccessDao.create(da);
////							MailSender mail=new MailSender(usr.getName(), obj.getString("email"));
////							Thread mailThr=new Thread(mail);
////							mailThr.start();
//							kirim=false;
//
//						}
//					}
//					
//					if(kirim) { // jika hanya share saja maka rubah status menjadi sudah dikirim
//						id_doc.setStatus('T');
//					}
//					
//
//					id_doc.setPayment(pay);
//					docDao.update(id_doc);
//
//					res="00";
//					userRecv=usr;
//				}
//				
//
//				else {
//					jo=sVerf.setResponFailed(jo);
//				}
//			}
//			
//
//			
//			
//
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result"))jo.put("result", res);
//	   return jo;
//	}
//	
//	
//	JSONObject sendReqDocModalku(JSONObject jsonRecv,FileItem dataFile,HttpServletRequest  request, int mode, JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			LogSystem.request(request, "");
//			UserManager um=new UserManager(db);
//			User usr= um.findByUsername(jsonRecv.get("userid").toString());
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			if(um==null){
//				res="06"; //username/ password salah
//			}
//			else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
//				System.out.println(usr.getLogin().getPassword());
//				System.out.println(jsonRecv.get("pwd"));
//				res="06";
//			}
//			else{
//				DocumentsDao docDao=new DocumentsDao(db);
//				Documents id_doc=null;
//				if(dataFile==null) {
//					id_doc=docDao.findByUserID(String.valueOf(usr.getId()),Long.valueOf(jsonRecv.get("document_id").toString()));
//				}
//				else {
//					  FileProcessor fProc=new FileProcessor();
//					  if(fProc.uploadFile(request, db, usr, dataFile,jsonRecv.get("document_id").toString())) {
//						  System.out.println("Upload Successfully");
//					      Log.info("Q2", "UPLOAD & REQ SIGN ["+usr.getNick()+"] , SUCCESS : "+ fProc.getDc().getPath()+ fProc.getDc().getFile_name());
//
//						    
//					  }
//					  
//					  id_doc=new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename());
//
//			          
//				}
//				
//				if(id_doc!=null) {
//					DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
//					if(jsonRecv.get("send-to")!=null) {
//						JSONArray sendList=jsonRecv.getJSONArray("send-to");
//						
//						for(int i=0; i<sendList.length(); i++) {
//							JSONObject obj=(JSONObject) sendList.get(i);
//
//							if(sendList.getString(i).equals(usr.getNick())) continue;
//							DocumentAccess da=new DocumentAccess();
////							User user= new UserManager(db).findByUsername(sendList.getString(i));
//							User user= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());
//							/*change eeuser*/
////							if(user!=null)da.setUserdata(user.getUserdata());
//
//							if(user!=null)da.setEeuser(user);
//							da.setDocument(id_doc);
//							da.setFlag(false);
//							da.setType("share");
//							da.setDate_sign(null);
//							da.setEmail(obj.getString("email").toLowerCase());
//							da.setName(obj.getString("name"));
//							dAccessDao.create(da);
//							//new SendMailSSL().sendMailFile(usr.getName(), sendList.getString(i));
////							MailSender mail=new MailSender(usr.getName(), obj.getString("email"));
////							Thread mailThr=new Thread(mail);
////							mailThr.start();
//							
//						}
//					}
//					
//					if(jsonRecv.get("req-sign")!=null) {
//						JSONArray sendList=jsonRecv.getJSONArray("req-sign");
//						SignerModalku sgn=new SignerModalku();
//						for(int i=0; i<sendList.length(); i++) {
//							JSONObject obj=(JSONObject) sendList.get(i);
//							System.out.println("tes  :" +obj.getString("email").toLowerCase());
//							String[] posSign=null;
//							if(mode==1) {
//								posSign=sgn.getLocation(obj.getString("user"));
//							}
//							else {
//								posSign=sgn.getLocationIndividu(obj.getString("user"));
//							}
//							DocumentAccess da=new DocumentAccess();
//							User user= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());
//							/*change to eeuser*/
////							if(user!=null)da.setUserdata(user.getUserdata());
//
//							if(user!=null)da.setEeuser(user);
//							da.setDocument(id_doc);
//							da.setFlag(false);
//							da.setType("sign");
//							da.setDate_sign(null);
//							da.setEmail(obj.getString("email").toLowerCase());
//							da.setName(obj.getString("name"));
//							da.setPage(Integer.parseInt(posSign[0]));
//							da.setLx(posSign[1]);
//							da.setLy(posSign[2]);
//							da.setRx(posSign[3]);
//							da.setRy(posSign[4]);
//							dAccessDao.create(da);
//							MailSender mail;
//							if(user!=null) {
//								mail=new MailSender(user.getUserdata(),usr.getUserdata(), obj.getString("email").toLowerCase());
//							}
//							else {
//								mail=new MailSender(da.getName(),usr.getUserdata(), obj.getString("email").toLowerCase());
//
//							}
//								
//							mail.run();
//							//new SendMailSSL().sendMailFileaReqSign(usr.getName(), obj.getString("email"));
//	
//						}
//					}
//					id_doc.setStatus('T');
//					docDao.update(id_doc);
//					res="00";
//					userRecv=usr;
//				}
//			}
//
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        jo.put("result", res);
//	   return jo;
//	}
//
//
//	JSONObject detailDoc(JSONObject jsonRecv, JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
////			User usr= um.findByUsername(jsonRecv.get("userid").toString());
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////			
//			if(sVerf.verification(jsonRecv)){
//				  User usr= sVerf.getEeuser();
//				  
//				
//			
//				List<DocumentAccess> listDoc= null;
//				DocumentsAccessDao docDao=new DocumentsAccessDao(db);
//				DocumentsDao dDao=new DocumentsDao(db);
//				listDoc =docDao.findByDoc(jsonRecv.get("document_id").toString());
//				
//				Documents fileDoc=null;
//				if(listDoc.size()>0) {
//					fileDoc=listDoc.get(0).getDocument();
//				}else {
//					fileDoc=dDao.findByUserID(String.valueOf(usr.getId()), Long.valueOf(jsonRecv.get("document_id").toString()));
//				}
//				/*change to eeuser*/
////				User author=new UserManager(db).findByUserID(String.valueOf(fileDoc.getUserdata().getId()));
//				
//				User author=fileDoc.getEeuser();
//				jo.put("author-name", author.getName());
//				jo.put("author-email", author.getNick().toLowerCase());
//				boolean setLoc=false;
//				JSONArray lDocArray= new JSONArray();
//				for(DocumentAccess doc:listDoc) {
//					JSONObject docObj=new JSONObject();
//					/*change to eeuser*/
////					User userDoc=new UserManager(db).findByUserID(String.valueOf(doc.getUserdata().getId()));
//					
//					User userDoc=doc.getEeuser();
//					String nameUs = null;
//					String emailUs = null;
//					if(userDoc==null) {
//						nameUs=doc.getName();
//						emailUs=doc.getEmail();
//					}else {
//
//						nameUs=doc.getEeuser().getName();
//						emailUs=doc.getEeuser().getNick();
//					}
//					String status="share";
//					if(doc.getType().equals("sign")) {
//						if(doc.isFlag()) {
//							status="sign";
//						}else {
//							status="unsigned";
//						}
//						
//						if(doc.getPage()>0) {
//							setLoc=true;
//						}
//					}
//					docObj.put("name", nameUs);
//					docObj.put("email", emailUs);
//					docObj.put("flag", status);
//					
//					lDocArray.put(docObj);
//				}
//				String pay=new String(new char[] {fileDoc.getPayment()});
//				jo.put("type", pay);
//				jo.put("recipient", lDocArray);
//				jo.put("state_location", setLoc);
//				res="00";
//				userRecv=usr;
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
//	//			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//       if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject deleteDocument(JSONObject jsonRecv, JPublishContext context) throws JSONException{
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			User usr= um.findByUsername(jsonRecv.get("userid").toString());
//			String idDoc=jsonRecv.get("document_id").toString();
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			if(um==null){
//				res="06"; //username/ password salah
//			}
//			else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
//				System.out.println(usr.getLogin().getPassword());
//				System.out.println(jsonRecv.get("pwd"));
//				res="06";
//			}
//			else{
//				
//				Documents doc= null;
//				DocumentsDao docDao=new DocumentsDao(db);
//				doc=docDao.findByUserID(String.valueOf(usr.getId()), Long.valueOf(idDoc));
//				if(new DocumentsAccessDao(db).findByDoc(idDoc).size()>0) {
//					res="05";
//				}else if(doc==null){
//					res="05";
//				}
//				else {
//					docDao.delete(doc);
//					res="00";
//				}
//			}
//
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        jo.put("result", res);
//	   return jo;
//	}
//	
//
//	class MailSender{
//
//		String email;
//		Userdata name;
//		String notRegisterName;
//		Userdata pengirim;
//		
//		public MailSender(Userdata penerimaemail,Userdata pengirim,String email) {
//			this.email=email;
//			this.name=penerimaemail;
//			this.pengirim=pengirim;
//		}
//		
//		public MailSender(String penerimaemail,Userdata pengirim,String email) {
//			this.email=email;
//			this.notRegisterName=penerimaemail;
//			this.pengirim=pengirim;
//		}
//
//		public void run() {
//			if(name!=null) {
//				HttpServletRequest request = null; 
//				new SendMailSSL(request, "").sendMailFileaReqSign(name,pengirim, email);
//			}else {
//				HttpServletRequest request = null; 
//				new SendMailSSL(request, "").sendMailFileaReqSign(notRegisterName,pengirim, email);
//			}
//		}
//		
//	}
//}
