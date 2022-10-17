package id.co.keriss.consolidate.action.km;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import api.email.SendSuccessGenerateCertificate;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailApi;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.UserdataManual;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.KMSRSAEncryption;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class AktivasiMitra extends ActionSupport {
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {

		DB db = getDB(context);
		HttpServletRequest request = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		
		JSONObject jo = new JSONObject();
		String res = "05";
		String notif = "Aktivasi Gagal !";
		org.hibernate.Transaction tx=null;
		String username = "";
		
		LoginDao logindao = new LoginDao(db);
		try {
			String email = "";
			String method = "Aktivasi";
			String password = "";
			String refTrx = "";
			String trxType="PRC-ACT";
			String kelas="id.co.keriss.consolidate.action.km.AktivasiMitra";
			String nohp = "";
			String sk = "";
			String se = "";

			FileItem fileTtd = null;
			FileItem fileSelfie = null;

			File fileTo;

			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			String strDate = sdfDate.format(date);

			UserManager mgr = new UserManager(db);

			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (!isMultipart) {
				LogSystem.info(request, "Bukan multipart");
			}

			else {
				LogSystem.info(request, "Multipart");
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
				List<FileItem> fileItems=null;
				LogSystem.info(request, "Check");
				try {
					fileItems = upload.parseRequest(request);
				}catch(Exception e)
				{
					LogSystem.error(getClass(), e);
				}

				for (FileItem fileItem : fileItems) {
					if (fileItem.isFormField()) {

						if (fileItem.getFieldName().equals("username")) {
							username = fileItem.getString();
						    
						}

						if (fileItem.getFieldName().equals("nohp")) {
							nohp = fileItem.getString();
							
						}

						if (fileItem.getFieldName().equals("password")) {
							password = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("refTrx")) {
							refTrx = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("sk")) {
							sk = fileItem.getString();
							
						}
						
						if (fileItem.getFieldName().equals("se")) {
							se = fileItem.getString();
						
						}
					}
					else
					{
						 if (fileItem.getFieldName().equals("fttd")) {
				              if (fileItem.getSize() > 0) {
				                fileTtd = fileItem;
				                
				              }
				            }
					}
				}
			}
			
			LogSystem.info(request, username, kelas, refTrx, trxType);
		    LogSystem.info(request, nohp, kelas, refTrx, trxType);
		    LogSystem.info(request, password, kelas, refTrx, trxType);
		    LogSystem.info(request, refTrx, kelas, refTrx, trxType);
		    LogSystem.info(request, sk, kelas, refTrx, trxType);
		    LogSystem.info(request, se, kelas, refTrx, trxType);
		    LogSystem.info(request, "File ttd : " + fileTtd, kelas, refTrx, trxType);

			if (method.equals("Aktivasi")) {

				LogSystem.info(request, "Tombol Saya Menyetujui Penerbitan Sertifikat Elektronik ? " + se, kelas, refTrx, trxType);
				LogSystem.info(request, "Tombol Saya Telah Membaca dan Menyetujui Kebijakan Privasi, Beserta Perjanjian Kepemilikan Sertifikat Elektronik Digisign yang Berlaku ? " + sk, kelas, refTrx, trxType);
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
					tx = db.session().beginTransaction();

					Date time = new Date();
					
					String encryptedPreregistration = AESEncryption.decryptDoc(nohp);
		    		PreRegistrationDao prDao = new PreRegistrationDao(db);
		    		PreRegistration pr = prDao.findById(Long.parseLong(encryptedPreregistration));
		    		
					User um = new UserManager(db).findByEmail(pr.getEmail());
					
					Login login = null;
					
					if(um != null)
					{
						jo.put("rc", "05");
						jo.put("notif", "User sudah melakukan aktifasi");
						context.put("trxjson", jo.toString());
						return;
					}
					
					if(pr == null)
					{
						jo.put("rc", "05");
						jo.put("notif", "User sudah melakukan aktifasi");
						context.put("trxjson", jo.toString());
						return;
					}
					
					Userdata userdata = new UserdataDao(db).findByKtp(pr.getNo_identitas());
					LogSystem.info(request, "Userdata : " + userdata, kelas, refTrx, trxType);
				
					if (userdata == null) {

						if(logindao.getByUsername(username) != null)
			            {
			              jo.put("rc", "06");
			              jo.put("notif", "Proses Aktifasi Gagal, Username sudah digunakan");
			              context.put("trxjson", jo.toString());
			              return;
			            }
						
						login = new Login();
						login.setUsername(username);
						login.setPassword(password);
						
						try {
							LogSystem.info(request, "Save login", kelas, refTrx, trxType);
							db.session().save(login);
						}
						catch(Exception e)
						{
//							e.printStackTrace();
//							context.getResponse().sendError(01, "Aktivasi Gagal");
//							return;
							LogSystem.error(getClass(), e);
							jo.put("rc", "05");
							jo.put("notif", "Proses Aktifasi Gagal");
							context.put("trxjson", jo.toString());
							return;
						}
						
						UserdataManual ud= new UserdataManual();
						ud.setId(pr.getId());
						ud.setMitra(pr.getMitra());
						
						//System.out.println("UserDataID : " + pr.getId());
						LogSystem.info(request, "UserDataID : " + pr.getId(), kelas, refTrx, trxType);

						String uploadTo = "/file2/data-DS/UploadFile/" + ud.getId() + "/original/";
//						File directory = new File(uploadTo);
//
//						if (!directory.exists()) {
//							directory.mkdirs();
//						}

						if (fileTtd != null) {

							String rename = "TTD" + strDate + ".png";
//							fileTo = new File(uploadTo + rename);
//
//							if (fileTtd != null)
//								fileTtd.write(fileTo);
							SaveFileWithSamba samba=new SaveFileWithSamba();
							samba.write(fileTtd.get(), uploadTo+rename);
							
							ud.setImageTtd(uploadTo + rename);
							LogSystem.info(request, "Save ttd to db : " + uploadTo + rename, kelas, refTrx, trxType);
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
							//System.out.println("Save ud");
							LogSystem.info(request, "Save ud", kelas, refTrx, trxType);
							db.session().save(ud);

						}
						catch(Exception e)
						{
							LogSystem.error(getClass(), e);
							jo.put("rc", "05");
							jo.put("notif", "Proses Aktifasi Gagal");
							context.put("trxjson", jo.toString());
							return;
						}
						userdata = new Userdata();
						userdata.setId(pr.getId());
						LogSystem.info(request, "PR ID : " + pr.getId(), kelas, refTrx, trxType);
						
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
					ee.setNick(pr.getEmail());
					ee.setName(pr.getNama());
					ee.setStatus('3');
					ee.grant("ds");
					ee.setTime(time);
					ee.grant("login");
					ee.setUserdata(userdata);
					ee.setLogin(login);
					//ee.setAuto_ttd(false);
					if(pr.getKode_user()!=null) {
						ee.setKode_user(pr.getKode_user());
					}
					
					ee.logRevision("created", new UserManager(db).findById((long) 0));
				
					System.out.println(ee.getName());
					System.out.println(ee.getNick());

					try {
						//System.out.println("Save ee");
						LogSystem.info(request, "Save ee", kelas, refTrx, trxType);
						db.session().save(ee);
						
					}catch(Exception e)
					{
						LogSystem.error(getClass(), e);
						//tx.rollback();
						
						if(logindao.getByUsername(username) != null)
				        {
				            jo.put("rc", "06");
				            jo.put("notif", "Proses Aktifasi Gagal, Username sudah digunakan");
				            context.put("trxjson", jo.toString());
				            return;
				        }
						
						jo.put("rc", "05");
						jo.put("notif", "Proses Aktifasi Gagal");
						return;
					}

					db.session().delete(pr);
					try {
						LogSystem.info(request, "Tryng commit", kelas, refTrx, trxType);
						DocumentsAccessDao dad=new DocumentsAccessDao(db);
						List<DocumentAccess> vda=dad.findByEmail(pr.getEmail());
						for(DocumentAccess da:vda) {
							da.setEeuser(ee);
							db.session().update(da);
						}
						
						tx.commit();
						
						jo.put("rc", "00");
						jo.put("notif", "Proses Aktivasi Berhasil");
						jo.put("username", username);
						
						//create certificate
						KmsService kms=new KmsService(request, "create certificate/"+refTrx);
						JSONObject sertifikat = kms.createSertifikat(ee.getId(), false, pr.getMitra().getLevel());
						
						if (sertifikat.getString("result").equals("00") || sertifikat.getString("result").equals("06"))
						{
//							if(sertifikat.equals("00"))
//							{
//								LogSystem.info(request, "Hasil penerbitan sertifikat elektronik : " + sertifikat, kelas, refTrx, trxType);
//								SendSuccessGenerateCertificate notifGenCert = new SendSuccessGenerateCertificate();
//								LogSystem.info(request, "Mengirim notifikasi penerbitan sertifikat elektronik : ", kelas, refTrx, trxType);
//								notifGenCert.kirimBaru(pr.getNama(), pr.getJk(), pr.getEmail(), "", pr.getMitra().getId().toString());
//							}
							
							if(sertifikat.getString("result").equals("06"))
							{
								LogSystem.info(request, "Hasil penerbitan sertifikat elektronik gagal, sertifikat sudah ada sebelumnya", kelas, refTrx, trxType);
							}
						}
					}catch(Exception e)
					{
						LogSystem.error(getClass(), e);
						tx.rollback();
						
						if(logindao.getByUsername(username) != null)
				        {
				            jo.put("rc", "06");
				            jo.put("notif", "Proses Aktifasi Gagal, Username sudah digunakan");
				            context.put("trxjson", jo.toString());
				            return;
				        }
						
						jo.put("rc", "05");
						jo.put("notif", "Proses Aktivasi Gagal");
						context.put("trxjson", jo.toString());
						return;
					}
					
					Mitra mitra = null;
					LogSystem.info(request, "Find mitra untuk notifikasi", kelas, refTrx, trxType);
					MitraDao mtdao = new MitraDao(db);
					mitra = mtdao.findById(pr.getMitra().getId());
					
					if(mitra.isNotifikasi()) 
					{
						try {
							LogSystem.info(request, "Sending email sukses aktivasi", kelas, refTrx, trxType);
							SendMailApi sendEmail = new SendMailApi();
							
							sendEmail.sendSuccessActivation(pr.getEmail(), pr.getNama(), pr.getJk(), pr.getMitra().getId());
	
						} catch (Exception e) {
							LogSystem.error(getClass(), e);
							LogSystem.info(request, "Gagal kirim email", kelas, refTrx, trxType);
	
						}
					}

				} catch (Exception e) {
					//tx.rollback();
					LogSystem.error(getClass(), e, kelas, refTrx, trxType);
					
					if(logindao.getByUsername(username) != null)
			        {
			            jo.put("rc", "06");
			            jo.put("notif", "Proses Aktifasi Gagal, Username sudah digunakan");
			            context.put("trxjson", jo.toString());
			            return;
			        }
					
					jo.put("rc", "05");
					jo.put("notif", "Proses Aktifasi Gagal");
					context.put("trxjson", jo.toString());
					return;
				}
				db.close();
			}
			
			jo.put("rc", "00");
			jo.put("notif", "Proses Aktivasi Berhasil");
			jo.put("username", username);
			LogSystem.info(request, "Proses aktivasi berhasil", kelas, refTrx, trxType);
			context.put("trxjson", jo.toString());
			

		} catch (Exception e) {
			//tx.rollback();
			LogSystem.error(getClass(), e);
			
			try {
				if(logindao.getByUsername(username) != null)
		        {
		            jo.put("rc", "06");
		            jo.put("notif", "Proses Aktifasi Gagal, Username sudah digunakan");
		            context.put("trxjson", jo.toString());
		            return;
		        }
				
				jo.put("rc", "05");
				jo.put("notif", "Proses Aktifasi Gagal");
				context.put("trxjson", jo.toString());
				return;
			} catch (JSONException e1) {
				LogSystem.error(getClass(), e1);
				//e1.printStackTrace();
			}
			return;
		}		
	}
}
