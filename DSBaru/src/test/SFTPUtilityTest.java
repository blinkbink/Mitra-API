//package test;
//
//import java.io.File;
//import java.net.URISyntaxException;
//
//import org.apache.commons.vfs2.FileObject;
//import org.apache.commons.vfs2.FileSystemException;
//import org.apache.commons.vfs2.FileSystemOptions;
//import org.apache.commons.vfs2.Selectors;
//import org.apache.commons.vfs2.impl.StandardFileSystemManager;
//import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
//
//import id.co.keriss.consolidate.util.SFTPUtility;
//
///**
// * The class SFTPUtil containing uploading, downloading, checking if file exists
// * and deleting functionality using Apache Commons VFS (Virtual File System)
// * Library
// *
// * @author Ashok
// *
// */
//public class SFTPUtilityTest {
//	
//	
//
//    public static void main(String[] args) throws URISyntaxException {
//        String hostName = "192.168.78.15:22";
//        String username = "fs_user";
//        String password = "BanBanZip";
//
//        String localFilePath = "C:\\Users\\62856\\Desktop\\";
//        String remoteFilePath = "/file2/data-DS/PreReg/25871/original/wajah1637479243415.png";
////        String remoteFilePath = "file2/development_test/tes.pdf";
//        String remoteTempFilePath = "/file2/data-DS/PreReg/25871/original/wajah1637479243415.png";
//   
////        upload(hostName, username, password, localFilePath, remoteFilePath);
////        exist(hostName, username, password, remoteFilePath);
////        download(hostName, username, password, localFilePath,remoteFilePath);
////        move(hostName, username, password, remoteFilePath, remoteTempFilePath);
////        delete(hostName, username, password, remoteFilePath);
//        SFTPUtility sftp = new SFTPUtility();
//        byte[] myvar = "Any String you want".getBytes();
//        sftp.upload(myvar , remoteFilePath, localFilePath);
//    }
//
//    /**
//     * Method to upload a file in Remote server
//     *
//     * @param hostName
//     *            HostName of the server
//     * @param username
//     *            UserName to login
//     * @param password
//     *            Password to login
//     * @param localFilePath
//     *            LocalFilePath. Should contain the entire local file path -
//     *            Directory and Filename with \\ as separator
//     * @param remoteFilePath
//     *            remoteFilePath. Should contain the entire remote file path -
//     *            Directory and Filename with / as separator
//     */
//    public static void upload(String hostName, String username, String password, String localFilePath, String remoteFilePath) {
//
//        File file = new File(localFilePath);
//        if (!file.exists())
//            throw new RuntimeException("Error. Local file not found");
//
//        StandardFileSystemManager manager = new StandardFileSystemManager();
//
//        try {
//            manager.init();
//
//            // Create local file object
//            FileObject localFile = manager.resolveFile(file.getAbsolutePath());
//
//            // Create remote file object
//            FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
//
//            // Copy local file to sftp server
//            remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
//
//            System.out.println("File upload success");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            manager.close();
//        }
//    }
//
//    public static boolean move(String hostName, String username, String password, String remoteSrcFilePath, String remoteDestFilePath){
//        StandardFileSystemManager manager = new StandardFileSystemManager();
//
//        try {
//            manager.init();
//
//            // Create remote object
//            FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteSrcFilePath), createDefaultOptions());
//            FileObject remoteDestFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteDestFilePath), createDefaultOptions());
//
//            if (remoteFile.exists()) {
//                remoteFile.moveTo(remoteDestFile);;
//                System.out.println("Move remote file success");
//                return true;
//            }
//            else{
//                System.out.println("Source file doesn't exist");
//                return false;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            manager.close();
//        }
//    }
//
//    /**
//     * Method to download the file from remote server location
//     *
//     * @param hostName
//     *            HostName of the server
//     * @param username
//     *            UserName to login
//     * @param password
//     *            Password to login
//     * @param localFilePath
//     *            LocalFilePath. Should contain the entire local file path -
//     *            Directory and Filename with \\ as separator
//     * @param remoteFilePath
//     *            remoteFilePath. Should contain the entire remote file path -
//     *            Directory and Filename with / as separator
//     */
//    
//    
//    public static void download(String hostName, String username, String password, String localFilePath, String remoteFilePath) {
//
//        StandardFileSystemManager manager = new StandardFileSystemManager();
//        FileSystemOptions fsOptions = new FileSystemOptions();
//        try {
//            manager.init();
//
//            FileObject localFile = manager.resolveFile(localFilePath);
//
//            // Create remote file object
//         
//            FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
//            System.out.print(remoteFile.getContent().getSize());
//            // Copy local file to sftp server
//            localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
//
//            System.out.println("File download success");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            manager.close();
//        }
//    }
//
//    public static void delete(String hostName, String username, String password, String remoteFilePath) {
//        StandardFileSystemManager manager = new StandardFileSystemManager();
//
//        try {
//            manager.init();
//
//            // Create remote object
//            FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
//
//            if (remoteFile.exists()) {
//                remoteFile.delete();
//                System.out.println("Delete remote file success");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            manager.close();
//        }
//    }
//
//    // Check remote file is exist function:
//    /**
//     * Method to check if the remote file exists in the specified remote
//     * location
//     *
//     * @param hostName
//     *            HostName of the server
//     * @param username
//     *            UserName to login
//     * @param password
//     *            Password to login
//     * @param remoteFilePath
//     *            remoteFilePath. Should contain the entire remote file path -
//     *            Directory and Filename with / as separator
//     * @return Returns if the file exists in the specified remote location
//     */
//    public static boolean exist(String hostName, String username, String password, String remoteFilePath) {
//        StandardFileSystemManager manager = new StandardFileSystemManager();
//
//        try {
//            manager.init();
//
//            FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
//
//            System.out.println("File exist: " + remoteFile.exists());
//
//            return remoteFile.exists();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            manager.close();
//        }
//    }
//
//    /**
//     * Generates SFTP URL connection String
//     *
//     * @param hostName
//     *            HostName of the server
//     * @param username
//     *            UserName to login
//     * @param password
//     *            Password to login
//     * @param remoteFilePath
//     *            remoteFilePath. Should contain the entire remote file path -
//     *            Directory and Filename with / as separator
//     * @return concatenated SFTP URL string
//     */
////    public static String createConnectionString(String hostName, String username, String password, String remoteFilePath) {
////        return "sftp://fs_user:BanBanZip@192.168.78.15:22/file2/data-DS/PreReg/25871/original/wajah1637479243415.png";
//////        sftp://fs_user:BanBanZip@192.168.78.15:22/file2/data-DS/PreReg/25871/original/wajah1637479243415.png
////    }
//    public static String createConnectionString(String hostName, String username, String password, String remoteFilePath) {
//        return "sftp://" + username + ":" + password + "@" + hostName + "/" + remoteFilePath;
//    }
//
//    /**
//     * Method to setup default SFTP config
//     *
//     * @return the FileSystemOptions object containing the specified
//     *         configuration options
//     * @throws FileSystemException
//     */
//    public static FileSystemOptions createDefaultOptions() throws FileSystemException {
//        // Create SFTP options
//        FileSystemOptions opts = new FileSystemOptions();
//
//        // SSH Key checking
//        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
//
//        /*
//         * Using the following line will cause VFS to choose File System's Root
//         * as VFS's root. If I wanted to use User's home as VFS's root then set
//         * 2nd method parameter to "true"
//         */
//        // Root directory set to user home
//        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
//
//        // Timeout is count by Milliseconds
//        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
//
//        return opts;
//    }
//}