package apiMitra;

import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.LogSystem;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class CheckUserENIKMitra extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="CU"+sdfDate2.format(tgl).toString();
	String kelas="apiMitra.CheckUserENIKMitra";
	String trxType="C-USER-ENM";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="CU"+sdfDate2.format(tgl).toString();
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		
		Mitra mitra=null;
		User useradmin=null;
		JSONObject jo=new JSONObject();
		try {
			jo.put("refTrx", refTrx);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try{
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);

			// no multipart form
			if (!isMultipart) {
				//JSONObject jo=new JSONObject();
				jo.put("res", "55");
				jo.put("notif", "Bukan multipart");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
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
					} else {
						LogSystem.error(request, "Token tidak ada",kelas, refTrx, trxType);
						//JSONObject jo=new JSONObject();
						jo.put("res", "55");
						jo.put("notif", "token salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						LogSystem.response(request, jo, kelas, refTrx, trxType);
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
//					         
//							 if(fileItem.getFieldName().equalsIgnoreCase("fotodiri")){
//								 filedata=fileItem;
//								 filename=fileItem.getName();
//							 }

						if(fileItem.getFieldName().equals("fotodiri")){
							fileSave.add(fileItem);
						}
					}
				}
			}
			String process=request.getRequestURI().split("/")[2];
			LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);

			LogSystem.request(request, fileItems,kelas, refTrx, trxType);
			if(jsonString==null) return;
			JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");


			//JSONObject jo = null;

			jo=CheckUserENIKMitra(mitra, useradmin, fileSave, jsonRecv, request, context);

			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";

			context.put("trxjson", res);
			LogSystem.response(request, jo, kelas, refTrx, trxType);

		}catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
			//JSONObject jo=new JSONObject();
			try {
				jo.put("result", "05");
				jo.put("notif", "Data tidak ditemukan");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				LogSystem.response(request, jo, kelas, refTrx, trxType);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}

	JSONObject CheckUserENIKMitra(Mitra mitratoken, User useradmin, List<FileItem> fileSave, JSONObject jsonRecv, HttpServletRequest  request,JPublishContext context) throws JSONException, IOException {
		DB db = getDB(context);

		JSONObject jo=new JSONObject();
		JSONArray lWaiting= new JSONArray();
		JSONArray lSigned= new JSONArray();
		String status = "waiting";
		String res="05";
		Boolean nikstatus = false;
		Boolean emailstatus = false;
		Boolean facematch = false;
		Documents id_doc=null;
		boolean kirim=false;

		PrintWriter outStream = null;
		try {
			outStream = context.getResponse().getWriter();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try{
			ApiVerification aVerf = new ApiVerification(db);
			boolean vrf=false;
			if(mitratoken!=null)
			{
				vrf=true;
			}
			else
			{
				vrf=aVerf.verification(jsonRecv);
			}

			if(vrf)
			{
				User usr=null;
				Mitra mitra=null;

				FileItem selfie = null;
				for (FileItem fileItem : fileSave) {
					if(fileItem.getFieldName().equals("fotodiri")){
						selfie=fileItem;
					}
				}

				if(mitratoken==null)
				{
					if(aVerf.getEeuser().isAdmin()==false)
					{
						jo.put("result", "12");
						jo.put("notif", "userid anda tidak diijinkan.");
						return jo;
					}
					usr = aVerf.getEeuser();
					mitra = usr.getMitra();
				}
				else
				{
					usr=useradmin;
					mitra=mitratoken;
				}

				UserManager um=new UserManager(db);
				UserdataDao usdo = new UserdataDao(db);
				FaceRecognition fRec=new FaceRecognition(refTrx, trxType, request);
				User user = um.findByEmailMitra2(jsonRecv.getString("email").toLowerCase());

				if(user != null)
				{
					LogSystem.info(request, "Email true",kelas, refTrx, trxType);
					emailstatus = true;
				}
				else
				{
					
					LogSystem.info(request, "Email false",kelas, refTrx, trxType);
					jo.put("result", "14");
					jo.put("notif", "false");
					LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
					return jo;
				}

				if(jsonRecv.getString("nik").equals(user.getUserdata().getNo_identitas()))
				{
					LogSystem.info(request, "NIK true",kelas, refTrx, trxType);
					nikstatus = true;
				}
				else
				{
					LogSystem.info(request, "NIK False",kelas, refTrx, trxType);
					jo.put("result", "14");
					jo.put("notif", "false");
					LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
					return jo;
				}

				BufferedImage image = null;
				File file = new File(user.getUserdata().getImageWajah());
				byte[] data = FileUtils.readFileToByteArray(file);

				JSONObject respFace=fRec.checkFacetoFace(Base64.encode(data), Base64.encode(selfie.get()), mitra.getId(),jsonRecv.get("nik").toString());

				if(!respFace.getBoolean("result") && respFace.getDouble("score")>0.50) {
					LogSystem.info(request,  "Face recognition info : " + respFace.getString("info"),kelas, refTrx, trxType);
					facematch = false;
					jo.put("result", "14");
					jo.put("notif", "false");
					LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
					return jo;
				}
				else
				{
					LogSystem.info(request,  "Face recognition info : " + respFace.getString("info"),kelas, refTrx, trxType);
					facematch = true;
				}

				LogSystem.info(request, "email : " + emailstatus,kelas, refTrx, trxType);
				LogSystem.info(request, "nik : " + nikstatus,kelas, refTrx, trxType);
				LogSystem.info(request, "match : " + facematch,kelas, refTrx, trxType);

				if(emailstatus && nikstatus && facematch)
				{
					jo.put("result", "00");
					jo.put("notif", "true");
					LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
					return jo;
				}
				else
				{
					jo.put("result", "14");
					jo.put("notif", "false");
					LogSystem.info(request, "Response : " + jo,kelas, refTrx, trxType);
					return jo;
				}
			}
			else {
				jo.put("res", res);
				jo.put("notif", "UserId atau Password salah");
			}
			return jo;
		}
		catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
			jo.put("res", "06");
			jo.put("notif", "check user gagal");
			return jo;
		}
	}
}
