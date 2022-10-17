package id.co.keriss.consolidate.ee;

public class DocumentSummary {

	private Long completed;
	private Long waiting;
	private Long needSign;
	
	public DocumentSummary() {
		completed=new Long(0);
		waiting=new Long(0);
		needSign=new Long(0);
	}
	public Long getCompleted() {
		return completed;
	}
	public void setCompleted(Long completed) {
		this.completed = completed;
	}
	public Long getWaiting() {
		return waiting;
	}
	public void setWaiting(Long waiting) {
		this.waiting = waiting;
	}
	public Long getNeedSign() {
		return needSign;
	}
	public void setNeedSign(Long needSign) {
		this.needSign = needSign;
	}
	
	
}
