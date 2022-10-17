package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.KeyV3Dao;
import id.co.keriss.consolidate.dao.SigningSessionDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.KeyV3;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

import api.email.SendMailPendaftaranUlang;


public class CheckUserSign extends ActionSupport implements DSAPI {
	User userRecv;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        boolean otp = false;
        boolean valid=false;
        String info="Dapat melanjutkan proses tandatangan";
		int count = 21;
		HttpServletRequest  request  = context.getRequest();
		String rc="00";
		int status=0;
		JSONObject result=new JSONObject();
		String sessionid="";
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		Boolean wna=false;
		String refTrx="";
		String path_app = this.getClass().getName();
		String CATEGORY = "SIGN";
		String email_req ="";
		String mitra_req ="";
		try{
			 String process=request.getRequestURI().split("/")[2];
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         
	         
	         refTrx = object.getString("refTrx");
	         
	         UserManager um=new UserManager(db);
        	 user=um.findByEmail(object.getString("usersign"));
        	 sessionid = object.getString("sessionid");
        	 
        	 SigningSessionDao signingSessionDB = new SigningSessionDao(db);
 	         SigningSession signingSession = new SigningSession();
 	         signingSession = signingSessionDB.findSessionId(sessionid);
 	         
 	         email_req = user.getNick();
 	         mitra_req = signingSession.getMitra().getName();
 	         
 	         Date datey = new Date();
 			 Date today = datey;
 			 Date expire = signingSession.getExpire_time();
 			 boolean used = signingSession.isUsed();
 			 if(today.after(expire) ) {
 				 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
 				 result.put("result", "408");
     			 result.put("info", "Tidak dapat melanjutkan tandatangan. Sesi habis.");
 				 context.put("trxjson", result.toString());
 				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
 				 return;
 			 }
 			 if (used == true) {
 	        	 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
 				 result.put("result", "401");
     			 result.put("info", "Tidak dapat melanjutkan tandatangan. Sesi sudah digunakan.");
 				 context.put("trxjson", result.toString());
 				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
 				 return;
 			 }
 	         
        	 
        	 
        	 boolean meterai = false;
        	 
        	 if(object.getInt("meterai") > 0)
        	 {
        		 meterai = true;
        	 }
        	 
        	 
        	 if(user!=null) 
        	 { 
        		 if(user.getUserdata().getWn() == '2')
        		 {
        			 wna = true;
        		 }
        		 
        		 String idDoc=object.getString("idDoc");
        		 List<DocumentAccess> docac = dao.findDocEEuserSignApp(Long.valueOf(idDoc), user.getNick());
        		 if(user.getUserdata().getLevel().equals("C2"))
        		 {
        			 result.put("result", "08");
        			 result.put("info", "Tidak dapat melanjutkan tandatangan. Mohon menghubungi CS Digisign untuk informasi lebih lanjut.");
    				 context.put("trxjson", result.toString());
    				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
    				 return;
        		 }
        		 // Cek sertifikat user ke KMS
    			 KmsService kms = new KmsService(request, "KMS Service/"+refTrx, CATEGORY, start, mitra_req, email_req);
    			 JSONObject cert = new JSONObject();

				 LogSystem.info(request, "Cek mitra : " + user.getUserdata().getMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 if(user.getMitra() != null)
				 {
					 LogSystem.info(request, "Mitra tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
				 }
				 else 
				 {
					 LogSystem.info(request, "Mitra null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), "");
				 }
    			 LogSystem.info(request, "Hasil cek sertifikat : " + cert, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
    			 
    			 if(cert.getString("result").equals("00"))
    	         {
    	        	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    	             String raw = cert.getString("expired-time");
    	             Date expired = sdf.parse(raw);
    	             Date now= new Date();
    	             Long day = TimeUnit.DAYS.convert(expired.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
    	             LogSystem.info(request, "Jarak ke waktu expired : " + day, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
   
		             if (cert.has("needRegenerate"))
		             {
		            	 if (!cert.getBoolean("needRegenerate")) 
			             {
			            	 if(day < 30)
				        	 {
			            		 result.put("result", "03");
			        			 result.put("info", "Sertifikat akan expired, butuh pembaruan.");
			        			 result.put("alertRenewalInfoBox", "Saya menyetujui untuk memperpanjang sertifikat elektronik");
			        			 result.put("alertRenewalInfo", "Masa aktif sertifikat elektronik anda akan segera habis. Silahkan konfirmasi untuk memperpanjang sertifikat elektronik anda dengan klik centang dibawah ini");
			        			 context.put("trxjson", result.toString());
			    				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    				 return; 
				        	 }
			             }
			             else
			             {
			            	 result.put("result", "04");
		        			 result.put("info", "Butuh pembaruan sertifikat baru.");
		        			 result.put("alertRenewalInfoBox", "Saya menyetujui untuk dilakukan Pencabutan Sertifikat Elektronik lama dan menyetujui Penerbitan Sertifikat Elektronik baru");
		        			 result.put("alertRenewalInfo", "Diinformasikan ke pengguna Sertifikat Elektronik Digisign, bahwa pada tanggal " + DSAPI.MASS_REVOKE + ", Digisign akan melakukan Pencabutan Sertifikat Elektronik lama dan selanjutnya akan membutuhkan persetujuan untuk Penerbitan Sertifikat Elektronik baru Anda. Tidak ada dampak terhadap semua Sertifikat maupun tanda tangan yang telah dilakukan. Seluruh dokumen yang sudah ditandatangani akan tetap valid. Silahkan konfirmasi jika Anda setuju untuk melakukan proses pencabutan Sertifikat lama dan penerbitan Sertifikat baru sekarang");
		        			 context.put("trxjson", result.toString());
		    				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    				 return; 
			             }
		             }
		             else
		             {
		            	 if(day < 30)
			        	 {
		            		 result.put("result", "03");
		        			 result.put("info", "Sertifikat akan expired, butuh pembaruan.");
		        			 result.put("alertRenewalInfoBox", "Saya menyetujui untuk memperpanjang sertifikat elektronik");
		        			 result.put("alertRenewalInfo", "Masa aktif sertifikat elektronik anda akan segera habis. Silahkan konfirmasi untuk memperpanjang sertifikat elektronik anda dengan klik centang dibawah ini");
		        			 context.put("trxjson", result.toString());
		    				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    				 return; 
			        	 }
		             }
    	         }
    			
    			 
    			 if(cert.getString("result").length() > 3)
    			 {
    				 result.put("result", "09");
    				 result.put("info", "Tidak dapat melanjutkan tandatangan. Silahkan menghubungi CS Digisign.");
	        		 context.put("trxjson", result.toString());
	        		 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	     			 return;
    			 }
    			 
    			 if(cert.getString("result").equals("06") || cert.getString("result").equals("07") || cert.getString("result").equals("05") || cert.getString("result").equals("G1"))
    			 {
    				 result.put("result", cert.getString("result"));
    				 
    				 if(cert.getString("result").equals("07"))
    				 {
    					 if(!wna)
    					 {
    						 result.put("info", "Masa aktif sertifikat elektronik Anda sudah dicabut. Kami sudah mengirimkan informasi pendaftaran ulang melalui email. Silakan cek email Anda untuk melanjutkan pendaftaran ulang Akun Digisign Anda.");
    					 }
    					 else
    					 {
    						 result.put("info", "Masa aktif sertifikat elektronik Anda sudah dicabut. Mohon menghubungi CS Digisign terkait informasi Pendaftaran Ulang lebih lanjut.");
    					 }
    				 }
    				 
    				 if(cert.getString("result").equals("06"))
    				 {
    					 if(!wna)
    					 {
    						 result.put("info", "Masa aktif sertifikat elektronik Anda sudah habis. Kami sudah mengirimkan informasi pendaftaran ulang melalui email. Silakan cek email Anda untuk melanjutkan pendaftaran ulang Akun Digisign Anda.");
    					 }
    					 else
    					 {
    						 result.put("info", "Masa aktif sertifikat elektronik Anda sudah habis. Mohon menghubungi CS Digisign terkait Informasi Pendaftaran Ulang lebih lanjut.");
    					 }
    				 }
    				 
    				 if(cert.getString("result").equals("05"))
    				 {
    					 result.put("infoNotif", "Anda belum memiliki sertifikat elektronik. Sertifikat elektronik akan dibuat sebelum tandatangan diproses");
    					 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik dan Menyetujui Menandatangani Dokumen ini");
    					 
    					 if(meterai)
    					 {
    						 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik dan Menandatangani Dokumen yang dibubuhi Meterai Elektronik");
    					 }
    				 }
    				 
    				 if(cert.getString("result").equals("G1"))
    				 {
    					 result.put("infoNotif", "Seperti yang telah diinformasikan sebelumnya Pencabutan Sertifikat Massal Digisign telah dilakukan pada tanggal "+DSAPI.MASS_REVOKE+". Maka status Sertifikat Elektronik Anda telah dicabut secara otomatis pada tanggal tersebut. Sertifikat Elektronik baru Anda akan diterbitkan sebelum tandatangan diproses");
    					 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik baru dan Menyetujui Menandatangani Dokumen ini");
    					 
    					 if(meterai)
    					 {
    						 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik baru dan Menyetujui Menandatangani Dokumen yang dibubuhi Meterai Elektronik");
    					 }
    				 }
    				 
    				 context.put("trxjson", result.toString());
    				 if(!cert.getString("result").equals("05") && !cert.getString("result").equals("G1"))
    				 {
    					 if(!wna)
    					 {
		    				 try
			        		 {
		    					 LogSystem.info(request, "Mengirim email pendaftaran ulang", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        			 SendMailPendaftaranUlang mail = new SendMailPendaftaranUlang(request, refTrx);
			        			 mail.kirim(user.getUserdata().getNama(), Character.toString(user.getUserdata().getJk()), user.getNick(), String.valueOf(user.getId()), String.valueOf(docac.get(0).getDocument().getEeuser().getMitra().getId()), mitra_req, CATEGORY, start);
			        		 }
			        		 catch(Exception e)
			        		 {
			        			 e.printStackTrace();
			        			 LogSystem.info(request, "Gagal mengirim email pendaftaran ulang", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        			 LogSystem.info(request, e.getMessage(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 }
    					 }
    					 else
    					 {
    						 LogSystem.info(request, "PENGGUNA WNA, TIDAK KIRIM EMAIL PENDAFTARAN ULANG", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
    					 }
    				 }
    				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	     			 return;
    			 }
        	 }
		}catch (Exception e) {
			rc="09";
            LogSystem.error(getClass(), e);
            e.printStackTrace();
		}
		
		try {
			result.put("result", rc);
			result.put("status", status);
			result.put("notif", info);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogSystem.error(getClass(), e);
		}
        context.getResponse().setContentType("application/json");
        try {
			LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "RESPON", (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        System.out.println("SEND :"+result.toString());
		context.put("trxjson", result.toString());
//		LogSystem.response(request, result,"");
		 HttpSession session          = context.getSession();
	        
        session.removeAttribute (USER);

	}
	
//	private boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db, HttpServletRequest req, String refTrx, String version) {
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//		Date date = new Date();
//		String strDate = sdfDate.format(date);
//		String tanggal = ftanggal.format(date);
//		String signdoc = "DSSG" + strDate + ".pdf";
//		String path = doc.getDocument().getPath();
//		String original = doc.getDocument().getSigndoc();
//		boolean usingopt = path.startsWith("/file2/");
//		if (!usingopt) {
//			String opath = path;
//			path="";
//			String apath[]=opath.split("/");
//			apath[1]="file2";
//			for (int i = 0; i < apath.length; i++) {
//				path+=apath[i]+"/";
//			}
//		}
//		try {
//			DocumentSigner dSign=new DocumentSigner();
//			
//			LogSystem.info(req, "Using key version :" + version, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		    
//			JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc, version);
//			if(resSign!=null && resSign.getString("result").equals("00")) {
//				Date dateSign= new Date(resSign.getLong("date"));
//				doc.setFlag(true);
//				doc.setDate_sign(date);
//				doc.setInvoice(inv);
//				new DocumentsAccessDao(db).update(doc);
//				
//				Documents documents=doc.getDocument();
//				documents.setSigndoc(signdoc);
//				documents.setPath(path);
//	    		new DocumentsDao(db).update(documents);
//	
//				
//				return true;
//			}
//			//						
////			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			LogSystem.error(getClass(), e);
//		}
//		return false;
//
//
//	}
//	
	
	User checkUser(String pwd, String email, DB db) {
		
		User user=new UserManager(db).findByUsername(email);
		//User user=new UserManager(db).findByUsernamePassword(email, pwd);
		
//		if(user!=null) {			  
//			  if(!user.getPassword().equals(pwd)) {
//				  user=null;  
//			  }
//		}
		return user;
	}
	
}
