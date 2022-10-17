package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.ee.Partner;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class PartnerDao {
	Session session;
	DB db;
	Log log;
	
	public PartnerDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Partner> findAll () throws HibernateException {
        return session.createCriteria (Partner.class).list();
      }
	  
	 
	  
	  public Partner findByPid(String pid) throws HibernateException {
		    try{
		    	return (Partner) session.createQuery("from Partner where pid='"+pid+"'").uniqueResult();
		    }
		    catch (HibernateException e) {
		    	return null;
		    }
	  }
	  
	  public Partner findById(Long id) throws HibernateException {
		    return (Partner)session.load(Partner.class, id);
	  }
	  
	  public  void deletePartner(Partner partner) {
		try {
	      session.delete(partner);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createPartner(Partner partner) {
	    try {
		      session.save(partner);
	      System.out.println("Partner Saved");
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updatePartner(Partner partner) {
	    try {
	      session.update(partner);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
}
