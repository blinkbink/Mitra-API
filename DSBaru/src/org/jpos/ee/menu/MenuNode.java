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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.jpos.ee.BLException;
import org.jpos.ee.Permission;
import org.jpos.ee.User;
/**
 * This class represents a menu tree.
 * 
 * @author alcarraz
 *  
 */

public class MenuNode implements Serializable, Iterable<MenuNode> {
    /*FIXME should implement Composite Pattern?*/
    
    /**
     * children elements 
     */
    private SortedSet<MenuNode> children;

    /**
     * what to display?
     */
    private String display;
    /**
     * name to be accessed
     */
    private String name;
    /**
     * url to load when clicked
     */
    private String url;
    
    /**
     * type of node (Separator, subtitle)
     */
    private String nodeType;
    
    /**
     * weight
     */
    private int weight;
    
    final static protected Pattern P = Pattern.compile("([^/]*)/(.*)");
    
    final static protected Comparator<MenuNode> COMPARATOR = new Comparator<MenuNode>(){

        @Override
		public int compare(MenuNode o1, MenuNode o2) {
            int w1 = o1.getWeight(), w2 = o2.getWeight();
            if (w1 != w2) 
                return  w1 - w2;
            String n1 = o1.getName(), n2 = o2.getName();
            if (n1 == null && n2 == null)
                return 0;
            else if (n1 == null)
                return -1;
            else if (n2 == null)
                return 1;
            else 
                return n1.compareTo(n2); 
                
        }
        
    };
    
    /**
     * Which permissions are able to see this menu?
     */
    private Set<String> permissions;
    private Set<String> permissionsChild;

    public MenuNode() {
        super();
        setChildren(new TreeSet<MenuNode>(COMPARATOR));
        permissions = new HashSet<String>();
    }
    
    /**
     * @param display
     * @param name
     * @param url
     * @param nodeType
     * @param weight
     * @param permissions
     */
    public MenuNode(String display, String name, String url, String nodeType, int weight, Set<String> permissions) {
        this();
        this.display = display;
        this.name = name;
        this.url = url;
        this.nodeType = nodeType;
        this.weight = weight;
        this.permissions = new HashSet<String>(permissions);
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }



    /**
     * @param children the children to set
     */
    public void setChildren(SortedSet<MenuNode> children) {
        this.children = children;
    }

    /**
     * @return the children
     */
    public SortedSet<MenuNode> getChildren() {
        return children;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        if (display != null) return display;
        if (name != null) return name;
        return null;
    }

    /**
     * @param display the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return the name
     */
    public String getName() {
        if (name != null) return name;
        if (display != null) return display;
        return null;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the weight
     */
    protected int getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    protected void setWeight(int weight) {
        this.weight = weight;
    }

    public void addChild(MenuNode child){
        children.add(child);
    }

    /**
     * Builds the Menu given by xml in tree.
     * For examples look at modules/eeweb3/cfg/menus
     * 
     * @param tree jDom Element with a representation of the xml
     */
    public void build(Element tree){
        nodeType = tree.getName(); //quickfix for handling separators
        display = tree.getAttributeValue("display");
        name = tree.getAttributeValue("name",display);
        url = tree.getAttributeValue("url");
        weight = Integer.parseInt(tree.getAttributeValue("weight","0"));
        String permsAsString = tree.getAttributeValue("permissions"); 
        if (permsAsString !=null)
            addPermissions(Arrays.asList(permsAsString.split("[, ]")));
        int nextWeight = 0;
        for (Object o : tree.getChildren()) {
            Element e = (Element)o;
            MenuNode child = new MenuNode();
            //propagate permissions to descendents, what other meaning could
            //have a permission defined in an internal node?
            //note that this only apply to nodes defined in the same xml
            //nodes added later by a module will have their own permissions
            //defined
            //FIXME: Don't know if this is the best way to do this but is the 
            // easier to start :)
            permissionsChild = new HashSet<String>();
            String permsAsStrings = tree.getAttributeValue("permissions"); 
            if (permsAsStrings !=null)
                addPermissionsChild(Arrays.asList(permsAsStrings.split("[, ]")));
            else
            	addPermissionsChild(permissions);
            
            child.addPermissions(permissionsChild);
            child.build(e);
            //if weight not defined increment the last one
            if (child.getWeight() == 0) 
                child.setWeight(nextWeight++);
            else
                nextWeight = child.getWeight(); 
            addChild(child);
        }
    }
    
    /**
     * 
     * @param name
     * @return
     * @throws BLException if node does not exists
     */
    public MenuNode getChild(String name) throws BLException{
        for (MenuNode node : children) {
            if (name.equals(node.getName())){
                return node;
            }
        }
        throw new BLException("node not found " + name);
    }
    /**
     * Handy method to quicly get access to a node by path.
     * This let other modules to put sub menus without navigating all the path.  
     * @param path Desired path separated by slashes (/)
     * @return The node given by the path
     * @throws BLException if node not found in given path
     */
    public MenuNode getNodeByPath(String path ) throws BLException{
        Matcher m = P.matcher(path);
        if (m.matches()){ 
            try {
                return getChild(m.group(1)).getNodeByPath(m.group(2));
            } catch(BLException e) {//quick fix to show the real path in the exception
                throw new BLException("Node not found in path " + path + " of menu " + getName());
            }
        } else {
            return getChild(path);
        }
    }
    
    /**
     * 
     * @return an iterator to iterate over children in the order given by order
     */
    @Override
	public Iterator<MenuNode> iterator(){
        return children.iterator();
    }
    
    /**
     * 
     * @return count of direct children
     */
    public int size(){
        return children.size();
    }
    
    /**
     * Create permission set if it still hasn't be done.
     * FIXME: This isn't the best name. Is it?
     */
    protected void initPerms(){
        if (permissions == null) permissions = new HashSet<String>();
    }
    
    protected void addPermissions(Collection<String> perms){
        initPerms();
        permissions.addAll(perms);
    }
    
    protected void addPermissionsChild(Collection<String> perms){
        initPerms();
        permissionsChild.addAll(perms);
    }
    
    
    public boolean hasPermission(User u){
        if (permissions.isEmpty())
            return true;
        for (Object o: u.getPermissions()){
            if (permissions.contains(((Permission)o).getName()))
                return true;
        }
        return false;
            
    }
    public MenuNode prune(User u){
        MenuNode pruned = new MenuNode(display, name, url, nodeType, weight, permissions);
        for (MenuNode child : this){
            MenuNode prunedChild = child.prune(u);
            if (prunedChild != null && hasPermission(u))
                pruned.addChild(prunedChild);
        }
        if (pruned.getChildren().size() == 0 && !hasPermission(u))
            return null;
        pruned.addPermissions(permissions);
        return pruned;
    }

    /**
     * @return the nodeType
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return "[type= " + nodeType + ", name=" + name + ", weight="+weight+"]" ;
    }
}
