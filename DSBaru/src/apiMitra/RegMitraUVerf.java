package apiMitra;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
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
import org.mortbay.util.ajax.JSON;

import com.anthonyeden.lib.config.Configuration;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import api.dukcapil.checkingData;
import api.email.SendActivasi;
import api.email.SendRegistrasiGagal;
import api.verifikasi.CheckPhotoToAsliRI;
import api.verifikasi.CheckToDukcapil;
import api.verifikasi.VerifikasiDoubleLoginForLevelMitra2;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.CallbackPendingDao;
import id.co.keriss.consolidate.dao.InterfaceMitraDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.RegLogDao;
import id.co.keriss.consolidate.dao.RekeningDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.VerificationDataDao;
import id.co.keriss.consolidate.dao.VerifikasiManualDao;
import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.ee.InterfaceMitra;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.RegistrationLog;
import id.co.keriss.consolidate.ee.Rekening;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.ee.VerifikasiManual;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;


public class RegMitraUVerf extends ActionSupport {

	//private static String basepath="/opt/data-DS/UploadFile/";
	private static String basepathPreReg="/file2/data-DS/PreReg/";
	private static String basepathRegLog="/file2/data-DS/LogRegistrasi/";
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx="";
	String kelas="apiMitra.RegMitraUVerf";
	String trxType="REGUNVER";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="REGUNVER"+sdfDate2.format(tgl).toString();
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
	        	 List<RegistrationLog> lregLog=new RegLogDao(db).findByMitraNikEmail(mitra.getId(), jsonRecv.getString("idktp"), jsonRecv.getString("email"));
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
	         }
	         
	         
	        //PROSES REGISTRASI// 
	        jo=RegisterUserMitra(refTrx, jsonRecv, fileSave, context, mitra, useradmin, request);
	        
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
        	
        	/*
        	if(!jo.getString("result").equals("00")) {
	        	if(jo.has("data")) {
	        		JSONObject object=jo.getJSONObject("data");
	        		
	        		VerifikasiManual vman=vdao.cekNik(jsonRecv.getString("idktp"));
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
		        			}
		        		} else {
		        			VerifikasiManual vm=new VerifikasiManual();
	        				vm.setEmail(jsonRecv.getString("email"));
	        				vm.setNik(jsonRecv.getString("idktp"));
	        				vm.setId_registration_log(RL);
	        				vm.setCreate_date(tgl);
	        				vdao.create(vm);
		        		}
	        		}
	        		
	        	}
	        }
        	*/
        	
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
	
	JSONObject RegisterUserMitra(String refTrx, JSONObject jsonRecv,List<FileItem> fileSave, JPublishContext context, Mitra mitratoken, User useradmin, HttpServletRequest  request) throws JSONException{
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
        	
        	PreRegistration userdata= null;
    		
    		PreRegistrationDao udataDao=new PreRegistrationDao(db);
    		//userdata = udataDao.findByEmail(jsonRecv.getString("email").toLowerCase());
    		userdata = udataDao.findByEmailNik(jsonRecv.getString("email"), jsonRecv.getString("idktp"));
    		if(userdata!=null) {
    			
    			
    			if(userdata.getMitra()==mitra) {
    				jo.put("result", "00");
    			} else {
    				Date tgl_lahir = null;
					try {
						tgl_lahir = new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				if(jsonRecv.getString("nama").equalsIgnoreCase(userdata.getNama()) && tgl_lahir.equals(userdata.getTgl_lahir())) {
    					LogSystem.info(request, "data sesuai dengan preregister", kelas, refTrx, trxType);
    					
    				} else {
						
						jo.put("result", "12");
	    				jo.put("notif", "verifikasi user gagal.");
	    				jo.put("info", "nama atau tanggal lahir tidak sesuai");
	    				return jo;
    				}
    				jo.put("result", "14");
    			}
				jo.put("notif", "Email sudah terdaftar, namun belum melakukan aktivasi. Silahkan untuk melakukan aktivasi sebelum data dihapus dari daftar aktivasi.");
				if(userdata.getKode_user()!=null) {
					jo.put("kode_user", userdata.getKode_user());
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
    				return jo;
        		} else {
        			pr=udataDao.findEmail(jsonRecv.getString("email"));
        			if(pr != null) {
        				jo.put("result", "14");
        				jo.put("notif", "email sudah terdaftar dengan NIK lain, silahkan gunakan email lain.");
        				//jo.put("email_registered", pr.getEmail());
        				return jo;
        			}
        		}
    			
    			userdata=new PreRegistration();
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
	    	  	
	    	  	
		        
    			user= new UserManager(db).findByUserNikNonStatus(jsonRecv.getString("email").toLowerCase(), jsonRecv.getString("idktp"));
    			boolean devel=DSAPI.DEVELOPMENT;
    			
    			if(user!=null) 
    			{ 
    				int leveluser=Integer.parseInt(user.getUserdata().getLevel().substring(1));
    				if((levelmitra>2 && leveluser<3) || user.getStatus()=='0') {
    					//LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA");
    					LogSystem.info(request, "user sudah terdaftar dengan kondisi level USER < level MITRA", kelas, refTrx,trxType);
    					
    					//Check Ke DUKCAPIL
    					boolean verifikasitext=false;
    					String jk="P";
    					if(jsonRecv.getString("jenis_kelamin").equalsIgnoreCase("laki-laki")) {
    						jk="L";
    					}
    					if(devel==false) {
	    					CheckToDukcapil dukcapil=new CheckToDukcapil(refTrx, trxType, request);
							JSONObject job=dukcapil.check(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
							if(job!=null) {
								LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
								if(job.has("content")) {
									org.codehaus.jettison.json.JSONArray jarray=job.getJSONArray("content");
									JSONObject jobj=jarray.getJSONObject(0);
									if(jobj.has("RESPONSE_CODE")){
										if(jobj.getString("RESPONSE_CODE").equals("00")) {
											verifikasitext=true;
											LogSystem.info(request, "verifikasi dukcapil sukses", kelas, refTrx, trxType);
										} else {
											jo.put("result", "12");
						    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
						    				
						    				new SendRegistrasiGagal().kirim(jsonRecv.getString("nama"), jk, jsonRecv.getString("email"), "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai", String.valueOf(mitra.getId()));
						    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
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
					    				new SendRegistrasiGagal().kirim(jsonRecv.getString("nama"), jk, jsonRecv.getString("email"), "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai", String.valueOf(mitra.getId()));
					    				
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
    					
    					if(verifikasitext==true) {
    						//verifikasi wajah dari data yg sudah ada
    						boolean verifikasiwajah=false;
    						LogSystem.info(request, "kirim ke AsliRI",kelas, refTrx,trxType);
    						SaveFileWithSamba samba=new SaveFileWithSamba();
  						  	//boolean respSaveData=false;
    						byte[] wajah=samba.openfile(user.getUserdata().getImageWajah());
							CheckPhotoToAsliRI checkPhoto=new CheckPhotoToAsliRI();
							JSONObject jcheck= checkPhoto.check(jsonRecv.getString("idktp"), new String(Base64.encode(wajah),StandardCharsets.US_ASCII), mitra.getName(), refTrx, request);
							
							if(jcheck.getString("result").equals("23"))
							{
								jo.put("result", "12");
								jo.put("notif", "verifikasi user gagal.");
								jo.put("info", "Verifikasi wajah gagal. Size foto selfie maksimal 500KB");
			    				return jo;
							}
							
							if(jcheck.getString("result").equals("22"))
							{
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
										jcheck = checkPhoto.check(jsonRecv.getString("idktp"), new String(Base64.encode(wajah),StandardCharsets.US_ASCII), mitra.getName(), refTrx, request);
									}
									else
									{
										i = 3;
									}
								}
							}
							
								
							if(jcheck.get("result").equals("00") || jcheck.get("result").equals("true")) {
								LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
								
								if(jcheck.has("dataFace")) {
									try {
										if(jcheck.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo")>65) {
											LogSystem.info(request, "Selfie photo > 65 = sukses", kelas, refTrx, trxType);
											verifikasiwajah=true;
										} else {
											verifikasiwajah=false;
										}
									} catch (Exception e) {
										// TODO: handle exception
										verifikasiwajah=false;
									}
									
								} else {
									verifikasiwajah=false;
								}
								
							} else {
								LogSystem.error(request, jcheck.toString(), kelas, refTrx, trxType);
								if(jcheck.has("connection")) {
									if(jcheck.getBoolean("connection")==false) {
										
					    				LogSystem.info(request, "masalah koneksi verifikasi wajah", kelas, refTrx, trxType);
					    				verifikasiwajah=false;
									}
								}
								
								
								verifikasiwajah=false;
							}
							
							//jika verifikasi wajah gagal
							if(verifikasiwajah==false) {
								jo.put("result", "10");
			    				jo.put("notif", "Proses registrasi gagal, user harap check email dan menghubungi cs digisign.");
			    				new SendRegistrasiGagal().kirim(jsonRecv.getString("nama"), jk, jsonRecv.getString("email"), "verifikasi wajah gagal.", String.valueOf(mitra.getId()));
			    				
			    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
			    				return jo;
							}
    					} else {
    						jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
		    				new SendRegistrasiGagal().kirim(jsonRecv.getString("nama"), jk, jsonRecv.getString("email"), "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai", String.valueOf(mitra.getId()));
		    				
		    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
		    				return jo;
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
    		        user= new UserManager(db).findByUsername2(jsonRecv.get("email").toString().toLowerCase());  
    		        
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
    					
    					FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
    					
    					//semua mitra check user by dukcapil
    					LogSystem.info(request, "kirim ke DUKCAPIL = "+devel,kelas, refTrx,trxType);
    					if(devel==false) {
    						CheckToDukcapil Checkdukcapil=new CheckToDukcapil(refTrx, trxType, request);
    						JSONObject ResDukcapil=Checkdukcapil.check(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
    						if(ResDukcapil!=null) {
    							LogSystem.info(request, ResDukcapil.toString(), kelas, refTrx, trxType);
    							if(ResDukcapil.has("content")) {
	    							org.codehaus.jettison.json.JSONArray jarray=ResDukcapil.getJSONArray("content");
	    							JSONObject jobj=jarray.getJSONObject(0);
	    							if(jobj.has("RESPONSE_CODE")){
	    								if(jobj.getString("RESPONSE_CODE").equals("00")) {
	    									System.out.println("sukses");
	    									LogSystem.info(request, "sukses verifikasi ke dukcapil", kelas, refTrx, trxType);
	    								} else {
	    									jo.put("result", "12");
	    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
	    				    				
	    				    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
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
    			        userdata.setType('3');
    			        
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
    			        
    			        Long idUserData=udataDao.create(userdata);
    			        LogSystem.info(request, "IDUserData : " + idUserData,kelas, refTrx,trxType);
    			        
    			        
    			        if(idUserData!=null) {
	    			        userdata.setId(idUserData.longValue());
	    			        jo.put("kode_user", kodeuser);
							res="00";
							notif="Berhasil, silahkan check email untuk aktivasi akun anda.";
							Long tgl = new Date().getTime()+(30L * 24 * 60 * 60 * 1000);
							Date exp= new Date(tgl);
							SimpleDateFormat sdfDate2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							jo.put("expired_aktifasi", sdfDate2.format(exp)+" WIB");
							
							LogSystem.info(request, "Userdata ID : "+ userdata.getId(),kelas, refTrx,trxType);
			                
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
    			        	
    				}
    			
    				catch (Exception e) {
    					// TODO: handle exception
    					udataDao.delete(userdata);
    					LogSystem.error(getClass(), e,kelas, refTrx, trxType);
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
	
}
