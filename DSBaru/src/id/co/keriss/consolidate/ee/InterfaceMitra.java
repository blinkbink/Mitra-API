package id.co.keriss.consolidate.ee;

public class InterfaceMitra implements java.io.Serializable {
	
    private long id;
    private Mitra mitra;
    private String i_aktivasi;
    private String i_sign_doc;
    private String i_sign;
    private String token_auth;
    private String i_registrasi;
    
    public InterfaceMitra() {
        
    }
    
    public String getI_registrasi() {
		return i_registrasi;
	}

	public void setI_registrasi(String i_registrasi) {
		this.i_registrasi = i_registrasi;
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

    public String getI_aktivasi() {
        return i_aktivasi;
    }

    public void setI_aktivasi(String i_aktivasi) {
        this.i_aktivasi = i_aktivasi;
    }

    public String getI_sign_doc() {
        return i_sign_doc;
    }

    public void setI_sign_doc(String i_sign_doc) {
        this.i_sign_doc = i_sign_doc;
    }

    public String getToken_auth() {
        return token_auth;
    }

    public void setToken_auth(String token_auth) {
        this.token_auth = token_auth;
    }

    public String getI_sign() {
        return i_sign;
    }

    public void setI_sign(String i_sign) {
        this.i_sign = i_sign;
    }    
    
    
}