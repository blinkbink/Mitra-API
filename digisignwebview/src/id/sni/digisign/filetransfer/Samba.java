package id.sni.digisign.filetransfer;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import id.co.keriss.consolidate.util.DSAPI;

public class Samba implements DSAPI {
	
	FileTransfer fTransfer=null;
	
	public Samba(String refTrx, HttpServletRequest  request, String mitra_req, String email_req, String category, long start) {
		fTransfer=new FTP(refTrx, request, mitra_req, email_req, category, start);
	}
	
	public Exception getEx() {
		return fTransfer.getEx();
	}
	public void close() {
		fTransfer.close();
	}
	public void setTimetamp(String date) {
		fTransfer.setTimetamp(date);
	}
	public byte[] openfile(String pathFile) throws Exception {
		return fTransfer.openfile(pathFile);
	}
	public boolean write(byte[] data, String pathFile) throws Exception {
		return fTransfer.write(data, pathFile);
	}
	
}
