	package id.co.keriss.consolidate.action.km;

import java.io.ByteArrayInputStream;
import java.io.File;
//import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import id.co.keriss.consolidate.DS.Samba;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.KmsService;

import com.anthonyeden.lib.config.Configuration;

import api.callback.callback;
import api.email.SendSuccessGenerateCertificate;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailApi;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.ActivationSessionDao;
import id.co.keriss.consolidate.dao.CallbackPendingDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.RekeningDao;
import id.co.keriss.consolidate.dao.SigningSessionDao;
import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Rekening;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.ActivationSession;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.UserdataManual;
import id.co.keriss.consolidate.util.LogSystem;

public class AktivasiMitra_session extends ActionSupport {
	Logger log = LogManager.getLogger(AktivasiMitra_session.class);
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		Random rand = new Random();
		int number = rand.nextInt(99999999);
		String code = String.format("%06d", number);
		String refTrx="ACTPAGERED"+sdfDate2.format(tgl).toString()+"/A"+code;
		DB db = getDB(context);
		HttpServletRequest request = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		
		JSONObject jo = new JSONObject();
		String res = "05";
		String notif = "Aktivasi Gagal !";
		Mitra mitra = null;
		Transaction tx = null;
		AESEncryption AES = new AESEncryption();
		
		JSONObject logInfo = new JSONObject();
		String email_req = "";
		String mitra_req = "";
		String path_app = this.getClass().getName();
		String CATEGORY = "ACTIVATION";
				
		try {
			String email = "";
			String Encpreid= "";
			String preid = "";
			String sessionid = "";
			String sessionkey = "";
			String method = "Aktifasi";
			String password = "";
			String username = "";
			String sk = "";
			String se = "";

			FileItem fileTtd = null;
			FileItem fileSelfie = null;

			//File fileTo;
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			String strDate = sdfDate.format(date);

			UserManager mgr = new UserManager(db);

			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (!isMultipart) {
			    LogSystem.info(request, "Bukan multipart", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			}

			else {
			    LogSystem.info(request, "Multipart", refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
				List<FileItem> fileItems=null;
				try 
				{
					fileItems = upload.parseRequest(request);
				}
				catch(Exception e)
				{
				    LogSystem.error(getClass(), e);
				}

				for (FileItem fileItem : fileItems) {
					if (fileItem.isFormField()) {

						if (fileItem.getFieldName().equals("preid")) {
							Encpreid = fileItem.getString();
							preid = AESEncryption.decryptIdpreregis(Encpreid);
						    
						}
						if (fileItem.getFieldName().equals("sessionid")) {
							sessionid = fileItem.getString();
						    
						}
						if (fileItem.getFieldName().equals("sessionkey")) {
							sessionkey = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("username")) {
							username = fileItem.getString().toLowerCase();
							
						}

						if (fileItem.getFieldName().equals("email")) {
							String s = fileItem.getString();
							email = s.toLowerCase();
							email_req = email;
						}

						if (fileItem.getFieldName().equals("password")) {
							password = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("se")) {
							se = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("sk")) {
							sk = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("refTrx")) {
							refTrx = fileItem.getString();
						
						}
						

					} else {
						if (fileItem.getFieldName().equals("fttd")) {
							if (fileItem.getSize() > 0) {
								fileTtd = fileItem;
								//System.out.println(fileTtd);
								
							}
						}
					}
				}
			}
			
			LogSystem.info(request, "Session ID : "+sessionid,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    LogSystem.info(request,"Session Key :"+sessionkey,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    
			 ActivationSessionDao activationSessionDB = new ActivationSessionDao(db);
	         ActivationSession activationSession = new ActivationSession();
	         activationSession = activationSessionDB.findSessionId(sessionid);
	         
	         mitra_req = activationSession.getMitra().getName();
	         
	         Date datey = new Date();
			 Date today = datey;
			 Date expire = activationSession.getExpire_time();
			 boolean used = activationSession.isUsed();
			 if(today.after(expire) ) {
				 LogSystem.error(request, "Session has already expired",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 jo.put("rc", "408");
	        	 jo.put("notif", "Tidak dapat melanjutkan aktivasi. Sesi habis.");
	        	 context.put("trxjson", jo.toString());
	        	 return;
			 }
			 if (used == true) {
	        	 LogSystem.error(request, "Session has already expired",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        	 jo.put("rc", "401");
	        	 jo.put("notif", "Tidak dapat melanjutkan aktivasi. Sesi sudah digunakan.");
	        	 context.put("trxjson", jo.toString());
	        	 return;
			 }
			 
		
		    LogSystem.info(request, username,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    LogSystem.info(request, email,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		    LogSystem.info(request, password,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    LogSystem.info(request, refTrx,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			LogSystem.info(request, preid, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			LogSystem.info(request, "File ttd : " + fileTtd,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

			if (method.equals("Aktifasi")) {
				

				LogSystem.info(request, "Tombol Saya Menyetujui Penerbitan Sertifikat Elektronik ? " + se,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				LogSystem.info(request, "Tombol Saya Telah Membaca dan Menyetujui Kebijakan Privasi, Beserta Perjanjian Kepemilikan Sertifikat Elektronik Digisign yang Berlaku ? " + sk,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				if (!sk.equals("true") )
				{
					jo.put("rc", "06");
					jo.put("notif", "User harus menyetujui penerbitan sertifikat elektronik untuk melanjutkan aktifasi");
					context.put("trxjson", jo.toString());
					return;
				}
				
				if (!se.equals("true") )
				{
					jo.put("rc", "06");
					jo.put("notif", "User harus menyetujui syarat kebijakan privasi digisign");
					context.put("trxjson", jo.toString());
					return;
				}
				
				Alamat alamat = new Alamat();
				
				try {
					KmsService kms = new KmsService(request, "KMS Service/"+refTrx, CATEGORY, start, mitra_req, email_req);
					tx = db.session().beginTransaction();
					tx.setTimeout(90);
					Date time = new Date();
					
					PreRegistrationDao dd=new PreRegistrationDao(db);
					PreRegistration pr=dd.findById(Long.valueOf(preid));
				
					Userdata userdata = new UserdataDao(db).findByKtp(pr.getNo_identitas());
					User um = new UserManager(db).findByEmail(pr.getEmail());
					
					Login login = null;
					if(dd.findUsername(username)!=null) {
						jo.put("rc", "05");
						jo.put("notif", "Username Telah Terdaftar");
						context.put("trxjson", jo.toString());
						return;
					}
					
					LogSystem.info(request, "Userdata : " + userdata,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					
					if(um != null)
					{
						jo.put("rc", "05");
						jo.put("notif", "User sudah melakukan aktivasi");
						context.put("trxjson", jo.toString());
						return;
					}
					
					if (userdata == null) {
						LoginDao logindao = new LoginDao(db);

						login = new Login();
						login.setUsername(username);
						login.setPassword(EEUtil.getHash(username,password));
						
						
						try {
						    LogSystem.info(request, "Save login",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							db.session().save(login);
						}catch(Exception e)
						{
						    LogSystem.error(getClass(), e);
							e.printStackTrace();

							jo.put("rc", "05");
							jo.put("notif", "Proses Aktivasi Gagal");
							context.put("trxjson", jo.toString());
							return;
						}
						
						UserdataManual ud= new UserdataManual();
						ud.setId(pr.getId());
						ud.setMitra(pr.getMitra());
						
						//System.out.println("UserDataID : " + pr.getId());
					    LogSystem.info(request, "UserDataID : " + pr.getId(),refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

//						String uploadTo = "/opt/data-DS/UploadFile/" + ud.getId() + "/original/"; //before NAS
					    String uploadTo = "/file2/data-DS/UploadFile/" + ud.getId() + "/original/";
//						File directory = new File(uploadTo); //sshfs
						
//						if (!directory.exists()) {
//							directory.mkdirs();//sshfs
//						}

						if (fileTtd != null) 
						{
							String rename = "TTD" + strDate + ".png";
//							byte[] file = rename.getBytes();
//							fileTo = new File(uploadTo + rename);

							if (fileTtd != null) {
								id.sni.digisign.filetransfer.Samba smb=new id.sni.digisign.filetransfer.Samba(refTrx, request, mitra_req, email_req, CATEGORY, start);
								try {
										smb.write(fileTtd.get(), uploadTo+rename);
								}catch (Exception e) {
						      	     log.error(LogSystem.getLog( ExceptionUtils.getStackTrace(e), refTrx, "ERROR"));
					  				 smb.close();
						      	     throw e;
								}finally {
									smb.close();
								}
							}
								//fileTtd.write(fileTo);//sshfs

							ud.setImageTtd(uploadTo + rename);
							LogSystem.info(request, "Save ttd to db : " + uploadTo + rename, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						}

						ud.setJk(pr.getJk());
						ud.setNama(pr.getNama());
						ud.setTgl_lahir(pr.getTgl_lahir());
						ud.setNo_identitas(pr.getNo_identitas());
						ud.setTempat_lahir(pr.getTempat_lahir());
						ud.setNo_handphone(pr.getNo_handphone());
						ud.setNpwp(pr.getNpwp());
						ud.setImageKTP(pr.getImageKTP());
						
						if (pr.getImageWajah() != null) {
							ud.setImageWajah(pr.getImageWajah());
						}

						if (pr.getImageTtd() != null) {
							ud.setImageTtd(pr.getImageTtd());
						}

						if (pr.getImageNPWP() != null) {
							ud.setImageNPWP(pr.getImageNPWP());
						}
						
						try {
							LogSystem.info(request, "Save ud",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							db.session().save(ud);

						}catch(Exception e)
						{
							e.printStackTrace();
							jo.put("rc", "05");
							jo.put("notif", "Proses Aktifasi Gagal");
							context.put("trxjson", jo.toString());
							return;
						}
						userdata = new Userdata();
						userdata.setId(pr.getId());
						//System.out.println("PR ID : " + pr.getId());
						LogSystem.info(request, "PR ID : " + pr.getId(),refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						
						alamat.setUserdata(userdata);
						alamat.setKecamatan(pr.getKecamatan());
						alamat.setPropinsi(pr.getPropinsi());
						alamat.setKelurahan(pr.getKelurahan());
						alamat.setKodepos(pr.getKodepos());
						alamat.setKota(pr.getKota());
						alamat.setStatus('1');
						
						db.session().save(alamat);
					} else {

						jo.put("rc", "05");
						jo.put("notif", "User sudah melakukan aktifasi");
						context.put("trxjson", jo.toString());
						return;
					}
					
					//Check EE
					User ee = new User();
					ee.setPay_type('3');
					ee.setNick(pr.getEmail().toLowerCase());
					ee.setName(pr.getNama());
					ee.setStatus('3');
					ee.grant("ds");
					ee.setTime(time);
					ee.grant("login");
					ee.setUserdata(userdata);
					ee.setLogin(login);
					ee.setKode_user(pr.getKode_user()==null?null:pr.getKode_user());
					ee.setAuto_ttd(false);
					ee.logRevision("created", new UserManager(db).findById((long) 0));
				
					try {
						LogSystem.info(request, "Save ee",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						db.session().save(ee);
					}catch(Exception e)
					{
						e.printStackTrace();
						jo.put("rc", "05");
						jo.put("notif", "Proses Aktifasi Gagal");
						return;
					}
					if(pr.getKode_bank()!=null && pr.getNo_rekening()!=null && pr.getI_buku_tabungan()!=null) {
						LogSystem.info(request, "Save rekening",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						Rekening rek = new Rekening();
						rek.setImage_buku_tabungan(pr.getI_buku_tabungan());
						rek.setKode_bank(pr.getKode_bank());
						rek.setNo_rekening(pr.getNo_rekening());
						db.session().save(rek);
					}
					
					
					db.session().delete(pr);
					try {
						LogSystem.info(request, "Tryng commit",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

						LogSystem.info(request, "Save DocAccess",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						//DocumentsAccessDao DA = new DocumentsAccessDao(db);
//						//long up_da = ee.getId();
//						DocumentAccess de = new DocumentAccess();
//						de.setEeuser(ee);///////FAIL
//			    		new DocumentsAccessDao(db).update(de);
						
						DocumentsAccessDao accessDao=new DocumentsAccessDao(db);
						List<DocumentAccess>listDocAccess = accessDao.findOtherDoc(ee);
						
						if(listDocAccess.size() > 0)
						{
							for(DocumentAccess acs:listDocAccess) {
								if(acs.getEeuser()==null) {
								acs.setEeuser(ee);
								db.session().update(acs);
								}
							}
						}
						
						ActivationSessionDao asd=new ActivationSessionDao(db);
						long idpre = pr.getId();
						LogSystem.info(request, "ID preregister :"+idpre,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, "Session Key :"+sessionkey,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, "Session id :"+sessionid,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						
						ActivationSession pre = asd.findByUserto(idpre,sessionkey,sessionid);
						pre.setUsed(true);
						db.session().update(pre);
						LogSystem.info(request, "Proses commit",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						tx.commit();
						try {
							LogSystem.info(request, "Creating Certificate",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							JSONObject sertifikat = kms.createSertifikat(ee.getId(), false, pr.getMitra().getLevel());
							
							if (sertifikat.getString("result").equals("00") || sertifikat.getString("result").equals("06"))
							{
								
								if(sertifikat.getString("result").equals("06"))
								{
									LogSystem.info(request, "Hasil penerbitan sertifikat elektronik gagal, sertifikat sudah ada sebelumnya",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								}
							}
						}catch(Exception e){
							e.printStackTrace();
							LogSystem.info(request, "Failed to Creating Certificate",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							//context.getResponse().sendError(05, "Aktivasi Gagal");
//							jo.put("rc", "05");
//							jo.put("notif", "Proses Aktivasi Gagal");
//							context.put("trxjson", jo.toString());
//							tx.rollback();
//							return;
						}
						LogSystem.info(request, "Apakah redirect ? (True/False):: "+pr.isRedirect(),refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");				
						
						String nama = "\"nama\""+":"+"\"true\"";
						String tanggal_lahir = "\"tanggal_lahir\""+":"+"\"true\"";
						String selfie = "\"selfie\""+":"+"\"true\"";
						
						JSONObject verifikasi = new JSONObject();
						verifikasi.put("nik", true);
						verifikasi.put("nama", true);
						verifikasi.put("tanggal_lahir", true);
						verifikasi.put("selfie", true);
						
//						String verifikasi = "{"+nama+","+tanggal_lahir+","+selfie+"}";
						String response_verifikasi = "\"verifikasi\""+":" + verifikasi;
						
						String username_user = "\"email_user\""+":"+"\""+pr.getEmail().toLowerCase()+"\"";
						String nik = "\"nik\""+":"+"\""+pr.getNo_identitas()+"\"";
						String notifikasi = "\"notif\""+":"+"\"Proses Aktivasi Berhasil\"" ;
						String Final = "\"result\""+":"+"\"00\"" ;
						String successresult = "{"+Final+","+notifikasi+","+username_user+","+nik+","+response_verifikasi+"}";
						LogSystem.info(request, "Resultnya:: "+successresult,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						if (pr.getMitra().getActivation_redirect()!=null) {
							long dapatkanidMitra = pr.getMitra().getId();
							TokenMitra tm = mgr.findTokenMitra(dapatkanidMitra);
							if(tm.getAes_key()!=null) {
							String aes_key = tm.getAes_key();
							
							String encryptresult = AES.encryptKeyAndResultRedirect(aes_key,successresult);
							String encoderencrypt = URLEncoder.encode(AESEncryption.encryptKeyAndResultRedirect(aes_key,successresult),"UTF-8");
							LogSystem.info(request, "Sukses DI encrypt :"+encryptresult,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							LogSystem.info(request, "Sukses DI encode encrypt :"+encoderencrypt,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							String decryptresult = AES.decryptKeyAndResultRedirect(aes_key,encryptresult);
							LogSystem.info(request, "Sukses DI decrypt :"+decryptresult,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							
							LogSystem.info(request,"REDIRECT",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							String alamat_redirect = pr.getMitra().getActivation_redirect();
							String concat = alamat_redirect+"?msg="+encoderencrypt;
							LogSystem.info(request, "LINK :"+concat,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							if(pr.isRedirect()==true) {
								jo.put("link", concat);
								LogSystem.info(request, "Redirect Process Works !",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							}
							
								if (pr.isRedirect())
								{
									LogSystem.info(request, "Kirim callback method GET ke : " + concat ,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									//kirim GET
									callback callback = new callback(request, refTrx);
									JSONObject prjo = new JSONObject();
									try {
										callback = new callback(request,refTrx);
										
										prjo = callback.call(concat, mitra_req, email_req, CATEGORY, start);
										LogSystem.info(request, prjo.toString() ,refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									}catch(Exception e)
									{
										LogSystem.info(request, "Gagal kirim callback",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										LogSystem.error(getClass(), e);
									}
									
									LogSystem.info(request, "prjo GET STRING " + prjo.getString("code"),refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									
									if (prjo.getString("code") == "200" || prjo.getInt("code") == 200)
									{
										CallbackPending CP = new CallbackPending();
										CP.setCallback(concat);
										CP.setMitra(pr.getMitra());
										CP.setResponse(200);
										CP.setTipe("redirect");
										new CallbackPendingDao(db).create(CP);
										LogSystem.info(request, "Insert into callback_pending done 200!",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									}
									else
									{
										CallbackPending CP = new CallbackPending();
										CP.setCallback(concat);
										CP.setMitra(pr.getMitra());
										CP.setResponse(212);
										CP.setTipe("redirect");
										new CallbackPendingDao(db).create(CP);
										LogSystem.info(request, "Insert into callback_pending done 212!",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									}
								}
							}
						}
						jo.put("rc", "00");
						jo.put("notif", "Proses Aktivasi Berhasil ");
						context.put("trxjson", jo.toString());
						
					}catch(Exception e)
					{
						e.printStackTrace();
						jo.put("rc", "05");
						jo.put("notif", "Proses Aktivasi Gagal");
						context.put("trxjson", jo.toString());
						return;
					}
					
					try {
						LogSystem.info(request, "Find mitra untuk notifikasi",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						MitraDao mtdao = new MitraDao(db);
						mitra = mtdao.findById(pr.getMitra().getId());
						
						if(mitra.isNotifikasi()) 
						{
							LogSystem.info(request, "Sending email sukses aktifasi :  True",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							SendMailApi sendEmail = new SendMailApi();
							
							sendEmail.sendSuccessActivation(pr.getEmail(), pr.getNama(), pr.getJk(), pr.getMitra().getId(), refTrx, request);
					    }
				
					} catch (Exception e) {
						e.printStackTrace();
						LogSystem.info(request, "Gagal kirim email",refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

						jo.put("rc", "05");
						jo.put("notif", "Proses Aktifasi Gagal");
						context.put("trxjson", jo.toString());
						return;
					}

				} catch (Exception e) {
					//context.getResponse().sendError(07, "Aktivasi Gagal");
					jo.put("rc", "05");
					jo.put("notif", "Proses Aktifasi Gagal");
					context.put("trxjson", jo.toString());

					tx.rollback();
					e.printStackTrace();
					return;
				}
				db.close();
			}
			
			context.put("trxjson", jo.toString());

		} catch (Exception e) {

			try {
				//context.getResponse().sendError(8, "Aktivasi Gagal");
				jo.put("rc", "05");
				jo.put("notif", "Proses Aktifasi Gagal");
				context.put("trxjson", jo.toString());
				return;

			} catch (JSONException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}		
	}
}
