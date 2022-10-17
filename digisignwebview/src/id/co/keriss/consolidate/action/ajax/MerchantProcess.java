package id.co.keriss.consolidate.action.ajax;

import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.dao.TerminalDao;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.util.SystemUtil;

public class MerchantProcess {
	org.jpos.ee.DB db;
	public MerchantProcess(org.jpos.ee.DB db) {
		this.db=db;
		// TODO Auto-generated constructor stub
	}
	public String changeStatus(String id, String tid, boolean sts) throws JSONException {
		// TODO Auto-generated method stub
		JSONObject obj=new JSONObject();
		String status="FAIL";
		TerminalDao tdao=new TerminalDao(db);
	
		try{
			Terminal t=tdao.findByTid(tid, new Long(id));
			t.setActive(sts);
			tdao.updateTerminalCommit(t);
			status="OK";
			obj.put("status", status);
		}
		catch(Exception e){
			SystemUtil.getLog("Error Exception", "Q2").error(e.getMessage());
		}
		return obj.toString();
	}
	
	public String deleteMerchant(String id) throws JSONException {
		JSONObject obj=new JSONObject();
		TerminalDao tdao=new TerminalDao(db);
		MerchantDao mdao=new MerchantDao(db);
		Merchant mDelete=mdao.findById(new Long(id));
		boolean canDeleteMerchant=true;
		Set<Terminal> listTerm=mDelete.getTerminal();
		for (Terminal terminal : listTerm) {
			if(terminal.getBatch().size()==0){
				tdao.deleteTerminalCommit(terminal);
			}else{
				canDeleteMerchant=false;
			}
		}
		
		if(canDeleteMerchant){
			mdao.deleteMerchantCommit(mDelete);
		}else{
			mDelete.setDeleted(true);
			mDelete.setMid("DEL"+mDelete.getMid());
			mdao.updateMerchant(mDelete);
		}
		
		obj.put("status", "OK");
		
		return obj.toString();
	}

}
