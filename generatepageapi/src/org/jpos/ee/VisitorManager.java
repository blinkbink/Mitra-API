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

package org.jpos.ee;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Set;
import java.util.HashMap;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.jpos.iso.ISOUtil;
import javax.servlet.http.Cookie;


/**
 * @author Alejandro Revilla
 */
public class VisitorManager {
    Session session;
    String digest;
    public static final String COOKIE_NAME       = "JPOSEE";
    public static final int    COOKIE_EXPIRATION = 365*24*3600;

    public VisitorManager (Session session) {
        super ();
        this.session = session;
    }
    public VisitorManager (Session session, Cookie[] cookies) {
        this (session);
        if (cookies != null)
            init (cookies);
    }
    private void init (Cookie[] cookies) {
        for (int i=0; i<cookies.length; i++) {
            if (COOKIE_NAME.equals (cookies[i].getName()))
                digest = cookies[i].getValue();
        }
        if (digest == null)
            digest = EEUtil.getRandomHash();
    }
    public Cookie getCookie () {
        Cookie cookie = new Cookie (COOKIE_NAME, digest);
        // cookie.setPath ("/");
        cookie.setMaxAge (COOKIE_EXPIRATION);
        return cookie;
    }
    public Visitor getVisitor ()
        throws HibernateException
    {
        try {
            Visitor v = (Visitor) session.load (Visitor.class, digest);
            if (v != null)
                v.setLastUpdate(new Date()); // force proxy to actually load the object
            return v;
        } catch (ObjectNotFoundException e) { }
        return null;
    }
    public Visitor getVisitor (boolean create) 
        throws HibernateException
    {
        Visitor visitor = getVisitor ();
        if (visitor == null && create) {
            visitor = new Visitor();
            visitor.setId (digest);
            visitor.setProps (new HashMap ());
            visitor.setLastUpdate (new Date());
            session.save (visitor);
        }
        return visitor;
    }
    public void set (Visitor visitor, String prop, String value) 
        throws HibernateException
    {
        visitor.getProps().put (prop, value);
    }
    public void update (Visitor visitor) 
        throws HibernateException
    {
        visitor.setLastUpdate (new Date());
    }
}

