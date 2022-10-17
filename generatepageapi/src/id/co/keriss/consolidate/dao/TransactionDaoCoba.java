package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Transaction;
import id.co.keriss.consolidate.ee.TransactionVO;
import id.co.keriss.consolidate.util.ReportUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class TransactionDaoCoba {
	Session session;
	DB db;
	Log log;
	
	public TransactionDaoCoba(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<?> findAll () throws HibernateException {
        return session.createCriteria(Transaction.class).list();
      }
	  
	  
	  
	  
	  /**
	  public Paging findAll (String env, int start, int count) throws HibernateException {
		  Query query = session.createQuery("from Transaction as t where "+env);
		  return new Paging(query, start, count);
	  }
	  */
	  
	  public List<TransactionVO> findAll(String env) throws HibernateException {
	        return session.createQuery("from Transaction as t where "+env).list();
	  }
	  
	  
	  public Transaction findById(Long id) throws HibernateException {
		    return (Transaction)session.load(Transaction.class, id);
	  }
	  
	  public Long amountCardStore(String from,String to, String id) throws HibernateException {
		  System.out.println("at amountCard Store From, To : "+from+","+to);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Store store = new StoreDao(db).findById(Long.parseLong(id));
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime between :fromDate and :toDate and "+
				  		"t.type='card' and " +
		  				"t.batch=b and " +
		  				"b.terminal=tm and " +
		  				"tm.merchant=m and " +
		  				"m.store.id="+store.getId());
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  System.out.println("Amount of CardStore unavalible");
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Double percentCardStore(String from,String to, String id){
		  Double percent=new Double(0);
		  percent = ((double)amountCardStore(from, to, id) / (double)totalStore(from, to, id))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return ((Math.round(percent)/100d))*100;
	  }
	  
	  public Double percentCashStore(String from,String to, String id){
		  Double percent=new Double(0);
		  percent = ((double)amountCashStore(from, to, id) / (double)totalStore(from, to, id))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Double percentVoucherStore(String from,String to, String id){
		  Double percent=new Double(0);
		  percent = ((double)amountVoucherStore(from, to, id) / (double)totalStore(from, to, id))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Double percentMoreStore(String from,String to, String id){
		  Double percent=new Double(0);
		  percent = ((double)amountMoreStore(from, to, id) / (double)totalStore(from, to, id))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Double percentCard(String from,String to){
		  Double percent=new Double(0);
		  percent = ((double)totalCard(from, to) / (double)grandTotal(from, to))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Double percentCash(String from,String to){
		  Double percent=new Double(0);
		  percent = ((double)totalCash(from, to) / (double)grandTotal(from, to))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Double percentVoucher(String from,String to){
		  Double percent=new Double(0);
		  percent = ((double)totalVoucher(from, to) / (double)grandTotal(from, to))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Double percentMore(String from,String to){
		  Double percent=new Double(0);
		  percent = ((double)totalMore(from, to) / (double)grandTotal(from, to))*100;
		  if(percent.isNaN()){
			  percent=new Double(0);
		  }
		  return (Math.round(percent)/100d)*100;
	  }
	  
	  public Long amountCashStore(String from,String to, String id) throws HibernateException {
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Store store = new StoreDao(db).findById(Long.parseLong(id));
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='cash' and " +
		  				"t.batch=b and " +
		  				"b.terminal=tm and " +
		  				"tm.merchant=m and " +
		  				"m.store.id="+store.getId());
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long amountVoucherStore(String from,String to, String id) throws HibernateException {
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Store store = new StoreDao(db).findById(Long.parseLong(id));
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='voucher' and " +
		  				"t.batch=b and " +
		  				"b.terminal=tm and " +
		  				"tm.merchant=m and " +
		  				"m.store.id="+store.getId());
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long amountMoreStore(String from,String to, String id) throws HibernateException {
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Store store = new StoreDao(db).findById(Long.parseLong(id));
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='more' and " +
		  				"t.batch=b and " +
		  				"b.terminal=tm and " +
		  				"tm.merchant=m and " +
		  				"m.store.id="+store.getId());
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long totalCard(String from,String to) throws HibernateException{
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='card'");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long totalCash(String from,String to) throws HibernateException{
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='cash'");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long totalVoucher(String from,String to) throws HibernateException{
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='voucher'");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long totalMore(String from,String to) throws HibernateException{
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
		  				"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
		  				"t.type='more'");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long totalStore(String from,String to, String id) throws HibernateException{
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Store store = new StoreDao(db).findById(Long.parseLong(id));
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
			  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate and " +
	  				"t.batch=b and " +
	  				"b.terminal=tm and " +
	  				"tm.merchant=m and " +
	  				"m.store.id="+store.getId());
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  System.out.println("No total Store Avalible");
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public Long grandTotal(String from,String to) throws HibernateException{
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query query = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
			  		"where t.approvaltime <= :toDate and t.approvaltime >= :fromDate");
		  query.setParameter("fromDate", fromDate);
		  query.setParameter("toDate", toDate);
		  Long amount  = (Long) query.uniqueResult();
		  if(amount==null){
			  amount=new Long(0);
	      }
		  return amount;
	  }
	  
	  public  void deleteTransaction(Transaction transLog) {
		try {
	      session.delete(transLog);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createTransaction(Transaction trans) {
	    try {
	      session.save(trans);
	      System.out.println("Transaction Saved");
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  public  void createTransactionui(Transaction trans) {
		    try {
		      session.save(trans);
		      session.beginTransaction().commit();
		    } catch (RuntimeException e) {
		    	session.beginTransaction().rollback();
		    	log.debug(e);
		    }
		  }

	  public  void updateTransaction(Transaction trans) {
	    try {
	      session.update(trans);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public List<Transaction> matching(String fromDate, String toDate, String store,String ds1, String ds2){
		  List match = null;
		  String dsmatch="csa";
		  Query q1 = session.createQuery("select count(*) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
		  		"where t.approvaltime <= :fromDate and " +
		  		":toDate <= t.approvaltime and " +
		  		"t.batch=b and " +
		  		"b.terminal=tm and " +
		  		"tm.merchant=m and " +
		  		"m.store.id="+store+" and " +
		  		"t.source='"+ds1+"'");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateStart(toDate));
		  Query q2 = session.createQuery("select count(*) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
			  		"where t.approvaltime <= :fromDate and " +
			  		":toDate <= t.approvaltime and " +
			  		"t.batch=b and " +
			  		"b.terminal=tm and " +
			  		"tm.merchant=m and " +
			  		"m.store.id='"+store+"' and " +
			  		"t.source='"+ds2+"'");
			  q2.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
			  q2.setParameter("toDate", ReportUtil.getInstance().dateStart(toDate));
		  Long ds1lenght = (Long) q1.uniqueResult(); 
		  Long ds2lenght = (Long) q2.uniqueResult();
		  if(ds1lenght >= ds2lenght){
			  dsmatch=ds2;
		  }
		  q1 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
			  		"where t.approvaltime <= :fromDate and " +
			  		":toDate <= t.approvaltime and " +
			  		"t.batch=b and " +
			  		"b.terminal=tm and " +
			  		"tm.merchant=m and " +
			  		"m.store.id='"+store+"' and " +
			  		"t.source='"+dsmatch+"'");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateStart(toDate));
		  match = q1.list();
		  return match;
	  }
	  
	  public List unmatch(String fromDate, String toDate, String store,String ds1, String ds2){
		  List unmatch=null;
		  String dsmatch="csa";
		  Query q1 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
			  		"where t.approvaltime <= :fromDate and " +
			  		":toDate <= t.approvaltime and " +
			  		"t.batch=b and " +
			  		"b.terminal=tm and " +
			  		"tm.merchant=m and " +
			  		"m.store.id="+store+" and " +
			  		"t.source='"+ds1+"'");
			  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
			  q1.setParameter("toDate", ReportUtil.getInstance().dateStart(toDate));
			  Query q2 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
				  		"where t.approvaltime <= :fromDate and " +
				  		":toDate <= t.approvaltime and " +
				  		"t.batch=b and " +
				  		"b.terminal=tm and " +
				  		"tm.merchant=m and " +
				  		"m.store.id='"+store+"' and " +
				  		"t.source='"+ds2+"'");
				  q2.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
				  q2.setParameter("toDate", ReportUtil.getInstance().dateStart(toDate));
			  List<Transaction> ds1l = (List) q1.list(); 
			  List<Transaction> ds2l = (List) q2.list();
			  if(ds1l.size()<ds2l.size()){
				  unmatch=ds1l;
				  ds1l = ds2l;
				  ds2l=unmatch;
			  }
			  unmatch.clear();
			  for(Transaction t :ds1l){
				  for(Transaction tran : ds1l){
					  if(tran.getApprovalcode()!=t.getApprovalcode() || tran.getApprovaltime()!=t.getApprovaltime() || tran.getCardno() != t.getCardno()){
						 unmatch.add(tran); 
					  }
				  }
			  }
		  return unmatch;
	  }

	  public Long totalStoreSettle(Boolean settle, String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  System.out.println("Date within totalStoreSettle :"+fromDate+", "+toDate);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
		  		"where t.batch=b and " +
		  		"b.terminal=tm and " +
		  		"t.approvaltime between :fromDate and :toDate and " +
		  		"tm.merchant=m and " +
		  		"t.batch.settled=:settle and " +
		  		"m.store.id="+store);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  q.setParameter("settle", settle);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalStoreUnsettle(Boolean settle, String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
		  		"where t.batch=b and " +
		  		"b.terminal=tm and " +
		  		"t.approvaltime <= :fromDate and :toDate <= t.approvaltime and " +
		  		"tm.merchant=m and " +
		  		"t.batch.settled=:settle and" +
		  		"m.store.id="+store);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  q.setParameter("settle", settle);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalVisaStore(String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(trans.amount) from Transaction trans, Product p, CardType c " +
		  		"where trans.product=p and " +
		  		"p.cardtype=c and " +
		  		"trans.approvaltime <= :toDate and " +
		  		":fromDate <= trans.approvaltime and "+
		  		"c.name.id=1");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalMasterStore(String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Product p, CardType c " +
		  		"where t.product=p and " +
		  		"p.cardtype=c and " +
		  		"t.approvaltime <= :fromDate and " +
		  		":toDate <= t.approvaltime and " +
		  		"c.name.id=2");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalSaleStore(String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Product p, CardType c " +
		  		"where t.approvaltime <= :fromDate and " +
		  		":toDate <= t.approvaltime and " +
		  		"t.transactiontype='sale'");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalVoidStore(String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Product p, CardType c " +
		  		"where t.approvaltime <= :fromDate and " +
		  		":toDate <= t.approvaltime and " +
		  		"t.transactiontype='void'");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalRefundStore(String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Product p, CardType c " +
		  		"where t.approvaltime <= :fromDate and :toDate <= t.approvaltime and " +
		  		"t.transactiontype='refund'");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public Long totalAmexStore(String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Product p, CardType c " +
		  		"where t.product=p and " +
		  		"p.cardtype=c and " +
		  		"t.approvaltime <= :fromDate and " +
		  		":toDate <= t.approvaltime and " +
		  		"c.name.id=3");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  return total;
	  }
	  
	  public List findByDate(String bank, String store, String from, String to, String batch){
		  List trans = null;
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf;
		  sqf=", Batch b, Terminal tm, Merchant m, Store s";
		  bq="t.bank.id="+ bank +" and ";
		  sq="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  batchq="and t.batch="+batch;
		  if(bank.equalsIgnoreCase("0"))bq="";
		  if(batch.equalsIgnoreCase("0"))batchq=""; 
		  if(store.equalsIgnoreCase("0"))
		  {
			  sq="";
			  sqf="";
		  }
		  Query q = session.createQuery("select t from Transaction t "+sqf+" where " +
				bq +
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime " +
		  		sq+batchq);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  trans=q.list();
		  return trans;
	  }
	  
	  public Date dateStart(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  return dateVal;
	  }
	  
	  public Date dateEnd(String date){
		  Date dateVal = null;
		  
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			dateVal=cal.getTime();
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  return dateVal;
	  }
	  
	  public Long totalTrxBank(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select count(*) from Transaction t where t.onus=true " +
		  		"and t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
		  q = session.createQuery("select count(*) from Transaction t where t.onus=false ");
		  Long temp = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
		  val += temp;
	      return val;} 
	  
	  public Long totalGrsBank(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  val = onusGrsBank(ds, bank, from, to)+offusGrsBank(ds, bank, from, to);
		  if(val==null){
			  val=new Long(0);
		  }
	      return val;} 
	  
	  public Long onusTrxBank(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select count(*) from Transaction t where t.onus=true " +
				  "and t.approvaltime <= :toDate and " +
	  			  ":fromDate <= t.approvaltime and " +
	  			  "t.bank.id="+bank);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
	      return val;} 
	  
	  public Long onusGrsBank(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t where t.onus=true " +
				  "and t.approvaltime <= :toDate and " +
	  		":fromDate <= t.approvaltime and " +
	  		"t.bank.id="+bank);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
	      return val;} 
	  
	  public Long offusTrxBank(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select count(*) from Transaction t where t.onus=false " +
				  "and t.approvaltime <= :toDate and " +
	  		":fromDate <= t.approvaltime and " +
	  		"t.bank.id="+bank);
				  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
	      return val;} 
	  
	  public Long offusGrsBank(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select count(*) from Transaction t where t.onus=false " +
				  "and t.approvaltime <= :toDate and " +
	  		":fromDate <= t.approvaltime and " +
	  		"t.bank.id="+bank);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
	      return val;} 
		
	  /*public Long totalPerBankMdr(String ds, String bank, String from, String to){
		  Long val = new Long(0);
		  Query q = session.createQuery("select b. from Transaction t where t.bank" +
				  "t.bank.id="+bank);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }
	      return val;}
*/	  
	  public Double totalNetBank(String ds, String bank, String from, String to){
		  Double val = new Double(0);
		  /*Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select count(*) from Transaction t where t.onus=false" +
				  "and t.approvaltime <= :toDate and " +
	  		":fromDate <= t.approvaltime and " +
	  		"t.bank.id="+bank);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  val = (Long)q.uniqueResult();
		  if(val==null){
			  val=new Long(0);
		  }*/
		  val = onusNetBank(ds, bank, from, to)+offusNetBank(ds, bank, from, to); 
	      return val;} 
		
	  public Double onusPerBank(String ds, String bank, String from, String to){
		  Double val = new Double(0);
		  Query q = session.createQuery("select b.onus from Bank b where b.id="+bank);
		  val = (Double)q.uniqueResult();
		  if(val==null){
			  val=new Double(0);
		  }
	      return val;}
	  	
	  public Double onusNetBank(String ds, String bank, String from, String to){
		  Double val = new Double(0);
		  val = onusGrsBank(ds, bank, from, to)+onusGrsBank(ds, bank, from, to)*onusPerBank(ds, bank, from, to);
	      return val;} 
	  
	  public Double offusPerBank(String ds, String bank, String from, String to){
		  Double val = new Double(0);
		  Query q = session.createQuery("select b.offus from Bank b where b.id="+bank);
		  val = (Double)q.uniqueResult();
		  if(val==null){
			  val=new Double(0);
		  }
	      return val;}
	  
	  public Double offusNetBank(String ds, String bank, String from, String to){
		  Double val = new Double(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select b.offus from Bank b where b.id="+bank);
		  val = (Double)q.uniqueResult();
		  if(val==null){
			  val=new Double(0);
		  }
	      return val;}
}
