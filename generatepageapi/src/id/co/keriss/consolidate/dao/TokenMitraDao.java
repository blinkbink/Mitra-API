package id.co.keriss.consolidate.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.TokenMitra;

public class TokenMitraDao {
    private Session session;

    public TokenMitraDao (DB db) {
        super ();
        this.session = db.session();
    }
    public TokenMitraDao (Session session) {
        super ();
        this.session = session;
    }
    
    public TokenMitra findById(Long id) throws HibernateException {
	    return (TokenMitra)session.load(TokenMitra.class, id);
    }
    
    public TokenMitra findByToken(String token)throws Exception{
	    try{
	    	String sql = "from TokenMitra where lower(token) = :token and status_aktif='true'";
	    	Query query= session.createQuery(sql);
	    	query.setString("token", token);
	    	return (TokenMitra) query.setMaxResults(1).uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
	    	LogManager.getLogger(getClass()).error(e);
	    	throw new Exception("Failed check token to database", e);
//	    	return null;
	    	
	    }
  }
    
    public TokenMitra findByName(long mitraName){
	    try{
	    	String sql = "from TokenMitra where mitra.id = :token and status_aktif='true'";
	    	Query query= session.createQuery(sql);
	    	query.setParameter("token", Long.valueOf(mitraName));
	    	return (TokenMitra) query.setMaxResults(1).uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
	    	LogManager.getLogger(getClass()).error(e);

	    	return null;
	    	
	    }
  }
    
  public void update(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

      }
    
    
  }
  
  public void create(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

      }
    
    
  }
  
  public void delete(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

      }
    
    
  }
    
}
