package id.co.keriss.consolidate.dao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.LetakTtd;
import id.co.keriss.consolidate.util.LogSystem;
public class LetakTtdDao {
	Session session;
	DB db;
	Log log;
	
	public LetakTtdDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public LetakTtd findById(Long id) throws HibernateException {
		    return session.load(LetakTtd.class, id);
	  }

  
	  public LetakTtd findLetakTtd(String ttdke, Long idformatpdf){
		    try{
		    	String sql = "from LetakTtd where ttd_ke = :ttdke and format_pdf = :id";
		    	Query query= session.createQuery(sql);
		    	query.setInteger("ttdke", Integer.valueOf(ttdke));
		    	query.setLong("id", idformatpdf);
		    	return (LetakTtd) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public LetakTtd findLetakPrf(String prfke, Long idformatpdf){
		    try{
		    	String sql = "from LetakTtd where prf_ke = :prfke and format_pdf = :id";
		    	Query query= session.createQuery(sql);
		    	query.setInteger("prfke", Integer.valueOf(prfke));
		    	query.setLong("id", idformatpdf);
		    	return (LetakTtd) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(LetakTtd key) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(key);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(LetakTtd key) {
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

	  public  void update(LetakTtd key) {
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
