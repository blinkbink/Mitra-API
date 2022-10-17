package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.RefRegistrasi;
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
	    return session.load(RefRegistrasi.class, id);
    }
    
  public void update(RefRegistrasi rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void create(RefRegistrasi rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void delete(RefRegistrasi rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
    
}
