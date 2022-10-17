package apiMitraV3;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
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
import apiMitra.verifikasiDoubleLoginForLevelMitra;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.billing.SistemBilling;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.EeuserMitraDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
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


public class RegMitraReferenceV3 extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	static String basepathRegLog="/opt/data-DS/LogRegistrasi/";
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="REGS"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.RegMitraReference";
	String trxType="REG";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="REGS"+sdfDate2.format(tgl).toString();
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
		}
		RegistrationLog RL = new RegistrationLog();
		String respon = null;
		try {
			jo.put("refTrx", refTrx);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
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
					TokenMitraDao tmd=new TokenMitraDao(getDB(context));
					String token=request.getHeader("authorization");
					if(token!=null) {
						String[] split=token.split(" ");
						if(split.length==2) {
							if(split[0].equals("Bearer"))token=split[1];
						}
						
					}
					
					TokenMitra tm=tmd.findByToken(token.toLowerCase());
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
							 
							 if(fileItem.getFieldName().equals("ttd")||fileItem.getFieldName().equals("fotodiri")||fileItem.getFieldName().equals("fotoktp")||fileItem.getFieldName().equals("fotonpwp")){
								 fileSave.add(fileItem);
								 
							 }
							
						}
						String FileUploading = "["+fileItem.getFieldName()+"]:{"+fileItem.getName()+","+fileItem.getSize()+"};";
						LogSystem.info(request,"RL_CHECK FileREQUEST>"+ FileUploading);
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
	        	 jo.put("res", "30");
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
	      	 	  BufferedImage jpgImage = null;
	      	      try {
	      	            jpgImage = ImageIO.read(fileItem.getInputStream());
	      	      } catch (IOException e) {
	      	            e.printStackTrace();
	      	      }
	      	      if(containsTransparency(jpgImage)==true) {
	      	    	  //fileItem.write(fileTo);
	      	    	  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".png");
	      	      } else {
	      	    	  int color = jpgImage.getRGB(0, 0);
	                 Image data=FileProcessor.makeColorTransparent(jpgImage, new Color(color), 10000000);
	                 BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,jpgImage.getWidth(),
	                 jpgImage.getHeight());
	                     //ImageIO.write(newBufferedImage, "png" ,fileTo);
	                 ByteArrayOutputStream baos=new ByteArrayOutputStream();
	                 ImageIO.write(newBufferedImage, "png" , baos);
	                 samba.write(baos.toByteArray(), uploadToRegLog+fileItem.getFieldName()+".png");
	      	      }
	      	      
	      	      jpgImage.flush();
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
				 jo.put("res", "05");
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
					 jo.put("res", "55");
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
				 jo.put("res", "55");
				 jo.put("notif", "userid tidak ditemukan");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				 LogSystem.response(request, jo, kelas, refTrx, trxType);
				 respon = jo.toString();
	    		 LogSystem.info(request, "Response String: " + respon);
	    		 RL.setMessage_response(respon);
	             new RegLogDao(db).create(RL);
				 return;
	         }
	         
	         //JSONObject jo = null;
	         //jo=RegisterUserMitra(jsonRecv, fileSave, context);
	         jo=register(mitra, jsonRecv, fileSave, context, request);
	         
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
			LogSystem.response(request, jo, kelas, refTrx, trxType);

			
		}catch (Exception e) {
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
            //JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Data request tidak ditemukan");
				respon = jo.toString();
    		    LogSystem.info(request, "Response String: " + respon);
    		    RL.setMessage_response(respon);
            	new RegLogDao(db).create(RL);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
	
	JSONObject register(Mitra mitra,JSONObject jsonRecv,List<FileItem> fileSave,JPublishContext context, HttpServletRequest request) throws JSONException{
		Boolean default_ttd = false;
		VerificationData vd=null;		
		User userRecv;
		DB 	db = getDB(context);
        JSONObject jo=new JSONObject();
        String res="06";
        String notif="Registrasi gagal";
		List<Userdata> userData= null;
		User login =new User();
		Alamat alamat=new Alamat();
		UserdataDao udataDao=new UserdataDao(db);
		//User user=null;
		boolean fNik=false;
		FileItem ktpItm = null, WajahItm = null, ttditm = null, npwpItm = null;
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
        	
		    res="28";
	        notif="Data upload tidak lengkap";
	        jo.put("result", res);
	        jo.put("notif", notif);
	        return jo;
        }
        
        
//        if(jsonRecv.get("alamat").toString().length()>50) 
//  	  	{
//  	  		notif="teks alamat maksimum 50 karakter";
//	  	  	jo.put("result", "28");
//			jo.put("notif", notif);
//			
//			return jo;
//  	  	}
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
        
        
//  	  	if(jsonRecv.get("kecamatan").toString().length()>30) 
//  	  	{
//  	  		notif="teks kecamatan maksimum 30 karakter";
//	  	  	jo.put("result", "28");
//			jo.put("notif", notif);
//			
//			return jo;
//  	  	}
//  	  	if(jsonRecv.get("kelurahan").toString().length()>30) 
//  	  	{
//  	  		notif="teks kelurahan maksimum 30 karakter";
//	  	  	jo.put("result", "28");
//			jo.put("notif", notif);
//			
//			return jo;
//  	  	}
//  	  	if(jsonRecv.get("kota").toString().length()>30) 
//  	  	{
//  	  		notif="teks kota maksimum 30 karakter";
//	  	  	jo.put("result", "28");
//			jo.put("notif", notif);
//			
//			return jo;
//  	  	}
  	  	if(jsonRecv.get("nama").toString().length()>128) 
  	  	{
  	  		notif="teks nama maksimum 128 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
  	  	}
//  	  	if(jsonRecv.get("provinci").toString().length()>50) 
//  	  	{
//  	  		notif="teks provinsi maksimum 30 karakter";
//	  	  	jo.put("result", "28");
//			jo.put("notif", notif);
//			
//			return jo;
//  	  	}
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
  		if(jsonRecv.get("jenis_kelamin").toString().length()>10) 
	  	{
	  		notif="teks jenis kelamin maksimum 10 karakter";
	  	  	jo.put("result", "FE");
			jo.put("notif", notif);
			
			return jo;
	  	}
  		
  		
        
        if(jsonRecv.has("default_ttd"))
        {
        	default_ttd = jsonRecv.getBoolean("default_ttd");
        }
           
        User user=null;
        user= new UserManager(db).findByUserNik(jsonRecv.getString("email").toLowerCase(), jsonRecv.getString("idktp"));
        if(user!=null){
			//notif="Nomor KTP sudah terdaftar dengan email lain";
			//User user= new UserManager(db).findByUsername(jsonRecv.get("email").toString());
			//if(user!=null) {
				//if(user.getNick().equalsIgnoreCase(jsonRecv.getString("email"))) {
        	int leveluser=Integer.parseInt(user.getUserdata().getLevel().substring(1));
        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
        	if(levelmitra>2 && leveluser<3) {
        		LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA",kelas, refTrx, trxType);
				verifikasiDoubleLoginForLevelMitra verifikasi=new verifikasiDoubleLoginForLevelMitra(refTrx);
				
		        if(mitra.isVerifikasi()) {
					JSONObject check=verifikasi.check(db, user, basepathPreReg, mitra, null, WajahItm, ktpItm, jsonRecv, request);
					if(check!=null) {
						if(!check.getString("result").equals("00")) {
							jo=check;
							return jo;
						}
						else {
							LogSystem.info(request, "update level user sesuai mitra berhasil",kelas, refTrx, trxType);
							jo.put("result", "00"); 
		    		        jo.put("notif", "Registrasi berhasil. Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign"); 
		    		        if(user.getStatus()=='4')
		    		        { 
			    		         jo.put("result", "78"); 
			    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
		    		        } else {
		    		        	Userdata udata=user.getUserdata();
		    		        	udata.setLevel("C4");
		    		        	new UserdataDao(db).update(udata);
		    		        	if(user.getStatus()=='0') {
		    		        		user.setStatus('1');
		    		        		new UserManager(db).update(user);
		    		        	}
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
		        	//jika mitra menggunakan Non-EKYC
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
						
						if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
							res="12";
						    
						    jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal.");
		    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
		    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
		    				return jo;
						}
						
						if(!jsonRecv.has("data_verifikasi")) {
							res="93";
					        notif="Message request tidak lengkap. data_verifikasi.";
					        jo.put("result", res);
					        jo.put("notif", notif);
					        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
					        return jo;
						}
						if(!jsonRecv.has("score_selfie")) {
							res="93";
					        notif="Message request tidak lengkap. score_selfie.";
					        jo.put("result", res);
					        jo.put("notif", notif);
					        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
					        return jo;
						}
						
						String dataverf=jsonRecv.getString("data_verifikasi");
						JSONObject joj=new JSONObject(dataverf);
						if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
							jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal.");
		    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
		    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
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
						//JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId());
						JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
						
						
						if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
						    res="12";
						    notif=respFace.getString("info");
						    jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal. kecocokan photo selfie dan ktp tidak sama.");
		    				jo.put("info", notif);
		    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
		    				return jo;
						} else {
							System.out.println("LOLOS");
						}
						
						LogSystem.info(request, "update level user sesuai mitra berhasil",kelas, refTrx, trxType);
						if(user.getStatus()=='4')
	    		        { 
		    		         jo.put("result", "78"); 
		    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
	    		        } else {
	    		        	Userdata udata=user.getUserdata();
	    		        	udata.setLevel("C4");
	    		        	new UserdataDao(db).update(udata);
	    		        	if(user.getStatus()=='0') {
	    		        		user.setStatus('1');
	    		        		new UserManager(db).update(user);
	    		        	}
	    		        }
						jo.put("result", "00"); 
	    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
						return jo;
						
					}
		        }
        	}
        	else {
					notif="Registrasi berhasil. Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign";	
					res="00";
					
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
			
			
			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16) {
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
					  	
						
						if(mitra.isVerifikasi()) {
							FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
							JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
							
							if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
							    res="12";
							    notif=respFace.getString("info");
							    jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("info", notif);
			    				return jo;
							}
							
							checkingData cd=new checkingData();
							JSONObject result = cd.check(jsonRecv.getString("idktp"), jsonRecv.getString("nama"), jsonRecv.getString("tmp_lahir"), jsonRecv.getString("tgl_lahir"), jsonRecv.getString("alamat"),  new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII), mitra.getName());
							//JSONObject obj=
							
							//System.out.println("assssssssssssssssssssssssssssssssssssssssss" + result.toString());
							LogSystem.error(request, "assssssssssssssssssssssssssssssssssssssssss" + result.toString(),kelas, refTrx, trxType);
							if(result.get("result").equals("00") || result.get("result").equals("true")) {
								LogSystem.error(request, "Hasilnya berhasil",kelas, refTrx, trxType);
								//System.out.println("hasil nya berhasil");
								JSONObject data = null;
								if(result.has("dataID") && result.getJSONObject("dataID").has("data"))
								{
									data = new JSONObject();
									if(!result.getJSONObject("dataID").isNull("data"))
									{
										data = result.getJSONObject("dataID").getJSONObject("data");
									}
								}
							
								jo.put("data", data);
							}
							else {

								
								if(result.has("connection"))
								{
									if(result.get("connection").equals(false) && result.get("result").equals("05"))
									{
										jo.put("result", "91");
					    				jo.put("notif", "verifikasi user gagal.");
					    				jo.put("info", "Masalah koneksi, mohon ulangi kembali");
					    				return jo;
									} 
									if (result.get("result").equals("FE")){
										jo.put("result", "FE");
					    				jo.put("notif", "verifikasi user gagal.");
					    				jo.put("info", "Format tgl salah");
					    				return jo;
									}
								}
								
						
								JSONObject data = null;
								boolean matchFace = false;
								
								if(result.has("dataFace") )
								{
									if(result.getJSONObject("dataFace").has("data") && !result.getJSONObject("dataFace").isNull("data"))
									{
										if(result.getJSONObject("dataFace").getJSONObject("data").has("selfie_photo") && result.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo") > 55)
										{
											matchFace = true;
											LogSystem.info(request, "Match :" + matchFace,kelas, refTrx, trxType);
										}
									}
									
									if(result.has("dataID"))
									{
										if(result.getJSONObject("dataID").has("data"))
										{
											data = new JSONObject();
											if(!result.getJSONObject("dataID").isNull("data"))
											{
												data = result.getJSONObject("dataID").getJSONObject("data");
												data.put("selfie_match", matchFace);
											}
											else
											{
												result.put("information", "Data KTP tidak ditemukan");
											}
										}
										
									}
							
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("data", data);
				    				jo.put("info", result.get("information"));
				    				LogSystem.error(request, "Isi jo :" + jo,kelas, refTrx, trxType);
				    				//System.out.println("Isi jo :" + jo);
				    				return jo;
								}
								else
								{
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				
				    				if(result.has("dataID"))
				    				{
				    					if(!result.getJSONObject("dataID").isNull("data"))
				    					{
				    						jo.put("data", result.getJSONObject("dataID").getJSONObject("data"));
						    				jo.put("info", "Verifikasi wajah gagal");
				    					}
				    					else
				    					{
				    						jo.put("info", "Data KTP tidak ditemukan");
				    					}
				    				}				    			
				    				else
				    				{
				    					jo.put("data", result.get("information"));
				    					jo.put("info", "Registrasi gagal");
				    				}
				    				
				    				return jo;
								}
								
							
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
								
								if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
									res="12";
								    
								    jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
				    				return jo;
								}
								
								String dataverf=jsonRecv.getString("data_verifikasi");
								JSONObject joj=new JSONObject(dataverf);
								if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
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
								
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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
									vd.setTgl_lahir(jsonRecv.getString("tgl_lahir"));
									vd.setTimestamp_selfie(jsonRecv.getString("ref_verifikasi").trim());
									vd.setTimestamp_text(jsonRecv.getString("ref_verifikasi").trim());
									vd.setAlamat(jsonRecv.getString("alamat").trim());
									db.session().save(vd);
								}
							}
							
							LogSystem.info(request, "level = "+mitra.getLevel(),kelas, refTrx, trxType);
							if(mitra.getLevel().equals("C2")) {
								FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
								//JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId());
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
								
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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
			        Transaction tx = db.session().beginTransaction();
			        tx.setTimeout(300);
		
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
		//		        udataDao.create(userdata);
				        
		
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
		                			  		LogSystem.error(request, "Could not generate QR Code, WriterException :: " + e.getMessage(),kelas, refTrx, trxType);
		                		            //System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
		                		        } catch (IOException e) {
		                		        	LogSystem.error(request, "Could not generate QR Code, IOException :: " + e.getMessage(),kelas, refTrx, trxType);
		                		            //System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
		                		        }
		                			  LogSystem.info(request, uploadTo,kelas, refTrx, trxType);
		                			  //System.out.println(uploadTo);
		                			  userdata.setImageTtd(uploadTo+"ttd.png");
		                		  }
		                		  else
		                		  {
		                			  res="28";
		                	 	      notif="Data upload tidak lengkap";
		                	 	      jo.put("result", res);
		                	 	      jo.put("notif", notif);
		                	 	      return jo;
		                		  }
		                	  }
							  
							  SaveFileWithSamba samba=new SaveFileWithSamba();
			                  for (FileItem fileItem : fileSave) {
			                	  
			                	  if(fileItem.getFieldName().equals("ttd")){
			                		  BufferedImage jpgImage = null;
			                	        try {
			                	            jpgImage = ImageIO.read(fileItem.getInputStream());
			                	        } catch (IOException e) {
			                	            e.printStackTrace();
			                	        }
			                	      if(containsTransparency(jpgImage)==true) {
			                	    	  //fileItem.write(fileTo);
			                	    	  samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".png");
			                	      } else {
			                	    	  int color = jpgImage.getRGB(0, 0);
				                          Image data=FileProcessor.makeColorTransparent(jpgImage, new Color(color), 10000000);
				                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,jpgImage.getWidth(),
				                        		  jpgImage.getHeight());
					                      //ImageIO.write(newBufferedImage, "png" ,fileTo);
					                      ByteArrayOutputStream baos=new ByteArrayOutputStream();
					                      ImageIO.write(newBufferedImage, "png" , baos);
					                      samba.write(baos.toByteArray(), uploadTo+fileItem.getFieldName()+".png");
			                	      }
			                	      
			                	      jpgImage.flush();
			                    	  LogSystem.info(request, "File ttd :" + uploadTo+fileItem.getFieldName()+".png",kelas, refTrx, trxType);
			                    	  //System.out.println("File ttd :" + fileTo.getPath());
			                    	  fileItem.getOutputStream().close();
			                    	  userdata.setImageTtd(uploadTo+fileItem.getFieldName()+".png");
			                    	  fileItem.getOutputStream().close();
			                	  }
			                	  else if(fileItem.getFieldName().equals("fotodiri")){
			                		  samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
    		                    	  userdata.setImageWajah(uploadTo+fileItem.getFieldName()+".jpg");
    		                    	  fileItem.getOutputStream().close();
			                    	  if(vd!=null) {
			                    		  vd.setWajah(uploadTo+fileItem.getFieldName()+".jpg");
			                    		  db.session().update(vd);
			                    	  }
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
				        	
				        	cekLogin = ldo.cekLogin2(user_name);
				        	int size=cekLogin.size()+1;
				        	System.out.println("cekLogin" + ".." + cekLogin);
				        	if (cekLogin == null || cekLogin.isEmpty()) {
				        		 System.out.println("cekLogin" + ".." + user_name);
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
										LogSystem.error(request, e.getMessage(), kelas, refTrx, trxType);
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
						}
					     
					}
					catch (Exception e) {
						// TODO: handle exception
						LogSystem.error(getClass(), e,kelas, refTrx, trxType);
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
	
}


