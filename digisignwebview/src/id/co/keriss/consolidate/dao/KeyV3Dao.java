package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.KeyV3;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class KeyV3Dao {
	Session session;
	DB db;
	Log log;
	
	public KeyV3Dao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	public KeyV3Dao(Session s){
		super();
		session = s;
	}
	  
	  public List<KeyV3> findByEeuser(Long eeuser) throws HibernateException {
		    return (List<KeyV3>) session.createQuery("from KeyV3 where user ='"+eeuser+"' and status='ACT' order by id desc").list();
	  }
	  
	  public  void delete(Key key) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(key);
	      tx.commit();

		} catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void create(Key key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void update(Key key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(key);
	      tx.commit();
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
