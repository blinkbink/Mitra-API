package id.co.keriss.consolidate.action.km;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.ee.Key;

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
