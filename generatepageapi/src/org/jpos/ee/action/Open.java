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

package org.jpos.ee.action;

import java.util.Date;

import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.Visitor;
import org.jpos.ee.VisitorManager;

import javax.servlet.http.Cookie;

import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import javax.servlet.http.HttpServletRequest;

public class Open extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        // some standard stuff
        context.put (DATE, new Date());
        
  	  	
  	  	

        context.getSyslog().info (" OPEN: " + context.getRequest().getRequestURI());
        DB db = getDB (context);
        try {
            db.open ();
            Session hs = db.session();
            User u = (User) context.getSession().getAttribute (USER);
            if (u != null) {
                u = (User) hs.load (User.class, new Long (u.getId()));
                context.put (USER, u);
            } 
            setVisitor (context, hs);
            context.put (HS, hs);
        } catch (Throwable t) {
            context.getSyslog().warn (t);
        }
    }

    private void setVisitor (JPublishContext context, Session hs) 
        throws HibernateException
    {
        HttpServletRequest request = context.getRequest();
        Cookie [] cookies = request.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        VisitorManager vmgr = new VisitorManager (hs, cookies);
        Transaction tx = hs.beginTransaction();
        tx.setTimeout (5);
        Visitor visitor = vmgr.getVisitor (true);
        vmgr.set (visitor, "IP", request.getRemoteAddr());
        vmgr.set (visitor, "HOST", request.getRemoteHost());
        vmgr.update (visitor);
        context.getResponse().addCookie(vmgr.getCookie());
        context.put (VISITOR, visitor);
        context.put("web_title", TITLE );

        User u = visitor.getUser();
        if (u != null && !u.isDeleted()) {
            context.getSession().setAttribute (USER, u);
            context.put (USER, u);
        } 
        try {
            tx.commit ();
        } catch (RuntimeException ex) {
            try {
                tx.rollback();
            } catch (RuntimeException rte) {
                context.getSyslog().warn (rte);
            }
            throw ex;
        }
    }
}

