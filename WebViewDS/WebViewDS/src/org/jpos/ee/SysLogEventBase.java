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

public abstract class SysLogEventBase {
    public static final String[] severityAsString = 
        new String[] { "debug", "trace", "info", "warn", "error", "critical" };

    public abstract int getSeverity();

    /**
     * @param severity the severity
     * @return DEBUG, TRACE, INFO, WARN, ERROR, CRITICAL or severity'a value
     */
    public String getSeverityAsString () {
        if (getSeverity() > SysLog.CRITICAL)
            return Integer.toString (getSeverity());
        else
            return severityAsString [getSeverity()];
    }
}

