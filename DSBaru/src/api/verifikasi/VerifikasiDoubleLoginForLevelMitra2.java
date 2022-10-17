package api.verifikasi;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

import api.dukcapil.checkingData;
import api.log.ActivityLog;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.dao.ReRegistrationDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.ReRegistration;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class VerifikasiDoubleLoginForLevelMitra2 {
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="VERF"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.VerifikasiDoubleLoginForLevelMitra";
	String trxType="VERF-DT";
	
	public VerifikasiDoubleLoginForLevelMitra2(String refTrx) {
		this.refTrx=refTrx;
	}
	
	public JSONObject check(DB db,User user, String basepathPreReg, Mitra mitra, FileItem dukcapilItm, FileItem WajahItm, FileItem ktpItm, JSONObject jsonRecv, HttpServletRequest request) throws JSONException {
		JSONObject jo=new JSONObject();
		jo.put("result", "00");
		
		try {
			if(mitra.isVerifikasi()) {
		
			FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
			JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
			
			if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
			    
			    String notif=respFace.getString("info");
			    jo.put("result", "12");
				jo.put("notif", "verifikasi user gagal.");
				jo.put("info", notif);
				return jo;
			}
			
			String photowajah64=new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII);
			if(respFace.has("file")) {
				LogSystem.info(request, "menggunakan photo yg diputer<potrait>",kelas, refTrx,trxType);
				photowajah64=respFace.getString("file");
			}
			
			LogSystem.info(request, "kirim ke lembaga pemerintah", kelas, refTrx, trxType);
			CheckPhotoToAsliRI checkPhoto=new CheckPhotoToAsliRI();
			JSONObject jcheck= checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(),refTrx, request);
		
			JSONObject checkdata=new JSONObject();
				checkdata.put("nik", true);
				checkdata.put("name", true);
				checkdata.put("birthdate", true);
				jo.put("data", checkdata);
				
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
				if(jcheck.has("dataFace")) {
					try {
						if(jcheck.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo")>65) {
							//jo.put("data", checkdata);
							LogSystem.info(request, "verifikasi CheckingFace berhasil untuk user terdaftar namun level user < mitra", kelas, refTrx, trxType);
						} else {
							jo.put("result", "12");
							jo.put("notif", "verifikasi user gagal.");
							jo.put("info", "Verifikasi wajah gagal");
		    				return jo;
						}
					} catch (Exception e) {
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
				jo.put("result", "12");
				jo.put("notif", "verifikasi user gagal.");
				jo.put("data", checkdata);
				jo.put("info", "Verifikasi wajah gagal");
				return jo;
			}
			
			
		} 
		else {
			FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
			if(!mitra.isEkyc()) {
				if(!jsonRecv.has("ref_verifikasi") && !jsonRecv.has("data_verifikasi") && !jsonRecv.has("score_selfie")) {
					
					
			        String notif="Message request tidak lengkap. ref_verifikasi, data_verifikasi, dan score_selfie, vnik";
			        jo.put("result", "93");
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				
				if(!jsonRecv.has("vnik") && !jsonRecv.has("vnama") && !jsonRecv.has("vtgl_lahir") && !jsonRecv.has("vtmp_lahir")) {
					
			        String notif="Message request tidak lengkap. vnik, vnama, vtgl_lahir dan vtmp_lahir";
			        jo.put("result", "93");
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				
				/*
				if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
					String res="12";
				    
				    jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}
				*/
				
				if(!jsonRecv.has("data_verifikasi")) {
					String res="93";
			        String notif="Message request tidak lengkap. data_verifikasi.";
			        jo.put("result", res);
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				if(!jsonRecv.has("score_selfie")) {
					String res="93";
			        String notif="Message request tidak lengkap. score_selfie.";
			        jo.put("result", res);
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				
				/*
				String dataverf=jsonRecv.getString("data_verifikasi");
				JSONObject joj=new JSONObject(dataverf);
				if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
					jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}*/
				
				String score=jsonRecv.getString("score_selfie");
				Float fscore=Float.valueOf(score);
				if(fscore<75) {
					jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "score selfie dibawah batas minimum.");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}
				
				
				
				JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
				
				if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
				    String res="12";
				    String notif=respFace.getString("info");
				    jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal. photo selfie dan ktp tidak cocok");
    				jo.put("info", notif);
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				} else {
					System.out.println("LOLOS");
				}
				
				LogSystem.info(request, "verifikasi berhasil untuk user terdaftar namun level user < mitra DENGAN MITRA NON-EKYC",kelas, refTrx, trxType);
				
				try {
		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
		        	logSystem.POST("bio-verification", "success", "[API] Berhasil verifikasi biometrik non-ekyc. Data : " + jsonRecv.getString("data_verifikasi"), Long.toString(user.getId()), null, null, null, null,jsonRecv.getString("idktp"));
		        }catch(Exception e)
		        {
		        	e.printStackTrace();
		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
		        }
			}
			
			//jika pengecekan dari dukcapil dengan mngirimkan photo selfie dari dukcapil
        	if(mitra.isDukcapil()) {
				if(dukcapilItm==null) {
					
			        jo.put("result", "28");
			        jo.put("notif", "Data upload tidak lengkap");
			        jo.put("info", "photo hasil dari dukcapil harus dikirim.");
			        return jo;
				}
				JSONObject respFace=fRec.checkFacetoFace(Base64.encode(dukcapilItm.get()), Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());

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
		
			//jika result 00
			user.setName(jsonRecv.getString("nama"));
			
			Userdata udata=user.getUserdata();
			udata.setTempat_lahir(jsonRecv.getString("tmp_lahir"));
			udata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
			udata.setLevel(mitra.getLevel());
			udata.setNama(jsonRecv.getString("nama"));
			
			try {
				SaveFileWithSamba samba=new SaveFileWithSamba();
				String uploadTo = basepathPreReg+udata.getId()+"/original/";
				//File fileTo = new File(uploadTo +ktpItm.getFieldName()+"2.jpg");
          	  	//ktpItm.write(fileTo);
          	  	samba.write(ktpItm.get(), uploadTo+ktpItm.getFieldName()+"2.jpg");
          	  	udata.setImageKTP(uploadTo+ktpItm.getFieldName()+"2.jpg");
          	  	//ktp=true;
          	  	ktpItm.getOutputStream().close();
          	  	
          	  	//File fileToSelfie = new File(uploadTo +WajahItm.getFieldName()+"2.jpg");
          	  	samba.write(WajahItm.get(), uploadTo+WajahItm.getFieldName()+"2.jpg");
          	  	//WajahItm.write(fileToSelfie);
          	  	
          	  	
        	  	udata.setImageWajah(uploadTo+WajahItm.getFieldName()+"2.jpg");
        	  	//ktp=true;
        	  	WajahItm.getOutputStream().close();
        	  	
        	  	if(dukcapilItm!=null) {
        	  		samba.write(dukcapilItm.get(), uploadTo+dukcapilItm.getFieldName()+"2.jpg");
        	  		udata.setImageWajahDukcapil(uploadTo+dukcapilItm.getFieldName()+"2.jpg");
        	  		dukcapilItm.getOutputStream().close();
        	  	}
			} catch (Exception e) {
				// TODO: handle exception
				LogSystem.error(request, e.getMessage(),kelas, refTrx, trxType);
				LogSystem.info(request, "save image ktp atau selfie gagal",kelas, refTrx, trxType);
				jo.put("result", "91");
				jo.put("notif", "Registrasi gagal.");
				jo.put("info", "Masalah koneksi, mohon ulangi kembali");
				return jo;
			}
			
    	  	try {
    	  		new UserManager(db).update(user);
    	  		new UserdataDao(db).update(udata);
    	  	} catch (Exception e) {
				// TODO: handle exception
    	  		LogSystem.error(request, e.getMessage(),kelas, refTrx, trxType);
    	  		LogSystem.info(request, "update user ke database gagal",kelas, refTrx, trxType);
    	  		jo.put("result", "06");
				jo.put("notif", "Registrasi gagal.");
				jo.put("info", "General Error");
				return jo;
			}
    	  	
		return jo;
	} catch(Exception e) {
			jo.put("result", "06");
			jo.put("notif", "Registrasi gagal");
			jo.put("info", "General Error");
			return jo;
		}
	}
	
	
	public JSONObject checkWithCertificate(DB db,User user, String basepathPreReg, Mitra mitra, FileItem dukcapilItm, FileItem WajahItm, FileItem ktpItm, JSONObject jsonRecv, HttpServletRequest request) throws JSONException {
		JSONObject jo=new JSONObject();
		jo.put("result", "00");
		
		try {
			if(mitra.isVerifikasi()) 
			{
				FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
				JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
				
				if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
				    
				    String notif=respFace.getString("info");
				    jo.put("result", "12");
					jo.put("notif", "verifikasi user gagal.");
					jo.put("info", notif);
					return jo;
				}
				
				String photowajah64=new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII);
				if(respFace.has("file")) {
					LogSystem.info(request, "menggunakan photo yg diputer<potrait>",kelas, refTrx,trxType);
					photowajah64=respFace.getString("file");
				}
				
				LogSystem.info(request, "kirim ke lembaga pemerintah untuk verifikasi ulang", kelas, refTrx, trxType);
				CheckPhotoToAsliRI checkPhoto=new CheckPhotoToAsliRI();
				JSONObject jcheck= checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(), refTrx, request);
				LogSystem.info(request, "Response : " + jcheck,kelas, refTrx,trxType);
				
				
				JSONObject checkdata=new JSONObject();
				checkdata.put("nik", true);
				checkdata.put("name", true);
				checkdata.put("birthdate", true);
				jo.put("data", checkdata);
				
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
					if(jcheck.has("dataFace")) {
						try {
							if(jcheck.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo")>65) {
								LogSystem.info(request, "Verifikasi checking face berhasil untuk user terdaftar untuk verifikasi ulang", kelas, refTrx, trxType);
							} else {
								jo.put("result", "12");
								jo.put("notif", "verifikasi user gagal.");
								jo.put("info", "Verifikasi wajah gagal");
			    				return jo;
							}
						} catch (Exception e) {
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
					jo.put("result", "12");
					jo.put("notif", "verifikasi user gagal.");
					jo.put("data", checkdata);
					jo.put("info", "Verifikasi wajah gagal");
					return jo;
				}
		} 
		else {
			FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
			if(!mitra.isEkyc()) {
				if(!jsonRecv.has("ref_verifikasi") && !jsonRecv.has("data_verifikasi") && !jsonRecv.has("score_selfie")) {
			        String notif="Message request tidak lengkap. ref_verifikasi, data_verifikasi, dan score_selfie, vnik";
			        jo.put("result", "93");
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				
				if(!jsonRecv.has("vnik") && !jsonRecv.has("vnama") && !jsonRecv.has("vtgl_lahir") && !jsonRecv.has("vtmp_lahir")) {
					
			        String notif="Message request tidak lengkap. vnik, vnama, vtgl_lahir dan vtmp_lahir";
			        jo.put("result", "93");
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				
				/*
				if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
					String res="12";
				    
				    jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}
				*/
				
				if(!jsonRecv.has("data_verifikasi")) {
					String res="93";
			        String notif="Message request tidak lengkap. data_verifikasi.";
			        jo.put("result", res);
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				if(!jsonRecv.has("score_selfie")) {
					String res="93";
			        String notif="Message request tidak lengkap. score_selfie.";
			        jo.put("result", res);
			        jo.put("notif", notif);
			        LogSystem.info(request, jo.toString(),kelas, refTrx, trxType);
			        return jo;
				}
				
				/*
				String dataverf=jsonRecv.getString("data_verifikasi");
				JSONObject joj=new JSONObject(dataverf);
				if(!joj.getBoolean("name") || !joj.getBoolean("birthplace") || !joj.getBoolean("birthdate")) {
					jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "nama, tempat lahir dan tanggal lahir harus true");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}*/
				
				String score=jsonRecv.getString("score_selfie");
				Float fscore=Float.valueOf(score);
				if(fscore<75) {
					jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "score selfie dibawah batas minimum.");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}
				
				JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
				
				if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
				    String res="12";
				    String notif=respFace.getString("info");
				    jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal. photo selfie dan ktp tidak cocok");
    				jo.put("info", notif);
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				} else {
					System.out.println("LOLOS");
				}
				
				LogSystem.info(request, "verifikasi berhasil untuk user terdaftar namun level user < mitra DENGAN MITRA NON-EKYC",kelas, refTrx, trxType);

				try {
		        	ActivityLog logSystem = new ActivityLog(request, refTrx);
		        	logSystem.POST("bio-verification", "success", "[API] Berhasil verifikasi biometrik non-ekyc. Data : " + jsonRecv.getString("data_verifikasi"), Long.toString(user.getId()), null, null, null, null,jsonRecv.getString("idktp"));
		        }catch(Exception e)
		        {
		        	e.printStackTrace();
		        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
		        }
			}
			
			//jika pengecekan dari dukcapil dengan mengirimkan photo selfie dari dukcapil
        	if(mitra.isDukcapil()) {
				if(dukcapilItm==null) {
					
			        jo.put("result", "28");
			        jo.put("notif", "Data upload tidak lengkap");
			        jo.put("info", "photo hasil dari dukcapil harus dikirim.");
			        return jo;
				}
				JSONObject respFace=fRec.checkFacetoFace(Base64.encode(dukcapilItm.get()), Base64.encode(WajahItm.get()), mitra.getId(), jsonRecv.get("idktp").toString());

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
					
					
					String reSelfieTo = basepathPreReg+user.getUserdata().getId()+"/original/";
	              	
	              	boolean reSelfie=new SaveFileWithSamba().write(WajahItm.get(), reSelfieTo+"SelfieUlang.jpg");
	              	
	              	if(reSelfie==false) {
	              		jo.put("result", "91");
					        jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
					        return jo;
	              	}
	              	
					//Simpan data ke table verifikasi ulang
					ReRegistration rereg = new ReRegistration();
					rereg.setUserdata(user.getUserdata());
					rereg.setDatetime(new Date());
					rereg.setSelfie_photo(reSelfieTo+"SelfieUlang"+new Date().getTime()+(30L * 24 * 60 * 60 * 1000)+".jpg");
					try 
					{
						new ReRegistrationDao(db).create(rereg);
						LogSystem.info(request, "Berhasil verifikasi ulang", kelas, refTrx, trxType);
					}catch(Exception e)
					{
						LogSystem.info(request, "Gagal menyimpan data registrasi ulang", kelas, refTrx, trxType);
						LogSystem.error(getClass(), e);
						jo.put("result", "06");
						jo.put("notif", "verifikasi user gagal.");
						jo.put("info", "Verifikasi wajah gagal");
						return jo;
					}
					
					jo.put("result", "00"); 
    		        jo.put("notif", "Anda sudah terdaftar sebelumnya, silahkan gunakan layanan Digisign");
					return jo;									
				}
			}
		}
		
			//jika result 00
			user.setName(jsonRecv.getString("nama"));
			
			Userdata udata=user.getUserdata();
			udata.setTempat_lahir(jsonRecv.getString("tmp_lahir"));
			udata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(jsonRecv.get("tgl_lahir").toString()));
			udata.setLevel(mitra.getLevel());
			udata.setNama(jsonRecv.getString("nama"));
			
			try {
				SaveFileWithSamba samba=new SaveFileWithSamba();
				String uploadTo = basepathPreReg+udata.getId()+"/original/";
				//File fileTo = new File(uploadTo +ktpItm.getFieldName()+"2.jpg");
          	  	//ktpItm.write(fileTo);
          	  	samba.write(ktpItm.get(), uploadTo+ktpItm.getFieldName()+"2.jpg");
          	  	udata.setImageKTP(uploadTo+ktpItm.getFieldName()+"2.jpg");
          	  	//ktp=true;
          	  	ktpItm.getOutputStream().close();
          	  	
          	  	//File fileToSelfie = new File(uploadTo +WajahItm.getFieldName()+"2.jpg");
          	  	samba.write(WajahItm.get(), uploadTo+WajahItm.getFieldName()+"2.jpg");
          	  	//WajahItm.write(fileToSelfie);
          	  	
          	  	
        	  	udata.setImageWajah(uploadTo+WajahItm.getFieldName()+"2.jpg");
        	  	//ktp=true;
        	  	WajahItm.getOutputStream().close();
        	  	
        	  	if(dukcapilItm!=null) {
        	  		samba.write(dukcapilItm.get(), uploadTo+dukcapilItm.getFieldName()+"2.jpg");
        	  		udata.setImageWajahDukcapil(uploadTo+dukcapilItm.getFieldName()+"2.jpg");
        	  		dukcapilItm.getOutputStream().close();
        	  	}
			} catch (Exception e) {
				// TODO: handle exception
				LogSystem.error(request, e.getMessage(),kelas, refTrx, trxType);
				LogSystem.info(request, "save image ktp atau selfie gagal",kelas, refTrx, trxType);
				jo.put("result", "91");
				jo.put("notif", "Registrasi gagal.");
				jo.put("info", "Masalah koneksi, mohon ulangi kembali");
				return jo;
			}
			
    	  	try {
    	  		new UserManager(db).update(user);
    	  		new UserdataDao(db).update(udata);
    	  	} catch (Exception e) {
				// TODO: handle exception
    	  		LogSystem.error(request, e.getMessage(),kelas, refTrx, trxType);
    	  		LogSystem.info(request, "update user ke database gagal",kelas, refTrx, trxType);
    	  		jo.put("result", "06");
				jo.put("notif", "Registrasi gagal.");
				jo.put("info", "General Error");
				return jo;
			}
    	  	
		return jo;
	} catch(Exception e) {
			jo.put("result", "06");
			jo.put("notif", "Registrasi gagal");
			jo.put("info", "General Error");
			return jo;
		}
	}
	
}
