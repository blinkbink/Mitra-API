package apiMitra;

//import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.InitialDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SystemUtil;
import id.sni.digisign.filetransfer.Samba;

public class SignWebViewMitraDocSession extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Random rand = new Random();
//		int i=0;
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		int number = rand.nextInt(99999999);
		String code = String.format("%06d", number);
		String refTrx="SPAGE"+sdfDate2.format(tgl).toString();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		Boolean by_pass_click=false;
		int statusSign=0;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		String path_app = this.getClass().getName();
		String CATEGORY = "SIGN";
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
			 
			 LogSystem.info(request, "Masuk Webview proses tandatangan", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 LogSystem.info(request, "SGN :"+encryptedsgn, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 LogSystem.info(request, "DCRYPT :"+hasildecrypt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			 LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
	         LogSystem.info(request, jsonDecrypt.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "REQUEST", (System.currentTimeMillis() - start) / 1000f + "s");
			 
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
			 LogSystem.info(request, "id :"+session_id, refTrx , path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
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
					LogSystem.error(request, e.getMessage(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 }
			 UserManager user = null;
			 try {
				 	user=new UserManager(db);
			 }
			 catch(Exception e) {
					e.printStackTrace();
					LogSystem.error(request, e.getMessage(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 }
			 
			 // TODO: useremail
			 SigningSession eeuserStrings = user.findStringBySession_Keyid(session_key,session_id);
			 //LogSystem.info(request, "eeusernya user :"+eeuserString);
			 long eeuserString = eeuserStrings.getEeuser().getId();
			 
			 User emaileeusers = user.findByeeuser2(eeuserString);
			 String emaileeuser = emaileeusers.getNick(); //email user di string
			 
			 mitra_req = eeuserStrings.getMitra().getName();
			 email_req = emaileeuser;
			 
			 LogSystem.info(request, "eeusernya user :"+eeuserString, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 LogSystem.info(request, "EMAILNYA USER :"+emaileeuser, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
			 DocSigningSession documentIDs = null;
			 long documentIDa ;
			 String documentIDe = null;
			 
      		 Documents eeuseruserids = null;
      		 long eeuseruserid;
      		 User userida = null;
      		 String documentName = null;
      		 String userid  = null;
			 //SigningSessionManager euser=null;
			 try {
				 documentIDs = user.findDocBySession_Keyid(session_id);
//				 LogSystem.info(request, "brooo ");
	      		 documentIDa = documentIDs.getDocument().getId();
	      		 documentName = documentIDs.getDocument().getFile_name();
	      		 documentIDe = documentIDs.getDocument().getId().toString();//d_id di string
	      		 LogSystem.info(request, "Document_id :"+documentIDe, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	      		 
				 eeuseruserids = user.findOwnereeuserByDocument(documentIDa);
				 eeuseruserid = eeuseruserids.getEeuser().getId();
				 
				 LogSystem.info(request, "EEUSERNYA USER ID :"+eeuseruserid, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 userida = user.findOwnernickByDocument(eeuseruserid);
				 
				 userid = userida.getNick().toLowerCase();//userid di string
				 LogSystem.info(request, "EMAIL USERID :"+userid, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				  
			 }
			 catch(Exception e) {
					e.printStackTrace();
					LogSystem.error(request, e.getMessage(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					//LogSystem.error(getClass(), e);
			 }
			 
			// TODO: handle exception cek di query ini
			 
			 User eeuser=user.findByUsername(userid);
			 SigningSession session = null;
			 long mitraid = 0;
			 try { 
				 	session = user.findMitraSigningSession(session_key,session_id);
				 	mitraid = session.getMitra().getId();
				 	LogSystem.info(request, "Mitra ID :"+mitraid, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 	mitra = user.findMitrafromSigningSession(mitraid);
			 } 
			 catch(Exception e) {
				 LogSystem.error(request, e.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					e.printStackTrace();
					//LogSystem.error(getClass(), e);
			 }
			 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			 
//			 DocumentAccess sequence = user.findSequenceByDocument(documentIDe, emaileeuser);
//			 int Order = sequence.getSequence_no();
//			 int smallest_seq = user.findSmallestSequenceByDocument(documentIDe);
//			 int Result = Order - 1; 
//			 boolean seq = eeuseruserids.isSequence();
//			 LogSystem.info(request, "APAKAH SEQUENCE : "+seq);
//			 if (seq==true) {
//				 LogSystem.info(request, "SEQUENCE NIH");
//				 if(Result >= smallest_seq) {	
//				 	DocumentAccess sequence_before = user.findSequenceBeforeByDocument(documentIDe, Result);
//					//boolean before = sequence_before.isFlag();
//				 	//LogSystem.info(request, "APAKAH SEQUENCE SEBELUM SUDAH : "+before);
//					if (sequence_before != null)
//					{
//						LogSystem.error(request, "Not Your Turn to Sign The Document");
//				    	context.getResponse().sendError(403);
//						return;
//					 }
//				 }
//			 }
			 DocumentAccess sequence = user.findSequenceByDocument(documentIDe, emaileeuser);
			 boolean approvaltype = false;
			 
			 int Order = sequence.getSequence_no();
			 Long giliran = Long.valueOf(Order);
//			 int smallest_seq = user.findSmallestSequenceByDocument(documentIDe);
			 int smallest_seq = eeuseruserids.getCurrent_seq();
			 int Result = Order; 
			 boolean seq = eeuseruserids.isSequence();
			 LogSystem.info(request, "APAKAH SEQUENCE : "+seq, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 if (seq==true) {
				 if(user.findSequenceByDocumentApproval(documentIDe, emaileeuser)!=null && user.findSequenceByDocumentSign(documentIDe, emaileeuser)!=null) {
					 int seqsign=user.findSequenceByDocumentSign(documentIDe, emaileeuser).getSequence_no();
					 LogSystem.info(request, "seqsign: "+seqsign, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 int seqapp=user.findSequenceByDocumentApproval(documentIDe, emaileeuser).getSequence_no();
					 LogSystem.info(request, "seqapp: "+seqapp, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 if(seqsign==seqapp) {
						 approvaltype = false;
						 LogSystem.info(request, "approvaltype: "+approvaltype, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");//sequence first
					 }else if(user.findSequenceByDocumentSign(documentIDe, emaileeuser).getSequence_no() > user.findSequenceByDocumentApproval(documentIDe, emaileeuser).getSequence_no()) // cek kalo app dulu
					 {
						 approvaltype = true;
						 LogSystem.info(request, "approvaltype: "+approvaltype, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 }
				 }else if(user.findSequenceByDocumentApproval(documentIDe, emaileeuser)!=null){
					 approvaltype = true;
					 LogSystem.info(request, "approvaltype: "+approvaltype, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 }
				 LogSystem.info(request, "SEQUENCE NIH", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 LogSystem.info(request, "RESULT >>"+Result, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 LogSystem.info(request, "Nilai Terkecil >>"+smallest_seq, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 
				 if(Result > smallest_seq) {	
				 	DocumentAccess sequence_before = user.findSequenceBeforeByDocument(documentIDe, Result);
					//boolean before = sequence_before.isFlag();
				 	//LogSystem.info(request, "APAKAH SEQUENCE SEBELUM SUDAH : "+before);
					if (sequence_before != null)
					{
						LogSystem.error(request, "Not Your Turn to Sign The Document", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    	context.getResponse().sendError(406);
						return;
					}
				 }
			 }
			 SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
			 Date datey = new Date();
//			 System.out.println(formatter.format(datey));
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
			 if(mitra==null) {
				 LogSystem.error(request, "Wrong Session", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");//kalo mitra gak nemu bisa jadi salah id atau key salah berati salah kode
		         context.getResponse().sendError(403);
				 return;
			 } 
	         User usermitra=eeuser;//hasil eeuser
	         
			 String documentID=documentIDs.getDocument().getIdMitra();
	    	 
			 DocumentsDao dd = new DocumentsDao(db);
			 LoginDao ldao=new LoginDao(db);
			 Documents doc=null;
			 List<Documents> ldocs;
					try {
						//doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
						ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
						if(ldocs.size()==0) {
							LogSystem.error(request, "Dokumen size 0", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							context.getResponse().sendError(404);
				        	return;
						}
						else {
							doc=ldocs.get(0);
						}
					} catch (Exception e) {
						// TODO: handle exception
						LogSystem.error(request, "Error dokumen", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						e.printStackTrace();
						context.getResponse().sendError(404);
			        	return;
					}
					
					String idDoc=null;
					Boolean flag = null;
					Boolean approval = false;
					Boolean cekapproval = false;
					//String userEmail=jsonRecv.getString("email_user").toLowerCase(); 
					
					String userEmail = emaileeuser;
					// TODO: handle exception setting buat BYPASS
					if(jsonDecrypt.has("by-pass-click"))
					{
						by_pass_click = jsonDecrypt.getBoolean("by-pass-click");//AUTO-SIGN
					}

					Long idmitra = usermitra.getId();
					//UserManager um=new UserManager(db);
					//System.out.println(userEmail);
					LogSystem.info(request, userEmail, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					User usr= user.findByUsernameSign(userEmail);
					// TODO: handle exception cari eeuser string
					//System.out.println("user nya adalah = "+usr.getName());
					if(usr!=null) {
						/*
						id.co.keriss.consolidate.ee.Login login=ldao.getUsername(usr.getNick());
						if(login==null) {
							PrintWriter outStream=context.getResponse().getWriter();
			        		outStream.write(403);
			        		outStream.close();
			        		return;
						}
						*/
						DocumentsAccessDao docDao=new DocumentsAccessDao(db);
						DocumentAccess aq = null;
						boolean kosong = false;
						List<DocumentAccess> da=null;
						List<DocumentAccess> dam=null;
						if(approvaltype) {
							List<DocumentAccess> dap=docDao.findDocAccessEEuserByMitraApproval(documentID, idmitra, userEmail, doc.getId(),giliran);
							LogSystem.info(request, "idAccess: "+dap.get(0).getId().toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							LogSystem.info(request, "idDoc: "+dap.get(0).getDocument().getId().toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							aq=dap.get(0);
							LogSystem.info(request, "Proses Approval", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							cekapproval = true;
							da=dap;
						}else {
							 da = docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
							 dam = docDao.findDocAccessMeterai(doc.getId());
							 LogSystem.info(request, "Meterai Dokumen " + dam.size(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							 LogSystem.info(request, "Bukan Proses Approval", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							 if (da.size() <= 0) {
									LogSystem.error(request, "Dokumen size 0", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									context.getResponse().sendError(404);
									return;
								}
						}
						
						//check level user < 3 dan level mitra > 2
						int leveluser=Integer.parseInt(usr.getUserdata().getLevel().substring(1));
			        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
			        	if(levelmitra>2 && leveluser<3) {
			        		LogSystem.error(request, "level user tidak diperbolehkan untuk mitra ini", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		context.getResponse().sendError(403);
			        		return;
			        	}
						
						//System.out.println("TES SSSSSSSSSSSSSSSSSSS :"+usr.getUserdata().getImageTtd());
						String id=AESEncryption.encryptDoc(String.valueOf(da.get(0).getDocument().getEeuser().getId()));
						String namafile=AESEncryption.encryptDoc(da.get(0).getDocument().getSigndoc());
						String usingopt[] = da.get(0).getDocument().getPath().split("/");
						LogSystem.info(request, "useopt = " + usingopt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						String optget = "";
						optget = AESEncryption.encryptDoc(usingopt[1]);
						LogSystem.info(request, "id = " + da.get(0).getDocument().getEeuser().getId(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, "namafile = " + da.get(0).getDocument().getSigndoc(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						//System.out.println("id="+id);
						//System.out.println("namafile="+namafile);
						//context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
						context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8")+"&tp="+URLEncoder.encode(optget, "UTF-8")+"&ss="+URLEncoder.encode(SystemUtil.getExp(namafile,expire), "UTF-8")+"&refTrx="+URLEncoder.encode(refTrx, "UTF-8")+"&mr="+URLEncoder.encode(AESEncryption.encryptDoc(mitra_req), "UTF-8")+"&er="+URLEncoder.encode(AESEncryption.encryptDoc(email_req), "UTF-8")+"&ca="+URLEncoder.encode(AESEncryption.encryptDoc(CATEGORY), "UTF-8"));
						//System.out.println("https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
						//System.out.println("https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile);
						LogSystem.info(request, "https://"+DOMAINAPIWV+"/dt02.html/?id="+id+"&doc="+namafile+"&tp="+optget, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						////File img=new File(usr.getUserdata().getImageTtd());
						//File img=new File(usr.getI_ttd());
						
						
//						boolean isopt=usr.getUserdata().getImageTtd().startsWith("/opt");
//						LogSystem.info(request, "is /OPT ?"+isopt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//						byte[] encoded ;
//						if (isopt) {
//							encoded = Base64.encode(smb.openfile(usr.getUserdata().getImageTtd()));
//						}else {
//							encoded = Base64.encode(smb.openfile_NAS(usr.getUserdata().getImageTtd()));
//						}

						Samba smb=new Samba(refTrx, request, mitra_req,  email_req,  CATEGORY,  start);
						byte[] encoded ;
						try {
							encoded=Base64.encode(smb.openfile(usr.getUserdata().getImageTtd()));
						}catch (Exception e) {
				      	     log.error(LogSystem.getLog( ExceptionUtils.getStackTrace(e), refTrx, "ERROR"));
				      	     smb.close();
				      	     throw e;
						}finally {
							smb.close();
						}
						int visible=1;
						JSONObject dataUser=new JSONObject();
						dataUser.put("key_session", session_key);
						dataUser.put("id_session", session_id);
						dataUser.put("refTrx", refTrx);
					
						//dataUser.put("idDoc", idDoc);
						JSONArray uL=new JSONArray();
						LogSystem.info(request, "isApproval? = " +cekapproval , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						if (!cekapproval) {
							for (DocumentAccess dAcs : da) {
								try {
									if (dAcs.getEeuser() != null) {
										dAcs.setEeuser(usr);
										docDao.update(dAcs);
									}
								} catch (Exception e) {
									// TODO: handle exception
								}

								//							idDoc = String.valueOf(dAcs.getDocument().getId());
								idDoc = documentIDe;
								flag = dAcs.isFlag();
								if (((dAcs.getType().equals("approval")))) {
									approval = true;
									break;
								}
								if (((dAcs.getType().equals("sign") || dAcs.getType().equals("initials"))
										&& dAcs.isFlag() == true)) {
									statusSign = 1;
									continue;
								}
								if (!dAcs.isRead()) {
									dAcs.setRead(true);
									docDao.update(dAcs);
								}
								if (dAcs.getType().equals("share")) {
									continue;
								}

								if (dAcs.getType().equalsIgnoreCase("initials")) {
									String sgn = "0";
									if (!dAcs.isVisible()) {
										visible = 1;
										sgn = "1";
									}
									InitialDao idao = new InitialDao(db);
									List<Initial> li = idao.findInitialByDocac(dAcs.getId());
									for (Initial in : li) {
										JSONObject us = new JSONObject();
										us.put("idAccess", dAcs.getId().toString());
										us.put("lx", in.getLx());
										us.put("ly", in.getLy());
										us.put("rx", in.getRx());
										us.put("ry", in.getRy());
										us.put("page", in.getPage());
										us.put("type", dAcs.getType());
										us.put("sgn", sgn);
										uL.put(us);
									}
									continue;
								}

								JSONObject us = new JSONObject();
								us.put("idAccess", dAcs.getId().toString());
								us.put("lx", dAcs.getLx());
								us.put("ly", dAcs.getLy());
								us.put("rx", dAcs.getRx());
								us.put("ry", dAcs.getRy());
								us.put("page", dAcs.getPage());
								us.put("type", dAcs.getType());
								us.put("sgn", "0");

								if (!dAcs.isVisible()) {
									visible = 0;
									us.put("sgn", "1");
								}
								uL.put(us);

							} 
						}else {
							
							if (!aq.isRead()) {
								aq.setRead(true);
								docDao.update(aq);
							}
							idDoc = documentIDe;
							flag = aq.isFlag();
							if (((aq.getType().equals("approval")))) {
								approval = true;
							}	
							JSONObject us = new JSONObject();
							us.put("idAccess", aq.getId().toString());
							us.put("sgn", "0");
							uL.put(us);
							LogSystem.info(request, "Approval? = " +approval , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						}
						
						//Update last proses dokumen
//						DocumentsDao ddao = new DocumentsDao(db);
//						Documents docLast = ddao.findByidDoc(Long.valueOf(idDoc));
//						docLast.setLast_proses(new Date());
//						new DocumentsDao(db).update(docLast);
						
						dataUser.put("idDoc", idDoc);
						dataUser.put("user", uL);
						dataUser.put("statustypeapproval", approval);
						dataUser.put("refTrx", refTrx);
						String AvailUsername = "";
						int value = rand.nextInt(50000); 
						//System.out.println(dataUser.toString());
						LogSystem.info(request, "Data User = " + dataUser.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						context.put("approval", approval);//////////////////////////////////fitur Approval New
						context.put("flag", flag);
						context.put("by_pass_click", by_pass_click);
						context.put("meterai", dam.size());
						context.put("rand", value);
						context.put("locSign", dataUser);
						context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
						context.put("title_doc", da.get(0).getDocument().getFile_name());
						context.put("usersign", dataUser);
						context.put("statusSign", statusSign);
						context.put("visible", visible);
						context.put("size", uL.length());
						//context.put("username", login.getUsername());
						if(usr.getLogin()!=null) {
							//System.out.println("login ada bro ");
							LogSystem.info(request, "Login tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							context.put("username", usr.getLogin().getUsername());
							AvailUsername = usr.getLogin().getUsername().toString();
						}
						
						context.put("email", usr.getNick().toLowerCase());
						//context.put("pwd_user", pwd_user);
						context.put("mitra", String.valueOf(mitra.getId()));
						context.put("statususer", usr.getStatus());
						context.put("nohp", usr.getUserdata().getNo_handphone());
						context.put("iduser", usr.getId());
						
						String username="";
						String pickUsername="";

						if(usr.getStatus()=='1') {
							if(usr.getLogin()==null) {
								LogSystem.info(request, "Login null create new username", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								id.co.keriss.consolidate.ee.Login lgn = null;
						        String nama = usr.getName().toLowerCase();
						        String character = "- ";
						        String nameSplit[] = nama.split(" ");
						        String date=new SimpleDateFormat("ddMMYY").format(new Date());
	
						        int go = 0;
						        int a = 0;
						        int b = 0;
						        int c = 0;
						        
						        while (go < 3)
						        {
						        	pickUsername = nameSplit[rand.nextInt(nameSplit.length)]+shuffle((date+usr.getUserdata().getNo_identitas()+usr.getId())+"_").substring(0,rand.nextInt(6));
						        	lgn=ldao.getByUsername2(pickUsername);
						        	
						        	if(pickUsername.length() > 5)
						        	{
						        		a = 1;
						        	}
						        	if(lgn == null)
						        	{
						        		b = 1;
						        	}
						        	if(pickUsername.charAt(pickUsername.length() - 1) != '_' && pickUsername.charAt(0) != '_')
						        	{
						        		c = 1;
						        	}
						        	go = a + b + c;
						        }
						        
						        LogSystem.info(request, "Found unique username: " + pickUsername, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				                username=pickUsername;
	
						        
	//					        do{
	//					        	pickUsername = nameSplit[rand.nextInt(nameSplit.length)]+shuffle((date+usr.getUserdata().getNo_identitas()+usr.getId())+"_").substring(0,rand.nextInt(6));
	//					        	lgn=ldao.getByUsername2(pickUsername);
	//					        	
	//					            //System.out.println("Generate: "+pickUsername);
	//					        	LogSystem.info(request, "Generate: "+pickUsername);
	//					            if (lgn == null)
	//					            {
	//					            	LogSystem.info(request, "Found unique username: "+pickUsername);
	//					                //System.out.println("Found unique username: "+pickUsername);
	//					                username=pickUsername;
	//					            }
	//					        }while(lgn != null && pickUsername.length() <= 6);
						        
						        //System.out.println("usernamenya adalah = "+username);
						        LogSystem.info(request, "Usernamenya adalah = "+username, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	//							String nama=usr.getName().replaceAll("\\s", "");
	//							
	//							String ambil=new SimpleDateFormat("ddMMyyyy").format(usr.getUserdata().getTgl_lahir());
	//							if(nama.length()<6) {
	//								username=nama+ambil.substring(0, 4);
	//							} else {
	//								username=nama.substring(0,6)+ambil.substring(0, 4);
	//							}
	//							id.co.keriss.consolidate.ee.Login lgn=ldao.getByUsername(username);
	//							if(lgn!=null)username=username+ambil.substring(4);
	//							System.out.println("usernamenya adalah = "+username);
							}else {
								username = AvailUsername;
								LogSystem.info(request, "Usernamenya adalah = "+username, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							}
							
						}
						context.put("newusername", username);
						context.put("file_name", documentName);
					    LogSystem.info(request, "Open Document ID : "+idDoc, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					}
					else {
						LogSystem.info(request, "User null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						context.getResponse().sendError(401);
			        	
						return;
					}
	         //}
					
			// Cek sertifikat user ke KMS
			 KmsService kms = new KmsService(request, "KMS Service/"+refTrx, CATEGORY, start, mitra_req, email_req);
			 JSONObject cert = new JSONObject();
		
			 LogSystem.info(request, "Cek mitra : " + usr.getMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 if(usr.getMitra() != null)
			 {
				 LogSystem.info(request, "Mitra tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 cert = kms.checkSertifikat(usr.getId(), usr.getUserdata().getLevel(), usr.getMitra().getId().toString());
			 }
			 else 
			 {
				 LogSystem.info(request, "Mitra null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 cert = kms.checkSertifikat(usr.getId(), usr.getUserdata().getLevel(), "");
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
	        		 context.put("cert", "00");
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
	         LogSystem.info(request, "Dapat melanjutkan membuka halaman webview", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
            LogSystem.error(request, e.getMessage(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
            try {
				context.getResponse().sendError(400);
			} catch (IOException e1) {
				e1.printStackTrace();
				LogSystem.error(request, e1.getMessage(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
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