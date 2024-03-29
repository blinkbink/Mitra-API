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
import org.jpos.ee.status.Status;


/** @author Hibernate CodeGenerator */
public class StatusRevisionEntry extends RevisionEntry implements Serializable {

    /** nullable persistent field */
    private Status status;

    /** full constructor */
    public StatusRevisionEntry(Date date, String info, org.jpos.ee.User author, Status status) {
        super(date, info, author);
        this.status = status;
    }

    /** default constructor */
    public StatusRevisionEntry() {
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
	public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
