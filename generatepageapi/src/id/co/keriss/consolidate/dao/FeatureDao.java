package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.ee.Feature;
import id.co.keriss.consolidate.ee.Productbiller;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class FeatureDao {
	Session session;
	DB db;
	Log log;
	
	public FeatureDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Feature.class).list();
      }
	  
	  public Feature findById(Long id) throws HibernateException {
		    return (Feature)session.load(Feature.class, id);
	  }
	
	  public List<Feature> getAllFeatureEmpty(String mid) throws HibernateException {
		  	
			 
		    try{
		    	return session.createQuery("select ft from Feature ft where id not in (select f.id from Productbiller p , Feature f , Productbillermerchant pm, Merchant m " +
			 "where p.feature=f.id AND p.id=pm.productbiller AND pm.merchant=m.id AND m.mid='"+mid+"') ").list();
		    
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	    
	  public  void deleteTransaction(Feature feature) {
		try {
	      session.delete(feature);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createFeature(Feature feature) {
	    try {
	      session.save(feature);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateFeature(Feature feature) {
	    try {
	      session.update(feature);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
