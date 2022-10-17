package apiMitra;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;

public class DownloadSpeciment64 extends ActionSupport {

	  static String basepath="/opt/data-DS/UploadFile/";
	  static String basepathPreReg="/opt/data-DS/PreReg/";
	  Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="DTTD"+sdfDate2.format(tgl).toString();
		String kelas="apiMitra.DownloadSpeciment64";
		String trxType="DWLTTD";
	  
	  @SuppressWarnings("unchecked")
	  @Override
	  public void execute(JPublishContext context, Configuration cfg) {
		  Date tgl= new Date();
			SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
			refTrx="DTTD"+sdfDate2.format(tgl).toString();
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
	    try{
	        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

	        // no multipart form
	        if (!isMultipart) {
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
	            TokenMitra tm=tmd.findByToken(token.toLowerCase());
	            //System.out.println("token adalah = "+token);
	            if(tm!=null) {
	              LogSystem.info(request, "Token ada : " + token,kelas, refTrx, trxType);
	              mitra=tm.getMitra();
	            } else {
	              LogSystem.error(request, "Token salah", kelas, refTrx, trxType);
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
	            
	          }
	        }
	       String process=request.getRequestURI().split("/")[2];
	           //System.out.println("PATH :"+request.getRequestURI());
	       LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);
	 
	       LogSystem.request(request, fileItems, kelas, refTrx, trxType);
	       if(jsonString==null) return;           
	           JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	           
	           if(mitra!=null) {
	             
	             if (!jsonRecv.has("userid"))
	                {
	                  LogSystem.error(request, "Parameter userid tidak ditemukan", kelas, refTrx, trxType);
	                  JSONObject jo=new JSONObject();
	              jo.put("res", "05");
	              jo.put("notif", "Parameter userid tidak ditemukan");
	              context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
	             
	              return;
	                }
	             if (!jsonRecv.has("email"))
	                {
	                  LogSystem.error(request, "Parameter email tidak ditemukan", kelas, refTrx, trxType);
	                  JSONObject jo=new JSONObject();
	              jo.put("res", "05");
	              jo.put("notif", "Parameter email tidak ditemukan");
	              context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
	             
	              return;
	                }
	                
	             String userid=jsonRecv.getString("userid").toLowerCase();
	             UserManager user=new UserManager(getDB(context));
	             useradmin=user.findByUsername(userid);
	             if(useradmin!=null) {
	               if(useradmin.getMitra().getId()==mitra.getId() && useradmin.isAdmin()) {
	                 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
	               }
	               else {
	                 LogSystem.error(request, "Token dan mitra tidak valid", kelas, refTrx, trxType);
	                 JSONObject jo=new JSONObject();
	             jo.put("res", "12");
	             jo.put("notif", "Token dan Mitra tidak sesuai");
	             context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
	            
	             return;
	               }
	             }
	             else {
	               LogSystem.info(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
	               JSONObject jo=new JSONObject();
	           jo.put("res", "12");
	           jo.put("notif", "userid tidak ditemukan");
	           context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
	          
	           return;
	             }
	           }
	           
	           JSONObject jo = null;
	          
	           jo=DownloadDocMitra(mitra, useradmin, jsonRecv, request, context);
	           
	      String res="";
	      if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
	      else res="<b>ERROR 404</b>";
	          
	      context.put("trxjson", res);
	      LogSystem.response(request, jo, kelas, refTrx, trxType);
	    }catch (Exception e) {
	            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
	    }
	  }
	  
	  JSONObject DownloadDocMitra(Mitra mitratoken, User useradmin, JSONObject jsonRecv, HttpServletRequest  request,JPublishContext context) throws JSONException, IOException {
	    DB db = getDB(context);

	 
	JSONObject jo=new JSONObject();
	        JSONArray lWaiting= new JSONArray();
	    JSONArray lSigned= new JSONArray();
	    String status = "waiting";
	        String res="06";
	        Documents id_doc=null;
	        boolean kirim=false;
	        
	        PrintWriter outStream = null;
	    try {
	      outStream = context.getResponse().getWriter();
	    } catch (IOException e1) {
	      // TODO Auto-generated catch block
	      e1.printStackTrace();
	    }
	        DocumentsDao docDao = new DocumentsDao(db);
	        
	        try{
	          ApiVerification aVerf = new ApiVerification(db);
	          boolean vrf=false;
	          if(mitratoken!=null) {
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
	            
	            Userdata us = new Userdata();
	            UserManager usm = new UserManager(db);
	            
	            if(usm.findByEmail(jsonRecv.get("email").toString()) != null)
	            {
	              if(usm.findByEmail(jsonRecv.get("email").toString()).getUserdata().getImageTtd() == "")
	            {
	                LogSystem.info(request, "File tandatangan kosong",kelas, refTrx, trxType);
	                jo.put("result", "05");
	                jo.put("notif", "File tandatangan kosong");
	                return jo;
	            }
	              else
	              {
	                String filepath = usm.findByEmail(jsonRecv.get("email").toString()).getUserdata().getImageTtd();
	            File downloadFile = new File(filepath);
	               
	            byte[] encoded = Base64.encode(FileUtils.readFileToByteArray(downloadFile));
	            
	            LogSystem.info(request, "Sukses",kelas, refTrx, trxType);
	                jo.put("result", "00");
	                jo.put("file", new String(encoded, StandardCharsets.US_ASCII));
	              return jo;
	              }
	            }
	            else
	            {
	              LogSystem.info(request, "Email tidak ditemukan",kelas, refTrx, trxType);
	              jo.put("result", "05");
	              jo.put("notif", "Email tidak ditemukan");
	              return jo;
	            }
	      }
	      else {
	        LogSystem.info(request, "UserID atau password salah",kelas, refTrx, trxType);
	        jo.put("result", "12");
	        jo.put("notif", "UserId atau Password salah");
	        return jo;
	      }
	        }
	        catch (Exception e) {
	          jo.put("result", "06");
	      jo.put("notif", "Download file gagal");
	      return jo;
	    }
	  }
	}