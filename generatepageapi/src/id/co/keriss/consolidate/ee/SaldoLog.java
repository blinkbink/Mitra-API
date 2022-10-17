package id.co.keriss.consolidate.ee;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import org.jpos.ee.User;

public class SaldoLog {

	private Long id;
	private String tid,refnum,ket, currString, saldoString;
	private int stn;
	private Date datetime;
	private Merchant merchant;
	private User userid;
	private BigDecimal amount;
	private BigDecimal curr_balance;
	private String transaction;


	public String getTransaction() {
		return transaction;
	}


	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}


	public String getSaldoString() {
		DecimalFormat myFormatter = new DecimalFormat("###,###.##");
		return myFormatter.format(amount.longValue());
	}
	

	public String getCurrString() {
		DecimalFormat myFormatter = new DecimalFormat("###,###.##");
		return myFormatter.format(curr_balance.longValue());
	}
	
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getRefnum() {
		return refnum;
	}
	public void setRefnum(String refnum) {
		this.refnum = refnum;
	}
	public String getKet() {
		return ket;
	}
	public void setKet(String ket) {
		this.ket = ket;
	}
	public int getStn() {
		return stn;
	}
	public void setStn(int stn) {
		this.stn = stn;
	}
	public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
	public Merchant getMerchant() {
		return merchant;
	}
	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}
	public User getUserid() {
		return userid;
	}
	public void setUserid(User userid) {
		this.userid = userid;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getCurr_balance() {
		return curr_balance;
	}
	public void setCurr_balance(BigDecimal curr_balance) {
		this.curr_balance = curr_balance;
	}
	private String status;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
