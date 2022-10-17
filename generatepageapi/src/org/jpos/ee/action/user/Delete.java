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

package org.jpos.ee.action.user;

import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.User;
import org.jpos.ee.DB;

import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;

public class Delete extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        System.out.println ("DELETE: " + context.getRequest().getRequestURI());
        Transaction tx = null;
        try {
            String id = context.getRequest().getParameter ("id");
            if (id == null) {
                error (context, "The user id was not specified.", true);
                return;
            }
            DB db = getDB (context);
            User u = (User) db.session().load (User.class, new Long (id));
            User me = (User) context.getSession().getAttribute (USER);
            if(!me.hasPermission("admin") && (u.hasPermission("admin")||u.hasPermission("sysconfig")||u.hasPermission("login"))){
              	 sendRedirect (context, context.getRequest().getContextPath() 
                           + "/stop.html"
                       );
              	 return;
              }
            if (u.equals (me)) {
                error (context, "You're trying to delete yourself. For your protection, we are not going to move forward with this operation.", true);
                return;
            }
            tx = db.beginTransaction();
            // db.session().delete (u);
            u.setDeleted (true);
            u.logRevision ("deleted", me);
            db.session().update (u);
            tx.commit();
            context.put ("u", u);
        } catch (ObjectNotFoundException e) {
            error (context, "The user does not exist.", true);
        } catch (HibernateException e) {
            context.getSyslog().error (e);
            error (context, e.getMessage(), true);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (HibernateException ee) {
                    context.getSyslog().error (ee);
                    error (context, ee.getMessage(), true);  // Ugghh..
                }
            }
        } catch (NumberFormatException e) {
            error (context, "We have received an invalid user id.", true);
        } 
    }
}

