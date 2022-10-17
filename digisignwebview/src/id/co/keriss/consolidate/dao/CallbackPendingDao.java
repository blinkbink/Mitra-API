package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.CallbackPending;
import id.co.keriss.consolidate.util.LogSystem;

public class CallbackPendingDao {
    private Session session;

    public CallbackPendingDao (DB db) {
        super ();
        this.session = db.session();
    }
    public CallbackPendingDao (Session session) {
        super ();
        this.session = session;
    }
    
    public CallbackPending findById(Long id) throws HibernateException {
	    return session.load(CallbackPending.class, id);
    }
    
    
    
  public void update(CallbackPending rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void create(CallbackPending rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void delete(CallbackPending rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
    
}
