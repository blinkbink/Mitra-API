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
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
import api.email.SendNotifSignDoc;
import api.email.SendSuccessGenerateCertificate;
import api.log.ActivityLog;



public class SignDocMitraV extends ActionSupport implements DSAPI {
	User userRecv;
	String kelas="apiMitra.SignDocMitraV";
	String trxType="PRC-SGN";
	String refTrx="";
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        boolean otp = false;
        boolean valid=false;
        String info="";
		int count = 21;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		int status=0;
		JSONObject result=new JSONObject();
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		DocumentsDao docao=new DocumentsDao(db);
		try{
			
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI());
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         LogSystem.request(request);
	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         if(object.has("refTrx"))refTrx=object.getString("refTrx");
	         
	         LogSystem.request(request, object.toString(), kelas, refTrx, trxType);
	         LogSystem.info(request, sb.toString(), kelas, refTrx, trxType);
	         
	         UserManager um=new UserManager(db);
        	 user=um.findByEmail(object.getString("usersign"));
        	 String idDoc=object.getString("idDoc");
        	 if(user!=null) {
		         
	        	 List<DocumentAccess> docac = dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
	        	 if(docac.size()==0) {
	        		 System.out.println("size < 0");
	        		 docac = dao.findDocEEuserSigned(Long.valueOf(idDoc), user.getNick());
		        	 result.put("result", "00");
	     			 result.put("status", status);
	     			 result.put("notif", "Dokumen Sudah Ditandatangan");
	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
					 context.put("trxjson", result.toString());
					 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
	     			 return;

	        	 }
        	 }
	         
	         ConfirmCode cc = null;
	         ConfirmCodeDao ccd = new ConfirmCodeDao(db);
	         //UserManager um=new UserManager(db);
	         EeuserMitraDao emdao=new EeuserMitraDao(db);
	         if(object.getString("userpwd")!=null && object.getString("usersign")!=null && object.getString("otp")!=null) {
	        	 //user=checkUser(object.getString("userpwd"), object.getString("usersign"), db);
	        	 user=um.findByUsernamePassword(object.getString("usersign"), object.getString("userpwd"));
	        	 //EeuserMitra em=emdao.findUserPwdMitra(object.getString("usersign"), object.getString("userpwd"), Long.valueOf(object.getString("mitra")));
	        	 //if(em!=null)user=em.getEeuser();
	        	 if(user!=null) {
	        		 System.out.println("User valid");
	        		 //check otp
	        		 cc = ccd.getLastOTP(user.getId());
	        		 if(cc!=null) {
	        			 if(cc.getCode().equals(object.getString("otp"))) {
	        				 otp=true;
		        			 valid=true;
	        			 }
	        			 else {
	        				 LogSystem.error(request, "OTP tidak valid");
	        				 //System.out.println("Otp tidak valid");
		        			 result.put("result", "12");
		        			 result.put("notif", "kode OTP tidak valid");
		        			 context.put("trxjson", result.toString());
		        			 LogSystem.info(request, result.toString());
		        			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
		        			 
		        			 try {
		 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
		 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif"), Long.toString(user.getId()), idDoc, null, "password", "otp",null);
		 			        }catch(Exception e)
		 			        {
		 			        	e.printStackTrace();
		 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
		 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
		 			        }
		        			 
		        			 return;
	        			 }
	        			 
	        		 }
	        		 else {
	        			 LogSystem.error(request, "OTP tidak valid");
	        			 //System.out.println("Otp tidak valid");
	        			 result.put("result", "12");
	        			 result.put("notif", "kode OTP tidak valid");
	        			 context.put("trxjson", result.toString());
	        			 LogSystem.info(request, result.toString());
	        			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
	        			 
	        			 try {
		 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
		 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif"), Long.toString(user.getId()), idDoc, null, "password", "otp",null);
		 			        }catch(Exception e)
		 			        {
		 			        	e.printStackTrace();
		 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
		 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
		 			        }
	        			 
	        			 return;
	        		 }
	        		 valid=true;
	        	 }
	        	 else {
	        		 LogSystem.error(request, "Password salah",kelas, refTrx, trxType);
	        		 //System.out.println("user tidak valid");
	        		 result.put("result", "12");
        			 result.put("notif", "Password salah");
        			 context.put("trxjson", result.toString());
        			 LogSystem.info(request, result.toString());
        			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
        			 
        			 try {
	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
	 			        }catch(Exception e)
	 			        {
	 			        	e.printStackTrace();
	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
	 			        	LogSystem.info(request, e.toString(),kelas, refTrx, trxType);
	 			        }
        			 
        			 return;
	        	 }
	         }
	         
	         Documents doc=docao.findById(Long.valueOf(idDoc));
	         if(doc.isProses()) {
	        	 Long lastProses=doc.getLast_proses().getTime()/1000L;
	        	 Long skrg=new Date().getTime()/1000L;
	        	 Long selisih=skrg-lastProses;
	        	 LogSystem.info(request, "last = "+lastProses+"|"+skrg+"|"+selisih, kelas, refTrx, trxType);
	        	 if(selisih<300) {
	        		 result.put("result", "06");
	    			 result.put("notif", "Mohon menunggu beberapa saat lagi, document masih dalam proses tandatangan.");
	    			 context.put("trxjson", result.toString());
	    			 //LogSystem.info(request, result.toString());
	    			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
	    			 return;
	        	 } else {
	        		doc.setLast_proses(new Date());
	        		docao.update(doc);
	        	 }
	        	 
	         } else {
	        	 doc.setProses(true);
	        	 doc.setLast_proses(new Date());
	        	 docao.update(doc);
	         }
	         
	         SignDoc sd=new SignDoc();
	         String version = null;
	         if(valid==true) {
	        	 
		         //String idDoc=object.getString("idDoc");

		         //List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getId());
		         List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
		         
		         if(dList.size() == 0)
		         {
		        	 List<DocumentAccess> docac = dao.findDocEEuserSigned(Long.valueOf(idDoc), user.getNick());
		        	 result.put("result", "00");
	     			 result.put("status", status);
	     			 result.put("notif", "Dokumen Sudah Ditandatangan");
	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
					 context.put("trxjson", result.toString());
					 
					 doc.setProses(false);
					 docao.update(doc);
					 
					 LogSystem.info(request, result.toString());
					 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
	     			 return;
		         }
		         
		         List<DocumentAccess> docAccMeteraiList = dao.findDocMeteraiSignApp(Long.valueOf(object.getString("idDoc")));
	        	 
	        	 if(docAccMeteraiList.size() > 0)
	        	 {
		        	 DocumentAccess docAccMeterai = docAccMeteraiList.get(0);
		        	 
		        	 if(docAccMeterai != null)
		        	 {
		        		 if(!docAccMeterai.isFlag())
		        		 {
		        			LogSystem.info(request, "Terdapat meterai, lanjut proses meterai", kelas, refTrx, trxType);
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
							
							SaveFileWithSamba samba=new SaveFileWithSamba();
							byte[] encoded = Base64.encode(samba.openfile(path+OriginPath));
							String base64Document = new String(encoded, StandardCharsets.US_ASCII);
							
							float llx = Float.parseFloat(dac.getLx());
							float lly = Float.parseFloat(dac.getLy());
							float urx = Float.parseFloat(dac.getRx());
							float ury = Float.parseFloat(dac.getRy());
							
							JSONObject resSign = null;
							DocumentSigner ds = new DocumentSigner();
							try {
								resSign = ds.kirimMeterai(request, refTrx, id_doc.getId(), id_doc.getFile_name(), id_doc.getEeuser().getMitra().getId(), llx, lly, urx, ury, dac.getPage(), base64Document, id_doc.getEeuser().getMitra().getProvinsi().getName());
								
							}catch(Exception e)
							{
								e.printStackTrace();
								// TODO: handle exception
								LogSystem.error(request, "Error save file meterai"+e.toString(), kelas, refTrx, trxType);
						  	  	result.put("result", "91");
								result.put("notif", "System timeout. silahkan coba kembali.");
							
								context.put("trxjson", result.toString());
								return;
							}
							
							if(resSign == null)
							{
								LogSystem.info(request, "Response meterai null", kelas, refTrx, trxType);
						  	  	result.put("result", "91");
								result.put("notif", "System timeout. silahkan coba kembali.");
								
								context.put("trxjson", result.toString());
								return;
							}
							
							LogSystem.info(request, "Response meterai : "+resSign.getString("result") + " information " + resSign.getString("information"), kelas, refTrx, trxType);

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
								    LogSystem.info(request, "hasil save File : "+resp, kelas, refTrx, trxType);
								    if(resp==false) {
								  	  	LogSystem.error(request, "error samba", kelas, refTrx, trxType);
								  	  	result.put("result", "91");
										result.put("notif", "System timeout. silahkan coba kembali.");
										
										context.put("trxjson", result.toString());
										return;
								    }
								    finalDoc = null;
								} catch (Exception e) {
									e.printStackTrace();
									// TODO: handle exception
									LogSystem.error(request, "Error save file meterai"+e.toString(), kelas, refTrx, trxType);
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
									
									LogSystem.info(request, "Proses meterai berhasil ", kelas, refTrx, trxType);
								} catch (Exception e) {
									// TODO: handle exception
									LogSystem.info(request, "DB Timeout", kelas, refTrx, trxType);
							  	  	result.put("result", "91");
									result.put("notif", "System timeout. silahkan coba kembali.");
								
									context.put("trxjson", result.toString());
									return;
								}
							}
							else
							{
								LogSystem.info(request, "Response Meterai tidak berhasil ", kelas, refTrx, trxType);
						  	  	result.put("result", "91");
								result.put("notif", "System timeout. silahkan coba kembali.");
								
								context.put("trxjson", result.toString());
								return;
							}
		        		 }
		        		 else
		        		 {
		        			 LogSystem.info(request, "Meterai pada dokumen ini sudah diproses", kelas, refTrx, trxType);
		        		 }
		        	 }
	        	 }
		         
		         
		         if(object.has("cert"))
		         {
		        	 if(object.has("checkme"))
			         {
			        	 LogSystem.info(request, "Tombol Menyetujui Penandatangan Dokumen ini ? "+object.getBoolean("checkme"), kelas, refTrx, trxType);
			        	 if (!object.getBoolean("checkme"))
			        	 {
		        			 result.put("result", "12");
	            			 result.put("notif", "gagal proses tandatangan");
	            			 context.put("trxjson", result.toString());
	            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
	            			 
	            			 try {
	 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
	 	 			        }catch(Exception e)
	 	 			        {
	 	 			        	e.printStackTrace();
	 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
	 	 			        	LogSystem.info(request, e.toString(),kelas, refTrx, trxType);
	 	 			        }
	            			 
	            			 return;
			        	 }
			         }
		        	 
		        	 if(!object.getString("cert").equals(""))
		        	 {
			        	 LogSystem.info(request, "Proses generate sertifikat", kelas, refTrx, trxType);
			        	 if(!object.getString("cert").equals("00"))
			        	 {
			        		 LogSystem.info(request, "Hasil cek sertifikat dari depan = " + object.getString("cert"), kelas, refTrx, trxType);
			        		 LogSystem.info(request, "Hasil dari depan bukan 00, cek lagi sertifikatnya", kelas, refTrx, trxType);
			        		 KmsService kms = new KmsService(request, "KMS Service/"+refTrx);
			        		 JSONObject cert = new JSONObject();
			        		 String mitra = null;

			        		 LogSystem.info(request, "USERRR : " + user.getNick(), kelas, refTrx, trxType);
			        		 LogSystem.info(request, "Cek mitra : " + user.getMitra(), kelas, refTrx, trxType);
		    				 if(user.getMitra() != null)
		    				 {
		    					 LogSystem.info(request, "Mitra tidak null", kelas, refTrx, trxType);
		    					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
		    					 mitra = user.getMitra().getId().toString();
		    				 }
		    				 else 
		    				 {
		    					 LogSystem.info(request, "Mitra null", kelas, refTrx, trxType);
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
		    				 
		    				 if(cert.getString("result").length() > 3 || cert.getString("result").equals(""))
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
			        		 LogSystem.info(request, "Response cek sertifikat = " + cert.toString(), kelas, refTrx, trxType);
			        		 if(object.getString("cert").equals("04"))
			        		 {
			        			 LogSystem.info(request, "Tombol pencabutan sertifikat elektronik lama dan penerbitan sertifikat elektronik baru ? " + renewal, kelas, refTrx, trxType);
			        		 }
			        		 else
			        		 {
			        			 LogSystem.info(request, "Tombol Renewal ? " + renewal, kelas, refTrx, trxType);
			        		 }
			        		 
			        		 if (cert.getString("result").equals("G1") || cert.getString("result").equals("05") || (object.getString("cert").equals("03") && renewal) || (object.getString("cert").equals("04") && renewal))
			        		 {
			        			 JSONObject genCert= new JSONObject();
			        			 try {
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
				    		        		 LogSystem.info(request, genCertInfo + " "+object.getBoolean("genCert"), kelas, refTrx, trxType);
				    		        		 
				    		        		 if (!object.getBoolean("genCert"))
				    		        		 {
				    		        			 result.put("result", "12");
				    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
				    	            			 context.put("trxjson", result.toString());
				    	            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
				    	            			 
				    	            			 try {
				    	 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
				    	 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
				    	 	 			        }catch(Exception e)
				    	 	 			        {
				    	 	 			        	e.printStackTrace();
				    	 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
				    	 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
				    	 	 			        }
				    	            			 return;
				    		        		 }
				    		        	 }
				        				 else
				        				 {
				        					 LogSystem.info(request, "Proses generate sertifikat, tapi tidak ada tombol setuju membuat sertifikat", kelas, refTrx, trxType);
				        					 result.put("result", "12");
			    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			    	            			 context.put("trxjson", result.toString());
			    	            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
			    	            			 
			    	            			 try {
			    	 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			    	 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
			    	 	 			        }catch(Exception e)
			    	 	 			        {
			    	 	 			        	e.printStackTrace();
			    	 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
			    	 	 			        	LogSystem.error(request, e.toString());
			    	 	 			        }
			    	            			 
			    	            			 return;
				        				 }
				    		        	 
				    		        	 if (object.has("termCondition"))
				    		        	 {
				    		        		 LogSystem.info(request, "Tombol Saya Telah Membaca dan Menyetujui Kebijakan Privasi, Beserta Perjanjian Kepemilikan Sertifikat Elektronik Digisign yang Berlaku ? "+object.getBoolean("termCondition"), kelas, refTrx, trxType);
				    		        		 if (!object.getBoolean("termCondition"))
				    		        		 {
				    		        			 result.put("result", "12");
				    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
				    	            			 context.put("trxjson", result.toString());
				    	            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
				    	            			 
				    	            			 try {
				    	 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
				    	 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
				    	 	 			        }catch(Exception e)
				    	 	 			        {
				    	 	 			        	e.printStackTrace();
				    	 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
				    	 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
				    	 	 			        }
				    	            			 
				    	            			 return;
				    		        		 }
				    		        	 }
				    		        	 else
				        				 {
				        					 LogSystem.info(request, "Proses generate sertifikat, tapi tidak ada tombol menyetujui syarat kebijakan privasi", kelas, refTrx, trxType);
				        					 result.put("result", "12");
			    	            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			    	            			 context.put("trxjson", result.toString());
			    	            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
			    	            			 
			    	            			 try {
			    	 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			    	 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
			    	 	 			        }catch(Exception e)
			    	 	 			        {
			    	 	 			        	e.printStackTrace();
			    	 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
			    	 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
			    	 	 			        }
			    	            			 
			    	            			 return;
				        				 }
				    		        	 
				    		        	 genCert = kms.createSertifikat(user.getId(), renewal, user.getUserdata().getLevel());
			        					 
			        					 if (genCert.getString("result").equals("00"))
					        			 {
					        				 version = "v3";
//					        				 LogSystem.info(request, "Sukses generate sertifikat, kirim email ke " + object.getString("usersign"),kelas, refTrx, trxType);
//					        				 
//					        				 SendSuccessGenerateCertificate notifGenCert = new SendSuccessGenerateCertificate();
//					        				 if(renewal && object.getString("cert").equals("03"))
//					        				 {
//					        					 notifGenCert.kirimPerpanjang(user.getName(), user.getUserdata().getJk(), user.getNick(), "", mitra);
//					        				 }
//					        				 else
//					        				 {
//					        					 if(!object.getString("cert").equals("04") && !object.getString("cert").equals("G1"))
//					        					 {
//					        						 notifGenCert.kirimBaru(user.getName(), user.getUserdata().getJk(), user.getNick(), "", mitra);
//					        					 }
//					        				 }
		//
//					        				 LogSystem.info(request, "Selesai mengirim email", kelas, refTrx, trxType);		        				 
					        			 }
					        			 else if (genCert.getString("result").equals("06") || (genCert.getString("result").equals("05") && genCert.getString("info").equals("certificate is already exist")))
					        			 {
					        				 version = "v3";
					        				 LogSystem.info(request, "Sertifikat sudah ada", kelas, refTrx, trxType);
					        			 }
					        			 else
					        			 {
					        				 LogSystem.info(request, "Gagal generate sertifikat " + genCert,kelas, refTrx, trxType);
					    	        		 result.put("result", "12");
					            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
					            			 context.put("trxjson", result.toString());
					            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
					            			 
					            			 try {
					 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
					 	 			        }catch(Exception e)
					 	 			        {
					 	 			        	e.printStackTrace();
					 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
					 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
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
			            	             LogSystem.info(request, "Jarak ke waktu expired : " + day, kelas, refTrx, trxType);
			            	             
			        					 if(cert.has("needRegenerate"))
			        					 {
			        						 if(!cert.getBoolean("needRegenerate") && day > 30 && cert.getString("keyVersion").equals("3.0"))
			        						 {
			        							 LogSystem.info(request, "result 00"+cert.getBoolean("needRegenerate")+" " + day +" " + cert.getString("keyVersion") + ", tidak perlu renewal", kelas, refTrx, trxType);
			        							 renewal = false;
			        							 LogSystem.info(request, "Buat sertifikat ? " + renewal, kelas, refTrx, trxType);
			        						 }
			        					 }
			        					 else
			        					 {
				        					 if(day > 30 && cert.getString("keyVersion").equals("3.0"))
								        	 {
				        						 LogSystem.info(request, "result 00 " + day + " " + cert.getString("keyVersion") + ", tidak perlu renewal", kelas, refTrx, trxType);
			        							 renewal = false;
			        							 LogSystem.info(request, "Buat sertifikat ? " + renewal, kelas, refTrx, trxType);
								        	 }
			        					 }
			        				 }

			        				 if(renewal)
			        				 {
			        					 genCert = kms.createSertifikat(user.getId(), renewal, user.getUserdata().getLevel());
			        					 
			        					 if (genCert.getString("result").equals("00"))
					        			 {
					        				 version = "v3";
//					        				 LogSystem.info(request, "Sukses generate sertifikat, kirim email ke " + object.getString("usersign"),kelas, refTrx, trxType);
//					        				 
//					        				 SendSuccessGenerateCertificate notifGenCert = new SendSuccessGenerateCertificate();
//					        				 if(renewal && object.getString("cert").equals("03"))
//					        				 {
//					        					 notifGenCert.kirimPerpanjang(user.getName(), user.getUserdata().getJk(), user.getNick(), "", mitra);
//					        				 }
//					        				 else
//					        				 {
//					        					 if(!object.getString("cert").equals("04") && !object.getString("cert").equals("G1"))
//					        					 {
//					        						 notifGenCert.kirimBaru(user.getName(), user.getUserdata().getJk(), user.getNick(), "", mitra);
//					        					 }
//					        				 }
		//
//					        				 LogSystem.info(request, "Selesai mengirim email", kelas, refTrx, trxType);		        				 
					        			 }
					        			 else if (genCert.getString("result").equals("06") || (genCert.getString("result").equals("05") && genCert.getString("info").equals("certificate is already exist")))
					        			 {
					        				 version = "v3";
					        				 LogSystem.info(request, "Sertifikat sudah ada", kelas, refTrx, trxType);
					        			 }
					        			 else
					        			 {
					        				 LogSystem.info(request, "Gagal generate sertifikat " + genCert,kelas, refTrx, trxType);
					    	        		 result.put("result", "12");
					            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
					            			 context.put("trxjson", result.toString());
					            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
					            			 
					            			 try {
					 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
					 	 			        }catch(Exception e)
					 	 			        {
					 	 			        	e.printStackTrace();
					 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
					 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
					 	 			        }
					            			 
					            			 return;
					        			 }
			        				 }
			        			 }
			        			 catch(Exception e)
			        			 {
			        				 result.put("result", "12");
			            			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
			            			 context.put("trxjson", result.toString());
			            			 LogSystem.info(request, result.toString());
			            			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
			            			 LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
			            			 try {
			 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
			 	 			        }catch(Exception q)
			 	 			        {
			 	 			        	q.printStackTrace();
			 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
			 	 			        	LogSystem.error(request, q.toString(),kelas, refTrx, trxType);
			 	 			        }
			            			 
			            			 return;
			        			 }
			        			 
			        			 
			        		 }
			        		 	 
			        		 if(cert.getString("result").equals("06") || cert.getString("result").equals("07"))
			        		 {
			        			 LogSystem.info(request, "Sertifikat expired/revoke",kelas, refTrx, trxType);
			        			 result.put("result", "12");
		            			 result.put("notif", "gagal proses tandatangan, sertifikat elektronik kedaluwarsa atau dicabut");
		            			 context.put("trxjson", result.toString());
		            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
		            			 
		            			 try {
		 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
		 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
		 	 			        }catch(Exception e)
		 	 			        {
		 	 			        	e.printStackTrace();
		 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
		 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
		 	 			        }
		            			 
		            			 return;
			        		 }
			        	 }
			        	 else
			        	 {
			        		 LogSystem.info(request, "Hasil cek sertifikat dari depan = " + object.getString("cert"), kelas, refTrx, trxType);
			        		 LogSystem.info(request, "Hasil dari depan 00, cek versi sertifikatnya", kelas, refTrx, trxType);
			        		 KmsService kms = new KmsService(request, "KMS Service/"+refTrx);
			        		 JSONObject cert = new JSONObject();
			        		 String mitra = null;

			        		 LogSystem.info(request, "USERRR : " + user.getNick(), kelas, refTrx, trxType);
			        		 LogSystem.info(request, "Cek mitra : " + user.getMitra(), kelas, refTrx, trxType);
		    				 if(user.getMitra() != null)
		    				 {
		    					 LogSystem.info(request, "Mitra tidak null", kelas, refTrx, trxType);
		    					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
		    					 mitra = user.getMitra().getId().toString();
		    				 }
		    				 else 
		    				 {
		    					 LogSystem.info(request, "Mitra null", kelas, refTrx, trxType);
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
		    				 
		    				 if(cert.getString("result").length() > 3 || cert.getString("result").equals(""))
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
			        	 }
		        	 }
		        	 else
		        	 {
		        		 LogSystem.info(request, "Gagal generate sertifikat. Status sertifikat null dari depan", kelas, refTrx, trxType);
    	        		 result.put("result", "12");
            			 result.put("notif", "gagal proses tandatangan");
            			 context.put("trxjson", result.toString());
            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
            			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
            			 
            			 try {
 	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
 	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
 	 			        }catch(Exception e)
 	 			        {
 	 			        	e.printStackTrace();
 	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
 	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
 	 			        }
            			 
            			 return;
		        	 }
		         }
		         else
	        	 {
	        		 LogSystem.info(request, "Gagal generate sertifikat. Tidak ada data cert sertifikat null dari depan", kelas, refTrx, trxType);
	        		 result.put("result", "12");
        			 result.put("notif", "Proses penerbitan sertifikat elektronik gagal. Silakan coba kembali");
        			 context.put("trxjson", result.toString());
        			 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
        			 LogSystem.info(request, result.toString(),kelas, refTrx, trxType);
        			 
        			 try {
	 			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	 			        	logSystem.POST("signing", "failed", "[API] Gagal tandatangan dokumen, " + result.getString("notif") + " " + object.getString("usersign") , null, idDoc, null, "password", "otp",null);
	 			        }catch(Exception e)
	 			        {
	 			        	e.printStackTrace();
	 			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
	 			        	LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
	 			        }
        			 
        			 return;
	        	 }

		         LogSystem.info(request, "Jumlah list = " + dList.size());
		         
		         boolean sign=false;
		         boolean signPrc=false;
		         BillingSystem bs;
		         int balance=0;
//		         KillBillDocument kd = null;
//		         KillBillPersonal kp = null;
		         KillBillDocumentHttps kdh=null;
		         KillBillPersonalHttps kph=null;
		         try {
//		        	 kd=new KillBillDocument();
//			         kp=new KillBillPersonal();
			         kdh=new KillBillDocumentHttps(request, refTrx);
		        	 kph=new KillBillPersonalHttps(request, refTrx);
				} catch (Exception e) {
					// TODO: handle exception
//					kp.close();
//					kd.close();
					LogSystem.error(request, "System timeout");
					 result.put("result", "06");
	     			 result.put("status", status);
	     			 result.put("notif", "System timeout, mohon menunggu 10 menit kemudian");
	     			 context.put("trxjson", result.toString());
	     			 
	     			 doc.setProses(false);
					 docao.update(doc);
	     			 
	     			LogSystem.info(request, result.toString());
	     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
	     			LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
	     			 return;
				}
		         
		         Mitra mitra=dList.get(0).getDocument().getEeuser().getMitra();
		         int jmlttd=dList.size();
		         String inv=null;
		         InvoiceDao idao=new InvoiceDao(db);
		         
		         if(doc.getPayment()=='2') {
		        	 //bs=new BillingSystem(dList.get(0).getDocument().getEeuser());
//		        	 balance=kp.getBalance("MT"+mitra.getId());
		        	 JSONObject resp=kph.getBalance("MT"+mitra.getId(), request);
				  		if(resp.has("data")) {
				  			JSONObject data=resp.getJSONObject("data");
							balance = data.getInt("amount");
				  		} else {
				  			result.put("result", "FE");
							result.put("notif", "System timeout. silahkan coba kembali.");
							LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
							
							doc.setProses(false);
							docao.update(doc);
							return;
				  		}
		        	 if(balance<jmlttd) {
//		        		 kp.close();
//		        		 kd.close();
		        		 LogSystem.info(request, "Balance ttd mitra tidak cukup");
		        		 result.put("result", "61");
		     			 result.put("status", status);
		     			 result.put("notif", "balance ttd mitra tidak cukup");
		     			 context.put("trxjson", result.toString());
		     			LogSystem.info(request, result.toString());
		     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
		     			
		     			 doc.setProses(false);
						 docao.update(doc);
						 
		     			 return;
		        	 }
		        	 
//		        	 inv=kp.setTransaction("MT"+mitra.getId(), jmlttd);
		        	 JSONObject obj=kph.setTransaction("MT"+mitra.getId(), jmlttd, idDoc);
					  if(obj.has("result")) {
						  String hasil=obj.getString("result");
						  if(hasil.equals("00")) {
								inv=obj.getString("invoiceid");
								balance=obj.getInt("current_balance");
								
								id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
								ivc.setDatetime(new Date());
								ivc.setAmount(jmlttd);
								ivc.setEeuser(doc.getEeuser());
								ivc.setExternal_key("MT"+mitra.getId());
								ivc.setTenant('1');
								ivc.setTrx('2');
								ivc.setKb_invoice(inv);
								ivc.setDocument(doc);
								ivc.setCur_balance(balance);
								idao.create(ivc);
							} else {
					  			result.put("result", "FE");
								result.put("notif", "System timeout. silahkan coba kembali.");
								LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
								doc.setProses(false);
								docao.update(doc);
								return;
							}
					  } else {
						  
				  			result.put("result", "FE");
							result.put("notif", "System timeout. silahkan coba kembali.");
							LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
							doc.setProses(false);
							docao.update(doc);
							return;
					  }
		        	 
					  
		        	 if(inv!=null) {
		        		 
		        		 //SignDoc sd=new SignDoc();
		        		 //DocumentsDao docao=new DocumentsDao(db);
		        		 //Documents doc=dList.get(0).getDocument();
		        		 String pathFile=doc.getSigndoc();
		        		 try {
		        			 sign=sd.signDoc2(user, doc, inv, db, refTrx, request, dList, version);
						 } catch (Exception e) {
							// TODO: handle exception
							sign=false;
						 }
		        		 
		        		 if(sign==false) {
	        					
	        					result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses tandatangan");
//				     			kp.reverseTransaction(inv);
				     			
								
				     			JSONObject jrev=kph.reverseTransaction(inv, jmlttd, idDoc);
				     			if(jrev.has("result")) {
				     				LogSystem.info(request, "response reversal = "+jrev.toString(), kelas, refTrx, trxType);
				     				if(jrev.getString("result").equals("00")) {
				     					Invoice invo=new Invoice();
										invo.setAmount(jmlttd);
										invo.setDatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jrev.getString("datetime")));
										invo.setDocument(doc);
										invo.setExternal_key("MT"+mitra.getId());
										invo.setKb_invoice(inv);
										invo.setTenant('1');
										invo.setTrx('3');
										invo.setCur_balance(jrev.getInt("current_balance"));
										//doc.setSigndoc(pathFile);
										try {
											idao.create(invo);
											//docao.update(doc);
											//DocumentsAccessDao dad=new DocumentsAccessDao(db);
											dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
										} catch (Exception e) {
											// TODO: handle exception
											//doc.setSigndoc(pathFile);
											//docao.update(doc);
											//DocumentsAccessDao dad=new DocumentsAccessDao(db);
											//dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
											LogSystem.info(request, "Gagal save invoice reversal", kelas, refTrx, trxType);
											LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
										}
				     				} else {
				     					LogSystem.info(request, "Gagal reversal", kelas, refTrx, trxType);
				     				}
				     				
				     				
				     			} else {
				     				doc.setSigndoc(pathFile);
									docao.update(doc);
				     				//DocumentsAccessDao dad=new DocumentsAccessDao(db);
									dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
									
						  			result.put("result", "FE");
									result.put("notif", "System timeout. silahkan coba kembali.");
									LogSystem.info(request, "Gagal reversal", kelas, refTrx, trxType);
									LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
									doc.setProses(false);
									docao.update(doc);
									return;
				     			}
				     			//idao.deleteWhere(inv);
								
				     			context.put("trxjson", result.toString());
				     			LogSystem.info(request, result.toString());
				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
				     			doc.setProses(false);
				     			doc.setSigndoc(pathFile);
								docao.update(doc);
//				     			kd.close();
//	        					kp.close();
				        		return;
	        			 } 
		        		 
		        		 
		        		 /*
		        		 for(DocumentAccess da:dList) {
		        			 try {
		        				 sign=sd.signDoc(user, da, inv, db, refTrx, request);
		        				 if(sign==false) {
		        					
		        					result.put("result", "06");
					     			result.put("status", status);
					     			result.put("notif", "gagal proses tandatangan");
//					     			kp.reverseTransaction(inv);
					     			
									
					     			JSONObject jrev=kph.reverseTransaction(inv, jmlttd, idDoc);
					     			if(jrev.has("result")) {
					     				LogSystem.info(request, "response reversal = "+jrev.toString(), kelas, refTrx, trxType);
					     				//if(jrev.getString("result").equals("00")) {
					     					Invoice invo=new Invoice();
											invo.setAmount(jmlttd);
											invo.setDatetime(new Date());
											invo.setDocument(da.getDocument());
											invo.setExternal_key("MT"+mitra.getId());
											invo.setKb_invoice(inv);
											invo.setTenant('1');
											invo.setTrx('3');
											invo.setCur_balance(jrev.getInt("current_balance"));
											//doc.setSigndoc(pathFile);
											try {
												idao.create(invo);
												//docao.update(doc);
												//DocumentsAccessDao dad=new DocumentsAccessDao(db);
												dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
											} catch (Exception e) {
												// TODO: handle exception
												//doc.setSigndoc(pathFile);
												//docao.update(doc);
												//DocumentsAccessDao dad=new DocumentsAccessDao(db);
												//dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
												LogSystem.info(request, "Gagal save invoice reversal", kelas, refTrx, trxType);
											}
					     				//}
					     			} else {
					     				doc.setSigndoc(pathFile);
										docao.update(doc);
					     				//DocumentsAccessDao dad=new DocumentsAccessDao(db);
										dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
										
							  			result.put("result", "FE");
										result.put("notif", "System timeout. silahkan coba kembali.");
										LogSystem.info(request, "Gagal reversal", kelas, refTrx, trxType);
										LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
										doc.setProses(false);
										docao.update(doc);
										return;
					     			}
					     			//idao.deleteWhere(inv);
									
					     			context.put("trxjson", result.toString());
					     			LogSystem.info(request, result.toString());
					     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
					     			doc.setProses(false);
					     			doc.setSigndoc(pathFile);
									docao.update(doc);
//					     			kd.close();
//		        					kp.close();
					        		return;
		        				 }
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
//								kd.close();
//								kp.close();
								result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses tandatangan");
//				     			kp.reverseTransaction(inv);
				     			JSONObject jrev=kph.reverseTransaction(inv, jmlttd, idDoc);
				     			if(jrev.has("result")) {
				     				LogSystem.info(request, "response reversal = "+jrev.toString(), kelas, refTrx, trxType);
				     				//if(jrev.getString("result").equals("00")) {
				     					Invoice invo=new Invoice();
										invo.setAmount(jmlttd);
										invo.setDatetime(new Date());
										invo.setDocument(da.getDocument());
										invo.setExternal_key("MT"+mitra.getId());
										invo.setKb_invoice(inv);
										invo.setTenant('1');
										invo.setTrx('3');
										invo.setCur_balance(jrev.getInt("current_balance"));
										
										try {
											idao.create(invo);
											//docao.update(doc);
											
											//DocumentsAccessDao dad=new DocumentsAccessDao(db);
											dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
										} catch (Exception e2) {
											// TODO: handle exception
											//doc.setSigndoc(pathFile);
											//docao.update(doc);
											//DocumentsAccessDao dad=new DocumentsAccessDao(db);
											//dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
											LogSystem.info(request, "Gagal save invoice reversal", kelas, refTrx, trxType);
										}
				     				//}
				     			} else {
				     				doc.setSigndoc(pathFile);
									docao.update(doc);
									//DocumentsAccessDao dad=new DocumentsAccessDao(db);
									dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
									
						  			result.put("result", "FE");
									result.put("notif", "System timeout. silahkan coba kembali.");
									LogSystem.info(request, "Gagal reversal", kelas, refTrx, trxType);
									LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
									doc.setProses(false);
									docao.update(doc);
									return;
				     			}
				     			//idao.deleteWhere(inv);
				     			
								
				     			sign=false;
				     			context.put("trxjson", result.toString());
				     			LogSystem.info(request, result.toString());
				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
				     			doc.setSigndoc(pathFile);
				     			doc.setProses(false);
								docao.update(doc);
				        		return;
							}
		        			  
		        		 } */
		        		 
		        	 } else {
		        		 	result.put("result", "91");
							result.put("notif", "System timeout. silahkan coba kembali.");
							LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
							doc.setProses(false);
							docao.update(doc);
							return;
		        	 }
		        	 
//		        	 kd.close();
//		        	 kp.close();
		        	 
		         } else if(doc.getPayment()=='3'){
//		        	 balance=kd.getBalance("MT"+mitra.getId());
		        	 
					  //List<DocumentAccess> ld=dao.findByDocSign(dList.get(0).getDocument().getId());
					  //List<id.co.keriss.consolidate.ee.Invoice> li=idao.findByDoc(dList.get(0).getDocument().getId());
					 
					  //Documents docu=dList.get(0).getDocument();
					  List<DocumentAccess> lda=dao.findByDocFlagTrue(doc.getId());
					  String path=doc.getSigndoc();
					  boolean at=false;
					  
					  if(lda.size()==0) {
						  JSONObject response=kdh.getBalance("MT"+mitra.getId(), request);
					  		if(response.has("data")) {
					  			JSONObject data=response.getJSONObject("data");
								balance = data.getInt("amount");
					  		} else {
					  			result.put("result", "FE");
								result.put("notif", "System timeout. silahkan coba kembali.");
								LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
								doc.setProses(false);
								docao.update(doc);
								return;
					  		}
					  		LogSystem.info(request, "balance dokumen = "+balance, kelas, refTrx, trxType);
						  if(balance<1) {
//							  kd.close();
//							  kp.close();
							  LogSystem.info(request, "Balance doc mitra tidak cukup");
							  result.put("result", "06");
				     		  result.put("status", status);
				     		  result.put("notif", "balance doc mitra tidak cukup");
				     		  context.put("trxjson", result.toString());
				     		 LogSystem.info(request, result.toString());
				     		LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
				     		doc.setProses(false);
							docao.update(doc);
				     		  return;
						  }
						  
//						  inv=kd.setTransaction("MT"+mitra.getId(), 1);
						  JSONObject objek=kdh.setTransaction("MT"+mitra.getId(), 1, idDoc);
						  if(objek.has("result")) {
							  String hasil=objek.getString("result");
							  if(hasil.equals("00")) {
									inv=objek.getString("invoiceid");
									balance=objek.getInt("current_balance");
									
									id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
									ivc.setDatetime(new Date());
									ivc.setAmount(1);
									ivc.setEeuser(dList.get(0).getDocument().getEeuser());
									ivc.setExternal_key("MT"+mitra.getId());
									ivc.setTenant('2');
									ivc.setTrx('2');
									ivc.setKb_invoice(inv);
									ivc.setDocument(doc);
									ivc.setCur_balance(balance);
									idao.create(ivc);
								} else {
						  			result.put("result", "FE");
									result.put("notif", "System timeout. silahkan coba kembali.");
									LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
									doc.setProses(false);
									docao.update(doc);
									return;
								}
						  } else {
							  
					  			result.put("result", "FE");
								result.put("notif", "System timeout. silahkan coba kembali.");
								LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
								doc.setProses(false);
								docao.update(doc);
								return;
							
						  }
						  //String[] split=inv.split(" ");
						  //inv=split[1];
						  
						  
						  
						  if(inv==null) {
//							  kd.close();
//							  kp.close();
							  	result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses tandatangan [bill]");
//				     			kd.reverseTransaction(inv);
				     			//kdh.reverseTransaction(inv, 1);
				     			//idao.deleteWhere(inv);
				     			context.put("trxjson", result.toString());
				     			//LogSystem.info(request, result.toString());
				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
				     			doc.setProses(false);
								docao.update(doc);
				        		return;
						  }
					  } else {
						  inv=lda.get(0).getInvoice();
						  at=true;
					  }
					  
					  //proses sign
					  sign=sd.signDoc2(user, doc, inv, db, refTrx, request, dList, version);
					  if(sign==false) {
      					LogSystem.info(request, "reversal sign", kelas, refTrx, trxType); 
      					result.put("result", "06");
			     			result.put("status", status);
			     			result.put("notif", "gagal proses tandatangan, silahkan coba kembali");
			     			if(inv!=null && at==false) {
//			     				kd.reverseTransaction(inv);
			     				try {
			     					JSONObject job=kdh.reverseTransaction(inv, 1, idDoc);
//			     					if(job.has("result")) {
//			     						if(job.getString("result").equals("00")) {
			     							
						     				Invoice invo=new Invoice();
											invo.setAmount(1);
											invo.setDatetime(new Date());
											invo.setDocument(doc);
											invo.setExternal_key("MT"+mitra.getId());
											invo.setKb_invoice(inv);
											invo.setTenant('2');
											invo.setTrx('3');
											invo.setCur_balance(job.getInt("current_balance"));
											
											
											idao.create(invo);
											//docao.update(doc);
											
											LogSystem.info(request, "emailDoc = "+doc.getId()+user.getNick(), kelas, refTrx, trxType);
											//dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
//			     						}
//			     					}
								} catch (Exception e) {
									
									LogSystem.info(request, "create invoice reversal gagal", kelas, refTrx, trxType);
									LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
								}
			     				
			     				
			     			}
			     			context.put("trxjson", result.toString());
			     			LogSystem.info(request, result.toString());
			     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
			     			
			     			
			     			doc.setSigndoc(path);
			     			doc.setProses(false);
			     			
			     			try {
			     				docao.update(doc);
								dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
							} catch (Exception e) {
								// TODO: handle exception
								LogSystem.info(request, "Gagal update document, document_access : reversal", kelas, refTrx, trxType);
								LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
							}
			        		return;
      				 }
					  
					  
					 //SignDoc sd=new SignDoc();
					 /* 
	        		 for(DocumentAccess da:dList) {
	        			 try {
	        				 sign=sd.signDoc(user, da, inv, db, refTrx, request);
	        				 if(sign==false) {
	        					LogSystem.info(request, "reversal sign", kelas, refTrx, trxType); 
	        					result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses tandatangan, silahkan coba kembali");
				     			if(inv!=null && at==false) {
//				     				kd.reverseTransaction(inv);
				     				try {
				     					JSONObject job=kdh.reverseTransaction(inv, 1, idDoc);
//				     					if(job.has("result")) {
//				     						if(job.getString("result").equals("00")) {
				     							
							     				Invoice invo=new Invoice();
												invo.setAmount(1);
												invo.setDatetime(new Date());
												invo.setDocument(doc);
												invo.setExternal_key("MT"+mitra.getId());
												invo.setKb_invoice(inv);
												invo.setTenant('2');
												invo.setTrx('3');
												invo.setCur_balance(job.getInt("current_balance"));
												
												
												idao.create(invo);
												//docao.update(doc);
												
												LogSystem.info(request, "emailDoc = "+doc.getId()+user.getNick(), kelas, refTrx, trxType);
												//dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
//				     						}
//				     					}
									} catch (Exception e) {
										
										LogSystem.info(request, "create invoice reversal gagal", kelas, refTrx, trxType);
									}
				     				
				     				
				     			}
				     			context.put("trxjson", result.toString());
				     			LogSystem.info(request, result.toString());
				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
				     			
				     			
				     			doc.setSigndoc(path);
				     			doc.setProses(false);
				     			
				     			try {
				     				docao.update(doc);
									dao.updateWhereDocumentEmail(doc.getId(), user.getNick());
								} catch (Exception e) {
									// TODO: handle exception
									LogSystem.info(request, "Gagal update document, document_access : reversal", kelas, refTrx, trxType);
								}
								
				        		return;
	        				 }
						} catch (Exception e) {
							// TODO: handle exception
//							kd.close();
//							kp.close();
							LogSystem.info(request, "Gagal update document, document_access : reversal", kelas, refTrx, trxType);
							
							result.put("result", "06");
			     			result.put("status", status);
			     			result.put("notif", "gagal proses tandatangan, silahkan coba kembali.");
			     			return;
			     		}
	        			  
					}
					*/
//	        		 kd.close();
//	        		 kp.close();
		         }
		         
		         Long jml=(long) 1;
		         if(sign==true) {
		        	 rc="00";
		        	 status=00;
		        	 info="Proses tanda tangan berhasil!";
		        	 cc.setStatus("yes");
		        	 ccd.update(cc);
		        	 
		        	 //update proses false
		        	 doc.setProses(false);
					 docao.update(doc);
		        	 
		        	 jml=dao.getWaitingSignUserByDoc(idDoc);
			        	if(jml==0) {
			        		//Documents doc=dList.get(0).getDocument();
			        		doc.setSign(true);
			        		docao.update(doc);
			        	}
		        	 
		        	DocumentAccess docac=dList.get(0);
		        	String iddoc=String.valueOf(docac.getDocument().getId());
		        	String namadoc=docac.getDocument().getFile_name();
		        	String namattd=user.getName();
		        	String jkttd=String.valueOf(docac.getEeuser().getUserdata().getJk());
		        	String docs ="";
					String link ="";
					result.put("document_id", docac.getDocument().getIdMitra());
					if(jml==0) {
						result.put("status_document", "complete");
					} else {
						result.put("status_document", "waiting");
						
					}
					
					try {
						   docs = AESEncryption.encryptDoc(iddoc);
						   link = "https://"+DSAPI.DOMAIN+"/doc/source.html?frmProcess=viewFile&doc="+iddoc;
						     //+ URLEncoder.encode(docs, "UTF-8");
						} catch (Exception e1) {
						   // TODO Auto-generated catch block
						   e1.printStackTrace();
						   LogSystem.info(request, e1.toString(),kelas, refTrx, trxType);
						}
		        	SendNotifSignDoc std=new SendNotifSignDoc();
		        	List<DocumentAccess> vda=dao.findByDoc(iddoc);
		        	Vector<String> checkmail = new Vector();
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
		        				//if(mitra.isNotifikasi()) {
		        				//date
		        				//da.getName()
		        				LogSystem.info(request, "Kirim email notif sign", kelas, refTrx, trxType);
	        					LogSystem.info(request, "Nama di eeuser" + user.getName(), kelas, refTrx, trxType);
	        					std.kirim(sd.getTgl(), da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
	        					//std.kirim(da.getDate_sign(), da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
		        			
	        					checkmail.add(da.getEmail());
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
		        				LogSystem.info(request, "Kirim email notif sign", kelas, refTrx, trxType);
	        					LogSystem.info(request, "Nama di eeuser" + user.getName(), kelas, refTrx, trxType);
	        					std.kirim(sd.getTgl(), da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
	        					//std.kirim(da.getDate_sign(), da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
			        			checkmail.add(da.getEmail());
		        				//}
		        				
		        			}
		        			//
		        		}
		        		
		        	}
		        	
		        	try {
			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			        	logSystem.POST("signing", "success", "[API] Berhasil tandatangan dokumen", Long.toString(docac.getEeuser().getId()), Long.toString(docac.getDocument().getId()), null, "password", "otp",null);
			        }catch(Exception e)
			        {
			        	e.printStackTrace();
			        	LogSystem.info(request, e.toString(),kelas, refTrx, trxType);
			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
			        }
		         }
		         
	         }
	         
		}catch (Exception e) {
            LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
            e.printStackTrace();
		}
		
		
		try {
			
			result.put("result", rc);
			result.put("status", status);
			result.put("notif", info);
			LogSystem.info(request, result.toString());
			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
			
		}
        context.getResponse().setContentType("application/json");
        LogSystem.info(request, "SEND :"+result.toString());
        //System.out.println("SEND :"+result.toString());
		context.put("trxjson", result.toString());
		LogSystem.response(request, result, kelas, refTrx, trxType);
		 HttpSession session          = context.getSession();
	        
        session.removeAttribute (USER);
        return;

	}
	
	
}

