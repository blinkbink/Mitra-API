package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.TMPUsername;
import id.co.keriss.consolidate.util.LogSystem;
public class TmpUsernameDao {
		Session session;
		DB db;
		Log log;

		public TmpUsernameDao(DB db) {
			super();
			session = db.session();
			this.db = db;
			log = db.getLog();
		}

		public Long create(TMPUsername key) {
			Long id = null;
			try {
				Transaction tx = session.beginTransaction();
				id = (Long) session.save(key);
				tx.commit();
			} catch (RuntimeException e) {
				LogSystem.error(getClass(), e);

				// log.debug(e);
			}
			return id;
		}
		
		public Long create2(TMPUsername key) {
			Long id = null;
			try {
				Transaction tx = session.beginTransaction();
				id = (Long) session.save(key);
				tx.commit();
			} catch (RuntimeException e) {
				LogSystem.error(getClass(), e);
				id=(long) 0;
				// log.debug(e);
			}
			return id;
		}

		public List<TMPUsername> cekUserName(String userName) {
			try {
				String sql = "select t from TMPUsername t where t.username = :username";
				Query<TMPUsername> query = session.createQuery(sql);
				query.setParameter("username", userName);
				return query.list();
			} catch (Exception e) {
				LogSystem.error(getClass(), e);
				return null;

			}

		}


		@SuppressWarnings("unchecked")
		public TMPUsername updateData(String userName) {
			try {
				Query<TMPUsername> query = session.createQuery("delete TMPUsername where username = :username");
				query.setParameter("username", userName);
				int result = query.executeUpdate();
			} catch (Exception e) {
				LogSystem.error(getClass(), e);
				return null;

			}
			return null;
		}

	}
