package id.co.keriss.consolidate.action.km;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.ee.ConfirmCode;

public class Verification extends ActionSupport {
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
	try{
		DB db=getDB(context);
		User uservw=(User) context.get(USER);
        User user= uservw!=null?new UserManager(db).findById(uservw.getId()):null;
        String hp = user.getUserdata().getNo_handphone();
        context.put("nohp", hp);
        try {
        ConfirmCodeDao ccd = new ConfirmCodeDao(db);	
		ConfirmCode cc = ccd.findByUserID(user.getUserdata().getId());
		if (cc != null) {
	        context.put("verif", "sudah");
	        
			} else {
				context.put("verif", "belum");
			}
        }catch (Exception e) {
        	context.put("verif", "belum");
			e.printStackTrace();
		}
		String nohp = user.getUserdata().getNo_handphone();
	       StringBuilder sb = new StringBuilder(nohp)
                   .insert(4,"-")
                   .insert(9,"-");
                   
String output = sb.toString();
System.out.println(output);
context.put("hp", output);

	}catch (Exception e) {
            e.printStackTrace();
			error (context, e.getMessage());
            context.getSyslog().error (e);
           // context.put("content", content);
		}
	}
}
