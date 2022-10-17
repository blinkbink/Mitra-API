package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.ee.Productbiller;
import id.co.keriss.consolidate.ee.Productbillermerchant;
import id.co.keriss.consolidate.util.DBConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class ProductBillerMerchantDao {
	Session session;
	DB db;
	Log log;
	
	public ProductBillerMerchantDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Productbillermerchant.class).list();
      }
	  
	  public Productbillermerchant findById(Long id) throws HibernateException {
		    return (Productbillermerchant)session.load(Productbillermerchant.class, id);
	  }
	
	
	  public Productbillermerchant getPB(String mid, String feature_code, String Amount) throws HibernateException {
		  try{
			int amt=Integer.parseInt(Amount)/1000;
		    String query="select pm from Productbiller p, Productbillermerchant pm, Merchant m, Feature f " +
		    			 " where p.id=pm.productbiller AND pm.merchant=m.id AND p.feature=f.id AND " +
		    			 " m.mid='"+mid+"' AND f.nominal ="+amt+"AND f.feature_code ='"+feature_code+"'  ";
		    Productbillermerchant p=(Productbillermerchant) session.createQuery(query).uniqueResult();
		    
		    	return p;
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	  
	  public Productbillermerchant getPBByCode(String mid, String Code) throws HibernateException {
		  try{
		    String query="select p from Productbillermerchant p, Merchant m ,Productbiller pb where m.id=p.merchant and pb.id=p.productbiller and m.mid='"+mid+"' and productbillercode='"+Code+"'";
		    Productbillermerchant p=(Productbillermerchant) session.createQuery(query).uniqueResult();
		    
		    	return p;
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	  
	  public int copyPBFromMID(Long from, Long to) throws HibernateException {
			

		    try{
		    	Transaction tx=session.beginTransaction();
			      int stat= session.createQuery("insert into Productbillermerchant (merchant, productbiller) select (select m from Merchant m where id='"+String.valueOf(to)+"'), pm.productbiller from Productbillermerchant pm where pm.merchant='"+String.valueOf(from)+"'").executeUpdate();
			      tx.commit();
		    	return stat; 
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return -1;
		    }
	  }
	  
	  public int deleteAllPBFromMerchant(Long merchant) throws HibernateException {
			

		    try{
		    	Transaction tx=session.beginTransaction();
			      int stat= session.createQuery("delete from Productbillermerchant where merchant='"+String.valueOf(merchant)+"'").executeUpdate();
			      tx.commit();
		    	return stat; 
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return -1;
		    }
	  }
	  
	  /*
	   * menghapus satu produk merchant berdasarkan id product pada merchant tersebut
	   */
	  public int deleteProductMerchant(Long merchant, Long product) throws HibernateException {
			

		    try{
		    	Transaction tx=session.beginTransaction();
			      int stat= session.createQuery("delete from Productbillermerchant where merchant='"+String.valueOf(merchant)+"' AND  productbiller='"+String.valueOf(product)+"'").executeUpdate();
			      tx.commit();
		    	return stat; 
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return -1;
		    }
	  }
	    
	  public  void deleteProductBillerMerchant(Productbillermerchant productBiller) {
		try {
	    	Transaction tx=session.beginTransaction();
	         session.delete(productBiller);
		      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public void createProductBillerMerchantJDBC(Long id_merchant, Long id_pb) throws SQLException {
		  String sql="INSERT INTO productbillermerchant(id, merchant, productbiller) "
		  		+ " VALUES (nextval('productbillermerchant_sequence'), ?, ?)";
	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);

		    	statement.setLong(1, id_merchant);
		      	statement.setLong(2, id_pb);

		      	statement.execute();
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		    
		  }
	  
	  public void deleteProductBillerMerchantJDBC(Long id_merchant, Long id_pb) throws SQLException {
		  String sql="DELETE FROM productbillermerchant WHERE merchant=? and productbiller=? ";
	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);

		    	statement.setLong(1, id_merchant);
		      	statement.setLong(2, id_pb);

		      	statement.execute();
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		    
		  }
	  
	  public  void createProductBillerMerchant(Productbillermerchant productbillermerchant) {
	    try {
	    	Transaction tx=session.beginTransaction();
	        session.save(productbillermerchant);
	        tx.commit();

	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateProductBillerMerchant(Productbillermerchant productbillermerchant) {
	    try {
	      session.update(productbillermerchant);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	public int deleteProductBillerFromMerchant(Productbiller pb) {
		  try{
		    	Transaction tx=session.beginTransaction();
			      int stat= session.createQuery("delete from Productbillermerchant where productbiller='"+pb.getId()+"'").executeUpdate();
			      tx.commit();
		    	return stat; 
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return -1;
		    }	
	}
	  
	  
}
