package id.co.keriss.consolidate.action.ajax;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class DocController extends ActionSupport implements DSAPI{

	public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds
	private String getreq = null;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		int count = 21;
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse  response  = context.getResponse();
        
	    String process=request.getRequestURI().split("/")[2];
	    String[] uri=request.getRequestURI().split("/");
	    
//        System.out.println("masukkkk nihhhhhhhhhhhhhhhhh");
//        log.info(request.getRemoteUser()+" |" +request.getRemoteAddr()+" info >>>>>>> masukkkk nihhhhhhhhhhhhhhhhh");
	    LogSystem.request(request);


		try{

			String method = null;
			method = request.getParameter("frmProcess");
	        User userTrx= new UserManager(db).findByUsername(request.getParameter("user"));
			
//	        if(userTrx==null) {
//		        sendRedirect (context, request.getContextPath() 
//	                    + "/stop.html"
//	                );
//		        return;
//	        }
//	        if(!userTrx.getPassword().equals(request.getParameter("pwd"))) {
//		        sendRedirect (context, request.getContextPath() 
//	                    + "/stop.html"
//	                );
//		        return;
//	        }
	        
	        
	        if(userTrx==null  && request.getRequestURL().indexOf("signdoc.html")<=0) {
		        sendRedirect (context, request.getContextPath() 
	                    + "/stop.html"
	                );
		        LogSystem.info(request, "stop.html");
		        return;
	        }
	        if(!userTrx.getLogin().getPassword().equals(request.getParameter("pwd")) && request.getRequestURL().indexOf("signdoc.html")<=0) {
		        sendRedirect (context, request.getContextPath() 
	                    + "/stop.html"
	                );
		        LogSystem.info(request, "stop.html");
		        return;
	        }
	       
			String jsonResult = null;
			if(process.equals("viewdoc.html")) {
				String idDoc=request.getParameter("idDoc");
				String userEmail=request.getParameter("user");
				UserManager um=new UserManager(db);
				System.out.println(userEmail);
				User usr= um.findByUsername(userEmail);
				if(usr!=null) {

					DocumentsAccessDao docDao=new DocumentsAccessDao(db);
					DocumentAccess da =docDao.findByDocAndUserShareSign(idDoc, String.valueOf(usr.getId()), userEmail);
					Documents doc=null;
					if(da==null) {
						doc=new DocumentsDao(db).findByUserID(String.valueOf(usr.getId()), Long.valueOf(idDoc));
						context.put("pdf_link", "https://"+DOMAINAPI+"/dt01.html/"+usr.getId()+"/"+doc.getSigndoc());
					}else {
						if(!da.isRead()) {
							da.setRead(true);
							docDao.update(da);
						}
						doc= da.getDocument();
						context.put("pdf_link", "https://"+DOMAINAPI+"/dt01.html/"+da.getDocument().getEeuser().getId()+"/"+doc.getSigndoc());
					}
					context.put("title_doc", doc.getFile_name());

			        LogSystem.info(request, "Open Document ID : "+idDoc);
				}else {
			        LogSystem.info(request, "Error user : "+request.getParameter("user"));
					
				}
			}		
			if(process.equals("signdoc.html")) {
				HttpSession session = request.getSession();
	            session.setAttribute (USER, userTrx);
				String idDoc=request.getParameter("idDoc");
				String userEmail=request.getParameter("user");
				UserManager um=new UserManager(db);
				System.out.println(userEmail);
				User usr= um.findByUsername(userEmail);
				if(usr!=null) {
					DocumentsAccessDao docDao=new DocumentsAccessDao(db);
					List<DocumentAccess> da =docDao.findDocAccessEEuser(idDoc, String.valueOf(usr.getId()), userEmail);
					System.out.println("TES SSSSSSSSSSSSSSSSSSS :"+usr.getUserdata().getImageTtd());
					context.put("pdf_link", "https://"+DOMAINAPI+"/dt01.html/"+da.get(0).getDocument().getEeuser().getId()+"/"+da.get(0).getDocument().getSigndoc());
					File img=new File(usr.getUserdata().getImageTtd());
					byte[] encoded = Base64.encode(FileUtils.readFileToByteArray(img));

					
					JSONObject dataUser=new JSONObject();
					dataUser.put("idDoc", idDoc);
					JSONArray uL=new JSONArray();
					for(DocumentAccess dAcs:da) {
						if( (dAcs.getType().equals("sign") && dAcs.isFlag()==true)) {
							continue;
						}
						if(!dAcs.isRead()) {
							dAcs.setRead(true);
							docDao.update(dAcs);
						}
						if(dAcs.getType().equals("share")) {
							continue;
						}
						JSONObject us=new JSONObject();
						us.put("idAccess", dAcs.getId().toString());
						us.put("lx", dAcs.getLx());
						us.put("ly", dAcs.getLy());
						us.put("rx", dAcs.getRx());
						us.put("ry", dAcs.getRy());
						us.put("page",dAcs.getPage());	
						uL.put(us);
					}
					dataUser.put("user", uL);
					System.out.println(dataUser.toString());
					context.put("locSign", dataUser);
					context.put("sgn_img", new String(encoded, StandardCharsets.US_ASCII));
					context.put("title_doc", da.get(0).getDocument().getFile_name());
				    LogSystem.info(request, "Open Document ID : "+idDoc);
				}else {
			        LogSystem.info(request, "Error user : "+request.getParameter("user"));
					
				}
			}
			if(process.equals("reqsign.html")) {
				HttpSession session = request.getSession();
	            session.setAttribute (USER, userTrx);
		            
				JSONObject data=new JSONObject();
				String idDoc=request.getParameter("idDoc");
				String userEmail=request.getParameter("user");
				UserManager um=new UserManager(db);
				System.out.println(userEmail);
				User usr= um.findByUsername(userEmail);
				if(usr!=null) {
					DocumentsAccessDao docDao=new DocumentsAccessDao(db);
					List<DocumentAccess> da =docDao.findByDoc(idDoc);
					String renameDoc= da.get(0).getDocument().getSigndoc();
					String namaDoc= da.get(0).getDocument().getFile_name();
					String idUserDoc=String.valueOf(da.get(0).getDocument().getEeuser().getId());
					System.out.println("doc: "+renameDoc);
					JSONArray dataUser=new JSONArray();
					for (DocumentAccess documentAccess : da) {
						JSONObject userRq=new JSONObject();
						if(documentAccess.getType().equals("share"))continue;
						userRq.put("user", documentAccess.getName());
						userRq.put("email", documentAccess.getEmail());
						userRq.put("status", "0");
						dataUser.put(userRq);
					}
					data.put("data", dataUser);
					data.put("idDoc", idDoc);
					context.put("jsonObj", data);
					context.put("pdf_link", request.getRequestURL().substring(0,request.getRequestURL().indexOf("reqsign.html"))+"dt01.html/"+idUserDoc+"/"+renameDoc);
					context.put("title_doc", namaDoc);
					LogSystem.info(request, "Open Document ID : "+idDoc);
				}else {
			        LogSystem.info(request, "Error user : "+request.getParameter("user"));
					
				}
			}
		
		
			
			
			

		}catch (Exception e) {
            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
		}
	}

	/*
    private String extractFileName(Part part) {
        // form-data; name="file"; filename="C:\file1.zip"
        // form-data; name="file"; filename="C:\Note\file2.zip"
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                // C:\file1.zip
                // C:\Note\file2.zip
                String clientFileName = s.substring(s.indexOf("=") + 2, s.length() - 1);
                clientFileName = clientFileName.replace("\\", "/");
                int i = clientFileName.lastIndexOf('/');
                // file1.zip
                // file2.zip
                return clientFileName.substring(i + 1);
            }
        }
        return null;
    }
	
	*/
	public static String request(HttpResponse response) {
		String result = "";
		try {
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				str.append(line + "\n");
			}
			in.close();
			result = str.toString();
		} catch (Exception ex) {
			result = "Error";
		}
		return result;
	}
	
	private long stream(InputStream input, OutputStream output) throws IOException {

	    try (ReadableByteChannel inputChannel = Channels.newChannel(input); WritableByteChannel outputChannel = Channels.newChannel(output)) {
	        ByteBuffer buffer = ByteBuffer.allocate(10240);
	        long size = 0;

	        while (inputChannel.read(buffer) != -1) {
	            buffer.flip();
	            size += outputChannel.write(buffer);
	            buffer.clear();
	        }
	        return size;
	    }
	}
	
}
