package id.co.keriss.consolidate.action.km;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.dao.BankDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.dao.NonTaglisDao;
import id.co.keriss.consolidate.dao.PostpaidDao;
import id.co.keriss.consolidate.dao.PrepaidDao;
import id.co.keriss.consolidate.dao.StoreDao;
import id.co.keriss.consolidate.dao.TerminalDao;
import id.co.keriss.consolidate.dao.TransactionDao;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.CardTransactionVO;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.Nontaglis;
import id.co.keriss.consolidate.ee.Postpaid;
import id.co.keriss.consolidate.ee.Prepaid;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.Transaction;
import id.co.keriss.consolidate.util.ReportUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.iso.ISOUtil;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

public class InfoKey extends ActionSupport {
	private Boolean content = true;
	private String from,to,bank,store,batch, merchant, terminal,statusTrx, trxtype,subs;
	private BigDecimal sumtotal;
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	try{
		int start = 0;
		HttpServletRequest  request  = context.getRequest();
		
		DB db=getDB(context);
		User uservw=(User) context.get(USER);
        User user= uservw!=null?new UserManager(db).findById(uservw.getId()):null;
        KeyDao kdao=new KeyDao(db);
       
        List<Key> keys=kdao.findByUser(String.valueOf(user.getUserdata().getId()));
        context.put("keys",keys);
	}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
            context.getSyslog().error (e);
           // context.put("content", content);
		}
	}
}
