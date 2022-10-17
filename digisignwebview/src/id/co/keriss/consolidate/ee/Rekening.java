package id.co.keriss.consolidate.ee;

public class Rekening implements java.io.Serializable {
	
	private Long id;
	private String kode_bank;
	private String no_rekening;
	private String image_buku_tabungan;
	private Userdata userdata;
	
	public Rekening() {
		// TODO Auto-generated constructor stub
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKode_bank() {
		return kode_bank;
	}

	public void setKode_bank(String kode_bank) {
		this.kode_bank = kode_bank;
	}
	public String getNo_rekening() {
		return no_rekening;
	}

	public void setNo_rekening(String no_rekening) {
		this.no_rekening = no_rekening;
	}

	public String getImage_buku_tabungan() {
		return image_buku_tabungan;
	}

	public void setImage_buku_tabungan(String image_buku_tabungan) {
		this.image_buku_tabungan = image_buku_tabungan;
	}

	public Userdata getUserdata() {
		return userdata;
	}

	public void setUserdata(Userdata userdata) {
		this.userdata = userdata;
	}

	


		
}
