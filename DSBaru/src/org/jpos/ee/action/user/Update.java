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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.action.ActionSupport;
import org.jpos.transaction.Constants;
import org.jpos.util.Validator;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

public class Update extends ActionSupport {
    @Override
	public void execute (JPublishContext context, Configuration cfg) {
        Transaction tx = null;
        try {
            User me = (User) context.getSession().getAttribute (USER);
            if (me == null) {
                error (context, "Access denied.", true);
                return;
            }
            HttpServletRequest request = context.getRequest();
            String id = request.getParameter ("id");
            if (id == null) {
                error (context, "The user id was not specified.", true);
                return;
            }
            DB db = getDB (context);
            User u = db.session().load (User.class, new Long (id));
            if(!me.hasPermission("admin") && (u.hasPermission("admin")||u.hasPermission("sysconfig")||u.hasPermission("login"))){
           	 sendRedirect (context, context.getRequest().getContextPath() 
                        + "/stop.html"
                    );
           	 return;
           }
            if (u.isDeleted()) {
                error (context, "The user does not longer exist.", true);
                return;
            }
            User original = (User) u.clone();

            context.put ("u", u);
            context.put ("nick", u.getNick());
            context.put ("name", u.getName());
            context.put("userSelect", id);
			context.put("submenu2", "User");
            boolean changePasswd = request.getParameter ("passwd") != null;

            if (!"POST".equals (request.getMethod())) {
                if (changePasswd) {
                    context.put ("passwd", Boolean.TRUE);
                }
                return;
            }
            int errors = 0;

            String name = request.getParameter ("name");
            if (!Validator.isName (name)) {
                context.put ("name", name);
                context.put ("errorName", "Invalid name");
                errors++;
            }
            String pass  = request.getParameter ("pass");
            String pass2 = request.getParameter ("pass2");
            if (pass != null) {
                if (pass.length() != 32) {
                    context.put ("errorPass", "Invalid password.");
                    errors++;
                }
                if (pass2 == null || pass2.length() != 32) {
                    context.put ("errorPass2", "Invalid password.");
                    errors++;
                }
                if (pass != null && !pass.equals (pass2)) {
                    context.put ("errorPass",  "Passwords differ.");
                    context.put ("errorPass2", "Please verify.");
                    errors++;
                }
            }
            if (errors > 0)
                return;

            tx = db.beginTransaction();
            StringBuffer rh = new StringBuffer();
            setPermissions (request, u, me);
            recordChange ("name", u.getName(), name, rh);
            u.setName (name);
            if (pass != null) {
                rh.append ("password changed");
                rh.append (BR);
                //u.setPassword (pass);
            }
            rh.append ("Permissions: ");
            rh.append(u.getPermissions());
            rh.append (BR);
            // BeanDiff bd = new BeanDiff 
            // (original, u, new String[] { "name" });
            u.logRevision (rh.toString(), me);

            db.session().update (u);
            tx.commit ();
            context.put ("nick", u.getNick());
            context.put ("name", u.getName());
            context.put (MESSAGE, "User updated.");
        } catch (ObjectNotFoundException e) {
            error (context, "The user does not exist.", true);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    context.getSyslog().error (ex);
                }
            }
        } catch (HibernateException e) {
            context.getSyslog().error (e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    context.getSyslog().error (ex);
                }
            }
            error (context, e.getMessage(), true);
        } catch (NumberFormatException e) {
            error (context, "We have received an invalid user id.", true);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    context.getSyslog().error (ex);
                }
            }
        } catch (Exception e) {
            error (context, "Unexpected exception: " + e.getMessage(), true);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    context.getSyslog().error (ex);
                }
            }
        }
    }
    private void setPermissions (HttpServletRequest request, User u, User me)
        throws HibernateException
    {
        List perms = new ArrayList();
        Enumeration en = request.getParameterNames ();
        while (en.hasMoreElements()) {
            String p = (String) en.nextElement();
            if (p.startsWith ("_perm_") && p.length() > 6) {
                String permName = p.substring (6);
                if (me.hasPermission (permName) || 
                    me.hasPermission (Constants.USERADMIN))
                {
                    perms.add (permName);
                } 
            }
        }
        u.revokeAll();
        Iterator iter = perms.iterator();
        while (iter.hasNext()) {
            u.grant ((String) iter.next());
        }
    }
}

