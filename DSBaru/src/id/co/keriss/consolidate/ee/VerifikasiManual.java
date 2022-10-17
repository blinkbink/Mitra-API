package id.co.keriss.consolidate.ee;

import java.util.Date;

public class VerifikasiManual implements java.io.Serializable{
	private Long id;
	private String nik;
	private String email;
	private Date create_date;
	private RegistrationLog id_registration_log;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNik() {
		return nik;
	}
	public void setNik(String nik) {
		this.nik = nik;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public RegistrationLog getId_registration_log() {
		return id_registration_log;
	}
	public void setId_registration_log(RegistrationLog id_registration_log) {
		this.id_registration_log = id_registration_log;
	}
	
}
