package id.co.keriss.consolidate.action.ajax;

import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.util.ReportUtil;
import id.co.keriss.consolidate.util.SaldoTransaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;


public class DepositProcessForm {
	static SimpleDateFormat fm;

	
	public DepositProcessForm() {
		// TODO Auto-generated constructor stub
		if(fm==null){
			fm=new SimpleDateFormat("HHmmss");
		}
	}
	public String topupProcessing(String amt, String mid, String act, Long userid) throws JSONException {
		// TODO Auto-generated method stub
		String result=null;
		int stn=0;
		
		synchronized (fm) {
			stn=Integer.parseInt(fm.format(new Date()));
		}
		
		SaldoTransaction st=new SaldoTransaction();
		int rc=st.processSaldo(act, mid, String.valueOf(userid), new BigDecimal(amt), stn, userid, "", null,null);
	
		if(rc==0){
			String saldo = null;
			MerchantDao mdao=new MerchantDao(new DB());
			JSONObject obj=new JSONObject();
			obj.put("saldo",mid);

			try {
				BigDecimal bdSal=mdao.getSaldoMerchant(mid);
				saldo=ReportUtil.getInstance().formatNumber(bdSal.doubleValue());
				obj.put("saldo",saldo);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			obj.put("status","OK");
			result=obj.toString();

		}else{
			JSONObject obj=new JSONObject();
			obj.put("status","FAIL");
			result=obj.toString();
			
		}
		return result;
	}

}
