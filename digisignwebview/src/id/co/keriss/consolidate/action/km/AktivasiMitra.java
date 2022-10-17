//package id.co.keriss.consolidate.action.km;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
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
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.ajax.SendMailApi;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.LoginDao;
//import id.co.keriss.consolidate.dao.PreRegistrationDao;
//import id.co.keriss.consolidate.dao.UserdataDao;
//import id.co.keriss.consolidate.ee.Alamat;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.Login;
//import id.co.keriss.consolidate.ee.PreRegistration;
//import id.co.keriss.consolidate.ee.Userdata;
//import id.co.keriss.consolidate.ee.UserdataManual;
//import id.co.keriss.consolidate.util.LogSystem;
//
//public class AktivasiMitra extends ActionSupport {
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		Date tgl= new Date();
//		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		String refTrx="ACTMITRA"+sdfDate2.format(tgl).toString();
//		DB db = getDB(context);
//		HttpServletRequest request = context.getRequest();
//		HttpServletResponse resp = context.getResponse();
//		
//		JSONObject jo = new JSONObject();
//		String res = "05";
//		String notif = "Aktivasi Gagal !";
//		org.hibernate.Transaction tx=null;
//		try {
//			String email = "";
//			String preid = "";
//			String method = "Aktivasi";
//			String password = "";
//			String username = "";
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
//				LogSystem.info(request, "Bukan multipart",refTrx);
//			}
//
//			else {
//				LogSystem.info(request, "Multipart",refTrx);
//				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
//				List<FileItem> fileItems=null;
//				LogSystem.info(request, "Check",refTrx);
//				try {
//					fileItems = upload.parseRequest(request);
//				}catch(Exception e)
//				{
//					LogSystem.error(getClass(), e);
//				}
//
//				for (FileItem fileItem : fileItems) {
//					if (fileItem.isFormField()) {
//
//						if (fileItem.getFieldName().equals("preid")) {
//							preid = fileItem.getString();
//						    LogSystem.info(request, preid,refTrx);
//						}
//						
//						if (fileItem.getFieldName().equals("username")) {
//							username = fileItem.getString();
//						    LogSystem.info(request, username,refTrx);
//						}
//
//						if (fileItem.getFieldName().equals("email")) {
//							String s = fileItem.getString();
//							email = s.toLowerCase();
//						    LogSystem.info(request, email,refTrx);
//						}
//
//						if (fileItem.getFieldName().equals("password")) {
//							password = fileItem.getString();
//						    LogSystem.info(request, password,refTrx);
//						}
//						
//
//					} else {
//						if (fileItem.getFieldName().equals("fttd")) {
//							if (fileItem.getSize() > 0) {
//								fileTtd = fileItem;
//								LogSystem.info(request, "File ttd : " + fileTtd,refTrx);
//							}
//						}
//					}
//				}
//			}
//
//			if (method.equals("Aktivasi")) {
//				
//				Alamat alamat = new Alamat();
//				
//				try {
//					tx = db.session().beginTransaction();
//
//					Date time = new Date();
//					PreRegistrationDao dd=new PreRegistrationDao(db);
//					PreRegistration pr=dd.findByEmail(email);
//					
//					User um = new UserManager(db).findByEmail(email);
//					
//					Login login = null;
//					
//					if(um != null)
//					{
//						jo.put("rc", "05");
//						jo.put("notif", "User sudah melakukan aktifasi");
//						context.put("trxjson", jo.toString());
//						return;
//					}
//					
//					if(pr == null)
//					{
//						jo.put("rc", "05");
//						jo.put("notif", "User sudah melakukan aktifasi");
//						context.put("trxjson", jo.toString());
//						return;
//					}
//					
//					Userdata userdata = new UserdataDao(db).findByKtp(pr.getNo_identitas());
//					LogSystem.info(request, "Userdata : " + userdata,refTrx);
//				
//					if (userdata == null) {
//						LoginDao logindao = new LoginDao(db);
//						
//						login = new Login();
//						login.setUsername(username);
//						login.setPassword(EEUtil.getHash(username,password));
//						
//						try {
//							LogSystem.info(request, "Save login",refTrx);
//							db.session().save(login);
//						}
//						catch(Exception e)
//						{
////							e.printStackTrace();
////							context.getResponse().sendError(01, "Aktivasi Gagal");
////							return;
//							LogSystem.error(getClass(), e);
//							jo.put("rc", "05");
//							jo.put("notif", "Proses Aktifasi Gagal");
//							context.put("trxjson", jo.toString());
//							return;
//						}
//						
//						UserdataManual ud= new UserdataManual();
//						ud.setId(pr.getId());
//						ud.setMitra(pr.getMitra());
//						
//						//System.out.println("UserDataID : " + pr.getId());
//						LogSystem.info(request, "UserDataID : " + pr.getId(),refTrx);
//
//						String uploadTo = "/opt/data-DS/UploadFile/" + ud.getId() + "/original/";
//						File directory = new File(uploadTo);
//
//						if (!directory.exists()) {
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
//							ud.setImageTtd(uploadTo + rename);
//							LogSystem.info(request, "Save ttd to db : " + uploadTo + rename,refTrx);
//						}
//
//						ud.setJk(pr.getJk());
//						ud.setNama(pr.getNama());
//						ud.setTgl_lahir(pr.getTgl_lahir());
//						ud.setNo_identitas(pr.getNo_identitas());
//						ud.setTempat_lahir(pr.getTempat_lahir());
//						ud.setNo_handphone(pr.getNo_handphone());
//						ud.setNpwp(pr.getNpwp());
//						ud.setImageKTP(pr.getImageKTP());
//						
//						if (pr.getImageWajah() != null) {
//							ud.setImageWajah(pr.getImageWajah());
//						}
//
//						if (pr.getImageTtd() != null) {
//							ud.setImageTtd(pr.getImageTtd());
//						}
//
//						if (pr.getImageNPWP() != null) {
//							ud.setImageNPWP(pr.getImageNPWP());
//						}
//						
//						try {
//							//System.out.println("Save ud");
//							LogSystem.info(request, "Save ud",refTrx);
//							db.session().save(ud);
//
//						}
//						catch(Exception e)
//						{
//							LogSystem.error(getClass(), e);
//							jo.put("rc", "05");
//							jo.put("notif", "Proses Aktifasi Gagal");
//							context.put("trxjson", jo.toString());
//							return;
//						}
//						userdata = new Userdata();
//						userdata.setId(pr.getId());
//						LogSystem.info(request, "PR ID : " + pr.getId(),refTrx);
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
//						jo.put("rc", "05");
//						jo.put("notif", "User sudah melakukan aktifasi");
//						context.put("trxjson", jo.toString());
//						return;
//					}
//					
//					//Check EE
//					User ee = new User();
//					ee.setPay_type('3');
//					ee.setNick(pr.getEmail());
//					ee.setName(pr.getNama());
//					ee.setStatus('3');
//					ee.grant("ds");
//					ee.setTime(time);
//					ee.grant("login");
//					ee.setUserdata(userdata);
//					ee.setLogin(login);
//					//ee.setAuto_ttd(false);
//					ee.logRevision("created", new UserManager(db).findById((long) 0));
//				
//					System.out.println(ee.getName());
//					System.out.println(ee.getNick());
//
//					try {
//						//System.out.println("Save ee");
//						LogSystem.info(request, "Save ee",refTrx);
//						db.session().save(ee);
//						
//					}catch(Exception e)
//					{
//						LogSystem.error(getClass(), e);
//						jo.put("rc", "05");
//						jo.put("notif", "Proses Aktifasi Gagal");
//						return;
//					}
//					
//					db.session().delete(pr);
//					try {
//						LogSystem.info(request, "Tryng commit",refTrx);
//						DocumentsAccessDao dad=new DocumentsAccessDao(db);
//						List<DocumentAccess> vda=dad.findByEmail(pr.getEmail());
//						for(DocumentAccess da:vda) {
//							da.setEeuser(ee);
//							db.session().update(da);
//						}
//						
//						tx.commit();
//						
//						jo.put("rc", "00");
//						jo.put("notif", "Proses Aktivasi Berhasil");
//						jo.put("username", username);
//						
//					}catch(Exception e)
//					{
//						LogSystem.error(getClass(), e);
//						jo.put("rc", "05");
//						jo.put("notif", "Proses Aktivasi Gagal");
//						context.put("trxjson", jo.toString());
//						return;
//					}
//					
//					try {
//						LogSystem.info(request, "Sending email sukses aktivasi",refTrx);
//						SendMailApi sendEmail = new SendMailApi();
//						
//						sendEmail.sendSuccessActivation(pr.getEmail(), pr.getNama(), pr.getJk(), pr.getMitra().getId(), refTrx, request);
//
//					} catch (Exception e) {
//						LogSystem.error(getClass(), e);
//						LogSystem.info(request, "Gagal kirim email",refTrx);
//
//					}
//
//				} catch (Exception e) {
//					tx.rollback();
//					LogSystem.error(getClass(), e);
//					jo.put("rc", "05");
//					jo.put("notif", "Proses Aktifasi Gagal");
//					context.put("trxjson", jo.toString());
//					return;
//				}
//				db.close();
//			}
//			
//			jo.put("rc", "00");
//			jo.put("notif", "Proses Aktivasi Berhasil");
//			jo.put("username", username);
//			context.put("trxjson", jo.toString());
//
//		} catch (Exception e) {
//
//			try {
//				jo.put("rc", "05");
//				jo.put("notif", "Proses Aktifasi Gagal");
//				context.put("trxjson", jo.toString());
//				return;
//			} catch (JSONException e1) {
//				LogSystem.error(getClass(), e1);
//				//e1.printStackTrace();
//			}
//			return;
//		}		
//	}
//}
