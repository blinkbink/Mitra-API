package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JSAuth;
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


public class CheckDocSign extends ActionSupport implements DSAPI {
	User userRecv;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        boolean otp = false;
        boolean valid=false;
        String kelas="apiMitra.CheckDocSign";
        String typeTrx = "CHK-DOC-SGN";
        String info="";
		int count = 21;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		int status=0;
		JSONObject result=new JSONObject();
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		String refTrx="";
		Boolean wna = false;
		try{
			
			 String process=request.getRequestURI().split("/")[2];
	         System.out.println("PATH :"+request.getRequestURI());;
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

//	         System.out.println("RECEIVE :"+sb.toString());
//	         log.info("RECEIVE :"+sb.toString());
	         //LogSystem.request(request);
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
	         
	         
	         UserManager um=new UserManager(db);
        	 user=um.findByEmail(object.getString("usersign"));
        	 refTrx=object.getString("refTrx");
        	 if(user!=null) {
        		 
        		 if(user.getUserdata().getWn() == '2')
        		 {
        			 wna = true;
        		 }
        		 
        		 String idDoc=object.getString("idDoc");
	        	 //List<DocumentAccess> docac = dao.findDocEEuserSign(Long.valueOf(idDoc), user.getId());
	        	 List<DocumentAccess> docac = dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
	        	 
	        	 List<DocumentAccess> dam = dao.findDocAccessMeterai(Long.valueOf(idDoc));
	        	 
        		 if(user.getUserdata().getLevel().equals("C2"))
        		 {
        			 result.put("result", "08");
        			 result.put("info", "Tidak dapat melanjutkan tandatangan. Mohon menghubungi CS Digisign untuk informasi lebih lanjut.");
    				 context.put("trxjson", result.toString());
    				 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
    				 return;
        		 }
        		// Cek sertifikat user ke KMS
    			 KmsService kms = new KmsService(request, "KMS Service/"+refTrx);
    			 JSONObject cert = new JSONObject();

				 LogSystem.info(request, "Cek mitra : " + user.getUserdata().getMitra(), kelas, refTrx, typeTrx);
				 if(user.getMitra() != null)
				 {
					 LogSystem.info(request, "Mitra tidak null", kelas, refTrx, typeTrx);
					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
				 }
				 else 
				 {
					 LogSystem.info(request, "Mitra null", kelas, refTrx, typeTrx);
					 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), "");
				 }
    			 LogSystem.info(request, "Hasil cek sertifikat : " + cert, kelas, refTrx, typeTrx);
    			 
    			 if(cert.getString("result").equals("00"))
    	         {
    	        	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    	             String raw = cert.getString("expired-time");
    	             Date expired = sdf.parse(raw);
    	             Date now= new Date();
    	             Long day = TimeUnit.DAYS.convert(expired.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
    	             LogSystem.info(request, "Jarak ke waktu expired : " + day,kelas, refTrx, typeTrx);
   
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
			    				 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
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
		    				 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
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
		    				 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
		    				 return; 
			        	 }
		             }
    	         }
    			 
    			 if(cert.getString("result").length() > 3)
    			 {
    				 result.put("result", "09");
    				 result.put("info", "Tidak dapat melanjutkan tandatangan. Silahkan menghubungi CS Digisign.");
	        		 context.put("trxjson", result.toString());
	        		 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
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
    					 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik dan Menyetujui Penandatangan Dokumen ini");
    					 if(dam.size() > 0)
    					 {
    						 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik dan Menyetujui Menandatangani Dokumen yang dibubuhi Meterai Elektronik");
    					 }
    				 }
    				 
    				 if(cert.getString("result").equals("G1"))
    				 {
    					 result.put("infoNotif", "Seperti yang telah diinformasikan sebelumnya Pencabutan Sertifikat Massal Digisign telah dilakukan pada tanggal "+DSAPI.MASS_REVOKE+". Maka status Sertifikat Elektronik Anda telah dicabut secara otomatis pada tanggal tersebut. Sertifikat Elektronik baru Anda akan diterbitkan sebelum tandatangan diproses");
    					 result.put("signNotif", "Saya Menyetujui Penerbitan Sertifikat Elektronik baru dan Menyetujui Menandatangani Dokumen ini");
    					 if(dam.size() > 0)
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
		    					 LogSystem.info(request, "Mengirim email pendaftaran ulang", kelas, refTrx, typeTrx);
			        			 SendMailPendaftaranUlang mail = new SendMailPendaftaranUlang(request, refTrx);
			        			 mail.kirim(user.getUserdata().getNama(), Character.toString(user.getUserdata().getJk()), user.getNick(), String.valueOf(user.getId()), String.valueOf(docac.get(0).getDocument().getEeuser().getMitra().getId()));
			        		 }
			        		 catch(Exception e)
			        		 {
			        			 LogSystem.info(request, "Gagal mengirim email pendaftaran ulang");
			        			 LogSystem.info(request, e.toString(),kelas, refTrx, typeTrx);
			        		 }
    					 }
    					 else
    					 {
    						 LogSystem.info(request, "PENGGUNA WNA, TIDAK KIRIM EMAIL PENDAFTARAN ULANG",kelas, refTrx, typeTrx);
    					 }
    				 }
    				 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
	     			 return;
    			 }
		         
	        	 if(docac.size()>0) {
	        		 System.out.println("size > 0");
	        		 result.put("result", "05");
	        		 result.put("signing", true);
	        		 context.put("trxjson", result.toString());
	        		 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
	     			 return;
	        	 }else {
	        		 System.out.println("size < 0");
	        		 docac = dao.findDocEEuserSigned(Long.valueOf(idDoc), user.getNick());
		        	 result.put("result", "00");
	     			 result.put("status", status);
	     			 result.put("notif", "Dokumen Sudah ditandatangani");
	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
					 String docid = AESEncryption.encryptDoc(docac.get(0).getDocument().getId().toString());
					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8")+"&access="+URLEncoder.encode(docid, "UTF-8"));
					 
					 context.put("trxjson", result.toString());
					 LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
	     			 return;

	        	 }
        	 }

	         
		}catch (Exception e) {
            LogSystem.error(request, e.toString(),kelas, refTrx, typeTrx);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
            e.printStackTrace();
		}
		
		try {
			result.put("result", rc);
			result.put("status", status);
			result.put("notif", info);
			LogSystem.info(request, result.toString(),kelas, refTrx, typeTrx);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogSystem.error(request, e.toString(),kelas, refTrx, typeTrx);
		}
        context.getResponse().setContentType("application/json");

        System.out.println("SEND :"+result.toString());
		context.put("trxjson", result.toString());
		LogSystem.response(request, result);
		 HttpSession session          = context.getSession();
	        
        session.removeAttribute (USER);
	}
	
		
}
