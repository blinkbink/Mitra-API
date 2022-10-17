package apiMitra2020;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.killbill.billing.client.KillBillClientException;

import api.log.ActivityLog;
import apiMitra.SignDoc;
import id.co.keriss.consolidate.action.billing.Billing;
import id.co.keriss.consolidate.action.billing.KillBillDocumentHttps;
import id.co.keriss.consolidate.action.billing.KillBillPersonalHttps;
import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.InvoiceDao;
import id.co.keriss.consolidate.dao.SealDocAccessDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.SealDocAccess;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class ProsesSign {
	public JSONObject sign(String refTrx, DB db, Vector<DocumentAccess> vda, Documents doc, Mitra mitra, HttpServletRequest request, List<Integer> list_seq, boolean sequence, boolean autoseal, String version, SealDocAccess sda, JSONObject jsonRecv, int countAutoSign, int jmlpage, User usr) throws Exception {
		JSONObject job=new JSONObject();
		String kelas="apiMitra2020/ProsesSignAT";
		String trxType="SEND-DOC";
		
//		if(doc.getSequence()) {
//			job = withSequence(refTrx, db, vda, doc, request);
//		} else {
			DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
			DocumentsDao ddao=new DocumentsDao(db);
			InvoiceDao invDao=new InvoiceDao(db);
			String inv=null;
			int current_balance=0;
			long amount=0;
			Date tglbilling=new Date();
			
			
			KillBillDocumentHttps kdh = null;
			KillBillPersonalHttps kph = null;
			  try {
				  kdh=new KillBillDocumentHttps(request, refTrx);
				  kph=new KillBillPersonalHttps(request, refTrx);
			  } catch (Exception e) {
				// TODO: handle exception
				  dAccessDao.deleteWhere(doc.getId());
				  File file=new File(doc.getPath()+doc.getRename());
				  file.delete();
				  ddao.delete(doc);
					
		  			try {
						job.put("result", "91");
						job.put("notif", "System timeout. silahkan coba kembali.");
						return job;
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
			  }
			  
			  boolean billdoc=false;
			  boolean billstempel=false;
			  boolean billmeterai=false;
			  String invstempel = null;
			  String invmeterai = null;
			  try {
				  
				  if(autoseal==true) {
					amount=vda.size()-1;
				  } else {
					  amount = vda.size();
				  }
				  
					if(amount>0) {
						billdoc=true;
						Invoice ic=new Invoice();
						if(doc.getPayment()=='2') {
							LogSystem.info(request, "payment per sign", kelas, refTrx, trxType);
							//amount=vda.size();
							
							JSONObject obj=null;
							try {
								obj = kph.setTransaction("MT"+mitra.getId(), (int)amount, String.valueOf(doc.getId()));
							} catch (KillBillClientException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(obj!=null) {
								if(obj.has("result")) {
									  String result=obj.getString("result");
									  if(result.equals("00")) {
											inv=obj.getString("invoiceid");
											current_balance=obj.getInt("current_balance");				
											ic.setTenant('1');
											try {
												tglbilling=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("datetime"));
											} catch (ParseException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										} else if(result.equals("05")) {
											if(sda != null)
										  	{
										  		new SealDocAccessDao(db).delete(sda);
										  	}
											dAccessDao.deleteWhere(doc.getId());
											File file=new File(doc.getPath()+doc.getRename());
											file.delete();
											ddao.delete(doc);
											
											job.put("result", "61");
										    job.put("notif", "Saldo Tandatangan mitra tidak cukup.");
										    return job;
										} else {
											if(sda != null)
										  	{
										  		new SealDocAccessDao(db).delete(sda);
										  	}
											dAccessDao.deleteWhere(doc.getId());
											File file=new File(doc.getPath()+doc.getRename());
											file.delete();
											ddao.delete(doc);
											
								  			job.put("result", "91");
											job.put("notif", "System timeout. silahkan coba kembali.");
											return job;
										}
								  } else {
									  	dAccessDao.deleteWhere(doc.getId());
										File file=new File(doc.getPath()+doc.getRename());
										file.delete();
										ddao.delete(doc);
										
							  			job.put("result", "91");
										job.put("notif", "System timeout. silahkan coba kembali.");
										return job;
								  }
							} else {
								if(sda != null)
							  	{
							  		new SealDocAccessDao(db).delete(sda);
							  	}
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								ddao.delete(doc);
								
					  			job.put("result", "91");
								job.put("notif", "System timeout. silahkan coba kembali.");
								return job;
							}
						} else if(doc.getPayment()=='3') {
							LogSystem.info(request, "payment per document", kelas, refTrx, trxType);
							amount=1;
							JSONObject obj=null;
							try {
								obj = kdh.setTransaction("MT"+mitra.getId(), 1, String.valueOf(doc.getId()));
							} catch (KillBillClientException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(obj!=null) {
								if(obj.has("result")) {
									  String result=obj.getString("result");
									  if(result.equals("00")) {
											inv=obj.getString("invoiceid");
											current_balance=obj.getInt("current_balance");				
											ic.setTenant('2');
											try {
												tglbilling=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("datetime"));
											} catch (ParseException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										} else if(result.equals("05")) {
											if(sda != null)
										  	{
										  		new SealDocAccessDao(db).delete(sda);
										  	}
											dAccessDao.deleteWhere(doc.getId());
											File file=new File(doc.getPath()+doc.getRename());
											file.delete();
											ddao.delete(doc);
											
											job.put("result", "61");
										    job.put("notif", "Saldo Dokumen mitra tidak cukup.");
										    return job;
										} else {
											if(sda != null)
										  	{
										  		new SealDocAccessDao(db).delete(sda);
										  	}
											dAccessDao.deleteWhere(doc.getId());
											File file=new File(doc.getPath()+doc.getRename());
											file.delete();
											ddao.delete(doc);
											
								  			job.put("result", "91");
											job.put("notif", "System timeout. silahkan coba kembali.");
											return job;
										}
								  } else {
									  if(sda != null)
									  	{
									  		new SealDocAccessDao(db).delete(sda);
									  	}
									  	dAccessDao.deleteWhere(doc.getId());
										File file=new File(doc.getPath()+doc.getRename());
										file.delete();
										ddao.delete(doc);
										
							  			job.put("result", "91");
										job.put("notif", "System timeout. silahkan coba kembali.");
										return job;
								  }
							} else {
								if(sda != null)
							  	{
							  		new SealDocAccessDao(db).delete(sda);
							  	}
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								ddao.delete(doc);
								
					  			job.put("result", "91");
								job.put("notif", "System timeout. silahkan coba kembali.");
								return job;
							}
						}
						
						ic.setAmount(amount);
						ic.setCur_balance(current_balance);
						ic.setDatetime(tglbilling);
						ic.setDocument(doc);
						ic.setEeuser(doc.getEeuser());
						ic.setExternal_key("MT"+mitra.getId());
						ic.setTrx('2');
						ic.setKb_invoice(inv);
						
						invDao.create(ic);
					}
				  
				  
				  	if(autoseal==true) {
				  		billstempel=true;
				  		Billing bill = new Billing(request, refTrx);
				  		LogSystem.info(request, "payment SEAL", kelas, refTrx, trxType);
				  		int current_balance_stempel=0;
						//amount=1;
						JSONObject obj=null;
						try {
							obj = bill.setTransaction("MT"+mitra.getId(), 1, String.valueOf(doc.getId()), "stempel");
						} catch (KillBillClientException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(obj!=null) {
							if(obj.has("result")) {
								  String result=obj.getString("result");
								  if(result.equals("00")) {
									  	invstempel=obj.getString("invoiceid");
										current_balance_stempel=obj.getInt("current_balance");				
										//ic.setTenant('6');
										try {
											tglbilling=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("datetime"));
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									} else if(result.equals("05")) {
										if(sda != null)
									  	{
									  		new SealDocAccessDao(db).delete(sda);
									  	}
										dAccessDao.deleteWhere(doc.getId());
										File file=new File(doc.getPath()+doc.getRename());
										file.delete();
										ddao.delete(doc);
										
										job.put("result", "61");
									    job.put("notif", "Saldo Segel mitra tidak cukup.");
									    return job;
									} else {
										if(sda != null)
									  	{
									  		new SealDocAccessDao(db).delete(sda);
									  	}
										dAccessDao.deleteWhere(doc.getId());
										File file=new File(doc.getPath()+doc.getRename());
										file.delete();
										ddao.delete(doc);
										
							  			job.put("result", "91");
										job.put("notif", "System timeout. silahkan coba kembali.");
										return job;
									}
							  } else {
								  if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
								  	dAccessDao.deleteWhere(doc.getId());
									File file=new File(doc.getPath()+doc.getRename());
									file.delete();
									ddao.delete(doc);
									
						  			job.put("result", "91");
									job.put("notif", "System timeout. silahkan coba kembali.");
									return job;
							  }
						} else {
							if(sda != null)
						  	{
						  		new SealDocAccessDao(db).delete(sda);
						  	}
							dAccessDao.deleteWhere(doc.getId());
							File file=new File(doc.getPath()+doc.getRename());
							file.delete();
							ddao.delete(doc);
							
				  			job.put("result", "91");
							job.put("notif", "System timeout. silahkan coba kembali.");
							return job;
						}
				  		
						LogSystem.info(request, "save invoice stempel", kelas, refTrx, trxType);
						Invoice inv2=new Invoice();
						inv2.setAmount(1);
						inv2.setCur_balance(current_balance_stempel);
						inv2.setDatetime(tglbilling);
						inv2.setDocument(doc);
						inv2.setEeuser(doc.getEeuser());
						inv2.setExternal_key("MT"+mitra.getId());
						inv2.setTrx('2');
						inv2.setKb_invoice(invstempel);
						inv2.setTenant('6');
						
						invDao.create(inv2);
				  	}
				  	
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			  	
				
				SignDoc sd=new SignDoc();
				int sequenceNext=0;
				int loop=0;
				int tidakJadi=vda.size();
				LogSystem.info(request, "jumlah yg harus di ttd auto= "+tidakJadi, kelas, refTrx, trxType);
				LogSystem.info(request, "jumlah list sequence= "+list_seq.size(), kelas, refTrx, trxType);
				//int jmldiTTD=0;
				Vector<DocumentAccess> lda=new Vector<>();
				Vector<DocumentAccess> ldseal=new Vector<>();
				User user = null;

				List<Long> idUserSign = new ArrayList<>();
				List<String> signType = new ArrayList<>();
				
				for (int i = 0 ; i < vda.size() ; i++)
				{
					if(!idUserSign.contains(vda.get(i).getEeuser().getId()))
					{
						idUserSign.add(vda.get(i).getEeuser().getId());
					}
				}
				
				//Proses meterai kalo ada setelah transaksi ke billing
				//*saldo habis / tidak langsung dari billing
				//*otomatis kirim notifikasi email peringatan saldo dari billing
				//Check meterai
				  if(jsonRecv.has("meterai")) 
				  {
						JSONObject jsonMeterai = new JSONObject(jsonRecv.getString("meterai"));
						boolean sign=false;		
						DocumentAccess da=new DocumentAccess();
						
						if(jsonMeterai.getString("aksi").equalsIgnoreCase("at"))
						{
							LogSystem.info(request, "Proses meterai AT", kelas, refTrx,trxType);
							sign=true;
						}
						else
						{
							LogSystem.info(request, "Proses meterai MT", kelas, refTrx,trxType);
						}
						
						if(countAutoSign > 0)
						{
							if(jsonMeterai.getString("aksi").equalsIgnoreCase("mt"))
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(doc);
								
								job.put("result", "07");
				                job.put("notif", "Tidak dapat melakukan tandatangan otomatis sebelum meterai elektronik");
				                
				                try {
						        	ActivityLog logSystem = new ActivityLog(request, refTrx);
						        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + job.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
						        }catch(Exception e)
						        {
						        	e.printStackTrace();
						        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
						        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
						        }
				                
				                return job;
							}
							else
							{
								sign=true;
							}
						}
						
						da.setAction(jsonMeterai.getString("aksi"));
						
						if(Integer.parseInt(jsonMeterai.getString("page"))>jmlpage) {
							
							if(billdoc)
								reversal(doc, inv, amount, kph, kdh, db, mitra, true);
							
							if(billstempel)
								reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
							
							dAccessDao.deleteWhere(doc.getId());
							File file=new File(doc.getPath()+doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(doc);
							
							job.put("result", "FE");
			                job.put("notif", "Halaman meterai elektronik, melebihi dari jumlah halaman dokumen");
			                
			                try {
					        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + job.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
					        }catch(Exception e)
					        {
					        	e.printStackTrace();
					        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
					        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
					        }
			                
			                return job;
						}
						
						if(!isValid(jsonRecv.getString("userid").toLowerCase())) {
							
							if(billdoc)
								reversal(doc, inv, amount, kph, kdh, db, mitra, true);
							
							if(billstempel)
								reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
							
							dAccessDao.deleteWhere(doc.getId());
							File file=new File(doc.getPath()+doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(doc);
							
							job.put("result", "FE");
			                job.put("notif", "Format email salah.");
			                
			                try {
					        	ActivityLog logSystem = new ActivityLog(request, refTrx);
					        	logSystem.POST("upload", "failed", "[API] Gagal upload dokumen, " + job.getString("notif"), Long.toString(usr.getId()), null, null, null, null,null);
					        }catch(Exception e)
					        {
					        	e.printStackTrace();
					        	LogSystem.error(request, "Gagal mengirim ke Log API", kelas, refTrx,trxType);
					        	LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
					        }
			                
			                return job;
						}
						da.setDocument(doc);
						da.setEeuser(usr);
						da.setName(usr.getName());
						da.setFlag(false);
						da.setType("meterai");
						boolean meterai = true;
						da.setDate_sign(null);
						da.setEmail(jsonRecv.getString("userid").toLowerCase().trim());
					
						da.setPage(Integer.parseInt(jsonMeterai.getString("page")));
						da.setLx(jsonMeterai.getString("llx"));
						da.setLy(jsonMeterai.getString("lly"));
						da.setRx(jsonMeterai.getString("urx"));
						da.setRy(jsonMeterai.getString("ury"));
						da.setDatetime(new Date());
						da.setVisible(true);
						
						Long idAcc = null;
						try
						{
							idAcc=dAccessDao.create(da);
							
							if(idAcc == null)
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(doc);
								LogSystem.error(request, "idAcc null",kelas, refTrx, trxType); 
								job.put("result", "06");
				                job.put("notif", "Kirim dokumen gagal"); 
				                
				                return job;
							}
						}catch(Exception e)
						{
							if(billdoc)
								reversal(doc, inv, amount, kph, kdh, db, mitra, true);
							
							if(billstempel)
								reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
							LogSystem.error(request, e.toString(), kelas, refTrx,trxType);
							File file=new File(doc.getPath()+doc.getRename());
							file.delete();
							new DocumentsDao(db).delete(doc);
							dAccessDao.deleteWhere(doc.getId());
							job.put("result", "06");
			                job.put("notif", "Kirim dokumen gagal");
			                return job;
						}
						
						DocumentAccess dac = null;
						
						if(sign==true) {
							dac=dAccessDao.findbyId(idAcc);
							 
							Userdata udata=new Userdata();
							boolean reg=false;

							String OriginPath = doc.getSigndoc();
							String path = doc.getPath();
							Date date = new Date();
							SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
							String strDate = sdfDate.format(date);
							String signdoc = "APIA" + strDate+doc.getId() +".pdf";
							
							SaveFileWithSamba samba=new SaveFileWithSamba();
							byte[] encoded = Base64.encode(samba.openfile(path+OriginPath));
							String base64Document = new String(encoded, StandardCharsets.US_ASCII);
							
							float llx = Float.parseFloat(dac.getLx());
							float lly = Float.parseFloat(dac.getLy());
							float urx = Float.parseFloat(dac.getRx());
							float ury = Float.parseFloat(dac.getRy());
							
							JSONObject resSign = null;
							DocumentSigner ds = new DocumentSigner();
							try {
								resSign = ds.kirimMeterai(request, refTrx, doc.getId(), doc.getFile_name(), doc.getEeuser().getMitra().getId(), llx, lly, urx, ury,jsonMeterai.getInt("page"), base64Document, doc.getEeuser().getMitra().getProvinsi().getName());
								
							}catch(Exception e)
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								e.printStackTrace();
								// TODO: handle exception
								LogSystem.error(request, "Error save file meterai"+e.toString(), kelas, refTrx, trxType);
						  	  	job.put("result", "91");
								job.put("notif", "System timeout. silahkan coba kembali.");
								if(sda != null)
							  	{
							  		new SealDocAccessDao(db).delete(sda);
							  	}
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(doc);
								return job;
							}
							
							if(resSign == null)
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								LogSystem.error(request, "Response meterai null", kelas, refTrx, trxType);
						  	  	job.put("result", "91");
								job.put("notif", "System timeout. silahkan coba kembali.");
								if(sda != null)
							  	{
							  		new SealDocAccessDao(db).delete(sda);
							  	}
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(doc);
								return job;
							}
							
							if(resSign.getString("information").equalsIgnoreCase("saldo tidak mencukupi"))
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								job.put("result", "60");
								job.put("notif", "Saldo meterai elektronik tidak mencukupi");
								if(sda != null)
							  	{
							  		new SealDocAccessDao(db).delete(sda);
							  	}
								
								LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(doc);
								
								return job;
							}
							
							if(resSign.getString("information").equalsIgnoreCase("response code: 413 Request Entity Too Large"))
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								
								job.put("result", "13");
								job.put("notif", "Kirim dokumen gagal");
								job.put("info", "Ukuran file PDF untuk proses meterai elektronik maksimal 10Mb");
								
								LogSystem.info(request, job.toString(), kelas, refTrx, trxType);
								if(sda != null)
							  	{
							  		new SealDocAccessDao(db).delete(sda);
							  	}
								dAccessDao.deleteWhere(doc.getId());
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								new DocumentsDao(db).delete(doc);
								
								return job;
							}
							
							LogSystem.info(request, "Response meterai : "+resSign.getString("result") + " information " + resSign.getString("information"), kelas, refTrx, trxType);
							
							boolean resp=false;
							
							if(resSign.getString("result").equals("00"))
							{
							    try {
							    	byte[] finalDoc = Base64.decode(resSign.getString("final_document"));
								    resp=samba.write(finalDoc, path+signdoc);
								    LogSystem.info(request, "hasil save File : "+resp, kelas, refTrx, trxType);
								    if(resp==false) {
								    	if(billdoc)
											reversal(doc, inv, amount, kph, kdh, db, mitra, true);
										
										if(billstempel)
											reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								  	  	LogSystem.error(request, "error samba proses meterai", kelas, refTrx, trxType);
								  	  	job.put("result", "91");
										job.put("notif", "System timeout. silahkan coba kembali.");
										if(sda != null)
									  	{
									  		new SealDocAccessDao(db).delete(sda);
									  	}
										dAccessDao.deleteWhere(doc.getId());
										File file=new File(doc.getPath()+doc.getRename());
										file.delete();
										new DocumentsDao(db).delete(doc);
										
										return job;
								    }
								    finalDoc = null;
								} catch (Exception e) {
									e.printStackTrace();
									// TODO: handle exception
									
									if(billdoc)
										reversal(doc, inv, amount, kph, kdh, db, mitra, true);
									
									if(billstempel)
										reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
									
									LogSystem.error(request, "Error save file meterai"+e.toString(), kelas, refTrx, trxType);
							  	  	job.put("result", "91");
									job.put("notif", "System timeout. silahkan coba kembali.");
									if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
									File file=new File(doc.getPath()+doc.getRename());
									file.delete();
									dAccessDao.deleteWhere(doc.getId());
									new DocumentsDao(db).delete(doc);
									return job;
								}
							    
								try {
									doc.setSigndoc(signdoc);
									new DocumentsDao(db).update(doc);
									dac.setDate_sign(new Date());
									dac.setFlag(true);
									
									dAccessDao.update(dac);
									
									LogSystem.info(request, "Proses meterai berhasil ", kelas, refTrx, trxType);
								} catch (Exception e) {
									// TODO: handle exception
									if(billdoc)
										reversal(doc, inv, amount, kph, kdh, db, mitra, true);
									
									if(billstempel)
										reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
									
									LogSystem.info(request, "DB Timeout", kelas, refTrx, trxType);
							  	  	job.put("result", "91");
									job.put("notif", "System timeout. silahkan coba kembali.");
									if(sda != null)
								  	{
								  		new SealDocAccessDao(db).delete(sda);
								  	}
									dAccessDao.deleteWhere(doc.getId());
									File file=new File(doc.getPath()+doc.getRename());
									file.delete();
									new DocumentsDao(db).delete(doc);
									return job;
								}
							}
							else
							{
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
								
								LogSystem.info(request, "Response Meterai tidak berhasil ", kelas, refTrx, trxType);
						  	  	job.put("result", "91");
								job.put("notif", "System timeout. silahkan coba kembali.");
								File file=new File(doc.getPath()+doc.getRename());
								file.delete();
								dAccessDao.deleteWhere(doc.getId());
								new DocumentsDao(db).delete(doc);
								
								return job;
							}
						}
					}
				//Selesai cek meterai
				
				LogSystem.info(request, "List User Sign " + idUserSign, kelas, refTrx, trxType);
				try {
					LogSystem.info(request, "Total doc access " + vda.size(), kelas, refTrx, trxType);
					for(int i=0 ; i < idUserSign.size();)
					{
						for(int j = 0 ; j < vda.size() ; j++)
						{		
							if(idUserSign.get(i).equals(vda.get(j).getEeuser().getId()))
							{
								LogSystem.info(request, "Doc Access " + vda.get(j).getId() + " Type " + vda.get(j).getType() , kelas, refTrx, trxType); 
								if(vda.get(j).getType().equalsIgnoreCase("seal"))
								{
									ldseal.add(vda.get(j));
								}
								else
								{
									lda.add(vda.get(j));
								}
								
								user=vda.get(j).getEeuser();
								//check Sequence
								  if(sequence==true) {
									  LogSystem.info(request, "loop ke = "+loop, kelas, refTrx, trxType); 
									  if(vda.get(j).getSequence_no()>doc.getCurrent_seq()) {
										  LogSystem.info(request, "Tidak jadi karena lebih dari current sequence",kelas, refTrx, trxType);
										  loop++;
										  
										  break;
									  }
									  else {
										  //int ambil=loop++;;
										  if(loop<vda.size()) {
											  if(loop+1>list_seq.size()-1) {
												  sequenceNext=list_seq.get(loop);
											  } else
											  sequenceNext=list_seq.get(loop+1);
										  }
										  
										  loop++;
									  }
								  }
							}
						}
						
						
						if(lda.size()>0)
						{
							LogSystem.info(request, "List doc access" + lda.size(),kelas, refTrx, trxType);
							LogSystem.info(request, "Proses sign", kelas, refTrx, trxType);
							if(sd.signDoc3(user,lda,inv,db,request,refTrx,doc,invstempel, version, invmeterai)) {
								LogSystem.info(request, "ttd otomatis berhasil " + user + " doc access " + lda, kelas, refTrx, trxType);
								tidakJadi--;
								if(sequence) {
									doc.setCurrent_seq(sequenceNext);
									ddao.update(doc);
								}
							} else {
								if(billdoc)
									reversal(doc, inv, amount, kph, kdh, db, mitra, true);
								
								if(billstempel)
									reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
		
								try {
									job.put("result", "91");
									job.put("notif", "System timeout. silahkan coba kembali.");
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return job;
							}		
						}
						lda.removeAllElements();
						idUserSign.remove(0);
					}
				}catch(Exception e)
				{
					e.printStackTrace();
					throw new Exception(e.toString());
				}
				
				
				if(ldseal.size()>0)
				{
					LogSystem.info(request, "List doc access seal" + ldseal.size(),kelas, refTrx, trxType);
					LogSystem.info(request, "Proses seal", kelas, refTrx, trxType);
					if(sd.signDoc3(user,ldseal,inv,db,request,refTrx,doc, invstempel, version, invmeterai)) {
						LogSystem.info(request, "Seal otomatis berhasil " + user + " doc access " + ldseal, kelas, refTrx, trxType);
						tidakJadi--;
						if(sequence) {
							doc.setCurrent_seq(sequenceNext);
							ddao.update(doc);
						}
					} else {
						if(billdoc)
							reversal(doc, inv, amount, kph, kdh, db, mitra, true);
						
						if(billstempel)
							reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);

						try {
							job.put("result", "91");
							job.put("notif", "System timeout. silahkan coba kembali.");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return job;
					}		
					ldseal.removeAllElements();
				}
				
//				for(DocumentAccess da:vda) {
//					lda.add(da);
//					
//					user=da.getEeuser();
//					//check Sequence
//					  if(sequence==true) {
//						  LogSystem.info(request, "loop ke = "+loop, kelas, refTrx, trxType); 
//						  if(da.getSequence_no()>doc.getCurrent_seq()) {
//							  LogSystem.info(request, "Tidak jadi karena lebih dari current sequence",kelas, refTrx, trxType);
//							  loop++;
//							  
//							  break;
//						  }
//						  else {
//							  //int ambil=loop++;;
//							  if(loop<vda.size()) {
//								  if(loop+1>list_seq.size()-1) {
//									  sequenceNext=list_seq.get(loop);
//								  } else
//								  sequenceNext=list_seq.get(loop+1);
//							  }
//							  
//							  loop++;
//						  }
//					  }
//				
//				
//					//if(sd.signDoc2(da.getEeuser(),lda,inv,db,request,refTrx,doc)) {
//					if(sd.signDoc3(user,lda,inv,db,request,refTrx,doc,invstempel, version, invmeterai)) {
//						LogSystem.info(request, "ttd otomatis berhasil", kelas, refTrx, trxType);
//						tidakJadi--;
//						if(sequence) {
//							doc.setCurrent_seq(sequenceNext);
//							ddao.update(doc);
//						}
//					} else {
//						if(billdoc)
//							reversal(doc, inv, amount, kph, kdh, db, mitra, true);
//						
//						if(billstempel)
//							reversalstempel(doc, invstempel, 1, kph, kdh, db, mitra, true);
//
//						try {
//							job.put("result", "91");
//							job.put("notif", "System timeout. silahkan coba kembali.");
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//						return job;
//					}								
//				}
				
				//jika ada yg batal sign at karena masalah sequence
				if(tidakJadi>0) {
					if(doc.getPayment()=='2')
					reversal(doc, inv, tidakJadi, kph, kdh, db, mitra, false);
					if(tidakJadi==vda.size()&&doc.getPayment()=='3')reversal(doc, inv, 1, kph, kdh, db, mitra, false);
				}
				
				try {
					job.put("result", "00");
					job.put("notif", "proses sign berhasil");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		//}
		
		return job;
	}
	
	void reversal(Documents doc, String inv, long amount, KillBillPersonalHttps kph, KillBillDocumentHttps kdh, DB db, Mitra mitra, boolean deleteData) {
		JSONObject bill=new JSONObject();
		Invoice invo=new Invoice();
		DocumentsDao ddao=new DocumentsDao(db);
		DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
		InvoiceDao invDao=new InvoiceDao(db);
		if(doc.getPayment()=='2') {
			try {
				bill=kph.reverseTransaction(inv, (int) amount, String.valueOf(doc.getId()));
			} catch (KillBillClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			invo.setTenant('1');
		} else if (doc.getPayment()=='3') {
			try {
				bill=kdh.reverseTransaction(inv, 1, String.valueOf(doc.getId()));
			} catch (KillBillClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			invo.setTenant('2');
		}
		
		
		if(deleteData) {
			doc.setDelete(true);
			ddao.update(doc);
			
			dAccessDao.deleteWhere(doc.getId());
			File file=new File(doc.getPath()+doc.getRename());
			file.delete();
		}
		
		
		invo.setAmount(amount);
		try {
			invo.setDatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bill.getString("datetime")));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			invo.setDatetime(new Date());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			invo.setDatetime(new Date());
		}
		invo.setDocument(doc);
		invo.setExternal_key("MT"+mitra.getId());
		invo.setKb_invoice(inv);
		invo.setTrx('3');
		if(bill.has("current_balance")) {
			try {
				invo.setCur_balance(bill.getInt("current_balance"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		invDao.create(invo);
	}
	
	void reversalstempel(Documents doc, String inv, long amount, KillBillPersonalHttps kph, KillBillDocumentHttps kdh, DB db, Mitra mitra, boolean deleteData) {
		JSONObject bill=new JSONObject();
		Invoice invo=new Invoice();
		DocumentsDao ddao=new DocumentsDao(db);
		DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
		InvoiceDao invDao=new InvoiceDao(db);
		
		try {
			bill=kdh.reverseTransaction(inv, 1, String.valueOf(doc.getId()));
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		invo.setTenant('6');
		
		
		if(deleteData) {
			doc.setDelete(true);
			ddao.update(doc);
			
			//dAccessDao.deleteWhere(doc.getId());
			File file=new File(doc.getPath()+doc.getRename());
			file.delete();
		}
		
		
		invo.setAmount(amount);
		try {
			invo.setDatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bill.getString("datetime")));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			invo.setDatetime(new Date());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			invo.setDatetime(new Date());
		}
		invo.setDocument(doc);
		invo.setExternal_key("MT"+mitra.getId());
		invo.setKb_invoice(inv);
		invo.setTrx('3');
		if(bill.has("current_balance")) {
			try {
				invo.setCur_balance(bill.getInt("current_balance"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		invDao.create(invo);
	}
	

	void reversalmeterai(Documents doc, String inv, long amount, KillBillPersonalHttps kph, KillBillDocumentHttps kdh, DB db, Mitra mitra, boolean deleteData) {
		JSONObject bill=new JSONObject();
		Invoice invo=new Invoice();
		DocumentsDao ddao=new DocumentsDao(db);
		DocumentsAccessDao dAccessDao=new DocumentsAccessDao(db);
		InvoiceDao invDao=new InvoiceDao(db);
		
		try {
			bill=kdh.reverseTransaction(inv, 1, String.valueOf(doc.getId()));
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		invo.setTenant('3');
		
		
		if(deleteData) {
			doc.setDelete(true);
			ddao.update(doc);
			
			//dAccessDao.deleteWhere(doc.getId());
			File file=new File(doc.getPath()+doc.getRename());
			file.delete();
		}
		
		
		invo.setAmount(amount);
		try {
			invo.setDatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bill.getString("datetime")));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			invo.setDatetime(new Date());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			invo.setDatetime(new Date());
		}
		invo.setDocument(doc);
		invo.setExternal_key("MT"+mitra.getId());
		invo.setKb_invoice(inv);
		invo.setTrx('3');
		if(bill.has("current_balance")) {
			try {
				invo.setCur_balance(bill.getInt("current_balance"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		invDao.create(invo);
	}
	
	JSONObject withSequence(String refTrx, DB db, Vector<DocumentAccess> vda, Documents doc, HttpServletRequest request) {
		JSONObject resp=null;
		return resp;
	}
	
	
	public static boolean isValid(String email) 
    { 
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
                            "[a-zA-Z0-9_+&*-]+)*@" + 
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
                            "A-Z]{2,9}$"; 
                            
        Pattern pat = Pattern.compile(emailRegex); 
        if (email == null) 
            return false; 
        return pat.matcher(email).matches(); 
    } 
	
}
