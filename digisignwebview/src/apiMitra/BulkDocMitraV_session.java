package apiMitra;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;




public class BulkDocMitraV_session extends ActionSupport implements DSAPI {
	
	@Override
	
	public void execute(JPublishContext context, Configuration cfg) {
		long start = System.currentTimeMillis();
		JSONObject result=new JSONObject();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="BULKWVPAGE"+sdfDate2.format(tgl).toString();
		HttpServletRequest  request  = context.getRequest();
		String sukses_sign = null;
		String fail_sign =null;
		String total_sign =null;
		
		String path_app = this.getClass().getName();
		String CATEGORY = "BULKSIGN";
		String email_req ="";
		String mitra_req ="";
		
		try{
			
	         
	         StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

	         JSONObject object=new JSONObject(sb.toString());
	         
	         if (object.has("refTrx"))
	         {
	        	 refTrx = object.getString("refTrx");
	         }
	         
	         
	         LogSystem.info(request, "PATH :"+request.getRequestURI(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         LogSystem.info(request, sb.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "REQUEST", (System.currentTimeMillis() - start) / 1000f + "s");
	         LogSystem.info(getClass(), sb.toString());
	         
	         sukses_sign = object.getString("sukses");
	         fail_sign = object.getString("gagal");
	         total_sign = object.getString("total");
	         LogSystem.info(request,"BULK DONE :  (SUKSES SIGN) "+sukses_sign+" + "+fail_sign+" (GAGAL SIGN) = "+total_sign+" << Total Dokumen", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, "RESPON", (System.currentTimeMillis() - start) / 1000f + "s");
//	         LogSystem.info(getClass(),"BULK DONE :  (SUKSES SIGN) "+sukses_sign+" + "+fail_sign+" (GAGAL SIGN) = "+total_sign+" << Total Dokumen");
//	         System.out.println("BULK DONE :  (SUKSES SIGN) "+sukses_sign+" + "+fail_sign+" (GAGAL SIGN) = "+total_sign+" << Total Dokumen");
	         
	         

		}catch (Exception e) {
            LogSystem.error(getClass(), e);
            e.printStackTrace();
		}
		
		
		try {
			result.put("sukses", sukses_sign);
			result.put("gagal", fail_sign);
			result.put("total", total_sign);
			
			
			LogSystem.info(request, result.toString(), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			context.put("trxjson", result.toString());
//			LogSystem.info(request, result.toString());
		} catch (JSONException e) {
			LogSystem.error(getClass(), e);
		}
//        context.getResponse().setContentType("application/json");
//
//        LogSystem.info(request, "SEND :"+result.toString());
		
//		LogSystem.response(request, result);
//		HttpSession session = context.getSession();    
//        session.removeAttribute (USER);

	}
	
	
	
	
	
}
