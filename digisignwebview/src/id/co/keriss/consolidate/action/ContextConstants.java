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

import org.jpos.ee.Constants;

public interface ContextConstants extends Constants {
	
    public static final String LOGGER = "digisignlogger";

    /** org.jpos.ee.DB object */
    public static final String DB = "DB";

    /** Hibernate Session */
    public static final String HS = "HS";

    /** Hibernate Transaction */
    public static final String TX = "TX";

    /** a List with errors messages */
    public static final String ERRORS  = "errors";

    /** org.jpos.ee.Visitor */
    public static final String VISITOR = "visitor"; 

    /** current server time (localtime) */
    public static final String DATE = "date"; 

    /** org.jpos.ee.User */
    public static final String USER = "user"; 

    /** a random hash */
    public static final String HASH = "hash"; 

    /** Message to be displayed */
    public static final String MESSAGE = "message"; 

    /** session variable used to store redirect uri.  */
    public static final String REDIRECT = "redirect"; 
    
    /** Context variable where to store the menu*/
    public static final String MENU = "menu";
    
    /** Application variable for menu map*/
    public static final String MENU_MAP ="menuMap"; 
}

