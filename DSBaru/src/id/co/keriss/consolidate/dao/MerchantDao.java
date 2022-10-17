package id.co.keriss.consolidate.dao;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.action.ContextConstants;
import org.jpos.util.Log;

import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.util.DBConnection;

public class MerchantDao implements ContextConstants{
	Session session;
	DB db;
	Log log;
	
	public MerchantDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
	        return session.createQuery("from Merchant where  deleted=false ORDER BY mid ASC").list();
      }
	  
	  public List findAll2 (String store) throws HibernateException {
	        //return session.createCriteria (Terminal.class).list();
		  if(store.equalsIgnoreCase("0")) {
			  return session.createCriteria (Merchant.class).list();
		  } else{
	        return session.createQuery("from Merchant where store ='"+store+"' and deleted=false ORDER BY mid ASC").list();
		  }
	      }
	  
	  public List findAll2 () throws HibernateException {
	        //return session.createCriteria (Terminal.class).list();
		 
	        return session.createQuery("from Merchant where deleted=false ORDER BY mid ASC").list();
		  
	      }
	  
	  public List findAllexcept (String mid) throws HibernateException {
	        //return session.createCriteria (Terminal.class).list();
		  
	        return session.createQuery("select m from Merchant m where deleted=false and mid!='"+mid+"' AND m.id in (select distinct(merchant) from Productbillermerchant) ORDER BY mid ASC").list();
		  
	  }
	  
	  public Merchant findByMid(String mid, String pid) throws HibernateException {
		    try{
		    	return (Merchant) session.createQuery("select m from Merchant m, Partner p where deleted=false and mid='"+mid+"' AND m.partner=p.id AND pid='"+pid+"' ").uniqueResult();
		    }
		    catch (HibernateException e) {
		    	return null;
		    }
	  }
	  
	  public Merchant findByMid(String mid) throws HibernateException {
		    try{
		    	return (Merchant) session.createQuery("select m from Merchant m where mid='"+mid+"' and deleted=false").uniqueResult();
		    }
		    catch (HibernateException e) {
		    	return null;
		    }
	  }
	  
	  public Merchant findByMSISDN(String msisdn) throws HibernateException {
		    try{
		    	return (Merchant) session.createQuery("select m from Merchant m where username='"+msisdn+"' and deleted=false").uniqueResult();
		    }
		    catch (HibernateException e) {
		    	return null;
		    }
	  }
	  
	  
	  public Merchant findById(Long id) throws HibernateException {
		    return session.get(Merchant.class, id);
	  }
	  
	  public  void deleteTransaction(Merchant merchant) {
		try {
	      session.delete(merchant);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  public Paging findAll4 (int start, int count, String st, String m, String name) {
		  //Query query = (Query) session.createCriteria(Store.class);
		  String script = "";
		  
		  if(!m.equals("0")){
			  script = "where m.mid like '%"+m+"%'";
		  }
		  if(!name.equals("0")){
			  script = "where m.name like '%"+name+"%'";
		  }
		  if(!m.equals("0") && !name.equals("0")){
			  script = "where m.mid like '%"+m+"%' and m.name like '%"+name+"%'";
		  }
		  
		  if(script.equals("")){
			  script+=" where deleted=false";
		  }else{
			  script+=" and deleted=false";
		  }
		 
		  Query query = session.createQuery("select m from Merchant m "+script+" order by m.id desc");
		  System.out.println("q :"+query);
		  return new Paging(query, start, count);
		  
	  }
	  
	  public List<Merchant> findAllWithLike (String name) {
		  //Query query = (Query) session.createCriteria(Store.class);
		  String script = "";
		  
		 
			  script = "where deleted=false and (LOWER(m.mid) like '%"+name+"%' or  LOWER(m.name) like '%"+name+"%')";
		  
		  
		 
		  return session.createQuery("select m from Merchant m "+script+" order by m.id desc").list();
		  
	  }
	  
	  public Paging findAll4 (int start, int count, String st, String name) {
		  //Query query = (Query) session.createCriteria(Store.class);
		  String script = "";
		  
		 
		  if(!name.equals("0"))script = "where deleted=false and (LOWER(m.mid) like '%"+name+"%' or  LOWER(m.name) like '%"+name+"%')";
		  else script=" where deleted=false";
		  
		 
		  Query query = session.createQuery("select m from Merchant m "+script+" order by m.id desc");
		  System.out.println("q :"+query);
		  return new Paging(query, start, count);
		  
	  }
	  public Long lastPage(int count, String st, String name){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  String script = "";
		  
		  if(!name.equals("0"))script = "where deleted=false and (LOWER(m.mid) like '%"+name+"%' or LOWER(m.name) like '%"+name+"%')";
		  else script=" where deleted=false";


		  
		  Query q = session.createQuery("select count(*) from Merchant m "+script);
		  
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % count;
		  totalpage = total/count;
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  public Long lastPage(int count, String st, String m, String name){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  String script = "";
		  if(!m.equals("0")){
			  script = "where m.mid like '%"+m+"%'";
		  }
		  if(!name.equals("0")){
			  script = "where m.name like '%"+name+"%'";
		  }
		  if(!m.equals("0") && !name.equals("0")){
			  script = "where m.mid like '%"+m+"%' and m.name like '%"+name+"%'";
		  }

		  if(script.equals("")){
			  script+=" where deleted=false";
		  }else{
			  script+=" and deleted=false";
		  }
		 
		  
		  Query q = session.createQuery("select count(*) from Merchant m "+script);
		  
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % count;
		  totalpage = total/count;
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  public  void createMerchant(Merchant merchant) {
	    try {
		      session.save(merchant);
	      System.out.println("Merchant Saved");
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createMerchantCommit(Merchant merchant) {
		    try {
		    	Transaction tx=session.beginTransaction();
			      session.save(merchant);
			      tx.commit();

		      System.out.println("Merchant Saved");
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }

	  public  void updateMerchant(Merchant merchant) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(merchant);
	      tx.commit();

	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void deleteMerchantCommit(Merchant merchant) {
		    try {
		    	Transaction tx=session.beginTransaction();
		      session.delete(merchant);
		      tx.commit();

		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	public  void updateMerchantNoCommit(Merchant merchant) {
		    try {
		      session.update(merchant);

		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
	}
	
	public  boolean updateMerchantUpdateSaldo(Merchant merchant) throws SQLException {
		  String sql="update merchant set saldo = ? where id=?";
	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
	      boolean st=false;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setBigDecimal(1, merchant.getSaldo());
		      	statement.setLong(2, merchant.getId());
			 
		      	System.out.print("QUERRYY: "+statement.toString());
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
	
	/*
	 * saldo - hold
	 */
	public  BigDecimal getSaldoAndHoldMerchant(String mid) throws SQLException {
		  BigDecimal saldo=new BigDecimal(0);
		  String sql="SELECT (SELECT saldo FROM merchant where mid=?)-(SELECT COALESCE(sum(amount),0) FROM saldolog s, merchant m where s.merchant=m.id and mid=? and s.status='HOLD') as saldo ";
	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setString(1, mid);
		    	statement.setString(2, mid);
			 
		      	ResultSet rs=statement.executeQuery();
		      	
		      	while(rs.next()){
		      		saldo=rs.getBigDecimal(1);
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
		   return saldo;
	  
	}
	
	public  BigDecimal getSaldoMerchant(String mid) throws SQLException {
		  BigDecimal saldo=new BigDecimal(0);
		  String sql="SELECT saldo FROM merchant where mid=? ";
	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setString(1, mid);
			 
		      	ResultSet rs=statement.executeQuery();
		      	
		      	while(rs.next()){
		      		saldo=rs.getBigDecimal(1);
		      	}
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		   return saldo;
	  
	}
	public Merchant findByYM(String ym) {
		try{
	    	return (Merchant) session.createQuery("select m from Merchant m where (ymid='"+ym+"@yahoo.com' or ymid='"+ym+"@yahoo.co.id' or ymid='"+ym+"@rocketmail.com' or ymid='"+ym+"@ymail.com'  ) and deleted=false").uniqueResult();
	    }
	    catch (HibernateException e) {
	    	return null;
	    }		
	}
}
