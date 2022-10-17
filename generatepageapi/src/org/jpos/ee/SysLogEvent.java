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
public class SysLogEvent extends org.jpos.ee.SysLogEventBase implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Date date;

    /** nullable persistent field */
    private boolean deleted;

    /** nullable persistent field */
    private String source;

    /** nullable persistent field */
    private String type;

    /** nullable persistent field */
    private int severity;

    /** nullable persistent field */
    private String summary;

    /** nullable persistent field */
    private String detail;

    /** nullable persistent field */
    private String trace;

    /** full constructor */
    public SysLogEvent(Date date, boolean deleted, String source, String type, int severity, String summary, String detail, String trace) {
        this.date = date;
        this.deleted = deleted;
        this.source = source;
        this.type = type;
        this.severity = severity;
        this.summary = summary;
        this.detail = detail;
        this.trace = trace;
    }

    /** default constructor */
    public SysLogEvent() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSeverity() {
        return this.severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTrace() {
        return this.trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
