package id.co.keriss.consolidate.dao;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentSummary;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.ReRegistration;
import id.co.keriss.consolidate.util.LogSystem;

public class ReRegistrationDao {
	Session session;
	DB db;
	Log log;
	
	public ReRegistrationDao(DB db)
	{
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  

	public  void create(ReRegistration trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
	        LogSystem.error(getClass(), e);
	
	    }
	  }

	  public  void update(Documents trx) {
		  
	    try {
	      Transaction t=session.beginTransaction();
	      session.update(trx);
	      t.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    } 
	  }

	  public  void delete(Documents trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.delete(trx);
	      t.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }

	  
}
