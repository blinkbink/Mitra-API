package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.BillingSystem;
import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.CallbackPendingDao;
import id.co.keriss.consolidate.dao.CancelDocumentDao;
import id.co.keriss.consolidate.dao.EeuserMitraDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.ee.Canceled_document;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;

import id.co.keriss.consolidate.util.SystemUtil;
import id.sni.digisign.filetransfer.Samba;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.dao.SigningSessionDao;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

import api.callback.callback;
import api.email.SendNotifSignDoc;
import api.log.ActivityLog;



public class SignDocMitraV_session extends ActionSupport implements DSAPI {
	User userRecv;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		Random rand = new Random();
		int number = rand.nextInt(99999999);
		String code = String.format("%06d", number);
		String refTrx="SPAGE"+sdfDate2.format(tgl).toString()+"/C"+code;
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        String namaUser = "";
        boolean otp = false;
        boolean valid=false;
        String info="";
		int count = 21;
		HttpServletRequest request  = context.getRequest();
		String rc="05";
		int status=0;
		String status_doc = null;
		String email_user =null;
		String id_dokumen =null;
		JSONObject result=new JSONObject();
		long documentIDa= 0;
    	long mitraid = 0;
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		Integer docpros = null;
		Long docId = null;
		
		String path_app = this.getClass().getName();
		String CATEGORY = "SIGN";
		String email_req ="";
		String mitra_req ="";
		try{
			
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

//	         System.out.println("RECEIVE :"+sb.toString());
//	         log.info("RECEIVE :"+sb.toString());
	         LogSystem.info(request, sb.toString(), refTrx , path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "REQUEST", (System.currentTimeMillis() - start) / 1000f + "s");
	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         /*
	         if(!auth.processAuth(object.getString("auth"))) {
	        	 sendRedirect (context, context.getRequest().getContextPath() 
	                     + "/stop.html"
	                 );
	         	return;
	         }
	         user=auth.getUser();
	         DocumentsAccessDao dao=new DocumentsAccessDao(db);
	         String idDoc=object.getString("idDoc");
	         if(!idDoc.equals(auth.getIdDoc())) {
	        	 sendRedirect (context, context.getRequest().getContextPath() 
	                     + "/stop.html"
	                 );
	         	return;
	         }
	         */
	         refTrx=object.getString("refTrx");
	         //Cek sesi
	         SigningSessionDao signingSessionDB = new SigningSessionDao(db);
	         SigningSession signingSession = new SigningSession();
	         signingSession = signingSessionDB.findSessionId(object.getString("id_session"));
	         
	         UserManager um=new UserManager(db);
        	 user=um.findByEmail(object.getString("usersign"));
        	 
        	 mitra_req = signingSession.getMitra().getName();
        	 email_req = user.getNick();
	         
	         Date datey = new Date();
			 Date today = datey;
			 Date expire = signingSession.getExpire_time();
			 boolean used = signingSession.isUsed();
			 if(today.after(expire) ) {
				 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 result.put("result", "408");
    			 result.put("notif", "Tidak dapat melanjutkan tandatangan. Sesi habis.");
				 context.put("trxjson", result.toString());
				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 return;
			 }
			 if (used == true) {
	        	 LogSystem.error(request, "Session has already expired", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 result.put("result", "401");
    			 result.put("notif", "Tidak dapat melanjutkan tandatangan. Sesi sudah digunakan.");
				 context.put("trxjson", result.toString());
				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 return;
			 }
	         
	         
        	 
        	 if(user!=null) {
		         String idDoc=object.getString("idDoc");
	        	 List<DocumentAccess> docac = dao.findDocEEuserSignApp(Long.valueOf(idDoc), user.getNick());
	        	 if(docac.size()==0) {
	        		 LogSystem.info(request, "size < 0", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 docac = dao.findDocEEuserSigned(Long.valueOf(idDoc), user.getNick());
		        	 result.put("result", "00");
	     			 result.put("status", status);
	     			 result.put("notif", "Dokumen Sudah Ditandatangan");
	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
//					 boolean usingopt = docac.get(0).getDocument().getPath().startsWith("/opt");
//						LogSystem.info(request, "useopt = " + usingopt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//						String optget = "";
//						if (usingopt) {
//							optget = AESEncryption.encryptDoc("optfile");
//						}
						String usingopt[] = docac.get(0).getDocument().getPath().split("/");
						LogSystem.info(request, "useopt = " + usingopt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						String optget = "";
						optget = AESEncryption.encryptDoc(usingopt[1]);
					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8")+"&tp="+URLEncoder.encode(optget, "UTF-8")+"&ss="+URLEncoder.encode(SystemUtil.getExp(namafile), "UTF-8"));
					 context.put("trxjson", result.toString());
					 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	     			 return;

	        	 }
	        	 else
	        	 {
	        		 //check kalo dokumen lagi diproses
	        		 DocumentsDao docProc = new DocumentsDao(db);
	        		 docId = object.getLong("idDoc");

	        		 docpros = docProc.findByidDocLock(docId);
	        		 
	        		 LogSystem.info(request, "Dokumen dalam proses ? " + docpros, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

//	        		 if(docpros.getLast_proses() != null)
//	        		 {
//	        			 Date date = new Date();
//		                 Date waktubuat = docpros.getLast_proses();
//		                 long diff = date.getTime() - waktubuat.getTime();
//		                 long min = (diff / 1000) / 60;
//	        			 if(docpros.getProses() && min < 3)
//	        			 {
//	        				 LogSystem.error(request, "Dokumen sedang diproses oleh Pengguna lain", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		        			 result.put("result", "12");
//		        			 result.put("notif", "Dokumen sedang diproses oleh Pengguna lain. Mohon coba kembali setelah 3 menit.");
//		        			 context.put("trxjson", result.toString());
//		        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		        			 
//		        			 return;
//	        			 }
//	        			 else
//	        			 {
//	        				 try {
//	        					 docpros.setProses(true);
//	        					 docpros.setLast_proses(new Date());
//	        					 LogSystem.info(request, "Update dokumen proses", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	        					 new DocumentsDao(db).update(docpros);
//	        					 LogSystem.info(request, "Berhasil update dokumen proses " + docpros.getProses() + " Last proses " + docpros.getLast_proses() , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	        				 }catch(Exception e)
//	        				 {
//	        					 e.printStackTrace();
//	        					 LogSystem.error(request, "Gagal proses DB", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//			        			 result.put("result", "91");
//			        			 result.put("notif", "System timeout. silahkan coba kembali.");
//			        			 context.put("trxjson", result.toString());
//			        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//			        			 
//			        			 return;
//	        				 }
//	        			 }
//	        		 }
//	        		 else
//	        		 {
//	        			 if(docpros.getProses())
//	        			 {
//	        				 LogSystem.error(request, "Dokumen sedang diproses oleh Pengguna lain", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		        			 result.put("result", "12");
//		        			 result.put("notif", "Dokumen sedang diproses oleh Pengguna lain. Mohon coba kembali setelah 3 menit.");
//		        			 context.put("trxjson", result.toString());
//		        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		        			 
//		        			 return;
//	        			 }
//	        			 else
//	        			 {
//	        				 try {
//	        					 docpros.setProses(true);
//	        					 docpros.setLast_proses(new Date());
//	        					 LogSystem.info(request, "Update dokumen proses", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	        					 new DocumentsDao(db).update(docpros);
//	        					 LogSystem.info(request, "Berhasil update dokumen proses " + docpros.getProses() + " Last proses " + docpros.getLast_proses() , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	        				 }catch(Exception e)
//	        				 {
//	        					 e.printStackTrace();
//	        					 LogSystem.error(request, "Gagal proses DB", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//			        			 result.put("result", "91");
//			        			 result.put("notif", "System timeout. silahkan coba kembali.");
//			        			 context.put("trxjson", result.toString());
//			        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//			        			 
//			        			 return;
//	        				 }
//	        			 }
//	        		 }
	        		 
//	        		 JedisPool pool = new JedisPool(new JedisPoolConfig(), DSAPI.REDIS_HOST, DSAPI.REDIS_PORT, DSAPI.REDIS_TIMEOUT, DSAPI.REDIS_PASSWORD);
//	        			
//	        			try (Jedis jedis = pool.getResource()) {
//	        					String data = jedis.get(docId.toString());
//	        					LogSystem.info(request, "Dokumen dalam proses (redis)" + data, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	        					
//	        					if(data != null)
//	        					{
//	        						//dokumen lagi ada yang proses
//	        						LogSystem.error(request, "Dokumen sedang diproses oleh Pengguna lain", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		       	        			result.put("result", "12");
//		       	        			result.put("notif", "Dokumen sedang diproses oleh Pengguna lain. Mohon coba kembali setelah 3 menit.");
//		       	        			context.put("trxjson", result.toString());
//		       	        			LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//		       	        			 
//	        						return;
//	        					}
//	        					else
//	        					{
//	        						//dokumen bisa diproses, set ke redis, jedis.setex("docid", "waktu key docid dihapus otomatis dari redis", "true");
//	        						String redis = jedis.setex(docId.toString(), 180, "true");
//	        						
//	        						LogSystem.info(request, "Response set redis " + redis, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	        					}
//	        				}
//	        			catch(Exception e)
//	        			{
//	        				e.printStackTrace();
//	        			}
//	        			finally
//	        			{
//	        				pool.close();
//	        			}
	        		 
	        		 if(docpros != 200)
        			 {
        				 LogSystem.error(request, "Dokumen sedang diproses oleh Pengguna lain", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        			 result.put("result", "12");
	        			 result.put("notif", "Dokumen sedang diproses oleh Pengguna lain. Mohon coba kembali setelah 3 menit.");
	        			 context.put("trxjson", result.toString());
	        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        			 
	        			 return;
        			 }
	        	 }
        	 }
	         
        	
	         ConfirmCode cc = null;
	         ConfirmCodeDao ccd = new ConfirmCodeDao(db);
	         //UserManager um=new UserManager(db);
	         EeuserMitraDao emdao=new EeuserMitraDao(db);
	         String reason = object.getString("reason");
    		 LogSystem.info(request, "Alasannya : "+reason, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         if(object.getString("userpwd")!=null && object.getString("usersign")!=null && object.getString("otp")!=null) {
	        	 //user=checkUser(object.getString("userpwd"), object.getString("usersign"), db);
	        	 user=um.findByUsernamePassword(object.getString("usersign"), object.getString("userpwd"));
	        	 
	        	 
	        	 //EeuserMitra em=emdao.findUserPwdMitra(object.getString("usersign"), object.getString("userpwd"), Long.valueOf(object.getString("mitra")));
	        	 //if(em!=null)user=em.getEeuser();
	        	 if(user!=null) {
	        		 //System.out.println("User valid");
	        		 LogSystem.info(request, "User valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 //check otp
	        		 cc = ccd.getLastOTP(user.getId());
	        		 if(cc!=null) {
	        			 if(cc.getCode().equals(object.getString("otp"))) {
	        				 otp=true;
		        			 valid=true;
	        			 }
	        			 else {
	        				 LogSystem.error(request, "OTP tidak valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        				 //System.out.println("Otp tidak valid");
		        			 result.put("result", "12");
		        			 result.put("notif", "kode OTP tidak valid");
		        			 context.put("trxjson", result.toString());
		        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        			 
		        			 try 
		        			 {
		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
						     }catch(Exception e)
						     {
						    	 e.printStackTrace();
						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						     }
		        			 return;
	        			 }
	        		 }
	        		 else {
	        			 LogSystem.error(request, "OTP tidak valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        			 //System.out.println("Otp tidak valid");
	        			 result.put("result", "12");
	        			 result.put("notif", "kode OTP tidak valid");
	        			 context.put("trxjson", result.toString());
	        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        			 
	        			 try 
	        			 {
	        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
	        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
					     }catch(Exception e)
					     {
					    	 e.printStackTrace();
					    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					     }
	        			 
	        			 return;
	        			 
	        		 }
	        		 valid=true;
	        	 }
	        	 else {
	        		 LogSystem.error(request, "Password salah", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 //System.out.println("user tidak valid");
	        		 result.put("result", "12");
        			 result.put("notif", "Password salah");
        			 context.put("trxjson", result.toString());
        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
        			 
        			 try 
        			 {
        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
				     }catch(Exception e)
				     {
				    	 e.printStackTrace();
				    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				     }
        			 
        			 return;
	        	 }
	         }
	         String version = null; 
	         if(valid==true) {
	        	 
	        	 List<DocumentAccess> docAccMeteraiList = dao.findDocMeteraiSignApp(Long.valueOf(object.getString("idDoc")));
	        	 
	        	 if(docAccMeteraiList.size() > 0)
	        	 {
		        	 DocumentAccess docAccMeterai = docAccMeteraiList.get(0);
		        	 
		        	 if(docAccMeterai != null)
		        	 {
		        		 if(!docAccMeterai.isFlag())
		        		 {
		        			LogSystem.info(request, "Terdapat meterai, lanjut proses meterai", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	//	        			DocumentAccess dac = null;
		        			
		        			DocumentAccess dac = dao.findByDocMeterai(object.getString("idDoc")).get(0);
		        			
		        			Documents id_doc = dac.getDocument();
	//	        			
	//						dac=dao.findbyId(idAcc.getId());
							 
							Userdata udata=new Userdata();
							boolean reg=false;
							
							String OriginPath = id_doc.getSigndoc();
							String path = id_doc.getPath();
							Date date = new Date();
							SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
							String strDate = sdfDate.format(date);
							String signdoc = "APIA" + strDate+id_doc.getId() +".pdf";
							String oldpathSignDoc = path+OriginPath;
							
							Samba samba=new Samba(refTrx, request, mitra_req,  email_req,  CATEGORY,  start);
							byte[] encoded = Base64.encode(samba.openfile(path+OriginPath));
							String base64Document = new String(encoded, StandardCharsets.US_ASCII);
							
							float llx = Float.parseFloat(dac.getLx());
							float lly = Float.parseFloat(dac.getLy());
							float urx = Float.parseFloat(dac.getRx());
							float ury = Float.parseFloat(dac.getRy());
							
							JSONObject resSign = null;
							DocumentSigner ds = new DocumentSigner();
							try {
								resSign = ds.kirimMeterai(request, refTrx, id_doc.getId(), id_doc.getFile_name(), id_doc.getEeuser().getMitra().getId(), llx, lly, urx, ury, dac.getPage(), base64Document, id_doc.getEeuser().getMitra().getProvinsi().getName(), mitra_req, email_req, CATEGORY, start);
								
							}catch(Exception e)
							{
								e.printStackTrace();
								// TODO: handle exception
								LogSystem.error(request, "Error save file meterai"+e.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						  	  	result.put("result", "91");
								result.put("notif", "System timeout. silahkan coba kembali.");
							
								context.put("trxjson", result.toString());
								return;
							}
							
							if(resSign == null)
							{
								LogSystem.error(request, "Response meterai null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						  	  	result.put("result", "91");
								result.put("notif", "System timeout. silahkan coba kembali.");
								
								context.put("trxjson", result.toString());
								return;
							}
							
							LogSystem.info(request, "Response meterai : "+resSign.getString("result") + " information " + resSign.getString("information"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							
							if(resSign.getString("information").equalsIgnoreCase("saldo tidak mencukupi"))
							{
								result.put("result", "61");
								result.put("notif", "Saldo meterai tidak mencukupi");
								
								context.put("trxjson", result.toString());
								return;
							}
							
							boolean resp=false;
							
							if(resSign.getString("result").equals("00"))
							{
							    try {
							    	byte[] finalDoc = Base64.decode(resSign.getString("final_document"));
								    resp=samba.write(finalDoc, path+signdoc);
								    LogSystem.info(request, "hasil save File : "+resp, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								    if(resp==false) {
								  	  	LogSystem.error(request, "error samba", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								  	  	result.put("result", "91");
										result.put("notif", "System timeout. silahkan coba kembali.");
										
										context.put("trxjson", result.toString());
										return;
								    }
								    finalDoc = null;
								} catch (Exception e) {
									e.printStackTrace();
									// TODO: handle exception
									LogSystem.error(request, "Error save file meterai"+e.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							  	  	result.put("result", "91");
									result.put("notif", "System timeout. silahkan coba kembali.");
									
									context.put("trxjson", result.toString());
									return;
								}
							    
								try {
									id_doc.setSigndoc(signdoc);
									dac.setDate_sign(new Date());
									dac.setFlag(true);
									
									Transaction t = null;
							    	if(!db.session().getTransaction().isActive())
							    	{
							    		t=db.session().beginTransaction();
							    	}
							    	else
							    	{
							    		t=db.session().getTransaction();
							    	}
							    	
							    	db.session().update(id_doc);
							    	db.session().update(dac);
							    	
							    	t.commit();
									
									LogSystem.info(request, "Proses meterai berhasil ", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								} catch (Exception e) {
									// TODO: handle exception
									LogSystem.info(request, "DB Timeout", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							  	  	result.put("result", "91");
									result.put("notif", "System timeout. silahkan coba kembali.");
								
									context.put("trxjson", result.toString());
									return;
								}
							}
							else
							{
								LogSystem.info(request, "Response Meterai tidak berhasil ", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						  	  	result.put("result", "91");
								result.put("notif", "System timeout. silahkan coba kembali.");
								
								context.put("trxjson", result.toString());
								return;
							}
		        		 }
		        		 else
		        		 {
		        			 LogSystem.info(request, "Meterai pada dokumen ini sudah diproses", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        		 }
		        	 }
	        	 }
	        	 

		         String approval=object.getString("statustypeapproval");
		         LogSystem.info(request, "Is it Approval Proccess? : "+approval, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		         boolean spros = false;
		         String idDoc=object.getString("idDoc");
		         List<DocumentAccess>dList=null;
		         if (approval.equals("true")) {
		        	 dList=dao.findDocEEuserApproval(Long.valueOf(idDoc), user.getNick());
		         }else {
		        	 dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
		        	 spros = true;
		         }
		         List<DocumentAccess>dList_check=dao.findDocEEuserSign_check(Long.valueOf(idDoc));
		         LogSystem.info(request, "Check Jumlah TTD = "+dList_check.size(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		       		         
		         
		         if(object.has("cert"))
		         {		    
		        	 if(object.has("checkme"))
			         {
			        	 LogSystem.info(request, "Tombol Menyetujui Penandatangan Dokumen ini ? "+object.getBoolean("checkme"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	 if (!object.getBoolean("checkme"))
			        	 {
		        			 result.put("result", "12");
	            			 result.put("notif", "gagal proses tandatangan");
	            			 context.put("trxjson", result.toString());
	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	            			 
	            			 try 
		        			 {
		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
						     }catch(Exception e)
						     {
						    	 e.printStackTrace();
						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						     }
	            			 
	            			 return;
			        	 }
			         }
		        	 
//		        	 LogSystem.info(request, "Check Jumlah TTD = "+dList_check.size(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        	 if(!object.getString("cert").equals(""))
		        	 {
			        	 LogSystem.info(request, "Proses generate sertifikat", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	 LogSystem.info(request, "Hasil cek sertifikat dari depan = " + object.getString("cert"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	 if(!object.getString("cert").equals("00"))
			        	 {
			        		 LogSystem.info(request, "Hasil dari depan bukan 00, cek lagi sertifikatnya", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 KmsService kms = new KmsService(request, "KMS Service/"+refTrx, CATEGORY, start, mitra_req, email_req);
			        		 JSONObject cert = new JSONObject();
			        		 String mitra = null;

			        		 LogSystem.info(request, "USERRR : " + user.getNick(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 LogSystem.info(request, "Cek mitra : " + user.getMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    				 if(user.getMitra() != null)
		    				 {
		    					 LogSystem.info(request, "Mitra tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
		    					 mitra = user.getMitra().getId().toString();
		    				 }
		    				 else 
		    				 {
		    					 LogSystem.info(request, "Mitra null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), "");
		    					 mitra = "";
		    				 }
		    				 
		    				 if(!cert.has("result"))
		    				 {									
		    					 result.put("result", "12");
    	            			 result.put("notif", "System timeout. silahkan coba kembali.");
    	            			 context.put("trxjson", result.toString());
    	            			 return; 
		    				 }
		    				 
		    				 if(cert.getString("result").length() >3 || cert.getString("result").equals(""))
		    				 {
		    					 result.put("result", "12");
    	            			 result.put("notif", "gagal proses tandatangan");
    	            			 context.put("trxjson", result.toString());
    	            			 return; 
		    				 }
		    				 
		    				 if (cert.has("keyVersion"))
		    				 {
		    					 if(cert.getString("keyVersion").equals("1.0") || cert.getString("keyVersion").equals("2.0"))
		    					 {
		    						 version = "v1";
		    					 }
		    					 
		    					 if(cert.getString("keyVersion").equals("3.0"))
		    					 {
		    						 version = "v3";
		    					 }
		    				 }
		    				 else
		    				 {
		    					 version = "v1";
		    				 }

		    				 Boolean renewal = object.getBoolean("renewal");
			        		 LogSystem.info(request, "Response cek sertifikat = " + cert.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 if(object.getString("cert").equals("04"))
			        		 {
			        			 LogSystem.info(request, "Tombol pencabutan sertifikat elektronik lama dan penerbitan sertifikat elektronik baru ? " + renewal, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 }
			        		 else
			        		 {
			        			 LogSystem.info(request, "Tombol Renewal ? " + renewal, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 }
			        		 
			        		 LogSystem.info(request, "Tombol Renewal ? " + renewal, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 
			        		 if (cert.getString("result").equals("G1") || cert.getString("result").equals("05") || (object.getString("cert").equals("03") && renewal) || (object.getString("cert").equals("04") && renewal))
			        		 {
			        			 JSONObject genCert= new JSONObject();
			        			 try {
			        				 LogSystem.info(request, "Proses sertifikat", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        				 if(!renewal)
			        				 {
			        					 if (object.has("genCert"))
				    		        	 {
			        						 String genCertInfo= "Tombol Saya Menyetujui Penerbitan Sertifikat Elektronik dan Menyetujui Penandatangan Dokumen ini ?";
			        						 if (cert.getString("result").equals("G1"))
			        						 {
			        							 genCertInfo= "Tombol Saya Menyetujui Penerbitan Sertifikat Elektronik baru dan Menyetujui Penandatangan Dokumen ini ?";
//			        							 renewal = true;
			        						 }
				    		        		 LogSystem.info(request, genCertInfo + " "+object.getBoolean("genCert"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    		        		 if (!object.getBoolean("genCert"))
				    		        		 {
				    		        			 result.put("result", "12");
				    	            			 result.put("notif", "gagal proses tandatangan");
				    	            			 context.put("trxjson", result.toString());
				    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    	            			 
				    	            			 try 
				    		        			 {
				    		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
				    		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
				    						     }catch(Exception e)
				    						     {
				    						    	 e.printStackTrace();
				    						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    						     }
				    	            			 
				    	            			 return;
				    		        		 }
				    		        	 }
				        				 else
				        				 {
				        					 LogSystem.info(request, "Proses generate sertifikat, tapi tidak ada tombol setuju membuat sertifikat", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				        					 result.put("result", "12");
			    	            			 result.put("notif", "gagal proses tandatangan");
			    	            			 context.put("trxjson", result.toString());
			    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    	            			 
			    	            			 try 
			    		        			 {
			    		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
			    		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
			    						     }catch(Exception e)
			    						     {
			    						    	 e.printStackTrace();
			    						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    						     }
			    	            			 
			    	            			 return;
				        				 }
				    		        	 
				    		        	 if (object.has("termCondition"))
				    		        	 {
				    		        		 LogSystem.info(request, "Tombol Saya Telah Membaca dan Menyetujui Kebijakan Privasi, Beserta Perjanjian Kepemilikan Sertifikat Elektronik Digisign yang Berlaku ? "+object.getBoolean("termCondition"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    		        		 if (!object.getBoolean("termCondition"))
				    		        		 {
				    		        			 result.put("result", "12");
				    	            			 result.put("notif", "gagal proses tandatangan");
				    	            			 context.put("trxjson", result.toString());
				    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    	            			 
				    	            			 try 
				    		        			 {
				    		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
				    		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
				    						     }catch(Exception e)
				    						     {
				    						    	 e.printStackTrace();
				    						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    						     }
				    	            			 
				    	            			 return;
				    		        		 }
				    		        	 }
				    		        	 else
				        				 {
				        					 LogSystem.info(request, "Proses generate sertifikat, tapi tidak ada tombol menyetujui syarat kebijakan privasi", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				        					 result.put("result", "12");
			    	            			 result.put("notif", "gagal proses tandatangan");
			    	            			 context.put("trxjson", result.toString());
			    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    	            			 
			    	            			 try 
			    		        			 {
			    		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
			    		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
			    						     }catch(Exception e)
			    						     {
			    						    	 e.printStackTrace();
			    						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    						     }
			    	            			 
			    	            			 return;
				        				 }
				    		        	 
				    		        	 genCert = kms.createSertifikat(user.getId(), renewal, user.getUserdata().getLevel());
				    		        	 
				    		        	 if (genCert.getString("result").equals("00"))
					        			 {
					        				 version = "v3";
        				 
					        			 }
					        			 else if (genCert.getString("result").equals("06") || (genCert.getString("result").equals("05") && genCert.getString("info").equals("certificate is already exist")))
					        			 {
					        				 version = "v3";
					        				 LogSystem.info(request, "Sertifikat sudah ada", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					        			 }
					        			 else
					        			 {
					        				 LogSystem.info(request, "Gagal generate sertifikat " + genCert, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					    	        		 result.put("result", "12");
					            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
					            			 context.put("trxjson", result.toString());
					            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					            			 
					            			 try 
						        			 {
						        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
						        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
										     }catch(Exception e)
										     {
										    	 e.printStackTrace();
										    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										     }
					            			 
					            			 return;
					        			 }
			        				 }
			        				 
			        				 if(cert.getString("result").equals("00"))
			        				 {
			        					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			            	             String raw = cert.getString("expired-time");
			            	             Date expired = sdf.parse(raw);
			            	             Date now= new Date();
			            	             Long day = TimeUnit.DAYS.convert(expired.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
			            	             LogSystem.info(request, "Jarak ke waktu expired : " + day, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			            	             
			        					 if(cert.has("needRegenerate"))
			        					 {
			        						 if(!cert.getBoolean("needRegenerate") && day > 30 && cert.getString("keyVersion").equals("3.0"))
			        						 {
			        							 LogSystem.info(request, "result 00"+cert.getBoolean("needRegenerate")+" " + day +" " + cert.getString("keyVersion") + ", tidak perlu renewal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        							 renewal = false;
			        							 LogSystem.info(request, "Buat sertifikat ? " + renewal, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        						 }
			        					 }
			        					 else
			        					 {
				        					 if(day > 30 && cert.getString("keyVersion").equals("3.0"))
								        	 {
				        						 LogSystem.info(request, "result 00 " + day + " " + cert.getString("keyVersion") + ", tidak perlu renewal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        							 renewal = false;
			        							 LogSystem.info(request, "Buat sertifikat ? " + renewal, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								        	 }
			        					 }
			        				 }
			        				 
			        				 if(renewal)
			        				 {
			        					 genCert = kms.createSertifikat(user.getId(), renewal, user.getUserdata().getLevel());
			        					 
			        					 if (genCert.getString("result").equals("00"))
					        			 {
					        				 version = "v3";
        				 
					        			 }
					        			 else if (genCert.getString("result").equals("06") || (genCert.getString("result").equals("05") && genCert.getString("info").equals("certificate is already exist")))
					        			 {
					        				 version = "v3";
					        				 LogSystem.info(request, "Sertifikat sudah ada", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					        			 }
					        			 else
					        			 {
					        				 LogSystem.info(request, "Gagal generate sertifikat " + genCert, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					    	        		 result.put("result", "12");
					            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
					            			 context.put("trxjson", result.toString());
					            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					            			 
					            			 try 
						        			 {
						        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
						        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
										     }catch(Exception e)
										     {
										    	 e.printStackTrace();
										    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										     }
					            			 
					            			 return;
					        			 }
			        				 }
			        				 
			        			 }
			        			 catch(Exception e)
			        			 {
			        				 e.printStackTrace();
			        				 result.put("result", "12");
			            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			            			 context.put("trxjson", result.toString());
			            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			            			 
			            			 try 
				        			 {
				        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
				        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
								     }catch(Exception q)
								     {
								    	 q.printStackTrace();
								    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								     }
			            			 
			            			 return;
			        			 }
			        			 
			        			 
			        		 }
			        		 	 
			        		 if(cert.getString("result").equals("06") || cert.getString("result").equals("07"))
			        		 {
			        			 LogSystem.info(request, "Sertifikat expired/revoke", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        			 result.put("result", "12");
			        			 if(cert.getString("result").equals("06"))
			        			 {
			        				 result.put("notif", "gagal proses tandatangan, masa aktif sertifikat elektronik Anda sudah habis. Silahkan menghubungi pihak Aplikasi untuk melakukan registrasi ulang Akun Anda ke pihak Digisign");
			        			 }
			        			 
			        			 if(cert.getString("result").equals("07"))
			        			 {
			        				 result.put("notif", "gagal proses tandatangan, sertifikat elektronik Anda sudah dicabut. Silahkan menghubungi pihak Aplikasi untuk melakukan registrasi ulang Akun Anda ke pihak Digisign"); 
			        			 }
		            			 context.put("trxjson", result.toString());
		            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		            			 
		            			 try 
			        			 {
			        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
			        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
							     }catch(Exception e)
							     {
							    	 e.printStackTrace();
							    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							     }
		            			 return;
			        		 }
			        	 }
			        	 else
			        	 {
			        		 LogSystem.info(request, "Hasil dari depan 00, cek versi sertifikatnya", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 KmsService kms = new KmsService(request, "KMS Service/"+refTrx, CATEGORY, start, mitra_req, email_req);
			        		 JSONObject cert = new JSONObject();
			        		 String mitra = null;

			        		 LogSystem.info(request, "USERRR : " + user.getNick(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 LogSystem.info(request, "Cek mitra : " + user.getMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    				 if(user.getMitra() != null)
		    				 {
		    					 LogSystem.info(request, "Mitra tidak null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
		    					 mitra = user.getMitra().getId().toString();
		    				 }
		    				 else 
		    				 {
		    					 LogSystem.info(request, "Mitra null", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		    					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), "");
		    					 mitra = "";
		    				 }
		    				 
		    				 if(!cert.has("result"))
		    				 {									
		    					 result.put("result", "12");
    	            			 result.put("notif", "System timeout. silahkan coba kembali.");
    	            			 context.put("trxjson", result.toString());
    	            			 return; 
		    				 }
		    				 
		    				 if(cert.getString("result").length() >3 || cert.getString("result").equals(""))
		    				 {
		    					 result.put("result", "12");
    	            			 result.put("notif", "gagal proses tandatangan");
    	            			 context.put("trxjson", result.toString());
    	            			 return; 
		    				 }
		    				 
		    				 if(cert.has("keyVersion")) 
		    				 {
		    					 if(cert.getString("keyVersion").equals("1.0") || cert.getString("keyVersion").equals("2.0"))
		    					 {
		    						 version = "v1";
		    					 }
		    					 
		    					 if(cert.getString("keyVersion").equals("3.0"))
		    					 {
		    						 version = "v3";
		    					 }
		    				 }
		    				 else
		    				 {
		    					 version = "v1";
		    				 }
		    				 
		    				 LogSystem.info(request, "Version " + version, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	 }
		        	 }
		        	 else
		        	 {
		        		 LogSystem.info(request, "Gagal generate sertifikat. Status sertifikat null dari depan", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
    	        		 result.put("result", "12");
            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
            			 context.put("trxjson", result.toString());
            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
            			 
            			 try 
	        			 {
	        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
	        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
					     }catch(Exception e)
					     {
					    	 e.printStackTrace();
					    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					     }
            			 
            			 return;
		        	 }
		         }
		         else
	        	 {
	        		 LogSystem.info(request, "Gagal generate sertifikat. Tidak ada data cert sertifikat null dari depan", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 result.put("result", "12");
        			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
        			 context.put("trxjson", result.toString());
        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
        			 try 
        			 {
        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
				     }catch(Exception e)
				     {
				    	 e.printStackTrace();
				    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				     }
        			 return;
	        	 }
		         
		         boolean sign=false;
		         boolean signPrc=false;
		         Mitra mitra;
		         mitra = dList.get(0).getDocument().getEeuser().getMitra();
		         if (spros) {
					BillingSystem bs;
					int balance = 0;
					KillBillDocumentHttps kdh = null;
					KillBillPersonalHttps kph = null;
					try {
						kdh = new KillBillDocumentHttps(request, refTrx,  mitra_req, email_req, start, CATEGORY);
						kph = new KillBillPersonalHttps(request, refTrx,  mitra_req, email_req, start, CATEGORY);
					} catch (Exception e) {
						// TODO: handle exception
						LogSystem.error(request, "System timout", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						result.put("result", "06");
						result.put("status", status);
						result.put("notif", "System timeout, mohon menunggu 10 menit kemudian");
						context.put("trxjson", result.toString());
						LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						return;
					}
					
					int jmlttd = dList.size();
					String inv = null;
					String cur = null;
					InvoiceDao idao = new InvoiceDao(db);
					if (dList.get(0).getDocument().getPayment() == '2') {
						JSONObject resp = kph.getBalance("MT" + mitra.getId(), request);
						if (resp.has("data")) {
							JSONObject data = resp.getJSONObject("data");
							balance = data.getInt("amount");
						} else {
							result.put("result", "FE");
							result.put("notif", "System timeout. silahkan coba kembali.");
							return;
						}

						if (balance < jmlttd) {
							//		        		 kd.close();
							//		        		 kp.close();

							result.put("result", "61");
							result.put("status", status);
							result.put("notif", "balance ttd mitra tidak cukup");
							context.put("trxjson", result.toString());
							LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							
							try 
		        			 {
		        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
		        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
						     }catch(Exception e)
						     {
						    	 e.printStackTrace();
						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						     }
							
							return;
						}

						//		        	 inv=kp.setTransaction("MT"+mitra.getId(), jmlttd);
						JSONObject obj = kph.setTransaction("MT" + mitra.getId(), 1);
						if (obj.has("result")) {
							String hasil = obj.getString("result");
							if (hasil.equals("00")) {
								inv = obj.getString("invoiceid");
								cur = obj.getString("current_balance");
							} else {
								result.put("result", "FE");
								result.put("notif", "System timeout. silahkan coba kembali.");
								return;
							}
						} else {

							result.put("result", "FE");
							result.put("notif", "System timeout. silahkan coba kembali.");
							return;

						}
						
						id.co.keriss.consolidate.ee.Invoice ivc = new id.co.keriss.consolidate.ee.Invoice();
						ivc.setDatetime(new Date());
						ivc.setAmount(jmlttd);
						ivc.setEeuser(dList.get(0).getDocument().getEeuser());
						ivc.setExternal_key("MT" + mitra.getId());
						ivc.setTenant('1');
						ivc.setTrx('2');
						ivc.setKb_invoice(inv);
						ivc.setDocument(dList.get(0).getDocument());
						ivc.setCur_balance(Long.parseLong(cur));
						idao.create(ivc);

						
						if (inv != null) {
							SignDocMitraV_session sd = new SignDocMitraV_session();

							try {
								
								sign = sd.signDoc(user, dList, inv, db,refTrx,request, version, mitra_req, email_req, CATEGORY, start);
								if (sign == false) {
									for(DocumentAccess da : dList) {
										result.put("result", "06");
										result.put("status", status);
										result.put("notif", "gagal proses tandatangan");
										//					     			kph.reverseTransaction(inv, 1);
										JSONObject rev = kph.reverseTransaction(inv, 1);
										if (rev.has("result")) {
											String hasil = rev.getString("result");
											if (hasil.equals("00")) {
												inv = rev.getString("invoiceid");
												cur = rev.getString("current_balance");
												id.co.keriss.consolidate.ee.Invoice ivc_rev = new id.co.keriss.consolidate.ee.Invoice();
												ivc_rev.setDatetime(new Date());
												ivc_rev.setAmount(1);
												ivc_rev.setEeuser(da.getEeuser());
												ivc_rev.setExternal_key("MT" + mitra.getId());
												ivc_rev.setTenant('1');
												ivc_rev.setTrx('3');
												ivc_rev.setKb_invoice(inv);
												ivc_rev.setDocument(da.getDocument());
												ivc_rev.setCur_balance(Long.parseLong(cur));
												idao.create(ivc_rev);
											} else {
												String notify = rev.getString("notif");
												LogSystem.info(request, notify, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
											}
										} else {
											LogSystem.info(request, "Gagal Konek Ke Billing Reversal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
											idao.deleteWhere(inv);
										}
										context.put("trxjson", result.toString());
										LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										return;
									}
								}
							} catch (Exception e) {
								// TODO: handle exception
								result.put("result", "06");
								result.put("status", status);
								result.put("notif", "gagal proses tandatangan");
								for(DocumentAccess da : dList) {
									JSONObject rev = kph.reverseTransaction(inv, 1);
									if (rev.has("result")) {
										String hasil = rev.getString("result");
										if (hasil.equals("00")) {
											inv = rev.getString("invoiceid");
											cur = rev.getString("current_balance");
											id.co.keriss.consolidate.ee.Invoice ivc_rev = new id.co.keriss.consolidate.ee.Invoice();
											ivc_rev.setDatetime(new Date());
											ivc_rev.setAmount(1);
											ivc_rev.setEeuser(da.getEeuser());
											ivc_rev.setExternal_key("MT" + mitra.getId());
											ivc_rev.setTenant('1');
											ivc_rev.setTrx('3');
											ivc_rev.setKb_invoice(inv);
											ivc_rev.setDocument(da.getDocument());
											ivc_rev.setCur_balance(Long.parseLong(cur));
											idao.create(ivc_rev);
										} else {
											String notify = rev.getString("notif");
											LogSystem.info(request, notify, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										}
									} else {
										LogSystem.info(request, "Gagal Konek Ke Billing Reversal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										idao.deleteWhere(inv);
									}
								}
								sign = false;
								context.put("trxjson", result.toString());
								LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								return;
							}

						

						}
						//		        	 kd.close();
						//		        	 kp.close();

					} else if (dList.get(0).getDocument().getPayment() == '3') {
						//		        	 balance=kd.getBalance("MT"+mitra.getId());

						//List<DocumentAccess> ld=dao.findByDocSign(dList.get(0).getDocument().getId());
						List<id.co.keriss.consolidate.ee.Invoice> li = idao
								.findByDoc(dList.get(0).getDocument().getId());
						//if(ld.size()==0) {
						if (li.size() == 0) {
							//inv=kd.setTransaction("MT"+mitra.getId(), 1);
							JSONObject resp = kdh.getBalance("MT" + mitra.getId(), request);
							if (resp.has("data")) {
								JSONObject data = resp.getJSONObject("data");
								balance = data.getInt("amount");
							} else {
								result.put("result", "FE");
								result.put("notif", "System timeout. silahkan coba kembali.");
								return;
							}
							LogSystem.info(request, "Balance dokumen = " + balance, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							//System.out.println("balance dokumen = "+balance);
							if (balance < 1) {
								//							  kd.close();
								//							  kp.close();
								result.put("result", "06");
								result.put("status", status);
								result.put("notif", "balance doc mitra tidak cukup");
								context.put("trxjson", result.toString());
								LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								
								try 
			        			 {
			        				 ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
			        				 logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen " + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
							     }catch(Exception e)
							     {
							    	 e.printStackTrace();
							    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							     }
								
								return;
							}

							JSONObject obj = kdh.setTransaction("MT" + mitra.getId(), 1);
							if (obj.has("result")) {
								String hasil = obj.getString("result");
								if (hasil.equals("00")) {
									inv = obj.getString("invoiceid");
									cur = obj.getString("current_balance");
								} else {
									result.put("result", "FE");
									result.put("notif", "System timeout. silahkan coba kembali.");
									return;
								}
							} else {

								result.put("result", "FE");
								result.put("notif", "System timeout. silahkan coba kembali.");
								return;

							}

							//						  String[] split=inv.split(" ");
							//						  inv=split[1];

							id.co.keriss.consolidate.ee.Invoice ivc = new id.co.keriss.consolidate.ee.Invoice();
							ivc.setDatetime(new Date());
							ivc.setAmount(1);
							ivc.setEeuser(dList.get(0).getDocument().getEeuser());
							ivc.setExternal_key("MT" + mitra.getId());
							ivc.setTenant('2');
							ivc.setTrx('2');
							ivc.setKb_invoice(inv);
							ivc.setDocument(dList.get(0).getDocument());
							ivc.setCur_balance(Long.parseLong(cur));
							idao.create(ivc);

							if (inv == null) {
								result.put("result", "06");
								result.put("status", status);
								result.put("notif", "gagal proses tandatangan [bill]");
								//				     			kd.reverseTransaction(inv);
								kdh.reverseTransaction(inv, 1);
								idao.deleteWhere(inv);
								context.put("trxjson", result.toString());
								LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								//				     			kd.close();
								//				     			kp.close();
								return;
							}
						}

						SignDocMitraV_session sd = new SignDocMitraV_session();
						
							try {
								sign = sd.signDoc(user, dList, inv, db,refTrx,request, version, mitra_req, email_req, CATEGORY, start);
								if (sign == false) {
									for (DocumentAccess da : dList) {
										result.put("result", "06");
										result.put("status", status);
										result.put("notif", "gagal proses tandatangan");
										if (inv != null) {
											//				     				kd.reverseTransaction(inv);
											//				     				kdh.reverseTransaction(inv, 1);
											//				     				idao.deleteWhere(inv);
											JSONObject rev = kdh.reverseTransaction(inv, 1);
											if (rev.has("result")) {
												String hasil = rev.getString("result");
												if (hasil.equals("00")) {
													inv = rev.getString("invoiceid");
													cur = rev.getString("current_balance");
													id.co.keriss.consolidate.ee.Invoice ivc_rev = new id.co.keriss.consolidate.ee.Invoice();
													ivc_rev.setDatetime(new Date());
													ivc_rev.setAmount(1);
													ivc_rev.setEeuser(da.getEeuser());
													ivc_rev.setExternal_key("MT" + mitra.getId());
													ivc_rev.setTenant('2');
													ivc_rev.setTrx('3');
													ivc_rev.setKb_invoice(inv);
													ivc_rev.setDocument(da.getDocument());
													ivc_rev.setCur_balance(Long.parseLong(cur));
													idao.create(ivc_rev);
												} else {
													String notify = rev.getString("notif");
													LogSystem.info(request, notify, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
												}
											} else {
												LogSystem.info(request, "Gagal Konek Ke Billing Reversal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
												idao.deleteWhere(inv);
											}
										}
										context.put("trxjson", result.toString());
										LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										//				     			kd.close();
										//				     			kp.close();
										return;
									}
								}
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
								result.put("result", "06");
								result.put("status", status);
								result.put("notif", "gagal proses tandatangan");
								if (inv != null) {
									for (DocumentAccess da : dList) {
									//			     				kd.reverseTransaction(inv);
									//			     				kdh.reverseTransaction(inv, 1);
									//			     				idao.deleteWhere(inv);
									JSONObject rev = kdh.reverseTransaction(inv, 1);
									if (rev.has("result")) {
										String hasil = rev.getString("result");
										if (hasil.equals("00")) {
											inv = rev.getString("invoiceid");
											cur = rev.getString("current_balance");
											id.co.keriss.consolidate.ee.Invoice ivc_rev = new id.co.keriss.consolidate.ee.Invoice();
											ivc_rev.setDatetime(new Date());
											ivc_rev.setAmount(1);
											ivc_rev.setEeuser(da.getEeuser());
											ivc_rev.setExternal_key("MT" + mitra.getId());
											ivc_rev.setTenant('2');
											ivc_rev.setTrx('3');
											ivc_rev.setKb_invoice(inv);
											ivc_rev.setDocument(da.getDocument());
											ivc_rev.setCur_balance(Long.parseLong(cur));
											idao.create(ivc_rev);
										} else {
											String notify = rev.getString("notif");
											LogSystem.info(request, notify, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										}
									} else {
										LogSystem.info(request, "Gagal Konek Ke Billing Reversal", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										idao.deleteWhere(inv);
									}
								}
								sign = false;
								context.put("trxjson", result.toString());
								LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								//			        		kd.close();
								//			        		kp.close();
								return;
							}

						}
						//	        		 kd.close();
						//	        		 kp.close();
					} 
				}
				Long jml=(long) 1;
		         if(sign==true||approval.equals("true")) {
		        	rc="00";
		        	status=00;
		        	info="Proses tanda tangan berhasil!";
		        	cc.setStatus("yes");
		        	ccd.update(cc);
		        	DocumentAccess docac=dList.get(0);
		        	String iddoc=String.valueOf(docac.getDocument().getId());
		        	String namadoc=docac.getDocument().getFile_name();
		        	String namattd=docac.getName();
		        	String jkttd=String.valueOf(docac.getEeuser().getUserdata().getJk());
		        	String docs ="";
					String link ="";
					documentIDa =docac.getDocument().getId();
					mitraid =docac.getDocument().getEeuser().getMitra().getId();
					
					result.put("document_id", docac.getDocument().getIdMitra());
					//result untuk status document
					if(rc.equals("00")) {
						JSONArray lWaiting= new JSONArray();
						JSONArray lSigned= new JSONArray();
						JSONArray lInitials= new JSONArray();
						JSONArray lInitialsWaiting= new JSONArray();
						id_dokumen = iddoc;
						email_user = docac.getEeuser().getNick();
						for(DocumentAccess doc:dList_check) {
							
							
 		 					JSONObject docObj=new JSONObject();
 							/*change to eeuser*/
 		// 					User userDoc=new UserManager(db).findByUserID(String.valueOf(doc.getUserdata().getId()));
 		
 		 					User userDoc=doc.getEeuser();
 		 					if(userDoc==null) {
 								docObj.put("name", doc.getName());
 								docObj.put("email", doc.getEmail());
 		
 		 					}else {
	 							docObj.put("name", userDoc.getName());
	 							docObj.put("email", userDoc.getNick());
 		 					}
 		 					if(doc.getType().equals("sign") && !doc.isFlag()) {
 		 						lWaiting.put(docObj);
 		 							
 		 					}if(doc.getType().equals("sign") && doc.isFlag()) {
 		 						lSigned.put(docObj);
 		 					}
 		 					
 		 					if(doc.getType().equals("initials") && !doc.isFlag()) {
 		 						lInitialsWaiting.put(docObj);
 		 					}
 		 					
 		 					if(doc.getType().equals("initials") && doc.isFlag()) {
 		 						lInitials.put(docObj);
 		 					}
 		 				}
//						System.out.println("yang nunggu = "+lWaiting);
//						System.out.println("Berapa Orang = "+lWaiting.length());
 		 				if(lWaiting!=null && lWaiting.length()>0) {
 		 					//result.put("waiting", lWaiting);
 		 					result.put("status_document", "waiting");
 		 					result.put("status", "22");
 		 					status_doc = "waiting";
 		 					
 		 				}
 		 				if(lSigned!=null && lSigned.length()>0) {
 		 					//result.put("signed", lSigned);
 		 					result.put("status", "00");
 		 					if(lWaiting==null || lWaiting.length()==0) {
 		 						result.put("status_document", "complete");
 		 						status_doc = "complete";
 		 					}
 		 				}
 		 				
 		 				
 		 				if(lInitialsWaiting!=null && lInitialsWaiting.length()>0) {
 		 					//result.put("waiting", lInitialsWaiting);
 		 					result.put("status_document", "waiting");
 		 					result.put("status", "22");
 		 					status_doc = "waiting";
 		 				}
 		 				
 		 				if(lInitials!=null && lInitials.length()>0) {
 		 					//result.put("initials", lInitials);
 		 					result.put("status", "00");
 		 					if(lInitialsWaiting==null || lInitialsWaiting.length()==0) {
 		 						result.put("status_document", "complete");
 		 						status_doc = "complete";
 		 					}
 		 				}
					}
					//END result untuk status document
					
					try {
						   docs = AESEncryption.encryptDoc(iddoc);
						   link = "https://"+DSAPI.DOMAIN+"/doc/source.html?frmProcess=viewFile&doc="+iddoc;
						     //+ URLEncoder.encode(docs, "UTF-8");
						} catch (Exception e1) {
						   // TODO Auto-generated catch block
						   e1.printStackTrace();
						}
					
		        	SendNotifSignDoc std=new SendNotifSignDoc(request, refTrx);
		        	List<DocumentAccess> vda=dao.findByDoc(iddoc);
		        	Vector<String> checkmail = new Vector();
		        	String notif = "Proses tanda tangan berhasil!";
		        	boolean alasan = false;
		        	try {
		        		if(approval.equals("true")) {
							if(!reason.equals(null) && !reason.equals("null")) {
								notif="Proses Penolakan Dokumen berhasil!";
								String outputreason;
								info="Proses Penolakan Dokumen berhasil!";
								LogSystem.info(request, "STATUS: "+info +" | denied by: "+docac.getEmail()+" | idDoc: "+docac.getDocument().getIdMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								
								alasan = true;
			        		}else {
			        			org.hibernate.Transaction tx3 = db.session().beginTransaction();
								tx3.setTimeout(90);
								DocumentAccess aid = docac;
								aid.setCancel(false);
								aid.setFlag(true);
								aid.setDate_sign(new Date());;
								db.session().update(aid);
								tx3.commit();
								notif="Proses Approval berhasil!";
								info="Proses Persetujuan Dokumen berhasil!";
								LogSystem.info(request, "STATUS: "+info +" | approved by: "+docac.getEmail()+" | idDoc: "+docac.getDocument().getIdMitra(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		}
							
						}
		        		
		        		
			        	if (rc.equals("00")) {
			        		org.hibernate.Transaction tx = db.session().beginTransaction();
							String Final = "\"result\""+":"+"\""+rc+"\"";
							String the_status = "\"status\""+":"+"\""+status+"\"";
							String notifikasi = "\"notif\""+":"+"\""+info+"\"";
							String status_akhir = "\"status_document\""+":"+"\""+status_doc+"\"";
							String dokumen_id = "\"document_id\""+":"+"\""+docac.getDocument().getIdMitra()+"\"";
							String email_us = "\"email_user\""+":"+"\""+email_user+"\"";
							String successresult = "{"+dokumen_id+","+the_status+","+status_akhir+","+Final+","+notifikasi+","+email_us+"}";
				        	UserManager euser=new UserManager(getDB(context));
				        	AESEncryption AES = new AESEncryption();
				        	long documentIDs = documentIDa;
				        	Documents eeuseruserids = euser.findOwnereeuserByDocument(documentIDa);
							boolean redirect = eeuseruserids.isRedirect();
							TokenMitra tm = euser.findTokenMitra(mitraid);
							String aes_key = tm.getAes_key();
							LogSystem.info(request, "CEK REDIRECT (TRUE/FALSE) :"+redirect, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							
							//long iduser = docac.getEeuser().getId();
					        String id_session=object.getString("id_session");
					        String id_key =object.getString("key_session");
					    	SigningSessionDao sessions = new SigningSessionDao(db);
					    	SigningSession ses = sessions.findSession(id_session,id_key);
					    	ses.setUsed(true);
					    	db.session().update(ses);
							String encryptresult = AES.encryptKeyAndResultRedirect(aes_key,successresult);
							String encoderencrypt = URLEncoder.encode(AESEncryption.encryptKeyAndResultRedirect(aes_key,successresult),"UTF-8");
							LogSystem.info(request, "Sukses DI encode encrypt : "+encoderencrypt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							String decryptresult = AES.decryptKeyAndResultRedirect(aes_key,encryptresult);
							LogSystem.info(request, "Sukses DI decrypt : "+decryptresult, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							LogSystem.info(request, "Apakah Redirect ? (True/False) :"+redirect, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							
							String alamat_redirect = mitra.getSigning_redirect();
							tx.commit();
							if (alamat_redirect != null) {
								
								String concat = alamat_redirect+"?msg="+encoderencrypt;
								LogSystem.info(request, "LINK :"+concat, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								if (redirect == true) {
									result.put("link", concat);
									LogSystem.info(request, "Redirect Process Works !", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								}
								
								if (redirect)
								{
									LogSystem.info(request, "Kirim callback method GET ke : " + concat , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									//kirim GET
									callback callback = new callback(request, refTrx);
									JSONObject jo = new JSONObject();
									try {
//										callback = new callback(request, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										
										jo = callback.call(concat, mitra_req, email_req, CATEGORY, start);
										LogSystem.info(request, jo.toString() , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									}catch(Exception e)
									{
										LogSystem.info(request, "Gagal kirim callback", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										LogSystem.error(getClass(), e);
									}
									
									LogSystem.info(request, "JO GET STRING " + jo.getString("code"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									
									if (jo.getString("code") == "200" || jo.getInt("code") == 200)
									{
										CallbackPending CP = new CallbackPending();
										CP.setCallback(concat);
										CP.setMitra(mitra);
										CP.setResponse(200);
										CP.setTipe("redirect");
										new CallbackPendingDao(db).create(CP);
										LogSystem.info(request, "Insert into callback_pending done 200!", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									}
									else
									{
										CallbackPending CP = new CallbackPending();
										CP.setCallback(concat);
										CP.setMitra(mitra);
										CP.setResponse(212);
										CP.setTipe("redirect");
										new CallbackPendingDao(db).create(CP);
										LogSystem.info(request, "Insert into callback_pending done 212!", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									}
								}
//								else
//								{
//									CallbackPending CP = new CallbackPending();
//									CP.setCallback(concat);
//									CP.setMitra(mitra);
//									CP.setResponse(212);
//									CP.setTipe("redirect");
//									new CallbackPendingDao(db).create(CP);
//									LogSystem.info(request, "Insert into callback_pending done!", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//								}		
								
							}
							/////////////////////////CHANGE CURRENT SEQUENCE///////////////////////
							boolean seq = eeuseruserids.isSequence();
							LogSystem.info(request, "APAKAH SEQUENCE : "+seq, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							int smallest_seq =  eeuseruserids.getCurrent_seq();
							if(um.findSequenceByDocumentApproval(iddoc, email_user)!=null) {
								 if(smallest_seq == um.findSequenceByDocumentApproval(iddoc, email_user).getSequence_no()) {
										org.hibernate.Transaction tx4 = db.session().beginTransaction();
										tx4.setTimeout(90);
										DocumentAccess ds =  um.findSequenceByDocumentApproval(iddoc, email_user);
										ds.setCancel(false);
										ds.setFlag(true);
										ds.setDate_sign(new Date());;
										db.session().update(ds);
										tx4.commit();
								 }
							 }else {
								 LogSystem.info(request, "Tidak ada Proses Penolakan pada dokumen tersebut!", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							 }
							if(alasan) {
								org.hibernate.Transaction tx2 = db.session().beginTransaction();
								tx2.setTimeout(90);
								DocumentAccess ad = docac;
								ad.setCancel(true);
								ad.setFlag(true);
								ad.setDate_sign(new Date());
								db.session().update(ad);
								tx2.commit();
								Canceled_document CD = new Canceled_document();
								CD.setDoc_access(docac);
								CD.setDate_time(new Date());
								if(!reason.equals("alasan_null_fromuser")) {
									CD.setReason(reason);
								}
								
								new CancelDocumentDao(db).create(CD);
							}else {
								
								if (seq==true) {
									 LogSystem.info(request, "SEQUENCE NIH", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									 LogSystem.info(request, "ID DOC : "+iddoc, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									 LogSystem.info(request, "EMAIL User : "+email_us, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

									 
									 LogSystem.info(request, "Now turn >>"+smallest_seq, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
									 
									 
									 if(um.findSmallestSequenceByDocument(iddoc)!=null) { //kondisi belum abis
									 	LogSystem.info(request, "Next Sequence >>"+um.findSmallestSequenceByDocument(iddoc), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										Documents doku=dList.get(0).getDocument();
									   	doku.setCurrent_seq(um.findSmallestSequenceByDocument(iddoc));
									    new DocumentsDao(db).update(doku);
								    	}
									
								}
							}
							/////////////////////////EMAIL JIKA SUKSES/////////////////////////////
							jml=dao.getWaitingSignUserByDoc(idDoc);
					        if(jml==0) {
					        		Documents doc=dList.get(0).getDocument();
					        		doc.setSign(true);
					        		new DocumentsDao(db).update(doc);
					        	}
								for(DocumentAccess da:vda) {
									
					        		boolean kirim=true;
					        		if(da.getEeuser()!=null) {
					        			
					        			for(String mail:checkmail) {
					        				if(mail.equalsIgnoreCase(da.getEmail())) {
					        					//std.kirim(da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link);
					        					kirim=false;
					        					break;
					        				}
					        			}
					        			if(kirim==true) {
					        				LogSystem.info(request, "Kirim email nih eeuser != null " + da.getEmail(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					        				checkmail.add(da.getEmail());
				        					try {
					        					std.kirim(da.getDocument().getWaktu_buat(), da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()), mitra_req, CATEGORY, start);
												
											} catch (Exception e) {
												LogSystem.error(getClass(), e);
											}
				        					
				        					LogSystem.info(request, "Alhamdulillah selesai", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				        					
						        			//}
					        			}
					        			
					        		} else {
					        			for(String mail:checkmail) {
					        				if(mail.equalsIgnoreCase(da.getEmail())) {
					        					//std.kirim(da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link);
					        					kirim=false;
					        					break;
					        				}
					        			}
					        			if(kirim==true) {
					        				//if(mitra.isNotifikasi()) {
					        					LogSystem.info(request, "Kirim email nih eeuser == null " + da.getEmail(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					        					checkmail.add(da.getEmail());
//				        					std.kirim(da.getDate_sign(), user.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
					        				std.kirim(da.getDocument().getWaktu_buat(), da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()), mitra_req, CATEGORY, start);			        			
					        				
					        				LogSystem.info(request, "Alhamdulillah selesai", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					        				//}
					        			}
					        			//
					        		}
					        		
					        	}
								result.put("result", "00");
				     			result.put("status", "00");
				     			result.put("notif", notif);
				     			
				     			try {
						        	ActivityLog logSystem = new ActivityLog(request, refTrx,  mitra_req,  email_req,  start,  CATEGORY);
						        	logSystem.POST("signing", "success", "[API] Berhasil tandatangan dokumen", Long.toString(docac.getEeuser().getId()), Long.toString(documentIDa), null, "password", "otp",null);
						        }catch(Exception e)
						        {
						        	e.printStackTrace();
						        	LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						        }
				        		
							///////////////
			        	}
		        	} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
		        	}
		         }
		         
		         
		         /*
		         int blc=bs.getBalance();

		         JSONArray arr=object.getJSONArray("user");
		         int jmlTTD=0;
		         for(DocumentAccess documentAccess: dList) {
		        	 for(int i=0; i<arr.length();i++) {
		        		 JSONObject d=(JSONObject) arr.get(i);
		        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
	        				
	        				 jmlTTD++;
		        		 }
		        	 }
			         
		         }
		         
		         System.out.println("Jumlah ttd = "+jmlTTD);
		         if(blc-jmlTTD>=0) {
		         
			         for(DocumentAccess documentAccess: dList) {
			        	 for(int i=0; i<arr.length();i++) {
			        		 JSONObject d=(JSONObject) arr.get(i);
			        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
		        				 Deposit dep=new Deposit(documentAccess,db);
		
		        				 if(dep.transaksi(1)) {
		        					 boolean res=signDoc(user, documentAccess,dep.getBillSys().getLastInvoice(),db);
				        			 if(res) {
				        				 signPrc=true;
				        			 }else {
				        				 dep.reversal();
				        				 result.put("info", "gagal tanda tangan, coba ulangi beberapa saat lagi");
				        			 }
			        			 }else{
			        				 info="transaksi gagal, coba ulangi beberapa saat lagi";
			        				 
			        				 if(!dep.getLastError().equals("error")) {
			        					 info=dep.getLastError();
			        				 }
		        					 result.put("info", info);
		        					 
			        			 }
		        				 
			        		 }
			        	 }
				         
			         }
		         }
		         else {
		        	 
					 result.put("info", "saldo anda kurang, silakan topup terlebih dahulu");
					 System.out.println("saldo anda kurang");
					 rc="E1";

		         }
		         
		         if(signPrc) {
		        	  
				      boolean sign = true ;
				      List<DocumentAccess> list = new DocumentsAccessDao(db).findByDoc(idDoc);
				      for (DocumentAccess doa : list) {
				    	  
				    	 if(doa.getEeuser()!=null) {

							 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getEeuser().getUserdata(), doa.getEmail());
							 mail.run();
				    	 }else {
				    		 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getName(), doa.getEmail());
							 mail.run();
				    	 }
						 if(doa.getType().equals("sign") && doa.isFlag()==false) {
							sign=false; 
						 }
				      
				
		         
			        Documents docx=list.get(0).getDocument();
			        if(sign==true) {
			        	docx.setSign(true);
			        	status=1;
			        }
		    		new DocumentsDao(db).update(docx);
		    		
		    		
			        rc="00";
			        cc.setStatus("yes");
			        ccd.update(cc);
		         }
		         */
	         }
	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
            e.printStackTrace();
		}
		finally
		{
			if(docpros == 200)
			{
				DocumentsDao docProc = new DocumentsDao(db);
				docpros = docProc.findByidDocUnlock(docId);
//				JedisPool pool = new JedisPool(new JedisPoolConfig(), DSAPI.REDIS_HOST, DSAPI.REDIS_PORT, DSAPI.REDIS_TIMEOUT, DSAPI.REDIS_PASSWORD);
//    			
//    			try (Jedis jedis = pool.getResource()) {
//    					String data = jedis.get(docId.toString());
//    					
//    					if(data != null)
//    					{
//    						long redis = jedis.del(docId.toString());
//    						
//    						LogSystem.info(request, "Response del redis " + redis, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//    					}
//    				}
//    			catch(Exception e)
//    			{
//    				e.printStackTrace();
//    			}
//    			finally
//    			{
//    				pool.close();
//    			}
    			
//				DocumentsDao ddao = new DocumentsDao(db);
//				docpros = ddao.findByidDoc(docId);
//				
//				
//				Transaction t = null;
//	       		if(!db.session().getTransaction().isActive())
//	       		{
//			    	t=db.session().beginTransaction();
//	       		}
//	       		else
//	       		{
//			    	t=db.session().getTransaction();
//	       		}
//	       		
//	       		LogSystem.info(request, "Update kembali proses ke false " + docpros, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//				docpros.setProses(false);
//				docpros.setLast_proses(new Date());
//				t.commit();
//				
//				LogSystem.info(request, "Update dokumen proses " + docpros.getProses(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//				try {
//					new DocumentsDao(db).update(docpros);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				LogSystem.info(request, "Berhasil update dokumen proses " + docpros.getProses() + " Last proses " + docpros.getLast_proses() , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			}
		}
		
		try {
			result.put("result", rc);
			result.put("status", status);
			result.put("notif", info);
			
			
			LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
        context.getResponse().setContentType("application/json");

        //System.out.println("SEND :"+result.toString());
//        LogSystem.info(request, "SEND :"+result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		context.put("trxjson", result.toString());
		try {
			LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "RESPON", (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 HttpSession session          = context.getSession();
	        
        session.removeAttribute (USER);

	}
	
	private boolean signDoc(User userTrx, List<DocumentAccess> doc, String inv, DB db,String refTrx , HttpServletRequest req, String version, String mitra_req, String email_req, String CATEGORY, long start) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String tanggal = ftanggal.format(date);
		String signdoc = "DSSG" + strDate + doc.get(0).getId().toString() + ".pdf";
		String path="";
		String original = doc.get(0).getDocument().getSigndoc();
		Documents dos = doc.get(0).getDocument();
		boolean sod = dos.isRedirect();
		boolean usingopt = doc.get(0).getDocument().getPath().startsWith("/file2/");
		if (!usingopt) {
			String opath = doc.get(0).getDocument().getPath();
			String apath[]=opath.split("/");
			apath[1]="file2";
			for (int i = 0; i < apath.length; i++) {
				path+=apath[i]+"/";
			}
		}else {
			path = doc.get(0).getDocument().getPath();
		}
		
		
		ArrayList<Long> docacc = new ArrayList<Long>();
		for (DocumentAccess dta : doc) {
			Long idAccess=dta.getId();
			docacc.add(idAccess);
		}
		JSONArray ida = new JSONArray();
		for(Long id:docacc) {
			ida.put(id);
		}
		try {
			DocumentSigner dSign=new DocumentSigner();

			JSONObject resSign = dSign.kirimKMS(ida, path+signdoc, userTrx.getId(), doc.get(0).getDocument().getId(),refTrx,req, version, mitra_req, email_req, CATEGORY, start);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				for (DocumentAccess dto : doc) {
					Date dateSign= new Date(resSign.getLong("date"));
					dto.setFlag(true);
					dto.setDate_sign(date);
					dto.setInvoice(inv);
					new DocumentsAccessDao(db).update(dto);
					
					Documents documents=dto.getDocument();
					documents.setSigndoc(signdoc);
					documents.setPath(path);
		    		new DocumentsDao(db).update(documents);
				}
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e);
		}
		return false;


	}
	
	class MailSender implements Runnable{

		private String to;
		private Userdata penerimaemail;
		private Userdata action;
		private String doc;
		private String penerimaEmailNotReg;
		
		public MailSender(String doc,Userdata action ,Userdata penerimaemail,String to) {
			this.to=to;
			this.penerimaemail=penerimaemail;
			this.action=action;
			this.doc=doc;
		}
		public MailSender(String doc,Userdata action ,String penerimaEmailNotReg,String to) {
			this.to=to;
			this.penerimaEmailNotReg=penerimaEmailNotReg;
			this.action=action;
			this.doc=doc;
		}HttpServletRequest request = null; 
		@Override
		public void run() {
			if(penerimaemail!=null)
				
			new SendMailSSL(request, "").sendMailNotifSign(doc,action,penerimaemail, to);
			else
			new SendMailSSL(request, "").sendMailNotifSign(doc,action,penerimaEmailNotReg, to);

		}
		
	}
	
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
