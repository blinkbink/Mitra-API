package id.co.keriss.consolidate.action.billing;

import java.text.SimpleDateFormat;
import java.util.Date;
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
	String refTrx = "";
	String path_app = this.getClass().getName();
	String mitra_req;
	String email_req;
	String CATEGORY;
	long start;
	
	
	public KillBillDocumentHttps(HttpServletRequest request, String refTrx, String mitra_req, String email_req, long start, String CATEGORY) {
		// TODO Auto-generated constructor stub
		requ=request;
		this.refTrx=refTrx;
		this.mitra_req = mitra_req;
		this.email_req = email_req;
		this.start = start;
		this.CATEGORY = CATEGORY;
	}
			
	public JSONObject getBalance(String ex, HttpServletRequest request) throws KillBillClientException {
		JSONObject res=null;
 		String sr ="";
 		int amount=0;
 		Date tgl= new Date();
 		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		try{
			
			String url = DSAPI.KILLBILL+"Balance.html";
		
	
			JSONObject req=new JSONObject();
			JSONObject json=new JSONObject();
			json.put("externalkey",ex);
			json.put("tenant","document");
			//json.put("amount",String.valueOf(amount));
			req.put("JSONFile",json);
			LogSystem.info(requ,"[POST] "+url+" "+req.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	
			 List<String> response = multipart.finish();
	         
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			LogSystem.info(requ,"[RESP] "+url+" "+sr, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
			JSONObject jos=new JSONObject(sr);
			res=jos.getJSONObject("JSONFile");
			/*
			if(jos.has("data")) {
				JSONObject data=jos.getJSONObject("data");
				amount = data.getInt("amount");
				LogSystem.info(requ,"Amount adalah = "+data.getInt("amount"));
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

	public JSONObject setTransaction(String ex, int amount) throws KillBillClientException {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
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
			req.put("JSONFile",json);
			LogSystem.info(requ,"[POST] "+url+" "+req.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	
			 List<String> response = multipart.finish();
	         
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			LogSystem.info(requ,"[RESP] "+url+" "+sr, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			JSONObject jos=new JSONObject(sr);
			LogSystem.info(requ, jos.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			res=jos.getJSONObject("JSONFile");
			//String result=res.getString("result");
			/*
			if(result.equals("00")) {
				JSONObject data=jos.getJSONObject("data");
				amount = data.getInt("amount");
				LogSystem.info(requ,"Amount adalah = "+data.getInt("amount"));
			} else {
				invoice=result;
			}
			*/

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
		
	}

	public JSONObject reverseTransaction(String invoice, int amount) throws KillBillClientException {
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		JSONObject res=null;
		JSONObject jos=null;
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
			req.put("JSONFile",json);
			LogSystem.info(requ,"[POST] "+url+" "+req.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			String charset = "UTF-8";
			 MultipartUtility multipart = new MultipartUtility(url, charset,null, refTrx);
	         
	         multipart.addFormField("jsonfield", req.toString() );
	
			 List<String> response = multipart.finish();
	         
	
	         for (String line : response) {
	 			sr+=line;
	         }
	
	
			
			LogSystem.info(requ,"[RESP] "+url+" "+sr, refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			res=new JSONObject(sr);
			LogSystem.info(requ, res.toString(), refTrx, path_app, Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			jos=res.getJSONObject("JSONFile");
			result=jos.getString("result");
		

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jos;
		
	}
}
