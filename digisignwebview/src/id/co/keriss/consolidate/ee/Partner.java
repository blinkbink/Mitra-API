package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA

/**
 * Bank generated by hbm2java
 */
public class Partner  implements java.io.Serializable {


     private long id;
     private String pid;
     private String name;
     

    public Partner() {
    }
    

	public Partner(long id, String pid, String name) {
		super();
		this.id = id;
		this.pid = pid;
		this.name = name;
	}


	public String getPid() {
		return pid;
	}


	public void setPid(String pid) {
		this.pid = pid;
	}


	public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }



}


