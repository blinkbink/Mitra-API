package id.co.keriss.consolidate.ee;

import org.jpos.ee.User;

public class Kuser implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private User eeuser;
	private String kuser;
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
	public String getKuser() {
		return kuser;
	}
	public void setKuser(String kuser) {
		this.kuser = kuser;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
