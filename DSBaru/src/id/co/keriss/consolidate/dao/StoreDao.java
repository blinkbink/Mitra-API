package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Store;

public class StoreDao {
	Session session;
	DB db;
	Log log;
	
	public StoreDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Store.class).list();
      }
      
	 
	  public Paging findAll2 (int start, int count) {
		  //Query query = (Query) session.createCriteria(Store.class);
		  Query query = session.createQuery("from Store");
		  return new Paging(query, start, count);
		  
	  }
	  
	  public Long lastPage(){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  
		  Query q = session.createQuery("select count(*) from Store");
		  
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % 10;
		  totalpage = total/10;
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  
	  
	
	  
	  public Store findById(Long id) throws HibernateException {
		    return session.load(Store.class, id);
	  }
	
	  public  void deleteTransaction(Store store) {
		try {
	      session.delete(store);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createStore(Store Store) {
	    try {
	      session.save(Store);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateStore(Store store) {
	    try {
	      session.update(store);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
}
