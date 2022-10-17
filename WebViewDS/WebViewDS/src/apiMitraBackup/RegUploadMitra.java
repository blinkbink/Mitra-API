//package apiMitraBackup;
//
//import id.co.keriss.consolidate.DS.FaceRecognition;
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.ApiVerification;
//import id.co.keriss.consolidate.action.ajax.SendMailSSL;
//import id.co.keriss.consolidate.action.billing.KillBillDocument;
//import id.co.keriss.consolidate.action.billing.KillBillPersonal;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.FormatPDFDao;
//import id.co.keriss.consolidate.dao.InitialDao;
//import id.co.keriss.consolidate.dao.InvoiceDao;
//import id.co.keriss.consolidate.dao.LetakTtdDao;
//import id.co.keriss.consolidate.dao.PreRegistrationDao;
//import id.co.keriss.consolidate.dao.TokenMitraDao;
//import id.co.keriss.consolidate.dao.UserdataDao;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.FormatPdf;
//import id.co.keriss.consolidate.ee.Initial;
//import id.co.keriss.consolidate.ee.LetakTtd;
//import id.co.keriss.consolidate.ee.Mitra;
//import id.co.keriss.consolidate.ee.PreRegistration;
//import id.co.keriss.consolidate.ee.TokenMitra;
//import id.co.keriss.consolidate.ee.Userdata;
//import id.co.keriss.consolidate.ee.VO.EmailVO;
//import id.co.keriss.consolidate.util.AESEncryption;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.FileProcessor;
//import id.co.keriss.consolidate.util.LogSystem;
//import java.awt.Color;
//import java.awt.Image;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Vector;
//
//import javax.imageio.ImageIO;
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.bouncycastle.util.encoders.Base64;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//
//import api.dukcapil.checkingData;
//import api.email.SendActivasi;
//import api.email.SendTerimaDoc;
//import apiMitra.SignDoc;
//
//
//public class RegUploadMitra extends ActionSupport {
//
//	static String basepath="/opt/data-DS/UploadFile/";
//	static String basepathPreReg="/opt/data-DS/PreReg/";
//
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		
//		int i=0;
//		HttpServletRequest  request  = context.getRequest();
//		String jsonString=null;
//		byte[] dataFile=null;
//		List <FileItem> fileSave=new ArrayList<FileItem>() ;
//		List<FileItem> fileItems=null;
//		//System.out.println("DATA DEBUG :"+(i++));
//		LogSystem.info(request, "DATA DEBUG :"+(i++));
//		Mitra mitra=null;
//		User useradmin=null;
//		try{
//				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//				
//				// no multipart form
//				if (!isMultipart) {
//					JSONObject jo=new JSONObject();
//					jo.put("result", "30");
//					jo.put("notif", "Format request API salah.");
//					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//					
//					return;
//				}
//				// multipart form
//				else {
//					TokenMitraDao tmd=new TokenMitraDao(getDB(context));
//					String token=request.getHeader("authorization");
//					if(token!=null) {
//						String[] split=token.split(" ");
//						if(split.length==2) {
//							if(split[0].equals("Bearer"))token=split[1];
//						}
//						TokenMitra tm=tmd.findByToken(token.toLowerCase());
//						//System.out.println("token adalah = "+token);
//						if(tm!=null) {
//							LogSystem.info(request, "Token ada " + token);
//							mitra=tm.getMitra();
//						} else {
//							LogSystem.info(request, "Token null");
//							JSONObject jo=new JSONObject();
//							jo.put("res", "55");
//							jo.put("notif", "token salah");
//							context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//							
//							return;
//						}
//					}
//					
//					
//					// Create a new file upload handler
//					ServletFileUpload upload = new ServletFileUpload(
//							new DiskFileItemFactory());
//
//					// parse requests
//					 fileItems = upload.parseRequest(request);
//
//					// Process the uploaded items
//					for (FileItem fileItem : fileItems) {
//						// a regular form field
//						if (fileItem.isFormField()) {
//							if(fileItem.getFieldName().equals("jsonfield")){
//								jsonString=fileItem.getString();
//							}
//						}
//						else {
//							 int jsonFile= Integer.parseInt(new JSONObject(jsonString).getJSONObject("JSONFile").getString("jml_file"));
////							
////							 if(fileItem.getFieldName().equals("file")){
////								 dataFile=fileItem.get();
////							 }
//							 
//							 for (int fileSize = 1; fileSize <= jsonFile; fileSize++) 
//						     {
//								 
//								 if(fileItem.getFieldName().equals("file"+fileSize)){
//									 System.out.println("Jumlah File :" + fileSize);
//									 fileSave.add(fileItem);
//								 }
//						     }
//							 
//							 if(fileItem.getFieldName().equals("ttd")||fileItem.getFieldName().equals("fotodiri")||fileItem.getFieldName().equals("fotoktp")||fileItem.getFieldName().equals("fotonpwp")){
//								 fileSave.add(fileItem);	 
//							 }
//							
//						}
//					}
//				}
//			 String process=request.getRequestURI().split("/")[2];
//	         //System.out.println("PATH :"+request.getRequestURI());
//	         LogSystem.info(request, "PATH :"+request.getRequestURI());
//
//	         LogSystem.request(request, fileItems);
//			 if(jsonString==null) return;	         
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         
//	         if(mitra!=null) {
//	        	 String userid=jsonRecv.getString("userid");
//		         UserManager user=new UserManager(getDB(context));
//
//		         useradmin=user.findByUsername(userid);
//		         if(useradmin!=null) {
//		        	 if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
//		        		 LogSystem.info(request, "Token dan mitra valid");
//		        		 //System.out.println("token dan mitra valid");
//		        	 }
//		        	 else {
//		        		 LogSystem.error(request, "Token dan mitra tidak valid");
//		        		 JSONObject jo=new JSONObject();
//						 jo.put("res", "12");
//						 jo.put("notif", "Token dan Mitra tidak sesuai");
//						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//						
//						 return;
//		        	 }
//		         }
//		         else {
//		        	 LogSystem.error(request, "Userid tidak ditemukan");
//		        	 JSONObject jo=new JSONObject();
//					 jo.put("res", "12");
//					 jo.put("notif", "userid tidak ditemukan");
//					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//					
//					 return;
//		         }
//	         }
//	         
//	         
//	         JSONObject jo = null;
//	         jo=RegisterUserMitra(jsonRecv, fileSave, context, mitra, useradmin, request);
//	         
//	         
//			String res="";
//			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
//			else res="<b>ERROR 404</b>";
//	        
//
//			context.put("trxjson", res);
//			LogSystem.response(request, jo);
//
//			
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
//
//            JSONObject jo=new JSONObject();
//            try {
//				jo.put("result", "05");
//				jo.put("notif", "Request Data tidak ditemukan");
//				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
//	}
//	
//	JSONObject RegisterUserMitra(JSONObject jsonRecv,List<FileItem> fileSave, JPublishContext context, Mitra mitratoken, User useradmin, HttpServletRequest  request) throws JSONException{
//
//		boolean kirim=false;
//		User userRecv=null;
//		DB db = getDB(context);
//		JSONObject jo=new JSONObject();
//        String res="05";
//        String notif="Email sudah terdaftar gunakan email lain atau silahkan login dengan akun anda";
//		Documents id_doc=null;
//        ApiVerification aVerf = new ApiVerification(db);
//        boolean vrf=false;
//        DocumentsDao docDao = new DocumentsDao(db);
//        if(mitratoken!=null && useradmin!=null) {
//        	vrf=true;
//        }
//        else {
//        	vrf=aVerf.verification(jsonRecv);
//        }
//        
//        if(vrf){
//        	User usr=null;
//        	Mitra mitra=null;
//        	if(mitratoken==null && useradmin==null) {
//	        	if(aVerf.getEeuser().isAdmin()==false) {
//	        		jo.put("result", "12");
//	                jo.put("notif", "userid anda tidak diijinkan.");
//	                return jo;
//	        	}
//	        	usr = aVerf.getEeuser();
//	    		mitra = usr.getMitra();
//        	}
//        	else {
//        		usr=useradmin;
//        		mitra=mitratoken;
//        	}
//        	
//        	PreRegistration userdata= new PreRegistration();
//    		
//    		PreRegistrationDao udataDao=new PreRegistrationDao(db);
//    		if(udataDao.findEmail(jsonRecv.getString("email"))!=null) {
//    			jo.put("result", "14");
//				jo.put("notif", "Email sudah terdaftar, namun belum melakukan aktivasi. Silahkan untuk melakukan aktivasi sebelum data dihapus dari daftar aktivasi.");
//				
//				return jo;
//    		}
//    		boolean nextProcess=true;
//
//    		User user=null;
//    		boolean fNik=false;
//    			user= new UserManager(db).findByUserNik(jsonRecv.getString("email"), jsonRecv.getString("idktp"));
//    			if(user!=null) {
//    				jo.put("result", "00");
//    				jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
//    				if(user.getStatus()=='4')jo.put("notif", "User anda diblok, hubungi pihak Digisign");
//    				return jo;
//    			} else {
//	    			user= new UserManager(db).findByUsername(jsonRecv.get("email").toString()); 
//	    			if(user!=null) {
//	    				nextProcess=false;
//	    				jo.put("result", "14");
//	    				jo.put("notif", "Email sudah terdaftar dengan NIK lain. Gunakan email lain.");
//	    				if(user.getStatus()=='4')jo.put("notif", "User anda diblok, hubungi pihak Digisign");
//	    				return jo;
//	    			}
//    			}
//    			
//    			if(jsonRecv.getString("idktp").equals("")||jsonRecv.getString("idktp")==null||jsonRecv.getString("idktp").length()<16) {
//    				jo.put("result", "14");
//    				jo.put("notif", "Format NIK Salah");
//    				
//    				fNik=false;
//    				return jo;
//    			}
//    			
//    			Userdata userData=new UserdataDao(db).findByKtp(jsonRecv.get("idktp").toString());
//    			if(userData!=null) {
// 
//    				jo.put("result", "14");
//    				jo.put("notif", "NIK sudah terdaftar dengan email lain, silahkan login dengan email yang sesuai NIK atau gunakan NIK lain.");
//    				
//    				fNik=true;
//    				return jo;
//    			}
//    			
//    			
//    			if(nextProcess) {
//    				try{
//    					FileItem ktpItm = null, WajahItm = null, ttditm = null, npwpItm = null;
//    			        for (FileItem fileItem : fileSave) {
//    			    	  if(fileItem.getFieldName().equals("fotodiri")){
//    			    		  WajahItm=fileItem;
//    			      	  }else   if(fileItem.getFieldName().equals("fotoktp")){
//    			          	  ktpItm=fileItem;
//    			      	  }else if(fileItem.getFieldName().equals("ttd")) {
//    			      		  ttditm=fileItem;
//    			      	  }else if(fileItem.getFieldName().equals("fotonpwp")) {
//    			      		  npwpItm=fileItem;
//    			      	  }
//    			        }
//    			        
//    			        if(WajahItm==null || ktpItm==null) {
//    			        	System.out.println("Null Wajah atau KTP");
//    					    res="28";
//    				        notif="Data upload tidak lengkap";
//    				        jo.put("result", res);
//    				        jo.put("notif", notif);
//    				        return jo;
//    			        }
//    					
//						if(mitra.isVerifikasi()) {
//							FaceRecognition fRec=new FaceRecognition();
//							JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()));
//							
//							if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
//							    res="12";
//							    notif=respFace.getString("info");
//							    jo.put("result", "12");
//			    				jo.put("notif", "verifikasi user gagal.");
//			    				jo.put("info", notif);
//			    				return jo;
//							}
//							
//							checkingData cd=new checkingData();
//							JSONObject result = cd.check(jsonRecv.getString("idktp"), jsonRecv.getString("nama"), jsonRecv.getString("tmp_lahir"), jsonRecv.getString("tgl_lahir"), jsonRecv.getString("alamat"), new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII), mitra.getName());
//							//JSONObject obj=
//							if(result.get("result").equals("00")) {
//								System.out.println("hasil nya berhasil");
//							}
//							else {
//								JSONObject data = null;
//								if(result.has("dataID") && result.getJSONObject("dataID").has("data"))
//								{
//									data = new JSONObject();
//									data = result.getJSONObject("dataID").getJSONObject("data");
//								}
//								
//								
//								jo.put("result", "12");
//			    				jo.put("notif", "verifikasi user gagal.");
//			    				jo.put("data", data);
//			    				jo.put("info", result.get("information"));
//			    				return jo;
//							}
//						}
//    					
//    			        userdata.setAlamat(jsonRecv.get("alamat").toString());
//    			        if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
//    			        else  userdata.setJk('P');
//    			        userdata.setKecamatan(jsonRecv.get("kecamatan").toString());
//    			        userdata.setKelurahan(jsonRecv.get("kelurahan").toString());
//    			        userdata.setKodepos(jsonRecv.get("kode-pos").toString());
//    			        userdata.setKota(jsonRecv.get("kota").toString());
//    			        userdata.setNama(jsonRecv.get("nama").toString());
//    			        userdata.setNo_handphone(jsonRecv.get("tlp").toString());
//    			        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
//    			        userdata.setPropinsi(jsonRecv.get("provinci").toString());
//    			        userdata.setNo_identitas(jsonRecv.get("idktp").toString());
//    			        userdata.setTempat_lahir(jsonRecv.get("tmp_lahir").toString());
//    			        userdata.setEmail(jsonRecv.get("email").toString());
//    			        userdata.setData_exists(false);
//    			        userdata.setNpwp(jsonRecv.get("npwp").toString());
//    			        userdata.setMitra(mitra);
//    			        if(fNik) {
//    			        	userdata.setData_exists(fNik);
//    			        	userdata.setNo_handphone(userData.getNo_handphone());
//    			        }
//    			        Long idUserData=udataDao.create(userdata);
//    			        //System.out.println("idUserData = "+idUserData);
//    			        LogSystem.info(request, "idUserData = "+idUserData);
//    			        
//    			        if(idUserData!=null) {
//	    			        userdata.setId(idUserData.longValue());
//	    			      
//	    					//System.out.println("data size :" +fileSave.size());
//	    					LogSystem.info(request, "Data size :" +fileSave.size());
//	    					  if (fileSave.size() >=1) {
//	    				          res="00";
//	    				          notif="Berhasil, silahkan check email untuk aktivasi akun anda.";
//	    		
//	    						  String uploadTo = basepathPreReg+userdata.getId()+"/original/";
//	    						  String directoryName = basepathPreReg+userdata.getId()+"/original/";
//	    						  //String viewpdf = basepathPreReg+userdata.getId()+"/original/";
//	    						  File directory = new File(directoryName);
//	    						  if (!directory.exists()){
//	    						       directory.mkdirs();
//	    						  }
//	    						  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//
//	    						  Date date = new Date();
//	    						  String strDate = sdfDate.format(date);
//	    						  String rename = "DS"+strDate+".png";
//
//	    						  boolean ktp =false;
//	    						  boolean selfi=false;
//	    		                  for (FileItem fileItem : fileSave) {
//	    		                	  //System.out.println(fileItem.getFieldName());
//	    		                	  LogSystem.info(request, fileItem.getFieldName());
//	    		                	  if(fileItem.getFieldName().equals("ttd") && fileItem.getSize()!=0) {
//	    		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".png");
//
//	    		                          BufferedImage source =  ImageIO.read(fileItem.getInputStream());
//	    		                          int color = source.getRGB(0, 0);
//	    		                          Image data=FileProcessor.makeColorTransparent(source, new Color(color), 10000000);
//	    		                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,source.getWidth(),
//	    		                           		  source.getHeight());
//	    			                      ImageIO.write(newBufferedImage, "png" ,fileTo);
//	
//	    		                    	  userdata.setImageTtd(fileTo.getPath());
//	    		                	  }
//	    		                	  else if(fileItem.getFieldName().equals("fotodiri")){
//	    		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
//	    		                    	  fileItem.write(fileTo);
//	    		                    	  userdata.setImageWajah(fileTo.getPath());
//	    		                    	  selfi=true;
//	    		                	  }else if(fileItem.getFieldName().equals("fotoktp")){
//	    		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
//	    		                    	  fileItem.write(fileTo);
//	    		                    	  userdata.setImageKTP(fileTo.getPath());
//	    		                    	  ktp=true;
//	    		                	  }else if(fileItem.getFieldName().equals("fotonpwp") && fileItem.getSize()!=0){
//	    			            		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
//	    			            		  fileItem.write(fileTo);
//	    			            		  userdata.setImageNPWP(fileTo.getPath());		            		  
//	    		                	  }
//	    		                  }
//	    		                  
//	    		                  if(ktp==true && selfi==true) {
//	    				                udataDao.update(userdata);
//	    				                //System.out.println("userdata ID : "+ userdata.getId());
//	    				                LogSystem.info(request, "Userdata ID : "+ userdata.getId());
//	    				                //MailSender mail=new MailSender(userdata,userdata.getEmail(),String.valueOf(userdata.getId()));
//	    								//mail.run();
//	    								
//	    								String docs ="";
//	    								String link ="";
//	    								String namamitra=mitra.getName();
//	    								try {
//	    								   docs = AESEncryption.encryptDoc(String.valueOf(userdata.getId()));
//	    								   link = "https://"+DSAPI.DOMAIN+"/preregistration.html?preregister="
//	    								     + URLEncoder.encode(docs, "UTF-8");
//	    								} catch (Exception e1) {
//	    								   // TODO Auto-generated catch block
//	    								   e1.printStackTrace();
//	    								}
//	    				                SendActivasi sa=new SendActivasi();
//	    				                sa.kirim(jsonRecv.getString("nama"), String.valueOf(userdata.getJk()), jsonRecv.getString("email"), namamitra, link, String.valueOf(mitra.getId()), "");
//	    		                  }
//	    		                  else {
//	    		          		    	res="05";
//	    		      				    notif="data upload tidak lengkap";
//	    		      				    if(userdata.getImageKTP()!=null) {
//	    		      				    	File file=new File(userdata.getImageKTP());
//		    								file.delete();
//	    		      				    }
//	    		      				    if(userdata.getImageWajah()!=null) {
//	    		      				    	File file=new File(userdata.getImageWajah());
//		    								file.delete();
//	    		      				    }
//	    		      				    if(userdata.getImageTtd()!=null) {
//	    		      				    	File file=new File(userdata.getImageTtd());
//		    								file.delete();
//	    		      				    }
//	    		      				    
//	    		      				    udataDao.delete(userdata);
//	    		                  }
//    					  		}
//	    					  else {
//	    						  notif="data upload tidak lengkap";
//	    						  	if(userdata.getImageKTP()!=null) {
//	    						  		File file=new File(userdata.getImageKTP());
//	    								file.delete();
//	  		      				    }
//	  		      				    if(userdata.getImageWajah()!=null) {
//	  		      				    	File file=new File(userdata.getImageWajah());
//		    							file.delete();
//	  		      				    }
//	  		      				    if(userdata.getImageTtd()!=null) {
//	  		      				    	File file=new File(userdata.getImageTtd());
//		    							file.delete();
//	  		      				    }
//	    						  udataDao.delete(userdata);
//	    					  }
//    					  } else {
//    						  	if(userdata.getImageKTP()!=null) {
//	  						  		File file=new File(userdata.getImageKTP());
//	  								file.delete();
//		      				    }
//		      				    if(userdata.getImageWajah()!=null) {
//		      				    	File file=new File(userdata.getImageWajah());
//	    							file.delete();
//		      				    }
//		      				    if(userdata.getImageTtd()!=null) {
//		      				    	File file=new File(userdata.getImageTtd());
//	    							file.delete();
//		      				    }
//		      				    
//    						  udataDao.delete(userdata);
//    						  res="05";
//    						  notif="Data registrasi tidak lengkap";
//    						  if(jsonRecv.get("alamat").toString().length()>50) {notif="teks alamat maksimum 50 karakter";}
//    						  if(jsonRecv.get("kecamatan").toString().length()>30) {notif="teks kecamatan maksimum 30 karakter";}
//    						  if(jsonRecv.get("kelurahan").toString().length()>30) {notif="teks kelurahan maksimum 30 karakter";}
//    						  if(jsonRecv.get("kota").toString().length()>30) {notif="teks kota maksimum 30 karakter";}
//    						  if(jsonRecv.get("nama").toString().length()>50) {notif="teks nama maksimum 30 karakter";}
//    						  if(jsonRecv.get("provinci").toString().length()>50) {notif="teks provinsi maksimum 30 karakter";}
//    						  if(jsonRecv.get("email").toString().length()>80) {notif="teks email maksimum 80 karakter";}
//    					  }
//    			        	
//    		
//    			        int jml_file = Integer.parseInt(jsonRecv.getString("jml_file"));
//						
//    			        //Process sendDocument
//    			        FileProcessor fProc=new FileProcessor();
//    			        //System.out.println("Check Document ID");
//    			        LogSystem.info(request, "Check Document ID");
//    					//if(jsonRecv.getString("document_id").equals("") || jsonRecv.getString("document_id")==null) {
//    			        for(int k = 0 ; k < jml_file ; k++)
//    			        {
//	    			        if(jsonRecv.getJSONArray("ket_file").getJSONObject(k).getString("document_id")=="" || jsonRecv.getJSONArray("ket_file").getJSONObject(k).getString("document_id")==null) 
//	    			        {
//	    						  jo.put("result", "06");
//	    			              jo.put("notif", "document id "+k+" kosong");
//	    			              return jo;
//	    					}
//    			        }
//    			        
//    					int loop = 0;
//    			        //System.out.println("Save file dokumen");
//    			        LogSystem.info(request, "Save file dokumen");
//    			        do {
//    			        	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//	    					for (FileItem fileItem : fileSave) 
//	    					{
//	    						try {
//	    								 if(fileItem.getFieldName().startsWith("file"))
//	    								 {
//	    									
//			    							if(fProc.uploadFILEnQRMitraArray(request, db, usr, fileItem, jsonRecv.getJSONArray("ket_file").getJSONObject(loop).getString("document_id"), "DS"+sdfDate.format(new Date())+loop+".pdf")) {
//			    								res="00";
//			    								LogSystem.info(request, "Document id: "+jsonRecv.getJSONArray("ket_file").getJSONObject(loop).getString("document_id"));
//			    								LogSystem.info(request, "Upload Successfully");
//			    								//System.out.println("Document id: "+jsonRecv.getJSONArray("ket_file").getJSONObject(loop).getString("document_id")); 
//			    								//System.out.println("Upload Successfully");    
//			    							}
//	    									loop += 1;
//	    								 }
//	    						} catch (Exception e) {
//	    							//System.out.println("Error can't save file document");
//	    							LogSystem.info(request, "Error can't save file document");
//	    							e.printStackTrace();
//	    						}
//	    					}
//    			        }while(loop < jml_file);
//    					
//    					id_doc=fProc.getDc();
//    					Vector<DocumentAccess> lttd=new Vector();
//    					Vector<EmailVO> emails=new Vector<>();
//    					DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
//    					
//    					if(id_doc!=null) {
//    						
//    						int clear=dAccessDao.clearAkses(id_doc.getId());
//    						LogSystem.info(request, "clear access id: "+id_doc.getId()+", "+clear+" rows");
//    						//System.out.println("clear access id: "+id_doc.getId()+", "+clear+" rows");
//    						
//    						if(!jsonRecv.has("format_doc")) jsonRecv.put("format_doc", "");
//    						//System.out.println("KETERANGAN FILE "+ jsonRecv.has("ket_file"));
//    						LogSystem.info(request, "KETERANGAN FILE "+ jsonRecv.has("ket_file"));
//    						if(jsonRecv.has("ket_file"))
//    						{
//    							JSONArray ketFileList=jsonRecv.getJSONArray("ket_file");
//    							for(int fileSize = 0 ; fileSize < jml_file ; fileSize++)
//    							{
//    								JSONObject objKetFile=(JSONObject) ketFileList.get(fileSize);
//    								if(jsonRecv.getJSONArray("ket_file").getJSONObject(fileSize).get("req-sign")!=null) {
//		    						//if(jsonRecv.get("req-sign")!=null) {
//		    							//JSONArray sendList=(JSONArray) jsonRecv.getJSONArray("req-sign");
//		    							JSONArray sendList=ketFileList.getJSONObject(fileSize).getJSONArray("req-sign");
//		    							FormatPDFDao fdao=new FormatPDFDao(db);
//		    							LetakTtdDao ltdao=new LetakTtdDao(db);
//		    							
//		    							for(int i=0; i<sendList.length(); i++) {
//		    								JSONObject obj=(JSONObject) sendList.get(i);
//		    								
//		    								DocumentAccess da=new DocumentAccess();
//		    								User userEmail= new UserManager(db).findByUsername(obj.getString("email").toLowerCase());
//		    								/*change to eeuser*/
//		
//		    								//System.out.println("size json object = "+jsonRecv.getString("format_doc").length());
//		    								LogSystem.info(request, "Size json object = "+jsonRecv.getString("format_doc").length());
//		    								if(!obj.has("aksi_ttd")||obj.getString("aksi_ttd").equals(""))obj.put("aksi_ttd", "mt");
//		    								if(!obj.has("user")||obj.getString("user").equals(""))obj.put("user", "ttd0");
//		    								
//		    								if(jsonRecv.getString("format_doc").length()>0) {
//		    									Long idmitra = usr.getMitra().getId();
//		    									//System.out.println("id mitra = "+idmitra);
//		    									LogSystem.info(request, "id mitra = "+idmitra);
//		    									FormatPdf fd=fdao.findFormatPdf(jsonRecv.getString("format_doc"), idmitra);
//		    									if(fd!=null) {
//		    										//System.out.println("ttd ke = "+obj.getString("user").substring(3));
//		    										//System.out.println("USERRRRRRR = "+obj.getString("user").substring(0, 3));
//		    										LogSystem.info(request, "USERRRRRRR = "+obj.getString("user").substring(0, 3));
//		    										if(obj.getString("user").substring(0, 3).equalsIgnoreCase("ttd")) {
//		    											LetakTtd lt=ltdao.findLetakTtd(obj.getString("user").substring(3), fd.getId());
//		    											if(lt!=null) {
//		    												boolean sign=false;
//		    												if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
//		    													//System.out.println("masuk auto ttd.");
//		    													LogSystem.info(request, "Masuk auto ttd");
//		    													if(userEmail!=null) {
//		    														if(userEmail.isAuto_ttd()) {
//		    															sign=true;
//		    														}
//		    														else {
//		    															//System.out.println("delete data");
//		    															LogSystem.info(request, "Delete data");
//		    															dAccessDao.deleteWhere(id_doc.getId());
//		    															File file=new File(id_doc.getPath()+id_doc.getRename());
//		    															file.delete();
//		    															new DocumentsDao(db).delete(id_doc);
//		    															
//		    															jo.put("result", "07");
//		    											                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
//		    											                return jo;
//		    														}
//		    													}
//		    													else {
//		    														dAccessDao.deleteWhere(id_doc.getId());
//		    														File file=new File(id_doc.getPath()+id_doc.getRename());
//		    														file.delete();
//		    														new DocumentsDao(db).delete(id_doc);
//		    														
//		    														jo.put("result", "07");
//		    										                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan ttd otomatis");
//		    										                return jo;
//		    													}
//		    													
//		    												}
//		    												if(userEmail!=null)da.setEeuser(userEmail);
//		    												da.setDocument(id_doc);
//		    												da.setFlag(false);
//		    												da.setType("sign");
//		    												da.setDate_sign(null);
//		    												da.setEmail(obj.getString("email").toLowerCase());
//		    												da.setName(obj.getString("name"));
//		    												da.setPage(lt.getPage());
//		    												da.setLx(lt.getLx());
//		    												da.setLy(lt.getLy());
//		    												da.setRx(lt.getRx());
//		    												da.setRy(lt.getRy());
//		    												da.setDatetime(new Date());
//		    												da.setAction(obj.getString("aksi_ttd"));
//		    												if(obj.has("visible")) {
//		    													boolean vis=true;
//		    													String v=obj.getString("visible");
//		    													if(v.equalsIgnoreCase("0"))vis=false;
//		    													
//		    													System.out.println("visible = "+vis);
//		    													da.setVisible(vis);
//		    												} else {
//		    													da.setVisible(true);
//		    												}
//		    												
//		    												Long idAcc=dAccessDao.create(da);
//		    												
//		    												//System.out.println("sign = "+sign);
//		    												LogSystem.info(request, "Sign = "+sign);
//		    												if(sign==true) {
//		    													//System.out.println("id doc access = "+idAcc);
//		    													LogSystem.info(request, "id doc access = "+idAcc);
//		    													DocumentAccess dac=dAccessDao.findbyId(idAcc);
//		    													lttd.add(dac);
//		    													
//		    												}
//		    												
//		    												Userdata udata=new Userdata();
//		    												boolean reg=false;
//		    												if(userEmail!=null) {
//		    													udata=userEmail.getUserdata();
//		    													reg=true;
//		    												}
//		    												
//		    												boolean input=false;
//		    												for(EmailVO ev:emails) {
//		    													if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
//		    														input=true;
//		    														break;
//		    													}
//		    												}
//		    												
//		    												if(input==false) {
//		    				    								EmailVO evo=new EmailVO();
//		    				    								evo.setEmail(obj.getString("email"));
//		    				    								evo.setUdata(udata);
//		    				    								evo.setReg(reg);
//		    				    								evo.setNama(obj.getString("name"));
//		    				    								evo.setIddocac(idAcc);
//		    				    								emails.add(evo);
//		    												}
//		    											} else {
//		    												dAccessDao.deleteWhere(id_doc.getId());
//		    												File file=new File(id_doc.getPath()+id_doc.getRename());
//		    												file.delete();
//		    												new DocumentsDao(db).delete(id_doc);
//		    												
//		    												jo.put("result", "05");
//		    								                jo.put("notif", "Letak ttd tidak tersedia");
//		    								                return jo;
//		    											}
//		    										}
//		    										else {
//		    											//untuk paraf
//		    											LetakTtd lt=ltdao.findLetakPrf(obj.getString("user").substring(3), fd.getId());
//		    											if(lt!=null) {
//		    												boolean prf=false;
//		    												DocumentAccess daPrf=null;
//		    												if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
//		    													LogSystem.info(request, "Masuk auto prf.");
//		    													//System.out.println("masuk auto prf.");
//		    													
//		    													if(userEmail!=null) {
//		    														if(userEmail.isAuto_ttd()) {
//		    															prf=true;
//		    															//System.out.println("Paraf true");
//		    															LogSystem.info(request, "Paraf true");
//		    															daPrf=dAccessDao.findAccessByUserPrf(userEmail.getId(), id_doc.getId(), "at");
//		    															da.setAction("at");
//		    														}
//		    														else { 
//		    															dAccessDao.deleteWhere(id_doc.getId());
//		    															File file=new File(id_doc.getPath()+id_doc.getRename());
//		    															file.delete();
//		    															new DocumentsDao(db).delete(id_doc);
//		    															
//		    															jo.put("result", "07");
//		    											                jo.put("notif", "user tidak diijinkan untuk prf otomatis");
//		    											                return jo;
//		    														}
//		    													}
//		    													else {
//		    														dAccessDao.deleteWhere(id_doc.getId());
//		    														File file=new File(id_doc.getPath()+id_doc.getRename());
//		    														file.delete();
//		    														new DocumentsDao(db).delete(id_doc);
//		    														
//		    														jo.put("result", "07");
//		    										                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan prf otomatis");
//		    										                return jo;
//		    													}
//		    													
//		    												}
//		    												else {
//		    													if(userEmail!=null) {
//		    														daPrf=dAccessDao.findAccessByUserPrf(userEmail.getId(), id_doc.getId(), "mt");
//		    														
//		    													} else {
//		    														daPrf=dAccessDao.findAccessByEmailPrf(obj.getString("email"), id_doc.getId(), "mt");
//		    													}
//		    													da.setAction("mt");
//		    												}
//		    												
//		    												Long idAcc=(long) 0;
//		    												Initial ini=new Initial();
//		    												if(daPrf==null) {
//		    													if(userEmail!=null)da.setEeuser(userEmail);
//		    													da.setDocument(id_doc);
//		    													da.setFlag(false);
//		    													da.setType("initials");
//		    													da.setDate_sign(null);
//		    													da.setEmail(obj.getString("email").toLowerCase());
//		    													da.setName(obj.getString("name"));
//		    													da.setDatetime(new Date());
//		    													da.setAction(obj.getString("aksi_ttd"));
//		    													if(obj.has("visible")) {
//		    														boolean vis=true;
//		    														String v=obj.getString("visible");
//		    														if(v.equalsIgnoreCase("0"))vis=false;
//		    														
//		    														System.out.println("visible = "+vis);
//		    														da.setVisible(vis);
//		    													} else {
//		    														da.setVisible(true);
//		    													}
//		    													
//		    													idAcc=dAccessDao.create(da);
//		    													DocumentAccess dac=dAccessDao.findbyId(idAcc);
//		    													if(prf==true) {
//		    														//System.out.println("id doc access paraf = "+idAcc);
//		    														LogSystem.info(request, "id doc access paraf = "+idAcc);
//		    														lttd.add(dac);
//		    													}
//		    													
//		    													ini.setDoc_access(dac);
//		    													
//		    												}
//		    												else {
//		    													idAcc=daPrf.getId();
//		    													ini.setDoc_access(daPrf);
//		    												}
//		    												
//		    												LogSystem.info(request, "Paraf = " + prf);
//		    												//System.out.println("prf = "+prf);
//		    												
//		    												ini.setLx(lt.getLx());
//		    												ini.setLy(lt.getLy());
//		    												ini.setRx(lt.getRx());
//		    												ini.setRy(lt.getRy());
//		    												ini.setPage(lt.getPage());
//		    												new InitialDao(db).create(ini);
//		    												
//		    												Userdata udata=new Userdata();
//		    												boolean reg=false;
//		    												if(userEmail!=null) {
//		    													udata=userEmail.getUserdata();
//		    													reg=true;
//		    												}
//		    												
//		    												boolean input=false;
//		    												for(EmailVO ev:emails) {
//		    													if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
//		    														input=true;
//		    														break;
//		    													}
//		    												}
//		    												
//		    												if(input==false) {
//		
//		    				    								EmailVO evo=new EmailVO();
//		    				    								evo.setEmail(obj.getString("email"));
//		    				    								evo.setUdata(udata);
//		    				    								evo.setReg(reg);
//		    				    								evo.setNama(obj.getString("name"));
//		    				    								evo.setIddocac(idAcc);
//		    				    								emails.add(evo);
//		    												}
//		
//		    											} else {
//		    												dAccessDao.deleteWhere(id_doc.getId());
//		    												File file=new File(id_doc.getPath()+id_doc.getRename());
//		    												file.delete();
//		    												new DocumentsDao(db).delete(id_doc);
//		    												
//		    												jo.put("result", "05");
//		    								                jo.put("notif", "Letak prf tidak tersedia");
//		    								                return jo;
//		    											}
//		    										}
//		    										
//		    									}
//		    									else {
//		    										dAccessDao.deleteWhere(id_doc.getId());
//		    										File file=new File(id_doc.getPath()+id_doc.getRename());
//		    										file.delete();
//		    										new DocumentsDao(db).delete(id_doc);
//		    										
//		    										jo.put("result", "05");
//		    						                jo.put("notif", "Format doc tidak tersedia.");
//		    						                return jo;
//		    									}
//		    								} else {
//		    									if(!obj.has("user"))obj.put("user", "ttd");
//		    									String actUser="ttd";
//		    									if(obj.getString("user").length()>3)actUser=obj.getString("user").substring(0, 3);
//		    									else actUser=obj.getString("user").substring(0);
//		    									
//		    									if(actUser.equalsIgnoreCase("ttd")||actUser.equalsIgnoreCase("")) {
//		    										boolean sign=false;
//		    										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
//		    											if(userEmail!=null) {
//		    												if(userEmail.isAuto_ttd()) {
//		    													sign=true;
//		    												}
//		    												else { 
//		    													dAccessDao.deleteWhere(id_doc.getId());
//		    													File file=new File(id_doc.getPath()+id_doc.getRename());
//		    													file.delete();
//		    													new DocumentsDao(db).delete(id_doc);
//		    													
//		    													jo.put("result", "07");
//		    									                jo.put("notif", "user tidak diijinkan untuk ttd otomatis");
//		    									                return jo;
//		    												}
//		    											}
//		    											else {
//		    												dAccessDao.deleteWhere(id_doc.getId());
//		    												File file=new File(id_doc.getPath()+id_doc.getRename());
//		    												file.delete();
//		    												new DocumentsDao(db).delete(id_doc);
//		    												
//		    												jo.put("result", "07");
//		    								                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan ttd otomatis");
//		    								                return jo;
//		    											}
//		    											
//		    										}
//		    										
//		    										if(userEmail!=null)da.setEeuser(userEmail);
//		    										da.setDocument(id_doc);
//		    										da.setFlag(false);
//		    										da.setType("sign");
//		    										da.setDate_sign(null);
//		    										da.setEmail(obj.getString("email").toLowerCase());
//		    										da.setName(obj.getString("name"));
//		    										da.setPage(Integer.parseInt(obj.getString("page")));
//		    										da.setLx(obj.getString("llx"));
//		    										da.setLy(obj.getString("lly"));
//		    										da.setRx(obj.getString("urx"));
//		    										da.setRy(obj.getString("ury"));
//		    										da.setDatetime(new Date());
//		    										if(obj.has("visible")) {
//		    											boolean vis=true;
//		    											String v=obj.getString("visible");
//		    											if(v.equalsIgnoreCase("0"))vis=false;
//		    											
//		    											System.out.println("visible = "+vis);
//		    											da.setVisible(vis);
//		    										} else {
//		    											da.setVisible(true);
//		    										}
//		    										Long idAcc=dAccessDao.create(da);
//		    										
//		    										if(sign==true) {
//		    											DocumentAccess dac=dAccessDao.findbyId(idAcc);
//		    											lttd.add(dac);
//		    											
//		    										}
//		    										
//		    										Userdata udata=new Userdata();
//		    										boolean reg=false;
//		    										if(userEmail!=null) {
//		    											udata=userEmail.getUserdata();
//		    											reg=true;
//		    										}
//		    										
//		    										boolean input=false;
//		    										for(EmailVO ev:emails) {
//		    											if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
//		    												input=true;
//		    												break;
//		    											}
//		    										}
//		    										
//		    										if(input==false) {
//		
//		    		    								EmailVO evo=new EmailVO();
//		    		    								evo.setEmail(obj.getString("email"));
//		    		    								evo.setUdata(udata);
//		    		    								evo.setReg(reg);
//		    		    								evo.setNama(obj.getString("name"));
//		    		    								evo.setIddocac(idAcc);
//		    		    								emails.add(evo);
//		    										}
//		
//		    									}
//		    									else {
//		    										
//		    										boolean prf=false;
//		    										DocumentAccess daPrf=null;
//		    										if(obj.getString("aksi_ttd").equalsIgnoreCase("at")) {
//		    											LogSystem.info(request, "Masuk auto paraf");
//		    											//System.out.println("masuk auto prf.");
//		    											
//		    											if(userEmail!=null) {
//		    												if(userEmail.isAuto_ttd()) {
//		    													prf=true;
//		    													daPrf=dAccessDao.findAccessByUserPrf(userEmail.getId(), id_doc.getId(), "at");
//		    													da.setAction("at");
//		    												}
//		    												else { 
//		    													dAccessDao.deleteWhere(id_doc.getId());
//		    													File file=new File(id_doc.getPath()+id_doc.getRename());
//		    													file.delete();
//		    													new DocumentsDao(db).delete(id_doc);
//		    													
//		    													jo.put("result", "07");
//		    									                jo.put("notif", "user tidak diijinkan untuk prf otomatis");
//		    									                return jo;
//		    												}
//		    											}
//		    											else {
//		    												dAccessDao.deleteWhere(id_doc.getId());
//		    												File file=new File(id_doc.getPath()+id_doc.getRename());
//		    												file.delete();
//		    												new DocumentsDao(db).delete(id_doc);
//		    												
//		    												jo.put("result", "07");
//		    								                jo.put("notif", "user belum terdaftar, tidak bisa menggunakan prf otomatis");
//		    								                return jo;
//		    											}
//		    											
//		    										}
//		    										else {
//		    											//daPrf=dAccessDao.findAccessByUserPrf(user.getId(), id_doc.getId(), "mt");
//		    											if(userEmail!=null) {
//		    												daPrf=dAccessDao.findAccessByUserPrf(userEmail.getId(), id_doc.getId(), "mt");
//		    												
//		    											} else {
//		    												daPrf=dAccessDao.findAccessByEmailPrf(obj.getString("email"), id_doc.getId(), "mt");
//		    											}
//		    											da.setAction("mt");
//		    										}
//		    										
//		    										Long idAcc=(long) 0;
//		    										Initial ini=new Initial();
//		    										if(daPrf==null) {
//		    											if(userEmail!=null)da.setEeuser(userEmail);
//		    											da.setDocument(id_doc);
//		    											da.setFlag(false);
//		    											da.setType("initials");
//		    											da.setDate_sign(null);
//		    											da.setEmail(obj.getString("email").toLowerCase());
//		    											da.setName(obj.getString("name"));
//		    											da.setDatetime(new Date());
//		    											da.setAction(obj.getString("aksi_ttd"));
//		    											if(obj.has("visible")) {
//		    												boolean vis=true;
//		    												String v=obj.getString("visible");
//		    												if(v.equalsIgnoreCase("0"))vis=false;
//		    												
//		    												LogSystem.info(request, "Visible = "+vis);
//		    												//System.out.println("visible = "+vis);
//		    												da.setVisible(vis);
//		    											} else {
//		    												da.setVisible(true);
//		    											}
//		    											
//		    											idAcc=dAccessDao.create(da);
//		    											DocumentAccess dac=dAccessDao.findbyId(idAcc);
//		    											if(prf==true) {
//		    												LogSystem.info(request, "ID doc access = "+idAcc);
//		    												//System.out.println("id doc access = "+idAcc);
//		    												lttd.add(dac);
//		    											}
//		    											
//		    											ini.setDoc_access(dac);
//		    											
//		    										}
//		    										else {
//		    											idAcc=daPrf.getId();
//		    											ini.setDoc_access(daPrf);
//		    										}
//		    										
//		    										LogSystem.info(request, "Paraf = "+prf);
//		    										//System.out.println("prf = "+prf);
//		    										
//		    										ini.setLx(obj.getString("llx"));
//		    										ini.setLy(obj.getString("lly"));
//		    										ini.setRx(obj.getString("urx"));
//		    										ini.setRy(obj.getString("ury"));
//		    										ini.setPage(Integer.valueOf(obj.getString("page")));
//		    										new InitialDao(db).create(ini);
//		    										
//		    										Userdata udata=new Userdata();
//		    										boolean reg=false;
//		    										if(userEmail!=null) {
//		    											udata=userEmail.getUserdata();
//		    											reg=true;
//		    										}
//		    										
//		    										boolean input=false;
//		    										for(EmailVO ev:emails) {
//		    											if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
//		    												input=true;
//		    												break;
//		    											}
//		    										}
//		    										
//		    										if(input==false) {
//		    											//MailSender mail=new MailSender(obj.getString("email").toLowerCase(),udata,reg,obj.getString("name"));
//		    		    								//mail.run();
//		    		    								EmailVO evo=new EmailVO();
//		    		    								evo.setEmail(obj.getString("email"));
//		    		    								evo.setUdata(udata);
//		    		    								evo.setReg(reg);
//		    		    								evo.setNama(obj.getString("name"));
//		    		    								evo.setIddocac(idAcc);
//		    		    								emails.add(evo);
//		    										}
//		
//		    									}
//		    									
//		    								}
//		
//		    								kirim=false;
//		
//		    							}
//		    						}
//		    						
//    								
////		    						if(jsonRecv.get("send-to")!=null) {
////	    							
////	    							JSONArray sendList=(JSONArray) jsonRecv.getJSONArray("send-to");
//    								if(jsonRecv.getJSONArray("ket_file").getJSONObject(fileSize).get("send-to")!=null) {
//									
//   		    							JSONArray sendList=ketFileList.getJSONObject(fileSize).getJSONArray("send-to");
//
//		    							for(int i=0; i<sendList.length(); i++) {
//		    								JSONObject obj=(JSONObject) sendList.get(i);
//		
//		    								if(sendList.getString(i).equals(usr.getNick())) continue;
//		    								boolean sign=false;
//		    								for(EmailVO ev:emails) {
//		    									if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
//		    										sign=true;
//		    										break;
//		    									}
//		    								}
//		    								if(sign)continue;
//		    								
//		    								DocumentAccess da=new DocumentAccess();
//		    								User userEmail= new UserManager(db).findByUsername(obj.getString("email"));
//		
//		    								/*change to eeuser*/
//		
//		    								if(userEmail!=null)da.setEeuser(userEmail);
//		    								da.setDocument(id_doc);
//		    								da.setFlag(false);
//		    								da.setType("share");
//		    								da.setDate_sign(null);
//		    								da.setEmail(obj.getString("email").toLowerCase());
//		    								da.setName(obj.getString("name"));
//		    								da.setDatetime(new Date());
//		    								Long idAcc=dAccessDao.create(da);
//		    								
//		    								kirim=true;
//		    								
//		    								Userdata udata=new Userdata();
//		    								boolean reg=false;
//		    								if(userEmail!=null) {
//		    									udata=userEmail.getUserdata();
//		    									reg=true;
//		    								}
//		    								
//		    								boolean input=false;
//		    								for(EmailVO ev:emails) {
//		    									if(ev.getEmail().equalsIgnoreCase(obj.getString("email").toLowerCase())) {
//		    										input=true;
//		    										break;
//		    									}
//		    								}
//		    								
//		    								if(input==false) {
//		        								EmailVO evo=new EmailVO();
//		        								evo.setEmail(obj.getString("email"));
//		        								evo.setUdata(udata);
//		        								evo.setReg(reg);
//		        								evo.setNama(obj.getString("name"));
//		        								evo.setIddocac(idAcc);
//		        								emails.add(evo);
//		    								}
//		
//		    							}
//		    							
//		    						}
//    							}
//    							
//
//							  if(kirim) { // jika hanya share saja maka rubah status menjadi sudah dikirim
//									id_doc.setStatus('T');
//								}
//								
//							  if(jsonRecv.has("payment")) {
//								  	char pay = jsonRecv.getString("payment").charAt(0);
//									id_doc.setPayment(pay);
//									docDao.update(id_doc);  
//							  }
//							  
//							  int x=0;
//							  System.out.println("List Auto TTD = "+lttd.size());
//							  Vector<String> invs=new Vector();
//							  for(DocumentAccess docac:lttd) {
//								  LogSystem.info(request, "Masuk untuk auto TTD dan payment type = "+id_doc.getPayment());
//								  LogSystem.info(request, "ID dokumen akses = "+docac.getId());
//								  //System.out.println("masuk untuk auto TTD dan payment type = "+id_doc.getPayment());
//								  //System.out.println("id dokumen akses = "+docac.getId());
//								  boolean potong=false;
//								  String inv=null;
//								  User u=docac.getEeuser();
//								  int jmlttd=lttd.size();
//								  KillBillPersonal kp=new KillBillPersonal();
//								  KillBillDocument kd=new KillBillDocument();
//								  InvoiceDao idao=new InvoiceDao(db);
//								  if(id_doc.getPayment()=='2') {
//									  
//									  int balance=kp.getBalance("MT"+usr.getMitra().getId());
//									  System.out.println("balance 2= "+balance);
//									  if(balance<jmlttd) {
//										  dAccessDao.deleteWhere(id_doc.getId());
//											File file=new File(id_doc.getPath()+id_doc.getRename());
//											file.delete();
//											new DocumentsDao(db).delete(id_doc);
//											
//										  jo.put("result", "61");
//										  jo.put("notif", "Balance mitra tidak cukup.");
//										  return jo;
//									  }
//									  
//									  inv=kp.setTransaction("MT"+usr.getMitra().getId(), 1);
//									  String[] split=inv.split(" ");
//									  invs.add(split[1]);
//									  
//									  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
//									  ivc.setDatetime(new Date());
//									  ivc.setAmount(1);
//									  ivc.setEeuser(id_doc.getEeuser());
//									  ivc.setExternal_key("MT"+usr.getMitra().getId());
//									  ivc.setTenant('1');
//									  ivc.setTrx('2');
//									  ivc.setKb_invoice(split[1]);
//									  ivc.setDocument(id_doc);
//									  idao.create(ivc);
//									  
//									  
//									  jmlttd--;
//									  potong=true;
//								  }
//								  else if(id_doc.getPayment()=='3'){
//									  
//									  int balance=kd.getBalance("MT"+usr.getMitra().getId());
//									  LogSystem.info(request, "Balance dokumen = "+balance);
//									  //System.out.println("balance dokumen = "+balance);
//									  if(balance<1) {
//										  dAccessDao.deleteWhere(id_doc.getId());
//											File file=new File(id_doc.getPath()+id_doc.getRename());
//											file.delete();
//											new DocumentsDao(db).delete(id_doc);
//											
//										  jo.put("result", "61");
//										  jo.put("notif", "Balance mitra tidak cukup.");
//										  return jo;
//									  }
//									  
//									  //List<DocumentAccess> ld=dAccessDao.findByDocSign(id_doc.getId());
//									  List<id.co.keriss.consolidate.ee.Invoice> li=idao.findByDoc(id_doc.getId());
//									  //if(ld.size()==0) {
//									  if(li.size()==0) {
//										  inv=kd.setTransaction("MT"+usr.getMitra().getId(), 1);
//										  String[] split=inv.split(" ");
//										  
//										  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
//										  ivc.setDatetime(new Date());
//										  ivc.setAmount(1);
//										  ivc.setEeuser(id_doc.getEeuser());
//										  ivc.setExternal_key("MT"+usr.getMitra().getId());
//										  ivc.setTenant('2');
//										  ivc.setTrx('2');
//										  ivc.setKb_invoice(split[1]);
//										  ivc.setDocument(id_doc);
//										  idao.create(ivc);
//										  
//										  inv=split[1];
//									  }
//									  /*
//									  else {
//										  //inv=ld.get(0).getInvoice();
//										  inv=li.get(0).getKb_invoice();
//									  }
//									  */
//									  potong=true;
//								  }
//								  
//								  if(potong==true) {
//									  SignDoc sd=new SignDoc();
//									  
//									  try {
//										  if(sd.signDoc(u, docac, inv, db, "", request)) {
//											  //docac.setFlag(true);
//											  //docac.setDate_sign(new Date());
//											  //docac.setInvoice(inv);
//											  //dAccessDao.update(docac);
//											  LogSystem.info(request, "Tandatangan dokumen otomatis berhasil");
//											  //System.out.println("Tandatangan dokumen otomatis berhasil");
//											  							  
//										  }
//										  else {
//											  //System.out.println("REVERSAL untuk payment= "+id_doc.getPayment());
//											  LogSystem.info(request, "REVERSAL untuk payment= "+id_doc.getPayment());
//											  if(id_doc.getPayment()=='2') {
//													for(String in:invs) {
//														String resp=kp.reverseTransaction(in);
//														LogSystem.info(request, "Hasil reversal = "+resp);
//														//System.out.println("hasil reversal = "+resp);
//														idao.deleteWhere(in);
//													}
//												}
//											  else if(id_doc.getPayment()=='3') {
//													//if(x==0 && inv!=null)kd.reverseTransaction(inv);
//												  	LogSystem.info(request, "invoicenya adalah = "+inv);
//												  	//System.out.println("invoicenya adalah = "+inv);
//												  	if(inv!=null || !inv.equals("")) {
//												  		LogSystem.info(request, "MASUK PAK EKO");
//												  		//System.out.println("MASUK PAK EKO");
//												  		String resp=kd.reverseTransaction(inv);
//												  		LogSystem.info(request, "Hasil reversal = "+resp);
//												  		//System.out.println("hasil reversal = "+resp);
//												  		idao.deleteWhere(inv);
//												  	}
//												}
//												
//												dAccessDao.deleteWhere(id_doc.getId());
//												File file=new File(id_doc.getPath()+id_doc.getRename());
//												file.delete();
//												new DocumentsDao(db).delete(id_doc);
//												
//												jo.put("result", "06");
//												//jo.put("notif", "Upload file berhasil. Autottd berhasil "+x+" dari "+lttd.size());
//												jo.put("notif", "sign dokumen gagal");
//												return jo;
//										  }
//										  
//									} catch (Exception e) {
//										// TODO: handle exception
//										if(id_doc.getPayment()=='2') {
//											for(String in:invs) {
//												kp.reverseTransaction(in);
//												idao.deleteWhere(in);
//											}
//										}
//									  else if(id_doc.getPayment()=='3') {
//											//if(x==0 && inv!=null)kd.reverseTransaction(inv);
//										  	if(inv!=null) {
//										  		kd.reverseTransaction(inv);
//										  		idao.deleteWhere(inv);
//										  	}
//										}
//										
//										dAccessDao.deleteWhere(id_doc.getId());
//										File file=new File(id_doc.getPath()+id_doc.getRename());
//										file.delete();
//										new DocumentsDao(db).delete(id_doc);
//										
//										jo.put("result", "06");
//										//jo.put("notif", "Upload file berhasil. Autottd berhasil "+x+" dari "+lttd.size());
//										jo.put("notif", "sign dokumen gagal");
//										return jo;
//									}
//									  
//								  }
//								  x++;
//							  }
//							  res="00";
//							  for(EmailVO email:emails) {
//								  	LogSystem.info(request, "Email = "+email.getEmail());
//									//System.out.println("email = "+email.getEmail());
//									//MailSender mail=new MailSender(email.getEmail(),email.getUdata(),email.isReg(),email.getNama());
//									//mail.run();
//									
//									String docs ="";
//									String link ="";
//									try {
//										   docs = AESEncryption.encryptDoc(String.valueOf(id_doc.getId()));
//										   link = "https://"+DSAPI.DOMAIN+"/doc/pdf.html?frmProcess=getFile&doc="+id_doc.getId()+"&access="+email.getIddocac();
//										     //+ URLEncoder.encode(docs, "UTF-8");
//										} catch (Exception e1) {
//										   // TODO Auto-generated catch block
//										   e1.printStackTrace();
//										}
//									
//									SendTerimaDoc std=new SendTerimaDoc();
//
//									if(mitra.isNotifikasi()) {
//										if(email.isReg())std.kirim(email.getUdata().getNama(), String.valueOf(email.getUdata().getJk()), email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), "");
//										else std.kirim(email.getNama(), "", email.getEmail(), usr.getName(), String.valueOf(usr.getUserdata().getJk()), link, String.valueOf(mitra.getId()), "");
//									}
//								}
//							  
//							  jo.put("notif", "Registrasi dan Kirim dokumen berhasil.");
//							  userRecv=usr;
//							  id_doc.setStatus('T');
//							  docDao.update(id_doc);
//    						}
//    						else
//    						{
//    							LogSystem.info(request, "Kirim dokumen");
//    							//System.out.println("Kirim Dokumen");
//    							res="28";
//	    				        notif="Data upload tidak lengkap";
//	    				        jo.put("result", res);
//	    				        jo.put("notif", notif);
//	    				        return jo;
//    						}
//    							
//    				  }
//    			               
//    				}
//    			
//    				catch (Exception e) {
//    					// TODO: handle exception
//    					e.printStackTrace();
//    					udataDao.delete(userdata);
//    					LogSystem.error(getClass(), e);
//
//    				    res="06";
//    			        notif="Data gagal diproses";
//    				}
//    			}
////    		}
//        }
//        else {
//        	//jo=aVerf.setResponFailed(jo);
//        	notif="UserId atau Password salah";
//        }
//		
//        jo.put("result", res);
//        jo.put("notif", notif);
//		
//	   return jo;
//	}
//	
//	class MailSender{
//
//		String email;
//		PreRegistration name;
//		String id;
//		
//		public MailSender(PreRegistration name,String email, String id) {
//			this.email=email;
//			this.name=name;
//			this.id=id;
//		}
//		public void run() {
//			new SendMailSSL().sendMailPreregisterMitra(name, email, id);
//
//		}
//		
//	}
//	
//}
