package id.co.keriss.consolidate.ee;

import java.util.Date;

public class Activation implements java.io.Serializable{

	Long id;
	Date expire_time;
	Date create_time;
	Mitra mitra;
	PreRegistration preregistration;
	String session_key;
	boolean used;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getExpire_time() {
		return expire_time;
	}
	public void setExpire_time(Date expire_time) {
		this.expire_time = expire_time;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public PreRegistration getPreregistration() {
		return preregistration;
	}
	public void setPreregistration(PreRegistration preregistration) {
		this.preregistration = preregistration;
	}
	public String getSession_key() {
		return session_key;
	}
	public void setSession_key(String session_key) {
		this.session_key = session_key;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	
	
}
