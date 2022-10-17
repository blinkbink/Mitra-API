package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.SaldoLog;
import id.co.keriss.consolidate.util.DBConnection;

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
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.action.ContextConstants;
import org.jpos.util.Log;

public class SaldoLogDao implements ContextConstants{
	Session session;
	DB db;
	Log log;
	
	public SaldoLogDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	
	public SaldoLog cekStatusDeposit(String transaction) throws SQLException {
		  SaldoLog st = null;
		  java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
		    try {
		    	String sql="";
    		   sql="Select id,status,amount from saldolog where transaction=? order by datetime desc limit 1";
	    	   statement=conn.prepareStatement(sql);
		       statement.setString(1, transaction);
		    	ResultSet rs=statement.executeQuery();
		    	
		    	while(rs.next()){
		    		st=new SaldoLog();
		    		st.setId(rs.getLong(1));
		    		st.setStatus(rs.getString(2));
		    		st.setAmount(rs.getBigDecimal(3));
		    	}
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		   return st;
	  
	}
	
	
	public SaldoLog cekStatusDeposit(Long merchant,String transaction, String tid, int stn, BigDecimal amount) throws SQLException {
		  SaldoLog st = null;
		  java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
		    try {
		    	String sql="";
  		   sql="Select id,status from saldolog where merchant=? and tid=?  and stn=? and amount=? and transaction=? order by datetime desc limit 1";
	    	   statement=conn.prepareStatement(sql);
		       statement.setLong(1, merchant);
		       statement.setString(2, tid);
		       statement.setInt(3, stn);
		       statement.setBigDecimal(4, amount);
		       statement.setString(5, transaction);
		    	ResultSet rs=statement.executeQuery();
		    	
		    	while(rs.next()){
		    		st=new SaldoLog();
		    		st.setId(rs.getLong(1));
		    		st.setStatus(rs.getString(2));
		    	}
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		   return st;
	  
	}
	
	public boolean deleteHoldDeposit(Long idSaldo) throws SQLException {
		 boolean st=false;
		  String sql="DELETE FROM saldolog where id=?";
        java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setLong(1, idSaldo);
			 
		      	st=statement.execute();
		      	
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		   return st;
	  
	}
	
	
	public Long insertDeposit(Long merchant, String tid, BigDecimal amount, String status, int stn, String refnum, String ket, Long userid, String transaction, BigDecimal saldo) throws SQLException {
		  Long no=null;
		  String sql="INSERT INTO saldolog( "
		  		+ "merchant, tid, amount, status, stn, refnum, ket, userid, transaction, curr_balance) "
		  		+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
          java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Statement st=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setLong(1, merchant);
		    	statement.setString(2, tid);
		    	statement.setBigDecimal(3, amount);
		    	statement.setString(4, status);
		    	statement.setInt(5, stn);
		    	statement.setString(6, refnum);
		    	statement.setString(7, ket);
		    	statement.setLong(8, userid);
		    	statement.setString(9, transaction);
		    	if(saldo!=null)statement.setBigDecimal(10, saldo);
		    	else statement.setObject(10, null);

		      	if(statement.executeUpdate()>0){
			    	st=conn.createStatement();
			      	ResultSet rs=st.executeQuery("SELECT last_value from saldolog_sequence");
					 while(rs.next()){
						 no=rs.getLong(1);
						 System.out.println("data-in:"+no);
					 }
		      	}
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(st!=null)st.close();
				if(conn!=null)conn.close();
			}
		   return no;
	  
	}
	
	public boolean updateDeposit(Long idsaldo, String status, String refnum, String ket, Long userid, String transaction, BigDecimal saldo, BigDecimal amount) throws SQLException {
		  boolean st=false;
		  String sql="UPDATE saldolog "
		  		+ "SET datetime=now(), "
		  		+ "status=?, refnum=?, ket=?, userid=?, transaction=?, "
		  		+ "curr_balance=? ,amount=? WHERE id=?";
		  java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setString(1, status);
		    	statement.setString(2, refnum);
		    	statement.setString(3, ket);
		    	statement.setLong(4, userid);
		    	statement.setString(5, transaction);
		    	if(saldo!=null)statement.setBigDecimal(6, saldo);
		    	else statement.setObject(6, null);

		    	if(amount!=null)statement.setBigDecimal(7, amount);
		    	else statement.setObject(7, null);

		    	statement.setLong(8, idsaldo);

		      	st=statement.executeUpdate()>0?true:false;
		      	
		      
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		   return st;
	  
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

	  
	  public Long LastPage(String bank,  String from, String to, String merchant, int start, int count,String typetrx){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  String bq="";
		  String fr="";
		  if(!bank.equalsIgnoreCase("0")){
			  fr=", Bank b ";
			  bq+="AND b.id=m.bank AND b.id='"+bank+"' ";
		  }
		  if(!merchant.equalsIgnoreCase("0")){
			  bq+="AND m.id = '"+merchant+"' ";
		  }
		  if(!typetrx.equalsIgnoreCase("")){
			  bq+="AND status = '"+typetrx+"' ";
		  }
		  Query q = session.createQuery("select count(s) from SaldoLog s, Merchant m "+fr+" where s.merchant=m.id " +
				bq +
		  		"and s.datetime <= :toDate and " +
		  		":fromDate <= s.datetime ");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % (count-1);
		  totalpage = total/(count-1);
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  
	public Paging findByDate(String bank,  String from, String to, String merchant, int start, int count,String typetrx){
		  Date fromDate = dateStart(from);
		  Date toDate = dateEnd(to);
		  String bq="";
		  String fr="";
		  if(!bank.equalsIgnoreCase("0")){
			  fr=", Bank b ";
			  bq+="AND b.id=m.bank AND b.id='"+bank+"' ";
		  }
		  if(!merchant.equalsIgnoreCase("0")){
			  bq+="AND m.id = '"+merchant+"' ";
		  }
		  if(!typetrx.equals("")){
			  bq+="AND status = '"+typetrx+"' ";
		  }
		  Query q = session.createQuery("select s from SaldoLog s, Merchant m "+fr+" where s.merchant=m.id " +
				bq +
		  		"and s.datetime <= :toDate and " +
		  		":fromDate <= s.datetime " +
		  		" ORDER BY datetime DESC");
		  q.setParameter("fromDate", fromDate);
		  q.setParameter("toDate", toDate);
		  System.out.println(q);
		  return new Paging(q, start, count);
	  }
	  
}
