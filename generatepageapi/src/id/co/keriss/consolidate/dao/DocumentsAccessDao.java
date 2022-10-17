package id.co.keriss.consolidate.dao;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.util.LogSystem;

public class DocumentsAccessDao {
	Session session;
	DB db;
	Log log;
	
	public DocumentsAccessDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	  public int clearAkses(Long document) throws HibernateException {
	      Transaction t=session.beginTransaction();
		  int x=session.createQuery("delete from DocumentAccess d where d.document = :docx").setLong("docx", document).executeUpdate();
	      t.commit();

		  return x;
	  }
	  
	  public Long getWaitingSignUser(User u) throws HibernateException {
		  return (Long) session.createQuery("select count(id) from DocumentAccess where (type='sign' or type='initials') and flag='f' and page!= 0 and  (userdata='"+u.getUserdata().getId()+"' or email='"+u.getNick()+"')").uniqueResult();
	     
	  }
	  
	  public Long getWaitingSignUserByDoc(String doc) throws HibernateException {
		  return (Long) session.createQuery("select count(id) from DocumentAccess where (type='sign' or type='initials') and flag='f' and document='"+doc+"'").uniqueResult();
	     
	  }
	  
	  public DocumentAccess findbyId(Long id) throws HibernateException {
		  return (DocumentAccess) session.createQuery("from DocumentAccess d where d.id='"+id+"'").uniqueResult();
	  }
	  
	  public DocumentAccess findbyDocSign(Long id) throws HibernateException {
		  return (DocumentAccess) session.createQuery("from DocumentAccess d where d.document='"+id+"' and (d.type='sign' or d.type='initials') and d.flag='true' limit 1").uniqueResult();
	  }

	  public List<DocumentAccess> findByEEUser(String eeuser) throws HibernateException {
		    return  session.createQuery("from DocumentAccess d where d.eeuser ='"+eeuser+"' order by d.id desc").list();
	  }
	  
	  public List<DocumentAccess> findByEmail(String email) throws HibernateException {
		    return  session.createQuery("from DocumentAccess d where lower(d.email) ='"+email+"' order by d.id desc").list();
	  }
	  
	  public List<DocumentAccess> findOtherDoc(User eeuser) throws HibernateException {
		    return  session.createQuery("select d from Documents dc, DocumentAccess d where dc.id=d.document and dc.eeuser !='"+eeuser.getId()+"' and (d.eeuser ='"+eeuser.getId()+"' or d.email='"+eeuser.getNick()+"') order by d.id desc" ).list();
	  }

	  public List<DocumentAccess> findByDoc(String document) throws HibernateException {
		    return  session.createQuery("from DocumentAccess d where d.document ='"+document+"' order by d.id desc").list();
	  }
	  
	  public List<DocumentAccess> findByDocSign(Long document) throws HibernateException {
		    return  session.createQuery("from DocumentAccess d where d.document ='"+document+"' and (d.type='sign' or d.type='initials') and flag=true order by d.date_sign desc").list();
	  }
	  
	  public boolean findDraft(String document) {
		    Long size=(Long) session.createQuery("select count(id) from DocumentAccess d where d.document ='"+document+"' and type='sign' and page=0 ").uniqueResult();
		    if(size>0) {
		    	return true;
		    }
		    return false;
	  }
	  
	  public DocumentAccess findByDocAndUser(String document, String idUser, String email) {
		  String  qString="from DocumentAccess d where d.document =:doc and  email=:email  and type='sign'";
		  Query query=session.createQuery(qString);
		  if(idUser!=null) {
			  qString="from DocumentAccess d where d.document =:doc and  (eeuser=:idUser or email=:email ) and type='sign'";

			  query=session.createQuery(qString);
			  query.setLong("idUser",Long.valueOf( idUser));  
			   
		  }

		  query.setLong("doc",Long.valueOf( document));  
		  query.setString("email",email);  
		  List ls= query.list();
		  if(ls.size()==0)return null;
		  return  (DocumentAccess) ls.get(0);
	  }
	  
	  public DocumentAccess findByDocAndUserShareSign(String document, String idUser, String email) {
		  String  qString="from DocumentAccess d where d.document =:doc and  email=:email";
		  Query query=session.createQuery(qString);
		  if(idUser!=null) {
			  qString="from DocumentAccess d where d.document =:doc and  (eeuser=:idUser or email=:email )";

			  query=session.createQuery(qString);
			  query.setLong("idUser",Long.valueOf( idUser));  
			   
		  }

		  query.setLong("doc",Long.valueOf( document));  
		  query.setString("email",email);  
		  List ls= query.list();
		  if(ls.size()==0)return null;
		  return  (DocumentAccess) ls.get(0);
	  }
	  
	  public List<DocumentAccess> findDocEEuser(String document, String idUser, String email) throws HibernateException {
		  String  qString="select d from DocumentAccess d where d.document =:doc and  email=:email ";
		  Query query=session.createQuery(qString);
		  if(idUser!=null) {
			  qString="select d from DocumentAccess d ,Documents dc where  dc.id=d.document and  d.document =:doc and  (d.eeuser=:idUser or email=:email  or dc.eeuser=:idUser) order by dc.waktu_buat desc";

			  query=session.createQuery(qString);
			  query.setLong("idUser",Long.valueOf( idUser));  
			   
		  }

		  query.setLong("doc",Long.valueOf( document));  
		  query.setString("email",email);  
		  return query.list();
	  }
	  
	  public List<DocumentAccess> findDocEEuserSign(Long document, Long idUser) throws HibernateException {
		  String  qString="select d from DocumentAccess d where d.document =:doc and  eeuser=:idUser and (type='sign' or type='initials') and flag='false'";
		  Query query=session.createQuery(qString);
		  
		  query.setLong("doc",document);  
		  query.setLong("idUser",idUser);  
		  return query.list();
	  }
	  
	  public List<DocumentAccess> findDocAccessEEuser(String document, String idUser, String email) throws HibernateException {
		  String  qString="select d from DocumentAccess d where d.document =:doc and  email=:email ";
		  Query query=session.createQuery(qString);
		  if(idUser!=null) {
			  qString="select d from DocumentAccess d ,Documents dc where  dc.id=d.document and  d.document =:doc and  (d.eeuser=:idUser or email=:email )";

			  query=session.createQuery(qString);
			  query.setLong("idUser",Long.valueOf( idUser));  
			   
		  }

		  query.setLong("doc",Long.valueOf( document));  
		  query.setString("email",email);  
		  return query.list();
	  }
	  
	  
	  public List<DocumentAccess> findDocAccessHaveSign(String document, Long idUser, String email, Long idDoc) throws HibernateException {
		  String  qString="select da "
		  					+ "from "
		  						+ "DocumentAccess da, "
		  						+ "Documents d "
		  						//+ "User u "
		  					+ "where "
		  						+ "da.document = d.id and "
		  						//+ "da.eeuser=u.id and "
		  						+ "da.document=:idDoc and "
		  						+ "d.idMitra=:doc and "
		  						+ "lower(da.email)=:email and "
		  						//+ "d.eeuser=:idUser and "
		  						+ "(da.type like 'sign' or "
		  						+ "da.type like 'initials') order by da.id asc";
		  Query query=session.createQuery(qString);
		  
		  query.setString("doc", document);  
		  query.setString("email",email);
		  //query.setLong("idUser",idUser);
		  query.setLong("idDoc",idDoc);
		  return query.list();
	  }
	  
	  
	  public List<DocumentAccess> findDocAccessEEuserByMitra(String document, Long idUser, String email, Long idDoc) throws HibernateException {
		  String  qString="select da "
		  					+ "from "
		  						+ "DocumentAccess da, "
		  						+ "Documents d "
		  						//+ "User u "
		  					+ "where "
		  						+ "da.document = d.id and "
		  						//+ "da.eeuser=u.id and "
		  						+ "da.document=:idDoc and "
		  						+ "d.idMitra=:doc and "
		  						+ "lower(da.email)=:email and "
		  						//+ "d.eeuser=:idUser and "
		  						+ "(da.type = 'sign' or "
		  						+ "da.type = 'initials' or da.type ='approval' ) order by  da.sequence_no asc, da.id asc";
		  Query query=session.createQuery(qString);
		  
		  query.setString("doc", document);  
		  query.setString("email",email);
		  //query.setLong("idUser",idUser);
		  query.setLong("idDoc",idDoc);
		  return query.list();
	  }

	

		public List<DocumentAccess> getAllUserDoc(String document, Long idUser, Long idDoc) throws HibernateException {
		String viewOnlyCondition = "da.flag=false and ";
		String  qString="select da "
		  					+ "from "
		  						+ "DocumentAccess da, "
		  						+ "Documents d "
		  						//+ "User u "
		  					+ "where "
		  						+ "da.document = d.id and "
								+ viewOnlyCondition
		  						//+ "da.eeuser=u.id and "
		  						+ "da.document=:idDoc and "
		  						+ "d.idMitra=:doc and "
		  						//+ "d.eeuser=:idUser and "
		  						+ "(da.type like 'sign' or "
		  						+ "da.type like 'initials') order by da.sequence_no asc";
		  Query query=session.createQuery(qString);

		  query.setString("doc", document);
		  //query.setLong("idUser",idUser);
		  query.setLong("idDoc",idDoc);
		  return query.list();
	  }
	  
	 
	  
	  public List<DocumentAccess> findAccessByEEMitraDocID(String userid, String document) throws HibernateException {
		    return  session.createQuery("select d from Documents a, DocumentAccess d where a.id=d.document and a.eeuser='"+userid+"' and a.idMitra ='"+document+"' order by a.id desc").list();
	  }
	  
	  public DocumentAccess findAccessByUserPrf(Long user, Long iddoc, String aksi) throws HibernateException {
		  String  qString="select da "
					+ "from "
						+ "DocumentAccess da "
					+ "where "
						+ "da.eeuser=:iduser and "
						+ "da.type='initials' and "
						+ "da.flag='false' and "
						+ "da.document=:iddoc and "
						+ "da.action=:aksi";
			Query query=session.createQuery(qString);
			
			query.setLong("iduser",user);
			query.setLong("iddoc",iddoc);
			query.setString("aksi", aksi);
			return (DocumentAccess) query.uniqueResult();
	  }
	  
	  public DocumentAccess findAccessByEmailPrf(String email, Long iddoc, String aksi) throws HibernateException {
		  String  qString="select da "
					+ "from "
						+ "DocumentAccess da "
					+ "where "
						+ "lower(da.email)=:email and "
						+ "da.type='initials' and "
						+ "da.flag='false' and "
						+ "da.document=:iddoc and "
						+ "da.action=:aksi";
			Query query=session.createQuery(qString);
			
			//query.setLong("iduser",user);
			query.setLong("iddoc",iddoc);
			query.setString("aksi", aksi);
			query.setString("email", email);
			return (DocumentAccess) query.uniqueResult();
	  }
	  
	  
	  
	  public boolean checkSignLocation(Long document) throws HibernateException {
		     boolean result=true;
		     Query query=session.createQuery("select d from Documents a, DocumentAccess d where a.id=d.document and d.document = :doc and d.page=0 and d.type='sign'");
		     query.setLong("doc", document);
		     List <DocumentAccess> dataRow=(List<DocumentAccess>) query.list();
		     if(dataRow.size()==0) {
		    	 result=false;
		     }
		     if(dataRow.size()>0) {
		    	 if(dataRow.get(0).getPage()==0) result=false;
		    	 if(dataRow.get(0).getPage()==0) result=false;
		     }
		     return result;
	  }

	  public boolean checkSignedDocument(Long document) throws HibernateException {
		     boolean result=true;
		     Query query=session.createQuery("select d from Documents a, DocumentAccess d where a.id=d.document and d.document = :doc and  d.type='sign' and d.flag=false");
		     query.setLong("doc", document);
		     List <DocumentAccess> dataRow=(List<DocumentAccess>) query.list();
		     if(dataRow.size()>0) {
		    	 result=false;
		     }
		     return result;
	  }
	  public  Long create(DocumentAccess documentAccess) {
	    Long id = null;
		try {
	      Transaction t=session.beginTransaction();
	      id = (Long) session.save(documentAccess);
	      t.commit();
	      
	    } catch (RuntimeException e) {
//	    	log.debug(e);
	    	LogManager.getLogger(getClass()).error(e);

	    }
		return id;
	  }
	  
	  public  void update(DocumentAccess documentAccess) {
		    try {
		      Transaction t=session.beginTransaction();
		      session.update(documentAccess);
		      t.commit();
		    } catch (RuntimeException e) {
//		    	log.debug(e);
		    	LogManager.getLogger(getClass()).error(e);

		    }
		  }
  public  void update2(DocumentAccess documentAccess) {
		    try {
		      //Transaction t=session.beginTransaction();
		      session.update(documentAccess);
		      //t.commit();
		    } catch (RuntimeException e) {
//		    	log.debug(e);
		    	LogManager.getLogger(getClass()).error(e);

		    }
		  }

	  public void deleteWhere(Long iddoc) {
		  
			try {
			      session.beginTransaction();
			      String  qString="delete "
							+ "from "
								+ "DocumentAccess "
							+ "where "
								+ "document=:iddoc";
					Query query=session.createQuery(qString);
					query.setLong("iddoc", iddoc);
					query.executeUpdate();
					session.getTransaction().commit();
			    } catch (RuntimeException e) {
//			    	log.debug(e);
			    	LogManager.getLogger(getClass()).error(e);

			    }
	  }
	  
	  public void updateWhere(Long eeuser, String email) {
		  
			try {
			      session.beginTransaction();
			      String  qString="update DocumentAccess "
							+ "set "
								+ "eeuser=:eeuser "
							+ "where "
								+ "email=:email";
					Query query=session.createQuery(qString);
					query.setLong("eeuser", eeuser);
					query.setString("email", email);
					query.executeUpdate();
					
					session.getTransaction().commit();
			    } catch (RuntimeException e) {
//			    	log.debug(e);
			    	LogManager.getLogger(getClass()).error(e);

			    }
	  }
}
