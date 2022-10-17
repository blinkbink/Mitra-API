package apiMitra;

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
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class verifikasiDoubleLoginForLevelMitra {
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="VERF"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.VerifikasiDoubleLoginForLevelMitra";
	String trxType="VERF-DT";
	
	public verifikasiDoubleLoginForLevelMitra(String refTrx) {
		this.refTrx=refTrx;
	}
	
	public JSONObject check(DB db,User user, String basepathPreReg, Mitra mitra, FileItem dukcapilItm, FileItem WajahItm, FileItem ktpItm, JSONObject jsonRecv, HttpServletRequest request) throws JSONException {
		JSONObject jo=new JSONObject();
		jo.put("result", "00");
		
		try { 
			if(mitra.isVerifikasi()) {
		
			FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
			JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
			
			if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.65) {
			    
			    String notif=respFace.getString("info");
			    jo.put("result", "12");
				jo.put("notif", "verifikasi user gagal.");
				jo.put("info", notif);
				return jo;
			}
			
			LogSystem.info(request, "kirim ke AsliRI", kelas, refTrx, trxType);
			checkingData cd=new checkingData();
			JSONObject result = cd.check(jsonRecv.getString("idktp"), jsonRecv.getString("nama"), jsonRecv.getString("tmp_lahir"), jsonRecv.getString("tgl_lahir"), jsonRecv.getString("alamat"), new String(Base64.encode(WajahItm.get()),StandardCharsets.US_ASCII), mitra.getName());
			//JSONObject obj=
			LogSystem.info(request, result.toString(),kelas, refTrx, trxType);
			if(result.get("result").equals("00") || result.get("result").equals("true")) {
				LogSystem.info(request, "verifikasi berhasil untuk user terdaftar namun level user < mitra", kelas, refTrx, trxType);
				
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
	    				jo.put("notif", "Format tgl salah");
	    				jo.put("info", "Verifikasi user gagal");
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
							LogSystem.info(request, "Match :" + matchFace, kelas, refTrx, trxType);
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
				
				if(jsonRecv.getString("vnik").equals("0") || jsonRecv.getString("vnama").equals("0") || jsonRecv.getString("vtgl_lahir").equals("0") || jsonRecv.getString("vtmp_lahir").equals("0")) {
					String res="12";
				    
				    jo.put("result", "12");
    				jo.put("notif", "verifikasi user gagal.");
    				jo.put("info", "vnik, vnama, vtgl_lahir dan vtmp_lahir harus true");
    				LogSystem.info(request, "verifikasi user gagal non ekyc = "+jo,kelas, refTrx, trxType);
    				return jo;
				}
				
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
				
				
				
				JSONObject respFace=fRec.checkFace(Base64.encode(ktpItm.get()),Base64.encode(WajahItm.get()), mitra.getId(),jsonRecv.get("idktp").toString());
				
				if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.65) {
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

				if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.65) {
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
