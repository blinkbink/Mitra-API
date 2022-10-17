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

import java.util.ResourceBundle;
import org.jpos.transaction.Constants;      
public class K implements Constants {
    protected static ResourceBundle rb = 
        ResourceBundle.getBundle(Constants.class.getName());

    public static String get (String rn) {
        try {
            return rb.getString (rn);
        } catch (Throwable t) {
            // we don't care
        }
        return rn;
    }
    public static ResourceBundle getResourceBundle() {
        return rb;
    }
}

