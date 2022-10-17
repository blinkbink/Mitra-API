package id.co.keriss.consolidate.action.ajax;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.util.ReportUtil;

public class Searchmid extends ActionSupport {
	String mid = null;
	String mName = null;
	String bank = null;
	String cp = null;
	String storeid, merchantid, merchantname;
	int codemid = 0;
	int codemName = 0;
	int codestore = 0;
	int codebank = 0;
	int codecp = 0;
	boolean stat = true;
	private Boolean content = true;
	private String fromtemp,totemp;
	JPublishContext cont;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		codemid = 0;
		codemName = 0;
		codestore = 0;
		codebank = 0;
		stat=true;
		cont=context;
		int count = 21;
		DB db = getDB(context);

		try{
			int start = 0;
			HttpServletRequest  request  = context.getRequest();
//			try{
//				editStatus(request.getParameter ("tid"), request.getParameter ("status"));
//			}
//			catch(Exception e){
//				
//			}
			merchantid = request.getParameter("mid")==null? null:request.getParameter("mid");
			merchantname = request.getParameter("search")==null? null:request.getParameter("search");
			
			
		
			if(merchantid==null) {
				merchantid="0";
				MerchantDao md = new MerchantDao(db);
				List<Merchant> m = md.findAllWithLike(merchantname.toLowerCase());
				JSONArray jsonArray=new JSONArray();
				 
				for (Merchant merchant : m) {
				    JSONObject obj=new JSONObject();
					  obj.put("id", merchant.getId());
					  obj.put("mid", merchant.getMid());
					  obj.put("name",merchant.getName());
					  obj.put("address",merchant.getAddress());
					  if(merchant.getSaldo()!=null)obj.put("saldo",ReportUtil.getInstance().formatNumber(merchant.getSaldo().doubleValue()));
					  else obj.put("saldo","0");		
					  if(merchant.getUsername()!=null)obj.put("userid",merchant.getUsername());
					  else obj.put("userid","-");
					
					  jsonArray.put(obj);
				}
				
	            context.put("jsoncontent", jsonArray.toString());

			}else{
				MerchantDao md = new MerchantDao(db);
				Merchant merchant = md.findByMid(merchantid);
				 
			    JSONObject obj=new JSONObject();
				  obj.put("id", merchant.getId());
				  obj.put("mid", merchant.getMid());
				  obj.put("name",merchant.getName());
				  obj.put("address",merchant.getAddress());
				  if(merchant.getSaldo()!=null)obj.put("saldo",ReportUtil.getInstance().formatNumber(merchant.getSaldo().doubleValue()));
				  else obj.put("saldo","0");		
				  if(merchant.getUsername()!=null)obj.put("userid",merchant.getUsername());
				  else obj.put("userid","-");
				
	            context.put("jsoncontent", obj.toString());

			}
			
			

		}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
            context.getSyslog().error (e);
		}
	}
	
	
	
	
	
}
