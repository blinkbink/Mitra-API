package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.TopupLog;

public class TopupLogDao {
	Session session;
	DB db;
	Log log;
	
	public TopupLogDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (TopupLogDao.class).list();
      }
	  
	  

	  public  void createTopup(TopupLog touplog) {
		    try {
		    	Transaction tx=session.beginTransaction();
			      session.save(touplog);
			      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }catch(Exception e){
		    	log.debug(e);
		    }
		  }
}