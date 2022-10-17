package id.co.keriss.consolidate.dao;
import java.util.List;

import id.co.keriss.consolidate.ee.Postpaid;
import id.co.keriss.consolidate.ee.Prepaid;
import id.co.keriss.consolidate.ee.Productbiller;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class PrepaidDao {
	Session session;
	DB db;
	Log log;
	
	public PrepaidDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public PrepaidDao(Session ses){
		super();
		session = ses;
		
	}
	
	 public Prepaid findById(Long id) throws HibernateException {
		    return (Prepaid)session.load(Prepaid.class, id);
	  }
	 
	  public Prepaid findByPrepaidno(String prepaidno){
		    try{
		    	return (Prepaid) session.createQuery("from Prepaid b where id ='"+prepaidno+"'").uniqueResult();
		    }catch (Exception e) {
		    	return null;
		    }
	  }
	  
	  public Prepaid findByBatchAndTraceNumber(Long batch,int tracenumber){
		    try{
		    	return (Prepaid) session.createQuery("from Prepaid b where batch ='"+batch+"' and tracenumber='"+tracenumber+"'").uniqueResult();
		    }catch (Exception e) {
		    	return null;
		    }
	  }
	  
	  public List<Prepaid> findForRecon(String dateFrom, String dateTo) {
		   try{
			   
			   if(dateFrom.equals(dateTo)){
			   return session.createQuery("select p from Prepaid p,  Batch b, Terminal t, Merchant m, Bank bn" +
		       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
		    		"bn.id=p.bank and b.settledate='"+dateFrom+"' and p.type ='SUCCESS'").list();
			   }else{
				   return session.createQuery("select p from Prepaid p,  Batch b, Terminal t, Merchant m, Bank bn" +
				       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
				    		"bn.id=p.bank and b.settledate>='"+dateFrom+"' and b.settledate<='"+dateTo+"' and p.type ='SUCCESS'").list();
					   
			   }
			   
		   }catch (Exception e) {
			   	return null;
		   }
	}
	  
	  public  void deletePrepaid(Prepaid prepaid) {
		try {
	      session.delete(prepaid);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createPrepaid(Prepaid prepaid) {
	    try {
	      session.save(prepaid);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }
	  
	
	  
	  public  void createPrepaidExist(Prepaid prepaid) {
		    try {
//		    	String hql = "insert into Postpaid (transaction) " +
//	            "where t0.name123 =:name123";
		    	
		    	String hql="  INSERT INTO prepaid( "+
	            "transaction, pln_ref, switch_ref, no_meter, admin_charge, materai, "+
	            "ppn, ppj, angsuran, power_purchase, power_kwh, token, minor_amount, "+
	            "minor_admin, minor_materai, minor_ppn, minor_ppj, minor_angsuran, "+
	            "minor_purchase, minor_kwh, subs_id,segment, daya, name) "+
	            "VALUES (:id, :pln_ref, :ref, :meter, :admin, :materai, "+
	            ":ppn, :ppj, :angsuran, :pPurchase, :pKwh, :token, :minor_amount, "+
	            ":minor_admin, :minor_materai, :minor_ppn, :minor_ppj, :minor_angsuran, "+
	            ":minor_purchase, :minor_kwh, :subs_id, :segment, :daya, :name)";
	            
	           Query query = session.createSQLQuery(hql);
	           query.setLong("id", prepaid.getId());
	           query.setString("pln_ref", prepaid.getPln_ref());
	           query.setString("ref", prepaid.getSwitch_ref());
	           query.setString("meter", prepaid.getNo_meter());
	           query.setBigDecimal("admin", prepaid.getAdmin_charge());
	           query.setBigDecimal("materai", prepaid.getMaterai());
	           query.setBigDecimal("ppn", prepaid.getPpn());
	           query.setBigDecimal("ppj", prepaid.getPpj());
	           query.setBigDecimal("angsuran", prepaid.getAngsuran());
	           query.setBigDecimal("pPurchase", prepaid.getPower_purchase());
	           query.setBigDecimal("pKwh", prepaid.getPower_kwh());
	           query.setString("token", prepaid.getToken());
	           query.setString("minor_amount", prepaid.getMinor_amount());
	           query.setString("minor_admin", prepaid.getMinor_admin());
	           query.setString("minor_materai", prepaid.getMinor_materai());
	           query.setString("minor_ppn", prepaid.getMinor_ppn());
	           query.setString("minor_ppj", prepaid.getMinor_ppj());
	           query.setString("minor_angsuran", prepaid.getMinor_angsuran());
	           query.setString("minor_purchase", prepaid.getMinor_purchase());
	           query.setString("minor_kwh", prepaid.getMinor_kwh());
	           query.setString("subs_id", prepaid.getSubs_id());
	           query.setString("segment", prepaid.getSegment());
	           query.setString("name", prepaid.getName());
	           query.setInteger("daya", prepaid.getDaya());
	           query.executeUpdate();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		  }


	  public  void updatePrepaid(Prepaid prepaid) {
	    try {
	      session.update(prepaid);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
