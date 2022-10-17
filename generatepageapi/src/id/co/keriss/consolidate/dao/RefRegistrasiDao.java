package id.co.keriss.consolidate.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.RefRegistrasi;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;

public class RefRegistrasiDao {
    private Session session;

    public RefRegistrasiDao (DB db) {
        super ();
        this.session = db.session();
    }
    public RefRegistrasiDao (Session session) {
        super ();
        this.session = session;
    }
    
    public RefRegistrasi findById(Long id) throws HibernateException {
	    return (RefRegistrasi)session.load(RefRegistrasi.class, id);
    }
    
  public void update(RefRegistrasi rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

      }
    
    
  }
  
  public void create(RefRegistrasi rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);
      }
    
    
  }
  
  public void delete(RefRegistrasi rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
	    	LogManager.getLogger(getClass()).error(e);

      }
    
    
  }
    
}
