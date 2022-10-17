package id.co.keriss.consolidate.ee;

public class Alamat implements java.io.Serializable {
	private Long id;
	private String alamat;
	private String kelurahan;
	private String kecamatan;
	private String kota;
	private String propinsi;
	private String kodepos;
	private char status;
	private Userdata userdata;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAlamat() {
		return alamat;
	}
	public void setAlamat(String alamat) {
		this.alamat = alamat;
	}
	public String getKelurahan() {
		return kelurahan;
	}
	public void setKelurahan(String kelurahan) {
		this.kelurahan = kelurahan;
	}
	public String getKecamatan() {
		return kecamatan;
	}
	public void setKecamatan(String kecamatan) {
		this.kecamatan = kecamatan;
	}
	public String getKota() {
		return kota;
	}
	public void setKota(String kota) {
		this.kota = kota;
	}
	public String getPropinsi() {
		return propinsi;
	}
	public void setPropinsi(String propinsi) {
		this.propinsi = propinsi;
	}
	public String getKodepos() {
		return kodepos;
	}
	public void setKodepos(String kodepos) {
		this.kodepos = kodepos;
	}
	public char getStatus() {
		return status;
	}
	public void setStatus(char status) {
		this.status = status;
	}
	public Userdata getUserdata() {
		return userdata;
	}
	public void setUserdata(Userdata userdata) {
		this.userdata = userdata;
	}
	
}
