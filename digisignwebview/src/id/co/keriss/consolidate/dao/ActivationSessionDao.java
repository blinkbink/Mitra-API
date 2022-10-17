package id.co.keriss.consolidate.dao;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentSummary;
import id.co.keriss.consolidate.ee.ActivationSession;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.util.LogSystem;

import id.co.keriss.consolidate.ee.SigningSession;//numpang bentar
public class ActivationSessionDao {
	Session session;
	DB db;
	Log log;
	
	public ActivationSessionDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  
	public ActivationSession findByUserto(long preregis,String key, String id) throws HibernateException {
		  try{
			  ActivationSession data=(ActivationSession) session.createQuery("from ActivationSession where preregistration ="+preregis+"  and session_key='"+key+"' and id='"+id+"'").list().get(0);
			  return data;
		  }
		  catch (Exception e) {
			  LogSystem.error(getClass(), e);
	       
		  }
		  return null;

	}
	
	public ActivationSession findSessionId(String id) throws Exception {
		  try{
			  ActivationSession data=(ActivationSession) session.createQuery("from ActivationSession where id='"+id+"'").list().get(0);
			  return data;
		  }
		  catch (Exception e) {
			  LogSystem.error(getClass(), e);
			  throw new Exception("Failed process database");
		  }

	}
	  

	  
	  
	  	  
}
