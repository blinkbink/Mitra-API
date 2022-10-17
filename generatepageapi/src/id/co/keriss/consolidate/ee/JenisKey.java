package id.co.keriss.consolidate.ee;

public class JenisKey implements java.io.Serializable {
	private String jenis_key;
	private String keterangan;
	
	
	public JenisKey() {
		// TODO Auto-generated constructor stub
		
	}


	public JenisKey(String jenis_key) {
		super();
		this.jenis_key = jenis_key;
	}


	public String getJenis_key() {
		return jenis_key;
	}


	public void setJenis_key(String jenis_key) {
		this.jenis_key = jenis_key;
	}


	public String getKeterangan() {
		return keterangan;
	}


	public void setKeterangan(String keterangan) {
		this.keterangan = keterangan;
	}
	
	
}
