package id.co.keriss.consolidate.action.billing;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

import id.co.keriss.consolidate.ee.DocumentAccess;

public class Deposit {

	private DocumentAccess docAccess;
	private DB db;
	private BillingSystem billSys;
	private String lastError;
	private User useraccess;
	private char mthd;

	public Deposit(DocumentAccess docAccess, DB db) {
		this.docAccess=docAccess;
		this.db=db;
		useraccess=docAccess.getEeuser();
		mthd=docAccess.getDocument().getPayment();
		// TODO Auto-generated constructor stub
	}
	
		
	private void connectToBillingSys() throws Exception {
		if(billSys==null) {
			billSys=new BillingSystem(useraccess);

//			if(useraccess.getMitra()!=null) {
//				billSys=new BillingSystem(useraccess.getMitra());
//			}else {
//				billSys=new BillingSystem();
//			}
		}
	}
	
	public boolean transaksi(int cnt) throws Exception {
		boolean process=false;
		
		if(mthd=='1') {
			if(useraccess==null) {
				useraccess=new UserManager(db).findByUsername(docAccess.getEmail());
			}
			
			if(useraccess==null) {
				throw new Exception("user null");
			}
			process=pay(cnt);
		}else if(mthd=='2') {
			useraccess=docAccess.getDocument().getEeuser();
			process=pay(cnt);
		}
		return process;
	}
	
	private boolean pay(int cnt) {
		try {
	         System.out.println("connect :");

			connectToBillingSys();
			
	         System.out.println("berhasil :");

			if(useraccess.getPayType().equals("prepaid")||useraccess.getPayType().equals("postpaid")) {
				String res=billSys.transaksi(cnt);
				
				if(res.equals(BillingSystem.BERHASIL)) {
					return true;
				}else if(res.equals(BillingSystem.SALDO_KURANG)) {
					lastError="saldo tidak mencukupi";
					
				}else {
					lastError="error";
				}
			}else if(useraccess.getPayType().equals("langganan")) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean reversal() {
		try {
			
			if(useraccess.getPayType().equals("prepaid")||useraccess.getPayType().equals("postpaid")) {
				String res=billSys.reversal();
				
				if(res.equals(BillingSystem.BERHASIL)) {
					return true;
				}else {
					lastError="error";
				}
			}else if(useraccess.getPayType().equals("langganan")) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	


	public String getLastError() {
		return lastError;
	}

	public BillingSystem getBillSys() {
		return billSys;
	}


	
}
