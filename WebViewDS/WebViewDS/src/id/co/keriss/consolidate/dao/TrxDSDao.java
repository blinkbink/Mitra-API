package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.ee.TrxDs;
import id.co.keriss.consolidate.util.LogSystem;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class TrxDSDao {
	Session session;
	DB db;
	Log log;
	
	public TrxDSDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public List<TrxDs> findByto(String no) throws HibernateException {
		    return  session.createQuery("from TrxDs b where b.msg_to ='"+no+"' order by msg_time desc ").list();
	  }
	 
	  public  void create(TrxDs trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }

	  
}
