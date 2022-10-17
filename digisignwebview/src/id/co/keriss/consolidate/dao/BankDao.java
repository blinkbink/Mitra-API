package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.Range;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class BankDao {
	Session session;
	DB db;
	Log log;
	
	public BankDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<Bank> findAll () throws HibernateException {
		  Query query = session.createQuery("select bank from Bank bank");
          return query.list();
      }
	  
	  public Paging findAll2 (int start, int count, String search) {
		  try{
			  
		  String script = "";
		  
		  if(!search.equals("0")){
			  script = "where lower(b.bank_code) like lower('%"+search+"%') OR lower(b.name) like lower('%"+search+"%') ";
		  }
	      Query query = session.createQuery("from Bank b "+script);
		  return new Paging(query, start, count);
		  }catch (Exception e) {
			// TODO: handle exception
			  return null;
		}
	  }
	  
	  public Long lastPage(int count, String search){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  String script = "";

		  if(!search.equals("0")){
			  script = "where lower(b.bank_code) like lower('%"+search+"%') OR lower(b.name) like lower('%"+search+"%') ";
		  }
		  Query q = session.createQuery("select count(*) from Bank b "+script);
		  
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % (count-1);
		  totalpage = total/(count-1);
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	  
	  public Bank findById(Long id) throws HibernateException {
		    return session.load(Bank.class, id);
	  }
	
	  public Bank findByCard(Long card) throws HibernateException {
		  	System.out.println("Suspect, with card no : "+card);
		    Bank bank = findById(new Long(10));
		  	Query q = session.createQuery("select b from Bank b, Product p, Range r " +
		    		"where r.low <= :card and r.high>=:card and r.product=p and p.bank=b" 
		    		);
		    q.setParameter("card", card);
		    try{
	  			bank = (Bank)q.uniqueResult();
	  			System.out.println("Result bank = "+bank.getName());
		    }catch(NonUniqueResultException multi){
		    	List<Bank>banks = q.list();
		    	for(Bank b:banks){
		    		if(b.isAquirer())bank=b;
		    	}
		    	System.out.println("Result bank = "+bank.getName());
		    }catch(NullPointerException en){
		    	System.out.println("Result bank = NULL ");
		    }
		    return bank;
		    
	  }
	  
	  public Bank findByName(String name) throws HibernateException {
		    return (Bank) session.createQuery("from Bank where name ='"+name+"'").uniqueResult();
	  }
	  
	  public Bank findByBankCode(String code) throws HibernateException {
		  try{  
			  return (Bank) session.createQuery("from Bank where bank_code ='"+code+"'").uniqueResult();
		  }
		  catch (HibernateException e) {
			// TODO: handle exception
			  return null;
		  }
	   }
	  
	  public List<Bank> getListBank() {
		  try{  
			  return  session.createQuery("from Bank where name !='-' and bank_code is not null and rekening is not null ").list();		  }
		  catch (HibernateException e) {
			// TODO: handle exception
			  return null;
		  }
	   }
	  
	  public Boolean onus(String nii,String scardno) throws HibernateException{
		  Boolean onus=false;
		  Long cardno = Long.parseLong(scardno);System.out.println("Value of card :"+cardno);
		  Query q = session.createQuery("from Range r where r.low<=:cardno and :cardno<=r.high");
		  q.setParameter("cardno", cardno);
		  try{
			  Range range=(Range) q.uniqueResult();		  
			  onus=range.getProduct().getBank().isAquirer();		  
		  }catch(NonUniqueResultException multi){
			  List<Range>ranges = q.list();
			  for(Range range:ranges){
				  if(range.getProduct().getName().equalsIgnoreCase("MASTERCARD")||range.getProduct().getName().equalsIgnoreCase("VISA")){					  
				  }else{
					  onus=range.getProduct().getBank().isAquirer();
				  }
			  }
		  }catch(NullPointerException en){
			  System.out.println("Result range = NULL ");
		  }
		  return onus;
	  }
	  
	  public  void deleteBank(Bank bank) {
		try {
	      session.delete(bank);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createBank(Bank bank) {
	    try {
	      session.save(bank);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateBank(Bank bank) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(bank);
	      tx.commit();
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
