package id.co.keriss.consolidate.ee;

import java.util.Date;

public class FormatPdf implements java.io.Serializable{
	private Long id;
	private Mitra mitra;
	private String nama_format;
	private String file;
	private int jml_ttd;
	private Date createdate;
	
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
	public String getNama_format() {
		return nama_format;
	}
	public void setNama_format(String nama_format) {
		this.nama_format = nama_format;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public int getJml_ttd() {
		return jml_ttd;
	}
	public void setJml_ttd(int jml_ttd) {
		this.jml_ttd = jml_ttd;
	}
	public Date getCreatedate() {
		return createdate;
	}
	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}
	
	
}
