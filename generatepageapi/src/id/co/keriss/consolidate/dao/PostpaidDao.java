package id.co.keriss.consolidate.dao;
import java.sql.SQLException;
import java.util.List;

import id.co.keriss.consolidate.ee.Postpaid;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class PostpaidDao {
	Session session;
	DB db;
	Log log;
	
	public PostpaidDao(DB db){
		super();
		session = db.session();
		this.db = db;
	}
	public PostpaidDao(Session ses){
		super();
		session = ses;
		
	}
	  public Postpaid findByPostpaidno(String postpaidno) throws HibernateException {
		    return (Postpaid) session.createQuery("from Postpaid b where id ='"+postpaidno+"'").uniqueResult();
	  }
	  
	  public List<Postpaid> findForRecon(String dateFrom, String dateTo) {
		   try{
			   
			   if(dateFrom.equals(dateTo)){
				   return session.createQuery("select p from Postpaid p,  Batch b, Terminal t, Merchant m, Bank bn" +
			       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
			    		"bn.id=p.bank and b.settledate='"+dateTo+"' and p.type ='SUCCESS'").list();
			   }
			   else{
				   return session.createQuery("select p from Postpaid p,  Batch b, Terminal t, Merchant m, Bank bn" +
				       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
				    		"bn.id=p.bank and b.settledate>='"+dateFrom+"' and b.settledate<='"+dateTo+"' and p.type ='SUCCESS'").list();
				 
			   }
			   
		   }catch (Exception e) {
			   	return null;
		   }
	}
	  
	  public  void deletePostpaid(Postpaid postpaid) {
		try {
	      session.delete(postpaid);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createPostpaid(Postpaid postpaid) {
	    try {
	    	  session.save(postpaid);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  public  void createPostpaidExist(Postpaid postpaid) {
		    try {
//		    	String hql = "insert into Postpaid (transaction) " +
//	            "where t0.name123 =:name123";
		    	
		    	String hql="INSERT INTO postpaid(transaction, switch_ref, subs_id, bill_period, bill_pay, "+ 
		    				"electric_bill, bill_amount, incentive, ppn, penalty_fee, admin_charge)"+
		    				" VALUES (:id, :ref, :subsid, :bill_period, :bill, :electric_bill, :bill_amount, :incentive, :ppn, :penalty, :admin)";
	            
	           Query query = session.createSQLQuery(hql);
	           query.setLong("id", postpaid.getId());
	           query.setString("ref", postpaid.getSwitch_ref());
	           query.setString("subsid", postpaid.getSubs_id());
	           query.setString("ref", postpaid.getSubs_id());
	           query.setString("bill_period", postpaid.getBill_period());
	           query.setString("bill", postpaid.getBill_pay());
	           query.setLong("electric_bill", postpaid.getElectric_bill());
	           query.setLong("bill_amount", postpaid.getBill_amount());
	           query.setLong("incentive", postpaid.getIncentive());
	           query.setLong("ppn", postpaid.getPpn());
	           query.setLong("penalty", postpaid.getPenalty_fee());
	           query.setLong("admin", postpaid.getAdmin_charge());
	           query.executeUpdate();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		  }


	  public  void updatePostpaid(Postpaid postpaid) {
	    try {
	      session.update(postpaid);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
