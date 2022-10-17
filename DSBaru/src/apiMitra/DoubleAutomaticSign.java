package apiMitra;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
import id.co.keriss.consolidate.dao.CallbackPendingDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.FormatPDFDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.dao.KuserAllowDao;
import id.co.keriss.consolidate.dao.KuserDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.Kuser;
import id.co.keriss.consolidate.ee.KuserAllow;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VO.EmailVO;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.AESEncryption2;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class DoubleAutomaticSign extends ActionSupport {

	static String basepath = "/opt/data-DS/UploadFile/";
	static String basepathPreReg = "/opt/data-DS/PreReg/";
	Date tgl = new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx = "DOC" + sdfDate2.format(tgl).toString();
	String kelas = "apiMitra.DoubleAutomaticSign";
	String trxType = "DAuto-Sign";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl = new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx = "DOC" + sdfDate2.format(tgl).toString();

		HttpServletRequest request = context.getRequest();
		String jsonString = null;
		String filename = null;
		List<FileItem> fileItems = null;

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

			jo = automaticSign(mitra, useradmin, jsonRecv, filename, request, context, refTrx);

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
			HttpServletRequest request, JPublishContext context, String refTrx) throws JSONException {
		DB db = getDB(context);
		String format_doc = "";
		JSONObject jo = new JSONObject();
		String res = "06";
		Documents id_doc = null;
		boolean kirim = false;
		DocumentsDao docDao = new DocumentsDao(db);
		Boolean sequence = false;
		Vector<Integer> list_seq = new Vector<>();

		try {
			ApiVerification aVerf = new ApiVerification(db);
			boolean vrf = false;
			if (mitratoken != null && useradmin != null) {
				vrf = true;
			} else {
				vrf = aVerf.verification(jsonRecv);
			}

			if (vrf) {
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
				} else {
					usr = useradmin;
					mitra = mitratoken;
				}

				Vector<DocumentAccess> lttd = new Vector<DocumentAccess>();
				Vector<EmailVO> emails = new Vector<>();
				Vector<EmailVO> emailscc = new Vector<>();
				DocumentsAccessDao dAccessDao = new DocumentsAccessDao(db);

				if (!jsonRecv.has("document_id")) {
					jo.put("result", "83");
					jo.put("notif", "document id harus dikirimkan");
					return jo;
				}
				
			
				//List<Documents> ldoc = docDao.findByDocIdMitraLatest(jsonRecv.getString("document_id"), mitra.getId());
				List<Documents> ldoc = docDao.findByDocID(jsonRecv.getString("document_id"));
				if(ldoc.size() < 1)
				{
					jo.put("result", "05");
					jo.put("notif", "Document id tidak ditemukan");
					return jo;
				}
				
				id_doc = ldoc.get(0);
				
				
				if(id_doc.getKode_authorized() == null)
				{
					jo.put("result", "83");
					jo.put("notif", "Kode Authorized dokumen ini kosong");
					return jo;
				}
				else
				{
					if(!id_doc.getKode_authorized().equals(jsonRecv.getString("kode_authorized")))
					{
						jo.put("result", "85");
						jo.put("notif", "Kode authorized dokumen salah");
						return jo;
					}
				}
				LogSystem.info(request, "KODE AUTHORIZATION = " + id_doc.getKode_authorized(), kelas, refTrx, trxType);
				String version = null;
				// Create for document access
				if (jsonRecv.get("req-sign") != null) {
					JSONArray sendList = jsonRecv.getJSONArray("req-sign");

					if (sendList.length() == 0) {
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

					for (int i = 0; i < sendList.length(); i++) 
					{
						JSONObject obj = (JSONObject) sendList.get(i);
						
						//Check email format
						if (!isValid(obj.getString("email").toLowerCase())) {
							jo.put("result", "FE");
							jo.put("notif", "Format email salah.");
							return jo;
						}
						
						DocumentAccess da = new DocumentAccess();
						User user = new UserManager(db).findByUsername(obj.getString("email").toLowerCase());

						// check untuk user dengan level 2
						if (user != null) 
						{
							int levelmitra = Integer.parseInt(mitra.getLevel().substring(1));
							int leveluser = Integer.parseInt(user.getUserdata().getLevel().substring(1));
							if (levelmitra > 2 && leveluser < 3) {
								File file = new File(id_doc.getPath() + id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);
								
								jo.put("result", "06");
								jo.put("notif", "User tidak diperkenankan menerima dokumen dari mitra. Silahkan registrasi ulang atau konfirmasi ke DigiSign.");
								return jo;
							}
						}
						// end check untuk user dengan level 2

						/* change to eeuser */
//						LogSystem.info(request, "Size json object = " + jsonRecv.getString("format_doc").length(), kelas, refTrx, trxType);

						if (!obj.has("user") || obj.getString("user").equals("")) {
							obj.put("user", "ttd0");
						}

						Long idmitra = usr.getMitra().getId();

						LogSystem.info(request, "id mitra = " + idmitra, kelas, refTrx, trxType);
						
						LogSystem.info(request, "USERRRRRRR = " + obj.getString("user").substring(0, 3), kelas, refTrx, trxType);

//						List<DocumentAccess> docAccess = dAccessDao.findAccessByEmailDoc(obj.getString("email"), ldoc.get(0).getId());
						List<DocumentAccess> docAccess = dAccessDao.findDocEEuser(String.valueOf(id_doc.getId()), String.valueOf(user.getId()), obj.getString("email"));
						LogSystem.info(request, "DOC ACCESS SIZE = " + docAccess.size(), kelas, refTrx, trxType);
						LogSystem.info(request, "DOCUMENT ID = " + id_doc.getId(), kelas, refTrx, trxType);
						if(docAccess.size() == 0)
						{
							jo.put("result", "86");
							jo.put("notif", "User tidak diijinkan tandatangan dokumen ini.");
							return jo;
						}
						
						for(int j = 0 ; j < docAccess.size() ; j++)
						{
							//Untuk Sign
							if(docAccess.get(j).getType().equals("sign"))
							{
								LogSystem.info(request, "FLAG DOC ACCESS = " + docAccess.get(j).isFlag(), kelas, refTrx, trxType);

								if(docAccess.get(j).isFlag())
								{
									jo.put("result", "06");
									jo.put("notif", "User "+obj.getString("email")+" sudah melakukan tandatangan otomatis.");
									return jo;
								}		
								
								LogSystem.info(request, "DOCUMENT ID = " + id_doc.getId(), kelas, refTrx, trxType);
								
								boolean sign = false;

								LogSystem.info(request, "Masuk auto ttd.", kelas, refTrx, trxType);
								if (user != null) {
									if (user.isAuto_ttd()) {
										//KuserDao kdo = new KuserDao(db);

										//Kuser kuser = kdo.findByEeuser(user.getId());
										
										if(id_doc.getType_document()==null||id_doc.equals("")) {
											if(user.getKey_at_ttd().equals(obj.getString("kuser")))
											{
												LogSystem.info(request, "id doc access = " + docAccess.get(j).getId(), kelas, refTrx, trxType);
												lttd.add(docAccess.get(j));
												LogSystem.info(request, "SIZE LTTD = " + lttd.size(), kelas, refTrx, trxType);
												LogSystem.info(request, "LX LTTD = " + lttd.get(0).getLx(), kelas, refTrx, trxType);
												LogSystem.info(request, "LY LTTD = " + lttd.get(0).getLy(), kelas, refTrx, trxType);
											}
											else
											{
												jo.put("result", "84");
												jo.put("notif", "Kuser "+obj.getString("email")+" tidak valid");
												return jo;
											}
										} else {
											
											KuserDao kdo=new KuserDao(db);
											KuserAllowDao klo=new KuserAllowDao(db);
											//FormatPDFDao pdf=new FormatPDFDao(db);
											//MitraDao md=new MitraDao(db);
											Kuser key=kdo.findByEeuserKuser(user.getId(), obj.getString("kuser"));
											if(key!=null) {
												Mitra mitrapengirim=id_doc.getEeuser().getMitra();
												
												//FormatPdf fd=pdf.findFormatPdf(id_doc.getType_document(), mitrapengirim.getId());
												KuserAllow ka=klo.allowKuser(key, id_doc.getFormat_pdf());
												if(ka!=null) {
													lttd.add(docAccess.get(j));
												} else {
													jo.put("result", "84");
													jo.put("notif", "Kuser "+obj.getString("email")+" tidak valid");
													return jo;
												}
												
											} else {
												jo.put("result", "84");
												jo.put("notif", "Kuser "+obj.getString("email")+" tidak valid");
												return jo;
											}
										}
											
										
										
									} 
									else 
									{
										jo.put("result", "07");
										jo.put("notif", "user "+obj.getString("email")+" tidak diijinkan untuk ttd otomatis");
										return jo;
									}
								} 
								else {
									dAccessDao.deleteWhere(id_doc.getId());
									File file = new File(id_doc.getPath() + id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);

									jo.put("result", "07");
									jo.put("notif", "user "+obj.getString("email")+" belum terdaftar, tidak bisa menggunakan ttd otomatis");
									return jo;
								}

								LogSystem.info(request, "Sign = " + sign, kelas, refTrx, trxType);

								Userdata udata = new Userdata();
								boolean reg = false;
								if (user != null) {
									udata = user.getUserdata();
									reg = true;
								}

								boolean input = false;
								for (EmailVO ev : emails) {
									if (ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
										input = true;
										break;
									}
								}

								if (input == false) {

									EmailVO evo = new EmailVO();
									evo.setEmail(obj.getString("email").toLowerCase());
									evo.setUdata(udata);
									evo.setReg(reg);
									evo.setNama(obj.getString("name"));
									evo.setIddocac(docAccess.get(j).getId());
									emails.add(evo);
								}
							}
							//Untuk Paraf
							else
							{
								boolean prf = false;

								LogSystem.info(request, "Masuk auto paraf", kelas, refTrx, trxType);

								if (user != null) {
									if (user.isAuto_ttd()) 
									{
										//KuserDao kdo = new KuserDao(db);

										//Kuser kuser = kdo.findByEeuser(user.getId());
										
										
											if (user.getKey_at_ttd().equals(obj.getString("kuser"))) 
											{
												prf = true;
												lttd.add(docAccess.get(j));
												LogSystem.info(request, "Paraf true", kelas, refTrx, trxType);
												da.setAction("at");
											} 
											else 
											{
												File file = new File(id_doc.getPath() + id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);
												jo.put("result", "83");
												jo.put("notif", "Kuser "+obj.getString("email")+" tidak valid");
												return jo;
											}
										
									} 
									else 
									{
										dAccessDao.deleteWhere(id_doc.getId());
										File file = new File(id_doc.getPath() + id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);

										jo.put("result", "07");
										jo.put("notif", "user "+obj.getString("email")+" tidak diijinkan untuk prf otomatis");
										return jo;
									}
								} else {
									dAccessDao.deleteWhere(id_doc.getId());
									File file = new File(id_doc.getPath() + id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);

									jo.put("result", "07");
									jo.put("notif", "user "+obj.getString("email")+" belum terdaftar, tidak bisa menggunakan prf otomatis");
									return jo;
								}
								
								Long idAcc = (long) 0;
								if (!isValid(obj.getString("email").toLowerCase())) {
									dAccessDao.deleteWhere(id_doc.getId());
									File file = new File(id_doc.getPath() + id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);

									jo.put("result", "FE");
									jo.put("notif", "Format email salah.");
									return jo;
								}

								LogSystem.info(request, "prf = " + prf, kelas, refTrx, trxType);

								Userdata udata = new Userdata();
								boolean reg = false;
								if (user != null) {
									udata = user.getUserdata();
									reg = true;
								}

								boolean input = false;
								for (EmailVO ev : emails) {
									if (ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
										input = true;
										break;
									}
								}

								if (input == false) 
								{
									EmailVO evo = new EmailVO();
									evo.setEmail(obj.getString("email").toLowerCase());
									evo.setUdata(udata);
									evo.setReg(reg);
									evo.setNama(obj.getString("name"));
									evo.setIddocac(idAcc);
									emails.add(evo);
								}
							}
						}
					}
				}

				int x = 0;
				LogSystem.info(request, "List Auto TTD = " + lttd.size(), kelas, refTrx, trxType);
				
				if(lttd.size() <= 0)
				{
					jo.put("result", "05");
					jo.put("notif", "Dokumen sudah ditandatangan.");
					return jo;
				}

				Vector<String> invs = new Vector<String>();
				// koneksi ke billing
				KillBillDocumentHttps kdh = null;
				KillBillPersonalHttps kph = null;
				try {
					kdh = new KillBillDocumentHttps(request, refTrx);
					kph = new KillBillPersonalHttps(request, refTrx);
				} catch (Exception e) {
					// TODO: handle exception
					dAccessDao.deleteWhere(id_doc.getId());
					File file = new File(id_doc.getPath() + id_doc.getRename());
					file.delete();
					new DocumentsDao(db).delete(id_doc);

					jo.put("result", "91");
					jo.put("notif", "System timeout. silahkan coba kembali.");
					return jo;
				}

				LogSystem.info(request, "sequence = " + sequence, kelas, refTrx, trxType);
				if (sequence == true) {

					for (x = 0; x < list_seq.size() - 1; x++) 
					{
						for (int i = 0; i < list_seq.size() - 1; i++) 
						{
							if (list_seq.get(i) > list_seq.get(i + 1)) 
							{
								int j = list_seq.get(i);
								list_seq.set(i, list_seq.get(i + 1));
								list_seq.set(i + 1, j);
							}
						}
					}
					LogSystem.info(request, "current_sequence = " + list_seq.get(0), kelas, refTrx, trxType);
					id_doc.setCurrent_seq(list_seq.get(0));
					docDao.update(id_doc);

					for (x = 0; x < lttd.size() - 1; x++) {
						for (int i = 0; i < lttd.size() - 1; i++) {
							if (lttd.get(i).getSequence_no() > lttd.get(i + 1).getSequence_no()) {
								DocumentAccess j = lttd.get(i);
								lttd.set(i, lttd.get(i + 1));
								lttd.set(i + 1, j);
							}
						}
					}
				}

				int loop = 0;
				int sequenceNext = 0;
				for (DocumentAccess docac : lttd) {
					LogSystem.info(request, "masuk untuk auto TTD dan payment type = " + id_doc.getPayment(), kelas, refTrx, trxType);
					LogSystem.info(request, "id dokumen akses = " + docac.getId(), kelas, refTrx, trxType);

					boolean potong = false;
					String inv = null;
					User u = docac.getEeuser();
					int jmlttd = lttd.size();

					// check Sequence
					if (sequence == true) {

						if (docac.getSequence_no() > id_doc.getCurrent_seq()) {
							LogSystem.info(request, "Tidak jadi karena lebih dari current sequence", kelas, refTrx,
									trxType);
							loop++;
							break;
						} else {
							if (loop < lttd.size()) {
								sequenceNext = list_seq.get(loop + 1);
							}
							loop++;
						}
					}

					InvoiceDao idao = new InvoiceDao(db);
					if (id_doc.getPayment() == '2') {

						int balance = 0;
						JSONObject resp = kph.getBalance("MT" + usr.getMitra().getId(), request);
						if (resp.has("data")) {
							JSONObject data = resp.getJSONObject("data");
							balance = data.getInt("amount");
						} else {
							dAccessDao.deleteWhere(id_doc.getId());
							File file = new File(id_doc.getPath() + id_doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(id_doc);

							jo.put("result", "91");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							return jo;
						}

						LogSystem.info(request, "balance 2= " + balance, kelas, refTrx, trxType);

						if (balance < jmlttd) {
							dAccessDao.deleteWhere(id_doc.getId());
							File file = new File(id_doc.getPath() + id_doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(id_doc);

							jo.put("result", "61");
							jo.put("notif", "Balance mitra tidak cukup.");
							return jo;
						}

						try {
							// inv=kp.setTransaction("MT"+usr.getMitra().getId(), 1);
							JSONObject obj = kph.setTransaction("MT" + usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
							if (obj.has("result")) {
								String result = obj.getString("result");
								if (result.equals("00")) {
									inv = obj.getString("invoiceid");
								} else {
									dAccessDao.deleteWhere(id_doc.getId());
									File file = new File(id_doc.getPath() + id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);

									jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
								}
							} else {
								dAccessDao.deleteWhere(id_doc.getId());
								File file = new File(id_doc.getPath() + id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);

								jo.put("result", "91");
								jo.put("notif", "System timeout. silahkan coba kembali.");
								return jo;
							}

						} catch (Exception e) {
							// TODO: handle exception
							try {
								LogSystem.info(request, "coba ke 2 killbill", kelas, refTrx, trxType);
								JSONObject obj = kph.setTransaction("MT" + usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
								if (obj.has("result")) {
									String result = obj.getString("result");
									if (result.equals("00")) {
										inv = obj.getString("invoiceid");

									} else {
										dAccessDao.deleteWhere(id_doc.getId());
										File file = new File(id_doc.getPath() + id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);

										jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										return jo;
									}
								} else {
									dAccessDao.deleteWhere(id_doc.getId());
									File file = new File(id_doc.getPath() + id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);

									jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
								}
							} catch (Exception e2) {
								// TODO: handle exception
								try {
									LogSystem.info(request, "coba ke 3 killbill", kelas, refTrx, trxType);
									// inv=kp.setTransaction("MT"+usr.getMitra().getId(), 1);
									JSONObject obj = kph.setTransaction("MT" + usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
									if (obj.has("result")) {
										String result = obj.getString("result");
										if (result.equals("00")) {
											inv = obj.getString("invoiceid");

										} else {
											dAccessDao.deleteWhere(id_doc.getId());
											File file = new File(id_doc.getPath() + id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);

											jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
									} else {
										dAccessDao.deleteWhere(id_doc.getId());
										File file = new File(id_doc.getPath() + id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);

										jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										return jo;
									}
								} catch (Exception e3) {
									// TODO: handle exception
									LogSystem.error(getClass(), e3, kelas, refTrx, trxType);
//									dAccessDao.deleteWhere(id_doc.getId());
//									File file = new File(id_doc.getPath() + id_doc.getRename());
//									file.delete();
//									new DocumentsDao(db).delete(id_doc);
									// kp.close();
									// kd.close();
									jo.put("result", "06");
									// jo.put("notif", "Upload file berhasil. Autottd berhasil "+x+" dari
									// "+lttd.size());
									jo.put("notif", "Tandatangan otomatis gagal");
									return jo;
								}
							}

						}

						// String[] split=inv.split(" ");
						invs.add(inv);

						id.co.keriss.consolidate.ee.Invoice ivc = new id.co.keriss.consolidate.ee.Invoice();
						ivc.setDatetime(new Date());
						ivc.setAmount(1);
						ivc.setEeuser(id_doc.getEeuser());
						ivc.setExternal_key("MT" + usr.getMitra().getId());
						ivc.setTenant('1');
						ivc.setTrx('2');
						ivc.setKb_invoice(inv);
						ivc.setDocument(id_doc);

						try {
							idao.create(ivc);
						} catch (Exception e) {
							// kp.close();
							// kd.close();
							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//							dAccessDao.deleteWhere(id_doc.getId());
//							File file = new File(id_doc.getPath() + id_doc.getRename());
//							file.delete();
//							new DocumentsDao(db).delete(id_doc);
							jo.put("result", "06");
							jo.put("notif", "Tandatangan otomatis gagal");
							return jo;
						}

						jmlttd--;
						potong = true;
					} else if (id_doc.getPayment() == '3') {

						int balance = 0;
						try {
							JSONObject resp = kdh.getBalance("MT" + usr.getMitra().getId(), request);
							if (resp.has("data")) {
								JSONObject data = resp.getJSONObject("data");
								balance = data.getInt("amount");
							} else {
								dAccessDao.deleteWhere(id_doc.getId());
								File file = new File(id_doc.getPath() + id_doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(id_doc);

								jo.put("result", "91");
								jo.put("notif", "System timeout. silahkan coba kembali.");
								return jo;
							}
						} catch (Exception e) {
							// TODO: handle exception
							LogSystem.error(getClass(), e, kelas, refTrx, trxType);

//							dAccessDao.deleteWhere(id_doc.getId());
//							File file = new File(id_doc.getPath() + id_doc.getRename());
//							file.delete();
//							new DocumentsDao(db).delete(id_doc);
							jo.put("result", "06");
							jo.put("notif", "Tandatangan otomatis gagal");
							return jo;
						}

						LogSystem.info(request, "MT : " + usr.getMitra().getId() + "Balance dokumen : " + balance,
								kelas, refTrx, trxType);
						if (balance < 1) {
							dAccessDao.deleteWhere(id_doc.getId());
							File file = new File(id_doc.getPath() + id_doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(id_doc);

							jo.put("result", "61");
							jo.put("notif", "Balance mitra tidak cukup.");
							return jo;
						}

						List<id.co.keriss.consolidate.ee.Invoice> li = idao.findByDoc(id_doc.getId());
						if (li.size() == 0) {
							try {
								JSONObject obj = kdh.setTransaction("MT" + usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
								if (obj.has("result")) {
									String result = obj.getString("result");
									if (result.equals("00")) {
										inv = obj.getString("invoiceid");

									} else {
										dAccessDao.deleteWhere(id_doc.getId());
										File file = new File(id_doc.getPath() + id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);

										jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										return jo;
									}
								} else {
									dAccessDao.deleteWhere(id_doc.getId());
									File file = new File(id_doc.getPath() + id_doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(id_doc);

									jo.put("result", "91");
									jo.put("notif", "System timeout. silahkan coba kembali.");
									return jo;
								}
							} catch (Exception e) {
								// TODO: handle exception
								try {
									LogSystem.info(request, "coba ke 2 killbill", kelas, refTrx, trxType);
									JSONObject obj = kdh.setTransaction("MT" + usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
									if (obj.has("result")) {
										String result = obj.getString("result");
										if (result.equals("00")) {
											inv = obj.getString("invoiceid");

										} else {
											dAccessDao.deleteWhere(id_doc.getId());
											File file = new File(id_doc.getPath() + id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);

											jo.put("result", "FE");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
									} else {
										dAccessDao.deleteWhere(id_doc.getId());
										File file = new File(id_doc.getPath() + id_doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(id_doc);

										jo.put("result", "91");
										jo.put("notif", "System timeout. silahkan coba kembali.");
										return jo;
									}
								} catch (Exception e2) {
									// TODO: handle exception
									try {
										LogSystem.info(request, "coba ke 3 killbill", kelas, refTrx, trxType);
										JSONObject obj = kdh.setTransaction("MT" + usr.getMitra().getId(), 1, String.valueOf(id_doc.getId()));
										if (obj.has("result")) {
											String result = obj.getString("result");
											if (result.equals("00")) {
												inv = obj.getString("invoiceid");

											} else {
												dAccessDao.deleteWhere(id_doc.getId());
												File file = new File(id_doc.getPath() + id_doc.getRename());
												file.delete();
												new DocumentsDao(db).delete(id_doc);

												jo.put("result", "91");
												jo.put("notif", "System timeout. silahkan coba kembali.");
												return jo;
											}
										} else {
											dAccessDao.deleteWhere(id_doc.getId());
											File file = new File(id_doc.getPath() + id_doc.getRename());
											file.delete();
											new DocumentsDao(db).delete(id_doc);

											jo.put("result", "91");
											jo.put("notif", "System timeout. silahkan coba kembali.");
											return jo;
										}
									} catch (Exception e3) {
										// TODO: handle exception
										LogSystem.error(getClass(), e3, kelas, refTrx, trxType);
//										dAccessDao.deleteWhere(id_doc.getId());
//										File file = new File(id_doc.getPath() + id_doc.getRename());
//										file.delete();
//										new DocumentsDao(db).delete(id_doc);
										jo.put("result", "06");
										jo.put("notif", "Tandatangan otomatis gagal");
										return jo;
									}
								}
							}

							id.co.keriss.consolidate.ee.Invoice ivc = new id.co.keriss.consolidate.ee.Invoice();
							ivc.setDatetime(new Date());
							ivc.setAmount(1);
							ivc.setEeuser(id_doc.getEeuser());
							ivc.setExternal_key("MT" + usr.getMitra().getId());
							ivc.setTenant('2');
							ivc.setTrx('2');
							ivc.setKb_invoice(inv);
							ivc.setDocument(id_doc);

							try {
								idao.create(ivc);
							} catch (Exception e) {
								LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//								dAccessDao.deleteWhere(id_doc.getId());
//								File file = new File(id_doc.getPath() + id_doc.getRename());
//								file.delete();
//								new DocumentsDao(db).delete(id_doc);
								jo.put("result", "06");
								jo.put("notif", "Tandatangan otomatis gagal");
								return jo;
							}

						} else {
							inv = li.get(0).getKb_invoice();
						}

						potong = true;
					}

					if (potong == true) {
						SignDoc sd = new SignDoc();

						try {
							if(sd.signDoc2(u, lttd, inv, db, request, refTrx, id_doc, version))
							//if (sd.signDocAT(u, docac, inv, db, request)) 
							{
								LogSystem.info(request, "Tandatangan dokumen otomatis berhasil", kelas, refTrx, trxType);
								List<DocumentAccess> lda = dAccessDao.findByDoc(id_doc.getId().toString());

								LogSystem.info(request, "size list document = " + lda.size(), kelas, refTrx, trxType);
								int waiting = 0;
								for (DocumentAccess doc : lda) {
									if (doc.getType().equals("sign") || doc.getType().equals("initials")) {
										LogSystem.info(request, "masuk list SIgn or Initials", kelas, refTrx, trxType);
										if (doc.isFlag() == false)
											waiting++;
									}
								}
								if (waiting == 0) 
								{
									id_doc.setSign(true);
									docDao.update(id_doc);
								}

								if (sequence == true) 
								{
									id_doc.setCurrent_seq(sequenceNext);
									docDao.update(id_doc);
								}

							} 
							else 
							{
								LogSystem.info(request, "REVERSAL untuk payment= " + id_doc.getPayment(), kelas, refTrx,
										trxType);
								if (id_doc.getPayment() == '2') {
									for (String in : invs) {
										try {
											JSONObject resp = kph.reverseTransaction(in, 1, String.valueOf(id_doc.getId()));
											LogSystem.info(request, "hasil reversal = " + resp, kelas, refTrx, trxType);
										} catch (Exception e) {
											// TODO: handle exception
											LogSystem.error(request, "reversal gagal", kelas, refTrx, trxType);
										}

										// idao.deleteWhere(in);
										Invoice invo = new Invoice();
										invo.setAmount(1);
										invo.setDatetime(new Date());
										invo.setDocument(id_doc);
										invo.setExternal_key("MT" + mitra.getId());
										invo.setKb_invoice(in);
										invo.setTenant('1');
										invo.setTrx('3');
										idao.create(invo);
									}
								} else if (id_doc.getPayment() == '3') {
									try {
										LogSystem.info(request, "invoicenya adalah = " + inv, kelas, refTrx, trxType);
										if (inv != null) {
											LogSystem.info(request, "MASUK PAK EKO", kelas, refTrx, trxType);
											JSONObject resp = kdh.reverseTransaction(inv, 1, String.valueOf(id_doc.getId()));
											LogSystem.info(request, "hasil reversal = " + resp, kelas, refTrx, trxType);
										}
									} catch (Exception e) {
										// TODO: handle exception
										LogSystem.error(request, "reversal gagal", kelas, refTrx, trxType);
									}

									Invoice invo = new Invoice();
									invo.setAmount(1);
									invo.setDatetime(new Date());
									invo.setDocument(id_doc);
									invo.setExternal_key("MT" + mitra.getId());
									invo.setKb_invoice(inv);
									invo.setTenant('2');
									invo.setTrx('3');
									idao.create(invo);
								}

//								dAccessDao.deleteWhere(id_doc.getId());
//								File file = new File(id_doc.getPath() + id_doc.getRename());
//								file.delete();
//								id_doc.setDelete(true);
//								new DocumentsDao(db).update(id_doc);

								jo.put("result", "06");
								jo.put("notif", "Tandatangan otomatis gagal");
								return jo;
							}

						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							if (id_doc.getPayment() == '2') {
								for (String in : invs) {
									kph.reverseTransaction(in, 1, String.valueOf(id_doc.getId()));
									// idao.deleteWhere(in);
								}
							} else if (id_doc.getPayment() == '3') {
								if (inv != null) {
									kdh.reverseTransaction(inv, 1, String.valueOf(id_doc.getId()));
								}
							}

//							dAccessDao.deleteWhere(id_doc.getId());
//							File file = new File(id_doc.getPath() + id_doc.getRename());
//							file.delete();
//							id_doc.setDelete(true);
//							new DocumentsDao(db).update(id_doc);

							jo.put("result", "06");
							jo.put("notif", "Tandatangan otomatis gagal");
							return jo;
						}

					}
					x++;
				}

				res = "00";
				if (id_doc.getSequence()) {
					List<DocumentAccess> listEmail = dAccessDao.findDocByCurrentSeq(id_doc.getId(),
							id_doc.getCurrent_seq());
					if (mitra.isNotifikasi()) {
						SendTerimaDoc std = new SendTerimaDoc(request, refTrx, kelas, trxType);
						for (DocumentAccess email : listEmail) {
							String link = "";
							try {
								AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
								link = "https://" + DSAPI.DOMAIN + "/doc/pdf.html?frmProcess=getFile&doc="
										+ id_doc.getId() + "&access=" + email.getId();
								// + URLEncoder.encode(docs, "UTF-8");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							if (email.getEeuser() != null) {
								std.kirim(email.getEeuser().getName(),
										String.valueOf(email.getEeuser().getUserdata().getJk()), email.getEmail(),
										usr.getName(), String.valueOf(usr.getUserdata().getJk()), link,
										String.valueOf(mitra.getId()), format_doc);
							} else {
								std.kirim(email.getName(), "", email.getEmail(), usr.getName(),
										String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()),
										format_doc);
							}
						}
					}
				} else {
					for (EmailVO email : emails) {
						LogSystem.info(request, "email = " + email.getEmail(), kelas, refTrx, trxType);

						String link = "";
						try {
							AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
							link = "https://" + DSAPI.DOMAIN + "/doc/pdf.html?frmProcess=getFile&doc=" + id_doc.getId()
									+ "&access=" + email.getIddocac();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						SendTerimaDoc std = new SendTerimaDoc(request, refTrx, kelas, trxType);

						if (mitra.isNotifikasi()) {
							if (email.isReg())
								std.kirim(email.getUdata().getNama(), String.valueOf(email.getUdata().getJk()),
										email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()),
										link, String.valueOf(mitra.getId()), format_doc);
							else
								std.kirim(email.getNama(), "", email.getEmail(), usr.getName(),
										String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()),
										format_doc);
						}
					}
				}

				for (EmailVO emailcc : emailscc) {
					LogSystem.info(request, "email = " + emailcc.getEmail(), kelas, refTrx, trxType);
					emailcc.getIddocac();

					String link = "";
					try {
						link = "https://" + DSAPI.DOMAIN + "/doc/pdf.html?frmProcess=getFile&doc=" + id_doc.getId()
								+ "&access=" + emailcc.getIddocac();
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					SendTerimaDocCC std = new SendTerimaDocCC();

					if (mitra.isNotifikasi()) {

						if (emailcc.isReg())
							std.kirim(emailcc.getUdata().getNama(), String.valueOf(emailcc.getUdata().getJk()),
									emailcc.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link,
									String.valueOf(mitra.getId()), filename);
						else
							std.kirim(emailcc.getNama(), "", emailcc.getEmail(), usr.getName(),
									String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()),
									filename);
					} else {
						if (jsonRecv.has("notifikasi")) {
							if (jsonRecv.getString("notifikasi") == "1") {
								if (emailcc.isReg())
									std.kirim(emailcc.getUdata().getNama(), String.valueOf(emailcc.getUdata().getJk()),
											emailcc.getEmail(), usr.getName(),
											String.valueOf(usr.getUserdata().getJk()), link,
											String.valueOf(mitra.getId()), filename);
								else
									std.kirim(emailcc.getNama(), "", emailcc.getEmail(), usr.getName(),
											String.valueOf(usr.getUserdata().getJk()), link,
											String.valueOf(mitra.getId()), filename);
							}
						}
					}
				}

				jo.put("notif", "Tandatangan dokumen otomatis berhasil.");
				
				//check status document
				String stDoc = "waiting";
				  Long hasil=(long) 0;
				  hasil=dAccessDao.getWaitingSignUserByDoc(String.valueOf(id_doc.getId()));
				  if(hasil==0) {
					  id_doc.setSign(true);
					  stDoc = "complete";
				  }
				  
				id_doc.setStatus('T');
				docDao.update(id_doc);
				
				if(jsonRecv.has("return_document")) {
					  if(jsonRecv.getBoolean("return_document")==true) {
						  Documents dokumen=docDao.findById(id_doc.getId());
						  String pathDoc=dokumen.getPath()+dokumen.getSigndoc();
						  SaveFileWithSamba samba=new SaveFileWithSamba();
						  byte[] encoded = Base64.encode(samba.openfile(pathDoc));
						  jo.put("file", new String(encoded, StandardCharsets.US_ASCII));
					  }
				  }
				
				if(id_doc.getEeuser().getMitra().getSigning_redirect()!=null||id_doc.getEeuser().getMitra().getSigning_redirect()!="") {
					JSONObject msg = new JSONObject();
					msg.put("document_id", id_doc.getIdMitra());
					msg.put("status_document", stDoc);
					msg.put("result", "00");
					msg.put("email_user", lttd.get(0).getEmail());
					msg.put("notif", "Proses tandatangan berhasil");
					
					Mitra mitrapengirim = id_doc.getEeuser().getMitra();
					String key = new TokenMitraDao(db).findByMitra(mitrapengirim.getId()).getAes_key();
					LogSystem.info(request, "key encrypt= "+key, kelas, refTrx, trxType);
					String isi = AESEncryption2.encrypt(msg.toString(), key.getBytes());
					LogSystem.info(request, "encrypt isi callback = "+isi, kelas, refTrx, trxType);
					
					CallbackPending cp = new CallbackPending();
					cp.setCallback(mitrapengirim.getSigning_redirect()+"?msg="+URLEncoder.encode(isi, "UTF-8"));
					cp.setMitra(mitrapengirim);
					cp.setResponse(212);
					cp.setTime(new Date());
					cp.setTipe("redirect");
					new CallbackPendingDao(db).create(cp);
					
					
				}
				
			} else 
			{
				jo = aVerf.setResponFailed(jo);
			}

		} catch (Exception e) {
			// TODO: handle exception
			if (id_doc != null) {
				DocumentsAccessDao dad = new DocumentsAccessDao(db);
//				dad.deleteWhere(id_doc.getId());
//				File file = new File(id_doc.getPath() + id_doc.getRename());
//				file.delete();
//				id_doc.setDelete(true);
//				new DocumentsDao(db).update(id_doc);

				List<Invoice> linv = new InvoiceDao(db).findByDoc(id_doc.getId());
				KillBillDocumentHttps kdh = null;
				KillBillPersonalHttps kph = null;
				try {
					kdh = new KillBillDocumentHttps(request, refTrx);
					kph = new KillBillPersonalHttps(request, refTrx);
				} catch (Exception e1) {

					jo.put("result", "91");
					jo.put("notif", "System timeout. silahkan coba kembali.");
					return jo;
				}

				for (Invoice in : linv) {
					if (in.getTenant() == 1) {
						try {
							kph.reverseTransaction(in.getKb_invoice(), 1, String.valueOf(id_doc.getId()));
						} catch (KillBillClientException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else if (in.getTenant() == 2) {
						try {
							kdh.reverseTransaction(in.getKb_invoice(), 1, String.valueOf(id_doc.getId()));
						} catch (KillBillClientException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}

			}
			res = "28";
			jo.put("notif", "Request API tidak lengkap.");
			LogSystem.error(getClass(), e, kelas, refTrx, trxType);
		}
		if (!jo.has("result"))
			jo.put("result", res);
		return jo;
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

		public void run() {
			// new SendMailSSL().sendMailPreregisterMitra(name, email, id);
			if (reg == true) {
				new SendMailSSL().sendMailFileaReqSign(email, udata, email);
			} else {
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
}
