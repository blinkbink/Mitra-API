package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.util.LogSystem;

public class InvoiceDao {
	Session session;
	DB db;
	Log log;

	public InvoiceDao(DB db) {
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}

	public Invoice findById(Long id) throws HibernateException {
		return session.load(Invoice.class, id);
	}

	public List<Invoice> findByDoc(Long id) {
		try{
	    	String sql = "from Invoice where document = :iddoc";
	    	Query query= session.createQuery(sql);
	    	query.setLong("iddoc", id);
	    	return query.list();
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e);

	    	return null;
	    	
	    }
	}
	
	public void deleteWhere(String inv) {
		  
		try {
		      session.beginTransaction();
		      String  qString="delete "
						+ "from "
							+ "Invoice "
						+ "where "
							+ "kb_invoice=:inv";
				Query query=session.createQuery(qString);
				query.setString("inv", inv);
				query.executeUpdate();
				session.getTransaction().commit();
		    } catch (RuntimeException e) {
//		    	log.debug(e);
	            LogSystem.error(getClass(), e);

		    }
  }
	
	public void delete(Invoice Invoice) {
		try {
			Transaction tx = session.beginTransaction();
			session.delete(Invoice);
			tx.commit();

		} catch (RuntimeException e) {
			log.debug(e);
		}
	}

	public void create(Invoice Invoice) {
		try {
			Transaction tx = session.beginTransaction();
			session.save(Invoice);
			tx.commit();
		} catch (RuntimeException e) {
			log.debug(e);
		}
	}

	public void update(Invoice Invoice) {
		try {
			Transaction tx = session.beginTransaction();
			session.update(Invoice);
			tx.commit();
		} catch (RuntimeException e) {
			log.debug(e);
		}
	}

}
