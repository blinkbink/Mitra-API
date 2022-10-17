package id.co.keriss.consolidate.dao;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentSummary;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.util.LogSystem;

public class DocumentsDao {
	Session session;
	DB db;
	Log log;
	
	public DocumentsDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public List<Documents> findByUserto(String eeuser) throws HibernateException {
		    return  session.createQuery("from Documents d where d.eeuser ='"+eeuser+"' order by waktu_buat desc ").list();
	  }
	  
	  public Documents findById(Long id) throws HibernateException {
		    return session.load(Documents.class, id);
	  }
	 
	  public Documents findByUserID(String eeuser, Long id) throws HibernateException {
		    return  (Documents) session.createQuery("from Documents d where d.eeuser ='"+eeuser+"' and d.id='"+id+"' order by waktu_buat desc ").uniqueResult();
	  }
	  
	  public List<Documents> findByUserDocID(Long eeuser, String doc_id) throws HibernateException {
		    return  session.createQuery("from Documents d where d.eeuser ='"+eeuser+"' and d.idMitra='"+doc_id+"' order by d.id desc").list();
	  }
	  
	  public List<Documents> findByUserDocID2(Long eeuser, String doc_id) throws HibernateException {
		    //return  session.createQuery("select d from Documents d, User u, Userdata us where d.idMitra = '"+doc_id+"' and (u.mitra='"+eeuser+"' or us.mitra='"+eeuser+"') and d.eeuser = u.id and u.userdata = us.id order by d.id desc").list();
		    return  session.createQuery("select d from Documents d, User u where d.idMitra = '"+doc_id+"' and u.mitra='"+eeuser+"' and d.delete=false and d.eeuser = u.id order by d.id desc").list();
		    
	  }
	  
	  public List<Documents> findByDocID(String doc_id) throws HibernateException {
		    return  session.createQuery("from Documents d where d.idMitra='"+doc_id+"' and d.delete=false order by d.id desc").list();
	  }
	  
	  public Documents findByUserAndName(String eeuser,String rename) throws HibernateException {
		    return  (Documents) session.createQuery("from Documents d where d.eeuser ='"+eeuser+"' and rename='"+rename+"' order by waktu_buat desc ").uniqueResult();
	  }
	  
	  public DocumentSummary getSummaryDocument(User user) throws HibernateException {
		  DocumentSummary dSum=new DocumentSummary();
		  Iterator it=session.createQuery("select count(id) as num ,status, sign from Documents where userdata='"+user.getUserdata().getId()+"' group by status, sign").list().iterator();
		  
		  while ( it.hasNext() ) {
			 Object[] data=(Object[]) it.next();
			 char status= (char) data[1];
			 boolean sign=(boolean) data[2];
			 System.out.println("data : "+data[1]+" "+data[2]);
			 if(status=='T' && !sign) {
				System.out.println("masuk 1");
				dSum.setWaiting((long)data[0]); 

			 }
			 if(status=='T' && sign) {
					System.out.println("masuk 1");
				dSum.setCompleted((long) data[0]); 
			 }
		  }
		  dSum.setNeedSign(new DocumentsAccessDao(db).getWaitingSignUser(user));
		  return dSum;
	  }
	  
	  public List<Documents> findByDocIdMitra(String docid, Long mitra){
		    try{
		    	String sql = "select d "
		    			+ "from "
		    			+ "Documents d, User u "
		    			+ "where d.eeuser=u.id and d.idMitra = :docid and u.mitra=:mitra";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("docid", docid);
		    	query.setLong("mitra", mitra);
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public List<Documents> findByDocIdMitraDelFalse(String docid, Long mitra){
		    try{
		    	String sql = "select d "
		    			+ "from "
		    			+ "Documents d "
		    			+ "inner join User u ON d.eeuser=u.id "
		    			+ "where d.idMitra = :docid and u.mitra=:mitra and d.delete=false";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("docid", docid);
		    	query.setLong("mitra", mitra);
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  
	  public List<Documents> findByDocIdMitraTrue(String docid, Long mitra){
		    try{
		    	String sql = "select d "
		    			+ "from "
		    			+ "Documents d, User u "
		    			+ "where d.eeuser=u.id and d.idMitra = :docid and u.mitra=:mitra and d.delete=true";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("docid", docid);
		    	query.setLong("mitra", mitra);
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public List<Documents> findByDocIdMitraFalse(String docid, Long mitra){
//		  System.out.print("DOKUMEN IDDDD "+docid);
//		  System.out.print("MITRA IDDDD "+mitra);
		    try{
		    	String sql = "select d "
		    			+ "from "
		    			+ "Documents d, User u "
		    			+ "where d.eeuser=u.id and d.idMitra = :docid and u.mitra=:mitra and d.delete=false order by d.id";
		    	Query query= session.createQuery(sql);
		    	query.setParameter("docid", docid);
		    	query.setLong("mitra", mitra);
		    	return query.list();
		    }catch (Exception e) {
//		    	e.printStackTrace();
	            LogSystem.error(getClass(), e);

		    	return null;
		    	
		    }
	  }
	  
	  public void updateWhere(String document_id, Long idMitra) {
		  Session session = null;
		  DB db=new DB();
		  session=db.open();
			try {
			      Transaction t=session.beginTransaction();
			      String  qString="update Documents d "
							+ "set "
								+ "d.delete=:status "
							+ "where "
								+ "d.idMitra=:document_id and "
								+ "d.eeuser=:idMitra and "
								+ "d.delete='false'";
					Query query=session.createQuery(qString);
					query.setString("document_id", document_id);
					query.setBoolean("status", true);
					query.setLong("idMitra", idMitra);
					query.executeUpdate();
					
					//session.getTransaction().commit();
					t.commit();///
			    } catch (RuntimeException e) {
//			    	log.debug(e);
		            LogSystem.error(getClass(), e);

			    } finally {
					if(session!=null)session.close();
					if(db!=null)db.close();
				}
	  }
	  
	 
	  public  void create(Documents trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
	  }
	  
	  public  Long create2(Documents key) {
		  Long id=null;
	    try {
    	  Transaction tx=session.beginTransaction();
	      id=(Long) session.save(key);
	      tx.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    }
		  return id;
	  }
	  
	  public  void update(Documents trx) {
		  
	    try {
	      Transaction t=session.beginTransaction();
	      session.update(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
            LogSystem.error(getClass(), e);

	    } 
	  }
	  
	  public  void update2(Documents trx) {
		    try {
		      //Transaction t=session.beginTransaction();
		      session.update(trx);
		      //t.commit();
		    } catch (RuntimeException e) {
//		    	log.debug(e);
	            LogSystem.error(getClass(), e);

		    }
		  }
	  
	  public  void delete(Documents trx) {
	    try {
	    
		    Transaction t = null;
	    	if(!session.getTransaction().isActive())
			{
				t = session.beginTransaction();
			}
	    	else
	    	{
	    		t = session.getTransaction();
	    	}
		      
		    session.delete(trx);
		    t.commit();
	    } catch (RuntimeException e) {
            LogSystem.error(getClass(), e);
	    }
	  }

}
