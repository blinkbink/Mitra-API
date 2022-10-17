package id.co.keriss.consolidate.util;

import java.math.BigDecimal;
import java.util.Date;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.iso.QMUX;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

public class SaldoTransaction {
	static QMUX mux;
	final static String sync="SYNC";
	private BigDecimal diffamount=null;

	
	public BigDecimal getDiffamount() {
		return diffamount;
	}

	public void setDiffamount(BigDecimal diffamount) {
		this.diffamount = diffamount;
	}

	public SaldoTransaction() {
		// TODO Auto-generated constructor stub
		
		if(mux==null){
			try {
				mux = (QMUX) NameRegistrar.get("mux.saldomux");
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				SystemUtil.getLog("error", "Q2").error(e.getMessage());
			}
		}

	}
	
	/**
	 * memproses saldo ke server deposit
	 * 
	 * @param mode : TOPUP, HOLD, REVERSAL, DEBET, BALANCE
	 * @param mid
	 * @param tid
	 * @param amount
	 * @param stn
	 * @param userid
	 * @param keterangan
	 * @param transaction
	 * @param refnum
	 * @return 00: Sukses, 46: saldo tidak cukup
	 */
	public int processSaldo(String mode, String mid, String tid, BigDecimal amount, int stn, Long userid, String keterangan, Long transaction, String refnum){
		int rc=99;

		ISOMsg m =new ISOMsg();
		String stnSend;
		synchronized (sync) {
			stnSend=new Date().toString();

		}
		try {
			
			m.set(11,ISOUtil.zeropad(String.valueOf(stn), 6));
			if(amount!=null){
				String amt="";
				CharSequence cs= ".";
				System.out.println(amount.toString());

				if(amount.toString().contains(cs)){
					String [] a=amount.toString().split("\\.");

					amt=a[0];
					if(a[1].length()>2) amt+=a[1].substring(0,2);
					else amt+=ISOUtil.padright(a[1].substring(0,a[1].length()), 2, '0');
				}else{
					amt=amount.toString()+"00";
				}
				m.set(4,ISOUtil.zeropad(amt, 12));
			}
			
			if(diffamount!=null){
				String amt="";
				CharSequence cs= ".";
				System.out.println(diffamount.toString());

				if(diffamount.toString().contains(cs)){
					String [] a=diffamount.toString().split("\\.");

					amt=a[0];
					if(a[1].length()>2) amt+=a[1].substring(0,2);
					else amt+=ISOUtil.padright(a[1].substring(0,a[1].length()), 2, '0');
				}else{
					amt=diffamount.toString()+"00";
				}
				m.set(5,ISOUtil.zeropad(amt, 12));
			}
			
			
			if(mode.equals("TOPUP")){
				m.set(41, ISOUtil.zeropad(String.valueOf(userid), 8));
				
			}else{
				m.set(41, ISOUtil.zeropad(String.valueOf(tid), 8));
				m.set(61, userid.toString());
			}
			m.set(42, mid);
			if(transaction!=null)m.set(48, transaction.toString());
			if(refnum!=null)m.set(61,refnum);
			if(keterangan!=null)m.set(63,keterangan);
			m.setHeader(ISOUtil.hex2byte("6000100000"));
			m.setMTI("0200");
			if(mode.equals("TOPUP")){
				m.set(3, "500000");
			}else if(mode.equals("DEBET")){
				m.set(3, "200000");
			}else if(mode.equals("HOLD")){
				m.set(3, "100000");
			}else if(mode.equals("REVERSAL")){
				m.setMTI("0400");
				m.set(3, "100000");
			}else if(mode.equals("BALANCE")){
				m.setMTI("0100");
				m.set(3, "100000");
			}else{
				return 99;
			}
			
			ISOMsg resp=mux.request(m, 30000);
			if(resp!=null){
				rc=Integer.parseInt(resp.getString(39));
			}
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SystemUtil.getLog("error", "Q2").error(e.getMessage());
			rc=99;
		}
				
		return rc;
	}

}
