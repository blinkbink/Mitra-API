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
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class FaceRecognition implements DSAPI{
	public static Logger log=LogManager.getLogger(FaceRecognition.class);
	
	String kelas="api.rekening.CheckRekening";
	String refTrx="";
	String trxType="";
	HttpServletRequest request;
	
	public FaceRecognition(String refTrx, String trxType, HttpServletRequest request) {
		this.refTrx=refTrx;
		this.trxType=trxType;
		this.request=request;
	}
	
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
	public JSONObject checkFace(byte [] ktp, byte [] wajah, Long idmitra, String nik) {
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
							obj = new URL(FACE_API);
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
	
						LogSystem.info(request, "POST : "+obj.toString(), kelas, refTrx, trxType);
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("ktp", new String(ktp, StandardCharsets.US_ASCII));
						arguments.put("file", new String(wajah, StandardCharsets.US_ASCII));
						arguments.put("idmitra", String.valueOf(idmitra));
						arguments.put("nik", String.valueOf(nik));
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						LogSystem.info(request, "RSP CODE: "+responseCode, kelas, refTrx, trxType);

						if(responseCode==200) {
							try {
								LogSystem.info(request, "Response = masuk okeee", kelas, refTrx, trxType);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								log.error("FACE RECOGNITION", e1);
								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==300) {
							try {
								LogSystem.info(request, "Response = masuk 300", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(),e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==500) {
							try {
								LogSystem.info(request, "Response = masuk 500", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}


						// System.out.println("Post Data : " + postJsonData);
						LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						//log.info("RSP-DATA: "+response.toString());

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							LogSystem.info(request, "Message : " + rsJson, kelas, refTrx, trxType);
							if(rsJson.has("score")) {
								msgResp.put("score", Double.parseDouble(rsJson.getString("score")));
							}
							if(rsJson.has("file")) {
								msgResp.put("file", rsJson.getString("file"));
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
							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LogSystem.error(getClass(), e, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						log.error("FACE RECOGNITION", e2);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("FACE RECOGNITION", e);
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			} finally {
				con.disconnect();
			}
//			log.info("RSP-PARSE: "+msgResp.toString());

//			System.out.println(msgResp.toString());
			return msgResp;
			
	}
	
	public JSONObject checkFaceOnly(byte [] ktp, byte [] wajah, Long idmitra, String nik) {
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
							obj = new URL(FACE_ONLY);
						}else {
							obj = new URL(FACE_ONLY);
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
		
						LogSystem.info(request, "POST "+obj.toString(), kelas, refTrx, trxType);
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("ktp", new String(ktp, StandardCharsets.US_ASCII));
						arguments.put("file", new String(wajah, StandardCharsets.US_ASCII));
						arguments.put("idmitra", String.valueOf(idmitra));
						arguments.put("nik", String.valueOf(nik));
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						LogSystem.info(request, "RSP CODE: "+responseCode, kelas, refTrx, trxType);

						if(responseCode==200) {
							try {
								LogSystem.info(request, "Response = masuk okeee", kelas, refTrx, trxType);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==300) {
							try {
								LogSystem.info(request, "Response = masuk 300", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==500) {
							try {
								LogSystem.info(request, "Response = masuk 500", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}


						// System.out.println("Post Data : " + postJsonData);
						LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);

						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						//log.info("RSP-DATA: "+response.toString());

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							LogSystem.info(request, "Message : " + rsJson, kelas, refTrx, trxType);

							if(rsJson.has("score")) {
								msgResp.put("score", Double.parseDouble(rsJson.getString("score")));
							}
							if(rsJson.has("file")) {
								msgResp.put("file", rsJson.getString("file"));
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
							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LogSystem.error(getClass(),e, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			} finally {
				con.disconnect();
			}
			//log.info("RSP-PARSE: "+msgResp.toString());

//			System.out.println(msgResp.toString());
			return msgResp;
			
	}
	
	public JSONObject checkFaceWithKTP(byte [] ktp, byte [] wajah, Long idmitra, String nik) {
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
			HttpsURLConnection con = null;
			try {
				while(tryCnt<4 && msgResp.getBoolean("connection")==false) {
					URL obj;
					
					try {
						if(tryCnt>1) {
							obj = new URL(FACE_API);
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
					
						LogSystem.info(request, "POST : "+obj.toString(), kelas, refTrx, trxType);
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("ktp", new String(ktp, StandardCharsets.US_ASCII));
						arguments.put("file", new String(wajah, StandardCharsets.US_ASCII));
						arguments.put("idmitra", String.valueOf(idmitra));
						arguments.put("nik", String.valueOf(nik));
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						//log.info("RSP CODE: "+responseCode);

						if(responseCode==200) {
							try {
								LogSystem.info(request, "Response = masuk okeee", kelas, refTrx, trxType);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==300) {
							try {
								LogSystem.info(request, "Response = masuk 300", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==500) {
							try {
								LogSystem.info(request, "Response = masuk 500", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}


						// System.out.println("Post Data : " + postJsonData);
						LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);

						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						//log.info("RSP-DATA: "+response.toString());

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							LogSystem.info(request, "Message " + rsJson, kelas, refTrx, trxType);
							if(rsJson.has("score")) {
								msgResp.put("score", Double.parseDouble(rsJson.getString("score")));
							}
							if(rsJson.has("file")) {
								msgResp.put("file", rsJson.getString("file"));
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
							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LogSystem.error(getClass(), e, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			} finally {
				con.disconnect();
			}
			//log.info("RSP-PARSE: "+msgResp.toString());

//			System.out.println(msgResp.toString());
			return msgResp;
			
	}
	
	public JSONObject RegFaceRecognation(byte [] face,  Long idmitra, String nik, String name) {
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
			HttpsURLConnection con=null ;
			try {
				while(tryCnt<4 && msgResp.getBoolean("connection")==false) {
					URL obj;
					
					try {
						if(tryCnt>1) {
							obj = new URL(FACE_API3+"/register");
						}else {
							obj = new URL(FACE_API3+"/register");
						}
			      
						LogSystem.info(request, "URK "+obj.toString(), kelas, refTrx, trxType);
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
					
						
						LogSystem.info(request, "POST : "+obj.toString(), kelas, refTrx, trxType);
						
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("nik", nik);
						arguments.put("name", name);
						arguments.put("file", new String(face, StandardCharsets.US_ASCII));
						arguments.put("idmitra", String.valueOf(idmitra));
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						LogSystem.info(request, "RSP CODE: "+responseCode, kelas, refTrx, trxType);

						if(responseCode==200) {
							try {
								LogSystem.info(request, "Response = masuk okeee", kelas, refTrx, trxType);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}


						// System.out.println("Post Data : " + postJsonData);
						LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);

						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						//log.info("RSP-DATA: "+response.toString());

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							LogSystem.info(request, "Message : " + rsJson, kelas, refTrx, trxType);
							if(rsJson.has("file")) {
								msgResp.put("file", rsJson.getString("file"));
							}
							if(rsJson.getInt("response")==10) {
								msgResp.put("info", rsJson.getString("message"));
								msgResp.put("result", "00");
								msgResp.put("fileid", rsJson.getString("fileid"));
							} else {
								msgResp.put("info", rsJson.getString("message"));
								msgResp.put("result", "05");
								//msgResp.put("fileid", rsJson.getString("fileid"));
							}
							
							break;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();

							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						LogSystem.error(getClass(), e, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			} finally {
				con.disconnect();
			}
			//log.info("RSP-PARSE: "+msgResp.toString());

//			System.out.println(msgResp.toString());
			return msgResp;
			
	}
	
	public JSONObject FindFaceOnWefie(byte [] wefie,  Long idmitra, String nik) {
		 JSONObject msgResp=new JSONObject();
	        try {
				msgResp.put("connection", false);
				//msgResp.put("score", 1.00);
				msgResp.put("result", false);
		        msgResp.put("info", "verfikasi gagal, silakan coba kembali");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        
			int tryCnt=0;
			HttpsURLConnection con =null;
			try {
				while(tryCnt<4 && msgResp.getBoolean("connection")==false) {
					URL obj;
					
					try {
						if(tryCnt>1) {
							obj = new URL(FACE_TO_FACE);
						}else {
							obj = new URL(FACE_TO_FACE);
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
					
						LogSystem.info(request, "POST : "+obj.toString(), kelas, refTrx, trxType);
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("nik", nik);
						arguments.put("face", new String(wefie, StandardCharsets.US_ASCII));
						arguments.put("idmitra", String.valueOf(idmitra));
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						LogSystem.info(request, "RSP CODE: "+responseCode, kelas, refTrx, trxType);
						if(responseCode==200) {
							try {
								LogSystem.info(request, "Response = masuk okeee", kelas, refTrx, trxType);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}


						// System.out.println("Post Data : " + postJsonData);
						LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						//log.info("RSP-DATA: "+response.toString());

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							LogSystem.info(request, "Message : " + rsJson, kelas, refTrx, trxType);
							if(rsJson.getInt("rc")==10) {
								msgResp.put("info", rsJson.getBoolean("nik_face"));
								msgResp.put("result", "00");
								msgResp.put("fileid", rsJson.getString("fileid"));
								msgResp.put("name", rsJson.getString("name"));
								msgResp.put("nik", rsJson.getString("nik"));
							} else {
								msgResp.put("info", rsJson.getBoolean("nik_face"));
								msgResp.put("message", rsJson.getString("message"));
								msgResp.put("result", "05");
								//msgResp.put("fileid", rsJson.getString("fileid"));
							}
							
							break;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();

							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						LogSystem.error(getClass(), e, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			} finally {
				con.disconnect();
			}
			//log.info("RSP-PARSE: "+msgResp.toString());

//			System.out.println(msgResp.toString());
			return msgResp;
			
	}
	
	public JSONObject checkFacetoFace(byte [] wajah1, byte [] wajah2, Long idmitra, String nik) {
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
							obj = new URL(FACE_API3+"facetoface");
						}else {
							obj = new URL(FACE_API3+"facetoface");
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
					
						LogSystem.info(request, "POST : "+obj.toString(), kelas, refTrx, trxType);
						
						Map<String,String> arguments = new HashMap<>();
						arguments.put("face1", new String(wajah1, StandardCharsets.US_ASCII));
						arguments.put("face2", new String(wajah2, StandardCharsets.US_ASCII));
						arguments.put("idmitra", String.valueOf(idmitra));
						arguments.put("nik", String.valueOf(nik));
						
						StringJoiner sj = new StringJoiner("&");
						for(Map.Entry<String,String> entry : arguments.entrySet())
						    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
						         + URLEncoder.encode(entry.getValue(), "UTF-8"));
						byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
						
						wr.write(out);
						wr.flush();
						wr.close();

						int responseCode = con.getResponseCode();
						//log.info("RSP CODE: "+responseCode);

						if(responseCode==200) {
							try {
								LogSystem.info(request, "Response = masuk okeee", kelas, refTrx, trxType);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==300) {
							try {
								LogSystem.info(request, "Response = masuk 300", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}
						else if(responseCode==500) {
							try {
								LogSystem.info(request, "Response = masuk 500", kelas, refTrx, trxType);
							    msgResp.put("info", "verifikasi gagal. Pastikan cahaya cukup agar wajah anda terlihat jelas pada kamera. Dan pastikan foto langsung ke wajah anda");
								msgResp.put("score", 1.00);
								msgResp.put("connection", true);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}
						}


						// System.out.println("Post Data : " + postJsonData);
						LogSystem.info(request, "Response Code : " + responseCode, kelas, refTrx, trxType);

						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String output;
						StringBuffer response = new StringBuffer();

						while ((output = in.readLine()) != null) {
							response.append(output);
						}
						in.close();

						// printing result from response
						//log.info("RSP-DATA: "+response.toString());

//					System.out.println(response.toString());
						JSONObject rsJson;
						try {
							rsJson = new JSONObject(response.toString());
							LogSystem.info(request, "Message : " + rsJson, kelas, refTrx, trxType);
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

							LogSystem.error(getClass(), e, kelas, refTrx, trxType);
							try {
								msgResp.put("connection", false);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();

								LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
							}

						}
						
					} 
					catch (MalformedURLException e2) {
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();

							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						LogSystem.error(getClass(), e, kelas, refTrx, trxType);
						try {
							msgResp.put("connection", false);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							LogSystem.error(getClass(), e1, kelas, refTrx, trxType);
						}
					} catch (NoSuchAlgorithmException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} catch (KeyManagementException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

						LogSystem.error(getClass(), e2, kelas, refTrx, trxType);
					} 
					

					tryCnt++;
					
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogSystem.error(getClass(), e, kelas, refTrx, trxType);

			} finally {
				con.disconnect();
			}
			
			return msgResp;
			
	}
}
