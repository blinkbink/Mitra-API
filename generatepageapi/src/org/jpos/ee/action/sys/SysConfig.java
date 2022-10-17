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

package org.jpos.ee.action.sys;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.SysLog;
import org.jpos.ee.Visitor;
import org.jpos.ee.Permission;
import org.jpos.ee.BLException;
import org.jpos.ee.status.StatusManager;
import org.jpos.util.DateUtil;
import org.jpos.util.Validator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.exception.ConstraintViolationException;

public class SysConfig extends ActionSupport {
    public static final String DEFAULT_READPERM  = Permission.SYSCONFIG;
    public static final String DEFAULT_WRITEPERM = Permission.SYSCONFIG;
    public void execute (JPublishContext context, Configuration cfg) {

        HttpServletRequest request = context.getRequest();
        int page = getPage (request.getParameter ("page"));
        try {
            DB db = getDB (context);
            User user = getUser (context);
            String action = request.getParameter ("action");
            if ("add".equals (action)) {
                addConfig (db, request, user);
            } 
            else if ("update".equals (action)) {
                updateConfig (db, request, user);
            } 
            else if ("remove".equals (action)) {
                removeConfig (db, request, user);
            }
            Query query = db.session().createQuery (
              "from sysconfig in class org.jpos.ee.SysConfig where (not readperm is null) order by id"
            );
            context.put ("sysconfigs", query.iterate());
        } catch (Exception e) {
            error (context, e.getMessage(), true);
            context.getSyslog().error (e);
        }
    }
    private void assertId (String id) throws BLException {
        if (!Validator.isName (id)) {
            throw new BLException ("Invalid characters in configuration's id.");
        }
        if (id.trim().length() == 0) {
            throw new BLException ("Zero length id is not valid.");
        }
    }
    private void assertValue (String value) throws BLException {
        if (!Validator.isAlpha (value)) {
            throw new BLException (
                "Invalid characters in configuration's value '" + value + "'"
            );
        }
    }
    private void addConfig (DB db, HttpServletRequest request, User user) 
        throws BLException, HibernateException
    {
        String id = request.getParameter ("id");
        String value = request.getParameter ("value");
        if (!user.hasPermission (DEFAULT_WRITEPERM)) {
            throw new BLException ("User does not have permission to add '" + id + "'.");
        }
        assertId (id);
        assertValue (value);
        org.jpos.ee.SysConfig cfg = new org.jpos.ee.SysConfig();
        cfg.setId (id.trim());
        cfg.setValue (value);
        cfg.setReadPerm (DEFAULT_READPERM);
        cfg.setWritePerm (DEFAULT_WRITEPERM);
        Transaction tx = null;
        try {
            tx = db.beginTransaction();
            db.save (cfg);
            SysLog syslog = new SysLog (db);
            syslog.log ("WEB", "AUDIT", SysLog.INFO, 
                "SysConfig." + id + " has been created by " + user.getNickAndId(),
                "value is '" + cfg.getValue() + "'"
            );
            tx.commit();
            tx = null;
        } catch (ConstraintViolationException e) {
            throw new BLException ("'" + id + "' already exists.");
        } finally {
            if (tx != null)
                tx.rollback();
        }
    }
    private void updateConfig (DB db, HttpServletRequest request, User user) 
        throws BLException, HibernateException
    {
        String id = request.getParameter ("id");
        String value = request.getParameter ("value");
        assertId (id);
        assertValue (value);

        Transaction tx = null;
        try {
            tx = db.beginTransaction();
            org.jpos.ee.SysConfig cfg = (org.jpos.ee.SysConfig) 
                db.session().load (org.jpos.ee.SysConfig.class, id);

            if (!user.hasPermission (cfg.getWritePerm())) {
                throw new BLException ("User does not have permission to update '" + id + "'.");
            }

            StringBuffer sb = new StringBuffer();
            recordChange ("value", cfg.getValue(), value, sb);
            if (sb.length() > 0) {
                SysLog syslog = new SysLog (db);
                syslog.log ("WEB", "AUDIT", SysLog.INFO, 
                    "SysConfig." + id + " has been updated by " + user.getNickAndId(),
                    sb.toString());
            }
            cfg.setValue (value);
            tx.commit();
            tx = null;
        } catch (ObjectNotFoundException e) {
            throw new BLException ("'" + id + "' does not exist.");
        } finally {
            if (tx != null)
                tx.rollback();
        }
    }
    private void removeConfig (DB db, HttpServletRequest request, User user) 
        throws BLException, HibernateException
    {
        String id = request.getParameter ("id");
        assertId (id);
        Transaction tx = null;
        try {
            tx = db.beginTransaction();
            org.jpos.ee.SysConfig cfg = (org.jpos.ee.SysConfig) 
                db.session().load (org.jpos.ee.SysConfig.class, id);
            if (!user.hasPermission (cfg.getWritePerm()))
                throw new BLException ("User does not have permission to delete '" + id + "'.");
            SysLog syslog = new SysLog (db);
            syslog.log ("WEB", "AUDIT", SysLog.INFO, 
                "SysConfig." + id + " has been deleted by " + user.getNickAndId(),
                "value was '" + cfg.getValue() + "'"
            );
            db.session().delete (cfg);
            tx.commit();
            tx = null;
        } catch (ObjectNotFoundException e) {
            throw new BLException ("'" + id + "' does not exist.");
        } finally {
            if (tx != null)
                tx.rollback();
        }
    }
}

