package id.co.keriss.consolidate.dao;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.CardType;

public class CardTypeDao {
	Session session;
	DB db;
	Log log;
	
	public CardTypeDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (CardType.class).list();
      }
	  
	  public CardType findByDigitId(Integer cardid) throws HibernateException {
		  return (CardType) session.createQuery("from CardType where digitid ="+cardid+"").uniqueResult();
	  }
	  
	  public CardType findById(Long id) throws HibernateException {
		    return session.load(CardType.class, id);
	  }
	
	  public  void deleteTransaction(CardType type) {
		try {
	      session.delete(type);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createCardType(CardType type) {
	    try {
	      session.save(type);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateCardType(CardType type) {
	    try {
	      session.update(type);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
}
