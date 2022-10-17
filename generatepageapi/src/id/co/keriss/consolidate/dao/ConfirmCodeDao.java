package id.co.keriss.consolidate.dao;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.Userdata;

public class ConfirmCodeDao {
	Session session;
	DB db;
	Log log;
	
	public ConfirmCodeDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	 
	public ConfirmCodeDao(Session s){
		super();
		session = s;
	}
	  
	  public ConfirmCode findById(Long id) throws HibernateException {
		    return (ConfirmCode)session.load(ConfirmCode.class, id);
	  }

	  public ConfirmCode findByNewCode(String code,String nohp) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where code ='"+code+"' and msisdn='"+nohp+"' and status='no'").uniqueResult();
	  }
	  
	  
	  public ConfirmCode findByPhone(String nohp) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where msisdn='"+nohp+"' and status='no'").setMaxResults(1).uniqueResult();
	  }
	  
	  
	  public ConfirmCode findByUserID(String code,long userdata) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where code ='"+code+"' and userdata='"+userdata+"'").uniqueResult();
	  }
	  
	  public ConfirmCode findByUserID(long userdata) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where userdata='"+userdata+"' and status ='yes'" ).list().get(0);
	  }
	  
	  public ConfirmCode findLastByUserID(long userdata) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where userdata='"+userdata+"' and status ='no' order by id desc" ).list().get(0);
	  }
	  
	  public ConfirmCode findLastByEEUser(long eeuser) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where eeuser='"+eeuser+"' and status ='no' order by id desc" ).list().get(0);
	  }
	  
	  public ConfirmCode findByUserEmail(String user_id) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where user_id LIKE '%<"+user_id+">%' AND jenis_ConfirmCode='PS' order by id desc").list().get(0);
	  }
	  
	  public ConfirmCode getPrivByUserEmail(String user_id) throws HibernateException {
		    return (ConfirmCode) session.createQuery("from ConfirmCode where user_id LIKE '%<"+user_id+">%' AND jenis_ConfirmCode='PV' order by id desc").list().get(0);
	  }
	  
	  public List<ConfirmCode> findByUser(String user) throws HibernateException {
		    return session.createQuery("select k from ConfirmCode k where userdata ='"+user+"'").list();
	  }
	  
	  public ConfirmCode getLastOTP(Long idUser) throws HibernateException {
		  String qString = "from ConfirmCode where eeuser=:user and status='no' order by waktu_buat desc";
		  Query query = session.createQuery(qString);
		  query.setMaxResults(1);
		  query.setLong("user", idUser);
		  return (ConfirmCode) query.uniqueResult();
	  }
	  
	  public  void delete(ConfirmCode ConfirmCode) {
		try {
	    	Transaction tx=session.beginTransaction();
	      session.delete(ConfirmCode);
	      tx.commit();

		} catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void create(ConfirmCode ConfirmCode) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.save(ConfirmCode);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void update(ConfirmCode ConfirmCode) {
		  System.out.println("code otp="+ConfirmCode.getCode());
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(ConfirmCode);
	      tx.commit();
	      System.out.println("update pak eko");
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
