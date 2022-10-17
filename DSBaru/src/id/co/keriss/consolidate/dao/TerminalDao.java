package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Terminal;

public class TerminalDao {
	Session session;
	DB db;
	Log log;
	
	public TerminalDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Terminal.class).list();
      }
	  
	  public List findAll2 (String merchant) throws HibernateException {
	        //return session.createCriteria (Terminal.class).list();
	        return session.createQuery("from Terminal where merchant ='"+merchant+"'").list();
	      }
	  
	  public Terminal findByTid(String tid) throws HibernateException {
		  Criteria crit = session.createCriteria (Terminal.class).add (Restrictions.eq ("tid", tid));
		  try{
			  return (Terminal) crit.uniqueResult();
		  }catch (Exception e) {
			return null;
		}
	  }
	  
	  public Terminal findByTid(String tid, Long idMerchant) throws HibernateException {
	        Terminal res=null;
		  try{
		        res=(Terminal) session.createQuery("from Terminal where tid='"+tid+"' and merchant ='"+idMerchant+"'").list().get(0);
		        return res;
		  }catch (Exception e) {
			return null;
		}
	  }
	  
	  public Terminal findById(Long id) throws HibernateException {
	    return session.load(Terminal.class, id);
	  }
	
	  public  void deleteTerminal(Terminal terminal) {
		try {
	      session.delete(terminal);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  public  void deleteTerminalCommit(Terminal terminal) {
			try {
		    	Transaction tx=session.beginTransaction();
		      session.delete(terminal);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }

	  public  void createTerminal(Terminal terminal) {
	    try {
		      session.save(terminal);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createTerminalCommit(Terminal terminal) {
		    try {
		    	Transaction tx=session.beginTransaction();
			      session.save(terminal);
			      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }catch(Exception e){
		    	Log.getLog("Q2", "Terminal Insert Commit").info("Gagal insert");

		    }
		  }
	  public  void updateTerminal(Terminal terminal) {
	    try {
	      session.update(terminal);
	      System.out.println("Terminal Saved");
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public  void updateTerminalCommit(Terminal terminal) {
		    try {
		    	Transaction tx=session.beginTransaction();
		      session.update(terminal);
		      tx.commit();

		      System.out.println("Terminal Saved");
		    } catch (RuntimeException e) {
		        log.debug(e);
		    }
		  }
	  
	  public Terminal findByTidMid(String tid, String mid){
		 try{
			 Terminal term=null;
			 Query q = session.createQuery("select t from Terminal t, Merchant m where t.merchant=m.id and t.tid='"+tid+"' and m.mid='"+mid+"'");
			 term = (Terminal)q.uniqueResult();
			 return term;
		 }catch (Exception e) {
			 return null;
		 }
	  }
	  
	  public Terminal findByTidUsername(String tid, String username){

		  		return (Terminal) session.createQuery("select t from Terminal t, Merchant m where t.merchant=m.id and t.tid='"+tid+"' and m.username='"+username+"'").uniqueResult();
				
			 
			 
		  }
}