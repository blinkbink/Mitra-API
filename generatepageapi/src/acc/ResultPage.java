package acc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class ResultPage extends ActionSupport implements DSAPI {

	
	/**
	 * { "JSONFile" : { "document_id":"dgs_0003", "status":"0", "status_document":"complete", "result":"00", "notif":"Proses tanda tangan berhasil!" } }
	 */
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		// TODO Auto-generated method stub
		org.jpos.ee.DB db=getDB(context);
		HttpServletRequest  request  = context.getRequest();
		String msg=request.getParameter("msg");
		Date idTrx=new Date();
		TokenMitra tm=new TokenMitra();
		TokenMitraDao tdao=new TokenMitraDao(db);
		LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, msg, idTrx, "RCV"));
		tm=tdao.findByName(ACC_MITRA);
		try {
			String data=AESEncryption.decryptLinkMitra(tm.getAes_key(),msg);
//			LogSystem.info(request, data);
			LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, data, idTrx, "LOG"));

			JSONObject jo=new JSONObject(data);
			JSONObject accData=new JSONObject();
			accData.put("Email", jo.getString("email_user"));
			accData.put("IdDok", jo.getString("document_id"));
			accData.put("Message", "Done");
			boolean viewOnly=jo.has("view_only")?jo.getBoolean("view_only"):false;
			if(!viewOnly) {
				Documents d=new DocumentsDao(db).findByUserDocIDMitra(tm.getMitra().getId(),  jo.getString("document_id")).get(0);
				if(d.isSign()) {
					accData.put("StatusDok", "Completed");
					sendData(accData.toString(),request,idTrx);
				}else {
//					LogSystem.info(request, "DOCUMENT WAITING OTHER SIGNER");
					LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, "DOCUMENT WAITING OTHER SIGNER", idTrx, "LOG"));
					accData.put("StatusDok", "Waiting");
					sendData(accData.toString(),request,idTrx);

				}
			}else {
//				JSONObject viewJSON=new JSONObject();
//				JSONObject dV=new JSONObject();
////				dV.put("P_NO_REGISTRATION", "");
//				dV.put("P_EMAIL_DIGISIGN", jo.getString("email_user"));
//				dV.put("P_DOC_ID", jo.getString("document_id"));
//				dV.put("P_FLAG_VIEW", "Y");
//				viewJSON.put("signGenViewDocOne", dV );
//				sendView(viewJSON.toString(),request);
//
//				LogSystem.info(request, "DOCUMENT VIEW");
				
				String doid = jo.getString("document_id");
                String preg = doid.substring(0,doid.length()-4);
                JSONObject viewJSON=new JSONObject();
                JSONObject dV=new JSONObject();
                dV.put("P_NO_REGISTRATION", preg);
                dV.put("P_EMAIL_DIGISIGN", jo.getString("email_user"));
                dV.put("P_DOC_ID", doid);
                dV.put("P_FLAG_VIEW", "Y");
                viewJSON.put("signGenViewDocOne", dV );
                sendView(viewJSON.toString(),request, idTrx);
//				LogSystem.info(request, "DOCUMENT VIEW");
				LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, "DOCUMENT VIEW", idTrx, "LOG"));


			}
			context.put("view_only", viewOnly);
//			else
//				accData.put("StatusDok", "Waiting");
//			LogSystem.info(request, "completed");
			LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, "COMPLETED", idTrx, "LOG"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				context.getResponse().sendError(404);
//				LogSystem.error(getClass(),e);
//				LogSystem.info(request, "404");
				LogManager.getLogger(getClass()).error(LogSystem.getRESLog(request, ExceptionUtils.getStackTrace(e), idTrx, "ERROR"));

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	
	}
	
	public void sendData(String data,HttpServletRequest request,Date idTrx) {
		 HttpURLConnection conn = null;
			try {
				
				URL url=new URL(ACC_CALLBACK+"/accone_API/rest/SendNotifAndChangeStatus/SendNotifAndChangeStatus");
				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("POST");
		        conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
		        conn.setRequestProperty("Content-Type", "application/json");
		        conn.setDoOutput(true);
		        LogManager.getLogger(getClass()).info(url);
//				LogSystem.info(request, "REQ:"+data);
				LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, data, idTrx, "SND"));

		        conn.getOutputStream().write(data.getBytes());
		        conn.setConnectTimeout(30000);
		        
		        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		        int responseCode=conn.getResponseCode();
		        String a = "";
		        for (int c; (c = in.read()) >= 0;)
		            a+=((char)c);
		        
//				LogSystem.info(request, "RSP:"+a);
				LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, "RSP CODE "+responseCode+" , "+a, idTrx, "RCV"));

		        in.close();
			} catch (java.net.SocketTimeoutException t) {
				   //return false;
				t.printStackTrace();
				System.out.println("timeout");
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				conn.disconnect();
			}
	}
	
	/**
	 * https://sofiadev.acc.co.id/rest/com/acc/accme/in/httprest/esignApi/signgenviewdoc
	 * @param data
	 * @param request
	 */
	public void sendView(String data,HttpServletRequest request,Date idTrx) {
		 HttpURLConnection conn = null;
			try {
				
				URL url=new URL(ACC_CALLBACK_VIEW+"/rest/com/acc/accme/in/httprest/esignApi/signgenviewdoc");
				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("POST");
		        conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
		        conn.setRequestProperty("Content-Type", "application/json");
		        conn.setRequestProperty("imei", "359896060882436");
		        conn.setRequestProperty("os", "442");
		        conn.setRequestProperty("latitude", "-4,007393");
		        conn.setRequestProperty("longitude", "119,638463");
		        conn.setRequestProperty("versiapp", "1.0");
		        conn.setRequestProperty("ip_addr", "10.7.14.119");
		        conn.setRequestProperty("time", "2013-01-02 02:16:19");
		        conn.setRequestProperty("language", "EN");
		        conn.setRequestProperty("token", "TUZU1GWTX4MW8DWLVYQOJH7VALRA0O5E8RY2D2EDUJNCN7H5UQOZH7S4GQ414276VLZLGAZPHD17OP04CG9IWYIOI24I5MJQ4MY4IGVB4Q66WT8S6DK10PUDO34LWIFM0ZB75VGH6FPSRRHOGOJ5BE4SQ2QFYAY4PHD1PPACT9TSGC24SN");
		        conn.setRequestProperty("user", "FAT.W14797");
		        conn.setRequestProperty("APIKey", "1234567890");
		        conn.setRequestProperty("cache-control", "no-cache");
		        conn.setRequestProperty("Postman-Token", "7a17d036-ab61-4cbd-8e68-e0c680468b31");
		        conn.setDoOutput(true);
		        LogManager.getLogger(getClass()).info(url);
//				LogManager.getLogger(getClass()).infoLogSystem.info(request, "REQ:"+data);
				LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, data, idTrx, "SND"));

		        conn.getOutputStream().write(data.getBytes());
		        conn.setConnectTimeout(30000);
		        
		        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		        int responseCode=conn.getResponseCode();
		        String a = "";
		        for (int c; (c = in.read()) >= 0;)
		            a+=((char)c);
		        
//				LogSystem.info(request, "RSP CD:"+a);
				LogManager.getLogger(getClass()).info(LogSystem.getRESLog(request, "RSP CODE "+responseCode+" , "+ a, idTrx, "RCV"));

		        in.close();
			} catch (java.net.SocketTimeoutException t) {
				   //return false;
				t.printStackTrace();
				System.out.println("timeout");
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				conn.disconnect();
			}
	}

}
