//package id.co.keriss.consolidate.util;
//
//
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.Toolkit;
//import java.awt.image.BufferedImage;
//import java.awt.image.FilteredImageSource;
//import java.awt.image.ImageFilter;
//import java.awt.image.ImageProducer;
//import java.awt.image.RGBImageFilter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.net.URLEncoder;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.commons.fileupload.FileItem;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.DB;
//import org.jpos.ee.User;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;
//import com.itextpdf.text.pdf.parser.PdfTextExtractor;
//import com.lowagie.text.Image;
//import com.lowagie.text.Rectangle;
//import com.lowagie.text.pdf.BaseFont;
//import com.lowagie.text.pdf.PdfContentByte;
//import com.lowagie.text.pdf.PdfReader;
//import com.lowagie.text.pdf.PdfStamper;
//
//import id.co.keriss.consolidate.dao.DocumentsDao;
//import id.co.keriss.consolidate.ee.Documents;
//import id.co.keriss.consolidate.ee.FormatPdf;
//
//public class SFTPProcessor implements DSAPI{
//	
//	  Documents dc =null;
//	  int jmlPage=0;
//	  Date tgl= new Date();
//		SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		String refTrx="VERF"+sdfDate2.format(tgl).toString();
//		String kelas="id.co.keriss.consolidate.util.FileProcessor";
//		String trxType="SVD-DT";
//
//	
//	public int getJmlPage() {
//		return jmlPage;
//	}
//
//
//	public void setJmlPage(int jmlPage) {
//		this.jmlPage = jmlPage;
//	}
//
//
//	public Documents getDc() {
//		return dc;
//	}
//
//
//	public void setDc(Documents dc) {
//		this.dc = dc;
//	}
//	
//	public SFTPProcessor(String refTrx) {
//		this.refTrx=refTrx;
//	}
//
//	public static java.awt.Image makeColorTransparent(BufferedImage im, final Color color, float threshold) {
//	    ImageFilter filter = new RGBImageFilter() {
//	        public float markerAlpha = color.getRGB() | 0xFF000000;
//	        @Override
//			public final int filterRGB(int x, int y, int rgb) {
//	            int currentAlpha = rgb | 0xFF000000;           // just to make it clear, stored the value in new variable
//	            float diff = Math.abs((currentAlpha - markerAlpha) / markerAlpha);  // Now get the difference
//	            if (diff <= threshold) {                      // Then compare that threshold value
//	                return 0x00FFFFFF & rgb;
//	            } else {
//	                return rgb;
//	            }
//	        }
//	    };
//	    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
//	    return Toolkit.getDefaultToolkit().createImage(ip);
//	}
//	
//	public static BufferedImage ImageToBufferedImage(java.awt.Image image, int width, int height)
//	  {
//	    BufferedImage dest = new BufferedImage(
//	        width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
//	    Graphics2D g2 = dest.createGraphics();
//	    g2.drawImage(image, 0, 0, null);
//	    g2.dispose();
//	    return dest;
//	  }
//
//	
//	/*
//	public boolean uploadFile(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave) throws Exception {
//		return uploadFile(request, db, userTrx, fileSave, null);
//	}
//	
//	
//	public boolean uploadFile(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra) throws Exception {
//		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
////		  if(uploadTo.indexOf("/ds-api")>=0) {
////			  uploadTo=uploadTo.substring(7);
////		  }
////		  if(directoryName.indexOf("/ds-api")>=0) {
////			  directoryName=directoryName.substring(7);
////		  }
//		  File directory = new File(directoryName);
//		  if (!directory.exists()){
//		       directory.mkdirs();
//		  }
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+".pdf";
//		  dc.setEeuser(userTrx);
//          dc.setWaktu_buat(date);	
//          File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
////          File fileTo = new File((uploadTo +rename));
//          fileSave.write(fileTo);
////          Blob blob = Hibernate.createBlob(fileSave.getInputStream());
//          dc.setFile(uploadTo);
//          dc.setFile_name(fileSave.getName());
//          dc.setPath(uploadTo);
//          dc.setSigndoc(rename);
//          dc.setRename(rename);
//          dc.setStatus('F');
//          dc.setPayment('1');
//          //if(userTrx.getMitra()!=null)dc.setIdMitra(userTrx.getMitra().getId().toString());
//          new DocumentsDao(db).create(dc);
//		return true;
//	}
//	
//	public boolean uploadFileMitra(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra) throws Exception {
//		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
////		  if(uploadTo.indexOf("/ds-api")>=0) {
////			  uploadTo=uploadTo.substring(7);
////		  }
////		  if(directoryName.indexOf("/ds-api")>=0) {
////			  directoryName=directoryName.substring(7);
////		  }
//		  File directory = new File(directoryName);
//		  if (!directory.exists()){
//		       directory.mkdirs();
//		  }
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+".pdf";
//		  dc.setEeuser(userTrx);
//        dc.setWaktu_buat(date);	
//        File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
////        File fileTo = new File((uploadTo +rename));        
//        fileSave.write(fileTo);
//        //FileWriter fw=new FileWriter(fileTo);
//        //fileSave.getOutputStream().close();
////        Blob blob = Hibernate.createBlob(fileSave.getInputStream());
//        dc.setFile(uploadTo);
//        dc.setFile_name(fileSave.getName());
//        dc.setPath(uploadTo);
//        dc.setSigndoc(rename);
//        dc.setRename(rename);
//        dc.setStatus('F');
//        dc.setPayment('1');
//        if(idMitra!=null) {
//        	dc.setIdMitra(idMitra);
//        	dc.setPayment('3');
//        }
//        Long iddoc=new DocumentsDao(db).create2(dc);
//        dc.setId(iddoc);
//		return true;
//	}
//	
//	
//	public boolean uploadFILEnQRMitraArray(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra, String dateRename) throws Exception {
//		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
////		  if(uploadTo.indexOf("/ds-api")>=0) {
////			  uploadTo=uploadTo.substring(7);
////		  }
////		  if(directoryName.indexOf("/ds-api")>=0) {
////			  directoryName=directoryName.substring(7);
////		  }
//		  File directory = new File(directoryName);
//		  if (!directory.exists()){
//		       directory.mkdirs();
//		  }
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+".pdf";
//		  dc.setEeuser(userTrx);
//    dc.setWaktu_buat(date);	
//    File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
////    File fileTo = new File((uploadTo +rename));
//    byte[] data=fileSave.get();
//    
//    fileSave.write(fileTo);
//    //FileWriter fw=new FileWriter(fileTo);
//    fileSave.getOutputStream().close();
////    Blob blob = Hibernate.createBlob(fileSave.getInputStream());
//    dc.setFile(uploadTo);
//    dc.setFile_name(fileSave.getName());
//    dc.setPath(uploadTo);
//    dc.setSigndoc(dateRename);
//    dc.setRename(dateRename);
//    dc.setStatus('F');
//    dc.setPayment('1');
//    if(idMitra!=null) {
//    	dc.setIdMitra(idMitra);
//    	dc.setPayment('3');
//    }
//    Long iddoc=new DocumentsDao(db).create2(dc);
//    dc.setId(iddoc);
//    
//    //generateQRCode2(db, data, uploadTo, String.valueOf(iddoc));
//		return true;
//	}
//	
//	*/
//	
//	public boolean uploadFILEnQRMitra(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra, JSONObject message) throws Exception {
//		  String uploadTo =  "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  String directoryName = "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
////		  if(uploadTo.indexOf("/ds-api")>=0) {
////			  uploadTo=uploadTo.substring(7);
////		  }
////		  if(directoryName.indexOf("/ds-api")>=0) {
////			  directoryName=directoryName.substring(7);
////		  }
//		  File directory = new File(directoryName);
//		  if (!directory.exists()){
//		       directory.mkdirs();
//		  }
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+idMitra+".pdf";
//		  dc.setEeuser(userTrx);
//      dc.setWaktu_buat(date);	
//      
//      /*
//      File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
////      File fileTo = new File((uploadTo +rename));
//      byte[] data=fileSave.get();
//      
//      fileSave.write(fileTo);
//      //FileWriter fw=new FileWriter(fileTo);
//      fileSave.getOutputStream().close();
////      Blob blob = Hibernate.createBlob(fileSave.getInputStream());
//      */
//      
//      boolean resp=false;
//      
//      SaveFileWithSamba samba=new SaveFileWithSamba();
//      resp=samba.write(fileSave.get(), uploadTo+rename);
//      LogSystem.info(request, "hasil save File : "+resp, kelas, refTrx, trxType);
//      if(resp==false) {
//    	  LogSystem.error(request, "error samba", kelas, refTrx, trxType);
//      }
//      
//      try {  
//    	    //PdfReader pdfReader = new PdfReader(fileTo);  
//    	    //com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(fileTo.getPath());
//    	    com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(samba.openfile(uploadTo+rename));
//
//    	    String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage( pdfread, 1 );
//    	    jmlPage=pdfread.getNumberOfPages();
//    	    LogSystem.info(request, "jumlah page = "+jmlPage);
//    	    LogSystem.info(request, "size text = "+textFromPdfFilePageOne.length());
//    	}  
//    	catch ( Exception e ) {
//    	    // handle exception
//    		//fileTo.delete();
//    		samba.deletefile(uploadTo+rename);
//    		LogSystem.error(request, "bukan file pdf", kelas, refTrx, trxType);
//    		return false;
//    		
//    	} 
//      
//      dc.setFile(uploadTo);
//      dc.setFile_name(fileSave.getName());
//      dc.setPath(uploadTo);
//      dc.setSigndoc(rename);
//      dc.setRename(rename);
//      dc.setStatus('T');
//      dc.setPayment('1');
//      if(idMitra!=null) {
//      	dc.setIdMitra(idMitra);
//      	dc.setPayment('3');
//      }
//      boolean redirect=false;
//      if(message.has("redirect"))redirect=message.getBoolean("redirect");
//      dc.setRedirect(redirect);
//      if(message.has("branch"))dc.setBranch(message.getString("branch"));
//      if(message.has("dealer_code"))dc.setDealer_code(message.getString("dealer_code"));
//      if(message.has("sequence_option"))dc.setSequence(message.getBoolean("sequence_option"));
//      else dc.setSequence(false);
//      if(message.has("kode_authorized"))dc.setKode_authorized(message.getString("kode_authorized"));
//      DocumentsDao ddao=new DocumentsDao(db);
//      Long iddoc=ddao.create2(dc);
//      dc.setId(iddoc);
//      
//      List<Documents> ldoc=ddao.findByDocIdMitraTrue(idMitra, userTrx.getMitra().getId());
//      for(Documents doc:ldoc) {
//    	  if(doc.getId()!=iddoc) {
//    		  doc.setDelete(true);
//    		  ddao.update(doc);
//    		  LogSystem.info(request, "ada yang double document_id", kelas, refTrx, trxType);
//    	  }
//      }
//      
//      //generateQRCode2(db, data, uploadTo, String.valueOf(iddoc));
//		return true;
//	}
//	
//	public JSONObject uploadFILESFTP(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra, JSONObject message) throws Exception {
//		  String uploadTo =  "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  String directoryName = "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  JSONObject res=new JSONObject();
//		  res.put("rc", "91");
//		  res.put("keterangan", "default");
//		  
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+idMitra+".pdf";
//		  dc.setEeuser(userTrx);
//		  dc.setWaktu_buat(date);	
//    
//    
//	    boolean resp=false;
//	    SaveFileWithSamba samba;
//	    
//	    //data to temp local file
//    	UUID uuid = UUID.randomUUID();
//    	String localFilePath = "/tmp/"+uuid.toString().replace("-", "")+".pdf";
//    	LogSystem.info(request, "Temp File : "+localFilePath, kelas, refTrx, trxType);
//    	SFTPUtility sftp = new SFTPUtility();
//	    try {	    	
//	    	LogSystem.info(request, "Uploading with sftp", kelas, refTrx, trxType);
//	    	LogSystem.info(request, "File path : " +uploadTo+rename, kelas, refTrx, trxType);
//		    resp=sftp.upload(fileSave.get(), uploadTo+rename, localFilePath);
//		    LogSystem.info(request, "Finish uploading with sftp", kelas, refTrx, trxType);
//
//		    LogSystem.info(request, "hasil save File : "+resp, kelas, refTrx, trxType);
//		    if(resp==false) {
//		  	  	LogSystem.error(request, "error sftp", kelas, refTrx, trxType);
//			  	res.put("rc", "91");
//				res.put("keterangan", "timeout");
//				return res;
//		    }
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			LogSystem.error(request, "error sftp", kelas, refTrx, trxType);
//			res.put("rc", "91");
//			res.put("keterangan", "timeout");
//			return res;
//		}
//	    
//	    try {  
//	  	    //PdfReader pdfReader = new PdfReader(fileTo);  
//	  	    //com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(fileTo.getPath());
//	    	byte[] downFile = sftp.download(localFilePath, uploadTo+rename);
//	  	    //com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(samba.openfile(uploadTo+rename));
//	  	  	com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(downFile);
//	
//	  	    String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage( pdfread, 1 );
//	  	    jmlPage=pdfread.getNumberOfPages();
//	  	    LogSystem.info(request, "jumlah page = "+jmlPage);
//	  	    LogSystem.info(request, "size text = "+textFromPdfFilePageOne.length());
//	  	}  
//	  	catch ( Exception e ) {
//	  	    // handle exception
//	  		//fileTo.delete();
////	  		samba.deletefile(uploadTo+rename);
//	  		sftp.delete(uploadTo+rename);
//	  		LogSystem.info(request, "bukan file pdf", kelas, refTrx, trxType);
//	  		res.put("rc", "01");
//	  		res.put("keterangan", "bukan file pdf");
//	  		return res;
//	  	} 
//	    
//	    dc.setFile(uploadTo);
//	    dc.setFile_name(fileSave.getName());
//	    dc.setPath(uploadTo);
//	    dc.setSigndoc(rename);
//	    dc.setRename(rename);
//	    dc.setStatus('T');
//	    dc.setPayment('1');
//	    if(idMitra!=null) {
//	    	dc.setIdMitra(idMitra);
//	    	dc.setPayment('3');
//	    }
//	    boolean redirect=false;
//	    if(message.has("redirect"))redirect=message.getBoolean("redirect");
//	    dc.setRedirect(redirect);
//	    if(message.has("branch"))dc.setBranch(message.getString("branch"));
//	    if(message.has("dealer_code"))dc.setDealer_code(message.getString("dealer_code"));
//	    if(message.has("sequence_option"))dc.setSequence(message.getBoolean("sequence_option"));
//	    else dc.setSequence(false);
//	    if(message.has("kode_authorized"))dc.setKode_authorized(message.getString("kode_authorized"));
//	    if(message.has("exp_date")) {
//	    	dc.setExp_date(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(message.getString("exp_date")));
//	    	dc.setExp(true);
//	    }
//	    
//	    try {
//	    	DocumentsDao ddao=new DocumentsDao(db);
//	    	
//	    	ddao.updateWhere(idMitra);
//	    	
//	    	Long iddoc =ddao.create2(dc);
//	    	dc.setId(iddoc);
//	    	
//		} catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.error(request, "DB Failed", kelas, refTrx, trxType);
//	  		res.put("rc", "02");
//	  		res.put("keterangan", "DB Failed");
//		}
//	    
//	    
//		res.put("rc", "00");
//		res.put("keterangan", "sukses save file");
//		return res;
//	}
//	
//	public JSONObject uploadFILEnQRMitra3(HttpServletRequest  request,DB db, User userTrx, byte[] fileSave, String idMitra, JSONObject message, FormatPdf pdf) throws Exception {
//		  String uploadTo =  "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  String directoryName = "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  JSONObject res=new JSONObject();
//		  res.put("rc", "91");
//		  res.put("keterangan", "default");
//		  
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+idMitra+".pdf";
//		  dc.setEeuser(userTrx);
//		  dc.setWaktu_buat(date);
//		  dc.setFormat_pdf(pdf);
//  
//  
//	    boolean resp=false;
//	    SaveFileWithSamba samba;
//	    try {
//	    	samba=new SaveFileWithSamba();
//		    resp=samba.write(fileSave, uploadTo+rename);
//		    LogSystem.info(request, "hasil save File : "+resp, kelas, refTrx, trxType);
//		    if(resp==false) {
//		  	  	LogSystem.error(request, "error samba", kelas, refTrx, trxType);
//			  	res.put("rc", "91");
//				res.put("keterangan", "timeout");
//				return res;
//		    }
//		} catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.error(request, "error samba", kelas, refTrx, trxType);
//			res.put("rc", "91");
//			res.put("keterangan", "timeout");
//			return res;
//		}
//	    
//	    
//	    try {  
//	  	    //PdfReader pdfReader = new PdfReader(fileTo);  
//	  	    //com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(fileTo.getPath());
//	  	    com.itextpdf.text.pdf.PdfReader pdfread=new com.itextpdf.text.pdf.PdfReader(samba.openfile(uploadTo+rename));
//	
//	  	    String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage( pdfread, 1 );
//	  	    jmlPage=pdfread.getNumberOfPages();
//	  	    LogSystem.info(request, "jumlah page = "+jmlPage);
//	  	    LogSystem.info(request, "size text = "+textFromPdfFilePageOne.length());
//	  	}  
//	  	catch ( Exception e ) {
//	  	    // handle exception
//	  		//fileTo.delete();
//	  		samba.deletefile(uploadTo+rename);
//	  		LogSystem.info(request, "bukan file pdf", kelas, refTrx, trxType);
//	  		res.put("rc", "01");
//	  		res.put("keterangan", "bukan file pdf");
//	  		
//	  	} 
//	    
//	    dc.setFile(uploadTo);
//	    dc.setFile_name(idMitra);
//	    dc.setPath(uploadTo);
//	    dc.setSigndoc(rename);
//	    dc.setRename(rename);
//	    dc.setStatus('T');
//	    dc.setPayment('1');
//	    if(idMitra!=null) {
//	    	dc.setIdMitra(idMitra);
//	    	dc.setPayment('3');
//	    }
//	    boolean redirect=false;
//	    if(message.has("redirect"))redirect=message.getBoolean("redirect");
//	    dc.setRedirect(redirect);
//	    if(message.has("branch"))dc.setBranch(message.getString("branch"));
//	    if(message.has("dealer_code"))dc.setDealer_code(message.getString("dealer_code"));
//	    if(message.has("sequence_option"))dc.setSequence(message.getBoolean("sequence_option"));
//	    else dc.setSequence(false);
//	    if(message.has("kode_authorized"))dc.setKode_authorized(message.getString("kode_authorized"));
//	    if(message.has("nama_format"))dc.setType_document(message.getString("nama_format").trim());
//	    
//	    try {
//	    	DocumentsDao ddao=new DocumentsDao(db);
//	    	
//	    	ddao.updateWhere(idMitra);
//	    	
//	    	Long iddoc =ddao.create2(dc);
//	    	dc.setId(iddoc);
//	    	
//		} catch (Exception e) {
//			// TODO: handle exception
//			LogSystem.error(request, "DB Failed", kelas, refTrx, trxType);
//	  		res.put("rc", "02");
//	  		res.put("keterangan", "DB Failed");
//		}
//	    
//	    
//		res.put("rc", "00");
//		res.put("keterangan", "sukses save file");
//		return res;
//	}
//	
//	/*
//	public JSONObject uploadFilePDF(FileItem fileSave, User userTrx, String document_id) {
//		JSONObject resp=new JSONObject();
//		String uploadTo =  "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		String directoryName = "/file2/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//
//		  File directory = new File(directoryName);
//		  if (!directory.exists()){
//		       directory.mkdirs();
//		  }
//		  dc = new Documents();
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "DS"+strDate+document_id+".pdf";
//		
//		return resp;
//	}
//	
//	public String uploadBuktiTransfer(HttpServletRequest  request, User userTrx, FileItem fileSave) throws Exception {
//		  String uploadTo =  "/opt/data-DS/BuktiTransfer/"+userTrx.getId()+"/";
//		  String directoryName = "/opt/data-DS/BuktiTransfer/"+userTrx.getId()+"/";
////		  if(uploadTo.indexOf("/ds-api")>=0) {
////			  uploadTo=uploadTo.substring(7);
////		  }
////		  if(directoryName.indexOf("/ds-api")>=0) {
////			  directoryName=directoryName.substring(7);
////		  }
//		  File directory = new File(directoryName);
//		  if (!directory.exists()){
//		       directory.mkdirs();
//		  }
//		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//		  Date date = new Date();
//		  String strDate = sdfDate.format(date);
//		  String rename = "TRF"+strDate+".jpg";
//		  
//        File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
////        File fileTo = new File((uploadTo +rename));
//        fileSave.write(fileTo);
////    
//		return uploadTo +rename;
//	}
//	*/
//	public boolean generateQRCode(DB db) {
//		boolean res=false;
//		if(dc==null)return res;
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//		Date date = new Date();
//		String strDate = sdfDate.format(date);
//		String rename = "DS" + strDate + ".pdf";
//		String docs;
//		try {
//			docs = AESEncryption.encryptDoc(String.valueOf(dc.getId()));
//		
//			String link = "https://"+DOMAIN+"/dsmobile/doc/verpdf.html?doc="
//					+ URLEncoder.encode(docs, "UTF-8");
//			generateQRCodeImage(link, dc.getPath() + "qr.jpg");
//	
//			PdfReader reader = new PdfReader(dc.getPath()+dc.getSigndoc());
//			
//			Rectangle mediabox = reader.getPageSize(1);
//			int h = (int) mediabox.getWidth();
//			int w = (int) mediabox.getHeight();
//			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dc.getPath() + rename));
//	
//			Image qr = Image.getInstance(dc.getPath() + "qr.jpg");
//			qr.setTransparency(new int[] { 0XF0, 0XFF });
//			qr.setAbsolutePosition(h - 100, w - 100);
//			PdfContentByte over = stamper.getOverContent(1);
//	
//			over.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 10);
//			over.stroke();
//			over.addImage(qr);
//			stamper.close();
//			reader.close();
//			
//			dc.setSigndoc(rename);
//			dc.setRename(rename);
//			new DocumentsDao(db).update(dc);
//			res=true;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		return res;
//	}
//	
//	public boolean generateQRCode2(DB db, byte[] data, String home, String iddoc) {
//		boolean res=false;
//		if(dc==null)return res;
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
//		Date date = new Date();
//		String strDate = sdfDate.format(date);
//		String rename = "DS" + strDate + ".pdf";
//		String docs;
//		try {
//			docs = AESEncryption.encryptDoc(iddoc);
//		
//			String link = "https://"+DOMAIN+"/dsmobile/doc/verpdf.html?doc="
//					+ URLEncoder.encode(docs, "UTF-8");
//			generateQRCodeImage(link, dc.getPath() + "qr.jpg");
//	
//			//PdfReader reader = new PdfReader(dc.getPath()+dc.getSigndoc());
//			PdfReader reader = new PdfReader(data);
//			
//			Rectangle mediabox = reader.getPageSize(1);
//			int h = (int) mediabox.getWidth();
//			int w = (int) mediabox.getHeight();
//			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(home + rename));
//	
//			Image qr = Image.getInstance(dc.getPath() + "qr.jpg");
//			qr.setTransparency(new int[] { 0XF0, 0XFF });
//			qr.setAbsolutePosition(h - 100, w - 100);
//			PdfContentByte over = stamper.getOverContent(1);
//	
//			over.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 10);
//			over.stroke();
//			over.addImage(qr);
//			stamper.close();
//			reader.close();
//			
//	
//	
//			dc.setSigndoc(rename);
//			dc.setRename(rename);
//			new DocumentsDao(db).update(dc);
//			res=true;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		return res;
//	}
//	
//	private void generateQRCodeImage(String text, String filePath) {
//		try {
//			QRCodeWriter qrCodeWriter = new QRCodeWriter();
//			BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 75, 75);
//
//			Path path = FileSystems.getDefault().getPath(filePath);
//			MatrixToImageWriter.writeToPath(bitMatrix, "JPG", path);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/*
//	public boolean uploadFILEMitraAT(HttpServletRequest request, DB db, User userTrx, byte[] fileSave, String idMitra, String fileName, JSONObject message) throws Exception {
//		String uploadTo = "/opt/data-DS/UploadFile/" + userTrx.getId() + "/original/";
//		String directoryName = "/opt/data-DS/UploadFile/" + userTrx.getId() + "/original/";
//
//		File directory = new File(directoryName);
//		if (!directory.exists()) {
//			directory.mkdirs();
//		}
//		dc = new Documents();
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
//		Date date = new Date();
//		String strDate = sdfDate.format(date);
//		String rename = "DS" + strDate + idMitra + ".pdf";
//		dc.setEeuser(userTrx);
//		dc.setWaktu_buat(date);
//
//		boolean resp = false;
//		SaveFileWithSamba samba = new SaveFileWithSamba();
//		resp = samba.write(fileSave, uploadTo + rename);
//		LogSystem.info(request, "hasil save File : " + resp, kelas, refTrx, trxType);
//		if (resp == false) {
//			LogSystem.error(request, "error samba", kelas, refTrx, trxType);
//		}
//
//		try {
//			com.itextpdf.text.pdf.PdfReader pdfread = new com.itextpdf.text.pdf.PdfReader(samba.openfile(uploadTo + rename));
//
//			String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage(pdfread, 1);
//			jmlPage = pdfread.getNumberOfPages();
//			LogSystem.info(request, "jumlah page = " + jmlPage);
//			LogSystem.info(request, "size text = " + textFromPdfFilePageOne.length());
//		} catch (Exception e) {
//			// handle exception
//			// fileTo.delete();
////			samba.deletefile(uploadTo + rename);
//			e.printStackTrace();
//			LogSystem.error(request, "bukan file pdf", kelas, refTrx, trxType);
//			return false;
//
//		}
//
//		dc.setFile(uploadTo);
//		dc.setFile_name(fileName);
//		dc.setPath(uploadTo);
//		dc.setSigndoc(rename);
//		dc.setRename(rename);
//		dc.setStatus('T');
//		dc.setPayment('1');
//		if (idMitra != null) {
//			dc.setIdMitra(idMitra);
//			dc.setPayment('3');
//		}
//		if(message.has("payment"))
//		{
//			dc.setPayment(message.getString("payment").charAt(0));
//		}
//		boolean redirect = false;
//		if (message.has("redirect"))
//			redirect = message.getBoolean("redirect");
//		dc.setRedirect(redirect);
//		if (message.has("branch"))
//			dc.setBranch(message.getString("branch"));
//		if (message.has("dealer_code"))
//			dc.setDealer_code(message.getString("dealer_code"));
//		if (message.has("sequence_option"))
//			dc.setSequence(message.getBoolean("sequence_option"));
//		else
//			dc.setSequence(false);
//
//		Long iddoc = new DocumentsDao(db).create2(dc);
//		dc.setId(iddoc);
//
//		return true;
//	}
//	*/
//}
