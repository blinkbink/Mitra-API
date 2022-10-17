package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.ee.Account;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class AccountDao {
	Session session;
	DB db;
	Log log;
	
	public AccountDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Account.class).list();
      }
	  
	  public List findAll2 (String merchant) throws HibernateException {
	        //return session.createCriteria (Terminal.class).list();
	        return session.createQuery("from Account where merchant ='"+merchant+"'").list();
	      }
	  
	  public Account findTrack2(String track2) throws HibernateException {
		  Criteria crit = session.createCriteria (Account.class).add (Restrictions.eq ("datacard", track2));
		  try{
			  return (Account) crit.uniqueResult();
		  }catch (Exception e) {
			return null;
		}
	  }
	  
	  public Account findById(Long id) throws HibernateException {
	    return session.load(Account.class, id);
	  }
	
	  public  void deleteTerminal(Account account) {
		try {
	      session.delete(account);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  public  void deleteTerminalCommit(Account account) {
			try {
		    	Transaction tx=session.beginTransaction();
		      session.delete(account);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }

	  public  void createTerminal(Account account) {
	    try {
		      session.save(account);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createTerminalCommit(Account account) {
		    try {
		    	Transaction tx=session.beginTransaction();
			      session.save(account);
			      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }catch(Exception e){
		    	Log.getLog("Q2", "Terminal Insert Commit").info("Gagal insert");

		    }
		  }
	  public  void updateTerminal(Account account) {
	    try {
	      session.update(account);
	      System.out.println("Terminal Saved");
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void updateTerminalCommit(Account account) {
		    try {
		    	Transaction tx=session.beginTransaction();
		      session.update(account);
		      tx.commit();

		      System.out.println("Terminal Saved");
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }

}