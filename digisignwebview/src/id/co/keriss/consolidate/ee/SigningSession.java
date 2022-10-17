package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.User;
import id.co.keriss.consolidate.ee.Mitra;

public class SigningSession implements java.io.Serializable {
	private Long id;
	private Date create_time;
	private Date expire_time;
	private Mitra mitra;
	private User eeuser;
	private String session_key;
	private boolean used;
	
	public SigningSession() {
		// TODO Auto-generated constructor stub
		
	}

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
	
	
	
}
