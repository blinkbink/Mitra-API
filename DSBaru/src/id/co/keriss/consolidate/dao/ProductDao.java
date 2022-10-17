package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Product;

public class ProductDao {
	Session session;
	DB db;
	Log log;
	
	public ProductDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Product.class).list();
      }
	  
	  public Product findById(Long id) throws HibernateException {
		    return session.load(Product.class, id);
	  }
	
	  public Product findByCard(Long card) throws HibernateException {
		    Product product = null;
		  	Query q = session.createQuery("select p from Product p, Range r " +
		    		"where r.low <=:card and r.high>=:card and r.product=p " 
		    		);
		  	q.setParameter("card", card);
		  	try{
		  			product = (Product)q.uniqueResult();
		  			System.out.println(product.getId());
		  	}catch(NonUniqueResultException multi){
		  		List<Product>products = q.list();
		  		for(Product p:products){
		  			if(!p.getName().equalsIgnoreCase("MASTERCARD")||p.getName().equalsIgnoreCase("VISA"))product=p;
		  		}
		  	}catch(NullPointerException en){
		  		
		  	}
		  	return product;
	  }
	  
	  public Product findByName(String name) throws HibernateException {
		    return (Product) session.createQuery("from Product where name ='"+name+"'").uniqueResult();
	  }
	    
	  public  void deleteTransaction(Product Product) {
		try {
	      session.delete(Product);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createProduct(Product Product) {
	    try {
	      session.save(Product);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateProduct(Product Product) {
	    try {
	      session.update(Product);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
