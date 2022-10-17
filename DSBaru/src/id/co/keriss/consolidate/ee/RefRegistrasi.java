package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.User;

public class RefRegistrasi implements java.io.Serializable{
	private Long id;
	private User eeuser;
	private Mitra mitra;
	private String admin_verifikasi;
	private String noref_pendaftaran;
	private Date create_date;
	private String timestamp_verifikasi;
	private String data_verifikasi;
	
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
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public String getAdmin_verifikasi() {
		return admin_verifikasi;
	}
	public void setAdmin_verifikasi(String admin_verifikasi) {
		this.admin_verifikasi = admin_verifikasi;
	}
	public String getNoref_pendaftaran() {
		return noref_pendaftaran;
	}
	public void setNoref_pendaftaran(String noref_pendaftaran) {
		this.noref_pendaftaran = noref_pendaftaran;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public String getTimestamp_verifikasi() {
		return timestamp_verifikasi;
	}
	public void setTimestamp_verifikasi(String timestamp_verifikasi) {
		this.timestamp_verifikasi = timestamp_verifikasi;
	}
	public String getData_verifikasi() {
		return data_verifikasi;
	}
	public void setData_verifikasi(String data_verifikasi) {
		this.data_verifikasi = data_verifikasi;
	}
	
	
	
}
