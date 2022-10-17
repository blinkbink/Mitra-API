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

package id.co.keriss.consolidate.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.Visitor;
import org.jpos.ee.menu.MenuNode;
import org.jpublish.JPublishContext;
import org.jpublish.action.Action;

import com.anthonyeden.lib.config.Configuration;

public abstract class ActionSupport implements Action, ContextConstants {
    public static final String BR = "<br/>";
    public abstract void execute (JPublishContext context, Configuration cfg);

    /**
     * Get the DB object from context. 
     * If one does not exist, a new one is created.
     * @return a DB object
     */
    public DB getDB (JPublishContext context) {
        DB db = (DB) context.get (DB);
        if (db == null) {
            db = new DB();
            context.put (DB, db);
        }
        return db;
    }
    /**
     * @return context's user or null
     */
    public User getUser (JPublishContext context) {
        return (User) context.get (USER);
    }
    /**
     * @return context's visitor or null
     */
    public Visitor getVisitor (JPublishContext context) {
        return (Visitor) context.get (VISITOR);
    }
    /**
     * Add an error to the context.
     * @param context the Request Context.
     * @param msg an error message to be displayed to the user.
     */
    protected void error (JPublishContext context, String msg) {
        List errors = (List) context.get (ERRORS);
        if (errors == null) {
            errors = new ArrayList();
            context.put (ERRORS, errors);
        }
        errors.add (msg);
    }
    /**
     * Add an error to the context.
     * @param context the Request Context.
     * @param msg an error message to be displayed to the user.
     * @param redirect if true, errors are temporarily stored in session and response is redirected to error.html page.
     */
    protected void error (JPublishContext context, String msg, boolean redirect) 
    {
        error (context, msg);
        if (redirect) {
            context.getSession().setAttribute (ERRORS, context.get (ERRORS));
            sendRedirect (context, 
                context.getRequest().getContextPath() + "/error.html");
        }
    }
    protected void accessDenied (JPublishContext context) 
    {
        sendRedirect (context, 
            context.getRequest().getContextPath() + "/stop.html");
    }
    protected void sendRedirect (JPublishContext context, String url) {
        try {
            context.getResponse().sendRedirect(url);
        } catch (IOException e) {
            context.getSyslog().error (e);
        }
    }

    /**
     * place a new random hash value in the context, and saves it
     * in the context's session
     * @context the Context
     */
    protected void setHash (JPublishContext context) {
        String hash = EEUtil.getRandomHash();
        context.put (HASH, hash);
        context.getSession().setAttribute (HASH, hash);
    }

    /**
     * @return return the requests's seed
     */
    protected String getSeed (JPublishContext context) {
        StringBuffer seed = new StringBuffer (
            context.getRequest().getSession().getId()
        );
        String hash = (String) context.getSession().getAttribute (HASH);
        if (hash != null)
            seed.append (hash);
        return seed.toString();
    }
    /**
     * Put firstPage, previousPage, nextPage and lastPage uris in context.
     * @param context the context
     * @param page current page
     * @param maxPage max page
     */
    protected void setPagination 
        (JPublishContext context, int page, int maxPage) 
    {
        HttpServletRequest request = context.getRequest();
        String baseUri = request.getRequestURI ();
        if (page > 1) {
            context.put ("previousPage",
                baseUri + "?page=" + Integer.toString (page-1)
            );
        }
        if (page > 2) {
            context.put ("firstPage",
                baseUri + "?page=" + Integer.toString (1)
            );
        }
        if (page < maxPage) {
            context.put ("nextPage",
                baseUri + "?page=" + Integer.toString (page+1)
            );
        }
        if (page < maxPage-1) {
            context.put ("lastPage",
                baseUri + "?page=" + Integer.toString (maxPage)
            );
        }
        context.put ("currentPage", Integer.toString(page));
        context.put ("maxPage", Integer.toString (maxPage));
    }

    /**
     * record a property change in a format suitable to be displayed by the
     * revision history.
     * @param propName property name
     * @param oldValue property's old value
     * @param newValue property's new value
     * @param sb a StringBuffer where we optionally place the revision log * message.
     */
    protected void recordChange 
        (String propName, Object oldValue, Object newValue, StringBuffer sb) 
    {
        if (oldValue != null && !oldValue.equals (newValue)) {
            if (sb.length() > 0)
                sb.append (BR);
            sb.append (propName);
            sb.append (": '");
            sb.append (oldValue);
            sb.append ('\'');
            if (newValue != null) {
                sb.append (" ==> ");
                sb.append ('\'');
                sb.append (newValue);
                sb.append ('\'');
            }
        }
    }
    /**
     * record a property change in a format suitable to be displayed by the
     * revision history.
     * @param propName property name
     * @param oldValue property's old value
     * @param newValue property's new value
     * @param sb a StringBuffer where we optionally place the revision log * message.
     */
    protected void recordChange 
        (String propName, long oldValue, long newValue, StringBuffer sb) 
    {
        recordChange (propName, Long.toString(oldValue), Long.toString(newValue), sb);
    }

    /**
     * @param pageString (usually request.getParameter("page"))
     * @return page number (on error, return 1)
     */
    protected int getPage (String pageString) {
        int page = 1;
        if (pageString != null) {
            try {
                page = Integer.parseInt (pageString.trim());
            }
            catch (NumberFormatException e) { 
                // keep 1
            }
        }
        return page;
    }
    /**
     * @param s string containing integer value
     * @param def default value
     * @return int value
     */
    protected int getInteger (String s, int def) {
        int i = def;
        if (s != null) {
            try {
                i = Integer.parseInt (s.trim());
            }
            catch (NumberFormatException e) { 
                // keep i
            }
        }
        return i;
    }
    /** 
     * Gets a parameter off the servlet request and optionally put it in the Context
     * @param paramName the parameter name
     * @param context Request Context
     * @param putInContext if true, the value is placed back in Context
     * @return param value or null
     */
    protected String getParameter (String paramName, JPublishContext context, boolean putInContext) {
        HttpServletRequest request = context.getRequest();
        String value = request.getParameter (paramName);
        if (putInContext && value != null)
            context.put (paramName, value);
        return value;
    }
    /**
     * Gets a parameter off the servlet request and put it in the Context
     * @param paramName the parameter name
     * @param context Request Context
     * @return param value or null
     */
    protected String getParameter (String paramName, JPublishContext context) {
        return getParameter (paramName, context, true);
    }
    
    /**
     * Gets the root MenuNode
     */
    protected MenuNode getMenu (JPublishContext context){
        return (MenuNode)context.get(MENU);
    }
    
}

