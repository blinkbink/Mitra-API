package id.co.keriss.consolidate.ee;

public class Mitra implements java.io.Serializable {
	private String name;
	private Long id;
	private boolean verifikasi;
	private boolean notifikasi;
	private String level;
	private Wl_Provinces provinsi;
	
	
	
	public Mitra() {
		// TODO Auto-generated constructor stub
		
	}




	public Wl_Provinces getProvinsi() {
		return provinsi;
	}




	public void setProvinsi(Wl_Provinces provinsi) {
		this.provinsi = provinsi;
	}




	public String getLevel() {
		return level;
	}




	public void setLevel(String level) {
		this.level = level;
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


	
	
}
