package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.ActivationSessionDao;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.SigningSessionDao;
import id.co.keriss.consolidate.ee.ActivationSession;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class GenerateOtp extends ActionSupport {

	private boolean isNaN(String idmitra) {
		// TODO Auto-generated method stub
		return false;
	}

	User userRecv;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="GOTP"+sdfDate2.format(tgl).toString();
		
		DB db = getDB(context);
		HttpServletRequest request = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		JSONObject jo = new JSONObject();
		
		String idpre = "";
		boolean cekeeuser = false;
		String idpredec = "";
		String nohp = "";
		String encemail = "";
		String encnohp = "";
		String email = "";
		String idmitra = "";
		String eeuser = "";
		String tujuan = "";
		String tujuancek = "";
		boolean single_login = false;
		User usr = null;
		User user = null;
		String sessionIdAct = null;
		String sessionIdSign = null;
		String path_app = this.getClass().getName();
		String CATEGORY = "OTP";
		String email_req ="";
		String mitra_req ="";
		
		try {
			DocumentsAccessDao dad = new DocumentsAccessDao(db);
			ConfirmCodeDao ccd = new ConfirmCodeDao(db);
			PreRegistrationDao pere = new PreRegistrationDao(db);
			ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
			List<FileItem> fileItems = null;
			// System.out.println("Check");
			
			try {
				fileItems = upload.parseRequest(request);
			} catch (Exception e) {
				LogSystem.error(getClass(), e);
			}

			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField()) {
					if (fileItem.getFieldName().equals("refTrx")) {
						refTrx = fileItem.getString();
					}
					if (fileItem.getFieldName().equals("ptype")) {
						encnohp = fileItem.getString();
						nohp = AESEncryption.decryptIdpreregis(encnohp);;
						
						tujuancek = fileItem.getString();
					}
					if (fileItem.getFieldName().equals("etype")) {
						encemail = fileItem.getString();
						email = AESEncryption.decryptIdpreregis(encemail);;
						
						tujuancek = email;
					}
					if (fileItem.getFieldName().equals("type")) {
						idmitra = fileItem.getString();
						//udah di handle decrypt di belakang
					}
					if (fileItem.getFieldName().equals("setype")) {
						sessionIdSign = fileItem.getString();
					}
					if (fileItem.getFieldName().equals("seacttype")) {
						sessionIdAct = fileItem.getString();
					}
					if (fileItem.getFieldName().equals("utype")) {
						String enceeuser = fileItem.getString();
						eeuser = AESEncryption.decryptIdpreregis(enceeuser);
						LogSystem.info(request,"ENC eeuser> "+ enceeuser, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request,"DEC eeuser> "+ eeuser, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						usr = new UserManager(db).findById(Long.parseLong(eeuser));
						if(email==null || email == "") {
							tujuan = usr.getUserdata().getNo_handphone();
						}else {
							tujuan = usr.getNick();
						}
//						cekeeuser = true;
					}
					if (fileItem.getFieldName().equals("stype")) {
						String sl = fileItem.getString();
						single_login = Boolean.parseBoolean(sl);
						LogSystem.info(request, sl, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					}
					
					if (fileItem.getFieldName().equals("rtype")) {
						idpre = fileItem.getString();
						idpredec = AESEncryption.decryptIdpreregis(idpre);
						
						PreRegistration idpreg = pere.findById(Long.parseLong(idpredec));
						if(email==null || email == "") {
							tujuan = idpreg.getNo_handphone();
						}else {
							tujuan = idpreg.getEmail();
						}
					}
				}
			}
			
			
			LogSystem.info(request,"Encrypt idmitra: "+ idmitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			LogSystem.info(request,"Enc id pre: "+ idpre, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			LogSystem.info(request,"Dec id pre: "+ idpredec, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			LogSystem.info(request, "Check ", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			if(sessionIdSign != null)
			{
				SigningSessionDao signingSessionDB = new SigningSessionDao(db);
		        SigningSession signingSession = new SigningSession();
		        signingSession = signingSessionDB.findSessionId(sessionIdSign);
		        
		        Date datey = new Date();
				Date today = datey;
				Date expire = signingSession.getExpire_time();
				boolean used = signingSession.isUsed();
				if(today.after(expire) ) {
				 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 jo.put("rc", "408");
	    		 jo.put("notif", "Tidak dapat melanjutkan tandatangan. Sesi habis.");
				 context.put("trxjson", jo.toString());
				 LogSystem.info(request, jo.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 return;
				}
				if (used == true) {
		        	 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 jo.put("rc", "401");
	    			 jo.put("notif", "Tidak dapat melanjutkan tandatangan. Sesi sudah digunakan.");
					 context.put("trxjson", jo.toString());
					 LogSystem.info(request, jo.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 return;
				 }
			}
			
			if(sessionIdAct != null)
			{
				 ActivationSessionDao activationSessionDB = new ActivationSessionDao(db);
		         ActivationSession activationSession = new ActivationSession();
		         activationSession = activationSessionDB.findSessionId(sessionIdAct);
		         
		         Date datey = new Date();
				 Date today = datey;
				 Date expire = activationSession.getExpire_time();
				 boolean used = activationSession.isUsed();
				 if(today.after(expire) ) {
					 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 jo.put("rc", "408");
		        	 jo.put("notif", "Tidak dapat melanjutkan aktivasi. Sesi habis.");
		        	 context.put("trxjson", jo.toString());
		        	 return;
				 }
				 if (used == true) {
		        	 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        	 jo.put("rc", "401");
		        	 jo.put("notif", "Tidak dapat melanjutkan aktivasi. Sesi sudah digunakan.");
		        	 context.put("trxjson", jo.toString());
		        	 return;
				 }
			}
			
			ConfirmCode ccode = new ConfirmCode();
			ConfirmCodeDao OTPDAO = new ConfirmCodeDao(db);
			ConfirmCode Exist = OTPDAO.findByPhone(tujuancek);//untuk ngecek datanya ada apa nggak ?
			boolean cek = false;
//			if(cekeeuser==true){
//				tujuan = tujuancek;
//			}
			LogSystem.info(request, "Tujuan: " + tujuan, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			String encryptedIdMitra = null;
			String valueString = null;
			try {  
				Long.parseLong(idmitra);
			    cek = true;
			    LogSystem.info(request, "IdMitra Tanpa Encrypt: " + idmitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			} catch(NumberFormatException e){  
			    cek = false;  
			    LogSystem.info(request, "IdMitra Dengan Encrypt: " + idmitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			}  
			
			if (cek == true) {
				valueString = idmitra;
				LogSystem.info(request, "IdMitra: " + idmitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			} else {
				encryptedIdMitra = AESEncryption.decryptDoc(idmitra); // yang sebelumnya ada
				valueString = encryptedIdMitra;
				LogSystem.info(request, "Decrypted IdMitra1: " + AESEncryption.decryptDoc(idmitra), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				LogSystem.info(request, "Decrypted IdMitra2: " + encryptedIdMitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				
			}
			if((nohp!=null || nohp!="") &&(email == "" || email == null)) {
				LogSystem.info(request,"OTP Clicked Into Phone Number : "+nohp, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//				
				
				if (Exist != null) {
					LogSystem.info(request, "Code masih belum terpakai, kirim yang lama", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					LogSystem.info(request, "Code : " + Exist.getCode(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					LogSystem.info(request, "Nohp : " + tujuan, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					LogSystem.info(request, encryptedIdMitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					
					ConfirmSms sms = new ConfirmSms(request, refTrx);
					// JSONObject respsms = sms.sendingPostRequest(Exist.getCode(), nohp,
					// Long.parseLong(encryptedIdMitra));
					JSONObject respsms = sms.sendingPostRequest(Exist.getCode(), tujuan, Long.parseLong(valueString), mitra_req, email_req);
					if (respsms.getString("result").equals("OK")) {
						jo.put("rc", "00");
						jo.put("notif", "Berhasil kirim OTP");
						jo.put("ceknotif", "6");
						context.put("trxjson", jo.toString());
						return;
					} else {
						LogSystem.error(request, "Error " + respsms.getString("info"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						jo.put("rc", "05");
						jo.put("notif", "Gagal kirim OTP");
						jo.put("ceknotif", "5");
						context.put("trxjson", jo.toString());
						return;
					}
					
					
				} else {
					LogSystem.info(request, "Pakai code baru", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					ccode.setStatus("no");
					ccode.setWaktu_buat(new Date());
					ccode.setMsisdn(tujuan);
					ccode.setEeuser(usr);
					Random rand = new Random();
					int number = rand.nextInt(999999);
					String code = String.format("%06d", number);
					ccode.setCode(code);
	
					try {
						ccd.create(ccode);
					} catch (Exception e) {

						LogSystem.error(getClass(), e);
						jo.put("rc", "05");
						jo.put("notif", "Gagal Kirim OTP");
						jo.put("ceknotif", "4");
						context.put("trxjson", jo.toString());
						return;
					}
	
					if (ccode.getId() > 0) {
						ConfirmSms sms = new ConfirmSms(request, refTrx);
						LogSystem.info(request, "Code :" + code, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, "Nohp :" + tujuan, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, valueString, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						JSONObject respsms = sms.sendingPostRequest(code, tujuan, Long.parseLong(valueString), mitra_req, email_req);
	
						if (respsms.getString("result").equals("OK")) {
							jo.put("rc", "00");
							jo.put("notif", "Berhasil kirim OTP");
							jo.put("ceknotif", "3");
							context.put("trxjson", jo.toString());
							return;
						} else {
							LogSystem.error(request, "Error " + respsms.getString("info"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							jo.put("rc", "05");
							jo.put("notif", "Gagal kirim OTP");
							jo.put("ceknotif", "2");
							context.put("trxjson", jo.toString());
							return;
						}
					}
				}
			}
			if ((email != null || email != "" )&&(nohp==null || nohp=="")) {
				LogSystem.info(request,"OTP Clicked Into Email : "+email, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				String name = null;
				String jenkel= null;
				String nama = null;
				if(single_login) {
					user = new UserManager(db).findByEmailMitra2(email);
					name = user.getName();
					jenkel = user.getUserdata().getJk()=='L'?"Bpk. ":"Ibu ";
					nama = jenkel+name;
				}else {
					PreRegistration pr = pere.findByEmail(email);
			        name = pr.getNama();
			        jenkel = pr.getJk()=='L'?"Bpk. ":"Ibu ";
			        nama = jenkel + name;
				}
				
		        SendMailSSL smsl = new SendMailSSL(request, refTrx);
				if (Exist != null) {
					LogSystem.info(request, "Code masih belum terpakai, kirim yang lama", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					LogSystem.info(request, "Code :" + Exist.getCode(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					LogSystem.info(request, "Email :" + tujuan, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					LogSystem.info(request, encryptedIdMitra, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        JSONObject respemail = smsl.sendMailOTPMail(Exist.getCode(), tujuan, valueString, nama);
			        if (respemail.getString("result").equals("sukses")) {
				            jo.put("rc", "00");
				            jo.put("notif", "Berhasil kirim Kode Verifikasi");
				            jo.put("ceknotif", "6");
				            context.put("trxjson", jo.toString());
				            return;
			        } else {
				            LogSystem.error(request, "Error " + respemail.getString("notif"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				            jo.put("rc", "05");
				            jo.put("notif", "Gagal kirim Kode Verifikasi");
				            jo.put("ceknotif", "5");
				            context.put("trxjson", jo.toString());
				            return;
			        }
					
					
				} else {
					LogSystem.info(request, "Pakai code baru", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					ccode.setStatus("no");
					ccode.setWaktu_buat(new Date());
					ccode.setMsisdn(tujuan);
					ccode.setEeuser(usr);
					Random rand = new Random();
					int number = rand.nextInt(999999);
					String code = String.format("%06d", number);
					ccode.setCode(code);
					try {
						ccd.create(ccode);
					} catch (Exception e) {
	//			        	 LogSystem.info(request, "Error generate OTP");
	//			        	 e.printStackTrace();
	//			        	 context.getResponse().sendError(01);
	//			        	 return;
						LogSystem.error(getClass(), e);
						jo.put("rc", "05");
						jo.put("notif", "Gagal Kirim Kode Verifikasi");
						jo.put("ceknotif", "4");
						context.put("trxjson", jo.toString());
						return;
					}
	
					if (ccode.getId() > 0) {
						LogSystem.info(request, "Code :" + code, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, "email :" + tujuan, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						LogSystem.info(request, valueString, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						JSONObject respemail = smsl.sendMailOTPMail(code, tujuan, valueString, nama);
					    if (respemail.getString("result").equals("sukses")) {
						      jo.put("rc", "00");
						      jo.put("notif", "Berhasil kirim Kode Verifikasi");
						      jo.put("ceknotif", "6");
						      context.put("trxjson", jo.toString());
						      return;
					    } else {
						      LogSystem.error(request, "Error " + respemail.getString("notif"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						      jo.put("rc", "05");
						      jo.put("notif", "Gagal kirim Kode Verifikasi");
						      jo.put("ceknotif", "5");
						      context.put("trxjson", jo.toString());
						      return;
					   }
					}
				}
			}

		} catch (Exception e) {
//            LogSystem.error(getClass(), e);
//            e.printStackTrace();
//            return;
			LogSystem.error(getClass(), e);
			try {
				jo.put("rc", "05");
				jo.put("notif", "Gagal Kirim OTP");
				jo.put("ceknotif", "1");
				context.put("trxjson", jo.toString());
				return;
			} catch (JSONException e1) {
				LogSystem.error(getClass(), e1);
			}

		}

		// context.put("trxjson", 200);
	}
}
