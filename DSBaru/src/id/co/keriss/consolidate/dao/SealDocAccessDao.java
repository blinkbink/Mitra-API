package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Seal;
import id.co.keriss.consolidate.ee.SealDocAccess;
import id.co.keriss.consolidate.util.LogSystem;
public class SealDocAccessDao {
	Session session;
	DB db;
	Log log;
	
	public SealDocAccessDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	
	 public List<SealDocAccess> findByDocAccess(Long docaccess){
		    try{
		    	String sql = "from Seal where doc_access = :docaccess";
		    	Query query= session.createQuery(sql);
		    	query.setLong("docaccess",docaccess);
		    	return query.list();
		    }catch (Exception e) {
	            LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }	 
	 
	 
	  public  void delete(SealDocAccess SealDocAccess) {
		try {
			 Transaction t = null;
		    	if(!session.getTransaction().isActive())
				{
					t = session.beginTransaction();
				}
		    	else
		    	{
		    		t = session.getTransaction();
		    	}
		    	
	    	session.delete(SealDocAccess);
	    	t.commit();

		} catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(SealDocAccess SealDocAccess) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(SealDocAccess);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
		  return id;
	  }

	  public  void update(SealDocAccess SealDocAccess) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(SealDocAccess);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }
}
