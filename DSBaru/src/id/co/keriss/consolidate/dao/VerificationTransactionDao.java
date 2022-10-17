package id.co.keriss.consolidate.dao;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.VerificationTransaction;
import id.co.keriss.consolidate.util.LogSystem;

public class VerificationTransactionDao {
	Session session;
	DB db;
	Log log;
	
	public VerificationTransactionDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	
	 
	  public  Long create(VerificationTransaction trx) {
		Long id=null;
	    try {
	      Transaction t=session.beginTransaction();
	      id=(Long) session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	    return id;
	  }
	  
	
	  
	 
	  
}
