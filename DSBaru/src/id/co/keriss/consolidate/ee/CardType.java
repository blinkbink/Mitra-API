package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;

/**
 * CardType generated by hbm2java
 */
public class CardType  implements java.io.Serializable {


     private Long id;
     private String name;
     private Integer digitid;
     private Set product = new HashSet(0);

    public CardType() {
    }

    public CardType(String name, Integer digitid, Set product) {
       this.name = name;
       this.digitid = digitid;
       this.product = product;
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
    public Integer getDigitid() {
        return this.digitid;
    }
    
    public void setDigitid(Integer digitid) {
        this.digitid = digitid;
    }
    public Set getProduct() {
        return this.product;
    }
    
    public void setProduct(Set product) {
        this.product = product;
    }




}


