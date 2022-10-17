package id.co.keriss.consolidate.ee;

public class InvoiceVO {
	private  String totalTrxBank, totalGrsBank, 
					onusTrxBank, onusGrsBank, 
					offusTrxBank, offusGrsBank, 
					totalTrxBankMdr,totalGrsBankMdr, 
					onusPerBank,onusNetBank, offusPerBank, offusNetBank;

	public String getTotalTrxBank() {
		return totalTrxBank;
	}

	public void setTotalTrxBank(String totalTrxBank) {
		this.totalTrxBank = totalTrxBank;
	}

	public String getTotalGrsBank() {
		return totalGrsBank;
	}

	public void setTotalGrsBank(String totalGrsBank) {
		this.totalGrsBank = totalGrsBank;
	}

	public String getOnusTrxBank() {
		return onusTrxBank;
	}

	public void setOnusTrxBank(String onusTrxBank) {
		this.onusTrxBank = onusTrxBank;
	}

	public String getOnusGrsBank() {
		return onusGrsBank;
	}

	public void setOnusGrsBank(String onusGrsBank) {
		this.onusGrsBank = onusGrsBank;
	}

	public String getOffusTrxBank() {
		return offusTrxBank;
	}

	public void setOffusTrxBank(String offusTrxBank) {
		this.offusTrxBank = offusTrxBank;
	}

	public String getOffusGrsBank() {
		return offusGrsBank;
	}

	public void setOffusGrsBank(String offusGrsBank) {
		this.offusGrsBank = offusGrsBank;
	}

	public String getTotalTrxBankMdr() {
		return totalTrxBankMdr;
	}

	public void setTotalTrxBankMdr(String totalTrxBankMdr) {
		this.totalTrxBankMdr = totalTrxBankMdr;
	}

	public String getTotalGrsBankMdr() {
		return totalGrsBankMdr;
	}

	public void setTotalGrsBankMdr(String totalGrsBankMdr) {
		this.totalGrsBankMdr = totalGrsBankMdr;
	}

	public String getOnusPerBank() {
		return onusPerBank;
	}

	public void setOnusPerBank(String onusPerBank) {
		this.onusPerBank = onusPerBank;
	}

	public String getOnusNetBank() {
		return onusNetBank;
	}

	public void setOnusNetBank(String onusNetBank) {
		this.onusNetBank = onusNetBank;
	}

	public String getOffusPerBank() {
		return offusPerBank;
	}

	public void setOffusPerBank(String offusPerBank) {
		this.offusPerBank = offusPerBank;
	}

	public String getOffusNetBank() {
		return offusNetBank;
	}

	public void setOffusNetBank(String offusNetBank) {
		this.offusNetBank = offusNetBank;
	}

	public InvoiceVO(String totalTrxBank, String totalGrsBank,
			String onusTrxBank, String onusGrsBank, String offusTrxBank,
			String offusGrsBank, String totalTrxBankMdr,
			String totalGrsBankMdr, String onusPerBank, String onusNetBank,
			String offusPerBank, String offusNetBank) {
		super();
		this.totalTrxBank = totalTrxBank;
		this.totalGrsBank = totalGrsBank;
		this.onusTrxBank = onusTrxBank;
		this.onusGrsBank = onusGrsBank;
		this.offusTrxBank = offusTrxBank;
		this.offusGrsBank = offusGrsBank;
		this.totalTrxBankMdr = totalTrxBankMdr;
		this.totalGrsBankMdr = totalGrsBankMdr;
		this.onusPerBank = onusPerBank;
		this.onusNetBank = onusNetBank;
		this.offusPerBank = offusPerBank;
		this.offusNetBank = offusNetBank;
	}

	public InvoiceVO() {
		super();
	}
}
