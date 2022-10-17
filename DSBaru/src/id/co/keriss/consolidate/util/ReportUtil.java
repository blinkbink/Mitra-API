package id.co.keriss.consolidate.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jpos.transaction.Context;
import org.jpublish.JPublishContext;
import org.jpublish.SiteContext;

import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

public class ReportUtil {
	private static ReportUtil instance = null;
	private static Locale locale;
	public static final long MILLIS_IN_A_DAY = 1000*60*60*24;
	protected ReportUtil(){
		
	}
	public static ReportUtil getInstance() {
		locale = new Locale("id", "ID");	 
		if(instance == null) {
	         instance = new ReportUtil();
	      }
	      return instance;
	}
	
	public String formatNumberOnly(Long amount){
		
		NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
		return formatter.format(amount).substring(2);
	}
	
	public String formatNumber(Long amount){
		
		NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
		return ""+formatter.format(amount)+"";
	}
	
	public String formatNumber(Double amount){
		NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
		return ""+formatter.format(amount)+"";
	}
	  public Date dateStart(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  System.out.println("dateVal ="+dateVal);
		  return dateVal;
	  }
	  
	  public Date dateEnd(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			dateVal=cal.getTime();
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  System.out.println("dateEnd ="+dateVal);
		  return dateVal;
	  }
	  public String dateTitle(String from, String to){
		  String date= from +" - "+ to;
		  if(from.equalsIgnoreCase(to))date = from;
		  return date;
	  }
	  public String now(){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		  return sdf.format(new Date());
	  }
	  
	  public static Date today(){
		  Calendar cal=Calendar.getInstance();
		  cal.set(Calendar.SECOND,0);
		  cal.set(Calendar.HOUR,0);
		  cal.set(Calendar.MINUTE,0);
		  return  cal.getTime();
	  }
	  
	  
	  public String yesterday(){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		  return sdf.format(new Date().getTime()-MILLIS_IN_A_DAY);
	  }
	  public String stringDate(Date date){
		return new SimpleDateFormat("dd-MM-yyyy").format(date); 
	  }
	  
	  public void toPdf(JPublishContext context,String template, String author) throws IOException{
			String templateName="",path ="";
			HttpServletRequest request = context.getRequest();
			HttpServletResponse response = context.getResponse();
//			template = request.getParameter("template");
			System.out.println("Template : "+template);
			SiteContext sctx = context.getSiteContext();
			if(template.equalsIgnoreCase("index")){
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}else{
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}
			StringWriter stringWriter=new StringWriter();
			VelocityEngine ve = new VelocityEngine();
			try {
				System.out.println("Path : "+path);
				ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,path);
				ve.mergeTemplate(templateName, sctx.getCharacterEncodingManager().getMap(path).getPageEncoding(), context, stringWriter);
//				ve.mergeTemplate(templateName, "utf8", context, stringWriter);
	
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			String mergedContent = stringWriter.toString();
			if(!author.equals(""))author="-"+author;
			
			OutputStream file = response.getOutputStream();//new FileOutputStream(new File(request.getContextPath()+"webapps/consolidate/static/rekon/Starlink"+author+".pdf"));

			try {
//				OutputStream out = response.getOutputStream();
//				response.setContentType("application/pdf");
//				response.setHeader("Content-Disposition","attachment; filename="+template+".pdf" );
//				Document doc = new Document();
//				HTMLWorker worker = new HTMLWorker(doc);
//				PdfWriter.getInstance(doc, out);
//				doc.open();
//				worker.parse(new StringReader(mergedContent));
//				doc.close();
	            Document document = new Document();
	            PdfWriter.getInstance(document, file);
	            document.open();
	            HTMLWorker htmlWorker = new HTMLWorker(document);
	            htmlWorker.parse(new StringReader(mergedContent));
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition","attachment; filename="+template+author+".pdf" );
				document.close();
	            file.flush();
	            
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	  }
	  
	  public void toPdf(JPublishContext context,String template){
			String templateName="",path ="";
			HttpServletRequest request = context.getRequest();
			HttpServletResponse response = context.getResponse();
//			template = request.getParameter("template");
			System.out.println("Template : "+template);
			SiteContext sctx = context.getSiteContext();
			if(template.equalsIgnoreCase("index")){
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}else{
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}
			StringWriter stringWriter=new StringWriter();
			VelocityEngine ve = new VelocityEngine();
			try {
				System.out.println("Path : "+path);
				ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,path);
				ve.mergeTemplate(templateName, sctx.getCharacterEncodingManager().getMap(path).getPageEncoding(), context, stringWriter);
//				ve.mergeTemplate(templateName, "utf8", context, stringWriter);
	
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			String mergedContent = stringWriter.toString();
			try {
//				OutputStream out = response.getOutputStream();
//				response.setContentType("application/pdf");
//				response.setHeader("Content-Disposition","attachment; filename="+template+".pdf" );
//				Document doc = new Document();
//				HTMLWorker worker = new HTMLWorker(doc);
//				PdfWriter.getInstance(doc, out);
//				doc.open();
//				worker.parse(new StringReader(mergedContent));
//				doc.close();
				OutputStream file = new FileOutputStream(new File(request.getContextPath()+"webapps/consolidate/static/rekon/Starlink.pdf"));
	            Document document = new Document();
	            PdfWriter.getInstance(document, file);
	            document.open();
	            @SuppressWarnings("deprecation")
	            HTMLWorker htmlWorker = new HTMLWorker(document);
	            htmlWorker.parse(new StringReader(mergedContent));
	            document.close();
	            file.flush();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	  }
	  
//	  public void toXls(JPublishContext context,String template){
//			String templateName="",path ="";
//			HttpServletRequest request = context.getRequest();
//			HttpServletResponse response = context.getResponse();
//			template = request.getParameter("template");
//			SiteContext sctx = context.getSiteContext();
//			if(template.equalsIgnoreCase("index")){
//				templateName = template+".html";
//				path=sctx.getRoot().getPath()+"/content";
//			}else{
//				path=sctx.getRoot().getPath()+"/content";
//				templateName = template+".html";
//			}
//			StringWriter stringWriter = new StringWriter();
//			VelocityEngine ve = new VelocityEngine();
//			try {
//				ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,path);
//				ve.mergeTemplate(templateName, "utf8", context, stringWriter);
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			} 
//			String mergedContent = stringWriter.toString();
//			try{
//				response.setContentType("application/vnd.ms-excel");
//	            response.setHeader("Content-Disposition","attachment; filename="+template+".xls" );
//	            PrintWriter pw = response.getWriter();
//	            System.out.print(mergedContent);
//	            pw.flush();
//	            pw.print(mergedContent);
//	            //pw.flush();
//	            //pw.close();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//
//	  }

	  public void toXls(JPublishContext context,String template){
		  
			String templateName="",path ="";
			HttpServletRequest request = context.getRequest();
			HttpServletResponse response = context.getResponse();
			template = request.getParameter("template");
			SiteContext sctx = context.getSiteContext();
			if(template.equalsIgnoreCase("index")){
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}else{
				templateName = template+".html";
				path=sctx.getRoot().getPath()+"/content/report/other";
			}
			StringWriter stringWriter = new StringWriter();
			VelocityEngine ve = new VelocityEngine();
			try {
				ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,path);
				ve.mergeTemplate(templateName, sctx.getCharacterEncodingManager().getMap(path).getPageEncoding(), context, stringWriter);
				
				//ve.mergeTemplate(templateName, "utf8", context, stringWriter);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			String mergedContent = stringWriter.toString();
			try{
				response.setContentType("application/vnd.ms-excel");
	            response.setHeader("Content-Disposition","attachment; filename="+template+".xls" );
	            PrintWriter pw = response.getWriter();
	            //System.out.print(mergedContent);
	            //pw.flush();
	            pw.print(mergedContent);
	            //pw.flush();
	            pw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		  //System.out.println("report xls");
	  }	  
	  public void toRekonSL(Context context,String contentPostpaid,String contentPrepaid,String contentNonTaglis,String date,String partner, int postsize, int presize, int nonsize){
		  String uploadTo = "../rekonfile/";
	      String pathPostpaid = partner+"-50501-99-"+date+".ftr";
	      String pathPrepaid = partner+"-50502-99-"+date+".ftr";
	      String pathNontaglis = partner+"-50504-99-"+date+".ftr";
	      try {
	    	  	if(postsize>0){
		            FileWriter fw = new FileWriter(uploadTo+pathPostpaid);
		            BufferedWriter bw = new BufferedWriter(fw);
		            bw.write(contentPostpaid);
		            bw.close();
		            
	    	  	}
	    	  	
	    	  	if(presize>0){
	    	  		FileWriter fw = new FileWriter(uploadTo+pathPrepaid);
	    	  		BufferedWriter bw = new BufferedWriter(fw);
		            bw.write(contentPrepaid);
		            bw.close();
		            System.out.println(uploadTo+pathPrepaid);
	    	  	}
	            
	    	  	if(nonsize>0){
		    	  	FileWriter fw = new FileWriter(uploadTo+pathNontaglis);
		            BufferedWriter bw = new BufferedWriter(fw);
		            bw.write(contentNonTaglis);
		            bw.close();
	    	  	}
	            	    	  	
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      }
	  }
	  
	  public void toRekonSLZip(JPublishContext context,String contentPostpaid,String contentPrepaid,String contentNonTaglis,String date,String partner){
		  HttpServletRequest request = context.getRequest();
		  String uploadTo = request.getContextPath()+"../rekonfilemanual/";
	      String pathPostpaid = partner+"-50501-99-"+date+".ftr";
	      String pathPrepaid = partner+"-50502-99-"+date+".ftr";
	      String pathNontaglis = partner+"-50504-99-"+date+".ftr";
	      try {
	            FileWriter fw = new FileWriter(uploadTo+pathPostpaid);
	            BufferedWriter bw = new BufferedWriter(fw);
	            bw.write(contentPostpaid);
	            bw.close();
	    	  	
	            fw = new FileWriter(uploadTo+pathPrepaid);
	            bw = new BufferedWriter(fw);
	            bw.write(contentPrepaid);
	            bw.close();
	            
	            fw = new FileWriter(uploadTo+pathNontaglis);
	            bw = new BufferedWriter(fw);
	            bw.write(contentNonTaglis);
	            bw.close();
	            
	    	  	File f = new File(uploadTo+pathPostpaid);
	    	    if(!f.exists()){
	    	    System.out.println("File not found.");
	    	    }
	    	    String zipFileName =  uploadTo+"rekon-CSA"+date+".zip";
//	    	    String zipFileName =  request.getContextPath()+"../"+date+".zip";

	    	    byte[] buffer = new byte[18024];
	    	    byte[] buffer2 = new byte[18024];
	    	    byte[] buffer3= new byte[18024];
	    	  	
	    	  	 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
	  			 out.setLevel(Deflater.DEFAULT_COMPRESSION);
	  			 FileInputStream in = new FileInputStream(uploadTo+pathPostpaid);
	  			 out.putNextEntry(new ZipEntry(pathPostpaid));
	  			 int len;
	  			 while ((len = in.read(buffer)) > 0){
	  			 out.write(buffer, 0, len);
	  			 }
	  			 out.closeEntry();
	  			 in.close();
	  			 
	  			 
	  			 FileInputStream in2 = new FileInputStream(uploadTo+pathPrepaid);
	  			 out.putNextEntry(new ZipEntry(pathPrepaid));
	  			 while ((len = in2.read(buffer2)) > 0){
	  			 out.write(buffer2, 0, len);
	  			 }
	  			 out.closeEntry();
	  			 in2.close();
	  			 
	  			 FileInputStream in3 = new FileInputStream(uploadTo+pathNontaglis);
	  			 out.putNextEntry(new ZipEntry(pathNontaglis));
	  			 while ((len = in3.read(buffer3)) > 0){
	  			 out.write(buffer3, 0, len);
	  			 }
	  			 out.closeEntry();
	  			 in3.close();
	  			 out.close();
	    	  	
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      }
	  }
	  
}
