package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Verifier;
import id.co.keriss.consolidate.util.LogSystem;
public class VerifierDao {
	Session session;
	DB db;
	Log log;
	
	public VerifierDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	  
	  public Verifier findById(Long id) throws HibernateException {
		    return session.load(Verifier.class, id);
	  }

  
	  public Verifier findNik(String NIK){
		    try{
		    	String sql = "from Verifier where id_ktp = :nik";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("nik", NIK);
		    	return (Verifier) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public List<Verifier> findByVid(String vid, Long mitra){
		    try{
		    	String sql = "from Verifier where verifier_id = :vid and mitra=:mitra order by id desc";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("vid", vid);
		    	query.setLong("mitra", mitra);
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public Verifier findByEmail(String email){
		    try{
		    	String sql = "from Verifier where email = :email";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("email", email);
		    	return (Verifier) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public Verifier findByEmaildanNik(String email, String nik){
		    try{
		    	String sql = "from Verifier where email = :email and id_ktp=:nik";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("email", email);
		    	query.setParameter("nik", nik);
		    	return (Verifier) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public Verifier findByNoTlp(String tlp){
		    try{
		    	String sql = "from Verifier where tlp = :tlp";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("tlp", tlp);
		    	return (Verifier) query.uniqueResult();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	 
	  public  void delete(Verifier u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(Verifier key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

//	    	log.debug(e);
	    }
		  return id;
	  }

	  public  void update(Verifier key) {
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
