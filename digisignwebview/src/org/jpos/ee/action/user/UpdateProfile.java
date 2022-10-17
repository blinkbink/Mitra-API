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

import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.ee.BLException;
import org.jpos.ee.EEUtil;
import org.jpos.ee.DB;
import org.jpos.util.Validator;
import org.jpos.iso.ISOUtil;

import org.hibernate.Transaction;
import org.hibernate.HibernateException;

public class UpdateProfile extends ActionSupport {
    @Override
	public void execute (JPublishContext context, Configuration cfg) {
        context.getSyslog().info ("UpdateProfile: " + context.getRequest().getRequestURI());
        String seed = getSeed (context);
        try {
            int errors = 0;
            User u = (User) context.get (USER);
            if (u == null) {
                error (context, "Access denied.", true);
                return;
            }
            HttpServletRequest request = context.getRequest();
            DB db = getDB (context);
            context.put ("nick", u.getNick());
            context.put ("name", u.getName());
            boolean changePasswd = request.getParameter ("passwd") != null;

            if (!"POST".equals (request.getMethod())) {
                if (changePasswd) {
                    context.put ("passwd", Boolean.TRUE);
                    setHash (context);
                }
                return;
            }
            String oldpassword = request.getParameter ("oldpassword");
            String newpassword  = request.getParameter ("pass");
            String newpassword2 = request.getParameter ("pass2");

            if (oldpassword != null) {
                UserManager mgr = new UserManager (db);
                String pass = request.getParameter ("oldpassword");
                if (!mgr.checkPassword (u, seed, pass)) {
                    context.put ("passwd", Boolean.TRUE);
                    context.put ("errorOldPass", "Invalid password");
                    setHash (context);
                    errors++;
                }
                if (newpassword == null || !newpassword.equals (newpassword2)) {
                    context.put ("errorPass", "Invalid new password");
                    context.put ("errorPass2", "or password mismatch.");
                    errors++;
                }
            }
            String name = request.getParameter ("name");
            if (!Validator.isName (name)) {
                context.put ("name", name);
                context.put ("errorName", "Invalid name");
                errors++;
            }
            if (errors > 0)
                return;

            Transaction tx = db.beginTransaction();
            if (!u.getName().equals (name)) {
                u.logRevision (
                    "Profile updated, old name='" + u.getName() 
                    + "' new name='" + name + "'", u);
            }
            u.setName (name);
            if (newpassword != null && newpassword.length() == 32) {
                newpassword = ISOUtil.hexor (newpassword, EEUtil.getHash(seed));
                //u.setPassword (
                    //ISOUtil.hexor (u.getPassword(), newpassword).toLowerCase()
                //);
                context.put (MESSAGE, 
                    "Your profile and new password has been updated.");
            } else {
                context.put (MESSAGE, "Your profile has been updated.");
            }
            db.session().update (u);
            tx.commit ();
            context.put ("name", u.getName());
        } catch (HibernateException e) {
            context.getSyslog().error (e);
            error (context, e.getMessage(), true);
        } catch (NumberFormatException e) {
            error (context, "We have received an invalid user id.", true);
        } catch (BLException e) {
            error (context, e.getMessage());
        }
    }
}

