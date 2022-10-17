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

import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Userdata;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class User extends Cloneable implements Serializable {
    private long id;
    private String nick;
    //private String password;
    private String name;
    private char status;
    private Set    perms;
    private Map    props;
    private Set    visitors;
    private List   revisions;
    private boolean deleted;
    //private Merchant mid;
    private Userdata userdata;
    //private String imei;
    private char pay_type;
    private Mitra mitra;
    //private String i_ttd;
    //private String no_handphone;
    private boolean admin;
    private boolean auto_ttd;
    private Login login;
    private Date time;
    private String kode_user;
    
    
    public String getKode_user() {
		return kode_user;
	}


	public void setKode_user(String kode_user) {
		this.kode_user = kode_user;
	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public Login getLogin() {
		return login;
	}


	public void setLogin(Login login) {
		this.login = login;
	}


	public char getPay_type() {
		return pay_type;
	}


    /**
     * pay_type:
     *  1: prepaid
     *  2: postpaid
     *  3: langganan
     */
	public void setPay_type(char pay_type) {
		this.pay_type = pay_type;
	}

	/**
     * pay_type:
     *  prepaid, postpaid, langganan
     */
	public String getPayType() {
		String type=null;
		if(pay_type=='1') {
			type="prepaid";
		}else if(pay_type=='2'){
			type="postpaid";
		}else if(pay_type=='3'){
			type="langganan";
		}
		return type;
	}


	public User() {
        super();
        perms    = new LinkedHashSet ();
        visitors = new LinkedHashSet ();
    }
    
    
    
	public boolean isAuto_ttd() {
		return auto_ttd;
	}


	public void setAuto_ttd(boolean auto_ttd) {
		this.auto_ttd = auto_ttd;
	}


	public char getStatus() {
		return status;
	}



	public void setStatus(char status) {
		this.status = status;
	}

	public boolean isAdmin() {
		return admin;
	}


	public void setAdmin(boolean admin) {
		this.admin = admin;
	}


	


	public Set getPerms() {
		return perms;
	}


	public void setPerms(Set perms) {
		this.perms = perms;
	}


	public Userdata getUserdata() {
		return userdata;
	}


	public void setUserdata(Userdata user) {
		this.userdata = user;
	}


	
	
//	public String getImei() {
//		return imei;
//	}
//
//	public void setImei(String imei) {
//		this.imei = imei;
//	}



	public String getNick() {
        return nick;
    }
    public void setNick (String nick) {
        this.nick = nick.toLowerCase().trim();
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name.trim();
    }
    public long getId() {
        return id;
    }
    public void setId (long id) {
        this.id = id;
    }
//    public void setPassword (String password) {
//        this.password = password;
//    }
//    public String getPassword () {
//        return password;
//    }
    public void setDeleted (boolean deleted) {
        this.deleted = deleted;
    }
    public boolean isDeleted() {
        return deleted;
    }
    public void setPermissions (Set perms) {
        this.perms = perms;
    }
    public Set getPermissions () {
        return perms;
    }
    public void setVisitors (Set visitors) {
        this.visitors = visitors;
    }
    public Set getVisitors () {
        return visitors;
    }
    public Mitra getMitra() {
		return mitra;
	}
	public void setMitra(Mitra mitra) {
		this.mitra = mitra;
	}
	public boolean hasPermission (String permName) {
        return permName != null ? perms.contains (new Permission (permName)) : false;
    }
    public void grant (String permName) {
        perms.add (new Permission (permName));
    }
    public void revoke (String permName) {
        perms.remove (new Permission (permName));
    }
    public void revokeAll () {
        perms.clear ();
    }
    public void setProps (Map props) {
        this.props = props;
    }
    public Map getProps () {
        return (props = props == null ? new HashMap () : props);
    }
    public void set (String prop, String value) {
        getProps().put (prop, value);
    }
    public String get (String prop) {
        return (String) getProps().get (prop);
    }
    public String get (String prop, String defValue) {
        String value = (String) getProps().get (prop);
        return value == null ? defValue : value;
    }
    public void setRevisions (List revisions) {
        this.revisions = revisions;
    }
    public List getRevisions () {
        if (revisions == null)
            revisions = new ArrayList();
        return revisions;
    }
    @Override
	public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("nick", getNick())
            .toString();
    }
    @Override
	public boolean equals(Object other) {
        if ( !(other instanceof User) ) return false;
        User castOther = (User) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }
    @Override
	public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
    /**
     * factory method used to create a RevisionEntry associated with this user.
     *
     * @param summary information
     * @param detail detailed information
     * @param author change author
     */
    public UserRevisionEntry logRevision (String info, User author) {
        UserRevisionEntry re = new UserRevisionEntry();
        re.setDate (new Date());
        re.setInfo (info);
        re.setUser (this);
        re.setAuthor (author);
        getRevisions().add (re);
        return re;
    }
    /**
     * @return "nick(id)"
     */
    public String getNickAndId() {
        StringBuffer sb = new StringBuffer (getNick());
        sb.append ('(');
        sb.append (Long.toString(getId()));
        sb.append (')');
        return sb.toString();
    }


//	public String getI_ttd() {
//		return i_ttd;
//	}
//
//
//	public void setI_ttd(String i_ttd) {
//		this.i_ttd = i_ttd;
//	}


//	public String getNo_handphone() {
//		return no_handphone;
//	}
//	public void setNo_handphone(String no_handphone) {
//		this.no_handphone = no_handphone;
//	}
    
}

