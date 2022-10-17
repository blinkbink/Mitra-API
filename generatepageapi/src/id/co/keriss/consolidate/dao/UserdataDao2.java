package id.co.keriss.consolidate.dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Userdata2;
import id.co.keriss.consolidate.util.LogSystem;

public class UserdataDao2 {
	Session session;
	DB db;
	Log log;
	
	public UserdataDao2(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Userdata2 findById(Long id) throws HibernateException {
		    return (Userdata2)session.load(Userdata2.class, id);
	  }

  
	  public Userdata2 findByKtp(String no_identitas){
		    try{
		    	String sql = "from Userdata2 u where no_identitas ='"+no_identitas+"'";
		    	System.out.println(sql);
		    	return (Userdata2) session.createQuery(sql).uniqueResult();
		    }catch (Exception e) {
		    	LogManager.getLogger(getClass()).error(e);
		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(Userdata2 u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }

	  public  Long create(Userdata2 key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);
	    }
		  return id;
	  }

	  public  void update(Userdata2 key) {
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
