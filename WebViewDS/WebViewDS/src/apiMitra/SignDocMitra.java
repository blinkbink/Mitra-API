//package apiMitra;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.action.ajax.SendMailSSL;
//import id.co.keriss.consolidate.action.billing.BillingSystem;
//import id.co.keriss.consolidate.action.billing.KillBillDocument;
//import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
//import id.co.keriss.consolidate.action.billing.KillBillPersonal;
//import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
//import id.co.keriss.consolidate.action.kms.DocumentSigner;
//import id.co.keriss.consolidate.dao.ConfirmCodeDao;
//import id.co.keriss.consolidate.dao.DocumentsAccessDao;
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.dao.EeuserMitraDao;
//import id.co.keriss.consolidate.dao.InvoiceDao;
//import id.co.keriss.consolidate.ee.ConfirmCode;
//import id.co.keriss.consolidate.ee.DocumentAccess;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.JSAuth;
//import id.co.keriss.consolidate.ee.Mitra;
//import id.co.keriss.consolidate.ee.Userdata;
//import id.co.keriss.consolidate.util.AESEncryption;
//import id.co.keriss.consolidate.util.DSAPI;
//import id.co.keriss.consolidate.util.LogSystem;
//
//import java.io.File;
//import java.net.URLEncoder;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Vector;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//import org.jpos.ee.UserManager;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//
//import api.email.SendNotifSignDoc;
//import api.log.ActivityLog;
//
//
//
//public class SignDocMitra extends ActionSupport implements DSAPI {
//	User userRecv;
//	String kelas="apiMitra.SignDocMitra";
//	String trxType="PRC-SGN";
//	String refTrx="";
//	@SuppressWarnings("unchecked")
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		DB db = getDB(context);
//        JSAuth auth=new JSAuth(db);
//        User user=null;
//        String namaUser = "";
//        boolean otp = false;
//        boolean valid=false;
//        String info="";
//		int count = 21;
//		HttpServletRequest  request  = context.getRequest();
//		String rc="05";
//		int status=0;
//		JSONObject result=new JSONObject();
//		DocumentsAccessDao dao=new DocumentsAccessDao(db);
//		try{
//			
//			 String process=request.getRequestURI().split("/")[2];
//	         //System.out.println("PATH :"+request.getRequestURI());
//	         LogSystem.info(request, "PATH :"+request.getRequestURI());
//	         
//	         StringBuilder sb = new StringBuilder();
//	         String s;
//	         while ((s = request.getReader().readLine()) != null) {
//	                sb.append(s);
//	         }
//
////	         System.out.println("RECEIVE :"+sb.toString());
////	         log.info("RECEIVE :"+sb.toString());
//	         JSONObject object=new JSONObject(sb.toString());
//	         if(object.has("refTrx"))refTrx=object.getString("refTrx");
//	         
//	         LogSystem.request(request, object.toString(), kelas, refTrx,trxType);
//	         LogSystem.info(getClass(), sb.toString());       
//	         
//	         
//	         UserManager um=new UserManager(db);
//        	 user=um.findByEmail(object.getString("usersign"));
//        	 if(user!=null) {
//		         String idDoc=object.getString("idDoc");
//	        	 List<DocumentAccess> docac = dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
//	        	 if(docac.size()==0) {
//	        		 System.out.println("size < 0");
//	        		 docac = dao.findDocEEuserSigned(Long.valueOf(idDoc), user.getNick());
//		        	 result.put("result", "00");
//	     			 result.put("status", status);
//	     			 result.put("notif", "Dokumen Sudah Ditandatangan");
//	     			 String id=AESEncryption.encryptDoc(String.valueOf(docac.get(0).getDocument().getEeuser().getId()));
//					 String namafile=AESEncryption.encryptDoc(docac.get(0).getDocument().getSigndoc());
//					 result.put("doc_link", "https://"+DOMAINAPIWV+"/dt02.html?id="+URLEncoder.encode(id, "UTF-8")+"&doc="+URLEncoder.encode(namafile, "UTF-8"));
//					 context.put("trxjson", result.toString());
//					 LogSystem.info(request, result.toString(), kelas, refTrx, trxType);
//					 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//	     			 return;
//
//	        	 }
//        	 }
//	         
//	         
//	         ConfirmCode cc = null;
//	         ConfirmCodeDao ccd = new ConfirmCodeDao(db);
//	         //UserManager um=new UserManager(db);
//	         EeuserMitraDao emdao=new EeuserMitraDao(db);
//	         LogSystem.info(request, "check otp dan password", kelas, refTrx, trxType);
//	         if(object.getString("userpwd")!=null && object.getString("usersign")!=null && object.getString("otp")!=null) {
//	        	 //user=checkUser(object.getString("userpwd"), object.getString("usersign"), db);
//	        	 user=um.findByUsernamePassword(object.getString("usersign"), object.getString("userpwd"));
//	        	 
//	        	 
//	        	 //EeuserMitra em=emdao.findUserPwdMitra(object.getString("usersign"), object.getString("userpwd"), Long.valueOf(object.getString("mitra")));
//	        	 //if(em!=null)user=em.getEeuser();
//	        	 if(user!=null) {
//	        		 //System.out.println("User valid");
//	        		 LogSystem.info(request, "User valid", kelas, refTrx, trxType);
//	        		 //check otp
//	        		 cc = ccd.getLastOTP(user.getId());
//	        		 if(cc!=null) {
//	        			 if(cc.getCode().equals(object.getString("otp"))) {
//	        				 otp=true;
//		        			 valid=true;
//	        			 }
//	        			 else {
//	        				 LogSystem.error(request, "OTP tidak valid");
//	        				 //System.out.println("Otp tidak valid");
//		        			 result.put("result", "12");
//		        			 result.put("notif", "kode OTP tidak valid");
//		        			 context.put("trxjson", result.toString());
//		        			 LogSystem.info(request, result.toString());
//		        			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//		        			 return;
//	        			 }
//	        		 }
//	        		 else {
//	        			 LogSystem.error(request, "OTP tidak valid");
//	        			 //System.out.println("Otp tidak valid");
//	        			 result.put("result", "12");
//	        			 result.put("notif", "kode OTP tidak valid");
//	        			 context.put("trxjson", result.toString());
//	        			 LogSystem.info(request, result.toString());
//	        			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//	        			 return;
//	        			 
//	        		 }
//	        		 valid=true;
//	        	 }
//	        	 else {
//	        		 LogSystem.error(request, "Password salah");
//	        		 //System.out.println("user tidak valid");
//	        		 result.put("result", "12");
//        			 result.put("notif", "Password salah");
//        			 context.put("trxjson", result.toString());
//        			 LogSystem.info(request, result.toString());
//        			 LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//        			 return;
//	        	 }
//	         }
//	         
//	         if(valid==true) {
//	        	 
//		         String idDoc=object.getString("idDoc");
//
//		         //List<DocumentAccess>dList=dao.findDocAccessEEuser(idDoc, String.valueOf(user.getId()) ,user.getNick());
//		         //List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getId());
//		         List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getNick());
//		         //System.out.println("jumlah list = "+dList.size());
//		         LogSystem.info(request, "jumlah list = "+dList.size());
//		         
//		         boolean sign=false;
//		         boolean signPrc=false;
//		         BillingSystem bs;
//		         int balance=0;
////		         KillBillDocument kd = null;
////		         KillBillPersonal kp = null;
//		         KillBillDocumentHttps kdh=null;
//		         KillBillPersonalHttps kph=null;
//		         try {
////		        	 kd=new KillBillDocument();
////			         kp=new KillBillPersonal();
//		        	 kdh=new KillBillDocumentHttps(request);
//		        	 kph=new KillBillPersonalHttps(request);
//				} catch (Exception e) {
//					// TODO: handle exception
////					kd.close();
////					kp.close();
//					LogSystem.error(request, "System timout");
//					 result.put("result", "06");
//	     			 result.put("status", status);
//	     			 result.put("notif", "System timeout, mohon menunggu 10 menit kemudian");
//	     			 context.put("trxjson", result.toString());
//	     			LogSystem.info(request, result.toString());
//	     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//	     			 return;
//				}
//		         
//		         Mitra mitra=dList.get(0).getDocument().getEeuser().getMitra();
//		         int jmlttd=dList.size();
//		         String inv=null;
//		         InvoiceDao idao=new InvoiceDao(db);
//		         if(dList.get(0).getDocument().getPayment()=='2') {
//		        	 //bs=new BillingSystem(dList.get(0).getDocument().getEeuser());
//		        	 //balance=kp.getBalance("MT"+mitra.getId());
//		        	 JSONObject resp=kph.getBalance("MT"+mitra.getId(), request);
//				  		if(resp.has("data")) {
//				  			JSONObject data=resp.getJSONObject("data");
//							balance = data.getInt("amount");
//				  		} else {
//				  			result.put("result", "FE");
//							result.put("notif", "System timeout. silahkan coba kembali.");
//							LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//							return;
//				  		}
//				  		
//		        	 if(balance<jmlttd) {
////		        		 kd.close();
////		        		 kp.close();
//		        		 
//		        		 result.put("result", "61");
//		     			 result.put("status", status);
//		     			 result.put("notif", "balance ttd mitra tidak cukup");
//		     			 context.put("trxjson", result.toString());
//		     			LogSystem.info(request, result.toString());
//		     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//		     			 return;
//		        	 }
//		        	 
////		        	 inv=kp.setTransaction("MT"+mitra.getId(), jmlttd);
//		        	 JSONObject obj=kph.setTransaction("MT"+mitra.getId(), 1, idDoc);
//					  if(obj.has("result")) {
//						  String hasil=obj.getString("result");
//						  if(hasil.equals("00")) {
//								inv=obj.getString("invoiceid");
//								balance=obj.getInt("current_balance");
//							} else {
//					  			result.put("result", "FE");
//								result.put("notif", "System timeout. silahkan coba kembali.");
//								LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//								return;
//							}
//					  } else {
//						  
//				  			result.put("result", "FE");
//							result.put("notif", "System timeout. silahkan coba kembali.");
//							LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//							return;
//						
//					  }
//		        	 //String[] split=inv.split(" ");
//		        	 
//		        	 id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
//					  ivc.setDatetime(new Date());
//					  ivc.setAmount(jmlttd);
//					  ivc.setEeuser(dList.get(0).getDocument().getEeuser());
//					  ivc.setExternal_key("MT"+mitra.getId());
//					  ivc.setTenant('1');
//					  ivc.setTrx('2');
//					  ivc.setKb_invoice(inv);
//					  ivc.setDocument(dList.get(0).getDocument());
//					  ivc.setCur_balance(balance);
//					  idao.create(ivc);
//					  
//					 //inv=split[1];
//					  
//		        	 if(inv!=null) {
//		        		 
//		        		 SignDoc sd=new SignDoc();
//		        		 for(DocumentAccess da:dList) {
//		        			 try {
//		        				 sign=sd.signDoc(user, da, inv, db, refTrx, request);
//		        				 if(sign==false) {
//		        					result.put("result", "06");
//					     			result.put("status", status);
//					     			result.put("notif", "gagal proses tandatangan");
////					     			kp.reverseTransaction(inv);
//					     			kph.reverseTransaction(inv, 1, idDoc);
//					     			idao.deleteWhere(inv);
//					     			context.put("trxjson", result.toString());
//					     			LogSystem.info(request, result.toString());
//					     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
////					     			kp.close();
////					     			kd.close();
//					        		return;
//		        				 }
//							} catch (Exception e) {
//								// TODO: handle exception
////								kd.close();
////								kp.close();
//								result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "gagal proses tandatangan");
////				     			kp.reverseTransaction(inv);
//				     			kph.reverseTransaction(inv, 1, idDoc);
//				     			idao.deleteWhere(inv);
//				     			sign=false;
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
//				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//				        		return;
//							}
//		        			  
//		        		 }
//		        		 
//		        	 }
////		        	 kd.close();
////		        	 kp.close();
//		        	 
//		         } else if(dList.get(0).getDocument().getPayment()=='3'){
////		        	 balance=kd.getBalance("MT"+mitra.getId());
//		        	 
//					  
//					  //List<DocumentAccess> ld=dao.findByDocSign(dList.get(0).getDocument().getId());
//					  List<id.co.keriss.consolidate.ee.Invoice> li=idao.findByDoc(dList.get(0).getDocument().getId());
//					  //if(ld.size()==0) {
//					  if(li.size()==0) {
//						  //inv=kd.setTransaction("MT"+mitra.getId(), 1);
//						  JSONObject resp=kdh.getBalance("MT"+mitra.getId(), request);
//					  		if(resp.has("data")) {
//					  			JSONObject data=resp.getJSONObject("data");
//								balance = data.getInt("amount");
//					  		} else {
//					  			result.put("result", "FE");
//								result.put("notif", "System timeout. silahkan coba kembali.");
//								LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//								return;
//					  		}
//			        	 LogSystem.info(request, "Balance dokumen = "+balance);
//			        	 //System.out.println("balance dokumen = "+balance);
//						  if(balance<1) {
////							  kd.close();
////							  kp.close();
//							  	result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "balance doc mitra tidak cukup");
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
//				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//				     			return;
//						  }
//						  
//						  JSONObject obj=kdh.setTransaction("MT"+mitra.getId(), 1, idDoc);
//						  if(obj.has("result")) {
//							  String hasil=obj.getString("result");
//							  if(hasil.equals("00")) {
//									inv=obj.getString("invoiceid");
//									balance=obj.getInt("current_balance");
//								} else {
//						  			result.put("result", "FE");
//									result.put("notif", "System timeout. silahkan coba kembali.");
//									LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//									return;
//								}
//						  } else {
//							  
//					  			result.put("result", "FE");
//								result.put("notif", "System timeout. silahkan coba kembali.");
//								LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//								return;
//							
//						  }
//						  
////						  String[] split=inv.split(" ");
////						  inv=split[1];
//						  
//						  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
//						  ivc.setDatetime(new Date());
//						  ivc.setAmount(1);
//						  ivc.setEeuser(dList.get(0).getDocument().getEeuser());
//						  ivc.setExternal_key("MT"+mitra.getId());
//						  ivc.setTenant('2');
//						  ivc.setTrx('2');
//						  ivc.setKb_invoice(inv);
//						  ivc.setDocument(dList.get(0).getDocument());
//						  ivc.setCur_balance(balance);
//						  idao.create(ivc);
//						  
//						  if(inv==null) {
//							  	result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "gagal proses tandatangan [bill]");
////				     			kd.reverseTransaction(inv);
//				     			kdh.reverseTransaction(inv, 1, idDoc);
//				     			idao.deleteWhere(inv);
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
//				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
////				     			kd.close();
////				     			kp.close();
//				        		return;
//						  }
//					  }
//					  
//					 SignDoc sd=new SignDoc();
//	        		 for(DocumentAccess da:dList) {
//	        			 try {
//	        				 sign=sd.signDoc(user, da, inv, db, refTrx, request);
//	        				 if(sign==false) {
//	        					result.put("result", "06");
//				     			result.put("status", status);
//				     			result.put("notif", "gagal proses tandatangan");
//				     			if(inv!=null) {
////				     				kd.reverseTransaction(inv);
//				     				kdh.reverseTransaction(inv, 1, idDoc);
//				     				idao.deleteWhere(inv);
//				     			}
//				     			context.put("trxjson", result.toString());
//				     			LogSystem.info(request, result.toString());
//				     			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
////				     			kd.close();
////				     			kp.close();
//				        		return;
//	        				 }
//						} catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//							result.put("result", "06");
//			     			result.put("status", status);
//			     			result.put("notif", "gagal proses tandatangan");
//			     			if(inv!=null) {
////			     				kd.reverseTransaction(inv);
//			     				kdh.reverseTransaction(inv, 1, idDoc);
//			     				idao.deleteWhere(inv);
//			     			}
//			        		sign=false;
//			        		context.put("trxjson", result.toString());
//			        		LogSystem.info(request, result.toString());
//			        		LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
////			        		kd.close();
////			        		kp.close();
//			     			return;
//						}
//	        			  
//	        		 }
////	        		 kd.close();
////	        		 kp.close();
//		         }
//		         
//		         Long jml=(long) 1;
//		         if(sign==true) {
//		        	 rc="00";
//		        	 status=00;
//		        	 info="Proses tanda tangan berhasil!";
//		        	 cc.setStatus("yes");
//		        	 ccd.update(cc);
//		        	 
//		        	 jml=dao.getWaitingSignUserByDoc(idDoc);
//			        	if(jml==0) {
//			        		Documents doc=dList.get(0).getDocument();
//			        		doc.setSign(true);
//			        		new DocumentsDao(db).update(doc);
//			        	}
//		        	
//		        	DocumentAccess docac=dList.get(0);
//		        	String iddoc=String.valueOf(docac.getDocument().getId());
//		        	String namadoc=docac.getDocument().getFile_name();
//		        	String namattd=docac.getName();
//		        	String jkttd=String.valueOf(docac.getEeuser().getUserdata().getJk());
//		        	String docs ="";
//					String link ="";
//					result.put("document_id", docac.getDocument().getIdMitra());
//					//result untuk status document
//					if(rc.equals("00")) {
//						JSONArray lWaiting= new JSONArray();
//						JSONArray lSigned= new JSONArray();
//						JSONArray lInitials= new JSONArray();
//						JSONArray lInitialsWaiting= new JSONArray();
//						for(DocumentAccess doc:dList) {
//							
//							
// 		 					JSONObject docObj=new JSONObject();
// 							/*change to eeuser*/
// 		// 					User userDoc=new UserManager(db).findByUserID(String.valueOf(doc.getUserdata().getId()));
// 		
// 		 					User userDoc=doc.getEeuser();
// 		 					if(userDoc==null) {
// 								docObj.put("name", doc.getName());
// 								docObj.put("email", doc.getEmail());
// 		
// 		 					}else {
//	 							docObj.put("name", userDoc.getName());
//	 							docObj.put("email", userDoc.getNick());
// 		 					}
// 		 					if(doc.getType().equals("sign") && !doc.isFlag()) {
// 		 						lWaiting.put(docObj);
// 		 							
// 		 					}if(doc.getType().equals("sign") && doc.isFlag()) {
// 		 						lSigned.put(docObj);
// 		 					}
// 		 					
// 		 					if(doc.getType().equals("initials") && !doc.isFlag()) {
// 		 						lInitialsWaiting.put(docObj);
// 		 					}
// 		 					
// 		 					if(doc.getType().equals("initials") && doc.isFlag()) {
// 		 						lInitials.put(docObj);
// 		 					}
// 		 				}
// 		 				
// 		 				if(lWaiting!=null && lWaiting.length()>0) {
// 		 					//result.put("waiting", lWaiting);
// 		 					result.put("status_document", "waiting");
// 		 					result.put("status", "22");
// 		 				}
// 		 				if(lSigned!=null && lSigned.length()>0) {
// 		 					//result.put("signed", lSigned);
// 		 					result.put("status", "00");
// 		 					if(lWaiting==null || lWaiting.length()==0) {
// 		 						result.put("status_document", "complete");
// 		 					}
// 		 				}
// 		 				
// 		 				
// 		 				if(lInitialsWaiting!=null && lInitialsWaiting.length()>0) {
// 		 					//result.put("waiting", lInitialsWaiting);
// 		 					result.put("status_document", "waiting");
// 		 					result.put("status", "22");
// 		 				}
// 		 				
// 		 				if(lInitials!=null && lInitials.length()>0) {
// 		 					//result.put("initials", lInitials);
// 		 					result.put("status", "00");
// 		 					if(lInitialsWaiting==null || lInitialsWaiting.length()==0) {
// 		 						result.put("status_document", "complete");
// 		 					}
// 		 				}
//					}
//					//END result untuk status document
//					
//					try {
//						   docs = AESEncryption.encryptDoc(iddoc);
//						   link = "https://"+DSAPI.DOMAIN+"/doc/source.html?frmProcess=viewFile&doc="+iddoc;
//						     //+ URLEncoder.encode(docs, "UTF-8");
//						} catch (Exception e1) {
//						   // TODO Auto-generated catch block
//						   e1.printStackTrace();
//						}
//					
//		        	SendNotifSignDoc std=new SendNotifSignDoc();
//		        	List<DocumentAccess> vda=dao.findByDoc(iddoc);
//		        	Vector<String> checkmail = new Vector();
//		        	
//		        	for(DocumentAccess da:vda) {
//		        		boolean kirim=true;
//		        		if(da.getEeuser()!=null) {
//		        			
//		        			for(String mail:checkmail) {
//		        				if(mail.equalsIgnoreCase(da.getEmail())) {
//		        					//std.kirim(da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link);
//		        					kirim=false;
//		        					break;
//		        				}
//		        			}
//		        			if(kirim==true) {
//		        				//if(mitra.isNotifikasi()) {
//	        					LogSystem.info(request, "Kirim email notif sign");
//	        					LogSystem.info(request, "Nama di eeuser" + da.getName());
//	        					LogSystem.info(request, "Tanggal kirim dokumen : " + da.getDocument().getWaktu_buat());
//	        					//da.getName()
////	        					std.kirim(da.getDate_sign(), user.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
//	        					std.kirim(da.getDate_sign(), da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
//			        			
//	        					checkmail.add(da.getEmail());
//			        			//}
//		        			}
//		        			
//		        		} else {
//		        			for(String mail:checkmail) {
//		        				if(mail.equalsIgnoreCase(da.getEmail())) {
//		        					//std.kirim(da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link);
//		        					kirim=false;
//		        					break;
//		        				}
//		        			}
//		        			if(kirim==true) {
//		        				//if(mitra.isNotifikasi()) {
//		        				LogSystem.info(request, "Kirim email notif sign");
//		        				LogSystem.info(request, "Nama di eeuser" + user.getName());	
//		        				LogSystem.info(request, "Tanggal kirim dokumen : " + da.getDocument().getWaktu_buat());
//		        			
//		        				//da.getName()
//	
////	        					std.kirim(da.getDate_sign(), user.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
//		        				std.kirim(da.getDate_sign(), da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));			        			
//		        				checkmail.add(da.getEmail());
//		        				//}
//		        			}
//		        			//
//		        		}
//		        		
//		        	}
//		        	try {
//			        	ActivityLog logSystem = new ActivityLog(request, refTrx);
//			        	logSystem.POST("signing", "success", "[API] Berhasil tandatangan dokumen", Long.toString(docac.getEeuser().getId()), Long.toString(docac.getDocument().getId()), null, "password", "otp",null);
//			        }catch(Exception e)
//			        {
//			        	e.printStackTrace();
//			        	LogSystem.info(request, "Gagal mengirim ke Log API", kelas, refTrx, trxType);
//			        }
//		         }
//		         
//		         
//		         /*
//		         int blc=bs.getBalance();
//
//		         JSONArray arr=object.getJSONArray("user");
//		         int jmlTTD=0;
//		         for(DocumentAccess documentAccess: dList) {
//		        	 for(int i=0; i<arr.length();i++) {
//		        		 JSONObject d=(JSONObject) arr.get(i);
//		        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
//	        				
//	        				 jmlTTD++;
//		        		 }
//		        	 }
//			         
//		         }
//		         
//		         System.out.println("Jumlah ttd = "+jmlTTD);
//		         if(blc-jmlTTD>=0) {
//		         
//			         for(DocumentAccess documentAccess: dList) {
//			        	 for(int i=0; i<arr.length();i++) {
//			        		 JSONObject d=(JSONObject) arr.get(i);
//			        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
//		        				 Deposit dep=new Deposit(documentAccess,db);
//		
//		        				 if(dep.transaksi(1)) {
//		        					 boolean res=signDoc(user, documentAccess,dep.getBillSys().getLastInvoice(),db);
//				        			 if(res) {
//				        				 signPrc=true;
//				        			 }else {
//				        				 dep.reversal();
//				        				 result.put("info", "gagal tanda tangan, coba ulangi beberapa saat lagi");
//				        			 }
//			        			 }else{
//			        				 info="transaksi gagal, coba ulangi beberapa saat lagi";
//			        				 
//			        				 if(!dep.getLastError().equals("error")) {
//			        					 info=dep.getLastError();
//			        				 }
//		        					 result.put("info", info);
//		        					 
//			        			 }
//		        				 
//			        		 }
//			        	 }
//				         
//			         }
//		         }
//		         else {
//		        	 
//					 result.put("info", "saldo anda kurang, silakan topup terlebih dahulu");
//					 System.out.println("saldo anda kurang");
//					 rc="E1";
//
//		         }
//		         
//		         if(signPrc) {
//		        	  
//				      boolean sign = true ;
//				      List<DocumentAccess> list = new DocumentsAccessDao(db).findByDoc(idDoc);
//				      for (DocumentAccess doa : list) {
//				    	  
//				    	 if(doa.getEeuser()!=null) {
//
//							 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getEeuser().getUserdata(), doa.getEmail());
//							 mail.run();
//				    	 }else {
//				    		 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getName(), doa.getEmail());
//							 mail.run();
//				    	 }
//						 if(doa.getType().equals("sign") && doa.isFlag()==false) {
//							sign=false; 
//						 }
//				       }
//				      
//				
//		         
//			        Documents docx=list.get(0).getDocument();
//			        if(sign==true) {
//			        	docx.setSign(true);
//			        	status=1;
//			        }
//		    		new DocumentsDao(db).update(docx);
//		    		
//		    		
//			        rc="00";
//			        cc.setStatus("yes");
//			        ccd.update(cc);
//		         }
//		         */
//	         }
//	         
//		}catch (Exception e) {
//            LogSystem.error(getClass(), e, kelas, refTrx, trxType);
////			error (context, e.getMessage());
////            context.getSyslog().error (e);
//            e.printStackTrace();
//		}
//		
//		
//		try {
//			result.put("result", rc);
//			result.put("status", status);
//			result.put("notif", info);
//			LogSystem.info(request, result.toString());
//			LogSystem.response(request, result.toString(), kelas, refTrx, trxType);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			LogSystem.error(getClass(), e, kelas, refTrx, trxType);
//		}
//        context.getResponse().setContentType("application/json");
//
//        //System.out.println("SEND :"+result.toString());
//        LogSystem.info(request, "SEND :"+result.toString());
//		context.put("trxjson", result.toString());
//		LogSystem.response(request, result);
//		 HttpSession session          = context.getSession();
//	        
//        session.removeAttribute (USER);
//
//	}
//	
//}