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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.ee.SigningSession;
import id.co.keriss.consolidate.ee.DocSigningSession;
import id.co.keriss.consolidate.ee.ActivationSession;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.PreRegistration;

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
                .add (Restrictions.eq ("nick", nick));
            if (!includeDeleted)
                crit = crit.add (Restrictions.eq ("deleted", Boolean.FALSE));
            return (User) crit.uniqueResult();
        } catch (ObjectNotFoundException e) {
//        	e.printStackTrace();
            LogSystem.error(getClass(), e);

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
                .add (Restrictions.eq ("deleted", Boolean.FALSE))
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
	    return session.load(User.class, id);
  }
    
  public User findByUserData(Long id) {
	     boolean result=true;
	     
	     Query query=session.createQuery("select u from User u, Userdata ud where u.userdata=ud.id and u.userdata = : id");
	     query.setLong("id", id);
	   
	     try{
	    	 return (User) query.list().get(0);
	     }catch (Exception e) {
//	    	 e.printStackTrace();
	            LogSystem.error(getClass(), e);

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
	            LogSystem.error(getClass(), e);

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
	            LogSystem.error(getClass(), e);

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
	            LogSystem.error(getClass(), e);

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
          LogSystem.error(getClass(), e);
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
         LogSystem.error(getClass(), e);
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
         LogSystem.error(getClass(), e);
	  }
	  return null;
 }

 //update 5/17/2019 18:14 tambah 6 fungsi baru signing session
 public SigningSession findStringBySession_Keyid(String session_key,String session_id) {
	  try{
			// TODO: cari eeuser pake eeuser di signing_session (1)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  SigningSession data=(SigningSession) session.createQuery("from SigningSession ss where ss.session_key ='"+session_key+"' and ss.id='"+session_id+"'").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
        LogSystem.error(getClass(), e);
        
	  }
	  return null;
}

 public User findByeeuser2(long user) {
	  try{
		  // TODO: cari email user pake eeuser di signing_session (2)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  User data=(User) session.createQuery("from User u where u.id ="+user+"").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
          LogSystem.error(getClass(), e);
	  }
	  return null;
  }
	
 public DocSigningSession findDocBySession_Keyid(String session_id) {
	  try{
		  // TODO: cari dokumen_id pake dokumen di doc_sign (3)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  DocSigningSession data=(DocSigningSession) session.createQuery("from DocSigningSession ds where ds.signingSession ='"+session_id+"'").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
        LogSystem.error(getClass(), e);
	  }
	  return null;
}
 public Documents findOwnereeuserByDocument(long documentID) {
	  try{
		  // TODO: cari eeuser pake eeuser di documents (4)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  Documents data=(Documents) session.createQuery("from Documents d where d.id ="+documentID+"").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
        LogSystem.error(getClass(), e);
	  }
	  return null;
}  
 public User findOwnernickByDocument(long eeuseruserid) {
	  try{
		  // TODO: cari email user pake eeuser di documents (5)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  User data=(User) session.createQuery("from User u where u.id ="+eeuseruserid+"").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
        LogSystem.error(getClass(), e);
	  }
	  return null;
}  
 public SigningSession findMitraSigningSession(String session_key,String session_id) {
	  try{
		  // TODO: cari mitra dari dokuments(6)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  SigningSession data=(SigningSession) session.createQuery("from SigningSession ss where ss.session_key ='"+session_key+"' and ss.id='"+session_id+"'").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
       LogSystem.error(getClass(), e);
	  }
	  return null;
}
 public Mitra findMitrafromSigningSession(long mitraid) {
	  try{
		  // TODO: cari mitra dari dokuments(7)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  Mitra data=(Mitra) session.createQuery("from Mitra m where m.id ="+mitraid+"").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
      LogSystem.error(getClass(), e);
	  }
	  return null;
}

public DocumentAccess findSequenceByDocument(String documentID, String emaileeuser) {
	  try{
		  // TODO: cari Sequence_number pake Seq_num di doc_access (8)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  DocumentAccess data=(DocumentAccess) session.createQuery("from DocumentAccess da where da.document ='"+documentID+"' and da.email = '"+emaileeuser+"' order by da.sequence_no asc").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
        LogSystem.error(getClass(), e);
	  }
	  return null;
}  
public DocumentAccess findSequenceByDocumentApproval(String documentID, String emaileeuser) {
	  try{
		  // TODO: cari Sequence_number pake Seq_num di doc_access (8)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  DocumentAccess data=(DocumentAccess) session.createQuery("from DocumentAccess da where da.document ='"+documentID+"' and da.email = '"+emaileeuser+"' and da.type='approval' and da.flag = false and da.cancel = false order by da.sequence_no asc").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
//		  LogSystem.error(getClass(), e);
		  return null;
	  }
}  
public DocumentAccess findSequenceByDocumentSign(String documentID, String emaileeuser) {
	  try{
		  // TODO: cari Sequence_number pake Seq_num di doc_access (8)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  DocumentAccess data=(DocumentAccess) session.createQuery("from DocumentAccess da where da.document ='"+documentID+"' and da.email = '"+emaileeuser+"' and (da.type like 'sign' or da.type like 'initials') and da.flag = false and da.cancel = false order by da.sequence_no asc").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
//		  LogSystem.error(getClass(), e);
		  return null;
	  }
}  
public DocumentAccess findSequenceByDocumentApprovalrecheck(String documentID) {
	  try{
		  // TODO: cari Sequence_number pake Seq_num di doc_access (8)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  DocumentAccess data=(DocumentAccess) session.createQuery("from DocumentAccess da where da.document ='"+documentID+"' and da.type='approval' and da.flag = true and da.cancel = true order by da.sequence_no asc").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
		  LogSystem.error(getClass(), e);
		  return null;
	  }
	 
}  
public DocumentAccess findSequenceBeforeByDocument(String documentID, int Result) {
	  try{
		  // TODO: cari Sequence_number pake Seq_num di doc_access (9)
//		  System.out.println("DEBUG DB:" +(i++));
		  
		  DocumentAccess data=(DocumentAccess) session.createQuery("from DocumentAccess da where da.document ='"+documentID+"'  and da.sequence_no ="+Result+" and da.flag='false'").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
        LogSystem.error(getClass(), e);
	  }
	  return null;
}
public Integer findSmallestSequenceByDocument(String documentID) {
	  try{
		  // TODO: cari Sequence_number pake Seq_num di doc_access (10)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  Integer data=(Integer) session.createQuery("select min(da.sequence_no) from DocumentAccess da where da.document ='"+documentID+"' and da.flag='false' and (da.type like 'sign' or da.type like 'initials' or da.type like 'approval') ").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
      LogSystem.error(getClass(), e);
	  }
	  return 0;
}

 //update 5/18/2019 15:25 tambah 3 fungsi baru activation session
public ActivationSession findPreregistrationBySession_Keyid(String session_key,String session_id) {
	  try{
			// TODO: cari preregistration pake preregistration di activation_session (1)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  ActivationSession data=(ActivationSession) session.createQuery("from ActivationSession where session_key ='"+session_key+"' and id='"+session_id+"'").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
       LogSystem.error(getClass(), e);
       
	  }
	  return null;
}
public ActivationSession findMitraActivationSession(String session_key,String session_id) {
	  try{
		  // TODO: cari mitra dari activation(2)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  ActivationSession data=(ActivationSession) session.createQuery("from ActivationSession where session_key ='"+session_key+"' and id='"+session_id+"'").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
     LogSystem.error(getClass(), e);
	  }
	  return null;
}
public Mitra findMitrafromActivationSession(long mitraid) {
	  try{
		  // TODO: cari mitra dari activation(3)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  Mitra data=(Mitra) session.createQuery("from Mitra m where m.id ="+mitraid+"").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
    LogSystem.error(getClass(), e);
	  }
	  return null;
}


public TokenMitra findTokenMitra(long mitraids) {
	  try{
		  // TODO: cari token mitra dari idmitra(7)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  TokenMitra data=(TokenMitra) session.createQuery("from TokenMitra tm where tm.mitra ="+mitraids+"").uniqueResult();
		  return data;
	  }
	  catch (Exception e) {
     LogSystem.error(getClass(), e);
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
         LogSystem.error(getClass(), e);
	  }
	  return null;
 }
 
 public List<DocSigningSession> findDocumentBulkSign(String id) throws HibernateException {
	    //return  session.createQuery("from DocSigningSession ds where ds.signingSession ='"+id+"' order by ds.signingSession asc ").list();
	 	//return  session.createQuery("from DocSigningSession ds,DocumentAccess da,SigningSession ss,Documents d where ss.id ='"+id+"' and ds.document=d.id and d.id=da.document and ss.id=ds.signingSession and ss.eeuser=da.eeuser and da.flag='false' order by ds.signingSession asc").list();
	 	return  session.createQuery("from DocSigningSession ds where ds.signingSession = '"+id+"' and ds.document in (select d.id from DocSigningSession ds,Documents d,SigningSession ss,DocumentAccess da where ss.eeuser = da.eeuser and d.id = da.document and ss.id = '"+id+"' and da.flag='false' and ds.document=d.id and ds.signingSession=ss.id) order by ds.document asc").list();
	 	
}
 
 public DocSigningSession findDocMitra(String id_key) {
	  try{
		  // TODO: cari eeuser pake eeuser di documents (4)
//		  System.out.println("DEBUG DB:" +(i++));
		
		  DocSigningSession data=(DocSigningSession) session.createQuery("from DocSigningSession ds where ds.signingSession ='"+id_key+"'").list().get(0);
		  return data;
	  }
	  catch (Exception e) {
       LogSystem.error(getClass(), e);
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
        LogSystem.error(getClass(), e);
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
        LogSystem.error(getClass(), e);
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
         LogSystem.error(getClass(), e);
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
         LogSystem.error(getClass(), e);
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
            LogSystem.error(getClass(), e);

	    }
	  }
 
}

