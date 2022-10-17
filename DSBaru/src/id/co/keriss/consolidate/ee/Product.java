package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Product generated by hbm2java
 */
public class Product  implements java.io.Serializable {


     private long id;
     private String name;
     private String method;
     private double onus;
     private double offus;
     private Set range = new HashSet(0);
     private Set transaction = new HashSet(0);
     private Bank bank;
     private CardType cardtype;

    public Product() {
    }

    public Product(String name, String method, double onus, double offus, Set range, Set transaction, Bank bank, CardType cardtype) {
       this.name = name;
       this.method = method;
       this.onus = onus;
       this.offus = offus;
       this.range = range;
       this.transaction = transaction;
       this.bank = bank;
       this.cardtype = cardtype;
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
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    public double getOnus() {
        return this.onus;
    }
    
    public void setOnus(double onus) {
        this.onus = onus;
    }
    public double getOffus() {
        return this.offus;
    }
    
    public void setOffus(double offus) {
        this.offus = offus;
    }
    public Set getRange() {
        return this.range;
    }
    
    public void setRange(Set range) {
        this.range = range;
    }
    public Set getTransaction() {
        return this.transaction;
    }
    
    public void setTransaction(Set transaction) {
        this.transaction = transaction;
    }
    public Bank getBank() {
        return this.bank;
    }
    
    public void setBank(Bank bank) {
        this.bank = bank;
    }
    public CardType getCardtype() {
        return this.cardtype;
    }
    
    public void setCardtype(CardType cardtype) {
        this.cardtype = cardtype;
    }




}

