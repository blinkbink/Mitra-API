package api;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.util.LogSystem;

public class AccountAPI extends ActionSupport {

	SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		//userRecv=new UserManager(db).findById((long) 5790);
      
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		FileItem dataFile=null;
		String filename=null;
		List<FileItem> fileItems = null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {

				}
				// multipart form
				else {
					// Create a new file upload handler
					ServletFileUpload upload = new ServletFileUpload(
							new DiskFileItemFactory());

					// parse requests
					fileItems = upload.parseRequest(request);

					// Process the uploaded items
					for (FileItem fileItem : fileItems) {
						// a regular form field
						if (fileItem.isFormField()) {
							if(fileItem.getFieldName().equals("jsonfield")){
								jsonString=fileItem.getString();
							}
							
						}
						else {
							
							 if(fileItem.getFieldName().equals("file") || fileItem.getFieldName().equals("File")){
								 dataFile=fileItem;
								 filename=fileItem.getName();
							 }
							// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

						}
					}
				}
			 String process=request.getRequestURI().split("/")[2];
//	         System.out.println("PATH :"+request.getRequestURI());
//	         System.out.println("RECEIVE :"+jsonString);
	         LogSystem.request(request,fileItems);
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
	         
	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
			
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         JSONObject jo = null;
//	         if(process.equals("SGN01.html")){
//	        	 	jo=signFile(jsonRecv, dataFile, filename);
//	         }
	       
	         if(process.equals("CPWD.html")){
	                jo=changePassword(jsonRecv,context);
	         }
	         if(process.equals("FRGPWD.html")){
	                jo=forgotPassword(jsonRecv,context);
	         }
	         String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Logger.getLogger("q2").info(request.getRequestURI()+ ", RESPONSE : "+res);
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
	        LogSystem.response(request, jo);

			

		}catch (Exception e) {

			LogSystem.error(getClass(), e);
		}
	}
	

	JSONObject changePassword(JSONObject jsonRecv,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
        String res="05";
        String signature=null;
        User userRecv;
		DB db = getDB(context);
		try{
			UserManager um=new UserManager(db);
			User usr= um.findByUsername(jsonRecv.get("userid").toString());
			userRecv=null;
			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
			DigiSign ds=new DigiSign();
			if(um==null){
				res="06"; //username/ password salah
			}
			else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
				System.out.println(usr.getLogin().getPassword());
				System.out.println(jsonRecv.get("pwd"));
				res="06";
			}
			else{
				usr.getLogin().setPassword(jsonRecv.get("new-pwd").toString());
				Session session=db.session();
				Transaction t=session.beginTransaction();
		        session.save(usr);
		        t.commit();
				res="00";
				userRecv=usr;
			}

//			TrxDs trx=new TrxDs();
//			trx.setMessage(jsonRecv.toString());
//			trx.setMsg_from(k.getUserdata());
//			trx.setMsg_time(new Date());
//			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
//			else trx.setStatus(new StatusKey("EVR"));
//			trx.setType('1');
//			if(userRecv!=null){
//				trx.setMsg_to(userRecv.getUserdata());
//				new TrxDSDao(db).create(trx);
//			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			res="06";
			LogSystem.error(getClass(), e);
		}
        jo.put("result", res);
	   return jo;
	}
	
	JSONObject forgotPassword(JSONObject jsonRecv,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
        String res="05";
        String signature=null;
        User userRecv;
		DB db = getDB(context);
		try{
			UserManager um=new UserManager(db);
			User usr= um.findByUsername(jsonRecv.get("userid").toString());
			userRecv=null;
			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
			DigiSign ds=new DigiSign();
			if(um==null){
				res="06"; //username/ password salah
			}
			else{
				//kirim email
				new SendMailSSL().sendMailForgotPassword(usr.getUserdata(),  usr.getNick());
				res="00";
			}

//			TrxDs trx=new TrxDs();
//			trx.setMessage(jsonRecv.toString());
//			trx.setMsg_from(k.getUserdata());
//			trx.setMsg_time(new Date());
//			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
//			else trx.setStatus(new StatusKey("EVR"));
//			trx.setType('1');
//			if(userRecv!=null){
//				trx.setMsg_to(userRecv.getUserdata());
//				new TrxDSDao(db).create(trx);
//			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			res="06";
			LogSystem.error(getClass(), e);
		}
        jo.put("result", res);
	   return jo;
	}
	
}
