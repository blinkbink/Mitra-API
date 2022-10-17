package id.co.keriss.consolidate.dao;
import java.util.Iterator;
import java.util.List;

import javax.persistence.TemporalType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentSummary;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Payment;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.ReportUtil;

public class PaymentDao {
	Session session;
	DB db;
	Log log;
	
	public PaymentDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public List<Payment> findByUser(String eeuser) throws HibernateException {
		    return  session.createQuery("from Payment d where d.eeuser ='"+eeuser+"' order by date_request desc ").list();
	  }
	  
	  public Payment findByID(String id) throws HibernateException {
		  try {
			  Payment p=(Payment) session.createQuery("from Payment d where d.id ='"+id+"' order by date_request desc ").uniqueResult();
			  return p;
		  }
		  catch (Exception e) {
			// TODO: handle exception
		    	LogManager.getLogger(getClass()).error(e);

		}
		  return null;
	  
	  }
	  
	  
	 
	  public Payment findByUserID(String eeuser, Long id) throws HibernateException {
		  try {
			  Payment pay = (Payment) session.createQuery("from Payment d where d.eeuser ='"+eeuser+"' and d.id='"+id+"' order by date_request desc ").uniqueResult();   
			  return pay;
		  }catch (Exception e) {
			// TODO: handle exception
		    	LogManager.getLogger(getClass()).error(e);

		}
		  return null;
	  }
	  
	  public Long getAmount(String code, Long Harga) throws HibernateException {
		    String q="select d from Payment d where d.product_code = :code  and date_request > :date order by date_request desc ";
			Query query= session.createQuery(q);  
			 

			  query.setParameter("code", code);
			  query.setParameter("date", ReportUtil.today());
			  
			  List<Payment> lP=query.list();
			  if(lP.size()>0) {
				  Long amt=lP.get(0).getAmount_original();
				  Long codAmt=amt-Harga;
				  if(codAmt+1<1000) {
					  return amt+1;
				  }
			  }
			  return Harga;
		    
	  }
	
	 
	  public  void create(Payment trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }
	  
	  public  void update(Payment trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.update(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }
	  
	  public  void delete(Payment trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.delete(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }

	  
}
