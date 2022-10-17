package id.sni.digisign.filetransfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.DiskShare;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class FTP  implements FileTransfer, DSAPI{
	String refTrx = "";
	HttpServletRequest  request;
	FTPSClient ftpClient ;
	Exception ex;
	String date=String.valueOf(new Date().getTime());
	Logger log=LogManager.getLogger(FTP.class);
	String mitra_req = "";
	String email_req = "";
	String path_app =this.getClass().getName();
	String CATEGORY = "";
	long start;
	
	public FTP(String refTrx,HttpServletRequest  request, String mitra_req, String email_req, String category, long start)
	{
		this.refTrx = refTrx;
		this.request = request;
		this.mitra_req = mitra_req;
		this.email_req = email_req;
		this.CATEGORY = category;
		this.start = start;
	}
	
	@Override
	public void openConnection(boolean newServer) throws IOException {
		// TODO Auto-generated method stub
		String nas_user=null;
        String nas_pass=null;
        String nas_ip=null;
        if(!newServer) {
        	nas_ip=FILESYS_SERVER_ADDRESS;
        	nas_user=FILESYS_USERNAME;
        	nas_pass=FILESYS_PASSWORD;
        }else {
        	nas_ip=FILESYS_SERVER_ADDRESS_NAS;
        	nas_user=FILESYS_USERNAME_NAS;
        	nas_pass=FILESYS_PASSWORD_NAS;
        	
        }
        int trCount=0;
        try {
			LogSystem.info(request, "[FTP] open connection FTP", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        while (trCount<3 && ftpClient==null) {
        	trCount++;
	        try {
	    		ftpClient= new FTPSClient("TLS", false);
	            ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
	            ftpClient.connect(nas_ip, 21);
	            boolean login=ftpClient.login(nas_user, nas_pass);
	        	int reply = ftpClient.getReplyCode();
	            showServerReply(ftpClient);
	            if (!FTPReply.isPositiveCompletion(reply)|| !login)
	            {
	            	ftpClient.disconnect();
		            LogSystem.info(request, "[FTP] server refused connection", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
		            ftpClient=null;
		            continue;
	            }
	            ftpClient.enterLocalPassiveMode();
	            ftpClient.execPBSZ(0);
	            ftpClient.execPROT("C");
	            ftpClient.setKeepAlive(true);
	            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
	            ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.COMPRESSED_TRANSFER_MODE); 
	            LogSystem.info(request, "[FTP] server success connection", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
//	            System.out.println("connection established");
	        } catch (IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					LogSystem.info(request, "[FTP] "+ e.toString(),  refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ftpClient=null;
				ex=e;
	
			}
	        trCount++;
        }
        if(ftpClient!=null && !ftpClient.isConnected()) {
        	ftpClient=null;
        	throw new IOException("FTP connection problem");
        }
	}

	@Override
	public Exception getEx() {
		// TODO Auto-generated method stub
		return ex;
	}

	@Override
	public void close() {
		if(ftpClient!=null) {
			if(ftpClient.isConnected()) {
				try {
		            LogSystem.info(request, "[FTP] disconnecting...", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

					ftpClient.disconnect();
		            LogSystem.info(request, "[FTP] disconnected", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				} catch (IOException | JSONException e) {
					e.printStackTrace();
				}
			}
			ftpClient=null;
		}
	}

	@Override
	public byte[] openfile(String pathFile) throws Exception {
		 byte[] data=null;
         InputStream iStream=null;
         ByteArrayOutputStream outStream=null;
         // Connect to Share
	  	 String [] path=pathFile.split("/");
	  	 boolean statServer=false;
        if(path[1].equals("file2")) {
        	statServer=true;
        }
	     try{
	           openConnection(statServer);  
	        	
	           LogSystem.info(request, "[FTP] opening directory "+pathFile, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	            
	            outStream=new ByteArrayOutputStream();
	            boolean downloaded = ftpClient.retrieveFile(pathFile, outStream);
	           	 
	            if (downloaded) {
	            	LogSystem.info(request, "[FTP] download file "+pathFile+" completed", refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	            }else {
		            data=null;
		            throw new Exception(ftpClient.getReplyString()+"| document couldn't downloaded");
	            }
//	            System.out.println("dwl :"+downloaded);
	            data=outStream.toByteArray();
	            
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        ex=e;
	    	if(iStream!=null)iStream.close();
	    	if(outStream!=null)outStream.close();
	        throw ex;

	    } catch (Exception e) {
	        e.printStackTrace();
	        ex=e;
	    	if(iStream!=null)iStream.close();
	    	if(outStream!=null)outStream.close();
	        throw ex;
	    }finally {
	    	if(iStream!=null)iStream.close();
	    	if(outStream!=null)outStream.close();
	    }		
	      
	     return data;
	}

	@Override
	public boolean write(byte[] data, String pathFile) throws Exception {
		  boolean res=false;
          InputStream iStream=null;
	        
          // Connect to Share
    	  String [] path=pathFile.split("/");
    	  String pathString="";
    	  String filename="";
    	  boolean statServer=false;
          if(path[1].equals("file2")) {
         	statServer=true;
          }
	      try{
		        
	    	    openConnection(statServer);  
		
		        int i=0;
	        	while(i<path.length) {
	        		if(i!=path.length-1) {
	        			pathString+=path[i]+"/";
	    	            boolean chgDir=ftpClient.changeWorkingDirectory(path[i]+"/");
	    	            if(!chgDir) {
	    	            	LogSystem.info(request, "[FTP] creating directory "+path[i], refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
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
	        	LogSystem.info(request, "[FTP] opening directory "+pathFile, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");

	            FTPFile f = ftpClient.mdtmFile(filename);
                if (f != null) {
                	LogSystem.info(request, "[FTP]  duplicated file "+filename, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
			        throw new Exception(ftpClient.getReplyString()+"file cannot be created");
                }
                LogSystem.info(request, "[FTP] transferring file "+pathFile, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
	            iStream=new ByteInputStream(data, data.length);
	            res=ftpClient.storeFile(filename, iStream);

	            if(!res) {
			           throw new Exception(ftpClient.getReplyString()+"file cannot be created");
	            }		            
	            LogSystem.info(request, "[FTP] file has been uploaded "+pathFile, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");


	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        ex=e;
	    	if(iStream!=null)iStream.close();
	        throw ex;

	    } catch (Exception e) {
	        e.printStackTrace();
	        ex=e;
	    	if(iStream!=null)iStream.close();
	        throw ex;
	    }finally {
	    	if(iStream!=null)iStream.close();
	    }
	      
	    return res;
	}

	private void printList() throws IOException {
        FTPFile[] files = ftpClient.mlistDir();
		 DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        for (FTPFile file : files) {
	            String details = file.getName();
	            if (file.isDirectory()) {
	                details = "[" + details + "]";
	            }
	            details += "\t\t" + file.getSize();
	            details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());
	 
	            try {
					LogSystem.info(request, "[FTP] " + details, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		 
	}
	
	

	private void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
            	try {
					LogSystem.info(request, "[FTP] SERVER: "+aReply, refTrx, path_app,  Thread.currentThread().getStackTrace()[1].getLineNumber(), email_req, mitra_req, CATEGORY, (System.currentTimeMillis() - start) / 1000f + "s");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }

	
	@Override
	public void setTimetamp(String date) {
		this.date=date;
	}

}
