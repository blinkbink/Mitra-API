package apiMitraV3;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import api.dukcapil.checkingData;
import api.email.SendActivasi;
import apiMitra.verifikasiDoubleLoginForLevelMitra;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.RegLogDao;
import id.co.keriss.consolidate.dao.RekeningDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserTerdaftarDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.VerificationDataDao;
import id.co.keriss.consolidate.dao.VerifikasiManualDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.RegistrationLog;
import id.co.keriss.consolidate.ee.Rekening;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.UserTerdaftar;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.ee.VerifikasiManual;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;


public class RegMitraV3 extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	static String basepathRegLog="/opt/data-DS/LogRegistrasi/";
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="REGD"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.RegMitra";
	String trxType="REG";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="REGD"+sdfDate2.format(tgl).toString();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		//System.out.println("DATA DEBUG :"+(i++));
		//LogSystem.info(request, "DATA DEBUG :"+(i++));
		Mitra mitra=null;
		User useradmin=null;
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
							//LogSystem.info(request, "Token ada : " + token);
							LogSystem.info(request, "ada Token = "+token, kelas, refTrx, trxType);
							mitra=tm.getMitra();
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
							 
							 if(fileItem.getFieldName().equals("ttd")||fileItem.getFieldName().equals("fotodiri")||fileItem.getFieldName().equals("fotoktp")||fileItem.getFieldName().equals("fotonpwp")||fileItem.getFieldName().equals("foto_dukcapil")||fileItem.getFieldName().equals("foto_buku_tabungan")){
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
	              	Boolean RL_DataExist = true;
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
	      		
	      	  }else if(fileItem.getFieldName().equals("foto_buku_tabungan") && fileItem.getSize()!=0){
	      		  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".jpg");
	      		  RL.setImageKitas(uploadToRegLog+fileItem.getFieldName()+".jpg");
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
	       //START Pengecekan pada list verifikasi manual
	         VerifikasiManualDao vdao=new VerifikasiManualDao(db);
	         UserTerdaftarDao utdao=new UserTerdaftarDao(db);
	         VerifikasiManual verman=vdao.cekNikEmail(jsonRecv.getString("idktp"), jsonRecv.getString("email"));
	         if(verman!=null) {
	        	 jo.put("result", "03");
				 jo.put("notif", "Proses pendaftaran berhasil. User dalam proses verifikasi secara manual. Mohon menunggu maksimal 2x24 jam.");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				 LogSystem.response(request, jo, kelas, refTrx, trxType);
				 respon = jo.toString();
	    		 RL.setMessage_response(respon);
	             new RegLogDao(db).create(RL);
	             
	             UserTerdaftar ut=null;
 				 ut=utdao.cekNikMitra(jsonRecv.getString("idktp"), mitra.getId());
 				 if(ut==null) {
				 	ut=new UserTerdaftar();
	             	ut.setNik(jsonRecv.getString("idktp"));
	             	ut.setMitra(mitra);
	             	ut.setCreate_date(new Date());
	             	utdao.create(ut);
 				 }
	             
				 return;
	         }
	         //END Pengecekan pada list verifikasi manual
	         
	        //PROSES REGISTRASI// 
	        jo=RegisterUserMitra(jsonRecv, fileSave, context, mitra, useradmin, request);
	        
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);
			respon = jo.toString();
		    //LogSystem.info(request, "Response String: " + respon);
		    RL.setMessage_response(respon);
        	new RegLogDao(db).create(RL);
        	
        	//START Proses input ke table verifikasi manual
        	if(jo.getString("result").equals("12")) {
	        	if(jo.has("data")) {
	        		JSONObject object=jo.getJSONObject("data");
	        		
	        		VerifikasiManual vman=vdao.cekNikEmail(jsonRecv.getString("idktp"), jsonRecv.getString("email"));
	        		if(vman==null) {
	        			
	        			if(object.has("selfie_match")) {
		        			boolean selfie=object.getBoolean("selfie_match");
		        			if(selfie==true) {
		        				VerifikasiManual vm=new VerifikasiManual();
		        				vm.setEmail(jsonRecv.getString("email"));
		        				vm.setNik(jsonRecv.getString("idktp"));
		        				vm.setId_registration_log(RL);
		        				vm.setCreate_date(tgl);
		        				vdao.create(vm);
		        				
		        				UserTerdaftar ut=null;
		        				ut=utdao.cekNikMitra(jsonRecv.getString("idktp"), mitra.getId());
		        				if(ut==null) {
		        					ut=new UserTerdaftar();
			       	             	ut.setNik(jsonRecv.getString("idktp"));
			       	             	ut.setMitra(mitra);
			       	             	ut.setCreate_date(new Date());
			       	             	utdao.create(ut);
		        				}
		        				
		        			}
		        		} else {
		        			VerifikasiManual vm=new VerifikasiManual();
	        				vm.setEmail(jsonRecv.getString("email"));
	        				vm.setNik(jsonRecv.getString("idktp"));
	        				vm.setId_registration_log(RL);
	        				vm.setCreate_date(tgl);
	        				vdao.create(vm);
	        				
	        				UserTerdaftar ut=null;
	        				ut=utdao.cekNikMitra(jsonRecv.getString("idktp"), mitra.getId());
	        				if(ut==null) {
	        					ut=new UserTerdaftar();
		       	             	ut.setNik(jsonRecv.getString("idktp"));
		       	             	ut.setMitra(mitra);
		       	             	ut.setCreate_date(new Date());
		       	             	utdao.create(ut);
	        				}
		        		}
	        		}
	        		
	        	}
	        }
        	//END Proses input ke table verifikasi manual
        	
			context.put("trxjson", res);
			//LogSystem.response(request, jo);
			LogSystem.response(request, jo, kelas, refTrx, trxType);

		}catch (Exception e) {
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
            //JSONObject jo=new JSONObject();
            try {
				jo.put("result", "05");
				jo.put("notif", "Request Data tidak ditemukan");
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
	
	JSONObject RegisterUserMitra(JSONObject jsonRecv,List<FileItem> fileSave, JPublishContext context, Mitra mitratoken, User useradmin, HttpServletRequest  request) throws JSONException{
		//User userRecv;
		String reg_number = "";
		String link ="";
		DB db = getDB(context);
		JSONObject jo=new JSONObject();
        String res="05";
        String notif="Email sudah terdaftar gunakan email lain atau silahkan login dengan akun anda";
        VerificationData vd=null;
        
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
  		

  	  	if(jsonRecv.get("nama").toString().length()>128) 
  	  	{
  	  		notif="teks nama maksimum 128 karakter";
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
  	  	/*
  		if(jsonRecv.get("tmp_lahir").toString().length()>30) 
	  	{
	  		notif="teks tmp lahir maksimum 30 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
	  	}
	  	*/
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
        	PreRegistration userdata= new PreRegistration();
    		//Mitra mitra =new MitraDao(db).findMitra(namaMitra);
        	
    		
    		PreRegistrationDao udataDao=new PreRegistrationDao(db);
    		if(udataDao.findEmail(jsonRecv.getString("email").toLowerCase())!=null) {
    			userdata = udataDao.findByEmail(jsonRecv.getString("email"));
    			jo.put("result", "14");
				jo.put("notif", "Email sudah terdaftar, namun belum melakukan aktivasi. Silahkan untuk melakukan aktivasi sebelum data dihapus dari daftar aktivasi.");
				if(userdata.getKode_user()!=null) {
					jo.put("kode_user", userdata.getKode_user());
				}
				return jo;
    		}
    		
    		PreRegistration pr = udataDao.findNik(jsonRecv.getString("idktp"));    		
    		//if(udataDao.findNik(jsonRecv.getString("idktp"))!=null) {
    		if(pr != null)
    		{
    			jo.put("result", "14");
				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
				jo.put("email_registered", pr.getEmail());
				return jo;
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
		        
		        if(WajahItm==null || ktpItm==null) {
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
    				if(levelmitra>2 && leveluser<3) {
    					//LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA");
    					LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA", kelas, refTrx,trxType);
    					verifikasiDoubleLoginForLevelMitra verifikasi=new verifikasiDoubleLoginForLevelMitra(refTrx);
    					
    					//jika mitra ekyc dengan buku tabungan
	    		        if(mitra.isEkyc_tabungan()==true) {
	    		        	String uploadTo = basepathPreReg+user.getUserdata().getId()+"/original/";
	                    	
	                    	boolean respSaveData=new SaveFileWithSamba().write(bukuTabunganItm.get(), uploadTo+"bukuTabungan.jpg");
	                    	if(respSaveData==false) {
	                    		jo.put("result", "91");
						        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
						        return jo;
	                    	}
	                    	
	    		        	Rekening rek=new Rekening();
	    		        	rek.setImage_buku_tabungan(uploadTo);
	    		        	rek.setKode_bank(jsonRecv.getString("kode_bank"));
	    		        	rek.setNo_rekening(jsonRecv.getString("no_rekening"));
	    		        	rek.setUserdata(user.getUserdata());
	    		        	new RekeningDao(db).create(rek);
	    		        	
	    		        	if(user.getKode_user()!=null) {
	    		        		jo.put("kode_user", user.getKode_user());
	    		        	}
	    		        	jo.put("result", "00"); 
		    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
		    		        return jo;
	    		        }
    					
    			        if(mitra.isVerifikasi()) {
	    					JSONObject check=verifikasi.check(db, user, basepathPreReg, mitra, null, WajahItm, ktpItm, jsonRecv, request);
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
    			        	FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
    			        	//jika mitra menggunakan Non-EKYC
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
								
								if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
									res="12";
								    
								    jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								
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
								
								String dataverf=jsonRecv.getString("data_verifikasi");
								JSONObject joj=new JSONObject(dataverf);
								if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								
								String score=jsonRecv.getString("score_selfie");
								Float fscore=Float.valueOf(score);
								if(fscore<75) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "score selfie dibawah batas minimum.");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								
								
								
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
								
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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
								
								LogSystem.info(request, "update level user sesuai mitra berhasil",kelas, refTrx,trxType);
								if(user.getStatus()=='4')
    		    		        { 
    			    		         jo.put("result", "78"); 
    			    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
    		    		        } else {
    		    		        	Userdata udata=user.getUserdata();
    		    		        	udata.setLevel("C4");
    		    		        	new UserdataDao(db).update(udata);
    		    		        }
								
								if(user.getKode_user()!=null) {
		    		        		jo.put("kode_user", user.getKode_user());
		    		        	}
    							jo.put("result", "00"); 
    		    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
								return jo;
								
							}
    			        	
    			        	//jika pengecekan dari dukcapil dengan mngirimkan photo selfie dari dukcapil
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

								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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
									if(user.getStatus()=='4')
	    		    		        { 
	    			    		         jo.put("result", "78"); 
	    			    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
	    		    		        } else {
	    		    		        	Userdata udata=user.getUserdata();
	    		    		        	udata.setLevel("C4");
	    		    		        	new UserdataDao(db).update(udata);
	    		    		        }
									
									if(user.getKode_user()!=null) {
			    		        		jo.put("kode_user", user.getKode_user());
			    		        	}
									jo.put("result", "00"); 
	    		    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
									return jo;									
								}
							}
    			        	
    			        }
    				} else {
    					if(user.getKode_user()!=null) {
    		        		jo.put("kode_user", user.getKode_user());
    		        	}
	    		        jo.put("result", "00"); 
	    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign"); 
	    		        if(user.getStatus()=='4')
	    		        { 
		    		         jo.put("result", "78"); 
		    		         jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
	    		        }
	    		        
	    		        if(user.getStatus()=='0') {
	    		        	
	    		        }
	    		        return jo; 
    				}
    		    } 
    			else 
    		    { 
    		        user= new UserManager(db).findByUsername(jsonRecv.get("email").toString().toLowerCase());  
    		        
    		        if(user!=null) 
    		        { 
    		        	
		    		         nextProcess=false; 
		    		         jo.put("result", "14"); 
		    		         jo.put("notif", "Email sudah terdaftar dengan NIK lain. Gunakan email lain."); 
		    		         if(user.getStatus()=='4') 
		    		         { 
			    		          jo.put("result", "78"); 
			    		          jo.put("notif", "User anda diblok, hubungi pihak Digisign"); 
		    		         } 
		    		         return jo; 
    		        	
    		        } 
    		    }
    			
    			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16) {
    				jo.put("result", "14");
    				jo.put("notif", "Format NIK Salah");
    				
    				fNik=false;
    				return jo;
    			}
    			
    			
    			List<Userdata> userData=new UserdataDao(db).findByKtp(jsonRecv.get("idktp").toString());
    			
    			
    			if(userData.size()>0) {
    				
    				jo.put("result", "14");
    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
    				
    				UserManager um = new UserManager(db);
    				User UserEmail = um.findByUserData(userData.get(0).getId());
    				jo.put("email_registered", UserEmail.getNick());
    				
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
    			
    			if(nextProcess) {
    				try{
    					
    					
    					//check json semua key mandatori
    					FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
						if(mitra.isVerifikasi()) {
							
							JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
							
							if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
							    res="12";
							    notif=respFace.getString("info");
							    jo.put("result", "12");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("info", notif);
			    				return jo;
							}
							
							LogSystem.info(request, "kirim ke AsliRI",kelas, refTrx,trxType);
							checkingData cd=new checkingData();
							JSONObject result = cd.check(jsonRecv.getString("idktp"), jsonRecv.getString("nama"), jsonRecv.getString("tmp_lahir"), jsonRecv.getString("tgl_lahir"), jsonRecv.getString("alamat"), new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII), mitra.getName());
							//JSONObject obj=
							LogSystem.info(request, result.toString(),kelas, refTrx,trxType);
							if(result.get("result").equals("00") || result.get("result").equals("true")) {
								LogSystem.info(request, "Hasilnya berhasil",kelas, refTrx,trxType);
								//System.out.println("hasil nya berhasil");
							}
							else {

								
								if(result.has("connection"))
								{
									if(result.get("connection").equals(false) && result.get("result").equals("05"))
									{
										jo.put("result", "FE");
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
								
								if(result.has("dataFace"))
								{
									if(result.getJSONObject("dataFace").has("data") && !result.getJSONObject("dataFace").isNull("data"))
									{
										if(result.getJSONObject("dataFace").getJSONObject("data").has("selfie_photo") && result.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo") > 55)
										{
											matchFace = true;
											LogSystem.info(request, "Match :" + matchFace,kelas, refTrx,trxType);
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
								
								if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
									res="12";
								    
								    jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								
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
								
								String dataverf=jsonRecv.getString("data_verifikasi");
								JSONObject joj=new JSONObject(dataverf);
								if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
				    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx,trxType);
				    				return jo;
								}
								
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
								
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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

								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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
								if(!jsonRecv.getString("kode_bank").trim().equalsIgnoreCase("014") && !jsonRecv.getString("no_rekening").trim().equalsIgnoreCase("0611104579")) {
									jo.put("result", "04");
									jo.put("notif", "verifikasi buku tabungan tidak sesuai.");
									jo.put("info", "Nama dan nomor rekening tidak sesuai");
									return jo;
								}
								
								JSONObject respFace=fRec.checkFaceWithKTP(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());
								
								if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
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
								
							}
							
							
							
						}
						
						
    					
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
	    			        String kodeuser=sdf.format(tgl)+String.format("%011d", Integer.parseInt(valueKodeUser.toString()));
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
	                    	if(respSaveData==false) {
	                    		jo.put("result", "91");
						        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
						        userdata.setId(idUserData);
						        udataDao.delete(userdata);
						        return jo;
	                    	}
	                    	
	    		        	Rekening rek=new Rekening();
	    		        	rek.setImage_buku_tabungan(uploadTo);
	    		        	rek.setKode_bank(jsonRecv.getString("kode_bank").trim());
	    		        	rek.setNo_rekening(jsonRecv.getString("no_rekening").trim());
	    		        	
	    		        	new RekeningDao(db).create(rek);
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
	    		                  for (FileItem fileItem : fileSave) {
	    		                	  LogSystem.info(request, fileItem.getFieldName(),kelas, refTrx,trxType);
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
				                    	  LogSystem.info(request, "File ttd :" + uploadTo+fileItem.getFieldName()+".png",kelas, refTrx,trxType);
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
	    		                		  respSaveData=samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
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
	    		                  
	    		                  if(respSaveData==false) {
	    		                	  	udataDao.delete(userdata);
	    		                	  	jo.put("result", "91");
								        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
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
	    								}
	    								if(mitra.isNotifikasi()) {
		    				                SendActivasi sa=new SendActivasi(refTrx, kelas, trxType, request);
		    				                sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()), reg_number);
			    							
		    				                //sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()));
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
    					// TODO: handle exception
    					udataDao.delete(userdata);
    					LogSystem.error(getClass(), e,kelas, refTrx, trxType);
    					LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
    					e.printStackTrace();

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
	
}
