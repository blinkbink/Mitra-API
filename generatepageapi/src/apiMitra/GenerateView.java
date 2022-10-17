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

public class GenerateView extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date idTrx=new Date();

		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="GEN-SIGNING-PAGE"+sdfDate2.format(idTrx).toString();

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
				
				TokenMitraDao tmd=new TokenMitraDao(getDB(context));
				String token=request.getHeader("authorization");
				if(token!=null) {
					String[] split=token.split(" ");
					if(split.length==2) {
						if(split[0].equals("Bearer"))token=split[1];
					}
					TokenMitra tm=tmd.findByToken(token.toLowerCase());
					if(tm!=null) {
//						LogSystem.info(request, "Token tidak null:" + token);
						mitratoken=tm.getMitra();
					} else {
//						LogSystem.info(request, "Token null");
						JSONObject jo=new JSONObject();
						jo.put("result", "05");
						jo.put("notif", "token salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

						return;
					}
				}else {
//					LogSystem.info(request, "Token null");
					JSONObject jo=new JSONObject();
					jo.put("result", "05");
					jo.put("notif", "token salah");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

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
//	            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

	         LogSystem.request(request, fileItems, refTrx);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         LogSystem.info(request, "JSONFile Receive : "+jsonRecv);
	         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, jsonRecv.toString(), idTrx, "RCV"));

	         if(mitratoken!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
//		    		 LogSystem.error(request, "Parameter userid tidak ditemukan");
		    		 JSONObject jo=new JSONObject();
						jo.put("result", "05");
						jo.put("notif", "userid salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

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
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

						 return;
		        	 }
		         }
		         else {
//		        	 LogSystem.info(request, "Userid tidak ditemukan");
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

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
			         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

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
//					LogSystem.info(request, "ID MITRA : ----- " + mitra.getId());
//					LogSystem.info(request, "DOCUMENT ID : ----- " + documentID);
		            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "ID MITRA : ----- " + mitra.getId(), idTrx, "LOG"));
		            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "DOCUMENT ID : ----- " + documentID, idTrx, "LOG"));

					ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
					if(ldocs.size()==0) {
						//System.out.println("Error size 0 : ----- ");
						//						LogSystem.info(request, "Error size 0");
						//						context.getResponse().sendError(404);

						JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "dokumen tidak ditemukan");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			        	return;
					}
					else {
//						LogSystem.info(request, "Size tidak 0");
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "Size tidak 0", idTrx, "LOG"));

						//System.out.println("Success size not 0 : ----- ");
						doc=ldocs.get(0);
					}
				} catch (Exception e) {
//					LogSystem.error(request, "Error dokumen");
					e.printStackTrace();
					// TODO: handle exception
//					context.getResponse().sendError(404);
			         LogManager.getLogger(getClass()).error(LogSystem.getGENSGNLog(request, ExceptionUtils.getStackTrace(e), idTrx, "SND"));
			         
					JSONObject jo=new JSONObject();
	    			 jo.put("result", "05");
	    			 jo.put("notif", "dokumen tidak ditemukan");
	    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));


					return;
				}
				
				String idDoc=null;
				String userEmail=jsonRecv.getString("email_user").toLowerCase();
				Long idmitra = usermitra.getId();
				UserManager um=new UserManager(db);
//				LogSystem.info(request, "User Email: " + userEmail);
	            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "User Email: " + userEmail, idTrx, "LOG"));

				//System.out.println(userEmail);
				User usr= um.findByUsername(userEmail);
				
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
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "level user tidak diperbolehkan untuk mitra ini", idTrx, "LOG"));

		        		 JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "level user tidak diperbolehkan untuk mitra ini");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				    	 LogSystem.response(request, jo);
				         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

		    			 return;
		        	}
					
					List<DocumentAccess> da  = null;
					DocumentsAccessDao docDao = null;
					docDao=new DocumentsAccessDao(db);

					try {
						da = docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request,  "Document ID : " + documentID, idTrx, "LOG"));
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "ID Mitra : " + idmitra, idTrx, "LOG"));
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "User Mail : " + userEmail , idTrx, "LOG"));
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, "Get Id : " + doc.getId(), idTrx, "LOG"));

						//System.out.println("Document ID : " + documentID);
						//System.out.println("ID Mitra : " + idmitra);
						//System.out.println("User Mail : " + userEmail);
						//System.out.println("Get Id : " + doc.getId());
					}catch(Exception e)
					{
						//System.out.println("Errorrnya disiniiiiiiiiiiiii : ");
//						LogSystem.info(request, "Errorrnya disiniiiiiiiiiiiii : ");
						e.printStackTrace();

				         LogManager.getLogger(getClass()).error(LogSystem.getGENSGNLog(request, ExceptionUtils.getStackTrace(e), idTrx, "SND"));
					}
					
					

					
		    		SigningSession signSes=new SigningSession();
		    		Date current=new Date();
		    		Date expire=null;
	    			expire=new Date(current.getTime()+(1000*15*60)); // 15 menit
	    			signSes.setView_only(true);
		    		
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
					 link.put("refTrx", refTrx);
					 
					 JSONObject jo=new JSONObject();
					 jo.put("result", "00");
	    			jo.put("link", "https://"+DOMAINAPIWV+"/viewpage.html?view="+URLEncoder.encode(AESEncryption.encryptId(link.toString()),"UTF-8"));

					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//		    		LogSystem.response(request, jo);
			         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

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
			
	         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, ExceptionUtils.getStackTrace(e), idTrx, "ERROR"));
//	         LogManager.getLogger(getClass()).error(e);

            e.printStackTrace();
            try {  
			 JSONObject jo=new JSONObject();
			 jo.put("result", "05");
			 jo.put("notif", "error");
			 jo.put("error", e.getMessage());
//			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
	         LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
	}	
		
		
}
