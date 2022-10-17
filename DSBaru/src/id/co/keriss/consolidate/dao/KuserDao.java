package id.co.keriss.consolidate.dao;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Kuser;
import id.co.keriss.consolidate.ee.LetakTtd;
import id.co.keriss.consolidate.util.LogSystem;
public class KuserDao {
	Session session;
	DB db;
	Log log;
	
	public KuserDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	
	 public Kuser findByEeuser(Long eeuser){
		    try{
		    	String sql = "from Kuser where eeuser = :eeuser";
		    	Query query= session.createQuery(sql);
		    	query.setLong("eeuser", eeuser);
		    	return (Kuser) query.uniqueResult();
		    }catch (Exception e) {
	            LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }
	 
	 public Kuser findByEeuserKuser(Long eeuser, String key){
		    try{
		    	String sql = "from Kuser where eeuser = :eeuser and kuser=:key";
		    	Query query= session.createQuery(sql);
		    	query.setLong("eeuser", eeuser);
		    	query.setString("key", key);
		    	return (Kuser) query.uniqueResult();
		    }catch (Exception e) {
	            LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }
	 
	 
	 
	  public  void delete(Kuser kuser) {
		try {
	    	Transaction tx=session.beginTransaction();
	    	session.delete(kuser);
	    	tx.commit();

		} catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(Kuser kuser) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(kuser);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
		  return id;
	  }

	  public  void update(Kuser kuser) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(kuser);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }
}
