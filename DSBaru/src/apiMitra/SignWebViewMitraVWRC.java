package apiMitra;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.InitialDao;
import id.co.keriss.consolidate.dao.LoginDao;
import id.co.keriss.consolidate.dao.TmpUsernameDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TMPUsername;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class SignWebViewMitraVWRC extends ActionSupport implements DSAPI{

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx=null;
	String kelas="apiMitra.SignWebViewMitraVWRC";
	String trxType="GEN-SGN";
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="SGN"+sdfDate2.format(tgl).toString();
		
		Random rand = new Random();
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		byte[] dataFile=null;
		FileItem filedata=null;
		String filename=null;
		int statusSign=0;
		List <FileItem> fileSave=new ArrayList<FileItem>() ;
		List<FileItem> fileItems=null;
		Boolean by_pass_click=false;
		
		Mitra mitra=null;
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				PrintWriter outStream = null;
				try {
					outStream = context.getResponse().getWriter();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				TokenMitraDao tmd=new TokenMitraDao(getDB(context));
				String token=request.getHeader("authorization");
				if(token!=null) {
					String[] split=token.split(" ");
					if(split.length==2) {
						if(split[0].equals("Bearer"))token=split[1];
					}
				}
				TokenMitra tm=tmd.findByToken(token.toLowerCase());
				//System.out.println("token adalah = "+token);
				if(tm!=null) {
					LogSystem.info(request, "Token tidak null : " +  token,kelas, refTrx, trxType);
					mitra=tm.getMitra();
				} else {
					LogSystem.error(request, "Token null",kelas, refTrx, trxType);
					context.put("error", "Token tidak sesuai.");
					context.getResponse().setCharacterEncoding("Token tidak sesuai.");;
					context.getResponse().sendError(403);
					return;
				}

				// no multipart form
				if (!isMultipart) {
					//System.out.println("bukan multipart");
					LogSystem.info(request, "Bukan multipart",kelas, refTrx, trxType);
					jsonString=request.getParameter("jsonfield");

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
						
					}
				}
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(),kelas, refTrx, trxType);

	         LogSystem.request(request, fileItems, kelas, refTrx, trxType);
	         //LogSystem.request(request, fileItems);
	         //System.out.println("sampai LOGGGGGGG");
	         LogSystem.info(request, "sampai LOGGGGGGG",kelas, refTrx, trxType);
	         
			 if(jsonString==null) {
				 LogSystem.error(request, "Format json salah",kelas, refTrx, trxType);
				 context.getResponse().sendError(403);
				 return;     
			 }
			 
			 LogSystem.info(request, "jsonstring= "+jsonString,kelas, refTrx, trxType);
			 //System.out.println("jsonstring= "+jsonString);
	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
	         
	         if (!jsonRecv.has("userid"))
        	 {
	    		 LogSystem.error(request, "Parameter userid tidak ditemukan",kelas, refTrx, trxType);
        		 context.getResponse().sendError(403);
        		 return;
        	 }
	         
	         String userid=jsonRecv.getString("userid").toLowerCase();
	         
	         LogSystem.info(request, "sampai siniiiiiiiiiiiiiiiii",kelas, refTrx, trxType);
	         //System.out.println("sampai siniiiiiiiiiiiiiiiii");
	         UserManager user=new UserManager(getDB(context));
	         User eeuser=user.findByUsername(userid);
	         if(eeuser!=null) {
	        	 if(eeuser.getMitra().getId()==mitra.getId() && eeuser.isAdmin()) {
	        		 LogSystem.info(request, "Token dan mitra valid",kelas, refTrx, trxType);
	        		 //System.out.println("token dan mitra valid");
	        	 }
	        	 else {
//	        		 JSONObject jo=new JSONObject();
//					 jo.put("res", "05");
//					 jo.put("notif", "Token dan Mitra tidak sesuai");
//					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//					
//					 return;
	        		 //outStream.write(403);
					 //outStream.close();
	        		 //sendRedirect(context, "/403.html");
	        		 LogSystem.error(request, "Token dan mitra tidak valid",kelas, refTrx, trxType);
	        		 context.getResponse().sendError(403);
	        		 return;
	        	 }
	         }
	         else {
	        	 LogSystem.error(request, "Userid tidak ditemukan",kelas, refTrx, trxType);
	        	 context.getResponse().sendError(403);
				 return;
	         }
	         
	         
	         String email_user=jsonRecv.getString("email_user").toLowerCase();
	         /*
	         String pwd_user=jsonRecv.getString("pwd_user");
	         EeuserMitraDao emdao=new EeuserMitraDao(getDB(context));
	         EeuserMitra em=emdao.findUserPwdMitra(email_user, pwd_user, mitra.getId());
	         if(em==null) {
	        	 JSONObject jo=new JSONObject();
				 jo.put("res", "05");
				 jo.put("notif", "user atau password nasabah salah");
				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
				 
				 //PrintWriter outStream=context.getResponse().getWriter();
	        	 //outStream.write(401);
	        	 //outStream.close();
				 context.getResponse().sendError(401);
				 return;
	         }
	         */
	         JSONObject jo = null;
	        
	         //DownloadDocMitra(jsonRecv, request, context);
	         DB db = getDB(context);
	         /*
	         ApiVerification aVerf = new ApiVerification(db);
	         if(aVerf.verification(jsonRecv)){
	        	 if(aVerf.getEeuser().isAdmin()==false) {
		        		PrintWriter outStream=context.getResponse().getWriter();
		        		outStream.write(403);
		        		outStream.close();
		        		return;
	        	 }
	        	 //HttpSession session = request.getSession();
	        	 //User userTrx= new UserManager(db).findByUsername(jsonRecv.getString("user"));	
		            //session.setAttribute (USER, userTrx);
	        	 	User usermitra = aVerf.getEeuser();
	        	 	//Mitra mitra = usermitra.getMitra();
	        */
	         User usermitra=eeuser;
	         
					String documentID=jsonRecv.getString("document_id");
					DocumentsDao dd = new DocumentsDao(db);
					LoginDao ldao=new LoginDao(db);
					Documents doc=null;
					List<Documents> ldocs;
					try {
						//doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
						//ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
						ldocs=dd.findByDocIdMitraDelFalse(documentID, mitra.getId());
						if(ldocs.size()==0) {
							LogSystem.error(request, "Dokumen size 0",kelas, refTrx, trxType);
							context.getResponse().sendError(404);
				        	return;
						}
						else {
							doc=ldocs.get(0);
							boolean delete=doc.getDelete();
							if(delete==true) {
								LogSystem.info(request, "dokumen delete = true",kelas, refTrx, trxType);
								context.getResponse().sendError(404);
					        	return;
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						LogSystem.error(request, "Error dokumen",kelas, refTrx, trxType);
						context.getResponse().sendError(404);
			        	return;
					}
					
					String idDoc=null;
					String userEmail=jsonRecv.getString("email_user").toLowerCase();
					Long idmitra = usermitra.getId();
					UserManager um=new UserManager(db);
					LogSystem.info(request, "User Email : " + userEmail ,kelas, refTrx, trxType);
					//System.out.println(userEmail);
					User usr= um.findByUsernameSign(userEmail);
					
					//System.out.println("user nya adalah = "+usr.getName());
					if(usr!=null) {
						/*
						id.co.keriss.consolidate.ee.Login login=ldao.getUsername(usr.getNick());
						if(login==null) {
							PrintWriter outStream=context.getResponse().getWriter();
			        		outStream.write(403);
			        		outStream.close();
			        		return;
						}
						*/
						LogSystem.info(request, "User tidak null",kelas, refTrx, trxType);
						
						//check level user < 3 dan level mitra > 2
						int leveluser=Integer.parseInt(usr.getUserdata().getLevel().substring(1));
			        	int levelmitra=Integer.parseInt(mitra.getLevel().substring(1));
			        	if(levelmitra>2 && leveluser<3) {
			        		LogSystem.error(request, "level user tidak diperbolehkan untuk mitra ini",kelas, refTrx, trxType);
			        		context.getResponse().sendError(403);
			        		return;
			        	}
						
						DocumentsAccessDao docDao=new DocumentsAccessDao(db);
						List<DocumentAccess> lda=docDao.findByDocAndEmail(String.valueOf(doc.getId()), userEmail);
						if(lda.size()==0||lda==null) {
							LogSystem.info(request, "User tidak ada di daftar tandatangan",kelas, refTrx, trxType);
							context.getResponse().sendError(401);
				        	
							return;
						}
						List<DocumentAccess> da =docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
						if(da.size()==0||da==null) {
							statusSign=1;
						}
						
						//System.out.println("TES SSSSSSSSSSSSSSSSSSS :"+usr.getUserdata().getImageTtd());
//						String id=AESEncryption.encryptDoc(String.valueOf(da.get(0).getDocument().getEeuser().getId()));
//						String namafile=AESEncryption.encryptDoc(da.get(0).getDocument().getSigndoc());
						String id=AESEncryption.encryptDoc(String.valueOf(doc.getEeuser().getId()));
						String namafile=AESEncryption.encryptDoc(doc.getSigndoc());
						//String docid=AESEncryption.encryptDoc(String.valueOf(da.get(0).getDocument().getId()));
						String docid=AESEncryption.encryptDoc(String.valueOf(doc.getId()));
						LogSystem.info(request, "id = " + id,kelas, refTrx, trxType);
						LogSystem.info(request, "namafile = " + namafile,kelas, refTrx, trxType);
						//System.out.println("id="+id);
						//System.out.println("namafile="+namafile);
						
						//context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
						context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8")+"&access="+URLEncoder.encode(docid, "UTF-8")+"&refTrx="+URLEncoder.encode(refTrx, "UTF-8"));
						//System.out.println("https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
						//System.out.println("https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile);
						LogSystem.info(request, "Link = " + "https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile,kelas, refTrx, trxType);
						//File img=new File(usr.getUserdata().getImageTtd());
						//File img=new File(usr.getI_ttd());
						
						//byte[] encoded = Base64.encode(FileUtils.readFileToByteArray(img));
						SaveFileWithSamba samba=new SaveFileWithSamba();
						byte[] ttd=samba.openfile(usr.getUserdata().getImageTtd());
						byte[] encoded = Base64.encode(ttd);

						int visible=1;
						JSONObject dataUser=new JSONObject();
						//dataUser.put("idDoc", idDoc);
						JSONArray uL=new JSONArray();
						for(DocumentAccess dAcs:da) {
							try {
								if(dAcs.getEeuser()==null) {
									dAcs.setEeuser(usr);
									docDao.update(dAcs);
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
							
							idDoc = String.valueOf(dAcs.getDocument().getId());
							
							if( ((dAcs.getType().equals("sign") || dAcs.getType().equals("initials")) && dAcs.isFlag()==true)) {
								statusSign=1;
								
								continue;
							}
							if(!dAcs.isRead()) {
								dAcs.setRead(true);
								docDao.update(dAcs);
							}
							if(dAcs.getType().equals("share")) {
								continue;
							}
							
							if(dAcs.getType().equalsIgnoreCase("initials")) {
								String sgn="0";
								if(!dAcs.isVisible()) {
									visible=1;
									sgn="1";
								}
								InitialDao idao=new InitialDao(db);
								List<Initial> li=idao.findInitialByDocac(dAcs.getId());
								for(Initial in:li) {
									JSONObject us=new JSONObject();
									us.put("idAccess", dAcs.getId().toString());
									us.put("lx", in.getLx());
									us.put("ly", in.getLy());
									us.put("rx", in.getRx());
									us.put("ry", in.getRy());
									us.put("page",in.getPage());
									us.put("type", dAcs.getType());
									us.put("sgn", sgn);
									us.put("key", AESEncryption.encryptDoc(String.valueOf(dAcs.getId())));
									uL.put(us);
								}
								continue;
							}
							
							JSONObject us=new JSONObject();
							us.put("idAccess", dAcs.getId().toString());
							us.put("lx", dAcs.getLx());
							us.put("ly", dAcs.getLy());
							us.put("rx", dAcs.getRx());
							us.put("ry", dAcs.getRy());
							us.put("page",dAcs.getPage());
							us.put("type", dAcs.getType());
							us.put("sgn", "0");
							us.put("key", AESEncryption.encryptDoc(String.valueOf(dAcs.getId())));
							
							if(!dAcs.isVisible()) {
								visible=0;
								us.put("sgn", "1");
							}
							uL.put(us);
							
							
						}
						
						
						dataUser.put("idDoc", idDoc);
						dataUser.put("user", uL);
						dataUser.put("refTrx", refTrx);
						
						//System.out.println(dataUser.toString());
						LogSystem.info(request, "Data User = " + dataUser.toString(),kelas, refTrx, trxType);
						context.put("locSign", dataUser);
						context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
						context.put("title_doc", doc.getFile_name());
						context.put("usersign", dataUser);
						context.put("statusSign", statusSign);
						if(jsonRecv.has("by-pass-click"))
						{
							by_pass_click = jsonRecv.getBoolean("by-pass-click");
						}
						context.put("by_pass_click", by_pass_click);
						if(statusSign==1) {
							context.put("flag", true);
						} else {
							context.put("flag", false);
						}
						context.put("visible", visible);
						context.put("size", uL.length());
						//context.put("username", login.getUsername());
						if(usr.getLogin()!=null) {
							//System.out.println("login ada bro ");
							LogSystem.info(request, "Login tidak null",kelas, refTrx, trxType);
							context.put("username", usr.getLogin().getUsername());
						}
						context.put("email", usr.getNick().toLowerCase());
						//context.put("pwd_user", pwd_user);
						context.put("mitra", String.valueOf(mitra.getId()));
						context.put("statususer", usr.getStatus());
						context.put("nohp", usr.getUserdata().getNo_handphone());
						context.put("iduser", usr.getId());
						
						String username="";
						////////BUAT USERNAME///////////
						if(usr.getStatus()=='1' && usr.getLogin()==null) {
								
					        	LoginDao ldo = new LoginDao(db);
					        	TmpUsernameDao tmpuserDao = new TmpUsernameDao(db);
					        	Login dataLogin = new Login();
					        	TMPUsername tmpUserName = new TMPUsername();
					        	List<TMPUsername> cekUname = null;
					        	List<Login> cekLogin = null;
					        	User userNick = null;
					        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					        	Date now = new Date();
					        	String strDate = sdf.format(now);
					        	Date dateTime = sdf.parse(strDate);
					        	boolean hasiluname=false;
					        	//							
					        	userNick = um.findByEmailMitra2(jsonRecv.getString("email_user").toLowerCase().trim());
					        	String email = userNick.getNick();
					        	String kata1 = null;
					        	String kata2 = null;
					        	String user_name = null;
					        	String[] tokens = email.split("@");
					        	for (int j = 0; j < tokens.length; j++) {
					        		user_name = tokens[0].toLowerCase();
					        		kata1 = tokens[1].toLowerCase();
					        	}
					        	
					        	cekLogin = ldo.cekLogin2(user_name);
					        	int size=cekLogin.size()+1;
					        	if (cekLogin == null || cekLogin.isEmpty()) {
					        		 cekUname = tmpuserDao.cekUserName(user_name.toLowerCase().trim());
					        		 if (cekUname == null || cekUname.isEmpty()) {
					        		 	tmpUserName.setUsername(user_name.toLowerCase());
					        		 	tmpUserName.setDate_record(dateTime);
					        		 	dataLogin.setUsername(user_name.toLowerCase());
					        		 	dataLogin.setDate_record(dateTime);
					        		 	//Long idLogin = ldo.create(dataLogin);
					        		 	//Long idtmpUserName = tmpuserDao.create(tmpUserName);
					        		 	try {
					        		 		
						        		 	long tmp=tmpuserDao.create2(tmpUserName);
						        		 	LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
						        		 	if(tmp>0) {
						        		 		long g=ldo.create2(dataLogin);
						        		 		if(g>0) {
						        		 			usr.setLogin(dataLogin);
								        		 	um.update(usr);
								        		 	hasiluname=true;
								        		 	LogSystem.info(request, "berhasil create username = "+user_name, kelas, refTrx, trxType);
						        		 		}
							        		 	
						        		 	}
						        		 	
										} catch (Exception e) {
											// TODO: handle exception
											LogSystem.error(request, e.getMessage(), kelas, refTrx, trxType);
											LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
											
											String uname=user_name+size;
											List<Login> llogin=ldo.cekLogin(uname);
											if(llogin.size()>0) {
												hasiluname=false;
											} else {
												tmpUserName.setUsername(uname.toLowerCase());
							        		 	tmpUserName.setDate_record(dateTime);
							        		 	dataLogin.setUsername(uname.toLowerCase());
							        		 	dataLogin.setDate_record(dateTime);
							        		 	try {
							        		 		long tmp=tmpuserDao.create2(tmpUserName);
							        		 		LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
								        		 	if(tmp>0) {
								        		 		long g=ldo.create2(dataLogin);
								        		 		if(g>0) {
								        		 			usr.setLogin(dataLogin);
										        		 	um.update(usr);
										        		 	hasiluname=true;
										        		 	LogSystem.info(request, "berhasil create username = "+uname, kelas, refTrx, trxType);
								        		 		}
								        		 	}
												} catch (Exception e2) {
													// TODO: handle exception
													hasiluname=false;
													LogSystem.error(request, e2.getMessage(), kelas, refTrx, trxType);
													LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
												}
											}
											
										}
					        		 	
					        		 	//db.session().flush();
					        		 	//System.out.println("idtmpUserName" + ".." + idtmpUserName + "idLogin" + idLogin);
					        		 	//System.out.println("commit");
					        		 } else {
											String uname=user_name+size;
											List<Login> llogin=ldo.cekLogin(uname);
											if(llogin.size()>0) {
												hasiluname=false;
											} else {
												tmpUserName.setUsername(uname.toLowerCase());
							        		 	tmpUserName.setDate_record(dateTime);
							        		 	dataLogin.setUsername(uname.toLowerCase());
							        		 	dataLogin.setDate_record(dateTime);
							        		 	try {
							        		 		long tmp=tmpuserDao.create2(tmpUserName);
							        		 		LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
								        		 	if(tmp>0) {
								        		 		long g=ldo.create2(dataLogin);
								        		 		if(g>0) {
								        		 			usr.setLogin(dataLogin);
										        		 	um.update(usr);
										        		 	hasiluname=true;
										        		 	LogSystem.info(request, "berhasil create username = "+uname, kelas, refTrx, trxType);
								        		 		}
								        		 	}
												} catch (Exception e2) {
													// TODO: handle exception
													hasiluname=false;
													LogSystem.error(request, e2.getMessage(), kelas, refTrx, trxType);
													LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
												}
											}
											
					        		 }
					        	} else {
					        		
									String uname=user_name+size;
									List<Login> llogin=ldo.cekLogin(uname);
									if(llogin.size()>0) {
										hasiluname=false;
									} else {
										tmpUserName.setUsername(uname.toLowerCase());
					        		 	tmpUserName.setDate_record(dateTime);
					        		 	dataLogin.setUsername(uname.toLowerCase());
					        		 	dataLogin.setDate_record(dateTime);
					        		 	try {
					        		 		long tmp=tmpuserDao.create2(tmpUserName);
					        		 		LogSystem.info(request, "hasil idnya = "+tmp, kelas, refTrx, trxType);
						        		 	if(tmp>0) {
						        		 		long g=ldo.create2(dataLogin);
						        		 		if(g>0) {
						        		 			usr.setLogin(dataLogin);
								        		 	um.update(usr);
								        		 	hasiluname=true;
								        		 	LogSystem.info(request, "berhasil create username = "+uname, kelas, refTrx, trxType);
						        		 		}
						        		 	}
										} catch (Exception e2) {
											// TODO: handle exception
											hasiluname=false;
											LogSystem.error(request, e2.getMessage(), kelas, refTrx, trxType);
											LogSystem.error(request, "Duplicate username", kelas, refTrx, trxType);
										}	
									}
											
					        	}
					        	
					        	if(hasiluname==false) {
						    		 String[] name=usr.getName().trim().toLowerCase().split(" ");
						    		 String uname=name[0]+usr.getId();
							    	 tmpUserName.setUsername(uname);
					        		 tmpUserName.setDate_record(dateTime);
					        		 dataLogin.setUsername(uname);
					        		 dataLogin.setDate_record(dateTime);
					        		 tmpuserDao.create(tmpUserName);
					        		 ldao.create(dataLogin);
					        		 usr.setLogin(dataLogin);
					        		 um.update(usr);
					        		 username=uname;
					        		 LogSystem.info(request, "berhasil create username dengan id eeuser= "+uname, kelas, refTrx, trxType);
						    	 }
						      
						} else {
							username=usr.getLogin().getUsername();
						}
						///////////////////////////////
						
						context.put("newusername", username);
					    LogSystem.info(request, "Open Document ID : "+idDoc,kelas, refTrx, trxType);
					}
					else {
						LogSystem.info(request, "User belum terdaftar/masih dalam daftar preregister.",kelas, refTrx, trxType);
						context.getResponse().sendError(401);
			        	
						return;
					}

			 context.put("link", "https://"+LINK);
	         context.put("domain", "https://"+DOMAINAPI);
	         context.put("domainwv", "https://"+DOMAINAPIWV);
	         context.put("webdomain", "https://"+DOMAIN);
	         context.put("domainweb", "https://"+WEBDOMAIN);
	         context.put("refTrx", refTrx);

		}catch (Exception e) {
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
            try {
				context.getResponse().sendError(400);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}	
	
	public static String shuffle(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }
		
		
}
