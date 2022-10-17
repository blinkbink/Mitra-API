package id.co.keriss.consolidate.ee;

import java.util.Date;

public class Verifier {
	private Long id;
	private String name;
	private String position;
	private String tlp;
	private String email;
	private String id_ktp;
	private String ktp_image;
	private String face_image;
	private boolean status;
	private String verifier_id;
	private boolean delete;
	private Mitra mitra;
	private Date create_date;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getTlp() {
		return tlp;
	}
	public void setTlp(String tlp) {
		this.tlp = tlp;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getId_ktp() {
		return id_ktp;
	}
	public void setId_ktp(String id_ktp) {
		this.id_ktp = id_ktp;
	}
	public String getKtp_image() {
		return ktp_image;
	}
	public void setKtp_image(String ktp_image) {
		this.ktp_image = ktp_image;
	}
	public String getFace_image() {
		return face_image;
	}
	public void setFace_image(String face_image) {
		this.face_image = face_image;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getVerifier_id() {
		return verifier_id;
	}
	public void setVerifier_id(String verifier_id) {
		this.verifier_id = verifier_id;
	}
	public boolean isDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	
	
	
}
