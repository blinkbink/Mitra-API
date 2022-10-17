package id.co.keriss.consolidate.dao;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.ee.action.ContextConstants;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.StatusTrxLog;
import id.co.keriss.consolidate.util.DBConnection;

public class StatusTrxLogDao implements ContextConstants{
	Session session;
	DB db;
	Log log;
	
	public StatusTrxLogDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	
	public Long insertSTL(StatusTrxLog stl, Long userid) throws SQLException {
		  Long no=null;
		  String sql="INSERT INTO statustrxlog( "
		  		+ "oldsn, oldprice, oldstatus, information, by) "
		  		+ "VALUES (?, ?, ?, ?, ?)";
          java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
	      Statement st=null;
	      Long id=null;
		    try {
		    	statement=conn.prepareStatement(sql);
	
		    	statement.setString(1, stl.getOldsn());
		    	statement.setBigDecimal(2, stl.getOldprice());
		    	statement.setString(3, stl.getOldstatus());
		    	statement.setString(4, stl.getInformation());
		    	statement.setLong(5, userid);
		    	statement.setLong(6, stl.getTransaction());

		      	if(statement.executeUpdate()>0){
			    	st=conn.createStatement();
			      	ResultSet rs=st.executeQuery("SELECT last_value from statustrxlog_sequence");
					 while(rs.next()){
						 no=rs.getLong(1);
						 System.out.println("data-in:"+no);
					 }
		      	}
		      	
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(st!=null)st.close();
				if(conn!=null)conn.close();
			}
		   return no;
	  
	}
	
	public List<StatusTrxLog> getStatusTrxLog(Long id) throws SQLException {
		List<StatusTrxLog> listSt = new ArrayList<StatusTrxLog>();
		  java.sql.Connection conn=DBConnection.getConnection();
	      PreparedStatement statement=null;
		    try {
		    	String sql="";
		       sql="Select s.id, s.oldprice, s.oldsn, s.oldstatus, s.information, s.transaction, s.datetime, u.name as user from statustrxlog s , eeuser u where transaction=? and s.user=u.id order by datetime desc";
	    	   statement=conn.prepareStatement(sql);
		       statement.setLong(1, id);
		    	ResultSet rs=statement.executeQuery();
		    	
		    	while(rs.next()){
		    		StatusTrxLog st=new StatusTrxLog();
		    		st.setId(rs.getLong("id"));
		    		st.setOldprice(rs.getBigDecimal("oldprice"));
		    		st.setOldsn(rs.getString("oldsn"));
		    		st.setOldstatus(rs.getString("oldstatus"));
		    		st.setInformation(rs.getString("information"));
		    		st.setTransaction(rs.getLong("transaction"));
		    		st.setDatetime(rs.getTimestamp("datetime"));
		    		st.setUser(rs.getString("user"));
		    		listSt.add(st);
		    	}
		      	
		    } catch (RuntimeException e) {
		    	log.debug(e);
		    } catch (SQLException e) {
		    	log.debug(e);
			}finally{
				if(statement!=null)statement.close();
				if(conn!=null)conn.close();
			}
		   return listSt;
	  
	}
	
	
	  
}
