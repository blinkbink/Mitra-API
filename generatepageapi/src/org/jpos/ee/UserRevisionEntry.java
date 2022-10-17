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

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class UserRevisionEntry extends RevisionEntry implements Serializable {

    /** nullable persistent field */
    private org.jpos.ee.User user;

    /** full constructor */
    public UserRevisionEntry(Date date, String info, org.jpos.ee.User author, org.jpos.ee.User user) {
        super(date, info, author);
        this.user = user;
    }

    /** default constructor */
    public UserRevisionEntry() {
    }

    public org.jpos.ee.User getUser() {
        return this.user;
    }

    public void setUser(org.jpos.ee.User user) {
        this.user = user;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
