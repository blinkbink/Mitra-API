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
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.util.LogSystem;

import id.co.keriss.consolidate.ee.SigningSession;//numpang bentar
public class SigningSessionDao {
	Session session;
	DB db;
	Log log;
	
	public SigningSessionDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  
	public SigningSession findSession(String id, String key) throws HibernateException {
		  try{
			  SigningSession data=(SigningSession) session.createQuery("from SigningSession where id ='"+id+"' and session_key='"+key+"' ").uniqueResult();
			  return data;
		  }
		  catch (Exception e) {
			  LogSystem.error(getClass(), e);
	       
		  }
		  return null;
	}
	
	public SigningSession findSessionId(String id) throws Exception {
		  try{
			  SigningSession data=(SigningSession) session.createQuery("from SigningSession where id ='"+id+"'").uniqueResult();
			  return data;
		  }
		  catch (Exception e) {
			  LogSystem.error(getClass(), e);
			  throw new Exception("Failed process database");
		  }
//		  return null;
	}
	  

	  
	  
	  	  
}
