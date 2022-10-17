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

public class ProductBillerDao {
	Session session;
	DB db;
	Log log;
	
	public ProductBillerDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Productbiller.class).list();
      }
	  
	  public Productbiller findById(Long id) throws HibernateException {
		    return (Productbiller)session.load(Productbiller.class, id);
	  }
	  
	  public Paging findAll(int start, int count, String name) {
		  //Query query = (Query) session.createCriteria(Store.class);
		  String script = "";
		  
		 
		  if(!name.equals("0"))script = " AND (LOWER(m.productbillercode) like '%"+name+"%' or  LOWER(m.productsuppcode)  like '%"+name+"%' or  LOWER(m.name)  like '%"+name+"%' or  LOWER(s.name)  like '%"+name+"%')";
		  
		  
		 
		  Query query = session.createQuery("select m from Productbiller m , Supplier s where supplier=s.id "+script+" order by m.name, nominal asc");
		  System.out.println("q :"+query);
		  return new Paging(query, start, count);
		  
	  }
	  
	  public Long lastPage(int count, String name){
		  //List trans = null;
		  Long total = new Long(0);
		  Long totalpage = new Long(0);
		  Long totalmod = new Long(0);
		  String script = "";
		  
		  if(!name.equals("0"))script = " AND (LOWER(m.productbillercode) like '%"+name+"%' or  LOWER(m.productsuppcode)  like '%"+name+"%' or  LOWER(m.name)  like '%"+name+"%' or  LOWER(s.name)  like '%"+name+"%')";
	  
		  Query q = session.createQuery("select count(*) from Productbiller m , Supplier s where supplier=s.id  "+script);
		  
		  total = (Long)q.uniqueResult();
		  if(total==null){
			  total=new Long(0);
		  }
		  totalmod = total % count;
		  totalpage = total/count;
		  if(totalmod != 0){
			  totalpage = totalpage + 1;
		  }
		  return totalpage;
	  }
	
	  

	    
	  /*
	   * SELECT column_name(s)
		FROM table1
		LEFT JOIN table2
		ON table1.column_name=table2.column_name;
		
		
		SELECT *
		FROM feature
		LEFT JOIN (select * from Productbiller p, Productbillermerchant pm, Merchant m where p.id=pm.productbiller AND pm.merchant=m.id AND m.id='8') as p
		ON p.feature=feature.id
		ORDER BY feature_code , nominal
		
		query="SELECT Productbiller " +
		    			 "FROM Productbiller p RIGHT JOIN feature , Productbillermerchant pm, Merchant m " +
		    			 "where p.id=pm.productbiller AND pm.merchant=m.id AND m.mid='"+mid+"' " +
		    			 "ORDER BY feature_code , nominal";

	   */
	  public List<Productbiller> getPBFiturAll(String mid) throws HibernateException {
			

		    try{
		    	return session.createQuery("select p from Productbiller p , Feature f , Productbillermerchant pm, Merchant m " +
		    			 "where p.feature=f.id AND p.id=pm.productbiller AND pm.merchant=m.id AND m.mid='"+mid+"' " +
		    			 "ORDER BY f.feature_code").list();
		    
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	  
	  public Productbiller getPBbyCode(String code) throws HibernateException {
		   try{
			String query="select p from Productbiller p where p.productbillercode='"+code+"'";
		    Productbiller p=(Productbiller) session.createQuery(query).uniqueResult();
		 
		    	return p;
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }
	  }
	  
	  public  void deleteTransaction(Productbiller productBiller) {
		try {
	      session.delete(productBiller);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void deleteTransactionCommit(Productbiller productBiller) {
			try {
	          Transaction tx=session.beginTransaction();
		      session.delete(productBiller);
		      tx.commit();
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    }
		  }
	  
	  public  void createProductBiller(Productbiller productBiller) {
	    try {
          Transaction tx=session.beginTransaction();
	      session.save(productBiller);
	      tx.commit();
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    	
	    }
	  }

	  public  void updateProductBiller(Productbiller productBiller) {
	    try {
	      session.update(productBiller);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	
	public void updatePB(Productbiller pb) throws SQLException{
		 String sql="UPDATE productbiller"+
				 " SET name=?, productbillercode=?, feature=?, harga=?, admin=?,"+ 
				 " mitra=?, total=?, nominal=?, productsuppcode=?, supplier=? WHERE productbillercode=?";

      java.sql.Connection conn=DBConnection.getConnection();
      PreparedStatement statement=null;
      Long id=null;
	    try {
	    	statement=conn.prepareStatement(sql);

	    	statement.setString(1, pb.getName());
	      	statement.setString(2, pb.getProductbillercode());
	      	statement.setLong(3, pb.getFeature().getId());
	      	statement.setInt(4, pb.getHarga());
	      	statement.setInt(5, pb.getAdmin());
	      	statement.setInt(6, pb.getMitra());
	      	statement.setInt(7, pb.getTotal());
	      	statement.setInt(8, pb.getNominal());
	      	statement.setString(9, pb.getProductsuppcode());
	      	statement.setLong(10, pb.getSupplier().getId());
	      	statement.setString(11, pb.getProductbillercode());
//			System.out.println("sql :" +statement.toString());

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
	
	public void savePB(Productbiller pb) throws SQLException{
		 String sql="INSERT INTO productbiller("+
            "name, productbillercode, feature, harga, admin, mitra, total, nominal, productsuppcode, supplier)"+
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

     java.sql.Connection conn=DBConnection.getConnection();
     PreparedStatement statement=null;
     Long id=null;
	    try {
	    	statement=conn.prepareStatement(sql);

	    	statement.setString(1, pb.getName());
	      	statement.setString(2, pb.getProductbillercode());
	      	statement.setLong(3, pb.getFeature().getId());
	      	statement.setInt(4, pb.getHarga());
	      	statement.setInt(5, pb.getAdmin());
	      	statement.setInt(6, pb.getMitra());
	      	statement.setInt(7, pb.getTotal());
	      	statement.setInt(8, pb.getNominal());
	      	statement.setString(9, pb.getProductsuppcode());
	      	statement.setLong(10, pb.getSupplier().getId());
//			System.out.println("sql :" +statement.toString());

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
	
	
public List<Productbiller> getAllPBExistOrNot(Merchant m) throws SQLException{
	List<Productbiller> listPb = null;
		String sql="SELECT * "
		 		+ " FROM (select p.id as id, p.name, p.productbillercode,p.productsuppcode, s.name as supname ,nominal ,harga, admin, mitra,total, f.id as feature, feature_name, feature_code from Productbiller p, Supplier s, Feature f where p.feature=f.id and s.id=supplier) pb "
		 		+ " LEFT JOIN ( select p.id as pm_id, p.productbiller from productbillermerchant p , merchant m where p.merchant=m.id AND m.id=?) pm "
		 		+ " ON pb.id=pm.productbiller"
		 		+ " ORDER by pb.feature, pb.name, pb.nominal asc";

    java.sql.Connection conn=DBConnection.getConnection();
    PreparedStatement statement=null;
    Long id=null;
	    try {
	    	statement=conn.prepareStatement(sql);

	    	statement.setLong(1, m.getId());
	       	ResultSet rs=statement.executeQuery();
	       	listPb=new ArrayList<Productbiller>();
	      	while(rs.next()){
	      		Productbiller pb=new Productbiller();
	      		pb.setId(rs.getLong("id"));
	      		pb.setName(rs.getString("name"));
	      		pb.setProductbillercode(rs.getString("productbillercode"));
	      		pb.setNominal(rs.getInt("nominal"));
	      		pb.setHarga(rs.getInt("harga"));
	      		pb.setAdmin(rs.getInt("admin"));
	      		pb.setMitra(rs.getInt("mitra"));
	      		pb.setTotal(rs.getInt("total"));
	      		if(rs.getString("productsuppcode")!=null)pb.setProductsuppcode(rs.getString("productsuppcode"));
	      		else pb.setProductsuppcode("");
	      		pb.setSupplier(new Supplier());
	      		pb.getSupplier().setName(rs.getString("supname"));
	      		
	      		Feature ft=new Feature();
	      		ft.setId(rs.getLong("feature"));
	      		ft.setFeature_name(rs.getString("feature_name"));
	      		ft.setFeature_code(rs.getString("feature_code"));
	      		
	      		pb.setFeature(ft);
	      		if(rs.getObject("pm_id")==null||rs.getObject("pm_id").equals("")){
	      			pb.setExist(false);
	      		}else{
	      			pb.setExist(true);
	      		}
	      		listPb.add(pb);
	      	}
	      	if(listPb.size()==0)listPb=null;
	      	
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
	    	log.debug(e);
		}finally{
			if(statement!=null)statement.close();
			if(conn!=null)conn.close();
		}
	    
	    return listPb;
	}


public List<FeatureVO> listAllDenom(Merchant m) throws SQLException{
	List<FeatureVO> listFeature = null;
	String sql="SELECT * "
			+ " FROM (select feature_code, nominal from Productbiller p , Feature f where p.feature=f.id  group by feature_code, nominal order by feature_code,nominal asc) pb "
			+ " LEFT JOIN ( select p.id as pm_id, pb.productbillercode, pb.nominal, pb.name, f.feature_code as ftr from feature f,productbiller pb,productbillermerchant p , merchant m where p.merchant=m.id AND p.productbiller=pb.id AND f.id=feature AND m.id=?) pm "
			+ " ON pb.feature_code=pm.ftr and pb.nominal=pm.nominal "
			+ " ORDER by pb.feature_code, pb.nominal asc";

    java.sql.Connection conn=DBConnection.getConnection();
    PreparedStatement statement=null;
    Long id=null;
	    try {
	    	statement=conn.prepareStatement(sql);
	    	statement.setLong(1, m.getId());

	       	ResultSet rs=statement.executeQuery();

	       	listFeature=new ArrayList<FeatureVO>();
	      	while(rs.next()){
	      		
	      		FeatureVO ft=new FeatureVO();
	      		ft.setFeature_code(rs.getString("feature_code"));
	      		ft.setNominal(rs.getInt("nominal"));
	      		if(rs.getObject("productbillercode")==null||rs.getObject("productbillercode").equals("")){
	      			ft.setProductbiller("");
	      		}else{
	      			ft.setProductbiller(rs.getString("productbillercode"));
	      		}
	      		listFeature.add(ft);
	      	}
	      	if(listFeature.size()==0)listFeature=null;
	      	
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
	    	log.debug(e);
		}finally{
			if(statement!=null)statement.close();
			if(conn!=null)conn.close();
		}
	    
	    return listFeature;
	}

	
	
	public void saveOrUpdatePB(List<Productbiller> listPBNew) throws SQLException {
		// TODO Auto-generated method stub
		
		for (Productbiller productbiller : listPBNew) {
			if(productbiller.isExist()){
				updatePB(productbiller);
			}else{
				savePB(productbiller);
			}
		}
		
	}
	public List<Productbiller> findPBExist(String parameter, String parameter2) {
		 try{
			String query="select pb from Productbiller pb where productbillercode like '"+parameter+"%' and productbillercode like '%"+parameter2+"' order by nominal asc";
			return session.createQuery(query).list();
		 
		    }catch(Exception e){
		    	e.printStackTrace();
		    	return null;
		    }		
	}
	  
	  
}
