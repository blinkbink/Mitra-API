package id.co.keriss.consolidate.httpconf;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
 
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
 
import org.apache.http.conn.ssl.SSLSocketFactory;
 
/**
 * Creates an insecure SSL socket that trusts any certificate.
 * 
 * DO NOT USE IN PRODUCTION ENVIRONMENT!
 *
 */
public class MySSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");
 
    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
 
        TrustManager tm = new X509TrustManager() {
            @Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
 
            @Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
 
            @Override
			public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
 
        sslContext.init(null, new TrustManager[] { tm }, null);
    }
 
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }
 
    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
    
    
    
//    private HttpClient getNewHttpClient() {
//    	try {
//    		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//    		trustStore.load(null, null);
//    		MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
//    		sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//     
//    		HttpParams params = new BasicHttpParams();
//    		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//    		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//     
//    		SchemeRegistry registry = new SchemeRegistry();
//    		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//    		registry.register(new Scheme("https", sf, 443));
//     
//    		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
//    		return new DefaultHttpClient(ccm, params);
//    	} catch (Exception e) {
//    		return new DefaultHttpClient();
//    	}
//    }
    
    
}