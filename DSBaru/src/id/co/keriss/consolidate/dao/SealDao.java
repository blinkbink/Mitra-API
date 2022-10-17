package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Seal;
import id.co.keriss.consolidate.util.LogSystem;
public class SealDao {
	Session session;
	DB db;
	Log log;
	
	public SealDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	
	 public List<Seal> findByEeuser(Long mitra){
		    try{
		    	String sql = "from Seal where mitra = :mitra";
		    	Query query= session.createQuery(sql);
		    	query.setLong("mitra", mitra);
		    	return query.list();
		    }catch (Exception e) {
	            LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }	 
	 
	 
	  public  void delete(Seal seal) {
		try {
	    	Transaction tx=session.beginTransaction();
	    	session.delete(seal);
	    	tx.commit();

		} catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(Seal seal) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(seal);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
		  return id;
	  }

	  public  void update(Seal seal) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(seal);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }
}
