package api;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.action.ActionSupport;


import org.hibernate.Session;
import org.hibernate.Transaction;
import id.co.keriss.consolidate.util.LogSystem;



import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.jcajce.provider.symmetric.ARC4.Base;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.ee.action.Login;
import org.jpos.iso.ISOUtil;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;

public class AccountAPI extends ActionSupport {

	SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		//userRecv=new UserManager(db).findById((long) 5790);
      
		HttpServletRequest  request  = context.getRequest();
		
		try {
			
			context.put("trxjson", "");

			

		}catch (Exception e) {

			LogSystem.error(getClass(), e);
		}
	}
	

}
