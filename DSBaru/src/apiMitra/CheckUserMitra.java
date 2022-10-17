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
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.LogSystem;

public class CheckUserMitra extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="CUS"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.CheckUserMitra";
	String trxType="C-USER";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="CUS"+sdfDate2.format(tgl).toString();
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
		JSONObject jo=new JSONObject();
		try {
			jo.put("refTrx", refTrx);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			LogSystem.error(request, e2.getMessage());
		}
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {

				}
				// multipart form
				else {
					TokenMitraDao tmd = null;
					TokenMitra tm = null;
					String token = null;
					try {
						tmd=new TokenMitraDao(getDB(context));
						token=request.getHeader("authorization");
						if(token!=null) {
							String[] split=token.split(" ");
							if(split.length==2) {
								if(split[0].equals("Bearer"))token=split[1];
							}
						}
						
	//					TokenMitra tm=tmd.findByToken(token.toLowerCase());
						
						tm=tmd.findByToken(token.toLowerCase());
						}catch(Exception e)
						{
							jo.put("result", "91");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.error(request, e.toString() , kelas, refTrx, trxType);
							return;
						}
						
						
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada",kelas, refTrx, trxType);
							mitra=tm.getMitra();
						} else {
							LogSystem.error(request, "Token tidak ada",kelas, refTrx, trxType);
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
				
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType+"/"+mitra.getName());
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems,kelas, refTrx, trxType);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         
	         //JSONObject jo = null;
	        
	         jo=DownloadDocMitra(mitra, useradmin,jsonRecv, request, context);
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
//			LogSystem.response(request, jo,kelas, refTrx, trxType);
			LogSystem.response(request, jo,kelas, refTrx, trxType+"/"+mitra.getName());



		}catch (Exception e) {
			LogSystem.error(request, e.getMessage());
            LogSystem.error(getClass(), e,kelas, refTrx, trxType+"/"+mitra.getName());
            //JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				LogSystem.error(request, e1.getMessage());
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject DownloadDocMitra(Mitra mitratoken, User useradmin, JSONObject jsonRecv, HttpServletRequest  request,JPublishContext context) throws JSONException, IOException {
		DB db = getDB(context);

        JSONObject jo=new JSONObject();
        JSONArray lWaiting= new JSONArray();
		JSONArray lSigned= new JSONArray();
		String status = "waiting";
        String res="05";
        Documents id_doc=null;
        boolean kirim=false;
     // obtains response's output stream
        
        PrintWriter outStream = null;
		try {
			outStream = context.getResponse().getWriter();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LogSystem.error(request, e1.getMessage());
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
		        		jo.put("result", "12");
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
				
				String email=jsonRecv.getString("email").toLowerCase();
				UserManager um=new UserManager(db);
//				User user=um.findByEmailMitra(email, aVerf.getEeuser().getMitra().getId());
				User user=um.findByEmailMitra2(email);
				int leveluser=3;
				if(user!=null) {
					if(user.getUserdata().getLevel()!=null) {
						if(user.getUserdata().getLevel().isEmpty()==false && user.getUserdata().getLevel().length()>0) {
							leveluser=Integer.parseInt(user.getUserdata().getLevel().substring(1));
						}
					}
		        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
		        	LogSystem.info(request, "level mitra - leveluser = "+levelmitra+" -- "+leveluser,kelas, refTrx, trxType);
		        	if(levelmitra>2 && leveluser<3) {
		        		jo.put("result", "05");
			    		jo.put("notif", "user belum terdaftar.");
			    		return jo;
		        	}
					char stat=user.getStatus();
					switch (stat) {
					case '0':
						jo.put("notif", "belum verifikasi");
						jo.put("info", "belum aktif");
						break;
					case '1':
						jo.put("notif", "sudah verifikasi, belum aktivasi");
						jo.put("info", "belum aktif");
						break;
					case '2':
						jo.put("notif", "reject");
						jo.put("info", "belum aktif");
						break;
					case '3':
						jo.put("notif", "aktif");
						jo.put("info", "aktif");
						break;
					case '4':
						jo.put("notif", "suspend");
						jo.put("info", "aktif");
						break;

					default:
						jo.put("notif", "belum terdaftar");
						break;
					}
					jo.put("result", "00");
				}
				else {
					PreRegistrationDao pdao=new PreRegistrationDao(db);
					PreRegistration pre = pdao.findEmail(email);
					
			    	
			    	if(pre!=null)
					{
			    		jo.put("result", "00");
						jo.put("notif", "user sudah mendaftar namun belum melakukan aktivasi.");
						jo.put("info", "belum aktif");
				    }
			    	else
			    	{
			    		jo.put("result", "05");
						//jo.put("notif", "user belum terdaftar di " + mitra.getName());
			    		jo.put("notif", "user belum terdaftar.");
			    	}

				}	
 				
			}
			else {
				jo.put("res", res);
				jo.put("notif", "UserId atau Password salah");
			}
			return jo;
        }
        catch (Exception e) {
        	LogSystem.error(request, e.getMessage());
        	LogSystem.error(getClass(), e,kelas, refTrx, trxType);
        	jo.put("res", "06");
			jo.put("notif", "check user gagal");
			return jo;
		}
        
        
	}
	
		
		
}
