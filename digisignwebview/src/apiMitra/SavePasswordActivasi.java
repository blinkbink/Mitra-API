package apiMitra;

import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.LoginDao;
import org.hibernate.Session;
import id.co.keriss.consolidate.util.LogSystem;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;

public class SavePasswordActivasi extends HttpServlet {
	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//System.out.println("masukkkkkkkkkkkkkkkk save pwd");
		long start = System.currentTimeMillis();
		Date tgl= new Date();
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		String refTrx="SAVEPWD"+sdfDate2.format(tgl).toString();
//		LogSystem.info(request, "masukkkkkkkkkkkkkkkk save pwd", refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
        String filePath = null;
    	User userRecv;

    	String path_app = this.getClass().getName();
		String CATEGORY = "ACTIVATION";
		 String mitra_req="";
		 String email_req="";
		
		 DB db = new DB();
        try {
            db.open ();
            Session hs = db.session();
          
            StringBuilder sb = new StringBuilder();
	         String s;
	         while ((s = request.getReader().readLine()) != null) {
	                sb.append(s);
	         }

//	         System.out.println("RECEIVE :"+sb.toString());
//	         log.info("RECEIVE :"+sb.toString());
	         LogSystem.request(request, refTrx);
	         LogSystem.info(getClass(), sb.toString());
	         
	         JSONObject object=new JSONObject(sb.toString());
	         //System.out.println("passwordnya adalah = "+object.getString("newuserpwd"));
	         LogSystem.info(request, "passwordnya adalah = "+object.getString("newuserpwd"), refTrx , path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	         String username=object.getString("newusername");
	         String pwd=object.getString("newuserpwd");
	         String repwd=object.getString("renewuserpwd");
	         String iduser=object.getString("iduser");
	         OutputStream outStream = response.getOutputStream();
	         JSONObject j=new JSONObject();
	         
	         if(!pwd.equals(repwd)) {
	        	 try {
		 				j.put("result", "12");
		 				j.put("info", "Password dan ulang password tidak sama.");
		 				j.put("status", "1");
		 				outStream.write(j.toString().getBytes());
		 				return;
		 			} catch (JSONException e) {
		 				// TODO Auto-generated catch block
		 				LogSystem.error(getClass(), e);
		 			}
	         }
	         
	         id.co.keriss.consolidate.ee.Login login=null;
	         LoginDao ldao=new LoginDao(db);
	         UserManager um=new UserManager(db);
	         User user=um.findById(Long.parseLong(iduser));
	         if(user!=null) {
	        	 login=user.getLogin();
	         } else {
	        	try {
	 				j.put("result", "05");
	 				j.put("info", "data User tidak ditemukan");
	 				outStream.write(j.toString().getBytes());
	 				return;
	 			} catch (JSONException e) {
	 				// TODO Auto-generated catch block
	 				LogSystem.error(getClass(), e);
	 			}
	         }
	         
	         if(login==null) {
	        	 login=new id.co.keriss.consolidate.ee.Login();
	        	 login.setUsername(username);
	        	 login.setPassword(pwd);
	        	 Long idlogin=ldao.create(login);
	        	 
	        	 user.setStatus('3');
	        	 user.setLogin(ldao.findById(idlogin));
	        	 um.update(user);
	        	 
	         } else {
	        	 login.setPassword(pwd);
	        	 ldao.update(login);
	        	 user.setStatus('3');
	        	 um.update(user);
	        	 
//	        	 j.put("result", "12");
//	 				j.put("info", "Anda sudah membuat password, gunakan lupa password jika akan rubah password.");
//	 				j.put("status", "3");
//	 				outStream.write(j.toString().getBytes());
//	 				return;
	         }
	         
	         try {
	        	 DocumentsAccessDao dad=new DocumentsAccessDao(db);
	        	 dad.updateWhere(user.getId(), user.getNick());
	        	 
	 				j.put("result", "00");
	 				j.put("info", "Password berhasil dibuat. Password ini digunakan untuk setiap melakukan tandatangan elektronik.");
	 				j.put("status", "3");
	         } catch (JSONException e) {
	 				// TODO Auto-generated catch block
	 				user.setStatus('1');
	 				um.update(user);
	 				j.put("result", "06");
	 				j.put("info", "Buat password gagal !");
	 				LogSystem.error(getClass(), e);
	 			}
			
	         outStream.write(j.toString().getBytes());
        
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
		
        doPost(request, response);
	}
	

}
