package api;

import id.co.keriss.consolidate.action.SmartphoneVerification;
import id.co.keriss.consolidate.action.ajax.ConfirmSms;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import org.hibernate.Session;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.RSAEncryption;
import id.co.keriss.consolidate.util.SystemUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

public class OTP extends HttpServlet {
	String kelas="api.OTP";
	String trxType="GEN-OTP";
	String refTrx="";
	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
//		System.out.println("masukkkkkkkkkkkkkkkk");
        String filePath = null;
        JSONObject jo = new JSONObject();
        try {
			jo.put("result", "05");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LogSystem.error(getClass(), e1);

		}

		DB db = new DB();
		OutputStream outStream=null;
        try {
            db.open ();
            Session hs = db.session();
          
			String jsonString=null;
			byte[] dataFile=null;
			String res="";
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
								
								 if(fileItem.getFieldName().equals("file")){
									 dataFile=fileItem.get();
								 }
								// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());
	
							}
						}
					}
				 String process=request.getRequestURI().split("/")[2];
	//	         System.out.println("PATH :"+request.getRequestURI());
	//	         System.out.println("RECEIVE :"+jsonString);
	//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
		         LogSystem.request(request,fileItems);
				 if(jsonString==null) return;	         
		         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
		         User user=null;
	//	    	 user=new UserManager(db).findByUsername(jsonRecv.getString("userid"));
				 String idUser =null; 
				 User u=null;
				 if(jsonRecv.has("user")) {
					 idUser=AESEncryption.decryptDoc(jsonRecv.getString("user"));
				 }
				 else {
					 SmartphoneVerification verf=new SmartphoneVerification(db);
					 verf.verification(jsonRecv);
					 if(verf.getRC().equals("00")||verf.getRC().equals("E1")) {
						 idUser=String.valueOf(verf.getEeuser().getUserdata().getId());
						 u=verf.getEeuser();
					 }
				 }

		         if(idUser!=null) {
		        	 UserManager userm = new UserManager(db);
		        	 
		        	 User idm = userm.findById(Long.parseLong(idUser));
		        	 
			         String msisdn_enc =jsonRecv.getString("MSISDN");
			         String msisdn=RSAEncryption.decryptWithPriv(msisdn_enc);
			  
			         ConfirmCode code=new ConfirmCode();
			         code.setCode(SystemUtil.randomNumber());
			         code.setStatus("no");
			         code.setWaktu_buat(new Date());
			         if(u==null) {
			             Userdata userd=new Userdata();
				         userd.setId(new Long(idUser));
			         }else {
			        	 code.setEeuser(u);
			         }
			         new ConfirmCodeDao(db).create(code);


			         Thread th= new Thread(new Runnable() {
						
						@Override
						public void run() {
							new ConfirmSms().sendingPostRequest(code.getCode(), msisdn, idm.getUserdata().getMitra().getId(), refTrx, request);
							
						}
					 });
			         th.start();
						jo.put("result", "00");
	
		         }
	
			}catch (Exception e) {
	            LogSystem.error(getClass(), e);
	//			error (context, e.getMessage());
	//            context.getSyslog().error (e);
			}
	
			outStream = response.getOutputStream();
			JSONObject j=new JSONObject();
			try {
				j.put("JSONFile", jo);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				LogSystem.error(getClass(), e);
			}
			outStream.write(j.toString().getBytes());
			
			
        }catch (Exception e) {
            LogSystem.error(getClass(), e);

        }finally {
        	try {
        		db.close();
        		outStream.close();
        	}
        	catch (Exception e) {
                LogSystem.error(getClass(), e);
			}
        	
        }
	}
	
}
