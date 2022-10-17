package apiMitra;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import api.dukcapil.checkingData;
import api.verifikasi.CheckPhotoToAsliRI;
import api.verifikasi.CheckToDukcapil;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.VerificationDataDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class VerMitra extends ActionSupport {
	static String basepath = "/opt/data-DS/UploadFile/";
	static String basepathPreReg = "/opt/data-DS/PreReg/";
	final static Logger log = LogManager.getLogger("digisignlogger");
	Date tgl = new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	//String refTrx = "VERMITRA" + sdfDate2.format(tgl).toString();
	String kelas = "apiMitra.VerMitra";
	String trxType = "VERMITRA";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl = new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx = "VERF" + sdfDate2.format(tgl).toString();
		int i = 0;
		HttpServletRequest request = context.getRequest();
		String jsonString = null;
		byte[] dataFile = null;
		List<FileItem> fileSave = new ArrayList<FileItem>();
		List<FileItem> fileItems = null;
		// LogSystem.info(request, "DATA DEBUG :"+(i++));
		// LogSystem.info(request, "DATA DEBUG :"+(i++));
		Mitra mitra = null;
		User useradmin = null;
		JSONObject jo = new JSONObject();

		try {
			jo.put("refTrx", refTrx);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);

			// no multipart form
			if (!isMultipart) {
				// LogSystem.info(request, "Bukan multipart");
				// JSONObject jo=new JSONObject();
				jo.put("result", "30");
				jo.put("notif", "Format request API salah.");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.error(request, "req bukan multipart", kelas, refTrx, trxType);
				LogSystem.response(request, jo, kelas, refTrx, trxType);
				return;
			}
			// multipart form
			else {
				TokenMitraDao tmd = new TokenMitraDao(getDB(context));
				String token = request.getHeader("authorization");
				if (token != null) {
					String[] split = token.split(" ");
					if (split.length == 2) {
						if (split[0].equals("Bearer"))
							token = split[1];
					}
					TokenMitra tm = tmd.findByToken(token.toLowerCase());
					// LogSystem.info(request, "token adalah = "+token);
					if (tm != null) {
						// LogSystem.info(request, "Token ada : " + token);
						LogSystem.info(request, "ada Token = " + token, kelas, refTrx, trxType);
						mitra = tm.getMitra();
					} else {
						LogSystem.error(request, "Token null ", kelas, refTrx, trxType);

						jo.put("result", "55");
						jo.put("notif", "token salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						LogSystem.response(request, jo, kelas, refTrx, trxType);
						return;
					}
				}

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

				// parse requests
				fileItems = upload.parseRequest(request);

				// Process the uploaded items
				for (FileItem fileItem : fileItems) {
					// a regular form field
					if (fileItem.isFormField()) {
						if (fileItem.getFieldName().equals("jsonfield")) {
							jsonString = fileItem.getString();
						}

					} else {

						if (fileItem.getFieldName().equals("file")) {
							dataFile = fileItem.get();
						}

						if (fileItem.getFieldName().equals("ttd") || fileItem.getFieldName().equals("fotodiri")
								|| fileItem.getFieldName().equals("fotoktp")
								|| fileItem.getFieldName().equals("fotonpwp")
								|| fileItem.getFieldName().equals("foto_dukcapil")) {
							fileSave.add(fileItem);

						}

					}
				}
			}
			String process = request.getRequestURI().split("/")[2];
			LogSystem.info(request, "process :" + process);
			LogSystem.request(request, fileItems, kelas, refTrx, trxType);

			// LogSystem.info(request, refTrx);
			if (jsonString == null)
				return;
			JSONObject jsonRecv = new JSONObject(jsonString).getJSONObject("JSONFile");

			if (mitra != null) {

				if (!jsonRecv.has("userid")) {
					LogSystem.error(request, "Parameter userid tidak ditemukan", kelas, refTrx, trxType);
					// JSONObject jo=new JSONObject();
					jo.put("result", "05");
					jo.put("notif", "Parameter userid tidak ditemukan");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}

				String userid = jsonRecv.getString("userid").toLowerCase();

				UserManager user = new UserManager(getDB(context));
				// User eeuser=user.findByUsername(userid);
				useradmin = user.findByUsername(userid);
				if (useradmin != null) {
					if (useradmin.getMitra().getId() == mitra.getId() && useradmin.isAdmin()) {
						LogSystem.info(request, "Token dan Mitra Valid", kelas, refTrx, trxType);
					} else {
						// LogSystem.error(request, "Token dan mitra tidak sesuai");
						LogSystem.error(request, "Token dan Mitra Tidak Valid", kelas, refTrx, trxType);
						// JSONObject jo=new JSONObject();
						jo.put("result", "55");
						jo.put("notif", "Token dan Mitra tidak sesuai");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						LogSystem.response(request, jo, kelas, refTrx, trxType);
						return;
					}
				} else {
					// LogSystem.error(request, "Userid tidak ditemukan");
					LogSystem.info(request, "Userid tidak ditemukan", kelas, refTrx, trxType);
					// JSONObject jo=new JSONObject();
					jo.put("result", "12");
					jo.put("notif", "userid tidak ditemukan");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.response(request, jo, kelas, refTrx, trxType);
					return;
				}
			}

			// JSONObject jo = null;
			jo = jsonVerifikasiMitra(jsonRecv, fileSave, context, mitra, useradmin, request, refTrx);

			String res = "";
			if (jo != null)
				res = new JSONObject().put("JSONFile", jo).toString();
			else
				res = "<b>ERROR 404</b>";

//			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
//	         LogSystem.info(request, "SEND :"+res);

			context.put("trxjson", res);
			LogSystem.response(request, jo, kelas, refTrx, trxType);

		} catch (Exception e) {
			LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
//			log.error(e);
			// JSONObject jo=new JSONObject();
			try {
				jo.put("result", "05");
				jo.put("notif", "Request Data tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	JSONObject jsonVerifikasiMitra(JSONObject jsonRecv, List<FileItem> fileSave, JPublishContext context,
			Mitra mitratoken, User useradmin, HttpServletRequest request, String refTrx) throws JSONException {
		DB db = getDB(context);
		JSONObject jo = new JSONObject();
		String res = "";
		String notif = "";

//		
		ApiVerification aVerf = new ApiVerification(db);
		boolean nextProcess = true;
		boolean vrf = false;
		if (mitratoken != null && useradmin != null) {
			vrf = true;
		} else {
			vrf = aVerf.verification(jsonRecv);
		}
//		
		VerificationData vd = null;
		VerificationDataDao vdd = new VerificationDataDao(db);

//		
		if (jsonRecv.get("idktp").equals(null) || jsonRecv.get("idktp").equals("")) {
			notif = "idktp tidak boleh NULL";
			jo.put("result", "28");
			jo.put("notif", notif);

			return jo;
		}
		
		if (jsonRecv.get("nama").toString().length() > 128) {
			notif = "teks nama maksimum 128 karakter";
			jo.put("result", "28");
			jo.put("notif", notif);

			return jo;
		}
		if (jsonRecv.get("tgl_lahir").toString().length() < 10 || jsonRecv.get("tgl_lahir").toString().length() > 10) {
			notif = "teks tanggal lahir harus diisi dan maksimum 10 karakter";
			jo.put("result", "28");
			jo.put("notif", notif);

			return jo;
		}
		
		/*
		if (jsonRecv.get("tmp_lahir").equals(null) || jsonRecv.get("tmp_lahir").equals("")) {
			notif = "tempat lahir tidak boleh NULL";
			jo.put("result", "28");
			jo.put("notif", notif);

			return jo;
		}
		*/

		checkingData cd = new checkingData();
//		VerificationData vdata = vdd.getByNik(jsonRecv.getString("idktp"));
		FileItem ktpItm = null; 
		byte[] WajahItm = null;
		
		for (FileItem fileItem : fileSave) {
			if (fileItem.getFieldName().equals("fotodiri")) {
				WajahItm = fileItem.get();
				String extensi = FilenameUtils.getExtension(fileItem.getName());
				// LogSystem.info(request, "Extensi selfie = "+extensi);
				LogSystem.info(request, "Extensi selfie = " + extensi, kelas, refTrx, trxType);
				String ext2 = fileItem.getContentType();
				if (ext2 != null)
					LogSystem.info(request, "Type selfie = " + ext2, kelas, refTrx, trxType);

			} else if (fileItem.getFieldName().equals("fotoktp")) {
				ktpItm = fileItem;
				String ext2 = fileItem.getContentType();
				LogSystem.info(request, "Type ktp = " + ext2, kelas, refTrx, trxType);
			}
		}
		if (WajahItm == null) {
			// LogSystem.info(request, "KTP atau selfie null");
			if(jsonRecv.has("fotodiri")) {
				if(jsonRecv.getString("fotodiri").equals("")) {
					jo.put("result", "28");
					jo.put("notif", "Data upload tidak lengkap");
					jo.put("info", "harap upload photo wajah anda.");
					return jo;
				}
				WajahItm=Base64.decode(jsonRecv.getString("fotodiri"));
			} else {
				LogSystem.info(request, "Selfie NULL", kelas, refTrx, trxType);
				res = "28";
				notif = "Data upload tidak lengkap";
				jo.put("result", res);
				jo.put("notif", notif);
				jo.put("info", "harap upload photo wajah anda.");
				return jo;
			}
			
		}
		
		if(WajahItm==null || WajahItm.length==0) {
			LogSystem.info(request, "Selfie NULL", kelas, refTrx, trxType);
			res = "28";
			notif = "Data upload tidak lengkap";
			jo.put("result", res);
			jo.put("notif", notif);
			jo.put("info", "harap upload photo wajah anda.");
			return jo;
		}

		if (vrf) {
			User usr = null;
			Mitra mitra = null;
//			int levelmitra = 4;
			if (mitratoken == null && useradmin == null) {
				if (aVerf.getEeuser().isAdmin() == false) {
					jo.put("result", "12");
					jo.put("notif", "userid anda tidak diijinkan.");
					return jo;
				}
				usr = aVerf.getEeuser();
				mitra = usr.getMitra();
//				levelmitra = Integer.parseInt(mitra.getLevel().substring(1));
			} else {
				usr = useradmin;
				mitra = mitratoken;
//				levelmitra = Integer.parseInt(mitra.getLevel().substring(1));
			}
			if (mitra != null) {
				nextProcess = true;
			}
			if (nextProcess) {
				try {
					if(DSAPI.DEVELOPMENT) {
						String nik=jsonRecv.getString("idktp");
						if(nik.equalsIgnoreCase("3208130607940011") || nik.equalsIgnoreCase("3175104702960011")) {
							jo = verificationTesting(request, kelas, refTrx, trxType, jsonRecv, mitra, WajahItm);
						} else {
							jo.put("result", "12");
							jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
							jo.put("data", "{}");
						}
						
						return jo;
					} else {
						CheckToDukcapil Checkdukcapil=new CheckToDukcapil(refTrx, trxType, request);
						JSONObject ResDukcapil=Checkdukcapil.check2(jsonRecv.getString("idktp").trim(), jsonRecv.getString("nama").trim(), jsonRecv.getString("tgl_lahir").trim());
						if(ResDukcapil!=null) {
							LogSystem.info(request, ResDukcapil.toString(), kelas, refTrx, trxType);
							JSONObject data=new JSONObject();
							data.put("nik", false);
							data.put("name", false);
							data.put("birthdate", false);
							
							if(ResDukcapil.has("content")) {
    							org.codehaus.jettison.json.JSONArray jarray=ResDukcapil.getJSONArray("content");
    							JSONObject jobj=jarray.getJSONObject(0);
    							if(jobj.has("NAMA_LGKP")){
    								data.put("nik", true);
    								String split[]=jobj.getString("NAMA_LGKP").split(" ");
    								if(split[0].equalsIgnoreCase("Sesuai")) {
    									data.put("name", true);
    								}
    								if(jobj.getString("TGL_LHR").equalsIgnoreCase("Sesuai")){
    									data.put("birthdate", true);
    								}
    								jo.put("data", data);
    								if(data.getBoolean("name")==false||data.getBoolean("birthdate")==false) {
    									jo.put("result", "12");
    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
    				    				jo.put("info", "verifikasi text gagal.");
    				    				return jo;
    								}
    							} else {
    								if(jobj.has("RESPON")) {
										if(jobj.getString("RESPON").equalsIgnoreCase("Kuota Akses Hari ini telah Habis")) {
											jo.put("result", "91");
						    				jo.put("notif", "system timeout, silahkan coba kembali 10 menit kemudian");
						    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
						    				return jo;
										} else if(jobj.getString("RESPON").equalsIgnoreCase("Data Tidak Ditemukan")) {											
											jo.put("result", "12");
	    				    				jo.put("notif", "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai");
	    				    				jo.put("info", "verifikasi text gagal.");
	    				    				jo.put("data",data);
	    				    				LogSystem.info(request, jo.toString(), kelas, refTrx, trxType);
	    				    				return jo;
										}
									}
    								jo.put("result", "12");
    			    				jo.put("notif", "verifikasi user gagal.");
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
						
						LogSystem.info(request, "Check Photo Wajah",kelas, refTrx,trxType);
						FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
						JSONObject respFace=fRec.checkFaceOnly(Base64.encode(WajahItm),Base64.encode(WajahItm), mitra.getId(), jsonRecv.get("idktp").toString());
						//LogSystem.info(request, respFace.toString(),kelas, refTrx,trxType);
						if(respFace.has("file")) {
							JSONObject resp=new JSONObject(respFace.toString());
							resp.put("file", "wiped");
							LogSystem.info(request, "response FR = "+resp.toString(), kelas, refTrx, trxType);
						}else {
							LogSystem.info(request, "response FR = "+respFace.toString(), kelas, refTrx, trxType);
						}
						if(!respFace.getBoolean("result") && respFace.getDouble("score")>DSAPI.THRESHHOLD_FR) {
						    res="12";
						    notif=respFace.getString("info");
						    jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal.");
		    				jo.put("info", notif);
		    				return jo;
						} 
						
						String photowajah64=new String(Base64.encode(WajahItm),StandardCharsets.US_ASCII);
						if(respFace.has("file")) {
							LogSystem.info(request, "menggunakan photo yg diputer<potrait>",kelas, refTrx,trxType);
							photowajah64=respFace.getString("file");
						}
						
						LogSystem.info(request, "kirim ke lembaga pemerintah",kelas, refTrx,trxType);
						CheckPhotoToAsliRI checkPhoto=new CheckPhotoToAsliRI();
						JSONObject jcheck= checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(), refTrx, request);
						
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
						
						for(int i=0 ; i<=3 ; i++)
						{
							
							if(jcheck.has("connection"))
							{
								if(!jcheck.getBoolean("connection"))
								{
									jcheck= checkPhoto.check(jsonRecv.getString("idktp"), photowajah64, mitra.getName(), refTrx, request);
								}
								else
								{
									i = 3;
								}
							}
						}
//						JSONObject checkdata=new JSONObject();
//							checkdata.put("nik", true);
//							checkdata.put("name", true);
//							checkdata.put("birthdate", true);
							//jo.put("data", checkdata);
							
						if(jcheck.get("result").equals("00") || jcheck.get("result").equals("true")) {
							LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
							//jo.put("data", checkdata);
							if(jcheck.has("dataFace")) {
								try {
									if(jcheck.getJSONObject("dataFace").getJSONObject("data").getDouble("selfie_photo")>65) {
										LogSystem.info(request, "Selfie photo > 65", kelas, refTrx, trxType);
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
							if(jcheck.has("connection")) {
								if(jcheck.getBoolean("connection")==false) {
									jo.put("result", "91");
				    				jo.put("notif", "Masalah koneksi, mohon ulangi kembali setelah 10 menit.");
				    				LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
				    				return jo;
								}
							}
							
							
							jo.put("result", "12");
		    				jo.put("notif", "verifikasi user gagal.");
		    				//jo.put("data", checkdata);
		    				jo.put("info", "Verifikasi wajah gagal");
		    				if(jcheck.getString("information").equalsIgnoreCase("Saldo verifikasi habis")) {
		    					jo.put("data", "{}");
		    					jo.put("info", "Saldo verifikasi habis");
		    					jo.put("result", "61");
		    				}
		    				
		    				LogSystem.info(request, jcheck.toString(), kelas, refTrx, trxType);
		    				return jo;
						}
						
						//jo.put("data", checkdata);
						res="00";
						notif="verifikasi berhasil.";
					}
				}

				catch (Exception e) {
					// TODO: handle exception
//					udataDao.delete(userdata);
					LogSystem.error(request, e.toString(), kelas, refTrx, trxType);

					res = "06";
					notif = "Data gagal diproses";
				}
			}
		} else {
			notif = "UserId atau Password salah";
		}

		jo.put("result", res);
		jo.put("notif", notif);
//        jo.put("info", link);
		LogSystem.info(request, "Response akhir : " + jo, kelas, refTrx, trxType);
		return jo;

	}

	public static boolean isValid(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";

		Pattern pat = Pattern.compile(emailRegex);
		if (email == null)
			return false;
		return pat.matcher(email).matches();
	}
	
	public static JSONObject verificationTesting(HttpServletRequest request, String kelas, String refTrx, String trxType, JSONObject jsonRecv, Mitra mitra, byte[] WajahItm) {
		JSONObject jo=new JSONObject();
		String notif="";
		checkingData cd = new checkingData();
		try {
			JSONObject result = cd.check(jsonRecv.getString("idktp"), jsonRecv.getString("nama"),
					jsonRecv.getString("tmp_lahir"), jsonRecv.getString("tgl_lahir"), "",
					new String(Base64.encode(WajahItm), StandardCharsets.US_ASCII), mitra.getName());// jsonRecv.getString("alamat")
//	
//	LogSystem.info(request, "result.toString()" + jsonRecv.getString("idktp") + jsonRecv.getString("nama")
//			+ jsonRecv.getString("tmp_lahir") + mitra.getName());

			if (result == null) {
				LogSystem.info(request, "result.toString() NULL", kelas, refTrx, trxType);
				jo.put("result", "91");
				jo.put("notif", "Masalah koneksi, mohon ulangi kembali");
				return jo;
			}
//			JSONObject data = null;
//			boolean matchFace = false;
			LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
			if (result.get("result").equals("00") || result.get("result").equals("true")) {
				LogSystem.info(request, "Hasilnya berhasil", kelas, refTrx, trxType);
				// LogSystem.info(request, "hasil nya berhasil");
				if (result.has("connection")) {
					if (result.get("connection").equals(false) && result.get("result").equals("05")) {
						jo.put("result", "91");
						jo.put("notif", "verifikasi user gagal.");
						jo.put("info", "Masalah koneksi, mohon ulangi kembali");
						return jo;
					}
					if (result.get("result").equals("FE")) {
						jo.put("result", "91");
						jo.put("notif", "verifikasi user gagal.");
						jo.put("info", "Format tgl salah");
						return jo;
					}
				}

				JSONObject data = null;
				boolean matchFace = false;

				if (result.has("dataFace")) {
					if (result.getJSONObject("dataFace").has("data")
							&& !result.getJSONObject("dataFace").isNull("data")) {
						if (result.getJSONObject("dataFace").getJSONObject("data").has("selfie_photo")
								&& result.getJSONObject("dataFace").getJSONObject("data")
										.getDouble("selfie_photo") > 55) {
							matchFace = true;
							LogSystem.info(request, "Match :" + matchFace, kelas, refTrx, trxType);
						}
					}

					if (result.has("dataID")) {
						if (result.getJSONObject("dataID").has("data")) {
							data = new JSONObject();
							if (!result.getJSONObject("dataID").isNull("data")) {
								data = result.getJSONObject("dataID").getJSONObject("data");
								data.put("selfie_match", matchFace);
							} else {
								result.put("information", "Data KTP tidak ditemukan");
							}
						}

					}
//			
					jo.put("data", result.getJSONObject("dataID").getJSONObject("data"));
					jo.put("result", "00");
					jo.put("notif", "verifikasi berhasil.");
					return jo;

				} else {

					if (result.has("connection")) {
						if (result.get("connection").equals(false) && result.get("result").equals("05")) {
							jo.put("result", "91");
							jo.put("notif", "verifikasi user gagal.");
							jo.put("info", "Masalah koneksi, mohon ulangi kembali");
							return jo;
						}
						if (result.get("result").equals("FE")) {
							jo.put("result", "FE");
							jo.put("notif", "verifikasi user gagal.");
							jo.put("info", "Format tgl salah");
							return jo;
						}
					}

//				JSONObject data = null;
//				boolean matchFace = false;

					if (result.has("dataFace")) {
						if (result.getJSONObject("dataFace").has("data")
								&& !result.getJSONObject("dataFace").isNull("data")) {
							if (result.getJSONObject("dataFace").getJSONObject("data").has("selfie_photo")
									&& result.getJSONObject("dataFace").getJSONObject("data")
											.getDouble("selfie_photo") > 55) {
								matchFace = true;
								LogSystem.info(request, "Match :" + matchFace, kelas, refTrx, trxType);
							}
						}

						if (result.has("dataID")) {
							if (result.getJSONObject("dataID").has("data")) {
								data = new JSONObject();
								if (!result.getJSONObject("dataID").isNull("data")) {
									data = result.getJSONObject("dataID").getJSONObject("data");
									data.put("selfie_match", matchFace);
								} else {
									result.put("information", "Data KTP tidak ditemukan");
								}
							}

						}

						jo.put("result", "12");
						jo.put("notif", "verifikasi user gagal.");
						jo.put("data", data);
						jo.put("info", result.get("information"));
						LogSystem.error(request, "Isi jo :" + jo, kelas, refTrx, trxType);
						// LogSystem.info(request, "Isi jo :" + jo);
						return jo;

					} else {
						jo.put("result", "12");
						jo.put("notif", "verifikasi user gagal.");

						if (result.has("dataID")) {
							if (!result.getJSONObject("dataID").isNull("data")) {
								jo.put("data", result.getJSONObject("dataID").getJSONObject("data"));
								jo.put("info", "Verifikasi data gagal");// Verifikasi wajah gagal
							} else {
								jo.put("info", "Data KTP tidak ditemukan");
							}
						}

						else {
							jo.put("data", result.get("information"));
							jo.put("info", "Registrasi gagal");
						}

						return jo;
					}
				}

			} else {
//				
				if(result.has("dataID")) {
					if (!result.getJSONObject("dataID").isNull("data")) {
						notif = "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai";
						
						jo.put("result", "12");
						jo.put("notif", "verifikasi user gagal.");
						//jo.put("data", result.getJSONObject("dataID").getJSONObject("data"));
						jo.put("data", "{}");
						jo.put("info", result.get("information"));
						LogSystem.error(request, "Isi jo :" + jo, kelas, refTrx, trxType);
						// LogSystem.info(request, "Isi jo :" + jo);
						return jo;
					} else {
						notif = "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai";
						
						jo.put("result", "12");
						jo.put("notif", "verifikasi user gagal.");
						jo.put("data", "{}");
						jo.put("info", result.get("information"));
						LogSystem.error(request, "Isi jo :" + jo, kelas, refTrx, trxType);
						// LogSystem.info(request, "Isi jo :" + jo);
						return jo;
					}
				} else {
					notif = "verifikasi user gagal. NIK/Nama/tanggal lahir tidak sesuai";
					
					jo.put("result", "12");
					jo.put("notif", "verifikasi user gagal.");
					jo.put("data", "{}");
					jo.put("info", result.get("information"));
					LogSystem.error(request, "Isi jo :" + jo, kelas, refTrx, trxType);
					// LogSystem.info(request, "Isi jo :" + jo);
					return jo;
				}
			}
		}

		catch (Exception e) {
			// TODO: handle exception
//			udataDao.delete(userdata);
			//LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			LogSystem.info(request, e.toString(), kelas, refTrx, trxType);

			try {
				jo.put("result", "06");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			notif = "Data gagal diproses";
		}
		return jo;
	}

}
