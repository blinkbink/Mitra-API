package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.Date;

import org.jpos.ee.User;

/**
 * Terminal generated by hbm2java
 */
public class DocumentAccess  implements java.io.Serializable {


	 private Long id;
     private Documents document;
//     private Userdata userdata;
     private String type;
     private boolean flag;
     private Date date_sign;
     private String email;
     private int page;
     private String lx;
     private String ly;
     private String rx;
     private String ry;
     private String name;
     private boolean read;
     private User eeuser;
     private String invoice;
     private Date datetime;
     private String action;
     private boolean visible;
     private int sequence_no;
     private boolean cancel;
    


	public DocumentAccess() {
    }

    
   	public boolean isVisible() {
		return visible;
	}


	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public User getEeuser() {
   		return eeuser;
   	}


   	public void setEeuser(User eeuser) {
   		this.eeuser = eeuser;
   	}

    
	public boolean isRead() {
		return read;
	}


	public void setRead(boolean read) {
		this.read = read;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Documents getDocument() {
		return document;
	}
    
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public void setDocument(Documents document) {
		this.document = document;
	}

//	public Userdata getUserdata() {
//		return userdata;
//	}
//
//	public void setUserdata(Userdata user) {
//		this.userdata = user;
//	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public Date getDate_sign() {
		return date_sign;
	}

	public void setDate_sign(Date date_sign) {
		this.date_sign = date_sign;
	}


	public int getPage() {
		return page;
	}


	public void setPage(int page) {
		this.page = page;
	}


	public String getLx() {
		return lx;
	}


	public void setLx(String lx) {
		this.lx = lx;
	}


	public String getLy() {
		return ly;
	}


	public void setLy(String ly) {
		this.ly = ly;
	}


	public String getRx() {
		return rx;
	}


	public void setRx(String rx) {
		this.rx = rx;
	}


	public String getRy() {
		return ry;
	}


	public void setRy(String ry) {
		this.ry = ry;
	}


	public String getInvoice() {
		return invoice;
	}


	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}


	public Date getDatetime() {
		return datetime;
	}


	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
    
	public int getSequence_no() {
		return sequence_no;
	}


	public void setSequence_no(int sequence_no) {
		this.sequence_no = sequence_no;
	}


	public boolean isCancel() {
		return cancel;
	}


	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}
	
}




