package id.co.keriss.consolidate.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

import java.io.ByteArrayInputStream;
import java.util.Date;


public class SaveFileWithFTP {
	
	Exception ex;
	String date = String.valueOf(new Date().getTime());
	FTPSClient ftpClient;
	String refTrx = "";
	String kelas = "";
	String trxType = "";
	HttpServletRequest request;
	
	public SaveFileWithFTP(HttpServletRequest request, String refTrx, String kelas, String trxType) {
		this.request = request;
		this.refTrx = refTrx;
		this.kelas = kelas;
		this.trxType = trxType;
	}

	void openConnection() throws IOException {
		// TODO Auto-generated method stub
		String nas_user = DSAPI.FILESYS_USERNAME_NAS;
		String nas_pass = DSAPI.FILESYS_PASSWORD_NAS;
		String nas_ip = DSAPI.FILESYS_SERVER_ADDRESS_NAS;

		int trCount = 0;
		LogSystem.info(request, "open connection FTP", kelas, refTrx, trxType);

		while (trCount < 3 && ftpClient == null) {
			trCount++;
			try {
				ftpClient = new FTPSClient("TLS", false);
				ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
				ftpClient.connect(nas_ip, 21);
				boolean login = ftpClient.login(nas_user, nas_pass);
				int reply = ftpClient.getReplyCode();
				showServerReply(ftpClient);
				if (!FTPReply.isPositiveCompletion(reply) || !login) {
					ftpClient.disconnect();
					LogSystem.info(request, "FTP server refused connection", kelas, refTrx, trxType);
					ftpClient = null;
					continue;
				}
				ftpClient.enterLocalPassiveMode();
				ftpClient.execPBSZ(0);
				ftpClient.execPROT("C");
				ftpClient.setKeepAlive(true);
				ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
				ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.COMPRESSED_TRANSFER_MODE);
				LogSystem.info(request, "FTP connection successful", kelas, refTrx, trxType);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LogSystem.info(request, e.toString(), kelas, refTrx, trxType);
				e.printStackTrace();
				ftpClient = null;
				ex = e;

			}
			trCount++;
		}
		if (ftpClient != null && !ftpClient.isConnected()) {
			ftpClient = null;
			throw new IOException("FTP connection problem");
		}
	}

	private void showServerReply(FTPClient ftpClient) {
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				LogSystem.info(request, "SERVER: " + aReply, kelas, refTrx, trxType);
			}
		}
	}


	public Exception getEx() {
		// TODO Auto-generated method stub
		return null;
	}


	public void close() {
		// TODO Auto-generated method stub
		if (ftpClient != null) {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
					LogSystem.info(request, "disconnected", kelas, refTrx, trxType);
				} catch (IOException e) {
					LogSystem.info(request, e.toString(), kelas, refTrx, trxType);
					e.printStackTrace();
				}
			}
			ftpClient = null;
		}
	}


	public void setTimetamp(String date) {
		this.date=date;
	}


	public byte[] openfile(String pathFile) throws Exception {
		// TODO Auto-generated method stub
		byte[] data = null;

		ByteArrayOutputStream outStream = null;
		// Connect to Share
		try {
			openConnection();
			outStream = new ByteArrayOutputStream();
			LogSystem.info(request, "download file " + pathFile + " trying..", kelas, refTrx, trxType);
			boolean downloaded = ftpClient.retrieveFile(pathFile, outStream);
			if (downloaded) {
				LogSystem.info(request, "download file " + pathFile + " completed", kelas, refTrx, trxType);
			} else {
				data = null;
				throw new Exception(ftpClient.getReplyString() + "| document couldn't downloaded");
			}
			data = outStream.toByteArray();

		} catch (Exception e) {
			LogSystem.info(request, e.toString(), kelas, refTrx, trxType);
			e.printStackTrace();
			ex = e;
			if (outStream != null)
				outStream.close();
			close();
			throw ex;
		} finally {
			if (outStream != null)
				outStream.close();
			close();
		}

		return data;
	}
	

	public byte[] openfileOne(String pathFile) throws Exception {
		// TODO Auto-generated method stub
		byte[] data = null;

		ByteArrayOutputStream outStream = null;
		// Connect to Share
		try {
			outStream = new ByteArrayOutputStream();
			boolean downloaded = ftpClient.retrieveFile(pathFile, outStream);

			if (downloaded) {
				LogSystem.info(request, "download file " + pathFile + " completed", kelas, refTrx, trxType);
			} else {
				data = null;
				throw new Exception(ftpClient.getReplyString() + "| document couldn't downloaded");
			}
			data = outStream.toByteArray();

		} catch (Exception e) {
			LogSystem.info(request, e.toString(), kelas, refTrx, trxType);
			e.printStackTrace();
			ex = e;
			if (outStream != null)
				outStream.close();
			close();
			throw ex;
		} finally {
			if (outStream != null)
				outStream.close();
		}

		return data;
	}

	public boolean write(byte[] data, String pathFile) throws Exception {
		// TODO Auto-generated method stub
		  boolean res=false;
          InputStream iStream=null;
	        
          // Connect to Share
    	  String [] path=pathFile.split("/");
    	  String filename="";
    	 
	      try{
		        
	    	    openConnection();  
		
		        int i=0;
	        	while(i<path.length) {
	        		if(i!=path.length-1) {
	    	            boolean chgDir=ftpClient.changeWorkingDirectory(path[i]+"/");
	    	            if(!chgDir) {
	    		            LogSystem.info(request, "creating directory "+path[i], kelas, refTrx, trxType);
	    	            	ftpClient.makeDirectory(path[i]);
	    	            	chgDir=ftpClient.changeWorkingDirectory(path[i]);
	    	            	if(!chgDir) {
	    				           throw new Exception("Folder cannot be created");
	    		            }
	    	            }
	        		}
	        		else {
	        			filename=path[i];
	        		}	        		
		        	i++;
	        	}
	        	
	            LogSystem.info(request, "opening directory "+pathFile, kelas, refTrx, trxType);
	            	
	            int f = ftpClient.mdtm(filename);
                if (f != 0) {
    	            LogSystem.info(request, "duplicated file "+filename, kelas, refTrx, trxType);
			        throw new Exception(ftpClient.getReplyString()+"file cannot be created");
                } 
	            LogSystem.info(request, "transferring file "+pathFile, kelas, refTrx, trxType);

	            iStream=new ByteArrayInputStream(data);
	            res=ftpClient.storeFile(filename, iStream);

	            if(!res) {
			           throw new Exception(ftpClient.getReplyString()+"file cannot be created");
	            }		            
	            LogSystem.info(request, "file has been uploaded "+pathFile, kelas, refTrx, trxType);


	    } catch (Exception e) {
	        LogSystem.error(request, e.toString(), kelas, refTrx, trxType);
	        ex=e;
	    	if(iStream!=null)iStream.close();
	    	close();
	        throw ex;
	    }finally {
	    	if(iStream!=null)iStream.close();
	    	close();
	    }
	      
	    return res;
	}
	
	
}
