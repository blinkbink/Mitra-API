package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.User;

public class EeuserMitra implements java.io.Serializable{
	private Long id;
	private User eeuser;
	private Mitra mitra;
	private String password;
	private Date createdate;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getEeuser() {
		return eeuser;
	}
	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getCreatedate() {
		return createdate;
	}
	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}
	
	
}
