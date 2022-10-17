package id.co.keriss.consolidate.action.billing;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.jpos.ee.User;
import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;

import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.util.DSAPI;

public class KillBillPersonal {

	private KillBillClient killBillClient;

	public KillBillPersonal() {
		KillBillHttpClient killBillHttpClient = new KillBillHttpClient(
				DSAPI.BILLING_HOST, "admin", "password", "personal",
				"personal");
		killBillClient = new KillBillClient(killBillHttpClient);
	}

	public void createKillbill(User userTrx, Logger log, String ex, Alamat alamat) throws Exception {

		String name = userTrx.getUserdata().getNama();
		String Email = userTrx.getNick();
		String Address1 = alamat.getAlamat();
		String Address2 = "-";
		String PostalCode = alamat.getKodepos();
		String Company = "-";
		String City = alamat.getKota();
		String State = alamat.getPropinsi();
		//String Phone = userTrx.getNo_handphone();
		String Phone = userTrx.getUserdata().getNo_handphone();
		String Notes = "-";

//		String Creat = killBillClient.createAccount(name, ex, Email, Address1, Address2, PostalCode, Company, City,
//				State, Phone, Notes);
		int Creat = killBillClient.createAccount(name, ex, Email, Address1, Address2, PostalCode, Company, City,
				State, Phone, false);
		
		log.info("Creat Account :" + Creat);

	}
	
	public int createKillbill2(User userTrx, String ex, Alamat alamat) throws Exception {

		String name = userTrx.getUserdata().getNama();
		String Email = userTrx.getNick();
		String Address1 = alamat.getAlamat();
		String Address2 = "-";
		String PostalCode = alamat.getKodepos();
		String Company = "-";
		String City = alamat.getKota();
		String State = alamat.getPropinsi();
		//String Phone = userTrx.getNo_handphone();
		String Phone = userTrx.getUserdata().getNo_handphone();
		String Notes = "-";

//		String Creat = killBillClient.createAccount(name, ex, Email, Address1, Address2, PostalCode, Company, City,
//				State, Phone, Notes);
		int Creat = killBillClient.createAccount(name, ex, Email, Address1, Address2, PostalCode, Company, City,
				State, Phone, false);
		return Creat;

	}

	/*
	public void createMitra(Mitra mitra, String email, Logger log, String ex) {

		String name = mitra.getName();
		String Email = email;
		String Address1 = mitra.getAddress();
		String Address2 = "-";
		String PostalCode = "-";
		String Company = "-";
		String City = "-";
		String State = "-";
		String Phone = mitra.getPhone();
		String Notes = "-";

		try {
			String Creat = killBillClient.createAccount(name, ex, Email, Address1, Address2, PostalCode, Company, City,
					State, Phone, Notes);
			log.info("Creat Account :" + Creat);

		} catch (Exception e) {

			log.error(getClass(), e);
		}

	}
	*/

	public int getBalance(String ex) throws KillBillClientException {
		return killBillClient.getDSBalance(ex);
	}

	public String setTransaction(String key, int trx) throws KillBillClientException {
		return killBillClient.setTransaction(key, trx);
	}

	public String reverseTransaction(String trx) throws KillBillClientException {
		return killBillClient.reverseTransaction(UUID.fromString(trx));
	}

	public List<String> getPricePrePaid(String tenant) throws KillBillClientException {
		return killBillClient.getPlan(tenant);
	}
	
	public void close() {
		killBillClient.close();
	}
}
