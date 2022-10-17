package id.co.keriss.consolidate.ee;

import java.util.Date;

public class TMPUsername implements java.io.Serializable {
	private Long id;
	private String username;
	private Date date_record;
	
	public TMPUsername() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param id
	 * @param username
	 * @param date_record
	 */
	public TMPUsername(Long id, String username, Date date_record) {
		super();
		this.id = id;
		this.username = username;
		this.date_record = date_record;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
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
