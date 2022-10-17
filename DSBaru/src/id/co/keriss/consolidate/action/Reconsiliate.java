package id.co.keriss.consolidate.action;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jpos.iso.ISOUtil;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.dao.NonTaglisDao;
import id.co.keriss.consolidate.dao.PostpaidDao;
import id.co.keriss.consolidate.dao.PrepaidDao;
import id.co.keriss.consolidate.ee.Nontaglis;
import id.co.keriss.consolidate.ee.Postpaid;
import id.co.keriss.consolidate.ee.Prepaid;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.Transaction;
import id.co.keriss.consolidate.util.ReportUtil;

public class Reconsiliate extends ActionSupport {
	Transaction transaction=null;
	Terminal terminal=null;
	String env,bankName;
	Long csatot,banktot;
	private Boolean content=true;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		/*			
	        try {
	        	HttpServletRequest  request  = context.getRequest();
	        	TransactionDao tdao = new TransactionDao (getDB (context));
	        	bankName="-";
	        	env="";
	        	if(transaction==null)content=false;
	        	List<EdcReconsiliate> trans = new Vector();
	            bankName=request.getParameter("bank");
	            if(bankName!=null){
	              if(bankName.equalsIgnoreCase("bni")){
	            	env+=" t.bank=1";
	            	bankName="BNI46";
	              }else if(bankName.equalsIgnoreCase("permata")){
	            	env+=" t.bank=3";
	            	bankName="Permata Bank";
	              }else if(bankName.equalsIgnoreCase("citibank")){
	            	env+=" t.bank=2";
	            	bankName="Citibank";
	              }else if(bankName.equalsIgnoreCase("bii")){
	            	env+=" t.bank=4";
	            	bankName="BII";
	              }
	          	}else{
	            	env+=" t.bank=1";
	            	bankName="BNI46";
	            }
	          	if(request.getParameter("day")!=null){
	          		if(request.getParameter("day").equalsIgnoreCase("4/5/2010")){
	          			env+="";
	          		}
	            }else{
	            	env+=" and day(t.approvaltime)=day(current_date())";
	            }
	            env+=" and (t.type!=7 and t.type!=8 and t.type!=9)";
	            TerminalDao termdao = new TerminalDao(getDB (context));
	            List<Terminal> terminals = termdao.findAll();
	            System.out.println("transaction queried : "+terminals.toString());
	        	for(Terminal terminal : terminals){
	        		String tid = terminal.getTid();
	        		EdcReconsiliate recon = new EdcReconsiliate();
	            	recon.setAc(formatNumber(tdao.totalTermAmexCredit(tid,env)));
	            	recon.setAcb(formatNumber(tdao.totalTermAmexCreditBank(tid,env)));
	            	recon.setAcharge(formatNumber(tdao.totalTermAmexCharge(tid,env)));
	            	recon.setAchargeb(formatNumber(tdao.totalTermAmexChargeBank(tid,env)));
	            	recon.setBanktot(formatNumber(tdao.totalTerminalBank(tid, env)));
	            	recon.setJsatot(formatNumber(tdao.totalTerminal(tid, env)));
	            	recon.setMc(formatNumber(tdao.totalTermMasterCredit(tid,env)));
	            	recon.setMcb(formatNumber(tdao.totalTermMasterCreditBank(tid,env)));
	            	recon.setMd(formatNumber(tdao.totalTermMasterDebit(tid,env)));
	            	recon.setMdb(formatNumber(tdao.totalTermMasterDebitBank(tid, env)));
	            	recon.setMid(terminal.getMerchant().getMid());
	            	recon.setStore(terminal.getEdc().getCassa().getStore().getName());
	            	recon.setTid(terminal.getTid());
	            	recon.setVc(formatNumber(tdao.totalTermVisaCredit(tid,env)));
	            	recon.setVcb(formatNumber(tdao.totalTermVisaCreditBank(tid,env)));
	            	recon.setVd(formatNumber(tdao.totalTermVisaDebit(tid,env)));
	            	recon.setVdb(formatNumber(tdao.totalTermVisaDebitBank(tid,env)));
	            	recon.setDiff(formatNumber(tdao.diffTotalTerminal(tid, env)));
	            	trans.add(recon);
	            }
	            context.put("bankName", bankName);
	            context.put("vd",formatNumber(tdao.totalVisaDebit(env)));
	            context.put("vc",formatNumber(tdao.totalVisaCredit(env)));
	            context.put("md",formatNumber(tdao.totalMasterDebit(env)));
	            context.put("mc",formatNumber(tdao.totalMasterCredit(env)));
	            context.put("ac",formatNumber(tdao.totalAmexCredit(env)));
	            context.put("acharge",formatNumber(tdao.totalAmexCharge(env)));
	            context.put("total",formatNumber(tdao.totalGeneral(env)));
	            context.put("vdb",formatNumber(tdao.totalVisaDebitBank(env)));
	            context.put("vcb",formatNumber(tdao.totalVisaCreditBank(env)));
	            context.put("mdb",formatNumber(tdao.totalMasterDebitBank(env)));
	            context.put("mcb",formatNumber(tdao.totalMasterCreditBank(env)));
	            context.put("acb",formatNumber(+tdao.totalAmexCreditBank(env)));
	            context.put("achargeb",formatNumber(tdao.totalAmexChargeBank(env)));
	            context.put("totalb",formatNumber(tdao.totalGeneralBank(env)));
	            context.put("totaldiff",formatNumber(tdao.diffGeneral(env)));
	        	context.put ("trans", trans);
	        	context.put ("content", content);
	        } catch (HibernateException e) {
	            error (context, e.getMessage());
	            context.getSyslog().error (e);
	        }
	        
	        
*/	
		    String from,to,action;
			
			try{
				int start = 0;
				HttpServletRequest  request  = context.getRequest();
				from = ReportUtil.getInstance().now();
				to = ReportUtil.getInstance().now();
				
				ReportUtil util = ReportUtil.getInstance();
				org.jpos.ee.DB db = getDB(context);
				from = request.getParameter("fromdate");
				to = request.getParameter("todate");
				
				if (from == null) {
					from = ReportUtil.getInstance().yesterday();
				}
				if (to == null) {
					to = ReportUtil.getInstance().yesterday();
				}
				
				
				context.put("fromdate", from);
				context.put("todate", to);
				
				if(from==null || to==null){
					from = util.yesterday();
					to = util.yesterday();
				}
	
	
				context.put("from", from);
				context.put("to", to);
				context.put("date", ReportUtil.getInstance().dateTitle(from, to));
				
				String dateTo[]=new String[3];
				String dateFrom[]=new String[3];

				dateTo=to.split("-");
				dateFrom=from.split("-");
				String nameFile=dateTo[2]+dateTo[1]+dateTo[0];
				System.out.println("[1]: "+nameFile);
				action = request.getParameter("process");
				
				if(action==null)return;
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
				PostpaidDao postDao= new PostpaidDao(db);
				PrepaidDao preDao=new PrepaidDao(db);
				NonTaglisDao nonDao=new NonTaglisDao(db);
				
				trx=postDao.findForRecon(tglFrom,tglTo);
				trxPrepaid=preDao.findForRecon(tglFrom,tglTo);
				trxNontaglis=nonDao.findForRecon(tglFrom,tglTo);
				
				int i=0;
				int postpaidSize=0;
				int prepaidSize=0;
				int nontaglisSize=0;
				
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
						String pcid=data.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=data.getBatch().getTerminal().getTid();
						String mcode=data.getMcode();
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
						String pcid=data.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=data.getBatch().getTerminal().getTid();
						String mcode=data.getMcode();
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
						String pcid=data.getBatch().getTerminal().getMerchant().getPartner().getPid().trim();
						String tid=data.getBatch().getTerminal().getTid();
						String mcode=data.getMcode();
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
				
				ReportUtil.getInstance().toRekonSLZip(context,contentPostpaid,contentPrepaid,contentNonTaglis,fm.format(dateNow).substring(0, 8),partner);
				sendRedirect (context, request.getContextPath() 
	                    + "../rekon/rekon-CSA"+fm.format(dateNow).substring(0, 8)+".zip"
	                );
			}catch (Exception e) {
		            e.printStackTrace();
					error (context, e.getMessage());
		            context.getSyslog().error (e);
		           // context.put("content", content);
				}
			}
		
	
	
	
	
	
	
	
	
	
	
	
	private String formatNumber(Long amount){
		//NumberFormat formatter = new DecimalFormat("#,##0.00;(#,##0.00)");
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return ""+formatter.format(amount)+"";
	}

}
