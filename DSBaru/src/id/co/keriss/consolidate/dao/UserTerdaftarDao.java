package id.co.keriss.consolidate.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.TMPUsername;
import id.co.keriss.consolidate.ee.UserTerdaftar;
import id.co.keriss.consolidate.ee.VerifikasiManual;
import id.co.keriss.consolidate.util.LogSystem;
public class UserTerdaftarDao {
		Session session;
		DB db;
		Log log;

		public UserTerdaftarDao(DB db) {
			super();
			session = db.session();
			this.db = db;
			log = db.getLog();
		}

		public Long create(UserTerdaftar key) {
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
		
		public Long create2(UserTerdaftar key) {
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
		
		public UserTerdaftar cekNikMitra(String nik, Long mitra) {
			try {
				String sql = "select u from UserTerdaftar u where u.nik = :nik and u.mitra=:mitra";
				System.out.println(sql);
				Query<UserTerdaftar> query = session.createQuery(sql);
				query.setParameter("nik", nik);
				query.setLong("mitra", mitra);
				return query.uniqueResult();
			} catch (Exception e) {
				LogSystem.error(getClass(), e);
				return null;

			}

		}

	}
