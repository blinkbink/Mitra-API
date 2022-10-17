package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Range;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

public class KeyDao {
	Session session;
	DB db;
	Log log;
	
	public KeyDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	public KeyDao(Session s){
		super();
		session = s;
	}
	  
	  public Key findById(Long id) throws HibernateException {
		    return (Key)session.load(Key.class, id);
	  }
	
	  
	  
	  public Key findByUserID(String user_id) throws HibernateException {
		    return (Key) session.createQuery("from Key where user_id ='"+user_id+"' order by id desc").list().get(0);
	  }
	  
	  public Key findByUserEmail(String user_id) throws HibernateException {
		    return (Key) session.createQuery("from Key where user_id LIKE '%<"+user_id+">%' AND jenis_key='PS' order by id desc").list().get(0);
	  }
	  
	  public Key getPrivByUserEmail(String user_id) throws HibernateException {
		    return (Key) session.createQuery("from Key where user_id LIKE '%<"+user_id+">%' AND jenis_key='PV' order by id desc").list().get(0);
	  }
	  
	  public List<Key> findByUser(String user) throws HibernateException {
		    return session.createQuery("select k from Key k where userdata ='"+user+"'").list();
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
