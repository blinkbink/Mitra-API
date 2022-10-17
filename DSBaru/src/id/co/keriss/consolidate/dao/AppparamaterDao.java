package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Appparameter;
import id.co.keriss.consolidate.ee.Bank;

public class AppparamaterDao {
	Session session;
	DB db;
	Log log;
	
	public AppparamaterDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Appparameter> findAll () throws HibernateException {
		  Query query = session.createQuery("select appparameter from Appparameter appparameter");
          return query.list();
      }
	  
	  public Bank findById(Long id) throws HibernateException {
		    return session.load(Bank.class, id);
	  }
	
	  public Appparameter findByNameValue(String name, String value){
		    try{
		    	return (Appparameter) session.createQuery("select ap from Appparameter ap where name ='"+name+"' and value='"+value+"'").uniqueResult();
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	  
	 
	  
	  public  void deleteAppparameter(Appparameter app) {
		try {
	      session.delete(app);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createAppparameter(Appparameter app) {
	    try {
	      session.save(app);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateBank(Appparameter app) {
	    try {
	      session.update(app);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
