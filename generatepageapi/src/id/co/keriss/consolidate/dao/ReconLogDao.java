package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.ee.ReconLog;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.TopupLog;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class ReconLogDao {
	Session session;
	DB db;
	Log log;

	public ReconLogDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public ReconLogDao(Session ses){
		super();
		session = ses;
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (ReconLogDao.class).list();
      }
	  
	  

	  public  void createReconLog(ReconLog reconLog) {
		    try {
		    	Transaction tx=session.beginTransaction();
			      session.save(reconLog);
			      tx.commit();
			      System.out.println("CREATE RECON");
		    } catch (RuntimeException e) {
		    	e.printStackTrace();
		    }
		  }
	  
	  public  void updateReconLog(ReconLog reconLog) {
		    try {
		    	Transaction tx=session.beginTransaction();
			      session.update(reconLog);
			      tx.commit();
			      System.out.println("upload RECON");
		    } catch (RuntimeException e) {
		    	e.printStackTrace();
		    }
		  }
}