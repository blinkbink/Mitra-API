package id.co.keriss.consolidate.action.billing;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.killbill.billing.client.KillBillClientException;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.ning.http.client.Request;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;
import jdk.nashorn.internal.parser.JSONParser;

public class KillBillDocumentHttps {
	HttpServletRequest requ=null;
	String refTrx=""; 
	String kelas="action.billing";
	String trxType="PRC-SGN-BILLING";
	
	public KillBillDocumentHttps(HttpServletRequest request, String refTrx) {
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
			json.put("tenant","document");
			req.put("JSONFile",json);
			LogSystem.info(requ, "[POST] "+url+" "+req.toString(), kelas, refTrx, trxType);
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	
			 List<String> response = multipart.finish();
	         
			 LogSystem.info(requ, "SERVER REPLIED:", kelas, refTrx, trxType);
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			LogSystem.info(requ, "[RESP] "+url+" "+sr, kelas, refTrx, trxType);
			JSONObject jos=new JSONObject(sr);
			res=jos.getJSONObject("JSONFile");
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			json.put("tenant","document");
			json.put("amount",String.valueOf(amount));
			json.put("item",doc_id);
			req.put("JSONFile",json);
			LogSystem.info(requ, "[POST] "+url+" "+req.toString(), kelas, refTrx, trxType);
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	
			 List<String> response = multipart.finish();
	         
			 LogSystem.info(requ, "SERVER REPLIED:", kelas, refTrx, trxType);
	
	         for (String line : response) {
	 			sr+=line;
	         }
			
	        LogSystem.info(requ, "[RESP] "+url+" "+sr, kelas, refTrx, trxType);
			JSONObject jos=new JSONObject(sr);
			res=jos.getJSONObject("JSONFile");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			json.put("item",doc_id);
			req.put("JSONFile",json);
			LogSystem.info(requ, "[POST] "+url+" "+req.toString(), kelas, refTrx, trxType);
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	 		LogSystem.info(requ, "[POST] "+url+" "+req.toString(), kelas, refTrx, trxType);
	
			 List<String> response = multipart.finish();
	         
			 LogSystem.info(requ, "SERVER REPLIED:", kelas, refTrx, trxType);
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
	         LogSystem.info(requ, "[RESP] "+url+" "+sr, kelas, refTrx, trxType);
			res=new JSONObject(sr);
			res=res.getJSONObject("JSONFile");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
		
	}
}
