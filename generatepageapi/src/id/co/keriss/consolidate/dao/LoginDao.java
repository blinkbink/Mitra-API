package id.co.keriss.consolidate.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.util.LogSystem;

public class LoginDao {
    private Session session;

    public LoginDao (DB db) {
        super ();
        this.session = db.session();
    }
    public LoginDao (Session session) {
        super ();
        this.session = session;
    }
    
    public Login findById(Long id) throws HibernateException {
	    return (Login)session.load(Login.class, id);
    }
    
    public Login getUsername(String user) {
      try{
        String sql = "select l from User u, Login l where  (u.nick=:user or l.username=:user) and u.login=l.id ";
        Query query = session.createQuery(sql);          
        query.setParameter("user", user);
        return (Login) query.list().get(0);
      }catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);

        return null;
        
      }

    }
    
    public Login getByID(Long user) {
      try{
        String sql = "select l from User u, Login l where  l.id=:user and u.login=l.id ";
        Query query = session.createQuery(sql);          
        query.setParameter("user", user);
        return (Login) query.list().get(0);
      }catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);

        return null;
        
      }

    }
    
    public Login getByUsername(String user) {
      try{
        String sql = "select l from Login l where  l.username=:user";
        Query query = session.createQuery(sql);          
        query.setParameter("user", user);
        return (Login) query.list().get(0);
      }catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);

        return null;
        
      }

    }
    
    public Login getByUsername2(String user) {
        try{
          String sql = "select l from Login l where  l.username=:user";
          Query query = session.createQuery(sql);          
          query.setParameter("user", user);
          return (Login) query.uniqueResult();
        }catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);

          return null;
          
        }

      }
    
  public void update(Login lgn) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(lgn);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

      }
    
    
  }
  
  public  Long create(Login key) {
	  Long id=null;
	  try {
		  Transaction tx=session.beginTransaction();
	    id=(Long) session.save(key);
	    tx.commit();
	  } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);
	
	//  	log.debug(e);
	  }
	  return id;
  }
    
}
