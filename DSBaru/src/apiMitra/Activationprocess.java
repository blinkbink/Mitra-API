//package apiMitra;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.EEUtil;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//
//import com.anthonyeden.lib.config.Configuration;
//
//import api.email.SendSuksesRegistrasi;
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.dao.PreRegistrationDao;
//import id.co.keriss.consolidate.dao.UserdataDao;
//import id.co.keriss.consolidate.ee.Alamat;
//import id.co.keriss.consolidate.ee.Login;
//import id.co.keriss.consolidate.ee.PreRegistration;
//import id.co.keriss.consolidate.ee.Userdata;
//import id.co.keriss.consolidate.ee.UserdataManual;
//import id.co.keriss.consolidate.util.DSAPI;
//
//public class Activationprocess extends ActionSupport {
////	Logger log = Logger.getLogger(LOGGER);
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//
//		DB db = getDB(context);
//		HttpServletRequest request = context.getRequest();
//		HttpServletResponse resp = context.getResponse();
//		
//		JSONObject jo = new JSONObject();
//		String res = "05";
//		String notif = "Aktivasi Gagal !";
//		try {
//			String email = "";
//			String preid = "";
//			String method = "";
//			String password = "";
//			String username = "";
//		
//
//			FileItem fileTtd = null;
//			FileItem fileSelfie = null;
//
//			File fileTo;
//
//			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//			Date date = new Date();
//			String strDate = sdfDate.format(date);
//
//			UserManager mgr = new UserManager(db);
//
//			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//			if (!isMultipart) {
////				log.info("### Bukan Multipart");
//			}
//
//			else {
//
//				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
//				List<FileItem> fileItems = upload.parseRequest(request);
//				for (FileItem fileItem : fileItems) {
//					if (fileItem.isFormField()) {
//
////						log.info("### Field " + fileItem.getFieldName());
////						log.info("### Value " + fileItem.getString());
//
//						if (fileItem.getFieldName().equals("method")) {
//							method = fileItem.getString();
//						}
//
//						if (fileItem.getFieldName().equals("preid")) {
//							preid = fileItem.getString();
//						}
//
//						if (fileItem.getFieldName().equals("username")) {
//							username = fileItem.getString();
//						}
//
//						if (fileItem.getFieldName().equals("email")) {
//							String s = fileItem.getString();
//							email = s.toLowerCase();
//						}
//
//						if (fileItem.getFieldName().equals("password")) {
//							password = fileItem.getString();
//						}
//
//					} else {
////						log.info("filenya   " + fileItem.getContentType());
//						if (fileItem.getFieldName().equals("fttd")) {
//							if (fileItem.getSize() > 0) {
//								fileTtd = fileItem;
//							}
//						}
//
//						if (fileItem.getFieldName().equals("fselfie")) {
//							if (fileItem.getSize() > 0) {
//								fileSelfie = fileItem;
//							}
//						}
//
//					}
//				}
//
//			}
//
//			if (method.equals("preregister")) {
//
//			
//				Alamat alamat = new Alamat();
//				org.hibernate.Transaction tx = db.session().beginTransaction();
//
//				try {
//
////					PreregisterDao dd = new PreregisterDao(db);
////					Preregistration pr = dd.findById(Long.valueOf(preid));
//					PreRegistrationDao dd=new PreRegistrationDao(db);
//					PreRegistration pr=dd.findById(Long.valueOf(preid));
//					
//					List<Userdata> userData = new UserdataDao(db).findByKtp(pr.getNo_identitas());
//
//					Login login = null;
//
////					if (userdata == null) {
//					if(userData.size()<1)
//					{
//						login = new Login();
//						login.setUsername(username);
//						login.setPassword(EEUtil.getHash(username,password));
//
//						db.session().save(login);
//
////						log.info("User data NUll");
////						UserdataManual udm = new UserdataManual();
//						UserdataManual udm= new UserdataManual();
//						udm.setId(pr.getId());
//						udm.setMitra(pr.getMitra());
//
//						String uploadTo = "/opt/data-DS/UploadFile/" + udm.getId() + "/original/";
//						File directory = new File(uploadTo);
//
//						if (!directory.exists()) {
////							log.info(uploadTo);
//							directory.mkdirs();
//						}
//
//						if (fileTtd != null) {
//
//							String rename = "TTD" + strDate + ".png";
//							fileTo = new File(uploadTo + rename);
//
//							if (fileTtd != null)
//								fileTtd.write(fileTo);
//
////							udm.setI_ttd(uploadTo + rename);
//							udm.setImageTtd(uploadTo + rename);
////							log.info("#### Save File TTD ####");
//
//						}
//
//						if (fileSelfie != null) {
//
//							String rename = "Selfie" + strDate + ".png";
//							fileTo = new File(uploadTo + rename);
//							fileSelfie.write(fileTo);
//
////							udm.setI_wajah(uploadTo + rename);
//							udm.setImageWajah(uploadTo + rename);
////							log.info("#### Save File Selfie ####");
//						}
//
//						udm.setJk(pr.getJk());
//						udm.setNama(pr.getNama());
//						udm.setTgl_lahir(pr.getTgl_lahir());
//						udm.setNo_identitas(pr.getNo_identitas());
//						udm.setTempat_lahir(pr.getTempat_lahir());
//						udm.setNo_handphone(pr.getNo_handphone());
//						udm.setNpwp(pr.getNpwp());
////						udm.setI_ktp(pr.getI_ktp());
//						udm.setImageKTP(pr.getImageKTP());
//
//						if (pr.getImageWajah() != null) {
//							udm.setImageWajah(pr.getImageWajah());
//						}
//
//						if (pr.getImageTtd() != null) {
//							udm.setImageTtd(pr.getImageTtd());
//						}
//
//						udm.setImageNPWP(pr.getImageNPWP());
//
//						db.session().save(udm);
//
//						Userdata userdata = new Userdata();
//						userdata.setId(pr.getId());
//
//						alamat.setUserdata(userdata);
//						alamat.setKecamatan(pr.getKecamatan());
//						alamat.setPropinsi(pr.getPropinsi());
//						alamat.setKelurahan(pr.getKelurahan());
//						alamat.setKodepos(pr.getKodepos());
//						alamat.setKota(pr.getKota());
//						alamat.setStatus('1');
//
//						db.session().save(alamat);
//					} else {
//						
//						
//						User us = new UserManager(db).findByUserID(String.valueOf(userData.get(0).getId()));
//						
//						login = us.getLogin();
//						
//						
//						
//						
//					}
//
////					log.info("ID :" + userdata.getId());
//
//					Date time = new Date();
//
//					User ee = new User();
//					ee.setNick(pr.getEmail());
//					ee.setName(pr.getNama());
//					ee.setStatus('3');
//					ee.grant("ds");
//					ee.setTime(time);
//					ee.grant("login");
//					ee.setUserdata(userdata);
//					ee.setLogin(login);
//					ee.logRevision("created", new UserManager(db).findById((long) 0));
//					db.session().save(ee);
//					
//					/*
//					if (pr.get() == '2') {
//						User eecrop = new User();
//						eecrop.setNick(pr.getEmail_perusahaan());
//						eecrop.setName(pr.getNama());
//						eecrop.setStatus('3');
//						eecrop.grant("ds");
//						eecrop.setTime(time);
//						eecrop.grant("login");
//						eecrop.setUserdata(userdata);
//						eecrop.setLogin(login);
//						eecrop.setMitra(pr.getMitra());
//						eecrop.logRevision("created", new UserManager(db).findById((long) 0));
//						db.session().save(eecrop);
//					}
//					*/
//					
//					db.session().delete(pr);
//					tx.commit();
//
//					res = "OK";
//					notif = "Registrasi berhasil silahkan login dengan username : " + login.getUsername();
//
//					try {
////						SendMailSSL sendMailSSL = new SendMailSSL();
////						Userdata udata = new UserdataDao(db).findByUserID(pr.getId());
////						sendMailSSL.sendMailRegisterSuccess(udata, pr.getEmail());
//
////						sendingEmailSuccessRegister mail = new sendingEmailSuccessRegister(pr.getEmail(),pr.getNama(),String.valueOf(pr.getJk()),String.valueOf(pr.getMitra().getId()));
//						SendSuksesRegistrasi sr=new SendSuksesRegistrasi();
//						sr.kirim(pr.getNama(), String.valueOf(pr.getJk()), pr.getEmail(), DSAPI.LOGIN, String.valueOf(pr.getMitra().getId()));
//						//Thread th = new Thread(mail);
//						//th.start();
//
//					} catch (Exception e) {
////						log.error(getClass(), e);
//					}
//
//				} catch (Exception e) {
//					tx.rollback();
////					log.error(getClass(), e);
//				}
//				db.close();
//			}
//
//			jo.put("status", res);
//			jo.put("notif", notif);
//
////			log.info(jo.toString());
//			context.put("jsoncontent", jo.toString());
//			
//		} catch (Exception e) {
//
//			try {
//				jo.put("status", res);
//				jo.put("notif", notif);
//
////				log.info(jo.toString());
//				context.put("jsoncontent", jo.toString());
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
////				log.error(getClass(), e1);
//			}
//
////			log.error(getClass(), e);
//		}
//		
//		
//	}
//	
//	/*
//	class sendingEmailSuccessRegister implements Runnable {
//
//		String rst = "Timeout";
//		// {692015}
//		String USER_AGENT = "Mozilla/5.0";
//
//
//		String dest = "";
//		String name = "";
//		String url = "";
//		String jk = "";
//		String id_mitra ="";
//
//		public sendingEmailSuccessRegister( String email, String nama,String jeniskelamin,String idmit) {
//
//			try {
//				dest = URLEncoder.encode(email, "UTF-8");
//				jk = URLEncoder.encode(jeniskelamin, "UTF-8");
//				name = URLEncoder.encode(nama, "UTF-8");
//				id_mitra = URLEncoder.encode(idmit, "UTF-8");
//				url = EMAIL_HOST +"/sendMailRegisterSuccess.html?link=" + HOST_LINK+ "&nama=" + name+ "&jk=" + jk + "&email=" + dest + "&id_mitra=" + id_mitra;
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//
//		@Override
//		public void run() {
//			try {
//				URL obj = new URL(url);
//				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//				// TODO Auto-generated method stub
//				// Setting basic post request
//				con.setRequestMethod("POST");
//				con.setRequestProperty("User-Agent", USER_AGENT);
//				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//				// con.setRequestProperty("Content-Type", "application/json");
//				// String postJsonData = "{'text':'c54s123','dest' : '081212611881'}";
//				con.setConnectTimeout(5000);// set timeout to 5 seconds
//				// Send post request
//				con.setDoOutput(true);
//				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//				// wr.writeBytes(postJsonData);
//				wr.flush();
//				wr.close();
//
//				int responseCode = con.getResponseCode();
//				log.info("Sending 'POST' request to URL : " + url);
//				// log.info("Post Data : " + postJsonData);
//				log.info("Response Code : " + responseCode);
//
//				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//				String output;
//				StringBuffer response = new StringBuffer();
//
//				while ((output = in.readLine()) != null) {
//					response.append(output);
//				}
//				in.close();
//
//				// printing result from response
//				log.info(response.toString());
//				String sr = response.toString();
//				JSONObject jo = new JSONObject(sr);
//				rst = jo.getString("result");
//				log.info(rst);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				log.error(e);
//			}
//
//		}
//
//	}
//	*/
//
//}
