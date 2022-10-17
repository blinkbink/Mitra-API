package apiMitra;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import org.killbill.billing.client.KillBillClientException;

import com.anthonyeden.lib.config.Configuration;

import api.email.SendTerimaDoc;
import api.log.ActivityLog;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.FormatPDFDao;
import id.co.keriss.consolidate.dao.InitialDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.dao.LetakTtdDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.LetakTtd;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VO.EmailVO;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;

public class SendDocMitra extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	
	String refTrx="";
	String kelas="apiMitra.SendDocMitra";
	String trxType="SEND-DOC";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="DOC"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		String filetype=null;
		long filesize=0;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		
		Mitra mitra=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart",kelas, refTrx, trxType);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}
				// multipart form
				else {
					TokenMitraDao tmd=new TokenMitraDao(getDB(context));
					String token=request.getHeader("authorization");
					LogSystem.info(request, "Isi Token yang dikirim : " + token, kelas, refTrx, trxType);
					if(token!=null) {
						String[] split=token.split(" ");
						if(split.length==2) {
							if(split[0].equals("Bearer"))token=split[1];
						}
//						TokenMitra tm=tmd.findByToken(token.toLowerCase());
						TokenMitra tm = null;
						try {
							tm=tmd.findByToken(token.toLowerCase());
						}catch(Exception e)
						{
							JSONObject jo = new JSONObject();
							jo.put("result", "91");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.error(request, e.toString() , kelas, refTrx, trxType);
							return;
						}
						
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada : " + token,kelas, refTrx, trxType);
							mitra=tm.getMitra();
						} else {
							LogSystem.error(request, "Token null",kelas, refTrx, trxType);
							JSONObject jo=new JSONObject();
							jo.put("result", "55");
							jo.put("notif", "token salah");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.response(request, jo, kelas, refTrx, trxType);
							return;
						}
					}
					// Create a new file upload handler
					ServletFileUpload upload = new ServletFileUpload(
							new DiskFileItemFactory());
					
					Enumeration<String> headerNames = request.getHeaderNames();
					//System.out.println("authorization adalah = "+request.getHeader("authorization"));
					LogSystem.info(request, "authorization adalah = "+request.getHeader("authorization"),kelas, refTrx, trxType);
					/*
			        if (headerNames != null) {
			                while (headerNames.hasMoreElements()) {
			                	String name=headerNames.nextElement();
			                	System.out.println("header names adalah = "+name);
			                        System.out.println("Header: " + request.getHeader(name));
			                }
			        }
			        */
					// parse requests
//					 fileItems = upload.parseRequest(request);
					try
					{
						LogSystem.info(request, "Reading request", kelas, refTrx, trxType);
						fileItems = upload.parseRequest(request);
						LogSystem.info(request, "Complete reading request", kelas, refTrx, trxType);
					}catch(Exception e)
					{
						JSONObject jo = new JSONObject();
						LogSystem.info(request, "Timeout read request", kelas, refTrx, trxType);
		        		jo.put("result", "91");
						jo.put("notif", "System timeout. silahkan coba kembali.");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						return;
					}

					// Process the uploaded items
					for (FileItem fileItem : fileItems) {
						// a regular form field
						if (fileItem.isFormField()) {
							if(fileItem.getFieldName().equalsIgnoreCase("jsonfield")){
								jsonString=fileItem.getString();
							}
							
						}
						else {
							
							 if(fileItem.getFieldName().equalsIgnoreCase("file")){
								 //dataFile=fileItem.get();
								 filedata=fileItem;
								 filename=fileItem.getName();
								 filetype=fileItem.getContentType();
								 filesize=fileItem.getSize();
//								 System.out.println("Format File: " + filetype.split("/")[1]);
							 }
							 
						}
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems,kelas, refTrx, trxType);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         if(mitra!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
	        		 LogSystem.error(request, "Parameter userid tidak ditemukan",kelas, refTrx, trxType);
	        		 JSONObject jo=new JSONObject();
					 jo.put("result", "12");
					 jo.put("notif", "Parameter userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
	        	 }
	        	 
	        	 String userid=jsonRecv.getString("userid");
	        	 
		         UserManager user=new UserManager(getDB(context));
		         //User eeuser=user.findByUsername(userid);
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
		        	 }
		        	 else {
		        		 LogSystem.info(request, "Token dan mitra tidak sesuai",kelas, refTrx, trxType);
		        		 JSONObject jo=new JSONObject();
						 jo.put("result", "55");
						 jo.put("notif", "Token dan Mitra tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						 LogSystem.response(request, jo, kelas, refTrx, trxType);
						 return;
		        	 }
		         }
		         else {
		        	 LogSystem.info(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "12");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
		         }
	         }
	         
//	         
//	         if(!filetype.equals("pdf"))
//	         {
//	        	 JSONObject jo=new JSONObject();
//				 jo.put("result", "12");
//				 jo.put("notif", "Format file salah");
//				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				 return;
//	         }
//	         if(filesize <= 0)
//	         {
//	        	 JSONObject jo=new JSONObject();
//				 jo.put("result", "12");
//				 jo.put("notif", "File rusak");
//				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				 return;
//	         }
	         
	         JSONObject jo = null;
	        
	         jo = docMitra(mitra, useradmin, jsonRecv, filedata, filename, request, context); 
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo, kelas, refTrx, trxType+"/"+mitra.getName());



		}catch (Exception e) {
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject docMitra(Mitra mitratoken, User useradmin, JSONObject jsonRecv, FileItem dataFile, String filename,HttpServletRequest  request,JPublishContext context) throws JSONException{
		DB db = getDB(context);

        JSONObject jo=new JSONObject();
        String res="12";
        //String signature=null;
        SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Documents id_doc=null;
        boolean kirim=false;
        DocumentsDao docDao = new DocumentsDao(db);
		try{
			UserManager um=new UserManager(db);
			User userRecv=null;
//			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
			//DigiSign ds=new DigiSign();
			
			//SmartphoneVerification sVerf=new SmartphoneVerification(db);
			ApiVerification aVerf = new ApiVerification(db);
			boolean vrf=false;
	        if(mitratoken!=null && useradmin!=null) {
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
		        		jo.put("result", res);
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
	        	
				  
				  //String ext=FilenameUtils.getExtension(dataFile.getName());
	        	  /*
	        	  String ext2 = dataFile.getContentType();
	    		  String[] split=ext2.split("/");
	    		  String ext=split[1];
	    		  //LogSystem.info(request, "Ext selfie = "+ext);
	    		  LogSystem.info(request, "type doc = "+ext2);
	    		  LogSystem.info(request, "Ext dokumen = "+ext);
	    		  if(!ext.equalsIgnoreCase("pdf") && !ext.equalsIgnoreCase("pdf; charset=UTF-8")) {
	    			  String check=FilenameUtils.getExtension(dataFile.getName());
	    			  LogSystem.info(request, "ext name doc = "+check);
	    			  if(check.equalsIgnoreCase("pdf")) {
	    				  LogSystem.info(request, "ext name doc = LOLOS");
	    			  } else {
	    				  jo.put("result", "FE");
		    			  jo.put("notif", "Format dokumen harus pdf");
		    				
		    			  return jo;  
	    			  }
	    		  }
				  */
	    		  
				  //if(fProc.uploadFileMitra(request, db, usr, dataFile, jsonRecv.getString("document_id"))) {
				  if(jsonRecv.getString("document_id").equals("") || jsonRecv.getString("document_id")==null) {
					  jo.put("result", "28");
		              jo.put("notif", "document id kosong");
		              return jo;
				  } 
				  
				  FileProcessor fProc=new FileProcessor(refTrx);
				  if(fProc.uploadFILEnQRMitra(request, db, usr, dataFile, jsonRecv.getString("document_id"), jsonRecv)) {
					  //fProc.generateQRCode(db);
					  //fProc.getDc().getId();
 					  res="00";
 					 LogSystem.info(request, "Upload successfully",kelas, refTrx, trxType);
					 //System.out.println("Upload Successfully");
					    
				  } else {
					  jo.put("result", "FE");
		              jo.put("notif", "Format dokumen harus pdf");
		              return jo;
				  }
				  
				  //id_doc=new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename());
				  id_doc=fProc.getDc();
				  Vector<DocumentAccess> lttd=new Vector();
				  Vector<EmailVO> emails=new Vector<>();
				  DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
				  String version  = null;
				  ArrayList<String> listCert = new ArrayList<String>();
				  if(id_doc!=null) {
						/*
						int clear=dAccessDao.clearAkses(id_doc.getId());
						//System.out.println("clear access id: "+id_doc.getId()+", "+clear+" rows");
						LogSystem.info(request, "clear access id: "+id_doc.getId()+", "+clear+" rows",kelas, refTrx, trxType);
						*/
						if(!jsonRecv.has("format_doc")) jsonRecv.put("format_doc", "");
						
						if(jsonRecv.get("req-sign")!=null) {
							JSONArray sendList=jsonRecv.getJSONArray("req-sign");
							
							//Check sertifikat ke KMS
							for (int c=0 ; c < sendList.length() ; c++)
							{
								JSONObject expired = (JSONObject) sendList.get(c);
								LogSystem.info(request, "USER CEK " + expired.getString("email").toLowerCase(), kelas, refTrx, trxType);
								User userCert= new UserManager(db).findByUsername(expired.getString("email").toLowerCase());
								LogSystem.info(request, "USER " + userCert, kelas, refTrx, trxType);
								String userExpired = expired.getString("user").substring(0, 3);
								
								String level = null;

								if(userCert != null)
								{
									if (userExpired.equals("sgl"))
									{
										level = "C5";
										LogSystem.info(request, "Cek sertifikat seal " + level,kelas, refTrx, trxType);
									}
									else
									{
										level = userCert.getUserdata().getLevel();
										LogSystem.info(request, "Cek Sertifikat user " + level,kelas, refTrx, trxType);
									}
									
									//Check sertifikat ke KMS
									KmsService kms = new KmsService(request, "Cek Sertifikat Proses Kirim Dokumen/"+refTrx);
									JSONObject cert = new JSONObject();

									if(userCert.getMitra() != null)
									{
										cert = kms.checkSertifikat(userCert.getId(), level, userCert.getMitra().getId().toString());
									}
									else 
									{
										cert = kms.checkSertifikat(userCert.getId(), level, "");
									}
									
									if(!cert.has("result"))
									{
										LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
										jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										
										return jo;
									}
									
									if(cert.getString("result").length() > 3  || cert.getString("result").equals(""))
									{
										LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
										jo.put("result", "15");
										jo.put("notif", "Kirim dokumen gagal");
										
										return jo;
									}
									
									if(expired.has("aksi_ttd")) {
										
										if (expired.getString("aksi_ttd").equalsIgnoreCase("at"))
										{
											if(cert.getString("result").equals("05") || cert.getString("result").equals("G1"))
											{
												
												LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
												jo.put("result", "15");
												
												jo.put("notif", "Kirim dokumen gagal");
												
												if (cert.getString("result").equals("G1"))
												{
													jo.put("notif", "Kirim dokumen gagal");
													jo.put("info", "Sertifikat Elektronik lama milik " + expired.getString("email") + " sudah dicabut. Silakan melakukan konfirmasi penerbitan sertifikat elektronik baru dengan login melalui website Digisign");
													if (level.equals("C5"))
													{
														jo.put("info", userCert.getMitra().getName() + " belum memiliki sertifikat segel elektronik. Untuk melakukan segel otomatis, silakan melakukan konfirmasi penerbitan sertifikat segel elektronik dengan login melalui website Corporate Digisign");
													}
												}
												
												if (cert.getString("result").equals("05"))
												{
													jo.put("notif", "Kirim dokumen gagal");
													jo.put("info",  "User " + expired.getString("email") + " belum memiliki sertifikat elektronik untuk melakukan tandatangan otomatis, Silakan melakukan konfirmasi penerbitan sertifikat elektronik dengan login melalui website Digisign");
													if (level.equals("C5"))
													{
														jo.put("info", userCert.getMitra().getName() + " belum memiliki sertifikat segel elektronik. Untuk melakukan segel otomatis, silakan melakukan konfirmasi penerbitan sertifikat segel elektronik dengan login melalui website Corporate Digisign");
													}
												}
												
												 try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("info"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
											        }
												
												return jo;
											}
											else
											{
												if (cert.has("needRegenerate"))
												{
													if(cert.getBoolean("needRegenerate"))
													{
														if(level.equals("C5"))
														{
															jo.put("info", "Sertifikat segel elektronik lama milik "+userCert.getMitra().getName()+" akan dicabut. Mohon melakukan konfirmasi penerbitan sertifikat elektronik segel baru dengan login melalui website Corporate Digisign");
														}
														else
														{
															jo.put("info", "Sertifikat elektronik lama milik "+expired.getString("email")+" akan dicabut. Mohon melakukan konfirmasi penerbitan sertifikat elektronik baru dengan login melalui website Digisign");
														}
													}
												}
											}
										
										
										
											if (cert.has("keyVersion") && expired.getString("aksi_ttd").equalsIgnoreCase("at")) 
											{
												version = cert.getString("keyVersion");	
												listCert.add(cert.getString("keyVersion"));	
											}
											else
											{
												version = "1.0";
											}
										
										
										
											//Cek aksi ttd mt jika 07, 06, G1
											LogSystem.info(request, "Status sertifikat user : " + expired.getString("email") + " adalah " + cert,kelas, refTrx, trxType);
											if(cert.getString("result").equals("07") || cert.getString("result").equals("06"))
											{
												LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												  
												jo.put("result", "15");
												
												if(cert.getString("result").equals("06"))
												{
													jo.put("notif", "Kirim dokumen gagal");
													jo.put("info", "Sertifikat Elektronik milik " + expired.getString("email") + " sudah habis masa berlakunya, silahkan melakukan registrasi ulang");
													if (level.equals("C5"))
													{
														jo.put("info", "Sertifikat segel elektronik milik " + userCert.getMitra().getName() + " sudah habis masa berlakunya, silahkan melakukan registrasi ulang");
													}
												}
												
												if(cert.getString("result").equals("07"))
												{
													jo.put("notif", "Kirim dokumen gagal");
													jo.put("info", "Sertifikat Elektronik milik " + expired.getString("email") + " sudah dicabut, silahkan melakukan registrasi ulang");
													if (level.equals("C5"))
													{
														jo.put("info", "Sertifikat segel elektronik milik " + userCert.getMitra().getName() + " sudah dicabut, silahkan melakukan registrasi ulang");
													}
												}
												
												 try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("info"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
											        }
												
												return jo;
											}
										}
									}
								}
								else
								{
									if(expired.has("aksi_ttd")) {
										
										if (expired.getString("aksi_ttd").equalsIgnoreCase("at"))
										{
											LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "15");
											jo.put("notif", "Kirim dokumen gagal");
											jo.put("info", "user belum terdaftar, tidak bisa cek sertifikat");
											LogSystem.info(request, "User belum terdaftar tidak bisa cek sertifikat user at",kelas, refTrx, trxType);
											
											 try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, User at belum terdaftar tidak bisa cek sertifikat", Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
										        }
											 
											return jo;
										}
									}
									
									LogSystem.info(request, "User belum terdaftar skip cek sertifikat karena MT" + expired.getString("email"),kelas, refTrx, trxType);
								}
							}//Selesai cek sertifikat ke kms
						
							if (!verifyAllEqualUsingStream(listCert))
							{
								LogSystem.info(request, "List sertifikat " + listCert, kelas, refTrx, trxType);
								LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
								  
								jo.put("result", "15");
								jo.put("notif", "Kirim dokumen gagal");
								jo.put("info", "Sertifikat harus diperbarui");
								
								
								 try {
							        	ActivityLog logSystem = new ActivityLog(request, refTrx);
							        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, Sertifikat harus diperbarui", Long.toString(usr.getId()), null, null, null, null,null);
							        }catch(Exception e)
							        {
							        	e.printStackTrace();
							        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
							        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
							        }
								 
								return jo;
								
							}
							
							if (version != null)
							{
								if(version.equals("1.0") || version.equals("2.0") )
								{
									version = "v1";
								}
								
								if(version.equals("3.0"))
								{
									version = "v3";
								}
							}

							
							FormatPDFDao fdao=new FormatPDFDao(db);
							LetakTtdDao ltdao=new LetakTtdDao(db);
							
							for(int i=0; i<sendList.length(); i++) {
								JSONObject obj=(JSONObject) sendList.get(i);
								
								DocumentAccess da=new DocumentAccess();
								User user= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());
								
								//check untuk user dengan level 2
								if(user!=null) {
									  int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
									  int leveluser=Integer.parseInt(user.getUserdata().getLevel().substring(1));
									  if(levelmitra>2 && leveluser<3) {
										    
									  	//dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
										
										jo.put("result", "06");
										jo.put("notif", "User tidak diperkenankan menerima dokumen dari mitra. Silahkan registrasi ulang atau konfirmasi ke DigiSign.");
										return jo;
										 
									  }
								}
								
								/*change to eeuser*/
//								if(user!=null)da.setUserdata(user.getUserdata());
								
								LogSystem.info(request, "size json object = "+jsonRecv.getString("format_doc").length(),kelas, refTrx, trxType);
								//System.out.println("size json object = "+jsonRecv.getString("format_doc").length());
								if(!obj.has("aksi_ttd")||obj.getString("aksi_ttd").equals(""))obj.put("aksi_ttd", "mt");
								if(!obj.has("user")||obj.getString("user").equals(""))obj.put("user", "ttd0");
								
								if(jsonRecv.getString("format_doc").length()>0) {
									Long idmitra = usr.getMitra().getId();
									//System.out.println("id mitra = "+idmitra);
									LogSystem.info(request, "ID mitra = "+idmitra,kelas, refTrx, trxType);
									FormatPdf fd=fdao.findFormatPdf(jsonRecv.getString("format_doc"), idmitra);
									if(fd!=null) {
										//System.out.println("ttd ke = "+obj.getString("user").substring(3));
										LogSystem.info(request, "USERRRRRRR = "+obj.getString("user").substring(0, 3),kelas, refTrx, trxType);
										//System.out.println("USERRRRRRR = "+obj.getString("user").substring(0, 3));
										if(obj.getString("user").substring(0, 3).equalsIgnoreCase("ttd")) {
											LetakTtd lt=ltdao.findLetakTtd(obj.getString("user").substring(3), fd.getId());
											if(lt!=null) {
												boolean sign=false;
												if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
													LogSystem.info(request, "Masuk auto ttd.",kelas, refTrx, trxType);
													//System.out.println("masuk auto ttd.");
													if(user!=null) {
														if(user.isAuto_ttd()) {
															sign=true;
														}
														else {
															LogSystem.info(request, "Delete data",kelas, refTrx, trxType);
															//System.out.println("delete data");
															dAccessDao.deleteWhere(id_doc.getId());
															File file=new File(id_doc.getPath()+id_doc.getRename());
															file.delete();
															new DocumentsDao(db).delete(id_doc);
															
															jo.put("result", "05");
											                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
											                return jo;
														}
													}
													else {
														dAccessDao.deleteWhere(id_doc.getId());
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														
														jo.put("result", "07");
										                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan ttd otomatis");
										                return jo;
													}
													
												}
												if(user!=null)da.setEeuser(user);
												da.setDocument(id_doc);
												da.setFlag(false);
												da.setType("sign");
												da.setDate_sign(null);
												da.setEmail(obj.getString("email").toLowerCase());
												da.setName(obj.getString("name"));
												da.setPage(lt.getPage());
												da.setLx(lt.getLx());
												da.setLy(lt.getLy());
												da.setRx(lt.getRx());
												da.setRy(lt.getRy());
												da.setDatetime(new Date());
												da.setAction(obj.getString("aksi_ttd"));
												if(obj.has("visible")) {
													boolean vis=true;
													String v=obj.getString("visible");
													if(v.equalsIgnoreCase("0"))vis=false;
													
													LogSystem.info(request, "Visible = "+vis,kelas, refTrx, trxType);
													//System.out.println("visible = "+vis);
													da.setVisible(vis);
												} else {
													da.setVisible(true);
												}
												
												Long idAcc=dAccessDao.create(da);
												
												LogSystem.info(request, "Sign = "+sign,kelas, refTrx, trxType);
												//System.out.println("sign = "+sign);
												if(sign==true) {
													//System.out.println("id doc access = "+idAcc);
													LogSystem.info(request, "ID doc access = "+idAcc,kelas, refTrx, trxType);
													DocumentAccess dac=dAccessDao.findbyId(idAcc);
													lttd.add(dac);
													
												}
												
												Userdata udata=new Userdata();
												boolean reg=false;
												if(user!=null) {
													udata=user.getUserdata();
													reg=true;
												}
												
												boolean input=false;
												for(EmailVO ev:emails) {
													if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
														input=true;
														break;
													}
												}
												
												if(input==false) {
													//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
				    								//mail.run();
				    								EmailVO evo=new EmailVO();
				    								evo.setEmail(obj.getString("email"));
				    								evo.setUdata(udata);
				    								evo.setReg(reg);
				    								evo.setNama(obj.getString("name"));
				    								evo.setIddocac(idAcc);
				    								emails.add(evo);
												}
											} else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
												jo.put("result", "05");
								                jo.put("notif", "Letak ttd tidak tersedia");
								                return jo;
											}
										}
										else {
											//untuk paraf
											LetakTtd lt=ltdao.findLetakPrf(obj.getString("user").substring(3), fd.getId());
											if(lt!=null) {
												boolean prf=false;
												DocumentAccess daPrf=null;
												if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
													LogSystem.info(request, "Masuk auto paraf",kelas, refTrx, trxType);
													//System.out.println("masuk auto prf.");
													
													if(user!=null) {
														if(user.isAuto_ttd()) {
															prf=true;
															//System.out.println("Paraf true");
															LogSystem.info(request, "Paraf true",kelas, refTrx, trxType);
															daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "at");
															da.setAction("at");
														}
														else { 
															dAccessDao.deleteWhere(id_doc.getId());
															File file=new File(id_doc.getPath()+id_doc.getRename());
															file.delete();
															new DocumentsDao(db).delete(id_doc);
															
															jo.put("result", "07");
											                jo.put("notif", "user tidak diijinkan untuk prf otomatis");
											                return jo;
														}
													}
													else {
														dAccessDao.deleteWhere(id_doc.getId());
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														
														jo.put("result", "07");
										                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan prf otomatis");
										                return jo;
													}
													
												}
												else {
													if(user!=null) {
														daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
														
													} else {
														daPrf=dAccessDao.findAccessByEmailPrf(obj.getString("email"), id_doc.getId(), "mt");
													}
													da.setAction("mt");
												}
												
												Long idAcc=(long) 0;
												Initial ini=new Initial();
												if(daPrf==null) {
													if(user!=null)da.setEeuser(user);
													da.setDocument(id_doc);
													da.setFlag(false);
													da.setType("initials");
													da.setDate_sign(null);
													da.setEmail(obj.getString("email").toLowerCase());
													da.setName(obj.getString("name"));
													da.setDatetime(new Date());
													da.setAction(obj.getString("aksi_ttd"));
													if(obj.has("visible")) {
														boolean vis=true;
														String v=obj.getString("visible");
														if(v.equalsIgnoreCase("0"))vis=false;
														
														System.out.println("visible = "+vis);
														da.setVisible(vis);
													} else {
														da.setVisible(true);
													}
													
													idAcc=dAccessDao.create(da);
													DocumentAccess dac=dAccessDao.findbyId(idAcc);
													if(prf==true) {
														//System.out.println("id doc access paraf = "+idAcc);
														LogSystem.info(request, "id doc access paraf = "+idAcc,kelas, refTrx, trxType);
														lttd.add(dac);
													}
													
													ini.setDoc_access(dac);
													
												}
												else {
													idAcc=daPrf.getId();
													//if(prf==true)lttd.add(daPrf);
													ini.setDoc_access(daPrf);
												}
												
												//System.out.println("prf = "+prf);
												LogSystem.info(request, "Paraf "+prf,kelas, refTrx, trxType);
												ini.setLx(lt.getLx());
												ini.setLy(lt.getLy());
												ini.setRx(lt.getRx());
												ini.setRy(lt.getRy());
												ini.setPage(lt.getPage());
												new InitialDao(db).create(ini);
												
												Userdata udata=new Userdata();
												boolean reg=false;
												if(user!=null) {
													udata=user.getUserdata();
													reg=true;
												}
												
												boolean input=false;
												for(EmailVO ev:emails) {
													if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
														input=true;
														break;
													}
												}
												
												if(input==false) {
													//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
				    								//mail.run();
				    								EmailVO evo=new EmailVO();
				    								evo.setEmail(obj.getString("email"));
				    								evo.setUdata(udata);
				    								evo.setReg(reg);
				    								evo.setNama(obj.getString("name"));
				    								evo.setIddocac(idAcc);
				    								emails.add(evo);
												}
												//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
			    								//mail.run();
											} else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
												jo.put("result", "05");
								                jo.put("notif", "Letak prf tidak tersedia");
								                return jo;
											}
										}
										
									}
									else {
										dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
										jo.put("result", "05");
						                jo.put("notif", "Format doc tidak tersedia.");
						                return jo;
									}
								} else {
									if(!obj.has("user"))obj.put("user", "ttd");
									String actUser="ttd";
									if(obj.getString("user").length()>3)actUser=obj.getString("user").substring(0, 3);
									else actUser=obj.getString("user").substring(0);
									
									if(actUser.equalsIgnoreCase("ttd")||actUser.equalsIgnoreCase("")) {
										boolean sign=false;
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											da.setAction("at");
											if(user!=null) {
												if(user.isAuto_ttd()) {
													sign=true;
												}
												else { 
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "07");
									                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
									                return jo;
												}
											}
											else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
												jo.put("result", "07");
								                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan ttd otomatis");
								                return jo;
											}
											
										} else {
											da.setAction("mt");
										}
										
										if(user!=null)da.setEeuser(user);
										da.setDocument(id_doc);
										da.setFlag(false);
										da.setType("sign");
										da.setDate_sign(null);
										da.setEmail(obj.getString("email").toLowerCase());
										da.setName(obj.getString("name"));
										da.setPage(Integer.parseInt(obj.getString("page")));
										da.setLx(obj.getString("llx"));
										da.setLy(obj.getString("lly"));
										da.setRx(obj.getString("urx"));
										da.setRy(obj.getString("ury"));
										da.setDatetime(new Date());
										if(obj.has("visible")) {
											boolean vis=true;
											String v=obj.getString("visible");
											if(v.equalsIgnoreCase("0"))vis=false;
											
											System.out.println("visible = "+vis);
											da.setVisible(vis);
										} else {
											da.setVisible(true);
										}
										Long idAcc=dAccessDao.create(da);
										
										if(sign==true) {
											DocumentAccess dac=dAccessDao.findbyId(idAcc);
											lttd.add(dac);
											
										}
										
										Userdata udata=new Userdata();
										boolean reg=false;
										if(user!=null) {
											udata=user.getUserdata();
											reg=true;
										}
										
										boolean input=false;
										for(EmailVO ev:emails) {
											if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
												input=true;
												break;
											}
										}
										
										if(input==false) {
											//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
		    								//mail.run();
		    								EmailVO evo=new EmailVO();
		    								evo.setEmail(obj.getString("email"));
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								emails.add(evo);
										}
										//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
	    								//mail.run();
									}
									else {
										//untuk paraf
										
										boolean prf=false;
										DocumentAccess daPrf=null;
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											LogSystem.info(request, "Masuk auto paraf",kelas, refTrx, trxType);
											//System.out.println("masuk auto prf.");
											
											if(user!=null) {
												if(user.isAuto_ttd()) {
													prf=true;
													daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "at");
													da.setAction("at");
												}
												else { 
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "07");
									                jo.put("notif", "user tidak diijinkan untuk prf otomatis");
									                return jo;
												}
											}
											else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
												jo.put("result", "07");
								                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan prf otomatis");
								                return jo;
											}
										}
										else {
											//daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
											if(user!=null) {
												daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
												
											} else {
												daPrf=dAccessDao.findAccessByEmailPrf(obj.getString("email"), id_doc.getId(), "mt");
											}
											da.setAction("mt");
										}
										
										Long idAcc=(long) 0;
										Initial ini=new Initial();
										if(daPrf==null) {
											if(user!=null)da.setEeuser(user);
											da.setDocument(id_doc);
											da.setFlag(false);
											da.setType("initials");
											da.setDate_sign(null);
											da.setEmail(obj.getString("email").toLowerCase());
											da.setName(obj.getString("name"));
											da.setDatetime(new Date());
											da.setAction(obj.getString("aksi_ttd"));
											if(obj.has("visible")) {
												boolean vis=true;
												String v=obj.getString("visible");
												if(v.equalsIgnoreCase("0"))vis=false;
												
												LogSystem.info(request, "visible = "+vis,kelas, refTrx, trxType);
												//System.out.println("visible = "+vis);
												da.setVisible(vis);
											} else {
												da.setVisible(true);
											}
											
											idAcc=dAccessDao.create(da);
											DocumentAccess dac=dAccessDao.findbyId(idAcc);
											if(prf==true) {
												LogSystem.info(request, "id doc access = "+idAcc,kelas, refTrx, trxType);
												//System.out.println("id doc access = "+idAcc);
												lttd.add(dac);
											}
											
											ini.setDoc_access(dac);
											
										}
										else {
											idAcc=daPrf.getId();
											//if(prf==true)lttd.add(daPrf);
											ini.setDoc_access(daPrf);
										}
										
										
										//System.out.println("prf = "+prf);
										LogSystem.info(request, "Paraf = "+prf,kelas, refTrx, trxType);
										ini.setLx(obj.getString("llx"));
										ini.setLy(obj.getString("lly"));
										ini.setRx(obj.getString("urx"));
										ini.setRy(obj.getString("ury"));
										ini.setPage(Integer.valueOf(obj.getString("page")));
										new InitialDao(db).create(ini);
										
										Userdata udata=new Userdata();
										boolean reg=false;
										if(user!=null) {
											udata=user.getUserdata();
											reg=true;
										}
										
										boolean input=false;
										for(EmailVO ev:emails) {
											if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
												input=true;
												break;
											}
										}
										
										if(input==false) {
											//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
		    								//mail.run();
		    								EmailVO evo=new EmailVO();
		    								evo.setEmail(obj.getString("email"));
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								emails.add(evo);
										}
										//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
	    								//mail.run();
								
									}
									
								}
								
								
//								MailSender mail=new MailSender(usr.getName(), obj.getString("email"));
//								Thread mailThr=new Thread(mail);
//								mailThr.start();
								
								kirim=false;

							}
						}
						
						if(jsonRecv.get("send-to")!=null) {
							
							JSONArray sendList=jsonRecv.getJSONArray("send-to");
							
							
							
							for(int i=0; i<sendList.length(); i++) {
								JSONObject obj=(JSONObject) sendList.get(i);

								if(sendList.getString(i).equals(usr.getNick())) continue;
								DocumentAccess da=new DocumentAccess();
//								User user= new UserManager(db).findByUsername(sendList.getString(i));
								User user= new UserManager(db).findByUsername(obj.getString("email"));

								/*change to eeuser*/
//								if(user!=null)da.setUserdata(user.getUserdata());

								if(user!=null)da.setEeuser(user);
								da.setDocument(id_doc);
								da.setFlag(false);
								da.setType("share");
								da.setDate_sign(null);
								da.setEmail(obj.getString("email").toLowerCase());
								da.setName(obj.getString("name"));
								da.setDatetime(new Date());
								Long idAcc=dAccessDao.create(da);
								
								kirim=true;
								
								Userdata udata=new Userdata();
								boolean reg=false;
								if(user!=null) {
									udata=user.getUserdata();
									reg=true;
								}
								
								boolean input=false;
								for(EmailVO ev:emails) {
									if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
										input=true;
										break;
									}
								}
								
								if(input==false) {
									//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
    								//mail.run();
    								EmailVO evo=new EmailVO();
    								evo.setEmail(obj.getString("email"));
    								evo.setUdata(udata);
    								evo.setReg(reg);
    								evo.setNama(obj.getString("name"));
    								evo.setIddocac(idAcc);
    								emails.add(evo);
								}

							}
							
						}
						
						
				  }
				  
				  
				  if(kirim) { // jika hanya share saja maka rubah status menjadi sudah dikirim
						id_doc.setStatus('T');
					}
					
				  if(jsonRecv.has("payment")) {
					  	char pay = jsonRecv.getString("payment").charAt(0);
						id_doc.setPayment(pay);
						docDao.update(id_doc);  
				  }
				  
				  int x=0;
				  System.out.println("List Auto TTD = "+lttd.size());
				  Vector<String> invs=new Vector();
				  KillBillDocumentHttps kdh = null;
				  KillBillPersonalHttps kph = null;
				  try {
					  kdh=new KillBillDocumentHttps(request, refTrx);
					  kph=new KillBillPersonalHttps(request, refTrx);
				  } catch (Exception e) {
					// TODO: handle exception
					  dAccessDao.deleteWhere(id_doc.getId());
						File file=new File(id_doc.getPath()+id_doc.getRename());
						file.delete();
						new DocumentsDao(db).delete(id_doc);
						
			  			jo.put("result", "FE");
						jo.put("notif", "System timeout. silahkan coba kembali.");
						return jo;
				  }
				  for(DocumentAccess docac:lttd) {
					  LogSystem.info(request, "masuk untuk auto TTD dan payment type = "+id_doc.getPayment(),kelas, refTrx, trxType);
					  LogSystem.info(request, "id dokumen akses = "+docac.getId(),kelas, refTrx, trxType);
					  //System.out.println("masuk untuk auto TTD dan payment type = "+id_doc.getPayment());
					  //System.out.println("id dokumen akses = "+docac.getId());
					  boolean potong=false;
					  String inv=null;
					  User u=docac.getEeuser();
					  int jmlttd=lttd.size();
//					  KillBillPersonal kp=new KillBillPersonal();
//					  KillBillDocument kd=new KillBillDocument();
					  
					  
					  InvoiceDao idao=new InvoiceDao(db);
					  if(id_doc.getPayment()=='2') {
						  
							  //int balance=kp.getBalance("MT"+usr.getMitra().getId());
						  		int balance=0;
						  		JSONObject resp=kph.getBalance("MT"+usr.getMitra().getId(), request);
						  		if(resp.has("data")) {
						  			JSONObject data=resp.getJSONObject("data");
									balance = data.getInt("amount");
						  		} else {
						  			dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
						  			jo.put("result", "FE");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
						  		}
							  
							  LogSystem.info(request, "balance 2= "+balance,kelas, refTrx, trxType);
							  //System.out.println("balance 2= "+balance);
							  if(balance<jmlttd) {
								  dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
									//kd.close();
									//kp.close();
									
								  //kalo -401 balikin timeout
								  jo.put("result", "61");
								  jo.put("notif", "Balance mitra tidak cukup.");
								  return jo;
							  }
						  
							  try {  
								  //inv=kp.setTransaction("MT"+usr.getMitra().getId(), 1);
								  JSONObject obj=kph.setTransaction("MT"+usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
								  if(obj.has("result")) {
									  String result=obj.getString("result");
									  if(result.equals("00")) {
											inv=obj.getString("invoiceid");
											balance=obj.getInt("current_balance");
										} else {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "FE");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
								  } else {
									  dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
							  			jo.put("result", "FE");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										return jo;
								  }
								  
							} catch (Exception e) {
								// TODO: handle exception
								try {
									LogSystem.info(request, "coba ke 2 killbill",kelas, refTrx, trxType);
									//inv=kp.setTransaction("MT"+usr.getMitra().getId(), 1);
									JSONObject obj=kph.setTransaction("MT"+usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
									  if(obj.has("result")) {
										  String result=obj.getString("result");
										  if(result.equals("00")) {
												inv=obj.getString("invoiceid");
												balance=obj.getInt("current_balance");
											} else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "FE");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
											}
									  } else {
										  dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "FE");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
									  }
								} catch (Exception e2) {
									// TODO: handle exception
									try {
										LogSystem.info(request, "coba ke 3 killbill",kelas, refTrx, trxType);
										//inv=kp.setTransaction("MT"+usr.getMitra().getId(), 1);
										JSONObject obj=kph.setTransaction("MT"+usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
										  if(obj.has("result")) {
											  String result=obj.getString("result");
											  if(result.equals("00")) {
													inv=obj.getString("invoiceid");
													balance=obj.getInt("current_balance");
												} else {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
										  			jo.put("result", "FE");
													jo.put("notif", "System timeout. silahkan coba kembali.");
													return jo;
												}
										  } else {
											  dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "FE");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
										  }
									} catch (Exception e3) {
										// TODO: handle exception
										LogSystem.error(getClass(), e3,kelas, refTrx, trxType);
										dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										//kp.close();
										//kd.close();
										jo.put("result", "06");
										//jo.put("notif", "Upload file berhasil. Autottd berhasil "+x+" dari "+lttd.size());
										jo.put("notif", "Send dokumen gagal");
										return jo;
									}
								}
								
							}
						  
						  //String[] split=inv.split(" ");
						  invs.add(inv);
						  
						  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
						  ivc.setDatetime(new Date());
						  ivc.setAmount(1);
						  ivc.setEeuser(id_doc.getEeuser());
						  ivc.setExternal_key("MT"+usr.getMitra().getId());
						  ivc.setTenant('1');
						  ivc.setTrx('2');
						  ivc.setKb_invoice(inv);
						  ivc.setDocument(id_doc);
						  ivc.setCur_balance(balance);
						  try
						  {
							  idao.create(ivc);
						  }catch(Exception e)
						  {
							  //kp.close();
							  //kd.close();
							  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
							  dAccessDao.deleteWhere(id_doc.getId());
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
							  jo.put("result", "06");
				              jo.put("notif", "Gagal upload dokumen");
				              return jo;
						  }
						  
						  jmlttd--;
						  potong=true;
					  }
					  else if(id_doc.getPayment()=='3'){
//						  int balance = 0;
//						  try {
//							  balance=kd.getBalance("MT"+usr.getMitra().getId());
//						  }catch(Exception e)
//						  {
//							  //res="FE";
//							  jo.put("result", "FE");
//							  jo.put("notif", "Request timeout, silahkan coba kembali");
//							  LogSystem.error(getClass(), e);
//							  return jo;
//						  }
						  int balance=0;
						  try {						
						
							  //balance=kd.getBalance("MT" + usr.getMitra().getId());
							  JSONObject resp=kdh.getBalance("MT"+usr.getMitra().getId(), request);
						  		if(resp.has("data")) {
						  			JSONObject data=resp.getJSONObject("data");
									balance = data.getInt("amount");
						  		} else {
						  			dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
						  			jo.put("result", "FE");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
						  		}
						  } catch (Exception e) {
								// TODO: handle exception
							  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
							  //kp.close();
								//kd.close();
								dAccessDao.deleteWhere(id_doc.getId());
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
							  jo.put("result", "06");
				              jo.put("notif", "Gagal upload dokumen");
				              return jo;
							}
						  
							  //System.out.println("balance dokumen = "+balance);
						  LogSystem.info(request, "MT : "+usr.getMitra().getId()+"Balance dokumen : "+balance,kelas, refTrx, trxType);
						  if(balance<1) {
							  dAccessDao.deleteWhere(id_doc.getId());
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
							  
								//kp.close();
								//kd.close();
							  jo.put("result", "61");
							  jo.put("notif", "Balance mitra tidak cukup.");
							  return jo;
						  }
						  
						  //List<DocumentAccess> ld=dAccessDao.findByDocSign(id_doc.getId());
						  List<id.co.keriss.consolidate.ee.Invoice> li=idao.findByDoc(id_doc.getId());
						  //if(ld.size()==0) {
						  if(li.size()==0) {
							  try {
								  //inv=kd.setTransaction("MT"+usr.getMitra().getId(), 1);
								  JSONObject obj=kdh.setTransaction("MT"+usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
								  if(obj.has("result")) {
									  String result=obj.getString("result");
									  if(result.equals("00")) {
											inv=obj.getString("invoiceid");
											balance=obj.getInt("current_balance");
										} else {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "FE");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
								  } else {
									  dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
							  			jo.put("result", "FE");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										return jo;
								  }
							} catch (Exception e) {
								// TODO: handle exception
								try {
									LogSystem.info(request, "coba ke 2 killbill",kelas, refTrx, trxType);
//									inv=kd.setTransaction("MT"+usr.getMitra().getId(), 1);
									JSONObject obj=kdh.setTransaction("MT"+usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
									  if(obj.has("result")) {
										  String result=obj.getString("result");
										  if(result.equals("00")) {
												inv=obj.getString("invoiceid");
												balance=obj.getInt("current_balance");
											} else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "FE");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
											}
									  } else {
										  dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "FE");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
									  }
								} catch (Exception e2) {
									// TODO: handle exception
									try {
										LogSystem.info(request, "coba ke 3 killbill",kelas, refTrx, trxType);
//										inv=kd.setTransaction("MT"+usr.getMitra().getId(), 1);
										JSONObject obj=kdh.setTransaction("MT"+usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
										  if(obj.has("result")) {
											  String result=obj.getString("result");
											  if(result.equals("00")) {
													inv=obj.getString("invoiceid");
													balance=obj.getInt("current_balance");
												} else {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
										  			jo.put("result", "FE");
													jo.put("notif", "System timeout. silahkan coba kembali.");
													return jo;
												}
										  } else {
											  dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "FE");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
										  }
									} catch (Exception e3) {
										// TODO: handle exception
										LogSystem.error(getClass(), e3,kelas, refTrx, trxType);
//										kp.close();
//										kd.close();
										dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										  jo.put("result", "06");
							              jo.put("notif", "Gagal upload dokumen");
							              return jo;	
									}
								}							
							}
						
							  
							  //String[] split=inv.split(" ");
							  
							  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
							  ivc.setDatetime(new Date());
							  ivc.setAmount(1);
							  ivc.setEeuser(id_doc.getEeuser());
							  ivc.setExternal_key("MT"+usr.getMitra().getId());
							  ivc.setTenant('2');
							  ivc.setTrx('2');
							  ivc.setKb_invoice(inv);
							  ivc.setDocument(id_doc);
							  ivc.setCur_balance(balance);
							  try
							  {
								  idao.create(ivc);
							  }catch(Exception e)
							  {
								  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//								  kp.close();
//									kd.close();
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
								  jo.put("result", "06");
					              jo.put("notif", "Gagal upload dokumen");
					              return jo;
							  }
							  
							  //inv=split[1];
						  }
						  else {
							  //inv=ld.get(0).getInvoice();
							  inv=li.get(0).getKb_invoice();
						  }
						  
						  potong=true;
					  }
					  
					  if(potong==true) {
						  SignDoc sd=new SignDoc();
						  Vector<DocumentAccess> vda=new Vector<>();
						  vda.add(docac);
						  try {
							  if(sd.signDoc2(u, vda, inv, db, request, refTrx, id_doc, version)) {
//							  if(sd.signDoc(u, docac, inv, db, request, refTrx)) {
								  //docac.setFlag(true);
								  //docac.setDate_sign(new Date());
								  //docac.setInvoice(inv);
								  //dAccessDao.update(docac);
								  //System.out.println("Tandatangan dokumen otomatis berhasil");
								  
								  LogSystem.info(request, "Tandatangan dokumen otomatis berhasil",kelas, refTrx, trxType);
								  List<DocumentAccess> lda=dAccessDao.findByDoc(id_doc.getId().toString());
								  LogSystem.info(request,"size list document = "+lda.size(),kelas, refTrx, trxType);
								  int waiting=0;
								  for(DocumentAccess doc:lda) {
									  if(doc.getType().equals("sign") || doc.getType().equals("initials")) {
										  LogSystem.info(request,"masuk list SIgn or Initials",kelas, refTrx, trxType);
										  if(doc.isFlag()==false)waiting++;
									  }
			 		 				}
								  if(waiting==0) {
									  id_doc.setSign(true);
									  docDao.update(id_doc);
								  }
							  }
							  else {
								  //System.out.println("REVERSAL untuk payment= "+id_doc.getPayment());
								  LogSystem.info(request, "REVERSAL untuk payment= "+id_doc.getPayment(),kelas, refTrx, trxType);
								  JSONObject job=null;
								  if(id_doc.getPayment()=='2') {
										for(String in:invs) {
											try {
												job=kph.reverseTransaction(in, 1, String.valueOf(id_doc.getId()));
												//System.out.println("hasil reversal = "+resp);
												LogSystem.info(request, "hasil reversal = "+job.toString(),kelas, refTrx, trxType);
											} catch (Exception e) {
												// TODO: handle exception
												LogSystem.error(request, "reversal gagal",kelas, refTrx, trxType);
											}
											
											if(job!=null) {
												if(job.getString("result").equals("00")) {
													Invoice invo=new Invoice();
													invo.setAmount(1);
													invo.setDatetime(new Date());
													invo.setDocument(id_doc);
													invo.setExternal_key("MT"+mitra.getId());
													invo.setKb_invoice(in);
													invo.setTenant('1');
													invo.setTrx('3');
													invo.setCur_balance(job.getInt("current_balance"));
													idao.create(invo);
												}
												
											}
										}
									}
								  else if(id_doc.getPayment()=='3') {
									  try {
									  		LogSystem.info(request, "invoicenya adalah = "+inv,kelas, refTrx, trxType);
										  	//System.out.println("invoicenya adalah = "+inv);
										  	if(inv!=null || !inv.equals("")) {
										  		//System.out.println("MASUK PAK EKO");
										  		LogSystem.info(request, "MASUK PAK EKO",kelas, refTrx, trxType);
//										  		String resp=kd.reverseTransaction(inv);
										  		job=kdh.reverseTransaction(inv, 1, String.valueOf(id_doc.getId()));
										  		//System.out.println("hasil reversal = "+resp);
										  		LogSystem.info(request, "hasil reversal = "+job.toString(),kelas, refTrx, trxType);
										  		//idao.deleteWhere(inv);
										  	}
										} catch (Exception e) {
											// TODO: handle exception
											LogSystem.error(request, "reversal gagal",kelas, refTrx, trxType);
										}
									  	
									  if(job!=null) {
											if(job.getString("result").equals("00")) {
												Invoice invo=new Invoice();
												invo.setAmount(1);
												invo.setDatetime(new Date());
												invo.setDocument(id_doc);
												invo.setExternal_key("MT"+mitra.getId());
												invo.setKb_invoice(inv);
												invo.setTenant('2');
												invo.setTrx('3');
												invo.setCur_balance(job.getInt("current_balance"));
												idao.create(invo);
											}
									  	}
									}
									
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									
									id_doc.setDelete(true);
									new DocumentsDao(db).update(id_doc);
//									kp.close();
//									kd.close();
									jo.put("result", "06");
									//jo.put("notif", "Upload file berhasil. Autottd berhasil "+x+" dari "+lttd.size());
									jo.put("notif", "Send dokumen gagal");
									return jo;
							  }
							  
							  /*
							  if(inv==null) {
								  inv=dAccessDao.findbyDocSign(id_doc.getId()).getInvoice();
							  }
							  */
							  
							  
						} catch (Exception e) {
							// TODO: handle exception
//							kp.close();
//							kd.close();
							e.printStackTrace();
							if(id_doc.getPayment()=='2') {
								for(String in:invs) {
									JSONObject job=kph.reverseTransaction(in, 1, String.valueOf(id_doc.getId()));
									if(job!=null) {
										if(job.getString("result").equals("00")) {
											Invoice invo=new Invoice();
											invo.setAmount(1);
											invo.setDatetime(new Date());
											invo.setDocument(id_doc);
											invo.setExternal_key("MT"+mitra.getId());
											invo.setKb_invoice(in);
											invo.setTenant('1');
											invo.setTrx('3');
											invo.setCur_balance(job.getInt("current_balance"));
											idao.create(invo);
										}
										
									}
									//idao.deleteWhere(in);
								}
							}
						  else if(id_doc.getPayment()=='3') {
								//if(x==0 && inv!=null)kd.reverseTransaction(inv);
							  	if(inv!=null) {
//							  		kd.reverseTransaction(inv);
							  		JSONObject job=kdh.reverseTransaction(inv, 1, String.valueOf(id_doc.getId()));
							  		if(job!=null) {
										if(job.getString("result").equals("00")) {
											Invoice invo=new Invoice();
											invo.setAmount(1);
											invo.setDatetime(new Date());
											invo.setDocument(id_doc);
											invo.setExternal_key("MT"+mitra.getId());
											invo.setKb_invoice(inv);
											invo.setTenant('2');
											invo.setTrx('3');
											invo.setCur_balance(job.getInt("current_balance"));
											idao.create(invo);
										}
								  	}
							  		//idao.deleteWhere(inv);
							  	}
							}
							
							dAccessDao.deleteWhere(id_doc.getId());
							File file=new File(id_doc.getPath()+id_doc.getRename());
							file.delete();
							id_doc.setDelete(true);
							new DocumentsDao(db).update(id_doc);
							
							jo.put("result", "06");
							//jo.put("notif", "Upload file berhasil. Autottd berhasil "+x+" dari "+lttd.size());
							jo.put("notif", "Send dokumen gagal");
							return jo;
						}
						  
					  }
					  x++;
//					  kp.close();
//					  kd.close();
				  }
				  
				  res="00";
				  for(EmailVO email:emails) {
					  	LogSystem.info(request, "Email = "+email.getEmail(),kelas, refTrx, trxType);
						//System.out.println("email = "+email.getEmail());
						//MailSender mail=new MailSender(email.getEmail(),email.getUdata(),email.isReg(),email.getNama());
						//mail.run();
						
						String docs ="";
						String link ="";
						try {
					
							   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
							   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+email.getIddocac();
							     //+ URLEncoder.encode(docs, "UTF-8");
							} catch (Exception e1) {
							   // TODO Auto-generated catch block
							   e1.printStackTrace();
							}
						
						SendTerimaDoc std=new SendTerimaDoc(request, refTrx, kelas, trxType);
//						if(email.isReg())std.kirim(email.getUdata().getNama(), String.valueOf(email.getUdata().getJk()), email.getEmail(), aVerf.getEeuser().getName(), String.valueOf(aVerf.getEeuser().getUserdata().getJk()), link);
//						else std.kirim(email.getNama(), "", email.getEmail(), aVerf.getEeuser().getName(), String.valueOf(aVerf.getEeuser().getUserdata().getJk()), link);
						
						if(mitra.isNotifikasi()) {
							if(email.isReg())std.kirim(email.getUdata().getNama(), String.valueOf(email.getUdata().getJk()), email.getEmail(), mitra.getName(), "o", link, String.valueOf(mitra.getId()), "");
							else std.kirim(email.getNama(), "", email.getEmail(), mitra.getName(), "o", link, String.valueOf(mitra.getId()),  "");
						}
						
					}
				  
				  jo.put("notif", "Kirim dokumen berhasil.");
				  userRecv=usr;
				  id_doc.setStatus('T');
				  docDao.update(id_doc);
				  
			}else {
				jo=aVerf.setResponFailed(jo);
			}
			
			
		}
		catch (Exception e) {
			// TODO: handle exception
			if(id_doc!=null) {
				DocumentsAccessDao dad=new DocumentsAccessDao(db);
				dad.deleteWhere(id_doc.getId());
				File file=new File(id_doc.getPath()+id_doc.getRename());
				file.delete();
				id_doc.setDelete(true);
				new DocumentsDao(db).update(id_doc);
				
				List<Invoice> linv=new InvoiceDao(db).findByDoc(id_doc.getId());
				KillBillDocumentHttps kdh = null;
				  KillBillPersonalHttps kph = null;
				  try {
					  kdh=new KillBillDocumentHttps(request, refTrx);
					  kph=new KillBillPersonalHttps(request, refTrx);
				  } catch (Exception e1) {
						
			  			jo.put("result", "FE");
						jo.put("notif", "System timeout. silahkan coba kembali.");
						return jo;
				  }
				  
				  for(Invoice in:linv) {
					  if(in.getTenant()==1) {
						  try {
							kph.reverseTransaction(in.getKb_invoice(), 1, String.valueOf(id_doc.getId()));
						} catch (KillBillClientException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					  } else if(in.getTenant()==2) {
						  try {
								kdh.reverseTransaction(in.getKb_invoice(), 1, String.valueOf(id_doc.getId()));
							} catch (KillBillClientException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					  }
				  }
				
			}
			res="28";
			jo.put("notif", "Request API tidak lengkap.");
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
		}
        if(!jo.has("result"))jo.put("result", res);
	   return jo;
	}
	
	class MailSender{

		String email;
		Userdata udata;
		boolean reg;
		String name;
		
		public MailSender(String email, Userdata udata, boolean reg, String name) {
			this.email=email;
			this.udata=udata;
			this.reg=reg;
			this.name=name;
		}
		public void run() {
			//new SendMailSSL().sendMailPreregisterMitra(name, email, id);
			if(reg==true) {
				new SendMailSSL().sendMailFileaReqSign(email, udata, email);
			}
			else {
				new SendMailSSL().sendMailFileaReqSignNotReg(email, name, email);
			}
		}
		
	}
	
	public boolean verifyAllEqualUsingStream(List<String> list) {
	    return list.stream()
	      .distinct()
	      .count() <= 1;
	}
		
}
