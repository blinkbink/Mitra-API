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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;

import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.RSAEncryption;

import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Alejandro Revilla
 */
public class UserManager {
    private Session session;
    private String digest;
    private int i=0;

    public UserManager (DB db) {
        super ();
        this.session = db.session();
    }
    public UserManager (Session session) {
        super ();
        this.session = session;
    }
    public User getUserByNick (String nick, boolean includeDeleted)
        throws HibernateException
    {
        try {
            Criteria crit = session.createCriteria (User.class)
                .add (Expression.eq ("nick", nick));
            if (!includeDeleted)
                crit = crit.add (Expression.eq ("deleted", Boolean.FALSE));
            return (User) crit.uniqueResult();
        } catch (ObjectNotFoundException e) {
//        	e.printStackTrace();
	    	LogManager.getLogger(getClass()).error(e);

        }
        return null;
    }
    public User getUserByNick (String nick)
        throws HibernateException
    {
        return getUserByNick (nick, false);
    }
    /**
     * @param nick name.
     * @param seed initial seed
     * @param pass hash
     * @throws BLException if invalid user/pass
     */
    public User getUserByNick (String nick, String seed, String pass) 
        throws HibernateException, BLException
    {
        User u = getUserByNick (nick);
        assertNotNull (u, "User does not exist");
        assertTrue (checkPassword (u, seed, pass), "Invalid password");
        return u;
    }
    /**
     * @param nick name.
     * @param seed initial seed
     * @param pass hash
     * @return true if password matches
     * @throws BLException if invalid user/pass
     */
    public boolean checkPassword (User u, String seed, String pass) 
        throws HibernateException, BLException
    {
        assertNotNull (seed, "Invalid seed");
        assertNotNull (pass, "Invalid pass");
//        String password = u.getPassword();
        String password = u.getLogin().getPassword();
        assertNotNull (password, "Password is null");
        String computedPass = EEUtil.getHash (seed, password);
        return pass.equals (computedPass);
    }
    /**
     * @return all users
     */
    public List findAll () throws HibernateException {
        return session.createCriteria (User.class)
                .add (Expression.eq ("deleted", Boolean.FALSE))
                .list();
    }
    private void assertNotNull (Object obj, String error) throws BLException {
        if (obj == null)
            throw new BLException (error);
    }
    private void assertTrue (boolean condition, String error) 
        throws BLException 
    {
        if (!condition)
            throw new BLException (error);
    }
    
    public User findById(Long id) throws HibernateException {
	    return (User)session.load(User.class, id);
  }
    
  public User findByUserData(Long id) {
	     boolean result=true;
	     
	     Query query=session.createQuery("select u from User u, Userdata ud where u.userdata=ud.id and u.userdata = : id");
	     query.setLong("id", id);
	   
	     try{
	    	 return (User) query.list().get(0);
	     }catch (Exception e) {
//	    	 e.printStackTrace();
		    	LogManager.getLogger(getClass()).error(e);

	    	 return null;
			// TODO: handle exception
		}
  }

    
  public User findByUserID(String user_id) throws HibernateException {
	    return (User) session.createQuery("from User where userdata ='"+user_id+"' order by id desc").list().get(0);
  }
  
  public User getUser(String email, String nik) {
	     boolean result=true;
	     Query query=session.createQuery("select u from User u, Userdata ud where u.userdata=ud.id and u.nick = :email and no_identitas= :nik ");
	     query.setString("email", email);
	     query.setString("nik", nik);
	   
	     try{
	    	 return (User) query.list().get(0);
	     }catch (Exception e) {
//	    	 e.printStackTrace();
		    	LogManager.getLogger(getClass()).error(e);

	    	 return null;
			// TODO: handle exception
		}
  }
  
  public User findByEmailMitra(String email, Long mitra) {
	  Query query=session.createQuery("select u from User u, Userdata ud where u.userdata=ud.id and lower(u.nick) = :email and (ud.mitra= :mitra or u.mitra =: mitra) ");
	     query.setString("email", email);
	     query.setLong("mitra", mitra);
	   
	     try{
	    	 return (User) query.uniqueResult();
	     }catch (Exception e) {
		    	LogManager.getLogger(getClass()).error(e);

	    	 return null;
			// TODO: handle exception
		}
  }
  
  public User findByEmailMitra2(String email) {
	  Query query=session.createQuery("from User where lower(nick) = :email ");
	     query.setString("email", email);
	   
	     try{
	    	 return (User) query.uniqueResult();
	     }catch (Exception e) {
		    	LogManager.getLogger(getClass()).error(e);

	    	 return null;
			// TODO: handle exception
		}
  }
  
 public User findByUsername(String user) {
	  try{
//		  System.out.println("DEBUG DB:" +(i++));
		
		  User data=(User) session.createQuery("from User where lower(nick) ='"+user+"' and (status='3')").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
  }
 
 public User findByUsername2(String user) {
	  try{
//		  System.out.println("DEBUG DB:" +(i++));
		
		  User data=(User) session.createQuery("from User where lower(nick) ='"+user+"' and (status='3' or status='1')").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }
 
 //update 1/14/2019 10:14 tambah fungsi baru
 public User findByEmail(String user) {
	  try{
//		  System.out.println("DEBUG DB:" +(i++));
		  User data=(User) session.createQuery("from User where lower(nick) ='"+user+"' ").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }

 
 public User findByUsernameSign(String user) {
	  try{
//		  System.out.println("DEBUG DB:" +(i++));
		  User data=(User) session.createQuery("from User where lower(nick) ='"+user+"' and (status='3' or status='1')").uniqueResult();
		  
		  return data;
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }
 
 public User findByUserNik(String user, String nik) {
	 try{
//		  System.out.println("DEBUG DB:" +(i++));
		  Query query=session.createQuery("select e "
		  									+ "from "
		  										+ "User e, "
		  										+ "Userdata u "
		  									+ "where "
		  										+ "e.userdata=u.id and "
		  										+ "lower(e.nick) =:email and "
		  										+ "u.no_identitas=:nik and "
		  										+ "(e.status='3' or e.status='4' or e.status='1')");
		  query.setString("email", user);
		  query.setString("nik", nik);
		  
		  return (User) query.uniqueResult();
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }
 
 public User findByUserNikNonStatus(String user, String nik) {
	 try{
//		  System.out.println("DEBUG DB:" +(i++));
		  Query query=session.createQuery("select e "
		  									+ "from "
		  										+ "User e, "
		  										+ "Userdata u "
		  									+ "where "
		  										+ "e.userdata=u.id and "
		  										+ "lower(e.nick) =:email and "
		  										+ "u.no_identitas=:nik");
		  query.setString("email", user);
		  query.setString("nik", nik);
		  
		  return (User) query.uniqueResult();
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }
 
 public User findByUsernamePassword(String email, String pwd) {
	 
	 try{
//		  System.out.println("DEBUG DB:" +(i++));
		  Query query=session.createQuery("select u "
		  									+ "from "
		  										+ "User u, "
		  										+ "Login l "
		  									+ "where "
		  										+ "u.login=l.id and "
		  										+ "u.nick =:email and "
		  										+ "l.password=:pwd and "
		  										+ "(status='3')");
		  query.setString("email", email);
		  query.setString("pwd", pwd);
		  
		  return (User) query.uniqueResult();
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }
 
 public User findByUsernameByMitra(String user,Long mitra) {
	  try{
//		  System.out.println("DEBUG DB:" +(i++));
		  User data=(User) session.createQuery("from User u "
		  						+ "where "
		  							+ "lower(u.nick) ='"+user+"' "
		  							+ "and u.status='3' "
		  							+ "and u.userdata.mitra="+mitra).uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
	    	LogManager.getLogger(getClass()).error(e);
	  }
	  return null;
 }
  
  
  
  public  void update(User key) {
	    try {
	    	Transaction tx=session.beginTransaction();
	      session.update(key);
	      tx.commit();
	    } catch (RuntimeException e) {
//	    	e.printStackTrace();
	    	LogManager.getLogger(getClass()).error(e);

	    }
	  }
 
}

