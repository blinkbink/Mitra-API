package apiMitra;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;



public class PreregistrationData extends ActionSupport {
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	//super.logout (context, cfg); // just in case - force logout

	DB db = getDB(context);	
	HttpServletRequest  request  = context.getRequest();
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="PRE"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.PreregistrationData";
	String trxType="PRE-REG-DATA";
	
	String docs ="";
	String link ="";
	int i=0;
	String jsonString=null;
	byte[] dataFile=null;
	FileItem filedata=null;
	String filename=null;
	List <FileItem> fileSave=new ArrayList<FileItem>() ;
	List<FileItem> fileItems=null;
	
	Mitra mitra=null;
	User useradmin=null;
	try {
		/*
		docs = AESEncryption.encryptDoc("523");
		link = "https://localhost:8443/preregistration.html?preregister="
				+ URLEncoder.encode(docs, "UTF-8");
		//log.info("#### link  "+link);
		*/
		
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		TokenMitraDao tmd=new TokenMitraDao(getDB(context));
		String token=request.getHeader("authorization");
		if(token!=null) {
			String[] split=token.split(" ");
			if(split.length==2) {
				if(split[0].equals("Bearer"))token=split[1];
			}
		}
		TokenMitra tm=tmd.findByToken(token.toLowerCase());
		System.out.println("token adalah = "+token);
		if(tm!=null) {
			LogSystem.info(request, "Token ada : " + token,kelas, refTrx, trxType);
			mitra=tm.getMitra();
		} else {
			LogSystem.error(request, "Token tidak ada", kelas, refTrx, trxType);
			JSONObject jo=new JSONObject();
			jo.put("res", "05");
			jo.put("notif", "token salah");
			context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			context.getResponse().sendError(403);
			return;
		}
		
		// no multipart form
		if (!isMultipart) {
//			context.getResponse().sendError(404);
//			return;
			LogSystem.info(request, "Bukan multipart",kelas, refTrx, trxType);
			jsonString=request.getParameter("jsonfield");
		}
		// multipart form
		else {
			
			LogSystem.info(request, "Multipart",kelas, refTrx, trxType);
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
				
			}
		}
	 String process=request.getRequestURI().split("/")[2];
     //System.out.println("PATH :"+request.getRequestURI());
     LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
//     Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//	 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//     Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
     LogSystem.request(request, fileItems, kelas, refTrx, trxType);
	 if(jsonString==null) return;	         
     JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
     
     if(mitra!=null) {
    	 
    	 if (!jsonRecv.has("userid"))
    	 {
    		 LogSystem.error(request, "Parameter userid tidak ditemukan",kelas, refTrx, trxType);
    		 JSONObject jo=new JSONObject();
			 jo.put("res", "12");
			 jo.put("notif", "Parameter userid tidak ditemukan");
			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			
			 return;
    	 }
    	 
    	 String userid=jsonRecv.getString("userid").toLowerCase();
    	 
         UserManager user=new UserManager(getDB(context));
         //User eeuser=user.findByUsername(userid);
         useradmin=user.findByUsername(userid);
         if(useradmin!=null) {
        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
        	     LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
        		 //System.out.println("token dan mitra valid");
        	 }
        	 else {
        	     LogSystem.error(request, "Token dan mitra tidak valid",kelas, refTrx, trxType);
				 context.getResponse().sendError(403);
				 return;
        	 }
         }
         else {
             LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
			 context.getResponse().sendError(403);
			 return;
         }
     }
     
		ApiVerification aVerf = new ApiVerification(db);
		boolean vrf=false;
		 if(mitra!=null && useradmin!=null) {
		 	vrf=true;
		 }
		 else {
		 	vrf=aVerf.verification(jsonRecv);
		 }
		//		
		if(vrf){
			
			User usr=null;
			//Mitra mitra=null;
			if(mitra==null && useradmin==null) {
			    	if(aVerf.getEeuser().isAdmin()==false) {
			    		
			    		context.getResponse().sendError(403);
			    		return;
			    	}
			    	usr = aVerf.getEeuser();
					mitra = usr.getMitra();
			}
			else {
				usr=useradmin;
				
			}
			
			PreRegistrationDao pdao=new PreRegistrationDao(db);
			PreRegistration pr=pdao.findEmailByMitra(jsonRecv.getString("email_user").toLowerCase(), mitra.getId());
			if(pr!=null)context.put("trans",pr);
			else context.put("trans","User tidak ditemukan dalam daftar activasi. Silahkan login atau daftar Digisign.");
		}
		else {
		//			outStream.write(403);
		//outStream.flush();
		//			outStream.close();
			context.getResponse().sendError(403);
		}
		context.put("domain", "https://"+DSAPI.DOMAINAPI);
		context.put("refTrx", refTrx);
		
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
		try {
			context.getResponse().sendError(403);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	try{
		
		
		
		//log.info("###### GET TTD ######");

		String preregister = request.getParameter("preregister");
		
		//log.info("#### Encrpt Key  #### :" + preregister);
		AESEncryption aes = new AESEncryption();
		String preid = aes.decrypt(preregister);

		String complete ="yes";
//		log.info("PRE ID :"+preid);
//		PreregisterDao dd = new PreregisterDao(db);
//		Preregistration pr = dd.findById(Long.valueOf(preid));
		PreRegistrationDao pdao=new PreRegistrationDao(db);
		PreRegistration pr=pdao.findById(Long.valueOf(preid));
		if(pr == null) {
			context.put("trans","Sudah Terdaftar Silahkan Login");	
		}else
			context.put("trans",pr);
	
	}catch (Exception e) {
		context.put("trans","Error 404");	
//		log.error(e);
//          context.getSyslog().error (e);
		}
	*/
	}
	
}
