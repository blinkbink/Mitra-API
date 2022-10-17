//package apiMitra;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.ApiVerification;
//import id.co.keriss.consolidate.action.ajax.SendMailSSL;
//import id.co.keriss.consolidate.dao.PerubahanDataDao;
//import id.co.keriss.consolidate.ee.PerubahanData;
//import id.co.keriss.consolidate.util.LogSystem;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//
//
//public class UpdateDataUserMitra extends ActionSupport {
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
//		String refTrx="";
//		List <FileItem> fileSave=new ArrayList<FileItem>() ;
//		List<FileItem> fileItems=null;
//		//System.out.println("DATA DEBUG :"+(i++));
//		LogSystem.info(request, "DATA DEBUG :"+(i++),"");
//		try{
//				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//
//				// no multipart form
//				if (!isMultipart) {
//
//				}
//				// multipart form
//				else {
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
//					}
//				}
//			 String process=request.getRequestURI().split("/")[2];
//	         System.out.println("PATH :"+request.getRequestURI());
////	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
////			 Log.getLog("Q2", "RECEIVE "+request.getRequestURI().toString()).info("RECEIVE : "+jsonString);
////	         Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
//	         LogSystem.request(request, fileItems, refTrx);
//			 if(jsonString==null) return;	         
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         
//	         JSONObject jo = null;
//	         jo=reqPerubahanData(jsonRecv, context, request);
//	         
//	         
//			String res="";
//			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
//			else res="<b>ERROR 404</b>";
//	        
////			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
////			Log.info( "SEND "+request.getRequestURI().toString() +"  : "+res);
////	         System.out.println("SEND :"+res);
//
//			context.put("trxjson", res);
//			LogSystem.response(request, jo,"");
//
//			
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e);
////			error (context, e.getMessage());
////            context.getSyslog().error (e);
////			log.error(e);
//		}
//	}
//	
//	JSONObject reqPerubahanData(JSONObject jsonRecv, JPublishContext context, HttpServletRequest  request) throws JSONException{
//		DB db = getDB(context);
//		JSONObject jo=new JSONObject();
//        String res="05";
//        String notif="userid atau password salah";
//        
//        ApiVerification aVerf = new ApiVerification(db);
//        if(aVerf.verification(jsonRecv)){
//        	if(aVerf.getEeuser().isAdmin()==false) {
//        		jo.put("result", res);
//                jo.put("notif", "userid anda tidak diijinkan.");
//                return jo;
//        	}
//        	User uUpdate=aVerf.getEeuser();
//        	UserManager udao=new UserManager(db);
//        	User user=udao.findByUsernameByMitra(jsonRecv.getString("email"), uUpdate.getMitra().getId());
//        	
//        	if(user!=null) {
//        		boolean next=true;
//        		PerubahanDataDao pdao=new PerubahanDataDao(db);
//        		List<PerubahanData> pdata=pdao.findByUserMitra(user.getId(), uUpdate.getId());
//        		if(pdata.size()>0) {
//        			long check=hitungjam(new Date(), pdata.get(0).getTgl_req());
//        			//System.out.println("hasil selisih jam = "+check);
//        			LogSystem.info(request, "Hasil selisih jam = "+check,"");
//        			if(check<2) {
//        				next=false;
//        				res="05";
//        				notif="Anda belum melakukan konfirmasi perubahan data sebelumnya atau tunggu 2 jam kedepan.";
//        			}
//        		}
//        		
//        		if(next==true) {
//        			boolean sAlamat=false;
//                	boolean sPhone=false;
//                	String alamat=jsonRecv.getString("address");
//                	String phone=jsonRecv.getString("phone");
//                	if(alamat.equals("1")) {
//                		sAlamat=true;
//                	}
//                	if(phone.equals("1")) {
//                		sPhone=true;
//                	}
//                	
//                	PerubahanData pd=new PerubahanData();
//                	
//                	pd.setAddress(sAlamat);
//                	pd.setPhone(sPhone);
//                	pd.setKelurahan(jsonRecv.getString("kelurahan"));
//                	pd.setAlamat(jsonRecv.getString("alamat"));
//                	pd.setKecamatan(jsonRecv.getString("kecamatan"));
//                	pd.setKota(jsonRecv.getString("kota"));
//                	pd.setKodepos(jsonRecv.getString("kodepos"));
//                	pd.setPropinsi(jsonRecv.getString("propinsi"));
//                	pd.setNo_hp(jsonRecv.getString("nohp"));
//                	pd.setUpdate_from(uUpdate);
//                	pd.setEeuser(user);
//                	pd.setTgl_req(new Date());
//                	pd.setStatus('1');
//                	try {
//                		Long idReq = pdao.create(pd);
//                		if(idReq>0) {
//    	            		res="00";
//    	            		notif="sukses! silahkan klik link konfirmasi perubahan data pada email anda.";
//    	            		MailSender mail = new MailSender(user, idReq.toString());
//    	            		mail.run();
//                		}
//                		else {
//                			res="05";
//        					notif="data gagal diproses";
//                		}
//    				} catch (Exception e) {
//    					// TODO: handle exception
//    					res="05";
//    					notif="data gagal diproses";
//    					if(jsonRecv.getString("alamatdom").length()>50) {
//    						notif="teks alamat max 50 karakter";
//    					}
//    				}
//        		}
//        		
//            	
//        	}
//        	else {
//        		res="01";
//        		notif="bukan user mitra anda.";
//        	}
//        }
//        else {
//        	res="05";
//        }
//        
//        jo.put("result", res);
//        jo.put("notif", notif);
//		return jo;
//	}
//	
//	private long hitungjam(Date j1, Date j2) {
//		long diff = j1.getTime() - j2.getTime();
//	    long diffHours = diff / (60 * 60 * 1000) % 24;
//		return diffHours;
//	}
//	
//	class MailSender{
//
//		//String email;
//		User user;
//		String id;
//		
//		public MailSender(User user, String id) {
//			//this.email=email;
//			this.user=user;
//			this.id=id;
//		}
//		public void run() {
//			//new SendMailSSL().sendMailPreregisterMitra(name, email, id);
//			HttpServletRequest request = null; 
//			new SendMailSSL(request, "").sendMailKonfPerbData(user, id);
//
//		}
//		
//	}
//	
//}
