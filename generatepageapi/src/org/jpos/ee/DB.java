

package org.jpos.ee;

import java.io.Serializable;
import java.util.List;
import java.util.Iterator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.JDBCException;
import org.hibernate.Hibernate;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.type.Type;
import org.jpos.util.Log;
import org.jpos.core.Sequencer;

/**
 * @author Alejandro P. Revilla
 * @version $Revision: 1.5 $ $Date: 2004/12/09 00:50:14 $
 *
 * DB encapsulate some housekepping specific 
 * to Hibernate O/R mapping engine
 */
public class DB {
    protected static SessionFactory sf;
    protected static Configuration cfg;
    private Session session;
    private Log log;
	private int i=0;

    static {
        try {
            init ();
        } catch (Exception e) {
            // now we're in deep trouble
            e.printStackTrace();
        }
    }
    public DB () {
        super();
    }
    public DB (Log log) {
        super ();
        setLog (log);
    }
    private static void init () {
        synchronized (DB.class) {
            if (cfg != null)
                return;

            cfg = new Configuration().configure("hibernate.cfg.xml");
            sf  = cfg.buildSessionFactory();
        }
    }
    
    public void initManual(){
    	init();
    }
    /**
     * @return Hibernate's session factory
     */
    public SessionFactory getSessionFactory () {
        return sf;
    }
//    /**
//     * Creates database schema
//     * @param outputFile optional output file (may be null)
//     * @param create true to actually issue the create statements
//     */
//    public void createSchema (String outputFile, boolean create) 
//        throws HibernateException 
//    {
//        SchemaExport export = new SchemaExport (cfg);
//        if (outputFile != null) {
//            export.setOutputFile (outputFile);
//            export.setDelimiter (";");
//        }
//        export.create (true, create);  
//    }
    /**
     * open a new HibernateSession if none exists 
     * @throws HibernateException
     * @return HibernateSession associated with this DB object
     */
    public synchronized Session open () {
        if (session == null) {
            session = sf.openSession ();
//            System.out.println("DATA DEBUG : open session "+i);
        }
        return session;
    }
    /**
     * close hibernate session
     * @throws HibernateException
     */
    public synchronized void close ()  {
        if (session != null) {
            session.close ();
            session = null;
        }
    }
    /**
     * @return session hibernate Session 
     */
    public Session session () {
    	if(!session.isOpen()) {
    		open();
    	}
        return session;
    }
    /**
     * handy method used to avoid having to call db.session().save (xxx)
     * @param obj to save
     */
    public void save (Object obj) {
        session.save (obj);
    }
    /**
     * @return newly created Transaction
     * @throws HibernateException
     */
    public synchronized Transaction beginTransaction () 
    {
        return session.beginTransaction ();
    }
    /**
     * @param  timeout in seconds
     * @return newly created Transaction
     * @throws HibernateException
     */
    public synchronized Transaction beginTransaction (int timeout) 
    {
        Transaction tx = session.beginTransaction ();
        if (timeout > 0)
            tx.setTimeout (timeout);
        return tx;
    }
    public synchronized Log getLog () {
        if (log == null)
            log = Log.getLog ("Q2", "DB"); // Q2 standard Logger
        return log;
    }
    public synchronized void setLog (Log log) {
        this.log = log;
    }
}

