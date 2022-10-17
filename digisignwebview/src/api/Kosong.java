package api;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.util.DSAPI;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;


public class Kosong extends ActionSupport implements DSAPI {

  static String basepath="/opt/data-DS/UploadFile/";
  static String basepathPreReg="/opt/data-DS/PreReg/";
  final static Logger log=LogManager.getLogger("digisignlogger");

  @SuppressWarnings("unchecked")
  @Override
  public void execute(JPublishContext context, Configuration cfg) {
    Random rand = new Random(); 
    int i=0;
    HttpServletRequest  request  = context.getRequest();
    
    context.put("domain", "https://"+DOMAIN);
    context.put("domainapi", "https://"+DOMAINAPI);
    context.put("version", rand.nextInt(1000));
    context.put("a", "as");
  }
}