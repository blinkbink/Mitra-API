package id.co.keriss.consolidate.ee;

import java.io.Serializable;

public class CallbackPending implements Serializable {
    private long id;
    private Mitra mitra;
    private String callback;
    private String tipe = "redirect";
    private Integer response = 212;
    
    
    public CallbackPending() {
      
    }


	public long getId() {
		return id;
	}


	public void setId(long id) {
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


	public String getTipe() {
		return tipe;
	}


	public void setTipe(String tipe) {
		this.tipe = tipe;
	}


	public Integer getResponse() {
		return response;
	}


	public void setResponse(Integer response) {
		this.response = response;
	}
 
    
    
    
    
}
