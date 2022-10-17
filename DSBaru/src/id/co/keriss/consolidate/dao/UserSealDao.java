package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Seal;
import id.co.keriss.consolidate.ee.UserSeal;
import id.co.keriss.consolidate.util.LogSystem;
public class UserSealDao {
	Session session;
	DB db;
	Log log;
	
	public UserSealDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	
	 public UserSeal findByEeuser(Long user, String nameseal){
		    try{
		    	String sql = "select u from UserSeal u, Seal s where u.eeuser = :user and s.name=:name and u.seal=s.id";
		    	Query query= session.createQuery(sql);
		    	query.setLong("user", user);
		    	query.setString("name", nameseal);
		    	return (UserSeal) query.uniqueResult();
		    }catch (Exception e) {
	            LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }	 
	 
	 
	  public  void delete(UserSeal seal) {
		try {
	    	Transaction tx=session.beginTransaction();
	    	session.delete(seal);
	    	tx.commit();

		} catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(UserSeal seal) {
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

	  public  void update(UserSeal seal) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(seal);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }
}
