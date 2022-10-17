package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Supplier;

public class SupplierDao {
	Session session;
	DB db;
	Log log;
	
	public SupplierDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Supplier.class).list();
      }
	  
	  public Supplier findById(Long id) throws HibernateException {
		    return session.load(Supplier.class, id);
	  }
	  
	
	  
	  public  void deleteSupplier(Supplier supplier) {
		try {
	      session.delete(supplier);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteSupplierCommit(Supplier supplier) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(supplier);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createSupplier(Supplier supp) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(supp);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateSupplier(Supplier supplier) {
	    try {
	      session.update(supplier);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	
	
	  
	  
}
