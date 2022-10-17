package id.co.keriss.consolidate.util;

import id.co.keriss.consolidate.action.ContextConstants;
import id.co.keriss.consolidate.dao.NonTaglisDao;
import id.co.keriss.consolidate.dao.PostpaidDao;
import id.co.keriss.consolidate.dao.PrepaidDao;
import id.co.keriss.consolidate.dao.ReconLogDao;
import id.co.keriss.consolidate.ee.Nontaglis;
import id.co.keriss.consolidate.ee.Postpaid;
import id.co.keriss.consolidate.ee.Prepaid;
import id.co.keriss.consolidate.ee.ReconLog;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jpos.ee.DB;
import org.jpos.iso.ISOUtil;
import org.jpos.transaction.Context;
import org.jpublish.JPublishContext;

public class ReconReport{
	SessionFactory factory = null;
	Configuration cfg=null;
	org.hibernate.Session session = null;
	ReconLogDao rld=null;
	ReconLog recon=null;
	public ReconReport() {
		
		Configuration cfg = new Configuration();
		cfg.configure("hibernate.cfg.xml");
		
		try {
			factory = cfg.buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Failed Crate Session Factory Object "+ex);
			throw new ExceptionInInitializerError(ex);
		}
		session = factory.openSession();
		// TODO Auto-generated constructor stub
	}
	
	public int createRecon(Context context){
			int res=0;
		    String from,to,action;
			ReportUtil util = ReportUtil.getInstance();
			
			try{
				int start = 0;
		
				
				from = ReportUtil.getInstance().yesterday();
				to = ReportUtil.getInstance().yesterday();
				
				
				
	
				String dateTo[]=new String[3];
				String dateFrom[]=new String[3];

				dateTo=to.split("-");
				dateFrom=from.split("-");
				String nameFile=dateTo[2]+dateTo[1]+dateTo[0];
				System.out.println("[1]: "+nameFile);
				
				System.out.println("create rekon");

				/*Rekonsiliasi CSA-Starlink Transaksi PLN*/
				SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmmss");
				String contentPostpaid="DT|CA_ID|CENTRAL_ID|MERCHANT|SREFNUM|IDPEL|BILL_PERIOD|TRAN_AMOUNT|TOTAL_BILL|BILL|RP_INSENTIF|VAT|PENALTY|ADM_CHARGE|TERMINAL_ID\n";
				String contentPrepaid="DT|CENTRAL_ID|MERCHANT|REFNUM|SREFNUM|METERNUM|TRAN_AMOUNT|ADMIN|RP_STAMPDUTY|RP_VAT|RP_PLT|RP_CPI|PP|PU|TOKEN|BANKCODE|PP_ID\n";
				String contentNonTaglis="DT|CENTRAL_ID|MERCHANT|REFNUM|SREFNUM|SUBID|REGNUM|REGD|TRAN_CODE|TOTAL_AMOUNT|TRAN_AMOUNT|ADMIN_CHARGE|BANK_CODE|PPID\n";
				String tglTo=dateTo[2]+"-"+dateTo[1]+"-"+dateTo[0];
				String tglFrom=dateFrom[2]+"-"+dateFrom[1]+"-"+dateFrom[0];
				String partner="4510210";
				List<Postpaid> trx;
				List<Prepaid> trxPrepaid;
				List<Nontaglis> trxNontaglis;
				Date dateNow=new Date();
				
//				Configuration cfgHb=new Configuration();
//				cfgHb.configure("hibernate.cfg.xml");//populates the data of the configuration file  
//			    //creating seession factory object
//			    SessionFactory factory=cfgHb.buildSessionFactory();  
//			    //creating session object
//			    Session session=factory.openSession(); 


//				DB db=new DB();
//			    
//				PostpaidDao postDao= new PostpaidDao(db);
//				PrepaidDao preDao=new PrepaidDao(db);
//				NonTaglisDao nonDao=new NonTaglisDao(db);
//				ReconLogDao rld=new ReconLogDao(db);
				PostpaidDao postDao= new PostpaidDao(session);
				PrepaidDao preDao=new PrepaidDao(session);
				NonTaglisDao nonDao=new NonTaglisDao(session);
				rld=new ReconLogDao(session);

				trx=postDao.findForRecon(tglFrom,tglTo);
				trxPrepaid=preDao.findForRecon(tglFrom,tglTo);
				trxNontaglis=nonDao.findForRecon(tglFrom,tglTo);
				
				int i=0;
				int postpaidSize=0;
				int prepaidSize=0;
				int nontaglisSize=0;
				BigDecimal postAmount=new BigDecimal(0);
				BigDecimal preAmount=new BigDecimal(0);
				BigDecimal nonAmount=new BigDecimal(0);
				
				if(trx!=null){
					i=0;
					Postpaid dataCheckSum = null;
					BigDecimal trxAmount_csum=new BigDecimal(0);
					BigDecimal totalbill_csum=new BigDecimal(0);;
					BigDecimal bill_csum=new BigDecimal(0);;
					BigDecimal inc_csum=new BigDecimal(0);
					Long ppn_csum=(long)0;
					Long penalty_csum=(long)0;
					Long admin_csum=(long)0;
					for(Postpaid data:trx){
						dataCheckSum=data;
						postAmount=postAmount.add(data.getAmount());
						String pcid=data.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=data.getBatch().getTerminal().getTid();
						//String mcode=data.getMcode();
						String mcode=data.getBatch().getTerminal().getMcode();
//						String trxAmount= String.valueOf(data.getAmount().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_amount())))).longValue());
						String totalbill=ISOUtil.padleft(data.getElectric_bill().toString(), 11, '0');
						String bill=ISOUtil.padleft(data.getBill_amount().toString(), 11, '0');
						String inc=ISOUtil.padleft(data.getIncentive().toString(), 11, '0');
						String ppn=ISOUtil.padleft(data.getPpn().toString(), 10, '0');
						String penalty=ISOUtil.padleft(data.getPenalty_fee().toString(), 9, '0');
						String admin=ISOUtil.padleft(data.getAdmin_charge().toString(), 9, '0');
						
//						trxAmount_csum=trxAmount_csum.add(data.getAmount().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_amount())))));
						totalbill_csum=totalbill_csum.add(new BigDecimal(data.getElectric_bill()));
						bill_csum=bill_csum.add(new BigDecimal(data.getBill_amount()));
						inc_csum=inc_csum.add(new BigDecimal(data.getIncentive()));
						ppn_csum+=data.getPpn();
						penalty_csum+=data.getPenalty_fee();
						admin_csum+=data.getAdmin_charge();
//						contentPostpaid+=fm.format(data.getApprovaltime())+"|"+data.getBank().getBank_code()+"|"+pcid+"|"+mcode+"|"+data.getSwitch_ref()+"|"+data.getSubs_id()+"|"+data.getBill_period()+"|"+ISOUtil.padleft(trxAmount, 12, '0')+"|"+totalbill+"|"+bill+"|"+inc+"|"+ppn+"|"+penalty+"|"+admin+"|"+tid+"\n";
						i++;
					}
					if(dataCheckSum!=null){

						String pcid=dataCheckSum.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=dataCheckSum.getBatch().getTerminal().getTid();
						String mcode="0000";
						String trxAmount= String.valueOf(trxAmount_csum.toBigInteger());
						String totalbill=ISOUtil.padleft(String.valueOf(totalbill_csum.toBigInteger()), 11, '0');
						String bill=ISOUtil.padleft(String.valueOf(bill_csum.toBigInteger()), 11, '0');
						String inc=ISOUtil.padleft(String.valueOf(inc_csum.toBigInteger()), 11, '0');
						String ppn=ISOUtil.padleft(String.valueOf(ppn_csum), 10, '0');
						String penalty=ISOUtil.padleft(String.valueOf(penalty_csum), 9, '0');
						String admin=ISOUtil.padleft(String.valueOf(admin_csum), 9, '0');
						contentPostpaid+=fm.format(dateNow).substring(0,8)+"000000|"+dataCheckSum.getBank().getBank_code()+"|"+pcid+"|"+mcode+"|"+ISOUtil.padleft(String.valueOf(i), 32, '0')+"|"+ISOUtil.padleft("0", 12, '0')+"|000000|"+ISOUtil.padleft(trxAmount, 12, '0')+"|"+totalbill+"|"+bill+"|"+inc+"|"+ppn+"|"+penalty+"|"+admin+"|"+ISOUtil.padleft("0", 16, '0')+"\n";
					}
					postpaidSize=i;
				}
				
				if(trxPrepaid!=null){
					i=0;
					Prepaid dataCheckSum=null;
					
					BigDecimal trxAmount_csum= new BigDecimal(0);
					BigDecimal admin_csum=new BigDecimal(0);
					BigDecimal materai_csum=new BigDecimal(0);
					BigDecimal ppn_csum=new BigDecimal(0);
					BigDecimal ppj_csum=new BigDecimal(0);
					BigDecimal angsuran_csum=new BigDecimal(0);
					BigDecimal powerPurchase_csum=new BigDecimal(0);
					BigDecimal powerKWH_csum=new BigDecimal(0);
					
					for(Prepaid data:trxPrepaid){
						dataCheckSum=data;
						preAmount=preAmount.add(data.getAmount());
						String pcid=data.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=data.getBatch().getTerminal().getTid();
						//String mcode=data.getBatch().getTerminal().getMcode();
						String mcode=data.getBatch().getTerminal().getMcode();						
						String trxAmount= String.valueOf(data.getAmount().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_amount())))).longValue()*100);
						String admin=String.valueOf(data.getAdmin_charge().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_admin())))).longValue());
						String materai=String.valueOf(data.getMaterai().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_materai())))).longValue());
						String ppn=String.valueOf(data.getPpn().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_ppn())))).longValue());
						String ppj=String.valueOf(data.getPpj().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_ppj())))).longValue());
						String angsuran=String.valueOf(data.getAngsuran().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_angsuran())))).longValue());
						String powerPurchase=String.valueOf(data.getPower_purchase().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_purchase())))).longValue());
						String powerKWH=String.valueOf(data.getPower_kwh().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_kwh())))).longValue());
						
						trxAmount_csum=trxAmount_csum.add(data.getAmount().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_amount())))));
						admin_csum=admin_csum.add(data.getAdmin_charge().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_admin())))));
						materai_csum=materai_csum.add(data.getMaterai().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_materai())))));
						ppn_csum=ppn_csum.add(data.getPpn().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_ppn())))));
						ppj_csum=ppj_csum.add(data.getPpj().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_ppj())))));
						angsuran_csum=angsuran_csum.add(data.getAngsuran().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_angsuran())))));
						powerPurchase_csum=powerPurchase_csum.add(data.getPower_purchase().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_purchase())))));
						powerKWH_csum=powerKWH_csum.add(data.getPower_kwh().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_kwh())))));
						
						trxAmount=ISOUtil.padleft(trxAmount, 12, '0');
						admin=ISOUtil.padleft(admin, 10, '0');
						materai=ISOUtil.padleft(materai, 10, '0');
						ppn=ISOUtil.padleft(ppn, 10, '0');
						ppj=ISOUtil.padleft(ppj, 10, '0');
						angsuran=ISOUtil.padleft(angsuran, 10, '0');
						powerPurchase=ISOUtil.padleft(powerPurchase, 12, '0');
						powerKWH=ISOUtil.padleft(powerKWH, 10, '0');
						
						contentPrepaid+=fm.format(data.getApprovaltime())+"|"+pcid+"|"+mcode+"|"+data.getPln_ref()+"|"+data.getSwitch_ref()+"|"+data.getNo_meter()+"|"+ISOUtil.padleft(trxAmount, 12, '0')+"|"+admin+"|"+materai+"|"+ppn+"|"+ppj+"|"+angsuran+"|"+powerPurchase+"|"+powerKWH+"|"+data.getToken()+"|"+data.getBank().getBank_code()+"|"+tid+"\n";
						i++;
					}
					if(dataCheckSum!=null){

						String pcid=dataCheckSum.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=dataCheckSum.getBatch().getTerminal().getTid();
						String mcode="0000";
						String trxAmount= ISOUtil.padleft(String.valueOf(trxAmount_csum.toBigInteger())+"00", 12, '0');
						String admin=ISOUtil.padleft(String.valueOf(admin_csum.toBigInteger()), 10, '0');
						String materai=ISOUtil.padleft(String.valueOf(materai_csum.toBigInteger()), 10, '0');
						String ppn=ISOUtil.padleft(String.valueOf(ppn_csum.toBigInteger()), 10, '0');
						String ppj=ISOUtil.padleft(String.valueOf(ppj_csum.toBigInteger()), 10, '0');
						String angsuran=ISOUtil.padleft(String.valueOf(angsuran_csum.toBigInteger()), 10, '0');
						String powerPurchase=ISOUtil.padleft(String.valueOf(powerPurchase_csum.toBigInteger()), 12, '0');;
						String powerKWH=ISOUtil.padleft(String.valueOf(powerKWH_csum.toBigInteger()), 10, '0');
						
						contentPrepaid+=fm.format(dateNow).substring(0,8)+"000000|"+pcid+"|"+mcode+"|"+ISOUtil.padleft("0", 32, '0')+"|"+ISOUtil.padleft(String.valueOf(i), 32, '0')+"|"+ISOUtil.padleft("0", 11, '0')+"|"+ISOUtil.padleft(trxAmount, 12, '0')+"|"+admin+"|"+materai+"|"+ppn+"|"+ppj+"|"+angsuran+"|"+powerPurchase+"|"+powerKWH+"|"+ISOUtil.padleft("0", 20, '0')+"|"+dataCheckSum.getBank().getBank_code()+"|"+ISOUtil.padleft("0", 16, '0')+"\n";
					}
					prepaidSize=i;
				}
				
				if(trxNontaglis!=null){
					i=0;
					Nontaglis dataCheckSum=null;
					BigDecimal total_csum= new BigDecimal(0);
					BigDecimal admin_csum=new BigDecimal(0);
					BigDecimal plnBill_csum=new BigDecimal(0);
					for(Nontaglis data:trxNontaglis){
						dataCheckSum=data;
						nonAmount=nonAmount.add(data.getTrans_total());
						String pcid=data.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=data.getBatch().getTerminal().getTid();
						//String mcode=data.getMcode();
						String mcode=data.getBatch().getTerminal().getMcode();
						String total= String.valueOf(data.getTrans_total().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_total())))).longValue());
						String admin=String.valueOf(data.getAdmin_charge().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_admin())))).longValue());
						String plnBill=String.valueOf(data.getPln_bill().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_pln())))).longValue());

						total=ISOUtil.padleft(total, 17, '0');
						admin=ISOUtil.padleft(admin, 10, '0');
						plnBill=ISOUtil.padleft(plnBill, 17, '0');
						
						total_csum=total_csum.add(data.getTrans_total().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_total())))));
						admin_csum=admin_csum.add(data.getAdmin_charge().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_admin())))));
						plnBill_csum=plnBill_csum.add(data.getPln_bill().multiply(new BigDecimal(Math.pow(10, Integer.parseInt(data.getMinor_pln())))));
						
						contentNonTaglis+=fm.format(data.getApprovaltime())+"|"+pcid+"|"+mcode+"|"+data.getPln_ref()+"|"+data.getSwitch_ref()+"|"+data.getSubs_id()+"|"+ISOUtil.padright(data.getReg_num(), 32, ' ')+"|"+data.getReg_trans()+"|"+data.getTrans_code().substring(1)+"|"+total+"|"+plnBill+"|"+admin+"|"+data.getBank().getBank_code()+"|"+tid+"\n";
						i++;
					}
					if(dataCheckSum!=null){
						String pcid=dataCheckSum.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String mcode="0000";
						String total= ISOUtil.padleft(String.valueOf(total_csum.toBigInteger()), 17, '0');
						String admin=ISOUtil.padleft(String.valueOf(admin_csum.toBigInteger()), 10, '0');
						String plnBill=ISOUtil.padleft(String.valueOf(plnBill_csum.toBigInteger()), 17, '0');
						
						contentNonTaglis+=fm.format(dateNow).substring(0,8)+"000000|"+pcid+"|"+mcode+"|"+ISOUtil.padleft(String.valueOf(i), 32, '0')+"|"+ISOUtil.padleft("0", 32, '0')+"|"+ISOUtil.padleft("0", 12, '0')+"|"+ISOUtil.padleft("0", 32, '0')+"|"+ISOUtil.padleft("0", 8, '0')+"|00|"+total+"|"+plnBill+"|"+admin+"|"+dataCheckSum.getBank().getBank_code()+"|"+ISOUtil.padleft("0", 16, '0')+"\n";
					}
					nontaglisSize=i;
				}
				
				util.getInstance().toRekonSL(context,contentPostpaid,contentPrepaid,contentNonTaglis,fm.format(dateNow).substring(0, 8),partner, postpaidSize, prepaidSize, nontaglisSize);
				
				BigDecimal total_recon= new BigDecimal(0);
				total_recon=total_recon.add(postAmount);
				total_recon=total_recon.add(nonAmount);
				total_recon=total_recon.add(preAmount);
				
				recon=new ReconLog();
				recon.setNontaglis(nontaglisSize);
				recon.setNontaglisamt(nonAmount);
				recon.setPrepaid(prepaidSize);
				recon.setPrepaidamt(preAmount);
				recon.setPostpaid(postpaidSize);
				recon.setPostpaidamt(postAmount);
				recon.setAmount(total_recon);			
				rld.createReconLog(recon);
				
			}catch (Exception e) {
		            e.printStackTrace();
		            //context.getSyslog().error (e);
		           // context.put("content", content);
				}
			return res;
	}

	public void setReconLog(){
			recon.setRecontime(new Date());
			rld.updateReconLog(recon);
	}
	
}
