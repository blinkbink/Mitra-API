package apiMitra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;

import id.co.keriss.consolidate.action.kms.DocumentSigner;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.KeyV3Dao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.KeyV3;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.KmsService;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.SaveFileWithSamba;

public class SignDoc {
	String signdoc="";
	
	public String getSigndoc() {
		return signdoc;
	}

	public void setSigndoc(String signdoc) {
		this.signdoc = signdoc;
	}

	public boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db, HttpServletRequest  request, String refTrx, String version) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String tanggal = ftanggal.format(date);
		signdoc = "APIA" + strDate+doc.getId().toString() +".pdf";
		String path = doc.getDocument().getPath();
		String original = doc.getDocument().getSigndoc();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		//String refTrx="SGN"+sdfDate2.format(tgl).toString();
		String kelas="apiMitra.SendDocMitraAT";
		String trxType="PRC-SGN";
		
//		System.out.println("invoice = "+inv);
		LogSystem.info(request, "Invoice = "+inv, kelas, refTrx, trxType);
		try {
			DocumentSigner dSign=new DocumentSigner();
			//JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc);

		    LogSystem.info(request, "Using key version :" + version, kelas, refTrx, trxType);
			JSONObject resSign = dSign.sendingPostRequest2(doc.getId().toString(), path+signdoc, request, refTrx, version);
			
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
				doc.setFlag(true);
				doc.setDate_sign(dateSign);
				doc.setInvoice(inv);
				
				Documents documents=doc.getDocument();
				documents.setSigndoc(signdoc);
				
				try {
					new DocumentsAccessDao(db).update(doc);
					new DocumentsDao(db).update(documents);
				} catch (Exception e) {
					// TODO: handle exception
					LogSystem.info(request, "DB Timeout", kelas, refTrx, trxType);
					return false;
				}
				
	    		LogSystem.info(request, "Proses tandatangan berhasil", kelas, refTrx, trxType);
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
			return false;
		}
		return false;


	}
	
	public boolean signDoc2(User userTrx, List<DocumentAccess> doc, String inv, DB db, HttpServletRequest  request, String refTrx, Documents document, String version) throws JSONException {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		//SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		//String tanggal = ftanggal.format(date);
		signdoc = "APIA" + strDate+doc.get(0).getId() +".pdf";
		String path = document.getPath();
		//String original = document.getSigndoc();
		//Date tgl= new Date();
		//SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		//String refTrx="SGN"+sdfDate2.format(tgl).toString();
		String kelas="apiMitra.SendDocMitraAT";
		String trxType="PRC-SGN";
		
//		System.out.println("invoice = "+inv);
		LogSystem.info(request, "Invoice = "+inv, kelas, refTrx, trxType);
		JSONArray docaccess=new JSONArray();
		JSONArray docaccessv3=new JSONArray();
		
		for(DocumentAccess da:doc) {
			docaccess.put(da.getId());
		}
		try {
			DocumentSigner dSign=new DocumentSigner();
			LogSystem.info(request, "Using key version :" + version, kelas, refTrx, trxType);
			JSONObject resSign = dSign.kirimKMS(docaccess, path+signdoc, request, refTrx, doc.get(0).getEeuser().getId(), document.getId(), version);
			LogSystem.info(request, "decrypt = "+resSign, kelas, refTrx, trxType);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
				
				for(DocumentAccess da:doc) {
					da.setFlag(true);
					da.setDate_sign(dateSign);
					da.setInvoice(inv);
					new DocumentsAccessDao(db).update(da);
				}
				
				try {
					document.setSigndoc(signdoc);
					new DocumentsDao(db).update(document);
				} catch (Exception e) {
					// TODO: handle exception
					LogSystem.info(request, "DB Timeout", kelas, refTrx, trxType);
					return false;
				}
				
	    		LogSystem.info(request, "Proses tandatangan berhasil", kelas, refTrx, trxType);
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
			return false;
		}
		return false;
	}
	
	public boolean signDoc3(User userTrx, List<DocumentAccess> doc, String inv, DB db, HttpServletRequest  request, String refTrx, Documents document, String invstempel, String version, String invmeterai) throws Exception {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		//SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		//String tanggal = ftanggal.format(date);
		signdoc = "APIA" + strDate+doc.get(0).getId() +".pdf";
		String path = document.getPath();
		
		String kelas="apiMitra.SendDocMitraAT";
		String trxType="PRC-SGN";
		
//		System.out.println("invoice = "+inv);
		LogSystem.info(request, "Invoice = "+inv, kelas, refTrx, trxType);
		JSONArray docaccess=new JSONArray();
		
		for(DocumentAccess da:doc) {
			docaccess.put(da.getId());
		}
		LogSystem.info(request, "List Access :" + docaccess, kelas, refTrx, trxType);
		try {
			DocumentSigner dSign=new DocumentSigner();
			LogSystem.info(request, "Using key version :" + version, kelas, refTrx, trxType);
			JSONObject resSign = null;

			resSign = dSign.kirimKMS(docaccess, path+signdoc, request, refTrx, doc.get(0).getEeuser().getId(), document.getId(), version);
		
			LogSystem.info(request, "decrypt = "+resSign, kelas, refTrx, trxType);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
				
				for(DocumentAccess da:doc) {
					da.setFlag(true);
					da.setDate_sign(dateSign);
					if(da.getType().equalsIgnoreCase("seal")) {
						da.setInvoice(invstempel);
					}
					else if(da.getType().equalsIgnoreCase("meterai")) {
						da.setInvoice(invmeterai);
					}
					else {
						da.setInvoice(inv);
					}
					
					new DocumentsAccessDao(db).update(da);
				}
				
				try {
					document.setSigndoc(signdoc);
					new DocumentsDao(db).update(document);
				} catch (Exception e) {
					// TODO: handle exception
					LogSystem.info(request, "DB Timeout", kelas, refTrx, trxType);
					return false;
				}
				
	    		LogSystem.info(request, "Proses tandatangan berhasil", kelas, refTrx, trxType);
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
			return false;
		}
		return false;


	}
	
	public boolean signDocAT(User userTrx, DocumentAccess doc, String inv, DB db, HttpServletRequest  request, String version) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String tanggal = ftanggal.format(date);
		String signdoc = "API" + strDate+doc.getId().toString() +".pdf";
		String path = doc.getDocument().getPath();
		String original = doc.getDocument().getSigndoc();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="SGN"+sdfDate2.format(tgl).toString();
		String kelas="apiMitra.SendDocMitraAT";
		String trxType="PRC-SGN";
		

		LogSystem.info(request, "Invoice = "+inv, kelas, refTrx, trxType);
		try {
			DocumentSigner dSign=new DocumentSigner();

		    LogSystem.info(request, "Using key version :" + version, kelas, refTrx, trxType);
		    
			JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc, version, refTrx);
			LogSystem.info(request, "response kms = "+resSign, kelas, refTrx, trxType);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
				doc.setFlag(true);
				doc.setDate_sign(dateSign);
				doc.setInvoice(inv);
				doc.setAction("at");
				new DocumentsAccessDao(db).update(doc);
				
				Documents documents=doc.getDocument();
				documents.setSigndoc(signdoc);
	    		new DocumentsDao(db).update(documents);
	
	    		LogSystem.info(request, "Proses tandatangan berhasil", kelas, refTrx, trxType);
				return true;
			}
		} catch (Exception e) {
			LogSystem.error(getClass(), e,kelas, refTrx, trxType);
		}
		return false;


	}
}
