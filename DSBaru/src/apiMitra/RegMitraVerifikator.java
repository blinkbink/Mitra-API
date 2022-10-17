package apiMitra;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.RegLogDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.VerifierDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.RegistrationLog;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.ee.Verifier;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;


public class RegMitraVerifikator extends ActionSupport {

	private static String basepath="/file2/data-DS/UploadFile/";
	private static String basepathPreReg="/file2/data-DS/PreReg/";
	private static String basepathRegLog="/file2/data-DS/LogRegistrasi/";
	private static String basepathVerifikator="/file2/data-DS/Verifikator/";
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx=null;
	String kelas="apiMitra.RegMitraVerifikator";
	String trxType="REG";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="RVER"+sdfDate2.format(tgl).toString();
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
					LogSystem.info(request, "Bukan multipart", kelas, refTrx, trxType);
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
						TokenMitra tm=tmd.findByToken(token.toLowerCase());
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada : " + token,kelas, refTrx, trxType);
							mitra=tm.getMitra();
							Mitra RL_mitra = mitra;
				        	RL.setMitra(RL_mitra);
						} else {
							LogSystem.error(request, "Token null ",kelas, refTrx, trxType);
							//JSONObject jo=new JSONObject();
							jo.put("res", "55");
							jo.put("notif", "token salah");
							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
							LogSystem.response(request, jo, kelas, refTrx, trxType);
							respon = jo.toString();
			    		    LogSystem.info(request, "Response String: " + respon);
			    		    RL.setMessage_response(respon + "(req bukan multipart)");
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
	         LogSystem.request(request, fileItems, kelas, refTrx, trxType);
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
		       	 if(!jsonRecv.getString("type").equals("Corp")) {
			        	 char RL_type = '2';
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
//		      	 	  BufferedImage jpgImage = null;
//		      	      try {
//		      	            jpgImage = ImageIO.read(fileItem.getInputStream());
//		      	      } catch (IOException e) {
//		      	            e.printStackTrace();
//		      	      }
//		      	      if(containsTransparency(jpgImage)==true) {
//		      	    	  //fileItem.write(fileTo);
		      	    	  samba.write(fileItem.get(), uploadToRegLog+fileItem.getFieldName()+".png");
//		      	      } else {
//		      	    	  int color = jpgImage.getRGB(0, 0);
//		                 Image data=FileProcessor.makeColorTransparent(jpgImage, new Color(color), 10000000);
//		                 BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,jpgImage.getWidth(),
//		                 jpgImage.getHeight());
//		                     //ImageIO.write(newBufferedImage, "png" ,fileTo);
//		                 ByteArrayOutputStream baos=new ByteArrayOutputStream();
//		                 ImageIO.write(newBufferedImage, "png" , baos);
//		                 samba.write(baos.toByteArray(), uploadToRegLog+fileItem.getFieldName()+".png");
//		      	      }
//		      	      
//		      	      jpgImage.flush();
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
	         if(mitra!=null) {
	        	 
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
	        	 
	        	 String userid = jsonRecv.getString("userid").toLowerCase();
	        	 
		         UserManager user=new UserManager(getDB(context));
		         //User eeuser=user.findByUsername(userid);
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
		        	 }
		        	 else {
		        		 LogSystem.error(request, "Token dan mitra tidak sesuai",kelas, refTrx, trxType);
		        		 //JSONObject jo=new JSONObject();
						 jo.put("res", "55");
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
		        	 LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
		        	 //JSONObject jo=new JSONObject();
					 jo.put("res", "12");
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
	         jo=RegisterUserMitra(jsonRecv, fileSave, context, mitra, useradmin, request);
	         
	         
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
        
        
        /*
  	  	if(jsonRecv.get("alamat").toString().length()>50) 
  	  	{
  	  		notif="teks alamat maksimum 50 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	}
  	  	
  	  	if(jsonRecv.get("kecamatan").toString().length()>30) 
  	  	{
  	  		notif="teks kecamatan maksimum 30 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	}
  	  	*/
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
	            boolean isOnlyDigit = Character.isDigit(dataInput[i]);
	            if (isOnlyDigit == false) {
	            	notif="teks tlp harus angka";
	    	  	  	jo.put("result", "28");
	    			jo.put("notif", notif);
	    			
	    			return jo;
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
	  	  	jo.put("result", "28");
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
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
  	  	}
  	  	if(jsonRecv.get("position").toString().length()>30) 
	  	{
	  		notif="teks position maksimum 30 karakter";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			
			return jo;
	  	}
  	  	if(jsonRecv.get("verifier_id").toString().length()>30) 
	  	{
	  		notif="teks verifier_id maksimum 30 karakter";
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
        	
    		
    		
	    	  	//start check file image
	    	  	FileItem ktpItm = null, WajahItm = null;
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
			      	  }
			        }
		        
		        if(WajahItm==null || ktpItm==null) {
		        	LogSystem.info(request, "KTP atau selfie null",kelas, refTrx, trxType);
				    res="28";
			        notif="Data upload tidak lengkap";
			        jo.put("result", res);
			        jo.put("notif", notif);
			        return jo;
		        }
		      //End check file image
		        
		      VerifierDao vdao=new VerifierDao(db);
		      Verifier v=vdao.findByEmaildanNik(jsonRecv.getString("email"), jsonRecv.getString("idktp"));
		      if(v!=null) {
			    	jo.put("result", "00");
	  				jo.put("notif", "Verifikator sudah terdaftar sebelumnya.");
	  				return jo;
		      } else {
		    	  v=vdao.findByEmail(jsonRecv.getString("email"));
		    	  if(v!=null) {
		    		  	jo.put("result", "14");
		  				jo.put("notif", "email sudah terdaftar dengan NIK berbeda. Gunakan email lain.");
		  				return jo;
		    	  }
		    	  v=vdao.findNik(jsonRecv.getString("idktp"));
		    	  if(v!=null) {
		    		  	jo.put("result", "14");
		  				jo.put("notif", "NIK sudah terdaftar dengan email berbeda.");
		  				jo.put("email_registered", v.getEmail());
		  				return jo;
		    	  }
		      }
    			
    			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16) {
    				jo.put("result", "14");
    				jo.put("notif", "Format NIK Salah");
    				
    				return jo;
    			}
    			
    			v=vdao.findByNoTlp(jsonRecv.getString("tlp"));
    			if(v!=null) {
    				jo.put("result", "15");
    				jo.put("notif", "No Hanphone sudah digunakan. Gunakan no hanphone lain");
    				
    				return jo;
    			}
    			
    				try{
    					FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
						JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
						
						if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.65) {
						    res="12";
						    notif=respFace.getString("info");
						    jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal.");
		    				jo.put("info", notif);
		    				return jo;
						}
						
						JSONObject resRegFace=fRec.RegFaceRecognation(Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.getString("idktp"), jsonRecv.getString("nama"));
						if(resRegFace!=null) {
							if(resRegFace.getString("result").equals("00")) {
								LogSystem.info(request, "sukses register facerecognation",kelas, refTrx, trxType);
							} else {
								jo.put("result", "FE");
			    				jo.put("notif", "System timeout, silahkan coba kembali 10 menit kemudian");
			    				return jo;
							}
						} else {
							jo.put("result", "FE");
		    				jo.put("notif", "System timeout, silahkan coba kembali 10 menit kemudian");
		    				return jo;
						}
						
						v=new Verifier();
						v.setName(jsonRecv.getString("nama"));
						v.setEmail(jsonRecv.getString("email"));
						v.setPosition(jsonRecv.getString("position"));
						v.setTlp(jsonRecv.getString("tlp"));
						v.setId_ktp(jsonRecv.getString("idktp"));
						v.setVerifier_id(jsonRecv.getString("verifier_id"));
						v.setDelete(false);
						v.setMitra(mitra);
						v.setStatus(true);
						v.setCreate_date(new Date());
						Long idv=vdao.create(v);
						
				          res="00";
				          notif="Registrasi verifikator berhasil.";
		
						  String uploadTo = basepathVerifikator+idv+"/";
						  
//						  File directory = new File(uploadTo);
//						  if (!directory.exists()){
//						       directory.mkdirs();
//						  }
						  
						  boolean ktp =false;
						  boolean selfi=false;
						  String pathSelfie=null;
						  String pathKtp=null;
						  SaveFileWithSamba samba=new SaveFileWithSamba();
		                  for (FileItem fileItem : fileSave) {
		                	  LogSystem.info(request, fileItem.getFieldName(),kelas, refTrx, trxType);
		                	  //System.out.println(fileItem.getFieldName());
		                	  if(fileItem.getFieldName().equals("fotodiri")){
		                		  //File fileTo = new File(uploadTo+fileItem.getFieldName()+".jpg");
		                		  pathSelfie=uploadTo+fileItem.getFieldName()+".jpg";
		                    	  //fileItem.write(fileTo);
		                    	  
		                    	  samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
		                    	  v.setFace_image(uploadTo+fileItem.getFieldName()+".jpg");
		                    	  selfi=true;
		                    	  fileItem.getOutputStream().close();
		                	  }else if(fileItem.getFieldName().equals("fotoktp")){
		                		  samba.write(fileItem.get(), uploadTo+fileItem.getFieldName()+".jpg");
		                		  pathKtp=uploadTo+fileItem.getFieldName()+".jpg";
		                    	  v.setKtp_image(uploadTo+fileItem.getFieldName()+".jpg");
		                    	  ktp=true;
		                    	  fileItem.getOutputStream().close();
		                	  } 
		                  }
		                  
		                  if(ktp==true && selfi==true) {
		                	  try {
		                		  vdao.update(v);
		                	  } catch (Exception e) {
								// TODO: handle exception
		                		vdao.delete(v);
//		                		File fileselfie=new File(pathSelfie);
//		                		File filektp=new File(pathKtp);
//		                		fileselfie.delete();
//		                		filektp.delete();
		                		samba.deletefile(pathSelfie);
		                		samba.deletefile(pathKtp);
		                		LogSystem.error(request, "gagal update file",kelas, refTrx, trxType);
		      			        jo.put("result", "06");
		      			        jo.put("notif", "Registrasi gagal diproses");
		      			        return jo;
							}
		                	  
		                  }
	    		                  
	    		                  
    			        	
    				}
    			
    				catch (Exception e) {
    					// TODO: handle exception
    					
    					LogSystem.error(getClass(), e,kelas, refTrx, trxType);

    				    res="06";
    			        notif="Data gagal diproses";
    				}
    			
//    		}
        }
        else {
        	//jo=aVerf.setResponFailed(jo);
        	notif="UserId atau Password salah";
        }
        
        jo.put("result", res);
        jo.put("notif", notif);
        //jo.put("info", link);
        LogSystem.info(request, "Response akhir : " + jo,kelas, refTrx, trxType);
        return jo;
	}
	
	class MailSender{

		String email;
		PreRegistration name;
		String id;
		
		public MailSender(PreRegistration name,String email, String id) {
			this.email=email;
			this.name=name;
			this.id=id;
		}
		public void run() {
			new SendMailSSL().sendMailPreregisterMitra(name, email, id);

		}
		
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
