package id.co.keriss.consolidate.ee;

public class CardTransactionVO {
	private String mname, mid,tid, batch, cardno, approval, date, total, status,type, typeid;
	private Long trace;
	private String id;
	
	
	public String getMname() {
		return mname;
	}

	public void setMname(String mname) {
		this.mname = mname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getTypeid() {
		return typeid;
	}

	public void setTypeid(String typeid) {
		this.typeid = typeid;
	}

	
	
	public Long getTrace() {
		return trace;
	}

	public void setTrace(Long trace) {
		this.trace = trace;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		if(type.equals("Prepaid PLN")){
			typeid="pre";
		}
		else if(type.equals("Postpaid PLN")){
			typeid="post";
		}
		else if(type.equals("Non tagihan Listrik")){
			typeid="nontaglis";
		}
		else{
			typeid="pulsa";
		}
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public String getApproval() {
		return approval;
	}

	public void setApproval(String approval) {
		this.approval = approval;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public CardTransactionVO() {
		super();
	}

	public CardTransactionVO(String tid, String batch, String cardno,
			String approval, String date, String total, String status) {
		super();
		this.tid = tid;
		this.batch = batch;
		this.cardno = cardno;
		this.approval = approval;
		this.date = date;
		this.total = total;
		this.status = status;
	}
}
