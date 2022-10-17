package id.co.keriss.consolidate.dao;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Initial;
import id.co.keriss.consolidate.ee.LetakTtd;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.util.LogSystem;
public class InitialDao {
	Session session;
	DB db;
	Log log;
	
	public InitialDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Initial findById(Long id) throws HibernateException {
		    return (Initial)session.load(Initial.class, id);
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
		    	LogManager.getLogger(getClass()).error(e);

		    	return null;
		    	
		    }
	  }
	  
	  public List<Initial> findInitialByDocac(Long iddocac){
		    try{
		    	String sql = "from Initial where doc_access = :id order by page asc";
		    	Query query= session.createQuery(sql);
		    	
		    	query.setLong("id", iddocac);
		    	return (List<Initial>) query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
		    	LogManager.getLogger(getClass()).error(e);

		    	return null;
		    	
		    }
	  }
	  
	  	 
	  public  void delete(Initial key) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(key);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }

	  public  Long create(Initial key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

//	    	log.debug(e);
	    }
		  return id;
	  }

	  public  void update(Initial key) {
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
