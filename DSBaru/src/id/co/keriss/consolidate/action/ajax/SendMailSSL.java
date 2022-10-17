package id.co.keriss.consolidate.action.ajax;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jpos.ee.DB;
import org.jpos.ee.User;

import com.ibm.icu.text.SimpleDateFormat;

import id.co.keriss.consolidate.dao.BankDao;
import id.co.keriss.consolidate.ee.Payment;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SystemUtil;

public class SendMailSSL implements DSAPI{
	
	private static final String emailName="Digisign";
	private static final String email="noreply@digisign.id";
	private static final String pass="password123";
	private Properties props = null;

	public SendMailSSL() {
		props = new Properties();
		props.put("mail.smtp.host", "mail.digisign.id");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
	}
	
	private void sendMail(Message msg) throws MessagingException {
		Thread mailThr=new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Transport.send(msg);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		mailThr.start();

	}
	
	/**
	 * Setelah equest pembayaran
	 * @param user
	 * @param to
	 */
	public void sendMailPayRequest(DB db, Payment payment, String to) { 
		
		SimpleDateFormat sdf=new SimpleDateFormat("DDDHHmmss");

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {
			Userdata user=payment.getEeuser().getUserdata();
			String title="Tagihan";
			
			String invoice="PY"+sdf.format(payment.getDate_request());
			String kode_produk=payment.getProduct_code();
			String bank=payment.getBank_to();
			String rekening=new BankDao(db).findByName(bank).getRekening();
			String harga=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount_original()));
			long kodeAmt=payment.getAmount().longValue()-payment.getAmount_original().longValue();
			String kode=String.valueOf(kodeAmt);
			String amount_digit= String.valueOf(payment.getAmount());
			String amount=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount()));
			String buttonName="Konfirmasi Pembayaran";
			String link="https://"+DOMAIN;
			
			String content=getContentTrx(invoice, kode_produk, bank, rekening, harga, kode, amount_digit, amount, "<b>Menunggu pembayaran</b>");
			content=content+"<b>amount harus sesuai sampai digit terakhir, Silakan lakukan konfirmasi setelah anda melakukan pembayaran</b>";
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

			String body = getContentBody(title, kel + user.getNama(), content, buttonName, link);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	
	/**
	 * konfirmasi
	 * @param user
	 * @param to
	 */
	public void sendMailPaySuccess(DB db,Payment payment, String to) { 
		
		SimpleDateFormat sdf=new SimpleDateFormat("DDDHHmmss");

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {
			Userdata user=payment.getEeuser().getUserdata();
			String title="Verifikasi Pembayaran Berhasil";
			
			String invoice="PY"+sdf.format(payment.getDate_request());
			String kode_produk=payment.getProduct_code();
			String bank=payment.getBank_to();
			String rekening=new BankDao(db).findByName(bank).getRekening();
			String harga=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount_original()));
			long kodeAmt=payment.getAmount().longValue()-payment.getAmount_original().longValue();
			String kode=String.valueOf(kodeAmt);
			String amount_digit= String.valueOf(payment.getAmount());
			String amount=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount()));
			String buttonName="View Document";
			String link="https://"+DOMAIN;
			
			String content=getContentTrx(invoice, kode_produk, bank, rekening, harga, kode, amount_digit, amount, "<b>Selesai</b>");
			content="Pembayaran anda sudah berhasil kami verifikasi saldo tanda tangan anda sudah bertambah "+content;
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

			String body = getContentBody(title, kel + user.getNama(), content, null, null);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	/**
	 * konfirmasi
	 * @param user
	 * @param to
	 */
	public void sendMailPayKonfirmasi(DB db, Payment payment, String to) { 
		
		SimpleDateFormat sdf=new SimpleDateFormat("DDDHHmmss");

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {
			Userdata user=payment.getEeuser().getUserdata();
			String title="Notifikasi Verifikasi Pembayaran";
			
			String invoice="PY"+sdf.format(payment.getDate_request());
			String kode_produk=payment.getProduct_code();
			String bank=payment.getBank_to();
			String rekening=new BankDao(db).findByName(bank).getRekening();
			String harga=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount_original()));
			long kodeAmt=payment.getAmount().longValue()-payment.getAmount_original().longValue();
			String kode=String.valueOf(kodeAmt);
			String amount_digit= String.valueOf(payment.getAmount());
			String amount=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount()));
			String buttonName="View Document";
			String link="https://"+DOMAIN;
			
			String content=getContentTrx(invoice, kode_produk, bank, rekening, harga, kode, amount_digit, amount, "<b>Verifikasi Pembayaran</b>");
			content=content+"Pembayaran anda sedang kami verifikasi. Proses verifikasi maksimal 1x24 jam.";
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

			String body = getContentBody(title, kel + user.getNama(), content, null, null);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");
			sendMailKonfirmasiAdmin(db, payment);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	/**
	 * konfirmasi pembayaran admin
	 * @param user
	 * @param to
	 */
	private void sendMailKonfirmasiAdmin(DB db,Payment payment) { 
		
		SimpleDateFormat sdf=new SimpleDateFormat("DDDHHmmss");

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {
			Userdata user=payment.getEeuser().getUserdata();
			String title="Verifikasi Pembayaran";
			
			String invoice="PY"+sdf.format(payment.getDate_request());
			String kode_produk=payment.getProduct_code();
			String bank=payment.getBank_to();
			String rekening=new BankDao(db).findByName(bank).getRekening();
			String harga=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount_original()));
			long kodeAmt=payment.getAmount().longValue()-payment.getAmount_original().longValue();
			String kode=String.valueOf(kodeAmt);
			String amount_digit= String.valueOf(payment.getAmount());
			String amount=SystemUtil.amountDecFormatStr(new BigDecimal(payment.getAmount()));
			String buttonName="View Document";
			String link="https://"+DOMAIN;
			
			String content=getContentTrx(invoice, kode_produk, bank, rekening, harga, kode, amount_digit, amount, "<b>Verifikasi Pembayaran</b>");
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";
			content="User atas nama "+kel+user.getNama()+" dengan ID cust "+payment.getId_customer()+" melakukan pembayaran. Mohon lakukan pengecekan dan proses topup jika data benar.";
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("cs@digisign.id"));
			message.setSubject(title);

			String body = getContentBody(title, "CS Digisign", content, null, null);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	/**
	 * Setelah registrasi berhasil
	 * @param user
	 * @param to
	 */
	public void sendMailRegister(Userdata user, String to) { 
		
		

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {

			String title="Notifikasi Pendaftaran";
			String content="Anda sedang kami verifikasi. Setelah kami berhasil verifikasi akan kami beritahu melalui email ini, Mohon Kesediaannya untuk menunggu.<br> Jika anda merasa tidak mendaftar hubungi <a href='mailto:cs@digisign.id'>customer service</a> kami.";
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

			String body = getContentBody(title, kel + user.getNama(), content, null, null);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");
			sendMailRegisterAdmin(user, to);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	/**
	 * Setelah verifikasi berhasil
	 * @param user
	 * @param to
	 */
	public void sendMailRegisterSuccess(Userdata user, String to) { 
		
		

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {

			String title="Notifikasi Pendaftaran";
			String content="Anda telah berhasil kami verifikasi. Anda sudah dapat menggunakan layanan tandatangan elektronik kami. Silakan login untuk menggunakan layanan tersebut";
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";
			String buttonName="Login";
			String link="https://"+DOMAIN;
			
			String body = getContentBody(title, kel + user.getNama(), content, buttonName, link);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");
			sendMailRegisterAdmin(user, to);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	/**
	 * Setelah registrasi berhasil
	 * @param user
	 * @param to
	 */
	private void sendMailRegisterAdmin(Userdata user, String to) { 
		
		

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {
			String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

			String title="Notifikasi Pendaftaran";
			String content="User atas nama "+kel+user.getNama()+" dengan email "+to+" melakakukan pendaftaran. Mohon dicek data user tersebut";
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("cs@digisign.id"));
			message.setSubject(title);

			String body = getContentBody(title, "CS Digisign", content, null, null);
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}
	
	/**
	 */
	public void sendMailFileaReqSign(Userdata penerimaEmail,Userdata pengirim, String to) {
		


		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {

			String title="Menerima Dokumen";
			String kel=pengirim.getJk()=='L'? "Bpk. ": "Ibu ";
			String content="<span style='text-transform: capitalize;'>"+kel+pengirim.getNama()+"</span> mengirimi Anda dokumen";
			if(penerimaEmail.getId()==pengirim.getId()) {
				content="Anda telah mengirim dokumen untuk diperiksa dan ditandatangani";
			}
			
			String name="Review Document";
			String link="https://"+DOMAIN;
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);
			kel=penerimaEmail.getJk()=='L'? "Bpk. ": "Ibu ";

			String body = getContentBody(title, kel + penerimaEmail.getNama(), content, name, link);
			
			
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}	
	
	
public void sendMailFileaReqSign(String penerimaEmail,Userdata pengirim, String to) {
		


		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email,pass);
				}
			});

		try {

			String title="Menerima Dokumen";
			String kel=pengirim.getJk()=='L'? "Bpk. ": "Ibu ";
			String content="<span style='text-transform: capitalize;'>"+kel+pengirim.getNama()+"</span> mengirimi Anda dokumen";
			String name="Review Document";
			String link="https://"+DOMAIN;
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email,emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(title);

			String body = getContentBody(title, penerimaEmail, content, name, link);
			
			
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}	

public void sendMailFileaReqSignNotReg(String penerimaEmail,String nama, String to) {
	


	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Menerima Dokumen";
		//String kel=pengirim.getJk()=='L'? "Bpk. ": "Ibu ";
		String content="<span style='text-transform: capitalize;'>Bpk/Ibu "+nama+"</span> mengirimi Anda dokumen";
		String name="Review Document";
		String link="https://"+DOMAIN;
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);

		String body = getContentBody(title, penerimaEmail, content, name, link);
		
		
		
		message.setContent(body, "text/html; charset=utf-8");
		sendMail(message);

		System.out.println("Done");

	} catch (MessagingException e) {
		throw new RuntimeException(e);
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		LogSystem.error(getClass(), e);
	}
}	
	
public void sendMailFile(Userdata penerima,Userdata pengirim, String to) {
		

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Menerima Dokumen";
		String kel=pengirim.getJk()=='L'? "Bpk. ": "Ibu ";
		String content="<span style='text-transform: capitalize;'>"+kel+pengirim.getNama()+"</span> mengirimi Anda dokumen";
		String name="View Document";
		String link="https://"+DOMAIN;
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
		kel=penerima.getJk()=='L'? "Bpk. ": "Ibu ";

		String body = getContentBody(title, kel + penerima.getNama(), content, name, link);
		
		
			
			message.setContent(body, "text/html; charset=utf-8");
			sendMail(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
	}	


public void sendMailPreregister(PreRegistration user, String to,String id) {
	  
	  String docs ="";
	  String link ="";
	  try {
	   docs = AESEncryption.encryptDoc(id);
	   link = "https://"+DOMAIN+"/preregistration.html?prereigster="
	     + URLEncoder.encode(docs, "UTF-8");
	  } catch (Exception e1) {
	   // TODO Auto-generated catch block
	   e1.printStackTrace();
	  }

	  
	  

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Pendaftaran";
		String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

		String content="<span style='text-transform: capitalize;'>"+kel+user.getNama()+" silahkan melakukan registrasi untuk mendapatkan fasilitas tandatangan elektronik.";
		String name="Mendaftar";
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
	
		String body = getContentBody(title, kel + user.getNama(), content, name, link);
		
		
	   
	   message.setContent(body, "text/html; charset=utf-8");
	   sendMail(message);

	   System.out.println("Done");

	  } catch (MessagingException e) {
	   throw new RuntimeException(e);
	  } catch (UnsupportedEncodingException e) {
	   // TODO Auto-generated catch block
	   LogSystem.error(getClass(), e);
	  }
	 }

public void sendMailPreregisterMitra(PreRegistration user, String to,String id) {
	  
	  String docs ="";
	  String link ="";
	  try {
	   docs = AESEncryption.encryptDoc(id);
	   link = "https://"+DOMAIN+"/preregistration.html?prereigster="
	     + URLEncoder.encode(docs, "UTF-8");
	  } catch (Exception e1) {
	   // TODO Auto-generated catch block
	   e1.printStackTrace();
	  }

	  
	  

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Aktivasi Akun!";
		String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

		String content="<span style='text-transform: capitalize;'>"+kel+user.getNama()+" silahkan melakukan registrasi untuk mendapatkan fasilitas tandatangan elektronik.";
		String name="Mendaftar";
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
	
		String body = getContentBodyMitra(title, kel + user.getNama(), content, name, link, user.getMitra().getName());
	   
	   message.setContent(body, "text/html; charset=utf-8");
	   sendMail(message);

	   System.out.println("Done");

	  } catch (MessagingException e) {
	   throw new RuntimeException(e);
	  } catch (UnsupportedEncodingException e) {
	   // TODO Auto-generated catch block
	   LogSystem.error(getClass(), e);
	  }
	 }

public void sendMailKonfPerbData(User user, String id) {
	  String docs ="";
	  String link ="";
	  try {
	   docs = AESEncryption.encryptDoc(id);
	   link = "https://"+DOMAIN+"/update.html?update="
	     + URLEncoder.encode(docs, "UTF-8");
	  } catch (Exception e1) {
	   // TODO Auto-generated catch block
	   e1.printStackTrace();
	  }

	  
	  

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Konfirmasi Perubahan Data!";
		String kel=user.getUserdata().getJk()=='L'? "Bpk. ": "Ibu ";

		//String content="<span style='text-transform: capitalize;'>"+kel+user.getUserdata().getNama()+" silahkan melakukan registrasi untuk mendapatkan fasilitas tandatangan elektronik.";
		String name="Mendaftar";
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(user.getNick()));
		message.setSubject(title);
	
		String body = getContentKonfPerData(title, kel + user.getUserdata().getNama(), link, user.getUserdata().getMitra().getName());
	   
	   message.setContent(body, "text/html; charset=utf-8");
	   sendMail(message);

	   System.out.println("Done");

	  } catch (MessagingException e) {
	   throw new RuntimeException(e);
	  } catch (UnsupportedEncodingException e) {
	   // TODO Auto-generated catch block
	   LogSystem.error(getClass(), e);
	  }
}



public void sendMailPreregisterModalku(PreRegistration user, String to,String id) {
	  
	  String docs ="";
	  String link ="";
	  try {
	   docs = AESEncryption.encryptDoc(id);
	   link = "https://"+DOMAIN+"/preregistration.html?prereigster="
	     + URLEncoder.encode(docs, "UTF-8");
	  } catch (Exception e1) {
	   // TODO Auto-generated catch block
	   e1.printStackTrace();
	  }

	  
	  

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Perjanjian Berlangganan Anda!";
		String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

		String content="<span style='text-transform: capitalize;'>"+kel+user.getNama()+" silahkan melakukan registrasi untuk mendapatkan fasilitas tandatangan elektronik.";
		String name="Mendaftar";
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
	
		String body = getContentBodyModalku(title, kel + user.getNama(), content, name, link);
		
	   
//	   String body = " <html lang='en'> "
//	+" <head> "
//	+" <title>Registration</title> "
//	+" <meta charset='utf-8'> "
//	+" <meta name='viewport' content='width=device-width'> "
//	+" <style type='text/css'> "
//	+"     /* CLIENT-SPECIFIC STYLES */ "
//	+"     #outlook a{padding:0;} /* Force Outlook to provide a 'view in browser' message */ "
//	+"     .ReadMsgBody{width:100%;} .ExternalClass{width:100%;} /* Force Hotmail to display emails at full width */ "
//	+"     .ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div {line-height: 100%;} /* Force Hotmail to display normal line spacing */ "
//	+"     body, table, td, a{-webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;} /* Prevent WebKit and Windows mobile changing default text sizes */ "
//	+"     table, td{mso-table-lspace:0pt; mso-table-rspace:0pt;} /* Remove spacing between tables in Outlook 2007 and up */ "
//	+"     img{-ms-interpolation-mode:bicubic;} /* Allow smoother rendering of resized image in Internet Explorer */ "
//	+"     /* RESET STYLES */ "
//	+"     body{margin:0; padding:0;} "
//	+"     img{border:0; height:auto; line-height:100%; outline:none; text-decoration:none;} "
//	+"     table{border-collapse:collapse !important;} "
//	+"     body{height:100% !important; margin:0; padding:0; width:100% !important;} "
//	+"     /* iOS BLUE LINKS */ "
//	+"     .appleBody a {color:#68440a; text-decoration: none;} "
//	+"     .appleFooter a {color:#999999; text-decoration: none;} "
//	+"     /* MOBILE STYLES */ "
//	+"     @media screen and (max-width: 525px) { "
//	+"         /* ALLOWS FOR FLUID TABLES */ "
//	+"         table[class='wrapper']{ "
//	+"           width:100% !important; "
//	+"         } "
//	+"         /* ADJUSTS LAYOUT OF LOGO IMAGE */ "
//	+"         td[class='logo']{ "
//	+"           text-align: left; "
//	+"           padding: 20px 0 20px 0 !important; "
//	+"         } "
//	+"         td[class='logo'] img{ "
//	+"           margin:0 auto!important; "
//	+"         } "
//	+"         /* USE THESE CLASSES TO HIDE CONTENT ON MOBILE */ "
//	+"         td[class='mobile-hide']{ "
//	+"           display:none;} "
//	+"         img[class='mobile-hide']{ "
//	+"           display: none !important; "
//	+"         } "
//	+"         img[class='img-max']{ "
//	+"           max-width: 100% !important; "
//	+"           height:auto !important; "
//	+"         } "
//	+"         /* FULL-WIDTH TABLES */ "
//	+"         table[class='responsive-table']{ "
//	+"           width:100%!important; "
//	+"         } "
//	+"         /* UTILITY CLASSES FOR ADJUSTING PADDING ON MOBILE */ "
//	+"         td[class='padding']{ "
//	+"           padding: 10px 5% 15px 5% !important; "
//	+"         } "
//	+"         td[class='padding-copy']{ "
//	+"           padding: 10px 10px 10px 10px !important; "
//	+"           text-align: center; "
//	+"         } "
//	+"         td[class='padding-meta']{ "
//	+"           padding: 30px 5% 0px 5% !important; "
//	+"           text-align: center; "
//	+"         } "
//	+"         td[class='no-pad']{ "
//	+"           padding: 0 0 20px 0 !important; "
//	+"         } "
//	+"         td[class='no-padding']{ "
//	+"           padding: 0 !important; "
//	+"         } "
//	+"         td[class='section-padding']{ "
//	+"           padding: 50px 15px 50px 15px !important; "
//	+"         } "
//	+"         td[class='section-padding-bottom-image']{ "
//	+"           padding: 50px 15px 0 15px !important; "
//	+"         } "
//	+"         /* ADJUST BUTTONS ON MOBILE */ "
//	+"         td[class='mobile-wrapper']{ "
//	+"             padding: 10px 5% 15px 5% !important; "
//	+"         } "
//	+"         table[class='mobile-button-container']{ "
//	+"             margin:0 auto; "
//	+"             width:100% !important; "
//	+"         } "
//	+"         a[class='mobile-button']{ "
//	+"             width:80% !important; "
//	+"             padding: 15px !important; "
//	+"             border: 0 !important; "
//	+"             font-size: 16px !important; "
//	+"         } "
//	+"     } "
//	+" </style> "
//	+" </head> "
//	+" <body style='margin: 0; padding: 0;'> "
//	+" <!-- ONE COLUMN W/ BOTTOM IMAGE SECTION --> "
//	+" <table border='0' cellpadding='0' cellspacing='0' width='100%'> "
//	+"     <tr> "
//	+"         <td bgcolor='#f8f8f8' align='center' style='padding: 60px 15px 30px 15px;' class='section-padding-bottom-image'> "
//	+"             <table border='0' cellpadding='0' cellspacing='0' width='700' class='responsive-table'> "
//	+"                 <tr> "
//	+"                     <td> "
//	+"                         <table width='100%' border='0' cellspacing='0' cellpadding='0'> "
//	+"                             <tr> "
//	+"                                 <td> "
//	+"                                     <!-- COPY --> "
//	+"                                     <table width='100%' border='0' cellspacing='0' cellpadding='0'> "
//	+"                                         <tr> "
//	+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Kepada Bpk/Ibu <b>"+nama+ "</b></td> "
//	+"                                         </tr> "
//	+"										   <tr> " + 
//	"	                                            <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Berikut adalah perjanjian berlangganan untuk akun investor Anda.</td>" + 
//	"                                         </tr> "										
//	+"                                         <tr> "
//	+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Klik link berikut untuk melakukan tanda tangan elektronik.</td> "
//	+"                                         </tr> "
//	+"  										<tr> "
//	+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><a href=\""+link+"\" >"+link+ "</a></td> "
//    +"                                         </tr> "
//	+"                                         <tr> "
//	+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Setelah anda menyelesaikan proses ini, anda dapat mulai berinvestasi bersama Modalku!</td> "
//	+"                                         </tr> "
//	+"                                         <tr> "
//	+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Have a good week!</td> "
//	+"                                         </tr> "
//	+"                                         <tr> "
//	+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><b>Team Modalku</b></td> "
//	+"                                         </tr> "
//	+"                                     </table> "
//	+"                                 </td> "
//	+"                             </tr> "
//	+"                              <td> "
//	+"                                     <!--  BOTTOM IMAGE --> "
//	+"                                     <table width='100%' border='0' cellspacing='0' cellpadding='0'> "
//	+"                                         <tr> "
//	+"                                             <td style='padding: 50px 0 0 0;' align='center'> "
//	+"                                             </td> "
//	+"                                         </tr> "
//	+"                                     </table> "
//	+"                                 </td> "
//	+"                             </tr> "
//	+"                                     </table> "
//	+"                                 </td> "
//	+"                             </tr> "
//	+"                         </table> "
//	+"                     </td> "
//	+"                 </tr> "
//	+"             </table> "
//	+"         </td> "
//	+"     </tr> "
//	+" </table> "
//	+" </body> <html>  " ;
	   
	   message.setContent(body, "text/html; charset=utf-8");
	   sendMail(message);

	   System.out.println("Done");

	  } catch (MessagingException e) {
	   throw new RuntimeException(e);
	  } catch (UnsupportedEncodingException e) {
	   // TODO Auto-generated catch block
	   LogSystem.error(getClass(), e);
	  }
	 }

public void sendMailNotifSign(String dokumen, Userdata ttd, Userdata userPenerimaEmail, String to) {
  

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Notifikasi Tandatangan";
		String kel=ttd.getJk()=='L'? "Bpk. ": "Ibu ";

		String content="Dokumen : "+dokumen+ " telah berhasil ditandatangani oleh <span style='text-transform: capitalize;'>"+kel+ttd.getNama()+"</span>";
		String name="Lihat Dokumen";
		String link="https://"+DOMAIN;

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
		kel=userPenerimaEmail.getJk()=='L'? "Bpk. ": "Ibu ";
		String body = getContentBody(title, kel + userPenerimaEmail.getNama(), content, name, link);
		
		
   
   message.setContent(body, "text/html; charset=utf-8");
   sendMail(message);

   System.out.println("Done");

  } catch (MessagingException e) {
   throw new RuntimeException(e);
  } catch (UnsupportedEncodingException e) {
   // TODO Auto-generated catch block
   LogSystem.error(getClass(), e);
  }
 } 

public void sendMailNotifSign(String dokumen, Userdata ttd, String userPenerimaEmail, String to) {
	  

	Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Notifikasi Tandatangan";
		String kel=ttd.getJk()=='L'? "Bpk. ": "Ibu ";

		String content="Dokumen : "+dokumen+ " telah berhasil ditandatangani oleh <span style='text-transform: capitalize;'>"+kel+ttd.getNama()+"</span>";
		String name="Lihat Dokumen";
		String link="https://"+DOMAIN;

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
		String body = getContentBody(title, userPenerimaEmail, content, name, link);
		
		
   
   message.setContent(body, "text/html; charset=utf-8");
   sendMail(message);

   System.out.println("Done");

  } catch (MessagingException e) {
   throw new RuntimeException(e);
  } catch (UnsupportedEncodingException e) {
   // TODO Auto-generated catch block
   LogSystem.error(getClass(), e);
  }
 } 
  

public void sendMailForgotPassword(Userdata user, String to) {
	 String docs="";
	 String link ="";
	  try {
	   String date=String.valueOf(new Date().getTime()); 
	   docs = AESEncryption.encryptDoc(date+"|"+to);
	   link = "https://"+DOMAIN+"/resetpassword.html?data="
	     + URLEncoder.encode(docs, "UTF-8");
	  } catch (Exception e1) {
	   // TODO Auto-generated catch block
	   e1.printStackTrace();
	  }
	
	  Session session = Session.getDefaultInstance(props,
		new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email,pass);
			}
		});

	try {

		String title="Reset Password";
		String kel=user.getJk()=='L'? "Bpk. ": "Ibu ";

		String content="Kami menerima permintaan untuk merubah password akun Anda di <b>Digisign.id</b><br>Klik link dibawah ini untuk mereset password";
		String name="Reset Password";

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email,emailName));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
		message.setSubject(title);
		kel=user.getJk()=='L'? "Bpk. ": "Ibu ";
		String body = getContentBody(title, kel + user.getNama(), content, name, link);
		
	
		
		message.setContent(body, "text/html; charset=utf-8");
		sendMail(message);

		System.out.println("Done");

	} catch (MessagingException e) {
		throw new RuntimeException(e);
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		LogSystem.error(getClass(), e);
	}
}	

	private String getContentTrx(String invoice, String kode_produk, String bank, String rekening, String harga, String kode, String amount_digit, String amount, String status) {
		String txt="<script>\n" + 
				"								function myFunction() {\n" + 
				"								  /* Get the text field */\n" + 
				"								   var amount = document.getElementById('tes');\n" + 
				"								   var tbl = document.getElementById('test');\n" + 
				"								   amount.style.visibility='visible';\n" + 
				"								   tbl.style.visibility='visible';\n" + 
				"\n" + 
				"								  /* Select the text field */\n" + 
				"								  amount.select();\n" + 
				"\n" + 
				"								  /* Copy the text inside the text field */\n" + 
				"								  document.execCommand('copy');\n" + 
				"								  amount.style.visibility='hidden';\n" + 
				"							      tbl.style.visibility='collapse';\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"								} \n" + 
				"						</script>\n" + 
				"						<table width=100% style='padding-right:5px;padding-left:5px;background:rgb(247,247,247);width:100%' >\n" + 
				"							<tr>\n" + 
				"								<td colspan=2 style='padding-top:5px;padding-bottom:5px;font-family: Helvetica, Arial, sans-serif; color: #666666;text-align:center;font-weight: bold;'>Detail Tagihan</td>\n" + 
				"\n" + 
				"							</tr>\n" + 
				"							<tr >\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>Invoice</td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666; text-align:right;'witdth>"+invoice+"</td>\n" + 
				"							</tr>\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>Produk</td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666; text-align:right;'>"+kode_produk+"</td>	\n" + 
				"							</tr>\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>Bank Tujuan</td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666; text-align:right;'>"+bank+"</td>	\n" + 
				"							</tr>\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>Rekening </td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666; text-align:right;'>"+rekening+"</td>	\n" + 
				"							</tr>\n" + 
				"							\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>Harga</td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666; text-align:right;'>"+harga+"</td>	\n" + 
				"							</tr>\n" + 
				"							\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>Kode</td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;text-align:right;'>"+kode+"</td>	\n" + 
				"							</tr>\n" + 
				"							\n" + 
				"							<tr id='test' style='visibility: collapse; ' >\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'></td>\n" + 
				"								<td  style='font-family: Helvetica, Arial, sans-serif; font-size:18px; font-weight:bold; color: #666666;text-align:right;'>\n" + 
				"								<input style=\"visibility: hidden;\" type=\"text\" value='"+amount_digit+"' id='tes'>\n" + 
				"								</td>	\n" + 
				"							</tr>\n" + 
				"						\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif;padding-bottom:15px; color: #666666;' valign=top>Jumlah yang harus ditransfer</td>\n" + 
				"								<td  valign=top style='font-family: Helvetica, Arial, sans-serif; font-size:18px; font-weight:bold; color: #666666;text-align:right;' >\n" + 
				"								Rp "+amount+"\n" + 
				"								</td>\n" + 
				"							</tr>\n" + 
				"							\n" + 
				"							<tr>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;'>status</td>\n" + 
				"								<td style='font-family: Helvetica, Arial, sans-serif; color: #666666;text-align:right;'>"+status+"</td>	\n" + 
				"							</tr>\n" + 
				"						</table>\n" + 
				"						";
		
		return txt;
	}
	private String getContentBody(String title, String nama, String content, String buttonName, String linkButton) {
		

		String button=
				"			<tr>\n" + 
				"				<td align='center'><a href='"+linkButton+"' target='_blank' style='margin-top:10px;font-size: 12px; font-family: Helvetica, Arial, sans-serif; font-weight: normal; color: #ffffff; text-decoration: none; background-color: #48CFAD; border-top: 15px solid #48CFAD; border-bottom: 15px solid #48CFAD; border-left: 25px solid #48CFAD; border-right: 25px solid #48CFAD; border-radius: 3px; -webkit-border-radius: 3px; -moz-border-radius: 3px; display: inline-block;' class='mobile-button'>"+buttonName+"</a></td>\n" + 
				"			</tr>\n";
		
		if(buttonName==null) {
			button="";
		}
		
		String html="	<!DOCTYPE html>\n" +
				"	<html style='display: table;margin: auto;'>\n" + 
				"	\n" + 
				"	\n" + 
				"	<body style='display: table-cell;\n" + 
				"    vertical-align: middle;'>\n" + 
				"	<!-- ONE COLUMN W/ BOTTOM IMAGE SECTION -->\n" + 
				"	<table width='100%' style='border-collapse:collapse!important;width:100%;max-width:800px'>\n" + 
				"	   \n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' class='padding-copy'><a href='https://digisign.id'><img style='max-width:150px;padding-top:10px;padding-bottom:10px;display:block!important;line-height:0;font-size:0' src='https://digisign.id/ttd-white.png'></a></td>\n" + 
				"			</tr>\n" + 
				"			<tr>\n" + 
				"				<td align='center' style='padding-top:5px;padding-bottom:5px;font-size:16px; font-family: Helvetica, Arial, sans-serif; font-weight: bold;' bgcolor=\"#ffc412\"><b>"+title+"</b></td>\n" + 
				"				\n" + 
				"			</tr>\n" + 
				"			<tr>\n" + 
				"				<td align='center'  style='padding-top:15px;font-size:16px; font-family: Helvetica, Arial, sans-serif; color: #666666; font-weight: bold; text-transform: capitalize;' >Halo "+nama+"</td>\n" + 
				"				\n" + 
				"			</tr>\n" + 
				"			<tr>\n" + 
				"				<td align='center' style='padding-top:10px;font-size:12px; font-family: Helvetica, Arial, sans-serif; color: #666666;' >"+content+"</td>\n" + 
				"			</tr>\n" + button +
				"			<tr>\n" + 
				"				<td align='center' style='padding-top:10px;padding-bottom:15px; font-size:12px; font-family: Helvetica, Arial, sans-serif; color: #666666;font-weight: bold;'>Terimakasih </td>\n" + 
				"			</tr>\n" + 
				"			<tr style='background-color:rgb(244,244,244);'>\n" + 
				"				<td style='padding-left:10px;padding-top:10px;padding-bottom:10px;'>\n" + 
//				"					    <div style='margin-top:0px;padding-top:0px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: grey; '><b>Sahid Sudirman Center, 55th Floor </b><br>Jl. Jend. Sudirman no.86 <br>Jakarta Pusat 10250<br>" +
				"						email: <a href='mailto:cs@digisign.id'>cs@digisign.id</a></div>\n" + 
				"						<td align='center' style='padding-top:2px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: white;'></td>\n" + 
				"				</td>\n" + 
				"			</tr>\n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' style='padding-top:10px;padding-bottom:10px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: #666666;'>Copyright &#9400; 2018, www.digisign.id  All rights reserved.<br> Created by PT. Solusi Net Internusa.</td>\n" + 
				"\n" + 
				"				\n" + 
				"			</tr>\n" + 
				"	</table>\n" + 
				"	</body> </html>"; 
		
		return html;
		
	}
	
   private String getContentBodyModalku(String title, String nama, String content, String buttonName, String linkButton) {
		

		
		
		String html="	<!DOCTYPE html>\n" +
				"	<html style='display: table;margin: auto;'>\n" + 
				"	\n" + 
				"	\n" + 
				"	<body style='display: table-cell;\n" + 
				"    vertical-align: middle;'>\n" + 
				"	<!-- ONE COLUMN W/ BOTTOM IMAGE SECTION -->\n" + 
				"	<table width='100%' style='border-collapse:collapse!important;width:100%;max-width:800px'>\n" + 
				"	   \n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' class='padding-copy'><a href='https://digisign.id'><img alt='digisign' style='max-width:150px;padding-top:10px;padding-bottom:10px;display:block!important;line-height:0;font-size:0' src='https://digisign.id/ttd-white.png'></a></td>\n" + 
				"			</tr>\n" + 
				"			<tr>\n" + 
				"				<td align='center' style='padding-top:5px;padding-bottom:5px;font-size:16px; font-family: Helvetica, Arial, sans-serif; font-weight: bold;' bgcolor=\"#ffc412\"><b>"+title+"</b></td>\n" + 
				"				\n" + 
				"			</tr>\n" 
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Kepada <b>"+nama+ "</b></td> "
				+"                                         </tr> "
				+"										   <tr> " + 
				"	                                            <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Berikut adalah perjanjian berlangganan untuk akun investor Anda.</td>" + 
				"                                         </tr> "										
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Klik link berikut untuk melakukan tanda tangan elektronik.</td> "
				+"                                         </tr> "
				+"  										<tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><a href=\""+linkButton+"\" >"+linkButton+ "</a></td> "
			    +"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Setelah anda menyelesaikan proses ini, anda dapat mulai berinvestasi bersama Modalku!</td> "
				+"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Have a good week!</td> "
				+"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><b>Team Modalku</b></td> "
				+"                                         </tr> "+
				"			<tr style='background-color:rgb(244,244,244);'>\n"+ 
				"				<td style='padding-left:10px;padding-top:10px;padding-bottom:10px;'>\n" + 
				"					    <div style='margin-top:0px;padding-top:0px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: grey; '><b>Sahid Sudirman Center, 55th Floor </b><br>Jl. Jend. Sudirman no.86 <br>Jakarta Pusat 10250<br>email: <a href='mailto:cs@digisign.id'>cs@digisign.id</a></div>\n" + 
				"						<td align='center' style='padding-top:2px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: white;'></td>\n" + 
				"				</td>\n" + 
				"			</tr>\n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' style='padding-top:10px;padding-bottom:10px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: #666666;'>Copyright &#9400; 2018, www.digisign.id  All rights reserved.<br> Created by PT. Solusi Net Internusa.</td>\n" + 
				"\n" + 
				"				\n" + 
				"			</tr>\n" + 
				"	</table>\n" + 
				"	</body> </html>"; 
		
		return html;
		
	}
   
   private String getContentBodyMitra(String title, String nama, String content, String buttonName, String linkButton, String mitra) {
		

		
		
		String html="	<!DOCTYPE html>\n" + 
				"	<html style='display: table;margin: auto;'>\n" +
				"	\n" + 
				"	\n" + 
				"	<body style='display: table-cell;\n" + 
				"    vertical-align: middle;'>\n" + 
				"	<!-- ONE COLUMN W/ BOTTOM IMAGE SECTION -->\n" + 
				"	<table width='100%' style='border-collapse:collapse!important;width:100%;max-width:800px'>\n" + 
				"	   \n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' class='padding-copy'><a href='https://digisign.id'><img alt='digisign' style='max-width:150px;padding-top:10px;padding-bottom:10px;display:block!important;line-height:0;font-size:0' src='https://digisign.id/ttd-white.png'></a></td>\n" + 
				"			</tr>\n" + 
				"			<tr>\n" + 
				"				<td align='center' style='padding-top:5px;padding-bottom:5px;font-size:16px; font-family: Helvetica, Arial, sans-serif; font-weight: bold;' bgcolor=\"#ffc412\"><b>"+title+"</b></td>\n" + 
				"				\n" + 
				"			</tr>\n" 
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Kepada <b><span style='text-transform: capitalize;'>"+nama+ "</span></b></td> "
				+"                                         </tr> "										
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Untuk mengaktivasi account anda, silahkan klik link dibawah ini :</td> "
				+"                                         </tr> "
				+"  										<tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><a href=\""+linkButton+"\" >"+linkButton+ "</a></td> "
			    +"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Setelah anda menyelesaikan proses ini, anda dapat mulai menggunakan layanan tanda tangan elektronik.</td> "
				+"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Salam sukses!</td> "
				+"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><b>Tim <span style='text-transform: capitalize;'>"+mitra+"</span></b></td> "
				+"                                         </tr> "+
				"			<tr style='background-color:rgb(244,244,244);'>\n"+ 
				"				<td style='padding-left:10px;padding-top:10px;padding-bottom:10px;'>\n" + 
				"					    <div style='margin-top:0px;padding-top:0px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: grey; '><b>Sahid Sudirman Center, 55th Floor </b><br>Jl. Jend. Sudirman no.86 <br>Jakarta Pusat 10250<br>email: <a href='mailto:cs@digisign.id'>cs@digisign.id</a></div>\n" + 
				"						<td align='center' style='padding-top:2px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: white;'></td>\n" + 
				"				</td>\n" + 
				"			</tr>\n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' style='padding-top:10px;padding-bottom:10px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: #666666;'>Copyright &#9400; 2018, www.digisign.id  All rights reserved.<br> Created by PT. Solusi Net Internusa.</td>\n" + 
				"\n" + 
				"				\n" + 
				"			</tr>\n" + 
				"	</table>\n" + 
				"	</body> </html>"; 
		
		return html;
		
	}
   
   private String getContentKonfPerData(String title, String nama, String linkButton, String mitra) {
		

		
		
		String html="	<!DOCTYPE html>\n" +
				"	<html style='display: table;margin: auto;'>\n" + 
				"	\n" + 
				"	\n" + 
				"	<body style='display: table-cell;\n" + 
				"    vertical-align: middle;'>\n" + 
				"	<!-- ONE COLUMN W/ BOTTOM IMAGE SECTION -->\n" + 
				"	<table width='100%' style='border-collapse:collapse!important;width:100%;max-width:800px'>\n" + 
				"	   \n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' class='padding-copy'><a href='https://digisign.id'><img alt='digisign' style='max-width:150px;padding-top:10px;padding-bottom:10px;display:block!important;line-height:0;font-size:0' src='https://digisign.id/ttd-white.png'></a></td>\n" + 
				"			</tr>\n" + 
				"			<tr>\n" + 
				"				<td align='center' style='padding-top:5px;padding-bottom:5px;font-size:16px; font-family: Helvetica, Arial, sans-serif; font-weight: bold;' bgcolor=\"#ffc412\"><b>"+title+"</b></td>\n" + 
				"				\n" + 
				"			</tr>\n" 
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Kepada <b><span style='text-transform: capitalize;'>"+nama+ "</span></b></td> "
				+"                                         </tr> "										
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Memastikan bahwa anda sendiri yang mengharapkan perubahan data, jika betul silahkan klik link dibawah ini :</td> "
				+"                                         </tr> "
				+"  										<tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><a href=\""+linkButton+"\" >"+linkButton+ "</a></td> "
			    +"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Setelah anda menyelesaikan proses ini, data anda akan berubah sesuai permintaan perubahan.</td> "
				+"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'>Salam sukses!</td> "
				+"                                         </tr> "
				+"                                         <tr> "
				+"                                             <td align='left' style='padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif; color: #666666;' class='padding-copy'><b>Tim <span style='text-transform: capitalize;'>"+mitra+"</span></b></td> "
				+"                                         </tr> "+
				"			<tr style='background-color:rgb(244,244,244);'>\n"+ 
				"				<td style='padding-left:10px;padding-top:10px;padding-bottom:10px;'>\n" + 
				"					    <div style='margin-top:0px;padding-top:0px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: grey; '><b>Sahid Sudirman Center, 55th Floor </b><br>Jl. Jend. Sudirman no.86 <br>Jakarta Pusat 10250<br>email: <a href='mailto:cs@digisign.id'>cs@digisign.id</a></div>\n" + 
				"						<td align='center' style='padding-top:2px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: white;'></td>\n" + 
				"				</td>\n" + 
				"			</tr>\n" + 
				"			<tr bgcolor=\"#2d2724\">\n" + 
				"				<td align='center' style='padding-top:10px;padding-bottom:10px; font-size:8px; font-family: Helvetica, Arial, sans-serif; color: #666666;'>Copyright &#9400; 2018, www.digisign.id  All rights reserved.<br> Created by PT. Solusi Net Internusa.</td>\n" + 
				"\n" + 
				"				\n" + 
				"			</tr>\n" + 
				"	</table>\n" + 
				"	</body> </html>"; 
		
		return html;
		
	}
	
 
}
	
