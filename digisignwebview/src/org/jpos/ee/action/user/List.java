/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.ee.action.user;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;


import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.UserManager;
import org.jpos.ee.DB;
import org.hibernate.HibernateException;

public class List extends ActionSupport {
    @Override
	public void execute (JPublishContext context, Configuration cfg) {
        UserManager mgr = new UserManager (getDB (context));
        DB db = getDB (context);
		 Date time = new Date();
		 SimpleDateFormat date=new SimpleDateFormat("yyyyMMddHHmmss");
//		 ISOMsg m=new ISOMsg();
//		 m.setPackager(new ReqBSMStarlinkPackager());
//		try{
//post
//			 m.set(4,"3600000000567685");
//			 m.set(11,"000000000006");
//			 m.set(12, "20130827162910");
//			 m.set(15, "20130828");
//			 m.set(26, "6010");
//			 m.set(32, "0000007");
//			 m.set(33, "4510210");
//			 m.set(39, "0000");
//			 m.set(41, "8167332212345678");
//			 m.set(42, "8000000001234999");
//			 m.set(48, "33333335121000042394409126O4551210000423920130827154201ABD KADIR                51102022-1234567    R1  000000900000006400201009201002200000000000000075570000000000000000000000000027000000234230002358100000000000000000000000000000000201010201002200000000000000168470000000000000000000000000024000000278530002816000000000000000000000000000000000201011201002200000000000000094545000000000000000000000000021000000496770004983900000000000000000000000000000000201012201002200000000000000132700000000000000000000000000018000000283080002850400000000000000000000000000000000");
// Prepaid		
//			 msg.set(4,"3600000000020000");
//			 msg.set(11,"000000000022");
//			 msg.set(12, "20130827162910");
//			 msg.set(15, "20130828");
//			 msg.set(26, "6010");
//			 msg.set(32, "0000007");
//			 msg.set(33, "4510210");
//			 msg.set(39, "0000");
//			 msg.set(41, "8167332212345678");
//			 msg.set(42, "8000000001234999");
//			 msg.set(48, "00000000111215037851121053440001A95B976BE284AA487E35B2D1F858A4C0A37116666579456168791314920494000099755H.RAHMAD                 R1  0000009000200001600002000000000020000000000200001362962000000000020000017037042000000282039200870940704206376");
//		
//			
//NonTaglis
//			 msg.set(4,"3600000000012345");
//			 msg.set(11,"000000000009");
//			 msg.set(12, "20130827162910");
//			 msg.set(15, "20130828");
//			 msg.set(26, "6010");
//			 msg.set(32, "0000007");
//			 msg.set(33, "4510210");
//			 msg.set(39, "0068");
//			 msg.set(41, "8167332212345678");
//			 msg.set(42, "8000000001234999");
//			 msg.set(48, "33333335343111000169000PEMASANGAN LISTRIK BARU  2013090220130903000000000000ABDUL KADIR NANANANAN    00000000000000000000000000000000708E257383I80I95892013090215082653121                                   11111111111111120000000000139450020000000000123456720000160000");
		
//		}
//		catch (Exception e) {
//	        System.out.println("set data error");
//
//		}
//		
//		 String field42=m.getString(42);
//			
//			int rc=Integer.parseInt(m.getString(39));
//			System.out.println(rc);
			
			
			//save data into database nontaglis
//			try{ 
//		        BatchDao btcdao=new BatchDao(db);
//		        BankDao bankdao=new BankDao(db);
//		        MerchantDao mdao=new MerchantDao(db);
//		        TerminalDao tdao=new TerminalDao(db);
//		        NonTaglisDao nondao=new NonTaglisDao(db);
//		        PartnerDao partnerDao=new PartnerDao(db);
//		        Bank bank=null;
//		        Merchant merchant=null;
//		        Terminal terminal=null;
//		        Batch btc=null;
//		        Partner partner=null;
//		        
//		        bank=bankdao.findByBankCode(msg.getString(32));
//		        if(bank==null){
//		        	bank=new Bank();
//		        	bank.setAquirer(true);
//		        	bank.setName("default-"+msg.getString(32));
//		        	bank.setBank_code(msg.getString(32));
//		        	bankdao.createBank(bank);
//		        }
//		        partner=partnerDao.findByPid(msg.getString(33));
//		        if(partner==null){
//		        	partner=new Partner();
//		        	partner.setName("default-"+msg.getString(33));
//		        	partner.setPid(msg.getString(33));
//		        	partnerDao.createPartner(partner);
//		        }
//		        merchant=mdao.findByMid(field42,msg.getString(33));
//		        if(merchant==null){
//		        	merchant= new Merchant();
//		        	merchant.setMid(field42);
//		        	merchant.setBank(bank);
//		        	merchant.setName("default-"+field42);
//		        	merchant.setPartner(partner);
//		        	mdao.createMerchant(merchant);
//		        }
//		        terminal=tdao.findByTidMid(msg.getString(41), field42);
//		        if(terminal==null){
//		        	terminal=new Terminal();
//		        	terminal.setMerchant(merchant);
//		        	terminal.setTid(msg.getString(41));
//		        	terminal.setMcode(msg.getString(26));
//		        	tdao.createTerminal(terminal);
//		        }
//		        btc=btcdao.findUnsettle(msg.getString(41), field42);
//		        if(btc==null){
//		        	System.out.println("BATCH IS NULL, CREATE BATCH");
//					btc = new Batch();
//					btc.setAmount((long)0);
//					btc.setTerminal(terminal);
//					btc.setSettledate(new Date());
//					btc.setNumber(btcdao.maxBatch(msg.getString(41), field42)+new Integer(1));
//					btc.setSettled(false);
//					btcdao.createbatch(btc);
//		        }else{
//					System.out.println("BATCH EXISTED batch id = :"+btc.getId());
//					SimpleDateFormat fm=new SimpleDateFormat("yyyy-MM-dd");
//		        	Date now=new Date();
//		        	if(!fm.format(btc.getSettledate()).equals(fm.format(now))){
//						System.out.println("BATCH EXISTED batch id = :"+btc.getId()+ " settle");
//						btc.setSettled(true);
//		        		btcdao.updatebatch(btc);
//		        		btc = new Batch();
//		    			btc.setAmount((long)0);
//		    			btc.setTerminal(terminal);
//		    			btc.setSettledate(new Date());
//		    			btc.setNumber(btcdao.maxBatch(msg.getString(41), field42)+new Integer(1));
//		    			btc.setSettled(false);
//		    			btcdao.createbatch(btc);
//		        	}
//					
//				}
//				ISOMsg msg48 =null;
//				
//				if(rc==0){
//					Nontaglis post=new Nontaglis();
//					
//
//					msg48=new ISOMsg(48);
//					try {
//						msg48.setPackager(new SL_RespondNontagihanPayment48());
//						msg48.unpack(msg.getBytes(48));
//					} catch (ISOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					System.out.println("packager 48");
//					for(int a=0; a<19; a++){
//						System.out.println("[a] :"+msg48.getString(a));
//					}
//					//amount trx
//					BigDecimal amt=new BigDecimal(ISOUtil.unPadLeft(msg.getString(4).substring(4,16),'0'));
//					double dec=Math.pow(10, Integer.parseInt(msg.getString(4).substring(3,4)));
//					amt=amt.divide(new BigDecimal(dec));
//
//					//admin charge
//					BigDecimal admin_ch=new BigDecimal(msg48.getString(18));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(17)));
//					admin_ch=admin_ch.divide(new BigDecimal(dec));
//
//					
//					//pln bill
//					BigDecimal plnBill=new BigDecimal(msg48.getString(16));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(15)));
//					plnBill=plnBill.divide(new BigDecimal(dec));
//					
//					BigDecimal transTotal=new BigDecimal(msg48.getString(14));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(13)));
//					transTotal=transTotal.divide(new BigDecimal(dec));
//					
//					
//					post.setAdmin_charge(admin_ch);
//				    post.setAmount(amt);
//				    post.setApprovalcode(null);
//				    post.setApprovaltime(time);
//				    post.setBank(bank);
//				    post.setBatch(btc);
//				    post.setCardno("");
//				    post.setCassa(null);
//				    post.setMinor_amount(msg.getString(4).substring(3,4));
//				    post.setMinor_admin(msg48.getString(17));
//				    post.setMinor_pln(msg48.getString(15));
//				    post.setMinor_total(msg48.getString(13));
//				    post.setOnus(false);
//				    post.setPln_bill(plnBill);
//				    post.setPln_ref(msg48.getString(8));
//				    post.setProduct(null);
//				    post.setReg_num(msg48.getString(1));
//				    post.setReg_trans(msg48.getString(4));
//				    post.setSource("");
//				    post.setSwitch_ref(msg48.getString(9));
//				    post.setSubs_id(msg48.getString(6));
//				    post.setTracenumber(Integer.parseInt(msg.getString(11)));
//				    post.setTransactiontype("Non tagihan Listrik");
//				    post.setType("SUCCESS");       
//				    post.setTrans_code(msg48.getString(2));
//				    post.setTrans_total(transTotal);
//			        nondao.createNontaglis(post);
//			        try{
//				        btc.setAmount(btc.getAmount()+amt.longValue());
//				        btcdao.updatebatch(btc);
//			        }
//			        catch(Exception e){
//			        	System.out.println("batch update error");
//			        }
//				}
//				else{
//					BigDecimal amt=new BigDecimal(ISOUtil.unPadLeft(msg.getString(4).substring(4,16),'0'));
//					double dec=Math.pow(10, Integer.parseInt(msg.getString(4).substring(3,4)));
//					amt=amt.divide(new BigDecimal(dec));
//					id.co.keriss.consolidate.ee.Transaction trs=new id.co.keriss.consolidate.ee.Transaction();
//					TransactionDao transDao=new TransactionDao(db);
//					trs.setAmount(amt);
//					trs.setApprovaltime(time);
//					trs.setBank(bank);
//					trs.setBatch(btc);
//					trs.setCardno("");
//					trs.setCassa(null);
//					trs.setOnus(false);
//					trs.setProduct(null);
//					trs.setSource("");
//					trs.setTracenumber(Integer.parseInt(msg.getString(11)));
//					trs.setTransactiontype("Non tagihan Listrik");
//					if(rc==68)trs.setType("TIMEOUT");
//					else trs.setType("RC"+String.valueOf(rc));
//					transDao.createTransaction(trs);
//					
//				}
//			}
//			catch(Exception e){
//				System.out.println("data extract error");
//
//			}
			
			//save data into database Prepaid
//			try{ 
//				ISOMsg msg48 =null;
//
//		        BatchDao btcdao=new BatchDao(db);
//		        BankDao bankdao=new BankDao(db);
//		        MerchantDao mdao=new MerchantDao(db);
//		        TerminalDao tdao=new TerminalDao(db);
//		        PrepaidDao pdao=new PrepaidDao(db);
//		        PartnerDao partnerDao=new PartnerDao(db);
//		        Bank bank=null;
//		        Merchant merchant=null;
//		        Terminal terminal=null;
//		        Batch btc=null;
//		        Partner partner=null;
//		        
//		        bank=bankdao.findByBankCode(msg.getString(32));
//		        if(bank==null){
//		        	bank=new Bank();
//		        	bank.setAquirer(true);
//		        	bank.setName("default-"+msg.getString(32));
//		        	bank.setBank_code(msg.getString(32));
//		        	bankdao.createBank(bank);
//		        }
//		        partner=partnerDao.findByPid(msg.getString(33));
//		        if(partner==null){
//		        	partner=new Partner();
//		        	partner.setName("default-"+msg.getString(33));
//		        	partner.setPid(msg.getString(33));
//		        	partnerDao.createPartner(partner);
//		        }
//		        merchant=mdao.findByMid(field42,msg.getString(33));
//		        if(merchant==null){
//		        	merchant= new Merchant();
//		        	merchant.setMid(field42);
//		        	merchant.setBank(bank);
//		        	merchant.setName("default-"+field42);
//		        	merchant.setPartner(partner);
//		        	mdao.createMerchant(merchant);
//		        }
//		        terminal=tdao.findByTidMid(msg.getString(41), field42);
//		        if(terminal==null){
//		        	terminal=new Terminal();
//		        	terminal.setMerchant(merchant);
//		        	terminal.setTid(msg.getString(41));
//		        	terminal.setMcode(msg.getString(26));
//		        	tdao.createTerminal(terminal);
//		        }
//		        btc=btcdao.findUnsettle(msg.getString(41), field42);
//		        if(btc==null){
//		        	System.out.println("BATCH IS NULL, CREATE BATCH");
//					btc = new Batch();
//					btc.setAmount((long)0);
//					btc.setTerminal(terminal);
//					btc.setSettledate(new Date());
//					btc.setNumber(btcdao.maxBatch(msg.getString(41), field42)+new Integer(1));
//					btc.setSettled(false);
//					btcdao.createbatch(btc);
//		        }else{
//					System.out.println("BATCH EXISTED batch id = :"+btc.getId());
//					SimpleDateFormat fm=new SimpleDateFormat("yyyy-MM-dd");
//		        	Date now=new Date();
//		        	if(!fm.format(btc.getSettledate()).equals(fm.format(now))){
//						System.out.println("BATCH EXISTED batch id = :"+btc.getId()+ " settle");
//						btc.setSettled(true);
//		        		btcdao.updatebatch(btc);
//		        		btc = new Batch();
//		    			btc.setAmount((long)0);
//		    			btc.setTerminal(terminal);
//		    			btc.setSettledate(new Date());
//		    			btc.setNumber(btcdao.maxBatch(msg.getString(41), field42)+new Integer(1));
//		    			btc.setSettled(false);
//		    			btcdao.createbatch(btc);
//		        	}
//					
//				}
//			      
//				
//				if(rc==0){
//					Prepaid pr=new Prepaid();
//					
//
//					msg48=new ISOMsg(48);
//					try {
//						msg48.setPackager(new SL_RespondPrepaidPurchase48());
//						msg48.unpack(msg.getBytes(48));
//					} catch (ISOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					//amount trx
//					BigDecimal amt=new BigDecimal(ISOUtil.unPadLeft(msg.getString(4).substring(4,16),'0'));
//					double dec=Math.pow(10, Integer.parseInt(msg.getString(4).substring(3,4)));
//					amt=amt.divide(new BigDecimal(dec));
//					
//					//admin charge
//					BigDecimal admin_ch=new BigDecimal(msg48.getString(12));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(11)));
//					admin_ch=admin_ch.divide(new BigDecimal(dec));
//					
//					//angsuran
//					BigDecimal angsuran=new BigDecimal(msg48.getString(20));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(19)));
//					angsuran=angsuran.divide(new BigDecimal(dec));
//					
//					//materai
//					BigDecimal materai=new BigDecimal(msg48.getString(14));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(13)));
//					materai=materai.divide(new BigDecimal(dec));
//					
//					//power kwh
//					BigDecimal powerKwh=new BigDecimal(msg48.getString(24));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(23)));
//					powerKwh=powerKwh.divide(new BigDecimal(dec));
//					
//					//power purchase
//					BigDecimal powerPurchase=new BigDecimal(msg48.getString(22));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(21)));
//					powerPurchase=powerPurchase.divide(new BigDecimal(dec));
//					
//					//power purchase
//					BigDecimal ppj=new BigDecimal(msg48.getString(18));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(17)));
//					ppj=ppj.divide(new BigDecimal(dec));
//					
//					//power purchase
//					BigDecimal ppn=new BigDecimal(msg48.getString(16));
//					dec=Math.pow(10,Integer.parseInt(msg48.getString(15)));
//					ppn=ppn.divide(new BigDecimal(dec));
//					
//					pr.setAdmin_charge(admin_ch);
//			        pr.setAmount(amt);
//			        pr.setAngsuran(angsuran);
//			        pr.setApprovalcode(null);
//			        pr.setApprovaltime(time);
//			        pr.setBank(bank);
//			        pr.setBatch(btc);
//			        pr.setCardno("");
//			        pr.setCassa(null);
//			        pr.setMaterai(materai);
//			        pr.setMinor_admin(msg48.getString(11));
//			        pr.setMinor_amount(msg.getString(4).substring(3,4));
//			        pr.setMinor_angsuran(msg48.getString(19));
//			        pr.setMinor_kwh(msg48.getString(23));
//			        pr.setMinor_materai(msg48.getString(13));
//			        pr.setMinor_ppj(msg48.getString(17));
//			        pr.setMinor_ppn(msg48.getString(15));
//			        pr.setMinor_purchase(msg48.getString(21));
//			        pr.setNo_meter(msg48.getString(1));
//			        pr.setOnus(false);
//			        pr.setPln_ref(msg48.getString(4));
//			        pr.setPower_kwh(powerKwh);
//			        pr.setPower_purchase(powerPurchase);
//			        pr.setPpj(ppj);
//			        pr.setPpn(ppn);
//			        pr.setProduct(null);
//			        pr.setSource("");
//			        pr.setSwitch_ref(msg48.getString(5));
//			        pr.setToken(msg48.getString(25));
//			        pr.setTracenumber(Integer.parseInt(msg.getString(11)));
//			        pr.setTransactiontype("Prepaid PLN");
//			        pr.setType("SUCCESS");
//			        pdao.createPrepaid(pr);
//			        try{
//				        btc.setAmount(btc.getAmount()+amt.longValue());
//				        btcdao.updatebatch(btc);
//			        }
//			        catch(Exception e){
//			        	System.out.println("batch update error");
//			        }
//				}
//				else{
//					//amount trx
//					BigDecimal amt=new BigDecimal(ISOUtil.unPadLeft(msg.getString(4).substring(4,16),'0'));
//					double dec=Math.pow(10, Integer.parseInt(msg.getString(4).substring(3,4)));
//					amt=amt.divide(new BigDecimal(dec));
//					
//					id.co.keriss.consolidate.ee.Transaction trs=new id.co.keriss.consolidate.ee.Transaction();
//					TransactionDao transDao=new TransactionDao(db);
//					trs.setAmount(amt);
//					trs.setApprovaltime(time);
//					trs.setBank(bank);
//					trs.setBatch(btc);
//					trs.setCardno("");
//					trs.setCassa(null);
//					trs.setOnus(false);
//					trs.setProduct(null);
//					trs.setSource("");
//					trs.setTracenumber(Integer.parseInt(msg.getString(11)));
//					trs.setTransactiontype("Prepaid PLN");
//					if(rc==68)trs.setType("TIMEOUT");
//					else trs.setType("RC"+String.valueOf(rc));
//					transDao.createTransaction(trs);
//					
//				}
//			}
//			catch(Exception e){
//				System.out.println("data extract error");
//
//			}
			
//			int length48= m.getString(48).length();
//			System.out.println("field 48 length: "+ length48);
//			try {
//				m.set(48, ISOUtil.padright(m.getString(48), 899, '0'));
//			} catch (ISOException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}
//			
//			ISOMsg msg48 =null;
//
//			
//
//			
//			//save data into database
//			try{ 
//		        BatchDao btcdao=new BatchDao(db);
//		        BankDao bankdao=new BankDao(db);
//		        MerchantDao mdao=new MerchantDao(db);
//		        TerminalDao tdao=new TerminalDao(db);
//			    PostpaidDao pdao=new PostpaidDao(db);
//			    PartnerDao partnerDao=new PartnerDao(db);
//		        Bank bank=null;
//		        Merchant merchant=null;
//		        Terminal terminal=null;
//		        Batch btc=null;
//		        Partner partner=null;
//		        
//		        bank=bankdao.findByBankCode(m.getString(32));
//		        if(bank==null){
//		        	bank=new Bank();
//		        	bank.setAquirer(true);
//		        	bank.setName("default-"+m.getString(32));
//		        	bank.setBank_code(m.getString(32));
//		        	bankdao.createBank(bank);
//		        }
//		        partner=partnerDao.findByPid(m.getString(33));
//		        if(partner==null){
//		        	partner=new Partner();
//		        	partner.setName("default-"+m.getString(33));
//		        	partner.setPid(m.getString(33));
//		        	partnerDao.createPartner(partner);
//		        }
//		        merchant=mdao.findByMid(field42,m.getString(33));
//		        if(merchant==null){
//		        	merchant= new Merchant();
//		        	merchant.setMid(field42);
//		        	merchant.setBank(bank);
//		        	merchant.setName("default-"+field42);
//		        	merchant.setPartner(partner);
//		        	mdao.createMerchant(merchant);
//		        }
//		        terminal=tdao.findByTidMid(m.getString(41), field42);
//		        if(terminal==null){
//		        	terminal=new Terminal();
//		        	terminal.setMerchant(merchant);
//		        	terminal.setTid(m.getString(41));
//		        	terminal.setMcode(m.getString(26));
//		        	tdao.createTerminal(terminal);
//		        }
//		        btc=btcdao.findUnsettle(m.getString(41), field42);
//		        if(btc==null){
//		        	System.out.println("BATCH IS NULL, CREATE BATCH");
//					btc = new Batch();
//					btc.setAmount((long)0);
//					btc.setTerminal(terminal);
//					btc.setSettledate(new Date());
//					btc.setNumber(btcdao.maxBatch(m.getString(41), field42)+new Integer(1));
//					btc.setSettled(false);
//					btcdao.createbatch(btc);
//		        }else{
//					System.out.println("BATCH EXISTED batch id = :"+btc.getId());
//					SimpleDateFormat fm=new SimpleDateFormat("yyyy-MM-dd");
//		        	Date now=new Date();
//		        	if(!fm.format(btc.getSettledate()).equals(fm.format(now))){
//						System.out.println("BATCH EXISTED batch id = :"+btc.getId()+ " settle");
//						btc.setSettled(true);
//		        		btcdao.updatebatch(btc);
//		        		btc = new Batch();
//		    			btc.setAmount((long)0);
//		    			btc.setTerminal(terminal);
//		    			btc.setSettledate(new Date());
//		    			btc.setNumber(btcdao.maxBatch(m.getString(41), field42)+new Integer(1));
//		    			btc.setSettled(false);
//		    			btcdao.createbatch(btc);
//		        	}
//					
//				}
//		      
//				
//				if(rc==0){
//					Postpaid post=new Postpaid();
//
//					msg48=new ISOMsg(48);
//					try {
//						msg48.setPackager(new SL_RespondPostpaidPayment48());
//						msg48.unpack(m.getBytes(48));
//					} catch (ISOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					
//					//amount trx
//					BigDecimal amt=new BigDecimal(ISOUtil.unPadLeft(m.getString(4).substring(4,16),'0'));
//					double dec=Math.pow(10, Integer.parseInt(m.getString(4).substring(3,4)));
//					amt=amt.divide(new BigDecimal(dec));
//					
//					String admin_ch=msg48.getString(11);
//					Long billAmount=(long) 0;
//					Long penaltyFee=(long) 0;
//					Long ppn=(long) 0;
//					String billPeriod="";
//					Long incentive=(long)0;
//					int it=0;
//					for(it=0;it<7;it++){
//						if(!msg48.getString((13*it)+12).equals("000000"))
//						{
//							billPeriod=msg48.getString((13*it)+12);
//							billAmount+=Long.valueOf(msg48.getString((13*it)+15));
//							penaltyFee+=Long.valueOf(msg48.getString((13*it)+18));
//							incentive+=Long.valueOf(msg48.getString((13*it)+16));
//							ppn+=Long.valueOf(msg48.getString((13*it)+17));
//
//						}
//						else break;
//					}
//					
//					Long electricBill=billAmount+penaltyFee;
//					
//			        post.setAdmin_charge(Long.valueOf(admin_ch));
//			        post.setAmount(amt);
//			        post.setApprovalcode(null);
//			        post.setApprovaltime(time);
//			        post.setBank(bank);
//			        post.setBatch(btc);
//			        post.setBill_amount(billAmount);
//			        post.setBill_period(billPeriod);
//			        post.setCardno("");
//			        post.setCassa(null);
//			        post.setElectric_bill(electricBill);
//			        post.setIncentive(incentive);
//			        post.setMinor_amount(m.getString(4).substring(3,4));
//			        post.setOnus(false);
//			        post.setPenalty_fee(penaltyFee);
//			        post.setPpn(ppn);
//			        post.setProduct(null);
//			        post.setSource("");
//			        post.setSwitch_ref(msg48.getString(5));
//			        post.setSubs_id(msg48.getString(1));
//			        post.setTracenumber(Integer.parseInt(m.getString(11)));
//			        post.setTransactiontype("Postpaid PLN");
//			        post.setType("SUCCESS");       
//			        pdao.createPostpaid(post);
//			        try{
//				        btc.setAmount(btc.getAmount()+amt.longValue());
//				        btcdao.updatebatch(btc);
//			        }
//			        catch(Exception e){
//			        	System.out.println("batch update error");
//			        }
//				}
//				else{
//					//amount trx
//					BigDecimal amt=new BigDecimal(ISOUtil.unPadLeft(m.getString(4).substring(4,16),'0'));
//					double dec=Math.pow(10, Integer.parseInt(m.getString(4).substring(3,4)));
//					amt=amt.divide(new BigDecimal(dec));
//					
//					id.co.keriss.consolidate.ee.Transaction trs=new id.co.keriss.consolidate.ee.Transaction();
//					TransactionDao transDao=new TransactionDao(db);
//					trs.setAmount(amt);
//					trs.setApprovaltime(time);
//					trs.setBank(bank);
//					trs.setBatch(btc);
//					trs.setCardno("");
//					trs.setCassa(null);
//					trs.setOnus(false);
//					trs.setProduct(null);
//					trs.setSource("");
//					trs.setTracenumber(Integer.parseInt(m.getString(11)));
//					trs.setTransactiontype("Postpaid PLN");
//					if(rc==68)trs.setType("TIMEOUT");
//					else trs.setType("RC"+String.valueOf(rc));
//					transDao.createTransaction(trs);
//					
//				}
//			}
//			catch(Exception e){
//				System.out.println("data extract error");
//
//			}
			
			 
		
		
		
		
//        System.out.println("save data stralink");
        try {
            context.put ("users", mgr.findAll());
        } catch (HibernateException e) {
            error (context, e.getMessage());
            context.getSyslog().error (e);
        }
    }
}

