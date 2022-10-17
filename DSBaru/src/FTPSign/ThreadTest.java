package FTPSign;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.hibernate.Transaction;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.ee.DocumentAccess;

public class ThreadTest {

	public static void main(String [] args) throws Exception {
		DB db=new DB();
		db.open();
		
		//Session session=db.session();
		
		UserManager um =new UserManager(db);
		User us=um.findByUsername("dummy1@digisign.id");
		DocumentsAccessDao dao=new DocumentsAccessDao(db);
		List<DocumentAccess> lDocAccess=new ArrayList<DocumentAccess>();
				
		for(int i=0;i<100;i++) {
			try {
				//DocumentAccess dcs=dao.findAccessIDDoc_sign(String.valueOf(us.getId()), "testK-"+i).get(0);
				//System.out.println("get doc:" +dcs.getId()+" "+dcs.getDocument().getIdMitra());
				//if(dcs.isFlag())continue;
				//lDocAccess.add(dcs);
			}catch (Exception e) {
				e.printStackTrace();
			}
				
		}
		

        //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        //Set the maximum number of connections in the pool
        connManager.setMaxTotal(200);
        connManager.setDefaultMaxPerRoute(25);
        //Create a ClientBuilder Object by setting the connection manager
        HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);
 
        //Build the CloseableHttpClient object using the build() method.
        CloseableHttpClient httpclient = clientbuilder.build();

		Date start=new Date();
		System.out.println(start+" start process");
		List<Thread> lBulk=new ArrayList<Thread>();
	    Transaction t=db.session().beginTransaction();

		for(DocumentAccess da: lDocAccess) {
			
			SendBulk sb=new SendBulk(da,db,httpclient, "123");
			Thread th=new Thread(sb);
			th.start();
			lBulk.add(th);
		}
		
		for(Thread thr: lBulk) {
			thr.join();
		}

		Date end=new Date();
		int gagal=0;
		for(DocumentAccess da: lDocAccess) {
			if(!da.isFlag()) gagal++;
			System.out.println("check sign doc:" +da.getId()+" "+da.getDocument().getIdMitra()+" "+da.getDate_sign());

		}
		System.out.println(end+" end process, elapsed time :"+((double)(end.getTime()-start.getTime())/1000));
		System.out.println("completed : "+(lDocAccess.size()-gagal)+", failed :"+gagal);
		t.commit();
		db.close();
		
		
	}

}
