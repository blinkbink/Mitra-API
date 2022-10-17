package apiMitra;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import api.log.ActivityLog;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;

public class UpdateEP extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx=null;
	String kelas="apiMitra.UpdateEP";
	String trxType="UPD-DT";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="UPD"+sdfDate2.format(tgl).toString();
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		
		Mitra mitra=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {

				}
				// multipart form
				else {
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
							LogSystem.info(request, "Token ada : " + token, kelas, refTrx, trxType);
							mitra=tm.getMitra();
						} else {
							LogSystem.error(request, "Token tidak ada",kelas, refTrx, trxType);
							JSONObject jo=new JSONObject();
							jo.put("res", "55");
							jo.put("notif", "token salah");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							
							return;
						}
					}
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
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems, kelas, refTrx, trxType);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         
	         JSONObject jo = null;
	        
	         jo=UpdateEp(mitra, useradmin,jsonRecv, request, context);
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo, kelas, refTrx, trxType);



		}catch (Exception e) {
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject UpdateEp(Mitra mitratoken, User useradmin, JSONObject jsonRecv, HttpServletRequest  request,JPublishContext context) throws JSONException, IOException {
		DB db = getDB(context);

        JSONObject jo=new JSONObject();
        JSONArray lWaiting= new JSONArray();
		JSONArray lSigned= new JSONArray();
		String status = "waiting";
        String res="05";
        Documents id_doc=null;
        boolean kirim=false;

        PrintWriter outStream = null;
		try {
			outStream = context.getResponse().getWriter();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        DocumentsDao docDao = new DocumentsDao(db);
        
        try{
        	ApiVerification aVerf = new ApiVerification(db);
        	boolean vrf=false;
	        if(mitratoken!=null) {
	        	vrf=true;
	        }
	        else {
	        	vrf=aVerf.verification(jsonRecv);
	        }
	        
			if(vrf){
				User usr=null;
	        	Mitra mitra=null;
	        	if(mitratoken==null) {
		        	if(aVerf.getEeuser().isAdmin()==false) {
		        		jo.put("result", "55");
		                jo.put("notif", "userid anda tidak diijinkan.");
		                return jo;
		        	}
		        	usr = aVerf.getEeuser();
		    		mitra = usr.getMitra();
	        	}
	        	else {
	        		usr=useradmin;
	        		mitra=mitratoken;
	        	}

	        	String email_lama = jsonRecv.getString("email_lama").toLowerCase();
				String nohp_lama = jsonRecv.getString("nohp_lama").toLowerCase();
				String email_baru = jsonRecv.getString("email_baru").toLowerCase();
				String nohp_baru = jsonRecv.getString("nohp_baru").toLowerCase();
				
				UserManager um=new UserManager(db);
				User user=um.findByEmailMitra2(email_lama);
				
				if(user == null)
				{
					PreRegistrationDao pdo = new PreRegistrationDao(db);
					PreRegistration pr = pdo.findByEmail(email_lama);
					
					if(pr == null)
					{
						jo.put("result", "05");
						jo.put("notif", "Email belum terdaftar");
						LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
						return jo;
					}
					else
					{
						if(!pr.getNo_handphone().equals(nohp_lama))
						{
							jo.put("result", "08");
							jo.put("notif", "Nomor hp lama tidak cocok");
							LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
							return jo;
						} else if(!pr.getEmail().equals(email_lama)) {
							jo.put("result", "08");
							jo.put("notif", "Email lama tidak cocok");
							LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
							return jo;
						}
						else
						{
							UserdataDao udao=new UserdataDao(db);
							PreRegistrationDao predao=new PreRegistrationDao(db);
							user = um.findByEmail(email_baru);
							if(user!=null) {
								jo.put("result", "14");
								jo.put("notif", "Email pengganti sudah terdaftar, silahkan gunakan email lain.");
								LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
								return jo;
							}
							List<Userdata> ludata=udao.findByNoHp(nohp_baru);
							if(ludata.size()>0) {
								jo.put("result", "14");
								jo.put("notif", "Nomor handphone pengganti sudah terdaftar, silahkan gunakan nomor handpone lain.");
								LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
								return jo;
							}
							if(!email_baru.equals(email_lama)) {
								PreRegistration pre=predao.findByEmail(email_baru);
								if(pre!=null) {
									jo.put("result", "14");
									jo.put("notif", "Email pengganti sudah terdaftar, silahkan gunakan email lain.");
									LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
									return jo;
								}
							}
							if(!nohp_baru.equals(nohp_lama)) {
								List<PreRegistration> lpre=predao.findNoHp(nohp_baru);
								if(lpre.size()>0) {
									jo.put("result", "14");
									jo.put("notif", "Nomor handphone pengganti sudah terdaftar, silahkan gunakan nomor handpone lain.");
									LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
									return jo;
								}
							}
							
							
							
							Transaction tx = db.session().beginTransaction();   
							
							pr.setEmail(email_baru);
							pr.setNo_handphone(nohp_baru);
							
							db.session().update(pr);
							try
							{
								LogSystem.info(request, "Commit update",kelas, refTrx, trxType);
								tx.commit ();
								jo.put("result", "00");
								jo.put("notif", "Sukses update data");
								LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
								
								try {
			    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
			    		        	logSystem.POST("change-email", "success", "[API] Berhasil mengubah email " + email_lama + " menjadi " + email_baru, null, null, null, null, null, pr.getNo_identitas());
			    		        	logSystem.POST("change-phone-number", "success", "[API] Berhasil mengubah Nomor HP " + nohp_lama + " menjadi " + nohp_baru, null, null, null, null, null, pr.getNo_identitas());
			    		        	
			    		        }catch(Exception e)
			    		        {
			    		        	e.printStackTrace();
			    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
			    		        }
								
								return jo;
							}catch(Exception e)
							{
								LogSystem.error(getClass(), e,kelas, refTrx, trxType);
								jo.put("result", "06");
								jo.put("notif", "Gagal update data");
								LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
								return jo;
							}
						}
					}
				}
				else
				{
					jo.put("result", "14");
					jo.put("notif", "User sudah aktif, silahkan mengubah data pada halaman dashboard");
					LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
					return jo;
				}
			}
			else {
				jo.put("res", res);
				jo.put("notif", "UserId atau Password salah");
			}
			return jo;
        }
        catch (Exception e) {
			// TODO: handle exception
        	//context.getResponse().getOutputStream().flush();
        	//context.getResponse().getOutputStream().close();
        	LogSystem.error(getClass(), e,kelas, refTrx, trxType);
        	jo.put("res", "06");
			jo.put("notif", "Update email atau no hp gagal");
			return jo;
		}
        
        
	}
	
		
		
}
