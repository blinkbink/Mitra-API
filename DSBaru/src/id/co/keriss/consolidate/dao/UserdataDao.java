package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
		    return session.load(Userdata.class, id);
	  }

  
	  public List<Userdata> findByKtp(String no_identitas){
		    try{
		    	String sql = "from Userdata u where no_identitas ='"+no_identitas+"'";
//		    	System.out.println(sql);
		    	List<Userdata> data=  (List<Userdata>) session.createQuery(sql).list();
		    	return data;
		    }catch (Exception e) {
		    	LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }
	  
	  public List<Userdata> findByNoHp(String noHp){
		    try{
		    	String sql = "from Userdata where no_handphone=:nohp";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("nohp", noHp);
		    	
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

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
            LogSystem.error(getClass(), e);

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
            LogSystem.error(getClass(), e);

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
            LogSystem.error(getClass(), e);

	    }
	  }
	  
	  
}
