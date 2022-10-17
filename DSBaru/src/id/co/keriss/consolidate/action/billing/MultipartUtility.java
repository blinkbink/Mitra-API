package id.co.keriss.consolidate.action.billing;


import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server. 
 * @author www.codejava.net
 *
 */
public class MultipartUtility {
	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private HttpURLConnection httpConn;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;
	HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
		public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };


    private static class DefaultTrustManager implements X509TrustManager {

        @Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
		public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
    
	/**
	 * This constructor initializes a new HTTP POST request with content type
	 * is set to multipart/form-data
	 * @param requestURL
	 * @param charset
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public MultipartUtility(String requestURL, String charset, String auth, String refTrx)
			throws IOException, NoSuchAlgorithmException, KeyManagementException {
		this.charset = charset;
		
		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";
		
		URL url = new URL(requestURL);
		
		if (url.toString().startsWith("https://"))
		{
			httpConn = (HttpsURLConnection) url.openConnection();
			((HttpsURLConnection) httpConn).setHostnameVerifier(hostnameVerifier);
			  SSLContext ctx = SSLContext.getInstance("TLS");
		        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
		        SSLContext.setDefault(ctx);
		       ((HttpsURLConnection) httpConn).setSSLSocketFactory(ctx.getSocketFactory());
		}
		else
		{
			httpConn = (HttpURLConnection) url.openConnection();
		}
		
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true);	// indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("tokenId", refTrx);
		httpConn.setChunkedStreamingMode(0);
	    httpConn.setConnectTimeout(10000);
	    httpConn.setReadTimeout(300000);
		httpConn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		if(auth!=null)httpConn.setRequestProperty("Authorization", "Bearer "+auth);
		outputStream = httpConn.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
				true);
	}

	/**
	 * Adds a form field to the request
	 * @param name field name
	 * @param value field value
	 */
	public void addFormField(String name, String value) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
				.append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(
				LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Adds a upload file section to the request 
	 * @param fieldName name attribute in <input type="file" name="..." />
	 * @param uploadFile a File to be uploaded 
	 * @throws IOException
	 */
	public void addFilePart(String fieldName, File uploadFile)
			throws IOException {
		String fileName = uploadFile.getName();
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append(
				"Content-Disposition: form-data; name=\"" + fieldName
						+ "\"; filename=\"" + fileName + "\"")
				.append(LINE_FEED);
		writer.append(
				"Content-Type: "
						+ URLConnection.guessContentTypeFromName(fileName))
				.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		FileInputStream inputStream = new FileInputStream(uploadFile);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();
		
		writer.append(LINE_FEED);
		writer.flush();		
	}

	/**
	 * Adds a header field to the request.
	 * @param name - name of the header field
	 * @param value - value of the header field
	 */
	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}
	
	/**
	 * Completes the request and receives response from the server.
	 * @return a list of Strings as response in case the server returned
	 * status OK, otherwise an exception is thrown.
	 * @throws IOException
	 */
	public List<String> finish() throws IOException {
		List<String> response = new ArrayList<String>();

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				response.add(line);
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}

		return response;
	}

	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}
}
