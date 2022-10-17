package id.co.keriss.consolidate.dao;
import java.util.List;

import id.co.keriss.consolidate.ee.Nontaglis;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class NonTaglisDao {
	Session session;
	DB db;
	Log log;
	
	public NonTaglisDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public NonTaglisDao(Session ses){
		super();
		session = ses;
	}
	  public Nontaglis findByNontaglisno(String nontaglisno) throws HibernateException {
		    return (Nontaglis) session.createQuery("from Nontaglis b where id ='"+nontaglisno+"'").uniqueResult();
	  }
	  
	  public List<Nontaglis> findForRecon(String dateFrom, String dateTo) {
		   try{
			   if(dateFrom.equals(dateTo)){
			   return session.createQuery("select p from Nontaglis p,  Batch b, Terminal t, Merchant m, Bank bn" +
		       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
		    		"bn.id=p.bank and b.settledate='"+dateFrom+"' and p.type ='SUCCESS'").list();
			   }
			   else{
				   return session.createQuery("select p from Nontaglis p,  Batch b, Terminal t, Merchant m, Bank bn" +
				       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
				    		"bn.id=p.bank and b.settledate>='"+dateFrom+"' and b.settledate<='"+dateTo+"' and p.type ='SUCCESS'").list();
					   
			   }
			   
		   }catch (Exception e) {
			   	return null;
		   }
	}
	  
	  public  void deleteNontaglis(Nontaglis nontaglis) {
		try {
	      session.delete(nontaglis);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createNontaglis(Nontaglis nontaglis) {
	    try {
	      session.save(nontaglis);

	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	  public void createNontaglisExist(Nontaglis nontaglis) {
		    try {
//		    	String hql = "insert into Postpaid (transaction) " +
//	            "where t0.name123 =:name123";
		    	
		    	String hql="INSERT INTO nontaglis(transaction, pln_ref, switch_ref, subs_id, reg_num, reg_trans, "+ 
		    			   "trans_code, trans_total, pln_bill, admin_charge, minor_total, "+
		    			   "minor_pln, minor_admin, minor_amount) "+
		    			   "VALUES (:id, :pln_ref, :ref, :subsid, :reg_num, :reg_trans, :trans_code, "+
		    			   ":trans_total, :bill , :admin, :minor_total, :minor_pln, :minor_admin, :minor_amount)";
	            
	           Query query = session.createSQLQuery(hql);
	           query.setLong("id", nontaglis.getId());
	           query.setString("pln_ref", nontaglis.getPln_ref());
	           query.setString("ref", nontaglis.getSwitch_ref());
	           query.setString("subsid", nontaglis.getSubs_id());
	           query.setString("reg_num", nontaglis.getReg_num());
	           query.setString("reg_trans", nontaglis.getReg_trans());
	           query.setString("trans_code", nontaglis.getTrans_code());
	           query.setBigDecimal("trans_total", nontaglis.getTrans_total());
	           query.setBigDecimal("bill", nontaglis.getPln_bill());
	           query.setBigDecimal("admin", nontaglis.getAdmin_charge());
	           query.setString("minor_total", nontaglis.getMinor_total());
	           query.setString("minor_pln", nontaglis.getMinor_pln());
	           query.setString("minor_admin", nontaglis.getMinor_admin());
	           query.setString("minor_amount", nontaglis.getMinor_amount());
	           query.executeUpdate();
//	           System.out.println("Sql="+query.getQueryString());
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		  }



	  public  void updateNontaglis(Nontaglis nontaglis) {
	    try {
	      session.update(nontaglis);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
