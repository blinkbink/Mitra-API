package id.co.keriss.consolidate.dao;

public class PulsaDao {
/*	Session session;
	DB db;
	Log log;
	
	public PulsaDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public PulsaDao(Session ses){
		super();
		session = ses;
		
	}
	  public PulsaBAK findByPulsano(String pulsa){
		    try{
		    	return (PulsaBAK) session.createQuery("from Pulsa b where id ='"+pulsa+"'").uniqueResult();
		    }catch (Exception e) {
		    	return null;
		    }
	  }
	  
	  public List<PulsaBAK> findForRecon(String dateFrom, String dateTo) {
		   try{
			   
			   if(dateFrom.equals(dateTo)){
			   return session.createQuery("select p from Pulsa p,  Batch b, Terminal t, Merchant m, Bank bn" +
		       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
		    		"bn.id=p.bank and b.settledate='"+dateFrom+"' and p.type ='SUCCESS'").list();
			   }else{
				   return session.createQuery("select p from Pulsa p,  Batch b, Terminal t, Merchant m, Bank bn" +
				       		" where p.batch=b.id and b.terminal=t.id and t.merchant=m.id and " +
				    		"bn.id=p.bank and b.settledate>='"+dateFrom+"' and b.settledate<='"+dateTo+"' and p.type ='SUCCESS'").list();
					   
			   }
			   
		   }catch (Exception e) {
			   	return null;
		   }
	}
	  
	  
	  public PulsaBAK findTrxForAdvice(String tid, String mid, BigDecimal amt, String transtype, int trace){
			try{
			  PulsaBAK trx = null;
			  System.out.println("select tr from Batch b, Terminal t, Merchant m, Pulsa tr " +
				  			"where b.terminal=t and " +
				  			"t.merchant=m and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and b.settled=false and " +
				  			"tr.transactiontype='"+transtype+"' and " +
				  			"tr.amount="+amt+" and tr.tracenumber='"+trace+"'");
			  trx = (PulsaBAK)session.createQuery("select tr from Batch b, Terminal t, Merchant m, Pulsa tr " +
				  			"where b.terminal=t.id and " +
				  			"t.merchant=m.id and " +
				  			"t.tid = '"+tid+"' and " +
				  			"m.mid = '"+mid+"' and " +
				  			"tr.batch=b.id and b.settled=false and " +
				  			"tr.transactiontype='"+transtype+"' and " +
				  			"tr.amount="+amt+" and tr.tracenumber='"+trace+"'").uniqueResult();
			  
			  return trx;
			}
			catch(Exception e){
				return null;
			}
	}	
	  
	  public  void deletePulsa(PulsaBAK pulsa) {
		try {
	      session.delete(pulsa);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createPulsa(PulsaBAK pulsa) {
	    try {
	      session.save(pulsa);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updatePulsa(PulsaBAK pulsa) {
	    try {
	      session.update(pulsa);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  public void createPulsaExist(PulsaBAK pulsa) {
		    try {
//		    	String hql = "insert into Postpaid (transaction) " +
//	            "where t0.name123 =:name123";
		    	
		    	String hql="INSERT INTO pulsa(transaction, msisdn, sn, pc, kode, harga) "
		    			+ " VALUES (:id, :msisdn, :sn, :pc, :kode, :harga);";
	            
	           Query query = session.createSQLQuery(hql);
	           query.setLong("id", pulsa.getId());
	           query.setString("msisdn", pulsa.getMsisdn());
	           query.setString("sn", pulsa.getSn());
	           query.setString("pc", pulsa.getProductcode());
	           query.setString("kode", pulsa.getKode());
	           query.setString("harga", pulsa.getHarga());
	           query.executeUpdate();
//	           System.out.println("Sql="+query.getQueryString());
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		  }

	  
	*/  
}
