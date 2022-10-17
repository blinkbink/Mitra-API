package id.co.keriss.consolidate.action.billing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.killbill.billing.client.KillBillClientException;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class SistemBilling {
	HttpServletRequest requ=null;
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="BILL"+sdfDate2.format(tgl).toString();
	String kelas="id.co.keriss.consolidate.action.billing.KillbillPersonalHttps";
	String trxType="REQ-BILL";
	
	public SistemBilling(HttpServletRequest request, String refTrx) {
		// TODO Auto-generated constructor stub
		requ=request;
		this.refTrx=refTrx;
	}
			
	public JSONObject creatAccountPrepaid(String ex, String name, String email, String address, String phone, String company_name, HttpServletRequest request) throws KillBillClientException {
		JSONObject res=null;
 		String sr ="";
 		int amount=0;
		try{
			
			String url = DSAPI.KILLBILL+"NewAccount.html";
		
	
			JSONObject req=new JSONObject();
			JSONObject json=new JSONObject();
			json.put("externalkey",ex);
			json.put("name",name);
			json.put("email",email);
			json.put("address",address);
			json.put("phone",phone);
			json.put("company_name",company_name);
			//json.put("amount",String.valueOf(amount));
			req.put("JSONFile",json);
			System.out.println("[POST] "+url+" "+req.toString());
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	 		System.out.println("[POST] "+url+" "+req.toString());
	
			 List<String> response = multipart.finish();
	         
	        System.out.println("SERVER REPLIED:");
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			System.out.println("[RESP] "+url+" "+sr);
			LogSystem.info(request, sr, kelas, refTrx, trxType);
			JSONObject jos=new JSONObject(sr);
			res=jos.getJSONObject("JSONFile");
			/*
			if(jos.has("data")) {
				JSONObject data=jos.getJSONObject("data");
				amount = data.getInt("amount");
				System.out.println("Amount adalah = "+data.getInt("amount"));
			}
			else {
				LogSystem.error(request, sr);
			}
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
		
		
	}

	
}
