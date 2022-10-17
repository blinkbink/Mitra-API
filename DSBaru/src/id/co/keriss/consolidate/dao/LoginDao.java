package id.co.keriss.consolidate.dao;

import java.util.List;

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
	    return session.load(Login.class, id);
    }
    
    public Login getUsername(String user) {
      try{
        String sql = "select l from User u, Login l where  (u.nick=:user or l.username=:user) and u.login=l.id ";
        Query query = session.createQuery(sql);          
        query.setParameter("user", user);
        return (Login) query.list().get(0);
      }catch (Exception e) {
            LogSystem.error(getClass(), e);

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
            LogSystem.error(getClass(), e);

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
            LogSystem.error(getClass(), e);

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
              LogSystem.error(getClass(), e);

          return null;
          
        }

      }
    
  public void update(Login lgn) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(lgn);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public  Long create(Login key) {
	  Long id=null;
	  try {
		  Transaction tx=session.beginTransaction();
	    id=(Long) session.save(key);
	    tx.commit();
	  } catch (RuntimeException e) {
	      LogSystem.error(getClass(), e);
	
	//  	log.debug(e);
	  }
	  return id;
  }
  
  public  Long create2(Login key) {
	  Long id=null;
	  try {
		  Transaction tx=session.beginTransaction();
	    id=(Long) session.save(key);
	    tx.commit();
	  } catch (RuntimeException e) {
	      LogSystem.error(getClass(), e);
	      id=(long) 0;
	//  	log.debug(e);
	  }
	  return id;
  }
  
  public List<Login> cekLogin(String userName) {
		try {
			String sql = "select l from Login l where  l.username = :username";
			Query query = session.createQuery(sql);
			query.setParameter("username", userName);
			return query.list();
		} catch (Exception e) {
			LogSystem.error(getClass(), e);
			return null;

		}

	}
  
  public List<Login> cekLogin2(String userName) {
		try {
			String sql = "select l from Login l where  l.username like :username";
			Query query = session.createQuery(sql);
			query.setParameter("username", "%"+userName+"%");
			return query.list();
		} catch (Exception e) {
			LogSystem.error(getClass(), e);
			return null;

		}

	}
}
