package id.co.keriss.consolidate.util;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.jpos.ee.DB;
import org.jpos.ee.User;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.ee.Documents;

public class FileProcessor implements DSAPI{
	
	  Documents dc =null;


	
	public Documents getDc() {
		return dc;
	}



	public void setDc(Documents dc) {
		this.dc = dc;
	}

	public static java.awt.Image makeColorTransparent(BufferedImage im, final Color color, float threshold) {
	    ImageFilter filter = new RGBImageFilter() {
	        public float markerAlpha = color.getRGB() | 0xFF000000;
	        @Override
			public final int filterRGB(int x, int y, int rgb) {
	            int currentAlpha = rgb | 0xFF000000;           // just to make it clear, stored the value in new variable
	            float diff = Math.abs((currentAlpha - markerAlpha) / markerAlpha);  // Now get the difference
	            if (diff <= threshold) {                      // Then compare that threshold value
	                return 0x00FFFFFF & rgb;
	            } else {
	                return rgb;
	            }
	        }
	    };
	    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
	    return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	public static BufferedImage ImageToBufferedImage(java.awt.Image image, int width, int height)
	  {
	    BufferedImage dest = new BufferedImage(
	        width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
	    Graphics2D g2 = dest.createGraphics();
	    g2.drawImage(image, 0, 0, null);
	    g2.dispose();
	    return dest;
	  }


	public boolean uploadFile(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave) throws Exception {
		return uploadFile(request, db, userTrx, fileSave, null);
	}
	
	public boolean uploadFile(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra) throws Exception {
		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  if(uploadTo.indexOf("/ds-api")>=0) {
//			  uploadTo=uploadTo.substring(7);
//		  }
//		  if(directoryName.indexOf("/ds-api")>=0) {
//			  directoryName=directoryName.substring(7);
//		  }
		  File directory = new File(directoryName);
		  if (!directory.exists()){
		       directory.mkdirs();
		  }
		  dc = new Documents();
		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		  Date date = new Date();
		  String strDate = sdfDate.format(date);
		  String rename = "DS"+strDate+".pdf";
		  dc.setEeuser(userTrx);
          dc.setWaktu_buat(date);	
          File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
//          File fileTo = new File((uploadTo +rename));
          fileSave.write(fileTo);
//          Blob blob = Hibernate.createBlob(fileSave.getInputStream());
          dc.setFile(uploadTo);
          dc.setFile_name(fileSave.getName());
          dc.setPath(uploadTo);
          dc.setSigndoc(rename);
          dc.setRename(rename);
          dc.setStatus('F');
          dc.setPayment('1');
          //if(userTrx.getMitra()!=null)dc.setIdMitra(userTrx.getMitra().getId().toString());
          new DocumentsDao(db).create(dc);
		return true;
	}
	
	public boolean uploadFileMitra(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra) throws Exception {
		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  if(uploadTo.indexOf("/ds-api")>=0) {
//			  uploadTo=uploadTo.substring(7);
//		  }
//		  if(directoryName.indexOf("/ds-api")>=0) {
//			  directoryName=directoryName.substring(7);
//		  }
		  File directory = new File(directoryName);
		  if (!directory.exists()){
		       directory.mkdirs();
		  }
		  dc = new Documents();
		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		  Date date = new Date();
		  String strDate = sdfDate.format(date);
		  String rename = "DS"+strDate+".pdf";
		  dc.setEeuser(userTrx);
        dc.setWaktu_buat(date);	
        File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
//        File fileTo = new File((uploadTo +rename));        
        fileSave.write(fileTo);
        //FileWriter fw=new FileWriter(fileTo);
        //fileSave.getOutputStream().close();
//        Blob blob = Hibernate.createBlob(fileSave.getInputStream());
        dc.setFile(uploadTo);
        dc.setFile_name(fileSave.getName());
        dc.setPath(uploadTo);
        dc.setSigndoc(rename);
        dc.setRename(rename);
        dc.setStatus('F');
        dc.setPayment('1');
        if(idMitra!=null) {
        	dc.setIdMitra(idMitra);
        	dc.setPayment('3');
        }
        Long iddoc=new DocumentsDao(db).create2(dc);
        dc.setId(iddoc);
		return true;
	}
	
	
	public boolean uploadFILEnQRMitraArray(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra, String dateRename) throws Exception {
		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  if(uploadTo.indexOf("/ds-api")>=0) {
//			  uploadTo=uploadTo.substring(7);
//		  }
//		  if(directoryName.indexOf("/ds-api")>=0) {
//			  directoryName=directoryName.substring(7);
//		  }
		  File directory = new File(directoryName);
		  if (!directory.exists()){
		       directory.mkdirs();
		  }
		  dc = new Documents();
		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		  Date date = new Date();
		  String strDate = sdfDate.format(date);
		  String rename = "DS"+strDate+".pdf";
		  dc.setEeuser(userTrx);
    dc.setWaktu_buat(date);	
    File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
//    File fileTo = new File((uploadTo +rename));
    byte[] data=fileSave.get();
    
    fileSave.write(fileTo);
    //FileWriter fw=new FileWriter(fileTo);
    fileSave.getOutputStream().close();
//    Blob blob = Hibernate.createBlob(fileSave.getInputStream());
    dc.setFile(uploadTo);
    dc.setFile_name(fileSave.getName());
    dc.setPath(uploadTo);
    dc.setSigndoc(dateRename);
    dc.setRename(dateRename);
    dc.setStatus('F');
    dc.setPayment('1');
    if(idMitra!=null) {
    	dc.setIdMitra(idMitra);
    	dc.setPayment('3');
    }
    Long iddoc=new DocumentsDao(db).create2(dc);
    dc.setId(iddoc);
    
    //generateQRCode2(db, data, uploadTo, String.valueOf(iddoc));
		return true;
	}
	
	public boolean uploadFILEnQRMitra(HttpServletRequest  request,DB db, User userTrx, FileItem fileSave, String idMitra) throws Exception {
		  String uploadTo =  "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
		  String directoryName = "/opt/data-DS/UploadFile/"+userTrx.getId()+"/original/";
//		  if(uploadTo.indexOf("/ds-api")>=0) {
//			  uploadTo=uploadTo.substring(7);
//		  }
//		  if(directoryName.indexOf("/ds-api")>=0) {
//			  directoryName=directoryName.substring(7);
//		  }
		  File directory = new File(directoryName);
		  if (!directory.exists()){
		       directory.mkdirs();
		  }
		  dc = new Documents();
		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
		  Date date = new Date();
		  String strDate = sdfDate.format(date);
		  String rename = "DS"+strDate+".pdf";
		  dc.setEeuser(userTrx);
      dc.setWaktu_buat(date);	
      File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
//      File fileTo = new File((uploadTo +rename));
      byte[] data=fileSave.get();
      
      fileSave.write(fileTo);
      //FileWriter fw=new FileWriter(fileTo);
      fileSave.getOutputStream().close();
//      Blob blob = Hibernate.createBlob(fileSave.getInputStream());
      dc.setFile(uploadTo);
      dc.setFile_name(fileSave.getName());
      dc.setPath(uploadTo);
      dc.setSigndoc(rename);
      dc.setRename(rename);
      dc.setStatus('F');
      dc.setPayment('1');
      if(idMitra!=null) {
      	dc.setIdMitra(idMitra);
      	dc.setPayment('3');
      }
      Long iddoc=new DocumentsDao(db).create2(dc);
      dc.setId(iddoc);
      
      //generateQRCode2(db, data, uploadTo, String.valueOf(iddoc));
		return true;
	}
	
	public String uploadBuktiTransfer(HttpServletRequest  request, User userTrx, FileItem fileSave) throws Exception {
		  String uploadTo =  "/opt/data-DS/BuktiTransfer/"+userTrx.getId()+"/";
		  String directoryName = "/opt/data-DS/BuktiTransfer/"+userTrx.getId()+"/";
//		  if(uploadTo.indexOf("/ds-api")>=0) {
//			  uploadTo=uploadTo.substring(7);
//		  }
//		  if(directoryName.indexOf("/ds-api")>=0) {
//			  directoryName=directoryName.substring(7);
//		  }
		  File directory = new File(directoryName);
		  if (!directory.exists()){
		       directory.mkdirs();
		  }
		  SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		  Date date = new Date();
		  String strDate = sdfDate.format(date);
		  String rename = "TRF"+strDate+".jpg";
		  
        File fileTo = new File(uploadTo +rename);//.replaceAll("../", "../../DigitalSignature"));
//        File fileTo = new File((uploadTo +rename));
        fileSave.write(fileTo);
//    
		return uploadTo +rename;
	}
	
	public boolean generateQRCode(DB db) {
		boolean res=false;
		if(dc==null)return res;
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String rename = "DS" + strDate + ".pdf";
		String docs;
		try {
			docs = AESEncryption.encryptDoc(String.valueOf(dc.getId()));
		
			String link = "https://"+DOMAIN+"/dsmobile/doc/verpdf.html?doc="
					+ URLEncoder.encode(docs, "UTF-8");
			generateQRCodeImage(link, dc.getPath() + "qr.jpg");
	
			PdfReader reader = new PdfReader(dc.getPath()+dc.getSigndoc());
			
			Rectangle mediabox = reader.getPageSize(1);
			int h = (int) mediabox.getWidth();
			int w = (int) mediabox.getHeight();
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dc.getPath() + rename));
	
			Image qr = Image.getInstance(dc.getPath() + "qr.jpg");
			qr.setTransparency(new int[] { 0XF0, 0XFF });
			qr.setAbsolutePosition(h - 100, w - 100);
			PdfContentByte over = stamper.getOverContent(1);
	
			over.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 10);
			over.stroke();
			over.addImage(qr);
			stamper.close();
			reader.close();
			
	
	
			dc.setSigndoc(rename);
			dc.setRename(rename);
			new DocumentsDao(db).update(dc);
			res=true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return res;
	}
	
	public boolean generateQRCode2(DB db, byte[] data, String home, String iddoc) {
		boolean res=false;
		if(dc==null)return res;
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String strDate = sdfDate.format(date);
		String rename = "DS" + strDate + ".pdf";
		String docs;
		try {
			docs = AESEncryption.encryptDoc(iddoc);
		
			String link = "https://"+DOMAIN+"/dsmobile/doc/verpdf.html?doc="
					+ URLEncoder.encode(docs, "UTF-8");
			generateQRCodeImage(link, dc.getPath() + "qr.jpg");
	
			//PdfReader reader = new PdfReader(dc.getPath()+dc.getSigndoc());
			PdfReader reader = new PdfReader(data);
			
			Rectangle mediabox = reader.getPageSize(1);
			int h = (int) mediabox.getWidth();
			int w = (int) mediabox.getHeight();
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(home + rename));
	
			Image qr = Image.getInstance(dc.getPath() + "qr.jpg");
			qr.setTransparency(new int[] { 0XF0, 0XFF });
			qr.setAbsolutePosition(h - 100, w - 100);
			PdfContentByte over = stamper.getOverContent(1);
	
			over.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 10);
			over.stroke();
			over.addImage(qr);
			stamper.close();
			reader.close();
			
	
	
			dc.setSigndoc(rename);
			dc.setRename(rename);
			new DocumentsDao(db).update(dc);
			res=true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return res;
	}
	
	private void generateQRCodeImage(String text, String filePath) {
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 75, 75);

			Path path = FileSystems.getDefault().getPath(filePath);
			MatrixToImageWriter.writeToPath(bitMatrix, "JPG", path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
