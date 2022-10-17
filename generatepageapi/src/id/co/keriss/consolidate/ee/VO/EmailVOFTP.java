package id.co.keriss.consolidate.ee.VO;

import id.co.keriss.consolidate.ee.Userdata;

public class EmailVOFTP {
	private String email;
	private Userdata udata;
	private boolean reg;
	private String nama;
	private Long iddocac;
	private Long iddoc;
	private String document_id;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Userdata getUdata() {
		return udata;
	}
	public void setUdata(Userdata udata) {
		this.udata = udata;
	}
	public boolean isReg() {
		return reg;
	}
	public void setReg(boolean reg) {
		this.reg = reg;
	}
	public String getNama() {
		return nama;
	}
	public void setNama(String nama) {
		this.nama = nama;
	}
	public Long getIddocac() {
		return iddocac;
	}
	public void setIddocac(Long iddocac) {
		this.iddocac = iddocac;
	}
	public Long getIddoc() {
		return iddoc;
	}
	public void setIddoc(Long iddoc) {
		this.iddoc = iddoc;
	}
	public String getDocument_id() {
		return document_id;
	}
	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}
	
	
	
}
