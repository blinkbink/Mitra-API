package api;

import javax.servlet.http.HttpServlet;

public class ApplicationLogServlet extends HttpServlet {
	 
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init()
	 {
	     String prefix =  getServletContext().getRealPath("/");
	     String file = getInitParameter("log4j-init-file");
	  
	     // if the log4j-init-file context parameter is not set, then no point in trying
	     if(file != null){
//	      PropertyConfigurator.configure(prefix+file);
	      System.out.println("Log4J Logging started: " + prefix+file);
	     }
	     else{
	      System.out.println("Log4J Is not configured for your Application: " + prefix + file);
	     }     
	 }
}
