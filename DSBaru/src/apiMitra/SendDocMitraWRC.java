package apiMitra;

import java.io.File;
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

public class SendDocMitraWRC extends ActionSupport {

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
					LogSystem.info(request, "Multipart", kelas, refTrx, trxType);
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
					 fileItems = upload.parseRequest(request);

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
				}
			 String process=request.getRequestURI().split("/")[2];
			 LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
	         //System.out.println("PATH :"+request.getRequestURI());

	         LogSystem.request(request, fileItems,kelas, refTrx, trxType);
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
			LogSystem.response(request, jo,kelas, refTrx, trxType);

		}catch (Exception e) {
            LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data request tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
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
		              return jo;
				  }
				  
				  List<Documents> ldoc=docDao.findByDocIdMitra(jsonRecv.getString("document_id"), mitra.getId());
				  if(ldoc.size()>0) {
					  for(Documents docu:ldoc) {
						  docu.setDelete(true);
						  docDao.update(docu);
					  }
				  }
				  
				  
				  if(fProc.uploadFILEnQRMitra(request, db, usr, dataFile, jsonRecv.getString("document_id"), jsonRecv)) {
 					  res="00";
 					  jmlpage=fProc.getJmlPage();
 					 LogSystem.info(request, "Upload Successfully",kelas, refTrx, trxType);
				  } else {
					  jo.put("result", "FE");
		              jo.put("notif", "Format dokumen harus pdf");
		              return jo;
				  }
				  
				  //id_doc=new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename());
				  id_doc=fProc.getDc();
				  if(jsonRecv.has("redirect")) {
					  if(id_doc.getRedirect()==false ) {
						  docDao.delete(id_doc);
						  	jo.put("result", "FE");
		                    jo.put("notif", "redirect field harus diisi true");
		                    return jo;
					  }
					  
					  if(mitra.getSigning_redirect()==null) {
						  docDao.delete(id_doc);
						  	jo.put("result", "08");
		                    jo.put("notif", "Anda belum memasukkan link redirect anda.");
		                    return jo;
					  }
					  
				  }
				  Vector<DocumentAccess> lttd=new Vector();
				  Vector<EmailVO> emails=new Vector<>();
				  Vector<EmailVO> emailscc=new Vector<>();
				  DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
				  String version = null;
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
						
					
						
						if(jsonRecv.get("req-sign")!=null) {
							JSONArray sendList=jsonRecv.getJSONArray("req-sign");
							
							//Check sertifikat ke KMS
							for (int c=0 ; c < sendList.length() ; c++)
							{
								JSONObject expired = (JSONObject) sendList.get(c);
								User userCert= new UserManager(db).findByUsername(expired.getString("email").toLowerCase());
								
								String userExpired = expired.getString("user").substring(0, 3);
								
								String level = null;
								
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
								
								if(userCert != null)
								{
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
									
									if(cert.getString("result").length() > 3)
									{
										Session session = null;
										DB db3=new DB();
										session=db3.open();
										
										Transaction trx=session.beginTransaction();
										LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										session.delete(id_doc);
									
										trx.commit();
										if(db3!=null) 
										{
											db3.session().close();
											db3.close();
											session.close();
										}
										
										jo.put("result", "15");
										jo.put("notif", "Kirim dokumen gagal");
										
										return jo;
									}
									
									if (expired.getString("aksi_ttd").equalsIgnoreCase("at"))
									{
										if(cert.getString("result").equals("05"))
										{
											Session session = null;
											DB db3=new DB();
											session=db3.open();
											
											Transaction trx=session.beginTransaction();
											LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											session.delete(id_doc);
										
											trx.commit();
											if(db3!=null) 
											{
												db3.session().close();
												db3.close();
												session.close();
											}
											
											jo.put("result", "15");
											
											jo.put("notif", "Kirim dokumen gagal");
											jo.put("info",  "User " + expired.getString("email") + " belum memiliki sertifikat elektronik untuk melakukan tandatangan otomatis, Silakan melakukan konfirmasi penerbitan sertifikat elektronik dengan login melalui website Digisign");
											if (level.equals("C5"))
											{
												jo.put("info", "User " + expired.getString("email") + " belum memiliki sertifikat segel elektronik untuk melakukan segel otomatis, Silakan melakukan konfirmasi penerbitan sertifikat segel elektronik dengan login melalui website Corporate Digisign");
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
														jo.put("info", "Sertifikat segel elektronik lama milik "+expired.getString("email")+" akan dicabut. Mohon melakukan konfirmasi penerbitan sertifikat elektronik segel baru dengan login melalui website Corporate Digisign");
													}
													else
													{
														jo.put("info", "Sertifikat elektronik lama milik "+expired.getString("email")+" akan dicabut. Mohon melakukan konfirmasi penerbitan sertifikat elektronik baru dengan login melalui website Digisign");
													}
												}
											}
										}
									}
									
									if (version == null)
									{
										if(cert.has("keyVersion"))
										{
											version = cert.getString("keyVersion");
										}
									}
									
									if (version != null)
									{
										if (version != cert.getString("keyVersion"))
										{
											Session session = null;
											DB db3=new DB();
											session=db3.open();
											
											Transaction trx=session.beginTransaction();
											LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											session.delete(id_doc);
										
											trx.commit();
											if(db3!=null) 
											{
												db3.session().close();
												db3.close();
												session.close();
											}
											  
											jo.put("result", "15");
											jo.put("notif", "Kirim dokumen gagal");
											jo.put("info", "Sertifikat harus diperbarui");
											
											return jo;
										}
									}
									
									//Cek aksi ttd mt jika 07, 06, G1
									LogSystem.info(request, "Status sertifikat user : " + expired.getString("email") + " adalah " + cert,kelas, refTrx, trxType);
									if(cert.getString("result").equals("07") || cert.getString("result").equals("06") || cert.getString("result").equals("G1"))
									{
										Session session = null;
										DB db3=new DB();
										session=db3.open();
										
										Transaction trx=session.beginTransaction();
										LogSystem.info(request, "ISI DOCU " + id_doc.getId(), kelas, refTrx, trxType);
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										session.delete(id_doc);
									
										trx.commit();
										if(db3!=null) 
										{
											db3.session().close();
											db3.close();
											session.close();
										}
										  
										jo.put("result", "15");
										
										if(cert.getString("result").equals("06"))
										{
											jo.put("notif", "Kirim dokumen gagal");
											jo.put("info", "Sertifikat Elektronik milik " + expired.getString("email") + " sudah habis masa berlakunya, silahkan melakukan registrasi ulang");
											if (level.equals("C5"))
											{
												jo.put("info", "Sertifikat segel elektronik milik " + expired.getString("email") + " sudah habis masa berlakunya, silahkan melakukan registrasi ulang");
											}
										}
										
										if(cert.getString("result").equals("07"))
										{
											jo.put("notif", "Kirim dokumen gagal");
											jo.put("info", "Sertifikat Elektronik milik " + expired.getString("email") + " sudah dicabut, silahkan melakukan registrasi ulang");
											if (level.equals("C5"))
											{
												jo.put("info", "Sertifikat segel elektronik milik " + expired.getString("email") + " sudah dicabut, silahkan melakukan registrasi ulang");
											}
										}
										
										if (cert.getString("result").equals("G1"))
										{
											jo.put("notif", "Kirim dokumen gagal");
											jo.put("info", "Sertifikat Elektronik lama milik " + expired.getString("email") + " sudah dicabut. Silakan melakukan konfirmasi penerbitan sertifikat elektronik baru dengan login melalui website Digisign");
										}
										
										return jo;
									}
								}
							}//Selesai cek sertifikat ke kms
							
							if(version.equals("1.0"))
							{
								version = "v1";
							}
							
							if(version.equals("3.0"))
							{
								version = "v3";
							}

							
							FormatPDFDao fdao=new FormatPDFDao(db);
							LetakTtdDao ltdao=new LetakTtdDao(db);
							if(sendList.length()==0) {
								jo.put("result", "05");
								jo.put("notif", "field req-sign tidak ditemukan");
								return jo;
							}
							
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
								
								if(jsonRecv.has("sequence_option")) {
									if(jsonRecv.getBoolean("sequence_option")==true) {
										if(!obj.has("signing_seq")) {
											jo.put("result", "FE");
											jo.put("notif", "sequence option = true, harus menyertakan signing_seq");
											return jo;
										}
									}
								}
								
								/*change to eeuser*/
//								if(user!=null)da.setUserdata(user.getUserdata());
								LogSystem.info(request, "Size json object = "+jsonRecv.getString("format_doc").length(),kelas, refTrx, trxType);
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
												if(!isValid(obj.getString("email").toLowerCase())) {
													dAccessDao.deleteWhere(id_doc.getId());
													File file=new File(id_doc.getPath()+id_doc.getRename());
													file.delete();
													new DocumentsDao(db).delete(id_doc);
													
													jo.put("result", "FE");
									                jo.put("notif", "Format email salah.");
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
											              return jo;
													}
												}catch(Exception e)
												{
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
							                return jo;
										} else {
											if(obj.getString("user").length()>3) actUser=obj.getString("user").substring(0, 3);
											else actUser=obj.getString("user").substring(0);
												

											if(actUser.equalsIgnoreCase("ttd")||actUser.equalsIgnoreCase("prf")) {
												LogSystem.info(request, "user"+actUser,kelas, refTrx, trxType);
											} else {
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												jo.put("result", "FE");
								                jo.put("notif", "field 'USER', Harus diisi dengan 'ttd' atau 'prf'.");
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
										if(Integer.parseInt(obj.getString("page"))>jmlpage) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Halaman tandatangan, melebihi dari jumlah halaman dokumen");
							                return jo;
										}
										
										if(!isValid(obj.getString("email").toLowerCase())) {
											dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
											jo.put("result", "FE");
							                jo.put("notif", "Format email salah.");
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
										if(obj.has("signing_seq")) {
											sequence=true;
											da.setSequence_no(obj.getInt("signing_seq"));
											list_seq.add(obj.getInt("signing_seq"));
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
									else {
										//untuk paraf
										
										boolean prf=false;
										DocumentAccess daPrf=null;
										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
											//System.out.println("masuk auto prf.");
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
											if(obj.has("signing_seq")) {
												sequence=true;
												da.setSequence_no(obj.getInt("signing_seq"));
												list_seq.add(obj.getInt("signing_seq"));
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
							                return jo;
										}
										
										try 
										{
											new InitialDao(db).create(ini);
										}catch(Exception e)
										{
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
						docDao.update(id_doc);  
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
					  docDao.update(id_doc);
					  
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
				  for(DocumentAccess docac:lttd) {
					  LogSystem.info(request, "masuk untuk auto TTD dan payment type = "+id_doc.getPayment(),kelas, refTrx, trxType);
					  LogSystem.info(request, "id dokumen akses = "+docac.getId(),kelas, refTrx, trxType);
					  //System.out.println("masuk untuk auto TTD dan payment type = "+id_doc.getPayment());
					  //System.out.println("id dokumen akses = "+docac.getId());
					  boolean potong=false;
					  String inv=null;
					  User u=docac.getEeuser();
					  int jmlttd=lttd.size();
					  
					  //check Sequence
					  if(sequence==true) {
						   
						  if(docac.getSequence_no()>id_doc.getCurrent_seq()) {
							  LogSystem.info(request, "Tidak jadi karena lebih dari current sequence",kelas, refTrx, trxType);
							  loop++;
							  break;
						  }
						  else {
							  if(loop<lttd.size()) {
								  sequenceNext=list_seq.get(loop+1);
							  }
							  loop++;
						  }
					  }
					  
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
									
						  			jo.put("result", "91");
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
											
								  			jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
								  } else {
									  dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
							  			jo.put("result", "91");
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
												
									  			jo.put("result", "91");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
											}
									  } else {
										  dAccessDao.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "91");
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
													
										  			jo.put("result", "91");
													jo.put("notif", "System timeout. silahkan coba kembali.");
													return jo;
												}
										  } else {
											  dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "91");
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
										jo.put("notif", "Kirim dokumen gagal");
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
				              jo.put("notif", "Kirim dokumen gagal");
				              return jo;
						  }
						  
						  jmlttd--;
						  potong=true;
					  }
					  else if(id_doc.getPayment()=='3'){

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
									
						  			jo.put("result", "91");
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
				              jo.put("notif", "Kirim dokumen gagal");
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
											
								  			jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
								  } else {
									  dAccessDao.deleteWhere(id_doc.getId());
										File file=new File(id_doc.getPath()+id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);
										
							  			jo.put("result", "91");
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
											
								  			jo.put("result", "91");
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
													
										  			jo.put("result", "91");
													jo.put("notif", "System timeout. silahkan coba kembali.");
													return jo;
												}
										  } else {
											  dAccessDao.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "91");
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
							              jo.put("notif", "Kirim dokumen gagal");
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
					              jo.put("notif", "Kirim dokumen gagal");
					              return jo;
							  }
							  
							  //inv=split[1];
						  } else {
							  inv=li.get(0).getKb_invoice();
						  }

						  potong=true;
					  }
					  
					  if(potong==true) {
						  SignDoc sd=new SignDoc();
						  
						  
						  try {
							  if(sd.signDoc(u, docac, inv, db, request, refTrx, version)) {

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
								  
								  if(sequence==true) {
									  id_doc.setCurrent_seq(sequenceNext);
									  docDao.update(id_doc);
								  }
								  
								  
							  }
							  else {
								  //System.out.println("REVERSAL untuk payment= "+id_doc.getPayment());
								  LogSystem.info(request, "REVERSAL untuk payment= "+id_doc.getPayment(),kelas, refTrx, trxType);
								  JSONObject job=null;
								  if(id_doc.getPayment()=='2') {
										for(String in:invs) {
											//String resp=kp.reverseTransaction(in);
											
											try {
												job=kph.reverseTransaction(in, 1, String.valueOf(id_doc.getId()));
												//System.out.println("hasil reversal = "+resp);
												LogSystem.info(request, "hasil reversal = "+job.toString(),kelas, refTrx, trxType);
											} catch (Exception e) {
												// TODO: handle exception
												LogSystem.error(request, "reversal gagal",kelas, refTrx, trxType);
											}
											
											//idao.deleteWhere(in);
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
										//if(x==0 && inv!=null)kd.reverseTransaction(inv);
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
									jo.put("notif", "Kirim dokumen gagal");
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
							jo.put("notif", "Kirim dokumen gagal");
							return jo;
						}
						  
					  }
					  x++;
//					  kp.close();
//					  kd.close();
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
	
	public static boolean isValid(String email) 
    { 
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
                            "[a-zA-Z0-9_+&*-]+)*@" + 
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
                            "A-Z]{2,7}$"; 
                              
        Pattern pat = Pattern.compile(emailRegex); 
        if (email == null) 
            return false; 
        return pat.matcher(email).matches(); 
    } 
		
}
