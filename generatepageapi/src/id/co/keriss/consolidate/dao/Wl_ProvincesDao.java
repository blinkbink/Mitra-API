package id.co.keriss.consolidate.dao;

import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Wl_Provinces;

public class Wl_ProvincesDao {
	Session session;
	DB db;
	Log log;
	
	public Wl_ProvincesDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Wl_Provinces.class).list();
      }
	  
	  public Wl_Provinces findById(String id) throws HibernateException {
		    return (Wl_Provinces)session.load(Wl_Provinces.class, id);
	  }
	  
	  public  void deleteWl_Provinces(Wl_Provinces wl_Provinces) {
		try {
	      session.delete(wl_Provinces);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteWl_ProvincesCommit(Wl_Provinces wl_Provinces) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(wl_Provinces);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createSupplier(Wl_Provinces wl_Provinces) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(wl_Provinces);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateSupplier(Wl_Provinces wl_Provinces) {
	    try {
	      session.update(wl_Provinces);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	
	
	  
	  
}
