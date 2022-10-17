package id.co.keriss.consolidate.util;

public interface DSAPI {
//		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
//		static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
//		static final String DOMAIN="app.digisign.id";
//		static final String DOMAINAPI="api.digisign.id";
//        static final String BILLING_HOST="http://localhost:8080";
//	    static final String SMS_API="http://localhost:7090";
//	    static final String FACE_API="http://localhost/liveness/";
//		static final String FACE_API2="http://localhost/liveness/";
	
	
//	//production
	
//	static final String BILLING_HOST="http://localhost:8080";
//	static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/genLink/";
//	static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
//	static final String DOMAIN="app.digisign.id";
//	static final String DOMAINAPI="api.digisign.id";
//	static final String DOMAINAPIWV="wv.digisign.id";
//	static final String SMS_API="https://smsservice:7272/smsdigisign";
//	static final String WEBDOMAIN="digisign.id";
//	//static final String SMS_API="https://localhost:7272";
//	static final String FACE_API="https://192.168.250.10/";
//	static final String FACE_API2="https://192.168.250.10/";
//	static final String FACE_API_WITH_KTP="https://192.168.250.10:8811/test";
//	static final String EMAIL_API="http://localhost:8080/mail/api/"; 
//	static final String DUKCAPIL_API="https://localhost:7070/DSSv1-0/";
//	static final String LOGIN="https://app.digisign.id/login.html";
//	static final String LINK="app.digisign.id/forgotpassword.html";
//	static final String FTPPATH="/opt/data-DS/ftp/";
//	static final String FACE_API_WEFIE="https://192.168.250.10:8811";
//	static final String ACC_CALLBACK="https://mobile.bidmart.co.id";
//	/**TODO change this link callback_view*/
//	static final String ACC_CALLBACK_VIEW="https://sofia.acc.co.id";
//	static final long ACC_MITRA=68;
//	
	 	//test
		
	    static final String BILLING_HOST="http://192.168.182.7:8080";
		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/genLink/";
		static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
		static final String LINK="app.tandatanganku.com/forgotpassword.html";
		static final String DOMAIN="app.tandatanganku.com";
		static final String DOMAINAPI="api.tandatanganku.com";
		static final String DOMAINAPIWV="wv.tandatanganku.com";
		static final String SMS_API="https://corp.tandatanganku.com";
		static final String WEBDOMAIN="https://tandatanganku.com";
		static final String FACE_API="https://192.168.250.10/";
		static final String FACE_API2="https://192.168.250.10/";
		static final String FACE_API_WITH_KTP="https://192.168.250.10:8811/test";
		static final String EMAIL_API="https://192.168.182.7:7272/mail_dev/api/";
		static final String DUKCAPIL_API="https://192.168.182.7:7070/DSSv1-0/";
		static final String LOGIN="https://app.tandatanganku.com/login.html";
		static final String FTPPATH="/opt/data-DS/ftp/";
		static final String FACE_API_WEFIE="https://192.168.250.10:8811";
		//callback acc
		static final String ACC_CALLBACK="https://acc-dev1.outsystemsenterprise.com";
//		static final String ACC_CALLBACK_VIEW="https://sofiadev.acc.co.id";
		static final String ACC_CALLBACK_VIEW="https://apidev.acc.co.id";
	    static final long ACC_MITRA=123;

	
		//UAT
//		
//	    static final String BILLING_HOST="http://192.168.182.7:8080";
//		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/genLink/";
//		static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
//		static final String LINK="app.tandatanganku.com/forgotpassword.html";
//		static final String DOMAIN="app.tandatanganku.com";
//		static final String DOMAINAPI="apiuat.tandatanganku.com";
//		static final String DOMAINAPIWV="wv.tandatanganku.com";
//		static final String WEBDOMAIN="https://tandatanganku.com";
//		static final String SMS_API="https://corp.tandatanganku.com";
//		static final String FACE_API="https://192.168.250.10/";
//		static final String FACE_API2="https://192.168.250.10/";
//		static final String FACE_API_WITH_KTP="https://192.168.250.10:8811/test";
//		static final String FACE_API_WEFIE="https://192.168.250.10:8811";
//		static final String EMAIL_API="http://192.168.182.7:8080/mail/api/";
//		static final String DUKCAPIL_API="https://192.168.182.7:7070/DSSv1-0/";
//		static final String LOGIN="https://app.tandatanganku.com/login.html";
//		static final String FTPPATH="/opt/data-DS/ftp/";
//		//callback acc
//		static final String ACC_CALLBACK="https://acc-dev1.outsystemsenterprise.com";
//		static final String ACC_CALLBACK_VIEW="https://sofiadev.acc.co.id";
//	    static final String ACC_MITRA="ACC";		
		
	/*
	//Development
    //static final String BILLING_HOST="http://192.168.182.7:8080";
    //static final String BILLING_HOST="http://192.168.182.7:8080";
    //static final String BILLING_HOST="http://192.168.182.7:8080";
	static final String BILLING_HOST="http://192.168.182.7:8080";
	static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/genLink/";
	static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
	static final String DOMAIN="devapp.tandatanganku.com";
	static final String DOMAINAPI="devapi.tandatanganku.com";
	static final String DOMAINAPIWV="wvapi.tandatanganku.com";
	static final String WEBDOMAIN="https://tandatanganku.com";
	//static final String DOMAIN="devapp.tandatanganku.com";
	//static final String DOMAINAPI="devapi.tandatanganku.com";
	static final String SMS_API="https://corp.tandatanganku.com";
	//static final String FACE_API="http://192.168.78.16/liveness/";
	static final String FACE_API="https://192.168.250.10/";
	static final String FACE_API2="https://192.168.250.10/";
	static final String EMAIL_API="http://192.168.182.7:8080/mail/api/";
	//static final String EMAIL_API="http://192.168.78.16:8080/mail/api/";
	//static final String EMAIL_API="http://localhost:8080/mail/api/";
	static final String DUKCAPIL_API="https://192.168.182.7:7070/DSSv1-0/";
	static final String LOGIN="https://tandatanganku.com/login.html";
	static final String FTPPATH="/opt/data-DS/ftp/";
	static final String LINK="app.tandatanganku.com/forgotpassword.html";
	*/

}
