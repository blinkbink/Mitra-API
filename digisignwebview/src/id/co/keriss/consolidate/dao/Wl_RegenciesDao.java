package id.co.keriss.consolidate.dao;

import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;
import id.co.keriss.consolidate.ee.Wl_Regencies;

public class Wl_RegenciesDao {
	Session session;
	DB db;
	Log log;
	
	public Wl_RegenciesDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public List findAll () throws HibernateException {
        return session.createCriteria (Wl_Regencies.class).list();
      }
	  
	  public Wl_Regencies findById(String id) throws HibernateException {
		    return session.load(Wl_Regencies.class, id);
	  }
	  
	  public  void deleteWl_Regencies(Wl_Regencies wl_Regencies) {
		try {
	      session.delete(wl_Regencies);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteWl_RegenciesCommit(Wl_Regencies wl_Regencies) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(wl_Regencies);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createSupplier(Wl_Regencies wl_Regencies) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(wl_Regencies);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateSupplier(Wl_Regencies wl_Regencies) {
	    try {
	      session.update(wl_Regencies);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	public List findRegencies(String province_id) throws HibernateException {
		return this.session.createQuery("from Wl_Regencies where province_id = '"+province_id+"'").list();
	} 
	  
}
