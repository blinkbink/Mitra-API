package id.co.keriss.consolidate.DS;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
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

import id.co.keriss.consolidate.util.DSAPI;

public class FaceRecognition implements DSAPI{
	public static Logger log=LogManager.getLogger(FaceRecognition.class);
	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
          
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
	/**
	 * 
	 * @param ktp base64 encode
	 * @param wajah base64 encode
	 * @return
	 */
	public JSONObject checkFace(byte [] ktp, byte [] wajah) {
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
	        
	      
//	        try {
//	            fotoktp = Base64.encode(FileUtils.readFileToByteArray(new File("/home/dev19/Downloads/Telegram Desktop/FIKI/ktp.jpg")));
//		        fotowajah = Base64.encode(FileUtils.readFileToByteArray(new File("/home/dev19/Downloads/Telegram Desktop/FIKI/foto2.jpg")));
//			} catch (IOException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}

	        
			int tryCnt=0;
			try {
				while(tryCnt<4 && msgResp.getBoolean("connection")==false) {
					URL obj;
					HttpsURLConnection con ;
					try {
						if(tryCnt>1) {
							obj = new URL(FACE_API2);
						}else {
							obj = new URL(FACE_API);
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
					
						// wr.writeBytes(postJsonData);
					
						log.info("POST : "+obj.toString());
						
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("ktp", new String(ktp, StandardCharsets.US_ASCII));
						arguments.put("file", new String(wajah, StandardCharsets.US_ASCII)); // This is a fake password obviously
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

								log.error("FACE RECOGNITION", e1);
							}
						}
						else if(responseCode==300) {
							try {
								log.info("Response = masuk 300");
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								log.error("FACE RECOGNITION", e1);
							}
						}
						else if(responseCode==500) {
							try {
								log.info("Response = masuk 500");
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								log.error("FACE RECOGNITION", e1);
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

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							
							if(rsJson.has("score")) {
								msgResp.put("score", Double.parseDouble(rsJson.getString("score")));
							}
							if(rsJson.getInt("response")==200) {
								if(rsJson.getString("liveness").equals("true") && rsJson.getString("match").equals("true")) {
									msgResp.put("result", true);
							        msgResp.put("info", "verifikasi berhasil");
								}else if(rsJson.getString("liveness").equals("true") && rsJson.getString("match").equals("false")) {
							        msgResp.put("info", "Verifikasi gagal. Pastikan cahaya cukup dan foto ktp sesuai dengan foto wajah.");

								}else {
							        msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
									msgResp.put("score", 1.00);

								}
							}
							else {
								msgResp.put("info", rsJson.getString("classify"));
							}
							
							break;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

							log.error("FACE RECOGNITION", e);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								log.error("FACE RECOGNITION", e1);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();

						log.error("FACE RECOGNITION", e2);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();

							log.error("FACE RECOGNITION", e1);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						log.error("FACE RECOGNITION", e);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							log.error("FACE RECOGNITION", e1);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						log.error("FACE RECOGNITION", e2);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

						log.error("FACE RECOGNITION", e2);
					}
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("FACE RECOGNITION", e);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("FACE RECOGNITION", e);

			}
			log.info("RSP-PARSE: "+msgResp.toString());

//			System.out.println(msgResp.toString());
			return msgResp;
			
	}
}
