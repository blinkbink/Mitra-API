package id.co.keriss.consolidate.ee;

public class SealDocAccess implements java.io.Serializable{
	private Long id;
	private UserSeal user_seal;
	private DocumentAccess doc_access;
	private String qr_text;
	public String getQr_text() {
		return qr_text;
	}
	public void setQr_text(String qr_text) {
		this.qr_text = qr_text;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public UserSeal getUser_seal() {
		return user_seal;
	}
	public void setUser_seal(UserSeal user_seal) {
		this.user_seal = user_seal;
	}
	public DocumentAccess getDoc_access() {
		return doc_access;
	}
	public void setDoc_access(DocumentAccess doc_access) {
		this.doc_access = doc_access;
	}
	
	
}
