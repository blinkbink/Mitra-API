package apiMitraBackup;

import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLEncoder;
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


public class RegMitra extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";

	
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
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart");
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
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
						TokenMitra tm=tmd.findByToken(token.toLowerCase());
						//System.out.println("token adalah = "+token);
						if(tm!=null) {
							LogSystem.info(request, "Token ada : " + token);
							mitra=tm.getMitra();
						} else {
							LogSystem.error(request, "Token null ");
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
	         
	         if(mitra!=null) {
	        	 
	        	 String userid=jsonRecv.getString("userid");
		         UserManager user=new UserManager(getDB(context));
		         //User eeuser=user.findByUsername(userid);
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 LogSystem.info(request, "Token dan mitra valid");
		        	 }
		        	 else {
		        		 LogSystem.error(request, "Token dan mitra tidak sesuai");
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
	         }
	         
	         
	         JSONObject jo = null;
	         jo=RegisterUserMitra(jsonRecv, fileSave, context, mitra, useradmin, request);
	         
	         
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
				jo.put("notif", "Request Data tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
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
        
        ApiVerification aVerf = new ApiVerification(db);
        boolean vrf=false;
        if(mitratoken!=null && useradmin!=null) {
        	vrf=true;
        }
        else {
        	vrf=aVerf.verification(jsonRecv);
        }
        
        if(vrf){
        	User usr=null;
        	Mitra mitra=null;
        	if(mitratoken==null && useradmin==null) {
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
        	PreRegistration userdata= new PreRegistration();
    		//Mitra mitra =new MitraDao(db).findMitra(namaMitra);
    		
    		PreRegistrationDao udataDao=new PreRegistrationDao(db);
    		if(udataDao.findEmail(jsonRecv.getString("email"))!=null) {
    			jo.put("result", "14");
				jo.put("notif", "Email sudah terdaftar, namun belum melakukan aktivasi. Silahkan untuk melakukan aktivasi sebelum data dihapus dari daftar aktivasi.");
				
				return jo;
    		}
    		
    		if(udataDao.findNik(jsonRecv.getString("idktp"))!=null) {
    			jo.put("result", "14");
				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
				
				return jo;
    		}
    		boolean nextProcess=true;
    		
    		//UserManager um=new UserManager(db);
    		//User usr= um.findByUsername(jsonRecv.get("userid").toString());
    		
    		//User usr = aVerf.getEeuser();
    		//Mitra mitra = usr.getMitra();
    		User user=null;
    		boolean fNik=false;
    		
	    	  	if(jsonRecv.has("reg_number"))
	        	{
	        		 reg_number = jsonRecv.getString("reg_number");
	        		 LogSystem.info(request, "Reg number : " + reg_number);
	        	}
	    	  	
    			user= new UserManager(db).findByUserNik(jsonRecv.getString("email"), jsonRecv.getString("idktp"));
    			if(user!=null) {
    				jo.put("result", "00");
    				jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
    				if(user.getStatus()=='4')jo.put("notif", "User anda diblok, hubungi pihak Digisign");
    				return jo;
    			} else {
	    			user= new UserManager(db).findByUsername(jsonRecv.get("email").toString()); 
	    			if(user!=null) {
	    				nextProcess=false;
	    				jo.put("result", "14");
	    				jo.put("notif", "Email sudah terdaftar dengan NIK lain. Gunakan email lain.");
	    				if(user.getStatus()=='4')jo.put("notif", "User anda diblok, hubungi pihak Digisign");
	    				return jo;
	    			}
    			}
    			
    			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16) {
    				jo.put("result", "14");
    				jo.put("notif", "Format NIK Salah");
    				
    				fNik=false;
    				return jo;
    			}
    			
    			Userdata userData=new UserdataDao(db).findByKtp(jsonRecv.get("idktp").toString());
    			if(userData!=null) {
    				/*
    				nextProcess=false;
    				notif="No identitas sudah terdaftar, gunakan no identitas lain.";
    				if(user!=null) {
    					if(user.getStatus()=='4') {
    						notif="user anda diblok, hubungi digisign";
    					}
    				}
    				*/
    				jo.put("result", "14");
    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
    				
    				fNik=true;
    				return jo;
    			}
    			
    			if(nextProcess) {
    				try{
    					//Userdata udata=new Userdata();
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
    			        
    			        if(WajahItm==null || ktpItm==null) {
    			        	LogSystem.info(request, "KTP atau selfie null");
    					    res="28";
    				        notif="Data upload tidak lengkap";
    				        jo.put("result", res);
    				        jo.put("notif", notif);
    				        return jo;
    			        }

    					//check json semua key mandatori
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
							JSONObject result = cd.check(jsonRecv.getString("idktp"), jsonRecv.getString("nama"), jsonRecv.getString("tmp_lahir"), jsonRecv.getString("tgl_lahir"), jsonRecv.getString("alamat"), new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII), mitra.getName());
							//JSONObject obj=
							if(result.get("result").equals("00")) {
								LogSystem.info(request, "Hasilnya berhasi");
								//System.out.println("hasil nya berhasil");
							}
							else {
//								JSONObject data = null;
//								if(result.has("dataID") && result.getJSONObject("dataID").has("data"))
//								{
//									data = new JSONObject();
//									if(!result.getJSONObject("dataID").isNull("data"))
//									{
//										data = result.getJSONObject("dataID").getJSONObject("data");
//								
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
			    				LogSystem.info(request, "Isi jo : " + jo);
			    				//System.out.println("Isi jo :" + jo);
			    				return jo;
							}
						}
    					
    			        userdata.setAlamat(jsonRecv.get("alamat").toString());
    			        if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
    			        else  userdata.setJk('P');
    			        userdata.setKecamatan(jsonRecv.get("kecamatan").toString());
    			        userdata.setKelurahan(jsonRecv.get("kelurahan").toString());
    			        userdata.setKodepos(jsonRecv.get("kode-pos").toString());
    			        userdata.setKota(jsonRecv.get("kota").toString());
    			        userdata.setNama(jsonRecv.get("nama").toString());
    			        userdata.setNo_handphone(jsonRecv.get("tlp").toString());
    			        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
    			        userdata.setPropinsi(jsonRecv.get("provinci").toString());
    			        userdata.setNo_identitas(jsonRecv.get("idktp").toString());
    			        userdata.setTempat_lahir(jsonRecv.get("tmp_lahir").toString());
    			        userdata.setEmail(jsonRecv.get("email").toString());
    			        userdata.setData_exists(false);
    			        userdata.setNpwp(jsonRecv.get("npwp").toString());
    			        userdata.setMitra(mitra);
    			        if(fNik) {
    			        	userdata.setData_exists(fNik);
    			        	userdata.setNo_handphone(userData.getNo_handphone());
    			        }
    			        Long idUserData=udataDao.create(userdata);
    			        LogSystem.info(request, "IDUserData : " + idUserData);
    			        //System.out.println("idUserData = "+idUserData);
    			        
    			        if(idUserData!=null) {
	    			        userdata.setId(idUserData.longValue());
	    			      
	    					
	    					System.out.println("data size :" +fileSave.size());
	    					LogSystem.info(request, "Data size : " + fileSave.size());
	    					  if (fileSave.size() >=1) {
	    				          res="00";
	    				          notif="Berhasil, silahkan check email untuk aktivasi akun anda.";
	    		
	    						  String uploadTo = basepathPreReg+userdata.getId()+"/original/";
	    						  String directoryName = basepathPreReg+userdata.getId()+"/original/";
	    						  //String viewpdf = basepathPreReg+userdata.getId()+"/original/";
	    						  File directory = new File(directoryName);
	    						  if (!directory.exists()){
	    						       directory.mkdirs();
	    						  }
	    						  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
	    		
	    		//				  Ttd ttd = new Ttd();
	    						  Date date = new Date();
	    						  String strDate = sdfDate.format(date);
	    						  String rename = "DS"+strDate+".png";
	    		//				  ttd.setUserdata(userdata);
	    		//                  ttd.setWaktu_buat(date);
	    						  boolean ktp =false;
	    						  boolean selfi=false;
	    		                  for (FileItem fileItem : fileSave) {
	    		                	  LogSystem.info(request, fileItem.getFieldName());
	    		                	  //System.out.println(fileItem.getFieldName());
	    		                	  if(fileItem.getFieldName().equals("ttd") && fileItem.getSize()!=0) {
	    		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".png");
	//    		                    	  fileItem.write(fileTo);
	    		                          BufferedImage source =  ImageIO.read(fileItem.getInputStream());
	    		                          int color = source.getRGB(0, 0);
	    		                          Image data=FileProcessor.makeColorTransparent(source, new Color(color), 10000000);
	    		                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,source.getWidth(),
	    		                           		  source.getHeight());
	    			                      ImageIO.write(newBufferedImage, "png" ,fileTo);
	
	    		                    	  userdata.setImageTtd(fileTo.getPath());
	    		                	  }
	    		                	  else if(fileItem.getFieldName().equals("fotodiri")){
	    		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
	    		                    	  fileItem.write(fileTo);
	    		                    	  userdata.setImageWajah(fileTo.getPath());
	    		                    	  selfi=true;
	    		                	  }else if(fileItem.getFieldName().equals("fotoktp")){
	    		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
	    		                    	  fileItem.write(fileTo);
	    		                    	  userdata.setImageKTP(fileTo.getPath());
	    		                    	  ktp=true;
	    		                	  }else if(fileItem.getFieldName().equals("fotonpwp") && fileItem.getSize()!=0){
	    			            		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
	    			            		  fileItem.write(fileTo);
	    			            		  userdata.setImageNPWP(fileTo.getPath());		            		  
	    		                	  }
	    		                  }
	    		                  
	    		                  if(ktp==true && selfi==true) {
	    				                udataDao.update(userdata);
	    				                //System.out.println("userdata ID : "+ userdata.getId());
	    				                LogSystem.info(request, "Userdata ID : "+ userdata.getId());
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
		    				                SendActivasi sa=new SendActivasi();
		    				                sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()), reg_number);
			    							
		    				                //sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()));
		    							}
	    						  }
	    		                  else {
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
    						  if(jsonRecv.get("alamat").toString().length()>50) {notif="teks alamat maksimum 50 karakter";}
    						  if(jsonRecv.get("kecamatan").toString().length()>30) {notif="teks kecamatan maksimum 30 karakter";}
    						  if(jsonRecv.get("kelurahan").toString().length()>30) {notif="teks kelurahan maksimum 30 karakter";}
    						  if(jsonRecv.get("kota").toString().length()>30) {notif="teks kota maksimum 30 karakter";}
    						  if(jsonRecv.get("nama").toString().length()>50) {notif="teks nama maksimum 30 karakter";}
    						  if(jsonRecv.get("provinci").toString().length()>50) {notif="teks provinsi maksimum 30 karakter";}
    						  if(jsonRecv.get("email").toString().length()>80) {notif="teks email maksimum 80 karakter";}
    					  }
    			        	
    				}
    			
    				catch (Exception e) {
    					// TODO: handle exception
    					udataDao.delete(userdata);
    					LogSystem.error(getClass(), e);
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
	
}
