package id.co.keriss.consolidate.ee;

import java.util.Date;

public class TokenMitra {
	private Long id;
	private Mitra mitra;
	private String token;
	private String aes_key;
	private boolean status_aktif;
	private Date create_date;
	private Date update_date;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getAes_key() {
		return aes_key;
	}
	public void setAes_key(String aes_key) {
		this.aes_key = aes_key;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public boolean isStatus_aktif() {
		return status_aktif;
	}
	public void setStatus_aktif(boolean status_aktif) {
		this.status_aktif = status_aktif;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public Date getUpdate_date() {
		return update_date;
	}
	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}
	
	
}
