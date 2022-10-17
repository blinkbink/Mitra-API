//package id.sni.digitalsignature.lib.util;
package id.co.keriss.consolidate.DS;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2FileId;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import id.co.keriss.consolidate.util.DSAPI;

public class Samba implements DSAPI {
	

	
//	public byte[] openfile(String pathFile) {
//		
//		 byte[] data=null;
//		 SMBClient client = new SMBClient();
//		    try (Connection connection = client.connect(FILESYS_SERVER_ADDRESS)) {
//		        AuthenticationContext ac = new AuthenticationContext(FILESYS_USERNAME, FILESYS_PASSWORD.toCharArray(), FILESYS_DOMAIN);
//		        Session session = connection.authenticate(ac);
//		       
//		        
//		        // Connect to Share
//	        	String [] path=pathFile.split("/");
//	        	String pathString="";
//	        	int i=2;
//		        while(i<path.length) {
//	        		if(!pathString.equals("")) pathString+="\\";
//	        		pathString+=path[i];
//		        	i++;
//	        	}
//		        // Connect to Share
//		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
//		        	
//		            Set<SMB2ShareAccess> s = new HashSet<>();
//		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
//		            com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_OPEN, null);
//		            try (InputStream is = remoteSmbjFile.getInputStream();
//		            	ByteArrayOutputStream os = new ByteArrayOutputStream();) {
//		                byte[] buffer = new byte[1024];
//		                int length;
//		                while ((length = is.read(buffer)) > 0) {
//		                	os.write(buffer, 0, length);
//		                    
//		                }
//		                data=os.toByteArray();
//		                os.close();
//		                share.close();
//		            }
//		        }
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }finally {
//		    	client.close();
//			}
//		    return data;
//}
//	
//	
//	public boolean write(byte [] data, String pathFile) {
//	     boolean res=false;
//		 SMBClient client = new SMBClient();
//		    try (Connection connection = client.connect(FILESYS_SERVER_ADDRESS)) {
//		        AuthenticationContext ac = new AuthenticationContext(FILESYS_USERNAME, FILESYS_PASSWORD.toCharArray(), FILESYS_DOMAIN);
//		        Session session = connection.authenticate(ac);
//		        
//		        
//		        // Connect to Share
//	        	String [] path=pathFile.split("/");
//	        	String pathString="";
//
//	        	int i=2;
//		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
//		        	while(i<path.length-1) {
//		        		if(!pathString.equals("")) pathString+="\\";
//		        		pathString+=path[i];
//			        	if(!share.folderExists(pathString)){
//			        		share.mkdir(pathString);
//			        	}
//			        	i++;
//		        	}
//		        	pathString+="\\"+path[i];	        	
//		            Set<SMB2ShareAccess> s = new HashSet<>();
//		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
//		            Set<FileAttributes> fileAttributes = new HashSet<>();
//		            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
//		            com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.GENERIC_WRITE), fileAttributes, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_CREATE, null);
//		            OutputStream oStream = remoteSmbjFile.getOutputStream();
//		            oStream.write(data);
//
//		            oStream.flush();
//		            oStream.close();
//		            share.close();
//		            res=true;
//
//		        }
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }finally {
//			    client.close();
//
//			}
//		    return res;
//}
////////////////////////////////////////////////////////////////////////////////////////////////////NAS//////////////////////////////////////////////////////	
//	
//	public byte[] openfile_NAS(String pathFile) {
//		
//		 byte[] data=null;
//		 SMBClient client = new SMBClient();
//		    try (Connection connection = client.connect(FILESYS_SERVER_ADDRESS_NAS)) {
//		        AuthenticationContext ac = new AuthenticationContext(FILESYS_USERNAME_NAS, FILESYS_PASSWORD_NAS.toCharArray(), FILESYS_DOMAIN_NAS);
//		        Session session = connection.authenticate(ac);
//		       
//		        
//		        // Connect to Share
//	        	String [] path=pathFile.split("/");
//	        	String pathString="";
//	        	int i=2;
//		        while(i<path.length) {
//	        		if(!pathString.equals("")) pathString+="\\";
//	        		pathString+=path[i];
//		        	i++;
//	        	}
//		        // Connect to Share
//		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
//		        	
//		            Set<SMB2ShareAccess> s = new HashSet<>();
//		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
//		            com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_OPEN, null);
//		            try (InputStream is = remoteSmbjFile.getInputStream();
//		            	ByteArrayOutputStream os = new ByteArrayOutputStream();) {
//		                byte[] buffer = new byte[1024];
//		                int length;
//		                while ((length = is.read(buffer)) > 0) {
//		                	os.write(buffer, 0, length);
//		                    
//		                }
//		                data=os.toByteArray();
//		                os.close();
//		                share.close();
//		            }
//		        }
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }finally {
//		    	client.close();
//			}
//		    return data;
//}
//	public boolean write_NAS(byte [] data, String pathFile) {
//	     boolean res=false;
//		 SMBClient client = new SMBClient();
//		    try (Connection connection = client.connect(FILESYS_SERVER_ADDRESS_NAS)) {
//		        AuthenticationContext ac = new AuthenticationContext(FILESYS_USERNAME_NAS, FILESYS_PASSWORD_NAS.toCharArray(), FILESYS_DOMAIN_NAS);
//		        Session session = connection.authenticate(ac);
//		        
//		        
//		        // Connect to Share
//	        	String [] path=pathFile.split("/");
//	        	String pathString="";
//
//	        	int i=2;
//		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
//		        	while(i<path.length-1) {
//		        		if(!pathString.equals("")) pathString+="\\";
//		        		pathString+=path[i];
//			        	if(!share.folderExists(pathString)){
//			        		share.mkdir(pathString);
//			        	}
//			        	i++;
//		        	}
//		        	pathString+="\\"+path[i];	        	
//		            Set<SMB2ShareAccess> s = new HashSet<>();
//		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
//		            Set<FileAttributes> fileAttributes = new HashSet<>();
//		            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
//		            com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.GENERIC_WRITE), fileAttributes, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_CREATE, null);
//		            OutputStream oStream = remoteSmbjFile.getOutputStream();
//		            oStream.write(data);
//
//		            oStream.flush();
//		            oStream.close();
//		            share.close();
//		            res=true;
//
//		        }
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }finally {
//			    client.close();
//
//			}
//		    return res;
//}
	
	
}
