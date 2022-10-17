package id.co.keriss.consolidate.ee;

import java.math.BigDecimal;
import java.util.Date;

import org.jpos.ee.User;

public class ReconLog {
	BigDecimal amount;
	Long id;
	Date recontime;
	int postpaid;
	int prepaid;
	int nontaglis;
	BigDecimal postpaidamt;
	BigDecimal prepaidamt;
	BigDecimal nontaglisamt;
	
	
	public BigDecimal getPostpaidamt() {
		return postpaidamt;
	}
	public void setPostpaidamt(BigDecimal postpaidamt) {
		this.postpaidamt = postpaidamt;
	}
	public BigDecimal getPrepaidamt() {
		return prepaidamt;
	}
	public void setPrepaidamt(BigDecimal prepaidamt) {
		this.prepaidamt = prepaidamt;
	}
	public BigDecimal getNontaglisamt() {
		return nontaglisamt;
	}
	public void setNontaglisamt(BigDecimal nontaglisamt) {
		this.nontaglisamt = nontaglisamt;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getRecontime() {
		return recontime;
	}
	public void setRecontime(Date recontime) {
		this.recontime = recontime;
	}
	public int getPostpaid() {
		return postpaid;
	}
	public void setPostpaid(int postpaid) {
		this.postpaid = postpaid;
	}
	public int getPrepaid() {
		return prepaid;
	}
	public void setPrepaid(int prepaid) {
		this.prepaid = prepaid;
	}
	public int getNontaglis() {
		return nontaglis;
	}
	public void setNontaglis(int nontaglis) {
		this.nontaglis = nontaglis;
	}
	
	
	
	
	
}
