package id.co.keriss.consolidate.ee;


public class KuserAllow implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Kuser id_kuser;
	private FormatPdf id_format_pdf;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Kuser getId_kuser() {
		return id_kuser;
	}
	public void setId_kuser(Kuser id_kuser) {
		this.id_kuser = id_kuser;
	}
	public FormatPdf getId_format_pdf() {
		return id_format_pdf;
	}
	public void setId_format_pdf(FormatPdf id_format_pdf) {
		this.id_format_pdf = id_format_pdf;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
}
