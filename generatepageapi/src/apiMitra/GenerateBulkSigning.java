package apiMitra;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.SmartphoneVerification;
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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class GenerateBulkSigning extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date idTrx=new Date();

		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String stringDate = sdfDate2.format(idTrx).toString();
		String refTrx="GEN-BULKSIGNING-PAGE"+stringDate;
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
//		LogSystem.info(request, "DATA DEBUG : " + (i++));
		Mitra mitratoken=null;
		User useradmin=null;
		
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				TokenMitraDao tmd = null;
				TokenMitra tm = null;
				String token = null;
				try {
					tmd=new TokenMitraDao(getDB(context));
					token=request.getHeader("authorization");
					if(token!=null) {
						String[] split=token.split(" ");
						if(split.length==2) {
							if(split[0].equals("Bearer"))token=split[1];
						}
					}
					tm=tmd.findByToken(token.toLowerCase());
				}catch(Exception e)
				{
					JSONObject jo=new JSONObject();
					jo.put("result", "91");
					jo.put("notif", "System timeout. silahkan coba kembali.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.error(request, e.toString(), refTrx);
					return;
				}
					
				if(tm!=null) {
					LogSystem.info(request, "Token ada", refTrx);
					mitratoken=tm.getMitra();
				} else {
					LogSystem.error(request, "Token tidak ada", refTrx);
					JSONObject jo=new JSONObject();
					jo.put("result", "55");
					jo.put("notif", "token salah");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					LogSystem.info(request, jo.toString(), refTrx);
					return;
				}

				// no multipart form
				if (!isMultipart) {
					//System.out.println("bukan multipart");
//					LogSystem.info(request, "Bukan multipart");
					jsonString=request.getParameter("jsonfield");
		            LogSystem.info(request, "Bukan multipart", refTrx);

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
//	         LogSystem.info(request, "PATH :"+request.getRequestURI());

	         LogSystem.request(request, fileItems, refTrx);
			 if(jsonString==null) return;	         
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         LogSystem.info(request, jsonRecv.toString(), refTrx);
	         
	         if(mitratoken!=null) {
	        	 
	        	 if (!jsonRecv.has("userid"))
	        	 {
//		    		 LogSystem.error(request, "Parameter userid tidak ditemukan");
		    		 JSONObject jo=new JSONObject();
						jo.put("result", "05");
						jo.put("notif", "userid salah");
						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			            LogSystem.info(request, "Bukan multipart", refTrx);
						return;
	        	 }
	        	 
	        	 String userid=jsonRecv.getString("userid").toLowerCase();
	        	 
		         UserManager user=new UserManager(getDB(context));
		         useradmin=user.findByUsername(userid);
		         if(useradmin!=null) {
		        	 if(useradmin.getMitra().getId()==mitratoken.getId() && useradmin.isAdmin()) {

		        	 }
		        	 else {
		        		 JSONObject jo=new JSONObject();
						 jo.put("result", "05");
						 jo.put("notif", "Token dan Mitra tidak sesuai");
						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				         LogSystem.info(request,  new JSONObject().put("JSONFile", jo).toString(), refTrx);
						 return;
		        	 }
		         }
		         else {
		        	 JSONObject jo=new JSONObject();
					 jo.put("result", "05");
					 jo.put("notif", "userid tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			         LogSystem.info(request,  new JSONObject().put("JSONFile", jo).toString(), refTrx);
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
			         LogSystem.info(request,  new JSONObject().put("JSONFile", jo).toString(), refTrx);
				 return;

		        }

		        User usermitra=useradmin;
	        	Mitra mitra=useradmin.getMitra();
				 JSONObject jo=new JSONObject();

	     
        	 	
				List<String> documentID=new ArrayList<String>();
				JSONArray ja=jsonRecv.getJSONArray("document_id");
				for(int x=0;x<ja.length();x++) {
					documentID.add(ja.getString(x));
				}
				
				String idDoc=null;
				String userEmail=jsonRecv.getString("email_user").toLowerCase();
				Long idmitra = usermitra.getId();
				UserManager um=new UserManager(db);

	            LogSystem.info(request, "User Email: " + userEmail, refTrx);
	            
				User usr= um.findByUsername(userEmail);
				
				if(jsonRecv.has("by-pass-click"))
				{
					by_pass_click = jsonRecv.getBoolean("by-pass-click");
				}
				
				if(usr!=null) {
					
					
					//check level user < 3 dan level mitra > 2
					int leveluser=Integer.parseInt(usr.getUserdata().getLevel().substring(1));
		        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
		        	if(levelmitra>2 && leveluser<3) {
		    			 jo.put("result", "05");
		    			 jo.put("notif", "level user tidak diperbolehkan untuk mitra ini");
		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
 			           LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
		    			 return;
		        	}
					
					List<Documents> ldoc  = null;
					DocumentsDao docDao = null;
					List<Documents> ceklDoc=null;
					try {
						docDao=new DocumentsDao(db);
 			           LogSystem.info(request, jsonRecv.toString(), refTrx);
 			            ldoc = docDao.findDocForSigningByMitraWithSeqNative(documentID, idmitra, userEmail,useradmin.getId());

 			           LogSystem.info(request, "end query", refTrx);
						ceklDoc = new ArrayList(ldoc);
						
						List<String> cekSdhttd  = new ArrayList();
						boolean cekSeq=true;
						for (String documents : documentID) {
							boolean cekDoc=false;
							Iterator daIt=ceklDoc.iterator();
							while (daIt.hasNext()) {
								Documents docChk=(Documents) daIt.next();
								if(docChk.getIdMitra().equals(documents)) {
									//daIt.remove(); // tidak perlu dihapus
									cekDoc=true;
									break;
								}
							}
							
							if(!cekDoc) {
								cekSdhttd.add(documents);
								cekSeq=false;
							}
						}
						if(cekSeq==false) {
							List<String> hvSigned=docDao.findDocHaveSigned(cekSdhttd, idmitra, userEmail,mitra.getId());
			    			 jo.put("result", "05");
			    			 JSONArray docs=new JSONArray();
			    			 for (String documents : cekSdhttd) {
			    				 JSONObject dataDoc=new JSONObject();
			    				 boolean signed=false;
								 Iterator daIt=hvSigned.iterator();
			    				 while (daIt.hasNext()) {
			    					 String dc=(String) daIt.next();
									if(dc.equals(documents)) {
										signed=true;
										daIt.remove();
										break;
									}
								}
			    				 
			    				if(signed) {
			    					dataDoc.put("document_id", documents);
			    					dataDoc.put("notif", "user sudah menandatangani dokumen ini");

			    				}else {
			    					List<String> docCh=new ArrayList<>();
			    					docCh.add(documents);
			    					List l=docDao.findDocForSigningByMitra(docCh, idmitra, userEmail);
			    					dataDoc.put("document_id", documents);
			    					if(l.size()>0) {
			    						dataDoc.put("notif", "user belum waktunya menandatangani dokumen ini");
			    					}else {
			    						dataDoc.put("notif", "dokumen tidak ditemukan");
			    					}

			    				}
			    				docs.put(dataDoc);
			    			 } 
			    			 jo.put("documents", docs);

						}

			            LogSystem.info(request,  "Document ID : " + documentID, refTrx);
			            LogSystem.info(request,  "ID Mitra : " + idmitra, refTrx);
			            LogSystem.info(request,  "User Mail : " + userEmail, refTrx);
					}catch(Exception e)
					{
						e.printStackTrace();
						LogSystem.error(request, e.toString(), refTrx);
					}
					if(ceklDoc.size()==0) {
		    			 jo.put("result", "05");
		    			 if(!jo.has("documents"))jo.put("notif", "dokumen tidak ditemukan");

		    			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		    			 LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
		        		return;
					}
					
		    		SigningSession signSes=new SigningSession();
		    		Date current=new Date();
		    		Date expire=new Date(current.getTime()+(1000*20*60));
		    		signSes.setCreate_time(current);
		    		signSes.setExpire_time(expire);
		    		signSes.setEeuser(usr);
		    		signSes.setMitra(useradmin.getMitra());
		    		String dat=current.getTime()+";"+usr.getNick()+"";
		    		String sesKey=id.co.keriss.consolidate.util.SHA256.shaHex(dat);
		    		signSes.setSession_key(sesKey);
		    		signSes.setUsed(false);
		    		
		    		
		    		Session ss=db.session();
		    		Transaction t=ss.beginTransaction();
		    		Long idSes=(Long) ss.save(signSes);
		    		String docLast="";
		    		for(Documents dc : ceklDoc) {
		    			if(!docLast.equals(dc)) {
				    		DocSigningSession dcSgn=new DocSigningSession();
				    		dcSgn.setDocument(dc);
				    		dcSgn.setSigningSession(signSes);
				    		docLast=dc.getIdMitra();
				    		try {
				    			ss.save(dcSgn);
				    		}catch (Exception e) {
				    			
				    			e.getMessage();
				    		}
		    			}
		    		}
		    		t.commit();
		    		
		    		JSONObject link=new JSONObject();
					 link.put("sessionKey", sesKey);
					 link.put("sessionID", idSes.toString());
					 link.put("refTrx", stringDate);
					 jo.put("result", "00");
					 jo.put("link", "https://"+DOMAINAPIWV+"/bulksigningpage.html?sgn="+URLEncoder.encode(AESEncryption.encryptId(link.toString()),"UTF-8"));
				 
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			         LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
				}
				else {
					 jo.put("result", "05");
					 jo.put("notif", "user tidak ditemukan");
					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
		            LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);
					return;
				}
        
	         
	         context.put("link", "https://"+LINK);
	         context.put("domain", "https://"+DOMAINAPI);
	         context.put("domainwv", "https://"+DOMAINAPIWV);
	         context.put("webdomain", "https://"+DOMAIN);


		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
            LogSystem.error(request, e.toString(), refTrx);
            e.printStackTrace();
            try {  
			 JSONObject jo=new JSONObject();
			 jo.put("result", "05");
			 jo.put("notif", "error");
			 jo.put("error", e.getMessage());
			 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			 LogSystem.info(request, new JSONObject().put("JSONFile", jo).toString(), refTrx);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
	}	
		
		
}
