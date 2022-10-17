//package id.co.keriss.consolidate.action.ajax;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.billing.BillingSystem;
//import id.co.keriss.consolidate.action.billing.Deposit;
//import id.co.keriss.consolidate.action.kms.DocumentSigner;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.KeyDao;
//import id.co.keriss.consolidate.dao.KeyV3Dao;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.JSAuth;
//import id.co.keriss.consolidate.ee.Key;
//import id.co.keriss.consolidate.ee.KeyV3;
//import id.co.keriss.consolidate.ee.Userdata;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.LogSystem;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//import com.ning.http.client.Request;
//
//
//
//public class SignService extends ActionSupport {
//	User userRecv;
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		DB db = getDB(context);
//        JSAuth auth=new JSAuth(db);
//        User user=null;
//		int count = 21;
//		HttpServletRequest  request  = context.getRequest();
//		String rc="05";
//		int status=0;
//		JSONObject result=new JSONObject();
//		try{
//			
//			 String process=request.getRequestURI().split("/")[2];
//	         System.out.println("PATH :"+request.getRequestURI());;
//	         StringBuilder sb = new StringBuilder();
//	         String s;
//	         while ((s = request.getReader().readLine()) != null) {
//	                sb.append(s);
//	         }
//
////	         System.out.println("RECEIVE :"+sb.toString());
////	         log.info("RECEIVE :"+sb.toString());
//	         LogSystem.request(request);
//	         LogSystem.info(getClass(), sb.toString());
//	         
//	         JSONObject object=new JSONObject(sb.toString());
//	         if(!auth.processAuth(object.getString("auth"))) {
//	        	 sendRedirect (context, context.getRequest().getContextPath() 
//	                     + "/stop.html"
//	                 );
//	         	return;
//	         }
//	         user=auth.getUser();
//	         DocumentsAccessDao dao=new DocumentsAccessDao(db);
//	         String idDoc=object.getString("idDoc");
//	         if(!idDoc.equals(auth.getIdDoc())) {
//	        	 sendRedirect (context, context.getRequest().getContextPath() 
//	                     + "/stop.html"
//	                 );
//	         	return;
//	         }
//	         
//
//	         List<DocumentAccess>dList=dao.findDocAccessEEuser(idDoc, String.valueOf(user.getId()) ,user.getNick());
//	         boolean signPrc=false;
//	         BillingSystem bs=new BillingSystem(user);
//	         int blc=bs.getBalance();
//
//	         JSONArray arr=object.getJSONArray("user");
//	         int jmlTTD=0;
//	         for(DocumentAccess documentAccess: dList) {
//	        	 for(int i=0; i<arr.length();i++) {
//	        		 JSONObject d=(JSONObject) arr.get(i);
//	        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
//        				
//        				 jmlTTD++;
//	        		 }
//	        	 }
//		         
//	         }
//	         
//	         if(blc-jmlTTD>=0) {
//	         
//		         for(DocumentAccess documentAccess: dList) {
//		        	 for(int i=0; i<arr.length();i++) {
//		        		 JSONObject d=(JSONObject) arr.get(i);
//		        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
//	        				 Deposit dep=new Deposit(documentAccess,db);
//	
//	        				 if(dep.transaksi(1)) {
//	        					 boolean res=signDoc(user, documentAccess,dep.getBillSys().getLastInvoice(),db, request);
//			        			 if(res) {
//			        				 signPrc=true;
//			        			 }else {
//			        				 dep.reversal();
//			        			 }
//		        			 }else{
//		        				 String info="transaksi gagal, coba ulangi beberapa saat lagi";
//		        				 
//		        				 if(!dep.getLastError().equals("error")) {
//		        					 info=dep.getLastError();
//		        				 }
//	        					 result.put("info", info);
//	
//		        			 }
//	        				 
//		        		 }
//		        	 }
//			         
//		         }
//	         }
//	         else {
//	        	 
//				 result.put("info", "saldo anda kurang, silakan topup terlebih dahulu");
//				 System.out.println("saldo anda kurang");
//				 rc="E1";
//
//	         }
//	         
//	         if(signPrc) {
//	        	  
//			      boolean sign = true ;
//			      List<DocumentAccess> list = new DocumentsAccessDao(db).findByDoc(idDoc);
//			      for (DocumentAccess doa : list) {
//			    	  
//			    	 if(doa.getEeuser()!=null) {
//
//						 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getEeuser().getUserdata(), doa.getEmail());
//						 mail.run();
//			    	 }else {
//			    		 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getName(), doa.getEmail());
//						 mail.run();
//			    	 }
//					 if(doa.getType().equals("sign") && doa.isFlag()==false) {
//						sign=false; 
//					 }
//			       }
//			      
//			
//	         
//		        Documents docx=list.get(0).getDocument();
//		        if(sign==true) {
//		        	docx.setSign(true);
//		        	status=1;
//		        }
//	    		new DocumentsDao(db).update(docx);
//	    		
//		        rc="00";
//	         }
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
////			error (context, e.getMessage());
////            context.getSyslog().error (e);
//            e.printStackTrace();
//		}
//		
//		
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
//
//	}
//	
//	
//	private boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db, HttpServletRequest request, String version) {
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//		Date date = new Date();
//		String strDate = sdfDate.format(date);
//		String tanggal = ftanggal.format(date);
//		String signdoc = "DSSG" + strDate + ".pdf";
//		String path = doc.getDocument().getPath();
//		String original = doc.getDocument().getSigndoc();
//		try {
//			DocumentSigner dSign=new DocumentSigner();
//			
//			JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc, "", request, version);
//			if(resSign!=null && resSign.getString("result").equals("00")) {
//				Date dateSign= new Date(resSign.getLong("date"));
//				doc.setFlag(true);
//				doc.setDate_sign(date);
//				doc.setInvoice(inv);
//				new DocumentsAccessDao(db).update(doc);
//				
//				Documents documents=doc.getDocument();
//				documents.setSigndoc(signdoc);
//	    		new DocumentsDao(db).update(documents);
//	
//				
//				return true;
//			}
//			//						
////			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
//			
//		} catch (Exception e) {
//			LogSystem.error(getClass(), e);
//		}
//		return false;
//
//
//	}
//	
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
//	
//	
//}
