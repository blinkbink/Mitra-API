package id.co.keriss.consolidate.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy.SelfInjection.Split;

public class SaveFileWithSamba implements DSAPI{
	
	public byte[] openfile(String pathFile) {
		
		 byte[] data=null;
		 SMBClient client = new SMBClient();
		 String[] split = pathFile.split("/");
		 String server = FILESYS_SERVER_ADDRESS_NAS;
		 String username = FILESYS_USERNAME_NAS;
		 String pwd = FILESYS_PASSWORD_NAS;
		 String domain=FILESYS_DOMAIN_NAS;
		 
		 if(split[1].equalsIgnoreCase("file2")) {
			 server = FILESYS_SERVER_ADDRESS_NAS2;
//			 username = FILESYS_USERNAME_NAS;
//			 pwd = FILESYS_PASSWORD_NAS;
//			 domain=FILESYS_DOMAIN_NAS;
		 }
		    try (Connection connection = client.connect(server)) {
		        AuthenticationContext ac = new AuthenticationContext(username, pwd.toCharArray(), domain);
		        Session session = connection.authenticate(ac);
		       
		        
		        // Connect to Share
//		        System.out.println("pathnya = "+pathFile);
	        	//String [] path=pathFile.split("/");
		        String [] path=split;
		        String pathString="";
	        	int i=2;
		        while(i<path.length) {
	        		if(!pathString.equals("")) pathString+="\\";
	        		pathString+=path[i];
		        	i++;
	        	}
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
		    } catch (Exception e) {
		        e.printStackTrace();
		    }finally {
		    	client.close();
			}
		    return data;
}
	
	
	public boolean write(byte [] data, String pathFile) {
	     boolean res=false;
		 SMBClient client = new SMBClient();
		    try (Connection connection = client.connect(FILESYS_SERVER_ADDRESS_NAS2)) {
		        AuthenticationContext ac = new AuthenticationContext(FILESYS_USERNAME_NAS, FILESYS_PASSWORD_NAS.toCharArray(), FILESYS_DOMAIN_NAS);
		        Session session = connection.authenticate(ac);
		        
		        
		        // Connect to Share
//		        System.out.println("MASUKKKKKKKKKKKKKKKKKKKKKKK");
//		        System.out.println("pathnya = "+pathFile);
	        	String [] path=pathFile.split("/");
	        	String pathString="";

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

		        } catch (Exception e) {
					// TODO: handle exception
		        	e.printStackTrace();
		        	res=false;
				}
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }finally {
			    client.close();

			}
		    return res;
}
	
	public boolean deletefile(String pathFile) {
		
		boolean data=false;
		 SMBClient client = new SMBClient();
		 String[] split = pathFile.split("/");
		 String server = FILESYS_SERVER_ADDRESS_NAS;
		 String username = FILESYS_USERNAME_NAS;
		 String pwd = FILESYS_PASSWORD_NAS;
		 String domain=FILESYS_DOMAIN_NAS;
		 
		 if(split[1].equalsIgnoreCase("file2")) {
			 server = FILESYS_SERVER_ADDRESS_NAS2;
//			 username = FILESYS_USERNAME_NAS;
//			 pwd = FILESYS_PASSWORD_NAS;
//			 domain=FILESYS_DOMAIN_NAS;
		 }
		 
		    try (Connection connection = client.connect(server)) {
		        AuthenticationContext ac = new AuthenticationContext(username, pwd.toCharArray(), domain);
		        Session session = connection.authenticate(ac);
		       
		        
		        // Connect to Share
//		        System.out.println("pathnya = "+pathFile);
	        	//String [] path=pathFile.split("/");
		        String [] path=split;
		        String pathString="";
	        	int i=2;
		        while(i<path.length) {
	        		if(!pathString.equals("")) pathString+="\\";
	        		pathString+=path[i];
		        	i++;
	        	}
		        // Connect to Share
		        try (DiskShare share = (DiskShare) session.connectShare(path[1])) {
		        	
		            Set<SMB2ShareAccess> s = new HashSet<>();
		            s.add(SMB2ShareAccess.ALL.iterator().next()); // this is to get READ only
		            //com.hierynomus.smbj.share.File remoteSmbjFile = share.openFile(pathString, EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL,SMB2CreateDisposition.FILE_OPEN, null);
		            share.rm(pathString);
		            share.close();
		            data=true;
		            /*
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
		            */
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }finally {
		    	client.close();
			}
		    return data;
}
}
