package api.generatepdf;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.util.DSAPI;

public class GeneratePDF {
	public static Logger log=LogManager.getLogger(FaceRecognition.class);
	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() 
	{
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // TODO Auto-generated method stub

        }
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // TODO Auto-generated method stub

        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    }
	};

//public JSONObject fromAPI(JSONObject jsonfield) throws IOException, JSONException
//{
//	JSONObject jsonObject;
//    SSLContext sc = null;
//    try {
//        sc = SSLContext.getInstance("SSL");
//        sc.init(null, trustAllCerts, new java.security.SecureRandom());
//    } catch (KeyManagementException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    } catch (NoSuchAlgorithmException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//    // Create all-trusting host name verifier
//    HostnameVerifier allHostsValid = new HostnameVerifier() {
//        @Override
//        public boolean verify(String hostname, SSLSession session) {
//            return true;
//        }
//    };
//
//    URL url;
//    url = new URL("https://192.168.182.7:7070/pdf-api/api/pdfReport.html");
//
//    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//
//    con.setRequestMethod("POST");
//    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//    con.setRequestProperty("Accept", "application/json");
//    con.setRequestProperty("Authorization", "Bearer CmVcE2fuptE7ob2DXZOcvQhtcyaaF2KzzX5y8VV2yuiaJEoK9mULnD2ExLwk9w");
//
//    con.setDoOutput(true);
//
//   
//    String jsonInputString = "jsonfield=" + jsonfield.toString().replace("\\", "");
//    System.out.println(jsonInputString);
//
//    try (OutputStream os = con.getOutputStream()) {
//        byte[] input = jsonInputString.getBytes();
//        os.write(input, 0, input.length);
//    }
//
//    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
//        StringBuilder response = new StringBuilder();
//        String responseLine = null;
//        while ((responseLine = br.readLine()) != null) {
//            response.append(responseLine.trim());
//        }
//
//        jsonObject = new JSONObject(response.toString());
//        
//    }
//	return jsonObject;
//}

	
	public JSONObject fromAPI(JSONObject jsonfile) {
		 JSONObject msgResp=new JSONObject();
	        try {
				msgResp.put("connection", false);
				msgResp.put("score", 1.00);
				msgResp.put("result", false);
		        msgResp.put("info", "verfikasi gagal, silakan coba kembali");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        

	        
			int tryCnt=0;
			HttpsURLConnection con = null ;
			try {
				while(tryCnt<4 && msgResp.getBoolean("connection")==false) {
					URL obj;
					
					try {
						if(tryCnt>1) {
							obj = new URL(DSAPI.GENERATEPDF);
						}else {
							obj = new URL(DSAPI.GENERATEPDF);
						}
			      
			 
			        // Install the all-trusting trust manager
			        SSLContext sc = SSLContext.getInstance("SSL");
			        sc.init(null, trustAllCerts, new java.security.SecureRandom());
			        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			 
			        // Create all-trusting host name verifier
			        HostnameVerifier allHostsValid = new HostnameVerifier() {
			        	@Override
			            public boolean verify(String hostname, SSLSession session) {
			                return true;
			            }
			        };
			 
			        // Install the all-trusting host verifier
			        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			 
					con = (HttpsURLConnection) obj.openConnection();

						// Setting basic post request
						con.setRequestMethod("POST");
						// String postJsonData = "{'text':'c54s123','dest' : '081212611881'}";
						con.setConnectTimeout(60000);//set timeout to 60 seconds
						// Send post request
						con.setDoOutput(true);			
						DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				
					
						log.info("POST : "+obj.toString());

						Map<String,String> arguments = new HashMap<>();
						arguments.put("jsonfield", jsonfile.toString());
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						log.info("RSP CODE: "+responseCode);

						if(responseCode==200) {
							try {
								log.info("Response = masuk okeee");
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								log.error("GENERATE PDF ", e1);
							}
						}

						// System.out.println("Post Data : " + postJsonData);
						System.out.println("Response Code : " + responseCode);

						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						log.info("RSP-DATA: "+response.toString());

						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());

							return rsJson;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

							log.error("GENERATE PDF ", e);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								log.error("GENERATE PDF ", e1);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();

						log.error("GENERATE PDF ", e2);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();

							log.error("GENERATE PDF ", e1);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						log.error("GENERATE PDF ", e);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							log.error("GENERATE PDF ", e1);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						log.error("GENERATE PDF ", e2);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

						log.error("GENERATE PDF ", e2);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("GENERATE PDF ", e);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("GENERATE PDF ", e);

			} finally {
				con.disconnect();
			}
			log.info("RSP-PARSE: "+msgResp.toString());

			return msgResp;
			
	}
}
