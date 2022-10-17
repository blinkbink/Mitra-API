package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Feature;
import id.co.keriss.consolidate.ee.FeatureVO;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.NominalAmount;
import id.co.keriss.consolidate.ee.Productbiller;
import id.co.keriss.consolidate.ee.Productbillermerchant;
import id.co.keriss.consolidate.ee.Supplier;
import id.co.keriss.consolidate.util.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class SupplierDao {
	Session session;
	DB db;
	Log log;
	
	public SupplierDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Supplier.class).list();
      }
	  
	  public Supplier findById(Long id) throws HibernateException {
		    return (Supplier)session.load(Supplier.class, id);
	  }
	  
	
	  
	  public  void deleteSupplier(Supplier supplier) {
		try {
	      session.delete(supplier);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteSupplierCommit(Supplier supplier) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(supplier);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createSupplier(Supplier supp) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(supp);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateSupplier(Supplier supplier) {
	    try {
	      session.update(supplier);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	
	
	  
	  
}
