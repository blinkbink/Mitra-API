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
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.util.LogSystem;

public class DocSigningSessionDao {
	Session session;
	DB db;
	Log log;
	
	public DocSigningSessionDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  
	public DocSigningSession findSession1(String ss, long did) throws HibernateException {
		  try{
			  DocSigningSession data=(DocSigningSession) session.createQuery("from DocSigningSession where signingSession ='"+ss+"' and document='"+did+"' ").uniqueResult();
			  return data;
		  }
		  catch (Exception e) {
			  LogSystem.error(getClass(), e);
	       
		  }
		  return null;

	}
	  

	  
	  
	  	  
}
