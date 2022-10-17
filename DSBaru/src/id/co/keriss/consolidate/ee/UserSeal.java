package id.co.keriss.consolidate.ee;

import org.jpos.ee.User;

public class UserSeal implements java.io.Serializable{
	private Long id;
	private Seal seal;
	private User eeuser;
	private Boolean status;
	
	public User getEeuser() {
		return eeuser;
	}
	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Seal getSeal() {
		return seal;
	}
	public void setSeal(Seal seal) {
		this.seal = seal;
	}
	
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
}
