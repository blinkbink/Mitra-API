package apiMitra;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class GenerateLinkDocument extends ActionSupport implements DSAPI{

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
						JSONObject jo=new JSONObject();

						//System.out.println("Success size not 0 : ----- ");
						doc=ldocs.get(0);
						List <DocumentAccess> da = new DocumentsAccessDao(db).findDocAccessEEuserByMitra(documentID, mitra.getId(), jsonRecv.getString("email_user"), doc.getId());
						if(da==null ||da.size()==0) {

			    			 jo.put("result", "05");
			    			 jo.put("notif", "dokumen tidak ditemukan");
							
						}else {
							if(da.get(0).getEeuser()==null) {
								User signer=new UserManager(db).findByEmailMitra2(jsonRecv.getString("email_user"));
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
							
							String link = "getFile:" + doc.getId() + ":" + da.get(0).getId();
							
							jo.put("result", "00");
			    			jo.put("link", "https://"+DOMAIN+"/doc/pdf.html?enc="+URLEncoder.encode(AESEncryption.encryptDocWeb(link.toString()),"UTF-8"));
		    			}
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			            LogManager.getLogger(getClass()).info(LogSystem.getGENSGNLog(request, new JSONObject().put("JSONFile", jo).toString(), idTrx, "SND"));
				
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
