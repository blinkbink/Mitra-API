package apiMitra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import FTPSign.SendBulk;
import api.email.SendSuksesSignFTP;
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
import id.co.keriss.consolidate.ee.VO.EmailVOFTP;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class SendDocMitraFTP extends ActionSupport implements DSAPI{

	//static String basepath="/opt/data-DS/UploadFile/";
	//static String basepathPreReg="/opt/data-DS/PreReg/";
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="FTP"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.SendDocMitraFTP";
	String trxType="SEND-DOC";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="FTP"+sdfDate2.format(tgl).toString();
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
					jo.put("res", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}
				// multipart form
				else {
					LogSystem.info(request, "Multipart",kelas, refTrx, trxType);
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
							jo.put("res", "55");
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
					//System.out.println("authorization adalah = "+request.getHeader("authorization"));
					
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
					 jo.put("res", "12");
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
						 jo.put("res", "12");
						 jo.put("notif", "Token dan Mitra tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						 LogSystem.response(request, jo, kelas, refTrx, trxType);
						 return;
		        	 }
		         }
		         else {
		        	 LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
		        	 JSONObject jo=new JSONObject();
					 jo.put("res", "12");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
		         }
	         }
	         
//	         if(!filetype.equals("pdf"))
//	         {
//	        	 JSONObject jo=new JSONObject();
//				 jo.put("res", "12");
//				 jo.put("notif", "Format file salah");
//				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				 return;
//	         }
//	         if(filesize <= 0)
//	         {
//	        	 JSONObject jo=new JSONObject();
//				 jo.put("res", "12");
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
			LogSystem.response(request, jo,kelas, refTrx, trxType);

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
		String format_doc = "";
        JSONObject jo=new JSONObject();
        String res="06";
        int docGagal=0;
        //String signature=null;
        SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
        Documents id_doc=null;
        boolean kirim=false;
        DocumentsDao docDao = new DocumentsDao(db);
        Vector<JSONObject> vStatusDoc=new Vector<>();
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
		        		jo.put("result", "12");
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
	        	
	        	
	        	//check document_id
	        	JSONArray documents=jsonRecv.getJSONArray("documents");
	        	for(int d=0;d<documents.length();d++) {
	        		JSONObject jd=documents.getJSONObject(d);
	        		if(jd.getString("document_id").equals("") || jd.getString("document_id")==null) {
						  jo.put("result", "28");
			              jo.put("notif", "Gagal kirim dokumen karena ada document_id kosong. Dokumen ke "+(d+1));
			              return jo;
					  }
	        	}
	        	
	        	//get payment
	        	char payment= '3';
	        	if(jsonRecv.has("payment")) {
	        		payment=jsonRecv.getString("payment").charAt(0);
	        	}
	        	JSONArray vdocidFailed=new JSONArray();
	        	for(int d=0;d<documents.length();d++) {
	        		JSONObject jstatus=new JSONObject();
	        		JSONObject job=documents.getJSONObject(d);
	        		jstatus.put("document_id", job.getString("document_id"));
	        		jstatus.put("status","failed");
	        		jstatus.put("keterangan", "Gagal save data document");
	        		jstatus.put("state", "0");
	        		
	        		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	      		  	Date date = new Date();
	      		  	String strDate = sdfDate.format(date);
	      		  	String rename = "DS"+strDate+job.getString("document_id")+".pdf";
	      		    String uploadTo =  "/opt/data-DS/UploadFile/"+usr.getId()+"/original/";
	      		    String source = FTPPATH+mitra.getUser_sftp()+"/upload/input";
	      		    jstatus.put("uploadto", uploadTo);
	      		    
	      		  File[] fileList = getFileList(source, job.getString("document_id"));
	              
	              if(fileList.length==0) {
	            	  docGagal++;
	            	  jstatus.put("keterangan", "File tidak ditemukan");
	            	  LogSystem.info(request, d+"=File tidak ditemukan",kelas, refTrx, trxType);
	            	  vStatusDoc.add(jstatus);
	            	  
	            	  JSONObject ject=new JSONObject();
	            	  ject.put("document_id",job.getString("document_id"));
	            	  ject.put("keterangan", "File tidak ditemukan");
	            	  vdocidFailed.put(ject);
	            	  continue;
	              } 
	              else if(fileList.length>1) {
	            	  docGagal++;
	            	  jstatus.put("keterangan", "Multiple file");
	            	  LogSystem.info(request, d+"=Multiple file",kelas, refTrx, trxType);
	            	  vStatusDoc.add(jstatus);
	            	  JSONObject ject=new JSONObject();
	            	  ject.put("document_id",job.getString("document_id"));
	            	  ject.put("keterangan", "Multiple file");
	            	  vdocidFailed.put(ject);
	            	  continue;
	              }
	              else {
	            	  LogSystem.info(request, d+"=file ditemukan",kelas, refTrx, trxType);
	            	  FileInputStream fis=null;
	            	  FileOutputStream fos=null;
	            	  try {
		            	  fis=new FileInputStream(source+"/"+job.getString("document_id")+".pdf");
		            	  fos=new FileOutputStream(uploadTo+rename);
		            	  byte[] buffer = new byte[1024];
		            	  int c;
		                  while ((c = fis.read(buffer)) > 0) {
		                     fos.write(buffer, 0, c);
		                  }
		                  LogSystem.info(request, "write dokumen",kelas, refTrx, trxType);
		                  
	            	  } catch (Exception e) {
						// TODO: handle exception
	            		  LogSystem.info(request, "write dokumen gagal",kelas, refTrx, trxType);
	            		  docGagal++;
		            	  jstatus.put("keterangan", "Copy File failed");
		            	  vStatusDoc.add(jstatus);
		            	  JSONObject ject=new JSONObject();
		            	  ject.put("document_id",job.getString("document_id"));
		            	  ject.put("keterangan", "Copy File gagal.");
		            	  vdocidFailed.put(ject);
		            	  if (fis != null) {
	            	             fos.close();
	            	          }
	            	          if (fos != null) {
	            	             fis.close();
	            	          }
	            	       continue;
	            	  } 
	            	  finally {
            	         if (fis != null) {
            	             fos.close();
            	          }
            	          if (fos != null) {
            	             fis.close();
            	          }
            	       } 

	              }
	      		    
	        		Documents doc=new Documents();
	        		doc.setWaktu_buat(new Date());
	        		doc.setEeuser(usr);
	        		doc.setFile(uploadTo);
					  doc.setFile_name(job.getString("document_id"));
					  doc.setPath(uploadTo);
					  doc.setSigndoc(rename);
					  doc.setRename(rename);
					  doc.setStatus('T');
//					  if(job.has("payment")) {
//						  doc.setPayment(job.getString("payment").charAt(0));  
//					  } else {
//						  doc.setPayment('3');
//					  }
					  doc.setPayment(payment);
					  doc.setIdMitra(job.getString("document_id"));
					  //tambahan type document
					  if(job.has("tipe_dokumen")) {
						  doc.setType_document(job.getString("tipe_dokumen"));
					  }
					  
					try {
						Long iddoc=new DocumentsDao(db).create2(doc);
						doc.setId(iddoc);
						jstatus.put("keterangan", "berhasil save data document");
						jstatus.put("state", "1");
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(e);
						
					}
					  
					  
					  jstatus.put("iddoc", doc);
					  vStatusDoc.add(jstatus);
	        	}
	        	
				  //FileProcessor fProc=new FileProcessor();
				  
				  //if(fProc.uploadFileMitra(request, db, usr, dataFile, jsonRecv.getString("document_id"))) {
				  /*
				  if(jsonRecv.getString("document_id").equals("") || jsonRecv.getString("document_id")==null) {
					  jo.put("result", "28");
		              jo.put("notif", "document id kosong");
		              return jo;
				  }
				  */
				  
				  /*
				  if(fProc.uploadFILEnQRMitra(request, db, usr, dataFile, jsonRecv.getString("document_id"))) {
					  //fProc.generateQRCode(db);
					  //fProc.getDc().getId();
 					  res="00";
 					 LogSystem.info(request, "Upload Successfully");
					 //System.out.println("Upload Successfully");
				  }
				  */
	        	
	        	
				  
				  //id_doc=new DocumentsDao(db).findByUserAndName(String.valueOf(usr.getId()), fProc.getDc().getRename());
				  //id_doc=fProc.getDc();
				  Vector<DocumentAccess> lttd=new Vector();
				  Vector<EmailVOFTP> emails=new Vector<>();
				  Vector<EmailVOFTP> emailscc=new Vector<>();
				  DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
				  //if(id_doc!=null) {
				  for(int d=0;d<documents.length();d++) {
					  JSONObject jstatus=vStatusDoc.get(d);
					  if(jstatus.getString("state").equals("0"))continue;
					  
					  	JSONObject job=vStatusDoc.get(d);
					  	JSONObject odoc=documents.getJSONObject(d);
					  	id_doc=(Documents) job.get("iddoc");
					  	
					  	/*
						int clear=dAccessDao.clearAkses(id_doc.getId());
						//System.out.println("clear access id: "+id_doc.getId()+", "+clear+" rows");
						LogSystem.info(request, "clear access id: "+id_doc.getId()+", "+clear+" rows",kelas, refTrx, trxType);
						*/
						
						if(!odoc.has("format_doc")) 
						{
							odoc.put("format_doc", "");
						}
						
					
						
						if(odoc.get("req-sign")!=null) {
							JSONArray sendList=odoc.getJSONArray("req-sign");
							FormatPDFDao fdao=new FormatPDFDao(db);
							LetakTtdDao ltdao=new LetakTtdDao(db);
							
							for(int i=0; i<sendList.length(); i++) {
								JSONObject obj=(JSONObject) sendList.get(i);
								
								DocumentAccess da=new DocumentAccess();
								User user= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());
								/*change to eeuser*/
//								if(user!=null)da.setUserdata(user.getUserdata());
								LogSystem.info(request, "Size json object = "+odoc.getString("format_doc").length(),kelas, refTrx, trxType);
								//System.out.println("size json object = "+odoc.getString("format_doc").length());
								if(!obj.has("aksi_ttd")||obj.getString("aksi_ttd").equals(""))obj.put("aksi_ttd", "mt");
								if(!obj.has("user")||obj.getString("user").equals(""))obj.put("user", "ttd0");
								
								if(odoc.getString("format_doc").length()>0) {
									Long idmitra = usr.getMitra().getId();
									//System.out.println("id mitra = "+idmitra);
									LogSystem.info(request, "id mitra = "+idmitra,kelas, refTrx, trxType);
									FormatPdf fd=fdao.findFormatPdf(odoc.getString("format_doc"), idmitra);
									if(fd!=null) {
										
										if(odoc.has("format_doc"))
										{
											LogSystem.info(request, "Format doc : " + odoc.getString("format_doc"),kelas, refTrx, trxType);
											
											if(!odoc.getString("format_doc").equals(""))
											{
												format_doc = odoc.getString("format_doc");
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
														  LogSystem.error(request, "idAcc null",kelas, refTrx, trxType);
														  jo.put("result", "06");
											              jo.put("notif", "Gagal upload dokumen");
											              return jo;
													}
												}catch(Exception e)
												{
													  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
													  jo.put("result", "06");
										              jo.put("notif", "Gagal upload dokumen");
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
												for(EmailVOFTP ev:emails) {
													if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
														input=true;
														break;
													}
												}
												
												if(input==false) {
													//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
				    								//mail.run();
				    								EmailVOFTP evo=new EmailVOFTP();
				    								evo.setEmail(obj.getString("email").toLowerCase());
				    								evo.setUdata(udata);
				    								evo.setReg(reg);
				    								evo.setNama(obj.getString("name"));
				    								evo.setIddocac(idAcc);
				    								evo.setDocument_id(odoc.getString("document_id"));
				    								evo.setIddoc(id_doc.getId());
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
													
													try
													{
														idAcc=dAccessDao.create(da);
														if(idAcc==null || idAcc ==0)
														{
															  LogSystem.info(request, "idAcc null / 0",kelas, refTrx, trxType);
															  jo.put("result", "06");
												              jo.put("notif", "Gagal upload dokumen");
												              return jo;
														}
													}catch(Exception e)
													{
														  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
														  jo.put("result", "06");
											              jo.put("notif", "Gagal upload dokumen");
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
													  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
													  jo.put("result", "06");
										              jo.put("notif", "Gagal upload dokumen");
										              return jo;	
												}
												
												Userdata udata=new Userdata();
												boolean reg=false;
												if(user!=null) {
													udata=user.getUserdata();
													reg=true;
												}
												
												boolean input=false;
												for(EmailVOFTP ev:emails) {
													if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
														input=true;
														break;
													}
												}
												
												if(input==false) {
													//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
				    								//mail.run();
				    								EmailVOFTP evo=new EmailVOFTP();
				    								evo.setEmail(obj.getString("email").toLowerCase());
				    								evo.setUdata(udata);
				    								evo.setReg(reg);
				    								evo.setNama(obj.getString("name"));
				    								evo.setIddocac(idAcc);
				    								evo.setDocument_id(odoc.getString("document_id"));
				    								evo.setIddoc(id_doc.getId());
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
											if(user!=null) {
												if(user.isAuto_ttd()) {
													String key = user.getKey_at_ttd();
													if(key.equalsIgnoreCase(obj.getString("kuser")))sign=true;
													else {
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
												LogSystem.error(request, "idAcc null",kelas, refTrx, trxType); 
												jo.put("result", "06");
								                jo.put("notif", "Gagal upload dokumen");
								                return jo;
											}
										}catch(Exception e)
										{
											jo.put("result", "06");
							                jo.put("notif", "Gagal upload dokumen");
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
										for(EmailVOFTP ev:emails) {
											if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
												input=true;
												break;
											}
										}
										
										if(input==false) {
											//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
		    								//mail.run();
		    								EmailVOFTP evo=new EmailVOFTP();
		    								evo.setEmail(obj.getString("email").toLowerCase());
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								evo.setDocument_id(odoc.getString("document_id"));
		    								evo.setIddoc(id_doc.getId());
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
												
												//System.out.println("visible = "+vis);
												LogSystem.info(request, "Visible = "+vis,kelas, refTrx, trxType);
												da.setVisible(vis);
											} else {
												da.setVisible(true);
											}
											
											//idAcc=dAccessDao.create(da);
											
											try
											{
												idAcc=dAccessDao.create(da);
												
												if(idAcc == null || idAcc == 0)
												{
													jo.put("result", "06");
									                jo.put("notif", "Gagal upload dokumen");
									                return jo;
												}
											}catch(Exception e)
											{
												LogSystem.error(getClass(), e,kelas, refTrx, trxType);
												jo.put("result", "06");
								                jo.put("notif", "Gagal upload dokumen");
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
										
										try 
										{
											new InitialDao(db).create(ini);
										}catch(Exception e)
										{
											LogSystem.error(getClass(), e,kelas, refTrx, trxType);
											jo.put("result", "06");
							                jo.put("notif", "Gagal upload dokumen");
							                return jo;
										}
										Userdata udata=new Userdata();
										boolean reg=false;
										if(user!=null) {
											udata=user.getUserdata();
											reg=true;
										}
										
										boolean input=false;
										for(EmailVOFTP ev:emails) {
											if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
												input=true;
												break;
											}
										}
										
										if(input==false) {
											//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
		    								//mail.run();
		    								EmailVOFTP evo=new EmailVOFTP();
		    								evo.setEmail(obj.getString("email").toLowerCase());
		    								evo.setUdata(udata);
		    								evo.setReg(reg);
		    								evo.setNama(obj.getString("name"));
		    								evo.setIddocac(idAcc);
		    								evo.setDocument_id(odoc.getString("document_id"));
		    								evo.setIddoc(id_doc.getId());
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
						
						if(odoc.get("send-to")!=null) {
							
							JSONArray sendList=odoc.getJSONArray("send-to");
							
							for(int i=0; i<sendList.length(); i++) {
								JSONObject obj=(JSONObject) sendList.get(i);

								if(sendList.getString(i).equals(usr.getNick())) continue;
								boolean sign=false;
								for(EmailVOFTP ev:emails) {
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

								if(user!=null)da.setEeuser(user);
								da.setDocument(id_doc);
								da.setFlag(false);
								da.setType("share");
								da.setDate_sign(null);
								da.setEmail(obj.getString("email").toLowerCase());
								da.setName(obj.getString("name"));
								da.setDatetime(new Date());
								
								Long idAcc = null;
								try
								{
									idAcc = dAccessDao.create(da);
									if(idAcc == null)
									{
										 LogSystem.info(request, "idAcc null",kelas, refTrx, trxType);
										 jo.put("result", "06");
										 jo.put("notif", "Gagal upload dokumen");
										 return jo;
									}
								}catch(Exception e)
								{
									LogSystem.error(getClass(), e,kelas, refTrx, trxType);
									jo.put("result", "06");
					                jo.put("notif", "Gagal upload dokumen");
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
								for(EmailVOFTP ev:emails) {
									if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
										input=true;
										break;
									}
								}
								
								if(input==false) {
									//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
    								//mail.run();
    								EmailVOFTP evo=new EmailVOFTP();
    								evo.setEmail(obj.getString("email").toLowerCase());
    								evo.setUdata(udata);
    								evo.setReg(reg);
    								evo.setNama(obj.getString("name"));
    								evo.setIddocac(idAcc);
    								evo.setDocument_id(odoc.getString("document_id"));
    								evo.setIddoc(id_doc.getId());
    								//emails.add(evo);
    								emailscc.add(evo);
								}

							}
							
						}
						
						
				  }
				  
				  
//				  if(kirim) { // jika hanya share saja maka rubah status menjadi sudah dikirim
//						id_doc.setStatus('T');
//					}
					
				  /*
				  if(odoc.has("payment")) {
					  	char pay = odoc.getString("payment").charAt(0);
						id_doc.setPayment(pay);
						docDao.update(id_doc);  
				  }
				  */
				  
				  int x=0;
				  //System.out.println("List Auto TTD = "+lttd.size());
				  LogSystem.info(request, "List Auto TTD = "+lttd.size(),kelas, refTrx, trxType);
				  LogSystem.info(request, "Mitra ID = MT"+mitra.getId(),kelas, refTrx, trxType);
				  Vector<String> invs=new Vector();
				  LogSystem.info(request, "masuk untuk auto TTD dan payment type = "+id_doc.getPayment(),kelas, refTrx, trxType);
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
				  String inv=null;
				  InvoiceDao idao=new InvoiceDao(db);
				  boolean bayar=false;
				  if(payment=='2') {
//					  inv=kph.setTransaction("MT"+mitra.getId(), 1);
//					  
//					  String[] split=inv.split(" ");
//					  inv=split[1];
					  
					  JSONObject obj=kph.setTransaction("MT"+mitra.getId(), lttd.size(), String.valueOf(id_doc.getId()));
					  if(obj.has("result")) {
						  String result=obj.getString("result");
						  if(result.equals("00")) {
								inv=obj.getString("invoiceid");
								
							} else {
								for(int j=0;j<documents.length();j++) {
									  JSONObject json=documents.getJSONObject(j);
									  Documents objdoc=(Documents) json.get("iddoc");
									  dAccessDao.deleteWhere(objdoc.getId());
									  File file=new File(objdoc.getPath()+objdoc.getRename());
									  file.delete();
									  
									  objdoc.setDelete(true);
									  docDao.update(objdoc);
								  }
								
					  			jo.put("result", "FE");
								jo.put("notif", "System timeout. silahkan coba kembali.");
								return jo;
							}
					  } else {
						  for(int j=0;j<documents.length();j++) {
							  JSONObject json=documents.getJSONObject(j);
							  Documents objdoc=(Documents) json.get("iddoc");
							  dAccessDao.deleteWhere(objdoc.getId());
							  File file=new File(objdoc.getPath()+objdoc.getRename());
							  file.delete();
							  
							  objdoc.setDelete(true);
							  docDao.update(objdoc);
						  }
							
				  			jo.put("result", "FE");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							return jo;
					  }
					  
					  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
					  ivc.setDatetime(new Date());
					  ivc.setAmount(lttd.size());
					  ivc.setEeuser(id_doc.getEeuser());
					  ivc.setExternal_key("MT"+usr.getMitra().getId());
					  ivc.setTenant('1');
					  ivc.setTrx('2');
					  ivc.setKb_invoice(inv);
					  //ivc.setDocument(id_doc);
					  
					  try
					  {
						  idao.create(ivc);
						  bayar=true;
					  }catch(Exception e)
					  {
						  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
						  LogSystem.info(request, "reversal - karena gagal create invoice di DB",kelas, refTrx, trxType);
						  for(int j=0;j<documents.length();j++) {
							  JSONObject json=documents.getJSONObject(j);
							  Documents objdoc=(Documents) json.get("iddoc");
							  dAccessDao.deleteWhere(objdoc.getId());
							  File file=new File(objdoc.getPath()+objdoc.getRename());
							  file.delete();
							  
							  objdoc.setDelete(true);
							  docDao.update(objdoc);
						  }
						  jo.put("result", "06");
			              jo.put("notif", "Gagal upload dokumen");
			              return jo;
					  }
				  } else if(payment=='3') {
					  LogSystem.info(request, "mitra id = "+mitra.getId(),kelas, refTrx, trxType);
					  //inv=kd.setTransaction("MT"+mitra.getId(), documents.length()-docGagal);
					  
					  JSONObject obj=kdh.setTransaction("MT"+mitra.getId(), documents.length()-docGagal, String.valueOf(id_doc.getId()));
					  if(obj.has("result")) {
						  String result=obj.getString("result");
						  if(result.equals("00")) {
								inv=obj.getString("invoiceid");
								
							} else {
								for(int j=0;j<documents.length();j++) {
									  JSONObject json=documents.getJSONObject(j);
									  Documents objdoc=(Documents) json.get("iddoc");
									  dAccessDao.deleteWhere(objdoc.getId());
									  File file=new File(objdoc.getPath()+objdoc.getRename());
									  file.delete();
									  
									  objdoc.setDelete(true);
									  docDao.update(objdoc);
								  }
								
					  			jo.put("result", "FE");
								jo.put("notif", "System timeout. silahkan coba kembali.");
								return jo;
							}
					  } else {
						  for(int j=0;j<documents.length();j++) {
							  JSONObject json=documents.getJSONObject(j);
							  Documents objdoc=(Documents) json.get("iddoc");
							  dAccessDao.deleteWhere(objdoc.getId());
							  File file=new File(objdoc.getPath()+objdoc.getRename());
							  file.delete();
							  
							  objdoc.setDelete(true);
							  docDao.update(objdoc);
						  }
							
				  			jo.put("result", "FE");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							return jo;
					  }
					  
					  LogSystem.info(request, "invoice = "+inv,kelas, refTrx, trxType);
					  //String[] split=inv.split(" ");
					  //inv=split[1];
					  
					  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
					  ivc.setDatetime(new Date());
					  ivc.setAmount(lttd.size()-docGagal);
					  ivc.setEeuser(id_doc.getEeuser());
					  ivc.setExternal_key("MT"+usr.getMitra().getId());
					  ivc.setTenant('2');
					  ivc.setTrx('2');
					  ivc.setKb_invoice(inv);
					  //ivc.setDocument(id_doc);
					  
					  try
					  {
						  idao.create(ivc);
						  bayar=true;
					  }catch(Exception e)
					  {
						  LogSystem.error(getClass(), e,kelas, refTrx, trxType);
						  LogSystem.info(request, "reversal karena gagal create invoice di DB",kelas, refTrx, trxType);
						  for(int j=0;j<documents.length();j++) {
							  JSONObject json=documents.getJSONObject(j);
							  Documents objdoc=(Documents) json.get("iddoc");
							  dAccessDao.deleteWhere(objdoc.getId());
							  File file=new File(objdoc.getPath()+objdoc.getRename());
							  file.delete();
							  
							  objdoc.setDelete(true);
							  docDao.update(objdoc);
						  }
						  jo.put("result", "06");
			              jo.put("notif", "Gagal upload dokumen");
			              return jo;
					  }
				  }
				  
				  if(bayar==true) {
					//Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
				        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

				        //Set the maximum number of connections in the pool
				        connManager.setMaxTotal(200);
				        connManager.setDefaultMaxPerRoute(25);
				        //Create a ClientBuilder Object by setting the connection manager
				        HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);
				 
				        //Build the CloseableHttpClient object using the build() method.
				        CloseableHttpClient httpclient = clientbuilder.build();

						Date start=new Date();
						System.out.println(start+" start process");
						List<Thread> lBulk=new ArrayList<Thread>();
					    Transaction t=db.session().beginTransaction();

						for(DocumentAccess da: lttd) {
							
							SendBulk sb=new SendBulk(da,db,httpclient,inv);
							Thread th=new Thread(sb);
							th.start();
							lBulk.add(th);
						}
						
						for(Thread thr: lBulk) {
							thr.join();
						}

						Date end=new Date();
						int gagal=0;
						int docidGagal=0;
						//String ldoc_id[] = null;
						Vector<String> ldoc=new Vector<>();
						Vector<DocumentAccess> vdocAccSukses=new Vector<>();
						Vector<Documents> vdocGagal=new Vector<>();
						for(DocumentAccess da: lttd) {
							if(!da.isFlag()) {
								gagal++;
								boolean failed=false;
								for(int a=0;a<vdocidFailed.length();a++) {
									JSONObject oje=vdocidFailed.getJSONObject(a);
									if(oje.getString("document_id").equalsIgnoreCase(da.getDocument().getIdMitra())) {
										failed=true;
									}
								}
								if(failed==false) {
									JSONObject ject=new JSONObject();
				            	    ject.put("document_id",da.getDocument().getIdMitra());
				            	    ject.put("keterangan", "Sign dokumen gagal.");
				            	    vdocidFailed.put(ject);
				            	    vdocGagal.add(da.getDocument());
				            	    docidGagal++;
								}
							}
							else {
								boolean ada=false;
								vdocAccSukses.add(da);
								for(String docid:ldoc) {
									if(docid.equalsIgnoreCase(da.getDocument().getIdMitra())) {
										ada=true;
									}
								}
								if(ada==false) {
									ldoc.add(da.getDocument().getIdMitra());
								}
								
							}
							System.out.println("check sign doc:" +da.getId()+" "+da.getDocument().getIdMitra()+" "+da.getDate_sign());
							
						}
						System.out.println(end+" end process, elapsed time :"+((double)(end.getTime()-start.getTime())/1000));
						System.out.println("completed : "+(lttd.size()-gagal)+", failed :"+gagal);
						LogSystem.info(request, "gagal ttd otomatis = "+gagal,kelas, refTrx, trxType);
						t.commit();
						db.close();
						
						if(gagal>0) {
							LogSystem.info(request, "reversal untuk dokumen yang gagal sign.",kelas, refTrx, trxType);
							JSONObject job=null;
							if(payment==2) {
								try {
									job = kph.reverseTransaction(inv, gagal, String.valueOf(id_doc.getId()));
									LogSystem.info(request, "reversal = "+job.toString(),kelas, refTrx, trxType);
								} catch (Exception e) {
									// TODO: handle exception
									LogSystem.error(request, "gagal reversal",kelas, refTrx, trxType);
								}
								
								if(job!=null) {
									if(job.getString("result").equals("00")) {
										Invoice invo=new Invoice();
										invo.setAmount(1);
										invo.setDatetime(new Date());
										invo.setDocument(id_doc);
										invo.setExternal_key("MT"+mitra.getId());
										invo.setKb_invoice(inv);
										invo.setTenant('1');
										invo.setTrx('3');
										invo.setCur_balance(job.getInt("current_balance"));
										idao.create(invo);
									}
									
								}
								
							} else if(payment==3) {
								try {
									job = kdh.reverseTransaction(inv,vdocidFailed.length(), String.valueOf(id_doc.getId()));
									LogSystem.info(request, "Reversal = "+job.toString(),kelas, refTrx, trxType);
								} catch (Exception e) {
									// TODO: handle exception
									LogSystem.error(request, "gagal reversal",kelas, refTrx, trxType);
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
							
							for(Documents dokumen:vdocGagal) {
								  dAccessDao.deleteWhere(dokumen.getId());
								  File file=new File(dokumen.getPath()+dokumen.getRename());
								  file.delete();
								  
								  dokumen.setDelete(true);
								  docDao.update(dokumen);
							  }
							
							for(DocumentAccess dacc:vdocAccSukses) {
								dacc.setInvoice(inv);
								dAccessDao.update(dacc);
							}
						}
						
						LogSystem.info(request, "jumlah dokumen = "+documents.length(),kelas, refTrx, trxType);
						LogSystem.info(request, "jumlah berhasil sign = "+ldoc.size(),kelas, refTrx, trxType);
						LogSystem.info(request, "jumlah dokumen id yg gagal = "+vdocidFailed.length(),kelas, refTrx, trxType);
						
						jo.put("jml_doc", documents.length());
						jo.put("jml_sukses", ldoc.size());
						jo.put("jml_gagal", vdocidFailed.length());
						jo.put("document_id_gagal", vdocidFailed);
						jo.put("result", "00");
						String listdocsukses="";
						for(String docid:ldoc) {
							listdocsukses=listdocsukses+docid+",";
						}
						SendSuksesSignFTP signftp=new SendSuksesSignFTP();
						signftp.kirim(new Date(), usr.getUserdata().getNama(), String.valueOf(usr.getUserdata().getJk()), usr.getNick(), listdocsukses, String.valueOf(mitra.getId()));
						
						
				  }

				  jo.put("notif", "Kirim dokumen berhasil.");
				  userRecv=usr;
				  //id_doc.setStatus('T');
				  //docDao.update(id_doc);
			}else {
				jo=aVerf.setResponFailed(jo);
			}
	
		}
		catch (Exception e) {
			// TODO: handle exception
			res="28";
			jo.put("notif", "Request API tidak lengkap.");
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
		}
        if(!jo.has("result"))jo.put("result", res);
	   return jo;
	}
	
	private static File[] getFileList(String dirPath, String filter) {
        File dir = new File(dirPath);   

        File[] fileList = dir.listFiles(new FilenameFilter() {
            @Override
			public boolean accept(File dir, String name) {
                return name.endsWith(filter+".pdf");
            }
        });
        return fileList;
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
		
}
