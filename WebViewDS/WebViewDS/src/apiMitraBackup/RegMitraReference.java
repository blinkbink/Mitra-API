package apiMitraBackup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.billing.KillBillDocument;
import id.co.keriss.consolidate.action.billing.KillBillPersonal;
import id.co.keriss.consolidate.dao.EeuserMitraDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.EeuserMitra;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.RefRegistrasi;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;


public class RegMitraReference extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("billingLog");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		//System.out.println("DATA DEBUG :"+(i++));
		LogSystem.info(request, "DATA DEBUG :"+(i++));
		Mitra mitra=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart");
					JSONObject jo=new JSONObject();
					jo.put("res", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					
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
						LogSystem.info(request, "Token ada : " +  token);
						mitra=tm.getMitra();
					} else {
						LogSystem.error(request, "Token null");
						JSONObject jo=new JSONObject();
						jo.put("res", "55");
						jo.put("notif", "token salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						
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
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI());
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         if(jsonRecv==null) {
	        	 JSONObject jo=new JSONObject();
	        	 jo.put("res", "30");
				 jo.put("notif", "Format message anda salah");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				 return;
	         }
	         
	         
	         String userid=jsonRecv.getString("userid");
	         UserManager user=new UserManager(getDB(context));
	         User eeuser=user.findByUsername(userid);
	         if(eeuser!=null) {
	        	 if(eeuser.getMitra().getId()==mitra.getId() && eeuser.isAdmin()) {
	        		 LogSystem.info(request, "Token dan mitra valid");
	        		 //System.out.println("token dan mitra valid");
	        	 }
	        	 else {
	        		 LogSystem.error(request, "Token dan mitra tidak valid");
	        		 JSONObject jo=new JSONObject();
					 jo.put("res", "55");
					 jo.put("notif", "Token dan Mitra tidak sesuai");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					
					 return;
	        	 }
	         }
	         else {
	        	 LogSystem.error(request, "Userid tidak ditemukan");
	        	 JSONObject jo=new JSONObject();
				 jo.put("res", "12");
				 jo.put("notif", "userid tidak ditemukan");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				
				 return;
	         }
	         
	         JSONObject jo = null;
	         //jo=RegisterUserMitra(jsonRecv, fileSave, context);
	         jo=register(mitra, jsonRecv, fileSave, context, request);
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo);

			
		}catch (Exception e) {
            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
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
	
	
	
	JSONObject register(Mitra mitra,JSONObject jsonRecv,List<FileItem> fileSave,JPublishContext context, HttpServletRequest request) throws JSONException{
		Boolean default_ttd = false;
				
		User userRecv;
		DB 	db = getDB(context);
        JSONObject jo=new JSONObject();
        String res="05";
        String notif="Email sudah terdaftar gunakan email lain";
		Userdata userdata= null;
		User login =new User();
		Alamat alamat=new Alamat();
		UserdataDao udataDao=new UserdataDao(db);
		//User user=null;
		boolean fNik=false;
		FileItem ktpItm = null, WajahItm = null, ttditm = null, npwpItm = null;
        for (FileItem fileItem : fileSave) {
    	  if(fileItem.getFieldName().equals("fotodiri")){
    		  WajahItm=fileItem;
      	  }else   if(fileItem.getFieldName().equals("fotoktp")){
          	  ktpItm=fileItem;
      	  }else if(fileItem.getFieldName().equals("ttd")) {
      		  ttditm=fileItem;	  
      	  }else if(fileItem.getFieldName().equals("fotonpwp")) {
      		  npwpItm=fileItem;
      	  }
        }
        
        if(WajahItm==null || ktpItm==null /*|| ttditm==null*/) {
        	
		    res="28";
	        notif="Data upload tidak lengkap";
	        jo.put("result", res);
	        jo.put("notif", notif);
	        return jo;
        }
        
        if(jsonRecv.has("default_ttd"))
        {
        	default_ttd = jsonRecv.getBoolean("default_ttd");
        }
           
        User user=null;
        user= new UserManager(db).findByUserNik(jsonRecv.getString("email"), jsonRecv.getString("idktp"));
        if(user!=null){
			//notif="Nomor KTP sudah terdaftar dengan email lain";
			//User user= new UserManager(db).findByUsername(jsonRecv.get("email").toString());
			//if(user!=null) {
				//if(user.getNick().equalsIgnoreCase(jsonRecv.getString("email"))) {
					notif="User anda sudah terdaftar, silahkan gunakan layanan digisign";	
					res="00";
					if(jsonRecv.has("pwd_user")) 
						/*
					{
						EeuserMitraDao emdao=new EeuserMitraDao(db);
						EeuserMitra em=emdao.findByeeUser(mitra.getId(), user.getId());
					
						if(em==null) {
							em=new EeuserMitra();
							em.setEeuser(user);
							em.setMitra(mitra);
							em.setPassword(jsonRecv.getString("pwd_user"));
							em.setCreatedate(new Date());
							try {
								emdao.create(em);
							} catch (Exception e) {
								// TODO: handle exception
								res="06";
								notif="gagal registrasi";
							}
						}
					}
					*/
					jo.put("result", res);
			        jo.put("notif", notif);
			        return jo;
					
				//}
				
			//}
		}else if(new UserManager(db).findByUsername2(jsonRecv.get("email").toString())!=null) {
			notif="Email sudah terdaftar. Gunakan email lain.";
			res="14";
			jo.put("result", res);
	        jo.put("notif", notif);
	        return jo;
		}else {
			
			/*
			FaceRecognition fRec=new FaceRecognition();
			JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()));
			
			if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
			    res="05";
			    notif=respFace.getString("info");
				
			}
//			JSONObject respFace=null;
//			if(respFace!=null)return respFace;
			else {
			*/
			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16) {
				jo.put("result", "14");
				jo.put("notif", "Format NIK Salah");
				
				fNik=false;
				return jo;
			}
			
					if((userdata=udataDao.findByKtp(jsonRecv.getString("idktp")))!= null) {
						
						fNik=true;
						jo.put("result", "14");
	    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
	    				return jo;
					}
					else {
						userdata=new Userdata();
						if(mitra.isVerifikasi()) {
							FaceRecognition fRec=new FaceRecognition();
							JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()));
							
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
							LogSystem.error(request, "assssssssssssssssssssssssssssssssssssssssss" + result.toString());
							if(result.get("result").equals("00")) {
								LogSystem.error(request, "Hasilnya berhasil");
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
//								JSONObject data = null;
//								if(result.has("dataID") && result.getJSONObject("dataID").has("data"))
//								{
//									data = new JSONObject();
//									if(!result.getJSONObject("dataID").isNull("data"))
//									{
//										data = result.getJSONObject("dataID").getJSONObject("data");
//									}
//								}
//								
//								jo.put("result", "12");
//			    				jo.put("notif", "verifikasi user gagal.");
//			    				jo.put("data", data);
//			    				jo.put("info", result.get("information"));
//			    				return jo;
								
								JSONObject data = null;
								boolean matchFace = false;
								
								if(result.has("dataFace") && result.getJSONObject("dataFace").has("data") && !result.getJSONObject("dataFace").get("data").equals(""))
								{
									
									if(result.getJSONObject("dataFace").getJSONObject("data").has("selfie_photo") && result.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo") > 55)
									{
										matchFace = true;
										LogSystem.info(request, "Match :" + matchFace);
									}
								}
								else
								{
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("data", result.getJSONObject("dataID").getJSONObject("data"));
				    				jo.put("info", "Verifikasi wajah gagal");
				    				return jo;
								}
								
								if(result.get("connection").equals(false))
								{
									jo.put("result", "12");
				    				jo.put("notif", "verifikasi user gagal.");
				    				jo.put("info", "Masalah koneksi, mohon ulangi kembali");
				    				return jo;
								}
								
								if(result.has("dataID") && result.getJSONObject("dataID").has("data"))
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
						
								jo.put("result", "28");
			    				jo.put("notif", "verifikasi user gagal.");
			    				jo.put("data", data);
			    				jo.put("info", result.get("information"));
			    				LogSystem.error(request, "Isi jo :" + jo);
			    				//System.out.println("Isi jo :" + jo);
			    				return jo;
							}
						}
						
					}
			        Transaction tx = db.session().beginTransaction();
		
					try{
						if(!fNik) {
					        //userdata.setAlamat(jsonRecv.get("alamat").toString());
					        if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
					        else  userdata.setJk('P');
					        //userdata.setKecamatan(jsonRecv.get("kecamatan").toString());
					        //userdata.setKelurahan(jsonRecv.get("kelurahan").toString());
					        //userdata.setKodepos(jsonRecv.get("kode_pos").toString());
					        //userdata.setKota(jsonRecv.get("kota").toString());
					        userdata.setNama(jsonRecv.get("nama").toString());
					        userdata.setNo_handphone(jsonRecv.get("tlp").toString());
					        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
					        //userdata.setPropinsi(jsonRecv.get("provinci").toString());
					        userdata.setNo_identitas(jsonRecv.get("idktp").toString());
					        userdata.setTempat_lahir(jsonRecv.get("tmp_lahir").toString());
					        userdata.setMitra(mitra);
					        db.session().save (userdata);
					        
					        alamat.setAlamat(jsonRecv.get("alamat").toString());
					        alamat.setKecamatan(jsonRecv.get("kecamatan").toString());
					        alamat.setKelurahan(jsonRecv.get("kelurahan").toString());
					        alamat.setKodepos(jsonRecv.get("kode-pos").toString());
					        alamat.setKota(jsonRecv.get("kota").toString());
					        alamat.setPropinsi(jsonRecv.get("provinci").toString());
					        alamat.setUserdata(userdata);
					        alamat.setStatus('1');
					        db.session().save(alamat);
						}
				        
				        //System.out.println("masuk");
				        LogSystem.error(request, "Masuk");

				        
				        //login.setPassword(EEUtil.getHash(jsonRecv.get("email").toString(), jsonRecv.get("password").toString()));
				        login.setNick(jsonRecv.get("email").toString());
				        login.setName(jsonRecv.get("nama").toString());
				        login.grant("ds");
				        login.grant("login");
				        login.setUserdata(userdata);
				        login.logRevision("created", new UserManager(db).findById((long) 0));
				        login.setStatus('1');
				        login.setPay_type('1');
				        login.setTime(new Date());
		//		        udataDao.create(userdata);
				        
		
				        //System.out.println("masuk 2");
				        LogSystem.error(request, "Masuk 2");

		
		//		        DigiSign ds=null;
		//	
		//		        if(jsonRecv.get("pk")!=null ){
		//		        	 ds=new DigiSign();
		//		        	 String pb=ds.signKey(jsonRecv.get("pk").toString());
		//						
		//		 	         Key keyPubData=new Key();
		//		 			 keyPubData.setJenis_key(new JenisKey("PS"));
		//		 			 keyPubData.setKey(Base64.toBase64String(ds.getPublicSign().getEncoded()));
		//		 			 keyPubData.setKey_id(String.valueOf(ds.getPublicSign().getKeyID()));
		//		 			 keyPubData.setStatus(new StatusKey("ACT"));
		//		 			 keyPubData.setUserdata(userdata);
		//		 			 keyPubData.setUser_id((String) ds.getPublicSign().getUserIDs().next());
		//		 			 keyPubData.setWaktu_buat(new Date());
		//		 			 new KeyDao(db).create(keyPubData);
		//	        	}
						 
						 
			//			System.out.println("PB :"+pb); 
			//			System.out.println("CEK PB "+pb.equals(Base64.toBase64String(ds.getPublicSign().getEncoded()))); 
			//			System.out.println("VERF :"+ds.verifyPublicKey(keyPubData.getKey())); 
						 
				        db.session().save (login);
				        
				        
				        
				        
				        RefRegistrasi refreg=new RefRegistrasi();
				        refreg.setEeuser(login);
				        refreg.setMitra(mitra);
				        if(jsonRecv.has("nama_admin") && jsonRecv.has("no_ref")) {
				        	refreg.setAdmin_verifikasi(jsonRecv.getString("nama_admin"));
					        refreg.setNoref_pendaftaran(jsonRecv.getString("no_ref"));
				        }
				        
				        refreg.setCreate_date(new Date());
				        db.session().save(refreg);
				        
//				        EeuserMitra em=new EeuserMitra();
//				        em.setEeuser(login);
//				        em.setMitra(mitra);
//				        //em.setPassword(jsonRecv.getString("pwd_user"));
//				        em.setCreatedate(new Date());
//				        db.session().save(em);
				        
				        res="00";
				        notif="Registrasi berhasil, layanan sudah bisa digunakan.";
		//				if(ds!=null)jo.put("key", Base64.toBase64String(ds.getPublicSign().getEncoded()));
						
				        LogSystem.error(request, "Masuk 3");

		//		        System.out.println("masuk 3");
		
						//System.out.println("data size :" +fileSave.size());
						LogSystem.info(request, "Data size :" +fileSave.size());

						  if (fileSave.size() >=3 || (fileSave.size() >=2 && default_ttd == true)) {
					          res="00";
							  UserManager mgr = new UserManager (db);
							  User userTrx =login;
							  
							 
							  String uploadTo = basepath+userTrx.getId()+"/original/";
							  String directoryName = basepath+userTrx.getId()+"/original/";
							  String viewpdf = basepath+userTrx.getId()+"/original/";
							  File directory = new File(directoryName);
							  if (!directory.exists()){
							       directory.mkdirs();
							  }
							  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
			
							  Date date = new Date();
							  String strDate = sdfDate.format(date);
							  String rename = "DS"+strDate+".png";

							  if(ttditm == null){
								  LogSystem.info(request, "Ga ada ttd, chek default");
		                		  //System.out.println("Ga ada ttd, chek default");
		                		  if(default_ttd)
		                		  {
		                			  //System.out.println("Default true");
		                			  LogSystem.info(request, "Default True");
		                			  try {
		                		            generateQRCodeImage(jsonRecv.getString("nama")+ ",<" + jsonRecv.getString("email")+">", 400, 400, uploadTo+"ttd.png");
		                			  	} catch (WriterException e) {
		                			  		LogSystem.error(request, "Could not generate QR Code, WriterException :: " + e.getMessage());
		                		            //System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
		                		        } catch (IOException e) {
		                		        	LogSystem.error(request, "Could not generate QR Code, IOException :: " + e.getMessage());
		                		            //System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
		                		        }
		                			  LogSystem.info(request, uploadTo);
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
							  
			                  for (FileItem fileItem : fileSave) {
			                	  
//			                	  if(fileItem.getFieldName().equals("ttd")) {
//			                		  System.out.println("Ada tandatangannya nih ga pake default");
//			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".png");
//
//			                          BufferedImage source =  ImageIO.read(fileItem.getInputStream());
//			                          int color = source.getRGB(0, 0);
//			                          Image data=FileProcessor.makeColorTransparent(source, new Color(color), 10000000);
//			                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,source.getWidth(),
//			                           		  source.getHeight());
//				                      ImageIO.write(newBufferedImage, "png" ,fileTo);
//				                      System.out.println("File ttd :" + fileTo.getPath());
//			                    	  userdata.setImageTtd(fileTo.getPath());
//			                	  }
			                	  if(fileItem.getFieldName().equals("ttd")){
			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".png");
			                    	  fileItem.write(fileTo);
			                    	  System.out.println("File ttd :" + fileTo.getPath());
			                    	  userdata.setImageTtd(fileTo.getPath());
			                	  }
			                	  else if(fileItem.getFieldName().equals("fotodiri")){
			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                    	  fileItem.write(fileTo);
			                    	  System.out.println("File selfie :" + fileTo.getPath());
			                    	  userdata.setImageWajah(fileTo.getPath());
			                	  } else if(fileItem.getFieldName().equals("fotoktp")){
			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                    	  fileItem.write(fileTo);
			                    	  System.out.println("File ktp :" + fileTo.getPath());
			                    	  userdata.setImageKTP(fileTo.getPath());
			                	  } else if(fileItem.getFieldName().equals("fotonpwp") && npwpItm!=null){
				            		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
				                	  fileItem.write(fileTo);
				                	  System.out.println("File npwp :" + fileTo.getPath());
				                	  userdata.setImageNPWP(fileTo.getPath());
			                	  }
			                  
			                  }
			  		          db.session().update(userdata);
		
		//	                  udataDao.update(userdata);
			                  
						  }else {
		
							    res="28";
						        notif="Data upload tidak lengkap";
						        jo.put("result", res);
						        jo.put("notif", notif);
						        return jo;
						  }
			
					     tx.commit ();
					     
					    //MailSenderRegister mail=new MailSenderRegister(userdata,jsonRecv.get("email").toString());
						//mail.run();
					     
					     if(mitra.isNotifikasi()) {
					    	 SendSuksesRegistrasi ssr=new SendSuksesRegistrasi();
						     ssr.kirim(login.getName(), String.valueOf(userdata.getJk()), login.getNick(), DSAPI.DOMAIN, String.valueOf(mitra.getId()));
					     }
					     					
						UserManager um=new UserManager(db);

				        try {
				        	 //bikin user
				        	KillBillPersonal kp=new KillBillPersonal();
				        	KillBillDocument kd=new KillBillDocument();
				        	kp.createKillbill(login, log, "ID"+login.getId(), alamat);
				        	kd.createKillbill(login, log, "ID"+login.getId(), alamat);
				        	
				        	/*
					        BillingSystem bs=new BillingSystem();
					        bs.createUser(um.findByUsername(login.getNick()));
					        
				        	DocumentsAccessDao accessDao=new DocumentsAccessDao(db);
							List<DocumentAccess>listDocAccess =accessDao.findOtherDoc(login);
							JSONArray lAcsArray= new JSONArray();
							for(DocumentAccess acs:listDocAccess) {
								if(acs.getEeuser()==null) {
									acs.setEeuser(login);
									accessDao.update(acs);
								}
							}
							*/
				        }
				        catch (Exception e) {
				        	e.printStackTrace();
						}
					     
					}
					catch (Exception e) {
						// TODO: handle exception
						LogSystem.error(getClass(), e);
		//				log.error(e);
		//				new UserdataDao(db).delete(userdata);
						tx.rollback();
					    res="05";
				        notif="Data gagal diproses";
					}
			//Tutup untuk liveness
			//}
		}
        jo.put("result", res);
        jo.put("notif", notif);
		
	   return jo;
	   
	}
	
	private static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
	
}


