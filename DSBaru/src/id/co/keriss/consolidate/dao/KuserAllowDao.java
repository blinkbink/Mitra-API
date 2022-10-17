package id.co.keriss.consolidate.dao;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.FormatPdf;
import id.co.keriss.consolidate.ee.Kuser;
import id.co.keriss.consolidate.ee.KuserAllow;
import id.co.keriss.consolidate.ee.LetakTtd;
import id.co.keriss.consolidate.util.LogSystem;
public class KuserAllowDao {
	Session session;
	DB db;
	Log log;
	
	public KuserAllowDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	
	 public KuserAllow allowKuser(Kuser kuser, FormatPdf formatPdf){
		    try{
		    	String sql = "from KuserAllow where id_kuser = :id_kuser and id_format_pdf = :id_format_pdf";
		    	Query query= session.createQuery(sql);
		    	query.setLong("id_kuser", kuser.getId());
		    	query.setLong("id_format_pdf", formatPdf.getId());
		    	return (KuserAllow) query.uniqueResult();
		    }catch (Exception e) {
	            LogSystem.error(getClass(), e);
		    	return null;
		    }
	  }
	 
	  public  void delete(KuserAllow KuserAllow) {
		try {
	    	Transaction tx=session.beginTransaction();
	    	session.delete(KuserAllow);
	    	tx.commit();

		} catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }

	  public  Long create(KuserAllow KuserAllow) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(KuserAllow);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
		  return id;
	  }

	  public  void update(KuserAllow KuserAllow) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(KuserAllow);
	      tx.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);

	    }
	  }
}
