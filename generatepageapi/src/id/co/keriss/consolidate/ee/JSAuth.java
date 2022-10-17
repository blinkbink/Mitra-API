package id.co.keriss.consolidate.ee;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

import id.co.keriss.consolidate.util.RSAEncryption;

public class JSAuth {

	private String idDoc;
	private User user;
	private DB db;
	private String notif;
	
	public JSAuth(DB db) {
		this.db=db;
		
	}
 
	public String getIdDoc() {
		return idDoc;
	}

	public User getUser() {
		return user;
	}

	public String getNotif() {
		return notif;
	}

	public boolean processAuth(String auth){
		boolean stat=false;
		notif="User not found";
		String clearText=RSAEncryption.decryptWithPriv(auth);
		String data[]=clearText.split(";");
		user=new UserManager(db).findByUsername(data[1]);
		if(data.length<4) {
			notif="id document null";
		}
		else if(user!=null) {
			  idDoc=data[3];
//			  if(user.getPassword().equals(data[0])) {
			if(user.getLogin().getPassword().equals(data[0])) {	   
				  notif="success";
				  stat=true;
			  }else {
				  user=null;
		  	  }
		}

		
		return stat;
	}
}
