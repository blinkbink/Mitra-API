package id.co.keriss.consolidate.ee;

public class StoreSumVO {
	public StoreSumVO() {
		super();
	}

	private String mid, name, location, total;

	public StoreSumVO(String mid, String name, String location, String total) {
		super();
		this.mid = mid;
		this.name = name;
		this.location = location;
		this.total = total;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}
}
