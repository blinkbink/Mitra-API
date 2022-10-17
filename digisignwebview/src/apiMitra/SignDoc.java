package apiMitra;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import id.co.keriss.consolidate.util.LogSystem;

public class SignDoc {
	Date tgl;
	public boolean signDoc(User userTrx, DocumentAccess doc, String inv, DB db, HttpServletRequest req, String refTrx, String version) {
		long start = System.currentTimeMillis();
		String path_app = this.getClass().getName();
		String CATEGORY = "SIGN";
		String mitra_req="";
		String email_req="";
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String tanggal = ftanggal.format(date);
		String signdoc = "DSSG" + strDate + ".pdf";
		String path = doc.getDocument().getPath();
		String original = doc.getDocument().getSigndoc();
		boolean usingopt = path.startsWith("/file2/");
		if (!usingopt) {
			String opath = path;
			path="";
			String apath[]=opath.split("/");
			apath[1]="file2";
			for (int i = 0; i < apath.length; i++) {
				path+=apath[i]+"/";
			}
		}
//		System.out.println("invoice = "+inv);
		try {
			DocumentSigner dSign=new DocumentSigner();

			LogSystem.info(req, "Using key version :" + version, refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			
		    
			JSONObject resSign = dSign.sendingPostRequest(doc.getId().toString(), path+signdoc, version);
			if(resSign!=null && resSign.getString("result").equals("00")) {
				Date dateSign= new Date(resSign.getLong("date"));
//				tgl = dateSign;
				doc.setFlag(true);
				doc.setDate_sign(dateSign);
				doc.setInvoice(inv);
				new DocumentsAccessDao(db).update(doc);
				
				Documents documents=doc.getDocument();
				tgl = documents.getWaktu_buat();
				documents.setSigndoc(signdoc);
				documents.setPath(path);
	    		new DocumentsDao(db).update(documents);
	
				
				return true;
			}
			//						
//			System.out.println("Create itext Image \n Path: "+ path + dc.getRename()+"\nDestination :"+path+abc+"\nImage:"+ttds.getPath()+ttds.getRename());
			
		} catch (Exception e) {
			LogSystem.error(getClass(), e);
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
