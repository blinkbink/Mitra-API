package id.co.keriss.consolidate.dao;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Reconciliation;

public class ReconciliationDao {
	Session session;
	DB db;
	Log log;
	
	public ReconciliationDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Reconciliation.class).list();
      }
	  
	  public Paging findAll2 (int start, int count) {
		  Query query = session.createQuery("from Reconciliation as Reconciliation");
		  return new Paging(query, start, count);
	  }
	  
	  public Paging findByDate3(String from, int start, int count){
		  //List trans = null;
		  Date fromDate = dateStart(from);
		  //Date toDate = dateEnd(to);
		  
		  System.out.println(fromDate);

	
		  Query q = session.createQuery("select r from Reconciliation r where " +
			  		"r.dateday <= :fromDate and " +
			  	    ":fromDate <= r.dateday ");;

		  q.setParameter("fromDate", fromDate);
		  System.out.println(q);
		  //q.setParameter("toDate", toDate);
		  //trans=q.list();
		  return new Paging(q, start, count);
	  }
	  
	  
	  public Long lastPage(String from){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  Date fromDate = dateStart(from);
		  //Date toDate = dateEnd(to);
		  
		  System.out.println(fromDate);

	
		  Query q = session.createQuery("select count(*) from Reconciliation r where " +
			  		"r.dateday <= :fromDate and " +
			  	    ":fromDate <= r.dateday ");;

		  q.setParameter("fromDate", fromDate);
		  System.out.println(q);
		  //q.setParameter("toDate", toDate);
		  //trans=q.list();
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
	  
	  
	  public Paging findByDate4(int start, int count){
		  //List trans = null;
	
	
		  Query q = session.createQuery("select r from Reconciliation r where r.dateday='2010-10-28'");

		  return new Paging(q, start, count);
	  }
	  
	  public Date dateStart(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  return dateVal;
	  }
	  
	  public Date dateEnd(String date){
		  Date dateVal = null;
		  
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			dateVal=cal.getTime();
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  return dateVal;
	  }


	  
	  
}
