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
import org.jpos.ee.Visitor;
import org.jpos.ee.SysLogEventBase;
import org.jpos.util.DateUtil;
import org.jpos.util.Validator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;

public class SysLog extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        int PAGE_SIZE = 500;
        HttpServletRequest request = context.getRequest();
        int page = getPage (request.getParameter ("page"));
        try {
            DB db = getDB (context);
            Visitor visitor = getVisitor (context);
            Session hs = db.session();

            setOrderBy (hs, visitor, request);

            String orderBy = getOrderBy (visitor);
            String order   = getOrder   (visitor);
            String where   = getSeverityFilter (hs, visitor, context);

            String q = request.getParameter ("q");
            String f = request.getParameter ("f");
            if (q != null) {
                if (!Validator.isQuery (q)) {
                    error (context, "Invalid query " + q);
                    return;
                }
                if (f == null || "summary.type.source.detail".indexOf(f)<0)
                    f = "summary";
                StringBuffer sb = new StringBuffer (where);
                sb.append ("and ");
                sb.append (f);
                sb.append (" like '%");
                sb.append (q.trim());
                sb.append ("%' ");
                where = sb.toString();
                context.put ("q", q);
                context.put ("f", f);
                context.put (f + "_selected", "selected");
            }
            if (where == null) {
                error (context, "No events to display");
                return;
            }
            StringBuffer qs = new StringBuffer (
              "from syslog in class org.jpos.ee.SysLogEvent where deleted = 0 "
            );
            qs.append (where);
            qs.append (" order by ");
            qs.append (orderBy);
            if (!orderBy.equals ("id"))
                qs.append (", id");
            qs.append (' ');
            qs.append (order);

            int size    = getCount (hs, where);
            int maxPage = (size / PAGE_SIZE) + 1;
            page = page < 1 ? 1 : page;
            page = page > maxPage ? maxPage : page;


            Query query = db.session().createQuery (qs.toString());
            query.setMaxResults (PAGE_SIZE);
            query.setFirstResult (PAGE_SIZE * (page - 1));
            setPagination (context, page, maxPage);
            context.put ("events", query.iterate());
            context.put ("dateUtil", new DateUtil());
            context.put ("order", order);
            context.put ("revOrder", isAscending(visitor) ? "desc" : "asc");
            context.put ("orderBy", orderBy);
        } catch (HibernateException e) {
            error (context, e.getMessage());
            context.getSyslog().error (e);
        }
    }
    private int getCount (Session hs, String where) 
        throws HibernateException
    {
        Query q = hs.createQuery (
            "select count(syslog) from org.jpos.ee.SysLogEvent syslog where deleted = 0 " + where
        );
        return (int) ((Long) q.uniqueResult()).longValue();
    }
    private String getOrderBy (Visitor v) {
        return v.get ("SysLog.ORDER.BY", "id");
    }
    private String getOrder (Visitor v) {
        return v.get ("SysLog.ORDER", "desc");
    }
    private boolean isAscending (Visitor v) {
        return "asc".equals (getOrder (v));
    }
    /**
     * @return where clause, "" (if all options selected) or null (no option)
     */
    private String getSeverityFilter 
        (Session hs, Visitor visitor, JPublishContext context) 
        throws HibernateException
    {
        HttpServletRequest request = context.getRequest();

        String ss = ""; 
        int cnt = 0;
        String[] names = SysLogEventBase.severityAsString;
        StringBuffer where = new StringBuffer();
        if ("on".equals (request.getParameter ("set"))) {
            StringBuffer sb = new StringBuffer ();
            for (int i=0; i < names.length; i++) {
                if (isOn (request, names[i])) {
                    if (sb.length() > 0)
                        sb.append ('.');
                    sb.append (names[i]);
                }
            }
            ss = sb.toString();
            Transaction tx = hs.beginTransaction();
            visitor.set ("SysLog.SEVERITY", ss);
            tx.commit ();
        } 
        else {
            ss = visitor.get (
                "SysLog.SEVERITY", "info.warn.error.critical"
            );
        }
        int j=0;
        for (int i=0; i < names.length; i++) {
            if (checkSeverity (context, names[i], ss)) {
                appendWhere (where, Integer.toString(i));
                j++;
            }
        }
        if (j == names.length) {
            // all options selected, let's remove the where clause
            where.setLength(0);
        } else if (j > 0 && where.length() > 0)
            where.append (") " );

        return j > 0 ? where.toString() : null;
    }
    private void appendWhere (StringBuffer sb, String v) {
        if (sb.length() == 0) {
            sb.append ("and severity in (");
        } else {
            sb.append (',');
        }
        sb.append (v);
    }
    private boolean isOn (HttpServletRequest request, String field) {
        return "on".equals (request.getParameter (field));
    }
    private boolean checkSeverity 
        (JPublishContext context, String field, String severityString)
    {
        if (severityString.indexOf (field) >= 0) {
            context.put ("cb_" + field, "checked");
            return true;
        }
        return false;
    }

    public void setOrderBy 
        (Session hs, Visitor visitor, HttpServletRequest request) 
        throws HibernateException
    {
        String order   = request.getParameter ("order");
        String orderBy = request.getParameter ("orderby");

        if (order != null && orderBy != null) {
            Transaction tx = hs.beginTransaction();
            if (".desc.asc".indexOf (order) > 0)
                visitor.set ("SysLog.ORDER", order);
            if (".id.date.source.type.severity".indexOf (orderBy) > 0)
                visitor.set ("SysLog.ORDER.BY", orderBy);
            tx.commit();
        }
    }
}

