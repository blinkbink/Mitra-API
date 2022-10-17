//package apiMitra;
//
////import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Random;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang.exception.ExceptionUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.bouncycastle.util.encoders.Base64;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//
//import com.anthonyeden.lib.config.Configuration;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.InitialDao;
//import id.co.keriss.consolidate.dao.LoginDao;
//import id.co.keriss.consolidate.ee.DocSigningSession;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.Initial;
//import id.co.keriss.consolidate.ee.Mitra;
//import id.co.keriss.consolidate.ee.SigningSession;
//import id.co.keriss.consolidate.util.AESEncryption;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.LogSystem;
//import id.co.keriss.consolidate.util.SystemUtil;
//import id.sni.digisign.filetransfer.Samba;
//
//public class SignWebViewMitraActSession extends ActionSupport implements DSAPI{
//
//	static String basepath="/opt/data-DS/UploadFile/";
//	static String basepathPreReg="/opt/data-DS/PreReg/";
//	final static Logger log=LogManager.getLogger("digisignlogger");
//	
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		Random rand = new Random();
//		int i=0;
//		Date tgl= new Date();
//		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		String refTrx="SVP"+sdfDate2.format(tgl).toString();
//		String jsonString=null;
//		byte[] dataFile=null;
//		FileItem filedata=null;
//		String filename=null;
//		Boolean by_pass_click=false;
//		int statusSign=0;
//		List <FileItem> fileSave=new ArrayList<FileItem>() ;
//		List<FileItem> fileItems=null;
//		
//		HttpServletRequest request = context.getRequest();
//		
////		LogSystem.info(request, "DATA DEBUG :" + (i++),refTrx);
//		//System.out.println("DATA DEBUG :"+(i++));
//		
//		Mitra mitra=null;
//		try{
//				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//				PrintWriter outStream = null;
//				try {
//					outStream = context.getResponse().getWriter();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
////				TokenMitraDao tmd=new TokenMitraDao(getDB(context));
////				String token=request.getHeader("authorization");
////				if(token!=null) {
////					String[] split=token.split(" ");
////					if(split.length==2) {
////						if(split[0].equals("Bearer"))token=split[1];
////					}
////				}
////				
////				TokenMitra tm=tmd.findByToken(token.toLowerCase());
//////				System.out.println("token adalah = "+token);
////				if(tm!=null) {
////					LogSystem.info(request, "Tokennya tidak null : " + token);
////					mitra=tm.getMitra();
////				} else {
////					LogSystem.error(request, "Tokennya null");
////					context.getResponse().sendError(403);
////					
////					return;
////				}
//
//				// no multipart form
//				if (!isMultipart) {
//					LogSystem.info(request, "Masuk Sign Doc with Single-login Verification Email WebView",refTrx);
//					jsonString=request.getParameter("jsonfield");
//
//				}
//				// multipart form
//				else {
//					
//					
//					// Create a new file upload handler
//					ServletFileUpload upload = new ServletFileUpload(
//							new DiskFileItemFactory());
//
//					// parse requests
//					 fileItems = upload.parseRequest(request);
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
//						
//					}
//				}
//			 //String process=request.getRequestURI().split("/")[2];
//			 String encryptedsgn = request.getParameter("sgn").toString();
//			 LogSystem.info(request, "SGN :"+encryptedsgn,refTrx);
////			 System.out.println("plus");
//			 AESEncryption AES = new AESEncryption();
////			 System.out.println("minus");
//			 String hasildecrypt = AES.decryptSession(encryptedsgn);
//			 
//			 //String hasildecrypt = "{\"sessionKey\":\"543ed9259a144fc73b468ff04dc5ebf51714b2f5ff3ac37fae2ccc13ab6a1897\",\"sessionID\":\"4\"}";
//			 LogSystem.info(request, "DCRYPT :"+hasildecrypt,refTrx);
//			 JSONObject jsonDecrypt = new JSONObject(hasildecrypt);
//			
//	         //System.out.println("PATH :"+request.getRequestURI());
//			 LogSystem.info(request, "PATH :"+request.getRequestURI(),refTrx);
//			 
//	         LogSystem.request(request, fileItems, refTrx);
//	         //System.out.println("sampai LOGGGGGGG");
////	         LogSystem.info(request, "sampai LOGGGGGGG");
//			 //if(jsonString==null) return;	     
//			 
//			 
//			 //System.out.println("jsonstring= "+jsondecrypt);
//			 //LogSystem.info(request, "jsonstring= "+jsondecrypt);
//	         //JSONObject jsonRecv=new JSONObject(jsondecrypt);//.getJSONObject("JSONFile");/// ini dia inisilisasi jsonfilenya
//	         
//	         
//			
//			 if (!jsonDecrypt.has("sessionKey")) 
//			 { 
//				 LogSystem.error(request,"Parameter Session key tidak ditemukan",refTrx); 
//				 context.getResponse().sendError(403);
//				 return; 
//			 }
//			 if (!jsonDecrypt.has("sessionID")) 
//			 { 
//				 LogSystem.error(request,"Parameter Session ID tidak ditemukan",refTrx); 
//				 context.getResponse().sendError(403);
//			 	 return; 
//			 }
//			  
//			 String session_key=jsonDecrypt.getString("sessionKey");
//			 String session_id=jsonDecrypt.getString("sessionID");
//			 LogSystem.info(request, "id :"+session_id,refTrx);
//			 LogSystem.info(request, "key :"+session_key,refTrx);
//			 
//			 // TODO: handle exception query userid dari dokumen, document_id dari doc_sign, email_user dari eeuser signing_session
//			 // userid, document, dan email string
//			  
////			 LogSystem.info(request,"SAMPAI SINI");
//			 DB db = null;
//			 try {
//				 	db = getDB(context);
//			 }
//			 catch(Exception e) {
//					e.printStackTrace();
//			 }
//			 UserManager user = null;
//			 try {
//				 	user=new UserManager(db);
//			 }
//			 catch(Exception e) {
//					e.printStackTrace();
//			 }
//			 
//			 // TODO: useremail
//			 SigningSession eeuserStrings = user.findStringBySession_Keyid(session_key,session_id);
//			 //LogSystem.info(request, "eeusernya user :"+eeuserString);
//			 long eeuserString = eeuserStrings.getEeuser().getId();
//			 LogSystem.info(request, "eeusernya user :"+eeuserString,refTrx);
//			 User emaileeusers = user.findByeeuser2(eeuserString);
//			 String emaileeuser = emaileeusers.getNick(); //email user di string
//			 LogSystem.info(request, "EMAILNYA USER :"+emaileeuser,refTrx);
//			 
//			 DocSigningSession documentIDs = null;
//			 long documentIDa ;
//			 String documentIDe = null;
//			 
//      		 Documents eeuseruserids = null;
//      		 long eeuseruserid;
//      		 User userida = null;
//      		 String documentName = null;
//      		 String userid  = null;
//			 //SigningSessionManager euser=null;
//			 try {
//				 documentIDs = user.findDocBySession_Keyid(session_id);
////				 LogSystem.info(request, "brooo ");
//	      		 documentIDa = documentIDs.getDocument().getId();
//	      		 documentName = documentIDs.getDocument().getFile_name();
//	      		 documentIDe = documentIDs.getDocument().getId().toString();//d_id di string
//	      		 LogSystem.info(request, "Document_id :"+documentIDe,refTrx);
//	      		 
//				 eeuseruserids = user.findOwnereeuserByDocument(documentIDa);
//				 eeuseruserid = eeuseruserids.getEeuser().getId();
//				 
//				 LogSystem.info(request, "EEUSERNYA USER ID :"+eeuseruserid,refTrx);
//				 userida = user.findOwnernickByDocument(eeuseruserid);
//				 
//				 userid = userida.getNick().toLowerCase();//userid di string
//				 LogSystem.info(request, "EMAIL USERID :"+userid,refTrx);
//				  
//			 }
//			 catch(Exception e) {
//					e.printStackTrace();
//					//LogSystem.error(getClass(), e);
//			 }
//			 
//			// TODO: handle exception cek di query ini
////			  
////			  
//			 User eeuser=user.findByUsername(userid);
//			 SigningSession session = null;
//			 long mitraid = 0;
//			 try { 
//				 	session = user.findMitraSigningSession(session_key,session_id);
//				 	mitraid = session.getMitra().getId();
//				 	LogSystem.info(request, "Mitra ID :"+mitraid,refTrx);
//				 	mitra = user.findMitrafromSigningSession(mitraid);
//			 } 
//			 catch(Exception e) {
//					e.printStackTrace();
//					//LogSystem.error(getClass(), e);
//			 }
//			 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//			 
////			 DocumentAccess sequence = user.findSequenceByDocument(documentIDe, emaileeuser);
////			 int Order = sequence.getSequence_no();
////			 int smallest_seq = user.findSmallestSequenceByDocument(documentIDe);
////			 int Result = Order - 1; 
////			 boolean seq = eeuseruserids.isSequence();
////			 LogSystem.info(request, "APAKAH SEQUENCE : "+seq);
////			 if (seq==true) {
////				 LogSystem.info(request, "SEQUENCE NIH");
////				 if(Result >= smallest_seq) {	
////				 	DocumentAccess sequence_before = user.findSequenceBeforeByDocument(documentIDe, Result);
////					//boolean before = sequence_before.isFlag();
////				 	//LogSystem.info(request, "APAKAH SEQUENCE SEBELUM SUDAH : "+before);
////					if (sequence_before != null)
////					{
////						LogSystem.error(request, "Not Your Turn to Sign The Document");
////				    	context.getResponse().sendError(403);
////						return;
////					 }
////				 }
////			 }
//			 DocumentAccess sequence = user.findSequenceByDocument(documentIDe, emaileeuser);
//				int Order = sequence.getSequence_no();
////				int smallest_seq = user.findSmallestSequenceByDocument(documentIDe);
//				int smallest_seq = eeuseruserids.getCurrent_seq();
//				int Result = Order; 
//				 boolean seq = eeuseruserids.isSequence();
//				 LogSystem.info(request, "APAKAH SEQUENCE : "+seq,refTrx);
//				 if (seq==true) {
//					 LogSystem.info(request, "SEQUENCE NIH",refTrx);
//					 LogSystem.info(request, "RESULT >>"+Result,refTrx);
//					 LogSystem.info(request, "Nilai Terkecil >>"+smallest_seq,refTrx);
//					 
//					 if(Result > smallest_seq) {	
//					 	DocumentAccess sequence_before = user.findSequenceBeforeByDocument(documentIDe, Result);
//						//boolean before = sequence_before.isFlag();
//					 	//LogSystem.info(request, "APAKAH SEQUENCE SEBELUM SUDAH : "+before);
//						if (sequence_before != null)
//						{
//							LogSystem.error(request, "Not Your Turn to See The Document",refTrx);
//					    	context.getResponse().sendError(406);
//							return;
//						}
//					 }
//				 }
//			 SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
//			 Date datey = new Date();
//			 System.out.println(formatter.format(datey));
//			 Date today = datey;
//			 Date expire = eeuserStrings.getExpire_time();
//			 boolean used = eeuserStrings.isUsed();
//			 if(today.after(expire) ) {
//				 
//				 LogSystem.error(request, "Session has already expired",refTrx);
//	        	 context.getResponse().sendError(408);
//				 return;
//			 }
//			 if (used == true) {
//				 LogSystem.error(request, "Session has already Used",refTrx);
//	        	 context.getResponse().sendError(401);
//				 return;
//			 }
//			 if(mitra==null) {
//				 LogSystem.error(request, "Wrong Session",refTrx);//kalo mitra gak nemu bisa jadi salah id atau key salah berati salah kode
//		         context.getResponse().sendError(403);
//				 return;
//			 } 
//			
////	         if(eeuser!=null) {
////	        	 if(eeuser.getMitra().getId()==mitra.getId() && eeuser.isAdmin()) {
////	        		 //System.out.println("token dan mitra valid");
////	        		 LogSystem.info(request, "Token dan mitra valid");
////	        	 }
////	        	 else {
//////	        		 JSONObject jo=new JSONObject();
//////					 jo.put("res", "05");
//////					 jo.put("notif", "Token dan Mitra tidak sesuai");
//////					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//////					
//////					 return;
////	        		 //outStream.write(403);
////					 //outStream.close();
////	        		 //sendRedirect(context, "/403.html");
////	        		 LogSystem.error(request, "Token dan mitra tidak valid");
////	        		 context.getResponse().sendError(403);
////	        		 return;
////	        	 }
////	         }
////	         else {
//////	        	 JSONObject jo=new JSONObject();
//////				 jo.put("res", "05");
//////				 jo.put("notif", "userid tidak ditemukan");
//////				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//////				
//////				 return;
////				 //outStream.write(403);
////				 //outStream.close();
////	        	 //sendRedirect(context, "/403.html");
////	        	 LogSystem.error(request, "Userid tidak ditemukan");
////	        	 context.getResponse().sendError(403);
////				 return;
////	         }
//	         
//	         
//	         //String email_user=jsonRecv.getString("email_user").toLowerCase();
//	         /*
//	         String pwd_user=jsonRecv.getString("pwd_user");
//	         EeuserMitraDao emdao=new EeuserMitraDao(getDB(context));
//	         EeuserMitra em=emdao.findUserPwdMitra(email_user, pwd_user, mitra.getId());
//	         if(em==null) {
//	        	 JSONObject jo=new JSONObject();
//				 jo.put("res", "05");
//				 jo.put("notif", "user atau password nasabah salah");
//				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				 
//				 //PrintWriter outStream=context.getResponse().getWriter();
//	        	 //outStream.write(401);
//	        	 //outStream.close();
//				 context.getResponse().sendError(401);
//				 return;
//	         }
//	         */
//	         //JSONObject jo = null;
//	        
//	         //DownloadDocMitra(jsonRecv, request, context);
//	         //DB db = getDB(context);
//	         /*
//	         ApiVerification aVerf = new ApiVerification(db);
//	         if(aVerf.verification(jsonRecv)){
//	        	 if(aVerf.getEeuser().isAdmin()==false) {
//		        		PrintWriter outStream=context.getResponse().getWriter();
//		        		outStream.write(403);
//		        		outStream.close();
//		        		return;
//	        	 }
//	        	 //HttpSession session = request.getSession();
//	        	 //User userTrx= new UserManager(db).findByUsername(jsonRecv.getString("user"));	
//		            //session.setAttribute (USER, userTrx);
//	        	 	User usermitra = aVerf.getEeuser();
//	        	 	//Mitra mitra = usermitra.getMitra();
//	        */
//	         		User usermitra=eeuser;//hasil eeuser
//	         		
//					String documentID=documentIDs.getDocument().getIdMitra();
//	    			
//					DocumentsDao dd = new DocumentsDao(db);
//					LoginDao ldao=new LoginDao(db);
//					Documents doc=null;
//					List<Documents> ldocs;
//					try {
//						//doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
//						ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
//						if(ldocs.size()==0) {
//							LogSystem.error(request, "Dokumen size 0",refTrx);
//							context.getResponse().sendError(404);
//				        	return;
//						}
//						else {
//							doc=ldocs.get(0);
//						}
//					} catch (Exception e) {
//						// TODO: handle exception
//						LogSystem.error(request, "Error dokumen",refTrx);
//						e.printStackTrace();
//						context.getResponse().sendError(404);
//			        	return;
//					}
//					
//					String idDoc=null;
//					Boolean flag = null;
//					
//					//String userEmail=jsonRecv.getString("email_user").toLowerCase(); 
//					
//					String userEmail = emaileeuser;
//					// TODO: handle exception setting buat BYPASS
//					if(jsonDecrypt.has("by-pass-click"))
//					{
//						by_pass_click = jsonDecrypt.getBoolean("by-pass-click");//AUTO-SIGN
//					}
//
//					Long idmitra = usermitra.getId();
//					Long idMitra2 = usermitra.getMitra().getId();
//					//UserManager um=new UserManager(db);
//					//System.out.println(userEmail);
//					LogSystem.info(request, userEmail,refTrx);
//					User usr= user.findByUsernameSign(userEmail);
//					// TODO: handle exception cari eeuser string
//					//System.out.println("user nya adalah = "+usr.getName());
//					if(usr!=null) {
//						/*
//						id.co.keriss.consolidate.ee.Login login=ldao.getUsername(usr.getNick());
//						if(login==null) {
//							PrintWriter outStream=context.getResponse().getWriter();
//			        		outStream.write(403);
//			        		outStream.close();
//			        		return;
//						}
//						*/
//						DocumentsAccessDao docDao=new DocumentsAccessDao(db);
//						List<DocumentAccess> da = docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
//						////user email ganti eeuser
//						if(da.size() <= 0)
//						{
//							LogSystem.error(request, "Dokumen size 0",refTrx);
//							context.getResponse().sendError(404);
//							return;
//						}
//						
//						//check level user < 3 dan level mitra > 2
//						int leveluser=Integer.parseInt(usr.getUserdata().getLevel().substring(1));
//			        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
//			        	if(levelmitra>2 && leveluser<3) {
//			        		LogSystem.error(request, "level user tidak diperbolehkan untuk mitra ini",refTrx);
//			        		context.getResponse().sendError(403);
//			        		return;
//			        	}
//						
//						//System.out.println("TES SSSSSSSSSSSSSSSSSSS :"+usr.getUserdata().getImageTtd());
//						String id=AESEncryption.encryptDoc(String.valueOf(da.get(0).getDocument().getEeuser().getId()));
//						String namafile=AESEncryption.encryptDoc(da.get(0).getDocument().getSigndoc());
////						boolean usingopt = da.get(0).getDocument().getPath().startsWith("/opt");
////						LogSystem.info(request, "useopt = " + usingopt,refTrx);
////						String optget = "";
////						if (usingopt) {
////							optget = AESEncryption.encryptDoc("optfile");
////						}
//						
//						String usingopt[] = da.get(0).getDocument().getPath().split("/");
//						LogSystem.info(request, "useopt = " + usingopt,refTrx);
//						String optget = "";
//						optget = AESEncryption.encryptDoc(usingopt[1]);
//						
////						LogSystem.info(request, "id = " + id);
////						LogSystem.info(request, "namafile = " + namafile);
//						//System.out.println("id="+id);
//						//System.out.println("namafile="+namafile);
////						Samba smb = new Samba();
//						//context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
//						context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8")+"&tp="+URLEncoder.encode(optget, "UTF-8")+"&ss="+URLEncoder.encode(SystemUtil.getExp(namafile), "UTF-8"));
//						//System.out.println("https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
//						//System.out.println("https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile);
//						LogSystem.info(request, "https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile,refTrx);
//						////File img=new File(usr.getUserdata().getImageTtd());
//						//File img=new File(usr.getI_ttd());
////						byte[] encoded = Base64.encode(smb.openfile(usr.getUserdata().getImageTtd()));
////						boolean isopt=usr.getUserdata().getImageTtd().startsWith("/opt");
////						LogSystem.info(request, "is /OPT ?"+isopt,refTrx);
////						byte[] encoded ;
////						if (isopt) {
////							encoded = Base64.encode(smb.openfile(usr.getUserdata().getImageTtd()));
////						}else {
////							encoded = Base64.encode(smb.openfile_NAS(usr.getUserdata().getImageTtd()));
////						}
//						
//						Samba smb = new Samba(refTrx, request);
//						byte[] encoded ;
//						try {
//							encoded=Base64.encode(smb.openfile(usr.getUserdata().getImageTtd()));
//						}catch (Exception e) {
//				      	     log.error(LogSystem.getLog( ExceptionUtils.getStackTrace(e), refTrx, "ERROR"));
//				      	     smb.close();
//				      	     throw e;
//						}finally {
//							smb.close();
//						}
//
//
//						int visible=1;
//						JSONObject dataUser=new JSONObject();
//						dataUser.put("key_session", session_key);
//						dataUser.put("id_session", session_id);
//					
//						//dataUser.put("idDoc", idDoc);
//						JSONArray uL=new JSONArray();
//						for(DocumentAccess dAcs:da) {
//							try {
//								if(dAcs.getEeuser()!=null) {
//									dAcs.setEeuser(usr);
//									docDao.update(dAcs);
//								}
//							} catch (Exception e) {
//								// TODO: handle exception
//							}
//							
//							idDoc = String.valueOf(dAcs.getDocument().getId());
//							flag = dAcs.isFlag();
//							
//							
//							if( ((dAcs.getType().equals("sign") || dAcs.getType().equals("initials")) && dAcs.isFlag()==true)) {
//								statusSign=1;
//								continue;
//							}
//							if(!dAcs.isRead()) {
//								dAcs.setRead(true);
//								docDao.update(dAcs);
//							}
//							if(dAcs.getType().equals("share")) {
//								continue;
//							}
//							
//							if(dAcs.getType().equalsIgnoreCase("initials")) {
//								String sgn="0";
//								if(!dAcs.isVisible()) {
//									visible=1;
//									sgn="1";
//								}
//								InitialDao idao=new InitialDao(db);
//								List<Initial> li=idao.findInitialByDocac(dAcs.getId());
//								for(Initial in:li) {
//									JSONObject us=new JSONObject();
//									us.put("idAccess", dAcs.getId().toString());
//									us.put("lx", in.getLx());
//									us.put("ly", in.getLy());
//									us.put("rx", in.getRx());
//									us.put("ry", in.getRy());
//									us.put("page",in.getPage());
//									us.put("type", dAcs.getType());
//									us.put("sgn", sgn);
//									uL.put(us);
//								}
//								continue;
//							}
//							
//							JSONObject us=new JSONObject();
//							us.put("idAccess", dAcs.getId().toString());
//							us.put("lx", dAcs.getLx());
//							us.put("ly", dAcs.getLy());
//							us.put("rx", dAcs.getRx());
//							us.put("ry", dAcs.getRy());
//							us.put("page",dAcs.getPage());
//							us.put("type", dAcs.getType());
//							us.put("sgn", "0");
//							
//							if(!dAcs.isVisible()) {
//								visible=0;
//								us.put("sgn", "1");
//							}
//							uL.put(us);
//
//						}
//						
//						dataUser.put("idDoc", idDoc);
//						dataUser.put("user", uL);
//						String AvailUsername = "";
//						int value = rand.nextInt(50000); 
//						//System.out.println(dataUser.toString());
//						LogSystem.info(request, "Data User = " + dataUser.toString(),refTrx);
//						context.put("flag", flag);
//						context.put("by_pass_click", by_pass_click);
//						context.put("rand", value);
//						context.put("locSign", dataUser);
//						context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
//						context.put("title_doc", da.get(0).getDocument().getFile_name());
//						context.put("usersign", dataUser);
//						context.put("statusSign", statusSign);
//						context.put("visible", visible);
//						context.put("size", uL.length());
//						//context.put("username", login.getUsername());
//						if(usr.getLogin()!=null) {
//							//System.out.println("login ada bro ");
//							LogSystem.info(request, "Login tidak null",refTrx);
//							context.put("username", usr.getLogin().getUsername());
//							AvailUsername = usr.getLogin().getUsername().toString();
//						}
//						
//						context.put("email", usr.getNick().toLowerCase());
//						//context.put("pwd_user", pwd_user);
//						context.put("mitra", String.valueOf(mitra.getId()));
//						context.put("namamitra", String.valueOf(mitra.getName()));
//						context.put("statususer", usr.getStatus());
//						context.put("nohp", usr.getUserdata().getNo_handphone());
//						context.put("iduser", usr.getId());
//
//						String username="";
//						String pickUsername="";
//
//						if(usr.getStatus()=='1') {
//								if(usr.getLogin()==null) {
//									id.co.keriss.consolidate.ee.Login lgn = null;
//							        String nama = usr.getName().toLowerCase();
//							        String character = "- ";
//							        String nameSplit[] = nama.split(" ");
//							        String date=new SimpleDateFormat("ddMMYY").format(new Date());
//		
//							        int go = 0;
//							        int a = 0;
//							        int b = 0;
//							        int c = 0;
//							        
//							        while (go < 3)
//							        {
//							        	a = 0;
//								        b = 0;
//								        c = 0;
//							        	pickUsername = nameSplit[rand.nextInt(nameSplit.length)]+shuffle((date+usr.getUserdata().getNo_identitas()+usr.getId())+"_").substring(0,rand.nextInt(6));
//							        	lgn=ldao.getByUsername2(pickUsername);
//							        	
//							        	if(pickUsername.length() > 5)
//							        	{
//							        		a = 1;
//							        	}
//							        	if(lgn == null)
//							        	{
//							        		b = 1;
//							        	}
//							        	if(pickUsername.charAt(pickUsername.length() - 1) != '_' && pickUsername.charAt(0) != '_')
//							        	{
//							        		c = 1;
//							        	}
//							        	go = a + b + c;
//							        }
//								}
//								else {
//									pickUsername = AvailUsername;
//								}
//					        LogSystem.info(request, "Found unique username: " + pickUsername,refTrx);
//			                username=pickUsername;
//					        LogSystem.info(request, "Usernamenya adalah = "+username,refTrx);
//						}
//						context.put("newusername", username);
//						context.put("idmitra", idMitra2);
//						context.put("file_name", documentName);
//						JSONObject dUser=new JSONObject();
//						dUser.put("utype", AESEncryption.encryptIdpreregis(String.valueOf(usr.getId())));
//			    		dUser.put("etype", AESEncryption.encryptIdpreregis(usr.getNick().toLowerCase()));
////			    		dUser.put("ptype", usr.getUserdata().getNo_handphone());
//						dUser.put("type", AESEncryption.encryptDoc(String.valueOf(idMitra2)));
//						context.put("usertype", dUser);
//					    LogSystem.info(request, "Open Document ID : "+idDoc,refTrx);
//					}
//					else {
//						LogSystem.info(request, "User null",refTrx);
//						context.getResponse().sendError(401);
//			        	
//						return;
//					}
//	         //}
//			 context.put("link", "https://"+LINK);
//	         context.put("domain", "https://"+DOMAINAPI);
//	         context.put("domainwv", "https://"+DOMAINAPIWV);
//	         context.put("webdomain", "https://"+DOMAIN);
//			//String res="";
//			//if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
//			//else res="<b>ERROR 404</b>";
//
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
//            try {
//				context.getResponse().sendError(400);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		}
//	}	
//	
//	public static String shuffle(String input){
//        List<Character> characters = new ArrayList<Character>();
//        for(char c:input.toCharArray()){
//            characters.add(c);
//        }
//        StringBuilder output = new StringBuilder(input.length());
//        while(characters.size()!=0){
//            int randPicker = (int)(Math.random()*characters.size());
//            output.append(characters.remove(randPicker));
//        }
//        return output.toString();
//    }
//}