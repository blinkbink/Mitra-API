package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Floor generated by hbm2java
 */
public class Floor  implements java.io.Serializable {


     private Long id;
     private String name;
     private Store store;
     private Set cassa = new HashSet(0);

    public Floor() {
    }

    public Floor(String name, Store store, Set cassa) {
       this.name = name;
       this.store = store;
       this.cassa = cassa;
    }
   
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public Store getStore() {
        return this.store;
    }
    
    public void setStore(Store store) {
        this.store = store;
    }
    public Set getCassa() {
        return this.cassa;
    }
    
    public void setCassa(Set cassa) {
        this.cassa = cassa;
    }




}


