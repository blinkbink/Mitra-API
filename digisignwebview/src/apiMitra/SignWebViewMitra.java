//package apiMitra;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.ApiVerification;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.InitialDao;
//import id.co.keriss.consolidate.dao.LoginDao;
//import id.co.keriss.consolidate.dao.TokenMitraDao;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.Initial;
//import id.co.keriss.consolidate.ee.Mitra;
//import id.co.keriss.consolidate.ee.TokenMitra;
//import id.co.keriss.consolidate.util.AESEncryption;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.LogSystem;
//import java.io.File;
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import javax.servlet.http.HttpServletRequest;
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.commons.io.FileUtils;
//import org.bouncycastle.util.encoders.Base64;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//
//public class SignWebViewMitra extends ActionSupport implements DSAPI{
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
//		FileItem filedata=null;
//		String filename=null;
//		int statusSign=0;
//		Boolean by_pass_click=false;
//		List <FileItem> fileSave=new ArrayList<FileItem>() ;
//		List<FileItem> fileItems=null;
//		//System.out.println("DATA DEBUG :"+(i++));
//		LogSystem.info(request, "DATA DEBUG : " + (i++),"");
//		Mitra mitratoken=null;
//		String refTrx="";
//		User useradmin=null;
//		try{
//				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//				
//				TokenMitraDao tmd=new TokenMitraDao(getDB(context));
//				String token=request.getHeader("authorization");
//				if(token!=null) {
//					String[] split=token.split(" ");
//					if(split.length==2) {
//						if(split[0].equals("Bearer"))token=split[1];
//					}
//					TokenMitra tm=tmd.findByToken(token.toLowerCase());
//					//System.out.println("token adalah = "+token);
//					if(tm!=null) {
//						LogSystem.info(request, "Token tidak null","");
//						mitratoken=tm.getMitra();
//					} else {
//						LogSystem.info(request, "Token null","");
//						JSONObject jo=new JSONObject();
//						jo.put("res", "05");
//						jo.put("notif", "token salah");
//						context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//						
////						PrintWriter outStream=context.getResponse().getWriter();
////		        		outStream.write(403);
////		        		outStream.close();
//						context.getResponse().sendError(403);
//						return;
//					}
//				}
//
//				// no multipart form
//				if (!isMultipart) {
//					//System.out.println("bukan multipart");
//					LogSystem.info(request, "Bukan multipart","");
//					jsonString=request.getParameter("jsonfield");
//				}
//				// multipart form
//				else {
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
//							
//						}
//						
//					}
//				}
//			 String process=request.getRequestURI().split("/")[2];
//	         //System.out.println("PATH :"+request.getRequestURI());
//	         LogSystem.info(request, "PATH :"+request.getRequestURI(),"");
//
//	         LogSystem.request(request, fileItems, refTrx);
//			 if(jsonString==null) return;	         
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         LogSystem.info(request, "JSONFile Receive : "+jsonRecv,"");
//	         
//	         if(mitratoken!=null) {
//	        	 String userid=jsonRecv.getString("userid");
//		         UserManager user=new UserManager(getDB(context));
//		         //User eeuser=user.findByUsername(userid);
//		         useradmin=user.findByUsername(userid);
//		         if(useradmin!=null) {
//		        	 if(useradmin.getMitra().getId()==mitratoken.getId() && useradmin.isAdmin()) {
//		        		 //System.out.println("token dan mitra valid");
//		        		 LogSystem.info(request, "Token dan mitra valid","");
//		        	 }
//		        	 else {
//		        		 LogSystem.info(request, "Token dan mitra tidak valid","");
//		        		 JSONObject jo=new JSONObject();
//						 jo.put("res", "05");
//						 jo.put("notif", "Token dan Mitra tidak sesuai");
//						 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//
//			        	 context.getResponse().sendError(403);
//						 return;
//		        	 }
//		         }
//		         else {
//		        	 LogSystem.info(request, "Userid tidak ditemukan","");
//		        	 JSONObject jo=new JSONObject();
//					 jo.put("res", "05");
//					 jo.put("notif", "userid tidak ditemukan");
//					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//
//                     context.getResponse().sendError(403);
//					 return;
//		         }
//	         }
//	         
//	         //JSONObject jo = null;
//	        
//	         //DownloadDocMitra(jsonRecv, request, context);
//	         DB db = getDB(context);
//	         ApiVerification aVerf = new ApiVerification(db);
//	         boolean vrf=false;
//		        if(mitratoken!=null && useradmin!=null) {
//		        	vrf=true;
//		        }
//		        else {
//		        	vrf=aVerf.verification(jsonRecv);
//		        }
//	         if(vrf){
//	        	 User usermitra=null;
//		        	Mitra mitra=null;
//		        	if(mitratoken==null && useradmin==null) {
//			        	if(aVerf.getEeuser().isAdmin()==false) {
//
//			        		context.getResponse().sendError(403);
//			        		return;
//			        	}
//			        	usermitra = aVerf.getEeuser();
//			    		mitra = usermitra.getMitra();
//		        	}
//		        	else {
//		        		usermitra=useradmin;
//		        		mitra=mitratoken;
//		        	}
//		     
//	        	 	
//					String documentID=jsonRecv.getString("document_id");
//					DocumentsDao dd = new DocumentsDao(db);
//					LoginDao ldao=new LoginDao(db);
//					//Documents doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
//					Documents doc=null;
//					List<Documents> ldocs;
//					try {
//						LogSystem.info(request, "ID MITRA : ----- " + mitra.getId(),"");
//						LogSystem.info(request, "DOCUMENT ID : ----- " + documentID,"");
//
//						ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
//						if(ldocs.size()==0) {
//							//System.out.println("Error size 0 : ----- ");
//							LogSystem.info(request, "Error size 0","");
//							context.getResponse().sendError(404);
//				        	return;
//						}
//						else {
//							LogSystem.info(request, "Size tidak 0","");
//							//System.out.println("Success size not 0 : ----- ");
//							doc=ldocs.get(0);
//						}
//					} catch (Exception e) {
//						LogSystem.error(request, "Error dokumen","");
//						e.printStackTrace();
//						// TODO: handle exception
//						context.getResponse().sendError(404);
//			        	return;
//					}
//					
//					String idDoc=null;
//					String userEmail=jsonRecv.getString("email_user");
//					Long idmitra = usermitra.getId();
//					UserManager um=new UserManager(db);
//					LogSystem.info(request, "User Email: " + userEmail,"");
//					//System.out.println(userEmail);
//					User usr= um.findByUsername(userEmail);
//					
//					if(jsonRecv.has("by-pass-click"))
//					{
//						by_pass_click = jsonRecv.getBoolean("by-pass-click");
//					}
//					
//					//System.out.println("user nya adalah = "+usr.getName());
//					if(usr!=null) {
//						//System.out.println("USR NICK ------------ : " + usr.getNick());
//						LogSystem.info(request, "User Nick: " + userEmail,"");
//						id.co.keriss.consolidate.ee.Login login = null;
//						
//						try {
//							 login=ldao.getUsername(usr.getNick());
//						}
//						catch(Exception e)
//						{
//							//System.out.println(e);
//							LogSystem.error(request, "Error : " + e,"");
//							e.printStackTrace();
//						}
//						
//						if(login==null) {
////							PrintWriter outStream=context.getResponse().getWriter();
////			        		outStream.write(403);
////			        		outStream.close();
//							//System.out.println("Error nih di 403 login == null");
//							LogSystem.info(request, "Error nih di 403 login == null","");
//			        		context.getResponse().sendError(403);
//			        		return;
//						}
//						
//						List<DocumentAccess> da  = null;
//						DocumentsAccessDao docDao = null;
//						
//						try {
//							docDao=new DocumentsAccessDao(db);
//							da = docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
//							LogSystem.info(request, "Document ID : " + documentID,"");
//							LogSystem.info(request, "ID Mitra : " + idmitra,"");
//							LogSystem.info(request, "User Mail : " + userEmail,"");
//							LogSystem.info(request, "Get Id : " + doc.getId(),"");
//							
//							//System.out.println("Document ID : " + documentID);
//							//System.out.println("ID Mitra : " + idmitra);
//							//System.out.println("User Mail : " + userEmail);
//							//System.out.println("Get Id : " + doc.getId());
//						}catch(Exception e)
//						{
//							//System.out.println("Errorrnya disiniiiiiiiiiiiii : ");
//							LogSystem.info(request, "Errorrnya disiniiiiiiiiiiiii : ","");
//							e.printStackTrace();
//						}
//						if(da.size()<1) {
//							LogSystem.error(request, "Error nih da.size() error < dari 1 masuk 404","");
//							//System.out.println("Error nih da.size() error < dari 1 masuk 404");
//							context.getResponse().sendError(404);
//			        		return;
//						}
//						
//						
//						String id=AESEncryption.encryptDoc(String.valueOf(da.get(0).getDocument().getEeuser().getId()));
//						String namafile=AESEncryption.encryptDoc(da.get(0).getDocument().getSigndoc());
//						
//						DocumentAccess dacc=da.get(0);
//						if(dacc.getEeuser()==null) {
//							dacc.setEeuser(usr);
//							docDao.update(dacc);
//						}
//						
//						
//						context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
//						//System.out.println("https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
//						//System.out.println("https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile);
//						LogSystem.info(request, "https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile,"");
//						
//						File img=new File(usr.getUserdata().getImageTtd());
//						//File img=new File(usr.getI_ttd());
//						byte[] encoded = Base64.encode(FileUtils.readFileToByteArray(img));
//
//						int visible=1;
//						JSONObject dataUser=new JSONObject();
//						//dataUser.put("idDoc", idDoc);
//						JSONArray uL=new JSONArray();
//						for(DocumentAccess dAcs:da) {
//							idDoc = String.valueOf(dAcs.getDocument().getId());
//							
//							if( ((dAcs.getType().equals("sign") || dAcs.getType().equals("initials")) && dAcs.isFlag()==true)) {
//								statusSign=1;
//								continue;
//							}
//							if(!dAcs.isRead()) {
//								dAcs.setRead(true);
//								docDao.update(dAcs);
//							}
//							if(dAcs.getType().equals("share")) {
//								continue;
//							}
//							
//							if(dAcs.getType().equalsIgnoreCase("initials")) {
//								String sgn="0";
//								if(!dAcs.isVisible()) {
//									visible=1;
//									sgn="1";
//								}
//								InitialDao idao=new InitialDao(db);
//								List<Initial> li=idao.findInitialByDocac(dAcs.getId());
//								for(Initial in:li) {
//									JSONObject us=new JSONObject();
//									us.put("idAccess", dAcs.getId().toString());
//									us.put("lx", in.getLx());
//									us.put("ly", in.getLy());
//									us.put("rx", in.getRx());
//									us.put("ry", in.getRy());
//									us.put("page",in.getPage());
//									us.put("type", dAcs.getType());
//									us.put("sgn", sgn);
//									uL.put(us);
//								}
//								continue;
//							}
//							
//							JSONObject us=new JSONObject();
//							us.put("idAccess", dAcs.getId().toString());
//							us.put("lx", dAcs.getLx());
//							us.put("ly", dAcs.getLy());
//							us.put("rx", dAcs.getRx());
//							us.put("ry", dAcs.getRy());
//							us.put("page",dAcs.getPage());
//							us.put("type", dAcs.getType());
//							us.put("sgn", "0");
//							
//							if(!dAcs.isVisible()) {
//								visible=0;
//								us.put("sgn", "1");
//							}
//							uL.put(us);
//							
//							
//						}
//						
//						dataUser.put("idDoc", idDoc);
//						dataUser.put("user", uL);
//						
//						Random rand = new Random(); 
//						int value = rand.nextInt(50000); 
//						context.put("rand", value);
//						
//						//System.out.println(dataUser.toString());
//						LogSystem.info(request, "Data User" + dataUser.toString(),"");
//						context.put("locSign", dataUser);
//						context.put("by_pass_click", by_pass_click);
//						context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
//						context.put("title_doc", da.get(0).getDocument().getFile_name());
//						context.put("usersign", dataUser);
//						context.put("statusSign", statusSign);
//						context.put("visible", visible);
//						context.put("size", uL.length());
//						context.put("username", login.getUsername());
//						context.put("email", usr.getNick());
//					    LogSystem.info(request, "Open Document ID : "+idDoc,"");
//					}
//					else {
//
//						LogSystem.error(request, "User null","");
//			        	context.getResponse().sendError(401); 
//						return;
//					}
//	         } 
//	         else {
//	        	 //System.out.println("lemparan dari verify");
//	        	 LogSystem.error(request, "Lemparan dari verify","");
//	        	 context.getResponse().sendError(404);
//	        	 return;
//	         }
//	         
//	         context.put("domain", "https://"+DOMAINAPI);
//	         context.put("domainwv", "https://"+DOMAINAPIWV);
//
//
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
//
//            e.printStackTrace();
//            try {
//				context.getResponse().sendError(400);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		}
//	}	
//		
//		
//}
