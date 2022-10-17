package id.co.keriss.consolidate.ee;

public class TransactionSummaryVO {
	private String mid, name, sale, voide, refund;

	public TransactionSummaryVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TransactionSummaryVO(String mid, String name, String sale,
			String voide, String refund) {
		super();
		this.mid = mid;
		this.name = name;
		this.sale = sale;
		this.voide = voide;
		this.refund = refund;
	}

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

	public String getSale() {
		return sale;
	}

	public void setSale(String sale) {
		this.sale = sale;
	}

	public String getVoide() {
		return voide;
	}

	public void setVoide(String voide) {
		this.voide = voide;
	}

	public String getRefund() {
		return refund;
	}

	public void setRefund(String refund) {
		this.refund = refund;
	}
}
