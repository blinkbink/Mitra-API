package id.co.keriss.consolidate.dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.util.LogSystem;
public class FormatPDFDao {
	Session session;
	DB db;
	Log log;
	
	public FormatPDFDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public FormatPdf findById(Long id) throws HibernateException {
		    return (FormatPdf)session.load(FormatPdf.class, id);
	  }

  
	  public FormatPdf findFormatPdf(String nama, Long idmitra){
		    try{
		    	String sql = "from FormatPdf where nama_format = :nama and mitra = :idmitra";
		    	Query query= session.createQuery(sql);
		    	query.setString("nama", nama);
		    	query.setLong("idmitra", idmitra);
		    	return (FormatPdf) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
		    	LogManager.getLogger(getClass()).error(e);

		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(FormatPdf key) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(key);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }

	  public  Long create(FormatPdf key) {
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

	  public  void update(FormatPdf key) {
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
