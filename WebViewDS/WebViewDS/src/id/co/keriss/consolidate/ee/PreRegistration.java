package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * Merchant generated by hbm2java
 */
public class PreRegistration  implements java.io.Serializable {


     private long id;
     private String no_identitas;
     private String nama;
     private char jk;
     private String tempat_lahir;
     private Date tgl_lahir;
     private String alamat;
     private String kelurahan;
     private String kecamatan;
     private String kota;
     private String propinsi;
     private String kodepos;
     private String no_handphone;
     private String npwp;
     private String email;
     private String imageKTP;
     private String imageNPWP;
     private String imageWajah;
     private String imageTtd;
     private Mitra mitra;
     private Boolean data_exists;
     private String kode_user;
     
	public String getKode_user() {
		return kode_user;
	}

	public void setKode_user(String kode_user) {
		this.kode_user = kode_user;
	}

	public Boolean getData_exists() {
		return data_exists;
	}

	public PreRegistration() {
		
	}
	
	public String getImageKTP() {
		return imageKTP;
	}

	public void setImageKTP(String imageKTP) {
		this.imageKTP = imageKTP;
	}

	public String getImageNPWP() {
		return imageNPWP;
	}

	public void setImageNPWP(String imageNPWP) {
		this.imageNPWP = imageNPWP;
	}

	public String getImageWajah() {
		return imageWajah;
	}

	public void setImageWajah(String imageWajah) {
		this.imageWajah = imageWajah;
	}

	public String getImageTtd() {
		return imageTtd;
	}

	public void setImageTtd(String imageTtd) {
		this.imageTtd = imageTtd;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNama() {
		return nama;
	}
	public void setNama(String nama) {
		this.nama = nama;
	}
	
	public String getNo_identitas() {
		return no_identitas;
	}
	public void setNo_identitas(String no_identitas) {
		this.no_identitas = no_identitas.trim();
	}
	public char getJk() {
		return jk;
	}
	public void setJk(char jk) {
		this.jk = jk;
	}
	public String getTempat_lahir() {
		return tempat_lahir;
	}
	public void setTempat_lahir(String tempat_lahir) {
		this.tempat_lahir = tempat_lahir;
	}
	public Date getTgl_lahir() {
		return tgl_lahir;
	}
	public void setTgl_lahir(Date tgl_lahir) {
		this.tgl_lahir = tgl_lahir;
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
	public String getNo_handphone() {
		return no_handphone;
	}
	public void setNo_handphone(String no_handphone) {
		this.no_handphone = no_handphone;
	}
	public String getNpwp() {
		return npwp;
	}
	public void setNpwp(String npwp) {
		this.npwp = npwp;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email.toLowerCase().trim();
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}

	public Boolean isData_exists() {
		return data_exists;
	}

	public void setData_exists(Boolean data_exists) {
		this.data_exists = data_exists;
	}
        
}