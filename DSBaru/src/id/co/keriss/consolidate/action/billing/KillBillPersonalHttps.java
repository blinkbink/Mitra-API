package id.co.keriss.consolidate.action.billing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.killbill.billing.client.KillBillClientException;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class KillBillPersonalHttps {
	HttpServletRequest requ=null;
	Date tgl= new Date();
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
	String refTrx="BILL"+sdfDate2.format(tgl).toString();
	String kelas="id.co.keriss.consolidate.action.billing.KillbillPersonalHttps";
	String trxType="REQ-BILL";
	
	public KillBillPersonalHttps(HttpServletRequest request, String refTrx) {
		// TODO Auto-generated constructor stub
		requ=request;
		this.refTrx=refTrx;
	}
			
	public JSONObject getBalance(String ex, HttpServletRequest request) throws KillBillClientException {
		JSONObject res=null;
 		String sr ="";
 		int amount=0;
		try{
			
			String url = DSAPI.KILLBILL+"Balance.html";
		
	
			JSONObject req=new JSONObject();
			JSONObject json=new JSONObject();
			json.put("externalkey",ex);
			json.put("tenant","personal");
			//json.put("amount",String.valueOf(amount));
			req.put("JSONFile",json);
			LogSystem.info(requ, "[POST] "+url+" "+req.toString(),kelas, refTrx, trxType);
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	 		LogSystem.info(requ, "[POST] "+url+" "+req.toString(),kelas, refTrx, trxType);
	
			 List<String> response = multipart.finish();
	         
	        LogSystem.info(requ, "SERVER REPLIED:");
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			LogSystem.info(requ, "[RESP] "+url+" "+sr,kelas, refTrx, trxType);
			LogSystem.info(request, sr, kelas, refTrx, trxType);
			JSONObject jos=new JSONObject(sr);
			res=jos.getJSONObject("JSONFile");
//			if(jos.has("data")) {
//				JSONObject data=jos.getJSONObject("data");
//				amount = data.getInt("amount");
//				LogSystem.info(requ, "Amount adalah = "+data.getInt("amount"));
//			}
//			else {
//				LogSystem.error(request, sr);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				LogSystem.info(requ, "get balance timeout",kelas, refTrx, trxType);
				res.put("result", "91");
				return res;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return res;
		
		
	}

	public JSONObject setTransaction(String ex, int amount, String doc_id) throws KillBillClientException {
		JSONObject res=null;
 		String sr ="";
 		String invoice=null;
 		//int amount=0;
		try{
				
			String url = DSAPI.KILLBILL+"CreateTransaction.html";
		
	
			JSONObject req=new JSONObject();
			JSONObject json=new JSONObject();
			json.put("externalkey",ex);
			json.put("tenant","personal");
			json.put("amount",String.valueOf(amount));
			json.put("item", doc_id);
			req.put("JSONFile",json);
			LogSystem.info(requ, "[POST] "+url+" "+req.toString(),kelas, refTrx, trxType);
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	 		LogSystem.info(requ, "[POST] "+url+" "+req.toString(),kelas, refTrx, trxType);
	
			 List<String> response = multipart.finish();
	         
	        LogSystem.info(requ, "SERVER REPLIED:",kelas, refTrx, trxType);
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			LogSystem.info(requ, "[RESP] "+url+" "+sr,kelas, refTrx, trxType);
			JSONObject jos=new JSONObject(sr);
			LogSystem.info(requ, jos.toString(),kelas, refTrx, trxType);
			res=jos.getJSONObject("JSONFile");
//			String result=jos.getString("result");
//			if(result.equals("00")) {
//				JSONObject data=jos.getJSONObject("data");
//				amount = data.getInt("amount");
//				LogSystem.info(requ, "Amount adalah = "+data.getInt("amount"));
//			} else {
//				invoice=result;
//			}
		

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				LogSystem.info(requ, "transaksi billing timeout",kelas, refTrx, trxType);
				res.put("result", "91");
				return res;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return res;
		
	}

	public JSONObject reverseTransaction(String invoice, int amount, String doc_id) throws KillBillClientException {
		JSONObject res=null;
		
 		String sr ="";
 		String result=null;
 		//int amount=0;
		try{
				
			String url = DSAPI.KILLBILL+"Reversal.html";
		
	
			JSONObject req=new JSONObject();
			JSONObject json=new JSONObject();
			json.put("invoiceid",invoice);
			//json.put("tenant","document");
			json.put("amount",String.valueOf(amount));
			json.put("item", doc_id);
			req.put("JSONFile",json);
			LogSystem.info(requ, "[POST] "+url+" "+req.toString(),kelas, refTrx, trxType);
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString());
	 		LogSystem.info(requ, "[POST] "+url+" "+req.toString(),kelas, refTrx, trxType);
	
			 List<String> response = multipart.finish();
	         
			 LogSystem.info(requ, "SERVER REPLIED:",kelas, refTrx, trxType);
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
	         LogSystem.info(requ, "[RESP] "+url+" "+sr,kelas, refTrx, trxType);
			JSONObject jos=new JSONObject(sr);
			LogSystem.info(requ, jos.toString(),kelas, refTrx, trxType);
			res=jos.getJSONObject("JSONFile");
			//result=jos.getString("result");
		

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				LogSystem.info(requ, "reversal timeout",kelas, refTrx, trxType);
				res.put("result", "91");
				return res;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return res;
		
	}
}
