package apiMitra2020;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.bouncycastle.util.encoders.Base64;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import api.email.SendTerimaDoc;
import api.email.SendTerimaDocCC;
import api.generatepdf.GeneratePDF;
import apiMitra.SignDoc;
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
import id.co.keriss.consolidate.dao.KuserAllowDao;
import id.co.keriss.consolidate.dao.KuserDao;
import id.co.keriss.consolidate.dao.LetakTtdDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.Kuser;
import id.co.keriss.consolidate.ee.KuserAllow;
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

public class GenerateDoc extends ActionSupport {

	static String basepath = "/opt/data-DS/UploadFile/";
	static String basepathPreReg = "/opt/data-DS/PreReg/";
	Date tgl = new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx = "DOC" + sdfDate2.format(tgl).toString();
	String kelas = "apiMitra.AutomaticSign";
	String trxType = "GEN-DOC";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl = new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx = "GENDOC" + sdfDate2.format(tgl).toString();
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		HttpServletRequest request = context.getRequest();
		String jsonString = null;
		String filename = null;
		List<FileItem> fileItems = null;
		byte[] dataFile=null;
		Mitra mitra = null;
		User useradmin = null;
		try {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);

			// no multipart form
			if (!isMultipart) {
				LogSystem.info(request, "Bukan multipart", kelas, refTrx, trxType);
				JSONObject jo = new JSONObject();
				jo.put("result", "30");
				jo.put("notif", "Format request API salah.");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
				return;
			}

			// multipart form
			else {
				LogSystem.info(request, "Multipart", kelas, refTrx, trxType);
				TokenMitraDao tmd = new TokenMitraDao(getDB(context));
				String token = request.getHeader("authorization");
				if (token != null) {
					String[] split = token.split(" ");
					if (split.length == 2) {
						if (split[0].equals("Bearer"))
							token = split[1];
					}

					TokenMitra tm = tmd.findByToken(token.toLowerCase());

					if (tm != null) {
						LogSystem.info(request, "Token ada", kelas, refTrx, trxType);
						mitra = tm.getMitra();
					} else {
						LogSystem.error(request, "Token tidak ada", kelas, refTrx, trxType);
						JSONObject jo = new JSONObject();
						jo.put("result", "55");
						jo.put("notif", "token salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						LogSystem.response(request, jo, kelas, refTrx, trxType);
						return;
					}
				}
				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

				LogSystem.info(request, "authorization adalah = " + request.getHeader("authorization"), kelas, refTrx,
						trxType);

				// parse requests
				fileItems = upload.parseRequest(request);

				// Process the uploaded items
				for (FileItem fileItem : fileItems) {
					// a regular form field
					if (fileItem.isFormField()) {
						if (fileItem.getFieldName().equalsIgnoreCase("jsonfield")) {
							jsonString = fileItem.getString();
						}
					}
					else {
						
						 if(fileItem.getFieldName().equals("logo")){
							 fileSave.add(fileItem);
						 }
					}
					
				}
			}

			LogSystem.info(request, "PATH :" + request.getRequestURI(), kelas, refTrx, trxType);

			LogSystem.request(request, fileItems, kelas, refTrx, trxType);
			if (jsonString == null)
				return;
			JSONObject jsonRecv = new JSONObject(jsonString).getJSONObject("JSONFile");

			if (mitra != null) {

				if (!jsonRecv.has("userid")) {
					LogSystem.error(request, "Parameter userid tidak ditemukan", kelas, refTrx, trxType);
					JSONObject jo = new JSONObject();
					jo.put("result", "05");
					jo.put("notif", "Parameter userid tidak ditemukan");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}

				String userid = jsonRecv.getString("userid").toLowerCase();
				UserManager user = new UserManager(getDB(context));
				useradmin = user.findByUsername(userid);
				if (useradmin != null) {
					if (useradmin.getMitra().getId() == mitra.getId() && useradmin.isAdmin()) {
						LogSystem.info(request, "Token dan mitra valid", kelas, refTrx, trxType);
					} else {
						LogSystem.error(request, "Token dan mitra tidak valid", kelas, refTrx, trxType);
						JSONObject jo = new JSONObject();
						jo.put("result", "55");
						jo.put("notif", "Token dan Userid tidak sesuai");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						LogSystem.response(request, jo, kelas, refTrx, trxType);
						return;
					}
				} else {
					LogSystem.error(request, "Userid tidak ditemukan", kelas, refTrx, trxType);
					JSONObject jo = new JSONObject();
					jo.put("result", "55");
					jo.put("notif", "userid tidak ditemukan");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}
			}

			JSONObject jo = null;

			jo = automaticSign(mitra, useradmin, jsonRecv, filename, request, context, fileSave, refTrx);

			String res = "";
			if (jo != null)
				res = new JSONObject().put("JSONFile", jo).toString();
			else
				res = "<b>ERROR 404</b>";

			context.put("trxjson", res);
			LogSystem.response(request, jo, kelas, refTrx, trxType);

		} catch (Exception e) {
			LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			JSONObject jo = new JSONObject();
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

	JSONObject automaticSign(Mitra mitratoken, User useradmin, JSONObject jsonRecv, String filename,
			HttpServletRequest request, JPublishContext context, List<FileItem> fileSave, String refTrx) throws JSONException {
		DB db = getDB(context);
		String format_doc = "";
		JSONObject jo = new JSONObject();
		String res = "06";
		Documents id_doc = null;
		boolean kirim = false;
		DocumentsDao docDao = new DocumentsDao(db);
		DocumentsAccessDao doacDao = new DocumentsAccessDao(db);
		Boolean sequence = false;
		Vector<Integer> list_seq = new Vector<>();
		FileItem filedata = null;

		try {
			ApiVerification aVerf = new ApiVerification(db);
			boolean vrf = false;
			if (mitratoken != null && useradmin != null) {
				vrf = true;
			} else {
				vrf = aVerf.verification(jsonRecv);
			}

			if (vrf) 
			{
				User usr = null;
				Mitra mitra = null;
				if (mitratoken == null && useradmin == null) 
				{
					if (aVerf.getEeuser().isAdmin() == false) 
					{
						jo.put("result", "07");
						jo.put("notif", "userid anda tidak diijinkan.");
						return jo;
					}
					usr = aVerf.getEeuser();
					mitra = usr.getMitra();
				} else 
				{
					usr = useradmin;
					mitra = mitratoken;
				}
				
				FileProcessor fProc = new FileProcessor(refTrx);
				
				Vector<EmailVO> emails = new Vector<>();
				Vector<EmailVO> emailscc = new Vector<>();
				
				int jmlpage = 0;
				
				// Check format PDF mitra
				if(!jsonRecv.has("nama_format")) {
					jo.put("result", "05");
					jo.put("notif", "field nama_format tidak ditemukan");
					return jo;
				} 
				
				FormatPDFDao fdao = new FormatPDFDao(db);
				FormatPdf pdf = fdao.findFormatPdf(jsonRecv.getString("nama_format"), mitra.getId());

				if (pdf == null) {
					LogSystem.info(request, "nama format tidak tersedia", kelas, refTrx, trxType);
					jo.put("result", "08");
					jo.put("notif", "Format doc tidak tersedia.");
					return jo;
				}
				
				 List<Documents> ldoc=docDao.findByDocIdMitra(jsonRecv.getString("document_id"), mitra.getId());
				 if(ldoc.size()>0) 
				 {
					 jo.put("result", "17");
		             jo.put("notif", "Document_id sudah digunakan");
		             return jo;
				 }
				
				//Generate PDF
				JSONObject jsonfield = new JSONObject();
				JSONObject JSONFile = new JSONObject();
		        JSONArray items = new JSONArray();
		        
		        JSONArray itemList = jsonRecv.getJSONArray("items");
		        
//		        FileItem logo = null;
//				for (FileItem fileItem : fileSave) 
//				{
//					if(fileItem.getFieldName().equals("logo"))
//					{
//			    		  logo=fileItem;
//			    		  String extensi = FilenameUtils.getExtension(fileItem.getName());
//			    		  LogSystem.info(request, "Extensi logo = "+extensi, kelas, refTrx,trxType);
//			      	}    
//				}
//				
//				if (logo==null)
//				{
//					jo.put("result", "05");
//					jo.put("notif", "Logo harus dikirim");
//					return jo;
//				}
		        
		        for(int il = 0 ; il < itemList.length() ; il++)
		        {
		        	JSONObject objData = (JSONObject) itemList.get(il);
		        	JSONObject itemData = new JSONObject();
		        	itemData.put("item_name", objData.getString("item_name"));
		        	itemData.put("value", objData.getString("value"));
		        	itemData.put("item_format", "'field");
		        	itemData.put("max_char", "500");
		        	itemData.put("isStatic", false);
		        	
		        	items.put(itemData);
		        }

		        JSONFile.put("JSONFile", items);
		        
		        JSONFile.put("nama_format", jsonRecv.getString("nama_format"));
		        JSONFile.put("img_logo", "");
		        JSONFile.put("items", items);
		        JSONFile.put("id_mitra", mitra.getId());
		        jsonfield.put("JSONFile", JSONFile);
		        
		        GeneratePDF pdfFile = new GeneratePDF();
		        LogSystem.info(request, "Kirim JSON ke GeneratePDF : "  + jsonfield, kelas, refTrx, trxType);
		        JSONObject response = pdfFile.fromAPI(jsonfield);
		        
		        LogSystem.info(request, "Response JSON Generate PDF API : "  + response.toString(), kelas, refTrx, trxType);
		        
		        if(response.has("result"))
		        {
		        	if(response.getString("result").equals("00"))
		        	{
						// Insert to table document
		        		LogSystem.info(request, "BASE64 RESPONSE : "+response.get("file") , kelas, refTrx, trxType);

		                byte[] decodedString = Base64.decode(new String(response.getString("file")).getBytes("UTF-8"));
		                
		                /*
						if (fProc.uploadFILEMitraAT(request, db, usr, decodedString, jsonRecv.getString("document_id"), response.getString("fileName"), jsonRecv)) 
						{
							
							res = "00";
							jmlpage = fProc.getJmlPage();
							LogSystem.info(request, "Upload Successfully", kelas, refTrx, trxType);
						} 
						else 
						{
							jo.put("result", "FE");
							jo.put("notif", "Format dokumen harus pdf");
							return jo;
						}
						*/
						
						JSONObject saveFile=fProc.uploadFILEnQRMitra3(request, db, usr, decodedString, jsonRecv.getString("document_id"), jsonRecv, pdf);
						  if(saveFile!=null) {
							  if(saveFile.getString("rc").equals("00")) {
								  res="00";
			 					  jmlpage=fProc.getJmlPage();
			 					  LogSystem.info(request, "Upload Successfully",kelas, refTrx, trxType);
							  } else if(saveFile.getString("rc").equals("01")) {
								  jo.put("result", "FE");
					              jo.put("notif", "Format dokumen harus pdf");
					              return jo;
							  } else {
								  jo.put("result", "91");
								  jo.put("notif", "Sistem timeout, mohon ulangi proses generate document.");
								  return jo;
							  }
						  } else {
							  jo.put("result", "91");
							  jo.put("notif", "Sistem timeout, mohon ulangi proses generate document.");
							  return jo;
						  }
						
						//DocumentsAccessDao dAccessDao = new DocumentsAccessDao(db);
						
						id_doc = fProc.getDc();
						String version = null;
						// Create for document access
						JSONObject respsave=new JSONObject();
						respsave.put("autottd", false);
						if (jsonRecv.get("req-sign") != null) 
						{
							LogSystem.info(request, "Proses req-sign", kelas, refTrx, trxType);
							JSONArray sendList = jsonRecv.getJSONArray("req-sign");
							//LetakTtdDao ltdao = new LetakTtdDao(db);
							if(sendList.length()==0) {
								jo.put("result", "05");
								jo.put("notif", "field req-sign tidak ditemukan");
								return jo;
							}
							
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

							
							
							respsave=saveDocAccess(refTrx, db, jsonRecv.getJSONArray("req-sign"), pdf, id_doc);
							LogSystem.info(request, respsave.toString(), kelas, refTrx, trxType);
							if(respsave.getString("rc").equals("00")) {
								LogSystem.info(request, "berhasil save doc_access", kelas, refTrx, trxType);
							} else {
								new DocumentsAccessDao(db).deleteWhere(id_doc.getId());
								docDao.delete(id_doc);
								jo.put("result", respsave.getString("rc"));
								jo.put("notif", respsave.getString("keterangan"));
								return jo;
							}
							
						}
						
						if(jsonRecv.get("send-to")!=null) {
							JSONArray sendto=jsonRecv.getJSONArray("send-to");
							DocumentsAccessDao dad=new DocumentsAccessDao(db);
							for(int x=0;x<sendto.length();x++) {
								JSONObject job=sendto.getJSONObject(0);
								List<DocumentAccess> lda=dad.findByDocAndEmail(String.valueOf(id_doc.getId()), job.getString("email"));
								if(lda.size()==0||lda==null) {
									DocumentAccess docacc=new DocumentAccess();
									docacc.setType("share");
									docacc.setDatetime(new Date());
									docacc.setEmail(job.getString("email"));
									docacc.setName(job.getString("name"));
									docacc.setDocument(id_doc);
									
									dad.create(docacc);
								}
							}
						}
						
						DocumentsAccessDao dad=new DocumentsAccessDao(db);
						if(respsave.getBoolean("autottd")) {
							String inv=null;
							int current_balance=0;
							long amount=0;
							Date tglbilling=new Date();
							KillBillDocumentHttps kdh = null;
							KillBillPersonalHttps kph = null;
							
							InvoiceDao invDao=new InvoiceDao(db);
							Invoice ic=new Invoice();
							List<DocumentAccess> lda=dad.findAccessByAT(id_doc.getId());
							  try {
								  kdh=new KillBillDocumentHttps(request, refTrx);
								  kph=new KillBillPersonalHttps(request, refTrx);
							  } catch (Exception e) {
								// TODO: handle exception
								  dad.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									docDao.delete(id_doc);
									
						  			jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
							  }
							  
							if(id_doc.getPayment()=='2') {
								LogSystem.info(request, "payment per sign", kelas, refTrx, trxType);
								amount=lda.size();
								JSONObject obj=kph.setTransaction("MT"+mitra.getId(), lda.size(), String.valueOf(id_doc.getId()));
								if(obj!=null) {
									if(obj.has("result")) {
										  String result=obj.getString("result");
										  if(result.equals("00")) {
												inv=obj.getString("invoiceid");
												current_balance=obj.getInt("current_balance");				
												ic.setTenant('1');
												tglbilling=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("datetime"));
											} else if(result.equals("05")) {
												jo.put("result", "61");
											    jo.put("notif", "Balance mitra tidak cukup.");
											    return jo;
											} else {
												dad.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "91");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
											}
									  } else {
										    dad.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
									  }
								} else {
									dad.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									docDao.delete(id_doc);
									
						  			jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
								}
							} else if(id_doc.getPayment()=='3') {
								LogSystem.info(request, "payment per document", kelas, refTrx, trxType);
								amount=1;
								JSONObject obj=kdh.setTransaction("MT"+mitra.getId(), 1, String.valueOf(id_doc.getId()));
								if(obj!=null) {
									if(obj.has("result")) {
										  String result=obj.getString("result");
										  if(result.equals("00")) {
												inv=obj.getString("invoiceid");
												current_balance=obj.getInt("current_balance");				
												ic.setTenant('2');
												tglbilling=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("datetime"));
											} else if(result.equals("05")) {
												jo.put("result", "61");
											    jo.put("notif", "Balance mitra tidak cukup.");
											    return jo;
											} else {
												dad.deleteWhere(id_doc.getId());
												File file=new File(id_doc.getPath()+id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												
									  			jo.put("result", "91");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
											}
									  } else {
										    dad.deleteWhere(id_doc.getId());
											File file=new File(id_doc.getPath()+id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);
											
								  			jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
									  }
								} else {
									dad.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									docDao.delete(id_doc);
									
						  			jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
								}
							}
							
							
							ic.setAmount(amount);
							ic.setCur_balance(current_balance);
							ic.setDatetime(tglbilling);
							ic.setDocument(id_doc);
							ic.setEeuser(id_doc.getEeuser());
							ic.setExternal_key("MT"+mitra.getId());
							ic.setTrx('2');
							ic.setKb_invoice(inv);
							
							invDao.create(ic);
							
							SignDoc sd=new SignDoc();
							for(DocumentAccess da:lda) {
								Vector<DocumentAccess> vda=new Vector<>();
								vda.add(da);
								if(sd.signDoc2(lda.get(0).getEeuser(), vda, inv, db, request, refTrx, id_doc, version)) {
								//if(sd.signDoc(da.getEeuser(), da, inv, db, request, refTrx)) {
									LogSystem.info(request, "ttd otomatis berhasil", kelas, refTrx, trxType);
								} else {
									JSONObject bill=new JSONObject();
									Invoice invo=new Invoice();
									if(id_doc.getPayment()=='2') {
										bill=kph.reverseTransaction(inv, (int) amount, String.valueOf(id_doc.getId()));
										invo.setTenant('1');
									} else if (id_doc.getPayment()=='3') {
										bill=kdh.reverseTransaction(inv, 1, String.valueOf(id_doc.getId()));
										invo.setTenant('2');
									}
									
									
									id_doc.setDelete(true);
									docDao.update(id_doc);
									
									dad.deleteWhere(id_doc.getId());
									File file=new File(id_doc.getPath()+id_doc.getRename());
									file.delete();
									
									invo.setAmount(1);
									invo.setDatetime(new Date());
									invo.setDocument(id_doc);
									invo.setExternal_key("MT"+mitra.getId());
									invo.setKb_invoice(inv);
									invo.setTrx('3');
									if(bill.has("current_balance")) {
										invo.setCur_balance(bill.getInt("current_balance"));
									}
									invDao.create(invo);
									
									jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
								}								
							}
						}
						 
						 
						 //Send email
						List<DocumentAccess> emailsign=dad.findAccessSignGroupByEmail(id_doc.getId());
						 for(DocumentAccess da:emailsign) {
								//System.out.println("email = "+email.getEmail());
								LogSystem.info(request, "email = "+da.getEmail(),kelas, refTrx, trxType);
								//MailSender mail=new MailSender(email.getEmail(),email.getUdata(),email.isReg(),email.getNama());
								//mail.run();
								
								String docs ="";
								String link ="";
								try {
									   //String idm = "idm="+AESEncryption.encryptDoc(mitra.getId()+";"+email.getEmail()+";"+email.getIddocac());
									   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
									   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+da.getId();
									     //+ URLEncoder.encode(docs, "UTF-8");
									} catch (Exception e1) {
									   // TODO Auto-generated catch block
									   e1.printStackTrace();
									}
								
								SendTerimaDoc std=new SendTerimaDoc(request, refTrx, kelas, trxType);

								if(mitra.isNotifikasi()) {
									//Tambah email dokumen id
									if(da.getEeuser()!=null)std.kirim(da.getEeuser().getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), format_doc);
									else std.kirim(da.getName(), "", da.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), format_doc);
								}
							}
						 
						  //Send email cc
						 List<DocumentAccess> emailcc=dad.findAccessShareGroupByEmail(id_doc.getId());
						  for(DocumentAccess da:emailcc) {
								LogSystem.info(request, "email = "+da.getEmail(),kelas, refTrx, trxType);
							
								String docs ="";
								String link ="";
								try {
									   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
									   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+da.getId();
									     //+ URLEncoder.encode(docs, "UTF-8");
									} catch (Exception e1) {
									   // TODO Auto-generated catch block
									   e1.printStackTrace();
									}
								
								SendTerimaDocCC std=new SendTerimaDocCC();

								if(mitra.isNotifikasi()) {
									
									if(da.getEeuser()!=null)std.kirim(da.getEeuser().getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), filename);
									else std.kirim(da.getName(), "", da.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), filename);
								}
								
							}
						  //End send email and emailcc
		        	}
					else
			        {
			        	jo.put("result", "91");
						jo.put("notif", "Sistem timeout, mohon ulangi proses generate document.");
						return jo;
			        }
		        }
		        else
		        {
		        	try {
		        		docDao.delete(id_doc);
					} catch (Exception e) {
						// TODO: handle exception
						LogSystem.info(request, "document_null", kelas, refTrx, trxType);
					}
		        	
		        	
		        	jo.put("result", "91");
					jo.put("notif", "Sistem timeout, mohon ulangi proses generate document.");
					return jo;
		        }
//		        jo.put("file", response.getString("result"));
			}
			else
			{
				jo = aVerf.setResponFailed(jo);
			}
			if (!jo.has("result"))
			{
				jo.put("result", res);
			}

			jo.put("message", "Dokumen berhasil di-generate");
			return jo;
		}catch(Exception e)
		{
			jo.put("result", "05");
			jo.put("notif", "Gagal generate dokumen");
			LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
			return jo;
		}
	}
			

	class MailSender {

		String email;
		Userdata udata;
		boolean reg;
		String name;

		public MailSender(String email, Userdata udata, boolean reg, String name) {
			this.email = email;
			this.udata = udata;
			this.reg = reg;
			this.name = name;
		}

		public void run() 
		{
			if (reg == true) 
			{
				new SendMailSSL().sendMailFileaReqSign(email, udata, email);
			} else 
			{
				new SendMailSSL().sendMailFileaReqSignNotReg(email, name, email);
			}
		}
	}

	public static boolean isValid(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";

		Pattern pat = Pattern.compile(emailRegex);
		if (email == null)
			return false;
		return pat.matcher(email).matches();
	}
	
	public static JSONObject saveDocAccess(String refTrx, DB db, JSONArray jar, FormatPdf formatdoc, Documents doc) {
		JSONObject resp=new JSONObject();
		
		try {
			resp.put("autottd", false);
			resp.put("rc", "91");
			resp.put("keterangan", "sistem timeout, silahkan coba kembali");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(jar.length()!=formatdoc.getJml_ttd()) {
			try {
				resp.put("rc", "01");
				resp.put("keterangan", "Jumlah ttd tidak sesuai dengan format.");
				return resp;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		Long idformatdoc=formatdoc.getId();
		LetakTtdDao lttd=null;
		UserManager um=null;
		KuserAllowDao kad=null;
		KuserDao kd=null;
		DocumentsAccessDao dad=null;
		InitialDao idao=null;
		try {
			lttd=new LetakTtdDao(db);
			um=new UserManager(db);
			kad=new KuserAllowDao(db);
			kd=new KuserDao(db);
			dad=new DocumentsAccessDao(db);
			idao=new InitialDao(db);
		} catch (Exception e) {
			// TODO: handle exception
			return resp;
		}
		
		for(int x=0;x<jar.length();x++) {
			try {
				JSONObject sign=jar.getJSONObject(x);
				String name=sign.getString("name");
				String email=sign.getString("email");
				String aksi_ttd=sign.getString("aksi_ttd");
				String user=sign.getString("user").substring(0,3);
				if(sign.getString("user").length()<4) {
					resp.put("rc", "FE");
					resp.put("keterangan", "format field 'user' harus disertakan urutan penandatangan. contoh : ttd1/prf1.");
					return resp;
				}
				String ke=sign.getString("user").substring(3);
				boolean visible=true;
				if(sign.has("visible")) {
					if(sign.getString("visible").equals("0"))
						visible=false;
				}
				
				if(!user.equals("ttd") && !user.equals("prf")) {
					resp.put("rc", "FE");
					resp.put("keterangan", "format field 'user' harus ttd/prf. contoh : ttd1/prf1");
					return resp;
				}
				if(user.equalsIgnoreCase("ttd")) {
					
					LetakTtd ttd=lttd.findLetakTtd(ke, idformatdoc);
					if(ttd==null) {
						resp.put("rc", "FE");
						resp.put("keterangan", user+" tidak sesuai dengan format dokumen");
						return resp;
					}
					
					User usr=um.findByEmail(email);
					DocumentAccess docacc=new DocumentAccess();
					if(aksi_ttd.equalsIgnoreCase("at")) {
						resp.put("autottd", true);
						if(usr==null) {
							resp.put("rc", "07");
							resp.put("keterangan", "user belum terdaftar, tidak bisa menggunakan ttd otomatis");
							return resp;
						} else {
							if(!usr.isAuto_ttd()) {
								resp.put("rc", "07");
								resp.put("keterangan", "user tidak diijinkan untuk ttd otomatis.");
								return resp;
							}
							if(!sign.has("kuser")) {
								resp.put("rc", "04");
								resp.put("keterangan", "autosign harus menyertakan kuser.");
								return resp;
							} else {
								String kuser=sign.getString("kuser");
								Kuser key=kd.findByEeuserKuser(usr.getId(), kuser);
								if(key!=null) {
									KuserAllow ka=kad.allowKuser(key, formatdoc);
									if(ka==null) {
										resp.put("rc", "83");
										resp.put("keterangan", "kuser tidak valid untuk format document "+formatdoc.getNama_format());
										return resp;
									} else {
										//checking kuser valid
									}
								} else {
									resp.put("rc", "06");
									resp.put("keterangan", "kuser tidak tersedia.");
									return resp;
								}
							}
						}
						
						docacc.setAction("at");
					} else {
						docacc.setAction("mt");
					}
					
					
					docacc.setDocument(doc);
					docacc.setType("sign");
					docacc.setFlag(false);
					docacc.setEmail(email);
					docacc.setName(name);
					docacc.setPage(ttd.getPage());
					docacc.setLx(ttd.getLx());
					docacc.setLy(ttd.getLy());
					docacc.setRx(ttd.getRx());
					docacc.setRy(ttd.getRy());
					docacc.setDatetime(new Date());
					docacc.setRead(false);
					docacc.setEeuser(usr);
					docacc.setVisible(visible);
					docacc.setSequence_no(0);
					
					dad.create(docacc);
					
					
				} else if(user.equalsIgnoreCase("prf")) {
					LetakTtd prf=lttd.findLetakPrf(ke, idformatdoc);
					if(prf==null) {
						resp.put("rc", "FE");
						resp.put("keterangan", user+" tidak sesuai dengan format dokumen");
						return resp;
					}
					
					User usr=um.findByEmail(email);
					DocumentAccess docacc=dad.findAccessByEmailPrf(email, idformatdoc, "initials");
					boolean newdocacc=false;
					if(aksi_ttd.equalsIgnoreCase("at")) {
						resp.put("autottd", true);
						if(usr==null) {
							resp.put("rc", "07");
							resp.put("keterangan", "user belum terdaftar, tidak bisa menggunakan ttd otomatis");
							return resp;
						} else {
							if(!usr.isAuto_ttd()) {
								resp.put("rc", "07");
								resp.put("keterangan", "user tidak diijinkan untuk auto sign.");
								return resp;
							}
							if(!sign.has("kuser")) {
								resp.put("rc", "04");
								resp.put("keterangan", "autosign harus menyertakan kuser.");
								return resp;
							} else {
								String kuser=sign.getString("kuser");
								Kuser key=kd.findByEeuserKuser(usr.getId(), kuser);
								if(key!=null) {
									KuserAllow ka=kad.allowKuser(key, formatdoc);
									if(ka==null) {
										resp.put("rc", "83");
										resp.put("keterangan", "kuser tidak valid untuk format document "+formatdoc.getNama_format());
										return resp;
									} else {
										//checking kuser valid
									}
								} else {
									resp.put("rc", "06");
									resp.put("keterangan", "kuser tidak tersedia.");
									return resp;
								}
							}
						}
						
						if(docacc==null) {
							docacc=new DocumentAccess();
							newdocacc=true;
							docacc.setAction("at");
						}
						
					} else {
						if(docacc==null) {
							docacc=new DocumentAccess();
							newdocacc=true;
							docacc.setAction("mt");
						}
						
					}
					
					Long iddocacc=null;
					if(newdocacc) {
						docacc.setDocument(doc);
						docacc.setType("initials");
						docacc.setFlag(false);
						docacc.setEmail(email);
						docacc.setName(name);
						docacc.setDatetime(new Date());
						docacc.setRead(false);
						docacc.setEeuser(usr);
						docacc.setVisible(visible);
						docacc.setSequence_no(0);
						
						iddocacc=dad.create(docacc);
						docacc.setId(iddocacc);
					} else {
						iddocacc=docacc.getId();
					}
					
					Initial paraf=new Initial();
					paraf.setDoc_access(docacc);
					paraf.setLx(prf.getLx());
					paraf.setLy(prf.getLy());
					paraf.setRx(prf.getRx());
					paraf.setRy(prf.getRy());
					paraf.setPage(prf.getPage());
					
					idao.create(paraf);
					
				} else {
					resp.put("rc", "FE");
					resp.put("keterangan", "parameter user harus ttd/prf");
					return resp;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return resp;
			}
		}
		
		try {
			resp.put("rc", "00");
			resp.put("keterangan", "save data doc_access berhasil.");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}
}
