package id.co.keriss.consolidate.util;

public interface DSAPI {

//		sebelum migrasi
		static final boolean baru = true;
		static final String BILLING_HOST=System.getenv("BILLING_BASE");
//		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/betaapi/";
		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
		static final String KMS_HOST=System.getenv("KMS_HOST");
		static final String KMS_HOST_BANYAK=System.getenv("KMS_HOST_BANYAK");
		static final String KMS_HOST_OLD=System.getenv("KMS_HOST_OLD");
//		static final String KMS_HOST=System.getenv("KMS_HOST");
//		static final String KMS_HOST_BANYAK=System.getenv("KMS_HOST_BANYAK");
		static final String LINK=System.getenv("LINK_FORGOT_PASSWORD");
		static final String DOMAIN=System.getenv("DOMAIN_WEB_APP");
		static final String DOMAINAPI=System.getenv("DOMAIN_API");
		static final String DOMAINAPIWV=System.getenv("DOMAIN_API_WV");
		static final String SMS_API=System.getenv("SMS_API");
		static final String WEBDOMAIN=System.getenv("DOMAIN_WEB");
		static final String FACE_API=System.getenv("FR_FACE_API");
		static final String FACE_API3=System.getenv("FR_BASE");
		static final String FACE_ONLY=System.getenv("FR_CHECK_FACE");
		static final String EMAIL_API=System.getenv("EMAIL_API");
		static final String LOGIN=System.getenv("LINK_WEB_LOGIN");
		static final String FTPPATH="/opt/data-DS/ftp/";
		static final String KILLBILL=System.getenv("BILLING_API");
		static final String FACE_TO_FACE=System.getenv("FR_FIND_FACE");
		static final String FILESYS_SERVER_ADDRESS=System.getenv("FILESYS_SERVER_ADDRESS");

		static final String DB_LOG=System.getenv("ACTIVITY_LOG");
		static final String FILESYS_SERVER_ADDRESS_NAS2=System.getenv("FILESYS_SERVER_ADDRESS2");
		static final String FILESYS_SERVER_ADDRESS_NAS=System.getenv("FILESYS_SERVER_ADDRESS");
//		static final String FILESYS_USERNAME_NAS="snids";
//		static final String FILESYS_PASSWORD_NAS="$0luS!n3tD$";
//		static final String FILESYS_DOMAIN_NAS="SNIGROUP";
		public final static String FILESYS_USERNAME_NAS="fs_user";
	    public final static String FILESYS_PASSWORD_NAS="BanBanZip";
		static final String FILESYS_DOMAIN_NAS="WORKGROUP";
//		static final String REKENING="https://dukcapil:7070/DS-Support-ILUMA/";
//		static final String DUKCAPIL="https://dukcapil:7070/DSSv1-0/";	
		static final String DUKCAPIL=System.getenv("DUKCAPIL");
		static final boolean DEVELOPMENT=true;
		static final double THRESHHOLD_FR=0.50;
		static final int LONTONG=17;
		static final String CALLBACK=System.getenv("EMAIL_API")+"sendCallback.html?link=";
		static final String GENERATEPDF=System.getenv("GENERATE_PDF");
		static final String QRTEXT=System.getenv("QR_TEXT");
		static final String QRTEXTKEY="Sni!dgs1278881";
		static final String KP = System.getenv("KP");
		static final String KPSE = System.getenv("KPSE");
		static final String LINK_PENDAFTARAN_ULANG = System.getenv("LINK_PENDAFTARAN_ULANG"); //https://devkube.tandatanganku.com/pendaftaran_ulang.html
		static final String MASS_REVOKE = System.getenv("MASS_REVOKE");
		static final String METERAI = System.getenv("METERAI");
		static final String VERSION = "v2.5";
	
	//beta
//	static final boolean baru = true;
//	static final String BILLING_HOST=System.getenv("BILLING_BASE");
//	static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/betaapi/";
////	static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
//	static final String KMS_HOST=System.getenv("KMS_HOST_HSK_BETA");
//	static final String KMS_HOST_BANYAK=System.getenv("KMS_HOST_BANYAK_HSK_BETA");
//	static final String KMS_HOST_OLD=System.getenv("KMS_HOST_OLD");
////	static final String KMS_HOST=System.getenv("KMS_HOST");
////	static final String KMS_HOST_BANYAK=System.getenv("KMS_HOST_BANYAK");
//	static final String LINK=System.getenv("LINK_FORGOT_PASSWORD");
//	static final String DOMAIN=System.getenv("DOMAIN_WEB_APP");
//	static final String DOMAINAPI=System.getenv("DOMAIN_API_BETA");
//	static final String DOMAINAPIWV=System.getenv("DOMAIN_API_WV_BETA");
//	static final String SMS_API=System.getenv("SMS_API");
//	static final String WEBDOMAIN=System.getenv("DOMAIN_WEB");
//	static final String FACE_API=System.getenv("FR_FACE_API");
//	static final String FACE_API3=System.getenv("FR_BASE");
//	static final String FACE_ONLY=System.getenv("FR_CHECK_FACE");
//	static final String EMAIL_API=System.getenv("EMAIL_API");
//	static final String LOGIN=System.getenv("LINK_WEB_LOGIN");
//	static final String FTPPATH="/opt/data-DS/ftp/";
//	static final String KILLBILL=System.getenv("BILLING_API");
//	static final String FACE_TO_FACE=System.getenv("FR_FIND_FACE");
//	static final String FILESYS_SERVER_ADDRESS=System.getenv("FILESYS_SERVER_ADDRESS");
//
//	static final String DB_LOG=System.getenv("ACTIVITY_LOG");
//	static final String FILESYS_SERVER_ADDRESS_NAS2=System.getenv("FILESYS_SERVER_ADDRESS2");
//	static final String FILESYS_SERVER_ADDRESS_NAS=System.getenv("FILESYS_SERVER_ADDRESS");
////	static final String FILESYS_USERNAME_NAS="snids";
////	static final String FILESYS_PASSWORD_NAS="$0luS!n3tD$";
//	public final static String FILESYS_USERNAME_NAS="fs_user";
//    public final static String FILESYS_PASSWORD_NAS="BanBanZip";
//	static final String FILESYS_DOMAIN_NAS="WORKGROUP";
////	static final String REKENING="https://dukcapil:7070/DS-Support-ILUMA/";
////	static final String DUKCAPIL="https://dukcapil:7070/DSSv1-0/";	
//	static final String DUKCAPIL=System.getenv("DUKCAPIL");
//	static final boolean DEVELOPMENT=false;
//	static final double THRESHHOLD_FR=0.50;
//	static final int LONTONG=17;
//	static final String CALLBACK=System.getenv("EMAIL_API")+"sendCallback.html?link=";
//	static final String GENERATEPDF=System.getenv("GENERATE_PDF");
//	static final String QRTEXT=System.getenv("QR_TEXT");
//	static final String QRTEXTKEY="Sni!dgs1278881";
//	static final String KP = System.getenv("KP");
//	static final String KPSE = System.getenv("KPSE");
//	static final String LINK_PENDAFTARAN_ULANG = System.getenv("LINK_PENDAFTARAN_ULANG"); //https://devkube.tandatanganku.com/pendaftaran_ulang.html
//		static final String MASS_REVOKE = System.getenv("MASS_REVOKE");
		
}
