package id.co.keriss.consolidate.ee;

import java.io.Serializable;
import java.util.Date;

public class CallbackPending implements Serializable{
	private Long id;
	private Mitra mitra;
	private String callback;
	private Date time;
	private int response;
	private String tipe;
	
	public CallbackPending() {
		
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public int getResponse() {
		return response;
	}
	public void setResponse(int response) {
		this.response = response;
	}
	public String getTipe() {
		return tipe;
	}
	public void setTipe(String tipe) {
		this.tipe = tipe;
	}
	
}
