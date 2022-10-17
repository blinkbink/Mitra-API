package apiMitra;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class GenerateSigning extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date idTrx=new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String stringDate = sdfDate2.format(idTrx).toString();
		String refTrx="GEN-SIGNING-PAGE"+stringDate;
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		int statusSign=0;
		Boolean by_pass_click=false;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		//System.out.println("DATA DEBUG :"+(i++));
//		LogSystem.info(request, "DATA DEBUG : " + (i++));
		Mitra mitratoken=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				TokenMitraDao tmd = null;
				TokenMitra tm = null;
				String token = null;
				try {
					tmd=new TokenMitraDao(getDB(context));
					token=request.getHeader("authorization");
					if(token!=null) {
						String[] split=token.split(" ");
						if(split.length==2) {
							if(split[0].equals("Bearer"))token=split[1];
						}
					}
					tm=tmd.findByToken(token.toLowerCase());
				}catch(Exception e)
				{
					JSONObject jo=new JSONObject();
					jo.put("result", "91");
					jo.put("notif", "System timeout. silahkan coba kembali.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.error(request, e.toString(), refTrx);
					return;
				}
					
				if(tm!=null) {
					LogSystem.info(request, "Token ada", refTrx);
					mitratoken=tm.getMitra();
				} else {
					LogSystem.error(request, "Token tidak ada", refTrx);
					JSONObject jo=new JSONObject();
					jo.put("result", "55");
					jo.put("notif", "token salah");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.info(request, jo.toString(), refTrx);
					return;
				}
				

				// no multipart form
				if (!isMultipart) {
					//System.out.println("bukan multipart");
//					LogSystem.info(request, "Bukan multipart");
					jsonString=request.getParameter("jsonfield");
				}
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
						
					}
				}
			 String process=request.getRequestURI().split("/")[2];
//	         LogSystem.info(request, "PATH :"+request.getRequestURI());
//	            Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

	         LogSystem.request(request, fileItems, refTrx);
			 if(jsonString==null) return;
			 JSONObject jsonRecv = null;
			 try {
				 jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
			 }catch(Exception e)
			 {
				 	JSONObject jo=new JSONObject();
					jo.put("result", "28");
					jo.put("notif", "Request API tidak lengkap.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					return;
			 }
//	         LogSystem.info(request, "JSONFile Receive : "+jsonRecv);
//	         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, jsonRecv.toString(), idTrx, "RCV"));
	         LogSystem.info(request, jsonRecv.toString(), refTrx);

	         if(mitratoken!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
//		    		 LogSystem.error(request, "Parameter userid tidak ditemukan");
		    		 JSONObject jo=new JSONObject();
						jo.put("result", "05");
						jo.put("notif", "userid salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//			            Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
			            LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
	        		 return;
	        	 }
	        	 
	        	 String userid=jsonRecv.getString("userid").toLowerCase();
	        	 
		         UserManager user=new UserManager(getDB(context));
		         //User eeuser=user.findByUsername(userid);
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitratoken.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
//		        		 LogSystem.info(request, "Token dan mitra valid");
		        	 }
		        	 else {
//		        		 LogSystem.info(request, "Token dan mitra tidak valid");
		        		 JSONObject jo=new JSONObject();
						 jo.put("result", "05");
						 jo.put("notif", "Token dan Mitra tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//			            Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
			            LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
						 return;
		        	 }
		         }
		         else {
//		        	 LogSystem.info(request, "Userid tidak ditemukan");
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//			         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
			         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
					 return;
		         }
	         }
	         
	         //JSONObject jo = null;
	        
	         //DownloadDocMitra(jsonRecv, request, context);
	         DB db = getDB(context);
	         ApiVerification aVerf = new ApiVerification(db);
	         boolean vrf=false;
		        if(mitratoken!=null && useradmin!=null) {
		        	vrf=true;
		        }
		        else {
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "userid tidak diizinkan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//			         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
			         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
					 return;

		        }

		        User usermitra=useradmin;
	        	Mitra mitra=useradmin.getMitra();
        	 	
				String documentID=jsonRecv.getString("document_id");
				DocumentsDao dd = new DocumentsDao(db);
				LoginDao ldao=new LoginDao(db);
				//Documents doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
				Documents doc=null;
				List<Documents> ldocs;
				try {
		            LogSystem.info(request, "ID MITRA : ----- " + mitra.getId(), refTrx);
		            LogSystem.info(request, "DOCUMENT ID : ----- " + documentID, refTrx);
					ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
					LogSystem.info(request, "Document Access size " + ldocs.size(), refTrx);
					if(ldocs.size()==0) {

						JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "dokumen tidak ditemukan");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		    			 LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
			        	return;
					}
					else {
						doc=ldocs.get(0);
						for(int x=1; x<ldocs.size();x++) {
							DB dbConn= new DB();
							dbConn.open();
							DocumentsDao daDoc=new DocumentsDao(dbConn);
							Documents docData=ldocs.get(x);
							docData.setDelete(true);
							daDoc.update(docData);
							dbConn.close();						
						}
					}
				} catch (Exception e) {
//					LogSystem.error(request, "Error dokumen");
					e.printStackTrace();
					// TODO: handle exception
//					context.getResponse().sendError(404);
//			         Logger.getLogger(getClass()).error(LogSystem.getGENSGNLog(request, ExceptionUtils.getStackTrace(e), idTrx, "SND"));
			         LogSystem.info(request, e.toString(), refTrx);
					JSONObject jo=new JSONObject();
	    			 jo.put("result", "05");
	    			 jo.put("notif", "dokumen tidak ditemukan");
	    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//			         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
			         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);

					return;
				}
				
				String idDoc=null;
				String userEmail=jsonRecv.getString("email_user").toLowerCase();
				Long idmitra = usermitra.getId();
				UserManager um=new UserManager(db);
//	            LogSystem.info(request, "User Email: " + userEmail, refTrx);
				//System.out.println(userEmail);
				User usr= um.findByUsername2(userEmail);
				
				if(jsonRecv.has("by-pass-click"))
				{
					by_pass_click = jsonRecv.getBoolean("by-pass-click");
				}
				
				//System.out.println("user nya adalah = "+usr.getName());
				if(usr!=null) {
					//System.out.println("USR NICK ------------ : " + usr.getNick());
					
					
					//check level user < 3 dan level mitra > 2
					int leveluser=Integer.parseInt(usr.getUserdata().getLevel().substring(1));
		        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
		        	if(levelmitra>2 && leveluser<3) {
//		        		LogSystem.error(request, "level user tidak diperbolehkan untuk mitra ini");
//			            Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "level user tidak diperbolehkan untuk mitra ini", idTrx, "LOG"));
			            LogSystem.info(request, "level user tidak diperbolehkan untuk mitra ini", refTrx);
		        		 JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "level user tidak diperbolehkan untuk mitra ini");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				    	 LogSystem.response(request, jo);
//				         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
				         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
		    			 return;
		        	}
					
					List<DocumentAccess> da  = null;
					DocumentsAccessDao docDao = null;
					docDao=new DocumentsAccessDao(db);
					boolean viewOnly=jsonRecv.has("view_only")?jsonRecv.getBoolean("view_only"):false;

					try {
						da = docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
			            LogSystem.info(request, "Document ID : " + documentID, refTrx);
			            LogSystem.info(request, "ID Mitra : " + idmitra, refTrx);
			            LogSystem.info(request, "User Mail : " + userEmail , refTrx);
			            LogSystem.info(request, "Document Access ID : " + doc.getId(), refTrx);
						//System.out.println("Document ID : " + documentID);
						//System.out.println("ID Mitra : " + idmitra);
						//System.out.println("User Mail : " + userEmail);
						//System.out.println("Get Id : " + doc.getId());
					}catch(Exception e)
					{
						//System.out.println("Errorrnya disiniiiiiiiiiiiii : ");
//						LogSystem.info(request, "Errorrnya disiniiiiiiiiiiiii : ");
						e.printStackTrace();

//				         Logger.getLogger(getClass()).error(LogSystem.getGENSGNLog(request, ExceptionUtils.getStackTrace(e), idTrx, "SND"));
				         LogSystem.error(request, e.toString(), refTrx);
					}
					if(da.size()<1) {
//						LogSystem.error(request, "Error nih da.size() error < dari 1 masuk 404");
						//System.out.println("Error nih da.size() error < dari 1 masuk 404");
//			            Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "Error nih da.size() error < dari 1 masuk 404", idTrx, "LOG"));
			            LogSystem.info(request, "Error nih da.size() error < dari 1 masuk 404", refTrx);
						 JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "user tidak masuk penandatangan dokumen ini");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				    	 LogSystem.response(request, jo);
//				         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
				         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
		        		return;
					}else {
					
//						List<DocumentAccess> allDa =docDao.getAllUserDoc(documentID, idmitra, doc.getId());
//						
//						if(allDa.size()==0) {
////							LogSystem.error(request, "Error nih da.size() error < dari 1 masuk 404");
//				            Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "Error nih da.size() error < dari 1 masuk 404", idTrx, "LOG"));
//
//							//System.out.println("Error nih da.size() error < dari 1 masuk 404");
//							 JSONObject jo=new JSONObject();
//			    			 jo.put("result", "05");
//			    			 jo.put("notif", "user telah menandatangani dokumen ini");
//			    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//					         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
//
////					    	LogSystem.response(request, jo);
//
//			        		return;
//						}
//						int seq_now=allDa.get(0).getSequence_no();
						int seq_now=da.get(0).getDocument().getCurrent_seq();
						Iterator<DocumentAccess> daIt=da.iterator();
						boolean haveSigned=false;
						while (daIt.hasNext()) {
							DocumentAccess documentAccess=daIt.next();
							if(seq_now == documentAccess.getSequence_no()|| !documentAccess.getDocument().isSequence()) {
								//do nothing
								if(documentAccess.isFlag()) {
									if(viewOnly) break;
									daIt.remove();
									haveSigned=true;
								}
							}
							else if(seq_now > documentAccess.getSequence_no()) {
								if(viewOnly) break;
								daIt.remove();
								haveSigned=true;
							}else if(seq_now < documentAccess.getSequence_no()) {
								daIt.remove();
								haveSigned=false;
								
							}
						}
						
						
						if(da.size()<1) {
							 JSONObject jo=new JSONObject();
			    			 jo.put("result", "05");
			    			 if(haveSigned)	 jo.put("notif", "user telah menandatangani dokumen ini");
			    			 else jo.put("notif", "user belum waktunya menandatangani dokumen ini");
			    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//					         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
					         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
//				    		LogSystem.response(request, jo);
			        		return;
						}
						
					}
					

					
		    		SigningSession signSes=new SigningSession();
		    		Date current=new Date();
		    		Date expire=null;
		    		if(viewOnly) {
		    			expire=new Date(current.getTime()+(1000*20*60)); // 15 menit
		    			signSes.setView_only(true);

		    		}else {
		    			expire=new Date(current.getTime()+(1000*20*60)); // 5 menit
		    		}
		    		
		    		if(da.get(0).getEeuser()==null) {
						User signer=new UserManager(db).findByEmailMitra2(userEmail);
						if(signer!=null) {
							DocumentsAccessDao ddao=new DocumentsAccessDao(db);
							for (DocumentAccess documentAccess : da) {
								if(documentAccess.getEeuser()==null) {
									documentAccess.setEeuser(signer);
									ddao.update(documentAccess);
									
								}
							}
						}
					}
		    		signSes.setCreate_time(current);
		    		signSes.setExpire_time(expire);
		    		signSes.setEeuser(usr);
		    		signSes.setMitra(useradmin.getMitra());
		    		String dat=current.getTime()+";"+usr.getNick()+";"+da.get(0).getDocument().getId();
		    		String sesKey=id.co.keriss.consolidate.util.SHA256.shaHex(dat);
		    		signSes.setSession_key(sesKey);
		    		signSes.setUsed(false);
		    			
		    		DocSigningSession dcSgn=new DocSigningSession();
		    		dcSgn.setDocument(da.get(0).getDocument());
		    		dcSgn.setSigningSession(signSes);
		    				    		
		    		Session ss=db.session();
		    		Transaction t=ss.beginTransaction();
		    		Long idSes=(Long) ss.save(signSes);
		    		ss.save(dcSgn);
		    		t.commit();
		    		
		    		JSONObject link=new JSONObject();
					 link.put("sessionKey", sesKey);
					 link.put("sessionID", idSes.toString());
					 link.put("refTrx", stringDate);
					 
					 JSONObject jo=new JSONObject();
					 jo.put("result", "00");
		    		if(viewOnly) {
		    			jo.put("link", "https://"+DOMAINAPIWV+"/viewpage.html?view="+URLEncoder.encode(AESEncryption.encryptId(link.toString()),"UTF-8"));

		    		}else {
		    			jo.put("link", "https://"+DOMAINAPIWV+"/signingpage.html?sgn="+URLEncoder.encode(AESEncryption.encryptId(link.toString()),"UTF-8"));
		    		}
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//		    		LogSystem.response(request, jo);
//			         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
			         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
					//System.out.println(dataUser.toString());
//					LogSystem.info(request, "Data User" + dataUser.toString());
//					context.put("locSign", dataUser);
//					context.put("by_pass_click", by_pass_click);
//					context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
//					context.put("title_doc", da.get(0).getDocument().getFile_name());
//					context.put("usersign", dataUser);
//					context.put("statusSign", statusSign);
//					context.put("visible", visible);
//					context.put("size", uL.length());
//					context.put("username", login.getUsername());
//					context.put("email", usr.getNick().toLowerCase());
//				    LogSystem.info(request, "Open Document ID : "+idDoc);
				}
				else {
					
					PreRegistration preReg=new PreRegistrationDao(db).findEmail(userEmail);
					
					if(preReg==null) {
						 JSONObject jo=new JSONObject();
						 jo.put("result", "05");
						 jo.put("notif", "user tidak ditemukan");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						return;
					}else {
						JSONObject jo=new JSONObject();
						 jo.put("result", "05");
						 jo.put("notif", "user belum melakukan aktivasi, silakan melakukan aktivasi melalui email");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						return;
					}
				}
        
	         
	         context.put("link", "https://"+LINK);
	         context.put("domain", "https://"+DOMAINAPI);
	         context.put("domainwv", "https://"+DOMAINAPIWV);
	         context.put("webdomain", "https://"+DOMAIN);


		}catch (Exception e) {
			LogSystem.error(request, e.toString(), refTrx);
//	         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, ExceptionUtils.getStackTrace(e), idTrx, "ERROR"));
//	         Logger.getLogger(getClass()).error(e);

            e.printStackTrace();
            try {  
			 JSONObject jo=new JSONObject();
			 jo.put("result", "05");
			 jo.put("notif", "error");
			 jo.put("error", e.getMessage());
//			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//	         Logger.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
	         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
	}	
		
		
}
