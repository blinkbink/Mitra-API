package api.response;

import id.co.keriss.consolidate.action.ActionSupport;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

public class forbidden extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		context.getResponse().setStatus(403);
		
	}
	
		
		
		
}
