package test;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;

import org.jpos.ee.DB;

import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.DocumentAccess;


public class TestKey2 {

	public static void main(String [] args) throws Exception {
		DB db=new DB();
		db.open();
		
		DocumentsAccessDao docDao=new DocumentsAccessDao(db);
		List<DocumentAccess>da = docDao.findDocAccessEEuserByMitra("Test1231", new Long(1), "dummy1@digisign.id", new Long("17774"));
		
		for (DocumentAccess documentAccess : da) {
			System.out.println(documentAccess.getId());
		}
		
		
		
		db.close();

        
	}
}
