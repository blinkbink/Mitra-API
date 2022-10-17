//package apiMitra;
//
//import id.co.keriss.consolidate.action.ActionSupport;
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
//import java.io.PrintWriter;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
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
//public class SignWebViewMitraV extends ActionSupport implements DSAPI{
//
//	static String basepath="/opt/data-DS/UploadFile/";
//	static String basepathPreReg="/opt/data-DS/PreReg/";
//
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		Random rand = new Random();
//		int i=0;
//		HttpServletRequest  request  = context.getRequest();
//		String jsonString=null;
//		byte[] dataFile=null;
//		FileItem filedata=null;
//		String filename=null;
//		String refTrx="";
//		int statusSign=0;
//		List <FileItem> fileSave=new ArrayList<FileItem>() ;
//		List<FileItem> fileItems=null;
//		//System.out.println("DATA DEBUG :"+(i++));
//		LogSystem.info(request, "DATA DEBUG :"+(i++),"");
//		Mitra mitra=null;
//		try{
//				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//				PrintWriter outStream = null;
//				try {
//					outStream = context.getResponse().getWriter();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				TokenMitraDao tmd=new TokenMitraDao(getDB(context));
//				String token=request.getHeader("authorization");
//				if(token!=null) {
//					String[] split=token.split(" ");
//					if(split.length==2) {
//						if(split[0].equals("Bearer"))token=split[1];
//					}
//				}
//				TokenMitra tm=tmd.findByToken(token.toLowerCase());
//				//System.out.println("token adalah = "+token);
//				if(tm!=null) {
//					LogSystem.info(request, "Token tidak null","");
//					mitra=tm.getMitra();
//				} else {
//					LogSystem.error(request, "Token null","");
//					context.getResponse().sendError(403);
//					
//					return;
//				}
//
//				// no multipart form
//				if (!isMultipart) {
//					//System.out.println("bukan multipart");
//					LogSystem.info(request, "Bukan multipart","");
//					jsonString=request.getParameter("jsonfield");
//
//				}
//				// multipart form
//				else {
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
//	         //LogSystem.request(request, fileItems);
//	         //System.out.println("sampai LOGGGGGGG");
//	         LogSystem.info(request, "sampai LOGGGGGGG","");
//	         
//			 if(jsonString==null) return;	     
//			 
//			 LogSystem.info(request, "jsonstring= "+jsonString,"");
//			 //System.out.println("jsonstring= "+jsonString);
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         String userid=jsonRecv.getString("userid");
//	         
//	         LogSystem.info(request, "sampai siniiiiiiiiiiiiiiiii","");
//	         //System.out.println("sampai siniiiiiiiiiiiiiiiii");
//	         UserManager user=new UserManager(getDB(context));
//	         User eeuser=user.findByUsername(userid);
//	         if(eeuser!=null) {
//	        	 if(eeuser.getMitra().getId()==mitra.getId() && eeuser.isAdmin()) {
//	        		 LogSystem.info(request, "Token dan mitra valid","");
//	        		 //System.out.println("token dan mitra valid");
//	        	 }
//	        	 else {
////	        		 JSONObject jo=new JSONObject();
////					 jo.put("res", "05");
////					 jo.put("notif", "Token dan Mitra tidak sesuai");
////					 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
////					
////					 return;
//	        		 //outStream.write(403);
//					 //outStream.close();
//	        		 //sendRedirect(context, "/403.html");
//	        		 LogSystem.error(request, "Token dan mitra tidak valid","");
//	        		 context.getResponse().sendError(403);
//	        		 return;
//	        	 }
//	         }
//	         else {
//	        	 LogSystem.error(request, "Userid tidak ditemukan","");
//	        	 context.getResponse().sendError(403);
//				 return;
//	         }
//	         
//	         
//	         String email_user=jsonRecv.getString("email_user");
//	         /*
//	         String pwd_user=jsonRecv.getString("pwd_user");
//	         EeuserMitraDao emdao=new EeuserMitraDao(getDB(context));
//	         EeuserMitra em=emdao.findUserPwdMitra(email_user, pwd_user, mitra.getId());
//	         if(em==null) {
//	        	 JSONObject jo=new JSONObject();
//				 jo.put("res", "05");
//				 jo.put("notif", "user atau password nasabah salah");
//				 context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
//				 
//				 //PrintWriter outStream=context.getResponse().getWriter();
//	        	 //outStream.write(401);
//	        	 //outStream.close();
//				 context.getResponse().sendError(401);
//				 return;
//	         }
//	         */
//	         JSONObject jo = null;
//	        
//	         //DownloadDocMitra(jsonRecv, request, context);
//	         DB db = getDB(context);
//	         /*
//	         ApiVerification aVerf = new ApiVerification(db);
//	         if(aVerf.verification(jsonRecv)){
//	        	 if(aVerf.getEeuser().isAdmin()==false) {
//		        		PrintWriter outStream=context.getResponse().getWriter();
//		        		outStream.write(403);
//		        		outStream.close();
//		        		return;
//	        	 }
//	        	 //HttpSession session = request.getSession();
//	        	 //User userTrx= new UserManager(db).findByUsername(jsonRecv.getString("user"));	
//		            //session.setAttribute (USER, userTrx);
//	        	 	User usermitra = aVerf.getEeuser();
//	        	 	//Mitra mitra = usermitra.getMitra();
//	        */
//	         User usermitra=eeuser;
//	         
//					String documentID=jsonRecv.getString("document_id");
//					DocumentsDao dd = new DocumentsDao(db);
//					LoginDao ldao=new LoginDao(db);
//					Documents doc=null;
//					List<Documents> ldocs;
//					try {
//						//doc = dd.findByUserDocID(usermitra.getId(), documentID).get(0);
//						ldocs=dd.findByUserDocID2(mitra.getId(), documentID);
//						if(ldocs.size()==0) {
//							LogSystem.error(request, "Dokumen size 0","");
//							context.getResponse().sendError(404);
//				        	return;
//						}
//						else {
//							doc=ldocs.get(0);
//						}
//					} catch (Exception e) {
//						// TODO: handle exception
//						LogSystem.error(request, "Error dokumen","");
//						context.getResponse().sendError(404);
//			        	return;
//					}
//					
//					String idDoc=null;
//					String userEmail=jsonRecv.getString("email_user");
//					Long idmitra = usermitra.getId();
//					UserManager um=new UserManager(db);
//					LogSystem.info(request, "User Email : " + userEmail ,"");
//					//System.out.println(userEmail);
//					User usr= um.findByUsernameSign(userEmail);
//					
//					//System.out.println("user nya adalah = "+usr.getName());
//					if(usr!=null) {
//						
//						LogSystem.info(request, "User tidak null","");
//						DocumentsAccessDao docDao=new DocumentsAccessDao(db);
//						List<DocumentAccess> da =docDao.findDocAccessEEuserByMitra(documentID, idmitra, userEmail, doc.getId());
//						
//						//System.out.println("TES SSSSSSSSSSSSSSSSSSS :"+usr.getUserdata().getImageTtd());
//						String id=AESEncryption.encryptDoc(String.valueOf(da.get(0).getDocument().getEeuser().getId()));
//						String namafile=AESEncryption.encryptDoc(da.get(0).getDocument().getSigndoc());
//						LogSystem.info(request, "id = " + id,"");
//						LogSystem.info(request, "namafile = " + namafile,"");
//						
//						DocumentAccess dacc=da.get(0);
//						if(dacc.getEeuser()==null) {
//							dacc.setEeuser(usr);
//							docDao.update(dacc);
//						}
//						
//						//context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
//						context.put("pdf_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
//						//System.out.println("https://"+DOMAINAPIWV+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
//						//System.out.println("https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile);
//						LogSystem.info(request, "Link = " + "https://"+DOMAINAPIWV+"/dt02.html/"+id+"/"+namafile,"");
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
//						//System.out.println(dataUser.toString());
//						LogSystem.info(request, "Data User = " + dataUser.toString(),"");
//						context.put("locSign", dataUser);
//						context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
//						context.put("title_doc", da.get(0).getDocument().getFile_name());
//						context.put("usersign", dataUser);
//						context.put("statusSign", statusSign);
//						context.put("visible", visible);
//						context.put("size", uL.length());
//						//context.put("username", login.getUsername());
//						if(usr.getLogin()!=null) {
//							//System.out.println("login ada bro ");
//							LogSystem.info(request, "Login tidak null","");
//							context.put("username", usr.getLogin().getUsername());
//						}
//						context.put("email", usr.getNick());
//						//context.put("pwd_user", pwd_user);
//						context.put("mitra", String.valueOf(mitra.getId()));
//						context.put("statususer", usr.getStatus());
//						context.put("nohp", usr.getUserdata().getNo_handphone());
//						context.put("iduser", usr.getId());
//						
//						String username="";
//						String pickUsername="";
//
//						if(usr.getStatus()=='1') {
//							id.co.keriss.consolidate.ee.Login lgn = null;
//					        String nama = usr.getName().toLowerCase();
//					        String character = "- ";
//					        String nameSplit[] = nama.split(" ");
//					        String date=new SimpleDateFormat("ddMMYY").format(new Date());
//
//					        do{
//					        	pickUsername = nameSplit[rand.nextInt(nameSplit.length)]+shuffle((date+usr.getUserdata().getNo_identitas()+usr.getId())+"_").substring(0,rand.nextInt(6));
//					        	lgn=ldao.getByUsername(pickUsername);
//					        	
//					            //System.out.println("Generate: "+pickUsername);
//					            LogSystem.info(request, "Generate: " + pickUsername,"");
//					            if (lgn == null)
//					            {
//					                //System.out.println("Found unique username: "+pickUsername);
//					            	LogSystem.info(request, "Found unique username: " + pickUsername,"");
//					                username=pickUsername;
//					            }
//					        }while(lgn != null);
//					        LogSystem.info(request, "Usernamenya adalah = " + username,"");
//					        //System.out.println("usernamenya adalah = "+username);
//					        
////							String nama=usr.getName().replaceAll("\\s", "");
////							
////							String ambil=new SimpleDateFormat("ddMMyyyy").format(usr.getUserdata().getTgl_lahir());
////							
////							if(nama.length()<6) {
////								username=nama+ambil.substring(0, 4);
////							} else {
////								username=nama.substring(0,6)+ambil.substring(0, 4)+usr.getId();
////							}
////							id.co.keriss.consolidate.ee.Login lgn=ldao.getByUsername(username);
////							if(lgn!=null)username=username+ambil.substring(4);
////							System.out.println("usernamenya adalah = "+username);
//								
//						}
//						context.put("newusername", username);
//					    LogSystem.info(request, "Open Document ID : "+idDoc,"");
//					}
//					else {
//						LogSystem.info(request, "User null","");
//						context.getResponse().sendError(401);
//			        	
//						return;
//					}
//
//	         context.put("domain", "https://"+DOMAINAPI);
//	         context.put("domainwv", "https://"+DOMAINAPIWV);
//
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
//            try {
//				context.getResponse().sendError(400);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		}
//	}	
//	
//	public static String shuffle(String input){
//        List<Character> characters = new ArrayList<Character>();
//        for(char c:input.toCharArray()){
//            characters.add(c);
//        }
//        StringBuilder output = new StringBuilder(input.length());
//        while(characters.size()!=0){
//            int randPicker = (int)(Math.random()*characters.size());
//            output.append(characters.remove(randPicker));
//        }
//        return output.toString();
//    }
//		
//		
//}
