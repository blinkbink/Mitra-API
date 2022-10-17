package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.ee.InterfaceMitra;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.util.LogSystem;

public class CallbackPendingDao {
	Session session;
	DB db;
	Log log;
	
	public CallbackPendingDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public CallbackPending findById(Long id) throws HibernateException {
		    return session.load(CallbackPending.class, id);
	  }

	 
	  public  void delete(CallbackPending u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(CallbackPending key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

//	    	log.debug(e);
	    }
		  return id;
	  }

	  public  void update(CallbackPending key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(key);
	      tx.commit();
	      
	    } catch (RuntimeException e) {
//	        log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

}
