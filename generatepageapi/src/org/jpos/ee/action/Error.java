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

import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

public class Error extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        HttpSession session          = context.getSession();
        HttpServletRequest  request  = context.getRequest();
        Object errors = session.getAttribute (ERRORS);
        if (errors != null) {
            context.put (ERRORS, errors);
            session.removeAttribute (ERRORS);
        }
        String msg = request.getParameter ("msg");
        if (msg != null)
            error (context, msg);
    }
}

