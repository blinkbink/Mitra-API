package id.co.keriss.consolidate.ee;

import java.io.Serializable;
import java.util.Date;

public class Login implements Serializable {
	private long id;
	private String username;
	private String password;
	private String imei;
	private Date date_record;

	public Login() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	/**
	 * @return the date_record
	 */
	public Date getDate_record() {
		return date_record;
	}

	/**
	 * @param date_record the date_record to set
	 */
	public void setDate_record(Date date_record) {
		this.date_record = date_record;
	}

}
