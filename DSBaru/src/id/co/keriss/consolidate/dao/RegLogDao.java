package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.RegistrationLog;
import id.co.keriss.consolidate.util.LogSystem;
public class RegLogDao {
	Session session;
	DB db;
	Log log;
	
	public RegLogDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public RegistrationLog findById(Long id) throws HibernateException {
		    return session.load(RegistrationLog.class, id);
	  }

	  public List<RegistrationLog> findByMitraNikEmail(Long mitra, String nik, String email){
		    try{
		    	String sql = "from RegistrationLog where mitra = :mitra and no_identitas=:nik and lower(email)=:email and date(date)=date(now()) order by id ASC"; 
		    	Query query= session.createQuery(sql).setMaxResults(4);
		    	query.setLong("mitra", mitra);
		    	query.setString("nik", nik);
		    	query.setString("email", email);
		    	return (List<RegistrationLog>) query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    }
	  }
	 
	  public  void delete(RegistrationLog u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public Long create(RegistrationLog key) {
		  Long id=null;
	    try {
	    	Transaction tx=null;

	    	if(session.getTransaction().isActive())
	    	{
	    		tx=session.getTransaction();
	    	}
	    	else
	    	{
	    		tx=session.beginTransaction();
	    	}
    	  
	      id=(Long) session.save(key);
//	      System.out.println("id sequence>"+id);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);
	    }
		  return id;
	  }

	  public  void update(RegistrationLog key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(key);
	      tx.commit();
	      
	    } catch (RuntimeException e) {
//	        log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }
	  
	  
}
