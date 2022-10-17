package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.PerubahanData;
import id.co.keriss.consolidate.util.LogSystem;
public class PerubahanDataDao {
	Session session;
	DB db;
	Log log;
	
	public PerubahanDataDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public PerubahanData findById(Long id) throws HibernateException {
		    return session.load(PerubahanData.class, id);
	  }

	  public List<PerubahanData> findByUserMitra(Long idUser, Long userid) throws HibernateException {
		  try{
		    	String sql = "from PerubahanData where eeuser ='"+idUser+"' and update_from='"+userid+"' order by tgl_req desc";
		    	System.out.println(sql);
		    	return session.createQuery(sql).list();
		    }catch (Exception e) {
		    	LogSystem.error(getClass(), e);
		    	return null;
		    	
		    }
	  }
	  	 
	  public  void delete(PerubahanData p) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(p);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(PerubahanData key) {
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

	  public  void update(PerubahanData key) {
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
