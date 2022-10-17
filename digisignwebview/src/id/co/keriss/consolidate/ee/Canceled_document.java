package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.User;
import id.co.keriss.consolidate.ee.Mitra;

public class Canceled_document implements java.io.Serializable {
	private Long id;
	private Date date_time;
	private DocumentAccess doc_access;
	private String reason;
	
	public Canceled_document() {
		// TODO Auto-generated constructor stub
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate_time() {
		return date_time;
	}

	public void setDate_time(Date date_time) {
		this.date_time = date_time;
	}

	public DocumentAccess getDoc_access() {
		return doc_access;
	}

	public void setDoc_access(DocumentAccess doc_access) {
		this.doc_access = doc_access;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	
	
	
}
