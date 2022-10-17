package id.co.keriss.consolidate.util;

public interface DSAPI {

	//sebelum migrasi
		static final boolean baru = true;
		static final String devel = null;
	 	static final String BILLING_HOST=System.getenv("BILLING_BASE");
		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DSBaru_dev/";
		static final String LINK=System.getenv("LINK_FORGOT_PASSWORD");
		static final String DOMAIN=System.getenv("DOMAIN_WEB_APP");
		static final String WEB_BULKSIGN=System.getenv("DOMAIN_BULKSIGN");
		static final String DOMAINAPI=System.getenv("DOMAIN_API");
		static final String DOMAINAPIWV=System.getenv("DOMAIN_WV");
		static final String SMS_API=System.getenv("SMS_API");
		static final String FACE_API=System.getenv("FR_FACE_API");
		static final String FACE_API2=System.getenv("FR_FACE_API");
		static final String EMAIL_API=System.getenv("EMAIL_API");
		static final String DUKCAPIL_API=System.getenv("DUKCAPIL");
		static final String LOGIN=System.getenv("LINK_WEB_LOGIN");
		static final String KILLBILL=System.getenv("BILLING_API");

		public final static String FILESYS=System.getenv("FILESYS_SERVER_ADDRESS");
	    public final static String FILESYS_SERVER_ADDRESS=System.getenv("FILESYS_SERVER_ADDRESS");
//		    public final static String FILESYS_USERNAME="snids";
//		    public final static String FILESYS_PASSWORD="$0luS!n3tD$";
//		    public final static String FILESYS_DOMAIN="SNIGROUP";
	    public final static String FILESYS_USERNAME="fs_user";
	    public final static String FILESYS_PASSWORD="BanBanZip";
	    public final static String FILESYS_DOMAIN="WORKGROUP";
		public final static String FILESYS_NAS=System.getenv("FILESYS_SERVER_ADDRESS2");
	    public final static String FILESYS_SERVER_ADDRESS_NAS=System.getenv("FILESYS_SERVER_ADDRESS");
	    public final static String FILESYS_USERNAME_NAS="fs_user";
	    public final static String FILESYS_PASSWORD_NAS="BanBanZip";
	    public final static String FILESYS_DOMAIN_NAS="WORKGROUP";
//		    public final static String FILESYS_USERNAME_NAS="snids";
//		    public final static String FILESYS_PASSWORD_NAS="$0luS!n3tD$";
//	    public final static String FILESYS_DOMAIN_NAS="SNIGROUP";
	    final static String KMS_HOSTS=System.getenv("KMS_HOST_BANYAK");
	    final static String NEW_KMS_HOSTS=System.getenv("KMS_HOST");
	    static final String KMS_HOST_OLD=System.getenv("KMS_HOST_OLD");
//			static final String KMS_HOSTS=System.getenv("KMS_HOST_BANYAK");
//			static final String NEW_KMS_HOSTS=System.getenv("KMS_HOST");
		static final String KP = System.getenv("KP");
		static final String KPSE = System.getenv("KPSE");
		static final String DB_LOG=System.getenv("ACTIVITY_LOG");
		static final String LINK_PENDAFTARAN_ULANG = System.getenv("LINK_PENDAFTARAN_ULANG"); //https://devkube.tandatanganku.com/pendaftaran_ulang.html
		static final String MASS_REVOKE = System.getenv("MASS_REVOKE");
		static final String METERAI = System.getenv("METERAI");
		static final String VERSION = "WEBVIEW V3.1";
		
		//Config redis
		static final String REDIS_HOST = System.getenv("REDIS_HOST");
		static final Integer REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));
		static final String REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");
		static final Integer REDIS_TIMEOUT = Integer.parseInt(System.getenv("REDIS_TIMEOUT"));

//		//testing 182.7 beta
//		static final boolean baru = true;
//		static final String devel = null;
//	 	static final String BILLING_HOST=System.getenv("BILLING_BASE");
//		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/betawv/";
//		static final String LINK=System.getenv("LINK_FORGOT_PASSWORD");
//		static final String DOMAIN=System.getenv("DOMAIN_WEB_APP_BETA");
//		static final String DOMAINAPI=System.getenv("DOMAIN_API_BETA");
//		static final String DOMAINAPIWV=System.getenv("DOMAIN_WV_BETA");
//		static final String SMS_API=System.getenv("SMS_API");
//		static final String FACE_API=System.getenv("FR_FACE_API");
//		static final String FACE_API2=System.getenv("FR_FACE_API");
//		static final String EMAIL_API=System.getenv("EMAIL_API");
//		static final String DUKCAPIL_API=System.getenv("DUKCAPIL");
//		static final String LOGIN=System.getenv("LINK_WEB_LOGIN");
//		static final String KILLBILL=System.getenv("BILLING_API");
//
//		public final static String FILESYS=System.getenv("FILESYS_SERVER_ADDRESS");
//	    public final static String FILESYS_SERVER_ADDRESS=System.getenv("FILESYS_SERVER_ADDRESS");
////	    public final static String FILESYS_USERNAME="snids";
////	    public final static String FILESYS_PASSWORD="$0luS!n3tD$";
//	    public final static String FILESYS_USERNAME="fs_user";
//	    public final static String FILESYS_PASSWORD="BanBanZip";
//	    public final static String FILESYS_DOMAIN="WORKGROUP";
//		public final static String FILESYS_NAS=System.getenv("FILESYS_SERVER_ADDRESS2");
//	    public final static String FILESYS_SERVER_ADDRESS_NAS=System.getenv("FILESYS_SERVER_ADDRESS");
//	    public final static String FILESYS_USERNAME_NAS="fs_user";
//	    public final static String FILESYS_PASSWORD_NAS="BanBanZip";
////	    public final static String FILESYS_USERNAME_NAS="snids";
////	    public final static String FILESYS_PASSWORD_NAS="$0luS!n3tD$";
//	    public final static String FILESYS_DOMAIN_NAS="WORKGROUP";
//	    final static String KMS_HOSTS=System.getenv("KMS_HOST_BANYAK_HSK_BETA");
//	    final static String NEW_KMS_HOSTS=System.getenv("KMS_HOST_HSK_BETA");
//	    static final String KMS_HOST_OLD=System.getenv("KMS_HOST_OLD");
////		static final String KMS_HOSTS=System.getenv("KMS_HOST_BANYAK");
////		static final String NEW_KMS_HOSTS=System.getenv("KMS_HOST");
//		static final String KP = System.getenv("KP");
//		static final String KPSE = System.getenv("KPSE");
//		static final String DB_LOG=System.getenv("ACTIVITY_LOG");
//		static final String LINK_PENDAFTARAN_ULANG = System.getenv("LINK_PENDAFTARAN_ULANG"); //https://devkube.tandatanganku.com/pendaftaran_ulang.html
//		static final String MASS_REVOKE = System.getenv("MASS_REVOKE");		    
		
}
