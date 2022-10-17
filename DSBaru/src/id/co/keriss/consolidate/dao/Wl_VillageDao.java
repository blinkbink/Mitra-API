package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Wl_Villages;

public class Wl_VillageDao {
	Session session;
	DB db;
	Log log;
	
	public Wl_VillageDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public List findAll () throws HibernateException {
        return session.createCriteria (Wl_Villages.class).list();
      }
	  
	  public Wl_Villages findById(String id) throws HibernateException {
		    return session.load(Wl_Villages.class, id);
	  }
	  
	  public  void deleteWl_Villages(Wl_Villages wl_Villages) {
		try {
	      session.delete(wl_Villages);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteWl_VillagesCommit(Wl_Villages wl_Villages) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(wl_Villages);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createSupplier(Wl_Villages wl_Villages) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(wl_Villages);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateSupplier(Wl_Villages wl_Villages) {
	    try {
	      session.update(wl_Villages);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	public List findVillages(String district_id) throws HibernateException {
		return this.session.createQuery("from Wl_Villages where district_id = '"+district_id+"'").list();
	} 
	
	public Wl_Villages findKodepos(String district_id) throws HibernateException {
		return (Wl_Villages) this.session.createQuery("from Wl_Villages where id = '"+district_id+"'").uniqueResult();
	} 
	  
}
