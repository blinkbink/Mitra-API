package apiMitra;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import api.email.SendNotifSignDoc;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.BillingSystem;
import id.co.keriss.consolidate.action.billing.KillBillDocumentBug;
import id.co.keriss.consolidate.action.billing.KillBillPersonalBug;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.EeuserMitraDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.KeyV3Dao;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JSAuth;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.KeyV3;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;



public class SignDocMitraV extends ActionSupport {
	User userRecv;
	//Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx=null;
	String kelas="apiMitra.SignDocMitraV";
	String trxType="PRC-SGN";
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		refTrx="SGN"+sdfDate2.format(tgl).toString();
		DB db = getDB(context);
        JSAuth auth=new JSAuth(db);
        User user=null;
        boolean otp = false;
        boolean valid=false;
        String info="";
		int count = 21;
		HttpServletRequest  request  = context.getRequest();
		String rc="05";
		int status=0;
		JSONObject result=new JSONObject();
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		
		try{
			 result.put("refTrx", refTrx);
			 String process=request.getRequestURI().split("/")[2];
	         //System.out.println("PATH :"+request.getRequestURI());
	         LogSystem.info(request, "PATH :"+request.getRequestURI(), kelas, refTrx, trxType);
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         LogSystem.request(request, sb.toString(), kelas, refTrx, trxType);
	         //LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         /*
	         if(!auth.processAuth(object.getString("auth"))) {
	        	 sendRedirect (context, context.getRequest().getContextPath() 
	                     + "/stop.html"
	                 );
	         	return;
	         }
	         user=auth.getUser();
	         DocumentsAccessDao dao=new DocumentsAccessDao(db);
	         String idDoc=object.getString("idDoc");
	         if(!idDoc.equals(auth.getIdDoc())) {
	        	 sendRedirect (context, context.getRequest().getContextPath() 
	                     + "/stop.html"
	                 );
	         	return;
	         }
	         */
	         ConfirmCode cc = null;
	         ConfirmCodeDao ccd = new ConfirmCodeDao(db);
	         UserManager um=new UserManager(db);
	         EeuserMitraDao emdao=new EeuserMitraDao(db);
	         if(object.getString("userpwd")!=null && object.getString("usersign")!=null && object.getString("otp")!=null) {
	        	 //user=checkUser(object.getString("userpwd"), object.getString("usersign"), db);
	        	 user=um.findByUsernamePassword(object.getString("usersign"), object.getString("userpwd"));
	        	 //EeuserMitra em=emdao.findUserPwdMitra(object.getString("usersign"), object.getString("userpwd"), Long.valueOf(object.getString("mitra")));
	        	 //if(em!=null)user=em.getEeuser();
	        	 if(user!=null) {
	        		 //System.out.println("User valid");
	        		 LogSystem.info(request, "User valid",kelas, refTrx, trxType);
	        		 //check otp
	        		 cc = ccd.getLastOTP(user.getId());
	        		 if(cc!=null) {
	        			 if(cc.getCode().equals(object.getString("otp"))) {
	        				 otp=true;
		        			 valid=true;
	        			 }
	        			 else {
	        				 LogSystem.error(request, "OTP tidak valid",kelas, refTrx, trxType);
	        				 //System.out.println("Otp tidak valid");
		        			 result.put("result", "12");
		        			 result.put("notif", "kode OTP tidak valid");
		        			 context.put("trxjson", result.toString());
		        			 return;
	        			 }
	        			 
	        		 }
	        		 else {
	        			 LogSystem.error(request, "OTP tidak valid",kelas, refTrx, trxType);
	        			 //System.out.println("Otp tidak valid");
	        			 result.put("result", "12");
	        			 result.put("notif", "kode OTP tidak valid");
	        			 context.put("trxjson", result.toString());
	        			 return;
	        		 }
	        		 valid=true;
	        	 }
	        	 else {
	        		 LogSystem.error(request, "Password tidak valid",kelas, refTrx, trxType);
	        		 //System.out.println("user tidak valid");
	        		 result.put("result", "12");
        			 result.put("notif", "Password tidak valid");
        			 context.put("trxjson", result.toString());
        			 return;
	        	 }
	         }
	         
	         if(valid==true) {
	        	 
		         String idDoc=object.getString("idDoc");

		         //List<DocumentAccess>dList=dao.findDocAccessEEuser(idDoc, String.valueOf(user.getId()) ,user.getNick());
		         List<DocumentAccess>dList=dao.findDocEEuserSign(Long.valueOf(idDoc), user.getId());
		         LogSystem.info(request, "Jumlah list = " + dList.size(),kelas, refTrx, trxType);
		         //System.out.println("jumlah list = "+dList.size());
		         
		         boolean sign=false;
		         boolean signPrc=false;
		         BillingSystem bs;
		         int balance=0;
		         KillBillDocumentBug kd = null;
		         KillBillPersonalBug kp = null;
		         try {
		        	 kd=new KillBillDocumentBug();
			         kp=new KillBillPersonalBug();
				} catch (Exception e) {
					// TODO: handle exception
					LogSystem.error(request, "System timeout",kelas, refTrx, trxType);
					 result.put("result", "06");
	     			 result.put("status", status);
	     			 result.put("notif", "System timeout, mohon menunggu 10 menit kemudian");
	     			 context.put("trxjson", result.toString());
	     			 return;
				}
		         
		         Mitra mitra=dList.get(0).getDocument().getEeuser().getMitra();
		         int jmlttd=dList.size();
		         String inv=null;
		         InvoiceDao idao=new InvoiceDao(db);
		         if(dList.get(0).getDocument().getPayment()=='2') {
		        	 //bs=new BillingSystem(dList.get(0).getDocument().getEeuser());
		        	 balance=kp.getBalance("MT"+mitra.getId());
		        	 if(balance<jmlttd) {
		        		 LogSystem.error(request, "Balance ttd mitra tidak cukup",kelas, refTrx, trxType);
		        		 result.put("result", "61");
		     			 result.put("status", status);
		     			 result.put("notif", "balance ttd mitra tidak cukup");
		     			 context.put("trxjson", result.toString());
		     			 return;
		        	 }
		        	 
		        	 inv=kp.setTransaction("MT"+mitra.getId(), jmlttd);
		        	 String[] split=inv.split(" ");
		        	 
		        	 id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
					  ivc.setDatetime(new Date());
					  ivc.setAmount(jmlttd);
					  ivc.setEeuser(dList.get(0).getDocument().getEeuser());
					  ivc.setExternal_key("MT"+mitra.getId());
					  ivc.setTenant('1');
					  ivc.setTrx('2');
					  ivc.setKb_invoice(split[1]);
					  ivc.setDocument(dList.get(0).getDocument());
					  idao.create(ivc);
					  
					 inv=split[1];
					  
		        	 if(inv!=null) {
		        		 
		        		 SignDoc sd=new SignDoc();
		        		 for(DocumentAccess da:dList) {
		        			 try {
		        				 String version = "v2";
		        				 sign=sd.signDoc(user, da, inv, db, request, refTrx, version);
		        				 if(sign==false) {
		        					LogSystem.error(request, "Sign false, Gagal proses tandatangan",kelas, refTrx, trxType);
		        					result.put("result", "06");
					     			result.put("status", status);
					     			result.put("notif", "gagal proses sign");
					     			kp.reverseTransaction(inv);
					     			//idao.deleteWhere(inv);
					     			context.put("trxjson", result.toString());
					        		return;
		        				 }
							} catch (Exception e) {
								// TODO: handle exception
								LogSystem.error(request, "Gagal proses tandatangan",kelas, refTrx, trxType);
								e.printStackTrace();
								result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses sign");
				     			kp.reverseTransaction(inv);
				     			//idao.deleteWhere(inv);
				     			sign=false;
				     			context.put("trxjson", result.toString());
				        		return;
							}
		        			  
		        		 }
		        		 
		        	 }
		        	 
		         } else if(dList.get(0).getDocument().getPayment()=='3'){
		        	 balance=kd.getBalance("MT"+mitra.getId());
		        	 //System.out.println("balance dokumen = "+balance);
		        	  LogSystem.info(request, "Balance dokumen = "+balance,kelas, refTrx, trxType);
					  if(balance<1) {
						  LogSystem.error(request, "Balance doc mitra tidak cukup",kelas, refTrx, trxType);
						  result.put("result", "06");
			     		  result.put("status", status);
			     		  result.put("notif", "balance doc mitra tidak cukup");
			     		  context.put("trxjson", result.toString());
			     		  return;
					  }
					  
					  //List<DocumentAccess> ld=dao.findByDocSign(dList.get(0).getDocument().getId());
					  List<id.co.keriss.consolidate.ee.Invoice> li=idao.findByDoc(dList.get(0).getDocument().getId());
					  //if(ld.size()==0) {
					  if(li.size()==0) {
						  inv=kd.setTransaction("MT"+mitra.getId(), 1);
						  String[] split=inv.split(" ");
						  inv=split[1];
						  
						  id.co.keriss.consolidate.ee.Invoice ivc=new id.co.keriss.consolidate.ee.Invoice();
						  ivc.setDatetime(new Date());
						  ivc.setAmount(jmlttd);
						  ivc.setEeuser(dList.get(0).getDocument().getEeuser());
						  ivc.setExternal_key("MT"+mitra.getId());
						  ivc.setTenant('2');
						  ivc.setTrx('2');
						  ivc.setKb_invoice(split[1]);
						  ivc.setDocument(dList.get(0).getDocument());
						  idao.create(ivc);
						  
						  if(inv==null) {
							  	LogSystem.error(request, "Gagal proses sign [bill]",kelas, refTrx, trxType);
							  	result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses sign [bill]");
				     			kd.reverseTransaction(inv);
				     			//idao.deleteWhere(inv);
				     			context.put("trxjson", result.toString());
				        		return;
						  }
					  }
					  
					 SignDoc sd=new SignDoc();
	        		 for(DocumentAccess da:dList) {
	        			 try {
	        				 String version = "v2";
	        				 sign=sd.signDoc(user, da, inv, db, request, refTrx, version);
	        				 if(sign==false) {
	        					LogSystem.error(request, "Sign false, Gagal proses tandatangan",kelas, refTrx, trxType);
	        					result.put("result", "06");
				     			result.put("status", status);
				     			result.put("notif", "gagal proses sign");
				     			if(inv!=null) {
				     				kd.reverseTransaction(inv);
				     				//idao.deleteWhere(inv);
				     			}
				     			context.put("trxjson", result.toString());
				        		return;
	        				 }
						} catch (Exception e) {
							// TODO: handle exception
							LogSystem.error(request, "Gagal proses sign",kelas, refTrx, trxType);
							result.put("result", "06");
			     			result.put("status", status);
			     			result.put("notif", "gagal proses sign");
			     			if(inv!=null) {
			     				kd.reverseTransaction(inv);
			     				//idao.deleteWhere(inv);
			     			}
			        		sign=false;
			        		context.put("trxjson", result.toString());
			     			return;
						}
	        			  
	        		 }
		         }
		         
		         Long jml=(long) 1;
		         if(sign==true) {
		        	 LogSystem.info(request, "Proses tandantangan berhasil",kelas, refTrx, trxType);
		        	 rc="00";
		        	 status=00;
		        	 info="Proses tanda tangan berhasil!";
		        	 cc.setStatus("yes");
		        	 ccd.update(cc);
		        	 
		        	 jml=dao.getWaitingSignUserByDoc(idDoc);
			        	if(jml==0) {
			        		Documents doc=dList.get(0).getDocument();
			        		doc.setSign(true);
			        		new DocumentsDao(db).update(doc);
			        	}
		        	 
		        	DocumentAccess docac=dList.get(0);
		        	String iddoc=String.valueOf(docac.getDocument().getId());
		        	String namadoc=docac.getDocument().getFile_name();
		        	String namattd=docac.getName();
		        	String jkttd=String.valueOf(docac.getEeuser().getUserdata().getJk());
		        	String docs ="";
					String link ="";
					result.put("document_id", docac.getDocument().getIdMitra());
					try {
						   docs = AESEncryption.encryptDoc(iddoc);
						   link = "https://"+DSAPI.DOMAIN+"/doc/source.html?frmProcess=viewFile&doc="+iddoc;
						     //+ URLEncoder.encode(docs, "UTF-8");
						} catch (Exception e1) {
						   // TODO Auto-generated catch block
						   e1.printStackTrace();
						}
		        	SendNotifSignDoc std=new SendNotifSignDoc();
		        	List<DocumentAccess> vda=dao.findByDoc(iddoc);
		        	Vector<String> checkmail = new Vector();
		        	for(DocumentAccess da:vda) {
		        		boolean kirim=true;
		        		if(da.getEeuser()!=null) {
		        			
		        			for(String mail:checkmail) {
		        				if(mail.equalsIgnoreCase(da.getEmail())) {
		        					//std.kirim(da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link);
		        					kirim=false;
		        					break;
		        				}
		        			}
		        			if(kirim==true) {
		        				//if(mitra.isNotifikasi()) {
		        				//date
		        				//da.getName()
		        				LogSystem.info(request, "Kirim email notif sign",kelas, refTrx, trxType);
	        					LogSystem.info(request, "Nama di eeuser" + user.getName(),kelas, refTrx, trxType);
	        					std.kirim(da.getDate_sign(), user.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
	        					//std.kirim(da.getDate_sign(), da.getName(), String.valueOf(da.getEeuser().getUserdata().getJk()), da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
		        			
	        					checkmail.add(da.getEmail());
		        				//}	
		        			}
		        			
		        		} else {
		        			for(String mail:checkmail) {
		        				if(mail.equalsIgnoreCase(da.getEmail())) {
		        					//std.kirim(da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link);
		        					kirim=false;
		        					break;
		        				}
		        			}
		        			if(kirim==true) {
		        				//if(mitra.isNotifikasi()) {
		        				LogSystem.info(request, "Kirim email notif sign",kelas, refTrx, trxType);
	        					LogSystem.info(request, "Nama di eeuser" + user.getName(),kelas, refTrx, trxType);
	        					std.kirim(da.getDate_sign(), user.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
	        					//std.kirim(da.getDate_sign(), da.getName(), "", da.getEmail(), namattd, jkttd, namadoc, link, String.valueOf(mitra.getId()));
			        			checkmail.add(da.getEmail());
		        				//}
		        				
		        			}
		        			//
		        		}
		        		
		        	}
		         }
		         
		         
		         /*
		         int blc=bs.getBalance();

		         JSONArray arr=object.getJSONArray("user");
		         int jmlTTD=0;
		         for(DocumentAccess documentAccess: dList) {
		        	 for(int i=0; i<arr.length();i++) {
		        		 JSONObject d=(JSONObject) arr.get(i);
		        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
	        				
	        				 jmlTTD++;
		        		 }
		        	 }
			         
		         }
		         
		         System.out.println("Jumlah ttd = "+jmlTTD);
		         if(blc-jmlTTD>=0) {
		         
			         for(DocumentAccess documentAccess: dList) {
			        	 for(int i=0; i<arr.length();i++) {
			        		 JSONObject d=(JSONObject) arr.get(i);
			        		 if(documentAccess.getId().toString().equals(d.get("idAccess")) && d.get("sgn").equals("1")) {
		        				 Deposit dep=new Deposit(documentAccess,db);
		
		        				 if(dep.transaksi(1)) {
		        					 boolean res=signDoc(user, documentAccess,dep.getBillSys().getLastInvoice(),db);
				        			 if(res) {
				        				 signPrc=true;
				        			 }else {
				        				 dep.reversal();
				        				 result.put("info", "gagal tanda tangan, coba ulangi beberapa saat lagi");
				        			 }
			        			 }else{
			        				 info="transaksi gagal, coba ulangi beberapa saat lagi";
			        				 
			        				 if(!dep.getLastError().equals("error")) {
			        					 info=dep.getLastError();
			        				 }
		        					 result.put("info", info);
		        					 
			        			 }
		        				 
			        		 }
			        	 }
				         
			         }
		         }
		         else {
		        	 
					 result.put("info", "saldo anda kurang, silakan topup terlebih dahulu");
					 System.out.println("saldo anda kurang");
					 rc="E1";

		         }
		         
		         if(signPrc) {
		        	  
				      boolean sign = true ;
				      List<DocumentAccess> list = new DocumentsAccessDao(db).findByDoc(idDoc);
				      for (DocumentAccess doa : list) {
				    	  
				    	 if(doa.getEeuser()!=null) {

							 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getEeuser().getUserdata(), doa.getEmail());
							 mail.run();
				    	 }else {
				    		 MailSender mail=new MailSender(doa.getDocument().getFile_name(),user.getUserdata(),doa.getName(), doa.getEmail());
							 mail.run();
				    	 }
						 if(doa.getType().equals("sign") && doa.isFlag()==false) {
							sign=false; 
						 }
				       }
				      
				
		         
			        Documents docx=list.get(0).getDocument();
			        if(sign==true) {
			        	docx.setSign(true);
			        	status=1;
			        }
		    		new DocumentsDao(db).update(docx);
		    		
		    		
			        rc="00";
			        cc.setStatus("yes");
			        ccd.update(cc);
		         }
		         */
	         }
	         
		}catch (Exception e) {
            LogSystem.error(getClass(), e,kelas, refTrx, trxType);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
            e.printStackTrace();
		}
		
		
		try {
			result.put("result", rc);
			result.put("status", status);
			result.put("notif", info);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
		}
        context.getResponse().setContentType("application/json");
        LogSystem.info(request, "SEND :"+result.toString(),kelas, refTrx, trxType);
        //System.out.println("SEND :"+result.toString());
		context.put("trxjson", result.toString());
		LogSystem.response(request, result, kelas, refTrx, trxType);
		 HttpSession session          = context.getSession();
	        
        session.removeAttribute (USER);

	}
	
	private boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String tanggal = ftanggal.format(date);
		String signdoc = "DSSG" + strDate + ".pdf";
		String path = doc.getDocument().getPath();
		String original = doc.getDocument().getSigndoc();
		try {
			DocumentSigner dSign=new DocumentSigner();
			
			String version = "v2";

			List<Key> key = null;
			List<KeyV3> keyv3 = null;
			
		    KeyDao kdo = new KeyDao(db);
		    KeyV3Dao k3do = new KeyV3Dao(db);
		    
		    key = kdo.findByEeuser(userTrx.getId());
		    keyv3 = k3do.findByEeuser(userTrx.getId());
		    
		    if (key != null && key.size() > 0 )
		    {
		    	version = "v2";
		    }
		    
		    if (keyv3 != null && keyv3.size() > 0 )
		    {
		    	version = "v3";
		    }
			
		    
			JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc, version, refTrx);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
				doc.setFlag(true);
				doc.setDate_sign(date);
				doc.setInvoice(inv);
				new DocumentsAccessDao(db).update(doc);
				
				Documents documents=doc.getDocument();
				documents.setSigndoc(signdoc);
	    		new DocumentsDao(db).update(documents);

				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
		}
		return false;


	}
	
	class MailSender implements Runnable{

		private String to;
		private Userdata penerimaemail;
		private Userdata action;
		private String doc;
		private String penerimaEmailNotReg;
		
		public MailSender(String doc,Userdata action ,Userdata penerimaemail,String to) {
			this.to=to;
			this.penerimaemail=penerimaemail;
			this.action=action;
			this.doc=doc;
		}
		public MailSender(String doc,Userdata action ,String penerimaEmailNotReg,String to) {
			this.to=to;
			this.penerimaEmailNotReg=penerimaEmailNotReg;
			this.action=action;
			this.doc=doc;
		}
		@Override
		public void run() {
			if(penerimaemail!=null)
				new SendMailSSL().sendMailNotifSign(doc,action,penerimaemail, to);
			else
				new SendMailSSL().sendMailNotifSign(doc,action,penerimaEmailNotReg, to);

		}
		
	}
	
	User checkUser(String pwd, String email, DB db) {
		
		User user=new UserManager(db).findByUsername(email);
		//User user=new UserManager(db).findByUsernamePassword(email, pwd);
		
//		if(user!=null) {			  
//			  if(!user.getPassword().equals(pwd)) {
//				  user=null;  
//			  }
//		}
		return user;
	}
	
}
