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
	private boolean dukcapil;
	private boolean ekyc_tabungan;
	private String user_sftp;
	private boolean verifikasi_email;
	private Wl_Provinces provinsi;
	

	public Wl_Provinces getProvinsi() {
		return provinsi;
	}

	public void setProvinsi(Wl_Provinces provinsi) {
		this.provinsi = provinsi;
	}




	public boolean isVerifikasi_email() {
		return verifikasi_email;
	}




	public void setVerifikasi_email(boolean verifikasi_email) {
		this.verifikasi_email = verifikasi_email;
	}




	public boolean isEkyc_tabungan() {
		return ekyc_tabungan;
	}




	public void setEkyc_tabungan(boolean ekyc_tabungan) {
		this.ekyc_tabungan = ekyc_tabungan;
	}




	public String getUser_sftp() {
		return user_sftp;
	}




	public void setUser_sftp(String user_sftp) {
		this.user_sftp = user_sftp;
	}




	public boolean isDukcapil() {
		return dukcapil;
	}




	public void setDukcapil(boolean dukcapil) {
		this.dukcapil = dukcapil;
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


	
	
}
