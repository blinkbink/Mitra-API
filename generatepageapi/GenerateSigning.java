package apiMitra;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.SmartphoneVerification;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.BillingSystem;
import id.co.keriss.consolidate.action.billing.Deposit;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.dao.BankDao;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.InitialDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PartnerDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.StoreDao;
import id.co.keriss.consolidate.dao.TerminalDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.TrxDSDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.UserdataDao2;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.CardSummaryVO;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.JenisKey;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.MerchantVO;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Partner;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.StatusKey;
import id.co.keriss.consolidate.ee.StatusTrxLog;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.TrxDs;

import org.hibernate.Session;
import org.hibernate.Transaction;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.Userdata2;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.Encryption;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.ReportUtil;
import id.co.keriss.consolidate.util.SaldoTransaction;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.jcajce.provider.digest.SHA256.Digest;
import org.bouncycastle.jcajce.provider.symmetric.ARC4.Base;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.ee.action.Login;
import org.jpos.iso.ISOUtil;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;
import org.mortbay.util.ajax.JSON;

import com.anthonyeden.lib.config.Configuration;

public class GenerateSigning extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=Logger.getLogger("digisignlogger");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		int statusSign=0;
		Boolean by_pass_click=false;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		//System.out.println("DATA DEBUG :"+(i++));
		LogSystem.info(request, "DATA DEBUG : " + (i++));
		Mitra mitratoken=null;
		User useradmin=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				TokenMitraDao tmd=new TokenMitraDao(getDB(context));
				String token=request.getHeader("authorization");
				if(token!=null) {
					String[] split=token.split(" ");
					if(split.length==2) {
						if(split[0].equals("Bearer"))token=split[1];
					}
					TokenMitra tm=tmd.findByToken(token.toLowerCase());
					if(tm!=null) {
						LogSystem.info(request, "Token tidak null:" + token);
						mitratoken=tm.getMitra();
					} else {
						LogSystem.info(request, "Token null");
						JSONObject jo=new JSONObject();
						jo.put("result", "05");
						jo.put("notif", "token salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
						
						return;
					}
				}else {
					LogSystem.info(request, "Token null");
					JSONObject jo=new JSONObject();
					jo.put("result", "05");
					jo.put("notif", "token salah");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					return;

				}

				// no multipart form
				if (!isMultipart) {
					//System.out.println("bukan multipart");
					LogSystem.info(request, "Bukan multipart");
					jsonString=request.getParameter("jsonfield");
				}
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
						
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         LogSystem.info(request, "PATH :"+request.getRequestURI());

	         LogSystem.request(request, fileItems);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         LogSystem.info(request, "JSONFile Receive : "+jsonRecv);
	         
	         if(mitratoken!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
		    		 LogSystem.error(request, "Parameter userid tidak ditemukan");
		    		 JSONObject jo=new JSONObject();
						jo.put("result", "05");
						jo.put("notif", "userid salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
	        		 return;
	        	 }
	        	 
	        	 String userid=jsonRecv.getString("userid").toLowerCase();
	        	 
		         UserManager user=new UserManager(getDB(context));
		         //User eeuser=user.findByUsername(userid);
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitratoken.getId() && useradmin.isAdmin()) {
		        		 //System.out.println("token dan mitra valid");
		        		 LogSystem.info(request, "Token dan mitra valid");
		        	 }
		        	 else {
		        		 LogSystem.info(request, "Token dan mitra tidak valid");
		        		 JSONObject jo=new JSONObject();
						 jo.put("result", "05");
						 jo.put("notif", "Token dan Mitra tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());

						 return;
		        	 }
		         }
		         else {
		        	 LogSystem.info(request, "Userid tidak ditemukan");
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());

					 return;
		         }
	         }
	         
	         //JSONObject jo = null;
	        
	         //DownloadDocMitra(jsonRecv, request, context);
	         DB db = getDB(context);
	         ApiVerification aVerf = new ApiVerification(db);
	         boolean vrf=false;
		        if(mitratoken!=null && useradmin!=null) {
		        	vrf=true;
		        }
		        else {
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "userid tidak diizinkan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					 return;

		        }

		        User usermitra=useradmin;
	        	Mitra mitra=useradmin.getMitra();
	        	
	     
        	 	
				String documentID=jsonRecv.getString("document_id");
				DocumentsDao dd = new DocumentsDao(db);
				LoginDao ldao=new LoginDao(db);
				//Documents doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
				Documents doc=null;
				List<Documents> ldocs;
				try {
					LogSystem.info(request, "ID MITRA : ----- " + mitra.getId());
					LogSystem.info(request, "DOCUMENT ID : ----- " + documentID);

					ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
					if(ldocs.size()==0) {
						//System.out.println("Error size 0 : ----- ");
						LogSystem.info(request, "Error size 0");
						context.getResponse().sendError(404);
			        	return;
					}
					else {
						LogSystem.info(request, "Size tidak 0");
						//System.out.println("Success size not 0 : ----- ");
						doc=ldocs.get(0);
					}
				} catch (Exception e) {
					LogSystem.error(request, "Error dokumen");
					e.printStackTrace();
					// TODO: handle exception
					context.getResponse().sendError(404);
		        	return;
				}
				
				String idDoc=null;
				String userEmail=jsonRecv.getString("email_user").toLowerCase();
				Long idmitra = usermitra.getId();
				UserManager um=new UserManager(db);
				LogSystem.info(request, "User Email: " + userEmail);
				//System.out.println(userEmail);
				User usr= um.findByUsername(userEmail);
				
				if(jsonRecv.has("by-pass-click"))
				{
					by_pass_click = jsonRecv.getBoolean("by-pass-click");
				}
				
				//System.out.println("user nya adalah = "+usr.getName());
				if(usr!=null) {
					//System.out.println("USR NICK ------------ : " + usr.getNick());
					
					
					//check level user < 3 dan level mitra > 2
					int leveluser=Integer.parseInt(usr.getUserdata().getLevel().substring(1));
		        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
		        	if(levelmitra>2 && leveluser<3) {
		        		LogSystem.error(request, "level user tidak diperbolehkan untuk mitra ini");
		        		 JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "level user tidak diperbolehkan untuk mitra ini");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		    			 return;
		        	}
					
					List<DocumentAccess> da  = null;
					DocumentsAccessDao docDao = null;
					
					try {
						docDao=new DocumentsAccessDao(db);
						da = docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
						LogSystem.info(request, "Document ID : " + documentID);
						LogSystem.info(request, "ID Mitra : " + idmitra);
						LogSystem.info(request, "User Mail : " + userEmail);
						LogSystem.info(request, "Get Id : " + doc.getId());
						
						//System.out.println("Document ID : " + documentID);
						//System.out.println("ID Mitra : " + idmitra);
						//System.out.println("User Mail : " + userEmail);
						//System.out.println("Get Id : " + doc.getId());
					}catch(Exception e)
					{
						//System.out.println("Errorrnya disiniiiiiiiiiiiii : ");
						LogSystem.info(request, "Errorrnya disiniiiiiiiiiiiii : ");
						e.printStackTrace();
					}
					if(da.size()<1) {
						LogSystem.error(request, "Error nih da.size() error < dari 1 masuk 404");
						//System.out.println("Error nih da.size() error < dari 1 masuk 404");
						 JSONObject jo=new JSONObject();
		    			 jo.put("result", "05");
		    			 jo.put("notif", "user tidak masuk penandatangan dokumen ini");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		    			 
		        		return;
					}
					
		    		SigningSession signSes=new SigningSession();
		    		Date current=new Date();
		    		Date expire=new Date(current.getTime()+(1000*5));
		    		signSes.setCreate_time(current);
		    		signSes.setExpire_time(expire);
		    		signSes.setEeuser(usr);
		    		signSes.setMitra(useradmin.getMitra());
		    		String dat=current.getTime()+";"+usr.getNick()+";"+da.get(0).getDocument().getId();
		    		String sesKey=id.co.keriss.consolidate.util.SHA256.shaHex(dat);
		    		signSes.setSession_key(sesKey);
		    		signSes.setUsed(false);
		    		
		    		DocSigningSession dcSgn=new DocSigningSession();
		    		dcSgn.setDocument(da.get(0).getDocument());
		    		dcSgn.setSigningSession(signSes);
		    		
		    		Session ss=db.session();
		    		Transaction t=ss.beginTransaction();
		    		Long idSes=(Long) ss.save(signSes);
		    		ss.save(dcSgn);
		    		t.commit();
		    		
		    		JSONObject link=new JSONObject();
					 link.put("sessionKey", sesKey);
					 link.put("sessionID", idSes.toString());
					 
					 JSONObject jo=new JSONObject();
					 jo.put("result", "00");
					 jo.put("link", "https://wvapi.tandatanganku.com/signingpage.html?sgn="+URLEncoder.encode(AESEncryption.encryptId(link.toString()),"UTF-8"));
				 
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		    		
					//System.out.println(dataUser.toString());
//					LogSystem.info(request, "Data User" + dataUser.toString());
//					context.put("locSign", dataUser);
//					context.put("by_pass_click", by_pass_click);
//					context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
//					context.put("title_doc", da.get(0).getDocument().getFile_name());
//					context.put("usersign", dataUser);
//					context.put("statusSign", statusSign);
//					context.put("visible", visible);
//					context.put("size", uL.length());
//					context.put("username", login.getUsername());
//					context.put("email", usr.getNick().toLowerCase());
//				    LogSystem.info(request, "Open Document ID : "+idDoc);
				}
				else {
					 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "user tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					return;
				}
        
	         
	         context.put("link", "https://"+LINK);
	         context.put("domain", "https://"+DOMAINAPI);
	         context.put("domainwv", "https://"+DOMAINAPIWV);
	         context.put("webdomain", "https://"+DOMAIN);


		}catch (Exception e) {
            LogSystem.error(getClass(), e);

            e.printStackTrace();
            try {  
			 JSONObject jo=new JSONObject();
			 jo.put("result", "05");
			 jo.put("notif", "error");
			 jo.put("error", e.getMessage());
			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
      
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
	}	
		
		
}
