package id.co.keriss.consolidate.ee;

import id.co.keriss.consolidate.util.ReportUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MerchantVO {
	String id, mid, name, bank, store, address, partner, saldo,username;
	boolean newMerchant;
    private List<Terminal> terminal = new ArrayList<Terminal>();
    
    public List<Terminal> getTerminal() {
        return this.terminal;
    }
    
    public void setTerminal(Set terminal) {
    	for (Object object : terminal) {
			Terminal t=(Terminal) object;
			this.terminal.add(t);
		}
    }
	public MerchantVO() {
		// TODO Auto-generated constructor stub
		newMerchant=false;
	}
	public boolean isNewMerchant() {
		return newMerchant;
	}
	
	public void setNewMerchant(boolean newMerchant) {
		this.newMerchant = newMerchant;
	}
	
	public String getSaldo() {
		return saldo;
	}

	public void setSaldo(Long saldo) {
		this.saldo =ReportUtil.getInstance().formatNumber(saldo);

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setSaldo(String saldo) {
		this.saldo = saldo;
	}

	

}
