package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Nontaglis;
import id.co.keriss.consolidate.ee.Postpaid;
import id.co.keriss.consolidate.ee.Prepaid;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Transaction;
import id.co.keriss.consolidate.ee.TransactionVO;
import id.co.keriss.consolidate.util.DBConnection;
import id.co.keriss.consolidate.util.ReportUtil;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

public class TransactionDao{
	Session session;
	DB db;
	Log log;
	
	public TransactionDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<?> findAll () throws HibernateException {
        return session.createCriteria(Transaction.class).list();
      }
	  
	  public List<TransactionVO> findAll(String env) throws HibernateException {
	        return session.createQuery("from Transaction as t where "+env).list();
	  }
	  
	  /*public Transaction findTrxForReversal(String tid, String mid, Date date, BigDecimal amt, String transtype){
		try{
		  Transaction trx = null;
		  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
			  			"where b.terminal=t and " +
			  			"t.merchant=m and " +
			  			"t.tid = '"+tid+"' and " +
			  			"m.mid = '"+mid+"' and " +
			  			"tr.batch=b.id and b.settled=false and " +
			  			"tr.transactiontype='"+transtype+"' and " +
			  			"tr.amount="+amt+" and tr.approvaltime="+date).uniqueResult();
		  
		  return trx;
		}
		catch(Exception e){
			return null;
		}
	  }	
	  
	  public Transaction findTrxForAdvice(String tid, String mid, BigDecimal amt, String transtype){
			try{
			  Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
				  			"where b.terminal=t and " +
				  			"t.merchant=m and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and b.settled=false and " +
				  			"tr.transactiontype='"+transtype+"' and " +
				  			"tr.amount="+amt).uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				return null;
			}
	}	
	  */
	  
	  public Long lastTrxByTIDJDBC(Long idTerminal) throws SQLException{
		  java.sql.Connection conn=DBConnection.getConnection();
		  Statement statement=null;
		  Long no=(long) 0;
		try {
   		     statement=conn.createStatement();

			 ResultSet rs=statement.executeQuery("select tracenumber from Transaction tr, Batch b, Terminal t where batch=b.id and terminal=t.id and t.id='"+idTerminal+"' order by tr.id desc limit 1");
			 while(rs.next()){
				 no=rs.getLong(1);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(statement!=null)statement.close();
			if(conn!=null)conn.close();
		}
		return no;
	  }
	  
	  public Transaction lastTrxByTID(Long idTerminal){
			try{
				Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Transaction tr, Batch b, Terminal t where batch=b.id and terminal=t.id and t.id='"+idTerminal+"' order by tr.id desc limit 1").list().get(0);
			  
			  return trx;
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
	}	
	  
	 
	  
	  public Transaction lastTrxByID(Long id){
			Transaction trx = null;
			try{
			  trx = (Transaction)session.createQuery("select tr from Transaction tr where tr.id='"+id+"' order by tr.id desc limit 1").list().get(0);
			  
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
			return trx;

	}	
	  
	  public Transaction findTrxAllBatch(String tid, String mid, String reqid){
			try{
				Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
				  			"where b.terminal=t.id and " +
				  			"t.merchant=m.id and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and " +
				  			"tr.reqid='"+reqid+"' order by tr.id desc limit 1").setMaxResults(1).uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
	}	
	  public Transaction findTrx(String tid, String mid, String reqid){
			try{
				Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
				  			"where b.terminal=t.id and " +
				  			"t.merchant=m.id and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and b.settled=false and " +
				  			"tr.reqid='"+reqid+"' order by tr.id desc limit 1").setMaxResults(1).uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
	}
	  
	  public Transaction findTrxWithSTN(Long terminalid, Long stn){
			try{
				Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t,  Transaction tr " +
				  			"where b.terminal=t.id and " +
				  			"t.id = '"+terminalid+"' and " +
				  			"tr.batch=b.id  and " +
				  			"tr.tracenumber='"+stn+"' order by tr.id desc limit 1").setMaxResults(1).uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
	}
	  public Transaction findTrxForReversal(String tid, String mid, Date date, BigDecimal merchantAmt, String transtype){
		try{
		  Transaction trx = null;
		  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
			  			"where b.terminal=t and " +
			  			"t.merchant=m and " +
			  			"t.tid = '"+tid+"' and " +
			  			"m.mid = '"+mid+"' and " +
			  			"tr.batch=b.id and b.settled=false and " +
			  			"tr.transactiontype='"+transtype+"' and " +
			  			"tr.merchantamount="+merchantAmt+" " +
			  			"and tr.tracenumber=(select MAX(trx.tracenumber) from Batch bt, Terminal tm, Merchant mc, Transaction trx " +
			  			"where bt.terminal=tm and " +
			  			"tm.merchant=mc and " +
			  			"tm.tid = '"+tid+"' and " +
			  			"mc.mid = '"+mid+"' and " +
			  			"trx.batch=bt.id and bt.settled=false)").setMaxResults(1).uniqueResult();
		  
		  return trx;
		}
		catch(Exception e){
			return null;
		}
	  }	  
	  
	  public Transaction findTrxForAdvice(String tid, String mid, BigDecimal merchantAmt, String transtype, int trace){
			try{
			  Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
				  			"where b.terminal=t and " +
				  			"t.merchant=m and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and b.settled=false and " +
				  			"tr.transactiontype='"+transtype+"' and " +
				  			"tr.merchantamount="+merchantAmt+" and tr.tracenumber='"+trace+"'").setMaxResults(1).uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
	}	
	  
	  public Transaction findTrxForAdvicePPOB(String tid, String mid, String prodCode, String reqid){
			try{
			  Transaction trx = null;
			  trx = (Transaction)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Transaction tr " +
				  			"where b.terminal=t and " +
				  			"t.merchant=m and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and " +
				  			"tr.productcode='"+prodCode+"' and " +
				  			"tr.reqid='"+reqid+"'").setMaxResults(1).uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				log.error(e);
				return null;
			}
	}	
	  
	  public Transaction findById(Long id) throws HibernateException {
	        return (Transaction) session.createQuery("from Transaction where id='"+id+"'").list().get(0);

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
		  Long amount  = (Long) query.setMaxResults(1).uniqueResult();
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
	  
	  public  void createTransactionSession(Transaction trans) {
		  DB db2=new DB();
		  org.hibernate.Transaction txTransaction=null;

		   try {
			  Session ses=db2.open();
			  txTransaction=ses.beginTransaction();
		      ses.save(trans);
		      txTransaction.commit();
		      System.out.println("Transaction Saved");
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    	if(txTransaction!=null)txTransaction.rollback();
		    }finally{
				if(db2!=null)db2.close();
			}
		  }
	  
	  public Long getLastId() throws SQLException{
		  java.sql.Connection conn=DBConnection.getConnection();
		  Statement statement=conn.createStatement();
		  Long no=null;
		try {
			
			 ResultSet rs=statement.executeQuery("SELECT last_value from transaction_sequence");
			 while(rs.next()){
				 no=rs.getLong(1);
				 System.out.println("data-in:"+no);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(statement!=null)statement.close();
			if(conn!=null)conn.close();
		}
		return no;
	  }
	  
	  
	  public boolean getTransactionPulsaToday(int dup, Transaction t , String nohp, String produk ){

		  int no=0;
		  boolean res=false;
			 Date date=new Date();
			 date.setHours(0);
			 date.setMinutes(0);
			 date.setSeconds(0);
			 
			 List<Transaction> listtrx = null;
			 Query query = session.createQuery("SELECT t from Transaction t where productcode=:code and cardno=:nohp and approvaltime >= :time and (type='SUCCESS' or type='PENDING' or type='TIMEOUT' ) order by id asc");
			  query.setParameter("code", produk);
			  query.setParameter("nohp", nohp);
			  query.setParameter("time", date);
			  
			  listtrx =query.list();
			  
			  for (Transaction transaction : listtrx) {
				 if(no==dup){
					 t=transaction;
					 break;
				 }
				 no++;
			  }
			  if(no==dup){
				  res=true;
			  }
	
		return res;
	  }
	  
	  
	  public void createTransactionJDBC(Transaction trans) throws SQLException {
	  String sql="INSERT INTO transaction(id,productcode,reqid,iqpay,approvaltime,tracenumber,batch,Amount,product,productname,productsuppcode,supplier)"+
			  	" values (DEFAULT,?,?,?,?,?,?,?,?,?,?,?)";
      java.sql.Connection conn=DBConnection.getConnection();
      PreparedStatement statement=null;
      Long id=null;
	    try {
	    	statement=conn.prepareStatement(sql);

	    	statement.setString(1, trans.getProductcode());
	      	statement.setString(2, trans.getReqid());
	      	statement.setString(3, trans.getIqpay());
	      	statement.setTimestamp(4, new java.sql.Timestamp(trans.getApprovaltime().getTime()));
	      	statement.setLong(5, trans.getTracenumber());
	      	statement.setLong(6, trans.getBatch().getId());
	      	statement.setBigDecimal(7, trans.getAmount());
	      	if(trans.getProduct()!=null)statement.setLong(8, trans.getProduct().getId());
	      	else statement.setObject(8, null);
	      	statement.setString(9, trans.getProductname());
	      	statement.setString(10, trans.getProductsuppcode());
	      	statement.setLong(11, trans.getSupplier().getId());
	      	System.out.println("sql :" +statement.toString());

	      	statement.execute();
	      	
	      	
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
	    	log.debug(e);
		}finally{
			if(statement!=null)statement.close();
			if(conn!=null)conn.close();
		}
	    
	  }
	  
	  public  void createTransaction(Postpaid trans) {
		    try {
		      session.save(trans);
		      System.out.println("Transaction Saved");
		    } catch (RuntimeException e) {
		    	log.debug(e);
	    }
	  }
	  
	  public  void createTransaction(Prepaid trans) {
		    try {
		      session.save(trans);
		      System.out.println("Transaction Saved");
		    } catch (RuntimeException e) {
		    	log.debug(e);
	    }
	  }
	  
	  public  void createTransaction(Nontaglis trans) {
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
	  
	
	  
	  public  void updateTransactionSession(Transaction trans) throws SQLException {
		  String sql="UPDATE transaction SET amount=?, cardno=?, "
		  		+ "approvaltime=?, approvalcode=?, source=?, productcode=?, "
		  		+ "type=?, onus=?, bank=?, product=?, "
		  		+ "cassa=?, mcode=?,  iqpay=?, rc=?, sn=?, msg=?, "
		  		+ "admin=?, mitra=?, depositamount=?, merchantamount=? "
		  		+ "WHERE id=?;";


	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);

		      	statement.setBigDecimal(1, trans.getAmount());
		      	statement.setString(2, trans.getCardno());
		      	statement.setTimestamp(3, new java.sql.Timestamp(trans.getApprovaltime().getTime()));
		      	statement.setString(4, trans.getApprovalcode());
		      	statement.setString(5, trans.getSource());
		      	statement.setString(6, trans.getProductcode());
		      	statement.setString(7, trans.getType());
		      	statement.setBoolean(8, trans.isOnus());
		      	if(trans.getBank()!=null)statement.setLong(9, trans.getBank().getId());
		      	else statement.setObject(9, null);
		      	if(trans.getProduct()!=null)statement.setLong(10, trans.getProduct().getId());
		      	else statement.setObject(10, null);
		      	statement.setObject(11, null);
		      	statement.setString(12, trans.getMcode());
		      	statement.setString(13, trans.getIqpay());
		      	statement.setString(14, trans.getRc());
		      	statement.setString(15, trans.getSn());
		      	statement.setString(16, trans.getMsg());
		      	statement.setInt(17, trans.getAdmin());
		      	statement.setInt(18, trans.getMitra());
		      	statement.setBigDecimal(19, trans.getDepositamount());
		      	statement.setBigDecimal(20, trans.getMerchantamount());
		    	statement.setLong(21, trans.getTransaction().getId());

				System.out.println("sql :" +statement.toString());
				log.debug("sql :" +statement.toString());
		      	statement.execute();
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
	  }
	  public  void updateTransaction2(Transaction trans) {
		  DB db2=new DB();
		  org.hibernate.Transaction txTransaction=null;

		   try {
			  Session ses=db2.open();
			  txTransaction=ses.beginTransaction();
		      ses.save(trans);
		      txTransaction.commit();
		      System.out.println("Transaction Saved");
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    	if(txTransaction!=null)txTransaction.rollback();
		    }finally{
				if(db2!=null)db2.close();
			}
	  }
	  
	  public  void updateTransaction(Postpaid trans) {
		    try {
		      session.update(trans);

		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	  
	  public  void updateTransaction(Prepaid trans) {
		    try {
		      session.update(trans);

		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	  
	  public  void updateTransaction(Nontaglis trans) {
		    try {
		      session.update(trans);

		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	  
	  public Query matching(String fromDate, String toDate, String store,String ds1, String ds2){
		  Query match = null;
		  Query q1 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m where t.source='"+ds1+"'"+
				  		" and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
				  		"t.batch=b and "+
	  					"b.terminal=tm and "+
	  					"tm.merchant=m and "+
				  		"m.store.id="+store+
	  					" and exists ("+
				  		"from Transaction tran, Batch ba, Terminal ter, Merchant mch where tran.approvalcode=t.approvalcode"+ 
				  		" and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
				  		"tran.batch=ba and "+
  						"ba.terminal=ter and "+
  						"ter.merchant=mch and "+
  						"mch.store.id="+store+
				  		" and tran.source='"+ds2+"')");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateEnd(toDate));
		  System.out.println("Match query = "+q1);
		  match = q1;
		  return match;
	  }
	  
	  public List matchingList(String fromDate, String toDate, String store,String ds1, String ds2){
		  List match = null;
		  Query q1 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m where t.source='"+ds1+"'"+
				  		" and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
				  		"t.batch=b and "+
	  					"b.terminal=tm and "+
	  					"tm.merchant=m and "+
				  		"m.store.id="+store+
	  					" and exists ("+
				  		"from Transaction tran, Batch ba, Terminal ter, Merchant mch where tran.approvalcode=t.approvalcode"+ 
				  		" and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
				  		"tran.batch=ba and "+
  						"ba.terminal=ter and "+
  						"ter.merchant=mch and "+
  						"mch.store.id="+store+
				  		" and tran.source='"+ds2+"')");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateEnd(toDate));
		  System.out.println("Match query = "+q1);
		  match = q1.list();
		  return match;
	  }
	  
	  public Long summatch(String fromDate, String toDate, String store,String ds1, String ds2){
		  Long summatch = new Long(0);
		  Query q1 = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m where t.source='"+ds1+"'"+
			  		" and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"t.batch=b and "+
					"b.terminal=tm and "+
					"tm.merchant=m and "+
			  		"m.store.id="+store+
					" and exists ("+
			  		"from Transaction tran, Batch ba, Terminal ter, Merchant mch where tran.approvalcode=t.approvalcode"+ 
			  		" and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"tran.batch=ba and "+
					"ba.terminal=ter and "+
					"ter.merchant=mch and "+
					"mch.store.id="+store+
			  		" and tran.source='"+ds2+"')");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateEnd(toDate));
		  summatch = (Long) q1.uniqueResult();
		  if(summatch==null)summatch=new Long(0);
		  return summatch;
	  }
	  
	  public Query unmatch(String fromDate, String toDate, String store,String ds1, String ds2){
		  Query unmatch = null;
		  Query q1 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m where t.source='"+ds1+"'"+
			  		"and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"t.batch=b and "+
  					"b.terminal=tm and "+
  					"tm.merchant=m and "+
			  		"m.store.id="+store+
  					" and not exists ("+
			  		"from Transaction tran, Batch ba, Terminal ter, Merchant mch where tran.approvalcode=t.approvalcode "+ 
			  		"and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"tran.batch=ba and "+
						"ba.terminal=ter and "+
						"ter.merchant=mch and "+
						"mch.store.id="+store+
						" and tran.source='"+ds2+"')");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateEnd(toDate));
		  unmatch = q1;
		  return unmatch;
	  }
	  
	  public List unmatchList(String fromDate, String toDate, String store,String ds1, String ds2){
		  List unmatch = null;
		  Query q1 = session.createQuery("select t from Transaction t, Batch b, Terminal tm, Merchant m where t.source='"+ds1+"'"+
			  		"and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"t.batch=b and "+
  					"b.terminal=tm and "+
  					"tm.merchant=m and "+
			  		"m.store.id="+store+
  					" and not exists ("+
			  		"from Transaction tran, Batch ba, Terminal ter, Merchant mch where tran.approvalcode=t.approvalcode "+ 
			  		"and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"tran.batch=ba and "+
						"ba.terminal=ter and "+
						"ter.merchant=mch and "+
						"mch.store.id="+store+
						" and tran.source='"+ds2+"')");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateEnd(toDate));
		  unmatch = q1.list();
		  return unmatch;
	  }
	  
	  public Long sumunmatch(String fromDate, String toDate, String store,String ds1, String ds2){
		  Long sumunmatch = new Long(0);
		  Query q1 = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m where t.source='"+ds1+"'"+
			  		"and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"t.batch=b and "+
					"b.terminal=tm and "+
					"tm.merchant=m and "+
			  		"m.store.id="+store+
					" and not exists ("+
			  		"from Transaction tran, Batch ba, Terminal ter, Merchant mch where tran.approvalcode=t.approvalcode "+ 
			  		"and t.approvaltime >= :fromDate and t.approvaltime <= :toDate and "+
			  		"tran.batch=ba and "+
					"ba.terminal=ter and "+
					"ter.merchant=mch and "+
					"mch.store.id="+store+
					" and tran.source='"+ds2+"')");
		  q1.setParameter("fromDate", ReportUtil.getInstance().dateStart(fromDate));
		  q1.setParameter("toDate", ReportUtil.getInstance().dateEnd(toDate));
		  if(sumunmatch==null){sumunmatch=new Long(0);System.out.println("Query return null q: "+q1);}else{
			  System.out.println("Unmatchsum query = "+q1+"\n "+sumunmatch);
		  }
		  return sumunmatch;
	  }

	  public Long totalStoreSettle(Boolean settle, String from, String to, String store){
		  Long total = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  System.out.println("Date within totalStoreSettle :"+fromDate+", "+toDate);
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Batch b, Terminal tm, Merchant m, Store s " +
		  		"where t.batch=b and " +
		  		"b.terminal=tm and " +
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
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
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
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
		  Query q = session.createQuery("select sum(t.amount) from Transaction t, Product p, CardType c " +
		  		"where t.product=p and " +
		  		"p.cardtype=c and " +
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
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
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
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
		  Query q = session.createQuery("select sum(t.amount) from Transaction t " +
		  		"where " +
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
		  		"t.transactiontype='sale'");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  
		  System.out.println( "salenya :" +q);
		  
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
		  Query q = session.createQuery("select sum(t.amount) from Transaction t " +
		  		"where " +
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
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
		  Query q = session.createQuery("select sum(t.amount) from Transaction t " +
		  		"where " +
		  		"t.approvaltime <= :toDate and " +
                ":fromDate < t.approvaltime and " +
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
	  
	  public Paging findByDate2(String bank, String store, String from, String to, String batch,int start, int count){
		  //List trans = null;
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
		  //trans=q.list();
		  return new Paging(q, start, count);
	  }
	  
	  public Paging findByDate3(String bank, String store, String from, String to, String batch, String merchant, String terminal, int start, int count, String status,String typetrx ,String subs){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf,sqm,sqfm,sqs,sqft,sqt,sqtm,sqms,sqts,sqall, fr, wh,stat;
		  sqf=", Batch b, Terminal tm, Merchant m, Store s";
		  sqall=", Batch b, Terminal tm, Merchant m, Store s";
		  sqfm=", Batch b, Terminal tm, Merchant m";
		  sqft=", Batch b, Terminal tm";
		  bq="t.bank.id="+ bank +" and ";
		  sq="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqm="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant;// and s.id="+store;
		  sqt="and t.batch=b and b.terminal=tm and tm.id="+terminal; // and m.id="+merchant;
		  sqs="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  sqtm="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant;
		  sqms="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqts="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.store=s and s.id="+store;
		  batchq="and t.batch="+batch;
		  stat="";
		  fr = sqf;
		  wh = sq;
		  
		  if(bank.equalsIgnoreCase("0"))bq="";
		  if(batch.equalsIgnoreCase("0"))batchq=""; 
		  
		  if(store.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqtm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqts;
		  }
		  
		  if(terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqms;
		  }
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqft;
			  wh=sqt;
		  }
		  
		  if(store.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqs;
		  }
		  
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr="";
			  wh="";
		  }
		  
		  if(!typetrx.equals("All")){
			  wh+= " AND transactiontype='"+typetrx+"' ";
		  }
		  

		  if(!subs.isEmpty()){
			  if(typetrx.equals("Postpaid PLN")){
				  wh += " AND t.id IN (select id from Postpaid where subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Prepaid PLN")){
				  wh += " AND t.id IN (select id from Prepaid where no_meter='"+subs+"' OR subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Non tagihan Listrik")){
				  wh += " AND t.id IN (select id from Nontaglis where subs_id='"+subs+"' OR reg_num='"+subs+"') ";

			  }
		  }
		  
		  if(status.equals("SUCCESS")){
			  stat+=" and type='"+status+"' ";
		  }
		  Query q = session.createQuery("select t from Transaction t "+fr+" where " +
				bq +
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime " +
		  		wh+batchq+stat+" ORDER BY approvaltime DESC");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  System.out.println("QUERY :"+q.getQueryString());
		  List<Transaction> tList=q.list();
		  for(Transaction t : tList){
				System.out.println("iDTrace: "+t.getId());

		  }
		  return new Paging(q, start, count);
	  }
	  
	  public List<Transaction> findByDate3all(String bank, String store, String from, String to, String batch, String merchant, String terminal, String status, String typetrx ,String subs){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf,sqm,sqfm,sqs,sqft,sqt,sqtm,sqms,sqts,sqall, fr, wh,stat;
		  sqf=", Batch b, Terminal tm, Merchant m, Store s";
		  sqall=", Batch b, Terminal tm, Merchant m, Store s";
		  sqfm=", Batch b, Terminal tm, Merchant m";
		  sqft=", Batch b, Terminal tm";
		  bq="t.bank.id="+ bank +" and ";
		  sq="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqm="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant;// and s.id="+store;
		  sqt="and t.batch=b and b.terminal=tm and tm.id="+terminal; // and m.id="+merchant;
		  sqs="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  sqtm="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant;
		  sqms="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqts="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.store=s and s.id="+store;
		  batchq="and t.batch="+batch;
		  stat="";
		  fr = sqf;
		  wh = sq;
		  
		  if(bank.equalsIgnoreCase("0"))bq="";
		  if(batch.equalsIgnoreCase("0"))batchq=""; 
		  
		  if(store.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqtm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqts;
		  }
		  
		  if(terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqms;
		  }
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqft;
			  wh=sqt;
		  }
		  
		  if(store.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqs;
		  }
		  
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr="";
			  wh="";
		  }
		  if(status.equals("SUCCESS")){
			  stat+=" and type='"+status+"' ";
		  }
		  
		  if(!typetrx.equals("All")){
			  wh+= " AND transactiontype='"+typetrx+"' ";
		  }
		  
		  if(!subs.isEmpty()){
			  if(typetrx.equals("Postpaid PLN")){
				  wh += " AND t.id IN (select id from Postpaid where subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Prepaid PLN")){
				  wh += " AND t.id IN (select id from Prepaid where no_meter='"+subs+"' OR subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Non tagihan Listrik")){
				  wh += " AND t.id IN (select id from Nontaglis where subs_id='"+subs+"' OR reg_num='"+subs+"') ";

			  }
		  }
		  Query q = session.createQuery("select t from Transaction t "+fr+" where " +
				bq +
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime " +
		  		wh+batchq+stat+"  ORDER BY approvaltime ASC");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  System.out.println(q);
		  try{
			  return q.list();
  
		  }
		  catch (Exception e) {
			  return null;
		  }
	  }
	  
	  public Paging findByCardno(String cardno, String from, String to,int start, int count){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Query q = session.createQuery("select t from Transaction t where " +
				"t.cardno='"+cardno+"' and "+
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime ");
		  System.out.println(q);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  if(q!=null){
			  return new Paging(q, start, count);
		  }else{
			  return null;
		  }
	  }
	  
	  public Long lastPage3(String cardno, String from, String to){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  Query q = session.createQuery("select count(t) from Transaction t ,Card c where " +
				"t.card.cardno='"+cardno+"' and "+
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime ");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  if(q!=null){
			  total = (Long)q.uniqueResult();
		  }
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % 10;
		  totalpage = total/10;
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  
	  public BigDecimal transactionSum(String bank, String store, String from, String to, String batch, String merchant, String terminal, String typetrx ,String subs){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf,sqm,sqfm,sqs,sqft,sqt,sqtm,sqms,sqts,sqall, fr, wh,stat;
		  sqf=", Batch b, Terminal tm, Merchant m, Store s";
		  sqall=", Batch b, Terminal tm, Merchant m, Store s";
		  sqfm=", Batch b, Terminal tm, Merchant m";
		  sqft=", Batch b, Terminal tm";
		  bq="t.bank.id="+ bank +" and ";
		  sq="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqm="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant;// and s.id="+store;
		  sqt="and t.batch=b and b.terminal=tm and tm.id="+terminal; // and m.id="+merchant;
		  sqs="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  sqtm="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant;
		  sqms="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqts="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.store=s and s.id="+store;
		  batchq="and t.batch="+batch;
		  stat="and type='SUCCESS'";
		  
		  fr = sqf;
		  wh = sq;
		  
		  if(bank.equalsIgnoreCase("0"))bq="";
		  if(batch.equalsIgnoreCase("0"))batchq=""; 
		  
		  if(store.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqtm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqts;
		  }
		  
		  if(terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqms;
		  }
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqft;
			  wh=sqt;
		  }
		  
		  if(store.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqs;
		  }
		  
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr="";
			  wh="";
		  }
		  
		  if(!typetrx.equals("All")){
			  wh+= " AND transactiontype='"+typetrx+"' ";
		  }
		  

		  if(!subs.isEmpty()){
			  if(typetrx.equals("Postpaid PLN")){
				  wh += " AND t.id IN (select id from Postpaid where subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Prepaid PLN")){
				  wh += " AND t.id IN (select id from Prepaid where no_meter='"+subs+"' OR subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Non tagihan Listrik")){
				  wh += " AND t.id IN (select id from Nontaglis where subs_id='"+subs+"' OR reg_num='"+subs+"') ";

			  }
		  }
		  Query q = session.createQuery("select sum(t.amount) from Transaction t "+fr+" where " +
				bq +
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime " +
		  		wh+batchq+stat);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  BigDecimal result = (BigDecimal) q.uniqueResult();
		  return result;
	  }
	  public Long lastPage(String bank, String store, String from, String to, String batch){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  
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
		  Query q = session.createQuery("select count(t) from Transaction t "+sqf+" where " +
				bq +
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime " +
		  		sq+batchq);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  //trans=q.list();
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % 10;
		  totalpage = total/10;
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  
	  
	  public Long lastPage2(String bank, String store, String from, String to, String batch, String merchant, String terminal, String status, Long max, String typetrx ,String subs){
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf,sqm,sqfm,sqs,sqft,sqt,sqtm,sqms,sqts,sqall, fr, wh,stat;
		  sqf=", Batch b, Terminal tm, Merchant m, Store s";
		  sqall=", Batch b, Terminal tm, Merchant m, Store s";
		  sqfm=", Batch b, Terminal tm, Merchant m";
		  sqft=", Batch b, Terminal tm";
		  bq="t.bank.id="+ bank +" and ";
		  sq="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqm="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant;// and s.id="+store;
		  sqt="and t.batch=b and b.terminal=tm and tm.id="+terminal; // and m.id="+merchant;
		  sqs="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  sqtm="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.id="+merchant;
		  sqms="and t.batch=b and b.terminal=tm and tm.merchant=m and m.id="+merchant+" and m.store=s and s.id="+store;
		  sqts="and t.batch=b and b.terminal=tm and tm.id="+terminal+" and tm.merchant=m and m.store=s and s.id="+store;
		  batchq="and t.batch="+batch;
		  stat="";
		  
		  fr = sqf;
		  wh = sq;
		  
		  if(bank.equalsIgnoreCase("0"))bq="";
		  if(batch.equalsIgnoreCase("0"))batchq=""; 
		  
		  if(store.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqtm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqts;
		  }
		  
		  if(terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqms;
		  }
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0"))
		  {
			  fr=sqft;
			  wh=sqt;
		  }
		  
		  if(store.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqfm;
			  wh=sqm;
		  }
		  
		  if(merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  {
			  fr=sqf;
			  wh=sqs;
		  }
		  
		  
		  if(store.equalsIgnoreCase("0") && merchant.equalsIgnoreCase("0") && terminal.equalsIgnoreCase("0"))
		  //if(store=="0" && merchant=="0" && terminal=="0")
		  {
			  fr="";
			  wh="";
		  }
		  if(status.equals("SUCCESS")){
			  stat+=" and type='"+status+"' ";
		  }
		  
		  if(!typetrx.equals("All")){
			  wh+= " AND transactiontype='"+typetrx+"' ";
		  }
		  

		  if(!subs.isEmpty()){
			  if(typetrx.equals("Postpaid PLN")){
				  wh += " AND t.id IN (select id from Postpaid where subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Prepaid PLN")){
				  wh += " AND t.id IN (select id from Prepaid where no_meter='"+subs+"' OR subs_id='"+subs+"') ";
			  }
			  if(typetrx.equals("Non tagihan Listrik")){
				  wh += " AND t.id IN (select id from Nontaglis where subs_id='"+subs+"' OR reg_num='"+subs+"') ";

			  }
		  }
		  
		  Query q = session.createQuery("select count(t) from Transaction t "+fr+" where " +
				bq +
		  		"t.approvaltime <= :toDate and " +
		  		":fromDate <= t.approvaltime " +
		  		wh+batchq+stat);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % (max-1);
		  totalpage = total/(max-1);
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
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
