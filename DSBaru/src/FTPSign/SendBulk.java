package FTPSign;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;

public class SendBulk implements Runnable{
	DocumentAccess da;
	DB db;
	String inv;
	CloseableHttpClient httpclient;
	public SendBulk(DocumentAccess da, DB db,CloseableHttpClient httpclient, String inv ) {
		this.da=da;
		this.db=db;
		this.inv=inv;
		this.httpclient = httpclient; 
	}
	@Override
	public void run() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//		SimpleDateFormat ftanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		String strDate = sdfDate.format(date);
//		String tanggal = ftanggal.format(date);
		String signdoc = "DSSG" + strDate + ".pdf";
		String path = da.getDocument().getPath();
		DocumentSigner dSign=new DocumentSigner();
		
		System.out.println("Prepare for signing doc:" +da.getId()+" "+da.getDocument().getIdMitra());
		boolean success=false;
		int tryCnt=0;
		while(tryCnt<3) {
			JSONObject resSign = null;
			try {
				resSign = dSign.sendingPostRequest(da.getId().toString(), path+signdoc, httpclient);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				if(resSign!=null && resSign.getString("result").equals("00")) {
					Date dateSign= new Date(resSign.getLong("date"));
					da.setFlag(true);
					da.setDate_sign(dateSign);
					da.setInvoice(inv);
					new DocumentsAccessDao(db).update2(da);
					
					Documents documents=da.getDocument();
					documents.setSigndoc(signdoc);
					new DocumentsDao(db).update2(documents);
	
					System.out.println("completed sign doc:" +da.getId()+" "+da.getDocument().getIdMitra()+" "+da.getDate_sign());
					break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tryCnt++;
		}
	}
	
	private DocumentAccess getDocAccess() {
		return da;
	}
}