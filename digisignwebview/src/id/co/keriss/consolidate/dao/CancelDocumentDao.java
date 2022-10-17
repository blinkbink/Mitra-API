package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.Canceled_document;
import id.co.keriss.consolidate.util.LogSystem;

public class CancelDocumentDao {
    private Session session;

    public CancelDocumentDao (DB db) {
        super ();
        this.session = db.session();
    }
    public CancelDocumentDao (Session session) {
        super ();
        this.session = session;
    }
    
    public Canceled_document findById(Long id) throws HibernateException {
	    return session.load(Canceled_document.class, id);
    }
    
    
    
  public void update(Canceled_document cd) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(cd);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void create(Canceled_document cd) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(cd);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void delete(Canceled_document cd) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(cd);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
    
}
