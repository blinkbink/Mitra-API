package apiMitra;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.mapping.Array;
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
import id.co.keriss.consolidate.util.LogSystem;

public class SignDoc {
	Date tgl;
	
	public boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db, String refTrx, HttpServletRequest request, String version) {
		String kelas="apiMitra.SignDoc";
		String trxType="PRC-SGN";
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String tanggal = ftanggal.format(date);
		String signdoc = "API" +strDate+doc.getId().toString()+ ".pdf";
		Documents documents=doc.getDocument();
		String path = documents.getPath();
		String pathupdate=path;
		String[] split = path.split("/");
		boolean update=false;
		if(split[1].equalsIgnoreCase("opt")) {
			pathupdate=path.replace("opt", "file");
			update=true;
		}
		String original = doc.getDocument().getSigndoc();
		
		System.out.println("invoice = "+inv);
		try {
			DocumentSigner dSign=new DocumentSigner();
			LogSystem.info(request, "request Sign ke KMS", kelas, refTrx, trxType);
	
		    LogSystem.info(request, "Using key version :" + version, kelas, refTrx, trxType);
		    
			JSONObject resSign = dSign.sendingPostRequest2(doc.getId().toString(), pathupdate+signdoc, request, refTrx, version);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
//				tgl = dateSign;
				doc.setFlag(true);
				doc.setDate_sign(dateSign);
				doc.setInvoice(inv);
				new DocumentsAccessDao(db).update(doc);
				
				//Documents documents=doc.getDocument();
				if(update) {
					documents.setPath(pathupdate);
				}
				tgl = dateSign;
				documents.setSigndoc(signdoc);
	    		new DocumentsDao(db).update(documents);
	
				
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			LogSystem.info(request, "Gagal sign, KMS timeout", kelas, refTrx, trxType);
			return false;
		}
		return false;


	}
	
	public boolean signDoc2(User userTrx, Documents doc, String inv, DB db, String refTrx, HttpServletRequest request, List<DocumentAccess> ldoca, String version) {
		
		String kelas="apiMitra.SignDoc";
		String trxType="PRC-SGN";
		LogSystem.info(request, "Masuk request sign", kelas, refTrx, trxType);
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		//SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		//String tanggal = ftanggal.format(date);
		String signdoc = "API" +strDate+doc.getId().toString()+ ".pdf";
		Documents documents=doc;
		String path = documents.getPath();
		String pathupdate=path;
		String[] split = path.split("/");
		boolean update=false;
		if(split[1].equalsIgnoreCase("opt")) {
			pathupdate=path.replace("opt", "file2");
			update=true;
		}
		
		if(split[1].equalsIgnoreCase("file")) {
			pathupdate=path.replace("file", "file2");
			update=true;
		}
		
		//String original = doc.getSigndoc();
		//Long[] idDocAcc = new Long[ldoca.size()];
		JSONArray docAccessArr=new JSONArray();
		//String splitdocacc="";
		//int x=0;
		String emailUser=ldoca.get(0).getEmail();
		for(DocumentAccess da:ldoca) {
			docAccessArr.put(da.getId());
			LogSystem.info(request, "idAccess = "+da.getId(), kelas, refTrx, trxType);
		}
 		
		System.out.println("invoice = "+inv);
		LogSystem.info(request, "idAccess = "+docAccessArr.toString(), kelas, refTrx, trxType);
		try {
			DocumentSigner dSign=new DocumentSigner();
			LogSystem.info(request, "request Sign ke KMS", kelas, refTrx, trxType);
			//JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc, refTrx, request);
//			JSONObject resSign = dSign.sendingPostRequest2(doc.getId().toString(), pathupdate+signdoc, request, refTrx);
		
		    LogSystem.info(request, "Using key version :" + version, kelas, refTrx, trxType);
		    
			JSONObject resSign = dSign.kirimKMS(docAccessArr, pathupdate+signdoc, request, refTrx, userTrx.getId(), doc.getId(), version);
			LogSystem.info(request, resSign.toString(), kelas, refTrx, trxType);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
//				tgl = dateSign;
//				doc.setFlag(true);
//				doc.setDate_sign(dateSign);
//				doc.setInvoice(inv);
//				new DocumentsAccessDao(db).update(doc);
				new DocumentsAccessDao(db).updateWhereDocumentEmailFlagTrue(doc.getId(), emailUser, dateSign, inv);
				/*
				DocumentsAccessDao dadao=new DocumentsAccessDao(db);
				for(DocumentAccess da:ldoca) {
					da.setDate_sign(dateSign);
					da.setInvoice(inv);
					da.setFlag(true);
					dadao.update(da);
				}
				*/
				//Documents documents=doc.getDocument();
				if(update) {
					documents.setPath(pathupdate);
				}
				tgl = dateSign;
				documents.setSigndoc(signdoc);
	    		new DocumentsDao(db).update(documents);
	
				
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			LogSystem.info(request, "Gagal sign, KMS timeout", kelas, refTrx, trxType);
			return false;
		}
		return false;


	}

	
	public Date getTgl() {
		return tgl;
	}
	public void setTgl(Date tgl) {
		this.tgl = tgl;
	}
	
}
