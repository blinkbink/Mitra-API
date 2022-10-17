package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.sql.Blob;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.jpos.ee.User;

/**
 * Transaction generated by hbm2java
 */
public class Documents implements java.io.Serializable{

     private Long id;
     private String file_name;
     private String path;
     private String rename;
     private char status;
     private Date waktu_buat;
     private String file;
     private String signdoc;
//     private Userdata userdata;
     private String idMitra;
     private boolean sign;
     private User eeuser;
     private char payment;
     private Set docAccess;
     private String type_document;
     private int current_seq;
     private boolean sequence;
     private boolean delete;
     

     
     public boolean isDelete() {
		return delete;
	}


	public void setDelete(boolean delete) {
		this.delete = delete;
	}


	public String getType_document() {
		return type_document;
	}


	public void setType_document(String type_document) {
		this.type_document = type_document;
	}


	public Set getDocAccess() {
		return docAccess;
	}


	public void setDocAccess(Set docAccess) {
		this.docAccess = docAccess;
	}

	public String getTypeShare() {
		Iterator it=docAccess.iterator();
		int i=1;
		while(it.hasNext()) {
			DocumentAccess da=(DocumentAccess) it.next();
			
			if(da.getType().equals("sign")) {
				return "sign";
			}
		}
		
		return "share";
	}
	public String getStatusSign() {
		String stat="author";
		Iterator it=docAccess.iterator();
		int i=1;
		System.out.println("ID DOC :"+id+" "+docAccess.size());
		while(it.hasNext()) {
			DocumentAccess da=(DocumentAccess) it.next();
			if(da.getEeuser()==null) continue;
			System.out.println((i++)+" : "+da.getId()+" , "+da.getEeuser().getNick()+" , "+da.getType()+" , "+da.isFlag());
			if(da.getEeuser().getId()!=eeuser.getId() ) {
				continue;
			}
			if(da.getType().equals("sign") && !da.isFlag()) {
				return "unsign";
			}
			if(da.getType().equals("sign") && da.isFlag()) {
				stat= "signed";
			}
		}
		
		return stat;
		
	}

	public Documents() {
     
     }
 
     
	public User getEeuser() {
		return eeuser;
	}


	public void setEeuser(User eeuser) {
		this.eeuser = eeuser;
	}

	
	/**
	 * 
	 * @return
	 * '1' :bayar masing-masing
	 * '2' :ditanggung pengirim dokumen
	 */
	public char getPayment() {
		return payment;
	}

	/**
	 * 
	 * @param payment
	 * 1 :bayar masing-masing
	 * 2 :ditanggung pengirim dokumen
	 * 
	 */
	public void setPayment(char payment) {
		this.payment = payment;
	}


	public boolean isSign() {
		return sign;
	}

	public void setSign(boolean sign) {
		this.sign = sign;
	}

	public String getSigndoc() {
		return signdoc;
	}


	public void setSigndoc(String signdoc) {
		this.signdoc = signdoc;
	}

	public String getIdMitra() {
		return idMitra;
	}

	public void setIdMitra(String idMitra) {
		this.idMitra = idMitra;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}

	public Date getWaktu_buat() {
		return waktu_buat;
	}

	public void setWaktu_buat(Date waktu_buat) {
		this.waktu_buat = waktu_buat;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

//	public Userdata getUserdata() {
//		return userdata;
//	}
//
//	public void setUserdata(Userdata userdata) {
//		this.userdata = userdata;
//	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRename() {
		return rename;
	}
	public void setRename(String rename) {
		this.rename = rename;
	}
	public int getCurrent_seq() {
		return current_seq;
	}
	public void setCurrent_seq(int current_seq) {
		this.current_seq = current_seq;
	}
	public boolean isSequence() {
		return sequence;
	}
	public void setSequence(boolean sequence) {
		this.sequence = sequence;
	}
	

}