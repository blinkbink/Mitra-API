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
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailApi;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.UserdataManual;
import id.co.keriss.consolidate.util.LogSystem;

public class AktivasiMitra extends ActionSupport {
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="ACT"+sdfDate2.format(tgl).toString();
	String kelas="id.co.keriss.consolidate.action.km.AktivasiMitra";
	String trxType="PRC-ACT";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {

		DB db = getDB(context);
		HttpServletRequest request = context.getRequest();
		HttpServletResponse resp = context.getResponse();
		
		JSONObject jo = new JSONObject();
		String res = "05";
		String notif = "Aktivasi Gagal !";
		Mitra mitra = null;
		Transaction tx = null;
		
		try {
			String email = "";
			String preid = "";
			String method = "Aktifasi";
			String password = "";
			String username = "";

			FileItem fileTtd = null;
			FileItem fileSelfie = null;

			File fileTo;

			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			String strDate = sdfDate.format(date);

			UserManager mgr = new UserManager(db);

			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (!isMultipart) {
			    LogSystem.info(request, "Bukan multipart", kelas, refTrx, trxType);
				//System.out.println("Bukan Multipart");
			}

			else {
			    LogSystem.info(request, "Multipart", kelas, refTrx, trxType);
				//System.out.println("Multipart");
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
				List<FileItem> fileItems=null;
				//System.out.println("Check");
			    LogSystem.info(request, "Check",kelas, refTrx, trxType);
				try 
				{
					fileItems = upload.parseRequest(request);
				}
				catch(Exception e)
				{
				    LogSystem.error(getClass(), e,kelas, refTrx, trxType);
				}

				for (FileItem fileItem : fileItems) {
					if (fileItem.isFormField()) {

						if (fileItem.getFieldName().equals("preid")) {
							preid = fileItem.getString();
						    LogSystem.info(request, preid,kelas, refTrx, trxType);
						}
						
						if (fileItem.getFieldName().equals("username")) {
							username = fileItem.getString();
						    LogSystem.info(request, username, kelas, refTrx, trxType);
						}

						if (fileItem.getFieldName().equals("email")) {
							String s = fileItem.getString();
							email = s.toLowerCase();
						    LogSystem.info(request, email, kelas, refTrx, trxType);
						}

						if (fileItem.getFieldName().equals("password")) {
							password = fileItem.getString();
						    LogSystem.info(request, password, kelas, refTrx, trxType);
						}
						

					} else {
						if (fileItem.getFieldName().equals("fttd")) {
							if (fileItem.getSize() > 0) {
								fileTtd = fileItem;
								//System.out.println(fileTtd);
								LogSystem.info(request, "File ttd : " + fileTtd, kelas, refTrx, trxType);
							}
						}
					}
				}
			}

			if (method.equals("Aktifasi")) {
				
				Alamat alamat = new Alamat();
				
				try {
					tx = db.session().beginTransaction();
					Date time = new Date();
					
					PreRegistrationDao dd=new PreRegistrationDao(db);
					PreRegistration pr=dd.findById(Long.valueOf(preid));
					
					List<Userdata> userData = new UserdataDao(db).findByKtp(pr.getNo_identitas());
					User um = new UserManager(db).findByEmail(pr.getEmail());
					
					Login login = null;
					
					LogSystem.info(request, "Userdata : " + userData, kelas, refTrx, trxType);
					
					if(um != null)
					{
						jo.put("rc", "05");
						jo.put("notif", "User sudah melakukan aktifasi");
						context.put("trxjson", jo.toString());
						return;
					}
					Userdata userdata = new Userdata();
					if (userData.size()<1) {
						LoginDao logindao = new LoginDao(db);

						login = new Login();
						login.setUsername(username);
						login.setPassword(EEUtil.getHash(username,password));
						
						try {
						    LogSystem.info(request, "Save login", kelas, refTrx, trxType);
							db.session().save(login);
						}catch(Exception e)
						{
						    LogSystem.error(getClass(), e,kelas, refTrx, trxType);
							e.printStackTrace();

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

						String uploadTo = "/opt/data-DS/UploadFile/" + ud.getId() + "/original/";
						File directory = new File(uploadTo);

						if (!directory.exists()) {
							directory.mkdirs();
						}

						if (fileTtd != null) 
						{
							String rename = "TTD" + strDate + ".png";
							fileTo = new File(uploadTo + rename);

							if (fileTtd != null)
								fileTtd.write(fileTo);

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
							LogSystem.info(request, "Save ud", kelas, refTrx, trxType);
							db.session().save(ud);

						}catch(Exception e)
						{
							e.printStackTrace();
							jo.put("rc", "05");
							jo.put("notif", "Proses Aktifasi Gagal");
							context.put("trxjson", jo.toString());
							return;
						}
						
						userdata.setId(pr.getId());
						//System.out.println("PR ID : " + pr.getId());
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
					ee.setNick(pr.getEmail().toLowerCase());
					ee.setName(pr.getNama());
					ee.setStatus('3');
					ee.grant("ds");
					ee.setTime(time);
					ee.grant("login");
					ee.setUserdata(userdata);
					ee.setLogin(login);
					ee.setAuto_ttd(false);
					ee.logRevision("created", new UserManager(db).findById((long) 0));
				
					try {
						LogSystem.info(request, "Save ee", kelas, refTrx, trxType);
						db.session().save(ee);
					}catch(Exception e)
					{
						e.printStackTrace();
						jo.put("rc", "05");
						jo.put("notif", "Proses Aktifasi Gagal");
						return;
					}
					
					db.session().delete(pr);
					try {
						LogSystem.info(request, "Tryng commit", kelas, refTrx, trxType);
						tx.commit();
						
						jo.put("rc", "00");
						jo.put("notif", "Proses Aktifasi Berhasil");
						jo.put("username", username);

					}catch(Exception e)
					{
						e.printStackTrace();
						//context.getResponse().sendError(05, "Aktivasi Gagal");
						jo.put("rc", "05");
						jo.put("notif", "Proses Aktifasi Gagal");
						context.put("trxjson", jo.toString());
						return;
					}
					
					try {
						LogSystem.info(request, "Find mitra untuk notifikasi", kelas, refTrx, trxType);
						MitraDao mtdao = new MitraDao(db);
						mitra = mtdao.findById(pr.getMitra().getId());
						
						if(mitra.isNotifikasi()) 
						{
							LogSystem.info(request, "Sending email sukses aktifasi :  True", kelas, refTrx, trxType);
							SendMailApi sendEmail = new SendMailApi();
							
							sendEmail.sendSuccessActivation(pr.getEmail(), pr.getNama(), pr.getJk(), pr.getMitra().getId());
					    }
				
					} catch (Exception e) {
						e.printStackTrace();
						LogSystem.info(request, "Gagal kirim email", kelas, refTrx, trxType);

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
