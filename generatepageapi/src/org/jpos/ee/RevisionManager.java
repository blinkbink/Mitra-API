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

import java.util.Date;
import java.beans.Expression;
import java.beans.Introspector;
import java.beans.Statement;

public class RevisionManager {
    DB db;
    public RevisionManager (DB db) {
        this.db = db;
    }
    /*
    public RevisionEntry set
        (Object obj, String propName, String newValue, User user) 
        throws BLException
    {
        try {
            Expression expr = new Expression (
                obj,Introspector.decapitalize ("get" + propName), new Object[0]
            );
            expr.execute();

            String oldValue = (String) expr.getValue ();

            if (oldValue == null) {
                if (newValue == null)
                    return null;
            } else {
                if (oldValue.equals (newValue))
                    return null;
            }
            new Statement (
                obj, Introspector.decapitalize ("set" + propName),
                new Object[] { newValue }
            ).execute ();

            RevisionEntry re = new RevisionEntry ();
            re.setDate (new Date());
            re.setUser (user);
            re.setProp (propName);
            re.setOldValue ((String) expr.getValue ());
            re.setNewValue (newValue);
            db.save (re);
            return re;
        }
        catch (Exception e) {
            throw new BLException ("Error setting "+propName, e);
        }
    }
    */
}

