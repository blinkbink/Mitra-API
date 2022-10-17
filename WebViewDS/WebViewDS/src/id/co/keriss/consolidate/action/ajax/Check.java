package id.co.keriss.consolidate.action.ajax;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Userdata;

public class Check extends ActionSupport {

	public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds
	String getreq = null;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int count = 21;
		DB db = getDB(context);
		HttpServletRequest request = context.getRequest();
	
		try {

			String method = null;
			method = request.getParameter("frmProcess");
			String jsonResult = null;
			
			if (method.equals("email")) {
				JSONObject jo = new JSONObject();
				String res = "Email sudah terdaftar gunakan email lain";
				String user = request.getParameter("email").toString();
				try {
					UserManager mgr = new UserManager (db);
					User u = mgr.getUserByNick (user,false);
					if (u == null) {
						res = "OK";
					}
				} catch (Exception e) {
					// TODO: handle exception
					res = "OK";
					System.out.println("User "+user);
					e.printStackTrace();
				}
				jo.put("msg", res);
	
				jsonResult = jo.toString();
			}
			
			if (method.equals("ktp")) {
				JSONObject jo = new JSONObject();
				String res = "KTP sudah terdaftar";
				String ktp = request.getParameter("ktp").toString();
				try {
					UserdataDao ud = new UserdataDao(db);
					Userdata userdata = ud.findByKtp(ktp);
					System.out.println(ktp);
					if (userdata == null) {
						res = "OK";
					}
				} catch (Exception e) {
					// TODO: handle exception
					res = "OK";
					System.out.println("User "+ktp);
					e.printStackTrace();
				}
				jo.put("msg", res);
	
				jsonResult = jo.toString();
			}
			
			
			if (jsonResult != null && !jsonResult.equals("")) {
				context.put("jsoncontent", jsonResult);
			}
		} catch (Exception e) {
			context.getSyslog().error(e);
		}
	}

}
