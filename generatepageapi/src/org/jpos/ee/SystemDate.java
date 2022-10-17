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
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SystemDate {
    static long offset = 0L;
    public static Date getDate() {
        return new Date (System.currentTimeMillis() + offset);
    }
    public static void forceDate (Date d) {
        offset = d.getTime() - System.currentTimeMillis();
    }
    public static Calendar getCalendar() {
        Calendar cal = new GregorianCalendar();
        cal.setTime (getDate());
        return cal;
    }
    public static long getOffset() {
        return offset;
    }
    public static void resetOffset() {
        offset = 0L;
    }
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + offset;
    }
}

