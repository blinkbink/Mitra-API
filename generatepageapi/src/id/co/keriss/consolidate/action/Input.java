package id.co.keriss.consolidate.action;

import id.co.keriss.consolidate.dao.BankDao;
import id.co.keriss.consolidate.dao.CardTypeDao;
import id.co.keriss.consolidate.dao.TerminalDao;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.CardType;
import id.co.keriss.consolidate.ee.Transaction;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

public class Input extends ActionSupport {
	Transaction trans=null;
	BankDao bankdao;
	TerminalDao termdao;
	CardTypeDao typedao;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		try{
		HttpServletRequest  request  = context.getRequest();
		String amount = request.getParameter("amount");
		String bankid = request.getParameter("bank");
		String approvalcode = request.getParameter("approvalcode");
		String tid = request.getParameter("tid");
		String typeid = request.getParameter("type");
		System.out.println(amount+","+bankid+","+approvalcode+","+tid+","+typeid);
		/* TransactionDao tdao = new TransactionDao (getDB (context));
		bankdao = new BankDao(getDB (context));
		termdao = new TerminalDao(getDB (context));
		typedao = new TypeDao(getDB (context));
		if(bankid!=null && tid!=null && typeid!=null&& amount!=null&&approvalcode!=null){
			trans = new Transaction();
			trans.setAmount(new Long(amount));
			trans.setBank(bankdao.findById(new Long(bankid)));
			trans.setTermid(termdao.findById(new Long(tid)));
			trans.setType(typedao.findById(new Long(typeid)));
			trans.setApprovaltime(new Date());
			trans.setFrom_merchant(false);
			trans.setApprovalcode(approvalcode);
			System.out.println(trans);
			tdao.createTransactionui(trans);
		}*/
		List <Bank> bankList = bankdao.findAll();
		List <Terminal> tidList = termdao.findAll();
		List <CardType> typeList = typedao.findAll();
		context.put("bankList", bankList);
		context.put("tidList", tidList);
		context.put("typeList", typeList);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
