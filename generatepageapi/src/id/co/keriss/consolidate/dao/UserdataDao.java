package id.co.keriss.consolidate.dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;

public class UserdataDao {
	Session session;
	DB db;
	Log log;
	
	public UserdataDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Userdata findById(Long id) throws HibernateException {
		    return (Userdata)session.load(Userdata.class, id);
	  }

  
	  public Userdata findByKtp(String no_identitas){
		    try{
		    	String sql = "from Userdata u where no_identitas ='"+no_identitas+"'";
//		    	System.out.println(sql);
		    	Userdata data=  (Userdata) session.createQuery(sql).uniqueResult();
		    	return data;
		    }catch (Exception e) {
		    	LogManager.getLogger(getClass()).error(e);
		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(Userdata u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }

	  public  Long create(Userdata key) {
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

	  public  void update(Userdata key) {
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
