package apiMitra2020;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.LogSystem;

public class SendDocMitra extends ActionSupport{
	String kelas="apiMitra2020.SendDocMitra";
	String trxType="SEND-DOC";

	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		// TODO Auto-generated method stub
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="DOC"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		String filetype=null;
		long filesize=0;
		List <FileItem> fileSave=new ArrayList<FileItem>();
		List<FileItem> fileItems=null;
		
		Mitra mitra=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", kelas, refTrx, trxType);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API bukan multipart. ");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}
				// multipart form
				else {
					LogSystem.info(request, "Multipart", kelas, refTrx, trxType);
					TokenMitraDao tmd=new TokenMitraDao(getDB(context));
					String token=request.getHeader("authorization");
					if(token!=null) {
						String[] split=token.split(" ");
						if(split.length==2) {
							if(split[0].equals("Bearer"))token=split[1];
						}
						TokenMitra tm=tmd.findByToken(token.toLowerCase());
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada",kelas, refTrx, trxType);
							mitra=tm.getMitra();
						} else {
							LogSystem.error(request, "Token tidak ada",kelas, refTrx, trxType);
							JSONObject jo=new JSONObject();
							jo.put("result", "55");
							jo.put("notif", "token salah");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.response(request, jo, kelas, refTrx, trxType);
							return;
						}
					}
					// Create a new file upload handler
					ServletFileUpload upload = new ServletFileUpload(
							new DiskFileItemFactory());
					
					//Enumeration<String> headerNames = request.getHeaderNames();
					LogSystem.info(request, "authorization adalah = "+request.getHeader("authorization"),kelas, refTrx, trxType);

					// parse requests
					 fileItems = upload.parseRequest(request);

					// Process the uploaded items
					for (FileItem fileItem : fileItems) {
						// a regular form field
						if (fileItem.isFormField()) {
							if(fileItem.getFieldName().equalsIgnoreCase("jsonfield")){
								jsonString=fileItem.getString();
							}
						}
						else {
					         
							 if(fileItem.getFieldName().equalsIgnoreCase("file")){

								 filedata=fileItem;
								 filename=fileItem.getName();
								 filetype=fileItem.getContentType();
								 filesize=fileItem.getSize();
								 //System.out.println("Format File: " + filetype.split("/")[1]);
							 }
						}
					}
				}
			 String process=request.getRequestURI().split("/")[2];
			 LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
	         //System.out.println("PATH :"+request.getRequestURI());

	         LogSystem.request(request, fileItems,kelas, refTrx, trxType);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         if(mitra!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
	        		 LogSystem.error(request, "Parameter userid tidak ditemukan",kelas, refTrx, trxType);
	        		 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "Parameter userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
	        	 }
	        	 
	        	 String userid=jsonRecv.getString("userid").toLowerCase();
		         UserManager user=new UserManager(getDB(context));
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
		        	 }
		        	 else {
		        		 LogSystem.error(request, "Token dan mitra tidak valid",kelas, refTrx, trxType);
		        		 JSONObject jo=new JSONObject();
						 jo.put("result", "55");
						 jo.put("notif", "Token dan Userid tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						 LogSystem.response(request, jo, kelas, refTrx, trxType);
						 return;
		        	 }
		         }
		         else {
		        	 LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "55");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 return;
		         }
	         }
	         

	         
	         JSONObject jo = null;
	        
	         jo = docMitra(mitra, useradmin, jsonRecv, filedata, filename, request, context, refTrx); 
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo,kelas, refTrx, trxType);

		}catch (Exception e) {
            LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data request tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
		}
		
	}
	
	JSONObject docMitra(Mitra mitratoken, User useradmin, JSONObject jsonRecv, FileItem dataFile, String filename,HttpServletRequest  request,JPublishContext context, String refTrx) throws JSONException{
		JSONObject jo=new JSONObject();
		if(!jsonRecv.has("document_id")) {
			jo.put("result", "28");
            jo.put("notif", "parameter document_id tidak ditemukan.");
            return jo;
		}
		if(jsonRecv.getString("document_id").equals("") || jsonRecv.getString("document_id")==null) {
			  jo.put("result", "05");
              jo.put("notif", "document id kosong");
              return jo;
		}
		return jo;
	}

}
