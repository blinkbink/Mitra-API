package id.co.keriss.consolidate.ee;

import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.SigningSession;

public class DocSigningSession implements java.io.Serializable {
	private SigningSession signingSession;
	private Documents document;
	private boolean read;
	
	public DocSigningSession() {
		// TODO Auto-generated constructor stub
		
	}
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
