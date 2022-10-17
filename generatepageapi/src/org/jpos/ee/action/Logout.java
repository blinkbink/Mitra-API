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

import java.net.HttpCookie;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.Visitor;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.hibernate.Transaction;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Logout extends ActionSupport {

    /**
     * Permits to override 
     * @param context
     * @param cfg
     */
    protected void logout(JPublishContext context, Configuration cfg){
        DB db = getDB (context);
        HttpSession session          = context.getSession();
        
        session.removeAttribute (USER);
        context.remove (USER);
        context.getResponse().addCookie(new Cookie("loginNih", "false"));
        
        Visitor v = (Visitor) context.get (VISITOR);
        if (v.getUser() != null) {
            Transaction tx = db.beginTransaction();
            v.setUser (null);
            tx.commit();
        }
    }
    
    public void execute (JPublishContext context, Configuration cfg) {
        HttpSession session          = context.getSession();
        HttpServletRequest  request  = context.getRequest();

        try {
        	
            logout(context,cfg);
//          clear the session, so that next login begins clear
            session.invalidate();
            
//            String loginUrl    = request.getContextPath() + "/login.html";
//            sendRedirect (context, request.getContextPath() 
//                    + "/login.html"
//                );

            
        } catch (Exception e) {
            context.getSyslog().error (e);
        }
    }
}

