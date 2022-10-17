package id.co.keriss.consolidate.ee;

public class Reconciliate  implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String store,ca,cp,casha,cashp,va,vp,ma,mp,storetot;

	public Reconciliate() {
		super();
		store=ca=cp=casha=cashp=va=vp=ma=mp=storetot="-";
	}

	public Reconciliate(String store, String ca, String cp, String casha,
			String cashp, String va, String vp, String ma, String mp,
			String storetot, String totcard, String totcash, String totvoucher,
			String totmore) {
		super();
		this.store = store;
		this.ca = ca;
		this.cp = cp;
		this.casha = casha;
		this.cashp = cashp;
		this.va = va;
		this.vp = vp;
		this.ma = ma;
		this.mp = mp;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getCa() {
		return ca;
	}

	public void setCa(String ca) {
		this.ca = ca;
	}

	public String getCp() {
		return cp;
	}

	public void setCp(String cp) {
		this.cp = cp;
	}

	public String getCasha() {
		return casha;
	}

	public void setCasha(String casha) {
		this.casha = casha;
	}

	public String getCashp() {
		return cashp;
	}

	public void setCashp(String cashp) {
		this.cashp = cashp;
	}

	public String getVa() {
		return va;
	}

	public void setVa(String va) {
		this.va = va;
	}

	public String getVp() {
		return vp;
	}

	public void setVp(String vp) {
		this.vp = vp;
	}

	public String getMa() {
		return ma;
	}

	public void setMa(String ma) {
		this.ma = ma;
	}

	public String getMp() {
		return mp;
	}

	public void setMp(String mp) {
		this.mp = mp;
	}

	public String getStoretot() {
		return storetot;
	}

	public void setStoretot(String storetot) {
		this.storetot = storetot;
	}
}
