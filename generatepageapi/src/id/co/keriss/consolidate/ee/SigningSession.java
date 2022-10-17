package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.User;

public class SigningSession implements java.io.Serializable{
	
	Long id;
	Date create_time;
	Date expire_time;
	Mitra mitra;
	User eeuser;
	String session_key;
	boolean used;
	boolean view_only=false;
	boolean must_read=false;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	public Date getExpire_time() {
		return expire_time;
	}
	public void setExpire_time(Date expire_time) {
		this.expire_time = expire_time;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public User getEeuser() {
		return eeuser;
	}
	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
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
	public boolean isView_only() {
		return view_only;
	}
	public void setView_only(boolean view_only) {
		this.view_only = view_only;
	}
	public boolean isMust_read() {
		return must_read;
	}
	public void setMust_read(boolean must_read) {
		this.must_read = must_read;
	}
	
}
