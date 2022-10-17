package id.co.keriss.consolidate.dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.util.LogSystem;
public class MitraDao {
	Session session;
	DB db;
	Log log;
	
	public MitraDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Mitra findById(Long id) throws HibernateException {
		    return (Mitra)session.load(Mitra.class, id);
	  }

  
	  public Mitra findMitra(String nama){
		    try{
		    	String sql = "from Mitra where name = :nama";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("nama", nama);
		    	return (Mitra) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
		    	LogManager.getLogger(getClass()).error(e);

		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(Mitra u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }

	  public  Long create(Mitra key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

//	    	log.debug(e);
	    }
		  return id;
	  }

	  public  void update(Mitra key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(key);
	      tx.commit();
	      
	    } catch (RuntimeException e) {
//	        log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }
	  
	  
}
