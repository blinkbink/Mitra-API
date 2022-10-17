package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.LogSystem;

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
	    return session.load(TokenMitra.class, id);
    }
    
    public TokenMitra findByToken(String token) throws Exception{
	    try{
	    	String sql = "from TokenMitra where lower(token) = :token and status_aktif='true'";
	    	Query query= session.createQuery(sql);
	    	query.setString("token", token);
	    	return (TokenMitra) query.setMaxResults(1).uniqueResult();//return bisa ada isinya atau null
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e);
            throw new Exception("Failed check token to database", e);
//	    	return null;
	    }
    }
    
    public TokenMitra findByMitra(long mitra){
	    try{
	    	String sql = "from TokenMitra where mitra = :mitra and status_aktif='true'";
	    	Query query= session.createQuery(sql);
	    	query.setLong("mitra", mitra);
	    	return (TokenMitra) query.setMaxResults(1).uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e);

	    	return null;
	    	
	    }
    }
    
  public void update(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void create(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void delete(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
    
}
