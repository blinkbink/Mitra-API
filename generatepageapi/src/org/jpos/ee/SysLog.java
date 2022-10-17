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

package org.jpos.ee;

import java.util.Date;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Helper class used to log entries in the SysLog.
 */
public class SysLog {
    DB db;
    boolean autoCommit;
    public static final int DEBUG    = 0;
    public static final int TRACE    = 1;
    public static final int INFO     = 2;
    public static final int WARN     = 3;
    public static final int ERROR    = 4;
    public static final int CRITICAL = 5;

    /**
     * create a SysLog object with auto commit on.
     * (open/begin/commit/close is not required).
     */
    public SysLog () {
        super ();
        db = new DB();
        autoCommit = true;
    }
    /**
     * create a SysLog object with auto commit off.
     * User is responsible for openning the underlying session
     * and committing the transaction.
     */
    public SysLog (DB db) {
        super();
        this.db = db;
        autoCommit = false;
    }
    /**
     * @param autoCommit set autocommit mode
     */
    public void setAutocommit (boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
    /**
     * @return auto commit mode
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Create a SysLogEvent and add it to the DB.
     * If auto commit is on, then the operation is performed
     * within a transaction, otherwise, it's up to the user
     * to commit or flush the session.
     * @param source this log event source
     * @param type application specific event type (i.e. INFO, WARN, ERROR, ...)
     * @param severity either DEBUG, TRACE, INFO, WARN, ERROR or CRITICAL
     * @param summary summary information
     * @param detail optional detail information
     * @param trace optional trace information
     * @return the newly created SysLogEvent
     */
    public SysLogEvent log (String source, String type, int severity,
        String summary, String detail, String trace)
    {
        SysLogEvent evt = new SysLogEvent ();
        try {
            evt.setDate (new Date());
            evt.setSource (source);
            evt.setType (type);
            evt.setSeverity (severity);
            evt.setSummary (summary);
            evt.setDetail (detail);
            evt.setTrace (trace);

            boolean autoClose = false;
            if (autoCommit && db.session() == null) {
                db.open ();
                autoClose = true;
            }
            if (autoCommit) {
                Transaction tx = db.beginTransaction ();
                db.session().save (evt);
                tx.commit ();
            } else {
                db.session().save (evt);
            }
            if (autoClose)
                db.close ();
        } catch (Throwable t) {
            errorLog (evt, t);
        }
        return evt;
    }
    /**
     * Create a SysLogEvent and add it to the DB.
     * If auto commit is on, then the operation is performed
     * within a transaction, otherwise, it's up to the user
     * to commit or flush the session.
     * @param source this log event source
     * @param type application specific event type (i.e. INFO, WARN, ERROR, ...)
     * @param severity either DEBUG, TRACE, INFO, WARN, ERROR or CRITICAL
     * @param summary summary information
     * @param detail optional detail information
     * @return the newly created SysLogEvent
     */
    public SysLogEvent log (String source, String type, int severity,
        String summary, String detail)
    {
        return log (source, type, severity, summary, detail, null);
    }
    /**
     * Create a SysLogEvent and add it to the DB.
     * If auto commit is on, then the operation is performed
     * within a transaction, otherwise, it's up to the user
     * to commit or flush the session.
     * @param source this log event source
     * @param type application specific event type (i.e. INFO, WARN, ERROR, ...)
     * @param severity either DEBUG, TRACE, INFO, WARN, ERROR or CRITICAL
     * @param summary summary information
     * @return the newly created SysLogEvent
     */
    public SysLogEvent log 
        (String source, String type, int severity, String summary) 
    {
        return log (source, type, severity, summary, null, null);
    }
    private void errorLog (SysLogEvent evt, Throwable t) {
        LogEvent ev = db.getLog().createError ();
        ev.addMessage (new LoggeableSysLogEvent (evt));
        ev.addMessage (t);
        Logger.log (ev);
    }
    /**
     * @param event id
     * @return log event
     */
    public SysLogEvent get (long id) {
        try {
            return (SysLogEvent) 
                db.session().load (SysLogEvent.class, new Long (id));
        } catch (Throwable e) {
            db.getLog().error (e);
        } 
        return null;
    }
}

