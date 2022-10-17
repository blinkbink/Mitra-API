package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Wl_Districts;

public class Wl_DistrictsDao {
	Session session;
	DB db;
	Log log;
	
	public Wl_DistrictsDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public List findAll () throws HibernateException {
        return session.createCriteria (Wl_Districts.class).list();
      }
	  
	  public Wl_Districts findById(String id) throws HibernateException {
		    return session.load(Wl_Districts.class, id);
	  }
	  
	  public  void deleteWl_Districts(Wl_Districts wl_Districts) {
		try {
	      session.delete(wl_Districts);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteWl_DistrictsCommit(Wl_Districts wl_Districts) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(wl_Districts);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createSupplier(Wl_Districts wl_Districts) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(wl_Districts);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateSupplier(Wl_Districts wl_Districts) {
	    try {
	      session.update(wl_Districts);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	public List findDistricts(String regency_id) throws HibernateException {
		return this.session.createQuery("from Wl_Districts where regency_id = '"+regency_id+"'").list();
	} 
	  
}
