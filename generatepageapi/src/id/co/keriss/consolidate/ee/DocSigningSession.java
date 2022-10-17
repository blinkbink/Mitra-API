package id.co.keriss.consolidate.ee;

public class DocSigningSession implements java.io.Serializable {
	
	SigningSession signingSession;
	Documents document;
	boolean read=false;
	
	public SigningSession getSigningSession() {
		return signingSession;
	}
	public void setSigningSession(SigningSession signingSession) {
		this.signingSession = signingSession;
	}
	public Documents getDocument() {
		return document;
	}
	public void setDocument(Documents document) {
		this.document = document;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	
	
}
