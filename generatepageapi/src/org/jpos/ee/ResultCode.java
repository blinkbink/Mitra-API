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
import java.util.Map;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ResultCode implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String mnemonic;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Map locales;

    /** full constructor */
    public ResultCode(Long id, String mnemonic, String description, Map locales) {
        this.id = id;
        this.mnemonic = mnemonic;
        this.description = description;
        this.locales = locales;
    }

    /** default constructor */
    public ResultCode() {
    }

    /** minimal constructor */
    public ResultCode(Long id, Map locales) {
        this.id = id;
        this.locales = locales;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMnemonic() {
        return this.mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map getLocales() {
        return this.locales;
    }

    public void setLocales(Map locales) {
        this.locales = locales;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
