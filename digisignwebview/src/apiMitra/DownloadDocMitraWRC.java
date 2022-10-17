//package apiMitra;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.ApiVerification;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.TokenMitraDao;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.Mitra;
//import id.co.keriss.consolidate.ee.TokenMitra;
//import id.co.keriss.consolidate.util.LogSystem;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.List;
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//
//public class DownloadDocMitraWRC extends ActionSupport {
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
//		String refTrx="";
//		String filename=null;
//		List <FileItem> fileSave=new ArrayList<FileItem>() ;
//		List<FileItem> fileItems=null;
//		//System.out.println("DATA DEBUG :"+(i++));
//	    LogSystem.info(request, "DATA DEBUG :"+(i++),"");
//		Mitra mitra=null;
//		User useradmin=null;
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
//				// no multipart form
//				if (!isMultipart) {
//					context.getResponse().sendError(404);
//					return;
//				}
//				// multipart form
//				else {
//					TokenMitraDao tmd=new TokenMitraDao(getDB(context));
//					String token=request.getHeader("authorization");
//					if(token!=null) {
//						String[] split=token.split(" ");
//						if(split.length==2) {
//							if(split[0].equals("Bearer"))token=split[1];
//						}
//						TokenMitra tm=tmd.findByToken(token.toLowerCase());
//						//System.out.println("token adalah = "+token);
//						if(tm!=null) {
//							LogSystem.info(request, "Token ada : " + token,"");
//							mitra=tm.getMitra();
//						} else {
//							LogSystem.error(request, "Token tidak ada","");
//						
//							context.getResponse().sendError(403);
//							return;
//						}
//					}
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
////	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
////			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
////	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
//	         LogSystem.request(request, fileItems, refTrx);
//			 if(jsonString==null) return;	         
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         
//	         
//	         
//	         JSONObject jo = null;
//	        
//	         DownloadDocMitra(mitra, useradmin, jsonRecv, request, context);
//	         
//			String res="";
//			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
//			else res="<b>ERROR 404</b>";
//	        
////			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
////			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
////	         System.out.println("SEND :"+res);
//
//			//context.put("trxjson", res);
//			//LogSystem.response(request, jo);
//
//
//
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
//		}
//	}
//	
//	void DownloadDocMitra(Mitra mitratoken, User useradmin, JSONObject jsonRecv, HttpServletRequest  request,JPublishContext context) throws JSONException, IOException {
//		DB db = getDB(context);
//
//        JSONObject jo=new JSONObject();
//        JSONArray lWaiting= new JSONArray();
//		JSONArray lSigned= new JSONArray();
//		String status = "waiting";
//        String res="06";
//        Documents id_doc=null;
//        boolean kirim=false;
//     // obtains response's output stream
//        
//        PrintWriter outStream = null;
//		try {
//			outStream = context.getResponse().getWriter();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//        DocumentsDao docDao = new DocumentsDao(db);
//        
//        try{
//        	ApiVerification aVerf = new ApiVerification(db);
//        	boolean vrf=false;
//	        if(mitratoken!=null) {
//	        	vrf=true;
//	        }
//	        else {
//	        	vrf=aVerf.verification(jsonRecv);
//	        }
////    		
//			if(vrf){
//				
//				User usr=null;
//	        	Mitra mitra=null;
//	        	if(mitratoken==null) {
//		        	if(aVerf.getEeuser().isAdmin()==false) {
//		        		
//		        		context.getResponse().sendError(403);
//		        		return;
//		        	}
//		        	usr = aVerf.getEeuser();
//		    		mitra = usr.getMitra();
//	        	}
//	        	else {
//	        		usr=useradmin;
//	        		mitra=mitratoken;
//	        	}
//	        	
////				id_doc = docDao.findByUserDocID(usr.getId(), jsonRecv.get("document_id").toString()).get(0);
//				List<Documents> ld=docDao.findByDocID(jsonRecv.get("document_id").toString());
//				if(ld.size()>0) {
//					id_doc=ld.get(0);
//					if(id_doc.getEeuser().getMitra().getId()!=mitra.getId()) {
//						context.getResponse().sendError(403);
//						return;
// 					}
//					String filepath = id_doc.getPath()+id_doc.getSigndoc();
//					File downloadFile = new File(filepath);
//			        FileInputStream inStream = new FileInputStream(downloadFile);
//			        
//			        // modifies response
//			        
//			        context.getResponse().setContentType("application/pdf");
//			        context.getResponse().setContentLength((int) downloadFile.length());
//			         
//			        // forces download
//			        String headerKey = "Content-Disposition";
//			        String headerValue = String.format("attachment; filename=\"%s\"", id_doc.getSigndoc());
//			        LogSystem.info(request, headerKey,"");
//			        LogSystem.info(request, headerValue,"");
//			        context.getResponse().setHeader(headerKey, headerValue);
//			        
//			         
//			        byte[] buffer = new byte[4096];
//			        int bytesRead = -1;
//			         
//			        while ((bytesRead = inStream.read()) != -1) {
//			            outStream.write(bytesRead);
//			            //outStream.write(buffer, 0, bytesRead);
//			        }
//			         
//					LogSystem.info(request, "application/pdf download","");
//
//			        inStream.close();
//				} else {
//					context.getResponse().sendError(404);
//					return;
//				}
//				
// 				
//			}
//			else {
////				outStream.write(403);
//				//outStream.flush();
////				outStream.close();
//				context.getResponse().sendError(403);
//			}
//        }
//        catch (Exception e) {
//			// TODO: handle exception
//        	//context.getResponse().getOutputStream().flush();
//        	//context.getResponse().getOutputStream().close();
////        	outStream.close();
//        	context.getResponse().sendError(404);
//			LogSystem.error(getClass(), e);
//		}
//        finally
//        {
//             if (outStream != null)
//             {
//                  outStream.close();
//             }
//        }
//        
//	}
//	
//		
//		
//}
