package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.SigningSessionDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SystemUtil;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;



public class CreateOTP extends ActionSupport implements DSAPI {
	User userRecv;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="COTP"+sdfDate2.format(tgl).toString(); 
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        boolean otp = false;
        boolean valid=false;
        String info="";
		int count = 21;
		User userbyemail = null;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		String status="gagal";
		JSONObject result=new JSONObject();
		JSONObject jo=new JSONObject();
		String path_app = this.getClass().getName();
		String CATEGORY = "OTP";
		String email_req ="";
		String mitra_req ="";
		try{
			
			 String process=request.getRequestURI().split("/")[2];
			 LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			 
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         LogSystem.info(request, sb.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "REQUEST", (System.currentTimeMillis() - start) / 1000f + "s");
	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         
	         refTrx = object.getString("refTrx");
	         
	         SigningSessionDao signingSessionDB = new SigningSessionDao(db);
	         SigningSession signingSession = new SigningSession();
	         signingSession = signingSessionDB.findSessionId(object.getString("id_session"));
	         
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
	         
	         JSONArray jarr=object.getJSONArray("user");
	         
	         Long idDocAcc = null;
	         for(int i = 0; i < jarr.length(); i++) {
	        	    JSONObject obj = jarr.getJSONObject(i);
	        	    idDocAcc=obj.getLong("idAccess");
	        	}
	         
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);
	         DocumentAccess docAcc=dad.findbyId(idDocAcc);
	         
	         UserManager umi = new UserManager(db);
        	 userbyemail = umi.findByEmail(object.getString("usersign"));
        	 
        	 if(userbyemail!=null) {
		         String idDoc=object.getString("idDoc");
	        	 List<DocumentAccess> docac = dad.findDocEEuserSigned(Long.valueOf(idDoc), userbyemail.getNick());
//	        	 System.out.println("Check Doc Access");
//	        	 System.out.println("Docu size : " + docac.size());
	        	 if(docac.size()==0) {	        		 
	        		 System.out.println("size 0");
	        		 docac = dad.findDocEEuserSigned(Long.valueOf(idDoc), userbyemail.getNick());
		        	 result.put("result", "06");
	     			 result.put("status", status);
	     			 result.put("notif", "Dokumen Sudah ditandatangani");
	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
					 boolean usingopt = docac.get(0).getDocument().getPath().startsWith("/opt");
						LogSystem.info(request, "useopt = " + usingopt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
						String optget = "";
						if (usingopt) {
							optget = AESEncryption.encryptDoc("optfile");
						}
					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
//					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8")+"&tp="+URLEncoder.encode(optget, "UTF-8")+"&ss="+URLEncoder.encode(SystemUtil.getExp(namafile), "UTF-8"));
					 context.put("trxjson", result.toString());
	     			 return;
	        	 }
        	 }
	         
	         user = docAcc.getEeuser();
	         if(user==null) {
	        	 user=userbyemail;
	         }
	         String nohp = user.getUserdata().getNo_handphone();
	         User userowner=docAcc.getDocument().getEeuser();
	         Long idmitra=null;
	         if(userowner.getMitra()!=null) {
	        	idmitra=userowner.getMitra().getId(); 
	         }
	         else {
	        	 idmitra=userowner.getUserdata().getMitra().getId();
	         }
//	         String nohp = null;
//	         if(user==null) {
//	        	 nohp = new UserManager(db).findByUsername(docAcc.getEmail()).getUserdata().getNo_handphone();
//	         } else {
//	        	 nohp = user.getUserdata().getNo_handphone();
//	         }
	         //Long idmitra = user.getUserdata().getMitra().getId();

	         try {
	        	 ConfirmCode ccode=new ConfirmCode();
	        	 Date tanggal = new Date();
		         ccode.setEeuser(user);
		         ccode.setStatus("no");
		         ccode.setWaktu_buat(tanggal);
		         ccode.setMsisdn(nohp);
		         Random rand=new Random();
		         int number = rand.nextInt(999999);
		         String code=String.format("%06d", number);
		         ccode.setCode(code);
		         LogSystem.info(request, "OTP Clicked to PhoneNum : "+nohp , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		         LogSystem.info(request, "OTP TIME : "+tanggal , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		         LogSystem.info(request, "OTP Code : "+code , refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		         ccd.create(ccode);
		         if(ccode.getId()>0) {
		        	 ConfirmSms sms = new ConfirmSms(request, refTrx);
			         JSONObject respsms = sms.sendingPostRequest(code, nohp, idmitra, mitra_req, email_req);
			         
			         if(respsms.getString("result").equals("OK"))
			         {
			        	 jo.put("rc", "00");
				         jo.put("notif", "Berhasil kirim OTP");
				         context.put("trxjson", jo.toString());
				         return;
			         }
			         else
			         {
		        		 LogSystem.error(request, "Error " + respsms.getString("info"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	 jo.put("rc", "05");
				         jo.put("notif", "Gagal kirim OTP");
				         context.put("trxjson", jo.toString());
				         
				         return;  
			         }
		         }
		         rc="00";
		         status="sending otp";
	         } catch (Exception e) {
	        	 LogSystem.error(getClass(), e);
	        	 jo.put("rc", "05");
		         jo.put("notif", "Gagal kirim OTP");
		         context.put("trxjson", jo.toString());
		         return;  
	         }
	         
	        	         
		}catch (Exception e) {
           
            try {
            	LogSystem.error(getClass(), e);
                jo.put("rc", "05");
				jo.put("notif", "Gagal kirim OTP");
				context.put("trxjson", jo.toString());
		        return;  
			} catch (JSONException e1) {
				LogSystem.error(getClass(), e1);
			}  
		}
		
		
//		try {
//			result.put("result", rc);
//			result.put("status", status);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			LogSystem.error(getClass(), e);
//		}
//        context.getResponse().setContentType("application/json");
//
//        System.out.println("SEND :"+result.toString());
//		context.put("trxjson", result.toString());
//		LogSystem.response(request, result);
//		 HttpSession session          = context.getSession();
//	        
//        session.removeAttribute (USER);

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
		if(user!=null) {			  
			  if(!user.getLogin().getPassword().equals(pwd)) {
				  user=null;  
			  }
		}
		return user;
	}
	
}
