package id.co.keriss.consolidate.ee;

public class Mitra implements java.io.Serializable {
	private String name;
	private Long id;
	private boolean verifikasi;
	private boolean notifikasi;
	private boolean ekyc;
	private String level;
	
	private String activation_redirect;
	private String signing_redirect;

	public String getLevel() {
		return level;
	}




	public void setLevel(String level) {
		this.level = level;
	}




	public Mitra() {
		// TODO Auto-generated constructor stub
		
	}




	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}




	public boolean isVerifikasi() {
		return verifikasi;
	}




	public void setVerifikasi(boolean verifikasi) {
		this.verifikasi = verifikasi;
	}




	public boolean isNotifikasi() {
		return notifikasi;
	}




	public void setNotifikasi(boolean notifikasi) {
		this.notifikasi = notifikasi;
	}




	public boolean isEkyc() {
		return ekyc;
	}




	public void setEkyc(boolean ekyc) {
		this.ekyc = ekyc;
	}




	public String getActivation_redirect() {
		return activation_redirect;
	}




	public void setActivation_redirect(String activation_redirect) {
		this.activation_redirect = activation_redirect;
	}




	public String getSigning_redirect() {
		return signing_redirect;
	}




	public void setSigning_redirect(String signing_redirect) {
		this.signing_redirect = signing_redirect;
	}


	
	
}
