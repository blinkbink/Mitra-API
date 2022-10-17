package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Batch;
import id.co.keriss.consolidate.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
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
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class BatchDao {
	Session session;
	DB db;
	Log log;
	
	public BatchDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Batch.class).list();
      }
	  
	  public Batch findById(Long id) throws HibernateException {
		    return (Batch)session.load(Batch.class, id);
	  }
	
	  public  void deleteTransaction(Batch batch) {
		try {
	      session.delete(batch);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createbatch(Batch batch) {
	    try {
		      session.save(batch);
		      System.out.println("Batch Saved");
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  public Long createbatchJDBC(Batch batch) throws SQLException {
		   Connection conn=DBConnection.getConnection();
		   PreparedStatement statement=null;
		   String sql="INSERT INTO batch( "
		   		+ "\"number\", amount, settled, settledate, terminal) "
		   		+ "VALUES ( ?, ?, ?, ?, ?)";
		   Long no=null;
		   Statement st=null;
		    try {
		    	  statement=conn.prepareStatement(sql);
		    	  statement.setInt(1, batch.getNumber());
		    	  statement.setLong(2, batch.getAmount());
		    	  statement.setBoolean(3, batch.isSettled());
		    	  statement.setDate(4, new java.sql.Date(batch.getSettledate().getTime()));
		    	  statement.setLong(5, batch.getTerminal().getId());	

		    	  if(statement.executeUpdate()>0){
		    		  st=conn.createStatement();
			 		  ResultSet rs=st.executeQuery("SELECT last_value from batch_sequence");
			 		  while(rs.next()){
			 			  no=rs.getLong(1);
			 			  System.out.println("dataaaaaaa: "+no);
			 		  }
		 			  System.out.println("errorrrrrrrrrrrrrrr1111");

		    	  }
	 			  System.out.println("okkkkkkkkkkkkkkkkk");

		    	  
		    	  
		    } catch (RuntimeException | SQLException e) {
		    	log.debug(e);
		    }finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
				if(st!=null)st.close();
			}
		    return no;
		  }

	  public  void updatebatch(Batch batch) {
	    try {
	        session.update(batch);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public int maxBatch(String tid, String mid){
		  Integer batch;
		  batch = (Integer)session.createQuery("select max(b.number) from Batch b, Terminal t, Merchant m " +
		  			"where b.terminal=t and " +
		  			"t.merchant=m and " +
		  			"t.tid = '"+tid+"' and " +
		  			"m.mid = '"+mid+"'").uniqueResult();
		  if(batch == null) batch = new Integer("0");
		  return batch;
	  }
	  
	  public Batch findByMax(String tid, String mid, int max){
		 Batch batch = null;
		 batch = (Batch)session.createQuery("select b from Batch b, Terminal t, Merchant m " +
		  			"where b.terminal=t and " +
		  			"t.merchant=m and " +
		  			"t.tid = '"+tid+"' and " +
		  			"m.mid = '"+mid+"' and " +
		  			"b.number="+max+"").uniqueResult();
		  return batch;
	  }
	  
	  public Batch findUnsettle(String tid, String mid){
		 
		  Batch batch = null;
		try{
		  System.out.println("mid: "+mid);
		  batch = (Batch)session.createQuery("select b from Batch b, Terminal t, Merchant m " +
		  			"where b.terminal=t and " +
		  			"t.merchant=m and " +
		  			"t.tid = '"+tid+"' and " +
		  			"m.mid = '"+mid+"' and " +
		  			"b.settled=false order by b.settledate DESC limit 1").list().get(0);
		}
		catch (Exception e) {
			batch=null;
		}
		  return batch;
	  }
	  
	  public Paging findByDate1(String bank, String store, String from, String to, String Settle,int start, int count){
		  //List trans = null;
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf;
		  sqf=" , Bank ba, Terminal tm, Merchant m, Store s";
		  //bq="t.bank.id="+ bank +" and ";
		  //sq="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  //bq="b.id=tra.batch and tra.bank.id="+ bank +" and ";
		  //sq="b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  //bq=" and b.id=tra.batch and tra.bank.id="+ bank;
		  sq=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  //batchq="and t.batch="+Settle;
		  //batchq="and b.settled="+Settle;
		  if(bank.equalsIgnoreCase("0"))bq="";
		  //if(Settle.equalsIgnoreCase("0"))batchq=""; 
		  if(store.equalsIgnoreCase("0"))
		  {
			  sq="";
			  //sqf="";
		  }
		  
		  Query q = session.createQuery("select b from Batch b "+sqf+" where b.settled="+Settle 
				  //+ bq 
				  +" and b.settledate <= :toDate and " +
			  	  ":fromDate <= b.settledate "); // +
			  		//sq
			  		//);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  //trans=q.list();
		  return new Paging(q, start, count);
	  }
	  
	  public Paging findByDate2(String bank, String store, String from, String to, String Settle,int start, int count){
		  //List trans = null;
		  System.out.println(from);
		  System.out.println(to);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf,gg,sq2,sq3;
		  
		  //sqf=", Bank ba, Transaction t, Terminal tm, Merchant m, Store s";
		  sqf=", Bank ba, Terminal tm, Merchant m, Store s";
		  //bq="b.id=t.batch and t.bank.id="+ bank +" and ";
		  //bq=" and m.bank.id="+ bank;
		  
		  //sq=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  //gg=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store+ " and m.bank="+ bank;
		  //sq3=" and b.terminal=tm and tm.merchant=m and m.bank="+ bank;
		  sq=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  gg=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store+ " and m.bank=ba and ba.id="+ bank;
		  sq3=" and b.terminal=tm and tm.merchant=m and m.bank=ba and ba.id="+ bank;
		  if(bank.equalsIgnoreCase("0")) {
			  sqf=", Terminal tm, Merchant m, Store s";
			  gg=sq;
		  }
		  if(store.equalsIgnoreCase("0")){
			  gg=sq3;
			  //sqf=", Terminal tm, Merchant m";
			  sqf=", Bank ba, Terminal tm, Merchant m";
		  }
		  if(store.equalsIgnoreCase("0") && bank.equalsIgnoreCase("0")){
			  sqf="";
			  gg="";
		  }
			  
		  Query q = session.createQuery("select b from Batch b "+sqf+" where b.settled="+Settle +
				//bq +
		  		" and b.settledate <= :toDate and " +
		  		":fromDate <= b.settledate " +
		  		gg);
		  
		  System.out.println(q);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  System.out.println(q);
		  return new Paging(q, start, count);
	  }
	  
	  
	  
	  public Long lastPage(String bank, String store, String from, String to, String Settle){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf,gg,sq2,sq3;
		  
		  //sqf=", Bank ba, Transaction t, Terminal tm, Merchant m, Store s";
		  sqf=", Bank ba, Terminal tm, Merchant m, Store s";
		  //bq="b.id=t.batch and t.bank.id="+ bank +" and ";
		  //bq=" and m.bank.id="+ bank;
		  
		  //sq=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  //gg=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store+ " and m.bank="+ bank;
		  //sq3=" and b.terminal=tm and tm.merchant=m and m.bank="+ bank;
		  sq=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  gg=" and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store+ " and m.bank=ba and ba.id="+ bank;
		  sq3=" and b.terminal=tm and tm.merchant=m and m.bank=ba and ba.id="+ bank;
		  if(bank.equalsIgnoreCase("0")) {
			  sqf=", Terminal tm, Merchant m, Store s";
			  gg=sq;
		  }
		  if(store.equalsIgnoreCase("0")){
			  gg=sq3;
			  //sqf=", Terminal tm, Merchant m";
			  sqf=", Bank ba, Terminal tm, Merchant m";
		  }
		  if(store.equalsIgnoreCase("0") && bank.equalsIgnoreCase("0")){
			  sqf="";
			  gg="";
		  }
			  
		  Query q = session.createQuery("select count(b) from Batch b "+sqf+" where b.settled="+Settle +
				//bq +
		  		" and b.settledate <= :toDate and " +
		  		":fromDate <= b.settledate " +
		  		gg);

		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  System.out.println(q);
		  //q.setParameter("toDate", toDate);
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
	  
	  public Paging findByDate3(String bank, String store, String from, String to, String Settle,int start, int count){
		  //List trans = null;
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq,sq,batchq,sqf;
		  sqf="bank ba, Terminal tm, Merchant m, Store s, Transaction tra";
		  //bq="t.bank.id="+ bank +" and ";
		  //sq="and t.batch=b and b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  bq="b.id=tra.batch and tra.bank.id="+ bank +" and ";
		  sq="b.terminal=tm and tm.merchant=m and m.store=s and s.id="+store;
		  //batchq="and t.batch="+Settle;
		  batchq="and b.settled="+Settle;
		  if(bank.equalsIgnoreCase("0"))bq="";
		  if(Settle.equalsIgnoreCase("0"))batchq=""; 
		  if(store.equalsIgnoreCase("0"))
		  {
			  sq="";
			  sqf="";
		  }
		  //Query q = session.createQuery("select b from Batch b "+sqf+" where " +
			//	bq +
		  //		"t.approvaltime <= :toDate and " +
		  //		":fromDate <= t.approvaltime " +
		  //		sq+batchq);
		  Query q = session.createQuery("select b from Batch b "+sqf+" where " +
					bq +
			  		"b.settledate <= :toDate and " +
			  		":fromDate <= b.settledate " +
			  		sq+batchq);
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  //trans=q.list();
		  return new Paging(q, start, count);
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
	  
	  public void settlement(String tid, String mid, Long amount){
		  Batch batch = findUnsettle(tid,mid);
		  batch.setSettled(true);
		  batch.setAmount(amount);
		  Batch newbatch = new Batch();
		  newbatch.setAmount(Long.valueOf(0));
		  newbatch.setNumber(batch.getNumber()+1);
		  newbatch.setSettled(false);
		  newbatch.setTerminal(new TerminalDao(db).findByTidMid(tid, mid));
		  updatebatch(batch);
		  createbatch(newbatch);
	  }
	  
	  public synchronized void updateSN(Long id, BigDecimal amount) {
			// TODO Auto-generated method stub
			  String query="update Batch set amount=amount+"+amount+" where id='"+id+"'";
			  session.createQuery(query).executeUpdate();
	  }
		
}
