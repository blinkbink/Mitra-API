package id.co.keriss.consolidate.ee;

public class Appparameter implements java.io.Serializable {

	private long id;
	private String name;
	private String value;
	
	public Appparameter(){
	}
	
	public Appparameter(long id, String name, String value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
