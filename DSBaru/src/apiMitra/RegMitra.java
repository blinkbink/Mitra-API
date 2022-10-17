package apiMitra;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import api.email.SendActivasi;
import api.log.ActivityLog;
import api.verifikasi.CheckPhotoToAsliRI;
import api.verifikasi.CheckToDukcapil;
import api.verifikasi.VerifikasiDoubleLoginForLevelMitra2;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.CallbackPendingDao;
import id.co.keriss.consolidate.dao.InterfaceMitraDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.ReRegistrationDao;
import id.co.keriss.consolidate.dao.RegLogDao;
import id.co.keriss.consolidate.dao.RekeningDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.ee.InterfaceMitra;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.ReRegistration;
import id.co.keriss.consolidate.ee.RegistrationLog;
import id.co.keriss.consolidate.ee.Rekening;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;


public class RegMitra extends ActionSupport {

	//private static String basepath="/opt/data-DS/UploadFile/";
	private static final Pattern INVALID_CHARS_PATTERN_NAME = Pattern.compile("[a-zA-Z \\.\\,]*");
	private static final Pattern INVALID_CHARS_PATTERN_NIK = Pattern.compile("[0-9]*");
	private static String basepathPreReg="/file2/data-DS/PreReg/";
	private static String basepathRegLog="/file2/data-DS/LogRegistrasi/";
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx="";
	String kelas="apiMitra.RegMitra";
	String trxType="REGD";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="REGD"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		Mitra mitra=null;
		User useradmin=null;
		JSONObject jo=new JSONObject();
		DB db = null;
		try {
		  db = getDB(context);
		}
		catch(Exception e) {
		  e.printStackTrace();
		  LogSystem.error(request, e.toString());
		}
		RegistrationLog RL = new RegistrationLog();
		String respon = null;
		try {
			jo.put("refTrx", refTrx);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			LogSystem.error(request, e2.getMessage());
		}
		
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				ArrayList <String> array = new ArrayList();
				// no multipart form
				if (!isMultipart) {
					//LogSystem.info(request, "Bukan multipart");
					//JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.error(request, "req bukan multipart",kelas, refTrx, trxType);
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					respon = jo.toString();
	    		    LogSystem.info(request, "Response String: " + respon);
	    		    RL.setMessage_response(respon + "(req bukan multipart)");
	            	new RegLogDao(db).create(RL);
					return;
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
							e.printStackTrace();
							jo.put("result", "91");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.error(request, e.toString() , kelas, refTrx, trxType);
							return;
						}
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							mitra=tm.getMitra();
							//trxType=trxType+"/"+mitra.getName();
							LogSystem.info(request, "ada Token = "+token, kelas, refTrx, trxType+"/"+mitra.getName());
							
						} else {
							LogSystem.error(request, "Token null ",kelas, refTrx, trxType);
							
							jo.put("result", "55");
							jo.put("notif", "token salah");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.response(request, jo, kelas, refTrx, trxType);
							respon = jo.toString();
			    		    LogSystem.info(request, "Response String: " + respon);
			    		    RL.setMessage_response(respon);
			            	new RegLogDao(db).create(RL);
							return;
						}
					
					
					
					// Create a new file upload handler
					ServletFileUpload upload = new ServletFileUpload(
							new DiskFileItemFactory());

					// parse requests
					//handle try catch timeout upload file request
					try
					{
						LogSystem.info(request, "Reading request", kelas, refTrx, trxType);
						fileItems = upload.parseRequest(request);
						LogSystem.info(request, "Complete reading request", kelas, refTrx, trxType);
					}catch(Exception e)
					{
						e.printStackTrace();
						LogSystem.info(request, "Timeout read request", kelas, refTrx, trxType);
						LogSystem.info(request, e.toString(), kelas, refTrx, trxType);
		        		jo.put("result", "05");
						jo.put("notif", "Request Data tidak ditemukan");
						LogSystem.response(request, jo, kelas, refTrx, trxType);
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						return;
					}
					

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
							 
							 if(fileItem.getFieldName().equals("ttd")||fileItem.getFieldName().equals("fotodiri")||fileItem.getFieldName().equals("fotoktp")||fileItem.getFieldName().equals("fotonpwp")||fileItem.getFieldName().equals("foto_dukcapil")||fileItem.getFieldName().equals("foto_buku_tabungan")){
								 fileSave.add(fileItem);
								 
							 }
						}
						String FileUploading = "["+fileItem.getFieldName()+"]:{"+fileItem.getName()+","+fileItem.getSize()+"};";
						LogSystem.info(request,"RL_CHECK File REQUEST>"+ FileUploading, kelas, refTrx, trxType);
						if(FileUploading !=null && !fileItem.getFieldName().equals("jsonfield")) {
							array.add(FileUploading) ;
							LogSystem.info(request,"Array >"+ array, kelas, refTrx, trxType);
						}
					}
				}
			
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         //LogSystem.info(request, "PATH :"+request.getRequestURI());
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems, kelas, refTrx, trxType);
	         
	         //LogSystem.info(request, refTrx);
			 if(jsonString==null) { 
				 	respon = "jsonfield NULL (trxjson)";
	    		    LogSystem.info(request, "Response String: " + respon);
	    		    RL.setMessage_response(respon);
	            	new RegLogDao(db).create(RL);
	            	return;	       
			 }
			 
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         String con = "";
	         for (int a=0 ;a<array.size();a++) {
	        	  con+=array.get(a);
	         }
	         String concatReq = "\"" + jsonString +"\";"+ con ;
	         RL.setMessage_req(concatReq);
	//////////////////(START)///////////////////REQUEST REGISTRATION LOG CONTENT////////////////////////////
	        if (jsonRecv.has("email")){        	 
		       	 String RL_email = jsonRecv.getString("email");
		       	 RL.setEmail(RL_email);
	        }
	        if (jsonRecv.has("idktp")){        	 
		         String RL_no_identitas = jsonRecv.getString("idktp");
		         RL.setNo_identitas(RL_no_identitas);
		     }
	        if (jsonRecv.has("nama")){        	 
		       	 String RL_nama = jsonRecv.getString("nama");
		       	 RL.setNama(RL_nama);
		     }
	        if (jsonRecv.has("jenis_kelamin")){        	 
		       	 if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) RL.setJk('L');
				     else  RL.setJk('P');
		//       	 String RL_jk = jsonRecv.getString("jenis_kelamin");
		//       	 RL.setJk(RL_jk);
			     }
	        if (jsonRecv.has("tmp_lahir")){        	 
		       	 String RL_tempat_lahir = jsonRecv.getString("tmp_lahir");
		       	 RL.setTempat_lahir(RL_tempat_lahir);
		     }
	        if (jsonRecv.has("tgl_lahir")){  
	        	try {
			       	 Date RL_tgl_lahir = new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString());
			       	 RL.setTgl_lahir(RL_tgl_lahir);
	        	}catch(Exception e)
	        	{
	        		e.printStackTrace();
	        		LogSystem.info(request, "Format tanggal lahir salah", kelas, refTrx, trxType);
	        		jo.put("result", "28");
					jo.put("notif", "Format tanggal lahir salah");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.error(request, e.toString() , kelas, refTrx, trxType);
					return;
	        	}
		     }
	        if (jsonRecv.has("alamat")){        	 
		       	 String RL_alamat= jsonRecv.getString("alamat");
		       	 RL.setAlamat(RL_alamat);
		     }
	        if (jsonRecv.has("kelurahan")){        	 
		       	 String RL_kelurahan = jsonRecv.getString("kelurahan");
		       	 RL.setKelurahan(RL_kelurahan);
		     }
	        if (jsonRecv.has("kecamatan")){        	 
		       	 String RL_kecamatan = jsonRecv.getString("kecamatan");
		       	 RL.setKecamatan(RL_kecamatan);
		     }
	        if (jsonRecv.has("kota")){        	 
		       	 String RL_kota = jsonRecv.getString("kota");
		       	 RL.setKota(RL_kota);
		     }
	        if (jsonRecv.has("provinci")){        	 
		       	 String RL_propinsi = jsonRecv.getString("provinci");
		       	 RL.setPropinsi(RL_propinsi);
		     }
	        if (jsonRecv.has("kode-pos")){        	 
		       	 String RL_kodepos = jsonRecv.getString("kode-pos");
		       	 RL.setKodepos(RL_kodepos);
		     }
	        if (jsonRecv.has("tlp")){        	 
		       	 String RL_no_handphone = jsonRecv.getString("tlp");
		       	 RL.setNo_handphone(RL_no_handphone);
		     }
	        if (jsonRecv.has("npwp")){        	 
	       	 	if(!jsonRecv.getString("npwp").equals("")||!jsonRecv.getString("npwp").equals(null)) {
		        	 String RL_npwp = jsonRecv.getString("npwp");
		        	 RL.setNpwp(RL_npwp);
	       	 	}
		     }
	        if (jsonRecv.has("userid")){        	 
		       	 String RL_email_perusahaan = jsonRecv.getString("userid");
		       	 RL.setEmail_perusahaan(RL_email_perusahaan);
		     }
	        if (jsonRecv.has("no_rekening")){        	 
		       	 String RL_no_rekening = jsonRecv.getString("no_rekening");
		       	 RL.setNo_rekening(RL_no_rekening);
		     }
	        if (jsonRecv.has("kode_bank")){        	 
		       	 String RL_kode_bank = jsonRecv.getString("kode_bank");
		       	 RL.setKode_bank(RL_kode_bank);
		     }
	        if(jsonRecv.has("redirect")) {
	            if(jsonRecv.getBoolean("redirect")) {
	              	Boolean RL_Redirect = true;
	  	         	RL.setRedirect(RL_Redirect);
	            }
	        }
	        if(jsonRecv.has("verifikasi")) {
	            if(!jsonRecv.getBoolean("verifikasi")) {
	              	Boolean RL_verifikasi = false;
	              	RL.setVerifikasi(RL_verifikasi);
	  	         }
	        }
	        if (jsonRecv.has("type")){        	 
	       	 if(!jsonRecv.getString("type").equals("API")) {
		        	 char RL_type = '1';
		        	 RL.setType(RL_type);
	       	 }
		     }
	        if(jsonRecv.has("dataexist")) {
	            if(jsonRecv.getBoolean("dataexist")) {
	              	Boolean RL_DataExist = true;
	  	         	RL.setRedirect(RL_DataExist);
	            }
	        }
	        ////////////////////////////////////////////////REGLOG FILES/////////////////////////////////////////
	        String uploadToRegLog = basepathRegLog+refTrx+"/original/";
			SaveFileWithSamba samba=new SaveFileWithSamba();
	        for (FileItem fileItem : fileSave) {
//	      	 LogSystem.info(request, fileItem.getFieldName(), kelas, refTrx, trxType);
	      	 //System.out.println(fileItem.getFieldName());
	      	 if(fileItem.getFieldName().equals("ttd") && fileItem.getSize()!=0) {
	      	  
	      	 	  //File fileTo = new File(uploadTo +fileItem.getFieldName()+".png");
//	      	 	  BufferedImage jpgImage = null;
//	      	      try {
//	      	            jpgImage = ImageIO.read(fileItem.getInputStream());
//	      	      } catch (IOException e) {
//	      	            e.printStackTrace();
//	      	      }
//	      	      if(containsTransparency(jpgImage)==true) {
//	      	    	  //fileItem.write(fileTo);
	      	    	  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".png");
//	      	      } else {
//	      	    	  int color = jpgImage.getRGB(0, 0);
//	                 Image data=FileProcessor.makeColorTransparent(jpgImage, new Color(color), 10000000);
//	                 BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,jpgImage.getWidth(),
//	                 jpgImage.getHeight());
//	                     //ImageIO.write(newBufferedImage, "png" ,fileTo);
//	                 ByteArrayOutputStream baos=new ByteArrayOutputStream();
//	                 ImageIO.write(newBufferedImage, "png" , baos);
//	                 samba.write(baos.toByteArray(), uploadToRegLog+fileItem.getFieldName()+".png");
//	      	      }
//	      	      
//	      	      jpgImage.flush();
//	          	  LogSystem.info(request, "ttd :" + uploadToRegLog+fileItem.getFieldName()+".png", kelas, refTrx, trxType);
	          	  //System.out.println("File ttd :" + fileTo.getPath());
	          	  fileItem.getOutputStream().close();
	          	  RL.setImageTtd(uploadToRegLog+fileItem.getFieldName()+".png");
	//          	  userdata.setImageTtd(uploadToRegLog+fileItem.getFieldName()+".png");
	          	  fileItem.getOutputStream().close();
	      	  }
	      	  else if(fileItem.getFieldName().equals("fotodiri")){
	      		  //File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
	          	  //fileItem.write(fileTo);
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	          	  RL.setImageWajah(uploadToRegLog+fileItem.getFieldName()+".jpg");
	          	  fileItem.getOutputStream().close();
	      	  }else if(fileItem.getFieldName().equals("fotoktp")){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	          	  RL.setImageKTP(uploadToRegLog+fileItem.getFieldName()+".jpg");
	          	  fileItem.getOutputStream().close();
	      	  }else if(fileItem.getFieldName().equals("fotonpwp") && fileItem.getSize()!=0){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  RL.setImageNPWP(uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  fileItem.getOutputStream().close();
	      	  }else if(fileItem.getFieldName().equals("foto_dukcapil") && fileItem.getSize()!=0){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  RL.setImageWajahDukcapil(uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  fileItem.getOutputStream().close();
	      	  }else if(fileItem.getFieldName().equals("fotokitas") && fileItem.getSize()!=0){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  RL.setImageKitas(uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  fileItem.getOutputStream().close();
	      	  }else if(fileItem.getFieldName().equals("fotowefie") && fileItem.getSize()!=0){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  RL.setImageWefie(uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  fileItem.getOutputStream().close();
	      		
	      	  }else if(fileItem.getFieldName().equals("foto_buku_tabungan") && fileItem.getSize()!=0){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  RL.setI_buku_tabungan(uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  fileItem.getOutputStream().close();
	      	  }
	        }
        //////////////////(FINISH)//////////////////REQUEST REGISTRATION LOG CONTENT////////////////////////////
	         if(mitra!=null) {
	        	 Mitra RL_mitra = mitra;
	        	 RL.setMitra(RL_mitra);
	        	 if (!jsonRecv.has("userid"))
	        	 {
	        		 LogSystem.error(request, "Parameter userid tidak ditemukan",kelas, refTrx, trxType);
	        		 //JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "Parameter userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 respon = jo.toString();
		    		 LogSystem.info(request, "Response String: " + respon);
		    		 RL.setMessage_response(respon);
		             new RegLogDao(db).create(RL);
					 return;
	        	 }
	        	 
	        	 String userid = jsonRecv.getString("userid").toLowerCase();
	        	 
		         UserManager user=new UserManager(getDB(context));
		         //User eeuser=user.findByUsername(userid);
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 //LogSystem.info(request, "Token dan mitra valid");
		        		 LogSystem.info(request, "Token dan Mitra Valid", kelas, refTrx, trxType);
		        		 
		        	 }
		        	 else {
		        		 //LogSystem.error(request, "Token dan mitra tidak sesuai");
		        		 LogSystem.error(request, "Token dan Mitra Tidak Valid", kelas, refTrx,trxType);
		        		 //JSONObject jo=new JSONObject();
						 jo.put("result", "55");
						 jo.put("notif", "Token dan Mitra tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						 LogSystem.response(request, jo, kelas, refTrx, trxType);
						 respon = jo.toString();
			    		 LogSystem.info(request, "Response String: " + respon);
			    		 RL.setMessage_response(respon);
			             new RegLogDao(db).create(RL);
						 return;
		        	 }
		         }
		         else {
		        	 //LogSystem.error(request, "Userid tidak ditemukan");
		        	 LogSystem.error(request, "Userid tidak ditemukan", kelas, refTrx,trxType);
		        	 //JSONObject jo=new JSONObject();
					 jo.put("result", "55");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 LogSystem.response(request, jo, kelas, refTrx, trxType);
					 respon = jo.toString();
		    		 LogSystem.info(request, "Response String: " + respon);
		    		 RL.setMessage_response(respon);
		             new RegLogDao(db).create(RL);
					 return;
		         }
	         }
	         
	         
	         //JSONObject jo = null;
	         /*
	         VerifikasiManualDao vdao=new VerifikasiManualDao(db);
	         
	         VerifikasiManual verman=vdao.cekNikEmail(jsonRecv.getString("idktp"), jsonRecv.getString("email"));
	         if(verman!=null) {
	        	 jo.put("result", "03");
				 jo.put("notif", "User sudah terdaftar dan masih dalam proses verifikasi secara manual. Mohon menunggu 2x24 jam.");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				 LogSystem.response(request, jo, kelas, refTrx, trxType);
				 respon = jo.toString();
	    		 RL.setMessage_response(respon);
	             new RegLogDao(db).create(RL);
				 return;
	         }
	         */
	         
	         //Check lebih dari 3x Registrasi
	         if(mitra!=null && DSAPI.DEVELOPMENT==false) {
	        	 List<RegistrationLog> lregLog=new RegLogDao(db).findByMitraNikEmail(mitra.getId(), jsonRecv.getString("idktp"), jsonRecv.getString("email").toLowerCase());
	        	 LogSystem.info(request, "DATA REGISTRATION LOG : " +  lregLog.size(), kelas, refTrx, trxType);
		         if(lregLog.size()>3) {
		        	 int x=0;
		        	 for(RegistrationLog reg: lregLog){
		        		 String resp=reg.getMessage_response();
		        		 JSONObject object=new JSONObject(resp);
		        		 if(object.getString("result").equals("12")) {
		        			 x++;
		        		 }
		        	 }
		        	 LogSystem.info(request, "NILAI X : " +  x, kelas, refTrx, trxType);
		        	 if(x>3) {
		        		 jo.put("result", "20");
			        	 jo.put("notif", "User tersebut sudah mencoba didaftarkan lebih dari 3x dengan data tidak valid. Silahkan lakukan pengecekan data-data terlebih dahulu.");
			        	 LogSystem.response(request, jo, kelas, refTrx, trxType);
			        	 RL.setMessage_response(jo.toString());
			        	 new RegLogDao(db).create(RL);
			        	 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			        	 return;
		        	 }
		        	 
		         } 
	         }
	         
	         
	        //PROSES REGISTRASI// 
	        jo=RegisterUserMitra(refTrx, jsonRecv, fileSave, context, mitra, useradmin, request);
	        
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
			respon = jo.toString();
		    RL.setMessage_response(respon);
        	new RegLogDao(db).create(RL);
        	
        	InterfaceMitraDao imdao=new InterfaceMitraDao(db);
        	if(mitra!=null) {
        		InterfaceMitra im=imdao.findByMitra(mitra.getId());
            	if(im!=null) {
            		if(im.getI_registrasi()!=null) {
            			/*nur yang handle register callback gagal*/
//            			if(jo.getString("result").equals("00")) {
    	        			CallbackPendingDao cpdao=new CallbackPendingDao(db);
    	        			String result=URLEncoder.encode(jo.getString("result"), "UTF-8");
    	        			String notif=URLEncoder.encode(jo.getString("notif"), "UTF-8");
    	        			
    	        			String info=null;
    	        			if(jo.has("info")) {
    	        				info="&info="+URLEncoder.encode(jo.getString("info"), "UTF-8");
    	        			} 
    	        			
    	        			
    	        			String callback="result="+result+"&notif="+notif+info+"&email="+jsonRecv.getString("email")+"&token="+mitra.getId();
    	        			CallbackPending cp=new CallbackPending();
    	        			cp.setMitra(mitra);
    	        			cp.setResponse(500);
    	        			cp.setTime(tgl);
    	        			cp.setTipe("interface");
    	        			cp.setCallback(DSAPI.CALLBACK+URLEncoder.encode(im.getI_registrasi(), "UTF-8")+"?"+callback);
    	        			cpdao.create(cp);
//            			}
            		}
            	}
        	}
        	
        	
			context.put("trxjson", res);
			
			//LogSystem.response(request, jo);
			LogSystem.response(request, jo, kelas, refTrx, trxType+"/"+mitra.getName());

		}catch (Exception e) {
			e.printStackTrace();
            LogSystem.error(request, e.toString());

            try {
				jo.put("result", "05");
				jo.put("notif", "Request Data tidak ditemukan");
				respon = jo.toString();
    		    LogSystem.info(request, "Response String: " + respon);
    		    RL.setMessage_response(respon);
    		    try {
    		    	new RegLogDao(db).create(RL);
    		    }catch(Exception error)
    		    {
    		    	e.printStackTrace();
    		    	LogSystem.error(request, error.toString(), kelas, refTrx, trxType);
    		    	jo.put("result", "91");
    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
    				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
    				return;
    		    }
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LogSystem.error(request, e1.getMessage());
			}
		}
	}
	
	JSONObject RegisterUserMitra(String refTrx, JSONObject jsonRecv,List<FileItem> fileSave, JPublishContext context, Mitra mitratoken, User useradmin, HttpServletRequest  request) throws JSONException{
		//User userRecv;
		boolean devel=DSAPI.DEVELOPMENT;
		String reg_number = "";
		String link ="";
		DB db = getDB(context);
		JSONObject jo=new JSONObject();
        String res="05";
        String notif="Email sudah terdaftar gunakan email lain atau silahkan login dengan akun anda";
        VerificationData vd=null;
        
        Date reregDate = new Date();
        LogSystem.info(request, "TANGGAL MASUKK : " +  reregDate, kelas, refTrx, trxType);
        ApiVerification aVerf = new ApiVerification(db);
        boolean vrf=false;
        if(mitratoken!=null && useradmin!=null) {
        	vrf=true;
        }
        else {
        	vrf=aVerf.verification(jsonRecv);
        }
        
        
  		if(jsonRecv.get("tlp").toString().length()<8) 
  	  	{
  	  		notif="teks tlp kurang dari 8 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	} else {
  	  		char[] dataInput=jsonRecv.getString("tlp").toCharArray();
	  	  	for (int i = 0; i < dataInput.length; i++) {
	            // Menyaring Apakah data masukkan berupa DIGIT ??
	  	  		if(i>0) {
		            boolean isOnlyDigit = Character.isDigit(dataInput[i]);
		            if (isOnlyDigit == false) {
		            	notif="teks tlp harus angka";
		    	  	  	jo.put("result", "28");
		    			jo.put("notif", notif);
		    			
		    			return jo;
		            }
	  	  		}
	            if(i==0) {
	            	if(dataInput[0]!='0' && dataInput[0]!='6' && dataInput[0]!='+') {
	            		notif="format hp harus 62, 08 atau +62";
		    	  	  	jo.put("result", "28");
		    			jo.put("notif", notif);
		    			
		    			return jo;
	            	}
	            }
	            if(i==1) {
	            	if(dataInput[1]!='8' && dataInput[1]!='2' && dataInput[1]!='6') {
	            		notif="format hp harus 62, 08 atau +62";
		    	  	  	jo.put("result", "28");
		    			jo.put("notif", notif);
		    			
		    			return jo;
	            	}
	            }
	            if(i==2) {
	            	if(dataInput[0]=='+') {
	            		if(dataInput[2]!='2') {
	            			notif="format hp harus 62, 08 atau +62";
			    	  	  	jo.put("result", "28");
			    			jo.put("notif", notif);
			    			
			    			return jo;
	            		}
	            	}
	            }
	          } 	
  	  	}
  		

  		if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16||jsonRecv.getString("idktp").length()>16||nikCheck(jsonRecv.getString("idktp"))) {
			jo.put("result", "FE");
			jo.put("notif", "Format NIK Salah");
			
			return jo;
		}
  		
  	  	if(jsonRecv.get("nama").toString().length()>128) 
  	  	{
  	  		notif="teks nama maksimum 128 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	}
  	  	
  	  	if(nameCheck(jsonRecv.getString("nama")))
  	  	{
	  	  	notif="format nama salah";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	}

  	  	if(jsonRecv.get("email").toString().length()>80) 
  	  	{
  	  		notif="teks email maksimum 80 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	}
  	  	if(!isValid(jsonRecv.getString("email").trim())) {
	  	  	jo.put("result", "FE");
			jo.put("notif", "Format email salah");
			
			return jo;
  	  	}
  	  	
  	  	if(jsonRecv.get("tgl_lahir").toString().length()<10 || jsonRecv.get("tgl_lahir").toString().length()>10) 
	  	{
	  		notif="teks tanggal lahir harus diisi dan maksimum 10 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
	  	}

  		if(jsonRecv.get("kode-pos").toString().length()>10) 
	  	{
	  		notif="teks kode pos maksimum 10 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
	  	}
  		if(jsonRecv.get("jenis_kelamin").toString().length()>10) 
	  	{
	  		notif="teks jenis kelamin maksimum 10 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
	  	}
  		
  		
        
        if(vrf){
        	User usr=null;
        	Mitra mitra=null;
        	int levelmitra=4;
        	if(mitratoken==null && useradmin==null) {
	        	if(aVerf.getEeuser().isAdmin()==false) {
	        		jo.put("result", "12");
	                jo.put("notif", "userid anda tidak diijinkan.");
	                return jo;
	        	}
	        	usr = aVerf.getEeuser();
	    		mitra = usr.getMitra();
	    		levelmitra = Integer.parseInt(mitra.getLevel().substring(1));
        	}
        	else {
        		usr=useradmin;
        		mitra=mitratoken;
        		levelmitra = Integer.parseInt(mitra.getLevel().substring(1));
        	}
        	
        	//PreRegistration userdata= null;
    		
    		PreRegistrationDao udataDao=new PreRegistrationDao(db);
    		//userdata = udataDao.findByEmail(jsonRecv.getString("email").toLowerCase());
    		PreRegistration userdata3 = udataDao.findByEmailNik(jsonRecv.getString("email").toLowerCase(), jsonRecv.getString("idktp"));
    		if(userdata3!=null) {
    			
    			if(userdata3.getMitra()==mitra) {
    				jo.put("result", "00");
    			} else {
    				Date tgl_lahir = null;
					try {
						tgl_lahir = new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LogSystem.error(request, e.toString());
					}
    				if(jsonRecv.getString("nama").equalsIgnoreCase(userdata3.getNama()) && tgl_lahir.equals(userdata3.getTgl_lahir())) {
    					LogSystem.info(request, "data sesuai dengan preregister", kelas, refTrx, trxType);
    					FileItem selfie=null;
    					for (FileItem fileItem : fileSave) {
    						if(fileItem.getFieldName().equals("fotodiri")){
    				    		  selfie=fileItem;
    				    		  break;
    				      	  }
    					}
    					if(selfie!=null) {
    						FaceRecognition facerec=new FaceRecognition(refTrx, trxType, request);
    						SaveFileWithSamba smb=new SaveFileWithSamba();
    						byte[] selfieLama=smb.openfile(userdata3.getImageWajah());
    						JSONObject resp=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
    						if(resp.getBoolean("result")==true || resp.getString("result").equals("00")) {
    							LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
    						} else {
    							JSONObject checkdata=new JSONObject();
								checkdata.put("nik", true);
								checkdata.put("name", true);
								checkdata.put("birthdate", true);
								
    							jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("data", checkdata);
			    				jo.put("info", "Verifikasi wajah gagal");
			    				return jo;
    						}
    					} else {
    						res="28";
    				        notif="Data upload tidak lengkap";
    				        jo.put("result", res);
    				        jo.put("notif", notif);
    				        jo.put("info", "harap upload photo ektp dan wajah selfie anda.");
    				        return jo;
    					}
    				} else {
    					JSONObject checkdata=new JSONObject();
						checkdata.put("nik", true);
						checkdata.put("name", false);
						checkdata.put("birthdate", false);
						if(jsonRecv.getString("nama").equalsIgnoreCase(userdata3.getNama()))checkdata.put("name", true);
						if(tgl_lahir.equals(userdata3.getTgl_lahir()))checkdata.put("birthdate", true);
						
						jo.put("result", "12");
	    				jo.put("notif", "verifikasi user gagal.");
	    				jo.put("info", "nama atau tanggal lahir tidak sesuai");
	    				jo.put("data", checkdata);
	    				return jo;
    				}
    				jo.put("result", "14");
    			}
				jo.put("notif", "Email sudah terdaftar, namun belum melakukan aktivasi. Silahkan untuk melakukan aktivasi sebelum data dihapus dari daftar aktivasi.");
				if(userdata3.getKode_user()!=null) {
					jo.put("kode_user", userdata3.getKode_user());
				}
				return jo;
    		} else {
    			PreRegistration pr = udataDao.findNik(jsonRecv.getString("idktp"));    		
        		//if(udataDao.findNik(jsonRecv.getString("idktp"))!=null) {
        		if(pr != null)
        		{
        			jo.put("result", "14");
    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
    				jo.put("email_registered", pr.getEmail());
    				if(!devel) 
					{
	    				//Pengecekan ulang data nama, tanggal lahir dan foto selfie
	    				JSONObject data = new JSONObject();
	    				boolean name = false;
	    				boolean birthdate = false;
	    				boolean selfieStatus = false;
	    				
	    				if(pr.getNama().equalsIgnoreCase(jsonRecv.getString("nama")))
	    				{
	    					name = true;
	    				}
	    				
	    				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    				
	    				if(sdf.format(pr.getTgl_lahir()).equals(jsonRecv.getString("tgl_lahir")))
	    				{
	    					birthdate = true;
	    				}
	    				
	    				FileItem selfie=null;
						for (FileItem fileItem : fileSave) 
						{
							if(fileItem.getFieldName().equals("fotodiri"))
							{
					    		  selfie=fileItem;
					    		  break;
					      	}
						}
	    				
	    				FaceRecognition facerec = new FaceRecognition(refTrx, trxType, request);
	    				SaveFileWithSamba smb=new SaveFileWithSamba();
						byte[] selfieLama=smb.openfile(pr.getImageWajah());
						JSONObject resp=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
						
						LogSystem.info(request, "Response check facetoface " + resp.toString());
						
						if(resp.getBoolean("result")==true || resp.getString("result").equals("00")) 
						{
							LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
							selfieStatus = true;
						}
						else
						{
							LogSystem.info(request, "photo tidak selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
						}
						
						data.put("name", name);
	    				data.put("birthdate", birthdate);
	    				data.put("selfie", selfieStatus);
	    				
	    				jo.put("data", data);
	    				
	    				if(!name || !birthdate || !selfieStatus)
	 					{
		    				if(selfieStatus)
		    				{
		    					jo.put("info", "verifikasi text gagal.");
		    				}
		    				
	//	    				if(jo.has("email_registered"))
	//	    				{
	//	    					jo.remove("email_registered");
	//	    				}
	 					}
					}
    				
    				return jo;
        		} else {
        			pr=udataDao.findEmail(jsonRecv.getString("email").toLowerCase());
        			if(pr != null) {
        				jo.put("result", "14");
        				jo.put("notif", "Email sudah terdaftar dengan NIK lain, silahkan gunakan email lain.");
        				if(!devel) 
    					{
	        				//Pengecekan ulang data nama, tanggal lahir dan foto selfie
	        				JSONObject data = new JSONObject();
	        				boolean name = false;
	        				boolean birthdate = false;
	        				boolean selfieStatus = false;
	        				
	        				if(pr.getNama().equalsIgnoreCase(jsonRecv.getString("nama")))
	        				{
	        					name = true;
	        				}
	        				
	        				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        				
	        				if(sdf.format(pr.getTgl_lahir()).equals(jsonRecv.getString("tgl_lahir")))
	        				{
	        					birthdate = true;
	        				}
	        				
	        				FileItem selfie=null;
	    					for (FileItem fileItem : fileSave) 
	    					{
	    						if(fileItem.getFieldName().equals("fotodiri"))
	    						{
	    				    		  selfie=fileItem;
	    				    		  break;
	    				      	}
	    					}
	        				
	        				FaceRecognition facerec = new FaceRecognition(refTrx, trxType, request);
	        				SaveFileWithSamba smb=new SaveFileWithSamba();
	    					byte[] selfieLama=smb.openfile(pr.getImageWajah());
	    					JSONObject resp=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
	    					
	    					LogSystem.info(request, "Response check facetoface " + resp.toString());
	    					
	    					if(resp.getBoolean("result")==true || resp.getString("result").equals("00")) 
	    					{
	    						LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
	    						selfieStatus = true;
	    					}
	    					else
	    					{
	    						LogSystem.info(request, "photo tidak selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
	    					}
	    					
	    					data.put("name", name);
	        				data.put("birthdate", birthdate);
	        				data.put("selfie", selfieStatus);
	        				
	        				jo.put("data", data);
	        				
	    					if(!name || !birthdate || !selfieStatus)
	     					{
		        				if(selfieStatus)
			    				{
			    					jo.put("info", "verifikasi text gagal.");
			    				}
	     					}
    					}
        				
        				//jo.put("email_registered", pr.getEmail());
        				return jo;
        			}
        		}
    			
    			//userdata=new PreRegistration();
    		}
    		
    		
    		
    		
    		
    		boolean nextProcess=true;    		
    		User user=null;
    		boolean fNik=false;
    		
	    	  	if(jsonRecv.has("reg_number"))
	        	{
	        		 reg_number = jsonRecv.getString("reg_number");
	        		 //LogSystem.info(request, "Reg number : " + reg_number);
	        		 LogSystem.info(request, "Reg Number = "+reg_number, kelas, refTrx,trxType);
	        	}
	    	  	
	    	  	byte[] WajahRotate=null;
	    	  	FileItem ktpItm = null, WajahItm = null, ttditm = null, npwpItm = null,dukcapilItm=null, bukuTabunganItm=null;
				for (FileItem fileItem : fileSave) {
					if(fileItem.getFieldName().equals("fotodiri")){
			    		  WajahItm=fileItem;
			    		  String extensi = FilenameUtils.getExtension(fileItem.getName());
			    		  //LogSystem.info(request, "Extensi selfie = "+extensi);
			    		  LogSystem.info(request, "Extensi selfie = "+extensi, kelas, refTrx,trxType);
			    		  String ext2 = fileItem.getContentType();
			    		  if(ext2!=null)LogSystem.info(request, "Type selfie = "+ext2, kelas, refTrx,trxType);
			    		  
			      	  }else   if(fileItem.getFieldName().equals("fotoktp")){
			          	  ktpItm=fileItem;
			          	  String ext2 = fileItem.getContentType();
			          	  LogSystem.info(request, "Type ktp = "+ext2, kelas, refTrx,trxType);
			    		  
			      	  }else if(fileItem.getFieldName().equals("ttd")) {
			      		  ttditm=fileItem;
			      		  String ext2 = fileItem.getContentType();
			    		 
			      		  LogSystem.info(request, "Type ttd = "+ext2, kelas, refTrx,trxType);
			    		  
			      	  }else if(fileItem.getFieldName().equals("fotonpwp")) {
			      		  npwpItm=fileItem;
			      		  String ext2 = fileItem.getContentType();
			    		 
			      		  LogSystem.info(request, "Type npwp = "+ext2, kelas, refTrx,trxType);
			    		  
			      	  }else if(fileItem.getFieldName().equals("foto_dukcapil")) {
			      		  dukcapilItm=fileItem;
			      		  String ext2 = fileItem.getContentType();
			    		 
			      		  LogSystem.info(request, "Type dukcapil = "+ext2, kelas, refTrx,trxType);
			    		  
			      	  }else if(fileItem.getFieldName().equals("foto_buku_tabungan")) {
			      		  bukuTabunganItm=fileItem;
			      		  String ext2 = fileItem.getContentType();
			    		 
			      		  LogSystem.info(request, "Type photo buku-tabungan = "+ext2, kelas, refTrx,trxType);
			    		  
			      	  }
			        }
		        
		        if(WajahItm==null || ktpItm==null || WajahItm.getSize()==0 || ktpItm.getSize()==0) {
		        	//LogSystem.info(request, "KTP atau selfie null");
		        	LogSystem.info(request, "KTP atau Selfie NULL", kelas, refTrx,trxType);
				    res="28";
			        notif="Data upload tidak lengkap";
			        jo.put("result", res);
			        jo.put("notif", notif);
			        jo.put("info", "harap upload photo ektp dan wajah selfie anda.");
			        return jo;
		        }
		        
		        
		        
		        if(mitra.isEkyc_tabungan()==true) {
		        	if(bukuTabunganItm==null) {
		        		//LogSystem.info(request, "Mitra menggunakan ekyc buku tabungan namun image buku tabungan tidak dikirimkan.");
		        		LogSystem.info(request, "Mitra menggunakan ekyc buku tabungan namun image buku tabungan tidak dikirimkan.", kelas, refTrx,trxType);
					    res="28";
				        notif="Data upload tidak lengkap.";
				        jo.put("result", res);
				        jo.put("notif", notif);
				        jo.put("info", "harap upload photo buku tabungan anda.");
				        return jo;
		        	}
		        	
		        	if(!jsonRecv.has("no_rekening")) {
		        		jo.put("result", "28");
				        jo.put("notif", "No rekening harap dicantumkan");
				        return jo;
		        	}
		        	
		        	if(!jsonRecv.has("kode_bank")) {
		        		jo.put("result", "28");
				        jo.put("notif", "Kode bank harap dicantumkan");
				        return jo;
		        	}
		        }
		        
    			user= new UserManager(db).findByUserNikNonStatus(jsonRecv.getString("email").toLowerCase(), jsonRecv.getString("idktp"));
    			
    			
    			if(user!=null) 
    			{ 
    				int leveluser=Integer.parseInt(user.getUserdata().getLevel().substring(1));
    				if((levelmitra>2 && leveluser<3) || user.getStatus()=='0') {
    					//LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA");
    					LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA dengan status user "+user.getStatus(), kelas, refTrx,trxType);
    					if(!devel) 
    					{
							//Proses cek ulang nama, tanggal lahir dan foto selfie
		                	LogSystem.info(request, "Proses cek ulang nama, tanggal lahir dan foto selfie", kelas, refTrx,trxType);
		                	JSONObject dataUlang = new JSONObject();
		     				boolean name = false;
		     				boolean birthdate = false;
		     				boolean selfieStatus = false;
		     				
		     				if(user.getUserdata().getNama().equalsIgnoreCase(jsonRecv.getString("nama")))
		     				{
		     					name = true;
		     				}
		     				
		     				SimpleDateFormat sdfUlang = new SimpleDateFormat("dd-MM-yyyy");
		    				
		    				if(sdfUlang.format(user.getUserdata().getTgl_lahir()).equals(jsonRecv.getString("tgl_lahir")))
		     				{
		     					birthdate = true;
		     				}
		     				
		     				FileItem selfie=null;
		 					for (FileItem fileItem : fileSave) 
		 					{
		 						if(fileItem.getFieldName().equals("fotodiri"))
		 						{
		 				    		  selfie=fileItem;
		 				    		  break;
		 				      	}
		 					}
		     				
		     				FaceRecognition facerec = new FaceRecognition(refTrx, trxType, request);
		     				SaveFileWithSamba smb=new SaveFileWithSamba();
		 					byte[] selfieLama=smb.openfile(user.getUserdata().getImageWajah());
		 					JSONObject respUlang=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
		 					
		 					LogSystem.info(request, "Response check facetoface " + respUlang.toString());
		 					
		 					if(respUlang.getBoolean("result")==true || respUlang.getString("result").equals("00")) 
		 					{
		 						LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
		 						selfieStatus = true;
		 					}
		 					else
		 					{
		 						LogSystem.info(request, "photo tidak selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
		 					}
		 					
		 					
		     				if(!name || !birthdate || !selfieStatus)
		 					{
		     					
			 					dataUlang.put("name", name);
			 					dataUlang.put("birthdate", birthdate);
			 					dataUlang.put("selfie", selfieStatus);
			     				
			     				jo.put("data", dataUlang);
			     				
		 						jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal. Nama/Foto Selfie/Tanggal lahir tidak sesuai");
			    				if(selfieStatus)
			    				{
			    					jo.put("info", "verifikasi text gagal.");
			    				}
			    				jo.put("data", dataUlang);
			    				
			    				if(jo.has("email_registered"))
			    				{
			    					jo.remove("email_registered");
			    				}
			    				
			    				return jo;
		 					}
	     				//Selesai cek
    					}
    					
    					//Check Ke DUKCAPIL
    					
    					if(devel==false) {
	    					CheckToDukcapil dukcapil=new CheckToDukcapil(refTrx, trxType, request);
							JSONObject job=dukcapil.check2(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
							if(job!=null) {
								LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
								JSONObject data=new JSONObject();
								data.put("nik", false);
								data.put("name", false);
								data.put("birthdate", false);
								
								if(job.has("content")) {
									org.codehaus.jettison.json.JSONArray jarray=job.getJSONArray("content");
									JSONObject jobj=jarray.getJSONObject(0);
									if(jobj.has("NAMA_LGKP")){
										data.put("nik", true);
	    								String split[]=jobj.getString("NAMA_LGKP").split(" ");
	    								if(split[0].equalsIgnoreCase("Sesuai")) {
	    									data.put("name", true);
	    								} 
	    								if(jobj.getString("TGL_LHR").equalsIgnoreCase("Sesuai")){
	    									data.put("birthdate", true);
	    								}
	    								jo.put("data", data);
	    								if(data.getBoolean("name")==false||data.getBoolean("birthdate")==false) {
	    									jo.put("result", "12");
	    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
	    				    				jo.put("info", "verifikasi text gagal.");
	    				    				jo.put("data", data);
	    				    				return jo;
	    								}
									} else {
										if(jobj.has("RESPON")) {
											if(jobj.getString("RESPON").equalsIgnoreCase("Kuota Akses Hari ini telah Habis")) {
												jo.put("result", "91");
							    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
							    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
							    				return jo;
											}
										}
										jo.put("result", "12");
					    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
					    				jo.put("data", data);
					    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
					    				return jo;
									}
								} else {
									jo.put("result", "91");
				    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
				    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
				    				return jo;
								}
							} else {
								jo.put("result", "91");
			    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
			    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
			    				return jo;
							}
    					}
    					
    					//jika status user=0 <mendaftarkan mandiri ke web digisign>
    					if(user.getStatus()=='0') {
    						LogSystem.info(request, "kondisi user sudah mendaftar di web digisign dengan status 0",kelas, refTrx,trxType);
    						
    						SaveFileWithSamba samba=new SaveFileWithSamba();
    						byte[] wajahSave=samba.openfile(user.getUserdata().getImageWajah());
    						FaceRecognition fr=new FaceRecognition(refTrx, trxType, request);
    						JSONObject respFace=fr.checkFacetoFace(Base64.encode(WajahItm.get()), Base64.encode(wajahSave), mitra.getId(), jsonRecv.getString("idktp").trim());
    						//LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
    						if(respFace.has("file")) {
								JSONObject resp=new JSONObject(respFace.toString());
								resp.put("file", "wiped");
								LogSystem.info(request, "response FR = "+resp.toString(), kelas, refTrx, trxType);
							}else {
								LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
							}
    						if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
								LogSystem.info(request,  "Face recognition info : " + respFace.getString("info"),kelas, refTrx,trxType);
								jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("info", "Verifikasi wajah gagal");
			    				LogSystem.info(request, "verifikasi user gagal foto dengan yang sebelumnya = "+jo,kelas, refTrx,trxType);
			    				return jo;
							} else	{
								LogSystem.info(request,  "Face recognition info : " + respFace.getString("info"),kelas, refTrx,trxType);
							}
    					}
    					
    					//jika mitra ekyc dengan buku tabungan
	    		        if(mitra.isEkyc_tabungan()==true) {
	    		        	String uploadTo = basepathPreReg+user.getUserdata().getId()+"/original/";
	                    	
	                    	boolean respSaveData=new SaveFileWithSamba().write(bukuTabunganItm.get(), uploadTo+"bukuTabungan.jpg");
	                    	if(respSaveData==false) {
	                    		jo.put("result", "91");
						        jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
						        return jo;
	                    	}
	                    	
	    		        	Rekening rek=new Rekening();
	    		        	rek.setImage_buku_tabungan(uploadTo+"bukuTabungan.jpg");
	    		        	rek.setKode_bank(jsonRecv.getString("kode_bank"));
	    		        	rek.setNo_rekening(jsonRecv.getString("no_rekening"));
	    		        	rek.setUserdata(user.getUserdata());
	    		        	new RekeningDao(db).create(rek);
	    		        	
	    		        	Userdata udata=user.getUserdata();
	    		        	udata.setLevel(mitra.getLevel());
	    		        	new UserdataDao(db).update(udata);
	    		        	
	    		        	if(user.getKode_user()!=null) {
	    		        		jo.put("kode_user", user.getKode_user());
	    		        	}
	    		        	
	    		        	if(user.getStatus()=='0') {
		    		        	user.setStatus('3');
		    		        	new UserManager(db).update(user);
		    		        }
	    		        	
	    		        	jo.put("result", "00"); 
		    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
		    		        try {
		    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
		    		        	logSystem.POST("registration", "success", "[API] Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign", Long.toString(user.getId()), null, null, null, null, jsonRecv.getString("idktp"));
		    		        }catch(Exception e)
		    		        {
		    		        	e.printStackTrace();
		    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
		    		        	LogSystem.error(request, e.toString());
		    		        }
		    		        return jo;
	    		        }
	    		        
	    		        //verifikasiDoubleLoginForLevelMitra verifikasi=new verifikasiDoubleLoginForLevelMitra(refTrx);
	    		        //JSONObject check=verifikasi.check(db, user, basepathPreReg, mitra, dukcapilItm, WajahItm, ktpItm, jsonRecv, request);
	    		        
	    		        VerifikasiDoubleLoginForLevelMitra2 verifikasi2=new VerifikasiDoubleLoginForLevelMitra2(refTrx);
	    		        JSONObject check=verifikasi2.check(db, user, basepathPreReg, mitra, dukcapilItm, WajahItm, ktpItm, jsonRecv, request);
	    		        
    					if(check!=null) {
    						if(!check.getString("result").equals("00")) {
    							jo=check;
    							return jo;
    						}
    						else {
    							//LogSystem.info(request, "update level user sesuai mitra berhasil");
    							LogSystem.info(request, "update level user sesuai mitra berhasil", kelas, refTrx,trxType);
    							if(user.getKode_user()!=null) {
    	    		        		jo.put("kode_user", user.getKode_user());
    	    		        	}
    							jo.put("result", "00"); 
    		    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign"); 
    		    		        
    		    		        
    		    		        String reSelfieTo = basepathPreReg+user.getUserdata().getId()+"/original/SelfieUlang"+new Date().getTime()+(30L * 24 * 60 * 60)+".jpg";

		                    	boolean reSelfie=new SaveFileWithSamba().write(WajahItm.get(), reSelfieTo);
		                    	
		                    	
		                    	if(reSelfie==false) {
		                    		LogSystem.info(request, "Gagal menyimpan selfie ulang", kelas, refTrx,trxType);
		                    		jo.put("result", "91");
							        jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
							        return jo;
		                    	}
    		    		        
    		    		        
    		    		        if(user.getStatus()=='4')
    		    		        { 
    			    		         jo.put("result", "78"); 
    			    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
    		    		        } else {
    		    		        	Userdata udata=user.getUserdata();
    		    		        	udata.setLevel("C4");
    		    		        	udata.setMitra(mitra);
    		    		        	udata.setNo_handphone(jsonRecv.get("tlp").toString().trim());
    		    		        	new UserdataDao(db).update(udata);
    		    		        	
    		    		        	if(user.getKode_user()!=null) {
    		    		        		jo.put("kode_user", user.getKode_user());
    		    		        	}
    		    		        }
    		    		        
    		    		        if(user.getStatus()=='0') {
    		    		        	user.setStatus('3');
    		    		        	new UserManager(db).update(user);
    		    		        }
    		    		        
    		    		        //Check sertifikat user yang sudah terdaftar sebelumnya
    	    					KmsService kms = new KmsService(request, "Cek Sertifikat Proses Registrasi/"+refTrx);
    	    					JSONObject cert = new JSONObject();
    				
    							 if(user.getMitra() != null)
    							 {
    								 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
    							 }
    							 else 
    							 {
    								 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), "");
    							 }
    							 
    							 if(!cert.has("result"))
    							 {
    								jo.put("result", "91");
    								jo.put("notif", "System timeout. silahkan coba kembali.");
    								
    								return jo;
    							 }
    							 
    							 if(cert.getString("result").length() > 3 || cert.getString("result").equals(""))
    							 {				
    								 jo.put("result", "91");
 							         jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
 							         return jo;
    							 }
    							 
    							 if(cert.has("expired-time"))
    							 {
    								 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    					             String raw = cert.getString("expired-time");
    					             Date expired=null;
									try {
										expired = sdf.parse(raw);
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										LogSystem.error(request, e.toString());
									}
    					             Date now= new Date();
    					             Long day = TimeUnit.DAYS.convert(expired.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
    					             LogSystem.info(request, "Jarak waktu expired : " + day,kelas, refTrx, trxType);
    							 }

    							 LogSystem.info(request, "CERT RESULT = "+cert.getString("result"), kelas, refTrx, trxType);
    							 if(cert.getString("result").equals("06") || cert.getString("result").equals("07"))
    			    		        {
    								 	LogSystem.info(request, "MASUK EXPIRED / REVOKE", kelas, refTrx, trxType);
    			    		        	//Cek lagi ke dukcapil untuk user terdaftar
    			    		        	if(devel==false) {
    				    					CheckToDukcapil dukcapil=new CheckToDukcapil(refTrx, trxType, request);
    										JSONObject job=dukcapil.check2(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
    										if(job!=null) {
    											LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
    											JSONObject data=new JSONObject();
    											data.put("nik", false);
    											data.put("name", false);
    											data.put("birthdate", false);
    											
    											if(job.has("content")) {
    												org.codehaus.jettison.json.JSONArray jarray=job.getJSONArray("content");
    												JSONObject jobj=jarray.getJSONObject(0);
    												if(jobj.has("NAMA_LGKP")){
    													data.put("nik", true);
    				    								String split[]=jobj.getString("NAMA_LGKP").split(" ");
    				    								if(split[0].equalsIgnoreCase("Sesuai")) {
    				    									data.put("name", true);
    				    								} 
    				    								if(jobj.getString("TGL_LHR").equalsIgnoreCase("Sesuai")){
    				    									data.put("birthdate", true);
    				    								}
    				    								jo.put("data", data);
    				    								if(data.getBoolean("name")==false||data.getBoolean("birthdate")==false) {
    				    									jo.put("result", "12");
    				    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
    				    				    				jo.put("info", "verifikasi text gagal.");
    				    				    				jo.put("data", data);
    				    				    				return jo;
    				    								}
    												} else {
    													if(jobj.has("RESPON")) {
    														if(jobj.getString("RESPON").equalsIgnoreCase("Kuota Akses Hari ini telah Habis")) {
    															jo.put("result", "91");
    										    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
    										    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
    										    				return jo;
    														}
    													}
    													jo.put("result", "12");
    								    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
    								    				jo.put("data", data);
    								    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
    								    				return jo;
    												}
    											} else {
    												jo.put("result", "91");
    							    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
    							    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
    							    				return jo;
    											}
    										} else {
    											jo.put("result", "91");
    						    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
    						    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
    						    				return jo;
    										}
    			    					}
    			    		        	
    			    		        	//Verifikasi asliri untuk user terdaftar
    			    		        	VerifikasiDoubleLoginForLevelMitra2 verifikasiUlang=new VerifikasiDoubleLoginForLevelMitra2(refTrx);
    			  	    		        JSONObject checkulang=verifikasiUlang.checkWithCertificate(db, user, basepathPreReg, mitra, dukcapilItm, WajahItm, ktpItm, jsonRecv, request);
    			  	    		        LogSystem.info(request, "HASIL CEK CHECKULANG "+checkulang,kelas, refTrx, trxType);
    			  	    		        if(checkulang!=null) {
    			    						if(!checkulang.getString("result").equals("00")) {
    			    							jo=checkulang;
    			    							return jo;
    			    						}
    			    					}
    			    					else {
    			    						LogSystem.error(request, "update level user gagal",kelas, refTrx, trxType);
    			    						jo.put("result", "06");
    			    						jo.put("notif", "Registrasi gagal");
    			    						jo.put("info", "General Error");
    			    						return jo;
    			    					}
    			  	    		        
    			  	    		    //Simpan data ke table verifikasi ulang
        								ReRegistration rereg = new ReRegistration();
        								rereg.setUserdata(user.getUserdata());
        								rereg.setDatetime(reregDate);
        								rereg.setSelfie_photo(reSelfieTo);
        								try 
        								{
        									new ReRegistrationDao(db).create(rereg);
        								}catch(Exception e)
        								{
        									e.printStackTrace();
        									LogSystem.error(request, "Gagal menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
        									LogSystem.error(getClass(), e);
        									LogSystem.error(request, e.toString());
        									jo.put("result", "06");
        									jo.put("notif", "verifikasi user gagal.");
        									jo.put("info", "Registrasi gagal");
        									return jo;
        								}
        								LogSystem.info(request, "Berhasil menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
    			    		        }
    							 
    							 	
    								
    								
    								try {
    			    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
    			    		        	logSystem.POST("registration", "success", "[API] Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign", Long.toString(user.getId()), null, null, null, null,jsonRecv.getString("idktp"));
    			    		        }catch(Exception e)
    			    		        {
    			    		        	LogSystem.error(request, e.toString());
    			    		        	e.printStackTrace();
    			    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
    			    		        }
    								
    		    		        return jo; 
    						}
    					}
    					else {
    						LogSystem.error(request, "update level user gagal",kelas, refTrx, trxType);
    						jo.put("result", "06");
    						jo.put("notif", "Registrasi gagal");
    						jo.put("info", "General Error");
    						return jo;
    					}
    					
    					
    				} else {
    					if(user.getKode_user()!=null) {
    		        		jo.put("kode_user", user.getKode_user());
    		        	}
    					if(!devel) 
    					{
	    					//Proses cek ulang nama, tanggal lahir dan foto selfie
	                    	LogSystem.info(request, "Proses cek ulang nama, tanggal lahir dan foto selfie", kelas, refTrx,trxType);
	                    	JSONObject dataUlang = new JSONObject();
		     				boolean name = false;
		     				boolean birthdate = false;
		     				boolean selfieStatus = false;
		     				
		     				if(user.getUserdata().getNama().equalsIgnoreCase(jsonRecv.getString("nama")))
		     				{
		     					name = true;
		     				}
		     				
		     				SimpleDateFormat sdfUlang = new SimpleDateFormat("dd-MM-yyyy");
		    				
		    				if(sdfUlang.format(user.getUserdata().getTgl_lahir()).equals(jsonRecv.getString("tgl_lahir")))
		     				{
		     					birthdate = true;
		     				}
		     				
		     				FileItem selfie=null;
		 					for (FileItem fileItem : fileSave) 
		 					{
		 						if(fileItem.getFieldName().equals("fotodiri"))
		 						{
		 				    		  selfie=fileItem;
		 				    		  break;
		 				      	}
		 					}
		     				
		     				FaceRecognition facerec = new FaceRecognition(refTrx, trxType, request);
		     				SaveFileWithSamba smb=new SaveFileWithSamba();
		 					byte[] selfieLama=smb.openfile(user.getUserdata().getImageWajah());
		 					JSONObject respUlang=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
		 					
		 					LogSystem.info(request, "Response check facetoface " + respUlang.toString());
		 					
		 					if(respUlang.getBoolean("result")==true || respUlang.getString("result").equals("00")) 
		 					{
		 						LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
		 						selfieStatus = true;
		 					}
		 					else
		 					{
		 						LogSystem.info(request, "photo tidak selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
		 					}
		 					
		     				if(!name || !birthdate || !selfieStatus)
		 					{
		     					dataUlang.put("name", name);
			 					dataUlang.put("birthdate", birthdate);
			 					dataUlang.put("selfie", selfieStatus);
			     				
			     				jo.put("data", dataUlang);
			     				
		 						jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal. Nama/Foto Selfie/Tanggal lahir tidak sesuai");
			    				if(selfieStatus)
			    				{
			    					jo.put("info", "verifikasi text gagal.");
			    				}
			    				jo.put("data", dataUlang);
			    				
			    				if(jo.has("email_registered"))
			    				{
			    					jo.remove("email_registered");
			    				}
			    				
			    				return jo;
		 					}
		     				//Selesai cek
    					}
    					
    					//Check sertifikat user yang sudah terdaftar sebelumnya
    					KmsService kms = new KmsService(request, "Cek Sertifikat Proses Registrasi/"+refTrx);
    					JSONObject cert = new JSONObject();
			
						 if(user.getMitra() != null)
						 {
							 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), user.getMitra().getId().toString());
						 }
						 else 
						 {
							 cert = kms.checkSertifikat(user.getId(), user.getUserdata().getLevel(), "");
						 }
						 
						 if(!cert.has("result"))
						 {
							jo.put("result", "91");
							jo.put("notif", "System timeout. silahkan coba kembali.");
							
							return jo;
						 }
						 
						 if(cert.getString("result").length() > 3  || cert.getString("result").equals(""))
						 {				
							 jo.put("result", "91");
						     jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
						     return jo;
						 }

    					
	    		        jo.put("result", "00"); 
	    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
	    		        
	    		        
	    		        String reSelfieTo = basepathPreReg+user.getUserdata().getId()+"/original/SelfieUlang"+new Date().getTime()+(30L * 24 * 60 * 60)+".jpg";
                    	
                    	boolean reSelfie=new SaveFileWithSamba().write(WajahItm.get(), reSelfieTo);
                    	
                    	if(reSelfie==false) {
                    		LogSystem.info(request, "Gagal menyimpan selfie ulang", kelas, refTrx,trxType);
                    		jo.put("result", "91");
					        jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
					        return jo;
                    	}
                    	
	    		        
	    		        LogSystem.info(request, "CERT RESULT = "+cert.getString("result"), kelas, refTrx, trxType);
	    		        
	    		        if(cert.has("expired-time"))
						 {
							 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				             String raw = cert.getString("expired-time");
				             Date expired=null;
							try {
								expired = sdf.parse(raw);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								LogSystem.error(request, e.toString());
								e.printStackTrace();
								LogSystem.error(request, e.toString());
							}
				             Date now= new Date();
				             Long day = TimeUnit.DAYS.convert(expired.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
				             LogSystem.info(request, "Jarak waktu expired : " + day,kelas, refTrx, trxType);
						 }

	    		        
	    		        if(cert.getString("result").equals("06") || cert.getString("result").equals("07"))
	    		        {
	    		        	LogSystem.info(request, "MASUK EXPIRED / REVOKE", kelas, refTrx, trxType);
	    		        	//Cek lagi ke dukcapil untuk user terdaftar
	    		        	if(devel==false) {
		    					CheckToDukcapil dukcapil=new CheckToDukcapil(refTrx, trxType, request);
								JSONObject job=dukcapil.check2(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
								if(job!=null) {
									LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
									JSONObject data=new JSONObject();
									data.put("nik", false);
									data.put("name", false);
									data.put("birthdate", false);
									
									if(job.has("content")) {
										org.codehaus.jettison.json.JSONArray jarray=job.getJSONArray("content");
										JSONObject jobj=jarray.getJSONObject(0);
										if(jobj.has("NAMA_LGKP")){
											data.put("nik", true);
		    								String split[]=jobj.getString("NAMA_LGKP").split(" ");
		    								if(split[0].equalsIgnoreCase("Sesuai")) {
		    									data.put("name", true);
		    								} 
		    								if(jobj.getString("TGL_LHR").equalsIgnoreCase("Sesuai")){
		    									data.put("birthdate", true);
		    								}
		    								jo.put("data", data);
		    								if(data.getBoolean("name")==false||data.getBoolean("birthdate")==false) {
		    									jo.put("result", "12");
		    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
		    				    				jo.put("info", "verifikasi text gagal.");
		    				    				jo.put("data", data);
		    				    				return jo;
		    								}
										} else {
											if(jobj.has("RESPON")) {
												if(jobj.getString("RESPON").equalsIgnoreCase("Kuota Akses Hari ini telah Habis")) {
													jo.put("result", "91");
								    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
								    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
								    				return jo;
												}
											}
											jo.put("result", "12");
						    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
						    				jo.put("data", data);
						    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
						    				return jo;
										}
									} else {
										jo.put("result", "91");
					    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
					    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
					    				return jo;
									}
								} else {
									jo.put("result", "91");
				    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
				    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
				    				return jo;
								}
	    					}
	    		        	
	    		        	//Verifikasi asliri untuk user terdaftar
	    		        	VerifikasiDoubleLoginForLevelMitra2 verifikasiUlang=new VerifikasiDoubleLoginForLevelMitra2(refTrx);
	  	    		        JSONObject checkulang=verifikasiUlang.checkWithCertificate(db, user, basepathPreReg, mitra, dukcapilItm, WajahItm, ktpItm, jsonRecv, request);
	  	    		        LogSystem.info(request, "HASIL CEK ULANG "+checkulang,kelas, refTrx, trxType);
	  	    		        if(checkulang!=null) {
	    						if(!checkulang.getString("result").equals("00")) {
	    							jo=checkulang;
	    							return jo;
	    						}
	    					}
	    					else {
	    						LogSystem.error(request, "update level user gagal",kelas, refTrx, trxType);
	    						jo.put("result", "06");
	    						jo.put("notif", "Registrasi gagal");
	    						jo.put("info", "General Error");
	    						return jo;
	    					}
	    		        	
	  	    		      //Simpan data ke table verifikasi ulang
							ReRegistration rereg = new ReRegistration();
							rereg.setUserdata(user.getUserdata());
							rereg.setDatetime(reregDate);
							rereg.setSelfie_photo(reSelfieTo);
							try 
							{
								new ReRegistrationDao(db).create(rereg);
							}catch(Exception e)
							{
								e.printStackTrace();
								LogSystem.info(request, "Gagal menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
								LogSystem.error(getClass(), e);
								LogSystem.error(request, e.toString());
								jo.put("result", "06");
								jo.put("notif", "verifikasi user gagal.");
								jo.put("info", "Verifikasi wajah gagal");
								return jo;
							}

							LogSystem.info(request, "Berhasil menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
	    		        }

	    		        if(user.getStatus()=='4')
	    		        { 
		    		         jo.put("result", "78"); 
		    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
	    		        }
	    		        
	    		        if(user.getStatus()=='0') {
	    		        	user.setStatus('3');
	    		        	new UserManager(db).update(user);
	    		        }
	    		        
	    		       
						
						try {
	    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	    		        	logSystem.POST("registration", "success", "[API] Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign", Long.toString(user.getId()), null, null, null, null,jsonRecv.getString("idktp"));
	    		        }catch(Exception e)
	    		        {
	    		        	e.printStackTrace();
	    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
	    		        	LogSystem.error(request, e.toString());
	    		        }
						
						
	    		        return jo; 
    				}
    		    } 
    			else 
    		    { 
    		        user= new UserManager(db).findByUsername2(jsonRecv.get("email").toString().toLowerCase());  
    		        
    		        if(user!=null) 
    		        { 
    		        	
		    		         nextProcess=false; 
		    		         jo.put("result", "14"); 
		    		         jo.put("notif", "Email sudah terdaftar dengan NIK lain, silahkan gunakan email lain."); 
		    		         if(user.getStatus()=='4') 
		    		         { 
			    		          jo.put("result", "78"); 
			    		          jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
		    		         } 
		    		         
		    		         if(!devel) 
		    		         {
		    		        	 //Pengecekan ulang data nama, tanggal lahir dan foto selfie
			     				JSONObject data = new JSONObject();
			     				boolean name = false;
			     				boolean birthdate = false;
			     				boolean selfieStatus = false;
			     				
			     				if(user.getUserdata().getNama().equalsIgnoreCase(jsonRecv.getString("nama")))
			     				{
			     					name = true;
			     				}
			     				
			     				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			    				
			    				if(sdf.format(user.getUserdata().getTgl_lahir()).equals(jsonRecv.getString("tgl_lahir")))
			     				{
			     					birthdate = true;
			     				}
			     				
			     				FileItem selfie=null;
			 					for (FileItem fileItem : fileSave) 
			 					{
			 						if(fileItem.getFieldName().equals("fotodiri"))
			 						{
			 				    		  selfie=fileItem;
			 				    		  break;
			 				      	}
			 					}
			     				
			     				FaceRecognition facerec = new FaceRecognition(refTrx, trxType, request);
			     				SaveFileWithSamba smb=new SaveFileWithSamba();
			 					byte[] selfieLama=smb.openfile(user.getUserdata().getImageWajah());
			 					JSONObject resp=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
			 					
			 					LogSystem.info(request, "Response check facetoface " + resp.toString());
			 					
			 					if(resp.getBoolean("result")==true || resp.getString("result").equals("00")) 
			 					{
			 						LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
			 						selfieStatus = true;
			 					}
			 					else
			 					{
			 						LogSystem.info(request, "photo tidak selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
			 					}
			 					
		     					
			 					data.put("name", name);
			     				data.put("birthdate", birthdate);
			     				data.put("selfie", selfieStatus);
			     				
			     				jo.put("data", data);
			     				
			 				
			     				if(!name || !birthdate || !selfieStatus)
			 					{
				    				if(selfieStatus)
				    				{
				    					jo.put("info", "verifikasi text gagal.");
				    				}
			 					}
			 					
			    		        return jo; 
		    		         }
    		        	
    		        } 
    		    }
    			
    			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16||jsonRecv.getString("idktp").length()>16||nikCheck(jsonRecv.getString("idktp"))) {
    				jo.put("result", "FE");
    				jo.put("notif", "Format NIK Salah");
    				
    				fNik=false;
    				return jo;
    			}
    			
    			
    			List<Userdata> userData=new UserdataDao(db).findByKtp(jsonRecv.get("idktp").toString());
    			
    			
//    			if(userData!=null) {
    			if(userData.size() > 0)
    			{
    				jo.put("result", "14");
    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
    				
    				UserManager um = new UserManager(db);
    				User UserEmail = um.findByUserData(userData.get(0).getId());
    				jo.put("email_registered", UserEmail.getNick());
    				if(!devel) 
					{
	    				JSONObject data = new JSONObject();
	    				boolean name = false;
	    				boolean birthdate = false;
	    				boolean selfieStatus = false;
	    				
	    				if(UserEmail.getUserdata().getNama().equalsIgnoreCase(jsonRecv.getString("nama")))
	    				{
	    					name = true;
	    				}
	    				
	    				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    				
	    				if(sdf.format(UserEmail.getUserdata().getTgl_lahir()).equals(jsonRecv.getString("tgl_lahir")))
	    				{
	    					birthdate = true;
	    				}
	    				
	    				FileItem selfie=null;
						for (FileItem fileItem : fileSave) 
						{
							if(fileItem.getFieldName().equals("fotodiri"))
							{
					    		  selfie=fileItem;
					    		  break;
					      	}
						}
	    				
	    				FaceRecognition facerec = new FaceRecognition(refTrx, trxType, request);
	    				SaveFileWithSamba smb=new SaveFileWithSamba();
						byte[] selfieLama=smb.openfile(UserEmail.getUserdata().getImageWajah());
						JSONObject resp=facerec.checkFacetoFace(Base64.encode(selfie.get()), Base64.encode(selfieLama), mitra.getId(), jsonRecv.getString("idktp"));
						if(resp.getBoolean("result")==true || resp.getString("result").equals("00")) {
							LogSystem.info(request, "photo selfie sama dengan yang sebelumnya", kelas, refTrx, trxType);
							selfieStatus = true;
						}
	    			
						data.put("name", name);
	    				data.put("birthdate", birthdate);
	    				data.put("selfie", selfieStatus);
	    				
	    				jo.put("data", data);
	    				
	    				if(!name || !birthdate || !selfieStatus)
	 					{
	        			    if(selfieStatus)
		    				{
		    					jo.put("info", "verifikasi text gagal.");
		    				}
	 					}
					}
    				
    				fNik=true;
    				return jo;
    			}
    			
    			UserdataDao udao=new UserdataDao(db);
    	  	  	List<Userdata> lu=udao.findByNoHp(jsonRecv.get("tlp").toString());
    	  	  	if(lu!=null) {
    		  	  	if(lu.size()>0) {
    			  	  	notif="No Handphone sudah digunakan. Gunakan No Handphone lain.";
    			  	  	jo.put("result", "15");
    					jo.put("notif", notif);
    					
    					return jo;
    		  	  	}
    	  	  	}
    	  	  	
    	  	  	PreRegistrationDao prdao=new PreRegistrationDao(db);
    	  	  	List<PreRegistration> lp=prdao.findNoHp(jsonRecv.get("tlp").toString());
    	  	  	if(lp!=null) {
    		  	  	if(lp.size()>0) {
    			  	  	notif="No Handphone sudah digunakan. Gunakan No Handphone lain.";
    			  	  	jo.put("result", "15");
    					jo.put("notif", notif);
    					
    					return jo;
    		  	  	}
    	  	  	}
    			
    	  	  	PreRegistration userdata=new PreRegistration();
    			if(nextProcess) {
    				try{
    					
    					FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
    					
    					//semua mitra check user by dukcapil
    					LogSystem.info(request, "kirim ke DUKCAPIL = "+devel,kelas, refTrx,trxType);
    					if(devel==false) {
    						CheckToDukcapil Checkdukcapil=new CheckToDukcapil(refTrx, trxType, request);
    						JSONObject ResDukcapil=Checkdukcapil.check2(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
    						if(ResDukcapil!=null) {
    							LogSystem.info(request, ResDukcapil.toString(), kelas, refTrx, trxType);
    							JSONObject data=new JSONObject();
    							data.put("nik", false);
    							data.put("name", false);
    							data.put("birthdate", false);
    							
    							if(ResDukcapil.has("content")) {
	    							org.codehaus.jettison.json.JSONArray jarray=ResDukcapil.getJSONArray("content");
	    							JSONObject jobj=jarray.getJSONObject(0);
	    							if(jobj.has("NAMA_LGKP")){
										data.put("nik", true);
	    								String split[]=jobj.getString("NAMA_LGKP").split(" ");
	    								if(split[0].equalsIgnoreCase("Sesuai")) {
	    									data.put("name", true);
	    								} 
	    								if(jobj.getString("TGL_LHR").equalsIgnoreCase("Sesuai")){
	    									data.put("birthdate", true);
	    								}
	    								jo.put("data", data);
	    								if(data.getBoolean("name")==false||data.getBoolean("birthdate")==false) {
	    									jo.put("result", "12");
	    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
	    				    				jo.put("info", "verifikasi text gagal.");
	    				    				return jo;
	    								}
									} else {
	    								if(jobj.has("RESPON")) {
											if(jobj.getString("RESPON").equalsIgnoreCase("Kuota Akses Hari ini telah Habis")) {
												jo.put("result", "91");
							    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
							    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
							    				return jo;
											}
										}
	    								jo.put("result", "12");
	    			    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
	    			    				jo.put("data", data);
	    			    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
	    			    				return jo;
	    							}
    							} else {
    								jo.put("result", "91");
        		    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
        		    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
        		    				return jo;
    							}
    						} else {
    							jo.put("result", "91");
    		    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
    		    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
    		    				return jo;
    						}
    					}
    					
						if(mitra.isVerifikasi()) {
							
							JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
							if(respFace.has("file")) {
								JSONObject resp=new JSONObject(respFace.toString());
								resp.put("file", "wiped");
								LogSystem.info(request, "response FR = "+resp.toString(), kelas, refTrx, trxType);
							}else {
								LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
							}
							
							if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
							    res="12";
							    notif=respFace.getString("info");
							    jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("info", notif);
			    				return jo;
							}
							
							String photowajah64=new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII);
							if(respFace.has("file")) {
								LogSystem.info(request, "menggunakan photo yg diputer<potrait>",kelas, refTrx,trxType);
								photowajah64=respFace.getString("file");
								WajahRotate=java.util.Base64.getDecoder().decode(photowajah64);
							}
							
							LogSystem.info(request, "kirim ke Lembaga Pemerintah",kelas, refTrx,trxType);
//							LogSystem.info(request, DSAPI.DUKCAPIL+"faceCheck.html|"+jsonRecv.getString("idktp")+"|"+mitra.getName(),kelas, refTrx,trxType);
							LogSystem.info(request, DSAPI.DUKCAPIL+"facecheck|"+jsonRecv.getString("idktp")+"|"+mitra.getName(),kelas, refTrx,trxType);
							CheckPhotoToAsliRI checkPhoto=new CheckPhotoToAsliRI();
							JSONObject jcheck = new JSONObject();
							
							jcheck = checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(), refTrx, request);
							
							
							
							JSONObject checkdata=new JSONObject();
							checkdata.put("nik", true);
							checkdata.put("name", true);
							checkdata.put("birthdate", true);
							//jo.put("data", checkdata);
							
	
							if(jcheck.getString("result").equals("23"))
							{
								jo.put("data", checkdata);
								jo.put("result", "12");
								jo.put("notif", "verifikasi user gagal.");
								jo.put("info", "Verifikasi wajah gagal. Size foto selfie maksimal 500KB");
			    				return jo;
							}
							
							if(jcheck.getString("result").equals("22"))
							{
								jo.put("data", checkdata);
								jo.put("result", "12");
								jo.put("notif", "verifikasi user gagal.");
								jo.put("info", "Verifikasi wajah gagal. Format foto selfie harus JPEG");
			    				return jo;
							}

							for(int i = 0 ; i<=3 ; i++)
							{
								if(jcheck.has("connection"))
								{
									if(!jcheck.getBoolean("connection"))
									{
										jcheck = checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(), refTrx, request);
									}
									else
									{
										i = 3;
									}
								}
							}
								
							if(jcheck.get("result").equals("00") || jcheck.get("result").equals("true")) {
								LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
								jo.put("data", checkdata);
								if(jcheck.has("dataFace")) {
									try {
										if(jcheck.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo")>65) {
											LogSystem.info(request, "Selfie photo > 65", kelas, refTrx, trxType);
										} else {
											jo.put("result", "12");
											jo.put("notif", "verifikasi user gagal.");
											jo.put("info", "Verifikasi wajah gagal");
						    				return jo;
										}
									} catch (Exception e) {
										e.printStackTrace();
										LogSystem.error(request, e.toString());
										// TODO: handle exception
										jo.put("result", "12");
										jo.put("notif", "verifikasi user gagal.");
										jo.put("info", "Verifikasi wajah gagal");
					    				return jo;
									}
									
								} else {
									jo.put("result", "12");
									jo.put("notif", "verifikasi user gagal.");
									jo.put("info", "Verifikasi wajah gagal");
				    				
				    				return jo;
								}
								
							} else {
								LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
								if(jcheck.has("connection")) {
									if(jcheck.getBoolean("connection")==false) {
										jo.put("result", "91");
					    				jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
					    				LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
					    				return jo;
									}
								}
								
								
								jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("data", checkdata);
			    				jo.put("info", "Verifikasi wajah gagal");
			    				if(jcheck.getString("information").equalsIgnoreCase("Saldo verifikasi habis") || jcheck.getString("information").equalsIgnoreCase("saldo tidak cukup")) {
//			    					jo.put("data", jcheck.getString("information"));
			    					jo.put("data", "Saldo verifikasi habis");
			    					jo.put("info", "Registrasi gagal");
			    				}
			    				
			    				LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
			    				return jo;
							}
							
							
						} 
						else {
							if(!mitra.isEkyc()) {
								if(!jsonRecv.has("ref_verifikasi") && !jsonRecv.has("data_verifikasi") && !jsonRecv.has("score_selfie")) {
									
									res="93";
							        notif="Message request tidak lengkap. ref_verifikasi, data_verifikasi, dan score_selfie, vnik";
							        jo.put("result", res);
							        jo.put("notif", notif);
							        LogSystem.info(request, jo.toString(),kelas, refTrx,trxType);
							        return jo;
								}
								
								if(!jsonRecv.has("vnik") && !jsonRecv.has("vnama") && !jsonRecv.has("vtgl_lahir") && !jsonRecv.has("vtmp_lahir")) {
									res="93";
							        notif="Message request tidak lengkap. vnik, vnama, vtgl_lahir dan vtmp_lahir";
							        jo.put("result", res);
							        jo.put("notif", notif);
							        LogSystem.info(request, jo.toString(),kelas, refTrx,trxType);
							        return jo;
								}
								
								/*
								if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
									res="12";
								    
								    jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								*/
								
								if(!jsonRecv.has("data_verifikasi")) {
									res="93";
							        notif="Message request tidak lengkap. data_verifikasi.";
							        jo.put("result", res);
							        jo.put("notif", notif);
							        LogSystem.info(request, jo.toString(),kelas, refTrx,trxType);
							        return jo;
								}
								if(!jsonRecv.has("score_selfie")) {
									res="93";
							        notif="Message request tidak lengkap. score_selfie.";
							        jo.put("result", res);
							        jo.put("notif", notif);
							        LogSystem.info(request, jo.toString(),kelas, refTrx,trxType);
							        return jo;
								}
								
								/*
								String dataverf=jsonRecv.getString("data_verifikasi");
								JSONObject joj=new JSONObject(dataverf);
								if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								*/
								
								String score=jsonRecv.getString("score_selfie");
								Float fscore=Float.valueOf(score);
								if(fscore<75) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "score selfie dibawah batas minimum.");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								
								
								//FaceRecognition fRec=new FaceRecognition();
								//JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId());
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
								//LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								if(respFace.has("file")) {
									JSONObject resp=new JSONObject(respFace.toString());
									resp.put("file", "wiped");
									LogSystem.info(request, "response FR = "+resp.toString(), kelas, refTrx, trxType);
								}else {
									LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								}
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
								    res="12";
								    notif=respFace.getString("info");
								    jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal. kecocokan photo selfie dan ktp tidak sama.");
				    				jo.put("info", notif);
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								} else {
									System.out.println("LOLOS");
								}
								
								/*
								VerificationDataDao vdd=new VerificationDataDao(db);
								VerificationData vdata=vdd.getByNik(jsonRecv.getString("idktp"));
								if(vdata==null) {
								
									vd = new VerificationData();
									vd.setName(jsonRecv.getString("nama").trim());
									vd.setNik(jsonRecv.getString("idktp").trim());
									vd.setScore(jsonRecv.getString("score_selfie"));
									vd.setTanggal_verifikasi(new Date());
									vd.setTanggal_verifikasi_foto(new Date());
									vd.setTempat_lahir(jsonRecv.getString("tmp_lahir").trim());
									vd.setTgl_lahir(jsonRecv.getString("tgl_lahir").trim());
									vd.setTimestamp_selfie(jsonRecv.getString("ref_verifikasi").trim());
									vd.setTimestamp_text(jsonRecv.getString("ref_verifikasi").trim());
									vd.setAlamat(jsonRecv.getString("alamat").trim());
									db.session().save(vd);
								}
								*/
								

								try {
						        	ActivityLog logSystem = new ActivityLog(request, refTrx);
						        	logSystem.POST("bio-verification", "success", "[API] Berhasil verifikasi biometrik non-ekyc. Data : " + jsonRecv.getString("data_verifikasi") + " Score " + jsonRecv.getString("score_selfie"), null, null, null, null, null,jsonRecv.getString("idktp"));
						        }catch(Exception e)
						        {
						        	e.printStackTrace();
						        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
						        	LogSystem.error(request, e.toString());
						        }
							}
							
							if(mitra.isDukcapil()) {
								if(dukcapilItm==null) {
									res="28";
							        notif="Data upload tidak lengkap.";
							        jo.put("result", res);
							        jo.put("notif", notif);
							        jo.put("info", "photo hasil dari dukcapil harus dikirim.");
							        return jo;
								}
								JSONObject respFace=fRec.checkFacetoFace(Base64.encode(dukcapilItm.get()), Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
								//LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								if(respFace.has("file")) {
									JSONObject resp=new JSONObject(respFace.toString());
									resp.put("file", "wiped");
									LogSystem.info(request, "response FR = "+resp.toString(), kelas, refTrx, trxType);
								}else {
									LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								}
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
									LogSystem.info(request,  "Face recognition info : " + respFace.getString("info"),kelas, refTrx,trxType);
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal. photo selfie dengan photo dukcapil tidak sama.");
				    				//jo.put("info", notif);
				    				LogSystem.info(request, "verifikasi user gagal foto dukcapil = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								else
								{
									LogSystem.info(request,  "Face recognition info : " + respFace.getString("info"),kelas, refTrx,trxType);
								}
							}
							
							if(mitra.isEkyc_tabungan()) {
								/*
								if(!jsonRecv.getString("kode_bank").trim().equalsIgnoreCase("014") && !jsonRecv.getString("no_rekening").trim().equalsIgnoreCase("0611104579")) {
									jo.put("result", "04");
									jo.put("notif", "verifikasi buku tabungan tidak sesuai.");
									jo.put("info", "Nama dan nomor rekening tidak sesuai");
									return jo;
								}
								*/
								if(jsonRecv.has("verifikasi_rekening")) {
									if(jsonRecv.getString("verifikasi_rekening").equals("") || jsonRecv.getString("verifikasi_rekening")==null) {
										LogSystem.info(request, "verifikasi rekening string kosong", kelas, refTrx, trxType);
									} else {
										jo.put("result", "12");
					    				jo.put("notif", "verifikasi user gagal. verifikasi rekening harus sukses");
					    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
					    				return jo;
									}
								} else {
									jo.put("result", "FE");
				    				jo.put("notif", "Sertakan field verifikasi_rekening.");
				    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
				    				return jo;
								}
								
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
								//LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								if(respFace.has("file")) {
									JSONObject resp=new JSONObject(respFace.toString());
									resp.put("file", "wiped");
									LogSystem.info(request, "response FR = "+resp.toString(), kelas, refTrx, trxType);
								}else {
									LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								}
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
								    res="12";
								    notif=respFace.getString("info");
								    jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal. kecocokan photo selfie dan ktp tidak sama.");
				    				jo.put("info", notif);
				    				LogSystem.info(request, jo.toString(),kelas, refTrx,trxType);
				    				return jo;
								} else {
									System.out.println("LOLOS");
								}
								
								/*
								CheckToDukcapil dukcapil=new CheckToDukcapil(refTrx, trxType, request);
								JSONObject job=dukcapil.check(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
								if(job!=null) {
									LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
									org.codehaus.jettison.json.JSONArray jarray=job.getJSONArray("content");
									JSONObject jobj=jarray.getJSONObject(0);
									if(jobj.has("RESPONSE_CODE")){
										if(jobj.getString("RESPONSE_CODE").equals("00")) {
											System.out.println("sukses");
											LogSystem.info(request, "sukses", kelas, refTrx, trxType);
										} else {
											jo.put("result", "12");
						    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
						    				
						    				LogSystem.response(request, jo, kelas, refTrx, trxType);
						    				return jo;
										}
									} else {
										jo.put("result", "12");
					    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
					    				LogSystem.response(request, jo, kelas, refTrx, trxType);
					    				return jo;
									}
								} else {
									jo.put("result", "91");
				    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
				    				LogSystem.response(request, jo, kelas, refTrx, trxType);
				    				return jo;
								}
								*/
							}
						}
						
						//PreRegistration userdata=new PreRegistration();
    					userdata.setType('1');
    			        userdata.setAlamat(jsonRecv.get("alamat").toString().trim());
    			        if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
    			        else  userdata.setJk('P');
    			        userdata.setKecamatan(jsonRecv.get("kecamatan").toString().trim());
    			        userdata.setKelurahan(jsonRecv.get("kelurahan").toString().trim());
    			        userdata.setKodepos(jsonRecv.get("kode-pos").toString().trim());
    			        userdata.setKota(jsonRecv.get("kota").toString().trim());
    			        userdata.setNama(jsonRecv.get("nama").toString().trim());
    			        userdata.setNo_handphone(jsonRecv.get("tlp").toString().trim());
    			        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
    			        userdata.setPropinsi(jsonRecv.get("provinci").toString().trim());
    			        userdata.setNo_identitas(jsonRecv.get("idktp").toString().trim());
    			        userdata.setTempat_lahir(jsonRecv.get("tmp_lahir").toString().trim());
    			        userdata.setEmail(jsonRecv.get("email").toString().toLowerCase().trim());
    			        userdata.setData_exists(false);
    			        userdata.setNpwp(jsonRecv.get("npwp").toString().trim());
    			        userdata.setMitra(mitra);
    			        if(fNik) {
    			        	userdata.setData_exists(fNik);
    			        	userdata.setNo_handphone(userData.get(0).getNo_handphone().trim());
    			        }
	    			        Long valueKodeUser=udataDao.getNext();
	    	 				SimpleDateFormat sdf=new SimpleDateFormat("yyMM");
	    			        String kodeuser=sdf.format(new Date())+String.format("%011d", Integer.parseInt(valueKodeUser.toString()));
    			        userdata.setKode_user(kodeuser);
    			        
    			        Boolean redirect=false;
    			        if(jsonRecv.has("redirect")) {
    		                if(!jsonRecv.getBoolean("redirect")) {
    		                  jo.put("result", "FE");
    		                    jo.put("notif", "redirect field harus diisi true");
    		                    return jo;
    		                }
    		                
    		                if(mitra.getActivation_redirect()==null) {
    		                  jo.put("result", "08");
    		                    jo.put("notif", "anda belum memasukan redirect link anda");
    		                    return jo;
    		                }
    		                
    		                redirect=true;
    		                                
    		            }
    			        userdata.setRedirect(redirect);
    			        if(mitra.isEkyc_tabungan()==true) {
    			        	userdata.setKode_bank(jsonRecv.getString("kode_bank").trim());
    			        	userdata.setNo_rekening(jsonRecv.getString("no_rekening").trim());
    			        }
    			        Long idUserData=udataDao.create(userdata);
    			        LogSystem.info(request, "IDUserData : " + idUserData,kelas, refTrx,trxType);
    			        //System.out.println("idUserData = "+idUserData);
    			        
    			      //jika ekyc menggunkan buku tabungan
						if(mitra.isEkyc_tabungan()==true) {
							String uploadTo = basepathPreReg+idUserData+"/original/";
							boolean respSaveData=new SaveFileWithSamba().write(bukuTabunganItm.get(), uploadTo+"bukuTabungan.jpg");
							userdata.setId(idUserData);
							if(respSaveData==false) {
	                    		jo.put("result", "91");
						        jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
						        udataDao.delete(userdata);
						        return jo;
	                    	} else {
	                    		userdata.setI_buku_tabungan(uploadTo+"bukuTabungan.jpg");
	                    		udataDao.update(userdata);
	                    	}
	                    	
	                    	/*
	    		        	Rekening rek=new Rekening();
	    		        	rek.setImage_buku_tabungan(uploadTo);
	    		        	rek.setKode_bank(jsonRecv.getString("kode_bank").trim());
	    		        	rek.setNo_rekening(jsonRecv.getString("no_rekening").trim());
	    		        	
	    		        	new RekeningDao(db).create(rek);
	    		        	*/
						}
						//END
    			        
    			        if(idUserData!=null) {
	    			        userdata.setId(idUserData.longValue());
	    			      
	    					//System.out.println("data size :" +fileSave.size());
	    					LogSystem.info(request, "Data size : " + fileSave.size(),kelas, refTrx,trxType);
	    					  if (fileSave.size() >=1) {
	    						  jo.put("kode_user", kodeuser);
	    				          res="00";
	    				          notif="Berhasil, silahkan check email untuk aktivasi akun anda.";
	    				          Long tgl = new Date().getTime()+(30L * 24 * 60 * 60 * 1000);
	    				  		  Date exp= new Date(tgl);
	    				  		  SimpleDateFormat sdfDate2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	    				          jo.put("expired_aktifasi", sdfDate2.format(exp)+" WIB");
	    		
	    						  String uploadTo = basepathPreReg+userdata.getId()+"/original/";
	    						  String directoryName = basepathPreReg+userdata.getId()+"/original/";
	    						  //String viewpdf = basepathPreReg+userdata.getId()+"/original/";
	    						  //File directory = new File(directoryName);
	    						  //if (!directory.exists()){
	    						  //     directory.mkdirs();
	    						  //}
	    						  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
	    		
	    		//				  Ttd ttd = new Ttd();
	    						  Date date = new Date();
	    						  String strDate = sdfDate.format(date);
	    						  String rename = "DS"+strDate+".png";
	    		//				  ttd.setUserdata(userdata);
	    		//                  ttd.setWaktu_buat(date);
	    						  boolean ktp =false;
	    						  boolean selfi=false;
	    						  SaveFileWithSamba samba=new SaveFileWithSamba();
	    						  boolean respSaveData=false;
	    						  try {
		    		                  for (FileItem fileItem : fileSave) {
//		    		                	  LogSystem.info(request, fileItem.getFieldName(), kelas, refTrx,trxType);
		    		                	  //System.out.println(fileItem.getFieldName());
		    		                	  if(fileItem.getFieldName().equals("ttd") && fileItem.getSize()!=0) {
		    		                		  //File fileTo = new File(uploadTo +fileItem.getFieldName()+".png");
					                		  BufferedImage jpgImage;
					                	        try {
					                	            jpgImage = ImageIO.read(fileItem.getInputStream());
					                	        }
					                	        catch (Exception e) {
					                	            e.printStackTrace();
					                	            LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
						                	        e.printStackTrace();
					                	        	e.getStackTrace();
					                	        	jo.put("result", "06");
												    jo.put("notif", "Data gagal diproses");
												      return jo;
					                	        }
					                	 
					                	      if(containsTransparency(jpgImage)==true) {
					                	    	  //fileItem.write(fileTo);
					                	    	  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".png");
					                	      } else {
					                	    	  int color = jpgImage.getRGB(0, 0);
						                          Image data=FileProcessor.makeColorTransparent(jpgImage, new Color(color), 10000000);
						                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,jpgImage.getWidth(),
						                        		  jpgImage.getHeight());
							                      //ImageIO.write(newBufferedImage, "png" ,fileTo);
							                      ByteArrayOutputStream baos=new ByteArrayOutputStream();
							                      ImageIO.write(newBufferedImage, "png" , baos);
							                      respSaveData=samba.write(baos.toByteArray(), uploadTo+fileItem.getFieldName()+".png");
					                	      }
					                	       
					                	      jpgImage.flush();
//					                    	  LogSystem.info(request, "File ttd :" + uploadTo+fileItem.getFieldName()+".png",kelas, refTrx,trxType);
					                    	  //System.out.println("File ttd :" + fileTo.getPath());
					                    	  fileItem.getOutputStream().close();
					                    	  userdata.setImageTtd(uploadTo+fileItem.getFieldName()+".png");
					                    	  fileItem.getOutputStream().close();
					                    	  if(respSaveData==false) {
					                    		  break;
					                    	  }
		    		                	  }
		    		                	  else if(fileItem.getFieldName().equals("fotodiri")){
		    		                		  //File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
		    		                    	  //fileItem.write(fileTo);
		    		                		  if(WajahRotate!=null) {
		    		                			  respSaveData=samba.write(WajahRotate, uploadTo+fileItem.getFieldName()+".jpg");
		    		                		  }else {
		    		                			  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
		    		                		  }
		    		                    	  userdata.setImageWajah(uploadTo+fileItem.getFieldName()+".jpg");
		    		                    	  selfi=true;
		    		                    	  fileItem.getOutputStream().close();
		    		                    	  if(respSaveData==false) {
					                    		  break;
					                    	  }
		    		                	  }else if(fileItem.getFieldName().equals("fotoktp")){
		    		                		  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
		    		                    	  userdata.setImageKTP(uploadTo+fileItem.getFieldName()+".jpg");
		    		                    	  ktp=true;
		    		                    	  fileItem.getOutputStream().close();
		    		                    	  if(respSaveData==false) {
					                    		  break;
					                    	  }
		    		                	  }else if(fileItem.getFieldName().equals("fotonpwp") && fileItem.getSize()!=0){
		    		                		  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
		    			            		  userdata.setImageNPWP(uploadTo+fileItem.getFieldName()+".jpg");
		    			            		  fileItem.getOutputStream().close();
		    			            		  if(respSaveData==false) {
					                    		  break;
					                    	  }
		    		                	  }else if(fileItem.getFieldName().equals("foto_dukcapil") && fileItem.getSize()!=0){
		    		                		  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
		    			            		  userdata.setImageWajahDukcapil(uploadTo+fileItem.getFieldName()+".jpg");
		    			            		  fileItem.getOutputStream().close();
		    			            		  if(respSaveData==false) {
					                    		  break;
					                    	  }
		    		                	  }
		    		                  }
		    		                  
	    						  }catch(Exception e)
	    						  {
	    							  e.printStackTrace();
	    							  jo.put("result", "06");
								      jo.put("notif", "Data gagal diproses");
								      return jo;
	    						  }
	    						  
	    						  if(respSaveData==false) {
	    		                	  	udataDao.delete(userdata);
	    		                	  	jo.put("result", "91");
								        jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
								        return jo;
	    		                  }
	    		                  
	    		                  if(ktp==true && selfi==true) {
	    				                udataDao.update(userdata);
	    				                //System.out.println("userdata ID : "+ userdata.getId());
	    				                LogSystem.info(request, "Userdata ID : "+ userdata.getId(),kelas, refTrx,trxType);
	    				                //MailSender mail=new MailSender(userdata,userdata.getEmail(),String.valueOf(userdata.getId()));
	    								//mail.run();
	    								
	    								String docs ="";
	    								
	    								String namamitra=mitra.getName();
	    								try {
	    								   docs = AESEncryption.encryptDoc(String.valueOf(userdata.getId()));
	    								   link = "https://"+DSAPI.DOMAIN+"/preregistration.html?preregister="
	    								     + URLEncoder.encode(docs, "UTF-8");
	    								    
	    								} catch (Exception e1) {
	    								   // TODO Auto-generated catch block
	    								   e1.printStackTrace();
	    								   LogSystem.error(request, e1.getMessage());
	    								}
	    								if(mitra.isNotifikasi()) {
		    				                SendActivasi sa=new SendActivasi(refTrx, kelas, trxType, request);
		    				                sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()), reg_number);
			    							
		    				                //sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()));
		    							}
	    								
	    								try {
	    			    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	    			    		        	logSystem.POST("registration", "success", "[API] Berhasil, silahkan check email untuk aktivasi akun anda.", null, null, null, null, null,jsonRecv.getString("idktp"));
	    			    		        }catch(Exception e)
	    			    		        {
	    			    		        	e.printStackTrace();
	    			    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
	    			    		        	LogSystem.error(request, e.toString());
	    			    		        }
	    						  }
	    		                  else {
	    		                	  	LogSystem.error(request, "Status KTP : "+ktp+" Selfie : " +selfi,kelas, refTrx, trxType);
	    		          		    	res="28";
	    		      				    notif="data upload tidak lengkap";
	    		      				    if(userdata.getImageKTP()!=null) {
	    		      				    	File file=new File(userdata.getImageKTP());
		    								file.delete();
	    		      				    }
	    		      				    if(userdata.getImageWajah()!=null) {
	    		      				    	File file=new File(userdata.getImageWajah());
		    								file.delete();
	    		      				    }
	    		      				    if(userdata.getImageTtd()!=null) {
	    		      				    	File file=new File(userdata.getImageTtd());
		    								file.delete();
	    		      				    }
	    		      				    
	    		      				    udataDao.delete(userdata);
	    		                  }
    					  		}
	    					  else {
	    						  LogSystem.error(request, "File.size < 1",kelas, refTrx, trxType);
	    						  notif="data upload tidak lengkap";
	    						  	if(userdata.getImageKTP()!=null) {
	    						  		File file=new File(userdata.getImageKTP());
	    								file.delete();
	  		      				    }
	  		      				    if(userdata.getImageWajah()!=null) {
	  		      				    	File file=new File(userdata.getImageWajah());
		    							file.delete();
	  		      				    }
	  		      				    if(userdata.getImageTtd()!=null) {
	  		      				    	File file=new File(userdata.getImageTtd());
		    							file.delete();
	  		      				    }
	    						  udataDao.delete(userdata);
	    					  }
    					  } else {
    						  	LogSystem.error(request, "IdUserdata null",kelas, refTrx, trxType);
    						  	if(userdata.getImageKTP()!=null) {
	  						  		File file=new File(userdata.getImageKTP());
	  								file.delete();
		      				    }
		      				    if(userdata.getImageWajah()!=null) {
		      				    	File file=new File(userdata.getImageWajah());
	    							file.delete();
		      				    }
		      				    if(userdata.getImageTtd()!=null) {
		      				    	File file=new File(userdata.getImageTtd());
	    							file.delete();
		      				    }
		      				    
    						  udataDao.delete(userdata);
    						  res="28";
    						  notif="Data registrasi tidak lengkap";
    						  //if(jsonRecv.get("alamat").toString().length()>50) {notif="teks alamat maksimum 50 karakter";}
    						  //if(jsonRecv.get("kecamatan").toString().length()>30) {notif="teks kecamatan maksimum 30 karakter";}
    						  //if(jsonRecv.get("kelurahan").toString().length()>30) {notif="teks kelurahan maksimum 30 karakter";}
    						  //if(jsonRecv.get("kota").toString().length()>30) {notif="teks kota maksimum 30 karakter";}
    						  //if(jsonRecv.get("nama").toString().length()>50) {notif="teks nama maksimum 30 karakter";}
    						  //if(jsonRecv.get("provinci").toString().length()>50) {notif="teks provinsi maksimum 30 karakter";}
    						  if(jsonRecv.get("email").toString().length()>80) {notif="teks email maksimum 80 karakter";}
    					  }
    			        	
    				}
    			
    				catch (Exception e) {
    					e.printStackTrace();
    					// TODO: handle exception
    					udataDao.delete(userdata);
    					LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
    				    res="06";
    			        notif="Data gagal diproses";
    				}
    			}
//    		}
        }
        else {
        	//jo=aVerf.setResponFailed(jo);
        	notif="UserId atau Password salah";
        }
        
        jo.put("result", res);
        jo.put("notif", notif);
        jo.put("info", link);
        LogSystem.info(request, "Response akhir : " + jo,kelas, refTrx,trxType);
        return jo;
	}
	
	
	private static boolean containsTransparency(BufferedImage image){
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (isTransparent(image, j, i)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTransparent(BufferedImage image, int x, int y ) {
        int pixel = image.getRGB(x,y);
        return (pixel>>24) == 0x00;
    }
    
    public static boolean isValid(String email) 
    { 
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
                            "[a-zA-Z0-9_+&*-]+)*@" + 
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
                            "A-Z]{2,7}$"; 
                              
        Pattern pat = Pattern.compile(emailRegex); 
        if (email == null) 
            return false; 
        return pat.matcher(email).matches(); 
    } 
    
    public boolean nameCheck(String toExamine) {
        return !INVALID_CHARS_PATTERN_NAME.matcher(toExamine).matches();
    }
    
    public boolean nikCheck(String toExamine) {
        return !INVALID_CHARS_PATTERN_NIK.matcher(toExamine).matches();
    }
	
}
