package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.Date;
import org.jpos.ee.User;

/**
 * Card generated by hbm2java
 */
public class ConfirmCode  implements java.io.Serializable {

     private long id;
     private User eeuser;
     //private Userdata userdata;
     private String code;
     private Date waktu_buat;
     private String status;
     private String msisdn;
     private String hash_code;
     
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public User getEeuser() {
		return eeuser;
	}
	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Date getWaktu_buat() {
		return waktu_buat;
	}
	public void setWaktu_buat(Date waktu_buat) {
		this.waktu_buat = waktu_buat;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getHash_code() {
		return hash_code;
	}
	public void setHash_code(String hash_code) {
		this.hash_code = hash_code;
	}
	
     
     
}    