package id.co.keriss.consolidate.dao;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.VerificationData;
import id.co.keriss.consolidate.util.LogSystem;

public class VerificationDataDao {
	Session session;
	DB db;
	Log log;
	
	public VerificationDataDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	 public VerificationDataDao (Session session) {
	        super ();
	        this.session = session;
	    }
	    
	   
	 
	  public  void create(VerificationData trx) {
	    try {
	      if(trx.getWajah().length()>150) trx.setWajah(null);
	      Transaction t=session.beginTransaction();
	      session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }
	  
	  public  void update(VerificationData trx) {
		    try {
		      Transaction t=session.beginTransaction();
		      session.update(trx);
		      t.commit();
		    } catch (RuntimeException e) {
//		    	log.debug(e);
	            LogSystem.error(getClass(), e);

		    }
		  }

	public VerificationData getByNik(String nik) {
	    try
	    {
	      String sql = "select vd from VerificationData vd where vd.nik=:nik ";
	      
	      Query query = this.session.createQuery(sql);
	      query.setParameter("nik", nik);
	      if(query.list().size()>0)return (VerificationData)query.list().get(0);
	    }
	    catch (Exception e)
	    {
	      LogSystem.error(getClass(), e);
	    }
	    return null;
		 
	}
}
