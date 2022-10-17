package id.co.keriss.consolidate.action;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

public class ApiVerification {
	String RC="05";
	String info="Login berhasil";
	User eeuser=null;
	DB db;
	public ApiVerification(DB db) {
		this.db=db;
	}
	public JSONObject setResponFailed(JSONObject dataResp) throws JSONException {
		dataResp.put("result", RC);
		if(!info.equals("")) {
			dataResp.put("info", info);
		}
		return dataResp;
	}
	
	public String getRC() {
		return RC;
	}
	public boolean verification(JSONObject data) throws JSONException {
		String user=data.get("userid").toString();
		UserManager um=new UserManager(db);
		User userLogin=um.findByUsername(user);
		
		//System.out.println("userlogin = "+userLogin.getNick()+"-"+userLogin.getPassword());
		//System.out.println("userid = "+user);
		//System.out.println("password = "+data.getString("pwd"));
		//if(userLogin!=null && userLogin.getPassword().equals(data.get("pwd").toString())){
		if(userLogin!=null && userLogin.getLogin().getPassword().equals(data.get("pwd").toString())){	
	       eeuser=userLogin;

	       if(userLogin.getStatus()=='0') {
			   info="Harap verifikasi terlebih dahulu";
	    	   return false;
	       }else if(userLogin.getStatus()=='1') {
	    	   RC="E1";
			   info="Harap login kembali";

	    	   return false;
	       }else if(userLogin.getStatus()=='2') {
			   info="User anda terblokir";
	    	   return false;
	       }else if(userLogin.getStatus()=='3') {
	       }else if(userLogin.getStatus()=='4') {
	    	   info="User anda terblokir";
	    	   return false;
	       }
	       
	       
	       RC="00";
	       return true;

		}else {
			   info="password/username yang anda masukan salah";
			   System.out.println("user atau password salah");
		}
		
		return false;
		
	}
	
	public User getEeuser() {
		return eeuser;
	}
	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
	}
	
	
}
