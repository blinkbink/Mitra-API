package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.util.LogSystem;
public class AlamatDao {
	Session session;
	DB db;
	Log log;
	
	public AlamatDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Alamat findById(Long id) throws HibernateException {
		    return session.load(Alamat.class, id);
	  }

  
	  public List<Alamat> findByUserdata(Long userdata){
		    try{
		    	String sql = "from Alamat where userdata = :userdata order by status asc";
		    	Query query= session.createQuery(sql);
		    	query.setLong("userdata", userdata);
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public Alamat findAKtpByUserData(Long userdata){
		    try{
		    	String sql = "from Alamat where userdata = :userdata and status='1'";
		    	Query query= session.createQuery(sql);
		    	query.setLong("userdata", userdata);
		    	return (Alamat) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public Alamat findADomByUserData(Long userdata){
		    try{
		    	String sql = "from Alamat where userdata = :userdata and status='2'";
		    	Query query= session.createQuery(sql);
		    	query.setLong("userdata", userdata);
		    	return (Alamat) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(Alamat u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(Alamat key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

//	    	log.debug(e);
	    }
		  return id;
	  }

	  public  void update(Alamat key) {
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
