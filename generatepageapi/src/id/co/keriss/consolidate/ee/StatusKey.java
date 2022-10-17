package id.co.keriss.consolidate.ee;

public class StatusKey implements java.io.Serializable  {

	private String status_key;
	private String keterangan;
	
	public StatusKey() {
		// TODO Auto-generated constructor stub
	}

	public StatusKey(String status_key) {
		super();
		this.status_key = status_key;
	}

	public String getStatus_key() {
		return status_key;
	}

	public void setStatus_key(String status_key) {
		this.status_key = status_key;
	}

	public String getKeterangan() {
		return keterangan;
	}

	public void setKeterangan(String keterangan) {
		this.keterangan = keterangan;
	}
	
}
