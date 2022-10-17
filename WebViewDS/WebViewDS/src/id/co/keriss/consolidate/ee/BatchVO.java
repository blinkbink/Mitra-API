package id.co.keriss.consolidate.ee;

public class BatchVO{
	
	//private String tid, batch, cardno, approval, date, total, status;
	private String terminal, batch, date, total, status;


	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
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

	public BatchVO() {
		super();
	}
	
	public BatchVO(String terminal, String batch, String date, String total, String status) {
		super();
		this.terminal = terminal;
		this.batch = batch;
		this.date = date;
		this.total = total;
		this.status = status;
	}


}


