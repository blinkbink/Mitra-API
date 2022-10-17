package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.ee.Edc;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class EdcDao {
	Session session;
	DB db;
	Log log;
	
	public EdcDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Edc.class).list();
      }
	  
	  public Edc findById(Long id) throws HibernateException {
		    return (Edc)session.load(Edc.class, id);
	  }
	  
	  public Edc findByModel(String name) throws HibernateException {
		    return (Edc) session.createQuery("from Edc where model ='"+name+"'").uniqueResult();
	  }
	  
	  public  void deleteTransaction(Edc edc) {
		try {
	      session.delete(edc);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createEdc(Edc edc) {
	    try {
	      session.save(edc);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateEdc(Edc edc) {
	    try {
	      session.update(edc);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
