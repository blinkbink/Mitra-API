package id.co.keriss.consolidate.util;

import java.util.Enumeration;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
public class LogSystem {
	public static Logger log=LogManager.getLogger("digisignlogger");
	
	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date datey = new Date();
	    String strDate = sdf.format(datey);
	    return strDate;
	}
	public static void request(HttpServletRequest request, String refTrx) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		String logData=ipAddress+" "+request.getRequestURI();
		logData="[/"+refTrx+"] <RECEIVE> ";

		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if(!isMultipart) {
		Enumeration<String> params = request.getParameterNames(); 
		while(params.hasMoreElements()){
        String paramName = params.nextElement();
			 	if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
			 		logData="["+paramName+"] : {WIPED};";

			 	}else
			 		logData="["+paramName+"] :\""+request.getParameter(paramName)+"\";";
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
				 		logData="["+paramName+"] : {WIPED};";

				 	}else
				 		logData="["+paramName+"] :\""+fileItem.getString()+"\";";

					
				}
				else {
					
				 	logData="["+fileItem.getFieldName()+"] :{"+fileItem.getName()+","+fileItem.getSize()+"};";

					// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

				}
			}

		}
		log.info(logData);
	}
	
	public static void request(HttpServletRequest request, List<FileItem> fileItems, String refTrx) {
		
		
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		// no multipart form
		if (!isMultipart) {
			request(request, refTrx);	
			
		}
		// multipart form
		else {
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			String logData=ipAddress+" "+request.getRequestURI();
			
			logData="[/"+refTrx+"] <RECEIVE> ";
			
		
			// Process the uploaded items
			for (FileItem fileItem : fileItems) {
				// a regular form field

				if (fileItem.isFormField()) {
					String paramName=fileItem.getFieldName();
					if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.equals("foto")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
				 		logData="["+paramName+"] : {WIPED};";

				 	}else
				 		logData="["+paramName+"] :\""+fileItem.getString()+"\";";

					
				}
				else {
					
				 	logData="["+fileItem.getFieldName()+"] :{"+fileItem.getName()+","+fileItem.getSize()+"};";

					// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

				}
			}
			log.info(logData);

		}
		
	}
	
	public static void response(HttpServletRequest request, JSONObject respData,String refTrx) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		String logData=ipAddress+" "+request.getRequestURI();
		
		logData="["+DSAPI.VERSION+"API/"+refTrx+"] <SEND>  { \"JSONFile\" : { ";
		Iterator<String> it=respData.keys();
		int i=0;
		while(it.hasNext()) {
			  if(i>0) {
				  logData=", ";
			  }
		      String ky = it.next();
		      String val="[WIPED]";
		      switch (ky) {
				case "signature-pic":
				case "fotoktp":
				case "fotodiri":
				case "file":
					
					break;
	
				default:
					try {
						val=respData.getString(ky);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
		      logData="\""+ky+"\":\""+val+"\"";
		      i++;
		      
		}
		
			
//		logData=" } }";
		log.info(logData);
	}
	
	public static void info(HttpServletRequest request, String respData,String refTrx, String PATH_APP, int LINE, String EMAIL_REQ, String MITRA_REQ, String CATEGORY, String RESPON_TIME) throws JSONException {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		String logData=ipAddress+" "+request.getRequestURI();
		
//		logData = PATH_APP + " " + LINE + " " + DSAPI.VERSION + " " + refTrx + " " + EMAIL_REQ + " " + MITRA_REQ + " " + CATEGORY + " " + RESPON_TIME + " " + respData;
		logData=" ["+DSAPI.VERSION+"API/"+refTrx+"] CHK : "+respData;
		
		log.info(logData);
	}
	
	public static void error(HttpServletRequest request, String respData, String refTrx, String PATH_APP, int LINE, String EMAIL_REQ, String MITRA_REQ, String CATEGORY, String RESPON_TIME) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		String logData=ipAddress+" "+request.getRequestURI();
		
//		logData = PATH_APP + " " + LINE + " " + DSAPI.VERSION + " " + refTrx + " " + EMAIL_REQ + " " + MITRA_REQ + " " + CATEGORY + " " + RESPON_TIME + " " + respData;
		logData="["+DSAPI.VERSION+"API/"+refTrx+"] CHK : "+respData;
		
		
		log.error(logData);
	}
	
	public static void error(Class className, Exception obj) {
		LogManager.getLogger(className).error("CHECK ",obj);
		
	}
	public static void info(Class className, String obj) {
		LogManager.getLogger(className).info(obj);
		
	}
	
	public static String getLogTag(String process,String timestamp,String log) {
		String tag="[WVAPI/"+process+"/"+timestamp+"] : ["+log+"] ";
		return tag;
	}

	public static String getLog( String respData, String tsp, String LOG) {
		String logData=null;

		logData=getLogTag("KMS",tsp, LOG);

		logData= " "+respData;
		
		return logData;
	}
	
}
