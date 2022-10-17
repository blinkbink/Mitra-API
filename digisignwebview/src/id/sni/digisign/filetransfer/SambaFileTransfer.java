package id.sni.digisign.filetransfer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2FileId;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.security.jce.JceSecurityProvider;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.sun.crypto.provider.SunJCE;

import id.co.keriss.consolidate.util.DSAPI;
import sun.security.provider.Sun;

public class SambaFileTransfer implements DSAPI, FileTransfer {
	
	private SMBClient client;
	private Exception ex=null;
	Session session;
	Connection connection;
	String ts=String.valueOf(new Date());
	public void openConnection(boolean isNewServer) throws IOException  {
		// TODO Auto-generated constructor stub
	    SmbConfig config = SmbConfig.builder()
	            .withSecurityProvider(new JceSecurityProvider(new SunJCE())) 
	            .withSoTimeout(150, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
	            .withMultiProtocolNegotiate(true)
	            .withTransactTimeout(10, TimeUnit.SECONDS)
	            .build();
	    client=new SMBClient(config);
        AuthenticationContext ac = null;
        String nas_user=null;
        String nas_pass=null;
        String nas_domain=null;
        String nas_ip=null;
        IOException exc=null;
        if(!isNewServer) {
        	nas_ip=FILESYS_SERVER_ADDRESS;
        	nas_user=FILESYS_USERNAME;
        	nas_pass=FILESYS_PASSWORD;
        	nas_domain=FILESYS_DOMAIN;
        }else {
        	nas_ip=FILESYS_SERVER_ADDRESS_NAS;
        	nas_user=FILESYS_USERNAME_NAS;
        	nas_pass=FILESYS_PASSWORD_NAS;
        	nas_domain=FILESYS_DOMAIN_NAS;
        	
        }
        int trCount=0;
        while (trCount<3 && connection==null) {
	        try {
				connection = client.connect(nas_ip);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connection=null;
				exc=e;
	
			}
	        trCount++;
        }
        
        if(connection==null)throw exc;

        ac=new AuthenticationContext(nas_user, nas_pass.toCharArray(), nas_domain);
        session = connection.authenticate(ac);
		  
	}
	
	public Exception getEx() {
		return ex;
	}

	public void close() {
//		if(client!=null)client.close();
//		if(connection!=null) {
//			try {
//				connection.close();
//				connection=null;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		if(session!=null)
			try {
				session.close();
				if(connection.isConnected())connection.close();
				session=null;
				connection=null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public byte[] openfile(String pathFile) throws Exception {
		
		 byte[] data=null;
		    try {
		        // Connect to Share
	        	String [] path=pathFile.split("/");
	        	String pathString="";
	        	int i=2;
		        while(i<path.length) {
	        		if(!pathString.equals("")) pathString+="\\";
	        		pathString+=path[i];
		        	i++;
	        	}
		        boolean statServer=false;
		        if(path[1].equals("file")) {
		        	statServer=true;
		        }
		        openConnection(statServer);        
		        // Connect to Share
		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
		        	
		            Set<SMB2ShareAccess> s = new HashSet<>();
		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
		            com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_OPEN, null);
		            try (InputStream is = remoteSmbjFile.getInputStream();
		            	ByteArrayOutputStream os = new ByteArrayOutputStream();) {
		                byte[] buffer = new byte[1024];
		                int length;
		                while ((length = is.read(buffer)) > 0) {
		                	os.write(buffer, 0, length);
		                    
		                }
		                data=os.toByteArray();
		                os.close();
		                share.close();
		            }
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		        ex=e;
		        throw ex;
		    } catch (Exception e) {
		        e.printStackTrace();
		        ex=e;
		        throw ex;

		    }
		    return data;
}
	
	
	public boolean write(byte [] data, String pathFile) throws Exception {
	     boolean res=false;
		    try  {
		        
		        // Connect to Share
	        	String [] path=pathFile.split("/");
	        	String pathString="";
	            
	        	boolean statServer=false;
		        if(path[1].equals("file")) {
		        	statServer=true;
		        }
		        openConnection(statServer);  
        	
	        	int i=2;
		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
		        	while(i<path.length-1) {
		        		if(!pathString.equals("")) pathString+="\\";
		        		pathString+=path[i];
			        	if(!share.folderExists(pathString)){
			        		share.mkdir(pathString);
			        	}
			        	i++;
		        	}
		        	pathString+="\\"+path[i];	        	
		            Set<SMB2ShareAccess> s = new HashSet<>();
		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
		            Set<FileAttributes> fileAttributes = new HashSet<>();
		            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
		            com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.GENERIC_WRITE), fileAttributes, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_CREATE, null);
		            OutputStream oStream = remoteSmbjFile.getOutputStream();
		            oStream.write(data);

		            oStream.flush();
		            oStream.close();
		            share.close();
		            res=true;

		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		        ex=e;
		        throw ex;

		    } catch (Exception e) {
		        e.printStackTrace();
		        ex=e;
		        throw ex;
		    }
		    return res;
}



	@Override
	public void setTimetamp(String date) {
		// TODO Auto-generated method stub
		ts=date;

		
	}
	
	
}
