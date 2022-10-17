package id.co.keriss.consolidate.dao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Rekening;
import id.co.keriss.consolidate.util.LogSystem;
public class RekeningDao {
	Session session;
	DB db;
	Log log;
	
	public RekeningDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Rekening findById(Long id) throws HibernateException {
		    return session.load(Rekening.class, id);
	  }

  
//	  public Rekening findRekening(String nomor_rekening){
//		    try{
//		    	String sql = "from Rekening where nomor_rekening = :nomor_rekening";
//		    	Query query= session.createQuery(sql);
//		    	query.setParameter("nomor_rekening", nomor_rekening);
//		    	return (Rekening) query.uniqueResult();
//		    }catch (Exception e) {
////		    	e.printStackTrace();
//	            LogSystem.error(getClass(), e);
//
//		    	return null;
//		    	
//		    }
//	  }
	 
	  public  void delete(Rekening u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public void create(Rekening rr) {
	      try {
	        Transaction tx=session.beginTransaction();
	        session.save(rr);
	        tx.commit();
	      } catch (RuntimeException e) {
	            LogSystem.error(getClass(), e);
	      }  
	  }
	  
	  public  void update(Rekening key) {
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
