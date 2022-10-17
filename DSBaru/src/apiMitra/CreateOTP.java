package apiMitra;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;



public class CreateOTP extends ActionSupport {
	User userRecv;
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
		String status="gagal";
		JSONObject result=new JSONObject();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="OTP"+sdfDate2.format(tgl).toString();
		String kelas="apiMitra.CreateOTP";
		String trxType="REQ-OTP";
		
		try{
			
			 String process=request.getRequestURI().split("/")[2];
			 LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
	         //System.out.println("PATH :"+request.getRequestURI());
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

//	         System.out.println("RECEIVE :"+sb.toString());
//	         log.info("RECEIVE :"+sb.toString());
	         LogSystem.request(request, sb.toString(), kelas, refTrx, trxType);
	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         LogSystem.info(request, sb.toString(), kelas, refTrx, trxType);
	         
	         JSONArray jarr=object.getJSONArray("user");
	         Long idDocAcc = null;
	         for(int i = 0; i < jarr.length(); i++) {
	        	    JSONObject obj = jarr.getJSONObject(i);
	        	    idDocAcc=obj.getLong("idAccess");
	        	}
	         
	         DocumentsAccessDao dad = new DocumentsAccessDao(db);
	         ConfirmCodeDao ccd=new ConfirmCodeDao(db);
	         DocumentAccess docAcc=dad.findbyId(idDocAcc);
	         user = docAcc.getEeuser();
	         String nohp = user.getUserdata().getNo_handphone();
	         
	         Long idmitra = user.getUserdata().getMitra().getId();

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
			         sms.sendingPostRequest(code, nohp, idmitra,request, refTrx, trxType);
		         }
		         rc="00";
		         status="sending otp";
	         } catch (Exception e) {
	        	 LogSystem.error(request, e.getMessage());
	        	result.put("result", "05");
	 			//result.put("status", "sending otp");
	         }
	         
	        	         
		}catch (Exception e) {
			LogSystem.error(request, e.getMessage());
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
            e.printStackTrace();
		}
		
		
		try {
			result.put("result", rc);
			result.put("status", status);
		} catch (JSONException e) {
			LogSystem.error(request, e.getMessage());
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
		}
        context.getResponse().setContentType("application/json");

        System.out.println("SEND :"+result.toString());
		context.put("trxjson", result.toString());
		LogSystem.response(request, result, kelas, refTrx, trxType);
		 HttpSession session          = context.getSession();
	        
        session.removeAttribute (USER);

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
