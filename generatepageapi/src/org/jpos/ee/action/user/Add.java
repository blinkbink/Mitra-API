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

import java.util.StringTokenizer;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.ee.Permission;
import org.jpos.ee.DB;
import org.jpos.util.V;

import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;

public  class Add extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        try {
            User me = (User) context.getSession().getAttribute (USER);
            DB db = getDB (context);
            UserManager mgr = new UserManager (db);
//            me=mgr.getUserByNick("qwerty");
            if (me == null || !me.hasPermission (Permission.USERADMIN)) {
                error (context, "Access denied.", true);
                return;
            }
            HttpServletRequest request = context.getRequest();

            if (!"POST".equals (request.getMethod()))
                return;

            String nick  = request.getParameter ("nick");
            String name  = request.getParameter ("name");
            String pass  = request.getParameter ("pass");
            String pass2  = request.getParameter ("pass2");

            int errors = 0;
            if (!V.isNick (nick)) {
                context.put ("errorNick", "Invalid nick name.");
                errors++;
            }
            if (!V.isName (name)) {
                context.put ("errorName", "Invalid name.");
                errors++;
            }
            if (pass == null || pass.length() != 32) {
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
            nick = nick.toLowerCase();
            context.put ("nick", nick);
            context.put ("name", name);

            if (errors > 0)
                return;

            User u = new User();
            u.setNick (nick);
            u.setName (name);
            //u.setPassword (pass);
            setPermissions (request, u, me);


            User uu = mgr.getUserByNick (nick, true);
            if (uu != null) {
                if (uu.isDeleted()) {
                    context.put (
                      "errorNick", "Nickname already in use (deleted).");
                }
                else {
                    context.put ("errorNick", "Nickname already in use.");
                }
                return;
            }
            Transaction tx = db.beginTransaction();
            u.logRevision ("created", me);
            db.session().save (u);
            tx.commit ();
            context.put (MESSAGE, 
                "User '" + nick + "' has been successfully added.");
        } catch (HibernateException e) {
            context.getSyslog().error (e);
            error (context, e.getMessage(), true);
        } 
        
    }
    private void setPermissions (HttpServletRequest request, User u, User me) 
        throws HibernateException
    {
        u.revokeAll ();
        Enumeration en = request.getParameterNames ();
        while (en.hasMoreElements()) {
            String p = (String) en.nextElement();
            if (p.startsWith ("_perm_") && p.length() > 6) {
                String permName = p.substring (6);
                if (me.hasPermission (permName))
                    u.grant (permName);
            }
        }
    }
}

