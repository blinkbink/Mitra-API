package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.ee.Feature;
import id.co.keriss.consolidate.ee.NominalAmount;
import id.co.keriss.consolidate.ee.Productbiller;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class NominalAmountDao {
	Session session;
	DB db;
	Log log;
	
	public NominalAmountDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (NominalAmount.class).list();
      }
	  
	  public NominalAmount findById(Long id) throws HibernateException {
		    return (NominalAmount)session.load(NominalAmount.class, id);
	  }
	
	  public List<NominalAmount> getAll() throws HibernateException {
		  	
			 
		    try{
		    	return session.createQuery("select na from NominalAmount na order by denom asc").list();
		    
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	    
	  public  void deleteTransaction(NominalAmount nominalAmount) {
		try {
	      session.delete(nominalAmount);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createFeature(NominalAmount nominalAmount) {
	    try {
	      session.save(nominalAmount);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateFeature(NominalAmount nominalAm) {
	    try {
	      session.update(nominalAm);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
