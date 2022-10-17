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

import java.util.Date;

import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import org.jpublish.action.Action;

import com.anthonyeden.lib.config.Configuration;

public class Close implements Action, ContextConstants {
    @Override
	public void execute (JPublishContext context, Configuration cfg) {
        Date start = (Date) context.get (DATE);
        long elapsed = System.currentTimeMillis() - start.getTime();
        context.getSyslog().info ("CLOSE: " + context.getRequest().getRequestURI() + " elapsed=" + elapsed);
        try {
            DB db = (DB) context.get (DB);
            if (db != null) {
                db.close();
                context.remove (DB);
            }
        } catch (Throwable t) {
            context.getSyslog().warn (t);
        } 
    }
}

