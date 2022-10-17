package apiMitra;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

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
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;

public class BulkSignWebViewMitraDocSession extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Random rand = new Random();
		int i=0;
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		int number = rand.nextInt(99999999);
		String code = String.format("%06d", number);
		String refTrx="BULKWVPAGE"+sdfDate2.format(tgl).toString();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		Boolean by_pass_click=false;
		int statusSign=0;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		
		String path_app = this.getClass().getName();
		String CATEGORY = "BULKSIGN";
		String email_req ="";
		String mitra_req ="";
		
		
		HttpServletRequest request = context.getRequest();
		
//		LogSystem.info(request, "DATA DEBUG :" + (i++), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		//System.out.println("DATA DEBUG :"+(i++));
		
		Mitra mitra=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				PrintWriter outStream = null;
				try {
					outStream = context.getResponse().getWriter();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}


				// no multipart form
				if (!isMultipart) {
					jsonString=request.getParameter("jsonfield");
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
						
					}
				}

			 String encryptedsgn = request.getParameter("sgn").toString();
			
			 AESEncryption AES = new AESEncryption();
			 String hasildecrypt = AES.decryptSession(encryptedsgn);
			 
			 JSONObject jsonDecrypt = new JSONObject(hasildecrypt);
			 
			 if(jsonDecrypt.has("refTrx"))
			 {
				 refTrx = refTrx+"/"+jsonDecrypt.getString("refTrx");
			 }
			 
			 LogSystem.info(request, "Masuk Webview Bulksign", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
			 LogSystem.info(request, "SGN :"+encryptedsgn, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
			 LogSystem.info(request, "DCRYPT :"+hasildecrypt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			 LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
//	         LogSystem.info(request, jsonString, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "REQUEST", (System.currentTimeMillis() - start) / 1000f + "s");
			 
	         if(request.getParameter("app")!=null) {
	        	 context.put("app", request.getParameter("app"));
	         }
			
			 if (!jsonDecrypt.has("sessionKey")) 
			 { 
				 LogSystem.error(request,"Parameter Session key tidak ditemukan", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s"); 
				 context.getResponse().sendError(403);
				 return; 
			 }
			 if (!jsonDecrypt.has("sessionID")) 
			 { 
				 LogSystem.error(request,"Parameter Session ID tidak ditemukan", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s"); 
				 context.getResponse().sendError(403);
			 	 return; 
			 }
			  
			 String session_key=jsonDecrypt.getString("sessionKey");
			 String session_id=jsonDecrypt.getString("sessionID");
			 
			
			 LogSystem.info(request, "id :"+session_id, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 LogSystem.info(request, "key :"+session_key, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
			 // TODO: handle exception query userid dari dokumen, document_id dari doc_sign, email_user dari eeuser signing_session
			 // userid, document, dan email string
			  
//			 LogSystem.info(request,"SAMPAI SINI");
			 DB db = null;
			 try {
				 	db = getDB(context);
			 }
			 catch(Exception e) {
					e.printStackTrace();
			 }
			 UserManager user = null;
			 try {
				 	user=new UserManager(db);
			 }
			 catch(Exception e) {
					e.printStackTrace();
			 }
			 
			 // TODO: useremail
			 SigningSession eeuserStrings = user.findStringBySession_Keyid(session_key,session_id);
			 //LogSystem.info(request, "eeusernya user :"+eeuserString);
			 long eeuserString = eeuserStrings.getEeuser().getId();
			 LogSystem.info(request, "eeusernya user :"+eeuserString, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 User emaileeusers = user.findByeeuser2(eeuserString);
			 String emaileeuser = emaileeusers.getNick(); //email user di string
			 LogSystem.info(request, "EMAILNYA USER :"+emaileeuser, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
			 mitra_req = eeuserStrings.getMitra().getName();
			 email_req = emaileeusers.getNick();
			 
			 String userEmail = emaileeuser;
			 User usr= user.findByUsernameSign(userEmail);
			 context.put("nohp", usr.getUserdata().getNo_handphone());
			 
			 context.put("email", emaileeuser);
			 List<DocSigningSession> documentIDs = null;
			 long documentIDa ;
			 String documentIDe = null;
			 //OTP
			 JSONObject dataUser=new JSONObject();
			 dataUser.put("ptype", AESEncryption.encryptIdpreregis(usr.getUserdata().getNo_handphone()));
			 //dataUser.put("idmitra", usr.getMitra().getId());Ambil dari Signing_session
			 String encidmitra = AESEncryption.encryptDoc(eeuserStrings.getMitra().getId().toString());
			 String enceeuser = AESEncryption.encryptIdpreregis(String.valueOf(usr.getId()));
			 dataUser.put("type", encidmitra);
			 dataUser.put("utype", enceeuser);
			 dataUser.put("tstmp", usr.getId());
			 dataUser.put("refTrx", refTrx);
			 dataUser.put("sessionid", session_id);
			 
      		 Documents eeuseruserids = null;
      		 //long eeuseruserid;
      		 //User userida = null;
      		 String userid  = null;
      		 String emailadmin = null;
			 //SigningSessionManager euser=null;
      		DocSigningSession docmnt = null;
      		DocumentsAccessDao dao = new DocumentsAccessDao(db);
      		List<DocumentAccess> dam = new ArrayList<DocumentAccess>();
			 try {
				 documentIDs = user.findDocumentBulkSign(session_id);
				 if(documentIDs.size() < 1)
				 {
					 LogSystem.info(request, "Tidak ada dokumen yang perlu ditandatangani", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 try {
							context.getResponse().sendError(400);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
				 }
				 
				 for (int b = 0 ; b < documentIDs.size() ; b++)
				 {
					 List<DocumentAccess> docAccess = new ArrayList<DocumentAccess>();
					 docAccess = dao.findDocAccessMeterai(documentIDs.get(b).getDocument().getId());
					 if(docAccess.size() > 0)
					 {
						 dam.add(docAccess.get(0));
					 }
				 }
				 
				 if(dam.size()>0)
				 {
					 LogSystem.info(request, "Jumlah dokumen bermeterai " + dam.size(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 }
				
//				 LogSystem.info(request, "brooo ");
				 context.put("listDocument", documentIDs);
				 context.put("meterai", dam);
				 
				 dataUser.put("idDoc", documentIDs.get(0).getDocument().getId());
				 context.put("useruser", dataUser);
				 
				 docmnt = user.findDocMitra(session_id);
				 emailadmin = docmnt.getDocument().getEeuser().getNick();
				 context.put("userid", emailadmin);
				 
			 }
			 catch(Exception e) {
					e.printStackTrace();
					//LogSystem.error(getClass(), e);
			 }
			 
			// TODO: handle exception cek di query ini
			 
			 
			 SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
			 Date datey = new Date();
			 Date today = datey;
			 Date expire = eeuserStrings.getExpire_time();
			 boolean used = eeuserStrings.isUsed();
			 if(today.after(expire) ) {
				 
				 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	 context.getResponse().sendError(408);
				 return;
			 }
			 if (used == true) {
				 LogSystem.error(request, "Session has already Used", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	 context.getResponse().sendError(401);
				 return;
			 }
			

			context.put("locSign", dataUser);
			if(usr.getLogin()!=null) {
				//System.out.println("login ada bro ");
				LogSystem.info(request, "Login tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				context.put("username", usr.getLogin().getUsername());
			}
			
			 // Cek sertifikat user ke KMS
			 KmsService kms = new KmsService(request, "KMS Service/"+refTrx, CATEGORY, start, mitra_req, email_req);
			 JSONObject cert = new JSONObject();
		
			 LogSystem.info(request, "Cek mitra : " + emaileeusers.getMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 if(emaileeusers.getMitra() != null)
			 {
				 LogSystem.info(request, "Mitra tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 cert = kms.checkSertifikat(emaileeusers.getId(), emaileeusers.getUserdata().getLevel(), emaileeusers.getMitra().getId().toString());
			 }
			 else 
			 {
				 LogSystem.info(request, "Mitra null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 cert = kms.checkSertifikat(emaileeusers.getId(), emaileeusers.getUserdata().getLevel(), "");
			 }
			 
			 if(!cert.has("result"))
			 {									
				 context.getResponse().sendError(408);
				 return;
			 }
			 
			 if(cert.getString("result").length() >3 || cert.getString("result").equals(""))
			 {
				 context.getResponse().sendError(408);
    			 return; 
			 }
		
			 LogSystem.info(request, "Hasil cek sertifikat : " + cert, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 context.put("mass_revoke", DSAPI.MASS_REVOKE);
			 context.put("link", "https://"+LINK);
	         context.put("domain", "https://"+DOMAINAPI);
	         context.put("domainwv", "https://"+DOMAINAPIWV);
	         context.put("webdomain", "https://"+DOMAIN);
	         context.put("webbulksign", "https://"+WEB_BULKSIGN);
	         context.put("refTrx", refTrx);
	         context.put("KPSE", DSAPI.KPSE);
	         context.put("KP", DSAPI.KP);
	         context.put("rand", number);
	         
	         if(cert.getString("result").equals("00"))
	         {
	        	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	             String raw = cert.getString("expired-time");
	             Date expired = sdf.parse(raw);
	             Date now= new Date();
	             Long day = TimeUnit.DAYS.convert(expired.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
	             LogSystem.info(request, "Jarak ke waktu expired : " + day, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	             if(usr.getUserdata().getLevel().equals("C2"))
	        	 {
	        		 context.put("cert", "08");
	        	 }
	             else
	             {
	            	 if (cert.has("needRegenerate"))
		             {
		            	 if (!cert.getBoolean("needRegenerate")) 
			             {
			            	 if(day < 30)
				        	 {
				        		 context.put("cert", "03");
				        	 }
				        	 else
				        	 {
				        		 context.put("cert", "00");
				        	 }
			             }
			             else
			             {
			            	 context.put("cert", "04");
			             }
		             }
		             else
		             {
		            	 if(day < 30)
			        	 {
			        		 context.put("cert", "03");
			        	 }
			        	 else
			        	 {
			        		 context.put("cert", "00");
			        	 }
		             }
	             }
	             
	         }
	         else
	         {
	        	 context.put("cert", cert.getString("result"));
	         }

	         
	         if(docmnt.getDocument().getEeuser().getMitra().getSigning_redirect() == null)
	         {
	        	 context.put("rlink", 0);
	         }
	         else
	         {
		         if(docmnt.getDocument().getEeuser().getMitra().getSigning_redirect()!=null)
		         {
//		        	 context.put("rlink", emaileeusers.getUserdata().getMitra().getSigning_redirect());
		        	 context.put("rlink", docmnt.getDocument().getEeuser().getMitra().getSigning_redirect());
		         }
		         else
		         {
		        	 context.put("rlink", 0);
		         }
	         }
			//String res="";
			//if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			//else res="<b>ERROR 404</b>";
	         LogSystem.info(request, "Proses cek selesai", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "RESPON", (System.currentTimeMillis() - start) / 1000f + "s");
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
            try {
				context.getResponse().sendError(400);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}	
	
	public static String shuffle(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }
}