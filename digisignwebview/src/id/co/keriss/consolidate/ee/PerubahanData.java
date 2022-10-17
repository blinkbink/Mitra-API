package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.User;

public class PerubahanData implements java.io.Serializable {
	private Long id;
	private User eeuser;
	private boolean address;
	private boolean phone;
	private String kelurahan;
	private String alamat;
	private String kecamatan;
	private String kota;
	private String propinsi;
	private String kodepos;
	private String no_hp;
	private User update_from;
	private Date tgl_req;
	private char status;
	
	public PerubahanData() {
		// TODO Auto-generated constructor stub
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isAddress() {
		return address;
	}

	public void setAddress(boolean address) {
		this.address = address;
	}

	public boolean isPhone() {
		return phone;
	}

	public void setPhone(boolean phone) {
		this.phone = phone;
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

	public String getNo_hp() {
		return no_hp;
	}

	public void setNo_hp(String no_hp) {
		this.no_hp = no_hp;
	}

	public User getEeuser() {
		return eeuser;
	}

	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
	}

	public User getUpdate_from() {
		return update_from;
	}

	public void setUpdate_from(User update_from) {
		this.update_from = update_from;
	}

	public Date getTgl_req() {
		return tgl_req;
	}

	public void setTgl_req(Date tgl_req) {
		this.tgl_req = tgl_req;
	}

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}
	
}
