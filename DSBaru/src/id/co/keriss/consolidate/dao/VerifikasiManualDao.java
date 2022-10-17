package id.co.keriss.consolidate.dao;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.TMPUsername;
import id.co.keriss.consolidate.ee.VerifikasiManual;
import id.co.keriss.consolidate.util.LogSystem;
public class VerifikasiManualDao {
		Session session;
		DB db;
		Log log;

		public VerifikasiManualDao(DB db) {
			super();
			session = db.session();
			this.db = db;
			log = db.getLog();
		}

		public Long create(VerifikasiManual key) {
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
		
		public Long create2(VerifikasiManual key) {
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

		public VerifikasiManual cekNikEmail(String nik, String email) {
			try {
				String sql = "select v from VerifikasiManual v where v.nik = :nik and v.email=:email";
				System.out.println(sql);
				Query<VerifikasiManual> query = session.createQuery(sql);
				query.setParameter("nik", nik);
				query.setParameter("email", email);
				return query.uniqueResult();
			} catch (Exception e) {
				LogSystem.error(getClass(), e);
				return null;

			}

		}
		
		public VerifikasiManual cekNik(String nik) {
			try {
				String sql = "select v from VerifikasiManual v where v.nik = :nik";
				System.out.println(sql);
				Query<VerifikasiManual> query = session.createQuery(sql);
				query.setParameter("nik", nik);
				return query.uniqueResult();
			} catch (Exception e) {
				LogSystem.error(getClass(), e);
				return null;

			}

		}
		
		public Long getNext() {
		    Query query = 
		        session.createSQLQuery("select nextval('kode_user_seq') as num")
		            .addScalar("num", StandardBasicTypes.BIG_INTEGER);
		    BigInteger hasil= (BigInteger) query.uniqueResult();
		    return hasil.longValue(); 
		}

	}
