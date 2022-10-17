package id.co.keriss.consolidate.ee;

public class EdcReconsiliate  implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String store,diff,tid,mid,vd,vc,md,mc,ac,acharge,vdb,vcb,mdb,mcb,acb,achargeb,csatot,banktot;

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public EdcReconsiliate() {
		super();
		vd=vc=md=mc=ac=acharge=vdb=vcb=mdb=mcb=acb=achargeb=csatot=banktot=store=tid=diff=mid="-";
	}

	public EdcReconsiliate(String store, String tid, String mid, String vd,
			String vc, String md, String mc, String ac, String acharge,
			String vdb, String vcb, String mdb, String mcb, String acb,
			String achargeb, String csatot, String banktot) {
		super();
		this.store = store;
		this.tid = tid;
		this.mid = mid;
		this.vd = vd;
		this.vc = vc;
		this.md = md;
		this.mc = mc;
		this.ac = ac;
		this.acharge = acharge;
		this.vdb = vdb;
		this.vcb = vcb;
		this.mdb = mdb;
		this.mcb = mcb;
		this.acb = acb;
		this.achargeb = achargeb;
		this.csatot = csatot;
		this.banktot = banktot;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getVd() {
		return vd;
	}

	public void setVd(String vd) {
		this.vd = vd;
	}

	public String getVc() {
		return vc;
	}

	public void setVc(String vc) {
		this.vc = vc;
	}

	public String getMd() {
		return md;
	}

	public void setMd(String md) {
		this.md = md;
	}

	public String getMc() {
		return mc;
	}

	public void setMc(String mc) {
		this.mc = mc;
	}

	public String getAc() {
		return ac;
	}

	public void setAc(String ac) {
		this.ac = ac;
	}

	public String getAcharge() {
		return acharge;
	}

	public void setAcharge(String acharge) {
		this.acharge = acharge;
	}

	public String getVdb() {
		return vdb;
	}

	public void setVdb(String vdb) {
		this.vdb = vdb;
	}

	public String getVcb() {
		return vcb;
	}

	public void setVcb(String vcb) {
		this.vcb = vcb;
	}

	public String getMdb() {
		return mdb;
	}

	public void setMdb(String mdb) {
		this.mdb = mdb;
	}

	public String getMcb() {
		return mcb;
	}

	public void setMcb(String mcb) {
		this.mcb = mcb;
	}

	public String getAcb() {
		return acb;
	}

	public void setAcb(String acb) {
		this.acb = acb;
	}

	public String getAchargeb() {
		return achargeb;
	}

	public void setAchargeb(String achargeb) {
		this.achargeb = achargeb;
	}

	public String getJsatot() {
		return csatot;
	}

	public void setJsatot(String csatot) {
		this.csatot = csatot;
	}

	public String getBanktot() {
		return banktot;
	}

	public void setBanktot(String banktot) {
		this.banktot = banktot;
	}

}
