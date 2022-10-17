package id.co.keriss.consolidate.ee;

public class CardSummaryVO {
	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVisa() {
		return visa;
	}

	public void setVisa(String visa) {
		this.visa = visa;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getAmex() {
		return amex;
	}

	public void setAmex(String amex) {
		this.amex = amex;
	}

	public CardSummaryVO(String mid, String name, String visa, String master,
			String amex) {
		super();
		this.mid = mid;
		this.name = name;
		this.visa = visa;
		this.master = master;
		this.amex = amex;
	}

	public CardSummaryVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String mid, name, visa, master, amex;


}
