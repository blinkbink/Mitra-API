package id.co.keriss.consolidate.ee;

public class Verifier  implements java.io.Serializable {

	
	Long id;
	String id_ktp;
	String name;
	String position;
	String tlp;
	String email;
	String imageKTP;
	String imageWajah;
	boolean status;
	boolean delete;
	String verifier_id;
	Long mitra;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getId_ktp() {
		return id_ktp;
	}
	public void setId_ktp(String id_ktp) {
		this.id_ktp = id_ktp;
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
	public String getImageKTP() {
		return imageKTP;
	}
	public void setImageKTP(String imageKTP) {
		this.imageKTP = imageKTP;
	}
	public String getImageWajah() {
		return imageWajah;
	}
	public void setImageWajah(String imageWajah) {
		this.imageWajah = imageWajah;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public boolean isDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	public String getVerifier_id() {
		return verifier_id;
	}
	public void setVerifier_id(String verifier_id) {
		this.verifier_id = verifier_id;
	}
	public Long getMitra() {
		return mitra;
	}
	public void setMitra(Long mitra) {
		this.mitra = mitra;
	}
	
	
}
