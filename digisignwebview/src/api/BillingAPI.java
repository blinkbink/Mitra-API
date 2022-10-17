//package api;
//
//import id.co.keriss.consolidate.DS.DigiSign;
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.SmartphoneVerification;
//import id.co.keriss.consolidate.action.ajax.SendMailSSL;
//import id.co.keriss.consolidate.action.billing.BillingSystem;
//import id.co.keriss.consolidate.dao.BankDao;
//import id.co.keriss.consolidate.dao.PaymentDao;
//import id.co.keriss.consolidate.ee.Bank;
//import id.co.keriss.consolidate.ee.Payment;
//import id.co.keriss.consolidate.ee.ProductBilling;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.FileProcessor;
//import id.co.keriss.consolidate.util.LogSystem;
//import id.co.keriss.consolidate.util.ReportUtil;
//import java.text.SimpleDateFormat;
//import java.util.Date;
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
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//
//public class BillingAPI extends ActionSupport implements DSAPI {
//
//	User userRecv;
//	SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
//	SimpleDateFormat sdf2=new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
//	SimpleDateFormat sdfKode=new SimpleDateFormat("DDDHHmmss");
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		DB 	db = getDB(context);
//		//userRecv=new UserManager(db).findById((long) 5790);
////        User user = (User) context.getSession().getAttribute (USER);
////		int count = 21;
//		HttpServletRequest  request  = context.getRequest();
//		String jsonString=null;
//		String refTrx="";
//		FileItem dataFile=null;
//		String filename=null;
//		List<FileItem> fileItems =null;
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
//				fileItems = upload.parseRequest(request);
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
//						else {
//							
//							 if(fileItem.getFieldName().equals("file") || fileItem.getFieldName().equals("Gambar")){
//								 dataFile=fileItem;
//								 filename=fileItem.getName();
//							 }
//							// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());
//
//						}
//					}
//				}
//			 String process=request.getRequestURI().split("/")[2];
////	         System.out.println("RECEIVE :"+jsonString);
//	         LogSystem.request(request,fileItems, refTrx);
////	         Logger ll=Logger.getLogger(this.getClass());
////	         ll.info("testtttttttttttttttttttttttttt");
////	         Logger.getLogger("q2").info(request.getRequestURI()+ ", RECEIVE : "+jsonString);
////	         
//	         //Log.info( "RECEIVE "+request.getRequestURI().toString() +"  : "+jsonString);
//			
//			 if(jsonString==null) return;	         
//	         JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");
//	         
//	         JSONObject jo = null;
//
//	         if(process.equals("LSTPRD.html")){
//	        	 	jo=listProduct(jsonRecv,context);
//	         }
//	         if(process.equals("REQBUY.html")){
//	        	 	jo=reqPembelian(jsonRecv,context);
//	         }
//	         if(process.equals("CONFRMTRF.html")){
//	        	 	jo=confirmation(request, dataFile, jsonRecv,context);
//	         }
//	         if(process.equals("LSTTOPUP.html")){
//	        	 	jo=topupHistory(jsonRecv,context);
//	         }
//	         if(process.equals("TOPUPDTL.html")){
//	        	 	jo=topupDetail(jsonRecv,context);
//	         }
//	         
//	         String res="";
//			if(jo!=null) {
//				res= new JSONObject().put("JSONFile", jo).toString();
//				LogSystem.response(request, jo,"");
//			}
//			else {
//				res="<b>ERROR 404</b>";
//				LogSystem.info(request, res,"");
//			}
//	        
////			Logger.getLogger("q2").info(request.getRequestURI()+ ", RESPONSE : "+res);
////			Log.getLog("Q2", "SEND "+request.getRequestURI().toString()).info("RESPONSE : "+res);
//			context.put("trxjson", res);
//	        context.getSyslog().info (" SEND: " + context.getRequest().getRequestURI()+" - "+res);
//
//			
//
//		}catch (Exception e) {
//
//			LogSystem.log.error(e);
//		}
//	}
//	
//	
//	JSONObject listProduct(JSONObject jsonRecv,JPublishContext context) throws JSONException{
//        JSONObject jo=new JSONObject();
//		DB 	db = getDB(context);
//
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				BillingSystem billSys=new BillingSystem(usr);
//				List<ProductBilling> lPrd=billSys.getListProd("");
//				JSONArray prodArray=new JSONArray();
//				for (ProductBilling productBilling : lPrd) {
//					JSONObject prd=new JSONObject();
//					prd.put("nama", productBilling.getProduct_code());
//					prd.put("kode", productBilling.getProduct_code());
//					prd.put("jml_ttd", productBilling.getJmlh_ttd());
//					prd.put("harga", productBilling.getHarga());
//					prodArray.put(prd);
//				}
//				jo.put("produk", prodArray);
//				jo.put("bank", new Bank().getListBank(db));
//				userRecv=usr;
//				res="00";
//				billSys.close();
//
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject topupHistory(JSONObject jsonRecv,JPublishContext context) throws JSONException{
//        JSONObject jo=new JSONObject();
//		DB 	db = getDB(context);
//
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
//			long SATUHARI=1000*60*60*24;
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				PaymentDao payDao=new PaymentDao(db);
//				List<Payment> lPay=payDao.findByUser(String.valueOf(usr.getId()));
//				JSONArray prodArray=new JSONArray();
//				for (Payment pay : lPay) {
//					JSONObject p=new JSONObject();
//					p.put("id", pay.getId().toString());
//					p.put("no_topup", "PY"+sdfKode.format(pay.getDate_request()));
//					p.put("kode", pay.getProduct_code());
//					p.put("jml_ttd", pay.getJml_ttd());
//					p.put("amount_trf", pay.getAmount());
//					
//					p.put("status", pay.getStatusString());
//					p.put("time_req", sdf2.format(pay.getDate_request()));
//					if(pay.getStatus()==1 &&(new Date().getTime()-pay.getDate_request().getTime()>=SATUHARI)) {
//						continue;
//					}
//					prodArray.put(p);
//				}
//				jo.put("topup", prodArray);
//				userRecv=usr;
//				res="00";
//
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject topupDetail(JSONObject jsonRecv,JPublishContext context) throws JSONException{
//        JSONObject jo=new JSONObject();
//		DB 	db = getDB(context);
//
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				PaymentDao payDao=new PaymentDao(db);
//				Payment pay=payDao.findByUserID(String.valueOf(usr.getId()),new Long(jsonRecv.getString("id")));
//				jo.put("id", pay.getId().toString());
//				jo.put("no_topup", "PY"+sdfKode.format(pay.getDate_request()));
//				jo.put("kode", pay.getProduct_code());
//				jo.put("jml_ttd", pay.getJml_ttd());
//				jo.put("amount_trf", pay.getAmount());
//				
//				String date_conf=pay.getDate_confirmation()==null?"-":sdf2.format(pay.getDate_confirmation());
//				String date_update=pay.getDate_update()==null?"-":sdf2.format(pay.getDate_update());
//				long expDate=pay.getDate_request().getTime()+ReportUtil.MILLIS_IN_A_DAY;
//				String exp_date=sdf2.format(new Date(expDate));
//				jo.put("status", pay.getStatusString());
//				jo.put("time_req", sdf2.format(pay.getDate_request()));
//				jo.put("time_confirm", date_conf);
//			    jo.put("time_update", date_update);
//				jo.put("exp_date", exp_date);
//				jo.put("bank_to", pay.getBank_to());
//				jo.put("rek", new BankDao(db).findByName(pay.getBank_to()).getRekening());
//				jo.put("invoice", pay.getInvoice()==null?"-":pay.getInvoice());
//				
//					
//				userRecv=usr;
//				res="00";
//
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject reqPembelian(JSONObject jsonRecv,JPublishContext context) throws JSONException{
//        JSONObject jo=new JSONObject();
//		DB 	db = getDB(context);
//
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				BillingSystem billSys=new BillingSystem(usr);
//				String prod_cd=jsonRecv.getString("kode");
//				ProductBilling pb=billSys.getProductCode(prod_cd);
//				Long harga=new Long(pb.getHarga());
//				if(pb!=null &&harga>0 && new Bank().existRek(jsonRecv.getString("rek"))) {
//					PaymentDao payDao=new PaymentDao(db);
//					Long amtTrf=payDao.getAmount(prod_cd, harga);
//					
//					Payment pay= new Payment();
//					pay.setAmount(amtTrf);
//					pay.setAmount_original(harga);
//					pay.setBank_to(new BankDao(db).findByName(jsonRecv.getString("rek")).getRekening());
//					pay.setDate_request(new Date());
//					pay.setEeuser(usr);
//					pay.setStatus(1);
//					pay.setId_customer("ID"+usr.getId());
//					pay.setName_source(usr.getName());
//					pay.setProduct_code(prod_cd);
//					pay.setJml_ttd(Integer.parseInt(pb.getJmlh_ttd()));
//
//					payDao.create(pay);
//					
//					long expDate=pay.getDate_request().getTime()+ReportUtil.MILLIS_IN_A_DAY;
//					jo.put("amount_trf", String.valueOf(amtTrf));
//					jo.put("rek", jsonRecv.getString("rek"));
//					jo.put("bank", pay.getBank_to());
//					jo.put("exp_date",new Date(expDate));
//					userRecv=usr;
//					res="00";
//					
//					 
//					HttpServletRequest request = null; 
//					new SendMailSSL(request, "").sendMailPayRequest(db, pay, usr.getNick());
//					
//				}
//				billSys.close();
//
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	JSONObject confirmation(HttpServletRequest req,FileItem fi,JSONObject jsonRecv,JPublishContext context) throws JSONException{
//        JSONObject jo=new JSONObject();
//		DB 	db = getDB(context);
//
//        String res="05";
//        String signature=null;
//		try{
//			UserManager um=new UserManager(db);
//			userRecv=null;
////			Key k=new KeyDao(db).getPrivByUserEmail(usr.getNick());
//			DigiSign ds=new DigiSign();
//			
//			SmartphoneVerification sVerf=new SmartphoneVerification(db);
////		
//			if(sVerf.verification(jsonRecv)){
//				User usr= sVerf.getEeuser();
//				BillingSystem billSys=new BillingSystem(usr);
//				PaymentDao payDao=new PaymentDao(db);
//				Payment pay=payDao.findByUserID(String.valueOf(usr.getId()), Long.valueOf(jsonRecv.getString("id")));
//				if(pay!=null) {
//					String path=new FileProcessor().uploadBuktiTransfer(req, usr, fi);
//					pay.setDate_confirmation(new Date());
//					if(pay.getStatus()==1)pay.setStatus(2);
//					pay.setName_source(jsonRecv.getString("atas_nama"));
//					pay.setBank_from(jsonRecv.getString("bank_from"));
//					pay.setAmount(Long.valueOf(jsonRecv.getString("amount")));
//					pay.setPhoto(path);
//					payDao.update(pay);
//
//					userRecv=usr;
//					res="00";
//					
//				   
//					HttpServletRequest request = null; 
//					new SendMailSSL(request, "").sendMailPayKonfirmasi(db,pay, usr.getNick());
//					
//		    	}
//				billSys.close();
//			}
//			else {
//				jo=sVerf.setResponFailed(jo);
//			}
////			TrxDs trx=new TrxDs();
////			trx.setMessage(jsonRecv.toString());
////			trx.setMsg_from(k.getUserdata());
////			trx.setMsg_time(new Date());
////			if(res.equals("00"))trx.setStatus(new StatusKey("VRF"));
////			else trx.setStatus(new StatusKey("EVR"));
////			trx.setType('1');
////			if(userRecv!=null){
////				trx.setMsg_to(userRecv.getUserdata());
////				new TrxDSDao(db).create(trx);
////			}
//			
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.log.error(e);
//
//			res="06";
//			LogSystem.error(getClass(), e);
//		}
//        if(!jo.has("result")) jo.put("result", res);
//	   return jo;
//	}
//	
//	
//	
//}
