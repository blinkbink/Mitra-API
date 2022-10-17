package id.co.keriss.consolidate.action.ajax;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;

import id.co.keriss.consolidate.dao.PrepaidDao;
import id.co.keriss.consolidate.dao.SaldoLogDao;
import id.co.keriss.consolidate.dao.StatusTrxLogDao;
import id.co.keriss.consolidate.dao.TransactionDao;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.Prepaid;
import id.co.keriss.consolidate.ee.SaldoLog;
import id.co.keriss.consolidate.ee.StatusTrxLog;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.Transaction;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.PLNUtil;
import id.co.keriss.consolidate.util.SaldoTransaction;
import id.co.keriss.consolidate.util.SystemUtil;


public class TransactionProcessForm {
	private TransactionDao transDao;
	private SaldoLogDao saldoLogDao;
	private DB db;
	private Transaction trx;
	private StatusTrxLog newStat;
	private User user;
	private List<StatusTrxLog> listSTL=null;
	JSONObject data=null;
	public TransactionProcessForm(DB db) {
		this.db=db;
		transDao=new TransactionDao(db);
		saldoLogDao=new SaldoLogDao(db);
	}
	
	public List<StatusTrxLog> getListSTL() {
		return listSTL;
	}

	public void setListSTL(List<StatusTrxLog> listSTL) {
		this.listSTL = listSTL;
	}

	public StatusTrxLog getNewStat() {
		return newStat;
	}

	public void setNewStat(StatusTrxLog newStat) {
		this.newStat = newStat;
	}

	public String getDetailTrx(String id, User u) throws JSONException{
		String result="data not found";
		JSONObject obj=new JSONObject();
		trx=transDao.findById(new Long(id));
		if(trx!=null){
			//if(u.hasPermission("admin") || u.getMid().equals(trx.getBatch().getTerminal().getMerchant().getMid())){
			if(u.hasPermission("admin")) {
				data=new JSONObject();
				Terminal t=trx.getBatch().getTerminal();
				data.put("Transaction", trx.getProductname());
				data.put("Merchant Name",  t.getMerchant().getName());
				data.put("Merchant ID", t.getMerchant().getMid());
				data.put("Terminal ID", t.getTid());
				data.put("Request ID", trx.getReqid());
				data.put("Date/Time", trx.getApprovaltime());
				data.put("Product Code", trx.getProductcode());
				if(trx.getProductcode().substring(0, 3).equals("PLN")){
					if(trx.getType().equals("SUCCESS")){
						PrepaidDao prepaidDao=new PrepaidDao(db);
						Prepaid pre=prepaidDao.findById(trx.getId());
						data.put("Subs Id", pre.getSubs_id());
						data.put("Meter Id", pre.getNo_meter());
						data.put("Subs Name", pre.getName());
						data.put("Segment", pre.getSegment() +" / "+pre.getDaya()+" VA");
						data.put("Power Purchase", pre.getPower_purchase());
						data.put("Kwh", pre.getPower_kwh());
						data.put("Token", PLNUtil.getStringTokenXML(pre.getToken()));
					}else{
						data.put("Subs Id", trx.getCardno());
					}

				}else{

					if(u.hasPermission("admin")){
						if(trx.getProductsuppcode()!=null)data.put("Supplier Code", trx.getProductsuppcode());
						if(trx.getSupplier()!=null)data.put("Supplier", trx.getSupplier().getName());
						data.put("MSISDN", trx.getCardno());
						if(trx.getSn()!=null)data.put("SN", trx.getSn());
						if(trx.getMsg()!=null)data.put("Msg Supplier", trx.getMsg());
					}
					else{
						data.put("MSISDN", trx.getCardno());
						if(trx.getSn()!=null)data.put("SN", trx.getSn());
					}
				}
				if(trx.getDepositamount()!=null) data.put("Price", "Rp "+SystemUtil.amountFormatStr(trx.getDepositamount()));
				data.put("Status", trx.getType());
				
				if(u.hasPermission("admin")){
					StatusTrxLogDao stlDao=new StatusTrxLogDao(db);
					try {
						listSTL=stlDao.getStatusTrxLog(trx.getId());
						if(listSTL!=null && listSTL.size()>0){
							JSONArray dataStatus=new JSONArray();
							for (StatusTrxLog statusTrxLog : listSTL) {
								JSONObject jsonObject=new JSONObject();
								jsonObject.put("Datetime",SystemUtil.changeFormatDate(statusTrxLog.getDatetime()));
								jsonObject.put("Information",statusTrxLog.getInformation());
								jsonObject.put("SN",statusTrxLog.getOldsn());
								jsonObject.put("Price",SystemUtil.amountFormatStr(statusTrxLog.getOldprice()));
								jsonObject.put("Status",statusTrxLog.getOldstatus());
								jsonObject.put("User",statusTrxLog.getUser());
								dataStatus.put(jsonObject);
							}
							data.put("listlog", dataStatus);
						}
					} catch (SQLException e) {
						LogSystem.error(getClass(), e);
					}
					
				}
				result="OK";
				
			}else{
				result="permission error";
			}
		}
		obj.put("status", result);
		if(data!=null){
			obj.put("data", data);
		}
		return obj.toString();	
	}
	
	public String changeStatTrx(String id, String status, User u) throws JSONException {
		String result="";
		user=u;
		JSONObject obj=new JSONObject();
		trx=transDao.findById(new Long(id));
		if(trx==null) {
			obj.put("status", status);
			return obj.toString();
		}
		
		switch (status) {
			case "SUCCESS":
				result= successTrans();
				break;
			
			case "CANCEL":
			case "FAILED":
				result= cancelTrans();
				break;
	
			default:
				break;
		}
		obj.put("status", status);
		if(status=="OK"){
			getDetailTrx(id, u);
			obj.put("data", data);
		}
		return obj.toString();
	}
	
	String cancelTrans(){
		String result="";
		BigDecimal diffprice=null;
		Terminal term=null;
		Merchant merch=null;
		term=trx.getTransaction().getBatch().getTerminal();
		merch=term.getMerchant();
		
		if(newStat.getOldprice()!=null && newStat.getOldprice().compareTo(trx.getDepositamount())!=0 ){
			diffprice=newStat.getOldprice();
		}
		
		if(trx.getType().equals("REVERSAL"))
		{
			if(diffprice!=null){
				result="Error Price";
			}
			else{
				result="OK";
			}
		}else{
			result=sendDepositChange(diffprice, term, merch, "REVERSAL");
			
		}
		
		if(result.equals("OK")) updateDataTrx();
		
		return result;
		
	}
	
	String successTrans(){
		String result="";
		BigDecimal diffprice=null;
		Terminal term=null;
		Merchant merch=null;
		term=trx.getTransaction().getBatch().getTerminal();
		merch=term.getMerchant();
		
		if(newStat.getOldprice()!=null && newStat.getOldprice().compareTo(trx.getDepositamount())!=0 ){
			diffprice=newStat.getOldprice();
		}
		
		if(trx.getType().equals("SUCCESS"))
		{
			if(diffprice!=null){
				result="Error Price";
			}
			else{
				result="OK";
			}
		}else{
			result=sendDepositChange(diffprice, term, merch, "DEBET");
			
		}
		
		if(result.equals("OK")) updateDataTrx();
		
		return result;
		
	}
	
	private void updateDataTrx() {
		StatusTrxLogDao stlDao=new StatusTrxLogDao(db);
		StatusTrxLog oldData=new StatusTrxLog();
		if(newStat.getInformation()!=null)oldData.setInformation(newStat.getInformation());
		if(trx.getDepositamount()!=null)oldData.setOldprice(trx.getDepositamount());
		if(trx.getSn()!=null)oldData.setOldsn(trx.getSn());
		if(trx.getType()!=null)oldData.setOldstatus(trx.getType());
		oldData.setTransaction(trx.getId());
		
		try {
			stlDao.insertSTL(oldData, user.getId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
		
		if(newStat.getOldsn()!=null)trx.setSn(newStat.getOldsn());
		if(newStat.getOldprice()!=null){
			trx.setDepositamount(newStat.getOldprice());
			trx.setMerchantamount(trx.getDepositamount().add(new BigDecimal(trx.getMitra())));;
			trx.setAmount(trx.getDepositamount().subtract(new BigDecimal(trx.getAdmin())));;
		}
		if(newStat.getOldstatus()!=null)trx.setType(newStat.getOldstatus());
		
		try {
			transDao.updateTransactionSession(trx);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}
		
	}

	private String sendDepositChange(BigDecimal price, Terminal term, Merchant merch, String mode){
		String result="";
		SaldoLog saldo = null;
		try {
			saldo = saldoLogDao.cekStatusDeposit(trx.getId().toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogSystem.error(getClass(), e);
		}SaldoTransaction prcDeposit=new SaldoTransaction();
		if(price!=null)prcDeposit.setDiffamount(price);
		int iRc=prcDeposit.processSaldo(mode, merch.getMid(), term.getTid(), saldo.getAmount(), trx.getTracenumber().intValue(), new Long(1), null, trx.getId(), null);

		if(iRc==00){
			result="OK";
		}else if(iRc==05){
			result="transaction error";
			
		}else{
			result="saldo tidak mencukupi";
		}
		
		return result;
	}

}
