package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Batch;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Transaction;
import id.co.keriss.consolidate.ee.TransactionVO;
import id.co.keriss.consolidate.ee.TransactionDetail;
import id.co.keriss.consolidate.util.DBConnection;
import id.co.keriss.consolidate.util.ReportUtil;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

public class TransactionDetailDao {
	Session session;
	DB db;
	Log log;
	
	public TransactionDetailDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List<?> findAll () throws HibernateException {
        return session.createCriteria(Transaction.class).list();
      }
	  public TransactionDetail lastTrxByTrx(Long trx){
			try{
				TransactionDetail trxDetail = null;
				trxDetail = (TransactionDetail)session.createQuery("select detail from Transaction t , TransactionDetail detail where detail.transaction=t.id and transaction='"+trx+"'").uniqueResult();
			  
			  return trxDetail;
			}
			catch(Exception e){
				return null;
			}
	}	

	  
	  public TransactionDetail findById(Long id) throws HibernateException {
		    return (TransactionDetail)session.load(TransactionDetail.class, id);
	  }
	  
	 
	  
	  public  void deleteTransactionDetail(TransactionDetail transLog) {
		try {
	      session.delete(transLog);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createTransactionDetail(TransactionDetail trans) throws SQLException {
		  String sql="INSERT INTO transactiondetail"+
		             "(transaction, bit48, bit56, bit62, bit63) "+
		             "VALUES (?, ?, ?, ?, ?)";

	      java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);

		    	statement.setLong(1, trans.getTransaction().getId());
		      	statement.setString(2, trans.getBit48());
		      	statement.setString(3, trans.getBit56());
		      	statement.setString(4, trans.getBit62());
		      	statement.setString(5, trans.getBit63());
				System.out.println("sql :" +statement.toString());

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
	  
	  public  void createTransactionDetailui(TransactionDetail trans) {
		    try {
		      session.save(trans);
		      session.beginTransaction().commit();
		    } catch (RuntimeException e) {
		    	session.beginTransaction().rollback();
		    	log.debug(e);
		    }
		  }

	  public  void updateTransactionDetail(TransactionDetail trans) {
	    try {
	      session.update(trans);

	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
}
