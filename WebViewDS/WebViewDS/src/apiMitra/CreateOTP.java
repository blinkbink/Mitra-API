package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import java.net.URLEncoder;
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
	String kelas="apiMitra.CreateOTP";
	String trxType="GEN-OTP";
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
		User userbyemail = null;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		String status="gagal";
		JSONObject result=new JSONObject();
		JSONObject jo=new JSONObject();
		try{
			
			 String process=request.getRequestURI().split("/")[2];
			 LogSystem.info(request, "PATH :"+request.getRequestURI());
			 
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         JSONObject object=new JSONObject(sb.toString());
	         
	         if(object.has("refTrx"))refTrx=object.getString("refTrx");
	         LogSystem.request(request, object.toString(), kelas,refTrx, trxType);
	         //LogSystem.info(getClass(), sb.toString());
	         
	         JSONArray jarr=object.getJSONArray("user");
	         Long idDocAcc = null;
	         for(int i = 0; i < jarr.length(); i++) {
	        	    JSONObject obj = jarr.getJSONObject(i);
	        	    if(obj.has("key")) {
	        	    	idDocAcc=Long.decode(AESEncryption.decrypt(obj.getString("key")));
	        	    	LogSystem.info(request, "idAccess hasil decrypt : "+idDocAcc, kelas, refTrx, trxType);
	        	    } else {
	        	    	idDocAcc=obj.getLong("idAccess");
	        	    	LogSystem.info(request, "idAccess plantext : "+idDocAcc, kelas, refTrx, trxType);
	        	    }
	        	    
	        	}
	         
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);
	         DocumentAccess docAcc=dad.findbyId(idDocAcc);
	         
	         UserManager umi = new UserManager(db);
        	 userbyemail = umi.findByEmail(object.getString("usersign"));
        	 
        	 
        	 if(userbyemail!=null) {
		         String idDoc=object.getString("idDoc");
	        	 List<DocumentAccess> docac = dad.findDocEEuserSign(Long.valueOf(idDoc), userbyemail.getNick());
	        	 //System.out.println("Docu size : " + docac.size());
	        	 LogSystem.info(request, "doc access size = "+docac.size(), kelas, refTrx, trxType);
	        	 if(docac.size()==0) {	        		 
	        		 docac = dad.findDocEEuserSigned(Long.valueOf(idDoc), userbyemail.getNick());
		        	 result.put("result", "06");
	     			 result.put("status", status);
	     			 result.put("notif", "Dokumen Sudah ditandatangani");
	     			 LogSystem.info(request, "result : "+result.toString(), kelas, refTrx, trxType);
	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
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
		         ccode.setEeuser(user);
		         ccode.setStatus("no");
		         ccode.setWaktu_buat(new Date());
		         ccode.setMsisdn(nohp);
		         Random rand=new Random();
		         int number = rand.nextInt(999999);
		         String code=String.format("%06d", number);
		         ccode.setCode(code);
		         
		         ccd.create(ccode);
		         if(ccode.getId()>0) {
		        	 ConfirmSms sms = new ConfirmSms();
			         JSONObject respsms = sms.sendingPostRequest(code, nohp, idmitra, refTrx, request);
			         
			         if(respsms.getString("result").equals("OK"))
			         {
			        	 jo.put("rc", "00");
				         jo.put("notif", "Berhasil kirim OTP");
				         context.put("trxjson", jo.toString());
				         LogSystem.response(request, jo, kelas, refTrx, trxType);
				         return;
			         }
			         else
			         {
		        		 LogSystem.error(request, "Error " + respsms.getString("info"), kelas, refTrx, trxType);
			        	 jo.put("rc", "05");
				         jo.put("notif", "Gagal kirim OTP");
				         context.put("trxjson", jo.toString());
				         LogSystem.response(request, jo, kelas, refTrx, trxType);
				         return;  
			         }
		         }
		         rc="00";
		         status="sending otp";
	         } catch (Exception e) {
	        	 LogSystem.error(getClass(), e, kelas, refTrx, trxType);
	        	 jo.put("rc", "05");
		         jo.put("notif", "Gagal kirim OTP");
		         context.put("trxjson", jo.toString());
		         LogSystem.response(request, jo, kelas, refTrx, trxType);
		         return;  
	         }
	         
	        	         
		}catch (Exception e) {
           
            try {
            	LogSystem.error(getClass(), e, kelas, refTrx, trxType);
                jo.put("rc", "05");
				jo.put("notif", "Gagal kirim OTP");
				context.put("trxjson", jo.toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
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
		}
		@Override
		public void run() {
			if(penerimaemail!=null)
				new SendMailSSL().sendMailNotifSign(doc,action,penerimaemail, to);
			else
				new SendMailSSL().sendMailNotifSign(doc,action,penerimaEmailNotReg, to);

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
