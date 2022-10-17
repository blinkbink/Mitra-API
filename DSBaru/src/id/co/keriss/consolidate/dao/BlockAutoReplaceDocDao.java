package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.BlockAutoReplaceDoc;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.util.LogSystem;

public class BlockAutoReplaceDocDao {
    private Session session;

    public BlockAutoReplaceDocDao (DB db) {
        super ();
        this.session = db.session();
    }
    public BlockAutoReplaceDocDao (Session session) {
        super ();
        this.session = session;
    }
    
    public BlockAutoReplaceDoc findById(Long id) throws HibernateException {
	    return session.load(BlockAutoReplaceDoc.class, id);
    }
    
    public BlockAutoReplaceDoc findByMitra(Long mitra){
	    try{
	    	String sql = "from BlockAutoReplaceDoc where mitra = :mitra";
	    	Query query= session.createQuery(sql);
	    	query.setLong("mitra", mitra);
	    	return (BlockAutoReplaceDoc) query.setMaxResults(1).uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e);

	    	return null;
	    	
	    }
  }
    
  public void update(BlockAutoReplaceDoc rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void create(BlockAutoReplaceDoc rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void delete(BlockAutoReplaceDoc rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
    
}
