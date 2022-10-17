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
import java.util.Map;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Visitor extends org.jpos.ee.VisitorBase implements Serializable {

    /** identifier field */
    private String id;

    /** nullable persistent field */
    private Date lastUpdate;

    /** nullable persistent field */
    private org.jpos.ee.User user;

    /** persistent field */
    private Map props;

    /** full constructor */
    public Visitor(String id, Date lastUpdate, org.jpos.ee.User user, Map props) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.user = user;
        this.props = props;
    }

    /** default constructor */
    public Visitor() {
    }

    /** minimal constructor */
    public Visitor(String id, Map props) {
        this.id = id;
        this.props = props;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public org.jpos.ee.User getUser() {
        return this.user;
    }

    public void setUser(org.jpos.ee.User user) {
        this.user = user;
    }

    public Map getProps() {
        return this.props;
    }

    public void setProps(Map props) {
        this.props = props;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
