package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.Date;
import org.jpos.ee.User;


/**
 * Card generated by hbm2java
 */
public class KeyV3  implements java.io.Serializable {


     private long id;
     private JenisKey jenis_key;
     private String key_alias;
     private String key_version;
     private String key;
     private Date waktu_buat;
     private StatusKey status;
     private Date waktu_exp;
     private User user;
     private String level;
     private Mitra mitra;
     
     
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public JenisKey getJenis_key() {
		return jenis_key;
	}
	public void setJenis_key(JenisKey jenis_key) {
		this.jenis_key = jenis_key;
	}
	public String getKey_alias() {
		return key_alias;
	}
	public void setKey_alias(String key_alias) {
		this.key_alias = key_alias;
	}
	public String getKey_version() {
		return key_version;
	}
	public void setKey_version(String key_version) {
		this.key_version = key_version;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public Date getWaktu_buat() {
		return waktu_buat;
	}
	public void setWaktu_buat(Date waktu_buat) {
		this.waktu_buat = waktu_buat;
	}
	public StatusKey getStatus() {
		return status;
	}
	public void setStatus(StatusKey status) {
		this.status = status;
	}
	public Date getWaktu_exp() {
		return waktu_exp;
	}
	public void setWaktu_exp(Date waktu_exp) {
		this.waktu_exp = waktu_exp;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}   
     
     
}