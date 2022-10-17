package id.co.keriss.consolidate.dao;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.DocumentSummary;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Userdata;
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
	  
	 
	  public Documents findByUserID(String eeuser, Long id) throws HibernateException {
		    return  (Documents) session.createQuery("from Documents d where d.eeuser ='"+eeuser+"' and d.id='"+id+"' order by waktu_buat desc ").uniqueResult();
	  }
	  
	  public List<Documents> findByUserDocID(Long eeuser, String doc_id) throws HibernateException {
		    return  session.createQuery("from Documents d where d.eeuser ='"+eeuser+"' and d.idMitra='"+doc_id+"' order by d.id desc").list();
	  }
	  
	  public List<Documents> findByUserDocIDMitra(Long mitra, String doc_id) throws HibernateException {
		    return  session.createQuery("from Documents d where d.eeuser.mitra.id ='"+mitra+"' and d.idMitra='"+doc_id+"' order by d.id desc").list();
	  }
	  
	  public List<Documents> findByUserDocID2(Long eeuser, String doc_id) throws HibernateException {
		    //return  session.createQuery("select d from Documents d, User u, Userdata us where d.idMitra = '"+doc_id+"' and (u.mitra='"+eeuser+"' or us.mitra='"+eeuser+"') and d.eeuser = u.id and u.userdata = us.id order by d.id desc").list();
		    return  session.createQuery("select d from Documents d, User u where d.idMitra = '"+doc_id+"' and u.mitra='"+eeuser+"' and d.eeuser = u.id and d.delete ='false' order by d.id desc").list();
	  }
	  
	  public List<Documents> findByDocID(String doc_id) throws HibernateException {
		    return  session.createQuery("from Documents d where d.idMitra='"+doc_id+"' order by d.id desc").list();
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
	  
	 
	  public  void create(Documents trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.save(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

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
	    	LogManager.getLogger(getClass()).error(e);

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
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }
	  
	  public  void update2(Documents trx) {
		    try {
		      //Transaction t=session.beginTransaction();
		      session.update(trx);
		      //t.commit();
		    } catch (RuntimeException e) {
//		    	log.debug(e);
		    	LogManager.getLogger(getClass()).error(e);

		    }
		  }
	  
	  public  void delete(Documents trx) {
	    try {
	      Transaction t=session.beginTransaction();
	      session.delete(trx);
	      t.commit();
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }
	  
	  public List<Documents> findDocForSigningByMitra(List<String> document, Long mitraID, String email) throws HibernateException {
		  
		  String idListDoc="";
		  for (String data : document) {
			  if(!idListDoc.equals(""))idListDoc="or ";
			  idListDoc+="d.idMitra='"+data+"' ";
		  }
		  
		  String  qString="select d "
		  					+ "from "
		  						+ "DocumentAccess da, "
		  						+ "Documents d "
		  						//+ "User u "
		  					+ "where "
		  						+ "d.eeuser.mitra.id=:mitraID and "
		  						+ "da.document = d.id and "
		  						//+ "da.eeuser=u.id and "
		  						+ "("+idListDoc+") and "
		  						+ "lower(da.email)=:email and "
								+ "da.flag='false' and "
								+ "d.delete='false' and "
		  						//+ "d.eeuser=:idUser and "
		  						+ "(da.type like 'sign' or "
		  						+ "da.type like 'initials') order by da.id asc";
		  Query query=session.createQuery(qString);
		  
		  
		  query.setParameter("email",email);
		  query.setParameter("mitraID",mitraID);
		  return query.list();
	  }
	  
	  public List<String> findDocForSigningByMitraWithSeq(List<String> document, Long idUser, String email, Long mitraID) throws HibernateException {
		  
		  String idListDoc="";
		  for (String data : document) {
			  if(!idListDoc.equals(""))idListDoc+="or ";
			  idListDoc+="d.idMitra='"+data+"' ";
		  }
		  
		  String  qString="select d.idMitra "
		  					+ "from "
		  						+ "DocumentAccess da, "
		  						+ "Documents d "
		  						//+ "User u "
		  					+ "where "
		  						//+ "da.eeuser=u.id and "
		  						+ " d.eeuser.id in (select id from User where mitra.id=:mitraID) and "
		  						+ "("+idListDoc+") and "
		  						+ "lower(da.email)=:email and "
								+ "da.flag='false' and "
								+ "d.delete='false' and "
								+ "((da.sequence_no=d.current_seq and d.sequence is true ) or d.sequence is false) and "
		  						//+ "d.eeuser=:idUser and "
		  						+ "(da.type like 'sign' or "
		  						+ "da.type like 'initials') and "
		  						+ "da.document = d.id  "
		  						+ " order by d.id desc, da.id asc";
		  Query query=session.createQuery(qString);
		  
		  
		  query.setParameter("email",email);

		  query.setParameter("mitraID", mitraID);

		  //query.setLong("idUser",idUser);
		  return query.list();
	  }

	  
	  public List<Documents> findDocForSigningByMitraWithSeqNative(List<String> document, Long idUser, String email, Long mitraID) throws HibernateException {
		  
		  String idListDoc="";
		  List<Documents> docList=new ArrayList();
		  for (String data : document) {
			  if(!idListDoc.equals(""))idListDoc+=" or ";
			  idListDoc+="d.id_doc_mitra='"+data+"' ";
		  }
		  
		  String  qString="select d.id, d.id_doc_mitra "
					+ "from "
						+ "Documents d, "
						+ "doc_access da "
						//+ "User u "
					+ "where "
						//+ "da.eeuser=u.id and "
						+ "("+idListDoc+") and "
						+ " d.eeuser =:userID and "
						+ "da.document = d.id  and "
						+ "lower(da.email)=:email and "
						+ "da.flag='false' and "
						+ "d.delete='false' and "
						+ "((da.sequence_no=d.current_seq and d.sequence is true ) or d.sequence is false) and "
						//+ "d.eeuser=:idUser and "
						+ "(da.type = 'sign' or "
						+ "da.type = 'initials' or da.type = 'approval')"
						+ " order by d.id desc, da.id asc";
		  Query query=session.createNativeQuery(qString);
		  
		  
		  query.setParameter("email",email);
	
		  query.setParameter("userID", mitraID);
		  List<Object []> res=query.list();
		  
		  for (Object[] data : res) {
			  Documents dc=new Documents();
			  dc.setId(((BigInteger) data[0]).longValue());
			  dc.setIdMitra((String) data[1]);
			  docList.add(dc);
		  }
		  
		  

		  //query.setLong("idUser",idUser);
		  return docList;
	  }

	  
	  public List<Documents> findDocForSigningByMitraWithSeqNew(List<String> document, Long idUser, String email, Long mitraID) throws HibernateException {
		  
		  String idListDoc="";
		  List<Documents> docList=new ArrayList();
		  for (String data : document) {
			  idListDoc="d.idMitra='"+data+"' ";
			  String  qString="select d "
	  					+ "from "
	  						+ "Documents d, "
	  						+ "DocumentAccess da "
	  						//+ "User u "
	  					+ "where "
	  						//+ "da.eeuser=u.id and "
	  						+ "("+idListDoc+") and "
	  						+ " d.eeuser.id =:userID and "
	  						+ "da.document = d.id  and "
	  						+ "lower(da.email)=:email and "
							+ "da.flag='false' and "
							+ "d.delete='false' and "
							+ "((da.sequence_no=d.current_seq and d.sequence is true ) or d.sequence is false) and "
	  						//+ "d.eeuser=:idUser and "
	  						+ "(da.type = 'sign' or "
	  						+ "da.type = 'initials' or da.type = 'approval')"
	  						+ " order by d.id desc, da.id asc";
			  Query query=session.createQuery(qString);
			  
			  
			  query.setParameter("email",email);
		
			  query.setParameter("userID", mitraID);
			  List res=query.list();
			  if(res.size()>0)docList.add((Documents) res.get(0));
		  }
		  
		  

		  //query.setLong("idUser",idUser);
		  return docList;
	  }

	  
	  public List<String> findDocHaveSigned(List<String> document, Long idUser, String email, Long mitraID) throws HibernateException {
		  
		  String idListDoc="";
		  for (String data : document) {
			  if(!idListDoc.equals(""))idListDoc+="or ";
			  idListDoc+="d.idMitra='"+data+"' ";
		  }
		  
		  String  qString="select d.idMitra "
		  					+ "from "
		  						+ "DocumentAccess da, "
		  						+ "Documents d "
		  						//+ "User u "
		  					+ "where "
	  						+ "d.eeuser.id in (select id from User where mitra.id=:mitraID) and "
		  						//+ "da.eeuser=u.id and "
		  						+ "("+idListDoc+") and "
		  						+ "da.document = d.id and "
		  						+ "lower(da.email)=:email and "
								+ "da.flag='true' and "
								+ "d.delete='false' and "
								+ "((da.sequence_no<=d.current_seq and d.sequence is true ) or d.sequence is false) and "
		  						//+ "d.eeuser=:idUser and "
		  						+ "(da.type = 'sign' or "
		  						+ "da.type = 'initials' or da.type = 'approval') order by da.id asc";
		  Query query=session.createQuery(qString);
		  
		  
		  query.setParameter("email",email);

		  query.setParameter("mitraID", mitraID);

		  //query.setLong("idUser",idUser);
		  return query.list();
	  }
	  
}
