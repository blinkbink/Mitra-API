package id.co.keriss.consolidate.dao;
import java.math.BigInteger;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.StandardBasicTypes;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.PreRegistration;

public class PreRegistrationDao {
	Session session;
	DB db;
	Log log;
	
	public PreRegistrationDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	 
	  public PreRegistration findByEmail(String email) throws HibernateException {
		    return (PreRegistration) session.createQuery("from PreRegistration where email ='"+email+"'").uniqueResult();
	  }
	  
	  public PreRegistration findById(Long id) {
		  try {
		    return session.load(PreRegistration.class, id);
		  }catch (Exception e) {
			// TODO: handle exception
			  return null;
		}
	  }
	  
	  public PreRegistration findByEmailNik(String email, String nik){
		    try{
		    	String sql = "from PreRegistration reg where email = :email and no_identitas=:nik";
		    	Query query= session.createQuery(sql);
		    	query.setString("email", email.toLowerCase());
		    	query.setString("nik", nik);
		    	return (PreRegistration) query.uniqueResult();
		    }catch (Exception e) {
		    	e.printStackTrace();
		    	return null;
		    	
		    }
	  }

  
	  public PreRegistration findEmail(String email){
		    try{
		    	String sql = "from PreRegistration reg where lower(email) = :email";
		    	Query query= session.createQuery(sql);
		    	query.setString("email", email);
		    	return (PreRegistration) query.uniqueResult();
		    }catch (Exception e) {
		    	e.printStackTrace();
		    	return null;
		    	
		    }
	  }
	  
	  public PreRegistration emailPerusahaan(String email){
		    try{
		    	String sql = "from PreRegistration reg where lower(email_perusahaan) = :email";
		    	Query query= session.createQuery(sql);
		    	query.setString("email", email);
		    	return (PreRegistration) query.uniqueResult();
		    }catch (Exception e) {
		    	e.printStackTrace();
		    	return null;
		    	
		    }
	  }
	  
	  public List<PreRegistration> findNoHp(String hp){
		    try{
		    	String sql = "from PreRegistration reg where no_handphone = :hp";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("hp", hp);
		    	return query.list();
		    }catch (Exception e) {
		    	e.printStackTrace();
		    	return null;
		    	
		    }
	  }
	  
	  public PreRegistration findNik(String no_identitas){
		    try{
		    	String sql = "from PreRegistration reg where no_identitas = :no_identitas";
		    	Query query= session.createQuery(sql);
		    	query.setString("no_identitas", no_identitas);
		    	return (PreRegistration) query.uniqueResult();
		    }catch (Exception e) {
		    	e.printStackTrace();
		    	return null;
		    	
		    }
	  }
	  
	  public PreRegistration findEmailByMitra(String email, Long mitra){
		    try{
		    	String sql = "from PreRegistration reg where lower(email) = :email and mitra=:mitra";
		    	Query query= session.createQuery(sql);
		    	query.setString("email", email);
		    	query.setLong("mitra", mitra);
		    	return (PreRegistration) query.uniqueResult();
		    }catch (Exception e) {
		    	e.printStackTrace();
		    	return null;
		    	
		    }
	  }
	  
	  public Long getNext() {
		    Query query = 
		        session.createSQLQuery("select nextval('kode_user_seq') as num")
		            .addScalar("num", StandardBasicTypes.BIG_INTEGER);
		    BigInteger hasil= (BigInteger) query.uniqueResult();
		    return hasil.longValue(); 
		}
	 
	  public  void delete(PreRegistration u) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(u);
	      tx.commit();

		} catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  Long create(PreRegistration key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	id=null;
	    }
		  return id;
	  }

	  public  void update(PreRegistration key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(key);
	      tx.commit();
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
