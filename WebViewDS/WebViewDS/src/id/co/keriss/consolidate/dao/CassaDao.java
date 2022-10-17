package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.Cassa;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class CassaDao {
	Session session;
	DB db;
	Log log;
	
	public CassaDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Bank.class).list();
      }
	  
	  public Bank findById(Long id) throws HibernateException {
		    return session.load(Bank.class, id);
	  }
	
	  public Bank findByCard(Integer card) throws HibernateException {
		    return (Bank) session.createQuery("select b from Bank b, Product p, Range r " +
		    		"where r.low <="+card+" and r.high>="+card+" and r.product=p and p.bank=b" 
		    		).uniqueResult();
	  }
	  
	  public Bank findByName(String name) throws HibernateException {
		    return (Bank) session.createQuery("from Bank where name ='"+name+"'").uniqueResult();
	  }
	  
	  public Cassa findByTid(String tid){
		  	Cassa cassa=null;
		  	Query q=session.createQuery("select c from Cassa c,Terminal tm, Edc edc where " +
		  			"tm.edc=edc and " +
		  			"edc.cassa=c and " +
		  			"tm.tid='"+tid+"'");
		  	cassa = (Cassa)q.uniqueResult();
		  	return cassa;
		  
	  }
	  
	  public  void deleteBank(Bank bank) {
		try {
	      session.delete(bank);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createBank(Bank bank) {
	    try {
	      session.save(bank);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateBank(Bank bank) {
	    try {
	      session.update(bank);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
