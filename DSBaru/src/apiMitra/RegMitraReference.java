package apiMitra;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import org.apache.derby.impl.jdbc.authentication.LDAPAuthenticationSchemeImpl;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONString;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import api.dukcapil.checkingData;
import api.email.SendSuksesRegistrasi;
import api.log.ActivityLog;
import api.verifikasi.CheckPhotoToAsliRI;
import api.verifikasi.CheckToDukcapil;
import api.verifikasi.VerifikasiDoubleLoginForLevelMitra2;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.billing.SistemBilling;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.EeuserMitraDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.ReRegistrationDao;
import id.co.keriss.consolidate.dao.RegLogDao;
import id.co.keriss.consolidate.dao.TmpUsernameDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.VerificationDataDao;
import id.co.keriss.consolidate.dao.VerifikasiManualDao;
import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.ReRegistration;
import id.co.keriss.consolidate.ee.RegistrationLog;
import id.co.keriss.consolidate.ee.TMPUsername;
import id.co.keriss.consolidate.ee.RefRegistrasi;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.ee.VerifikasiManual;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;
import net.minidev.json.parser.JSONParser;


public class RegMitraReference extends ActionSupport {

	static String basepath="/file2/data-DS/UploadFile/";
	static String basepathPreReg="/file2/data-DS/PreReg/";
	static String basepathRegLog="/file2/data-DS/LogRegistrasi/";
	private static final Pattern INVALID_CHARS_PATTERN_NAME = Pattern.compile("[a-zA-Z \\.\\,]*");
	private static final Pattern INVALID_CHARS_PATTERN_NIK = Pattern.compile("[0-9]*");
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx="";
	String kelas="apiMitra.RegMitraReference";
	String trxType="REGS";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="REGS"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		
		Mitra mitra=null;
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
					LogSystem.info(request, "Bukan multipart",kelas, refTrx, trxType);
					//JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
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
						jo.put("result", "91");
						jo.put("notif", "System timeout. silahkan coba kembali.");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						LogSystem.error(request, e.toString() , kelas, refTrx, trxType);
						return;
					}
					//System.out.println("token adalah = "+token);
					if(tm!=null) {
						LogSystem.info(request, "Token ada : " +  token,kelas, refTrx, trxType);
						mitra=tm.getMitra();
						Mitra RL_mitra = mitra;
			        	RL.setMitra(RL_mitra);
					} else {
						LogSystem.error(request, "Token null",kelas, refTrx, trxType);
						//JSONObject jo=new JSONObject();
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
					try
					{
						LogSystem.info(request, "Reading request", kelas, refTrx, trxType);
						fileItems = upload.parseRequest(request);
						LogSystem.info(request, "Complete reading request", kelas, refTrx, trxType);
					}catch(Exception e)
					{
						LogSystem.info(request, "Timeout read request", kelas, refTrx, trxType);
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
							 
							 if(fileItem.getFieldName().equals("ttd")||fileItem.getFieldName().equals("fotodiri")||fileItem.getFieldName().equals("fotoktp")||fileItem.getFieldName().equals("fotonpwp")){
								 fileSave.add(fileItem);
								 
							 }
							
						}
						String FileUploading = "["+fileItem.getFieldName()+"]:{"+fileItem.getName()+","+fileItem.getSize()+"};";
						LogSystem.info(request,"RL_CHECK File REQUEST>"+ FileUploading, kelas, refTrx, trxType);
						if(FileUploading !=null && !fileItem.getFieldName().equals("jsonfield")) {
							array.add(FileUploading) ;
							LogSystem.info(request,"Array >"+ array);
						}
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems,kelas, refTrx, trxType);
	         if(jsonString==null) { 
				 	respon = "jsonfield NULL (trxjson)";
	    		    LogSystem.info(request, "Response String: " + respon);
	    		    RL.setMessage_response(respon);
	            	new RegLogDao(db).create(RL);
	            	return;	       
			 }         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         if(jsonRecv==null) {
	        	 //JSONObject jo=new JSONObject();
	        	 jo.put("result", "30");
				 jo.put("notif", "Format request API salah.");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				 LogSystem.response(request, jo, kelas, refTrx, trxType);
				 respon = jo.toString();
	    		 LogSystem.info(request, "Response String: " + respon);
	    		 RL.setMessage_response(respon);
	             new RegLogDao(db).create(RL);
				 return;
	         }
	         
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
		       	 Date RL_tgl_lahir = new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString());
		       	 RL.setTgl_lahir(RL_tgl_lahir);
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
	              	Boolean RL_DataExist = false;
	  	         	RL.setRedirect(RL_DataExist);
	            }
	        }
	        ////////////////////////////////////////////////REGLOG FILES/////////////////////////////////////////
	        String uploadToRegLog = basepathRegLog+refTrx+"/original/";
			SaveFileWithSamba samba=new SaveFileWithSamba();
	        for (FileItem fileItem : fileSave) {
	      	 LogSystem.info(request, fileItem.getFieldName());
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
	          	  LogSystem.info(request, "File ttd :" + uploadToRegLog+fileItem.getFieldName()+".png");
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
	      	  }
	        }
        //////////////////(FINISH)//////////////////REQUEST REGISTRATION LOG CONTENT////////////////////////////
	         
	         
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
	         
	         String userid=jsonRecv.getString("userid").toLowerCase();
	         
	         UserManager user=new UserManager(getDB(context));
	         User eeuser=user.findByUsername(userid);
	         if(eeuser!=null) {
	        	 if(eeuser.getMitra().getId()==mitra.getId() && eeuser.isAdmin()) {
	        		 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
	        		 //System.out.println("token dan mitra valid");
	        	 }
	        	 else {
	        		 LogSystem.error(request, "Token dan mitra tidak valid",kelas, refTrx, trxType);
	        		 //JSONObject jo=new JSONObject();
					 jo.put("result", "55");
					 jo.put("notif", "Token dan Userid tidak sesuai");
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
	        	 LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
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
	         
	         
	         //Pengecekan Registrasi lebih dari 3x
	         List<RegistrationLog> lregLog=new RegLogDao(db).findByMitraNikEmail(mitra.getId(), jsonRecv.getString("idktp"), jsonRecv.getString("email").toLowerCase());
	         if(lregLog.size()>3) {
	        	 int x=0;
	        	 for(RegistrationLog reg: lregLog){
	        		 String resp=reg.getMessage_response();
	        		 JSONObject object=new JSONObject(resp);
	        		 if(object.getString("result").equals("12")) {
	        			 x++;
	        		 }
	        	 }
	        	 
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
	         
	         //PROSES REGISTRASI
	         jo=register(mitra, jsonRecv, fileSave, context, request, refTrx);
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);
			respon = jo.toString();
		    LogSystem.info(request, "Response String: " + respon);
		    RL.setMessage_response(respon);
        	new RegLogDao(db).create(RL);
        	
        	/*
        	if(!jo.getString("result").equals("00")) {
	        	if(jo.has("data")) {
	        		JSONObject object=jo.getJSONObject("data");
	        		VerifikasiManualDao vdao=new VerifikasiManualDao(db);
	        		if(object.has("selfie_match")) {
	        			boolean selfie=object.getBoolean("selfie_match");
	        			if(selfie==true) {
	        				VerifikasiManual vm=new VerifikasiManual();
	        				vm.setEmail(jsonRecv.getString("email"));
	        				vm.setNik(jsonRecv.getString("idktp"));
	        				vm.setId_registration_log(RL);
	        				vdao.create(vm);
	        			}
	        		} else {
	        			VerifikasiManual vm=new VerifikasiManual();
        				vm.setEmail(jsonRecv.getString("email"));
        				vm.setNik(jsonRecv.getString("idktp"));
        				vm.setId_registration_log(RL);
        				vdao.create(vm);
	        		}
	        	}
	        }
        	*/
        	
			context.put("trxjson", res);
			LogSystem.response(request, jo, kelas, refTrx, trxType+"/"+mitra.getName());

			
		}catch (Exception e) {
			LogSystem.error(request, e.toString());
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//			error (context, e.toString());
//            context.getSyslog().error (e);
//			log.error(e);
            //JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data request tidak ditemukan");
				respon = jo.toString();
    		    LogSystem.info(request, "Response String: " + respon);
    		    RL.setMessage_response(respon);
    		    try {
    		    	new RegLogDao(db).create(RL);
    		    }catch(Exception error)
    		    {
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
	
	
	
	JSONObject register(Mitra mitra,JSONObject jsonRecv,List<FileItem> fileSave,JPublishContext context, HttpServletRequest request, String refTrx) throws JSONException{
		Boolean default_ttd = false;
		VerificationData vd=null;		
		User userRecv;
		DB 	db = getDB(context);
        JSONObject jo=new JSONObject();
        String res="06";
        String notif="Registrasi gagal";
		List<Userdata> userData= null;
		User login =new User();
		Date reregDate = new Date();
		Alamat alamat=new Alamat();
		UserdataDao udataDao=new UserdataDao(db);
		//User user=null;
		boolean fNik=false;
		FileItem ktpItm = null, WajahItm = null, ttditm = null, npwpItm = null;
		byte[] WajahRotate=null;
		for (FileItem fileItem : fileSave) {
	    	  if(fileItem.getFieldName().equals("fotodiri")){
	    		  WajahItm=fileItem;
	    		  String extensi = FilenameUtils.getExtension(fileItem.getName());
	    		  LogSystem.info(request, "Extensi selfie = "+extensi,kelas, refTrx, trxType);
	    		  String ext2 = fileItem.getContentType();
	    		  if(ext2!=null)LogSystem.info(request, "type selfie = "+ext2,kelas, refTrx, trxType);
	    		  
	      	  }else   if(fileItem.getFieldName().equals("fotoktp")){
	          	  ktpItm=fileItem;
	          	  String ext2 = fileItem.getContentType();
	    		  LogSystem.info(request, "type ktp = "+ext2,kelas, refTrx, trxType);
	    		  
	      	  }else if(fileItem.getFieldName().equals("ttd")) {
	      		  ttditm=fileItem;
	      		  String ext2 = fileItem.getContentType();
	    		 
	    		  LogSystem.info(request, "type ttd = "+ext2,kelas, refTrx, trxType);
	    		  
	      	  }else if(fileItem.getFieldName().equals("fotonpwp")) {
	      		  npwpItm=fileItem;
	      		  String ext2 = fileItem.getContentType();
	    		 
	    		  LogSystem.info(request, "type npwp = "+ext2,kelas, refTrx, trxType);
	    		  
	      	  }
	        }
        
        if(WajahItm==null || ktpItm==null /*|| ttditm==null*/) {
        	LogSystem.info(request, "KTP atau Selfie NULL", kelas, refTrx,trxType);
		    res="28";
	        notif="Data upload tidak lengkap";
	        jo.put("result", res);
	        jo.put("notif", notif);
	        jo.put("info", "harap upload photo ektp dan wajah selfie anda.");
	        return jo;
        }
        
        

        if(jsonRecv.get("tlp").toString().length()<8) 
  	  	{
  	  		notif="teks tlp kurang dari 8 karakter";
	  	  	jo.put("result", "FE");
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
		    	  	  	jo.put("result", "FE");
		    			jo.put("notif", notif);
		    			
		    			return jo;
		            }
	  	  		}
	            
	            if(i==0) {
	            	if(dataInput[0]!='0' && dataInput[0]!='6' && dataInput[0]!='+') {
	            		notif="format hp harus 62, 08 atau +62";
		    	  	  	jo.put("result", "FE");
		    			jo.put("notif", notif);
		    			
		    			return jo;
	            	}
	            }
	            if(i==1) {
	            	if(dataInput[1]!='8' && dataInput[1]!='2' && dataInput[1]!='6') {
	            		notif="format hp harus 62, 08 atau +62";
		    	  	  	jo.put("result", "FE");
		    			jo.put("notif", notif);
		    			
		    			return jo;
	            	}
	            }
	            if(i==2) {
	            	if(dataInput[0]=='+') {
	            		if(dataInput[2]!='2') {
	            			notif="format hp harus 62, 08 atau +62";
			    	  	  	jo.put("result", "FE");
			    			jo.put("notif", notif);
			    			
			    			return jo;
	            		}
	            	}
	            }
	            
	          }
  	  	}
        
        

//  	  	}
  	  	if(jsonRecv.get("nama").toString().length()>128) 
  	  	{
  	  		notif="teks nama maksimum 128 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
  	  	}

  	  	if(jsonRecv.get("email").toString().length()>80) 
  	  	{
  	  		notif="teks email maksimum 80 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
  	  	}
 	  	if(jsonRecv.get("tgl_lahir").toString().length()<10 || jsonRecv.get("tgl_lahir").toString().length()>10) 
	  	{
	  		notif="teks tanggal lahir harus diisi dan maksimum 10 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
	  	}
  		if(jsonRecv.get("tmp_lahir").toString().length()>100) 
	  	{
	  		notif="teks tmp lahir maksimum 100 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
	  	}
  		if(jsonRecv.get("kode-pos").toString().length()>10) 
	  	{
	  		notif="teks kode pos maksimum 10 karakter";
	  	  	jo.put("result", "FE");
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
  		
  		if(jsonRecv.get("jenis_kelamin").toString().length()>10) 
	  	{
	  		notif="teks jenis kelamin maksimum 10 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
	  	}
  		
  		if(!isValid(jsonRecv.getString("email").trim())) {
	  	  	jo.put("result", "FE");
			jo.put("notif", "Format email salah");
			
			return jo;
  	  	}
  		
        if(jsonRecv.has("default_ttd"))
        {
        	default_ttd = jsonRecv.getBoolean("default_ttd");
        }
           
        User user=null;
        user= new UserManager(db).findByUserNik(jsonRecv.getString("email").toLowerCase(), jsonRecv.getString("idktp"));
        boolean devel=DSAPI.DEVELOPMENT;
        if(user!=null){
			//notif="Nomor KTP sudah terdaftar dengan email lain";
			//User user= new UserManager(db).findByUsername(jsonRecv.get("email").toString());
			//if(user!=null) {
				//if(user.getNick().equalsIgnoreCase(jsonRecv.getString("email"))) {
        	int leveluser=Integer.parseInt(user.getUserdata().getLevel().substring(1));
        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
        	if((levelmitra>2 && leveluser<3) || user.getStatus()=='0') {
        		LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA",kelas, refTrx, trxType);
        		
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
        		
				VerifikasiDoubleLoginForLevelMitra2 verifikasi2=new VerifikasiDoubleLoginForLevelMitra2(refTrx);
		        JSONObject check=verifikasi2.check(db, user, basepathPreReg, mitra, null, WajahItm, ktpItm, jsonRecv, request);
		        
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
					        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
					        return jo;
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
                    	
	    		        if(user.getStatus()=='4')
	    		        { 
		    		         jo.put("result", "78"); 
		    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
	    		        } else {
	    		        	Userdata udata=user.getUserdata();
	    		        	udata.setLevel("C4");
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
						 
						 if(cert.getString("result").length() > 3  || cert.getString("result").equals(""))
						 {				
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
		  	    		        JSONObject checkulang=verifikasiUlang.checkWithCertificate(db, user, basepathPreReg, mitra, null, WajahItm, ktpItm, jsonRecv, request);
		  	    		        LogSystem.info(request, "HASIL CEK LANG"+checkulang,kelas, refTrx, trxType);
		  	    		        if(checkulang!=null) {
		    						if(!checkulang.getString("result").equals("00")) {
		    							jo=check;
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
									LogSystem.error(request, "Gagal menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
									LogSystem.error(getClass(), e);
									LogSystem.error(request, e.toString());
									jo.put("result", "06");
									jo.put("notif", "verifikasi user gagal.");
									jo.put("info", "Verifikasi wajah gagal");
									return jo;
								}
								LogSystem.info(request, "Berhasil menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
								
		    		        }
							
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
				}
				else {
					LogSystem.error(request, "update level user gagal",kelas, refTrx, trxType);
					jo.put("result", "06");
					jo.put("notif", "Registrasi gagal");
					jo.put("info", "General Error");
					return jo;
				}
				
				
        	}
        	else {
        		
        		String reSelfieTo = basepathPreReg+user.getUserdata().getId()+"/original/SelfieUlang"+new Date().getTime()+(30L * 24 * 60 * 60)+".jpg";
              	
	              	boolean reSelfie=new SaveFileWithSamba().write(WajahItm.get(), reSelfieTo);
	              	
	              	if(reSelfie==false) {
	              		LogSystem.info(request, "Gagal menyimpan selfie ulang", kelas, refTrx,trxType);
	              		jo.put("result", "91");
					        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
					        return jo;
	              	}
              	
					notif="Registrasi berhasil. Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign";	
					res="00";
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
	  	    		        JSONObject checkulang=verifikasiUlang.checkWithCertificate(db, user, basepathPreReg, mitra, null, WajahItm, ktpItm, jsonRecv, request);
	  	    		        LogSystem.info(request, "HASIL CEK ULANG"+checkulang,kelas, refTrx, trxType);
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
								LogSystem.error(request, "Gagal menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
								LogSystem.error(getClass(), e);
								LogSystem.error(request, e.toString());
								jo.put("result", "06");
								jo.put("notif", "verifikasi user gagal.");
								jo.put("info", "Verifikasi wajah gagal");
								return jo;
							}
							LogSystem.info(request, "Berhasil menyimpan data registrasi ulang " + jsonRecv.getString("idktp"), kelas, refTrx, trxType);
							
	    		        }
					 
					
						
						try {
	    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	    		        	logSystem.POST("registration", "success", "[API] Registrasi berhasil. Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign", Long.toString(user.getId()), null, null, null, null, jsonRecv.getString("idktp"));
	    		        }catch(Exception e)
	    		        {
	    		        	e.printStackTrace();
	    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
	    		        	LogSystem.error(request, e.toString());
	    		        }
						
					jo.put("result", res);
			        jo.put("notif", notif);
			        return jo;
        	}
					
				//}
				
			//}
		}else if(new UserManager(db).findByUsername2(jsonRecv.get("email").toString().toLowerCase())!=null) {
			notif="Email sudah terdaftar. Gunakan email lain.";
			res="14";
			jo.put("result", res);
	        jo.put("notif", notif);
	        return jo;
		}else {
			
			
			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16||jsonRecv.getString("idktp").length()>16||nikCheck(jsonRecv.getString("idktp"))) {
				jo.put("result", "FE");
				jo.put("notif", "Format NIK Salah");
				
				fNik=false;
				return jo;
			}
			
					userData=udataDao.findByKtp(jsonRecv.getString("idktp"));
					Userdata userdata=new Userdata();
					//if((userdata=udataDao.findByKtp(jsonRecv.getString("idktp")))!= null) {
					if(userData.size()>0)
					{	
						fNik=true;
						jo.put("result", "14");
	    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
	    				
	    				UserManager um = new UserManager(db);
	    				User UserEmail = um.findByUserData(userData.get(0).getId());
	    				jo.put("email_registered", UserEmail.getNick());
	    				if(!devel)
	    				{
		    				//Pengecekan ulang data nama, tanggal lahir dan foto selfie
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
	    				return jo;
					}
					else {
						//check No handphone
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
					  	
						
						//Checking DUKCAPIL
						if(devel==false && mitra.getId()!=DSAPI.LONTONG ) {
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
							FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
							JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
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
							
							LogSystem.info(request, "kirim ke lembaga pemerintah",kelas, refTrx,trxType);
							CheckPhotoToAsliRI checkPhoto=new CheckPhotoToAsliRI();
//							JSONObject jcheck= checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName());
							JSONObject jcheck = new JSONObject();
							jcheck = checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(),refTrx, request);
							
							
							JSONObject checkdata=new JSONObject();
								checkdata.put("nik", true);
								checkdata.put("name", true);
								checkdata.put("birthdate", true);
								
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
								
								
								for(int i=0 ; i<=3 ; i++)
								{
									if(jcheck.has("connection"))
									{
										if(!jcheck.getBoolean("connection"))
										{
											jcheck= checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(),refTrx, request);
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
								LogSystem.error(request, jcheck.toString(), kelas, refTrx, trxType);
								if(jcheck.has("connection")) {
									if(jcheck.getBoolean("connection")==false) {
										jo.put("result", "91");
					    				jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
					    				LogSystem.response(request, jcheck, kelas, refTrx, trxType);
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
								
			    				LogSystem.response(request, jcheck, kelas, refTrx, trxType);
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
							        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
							        return jo;
								}
								
								if(!jsonRecv.has("vnik") && !jsonRecv.has("vnama") && !jsonRecv.has("vtgl_lahir") && !jsonRecv.has("vtmp_lahir")) {
									res="93";
							        notif="Message request tidak lengkap. vnik, vnama, vtgl_lahir dan vtmp_lahir";
							        jo.put("result", res);
							        jo.put("notif", notif);
							        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
							        return jo;
								}
								
								
								
								String score=jsonRecv.getString("score_selfie");
								Float fscore=Float.valueOf(score);
								if(fscore<75) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "score selfie dibawah batas minimum.");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
				    				return jo;
								}
								
								FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
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
				    				jo.put("notif", "verifikasi user gagal. kecocokan photo selfie dan ktp : false");
				    				jo.put("info", notif);
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
				    				return jo;
								} else {
									System.out.println("LOLOS");
								}
								

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
							
							LogSystem.info(request, "level = "+mitra.getLevel(),kelas, refTrx, trxType);
							if(mitra.getLevel().equals("C2")) {
								FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
								//JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId());
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
								//LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
								//LogSystem.info(request, respFace.toString(), kelas, refTrx, trxType);
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
				    				LogSystem.info(request, "verifikasi user gagal level 2 = "+jo,kelas, refTrx, trxType);
				    				return jo;
								} else {
									System.out.println("LOLOS");
								}
							}
						}
						
					}
			        Transaction tx = db.session().getTransaction();
			        tx.setTimeout(300);
			        tx.begin();
		
					try{
						if(!fNik) {
					        //userdata.setAlamat(jsonRecv.get("alamat").toString());
					        if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
					        else  userdata.setJk('P');
					        //userdata.setKecamatan(jsonRecv.get("kecamatan").toString());
					        //userdata.setKelurahan(jsonRecv.get("kelurahan").toString());
					        //userdata.setKodepos(jsonRecv.get("kode_pos").toString());
					        //userdata.setKota(jsonRecv.get("kota").toString());
					        userdata.setNama(jsonRecv.get("nama").toString().trim());
					        userdata.setNo_handphone(jsonRecv.get("tlp").toString().trim());
					        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
					        //userdata.setPropinsi(jsonRecv.get("provinci").toString());
					        userdata.setNo_identitas(jsonRecv.get("idktp").toString().trim());
					        userdata.setTempat_lahir(jsonRecv.get("tmp_lahir").toString().trim());
					        userdata.setMitra(mitra);
					        userdata.setLevel(mitra.getLevel());
						        
					        db.session().save (userdata);
					        
					        alamat.setAlamat(jsonRecv.get("alamat").toString().trim());
					        alamat.setKecamatan(jsonRecv.get("kecamatan").toString().trim());
					        alamat.setKelurahan(jsonRecv.get("kelurahan").toString().trim());
					        alamat.setKodepos(jsonRecv.get("kode-pos").toString().trim());
					        alamat.setKota(jsonRecv.get("kota").toString().trim());
					        alamat.setPropinsi(jsonRecv.get("provinci").toString().trim());
					        alamat.setUserdata(userdata);
					        alamat.setStatus('1');
					        db.session().save(alamat);
						}
				        
				        //System.out.println("masuk");
				        LogSystem.info(request, "Masuk",kelas, refTrx, trxType);

				        
				        //login.setPassword(EEUtil.getHash(jsonRecv.get("email").toString(), jsonRecv.get("password").toString()));
				        login.setNick(jsonRecv.get("email").toString().toLowerCase().trim());
				        login.setName(jsonRecv.get("nama").toString().trim());
				        login.grant("ds");
				        login.grant("login");
				        login.setUserdata(userdata);
				        login.logRevision("created", new UserManager(db).findById((long) 0));
				        login.setStatus('1');
				        login.setPay_type('1');
				        login.setTime(new Date());
					        Long valueKodeUser=new PreRegistrationDao(db).getNext();
	    	 				SimpleDateFormat sdf2=new SimpleDateFormat("yyMM");
	    			        String kodeuser=sdf2.format(new Date())+String.format("%011d", Integer.parseInt(valueKodeUser.toString()));
    			        login.setKode_user(kodeuser);
				        
		
				        //System.out.println("masuk 2");
				        LogSystem.info(request, "Masuk 2",kelas, refTrx, trxType);

				        db.session().save(login);
				        
				        
				        RefRegistrasi refreg=new RefRegistrasi();
				        refreg.setEeuser(login);
				        refreg.setMitra(mitra);
				        if(jsonRecv.has("nama_admin") && jsonRecv.has("no_ref")) {
				        	refreg.setAdmin_verifikasi(jsonRecv.getString("nama_admin"));
					        refreg.setNoref_pendaftaran(jsonRecv.getString("no_ref"));
				        }
				        
				        refreg.setCreate_date(new Date());
				        db.session().save(refreg);
				        

				        
				        res="00";
				        notif="Registrasi berhasil, layanan sudah bisa digunakan.";
		//				if(ds!=null)jo.put("key", Base64.toBase64String(ds.getPublicSign().getEncoded()));
						
				        LogSystem.info(request, "Masuk 3",kelas, refTrx, trxType);

		//		        System.out.println("masuk 3");
		
						//System.out.println("data size :" +fileSave.size());
						LogSystem.info(request, "Data size :" +fileSave.size(),kelas, refTrx, trxType);

						  if (fileSave.size() >=3 || (fileSave.size() >=2 && default_ttd == true)) {
					          res="00";
							  UserManager mgr = new UserManager (db);
							  User userTrx =login;
							  
							 
//							  String uploadTo = basepath+userTrx.getId()+"/original/";
//							  String directoryName = basepath+userTrx.getId()+"/original/";
							  String uploadTo = basepathPreReg+userdata.getId()+"/original/";
    						  String directoryName = basepathPreReg+userdata.getId()+"/original/";
							  //String viewpdf = basepath+userTrx.getId()+"/original/";
//							  File directory = new File(directoryName);
//							  if (!directory.exists()){
//							       directory.mkdirs();
//							  }
							  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
			
							  Date date = new Date();
							  String strDate = sdfDate.format(date);
							  String rename = "DS"+strDate+".png";

							  if(ttditm == null){
								  LogSystem.info(request, "Ga ada ttd, chek default",kelas, refTrx, trxType);
		                		  //System.out.println("Ga ada ttd, chek default");
		                		  if(default_ttd)
		                		  {
		                			  //System.out.println("Default true");
		                			  LogSystem.info(request, "Default True",kelas, refTrx, trxType);
		                			  try {
		                		            generateQRCodeImage(jsonRecv.getString("nama")+ ",<" + jsonRecv.getString("email").toLowerCase()+">", 400, 400, uploadTo+"ttd.png");
		                			  	} catch (WriterException e) {
		                			  		LogSystem.error(request, "Could not generate QR Code, WriterException :: " + e.toString(),kelas, refTrx, trxType);
		                		            //System.out.println("Could not generate QR Code, WriterException :: " + e.toString());
		                		        } catch (IOException e) {
		                		        	LogSystem.error(request, "Could not generate QR Code, IOException :: " + e.toString(),kelas, refTrx, trxType);
		                		            //System.out.println("Could not generate QR Code, IOException :: " + e.toString());
		                		        }
		                			  LogSystem.info(request, uploadTo,kelas, refTrx, trxType);
		                			  //System.out.println(uploadTo);
		                			  userdata.setImageTtd(uploadTo+"ttd.png");
		                		  }
		                		  else
		                		  {
		                			  LogSystem.info(request, "File tandatangan tidak diupload",kelas, refTrx, trxType);
		                			  res="28";
		                	 	      notif="Data upload tidak lengkap";
		                	 	      jo.put("result", res);
		                	 	      jo.put("notif", notif);
		                	 	      return jo;
		                		  }
		                	  }
							  
							  SaveFileWithSamba samba=new SaveFileWithSamba();
							  boolean respSaveData=false;
			                  for (FileItem fileItem : fileSave) {
			                	  
			                	  if(fileItem.getFieldName().equals("ttd")){
			                		  BufferedImage jpgImage = null;
			                	        try {
			                	            jpgImage = ImageIO.read(fileItem.getInputStream());
			                	        } catch (IOException e) {
			                	            e.printStackTrace();
			                	            LogSystem.error(request, e.toString());
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
			                    	  LogSystem.info(request, "File ttd :" + uploadTo+fileItem.getFieldName()+".png",kelas, refTrx, trxType);
			                    	  //System.out.println("File ttd :" + fileTo.getPath());
			                    	  fileItem.getOutputStream().close();
			                    	  userdata.setImageTtd(uploadTo+fileItem.getFieldName()+".png");
			                    	  fileItem.getOutputStream().close();
			                    	  if(respSaveData==false)break;
			                	  }
			                	  else if(fileItem.getFieldName().equals("fotodiri")){
			                		  if(WajahRotate!=null) {
    		                			  respSaveData=samba.write(WajahRotate, uploadTo+fileItem.getFieldName()+".jpg");
    		                		  } else {
    		                			  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
    		                		  }
    		                    	  userdata.setImageWajah(uploadTo+fileItem.getFieldName()+".jpg");
    		                    	  fileItem.getOutputStream().close();
			                    	  if(vd!=null) {
			                    		  vd.setWajah(uploadTo+fileItem.getFieldName()+".jpg");
			                    		  db.session().update(vd);
			                    	  }
			                    	  if(respSaveData==false)break;
			                	  } else if(fileItem.getFieldName().equals("fotoktp")){
			                		  samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
    		                    	  userdata.setImageKTP(uploadTo+fileItem.getFieldName()+".jpg");
    		                    	  fileItem.getOutputStream().close();
			                	  } else if(fileItem.getFieldName().equals("fotonpwp") && npwpItm!=null){
			                		  samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
    			            		  userdata.setImageNPWP(uploadTo+fileItem.getFieldName()+".jpg");
    			            		  fileItem.getOutputStream().close();
			                	  }
			                  
			                  }
			  		          db.session().update(userdata);
			  		       		
			  		          if(respSaveData==false) {
			                	  	tx.rollback();
			                	  	jo.put("result", "91");
							        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
							        return jo;
			  		       		}
		
						  }else {
		
							    res="28";
						        notif="Data upload tidak lengkap";
						        jo.put("result", res);
						        jo.put("notif", notif);
						        return jo;
						  }	      
					      
			
					     tx.commit ();
					     //db.close();
					     
					      DocumentsAccessDao dad=new DocumentsAccessDao(db);
					      List<DocumentAccess> vda=dad.findByEmail(jsonRecv.getString("email").toLowerCase()); 
					      UserManager um = new UserManager(db);
					      LogSystem.info(request, "List vda " + vda,kelas, refTrx, trxType);
					      User useree=um.findByEmail(jsonRecv.getString("email").toLowerCase().trim());
					      for(DocumentAccess da:vda) 
					      { 
						       da.setEeuser(useree);
						       db.session().update(da);
						       dad.update(da);
					      }
					     
					////////BUAT USERNAME///////////
					      
				        	LoginDao ldo = new LoginDao(db);
				        	TmpUsernameDao tmpuserDao = new TmpUsernameDao(db);
				        	Login dataLogin = new Login();
				        	TMPUsername tmpUserName = new TMPUsername();
				        	List<TMPUsername> cekUname = null;
				        	List<Login> cekLogin = null;
				        	User userNick = null;
				        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				        	Date now = new Date();
				        	String strDate = sdf.format(now);
				        	Date dateTime = sdf.parse(strDate);
				        	boolean hasiluname=false;
				        	//							
				        	userNick = um.findByEmailMitra2(jsonRecv.getString("email").toLowerCase().trim());
				        	String email = userNick.getNick();
				        	String kata1 = null;
				        	String kata2 = null;
				        	String user_name = null;
				        	String[] tokens = email.split("@");
				        	for (int j = 0; j < tokens.length; j++) {
				        		user_name = tokens[0].toLowerCase();
				        		kata1 = tokens[1].toLowerCase();
				        	}
				        	
				        	if(user_name.length()>16) {
				        		user_name=user_name.substring(0,16);
				        	}
				        	cekLogin = ldo.cekLogin2(user_name);
				        	int size=cekLogin.size()+1;
				        	if (cekLogin == null || cekLogin.isEmpty()) {
				        		 cekUname = tmpuserDao.cekUserName(user_name.toLowerCase().trim());
				        		 if (cekUname == null || cekUname.isEmpty()) {
				        		 	tmpUserName.setUsername(user_name.toLowerCase());
				        		 	tmpUserName.setDate_record(dateTime);
				        		 	dataLogin.setUsername(user_name.toLowerCase());
				        		 	dataLogin.setDate_record(dateTime);
				        		 	//Long idLogin = ldo.create(dataLogin);
				        		 	//Long idtmpUserName = tmpuserDao.create(tmpUserName);
				        		 	try {
				        		 		
					        		 	long tmp=tmpuserDao.create2(tmpUserName);
					        		 	LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
					        		 	if(tmp>0) {
					        		 		long g=ldo.create2(dataLogin);
					        		 		if(g>0) {
					        		 			login.setLogin(dataLogin);
							        		 	um.update(login);
							        		 	hasiluname=true;
							        		 	LogSystem.info(request, "berhasil create username = "+user_name, kelas, refTrx, trxType);
					        		 		}
						        		 	
					        		 	}
					        		 	
									} catch (Exception e) {
										// TODO: handle exception
										LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
										LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
										
										String uname=user_name+size;
										List<Login> llogin=ldo.cekLogin(uname);
										if(llogin.size()>0) {
											hasiluname=false;
										} else {
											tmpUserName.setUsername(uname.toLowerCase());
						        		 	tmpUserName.setDate_record(dateTime);
						        		 	dataLogin.setUsername(uname.toLowerCase());
						        		 	dataLogin.setDate_record(dateTime);
						        		 	try {
						        		 		long tmp=tmpuserDao.create2(tmpUserName);
						        		 		LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
							        		 	if(tmp>0) {
							        		 		long g=ldo.create2(dataLogin);
							        		 		if(g>0) {
							        		 			login.setLogin(dataLogin);
									        		 	um.update(login);
									        		 	hasiluname=true;
									        		 	LogSystem.info(request, "berhasil create username = "+uname, kelas, refTrx, trxType);
							        		 		}
							        		 	}
											} catch (Exception e2) {
												// TODO: handle exception
												hasiluname=false;
												LogSystem.error(request, e2.getMessage(), kelas, refTrx, trxType);
												LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
											}
										}
										
									}
				        		 	
				        		 	//db.session().flush();
				        		 	//System.out.println("idtmpUserName" + ".." + idtmpUserName + "idLogin" + idLogin);
				        		 	//System.out.println("commit");
				        		 } else {
										String uname=user_name+size;
										List<Login> llogin=ldo.cekLogin(uname);
										if(llogin.size()>0) {
											hasiluname=false;
										} else {
											tmpUserName.setUsername(uname.toLowerCase());
						        		 	tmpUserName.setDate_record(dateTime);
						        		 	dataLogin.setUsername(uname.toLowerCase());
						        		 	dataLogin.setDate_record(dateTime);
						        		 	try {
						        		 		long tmp=tmpuserDao.create2(tmpUserName);
						        		 		LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
							        		 	if(tmp>0) {
							        		 		long g=ldo.create2(dataLogin);
							        		 		if(g>0) {
							        		 			login.setLogin(dataLogin);
									        		 	um.update(login);
									        		 	hasiluname=true;
									        		 	LogSystem.info(request, "berhasil create username = "+uname, kelas, refTrx, trxType);
							        		 		}
							        		 	}
											} catch (Exception e2) {
												// TODO: handle exception
												hasiluname=false;
												LogSystem.error(request, e2.getMessage(), kelas, refTrx, trxType);
												LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
											}
										}
										
				        		 }
				        	} else {
				        		
								String uname=user_name+size;
								List<Login> llogin=ldo.cekLogin(uname);
								if(llogin.size()>0) {
									hasiluname=false;
								} else {
									tmpUserName.setUsername(uname.toLowerCase());
				        		 	tmpUserName.setDate_record(dateTime);
				        		 	dataLogin.setUsername(uname.toLowerCase());
				        		 	dataLogin.setDate_record(dateTime);
				        		 	try {
				        		 		long tmp=tmpuserDao.create2(tmpUserName);
				        		 		LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
					        		 	if(tmp>0) {
					        		 		long g=ldo.create2(dataLogin);
					        		 		if(g>0) {
					        		 			login.setLogin(dataLogin);
							        		 	um.update(login);
							        		 	hasiluname=true;
							        		 	LogSystem.info(request, "berhasil create username = "+uname, kelas, refTrx, trxType);
					        		 		}
					        		 	}
									} catch (Exception e2) {
										// TODO: handle exception
										hasiluname=false;
										LogSystem.error(request, e2.getMessage(), kelas, refTrx, trxType);
										LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
									}
								}
											
				        	}
					     
					     
					     if(hasiluname==false) {
					    	 //if(userNick.getLogin()==null) {
					    	 LogSystem.info(request, "hasil username : false", kelas, refTrx, trxType);
					    	 //User useree=um.findByEmail(jsonRecv.getString("email").toLowerCase().trim());
					    		 String[] name=jsonRecv.getString("nama".toLowerCase().trim()).split(" ");
						    	 String username=name[0]+useree.getId();
					    		 tmpUserName.setUsername(username);
				        		 tmpUserName.setDate_record(dateTime);
				        		 dataLogin.setUsername(username);
				        		 dataLogin.setDate_record(dateTime);
				        		 tmpuserDao.create(tmpUserName);
				        		 ldo.create(dataLogin);
				        		 useree.setLogin(dataLogin);
				        		 um.update(useree);
				        		 LogSystem.info(request, "berhasil create username dengan id eeuser = "+username, kelas, refTrx, trxType);
					    	 //}
					    	 
					     }
					     /////////////////END////////////////////////
					     
					     
					    //MailSenderRegister mail=new MailSenderRegister(userdata,jsonRecv.get("email").toString());
						//mail.run();
					     
					     if(mitra.isNotifikasi()) {
					    	 SendSuksesRegistrasi ssr=new SendSuksesRegistrasi();
						     ssr.kirim(login.getName(), String.valueOf(userdata.getJk()), login.getNick(), DSAPI.DOMAIN, String.valueOf(mitra.getId()));
					     }
					     					
						//UserManager um=new UserManager(db);

				        try {
				        	 //bikin user

				        	
				        	SistemBilling sisbill=new SistemBilling(request,refTrx);
				        	JSONObject resp=sisbill.creatAccountPrepaid("ID"+login.getId(), login.getName(), login.getNick(), alamat.getKota(), userdata.getNo_handphone(), "self", request);
				        	if(resp!=null) {
				        		if(resp.getString("result").equals("00")){
				        			LogSystem.info(request, "berhasil create billing",kelas, refTrx, trxType);
				        		} else {
				        			LogSystem.info(request, "gagal create billing",kelas, refTrx, trxType);
				        		}
				        	} else {
				        		LogSystem.info(request, "timeout ke billing",kelas, refTrx, trxType);
				        	}
				        	
				        	//create certificate
				        	KmsService kms=new KmsService(request, refTrx);
				        	kms.createSertifikat(login.getId());
				        							
				        	
				        }
				        catch (Exception e) {
				        	e.printStackTrace();
				        	LogSystem.error(request, e.toString());
						}
				        
				        
				    	try {
	    		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
	    		        	logSystem.POST("registration", "success", "[API] Registrasi berhasil, layanan sudah dapat digunakan", null, null, null, null, null, jsonRecv.getString("idktp"));
	    		        }catch(Exception e)
	    		        {
	    		        	e.printStackTrace();
	    		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
	    		        	LogSystem.error(request, e.toString());
	    		        }
				    	
					}
					catch (Exception e) {
						// TODO: handle exception
						LogSystem.error(request, e.toString(),kelas, refTrx, trxType);
		//				log.error(e);
		//				new UserdataDao(db).delete(userdata);
						tx.rollback();
						
					    res="06";
				        notif="Registrasi gagal";
					}
			//Tutup untuk liveness
			//}
		}
        jo.put("result", res);
        jo.put("notif", notif);
		
        LogSystem.info(request, "Response akhir : " + jo,kelas, refTrx, trxType);
        return jo;
	}
	
	private static void generateQRCodeImage(String text, int width, int height, String filePath)
        throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        //Path path = FileSystems.getDefault().getPath(filePath);
        //MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        SaveFileWithSamba samba=new SaveFileWithSamba();
        samba.write(baos.toByteArray(), filePath);
        
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


