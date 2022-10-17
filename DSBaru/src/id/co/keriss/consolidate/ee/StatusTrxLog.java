package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.math.BigDecimal;
import java.util.Date;

import id.co.keriss.consolidate.ee.VO.StatusTrxLogVO;
import id.co.keriss.consolidate.util.SystemUtil;

/**
 * Bank generated by hbm2java
 */
public class StatusTrxLog  implements java.io.Serializable {

    private long id;
    private String oldstatus;
    private String oldsn;
    private BigDecimal oldprice;
    private String information;
    private	Long transaction;
    private Transaction trx;
    private Date datetime;
	private String user;
    
    public long getId() {
		return id;
	}
    
    public StatusTrxLogVO getVO(){
    	StatusTrxLogVO vo=new StatusTrxLogVO();
    	vo.setId(id);
    	vo.setOldstatus(oldstatus);
    	vo.setOldsn(oldsn);
    	vo.setOldprice(oldprice);
    	vo.setInformation(information);
    	vo.setTransaction(transaction);
    	vo.setDatetime(SystemUtil.changeFormatDate(datetime));
    	vo.setUser(user);
		return vo;
    }
    
    
	public Date getDatetime() {
		return datetime;
	}



	public void setDatetime(Date datetime) {
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
	public BigDecimal getOldprice() {
		return oldprice;
	}
	public void setOldprice(BigDecimal oldprice) {
		this.oldprice = oldprice;
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


