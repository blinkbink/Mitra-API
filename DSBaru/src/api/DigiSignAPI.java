package api;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.SmartphoneVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.BillingSystem;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.TrxDSDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.UserdataDao2;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.StatusKey;
import id.co.keriss.consolidate.ee.TrxDs;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.Userdata2;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.Encryption;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;

public class DigiSignAPI extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log= LogManager.getLogger("digisignlogger");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		System.out.println("DATA DEBUG :"+(i++));
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				// no multipart form
				if (!isMultipart) {

				}
				// multipart form
				else {
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
							// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

						}
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         System.out.println("PATH :"+request.getRequestURI());
//	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
//			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
//	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
	         LogSystem.request(request, fileItems);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         JSONObject jo = null;
	         if(process.equals("DS01.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=register(jsonRecv,fileSave,context);
	         }else if(process.equals("PRE01.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=preRegSmartphone(jsonRecv,context);
	         }else if(process.equals("PRE02.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=processPreRegSmartphone(jsonRecv, fileSave,context);
	         }else if(process.equals("REG-MODALKU.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=preRegisterMitra(jsonRecv, fileSave, "Modalku",context);
	         }else if(process.equals("DS02.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=checkPubKey(jsonRecv);
	         }else if(process.equals("TRX.html") && jsonRecv.get("trx").equals("01")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=checkSign(jsonRecv,context);
	         }else if(process.equals("TRX.html") && jsonRecv.get("trx").equals("02")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=setSign(jsonRecv,context);
	         }else if(process.equals("TRX01.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=checkFile(jsonRecv,dataFile,context);
	         }else if(process.equals("LGN.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=login(jsonRecv,dataFile,context);
	         }else if(process.equals("LGNRA.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=loginRA(jsonRecv);
	         }
	         else if(process.equals("verdt.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=verifikasi(jsonRecv,context);
	         }
	         else if(process.equals("veract.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=process(jsonRecv,context);
	         }
	         else if(process.equals("ACT01.html")){
		         System.out.println("RECEIVE :"+jsonString);
	        	 jo=activation(jsonRecv,context);
	         }
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         System.out.println("SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo);

			//
//			String method = null;
//			method = request.getParameter("frmProcess");
//			
//			String jsonResult = null;
//			if(method.equals("depAjx") && user!=null){
//				DepositProcessForm dp=new DepositProcessForm();
//				String amt=request.getParameter("amt");
//				String mid=request.getParameter("mid");
//				String act=request.getParameter("act");
//				
//				jsonResult=dp.topupProcessing(amt,mid,act,user.getId());
//			}
//			
//			if(method.equals("saveProduct") && user!=null && user.hasPermission("admin")){
//				ProductProcessForm p=new ProductProcessForm(request,db);
//				try{
//					jsonResult=p.processSaveProduct();
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			if(method.equals("deleteProduct") && user!=null && user.hasPermission("admin")){
//				ProductProcessForm p=new ProductProcessForm(request,db);
//				try{
//					jsonResult=p.deleteProductById();
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			
//			if(method.equals("getProduct") && user!=null && user.hasPermission("admin")){
//				ProductProcessForm p=new ProductProcessForm(request,db);
//				jsonResult=p.getProductbyCode();
//			}
//			
//			if(method.equals("mercAjx") && user!=null && user.hasPermission("admin")){
//				String id=request.getParameter("id");
//				String tid=request.getParameter("tid");
//				String sts=request.getParameter("sts");
//				boolean stsBoolean=false;
//				if(sts.equals("ON")) stsBoolean=true;
//				MerchantProcess mp=new MerchantProcess(db);
//				try{
//					jsonResult=mp.changeStatus(id,tid,stsBoolean);
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			
//			if(method.equals("mercDelAjx") && user!=null && user.hasPermission("admin")){
//				String id=request.getParameter("id");
//				
//				MerchantProcess mp=new MerchantProcess(db);
//				try{
//					jsonResult=mp.deleteMerchant(id);
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			
//			if(method.equals("getDetAjx") && user!=null){
//				String id=request.getParameter("id");
//				TransactionProcessForm tpf =new TransactionProcessForm(db);
//				jsonResult=tpf.getDetailTrx(id, user);
//			}
//			
//			if(method.equals("chgStatTransAjx") && user!=null && user.hasPermission("admin")){
//				String id=request.getParameter("id");
//				String sts=request.getParameter("sts");
//				String info=request.getParameter("info");
//				String price=request.getParameter("price");
//				String sn=request.getParameter("sn");
//
//				TransactionProcessForm tpf =new TransactionProcessForm(db);
//				StatusTrxLog newStatus=new StatusTrxLog();
//				newStatus.setId(new Long(id));
//				newStatus.setInformation(sts);
//				newStatus.setOldprice(new BigDecimal(price));
//				newStatus.setOldsn(sn);
//				newStatus.setOldstatus(sts);
//				tpf.setNewStat(newStatus);
//				jsonResult=tpf.changeStatTrx(id, sts, user);
//			}
//			
//			if(method.equals("genKey") && user!=null){
//				String pass=request.getParameter("password");
//				String id=userTrx.getUserdata().getNama()+" <"+userTrx.getName()+">";
//				DigiSign ds=new DigiSign();
//				
//				JSONObject jo =new JSONObject();
//				if(!ds.generateKey(id, pass)){
//					jo.put("status", "Tidak dapat diproses");
//					
//				}else{
//					
//					// Save ke database
//					Date crDate=new Date();
//					Key keyPrivData=new Key();
//					keyPrivData.setJenis_key(new JenisKey("PV"));
//					keyPrivData.setKey(Base64.toBase64String(ds.getSecKey().getEncoded()));
//					keyPrivData.setKey_id(String.valueOf(ds.getSecKey().getKeyID()));
//					keyPrivData.setStatus(new StatusKey("ACT"));
//					keyPrivData.setUserdata(userTrx.getUserdata());
//					keyPrivData.setUser_id((String) ds.getSecKey().getUserIDs().next());
//					keyPrivData.setWaktu_buat(crDate);
//					
//					Key keyPubData=new Key();
//					keyPubData.setJenis_key(new JenisKey("PS"));
//					keyPubData.setKey(Base64.toBase64String(ds.getPublicSign().getEncoded()));
//					keyPubData.setKey_id(String.valueOf(ds.getPublicSign().getKeyID()));
//					keyPubData.setStatus(new StatusKey("ACT"));
//					keyPubData.setUserdata(userTrx.getUserdata());
//					keyPubData.setUser_id((String) ds.getPublicSign().getUserIDs().next());
//					keyPubData.setWaktu_buat(crDate);
//					
//					KeyDao kDao = new KeyDao(db);
//					kDao.create(keyPrivData);
//					kDao.create(keyPubData);
//					
//					jo.put("status", "OK");
//					jo.put("key", ds.keyTxt(Base64.toBase64String(ds.getPublicSign().getEncoded())));
//					
//					System.out.println("PUB : "+Base64.toBase64String(ds.getPublicSign().getEncoded()));
//					System.out.println("PRIV : "+Base64.toBase64String(ds.getSecKey().getEncoded()));
//				}
//				System.out.println(jo.toString());
//				jsonResult=jo.toString();
//			}
//			if(method.equals("signPub") && user!=null){
//
//				DigiSign ds=new DigiSign();
//				String pb=ds.signKey(request.getParameter("pub"));
//				JSONObject jo =new JSONObject();
//				if(pb==null){
//					jo.put("status", "Tidak dapat diproses");
//					
//				}else{
//					Key keyPubData=new Key();
//					keyPubData.setJenis_key(new JenisKey("PS"));
//					keyPubData.setKey(Base64.toBase64String(ds.getPublicSign().getEncoded()));
//					keyPubData.setKey_id(String.valueOf(ds.getPublicSign().getKeyID()));
//					keyPubData.setStatus(new StatusKey("ACT"));
//					keyPubData.setUserdata(userTrx.getUserdata());
//					keyPubData.setUser_id((String) ds.getPublicSign().getUserIDs().next());
//					keyPubData.setWaktu_buat(new Date());
//					
//					KeyDao kDao = new KeyDao(db);
//					kDao.create(keyPubData);
//					
//					jo.put("status", "OK");
//					jo.put("key", ds.keyTxt(pb));
//					
//				}
//				System.out.println(jo.toString());
//				jsonResult=jo.toString();
//			}
//			
//			if(jsonResult!=null && !jsonResult.equals("")){
//	            context.put("jsoncontent", jsonResult);
//			}
			
			
			
			

		}catch (Exception e) {
            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
		}
	}
	
	JSONObject register(JSONObject jsonRecv,List<FileItem> fileSave,JPublishContext context) throws JSONException{
		User userRecv;
		DB 	db = getDB(context);
        JSONObject jo=new JSONObject();
        String res="05";
        String notif="Email sudah terdaftar gunakan email lain";
		Userdata userdata= new Userdata();
		UserdataDao udataDao=new UserdataDao(db);
		FileItem ktpItm = null, WajahItm = null;
        for (FileItem fileItem : fileSave) {
    	  if(fileItem.getFieldName().equals("fotodiri")){
    		  WajahItm=fileItem;
      	  }else   if(fileItem.getFieldName().equals("fotoktp")){
          	  ktpItm=fileItem;
      	  }
        }
        
        if(WajahItm==null || ktpItm==null) {

		    res="05";
	        notif="Data upload tidak lengkap";
        }
        else if(udataDao.findByKtp(jsonRecv.get("idktp").toString())!=null){
			notif="Nomor KTP sudah terdaftar";
		}else if(new UserManager(db).findByUsername(jsonRecv.get("email").toString())!=null) {
			notif="Email sudah terdaftar";

		}else {
			
			HttpServletRequest  request=null;
			FaceRecognition fRec=new FaceRecognition("", "", request);
			JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), userdata.getMitra().getId(),jsonRecv.get("idktp").toString());
			
			if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
			    res="05";
			    notif=respFace.getString("info");
				
			}
//			JSONObject respFace=null;
//			if(respFace!=null)return respFace;
			else {
		
			        Transaction tx = db.session().beginTransaction();
		
					try{
				        userdata.setAlamat(jsonRecv.get("alamat").toString());
				        if(jsonRecv.get("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
				        else  userdata.setJk('P');
				        userdata.setKecamatan(jsonRecv.get("kecamatan").toString());
				        userdata.setKelurahan(jsonRecv.get("kelurahan").toString());
				        userdata.setKodepos(jsonRecv.get("kode_pos").toString());
				        userdata.setKota(jsonRecv.get("kota").toString());
				        userdata.setNama(jsonRecv.get("nama").toString());
				        userdata.setNo_handphone(jsonRecv.get("tlp").toString());
				        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
				        userdata.setPropinsi(jsonRecv.get("provinci").toString());
				        userdata.setNo_identitas(jsonRecv.get("idktp").toString());
				        userdata.setTempat_lahir(jsonRecv.get("tmp_lahir").toString());
				        	 
				        System.out.println("masuk");
				        User login =new User();
				        login.getLogin().setPassword(EEUtil.getHash(jsonRecv.get("email").toString(), jsonRecv.get("password").toString()));
				        login.setNick(jsonRecv.get("email").toString());
				        login.setName(jsonRecv.get("nama").toString());
				        login.grant("ds");
				        login.grant("login");
				        login.setUserdata(userdata);
				        login.logRevision("created", new UserManager(db).findById((long) 0));
				        login.setStatus('0');
				        login.setPay_type('1');
		//		        udataDao.create(userdata);
				        db.session().save (userdata);
		
				        System.out.println("masuk 2");
		
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
				        res="05";
				        notif="Berhasil";
		//				if(ds!=null)jo.put("key", Base64.toBase64String(ds.getPublicSign().getEncoded()));
						
				        System.out.println("masuk 3");
		
						System.out.println("data size :" +fileSave.size());
						  if (fileSave.size() >=3) {
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
		//					  ttd.setUserdata(userTrx.getUserdata());
		//	                  ttd.setWaktu_buat(date);
			                  for (FileItem fileItem : fileSave) {
			                	  if(fileItem.getFieldName().equals("ttd")) {
			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
		//	                    	  fileItem.write(fileTo);
			                          BufferedImage source =  ImageIO.read(fileItem.getInputStream());
			                          int color = source.getRGB(0, 0);
			                          Image data=FileProcessor.makeColorTransparent(source, new Color(color), 10000000);
			                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,source.getWidth(),
			                           		  source.getHeight());
				                      ImageIO.write(newBufferedImage, "png" ,fileTo);
		//		                      ttd.setFile(viewpdf);
		//		                      
		//		                      ttd.setFile_name(fileItem.getName());
		//		                      ttd.setPath(uploadTo);
		//		                      ttd.setRename(rename);
		//		                      ttd.setStatus('F');
		//		                      System.out.println("#### Save File ####");
		////		                      new TtdDao(db).create(ttd);
		//			  		          db.session().save(ttd);
		//			  		          
			                    	  userdata.setImageTtd(fileTo.getPath());
		
			                	  }
			                	  else   if(fileItem.getFieldName().equals("fotodiri")){
			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                    	  fileItem.write(fileTo);
			                    	  userdata.setImageWajah(fileTo.getPath());
			                	  }else   if(fileItem.getFieldName().equals("fotoktp")){
			                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                    	  fileItem.write(fileTo);
			                    	  userdata.setImageKTP(fileTo.getPath());
			                	  }else   if(fileItem.getFieldName().equals("fotonpwp")){
				            		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
				                	  fileItem.write(fileTo);
				                	  userdata.setImageNPWP(fileTo.getPath());
			                	  }
			                  
			                  }
			  		          db.session().update(userdata);
		
		//	                  udataDao.update(userdata);
			                  
						  }else {
		
							    res="05";
						        notif="Data upload tidak lengkap";
						  }
			
					     tx.commit ();
					     
					    MailSenderRegister mail=new MailSenderRegister(userdata,jsonRecv.get("email").toString());
						mail.run();
						
						UserManager um=new UserManager(db);
						
						
				        
				        try {
				        	 //bikin user
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
			}
		}
        jo.put("result", res);
        jo.put("notif", notif);
		
	   return jo;
	}
	
	
	JSONObject preRegisterMitra(JSONObject jsonRecv,List<FileItem> fileSave, String namaMitra,JPublishContext context) throws JSONException{
		User userRecv;
		DB db = getDB(context);
		JSONObject jo=new JSONObject();
        String res="05";
        String notif="Email sudah terdaftar gunakan email lain";
		PreRegistration userdata= new PreRegistration();
		Mitra mitra =new MitraDao(db).findMitra(namaMitra);
		PreRegistrationDao udataDao=new PreRegistrationDao(db);
		boolean nextProcess=true;
		
		UserManager um=new UserManager(db);
		User usr= um.findByUsername(jsonRecv.get("userid").toString());
		userRecv=null;
//		Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
		DigiSign ds=new DigiSign();
		if(um==null){
			res="06"; //username/ password salah
		}else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
			System.out.println(usr.getLogin().getPassword());
			System.out.println(jsonRecv.get("pwd"));
			res="06";
		}else
		{
			User user= new UserManager(db).findByUsername(jsonRecv.get("email").toString()); 
			if(user!=null) {
				nextProcess=false;
			}
			List<Userdata> userData=new UserdataDao(db).findByKtp(jsonRecv.get("idktp").toString());
			if(userData.size()>1) {
				nextProcess=false;
		        notif="Akun sudah terdaftar";
			}
			if(nextProcess) {
				try{
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
			        userdata.setNpwp(jsonRecv.get("npwp").toString());
			        userdata.setMitra(mitra);
			        Long idUserData=udataDao.create(userdata);
			        userdata.setId(idUserData.longValue());
			      
					
					System.out.println("data size :" +fileSave.size());
					  if (fileSave.size() >=1) {
				          res="00";
				          notif="Berhasil";
		
						  String uploadTo = basepathPreReg+userdata.getId()+"/original/";
						  String directoryName = basepathPreReg+userdata.getId()+"/original/";
						  String viewpdf = basepathPreReg+userdata.getId()+"/original/";
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
		                  for (FileItem fileItem : fileSave) {
		                	  System.out.println(fileItem.getFieldName());
		                	  if(fileItem.getFieldName().equals("ttd")) {

		                    	  
		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
//		                    	  fileItem.write(fileTo);
		                          BufferedImage source =  ImageIO.read(fileItem.getInputStream());
		                          int color = source.getRGB(0, 0);
		                          Image data=FileProcessor.makeColorTransparent(source, new Color(color), 10000000);
		                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(data,source.getWidth(),
		                           		  source.getHeight());
			                      ImageIO.write(newBufferedImage, "png" ,fileTo);
//			                      ttd.setFile(viewpdf);
//			                      
//			                      ttd.setFile_name(fileItem.getName());
//			                      ttd.setPath(uploadTo);
//			                      ttd.setRename(rename);
//			                      ttd.setStatus('F');
//			                      System.out.println("#### Save File ####");
////			                      new TtdDao(db).create(ttd);
//				  		          db.session().save(ttd);
//				  		          
		                    	  userdata.setImageTtd(fileTo.getPath());
		
		                	  }
		                	  else   if(fileItem.getFieldName().equals("fotodiri")){
		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
		                    	  fileItem.write(fileTo);
		                    	  userdata.setImageWajah(fileTo.getPath());
		                	  }else   if(fileItem.getFieldName().equals("fotoktp")){
		                		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
		                    	  fileItem.write(fileTo);
		                    	  userdata.setImageKTP(fileTo.getPath());
		                    	  ktp=true;
		                	  }else   if(fileItem.getFieldName().equals("fotonpwp")){
			            		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                	  fileItem.write(fileTo);
			                	  userdata.setImageNPWP(fileTo.getPath());
		                	  }
		                  
		                  }
		                  
		
		                  if(ktp==true) {
				                udataDao.update(userdata);
				                System.out.println("userdata ID : "+ userdata.getId());
				                MailSender mail=new MailSender(userdata,userdata.getEmail(),String.valueOf(userdata.getId()));
								mail.run();
		                  }
		                  else {
		          		    	res="05";
		      				    notif="data upload tidak lengkap";
		
		                  }
					  }else {
						  notif="data upload tidak lengkap";
					  }
		
				}
			
				catch (Exception e) {
					// TODO: handle exception
					udataDao.delete(userdata);
					LogSystem.error(getClass(), e);
					log.error(e);

				    res="05";
			        notif="Data gagal diproses";
				}
			}
		}
        jo.put("result", res);
        jo.put("notif", notif);
		
	   return jo;
	}
	

	JSONObject checkPubKey(JSONObject jsonRecv) throws JSONException{
        JSONObject jo=new JSONObject();
        String res="05";

		try{
			String PublicKey=jsonRecv.get("pk").toString();
			DigiSign ds=new DigiSign();
			if(ds.verifyPublicKey(PublicKey)){
				res="00";
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
        jo.put("result", res);
		
	   return jo;
	}
	
	JSONObject checkSign(JSONObject jsonRecv,JPublishContext context) throws JSONException{
		User userRecv;
		DB 	db = getDB(context);
        JSONObject jo=new JSONObject();
        String res="05";

		try{
			String message=jsonRecv.get("data").toString();
			String sign=jsonRecv.get("sign").toString();
			String user=jsonRecv.get("data-from").toString();
			UserManager um=new UserManager(db);
			User usr= um.findByUsername(jsonRecv.get("userid").toString());
			userRecv=null;
			Key k=new KeyDao(db).findByUserEmail(user);
			DigiSign ds=new DigiSign();
			if(um==null){
				res="06"; //username/ password salah
			}else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
				System.out.println(usr.getLogin().getPassword());
				System.out.println(jsonRecv.get("pwd"));
				res="06";
			}
			else if(k!=null){
				
				if(ds.checkSign(message, sign, k.getKey()))res="00";
				userRecv=usr;
			}

			TrxDs trx=new TrxDs();
			trx.setMessage(jsonRecv.toString());
			trx.setMsg_from(usr.getUserdata());
			trx.setMsg_time(new Date());
			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
			else trx.setStatus(new StatusKey("EVR"));
			trx.setType('1');
			if(userRecv!=null){
				trx.setMsg_to(userRecv.getUserdata());
				new TrxDSDao(db).create(trx);
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
        jo.put("result", res);
		
	   return jo;
	}
	
	JSONObject setSign(JSONObject jsonRecv,JPublishContext context) throws JSONException{
		User userRecv;
		DB 		db = getDB(context);
        JSONObject jo=new JSONObject();
        String res="05";
        String signature=null;
		try{
			String message=jsonRecv.get("data").toString();
			UserManager um=new UserManager(db);
			User usr= um.findByUsername(jsonRecv.get("userid").toString());
			userRecv=null;
			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
			DigiSign ds=new DigiSign();
			if(um==null){
				res="06"; //username/ password salah
			}else if(!usr.getLogin().getPassword().equals(jsonRecv.get("pwd"))){
				System.out.println(usr.getLogin().getPassword());
				System.out.println(jsonRecv.get("pwd"));
				res="06";
			}
			else if(k!=null){
			    signature=ds.generateSignature(Base64.decode(message), k.getKey(), jsonRecv.get("pwd-key").toString());
				if(signature!=null){
					res="00";
			        jo.put("sign", signature);
				}else{
					jo.put("info", ds.getNotif());
					res="07";
				}
				userRecv=usr;
			}

			TrxDs trx=new TrxDs();
			trx.setMessage(jsonRecv.toString());
			trx.setMsg_from(usr.getUserdata());
			trx.setMsg_time(new Date());
			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
			else trx.setStatus(new StatusKey("EVR"));
			trx.setType('1');
			if(userRecv!=null){
				trx.setMsg_to(userRecv.getUserdata());
				new TrxDSDao(db).create(trx);
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
        jo.put("result", res);
	   return jo;
	}
	
	JSONObject checkFile(JSONObject jsonRecv,byte [] checkFile,JPublishContext context) throws JSONException{
		User userRecv = null;
		DB 		db = getDB(context);
		JSONObject jo=new JSONObject();
        String res="05";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date date=new Date();
        
		try{
			String user=jsonRecv.get("userID").toString();
			Key k=new KeyDao(db).findByUserID(user);
			DigiSign ds=new DigiSign();
			if(k!=null && ds.verifyFile(checkFile,k.getKey(),"upload_data/"+k.getUser_id()+"/"+sdf.format(date))){
				res="00";
			}
			TrxDs trx=new TrxDs();
			trx.setMessage(ds.getFilename());
			trx.setMsg_from(k.getUser().getUserdata());
			trx.setMsg_to(userRecv.getUserdata());
			trx.setMsg_time(date);
			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
			else trx.setStatus(new StatusKey("EVR"));
			trx.setType('2');
			new TrxDSDao(db).create(trx);
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
        jo.put("result", res);
		
	   return jo;
	}
	
	
	JSONObject login(JSONObject jsonRecv,byte [] checkFile,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
        String res="05";
        DB 		db = getDB(context);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date date=new Date();
//		db = getDB(context);

		try{
			String user=jsonRecv.get("userid").toString();
			SmartphoneVerification sVerf=new SmartphoneVerification(db);
//			
//			UserManager um=new UserManager(db);
//			User userLogin=um.findByUsername(user);
//			System.out.println("pass >"+jsonRecv.get("pwd").toString());
//			System.out.println("pass >"+userLogin.getPassword());
//			System.out.println("pass >"+userLogin.getName());
			if(sVerf.verification(jsonRecv)){
		        jo.put("name", sVerf.getEeuser().getName());
		        jo.put("key", Base64.toBase64String(DigiSign.pubCA));
		        jo.put("signature-pic", "");
		        jo.put("info", "Login berhasil");
		        jo.put("msisdn", sVerf.getEeuser().getUserdata().getNo_handphone());
		        jo.put("mitra", sVerf.getEeuser().getMitra()==null?"Personal": sVerf.getEeuser().getMitra().getName());
		        
		        res="00";
		      //bikin user
		        BillingSystem bs=new BillingSystem();
		        bs.createUser(sVerf.getEeuser()); 
		       

			}else {
				jo=sVerf.setResponFailed(jo);
				if(jo.getString("result").equals("E1")) {

					//bikin user
			        BillingSystem bs=new BillingSystem();
			        bs.createUser(sVerf.getEeuser()); 

			        
			        jo.put("msisdn", sVerf.getEeuser().getUserdata().getNo_handphone());
			        jo.put("name", sVerf.getEeuser().getName());
			        jo.put("key", Base64.toBase64String(DigiSign.pubCA));
			        jo.put("signature-pic", "");
			        jo.put("info", "Activation");
			        jo.put("msisdn", sVerf.getEeuser().getUserdata().getNo_handphone());
			        jo.put("mitra", sVerf.getEeuser().getMitra()==null?"Personal": sVerf.getEeuser().getMitra().getName());

				}
				return jo;
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
        jo.put("result", res);
		
	   return jo;
	}
	

	JSONObject preRegSmartphone(JSONObject jsonRecv,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
        User userRecv;
		DB 		db = getDB(context);
        String res="05";
        String notif="Data Tidak Ditemukan";
		UserdataDao udataDao=new UserdataDao(db);
		
		String userEnc=jsonRecv.get("user").toString();
		String idUser = null;
		try {
			idUser = AESEncryption.decryptDoc(userEnc);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LogSystem.error(getClass(), e1);


		}
		
			PreRegistration user=null;
			if(idUser!=null)user=new PreRegistrationDao(db).findById(Long.valueOf(idUser));
			if(user!=null){
			
			
			
			PreRegistration data=user;
	
			jo.put("alamat", user.getAlamat() );
			jo.put("jenis_kelamin",data.getJk()=='L'?"laki-laki":"perempuan");
			jo.put("kecamatan",data.getKecamatan());
			jo.put("kelurahan",data.getKelurahan());
			jo.put("kode-pos",data.getKodepos());
			jo.put("kota",data.getKota());
			jo.put("nama",data.getNama());
			jo.put("tlp",data.getNo_handphone());
			jo.put("tgl_lahir",data.getTgl_lahir());
			jo.put("tmp_lahir",data.getTempat_lahir());
			jo.put("provinci",data.getPropinsi());
			jo.put("idktp",data.getNo_identitas());
			jo.put("npwp",data.getNpwp());
			jo.put("email",data.getEmail());
			jo.put("password","");
			res="00";
			
			File img=null;
			jo.put("fotodiri","0");
				
			
			img=new File(data.getImageKTP());
			
			if(img!=null) {
				jo.put("fotoktp","1");
			}
			else{
				jo.put("fotoktp","0");
			}
		
			if(img!=null) {
				jo.put("fotottd","1");
			}
			else{
				jo.put("fotottd","0");
			}
			
			if(img!=null) {
				jo.put("fotonpwp","1");
			}
			else{
				jo.put("fotonpwp","0");
			}
		}else {
			res="ND";
			notif="User Tidak Valid";
	
		}

		
	        jo.put("result", res);
	        if(!res.equals("00"))jo.put("notif", notif);
			
		   return jo;
		}
		
		JSONObject processPreRegSmartphone(JSONObject jsonRecv, List<FileItem> fileSave,JPublishContext context) throws JSONException{
	        JSONObject jo=new JSONObject();
			DB 		db = getDB(context);
	        String res="05";
	        String notif="Data Tidak Ditemukan";
			UserdataDao2 udataDao=new UserdataDao2(db);
			UserManager um=new UserManager(db);
			
			
			String OTP=jsonRecv.get("OTP").toString();

			String userEnc=jsonRecv.get("userid").toString();
			String idUser = null;
			try {
				idUser = AESEncryption.decryptDoc(userEnc);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LogSystem.error(getClass(), e1);

	
			}
			
			ConfirmCodeDao cDao=new ConfirmCodeDao(db);
			ConfirmCode cc=cDao.findLastByUserID(new Long(idUser));
			if(cc==null) {
		       notif="kode OTP salah";

			}else if(!cc.getCode().equals(OTP)) {
			       notif="kode OTP salah";
				
			}else {
				cc.setStatus("yes");
				cDao.update(cc);
				PreRegistration user=null;
				if(idUser!=null)user=new PreRegistrationDao(db).findById(Long.valueOf(idUser));
				
				if(user!=null){
				
						res="TA";
						notif="Proses Gagal, Silakan Ulangi Kembali";
						Userdata2 data=new Userdata2();
						Userdata userdata=null;
						try {
							
//							data.setImageWajah2(basepath+data.getId()+"/original/fotodiri2.jpg");
							data.setAlamat(user.getAlamat());
							data.setId(user.getId());
							data.setImageKTP(user.getImageKTP());
							data.setImageNPWP(user.getImageNPWP());
							data.setImageWajah(user.getImageWajah());
							data.setJk(user.getJk());
							data.setKecamatan(user.getKecamatan());
							data.setKelurahan(user.getKelurahan());
							data.setKodepos(user.getKodepos());
							data.setKota(user.getKota());
							data.setNama(user.getNama());
							data.setNo_handphone(user.getNo_handphone());
							data.setNo_identitas(user.getNo_identitas());
							data.setNpwp(user.getNpwp());
							data.setPropinsi(user.getPropinsi());
							data.setTempat_lahir(user.getTempat_lahir());
							data.setTgl_lahir(user.getTgl_lahir());
							
							User u=new User();
							u.setName(data.getNama());
							u.setNick(user.getEmail());
							u.getLogin().setPassword(jsonRecv.get("password").toString());
							u.grant("ds");
							u.grant("login");
					        u.logRevision("created", new UserManager(db).findById((long) 0));
					        u.setStatus('1');
							u.setMitra(null);
							u.setPay_type('1');

					        
							udataDao.create(data);
							userdata=new UserdataDao(db).findById(user.getId());
							u.setUserdata(userdata);
							
							Transaction tx = db.session().beginTransaction();
					        db.session().save (u);
					        tx.commit ();
					        
					        new PreRegistrationDao(db).delete(user);
							
							String uploadTo = basepath+user.getId()+"/original/";
							String directoryName = basepath+user.getId()+"/original/";
							String viewpdf = basepath+user.getId()+"/original/";
							File directory = new File(directoryName);
							if (!directory.exists()){
							      directory.mkdirs();
							}
							SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
			
							
//							byte[] fotodiri = Base64.decode(jsonRecv.get("foto").toString());
		
							for (FileItem fileItem : fileSave) {
			              	  if(fileItem.getFieldName().equals("ttd")) {
		
//				  				  Ttd ttd = new Ttd();
//				  			      Date date = new Date();
//				  				  String strDate = sdfDate.format(date);
//				  				  String rename = "DS"+strDate+".png";
//				  				  ttd.setUserdata(userdata);
//				  	              ttd.setWaktu_buat(date);
//			              		  File fileTo = new File(uploadTo +rename);
//			                  	  fileItem.write(fileTo);
//				                    
//				                      ttd.setFile(viewpdf);
//				                      
//				                      ttd.setFile_name(fileItem.getName());
//				                      ttd.setPath(uploadTo);
//				                      ttd.setRename(rename);
//				                      ttd.setStatus('F');
//				                      System.out.println("#### Save File ####");
//				                      new TtdDao(db).create(ttd);
				                      
				                      File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
//			                    	  fileItem.write(fileTo);
			                          BufferedImage source =  ImageIO.read(fileItem.getInputStream());
			                          int color = source.getRGB(0, 0);
			                          Image dataImg=FileProcessor.makeColorTransparent(source, new Color(color), 10000000);
			                          BufferedImage newBufferedImage = FileProcessor.ImageToBufferedImage(dataImg,source.getWidth(),
			                           		  source.getHeight());
				                      ImageIO.write(newBufferedImage, "png" ,fileTo);
//				                      ttd.setFile(viewpdf);
//				                      
//				                      ttd.setFile_name(fileItem.getName());
//				                      ttd.setPath(uploadTo);
//				                      ttd.setRename(rename);
//				                      ttd.setStatus('F');
//				                      System.out.println("#### Save File ####");
////				                      new TtdDao(db).create(ttd);
//					  		          db.session().save(ttd);
//					  		          
			                    	  userdata.setImageTtd(fileTo.getPath());
			              	  }
			              	  else   if(fileItem.getFieldName().equals("fotodiri")){
			              		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                  	  fileItem.write(fileTo);
			                  	  userdata.setImageWajah(fileTo.getPath());
			              	  }else   if(fileItem.getFieldName().equals("fotoktp")){
			              		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
			                  	  fileItem.write(fileTo);
			                  	  userdata.setImageKTP(fileTo.getPath());
			              	  }else   if(fileItem.getFieldName().equals("fotonpwp")){
				            		  File fileTo = new File(uploadTo +fileItem.getFieldName()+".jpg");
				                	  fileItem.write(fileTo);
				                	  userdata.setImageNPWP(fileTo.getPath());
			              	  }
			                
			                }
							if(fileSave.size()>0)new UserdataDao(db).update(userdata);
		
							res="00";
		
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LogSystem.error(getClass(), e);
							log.error(e);
		
						} catch (Exception e) {
							// TODO Auto-generated catch block
							LogSystem.error(getClass(), e);
							log.error(e);
		
						}
				
					
				}
				else {
					res="06";
					notif="User/Password Tidak Sesuai";
				}
		}
        jo.put("result", res);
        if(!res.equals("00"))jo.put("notif", notif);
		
	   return jo;
	}
	
	JSONObject verifikasi(JSONObject jsonRecv,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
		DB 		db = getDB(context);
        String res="05";
        String notif="Data Tidak Ditemukan";
		UserdataDao udataDao=new UserdataDao(db);
		
		String user=jsonRecv.get("userid").toString();
		String pass = Encryption.md5("adminadmin");
		
		if(user.equals("admin")&&pass.equals(jsonRecv.get("pwd").toString())){
		
			User u= new UserManager(db).getUser(jsonRecv.get("email-user").toString(), jsonRecv.get("nik").toString());
			if(u!=null && u.getStatus()=='0') {
				Userdata data=u.getUserdata();
		
				try {
					File img=new File(data.getImageWajah());
					if(img!=null) {
						byte[] fotodiri = Base64.encode(FileUtils.readFileToByteArray(img));
						jo.put("fotodiri",new String(fotodiri, StandardCharsets.US_ASCII));
					}
					img=null;
					img=new File(data.getImageKTP());
					
					if(img!=null) {
						byte[] fotoktp = Base64.encode(FileUtils.readFileToByteArray(img));
						jo.put("fotoktp",new String(fotoktp, StandardCharsets.US_ASCII));
					}
				}catch (IOException e) {
					// TODO Auto-generated catch block
					LogSystem.error(getClass(), e);
				}
		
				jo.put("alamat", data.getAlamat() );
				jo.put("jenis_kelamin",data.getJk()=='L'?"laki-laki":"perempuan");
				jo.put("kecamatan",data.getKecamatan());
				jo.put("kelurahan",data.getKelurahan());
				jo.put("kode-pos",data.getKodepos());
				jo.put("kota",data.getKota());
				jo.put("nama",data.getNama());
				jo.put("tlp",data.getNo_handphone());
				jo.put("tgl_lahir",data.getTgl_lahir());
				jo.put("tmp_lahir",data.getTempat_lahir());
				jo.put("provinci",data.getPropinsi());
				jo.put("idktp",data.getNo_identitas());
				jo.put("npwp",data.getNpwp());
				jo.put("email",u.getNick());
					res="00";
				
			}else if(u!=null && u.getStatus()=='2') {
				res="VR";
				notif="User Pernah Diverifikasi dan Tidak Valid";

			}else if(u!=null && u.getStatus()!='2') {
				res="VR";
				notif="User Sudah Diverifikasi";

			}
			
		}
		else {
			res="06";
			notif="User/Password Tidak Sesuai";
		}

        jo.put("result", res);
        if(!res.equals("00"))jo.put("notif", notif);
		
	   return jo;
	}
	
	JSONObject process(JSONObject jsonRecv,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
		DB 		db = getDB(context);
        String res="05";
        String notif="Data Tidak Ditemukan";
		UserdataDao udataDao=new UserdataDao(db);
		UserManager um=new UserManager(db);
		
		String user=jsonRecv.get("userid").toString();
		String pass = Encryption.md5("adminadmin");
		
		if(user.equals("admin")&&pass.equals(jsonRecv.get("pwd").toString())){
		
			User u= um.getUser(jsonRecv.get("email-user").toString(), jsonRecv.get("nik").toString());
			if(u!=null && u.getStatus()=='0') {
				res="TA";
				notif="Proses Gagal, Silakan Ulangi Kembali";
				Userdata data=u.getUserdata();
				byte[] fotodiri = Base64.decode(jsonRecv.get("foto").toString());

				FileOutputStream stream = null;
				try {
					stream = new FileOutputStream(basepath+u.getId()+"/original/fotodiri2.jpg");
					stream.write(fotodiri);
					data.setImageWajah2(basepath+u.getId()+"/original/fotodiri2.jpg");
					if(jsonRecv.has("fprt-1"))data.setFingerprint1(jsonRecv.get("fprt-1").toString());
					if(jsonRecv.has("fprt-2"))data.setFingerprint2(jsonRecv.get("fprt-2").toString());
					u.setStatus(jsonRecv.get("action").toString().charAt(0)=='0'? '2':'1');
					
					udataDao.update(data);
					um.update(u);
					res="00";

				} catch (IOException e) {
					// TODO Auto-generated catch block
					LogSystem.error(getClass(), e);
					log.error(e);

				} finally {
				    try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LogSystem.error(getClass(), e);
						log.error(e);

					}
				}
				
			}else if(u!=null && u.getStatus()=='2') {
				res="VR";
				notif="User Pernah Diverifikasi Dan Tidak Valid";

			}else if(u!=null && u.getStatus()!='2') {
				res="VR";
				notif="User Sudah Diverifikasi";

			}
			
		}
		else {
			res="06";
			notif="User/Password Tidak Sesuai";
		}

        jo.put("result", res);
        if(!res.equals("00"))jo.put("notif", notif);
		
	   return jo;
	}
	
	JSONObject loginRA(JSONObject jsonRecv) throws JSONException{
        JSONObject jo=new JSONObject();
        String res="05";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date date=new Date();
		try{
			String user=jsonRecv.get("userid").toString();
			String pass=jsonRecv.get("pwd").toString();
			String adminPass= Encryption.md5("adminadmin");
			
			if(user.equals("admin") && pass.equals(adminPass)){
        
		        
		        res="00";

			}
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
        jo.put("result", res);
		
	   return jo;
	}

	JSONObject activation(JSONObject jsonRecv,JPublishContext context) throws JSONException{
        JSONObject jo=new JSONObject();
		DB 		db = getDB(context);
        String res="05";
        String notif=null;;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date date=new Date();
		try{
			SmartphoneVerification sVerf=new SmartphoneVerification(db);
			
			boolean hVerification=sVerf.verification(jsonRecv);
			
			if(sVerf.getRC().equals("00") || sVerf.getRC().equals("E1")){
		        res="00";

				String OTP=jsonRecv.get("OTP").toString();
				ConfirmCodeDao cDao=new ConfirmCodeDao(db);
				ConfirmCode cc=cDao.findLastByEEUser(sVerf.getEeuser().getId());
				if(cc==null) {
			           notif="kode OTP salah";
			           res="05";
				}else if(cc.getCode().equals(OTP)) {
					   cc.setStatus("yes");
					   cDao.update(cc);
					   User userAct=sVerf.getEeuser();
			           jo.put("name", userAct.getName());
			           jo.put("name", sVerf.getEeuser().getName());
				       jo.put("key", Base64.toBase64String(DigiSign.pubCA));
				       userAct.setStatus('3');
			           //userAct.setImei(jsonRecv.getString("imei").toString());
			           new UserManager(db).update(userAct);
			           notif="Aktivasi berhasil";
			           
				}else {
				       notif="kode OTP salah";
			           res="05";
				}

			}else {
				jo=sVerf.setResponFailed(jo);
				return jo;
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			LogSystem.error(getClass(), e);
			log.error(e);

		}
		if(notif!=null) jo.put("info", notif);
        jo.put("result", res);
		
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
			new SendMailSSL().sendMailPreregisterModalku(name, email, id);

		}
		
	}
	
	class MailSenderRegister{

		String email;
		Userdata name;
		
		public MailSenderRegister(Userdata name,String email) {
			this.email=email;
			this.name=name;
		}
		public void run() {
			new SendMailSSL().sendMailRegister(name, email);

		}
		
	}
	
}
