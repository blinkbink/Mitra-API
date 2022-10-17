package id.co.keriss.consolidate.ee;

import java.util.Date;

public class LetakTtd implements java.io.Serializable {
	private Long id;
	private FormatPdf format_pdf;
	private int ttd_ke;
	private int page;
	private String lx;
	private String ly;
	private String rx;
	private String ry;
	private Date createdate;
	private int prf_ke;
	
	
	public int getPrf_ke() {
		return prf_ke;
	}
	public void setPrf_ke(int prf_ke) {
		this.prf_ke = prf_ke;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public FormatPdf getFormat_pdf() {
		return format_pdf;
	}
	public void setFormat_pdf(FormatPdf format_pdf) {
		this.format_pdf = format_pdf;
	}
	public int getTtd_ke() {
		return ttd_ke;
	}
	public void setTtd_ke(int ttd_ke) {
		this.ttd_ke = ttd_ke;
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
	public Date getCreatedate() {
		return createdate;
	}
	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}
	
}
