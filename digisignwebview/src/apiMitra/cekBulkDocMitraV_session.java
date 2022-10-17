package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.BillingSystem;
import id.co.keriss.consolidate.action.billing.KillBillDocument;
import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
import id.co.keriss.consolidate.action.billing.KillBillPersonal;
import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.EeuserMitraDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.KeyV3Dao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.KeyV3;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.dao.SigningSessionDao;

import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

import api.email.SendNotifSignDoc;
import api.email.SendSuccessGenerateCertificate;
import api.log.ActivityLog;

import java.net.URLEncoder;



public class cekBulkDocMitraV_session extends ActionSupport implements DSAPI {
	User userRecv;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="BULKWVPAGE"+sdfDate2.format(tgl).toString();
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        String namaUser = "";
        boolean otp = false;
        boolean valid=false;
        String info="";
		int count = 21;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		int status=0;
		String status_doc = null;
		String email_user =null;
		String id_dokumen =null;
		JSONObject result=new JSONObject();
		long documentIDa= 0;
    	long mitraid = 0;
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		String path_app = this.getClass().getName();
		String CATEGORY = "BULKSIGN";
		String email_req ="";
		String mitra_req ="";
		try{
		
			 String process=request.getRequestURI().split("/")[2];
	         //LogSystem.info(request,"PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

//	         LogSystem.info(request,"RECEIVE :"+sb.toString());
//	         log.info("RECEIVE :"+sb.toString());
	         LogSystem.info(request, sb.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "REQUEST", (System.currentTimeMillis() - start) / 1000f + "s");
//	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	     	if (object.has("refTrx"))
	     	{
	     		refTrx = object.getString("refTrx");
	     	}
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
	         
	         UserManager um=new UserManager(db);
	         
        	 user=um.findByEmail(object.getString("usersign"));
        	 String  sessionid = object.getString("sessionid");
        	 
        	 SigningSessionDao signingSessionDB = new SigningSessionDao(db);
 	         SigningSession signingSession = new SigningSession();
 	         signingSession = signingSessionDB.findSessionId(sessionid);
 	         
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
 			 
//        	 if(user!=null) {
//		         String idDoc=object.getString("idDoc");
//	        	 List<DocumentAccess> docac = dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
//	        	 if(docac.size()==0) {
//	        		 LogSystem.info(request,"size < 0");
//	        		 docac = dao.findDocEEuserSigned(Long.valueOf(idDoc), user.getNick());
//		        	 result.put("result", "00");
//	     			 result.put("status", status);
//	     			 result.put("notif", "Dokumen Sudah Ditandatangan");
//	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
//					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
//					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
//					 context.put("trxjson", result.toString());
//					 LogSystem.info(request, result.toString());
//	     			 return;
//
//	        	 }
//        	 }
	         
	         
	         ConfirmCode cc = null;
	         ConfirmCodeDao ccd = new ConfirmCodeDao(db);
	         //UserManager um=new UserManager(db);
	         EeuserMitraDao emdao=new EeuserMitraDao(db);
	         LogSystem.info(request,object.getString("userpwd"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         LogSystem.info(request,object.getString("usersign"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         LogSystem.info(request,object.getString("otp"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        
	         if(object.getString("userpwd")!=null && object.getString("usersign")!=null && object.getString("otp")!=null) {
	        	 //user=checkUser(object.getString("userpwd"), object.getString("usersign"), db);
	        	 user=um.findByUsernamePassword(object.getString("usersign"), object.getString("userpwd"));
	        	 
	        	 
	        	 //EeuserMitra em=emdao.findUserPwdMitra(object.getString("usersign"), object.getString("userpwd"), Long.valueOf(object.getString("mitra")));
	        	 //if(em!=null)user=em.getEeuser();
	        	 if(user!=null) {
	        		 //LogSystem.info(request,"User valid");
	        		 LogSystem.info(request, "User valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        		 //check otp
	        		 cc = ccd.getLastOTP(user.getId());
	        		 if(cc!=null) {
	        			 if(cc.getCode().equals(object.getString("otp"))) {
	        				 otp=true;
		        			 valid=true;
	        			 }
	        			 else {
	        				 LogSystem.error(request, "1 OTP tidak valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        				 //LogSystem.info(request,"Otp tidak valid");
		        			 result.put("result", "12");
		        			 result.put("notif", "kode OTP tidak valid");
		        			 context.put("trxjson", result.toString());
		        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		        			 
		        			 try 
		        			 {
		        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
		        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
						     }catch(Exception e)
						     {
						    	 e.printStackTrace();
						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						     }
		        			 
		        			 return;
	        			 }
	        		 }
	        		 else {
	        			 LogSystem.error(request, "2 OTP tidak valid", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        			 //LogSystem.info(request,"Otp tidak valid");
	        			 result.put("result", "12");
	        			 result.put("notif", "kode OTP tidak valid");
	        			 context.put("trxjson", result.toString());
	        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	        			 
	        			 try 
	        			 {
	        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
	        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
	        		 //LogSystem.info(request,"user tidak valid");
	        		 result.put("result", "12");
        			 result.put("notif", "Password salah");
        			 context.put("trxjson", result.toString());
        			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
        			 
        			 try 
        			 {
        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
	        	 
	        	 cc.setStatus("yes");
			     ccd.update(cc);
	        	 result.put("result", "00");
				 result.put("notif", "OTP dan Password Sukses");
				 context.put("trxjson", result.toString());
				 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				 
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
		        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
		        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
						     }catch(Exception e)
						     {
						    	 e.printStackTrace();
						    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						     }
	            			 
	            			 return;
			        	 }
			         }
		        	 if(!object.getString("cert").equals(""))
		        	 {
			        	 LogSystem.info(request, "Proses generate sertifikat", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	 if(!object.getString("cert").equals("00"))
			        	 {
			        		 LogSystem.info(request, "Hasil cek sertifikat dari depan = " + object.getString("cert"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
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
    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
    	            			 context.put("trxjson", result.toString());
    	            			 return; 
		    				 }
		    					 
		    				 
		    				 if (cert.has("keyVersion"))
		    				 {
		    					 if(cert.getString("keyVersion").equals("1.0"))
		    					 {
		    						 version = "v1";
		    					 }
		    					 
		    					 if(cert.getString("keyVersion").equals("1.0"))
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
			        		 
			        		 if (cert.getString("result").equals("G1") || cert.getString("result").equals("05") || (object.getString("cert").equals("03") && renewal) || (object.getString("cert").equals("04") && renewal))
			        		 {
			        			 JSONObject genCert= new JSONObject();
			        			 try {
			        				 LogSystem.info(request, "Proses generate sertifikat", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        				 if(!renewal)
			        				 {
			        					 if (object.has("genCert"))
				    		        	 {
			        						 String genCertInfo= "Tombol Saya Menyetujui Penerbitan Sertifikat Elektronik dan Menyetujui Penandatangan Dokumen ini ?";
			        						 if (cert.getString("result").equals("G1"))
			        						 {
			        							 genCertInfo= "Tombol Saya Menyetujui Penerbitan Sertifikat Elektronik baru dan Menyetujui Penandatangan Dokumen ini ?";
			        							 renewal = true;
			        						 }
				    		        		 LogSystem.info(request, genCertInfo + " "+object.getBoolean("genCert"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    		        		 if (!object.getBoolean("genCert"))
				    		        		 {
				    		        			 result.put("result", "12");
				    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
				    	            			 context.put("trxjson", result.toString());
				    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    	            			 
				    	            			 try 
							        			 {
							        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
							        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
			    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			    	            			 context.put("trxjson", result.toString());
			    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    	            			 
			    	            			 try 
						        			 {
						        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
						        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
				    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
				    	            			 context.put("trxjson", result.toString());
				    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				    	            			 
				    	            			 try 
							        			 {
							        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
							        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
			    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			    	            			 context.put("trxjson", result.toString());
			    	            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			    	            			 
			    	            			 try 
						        			 {
						        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
						        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
										     }catch(Exception e)
										     {
										    	 e.printStackTrace();
										    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
										     }
			    	            			 
			    	            			 return;
				        				 }
			        				 }
			        				 
			        				 genCert = kms.createSertifikat(user.getId(), renewal, user.getUserdata().getLevel());
			        			 }
			        			 catch(Exception e)
			        			 {
			        				 result.put("result", "12");
			            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			            			 context.put("trxjson", result.toString());
			            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			            			 
			            			 try 
				        			 {
				        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
				        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
								     }catch(Exception q)
								     {
								    	 q.printStackTrace();
								    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								     }
			            			 return;
			        			 }
			        			 LogSystem.info(request, "Response generate sertifikat = " + genCert , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        			 if (genCert.getString("result").equals("00"))
			        			 {
			        				 version = "v3";
//			        				 LogSystem.info(request, "Sukses generate sertifikat, kirim email ke " + object.getString("usersign"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//			        				 
//			        				 SendSuccessGenerateCertificate notifGenCert = new SendSuccessGenerateCertificate();
//			        				 if(renewal && object.getString("cert").equals("03"))
//			        				 {
//			        					 notifGenCert.kirimPerpanjang(user.getName(), user.getUserdata().getJk(), user.getNick(), "", mitra);
//			        				 }
//			        				 else
//			        				 {
//			        					 if(!object.getString("cert").equals("04") && !object.getString("cert").equals("G1"))
//			        					 {
//			        						 notifGenCert.kirimBaru(user.getName(), user.getUserdata().getJk(), user.getNick(), "", mitra);
//			        					 }
//			        				 }
//
//			        				 LogSystem.info(request, "Selesai mengirim email", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");		        				 
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
				        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
				        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
								     }catch(Exception e)
								     {
								    	 e.printStackTrace();
								    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								     }
			            			 return;
			        			 }
			        		 }
			        		 	 
			        		 if(cert.getString("result").equals("06") || cert.getString("result").equals("07"))
			        		 {
			        			 
			        			 LogSystem.info(request, "Sertifikat expired/revoke", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        			 result.put("result", "12");
		            			 result.put("notif", "gagal proses tandatangan, sertifikat elektronik kedaluwarsa atau dicabut");
		            			 context.put("trxjson", result.toString());
		            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		            			 try 
			        			 {
			        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
			        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
			        		 LogSystem.info(request, "Sertifikat aktif", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        		 
			        		 LogSystem.info(request, "Hasil cek sertifikat dari depan = " + object.getString("cert"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
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
    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
    	            			 context.put("trxjson", result.toString());
    	            			 return; 
		    				 }
		    				 
		    				 if(cert.has("keyVersion")) 
		    				 {
		    					 if(cert.getString("keyVersion").equals("1.0"))
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
			        	 }
		        	 }
		        	 else
		        	 {
		        		 LogSystem.info(request, "Gagal generate sertifikat. Status sertifikat null dari depan", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
    	        		 result.put("result", "12");
            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
            			 context.put("trxjson", result.toString());
            			 LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
            			 try 
	        			 {
	        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
	        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
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
        				 ActivityLog logSystem = new ActivityLog(request, refTrx, mitra_req, email_req, start, CATEGORY);
        				 logSystem.POST("signing", "failed", "[API BULK] Gagal tandatangan dokumen" + result.getString("notif"), object.getString("usersign"), Long.toString(documentIDa), null, "password", "otp",null);
				     }catch(Exception e)
				     {
				    	 e.printStackTrace();
				    	 LogSystem.info(request, "Gagal mengirim ke Log API", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				     }
        			 return;
	        	 }
	         }
	         
	         
//	         if(valid==true) {
//	        	 
//		         String idDoc=object.getString("idDoc");
//
//		         //List<DocumentAccess>dList=dao.findDocAccessEEuser(idDoc, String.valueOf(user.getId()) ,user.getNick());
//		         //List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getId());
//		         List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
//		         //LogSystem.info(request,"jumlah list = "+dList.size());
//		         LogSystem.info(request, "jumlah list = "+dList.size());
//		         
//		         boolean sign=false;
//		         boolean signPrc=false;
//		         BillingSystem bs;
//		         int balance=0;
////		         KillBillDocument kd = null;
////		         KillBillPersonal kp = null;
//		         KillBillDocumentHttps kdh=null;
//		         KillBillPersonalHttps kph=null;
//		         try {
////		        	 kd=new KillBillDocument();
////			         kp=new KillBillPersonal();
//		        	 kdh=new KillBillDocumentHttps(request);
//		        	 kph=new KillBillPersonalHttps(request);
//				} catch (Exception e) {
//					// TODO: handle exception
////					kd.close();
////					kp.close();
//					LogSystem.error(request, "System timout");
//					 result.put("result", "06");
//	     			 result.put("status", status);
//	     			 result.put("notif", "System timeout, mohon menunggu 10 menit kemudian");
//	     			 context.put("trxjson", result.toString());
//	     			LogSystem.info(request, result.toString());
//	     			 return;
//				}
//		         
//		         Mitra mitra=dList.get(0).getDocument().getEeuser().getMitra();
//		         int jmlttd=dList.size();
//		         String inv=null;
//		         InvoiceDao idao=new InvoiceDao(db);
//		         if(dList.get(0).getDocument().getPayment()=='2') {
//		        	 //bs=new BillingSystem(dList.get(0).getDocument().getEeuser());
//		        	 //balance=kp.getBalance("MT"+mitra.getId());
//		        	 JSONObject resp=kph.getBalance("MT"+mitra.getId(), request);
//				  		if(resp.has("data")) {
//				  			JSONObject data=resp.getJSONObject("data");
//							balance = data.getInt("amount");
//				  		} else {
//				  			result.put("result", "FE");
//							result.put("notif", "System timeout. silahkan coba kembali.");
//							return;
//				  		}
//				  		
//		        	 if(balance<jmlttd) {
////		        		 kd.close();
////		        		 kp.close();
//		        		 
//		        		 result.put("result", "61");
//		     			 result.put("status", status);
//		     			 result.put("notif", "balance ttd mitra tidak cukup");
//		     			 context.put("trxjson", result.toString());
//		     			LogSystem.info(request, result.toString());
//		     			 return;
//		        	 }
//		        	 
////		        	 inv=kp.setTransaction("MT"+mitra.getId(), jmlttd);
//		        	 JSONObject obj=kph.setTransaction("MT"+mitra.getId(), 1);
//					  if(obj.has("result")) {
//						  String hasil=obj.getString("result");
//						  if(hasil.equals("00")) {
//								inv=obj.getString("invoiceid");
//								
//							} else {
//					  			result.put("result", "FE");
//								result.put("notif", "System timeout. silahkan coba kembali.");
//								return;
//							}
//					  } else {
//						  
//				  			result.put("result", "FE");
//							result.put("notif", "System timeout. silahkan coba kembali.");
//							return;
//						
//					  }
//		        	 //String[] split=inv.split(" ");
//		        	 
//		        	 id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
//					  ivc.setDatetime(new Date());
//					  ivc.setAmount(jmlttd);
//					  ivc.setEeuser(dList.get(0).getDocument().getEeuser());
//					  ivc.setExternal_key("MT"+mitra.getId());
//					  ivc.setTenant('1');
//					  ivc.setTrx('2');
//					  ivc.setKb_invoice(inv);
//					  ivc.setDocument(dList.get(0).getDocument());
//					  idao.create(ivc);
//					  
//					 //inv=split[1];
//					  
//		        	 if(inv!=null) {
//		        		 
//		        		 cekBulkDocMitraV_session sd=new cekBulkDocMitraV_session();
//		        		 for(DocumentAccess da:dList) {
//		        			 try {
//		        				 sign=sd.signDoc(user, da, inv, db);
//		        				 if(sign==false) {
//		        					result.put("result", "06");
//					     			result.put("status", status);
//					     			result.put("notif", "gagal proses tandatangan");
////					     			kp.reverseTransaction(inv);
//					     			kph.reverseTransaction(inv, 1);
//					     			idao.deleteWhere(inv);
//					     			context.put("trxjson", result.toString());
//					     			LogSystem.info(request, result.toString());
////					     			kp.close();
////					     			kd.close();
//					        		return;
//		        				 }
//							} catch (Exception e) {
//								// TODO: handle exception
////								kd.close();
////								kp.close();
//								result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "gagal proses tandatangan");
////				     			kp.reverseTransaction(inv);
//				     			kph.reverseTransaction(inv, 1);
//				     			idao.deleteWhere(inv);
//				     			sign=false;
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
//				        		return;
//							}
//		        			  
//		        		 }
//		        		 
//		        	 }
////		        	 kd.close();
////		        	 kp.close();
//		        	 
//		         } else if(dList.get(0).getDocument().getPayment()=='3'){
////		        	 balance=kd.getBalance("MT"+mitra.getId());
//		        	 
//					  
//					  //List<DocumentAccess> ld=dao.findByDocSign(dList.get(0).getDocument().getId());
//					  List<id.co.keriss.consolidate.ee.Invoice> li=idao.findByDoc(dList.get(0).getDocument().getId());
//					  //if(ld.size()==0) {
//					  if(li.size()==0) {
//						  //inv=kd.setTransaction("MT"+mitra.getId(), 1);
//						  JSONObject resp=kdh.getBalance("MT"+mitra.getId(), request);
//					  		if(resp.has("data")) {
//					  			JSONObject data=resp.getJSONObject("data");
//								balance = data.getInt("amount");
//					  		} else {
//					  			result.put("result", "FE");
//								result.put("notif", "System timeout. silahkan coba kembali.");
//								return;
//					  		}
//			        	 LogSystem.info(request, "Balance dokumen = "+balance);
//			        	 //LogSystem.info(request,"balance dokumen = "+balance);
//						  if(balance<1) {
////							  kd.close();
////							  kp.close();
//							  	result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "balance doc mitra tidak cukup");
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
//				     			return;
//						  }
//						  
//						  JSONObject obj=kdh.setTransaction("MT"+mitra.getId(), 1);
//						  if(obj.has("result")) {
//							  String hasil=obj.getString("result");
//							  if(hasil.equals("00")) {
//									inv=obj.getString("invoiceid");
//									
//								} else {
//						  			result.put("result", "FE");
//									result.put("notif", "System timeout. silahkan coba kembali.");
//									return;
//								}
//						  } else {
//							  
//					  			result.put("result", "FE");
//								result.put("notif", "System timeout. silahkan coba kembali.");
//								return;
//							
//						  }
//						  
////						  String[] split=inv.split(" ");
////						  inv=split[1];
//						  
//						  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
//						  ivc.setDatetime(new Date());
//						  ivc.setAmount(jmlttd);
//						  ivc.setEeuser(dList.get(0).getDocument().getEeuser());
//						  ivc.setExternal_key("MT"+mitra.getId());
//						  ivc.setTenant('2');
//						  ivc.setTrx('2');
//						  ivc.setKb_invoice(inv);
//						  ivc.setDocument(dList.get(0).getDocument());
//						  idao.create(ivc);
//						  
//						  if(inv==null) {
//							  	result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "gagal proses tandatangan [bill]");
////				     			kd.reverseTransaction(inv);
//				     			kdh.reverseTransaction(inv, 1);
//				     			idao.deleteWhere(inv);
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
////				     			kd.close();
////				     			kp.close();
//				        		return;
//						  }
//					  }
//					  
//					 cekBulkDocMitraV_session sd=new cekBulkDocMitraV_session();
//	        		 for(DocumentAccess da:dList) {
//	        			 try {
//	        				 sign=sd.signDoc(user, da, inv, db);
//	        				 if(sign==false) {
//	        					result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "gagal proses tandatangan");
//				     			if(inv!=null) {
////				     				kd.reverseTransaction(inv);
//				     				kdh.reverseTransaction(inv, 1);
//				     				idao.deleteWhere(inv);
//				     			}
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
////				     			kd.close();
////				     			kp.close();
//				        		return;
//	        				 }
//						} catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//							result.put("result", "06");
//			     			result.put("status", status);
//			     			result.put("notif", "gagal proses tandatangan");
//			     			if(inv!=null) {
////			     				kd.reverseTransaction(inv);
//			     				kdh.reverseTransaction(inv, 1);
//			     				idao.deleteWhere(inv);
//			     			}
//			        		sign=false;
//			        		context.put("trxjson", result.toString());
//			        		LogSystem.info(request, result.toString());
////			        		kd.close();
////			        		kp.close();
//			     			return;
//						}
//	        			  
//	        		 }
////	        		 kd.close();
////	        		 kp.close();
//		         }
//		         
//		         Long jml=(long) 1;
//		         if(sign==true) {
//		        	rc="00";
//		        	status=00;
//		        	info="Proses tanda tangan berhasil!";
//		        	cc.setStatus("yes");
//		        	ccd.update(cc);
//		        	DocumentAccess docac=dList.get(0);
//		        	String iddoc=String.valueOf(docac.getDocument().getId());
//		        	String namadoc=docac.getDocument().getFile_name();
//		        	String namattd=docac.getName();
//		        	String jkttd=String.valueOf(docac.getEeuser().getUserdata().getJk());
//		        	String docs ="";
//					String link ="";
//					documentIDa =docac.getDocument().getId();
//					mitraid =docac.getDocument().getEeuser().getMitra().getId();
//					
//					result.put("document_id", docac.getDocument().getIdMitra());
//					//result untuk status document
//					if(rc.equals("00")) {
//						JSONArray lWaiting= new JSONArray();
//						JSONArray lSigned= new JSONArray();
//						JSONArray lInitials= new JSONArray();
//						JSONArray lInitialsWaiting= new JSONArray();
//						id_dokumen = iddoc;
//						email_user = docac.getEeuser().getNick();
//						for(DocumentAccess doc:dList) {
//							
//							
// 		 					JSONObject docObj=new JSONObject();
// 							/*change to eeuser*/
// 		// 					User userDoc=new UserManager(db).findByUserID(String.valueOf(doc.getUserdata().getId()));
// 		
// 		 					User userDoc=doc.getEeuser();
// 		 					if(userDoc==null) {
// 								docObj.put("name", doc.getName());
// 								docObj.put("email", doc.getEmail());
// 		
// 		 					}else {
//	 							docObj.put("name", userDoc.getName());
//	 							docObj.put("email", userDoc.getNick());
// 		 					}
// 		 					if(doc.getType().equals("sign") && !doc.isFlag()) {
// 		 						lWaiting.put(docObj);
// 		 							
// 		 					}if(doc.getType().equals("sign") && doc.isFlag()) {
// 		 						lSigned.put(docObj);
// 		 					}
// 		 					
// 		 					if(doc.getType().equals("initials") && !doc.isFlag()) {
// 		 						lInitialsWaiting.put(docObj);
// 		 					}
// 		 					
// 		 					if(doc.getType().equals("initials") && doc.isFlag()) {
// 		 						lInitials.put(docObj);
// 		 					}
// 		 				}
// 		 				
// 		 				if(lWaiting!=null && lWaiting.length()>0) {
// 		 					//result.put("waiting", lWaiting);
// 		 					result.put("status_document", "waiting");
// 		 					result.put("status", "22");
// 		 					status_doc = "waiting";
// 		 					
// 		 				}
// 		 				if(lSigned!=null && lSigned.length()>0) {
// 		 					//result.put("signed", lSigned);
// 		 					result.put("status", "00");
// 		 					if(lWaiting==null || lWaiting.length()==0) {
// 		 						result.put("status_document", "complete");
// 		 						status_doc = "complete";
// 		 					}
// 		 				}
// 		 				
// 		 				
// 		 				if(lInitialsWaiting!=null && lInitialsWaiting.length()>0) {
// 		 					//result.put("waiting", lInitialsWaiting);
// 		 					result.put("status_document", "waiting");
// 		 					result.put("status", "22");
// 		 					status_doc = "waiting";
// 		 				}
// 		 				
// 		 				if(lInitials!=null && lInitials.length()>0) {
// 		 					//result.put("initials", lInitials);
// 		 					result.put("status", "00");
// 		 					if(lInitialsWaiting==null || lInitialsWaiting.length()==0) {
// 		 						result.put("status_document", "complete");
// 		 						status_doc = "complete";
// 		 					}
// 		 				}
//					}
//					//END result untuk status document
//					
//					try {
//						   docs = AESEncryption.encryptDoc(iddoc);
//						   link = "https://"+DSAPI.DOMAIN+"/doc/source.html?frmProcess=viewFile&doc="+iddoc;
//						     //+ URLEncoder.encode(docs, "UTF-8");
//						} catch (Exception e1) {
//						   // TODO Auto-generated catch block
//						   e1.printStackTrace();
//						}
//					
//		        	SendNotifSignDoc std=new SendNotifSignDoc();
//		        	List<DocumentAccess> vda=dao.findByDoc(iddoc);
//		        	Vector<String> checkmail = new Vector();
//		        	
//		        	
//		        	try {
//			        	if (rc.equals("00")) {
//			        		LogSystem.info(request, "TESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
//							String Final = "\"result\""+":"+"\""+rc+"\"";
//							String the_status = "\"status\""+":"+"\""+status+"\"";
//							String notifikasi = "\"notif\""+":"+"\""+info+"\"";
//							String status_akhir = "\"status_document\""+":"+"\""+status_doc+"\"";
//							String dokumen_id = "\"document_id\""+":"+"\""+docac.getDocument().getIdMitra()+"\"";
//							String email_us = "\"email_user\""+":"+"\""+email_user+"\"";
//							String successresult = "{"+dokumen_id+","+the_status+","+status_akhir+","+Final+","+notifikasi+","+email_us+"}";
//				        	UserManager euser=new UserManager(getDB(context));
//				        	AESEncryption AES = new AESEncryption();
//				        	long documentIDs = documentIDa;
//				        	Documents eeuseruserids = euser.findOwnereeuserByDocument(documentIDa);
//							boolean redirect = eeuseruserids.isRedirect();
//							TokenMitra tm = euser.findTokenMitra(mitraid);
//							String aes_key = tm.getAes_key();
//							LogSystem.info(request, "CEK REDIRECT (TRUE/FALSE) :"+redirect);
//							
//							//long iduser = docac.getEeuser().getId();
//					        String id_session=object.getString("id_session");
//					        String id_key =object.getString("key_session");
//					    	SigningSessionDao sessions = new SigningSessionDao(db);
//					    	SigningSession ses = sessions.findSession(id_session,id_key);
//					    	LogSystem.info(request, "Before :"+ses.isUsed());
//					    	ses.setUsed(true);
//					    	db.session().update(ses);
//					    	LogSystem.info(request, "After :"+ses.isUsed());
//							String encryptresult = AES.encryptKeyAndResultRedirect(aes_key,successresult);
//							String encoderencrypt = URLEncoder.encode(AESEncryption.encryptKeyAndResultRedirect(aes_key,successresult),"UTF-8");
//							LogSystem.info(request, "Sukses DI encrypt :"+encryptresult);
//							LogSystem.info(request, "Sukses DI encode encrypt :"+encoderencrypt);
//							String decryptresult = AES.decryptKeyAndResultRedirect(aes_key,encryptresult);
//							LogSystem.info(request, "Sukses DI decrypt :"+decryptresult);
//							LogSystem.info(request, "Apakah Redirect ? (True/False) :"+redirect);
//							String alamat_redirect = mitra.getSigning_redirect();
//							
//							if (redirect == true && alamat_redirect != null) {
//								String concat = alamat_redirect+"?msg="+encoderencrypt;
//								LogSystem.info(request, "LINK :"+concat);
//								result.put("link", concat);
//							}
//							/////////////////////////EMAIL JIKA SUKSES/////////////////////////////
//							jml=dao.getWaitingSignUserByDoc(idDoc);
//					        if(jml==0) {
//					        		Documents doc=dList.get(0).getDocument();
//					        		doc.setSign(true);
//					        		new DocumentsDao(db).update(doc);
//					        	}
//								for(DocumentAccess da:vda) {
//					        		boolean kirim=true;
//					        		if(da.getEeuser()!=null) {
//					        			
//					        			for(String mail:checkmail) {
//					        				if(mail.equalsIgnoreCase(da.getEmail())) {
//					        					//std.kirim(da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link);
//					        					kirim=false;
//					        					break;
//					        				}
//					        			}
//					        			if(kirim==true) {
//					        				//if(mitra.isNotifikasi()) {
//				        					LogSystem.info(request, "Kirim email notif sign");
//				        					LogSystem.info(request, "Nama di eeuser" + da.getName());
//				        					LogSystem.info(request, "Tanggal kirim dokumen : " + da.getDocument().getWaktu_buat());
//				        					//da.getName()
////				        					std.kirim(da.getDate_sign(), user.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
//				        					std.kirim(da.getDocument().getWaktu_buat(), da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
//						        			
//				        					checkmail.add(da.getEmail());
//						        			//}
//					        			}
//					        			
//					        		} else {
//					        			for(String mail:checkmail) {
//					        				if(mail.equalsIgnoreCase(da.getEmail())) {
//					        					//std.kirim(da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link);
//					        					kirim=false;
//					        					break;
//					        				}
//					        			}
//					        			if(kirim==true) {
//					        				//if(mitra.isNotifikasi()) {
//					        				LogSystem.info(request, "Kirim email notif sign");
//					        				LogSystem.info(request, "Nama di eeuser" + user.getName());
//					        				LogSystem.info(request, "Tanggal kirim dokumen : " + da.getDocument().getWaktu_buat());
//					        			
//					        				//da.getName()
//				
////				        					std.kirim(da.getDate_sign(), user.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
//					        				std.kirim(da.getDocument().getWaktu_buat(), da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));			        			
//					        				checkmail.add(da.getEmail());
//					        				//}
//					        			}
//					        			//
//					        		}
//					        		
//					        	}
//								result.put("result", "00");
//				     			result.put("status", "00");
//				     			result.put("notif", "Proses tanda tangan berhasil!");
//				        		
//							///////////////
//			        	}
//		        	} catch (Exception e) {
//						// TODO: handle exception
//						e.printStackTrace();
//		        	}
//		         }
//		         
//		         
//		         /*
//		         int blc=bs.getBalance();
//
//		         JSONArray arr=object.getJSONArray("user");
//		         int jmlTTD=0;
//		         for(DocumentAccess documentAccess: dList) {
//		        	 for(int i=0; i<arr.length();i++) {
//		        		 JSONObject d=(JSONObject) arr.get(i);
//		        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
//	        				
//	        				 jmlTTD++;
//		        		 }
//		        	 }
//			         
//		         }
//		         
//		         LogSystem.info(request,"Jumlah ttd = "+jmlTTD);
//		         if(blc-jmlTTD>=0) {
//		         
//			         for(DocumentAccess documentAccess: dList) {
//			        	 for(int i=0; i<arr.length();i++) {
//			        		 JSONObject d=(JSONObject) arr.get(i);
//			        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
//		        				 Deposit dep=new Deposit(documentAccess,db);
//		
//		        				 if(dep.transaksi(1)) {
//		        					 boolean res=signDoc(user, documentAccess,dep.getBillSys().getLastInvoice(),db);
//				        			 if(res) {
//				        				 signPrc=true;
//				        			 }else {
//				        				 dep.reversal();
//				        				 result.put("info", "gagal tanda tangan, coba ulangi beberapa saat lagi");
//				        			 }
//			        			 }else{
//			        				 info="transaksi gagal, coba ulangi beberapa saat lagi";
//			        				 
//			        				 if(!dep.getLastError().equals("error")) {
//			        					 info=dep.getLastError();
//			        				 }
//		        					 result.put("info", info);
//		        					 
//			        			 }
//		        				 
//			        		 }
//			        	 }
//				         
//			         }
//		         }
//		         else {
//		        	 
//					 result.put("info", "saldo anda kurang, silakan topup terlebih dahulu");
//					 LogSystem.info(request,"saldo anda kurang");
//					 rc="E1";
//
//		         }
//		         
//		         if(signPrc) {
//		        	  
//				      boolean sign = true ;
//				      List<DocumentAccess> list = new DocumentsAccessDao(db).findByDoc(idDoc);
//				      for (DocumentAccess doa : list) {
//				    	  
//				    	 if(doa.getEeuser()!=null) {
//
//							 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getEeuser().getUserdata(), doa.getEmail());
//							 mail.run();
//				    	 }else {
//				    		 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getName(), doa.getEmail());
//							 mail.run();
//				    	 }
//						 if(doa.getType().equals("sign") && doa.isFlag()==false) {
//							sign=false; 
//						 }
//				       }
//				      
//				
//		         
//			        Documents docx=list.get(0).getDocument();
//			        if(sign==true) {
//			        	docx.setSign(true);
//			        	status=1;
//			        }
//		    		new DocumentsDao(db).update(docx);
//		    		
//		    		
//			        rc="00";
//			        cc.setStatus("yes");
//			        ccd.update(cc);
//		         }
//		         */
//	         }
	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
            e.printStackTrace();
		}
		
		
        context.getResponse().setContentType("application/json");

        //LogSystem.info(request,"SEND :"+result.toString());
        try {
			LogSystem.info(request, "SEND :"+result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
//	private boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db, HttpServletRequest req, String refTrx ) {
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//		Date date = new Date();
//		String strDate = sdfDate.format(date);
//		String tanggal = ftanggal.format(date);
//		String signdoc = "DSSG" + strDate + ".pdf";
//		String path = doc.getDocument().getPath();
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
//		String original = doc.getDocument().getSigndoc();
//		Documents dos = doc.getDocument();
//		boolean sod = dos.isRedirect();
//		try {
//			DocumentSigner dSign=new DocumentSigner();
//			
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
//				return true;
//			}
//			//						
////			LogSystem.info(request,"Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
//			
//		} catch (Exception e) {
//			LogSystem.error(getClass(), e);
//		}
//		return false;


//	}
	
//	class MailSender implements Runnable{
//
//		private String to;
//		private Userdata penerimaemail;
//		private Userdata action;
//		private String doc;
//		private String penerimaEmailNotReg;
//		
//		public MailSender(String doc,Userdata action ,Userdata penerimaemail,String to) {
//			this.to=to;
//			this.penerimaemail=penerimaemail;
//			this.action=action;
//			this.doc=doc;
//		}
//		public MailSender(String doc,Userdata action ,String penerimaEmailNotReg,String to) {
//			this.to=to;
//			this.penerimaEmailNotReg=penerimaEmailNotReg;
//			this.action=action;
//			this.doc=doc;
//		}
//		@Override
//		public void run() {
//			if(penerimaemail!=null)
//				new SendMailSSL().sendMailNotifSign(doc,action,penerimaemail, to);
//			else
//				new SendMailSSL().sendMailNotifSign(doc,action,penerimaEmailNotReg, to);
//
//		}
//		
//	}
	
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
