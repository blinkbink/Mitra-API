package id.co.keriss.consolidate.action.ajax;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;



public class SignRequest extends ActionSupport {


	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		User userRecv;
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        
       
		int count = 21;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		JSONObject result=new JSONObject();
		try{
	        

			 String process=request.getRequestURI().split("/")[2];
	         System.out.println("PATH :"+request.getRequestURI());;
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         LogSystem.request(request, "");
	         System.out.println("RECEIVE :"+sb.toString());

	         JSONObject data=new JSONObject(sb.toString());
	         if(!auth.processAuth(data.getString("auth"))) {
	        	 sendRedirect (context, context.getRequest().getContextPath() 
	                     + "/stop.html"
	                 );
	         	return;
	         }
	         
	         JSONArray listUser=(JSONArray) data.get("data");
	         String idDoc=data.getString("idDoc");

	         if(!idDoc.equals(auth.getIdDoc())) {
	        	 sendRedirect (context, context.getRequest().getContextPath() 
	                     + "/stop.html"
	                 );
	         	return;
	         }
	         DocumentsAccessDao dao=new DocumentsAccessDao(db);
	         List<DocumentAccess> listDA=dao.findByDoc(idDoc);
 	         Documents docx=listDA.get(0).getDocument();
	         for(int i=0; i<listUser.length(); i++) {
	        	 JSONObject object=(JSONObject) listUser.get(i);
	        	 for (DocumentAccess documentAccess : listDA) {
					if(documentAccess.getName().equals(object.get("user")) && documentAccess.getEmail().equals(object.get("email"))) {
						documentAccess.setLx(String.valueOf(object.getDouble("lx")));
						documentAccess.setLy(String.valueOf(object.getDouble("ly")));
						documentAccess.setRx(String.valueOf(object.getDouble("rx")));
						documentAccess.setRy(String.valueOf(object.getDouble("ry")));
						documentAccess.setPage(Integer.parseInt(object.getString("status")));
						dao.update(documentAccess);
						
						if(documentAccess.getEeuser()!=null) {
							MailSender mail=new MailSender(documentAccess.getEeuser().getUserdata(),documentAccess.getDocument().getEeuser().getUserdata(), documentAccess.getEmail());
							mail.run();
						}else {
							MailSender mail=new MailSender(documentAccess.getName(),documentAccess.getDocument().getEeuser().getUserdata(), documentAccess.getEmail());
							mail.run();

						}
						listDA.remove(documentAccess);
						break;
					}
				}
	         }
	         
	         
	         for(int i=0; i<listUser.length(); i++) {
	        	 JSONObject object=(JSONObject) listUser.get(i);
	        	 for (DocumentAccess documentAccess : listDA) {
					if(documentAccess.getType().equals("share")) {
						
						if(documentAccess.getEeuser()!=null) {
							MailSender mail=new MailSender(documentAccess.getEeuser().getUserdata(),documentAccess.getDocument().getEeuser().getUserdata(), documentAccess.getEmail());
							mail.run();

						}else {
							MailSender mail=new MailSender(documentAccess.getName(),documentAccess.getDocument().getEeuser().getUserdata(), documentAccess.getEmail());
							mail.run();

						}
						listDA.remove(documentAccess);
						break;
					}
				}
	         }
	         
	        docx.setStatus('T');
    		new DocumentsDao(db).update(docx);
    		FileProcessor fp=new FileProcessor();
    		fp.setDc(docx);
    		fp.generateQRCode(db);
    		rc="00";

		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
            context.getSyslog().error (e);
		}
		
		
		try {
			result.put("result", rc);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
        System.out.println("SEND :"+result.toString());
        context.getResponse().setContentType("application/json");

		context.put("trxjson", result.toString());
		LogSystem.response(request, result,"");

	}
	
	
	class MailSender {

		String email;
		Userdata name;
		String notRegisterName;
		Userdata pengirim;
		
		public MailSender(Userdata penerimaemail,Userdata pengirim,String email) {
			this.email=email;
			this.name=penerimaemail;
			this.pengirim=pengirim;
		}
		
		public MailSender(String penerimaemail,Userdata pengirim,String email) {
			this.email=email;
			this.notRegisterName=penerimaemail;
			this.pengirim=pengirim;
		}
		public void run() {
			if(name!=null) {
				HttpServletRequest request = null; 
				new SendMailSSL(request, "").sendMailFileaReqSign(name,pengirim, email);
			}else {
				HttpServletRequest request = null; 
				new SendMailSSL(request, "").sendMailFileaReqSign(notRegisterName,pengirim, email);
			}
		}
		
	}
	
}
