package id.co.keriss.consolidate.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class LogSystem {
	public static Logger log=LogManager.getLogger("digisignlogger");
	
	public static String request(HttpServletRequest request) {
		
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
		}
		String logData=ipAddress+" "+request.getRequestURI();
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData+= formattedDate + " [RECEIVE] ";

		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if(!isMultipart) {
		Enumeration<String> params = request.getParameterNames(); 
		while(params.hasMoreElements()){
        String paramName = params.nextElement();
			 	if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
			 		logData+="["+paramName+"] : {WIPED};";

			 	}else
			 		logData+="["+paramName+"] :\""+request.getParameter(paramName)+"\";";
			}		
		}
		else {
			
			ServletFileUpload upload = new ServletFileUpload(
					new DiskFileItemFactory());

			// parse requests
			List<FileItem> fileItems=null;
			try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			// Process the uploaded items
			for (FileItem fileItem : fileItems) {
				// a regular form field

				if (fileItem.isFormField()) {
					String paramName=fileItem.getFieldName();
					if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
				 		logData+="["+paramName+"] : {WIPED};";

				 	}else
				 		logData+="["+paramName+"] :\""+fileItem.getString()+"\";";

					
				}
				else {
					
				 	logData+="["+fileItem.getFieldName()+"] :{"+fileItem.getName()+","+fileItem.getSize()+"};";

					// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

				}
			}

		}
		return logData;
	}
	
	public static String request(HttpServletRequest request, List<FileItem> fileItems, String idTrx) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
		}
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		String logData=ipAddress+" "+request.getRequestURI();
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		// no multipart form
		if (!isMultipart) {
			request(request);	
			
		}
		// multipart form
		else {
			
			logData+= "["+idTrx+"]" + " [RECEIVE] ";
			
			// Process the uploaded items
			for (FileItem fileItem : fileItems) {
				// a regular form field

				if (fileItem.isFormField()) {
					String paramName=fileItem.getFieldName();
					if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.equals("foto")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
				 		logData+="["+paramName+"] : {WIPED};";

				 	}else
				 		logData+="["+paramName+"] :\""+fileItem.getString()+"\";";

					
				}
				else {
					
				 	logData+="["+fileItem.getFieldName()+"] :{"+fileItem.getName()+","+fileItem.getSize()+"};";

					// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

				}
			}

		}
		return logData;

	}
	

	public static String getLogTag(String process,String timestamp,String log) {
		String tag="[Generate Link/"+process+"/"+timestamp+"] : ["+log+"] ";
		return tag;
	}
	
	public static String getRESLog(HttpServletRequest request, String respData, Date tsp, String LOG) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
		}
		long elapsedtime=new Date().getTime()-tsp.getTime();
		String logData=getLogTag("RES-ACC", String.valueOf(tsp.getTime()), LOG) + ipAddress+" "+request.getRequestURI()+"("+elapsedtime+ ")";
		
        
		logData= " "+respData;
		
		return logData;
	}
	
	public static String getGENACTLog(HttpServletRequest request, String respData, Date tsp, String LOG) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
		}
		long elapsedtime=new Date().getTime()-tsp.getTime();
		String logData=getLogTag("GEN-ACT", String.valueOf(tsp.getTime()), LOG) + ipAddress+" "+request.getRequestURI()+"("+elapsedtime+ ")";
		
        
		logData= " "+respData;
		
		return logData;
	}
	public static String getGENSGNLog(HttpServletRequest request, String respData, Date tsp, String LOG) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
		}
		long elapsedtime=new Date().getTime()-tsp.getTime();
		String logData=getLogTag("GEN-SGN", String.valueOf(tsp.getTime()), LOG) + ipAddress+" "+request.getRequestURI()+"("+elapsedtime+ ")";
		
        
		logData= " "+respData;
		
		return logData;
	}
	
	public static void info(HttpServletRequest request, String respData, String idTrx) {
		String logData=request.getRemoteAddr()+" "+request.getRequestURI();
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData= "["+idTrx+"]" + " [CHECK] : "+respData;
		
		
		log.info(logData);
	}
	
	public static void error(HttpServletRequest request, String respData, String idTrx) {
		String logData=request.getRemoteAddr()+" "+request.getRequestURI();
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData= "["+idTrx+"]" + " [CHECK] : "+respData;
		
		
		log.error(logData);
	}
	
	public static void error(Class className, Exception obj) {
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
		
		LogManager.getLogger(className).error(formattedDate + " [CHECK] ",obj);
		
	}
	public static void info(Class className, String obj) {
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		LogManager.getLogger(className).info(formattedDate + " [INFO] " + obj);
		
	}
}
