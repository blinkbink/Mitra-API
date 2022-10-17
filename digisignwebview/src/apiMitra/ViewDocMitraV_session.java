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
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.dao.SigningSessionDao;
import id.co.keriss.consolidate.dao.DocSigningSessionDao;

import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
import java.net.URLEncoder;



public class ViewDocMitraV_session extends ActionSupport implements DSAPI {
	
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
		String refTrx="VPAGE"+sdfDate2.format(tgl).toString()+"/D"+code;
		
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        String namaUser = "";
        //boolean otp = false;
        boolean valid=true;
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
		String CATEGORY = "OPEN";
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
	 
	         UserManager um=new UserManager(db);
        	 user=um.findByEmail(object.getString("usersign"));
        	 
        	 email_req = user.getNick();
        	 if(user!=null) {
		         String idDoc=object.getString("idDoc");
	        	 List<DocumentAccess> docac = dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
        	 }
	         
	         ConfirmCode cc = null;
	         ConfirmCodeDao ccd = new ConfirmCodeDao(db);
	         EeuserMitraDao emdao=new EeuserMitraDao(db);
	         
	         if(valid==true) {
	        	 String idDoc=object.getString("idDoc");
	        	 result.put("result", "00");
		         rc="00";
		         info="Dokumen Telah Dibaca dan Disetujui!";
		         result.put("notif", "Dokumen Telah Dibaca dan Disetujui!");
	        	 //CEK READ
		         List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
		         LogSystem.info(request, "jumlah list = "+dList.size(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		         DocumentAccess docac=dList.get(0);
		         String iddoc=String.valueOf(docac.getDocument().getId());
		         Mitra mitra=dList.get(0).getDocument().getEeuser().getMitra();
		         mitraid = mitra.getId();
		         documentIDa =docac.getDocument().getId();
		         email_user = docac.getEeuser().getNick();
	        	 try {
			        	if (rc.equals("00")) {
			        		org.hibernate.Transaction tx = db.session().beginTransaction();
							String Final = "\"result\""+":"+"\""+rc+"\"";
							String viewnya = "\"view_only\""+": true";
							String notifikasi = "\"notif\""+":"+"\""+info+"\"";
							String dokumen_id = "\"document_id\""+":"+"\""+docac.getDocument().getIdMitra()+"\"";
							String email_us = "\"email_user\""+":"+"\""+email_user+"\"";
							String successresult = "{"+dokumen_id+","+viewnya+","+Final+","+notifikasi+","+email_us+"}";
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
					    	LogSystem.info(request, "Before :"+ses.isUsed(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					    	ses.setUsed(true);
					    	db.session().update(ses);
					    	LogSystem.info(request, "After :"+ses.isUsed(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							String encryptresult = AES.encryptKeyAndResultRedirect(aes_key,successresult);
							String encoderencrypt = URLEncoder.encode(AESEncryption.encryptKeyAndResultRedirect(aes_key,successresult),"UTF-8");
							LogSystem.info(request, "Sukses DI encrypt :"+encryptresult, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							LogSystem.info(request, "Sukses DI encode encrypt :"+encoderencrypt, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							String decryptresult = AES.decryptKeyAndResultRedirect(aes_key,encryptresult);
							LogSystem.info(request, "Sukses DI decrypt :"+decryptresult, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							LogSystem.info(request, "Apakah Redirect ? (True/False) :"+redirect, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
							String view_redirect = mitra.getView_redirect();
							
							if (redirect == true && view_redirect != null) {
								String concat = view_redirect+"?msg="+encoderencrypt;
								LogSystem.info(request, "LINK :"+concat, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
								result.put("link", concat);
							}
							DocSigningSessionDao DSS = new DocSigningSessionDao(db);
					    	DocSigningSession SDS = DSS.findSession1(id_session,documentIDa);
					    	LogSystem.info(request, "Read Change From :"+SDS.isRead(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
					    	SDS.setRead(true);
					    	db.session().update(SDS);
					    	tx.commit();
					    	LogSystem.info(request, "Read Change Into :"+SDS.isRead(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        	}
			        	
	        	 }
	        	 catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
		         }
	         }
	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
            e.printStackTrace();
		}
		
		
		try {
			result.put("result", rc);
//			result.put("status", status);
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
