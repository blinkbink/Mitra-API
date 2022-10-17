package api;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
 
 

public class ContextListener implements ServletContextListener {
 
    /**
     * Initialize log4j when the application is being started
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        // initialize log4j here
        ServletContext context = event.getServletContext();
        String log4jConfigFile = context.getInitParameter("log4j-init-file");
        String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;
         
        System.out.println("initttttttttzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
//        PropertyConfigurator.configure(fullPath);
         
    }
     
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
        System.out.println("destroyyyyyyyyyyyyyyyyyy");

    }  
}
