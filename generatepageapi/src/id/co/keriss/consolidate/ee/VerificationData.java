package id.co.keriss.consolidate.ee;

import java.util.Date;

public class VerificationData implements java.io.Serializable {
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
       String tempat_lahir;
       String tgl_lahir;
       String alamat;
       String nik;
       String wajah;
       String timestamp_text;
       String timestamp_selfie;
       Date tanggal_verifikasi;
       Date tanggal_verifikasi_foto;
       Long id;
       String score;
       
    
       
    
	
	public String getScore() {
		return score;
	}


	public void setScore(String score) {
		this.score = score;
	}


	public VerificationData() {
		super();
	}
	
	
	public Date getTanggal_verifikasi_foto() {
		return tanggal_verifikasi_foto;
	}


	public void setTanggal_verifikasi_foto(Date tanggal_verifikasi_foto) {
		this.tanggal_verifikasi_foto = tanggal_verifikasi_foto;
	}


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTimestamp_text() {
		return timestamp_text;
	}
	public void setTimestamp_text(String timestamp_text) {
		this.timestamp_text = timestamp_text;
	}
	public String getTimestamp_selfie() {
		return timestamp_selfie;
	}
	public void setTimestamp_selfie(String timestamp_selfie) {
		this.timestamp_selfie = timestamp_selfie;
	}
	public Date getTanggal_verifikasi() {
		return tanggal_verifikasi;
	}
	public void setTanggal_verifikasi(Date tanggal_verifikasi) {
		this.tanggal_verifikasi = tanggal_verifikasi;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTempat_lahir() {
		return tempat_lahir;
	}
	public void setTempat_lahir(String tempat_lahir) {
		this.tempat_lahir = tempat_lahir;
	}
	public String getTgl_lahir() {
		return tgl_lahir;
	}
	public void setTgl_lahir(String tgl_lahir) {
		this.tgl_lahir = tgl_lahir;
	}
	public String getAlamat() {
		return alamat;
	}
	public void setAlamat(String alamat) {
		this.alamat = alamat;
	}
	
	
	public String getNik() {
		return nik;
	}
	public void setNik(String nik) {
		this.nik = nik;
	}
	public String getWajah() {
		return wajah;
	}
	public void setWajah(String wajah) {
		this.wajah = wajah;
	}
       
}
