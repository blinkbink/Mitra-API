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
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.User;
import org.jpos.ee.DB;
import org.jpos.util.DateUtil;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;

public class Show extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        try {
            String id = context.getRequest().getParameter ("id");
            if (id == null) {
                error (context, "The user id was not specified.", true);
                return;
            }
            DB db = getDB (context);
            User u = (User) db.session().load (User.class, new Long (id));
            User login=(User) context.getSession().getAttribute(USER);
            if(!login.hasPermission("admin") && (u.hasPermission("admin")||u.hasPermission("sysconfig")||u.hasPermission("login"))){
            	 sendRedirect (context, context.getRequest().getContextPath() 
                         + "/stop.html"
                     );
            	 return;
            }
            if (u.isDeleted())
                error (context, "The user does not longer exist.", true);
            else
                context.put ("u", u);

            context.put ("dateUtil", new DateUtil());
            
            context.put("userSelect", id);
			context.put("submenu2", "User");
        } catch (ObjectNotFoundException e) {
            error (context, "The user does not exist.", true);
        } catch (HibernateException e) {
            context.getSyslog().error (e);
            error (context, e.getMessage(), true);
        } catch (NumberFormatException e) {
            error (context, "We have received an invalid user id.", true);
        }
    }
}

