package id.co.keriss.consolidate.ee.VO;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.math.BigDecimal;
import id.co.keriss.consolidate.util.SystemUtil;

/**
 * Bank generated by hbm2java
 */
public class StatusTrxLogVO  implements java.io.Serializable {

    private long id;
    private String oldstatus;
    private String oldsn;
    private String oldprice;
    private String information;
    private	Long transaction;
    private String datetime;
	private String user;
    
    public long getId() {
		return id;
	}
    
    
    
	public String getDatetime() {
		return datetime;
	}



	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}



	public Long getTransaction() {
		return transaction;
	}

	public void setTransaction(Long transaction) {
		this.transaction = transaction;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getOldstatus() {
		if(oldstatus==null)return "";
		return oldstatus;
	}
	public void setOldstatus(String oldstatus) {
		this.oldstatus = oldstatus;
	}
	public String getOldsn() {
		if(oldsn==null)return "";
		return oldsn;
	}
	public void setOldsn(String oldsn) {
		this.oldsn = oldsn;
	}
	public String getOldprice() {
		return oldprice;
	}
	public void setOldprice(BigDecimal oldprice) {
		if(oldprice==null){
			this.oldprice="";
			return;
		}
		this.oldprice = SystemUtil.amountDecFormatStr(oldprice);
	}
	public String getInformation() {
		if(information==null)return "";
		return information;
	}
	public void setInformation(String information) {
		this.information = information;
	}
	public void setUser(String user) {
		this.user=user;
	}
	public String getUser() {
		return user;
	}
	
	
     
     
}


