package id.co.keriss.consolidate.dao;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Card;

public class CardDao {
	Session session;
	DB db;
	Log log;
	
	public CardDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	  public Card findByCardno(String cardno) throws HibernateException {
		    return (Card) session.createQuery("from Card b where b.cardno ='"+cardno+"'").uniqueResult();
	  }
	  
	  public  void deleteCard(Card card) {
		try {
	      session.delete(card);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void createCard(Card card) {
	    try {
	      session.save(card);
	    } catch (RuntimeException e) {
	    	log.debug(e);
	    }
	  }

	  public  void updateCard(Card card) {
	    try {
	      session.update(card);
	    } catch (RuntimeException e) {
	        log.debug(e);
	    }
	  }
	  
	  
}
