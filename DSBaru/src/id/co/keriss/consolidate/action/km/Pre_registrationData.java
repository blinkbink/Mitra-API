package id.co.keriss.consolidate.action.km;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class Pre_registrationData extends ActionSupport implements DSAPI {
	
	String refTrx="";
	String kelas="km.Pre_registrationData";
	String trxType="PRC-ACT";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
	//super.logout (context, cfg); // just in case - force logout
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="ACT"+sdfDate2.format(tgl).toString();
		
	DB db = getDB(context);

	HttpServletRequest  request  = context.getRequest();
	JSONObject result = new JSONObject();
	String userid = null;
	String pwd = null;
	String email = null;
	boolean exist = true;
	User dataUser = null;
	Mitra mitra=null;
	String jsonString=null;
	String jsondata = request.getParameter("jsonfield");
	//System.out.println(jsondata);
	LogSystem.info(request, "Json data : "+jsondata, kelas, refTrx, trxType);
	try {

			//Check Token
			TokenMitraDao tmd=new TokenMitraDao(getDB(context));
			String token=request.getHeader("authorization");
			//System.out.println("Token header = " + token);
			LogSystem.info(request, "Token header = " + token, kelas, refTrx, trxType);
			if(token != null) 
			{
				String[] split=token.split(" ");
				if(split.length==2) 
				{
					if(split[0].equals("Bearer"))token=split[1];
				}
				TokenMitra tm=tmd.findByToken(token.toLowerCase());
				if(tm!=null) 
				{
					LogSystem.info(request, "Token ada : " + token, kelas, refTrx, trxType);
					mitra=tm.getMitra();
					LogSystem.info(request, "Mitra = " + mitra.getName(), kelas, refTrx, trxType);
				}
				else
				{
					LogSystem.error(request, "Token salah",kelas, refTrx, trxType);
					context.getResponse().sendError(403);
					return;
				}	
			}
			
			try {
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				if (!isMultipart) {
					//System.out.println("Bukan Multipart");
					LogSystem.info(request, "Bukan multipart", kelas, refTrx, trxType);
					jsonString = jsondata;
				}

				else {
					//System.out.println("Multipart");		
					LogSystem.info(request, "Multipart", kelas, refTrx, trxType);
					ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
					List<FileItem> fileItems=null;
					//System.out.println("Check");
					LogSystem.info(request, "Check", kelas, refTrx, trxType);
					try {
						fileItems = upload.parseRequest(request);
					}catch(Exception e)
					{
						LogSystem.error(request, e.getMessage());
						LogSystem.error(getClass(), e, kelas, refTrx, trxType);
					}

					for (FileItem fileItem : fileItems) {
						if (fileItem.isFormField()) {

							if (fileItem.getFieldName().equals("jsonfield")) {
								jsonString = fileItem.getString();
								LogSystem.info(request, jsonString, kelas, refTrx, trxType);
								//System.out.println(jsonString);
							}
						} 
					}
				}
				
				UserManager user = new UserManager(db);
				JSONObject adata=new JSONObject(jsonString).getJSONObject("JSONFile");
				
				//final JSONObject adata = new JSONObject(jsondata).getJSONObject("JSONFile");

				//System.out.println("GET JSON API = " + adata);
				LogSystem.info(request, "GET JSON API = " + adata, kelas, refTrx, trxType);
				
				userid = adata.getString("userid").toLowerCase();
//			    pwd = adata.getString("pwd");
			    email = adata.getString("email_user").toLowerCase();
			    
			    context.put("email", email);
			    
				dataUser = user.findByUsername(userid);
				
				//System.out.println("Check User id : " +  dataUser);
				LogSystem.info(request, "Check User id : " +  dataUser, kelas, refTrx, trxType);
				if(dataUser != null)
				{
					LogSystem.info(request, "User Authentication : "+ dataUser.getName(), kelas, refTrx, trxType);
					LogSystem.info(request, "UserID : " + dataUser.getNick().toString(), kelas, refTrx, trxType);
//					System.out.println("User Authentication : "+ dataUser.getName());
//					System.out.println("userID : " + dataUser.getNick().toString() );
					ApiVerification aVerf = new ApiVerification(db);
			        boolean vrf = false;
			        LogSystem.info(request, "Is Admin : " + dataUser.isAdmin(), kelas, refTrx, trxType);
			        //System.out.println("Is Admin : " + dataUser.isAdmin() );
			        if(mitra!=null && dataUser.isAdmin()) {
			        	//Check mitra dan token
			        	if(dataUser.getMitra().getId()!=mitra.getId()) {
			        		//System.out.println("Token dan mitra tidak cocok");
			        		LogSystem.info(request, "Token dan mitra tidak valid", kelas, refTrx, trxType);
			        		context.getResponse().sendError(403);
			        		//result.put("rc", "12");
			        		//result.put("notif", "Token dan mitra tidak cocok");
							//context.put("result", result.toString());	
							return;
			        	}
			        	LogSystem.info(request, "Masuk karena token", kelas, refTrx, trxType);
			        	//System.out.println("Masuk karna token");
			        	vrf = true;
			        }
			        else {
			        	LogSystem.info(request, "Masuk karena password", kelas, refTrx, trxType);
			        	//System.out.println("Masuk karna password");
			        	vrf=aVerf.verification(adata);
			        }
			        LogSystem.info(request, "Verifikasi userID & Password : " + vrf, kelas, refTrx, trxType);
			        //System.out.println("Verifikasi userID & Password : " + vrf);
			        if(vrf)
			        {
			        	User usr=null;
			        	Mitra mitra2=null;
			            LogSystem.info(request, "Check Mitra : " + mitra, kelas, refTrx, trxType);
			        	//System.out.println("Check Mitra : " + mitra);
			        	if(mitra==null) 
			        	{
			        		LogSystem.info(request, "Mitra status : " + mitra, kelas, refTrx, trxType);
			        		
			        		//System.out.println("Mitra status : " + mitra);
			        		LogSystem.info(request, "Status user :" +aVerf.getEeuser().isAdmin(), kelas, refTrx, trxType);
			        		//System.out.println(aVerf.getEeuser().isAdmin());
				        	if(aVerf.getEeuser().isAdmin()==false) 
				        	{
				        		LogSystem.info(request, "Authentication : "+ "User tidak diijinkan", kelas, refTrx, trxType);
				        		//System.out.println("Authentication : "+ "User tidak diijinkan");
				        		context.getResponse().sendError(401);
				        		//result.put("rc", "12");
				        		//result.put("notif", "User tidak diijinkan");
								//context.put("result", result.toString());	
								return;
//					    		try 
//					    		{
//									context.getResponse().sendError(02);
//								} 
//					    		catch (IOException e1) 
//					    		{
//									e1.printStackTrace();
//								}
				        	}
				        	usr = aVerf.getEeuser();
				    		mitra2 = usr.getMitra();
			        	}
			        	else {
			        		usr=dataUser;
			        		mitra2=mitra;
			        	}
/*
		        		try
		        		{
		        		*/
					    	PreRegistrationDao prDao = new PreRegistrationDao(db);
					    	UserdataDao usd = new UserdataDao(db);
					    	UserManager eeuser = new UserManager(db);
					    	User emaileeuser = eeuser.findByEmail(email);
					    	

					    	if(emaileeuser != null)
					    	{
					    		LogSystem.info(request, "Email sudah melakukan aktivasi", kelas, refTrx, trxType);
					    		context.getResponse().sendError(401);
					    		//result.put("rc", "14");
					    		//result.put("notif", "Email sudah melakukan aktivasi");
								//context.put("result", result.toString());	
								return;
					    	}
					    	
					    	PreRegistration data = prDao.findByEmail(email);
					    	
					    	if(data == null)
					    	{
					    		if(prDao.emailPerusahaan(email) != null)
					    		{
					    			LogSystem.info(request, "Tidak dapat melanjutkan aktivasi email perusahaan", kelas, refTrx, trxType);
					    			context.getResponse().sendError(403);
					    			return;
					    		}
					    		LogSystem.info(request, "Email tidak ada didalam daftar aktivasi", kelas, refTrx, trxType);
					    		context.getResponse().sendError(403);
					    		return;
					    	}
					    	
					    	List<Userdata> ktp = usd.findByKtp(data.getNo_identitas());
					    	
					    	if(ktp.size()>0)
					    	{
					    		LogSystem.info(request, "NIK sudah aktivasi dengan email lain", kelas, refTrx, trxType);
					    		context.getResponse().sendError(403);
					    		return;
					    	}
					    	
					    	if(data != null)
					    	{
					    		if(data.getType() != '1')
					    		{
					    			LogSystem.info(request, "Tidak dapat melanjutkan aktivasi, akun prereg dengan type " + data.getType(), kelas, refTrx, trxType);
					    			context.getResponse().sendError(403);
					    			return;
					    		}
//					    		String response_email = data.getEmail();
//					    		String response_ittd = data.getImageTtd();
//					    		String responseNohp = data.getNo_handphone();
//					    		String idmitra =    AESEncryption.encryptDoc(Long.toString((data.getMitra().getId())));
//					    		
//					    		Long preid = data.getId();
					    		context.put("i_ttd", data.getImageTtd());
					    		//context.put("preid", preid);
					    		context.put("nohp", data.getNo_handphone());
					    		context.put("token", AESEncryption.encryptDoc(Long.toString((data.getId()))));
					    		
					    		//context.put("token", idmitra);
					    		context.put("refTrx", refTrx);
					    		
					    		LogSystem.info(request, "Ada data", kelas, refTrx, trxType);
					    	}
					    	else
					    	{
					    		LogSystem.info(request, "Email info (kosong belum registrasi) atau Kosong", kelas, refTrx, trxType);
					    		context.getResponse().sendError(404);
					    		//result.put("rc", "05");
					    		//result.put("notif", "Email tidak ada atau belum melakukan registrasi");
								//context.put("result", result.toString());	
								return;
//					    		try {
//									context.getResponse().sendError(04);
//								} catch (IOException e1) {
//									e1.printStackTrace();
//								}
					    	}
					    	
					    	if(data.getMitra().getId() != dataUser.getMitra().getId())
					    	{
					    		LogSystem.info(request, "Mitra tidak diijinkan melakukan aktivasi pada user ini", kelas, refTrx, trxType);
					    		context.getResponse().sendError(403);
//					    		result.put("rc", "12");
//					    		result.put("notif", "Mitra tidak diijinkan melakukan aktivasi pada email user ini");
//								context.put("result", result.toString());	
								return;
					    	}
					    	
//					    	Userdata userdata = new UserdataDao(db).findByKtp(data.getNo_identitas());
//							
//					    	if(userdata != null)
//					    	{
//					    		LogSystem.info(request, "KTP sudah terdaftar");
//					    		result.put("rc", "14");
//					    		result.put("notif", "Email belum melakukan registrasi");
//								context.put("result", result.toString());	
//								return;
////					    		try {
////									context.getResponse().sendError(03);
////								} catch (IOException e1) {
////									e1.printStackTrace();
////								}
//					    	}		
					    	

					    }
			        /*
		        		catch(Exception e)
					    {
					    	e.printStackTrace();
					    	try {
								context.getResponse().sendError(304);
							} catch (IOException e1) {
				
								e1.printStackTrace();
							}
							context.put("result", 304);	
					    }
			        }*/
			        else
					{
			        	LogSystem.info(request, "Userid atau password salah", kelas, refTrx, trxType);
			        	context.getResponse().sendError(403);
//			        	result.put("rc", "12");
//			        	result.put("notif", "Userid atau password salah");
//						context.put("result", result.toString());	
						return;
//				    	try {
//							context.getResponse().sendError(05);
//						} catch (IOException e1) {
//			
//							e1.printStackTrace();
//						}
					}
				}
				else
				{
					LogSystem.info(request, "Mitra tidak ada/belum melakukan aktivasi", kelas, refTrx, trxType);
					context.getResponse().sendError(404);
//		        	result.put("rc", "05");
//		        	result.put("notif", "User tidak ada atau belum melakukan aktivasi");
//					context.put("result", result.toString());	
					return;
//			    	try {
//						context.getResponse().sendError(06);
//					} catch (IOException e1) {
//		
//						e1.printStackTrace();
//					}
				}
		}
		catch(Exception e)
		{
			LogSystem.info(request, "Detail Error : " + e.getMessage(), kelas, refTrx, trxType);
			e.printStackTrace();
			context.getResponse().sendError(403);
//        	result.put("rc", "06");
//        	result.put("notif", "General Error");
//			context.put("result", result.toString());	
			return;
//			try 
//			{
//				//Email tidak ditemukan
//				e.printStackTrace();
//				context.getResponse().sendError(07);
//			} 
//			catch (IOException e1) 
//			{
//				e1.printStackTrace();
//			}
//			
//			context.put("result", 402);		
		}
			
			result.put("rc", "00");
			context.put("result", result.get("rc"));	
			
	}
	catch(Exception e)
	{
		LogSystem.info(request, "Error info : "+ e, kelas, refTrx, trxType);
		e.printStackTrace();
		try {
//			result.put("rc", "06");
//        	result.put("notif", "General Error");
//			context.put("result", result.toString());	
			context.getResponse().sendError(403);
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		try 
//		{
//			context.getResponse().sendError(8);
//		} 
//		catch (IOException e1) 
//		{
//			e1.printStackTrace();
//		}
	}
	
		Random rand = new Random(); 
		int value = rand.nextInt(50000); 
		context.put("rand", value);
		
		context.put("domainwv", "https://"+DOMAINAPIWV);
		context.put("refTrx", refTrx);
		context.put("KPSE", DSAPI.KPSE);
        context.put("KP", DSAPI.KP);
        context.put("verifikasi_email", mitra.isVerifikasi_email());
	}
}
