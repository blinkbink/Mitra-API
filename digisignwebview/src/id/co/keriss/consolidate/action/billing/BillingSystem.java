package id.co.keriss.consolidate.action.billing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jpos.ee.User;
import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;

import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.ProductBilling;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class BillingSystem implements DSAPI {

	public static final String SALDO_KURANG="NS";
	public static final String BERHASIL="00";
	public static final String ERROR="05";
	public static final String ERROR_PARAM="06";
	public static final String DATA_SUDAH_ADA="01";
	
	private static final String BILLING_USER_ROOT="admin";
	private static final String BILLING_USER_PWD="password";
	private static final String BILLING_USER_APIKEY="personal";
	private static final String BILLING_USER_APISECRET="personal";
	
	private KillBillHttpClient kbClient;
	private KillBillClient killBillClient;
	private String extKey;
	private String lastInvoice;
	
	
	public BillingSystem() {
		clientRoot();
	}
	
	public BillingSystem(User u) throws Exception {
		if(u.getMitra()==null) {
			extKey="ID"+String.valueOf(u.getId());

			clientRoot();
		}else {
			extKey="MT"+String.valueOf(u.getMitra().getId());
			clientRoot();
		}
	}
	
	private void clientRoot() {
		kbClient = new KillBillHttpClient(BILLING_HOST,
				BILLING_USER_ROOT,
				BILLING_USER_PWD,
				BILLING_USER_APIKEY, //apiKey untuk tenant
				BILLING_USER_APISECRET);
		killBillClient=new KillBillClient(kbClient);
	}
	private void clientMitra(Mitra mitra) throws Exception {
		String apikey=String.valueOf("MT"+mitra.getId());
		String apiSecret=AESEncryption.encryptId(String.valueOf(mitra.getId())+mitra.getName());
		kbClient = new KillBillHttpClient(BILLING_HOST,
				BILLING_USER_ROOT,
				BILLING_USER_PWD,
				apikey, //apiKey untuk tenant
				apiSecret);
		killBillClient=new KillBillClient(kbClient);
	}

	public BillingSystem(Mitra mitra) throws Exception {
		clientMitra(mitra);
	}
	
//	public String createMitra(Mitra mitra) {
//		try {
//
//			String apikey=String.valueOf(mitra.getId());
//			String apiSecret=AESEncryption.encryptId(String.valueOf(mitra.getId())+mitra.getName());
//			LogSystem.info(BillingSystem.class, "[REQ] create mitra "+ mitra.getName());
//			String res=killBillClient.createTenant(mitra.getName(), apikey, apiSecret);
//			LogSystem.info(BillingSystem.class, "[RSP] create mitra "+ mitra.getName() +" : "+res);
//			if(res.equals("200")) {
//				return BERHASIL;
//			}
//			else if(res.equals("400")) {
//				return ERROR_PARAM;
//			}else if(res.equals("500")) {
//				return DATA_SUDAH_ADA;
//			}
//		} catch (KillBillClientException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		LogSystem.info(BillingSystem.class, "[RSP] create mitra "+ mitra.getName() +" : ERROR");
//
//		return ERROR;
//	}
	
	
	public int createMitra(Mitra user) {
		try {
			String perusahaan="";
			LogSystem.info(BillingSystem.class, "[REQ] create user "+ user.getName());
			int res=killBillClient.createAccount(
					user.getName(),
					"MT"+String.valueOf(user.getId()),
					"",
					"",
					"",
					"",
					user.getName(),
					"", 
					"",
					"",
					false);
			LogSystem.info(BillingSystem.class, "[RSP] create user "+ user.getName() +" : "+res);

			if(res==200) {
				return 00;
			}
			else if(res==400) {
				return 05;
			}else if(res==500 ||res==409) {
				return 409;
			}
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogSystem.info(BillingSystem.class, "[RSP] create user "+ user.getName() +" : ERROR");

		return 01;
	}
	
	public int createUser(User user) {
		try {
			String perusahaan="";
			if(user.getMitra()!=null) perusahaan=user.getMitra().getName();
			LogSystem.info(BillingSystem.class, "[REQ] create user "+ user.getNick());
			int res=killBillClient.createAccount(
					user.getName(),
					"ID"+String.valueOf(user.getId()),
					user.getNick(),
					user.getUserdata().getAlamat(),
					"",
					user.getUserdata().getKodepos(),
					perusahaan,
					user.getUserdata().getKota(), 
					user.getUserdata().getPropinsi(),
					user.getUserdata().getNo_handphone(),
					false);
			LogSystem.info(BillingSystem.class, "[RSP] create user "+ user.getNick() +" : "+res);

			if(res==200) {
				return 00;
			}
			else if(res==400) {
				return 400;
			}else if(res==500 ||res==409) {
				return 409;
			}
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogSystem.info(BillingSystem.class, "[RSP] create user "+ user.getNick() +" : ERROR");

		return 01;
	}
	public String transaksi(int cnt) {
		try {
			LogSystem.info(BillingSystem.class, "[REQ] trx user "+ extKey);

			String res=killBillClient.setTransaction(extKey, cnt);
			System.out.println("hsail :" +res);
			LogSystem.info(BillingSystem.class, "[RSP] trx user "+ extKey +" : "+res);

		    if(res.equals("")) {
		    	return ERROR;
		    }
		    else if(res.equals("401")) {
				return SALDO_KURANG;
			}
		    else if(res.length()>3) {
		    	lastInvoice=res;
				return BERHASIL;
			}
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ERROR;
	}
	
	public String reversal() {
		try {
			LogSystem.info(BillingSystem.class, "[REQ] reversal user "+ lastInvoice);

			String res=killBillClient.reverseTransaction( UUID.fromString(lastInvoice));
			
			LogSystem.info(BillingSystem.class, "[RSP] reversal user "+ lastInvoice +" : "+res);

			if(res.equals("200")) {
				return BERHASIL;
			}
			else if(res.equals("401")) {
				return SALDO_KURANG;
			}
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ERROR;
	}
	
	public int getBalance() {
		try {
			LogSystem.info(BillingSystem.class, "[REQ] balance user "+ lastInvoice);
			
			int res=killBillClient.getDSBalance(extKey);
			if(res<0) {
				return -1;
			}
			LogSystem.info(BillingSystem.class, "[RSP] balance user "+ lastInvoice +" : "+res);

			return res;
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	
	public List<ProductBilling> getListProd(String tenant) {
		List<ProductBilling> listProd=new ArrayList<>();
		try {
			
			List<String> data=killBillClient.getPlan(tenant);
			for (String string : data) {
				String[] p=string.split(" ");
				ProductBilling prd=new ProductBilling();
				prd.setProduct_code(p[0]);
				prd.setJmlh_ttd(p[2]);
				prd.setHarga(p[1]);
				listProd.add(prd);
			}
		
			LogSystem.info(BillingSystem.class, "[GET LIST PROD]");

		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return listProd;
	}
	
	
	
	public ProductBilling getProductCode(String code) {
		try {
			ProductBilling pBill=null;
			List<String> data=killBillClient.getPlan(code);
			for (String string : data) {
				String[] p=string.split(" ");
				if(p[0].equals(code)) {
					pBill=new ProductBilling();
					pBill.setProduct_code(p[0]);
					pBill.setHarga(p[1]);
					pBill.setJmlh_ttd(p[2]);
					return pBill;
				}
			}
		
			LogSystem.info(BillingSystem.class, "[GET LIST PROD]");

		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public String topup(String extID, String code) {
		try {
			
			LogSystem.info(BillingSystem.class, "[REQ] trx user "+ extID +", code :"+code);

			String res=killBillClient.topUpPlan(code, extID);
			System.out.println("hsail :" +res);
			LogSystem.info(BillingSystem.class, "[REQ] trx user "+ extID +", code :"+code);

		    if(res.equals("")) {
		    	return ERROR;
		    }
		    else if(res.equals("401")) {
				return SALDO_KURANG;
			}
		    else if(res.length()>3 && res.substring(0, 3).equals("200")) {
		    	lastInvoice=res.split(" ")[1];
				return BERHASIL;
			}
		} catch (KillBillClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ERROR;
	}

	public void close() {
		if(killBillClient!=null)killBillClient.close();
		
	}
	
	public String getLastInvoice() {
		return lastInvoice;
	}
	
	
	
	
	
}
