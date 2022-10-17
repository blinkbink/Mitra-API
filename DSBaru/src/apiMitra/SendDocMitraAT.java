package apiMitra;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.util.encoders.Base64;
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
import api.email.SendTerimaDocCC;
import api.log.ActivityLog;
import api.qrtext.QRTextEncrypt;
import apiMitra2020.ProsesSign;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.BlockAutoReplaceDocDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.FormatPDFDao;
import id.co.keriss.consolidate.dao.InitialDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.dao.LetakTtdDao;
import id.co.keriss.consolidate.dao.SealDocAccessDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserSealDao;
import id.co.keriss.consolidate.ee.BlockAutoReplaceDoc;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.LetakTtd;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.SealDocAccess;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.UserSeal;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VO.EmailVO;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithFTP;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class SendDocMitraAT extends ActionSupport {

	//static String basepath="/opt/data-DS/UploadFile/";
	//static String basepathPreReg="/opt/data-DS/PreReg/";
	//Date tgl;
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx;
	String kelas="apiMitra.SendDocMitraAT";
	String trxType="SEND-DOC";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="DOC"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		String filetype=null;
		long filesize=0;
		List <FileItem> fileSave=new ArrayList<FileItem>();
		List<FileItem> fileItems=null;
		
		Mitra mitra=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", kelas, refTrx, trxType);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					
					return;
				}
				// multipart form
				else {
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
						
	//					TokenMitra tm=tmd.findByToken(token.toLowerCase());
						
						tm=tmd.findByToken(token.toLowerCase());
						}catch(Exception e)
						{
							JSONObject jo=new JSONObject();
							jo.put("result", "91");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.error(request, e.toString() , kelas, refTrx, trxType);
							return;
						}
						
						
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada",kelas, refTrx, trxType);
							mitra=tm.getMitra();
						} else {
							LogSystem.error(request, "Token tidak ada",kelas, refTrx, trxType);
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
					LogSystem.info(request, "authorization adalah = "+request.getHeader("authorization"),kelas, refTrx, trxType);

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
		        		jo.put("result", "05");
						jo.put("notif", "Request Data tidak ditemukan");
						LogSystem.response(request, jo, kelas, refTrx, trxType);
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

								 filedata=fileItem;
								 filename=fileItem.getName();
								 filetype=fileItem.getContentType();
								 filesize=fileItem.getSize();
								 //System.out.println("Format File: " + filetype.split("/")[1]);
							 }
						}
					}
				
			 String process=request.getRequestURI().split("/")[2];
			 LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType+"/"+mitra.getName());
	         //System.out.println("PATH :"+request.getRequestURI());

	         LogSystem.request(request, fileItems,kelas, refTrx, trxType+"/"+mitra.getName());
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         if(mitra!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
	        		 LogSystem.error(request, "Parameter userid tidak ditemukan",kelas, refTrx, trxType);
	        		 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "Parameter userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
	        	 }
	        	 
	        	 String userid=jsonRecv.getString("userid").toLowerCase();
		         UserManager user=new UserManager(getDB(context));
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
		        	 }
		        	 else {
		        		 LogSystem.error(request, "Token dan mitra tidak valid",kelas, refTrx, trxType);
		        		 JSONObject jo=new JSONObject();
						 jo.put("result", "55");
						 jo.put("notif", "Token dan Userid tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						 LogSystem.response(request, jo, kelas, refTrx, trxType);
						 return;
		        	 }
		         }
		         else {
		        	 LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "55");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
		         }
	         }
	         

	         
	         JSONObject jo = null;
	        
	         jo = docMitra(mitra, useradmin, jsonRecv, filedata, filename, request, context, refTrx); 
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo,kelas, refTrx, trxType+"/"+mitra.getName());

		}catch (Exception e) {
			LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
//			error (context, e.toString());
//            context.getSyslog().error (e);
//			log.error(e);
            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data request tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				LogSystem.error(request, e1.toString(), kelas, refTrx, trxType);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
		}
	}
	
	JSONObject docMitra(Mitra mitratoken, User useradmin, JSONObject jsonRecv, FileItem dataFile, String filename,HttpServletRequest  request,JPublishContext context, String refTrx) throws JSONException{
		DB db = getDB(context);
		String format_doc = "";
        JSONObject jo=new JSONObject();
        String res="06";
        //String signature=null;
        SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
        Documents id_doc=null;
        boolean kirim=false;
        DocumentsDao docDao = new DocumentsDao(db);
        Boolean sequence=false;
        Vector<Integer> list_seq=new Vector<>();
        int countMeterai = 0; 
		int countAutoSign = 0; 
		SealDocAccess sda=new SealDocAccess();
		
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
		        		jo.put("result", "07");
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
	        	
				  FileProcessor fProc=new FileProcessor(refTrx);
				  int jmlpage=0;
				  
				  //if(fProc.uploadFileMitra(request, db, usr, dataFile, jsonRecv.getString("document_id"))) {
				  if(jsonRecv.getString("document_id").equals("") || jsonRecv.getString("document_id")==null) {
					  jo.put("result", "05");
		              jo.put("notif", "document id kosong");
		              
		          	try {
			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, document id kosong", Long.toString(usr.getId()), null, null, null, null,null);
			        }catch(Exception e)
			        {
			        	e.printStackTrace();
			        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
			        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
			        }
		              return jo;
				  }
				  
				  DB db2 =new DB();
				  db2.open();
				  DocumentsDao docDao2 = new  DocumentsDao(db2);
				  BlockAutoReplaceDocDao replace=new BlockAutoReplaceDocDao(db2);
				  
				  Transaction tx=db2.beginTransaction();
				  List<Documents> ldoc=docDao2.findByDocIdMitraFalse(jsonRecv.getString("document_id"), mitra.getId());
				  BlockAutoReplaceDoc breplace=replace.findByMitra(mitra.getId());
				  if(breplace!=null && ldoc.size()>0) {
					  
						  jo.put("result", "17");
			              jo.put("notif", "Document_id sudah digunakan");
			              
			          	try {
				        	ActivityLog logSystem = new ActivityLog(request, refTrx);
				        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen " + jsonRecv.getString("document_id"), Long.toString(usr.getId()), null, null, null, null,null);
				        }catch(Exception e)
				        {
				        	e.printStackTrace();
				        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
				        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
				        }
			          	
			          	if(db2!=null) {
							  db2.session().close();
							  db2.close();
						  }
			              return jo;
				  }
				  
				  
				  LogSystem.info(request, "LIST LDOC YANG EXIST " + ldoc, kelas, refTrx, trxType);
				  LogSystem.info(request, "ID MITRANYA " + mitra.getId(), kelas, refTrx, trxType);
				 
				  if(ldoc.size()>0) {
					  for(Documents docu:ldoc) {
						  LogSystem.info(request, "ISI DOCU " + docu.getId(), kelas, refTrx, trxType);
						  docu.setDelete(true);
						  docDao2.update2(docu);
					  }
				  }
				  
				  tx.commit();
				  if(db2!=null) {
					  db2.session().close();
					  db2.close();
				  }
				  
				  JSONObject saveFile=fProc.uploadFILEnQRMitra2(request, db, usr, dataFile, jsonRecv.getString("document_id"), jsonRecv);
				  if(saveFile!=null) {
					  if(saveFile.getString("rc").equals("00")) {
						  res="00";
	 					  jmlpage=fProc.getJmlPage();
	 					  LogSystem.info(request, "Upload Successfully",kelas, refTrx, trxType);
					  } else if(saveFile.getString("rc").equals("01")) {
						  jo.put("result", "FE");
			              jo.put("notif", "Format dokumen harus pdf");
			              
			          	try {
				        	ActivityLog logSystem = new ActivityLog(request, refTrx);
				        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, Format dokumen harus pdf", Long.toString(usr.getId()), null, null, null, null,null);
				        }catch(Exception e)
				        {
				        	e.printStackTrace();
				        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
				        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
				        }
			          	
			              return jo;
					  } else {
						  jo.put("result", "91");
						  jo.put("notif", "Sistem timeout, mohon ulangi proses upload document.");
						  
						  try {
					        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, System timeout", Long.toString(usr.getId()), null, null, null, null,null);
					        }catch(Exception e)
					        {
					        	e.printStackTrace();
					        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
					        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
					        }
						  return jo;
					  }
				  } else {
					  jo.put("result", "91");
					  jo.put("notif", "Sistem timeout, mohon ulangi proses upload document.");
					  
					  try {
				        	ActivityLog logSystem = new ActivityLog(request, refTrx);
				        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, System timeout", Long.toString(usr.getId()), null, null, null, null,null);
				        }catch(Exception e)
				        {
				        	e.printStackTrace();
				        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
				        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
				        }
					  return jo;
				  }
				  
				  //id_doc=new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename());
				  id_doc=fProc.getDc();
				  if(jsonRecv.has("redirect")) {
					  if(id_doc.getRedirect()==false ) {
						  docDao.delete(id_doc);
						  	jo.put("result", "FE");
		                    jo.put("notif", "redirect field harus diisi true");
		                    
		                    try {
					        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, Redirect field harus diisi true", Long.toString(usr.getId()), null, null, null, null,null);
					        }catch(Exception e)
					        {
					        	e.printStackTrace();
					        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
					        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
					        }
		                    
		                    
		                    return jo;
					  }
					  
					  if(mitra.getSigning_redirect()==null) {
						  docDao.delete(id_doc);
						  	jo.put("result", "08");
		                    jo.put("notif", "Anda belum memasukkan link redirect anda.");
		                    
		                    try {
					        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, Belum memasukan link redirect", Long.toString(usr.getId()), null, null, null, null,null);
					        }catch(Exception e)
					        {
					        	e.printStackTrace();
					        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
					        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
					        }
		                    
		                    return jo;
					  }
				  }
				  
				  Vector<DocumentAccess> lttd=new Vector();
				  Vector<EmailVO> emails=new Vector<>();
				  Vector<EmailVO> emailscc=new Vector<>();
				  DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
				  
				  boolean seal = false;
				  boolean meterai = false;
				  String version="v1";
				  ArrayList<String> listCert = new ArrayList<String>();
				  if(id_doc!=null) {
						/*
						int clear=dAccessDao.clearAkses(id_doc.getId());
						//System.out.println("clear access id: "+id_doc.getId()+", "+clear+" rows");
						LogSystem.info(request, "clear access id: "+id_doc.getId()+", "+clear+" rows",kelas, refTrx, trxType);
						*/
						if(!jsonRecv.has("format_doc")) 
						{
							jsonRecv.put("format_doc", "");
						}
						
						if(jsonRecv.has("meterai"))
						{
							countMeterai++;
						}
						
						if(jsonRecv.get("req-sign")!=null) {
							JSONArray sendList=jsonRecv.getJSONArray("req-sign");

							//Check sertifikat ke KMS
							for (int c=0 ; c < sendList.length() ; c++)
							{
								JSONObject expired = (JSONObject) sendList.get(c);
								if(expired.has("aksi_ttd")) {
									
									if (expired.getString("aksi_ttd").equalsIgnoreCase("at"))
									{
								LogSystem.info(request, "USER CEK " + expired.getString("email").toLowerCase(), kelas, refTrx, trxType);
								User userCert= new UserManager(db).findByUsername(expired.getString("email").toLowerCase());
								LogSystem.info(request, "USER " + userCert, kelas, refTrx, trxType);
								String userExpired = "";
								
								if(countMeterai > 0)
								{
									//Check jika lokasi ttd sama dengan posisi meterai
									String mllx = jsonRecv.getJSONObject("meterai").getString("llx");
									String mlly = jsonRecv.getJSONObject("meterai").getString("lly");
									String murx = jsonRecv.getJSONObject("meterai").getString("urx");
									String mury = jsonRecv.getJSONObject("meterai").getString("ury");
									
									String tllx = expired.getString("llx");
									String tlly = expired.getString("lly");
									String turx = expired.getString("urx");
									String tury = expired.getString("ury");
									
									if(mllx.equalsIgnoreCase(tllx) && mlly.equalsIgnoreCase(tlly) && murx.equalsIgnoreCase(turx) && mury.equalsIgnoreCase(tury))
									{
										LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
										jo.put("result", "03");
										jo.put("notif", "Kirim dokumen gagal");
										jo.put("info", "Lokasi meterai tidak boleh sama dengan lokasi tandatangan");
										
										return jo;
									}
								}
								
								if(expired.has("user"))
								{
									userExpired = expired.getString("user").substring(0, 3);
								}
								else
								{
									userExpired = "ttd";
								}
									
								
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
									
									if(cert.getString("result").length() > 3 || cert.getString("result").equals(""))
									{
										LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
										jo.put("result", "15");
										jo.put("notif", "Kirim dokumen gagal");
										
										return jo;
									}
									
											countAutoSign++;
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
							if(sendList.length()==0) {
								jo.put("result", "05");
								jo.put("notif", "field req-sign tidak ditemukan");
								
								 try {
							        	ActivityLog logSystem = new ActivityLog(request, refTrx);
							        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, field req-sign tidak ditemukan", Long.toString(usr.getId()), null, null, null, null,null);
							        }catch(Exception e)
							        {
							        	e.printStackTrace();
							        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
							        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
							        }
								 
								return jo;
							}

							LogSystem.info(request, "Format doc = "+jsonRecv.getString("format_doc").length(),kelas, refTrx, trxType);
							
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
										dAccessDao.deleteWhere(id_doc.getId());
										new DocumentsDao(db).delete(id_doc);
										
										jo.put("result", "06");
										jo.put("notif", "User tidak diperkenankan menerima dokumen dari mitra. Silahkan registrasi ulang atau konfirmasi ke DigiSign.");
										
										 try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
									        }
										 
										return jo;
										 
									  }
								}
								
								if(jsonRecv.has("sequence_option")) {
									if(jsonRecv.getBoolean("sequence_option")==true) {
										sequence=true;
										if(!obj.has("signing_seq")) {
											jo.put("result", "FE");
											jo.put("notif", "sequence option = true, harus menyertakan signing_seq");
											
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											dAccessDao.deleteWhere(id_doc.getId());
											new DocumentsDao(db).delete(id_doc);
											
											 try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, "+ jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
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
								
								/*change to eeuser*/
//								if(user!=null)da.setUserdata(user.getUserdata());
								
								//System.out.println("size json object = "+jsonRecv.getString("format_doc").length());
								if(!obj.has("aksi_ttd")||obj.getString("aksi_ttd").equals(""))obj.put("aksi_ttd", "mt");
								if(!obj.has("user")||obj.getString("user").equals(""))obj.put("user", "ttd0");
								
								if(jsonRecv.getString("format_doc").length()>0) {
									Long idmitra = usr.getMitra().getId();
									//System.out.println("id mitra = "+idmitra);
									LogSystem.info(request, "id mitra = "+idmitra,kelas, refTrx, trxType);
									FormatPdf fd=fdao.findFormatPdf(jsonRecv.getString("format_doc"), idmitra);
									if(fd!=null) {
										
										if(jsonRecv.has("format_doc"))
										{
											LogSystem.info(request, "Format doc : " + jsonRecv.getString("format_doc"),kelas, refTrx, trxType);
											
											if(!jsonRecv.getString("format_doc").equals(""))
											{
												format_doc = jsonRecv.getString("format_doc");
											}
										}
										
										//System.out.println("ttd ke = "+obj.getString("user").substring(3));
										//System.out.println("USERRRRRRR = "+obj.getString("user").substring(0, 3));
										LogSystem.info(request, "USERRRRRRR = "+obj.getString("user").substring(0, 3),kelas, refTrx, trxType);
										
										if(obj.getString("user").substring(0, 3).equalsIgnoreCase("ttd")) {
											LetakTtd lt=ltdao.findLetakTtd(obj.getString("user").substring(3), fd.getId());
											if(lt!=null) {
												boolean sign=false;
												if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
													//System.out.println("masuk auto ttd.");
													LogSystem.info(request, "Masuk auto ttd.",kelas, refTrx, trxType);
													if(user!=null) {
														
														if(user.isAuto_ttd()) {
															String key = user.getKey_at_ttd();
															if(key.equalsIgnoreCase(obj.getString("kuser")))sign=true;
															else {
																File file=new File(id_doc.getPath()+id_doc.getRename());
																file.delete();
																new DocumentsDao(db).delete(id_doc);
																jo.put("result", "83");
												                jo.put("notif", "key user ttd otomatis tidak valid");
												                
												                try {
														        	ActivityLog logSystem = new ActivityLog(request, refTrx);
														        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
														        }catch(Exception e)
														        {
														        	e.printStackTrace();
														        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
														        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
														        }
												                return jo;
															}
														}
														else {
															//System.out.println("delete data");
															LogSystem.info(request, "Delete aja",kelas, refTrx, trxType);
															dAccessDao.deleteWhere(id_doc.getId());
															File file=new File(id_doc.getPath()+id_doc.getRename());
															file.delete();
															new DocumentsDao(db).delete(id_doc);
															
															jo.put("result", "07");
											                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
											                
											                try {
													        	ActivityLog logSystem = new ActivityLog(request, refTrx);
													        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
													        }catch(Exception e)
													        {
													        	e.printStackTrace();
													        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
													        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
													        }
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
										                
										                try {
												        	ActivityLog logSystem = new ActivityLog(request, refTrx);
												        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
												        }catch(Exception e)
												        {
												        	e.printStackTrace();
												        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
												        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
												        }
										                
										                return jo;
													}
													
												}
												if(!isValid(obj.getString("email").toLowerCase().trim())) {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "FE");
									                jo.put("notif", "Format email salah.");
									                
									                try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
											        }
									                return jo;
												}
												if(user!=null)da.setEeuser(user);
												da.setDocument(id_doc);
												da.setFlag(false);
												da.setType("sign");
												da.setDate_sign(null);
												da.setEmail(obj.getString("email").toLowerCase());
												if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "FE");
									                jo.put("notif", "Format name salah.");
									                
									                try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
											        }
									                return jo;
												}
												da.setName(obj.getString("name").trim());
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
													LogSystem.info(request, "visible = "+vis,kelas, refTrx, trxType);
													//System.out.println("visible = "+vis);
													da.setVisible(vis);
												} else {
													da.setVisible(true);
												}
												
												Long idAcc = null;
												
												try
												{
													idAcc=dAccessDao.create(da);
													if(idAcc == null)
													{
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														
														  LogSystem.error(request, "idAcc null",kelas, refTrx, trxType);
														  jo.put("result", "06");
											              jo.put("notif", "Kirim dokumen gagal");
											              
											              try {
													        	ActivityLog logSystem = new ActivityLog(request, refTrx);
													        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen", Long.toString(usr.getId()), null, null, null, null,null);
													        }catch(Exception e)
													        {
													        	e.printStackTrace();
													        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
													        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
													        }
											              return jo;
													}	
												}catch(Exception e)
												{
													LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													  LogSystem.error(getClass(), e, kelas, refTrx, trxType);
													  jo.put("result", "06");
										              jo.put("notif", "Kirim dokumen gagal");
										              return jo;
												}
												
												LogSystem.info(request, "Sign = "+sign,kelas, refTrx, trxType);
												//System.out.println("sign = "+sign);
												if(sign==true) {
													LogSystem.info(request, "id doc access = "+idAcc,kelas, refTrx, trxType);
													//System.out.println("id doc access = "+idAcc);
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
				    								evo.setEmail(obj.getString("email").toLowerCase());
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
								                
								                try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
										        }
								                
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
													//System.out.println("masuk auto prf.");
													LogSystem.info(request, "Masuk auto paraf",kelas, refTrx, trxType);
													
													if(user!=null) {
														if(user.isAuto_ttd()) {
															String key = user.getKey_at_ttd();
															if(key.equalsIgnoreCase(obj.getString("kuser"))) {
																prf=true;
																//System.out.println("Paraf true");
																LogSystem.info(request, "Paraf true",kelas, refTrx, trxType);
																daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "at");
																da.setAction("at");
															}
															else {
																File file=new File(id_doc.getPath()+id_doc.getRename());
																file.delete();
																new DocumentsDao(db).delete(id_doc);
																jo.put("result", "83");
												                jo.put("notif", "key user ttd otomatis tidak valid");
												                
												                try {
														        	ActivityLog logSystem = new ActivityLog(request, refTrx);
														        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
														        }catch(Exception e)
														        {
														        	e.printStackTrace();
														        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
														        	LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
														        }
												                return jo;
															}
															
														}
														else { 
															dAccessDao.deleteWhere(id_doc.getId());
															File file=new File(id_doc.getPath()+id_doc.getRename());
															file.delete();
															new DocumentsDao(db).delete(id_doc);
															
															jo.put("result", "07");
											                jo.put("notif", "user tidak diijinkan untuk prf otomatis");
											                
											                try {
													        	ActivityLog logSystem = new ActivityLog(request, refTrx);
													        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
													        }catch(Exception e)
													        {
													        	e.printStackTrace();
													        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
													        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
													        }
											                
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
										                
										                try {
												        	ActivityLog logSystem = new ActivityLog(request, refTrx);
												        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
												        }catch(Exception e)
												        {
												        	e.printStackTrace();
												        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
												        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
												        }
										                return jo;
													}
													
												}
												else {
													if(user!=null) {
														daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
														
													} else {
														daPrf=dAccessDao.findAccessByEmailPrf(obj.getString("email").toLowerCase(), id_doc.getId(), "mt");
													}
													da.setAction("mt");
												}
												
												Long idAcc=(long) 0;
												if(!isValid(obj.getString("email").toLowerCase())) {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "FE");
									                jo.put("notif", "Format email salah.");
									                
									                try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											        }
									                
									                return jo;
												}
												Initial ini=new Initial();
												if(daPrf==null) {
													if(user!=null)da.setEeuser(user);
													da.setDocument(id_doc);
													da.setFlag(false);
													da.setType("initials");
													da.setDate_sign(null);
													da.setEmail(obj.getString("email").toLowerCase().trim());
													if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
														dAccessDao.deleteWhere(id_doc.getId());
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														
														jo.put("result", "FE");
										                jo.put("notif", "Format name salah.");
										                
										                try {
												        	ActivityLog logSystem = new ActivityLog(request, refTrx);
												        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
												        }catch(Exception e)
												        {
												        	e.printStackTrace();
												        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
												        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
												        }
										                return jo;
													}
													da.setName(obj.getString("name").trim());
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
													
													try
													{
														idAcc=dAccessDao.create(da);
														if(idAcc==null || idAcc ==0)
														{
															File file=new File(id_doc.getPath()+id_doc.getRename());
															file.delete();
															new DocumentsDao(db).delete(id_doc);
															  LogSystem.info(request, "idAcc null / 0",kelas, refTrx, trxType);
															  jo.put("result", "06");
												              jo.put("notif", "Kirim dokumen gagal");
												              return jo;
														}
													}catch(Exception e)
													{
														LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
														  jo.put("result", "06");
											              jo.put("notif", "Kirim dokumen gagal");
											              return jo;
													}
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
												
												LogSystem.info(request, "prf = "+prf,kelas, refTrx, trxType);
												//System.out.println("prf = "+prf);
												
												ini.setLx(lt.getLx());
												ini.setLy(lt.getLy());
												ini.setRx(lt.getRx());
												ini.setRy(lt.getRy());
												ini.setPage(lt.getPage());
												
												try
												{
													new InitialDao(db).create(ini);
												}catch(Exception e)
												{
													LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
													  jo.put("result", "06");
										              jo.put("notif", "Kirim dokumen gagal");
										              return jo;	
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
				    								evo.setEmail(obj.getString("email").toLowerCase());
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
								                
								                try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
										        }
								                
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
						                
						                try {
								        	ActivityLog logSystem = new ActivityLog(request, refTrx);
								        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
								        }catch(Exception e)
								        {
								        	e.printStackTrace();
								        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
								        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
								        }
						                
						                return jo;
									}
								} else {
									String actUser="ttd";
									if(!obj.has("user")) {
										obj.put("user", "ttd");
									} else {
										if(obj.getString("user").length()<3) {
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "FE");
							                jo.put("notif", "field 'USER', Harus diisi dengan 'ttd' atau 'prf'.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										} else {
											if(obj.getString("user").length()>3) actUser=obj.getString("user").substring(0, 3);
											else actUser=obj.getString("user").substring(0);
												

											if(actUser.equalsIgnoreCase("ttd")||actUser.equalsIgnoreCase("prf")||actUser.equalsIgnoreCase("app")||actUser.equalsIgnoreCase("sgl")||actUser.equalsIgnoreCase("mtr")) {
												LogSystem.info(request, "user " + obj.getString("email") + " " +actUser + " halaman " + obj.getString("page"),kelas, refTrx, trxType);
											} else {
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												jo.put("result", "FE");
								                jo.put("notif", "field 'USER', Harus diisi dengan 'ttd' atau 'prf'.");
								                
								                try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
										        }
								                
								                return jo;
											}
										}
									}
									
//									if(obj.getString("user").length()>3)actUser=obj.getString("user").substring(0, 3);
//									else actUser=obj.getString("user").substring(0);
									
									if(actUser.equalsIgnoreCase("ttd")||actUser.equalsIgnoreCase("")) {
										boolean sign=false;
										
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											da.setAction("at");
											if(user!=null) {
												if(user.isAuto_ttd()) {
													String key = user.getKey_at_ttd();
													if(key.equalsIgnoreCase(obj.getString("kuser")))sign=true;
													else {
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														jo.put("result", "83");
										                jo.put("notif", "key user ttd otomatis tidak valid");
										                
										                try {
												        	ActivityLog logSystem = new ActivityLog(request, refTrx);
												        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
												        }catch(Exception e)
												        {
												        	e.printStackTrace();
												        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
												        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
												        }
										                
										                return jo;
													}
												}
												else {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													if(id_doc != null)
													{
														new DocumentsDao(db).delete(id_doc);
													}
													
													jo.put("result", "07");
									                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
									                
									                try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											        }
									                
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
								                
								                try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
										        }
								                
								                return jo;
											}
											
										} else {
											da.setAction("mt");
										}
										
										if(user!=null)da.setEeuser(user);
										if(Integer.parseInt(obj.getString("page"))>jmlpage) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Halaman tandatangan, melebihi dari jumlah halaman dokumen");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										
										if(!isValid(obj.getString("email").toLowerCase())) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format email salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										da.setDocument(id_doc);
										da.setFlag(false);
										da.setType("sign");
										da.setDate_sign(null);
										da.setEmail(obj.getString("email").toLowerCase().trim());
										if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format name salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										da.setName(obj.getString("name").trim());
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
											LogSystem.info(request, "visible = "+vis,kelas, refTrx, trxType);
											//System.out.println("visible = "+vis);
											da.setVisible(vis);
										} else {
											da.setVisible(true);
										}
										if(sequence) {
											//if(obj.has("signing_seq")) {
												//sequence=true;
												da.setSequence_no(obj.getInt("signing_seq"));
												list_seq.add(obj.getInt("signing_seq"));
											//}
										}
										
										
										Long idAcc = null;
										try
										{
											idAcc=dAccessDao.create(da);
											
											if(idAcc == null)
											{
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												LogSystem.error(request, "idAcc null",kelas, refTrx, trxType); 
												jo.put("result", "06");
								                jo.put("notif", "Kirim dokumen gagal"); 
								                
								               
								                
								                return jo;
											}
										}catch(Exception e)
										{
											LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "06");
							                jo.put("notif", "Kirim dokumen gagal");
							                return jo;
										}
										
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
		    								evo.setEmail(obj.getString("email").toLowerCase());
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								emails.add(evo);
										}
										//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
	    								//mail.run();
									}
									else if(actUser.equalsIgnoreCase("prf")){
										//untuk paraf
										
										boolean prf=false;
										DocumentAccess daPrf=null;
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											
											da.setAction("at");
											LogSystem.info(request, "Masuk auto paraf",kelas, refTrx, trxType);
											
											if(user!=null) {
												if(user.isAuto_ttd()) {
													String key = user.getKey_at_ttd();
													if(key.equalsIgnoreCase(obj.getString("kuser"))) {
														prf=true;
														daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "at");
														da.setAction("at");
													}
													else {
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														jo.put("result", "83");
										                jo.put("notif", "key user ttd otomatis tidak valid");
										                
										                try {
												        	ActivityLog logSystem = new ActivityLog(request, refTrx);
												        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
												        }catch(Exception e)
												        {
												        	e.printStackTrace();
												        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
												        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
												        }
										                
										                return jo;
													}
													
												}
												else { 
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "07");
									                jo.put("notif", "user tidak diijinkan untuk prf otomatis");
									                
									                try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											        }
									                
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
								                
								                try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
										        }
								                
								                return jo;
											}
											
										}
										else {
											//daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
											if(user!=null) {
												daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
												
											} else {
												daPrf=dAccessDao.findAccessByEmailPrf(obj.getString("email").toLowerCase(), id_doc.getId(), "mt");
											}
											da.setAction("mt");
										}
										
										if(!isValid(obj.getString("email").toLowerCase())) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format email salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										Long idAcc=(long) 0;
										Initial ini=new Initial();
										if(daPrf==null) {
											if(user!=null)da.setEeuser(user);
											da.setDocument(id_doc);
											da.setFlag(false);
											da.setType("initials");
											da.setDate_sign(null);
											da.setEmail(obj.getString("email").toLowerCase().trim());
											if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
												dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
												jo.put("result", "FE");
								                jo.put("notif", "Format name salah.");
								                
								                try {
										        	ActivityLog logSystem = new ActivityLog(request, refTrx);
										        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
										        }catch(Exception e)
										        {
										        	e.printStackTrace();
										        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
										        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
										        }
								                
								                return jo;
											}
											da.setName(obj.getString("name").trim());
											da.setDatetime(new Date());
											da.setAction(obj.getString("aksi_ttd"));
											if(obj.has("visible")) {
												boolean vis=true;
												String v=obj.getString("visible");
												if(v.equalsIgnoreCase("0"))vis=false;
												
												//System.out.println("visible = "+vis);
												LogSystem.info(request, "Visible = "+vis,kelas, refTrx, trxType);
												da.setVisible(vis);
											} else {
												da.setVisible(true);
											}
											
											if(sequence) {
												//if(obj.has("signing_seq")) {
													//sequence=true;
													da.setSequence_no(obj.getInt("signing_seq"));
													list_seq.add(obj.getInt("signing_seq"));
												//}
											}
											
											//idAcc=dAccessDao.create(da);
											
											try
											{
												idAcc=dAccessDao.create(da);
												
												if(idAcc == null || idAcc == 0)
												{
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													jo.put("result", "06");
									                jo.put("notif", "Kirim dokumen gagal");
									                return jo;
												}
											}catch(Exception e)
											{
												LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												LogSystem.error(getClass(), e,kelas, refTrx, trxType);
												jo.put("result", "06");
								                jo.put("notif", "Kirim dokumen gagal");
								                return jo;
											}
											
											DocumentAccess dac=dAccessDao.findbyId(idAcc);
											if(prf==true) {
												//System.out.println("id doc access = "+idAcc);
												LogSystem.info(request, "id doc access = "+idAcc,kelas, refTrx, trxType);
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
										LogSystem.info(request, "prf = "+prf,kelas, refTrx, trxType);
										
										ini.setLx(obj.getString("llx"));
										ini.setLy(obj.getString("lly"));
										ini.setRx(obj.getString("urx"));
										ini.setRy(obj.getString("ury"));
										ini.setPage(Integer.valueOf(obj.getString("page")));
										if(Integer.parseInt(obj.getString("page"))>jmlpage) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Halaman tandatangan, melebihi dari jumlah halaman dokumen");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										
										try 
										{
											new InitialDao(db).create(ini);
										}catch(Exception e)
										{
											LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											LogSystem.error(getClass(), e,kelas, refTrx, trxType);
											jo.put("result", "06");
							                jo.put("notif", "Kirim dokumen gagal");
							                return jo;
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
		    								evo.setEmail(obj.getString("email").toLowerCase());
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								emails.add(evo);
										}
										//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
	    								//mail.run();
								
									} else if(actUser.equalsIgnoreCase("app")) {
										LogSystem.info(request, "ada user approval = "+obj.getString("email"),kelas, refTrx, trxType);
										if(!obj.has("signing_seq")) {
											jo.put("result", "FE");
							                jo.put("notif", "Terdapat user approval, harus menggunakan sequence.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											jo.put("result", "FE");
							                jo.put("notif", "User approval tidak bisa menggunakan auto");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										if(user!=null)da.setEeuser(user);
										da.setAction("mt");
										if(!isValid(obj.getString("email").toLowerCase())) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format email salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										da.setDocument(id_doc);
										da.setFlag(false);
										da.setType("approval");
										da.setDate_sign(null);
										da.setEmail(obj.getString("email").toLowerCase().trim());
										if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format name salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										da.setName(obj.getString("name").trim());
										
										if(sequence) {
											//if(obj.has("signing_seq")) {
												//sequence=true;
												da.setSequence_no(obj.getInt("signing_seq"));
												list_seq.add(obj.getInt("signing_seq"));
											//}
										}
										
										
										Long idAcc = null;
										try
										{
											idAcc=dAccessDao.create(da);
											
											if(idAcc == null)
											{
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												LogSystem.error(request, "idAcc null",kelas, refTrx, trxType); 
												jo.put("result", "06");
								                jo.put("notif", "Kirim dokumen gagal");
								                return jo;
											}
										}catch(Exception e)
										{
											LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "06");
							                jo.put("notif", "Kirim dokumen gagal");
							                return jo;
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
		    								evo.setEmail(obj.getString("email").toLowerCase());
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								emails.add(evo);
										}
									} else if(actUser.equalsIgnoreCase("sgl")) {
										
										boolean sign=false;
										if(!obj.has("seal_name")) {
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "FE");
							                jo.put("notif", "nama seal harus disertakan.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										
										if(user==null) {
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "09");
							                jo.put("notif", "User belum terdaftar. Silahkan daftarkan user terlebih dahulu.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										
										UserSeal userseal=null;
										UserSealDao usd=new UserSealDao(db);
										userseal=usd.findByEeuser(user.getId(), obj.getString("seal_name"));
										if(userseal==null) {
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "10");
							                jo.put("notif", "nama seal tidak ditemukan");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											da.setAction("at");
											//if(user!=null) {
												if(user.isAuto_ttd()) {
													String key = user.getKey_at_ttd();
													if(key.equalsIgnoreCase(obj.getString("kuser")))sign=true;
													else {
														File file=new File(id_doc.getPath()+id_doc.getRename());
														file.delete();
														new DocumentsDao(db).delete(id_doc);
														jo.put("result", "83");
										                jo.put("notif", "key user ttd otomatis tidak valid");
										                
										                try {
												        	ActivityLog logSystem = new ActivityLog(request, refTrx);
												        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
												        }catch(Exception e)
												        {
												        	e.printStackTrace();
												        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
												        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
												        }
										                
										                return jo;
													}
												}
												else { 
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "07");
									                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
									                
									                try {
											        	ActivityLog logSystem = new ActivityLog(request, refTrx);
											        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
											        }catch(Exception e)
											        {
											        	e.printStackTrace();
											        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
											        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											        }
									                
									                return jo;
												}
											//}
												seal = true;
											
										} else {
											da.setAction("mt");
										}
										
										if(user!=null)da.setEeuser(user);
										if(Integer.parseInt(obj.getString("page"))>jmlpage) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Halaman tandatangan, melebihi dari jumlah halaman dokumen");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										
										if(!isValid(obj.getString("email").toLowerCase())) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format email salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										da.setDocument(id_doc);
										da.setFlag(false);
										da.setType("seal");
										da.setDate_sign(null);
										da.setEmail(obj.getString("email").toLowerCase().trim());
										if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format name salah.");
							                
							                try {
									        	ActivityLog logSystem = new ActivityLog(request, refTrx);
									        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
									        }catch(Exception e)
									        {
									        	e.printStackTrace();
									        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
									        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									        }
							                
							                return jo;
										}
										da.setName(obj.getString("name").trim());
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
											LogSystem.info(request, "visible = "+vis,kelas, refTrx, trxType);
											//System.out.println("visible = "+vis);
											da.setVisible(vis);
										} else {
											da.setVisible(true);
										}
										if(sequence) {
											//if(obj.has("signing_seq")) {
												//sequence=true;
												da.setSequence_no(obj.getInt("signing_seq"));
												list_seq.add(obj.getInt("signing_seq"));
											//}
										}
										
										
										Long idAcc = null;
										try
										{
											idAcc=dAccessDao.create(da);
											da.setId(idAcc);
											
											
											sda.setUser_seal(userseal);
											sda.setDoc_access(da);
											
											if(obj.has("qr_text")) {
												if(obj.has("encrypt_qr")) {
													if(obj.getBoolean("encrypt_qr")==true) {
														QRTextEncrypt qte=new QRTextEncrypt(refTrx);
														JSONObject respQR=qte.kirim(obj.getString("qr_text"), mitra.getId(), id_doc.getIdMitra(), request);
														
														if(respQR!=null) {
															LogSystem.info(request, "resp QRTEXT = "+respQR, kelas, refTrx, trxType);
															if(respQR.getString("result").equals("00")) {
																sda.setQr_text(respQR.getString("qr_text"));
															} else {
																File file=new File(id_doc.getPath()+id_doc.getRename());
																file.delete();
																new DocumentsDao(db).delete(id_doc);
																jo.put("result", "06");
												                jo.put("notif", "Kirim dokumen gagal");
												                return jo;
															}
														} else {
															LogSystem.info(request, "resp QRTEXT = telur ceplok", kelas, refTrx, trxType);
															File file=new File(id_doc.getPath()+id_doc.getRename());
															file.delete();
															new DocumentsDao(db).delete(id_doc);
															jo.put("result", "06");
											                jo.put("notif", "Kirim dokumen gagal");
											                return jo;
														}
													} else {
														sda.setQr_text(obj.getString("qr_text"));
													}
												} else {
													sda.setQr_text(obj.getString("qr_text"));
												}
												
											}
											new SealDocAccessDao(db).create(sda);
											
										}catch(Exception e)
										{
											LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											jo.put("result", "06");
							                jo.put("notif", "Kirim dokumen gagal");
							                return jo;
										}
										
										if(sign==true) {
											//DocumentAccess dac=dAccessDao.findbyId(idAcc);
											lttd.add(da);	
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
		    								evo.setEmail(obj.getString("email").toLowerCase());
		    								evo.setUdata(user.getUserdata());
		    								evo.setReg(true);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								emails.add(evo);
										}
									}
								}
								
								kirim=false;

							}
						}
						
						if(jsonRecv.get("send-to")!=null) {
							
							JSONArray sendList=jsonRecv.getJSONArray("send-to");
							
							for(int i=0; i<sendList.length(); i++) {
								JSONObject obj=(JSONObject) sendList.get(i);

								if(sendList.getString(i).equals(usr.getNick())) continue;
								boolean sign=false;
								for(EmailVO ev:emails) {
									if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
										sign=true;
										break;
									}
								}
								if(sign)continue;
								
								
								DocumentAccess da=new DocumentAccess();
//								User user= new UserManager(db).findByUsername(sendList.getString(i));
								User user= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());

								/*change to eeuser*/
//								if(user!=null)da.setUserdata(user.getUserdata());
								if(!isValid(obj.getString("email").toLowerCase())) {
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
									jo.put("result", "FE");
					                jo.put("notif", "Format email salah.");
					                
					                try {
							        	ActivityLog logSystem = new ActivityLog(request, refTrx);
							        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
							        }catch(Exception e)
							        {
							        	e.printStackTrace();
							        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
							        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
							        }
					                
					                return jo;
								}
								
								if(user!=null)da.setEeuser(user);
								da.setDocument(id_doc);
								da.setFlag(false);
								da.setType("share");
								da.setDate_sign(null);
								da.setEmail(obj.getString("email").toLowerCase().trim());
								if(obj.getString("name").trim().equals("")||obj.getString("name").trim()==null) {
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
									jo.put("result", "FE");
					                jo.put("notif", "Format name salah.");
					                
					                try {
							        	ActivityLog logSystem = new ActivityLog(request, refTrx);
							        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
							        }catch(Exception e)
							        {
							        	e.printStackTrace();
							        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
							        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
							        }
					                
					                return jo;
								}
								da.setName(obj.getString("name").trim());
								da.setDatetime(new Date());
								
								Long idAcc = null;
								try
								{
									idAcc = dAccessDao.create(da);
									if(idAcc == null)
									{
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										 LogSystem.info(request, "idAcc null",kelas, refTrx, trxType);
										 jo.put("result", "06");
										 jo.put("notif", "Kirim dokumen gagal");
										 return jo;
									}
								}catch(Exception e)
								{
									LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									LogSystem.error(getClass(), e,kelas, refTrx, trxType);
									jo.put("result", "06");
					                jo.put("notif", "Kirim dokumen gagal");
					                return jo;
								}
								
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
    								evo.setEmail(obj.getString("email").toLowerCase());
    								evo.setUdata(udata);
    								evo.setReg(reg);
    								evo.setNama(obj.getString("name"));
    								evo.setIddocac(idAcc);
    								//emails.add(evo);
    								emailscc.add(evo);
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
//						docDao.update(id_doc);  
				  }
				  
				  int x=0;
				  //System.out.println("List Auto TTD = "+lttd.size());
				  LogSystem.info(request, "List Auto TTD = "+lttd.size(),kelas, refTrx, trxType);
				  
				  Vector<String> invs=new Vector();
				  //koneksi ke billing
				  KillBillDocumentHttps kdh = null;
				  KillBillPersonalHttps kph = null;
				  try {
					  kdh=new KillBillDocumentHttps(request, refTrx);
					  kph=new KillBillPersonalHttps(request, refTrx);
				  } catch (Exception e) {
					  LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
					// TODO: handle exception
					  dAccessDao.deleteWhere(id_doc.getId());
						File file=new File(id_doc.getPath()+id_doc.getRename());
						file.delete();
						new DocumentsDao(db).delete(id_doc);
						
			  			jo.put("result", "91");
						jo.put("notif", "System timeout. silahkan coba kembali.");
						return jo;
				  }
				  
				  LogSystem.info(request, "sequence = "+sequence,kelas, refTrx, trxType);
				  if(sequence==true) {
					  
					  for (x = 0; x < list_seq.size()-1; x++) {
				            for (int i = 0; i < list_seq.size()-1; i++) {
				                if (list_seq.get(i) > list_seq.get(i+1)) {
				                    int j = list_seq.get(i);
				                    list_seq.set(i, list_seq.get(i+1));
				                    list_seq.set(i+1, j);
				                }
				            }
				        }
					  LogSystem.info(request, "current_sequence = "+list_seq.get(0),kelas, refTrx, trxType);
					  id_doc.setCurrent_seq(list_seq.get(0));
//					  docDao.update(id_doc);
					  
					  for (x = 0; x < lttd.size()-1; x++) {
				            for (int i = 0; i < lttd.size()-1; i++) {
				                if (lttd.get(i).getSequence_no() > lttd.get(i+1).getSequence_no()) {
				                    DocumentAccess j=lttd.get(i);
				                    lttd.set(i, lttd.get(i+1));
				                    lttd.set(i+1, j);
				                }
				            }
				        }
				  }
				  
				  int loop=0;
				  int sequenceNext=0;
				  
				  if(lttd.size()>0) {
			
					  ProsesSign ps=new ProsesSign();
					  JSONObject sign=ps.sign(refTrx, db, lttd, id_doc, mitra, request, list_seq, sequence, seal, version, sda, jsonRecv, countAutoSign,jmlpage, usr);
					  LogSystem.info(request, sign.toString(), kelas, refTrx, trxType);
					  if(sign.getString("result").equalsIgnoreCase("00")) {
						  LogSystem.info(request, "berhasil sign AT", kelas, refTrx, trxType);
					  } else if(sign.getString("result").equalsIgnoreCase("61")) {
						  jo.put("result", "61");
						  jo.put("notif", sign.getString("notif"));
						  
						  try {
					        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
					        }catch(Exception e)
					        {
					        	e.printStackTrace();
					        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
					        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
					        }
						  
							return jo;
						  
					  } else {
						  jo.put("result", sign.getString("result"));
						  jo.put("notif", sign.getString("notif"));
						  
						  	if(sda != null)
						  	{
						  		new SealDocAccessDao(db).delete(sda);
						  	}
							dAccessDao.deleteWhere(id_doc.getId());
							File file=new File(id_doc.getPath()+id_doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(id_doc);
							LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);

						  return jo;
					  }	  
				  }
				  else
				  {
					  LogSystem.info(request, "Tidak ada proses AT, cek meterai", kelas, refTrx,trxType);
					  //Tidak ada AT sign, cek meterai
					  //Check meterai
					  if(jsonRecv.has("meterai")) 
					  {
							JSONObject jsonMeterai = new JSONObject(jsonRecv.getString("meterai"));
							boolean sign=false;		
							DocumentAccess da=new DocumentAccess();
							
							if(jsonMeterai.getString("aksi").equalsIgnoreCase("at"))
							{
								LogSystem.info(request, "Proses meterai AT true", kelas, refTrx,trxType);
								sign=true;
							}
							else
							{
								LogSystem.info(request, "Proses meterai AT false", kelas, refTrx,trxType);
							}
							
							if(countAutoSign > 0)
							{
								if(jsonMeterai.getString("aksi").equalsIgnoreCase("mt"))
								{
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
									jo.put("result", "07");
					                jo.put("notif", "Tidak dapat melakukan tandatangan otomatis sebelum meterai elektronik");
					                
					                try {
							        	ActivityLog logSystem = new ActivityLog(request, refTrx);
							        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
							        }catch(Exception e)
							        {
							        	e.printStackTrace();
							        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
							        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
							        }
					                
					                return jo;
								}
								else
								{
									sign=true;
								}
							}
							
							da.setAction(jsonMeterai.getString("aksi"));
							
							if(Integer.parseInt(jsonMeterai.getString("page"))>jmlpage) {
								dAccessDao.deleteWhere(id_doc.getId());
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
								
								jo.put("result", "FE");
				                jo.put("notif", "Halaman meterai elektronik, melebihi dari jumlah halaman dokumen");
				                
				                try {
						        	ActivityLog logSystem = new ActivityLog(request, refTrx);
						        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
						        }catch(Exception e)
						        {
						        	e.printStackTrace();
						        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
						        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
						        }
				                
				                return jo;
							}
							
							if(!isValid(jsonRecv.getString("userid").toLowerCase())) {
								dAccessDao.deleteWhere(id_doc.getId());
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
								
								jo.put("result", "FE");
				                jo.put("notif", "Format email salah.");
				                
				                try {
						        	ActivityLog logSystem = new ActivityLog(request, refTrx);
						        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + jo.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
						        }catch(Exception e)
						        {
						        	e.printStackTrace();
						        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
						        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
						        }
				                
				                return jo;
							}
							da.setDocument(id_doc);
							da.setEeuser(usr);
							da.setName(usr.getName());
							da.setFlag(false);
							da.setType("meterai");
							meterai = true;
							da.setDate_sign(null);
							da.setEmail(jsonRecv.getString("userid").toLowerCase().trim());
						
							da.setPage(Integer.parseInt(jsonMeterai.getString("page")));
							da.setLx(jsonMeterai.getString("llx"));
							da.setLy(jsonMeterai.getString("lly"));
							da.setRx(jsonMeterai.getString("urx"));
							da.setRy(jsonMeterai.getString("ury"));
							da.setDatetime(new Date());
							da.setVisible(true);
							
							Long idAcc = null;
							try
							{
								idAcc=dAccessDao.create(da);
								
								if(idAcc == null)
								{
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									LogSystem.error(request, "idAcc null",kelas, refTrx, trxType); 
									jo.put("result", "06");
					                jo.put("notif", "Kirim dokumen gagal"); 
					                
					                return jo;
								}
							}catch(Exception e)
							{
								LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
								File file=new File(id_doc.getPath()+id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
								dAccessDao.deleteWhere(id_doc.getId());
								jo.put("result", "06");
				                jo.put("notif", "Kirim dokumen gagal");
				                return jo;
							}
							
							DocumentAccess dac = null;
							
							if(sign==true) {
								dac=dAccessDao.findbyId(idAcc);
								 
								Userdata udata=new Userdata();
								boolean reg=false;
	
								String OriginPath = id_doc.getSigndoc();
								String path = id_doc.getPath();
								Date date = new Date();
								SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
								String strDate = sdfDate.format(date);
								String signdoc = "APIA" + strDate+id_doc.getId() +".pdf";
								
								SaveFileWithSamba samba=new SaveFileWithSamba();
								byte[] encoded = Base64.encode(samba.openfile(path+OriginPath));
								String base64Document = new String(encoded, StandardCharsets.US_ASCII);
								
								float llx = Float.parseFloat(dac.getLx());
								float lly = Float.parseFloat(dac.getLy());
								float urx = Float.parseFloat(dac.getRx());
								float ury = Float.parseFloat(dac.getRy());
								
								JSONObject resSign = null;
								DocumentSigner ds = new DocumentSigner();
								try {
									resSign = ds.kirimMeterai(request, refTrx, id_doc.getId(), id_doc.getFile_name(), id_doc.getEeuser().getMitra().getId(), llx, lly, urx, ury,jsonMeterai.getInt("page"), base64Document, id_doc.getEeuser().getMitra().getProvinsi().getName());
									
								}catch(Exception e)
								{
									e.printStackTrace();
									// TODO: handle exception
									LogSystem.error(request, "Error save file meterai"+e.toString(), kelas, refTrx, trxType);
							  	  	jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									return jo;
								}
								
								if(resSign == null)
								{
									LogSystem.error(request, "Response meterai null", kelas, refTrx, trxType);
							  	  	jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									return jo;
								}
								
								if(resSign.getString("information").equalsIgnoreCase("saldo tidak mencukupi"))
								{
									jo.put("result", "60");
									jo.put("notif", "Saldo meterai elektronik tidak mencukupi");
									if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
									
									LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
									return jo;
								}
								
								if(resSign.getString("information").equalsIgnoreCase("response code: 413 Request Entity Too Large"))
								{
									jo.put("result", "13");
									jo.put("notif", "Kirim dokumen gagal");
									jo.put("info", "Ukuran file PDF untuk proses meterai elektronik maksimal 10Mb");
									
									LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
									if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
									dAccessDao.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);
									
									return jo;
								}
								
								LogSystem.info(request, "Response meterai : "+resSign.getString("result") + " information " + resSign.getString("information"), kelas, refTrx, trxType);
								
								boolean resp=false;
								
								if(resSign.getString("result").equals("00"))
								{
								    try {
								    	byte[] finalDoc = Base64.decode(resSign.getString("final_document"));
									    resp=samba.write(finalDoc, path+signdoc);
									    LogSystem.info(request, "hasil save File : "+resp, kelas, refTrx, trxType);
									    if(resp==false) {
									  	  	LogSystem.error(request, "error samba proses meterai", kelas, refTrx, trxType);
									  	  	jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											if(sda != null)
										  	{
										  		new SealDocAccessDao(db).delete(sda);
										  	}
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											return jo;
									    }
									    finalDoc = null;
									} catch (Exception e) {
										e.printStackTrace();
										// TODO: handle exception
										LogSystem.error(request, "Error save file meterai"+e.toString(), kelas, refTrx, trxType);
								  	  	jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										if(sda != null)
									  	{
									  		new SealDocAccessDao(db).delete(sda);
									  	}
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										dAccessDao.deleteWhere(id_doc.getId());
										new DocumentsDao(db).delete(id_doc);
										return jo;
									}
								    
									try {
										id_doc.setSigndoc(signdoc);
										new DocumentsDao(db).update(id_doc);
										dac.setDate_sign(new Date());
										dac.setFlag(true);
										
										dAccessDao.update(dac);
										
										LogSystem.info(request, "Proses meterai berhasil ", kelas, refTrx, trxType);
									} catch (Exception e) {
										// TODO: handle exception
										LogSystem.info(request, "DB Timeout", kelas, refTrx, trxType);
								  	  	jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										if(sda != null)
									  	{
									  		new SealDocAccessDao(db).delete(sda);
									  	}
										dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										return jo;
									}
								}
								else
								{
									LogSystem.info(request, "Response Meterai tidak berhasil ", kelas, refTrx, trxType);
							  	  	jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									dAccessDao.deleteWhere(id_doc.getId());
									new DocumentsDao(db).delete(id_doc);
									return jo;
								}
							}
						}
					//Selesai cek meterai
				  }
				  
				  res="00";
				  if(id_doc.getSequence()) {
					  List<DocumentAccess> listEmail=dAccessDao.findDocByCurrentSeq(id_doc.getId(), id_doc.getCurrent_seq());
					  if(mitra.isNotifikasi()) {
						  SendTerimaDoc std=new SendTerimaDoc(request, refTrx, kelas, trxType);
						  for(DocumentAccess email:listEmail) {
							  String docs ="";
								String link ="";
								try {
									   //String idm = "idm="+AESEncryption.encryptDoc(mitra.getId()+";"+email.getEmail()+";"+email.getIddocac());
									   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
									   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+email.getId();
									     //+ URLEncoder.encode(docs, "UTF-8");
									} catch (Exception e1) {
									   // TODO Auto-generated catch block
									   e1.printStackTrace();
									   LogSystem.error(request, e1.toString(), kelas, refTrx,trxType);
									}
								
							  if(email.getEeuser()!=null) {
								  std.kirim(email.getEeuser().getName(), String.valueOf(email.getEeuser().getUserdata().getJk()), email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), format_doc);
							  } else {
								  std.kirim(email.getName(), "", email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), format_doc);
							  }
						  }
					  }
				  } else {
					  for(EmailVO email:emails) {
							//System.out.println("email = "+email.getEmail());
							LogSystem.info(request, "email = "+email.getEmail(),kelas, refTrx, trxType);
							//MailSender mail=new MailSender(email.getEmail(),email.getUdata(),email.isReg(),email.getNama());
							//mail.run();
							
							String docs ="";
							String link ="";
							try {
								   //String idm = "idm="+AESEncryption.encryptDoc(mitra.getId()+";"+email.getEmail()+";"+email.getIddocac());
								   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
								   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+email.getIddocac();
								     //+ URLEncoder.encode(docs, "UTF-8");
								} catch (Exception e1) {
								   // TODO Auto-generated catch block
								   e1.printStackTrace();
								   LogSystem.error(request, e1.toString(), kelas, refTrx,trxType);
								}
							
							SendTerimaDoc std=new SendTerimaDoc(request, refTrx, kelas, trxType);
	//						if(email.isReg())std.kirim(email.getUdata().getNama(), String.valueOf(email.getUdata().getJk()), email.getEmail(), aVerf.getEeuser().getName(), String.valueOf(aVerf.getEeuser().getUserdata().getJk()), link);
	//						else std.(email.getNama(), "", email.getEmail(), aVerf.getEeuser().getName(), String.valueOf(aVerf.getEeuser().getUserdata().getJk()), link);
							
							if(mitra.isNotifikasi()) {
								//Tambah email dokumen id
								if(email.isReg())std.kirim(email.getUdata().getNama(), String.valueOf(email.getUdata().getJk()), email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), format_doc);
								else std.kirim(email.getNama(), "", email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), format_doc);
							}
						}
				  }
				  
				  for(EmailVO emailcc:emailscc) {
						LogSystem.info(request, "email = "+emailcc.getEmail(),kelas, refTrx, trxType);
						emailcc.getIddocac();
					
						String docs ="";
						String link ="";
						try {
							   //String idm = "idm="+AESEncryption.encryptDoc(mitra.getId()+";"+email.getEmail()+";"+email.getIddocac());
							   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
							   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+emailcc.getIddocac();
							     //+ URLEncoder.encode(docs, "UTF-8");
							} catch (Exception e1) {
							   // TODO Auto-generated catch block
							   e1.printStackTrace();
							   LogSystem.error(request, e1.toString(), kelas, refTrx,trxType);
							}
						
						SendTerimaDocCC std=new SendTerimaDocCC();

						if(mitra.isNotifikasi()) {
							
							if(emailcc.isReg())std.kirim(emailcc.getUdata().getNama(), String.valueOf(emailcc.getUdata().getJk()), emailcc.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), filename);
							else std.kirim(emailcc.getNama(), "", emailcc.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), filename);
						}
						else
						{
							if(jsonRecv.has("notifikasi"))
							{
								if(jsonRecv.getString("notifikasi") == "1")
								{
									if(emailcc.isReg())std.kirim(emailcc.getUdata().getNama(), String.valueOf(emailcc.getUdata().getJk()), emailcc.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), filename);
									else std.kirim(emailcc.getNama(), "", emailcc.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), filename);
								}
							}
						}
					}
				  
				  	jo.put("notif", "Kirim dokumen berhasil.");

					try {
			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			        	logSystem.POST("upload", "success", "[API] Berhasil upload dokumen", Long.toString(usr.getId()), Long.toString(id_doc.getId()), null, null, null,null);
			        }catch(Exception e)
			        {
			        	e.printStackTrace();
			        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
			        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
			        }
				  
				  //check status document
				  Long hasil=(long) 0;
				  hasil=dAccessDao.getWaitingSignUserByDoc(String.valueOf(id_doc.getId()));
				  if(hasil==0) {
					  id_doc.setSign(true);
				  }
				  
				  userRecv=usr;
				  id_doc.setStatus('T');
				  
				  
				  if(jsonRecv.has("return_document")) {
					  if(jsonRecv.getBoolean("return_document")==true) {
						  Documents dokumen=docDao.findById(id_doc.getId());
						  String pathDoc=dokumen.getPath()+dokumen.getSigndoc();
						  SaveFileWithSamba samba=new SaveFileWithSamba();
						  byte[] encoded = Base64.encode(samba.openfile(pathDoc));
						  jo.put("file", new String(encoded, StandardCharsets.US_ASCII));
					  }
				  }
				  
				  List<Documents> docValid=docDao.findByDocIdMitraFalse(id_doc.getIdMitra(), mitra.getId());
				  if(docValid.get(0).getId()!=id_doc.getId()) {
					  LogSystem.info(request, "ISI DOCU 2" + id_doc.getId(), kelas, refTrx, trxType);
					  id_doc.setDelete(true);
//					  docDao.update(id_doc);
				  }
				  docDao.update(id_doc);
			}else {
				
				jo=aVerf.setResponFailed(jo);
			}
		}
		catch (Exception e) {
			LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
			// TODO: handle exception
			e.printStackTrace();
			if(id_doc!=null) {
				if(sda != null)
			  	{
			  		new SealDocAccessDao(db).delete(sda);
			  	}
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
						
					  LogSystem.error(request, e1.toString(), kelas, refTrx, trxType);
			  			jo.put("result", "91");
						jo.put("notif", "System timeout. silahkan coba kembali.");

						return jo;
				  }
				  
				  for(Invoice in:linv) {
					  if(in.getTenant()==1) {
						  try {
							kph.reverseTransaction(in.getKb_invoice(), 1, String.valueOf(id_doc.getId()));
						} catch (KillBillClientException e1) {
							// TODO Auto-generated catch block
							LogSystem.error(request, e1.toString(), kelas, refTrx, trxType);
							e1.printStackTrace();
						}
					  } else if(in.getTenant()==2) {
						  try {
								kdh.reverseTransaction(in.getKb_invoice(), 1, String.valueOf(id_doc.getId()));
							} catch (KillBillClientException e1) {
								// TODO Auto-generated catch block
								LogSystem.error(request, e1.toString(), kelas, refTrx, trxType);
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
	
	public static boolean isValid(String email) 
    { 
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
                            "[a-zA-Z0-9_+&*-]+)*@" + 
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
                            "A-Z]{2,9}$"; 
                            
        Pattern pat = Pattern.compile(emailRegex); 
        if (email == null) 
            return false; 
        return pat.matcher(email).matches(); 
    } 
	
	public boolean verifyAllEqualUsingStream(List<String> list) {
	    return list.stream()
	      .distinct()
	      .count() <= 1;
	}
		
}
