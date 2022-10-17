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

package org.jpos.ee.menu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jpos.ee.BLException;

/**
 * This class holds a map with all defined menus in a directory.<br>
 * It takes care or reading the directory and instantiating the {@link MenuNode} objects.<br>
 * A menu from an xml file can be place inside a menu from another using 
 * the xml attribute parent and path, if path is ommited it will be places as a child 
 * of the parent root. <br>
 * Example:<br> 
 * &lt;menu name="child" parent="parent-menu" path="path/to/child"><br>
 * ...items <br>
 * &lt;/menu>
 * 
 * @author alcarraz
 * @see MenuNode
 */
public class MenuMap {
    /**holds the menu map*/
    private Map<String,MenuNode> menus;
    
    /**loads all the menus from a file*/
    public void load(File dir) throws BLException{
        try {
            Map<String,MenuNode> menus = new HashMap<String, MenuNode>();
            Set<Element> elements = new HashSet<Element>();
            if (!dir.isDirectory())
                throw new BLException("path " + dir.getPath() + " is not a directory");
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".xml")) {
                    SAXBuilder builder = new SAXBuilder();
                    Document d = builder.build(f);
                    Element e =d.getRootElement(); 
                    MenuNode mn = new MenuNode();
                    mn.build(e);
                    elements.add(e);
                    menus.put(mn.getName(), mn);
                }
            }
            //second pass to set parents
            for (Element e : elements){
                String parentName = e.getAttributeValue("parent");
                if (parentName != null){
                    //FIXME by now assuming that top most item isn't part of the hierarchy
                    //is this correct?
                    MenuNode child = menus.get(e.getAttributeValue("name"));
                    MenuNode parent = menus.get(parentName);
                    if (parent == null) {
                        throw new BLException("parent menu " + parentName + " does not exists");
                    } else {
                        String path = e.getAttributeValue("path");
                        if (path != null)
                            parent = parent.getNodeByPath(path);
                        for (MenuNode n : child) 
                            parent.addChild(n);
                    }
                }
                    
            }
            this.menus = menus;
        } catch (JDOMException e) {
            throw new BLException("Error reading menus", e);
        } catch (IOException e) {
            throw new BLException("Error reading menus", e);
        }
    }
    
    /**retrieves the menu given by name*/
    public MenuNode getMenu (String name){
        return menus.get(name);
    }
}
