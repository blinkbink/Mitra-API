package api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.ee.User;

import id.co.keriss.consolidate.action.ajax.SendMailSSL;
import id.co.keriss.consolidate.action.billing.BillingSystem;
import id.co.keriss.consolidate.dao.PaymentDao;
import id.co.keriss.consolidate.ee.Payment;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.LogSystem;

public class TopupSystem extends HttpServlet {

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		User userRecv;

        String result="05";
		 DB db = new DB();
        try {
            db.open ();
            Session hs = db.session();
	        
			int count = 21;
			String data=null;
			String res="";
			List<FileItem> fileItems = null;
			try{
	
				 data=request.getParameter("data");
				 System.out.println("DATA:" +data);
				 String process=request.getRequestURI().split("/")[2];
		         LogSystem.request(request,fileItems);
		     
		         String dataPlain = AESEncryption.decryptBilling(data);
		         String idPayment = dataPlain.split("\\|")[1];
		         
		         PaymentDao pDao=new PaymentDao(db);
		         Payment p=pDao.findByID(idPayment);
		         if(p==null) {
		        	 
		         }else {

			         BillingSystem bSys=new BillingSystem();
			         String rs=bSys.topup(p.getId_customer(), p.getProduct_code());

			         if(rs.equals(BillingSystem.BERHASIL)) {
			        	 p.setInvoice(bSys.getLastInvoice());
			        	 p.setStatus(3);
			        	 p.setDate_update(new Date());
			        	 pDao.update(p);
			        	 Payment p1=p;
			        	 User ue=p1.getEeuser();
			        	 
						new SendMailSSL().sendMailPaySuccess(db, p1, ue.getNick());
							
			         }
		        	 result=rs;
			         bSys.close();
		         }
	
		       

			
	
			}catch (Exception e) {
			
	            LogSystem.error(getClass(), e);
	//			error (context, e.getMessage());
	//            context.getSyslog().error (e);
			}
			OutputStream outStream = response.getOutputStream();
	        outStream.write(result.getBytes());
		        
			  
        } catch (Exception e) {
            LogSystem.error(getClass(), e);

        }finally {
        	try {
        		db.close();
        	}
        	catch (Exception e) {
                LogSystem.error(getClass(), e);
			}
        	
        }
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doPost(request, response);
		OutputStream outStream = response.getOutputStream();
        outStream.write("404".getBytes());

	}

	
}
