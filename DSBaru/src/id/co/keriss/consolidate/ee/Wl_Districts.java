package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA

/**
 * Terminal generated by hbm2java
 */
public class Wl_Districts  implements java.io.Serializable {

    private String id;
    private String regency_id;
    private String name;

    public Wl_Districts(){
    	
    }
    
    public Wl_Districts(String id,String regency_id, String name) {
       this.id = id;
       this.regency_id = regency_id;
       this.name = name;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRegency_id() {
		return regency_id;
	}

	public void setRegency_id(String regency_id) {
		this.regency_id = regency_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
}

