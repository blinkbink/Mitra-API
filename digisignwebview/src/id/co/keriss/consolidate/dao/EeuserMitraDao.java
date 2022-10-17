package id.co.keriss.consolidate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.EeuserMitra;
import id.co.keriss.consolidate.util.LogSystem;

public class EeuserMitraDao {
    private Session session;

    public EeuserMitraDao (DB db) {
        super ();
        this.session = db.session();
    }
    public EeuserMitraDao (Session session) {
        super ();
        this.session = session;
    }
    
    public EeuserMitra findById(Long id) throws HibernateException {
	    return session.load(EeuserMitra.class, id);
    }
    
    public EeuserMitra findUserPwdMitra(String email, String pwd, Long mitra){
	    try{
	    	String sql = "select e from EeuserMitra e, User u where e.eeuser=u.id and u.nick = :email and e.mitra=:mitra and e.password=:pwd";
	    	Query query= session.createQuery(sql);
	    	query.setParameter("email", email);
	    	query.setLong("mitra", mitra);
	    	query.setParameter("pwd", pwd);
	    	return (EeuserMitra) query.uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e);

	    	return null;
	    	
	    }
  }
    
    public EeuserMitra findByeeUser(Long mitra, Long eeuser){
	    try{
	    	String sql = "select e from EeuserMitra e where e.mitra=:mitra and e.eeuser=:eeuser";
	    	Query query= session.createQuery(sql);
	    	query.setParameter("eeuser", eeuser);
	    	query.setLong("mitra", mitra);
	    	
	    	return (EeuserMitra) query.uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e);

	    	return null;
	    	
	    }
    }
    
  public void update(EeuserMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void create(EeuserMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
  
  public void delete(EeuserMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

      }
    
    
  }
    
}
