package com.intuit.paymentsapi.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.intuit.paymentsapi.us.BaseTest;
import com.intuit.tame.ws.JsonTestClient;


public class TestUtil{
	private static Logger log = Logger.getLogger(TestUtil.class);
	
	    //the system variables we retrieve from the command line
		public static String auth = System.getProperty("auth");
		public static String env = System.getenv("vhost");
		public static String emulate = System.getProperty("emulate");
		public static String request_Id = "";
		public static String intuit_tid = "";
		public static boolean ifPassCompanyId = false;
		public static boolean isSBX = env.equals("ctosbx") ? true : false;
		public static boolean isOauth = false;
		public static boolean isinvalidOauth = false;	
		public static String[][] headers;
		public static OAuthConsumer consumer;
	
	/**
	 * This will do the JSON post request with map of map 
	 * @param url
	 * @param headerParams
	 * @param mapBody
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse post(String url, String[][] headerParams, HashMap<String,Object> mapBody) throws Exception {
				
		headers = setHeaders(headerParams);	
		HttpResponse response;
		
		if(	isOauth || isinvalidOauth) {
			response = JsonTestClient.doPost(url, headers, serializeToStringEntity(mapBody),consumer);
		}else {
			response = JsonTestClient.doPost(url, headers, serializeToStringEntity(mapBody));
		}
		
		return response;
	}
	
	/**
	 * This will do the JSON post request with duplicate request id and specified app id 
	 * @param url
	 * @param headerParams
	 * @param mapBody
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse postDupReq(String url, String[][] headerParams, HashMap<String,Object> mapBody,String requestId,String appId) throws Exception {
		
		headers = setHeaders(headerParams);	
		HttpResponse response;
		
		if(isOauth) {
			headers[2][1] = requestId;
		}else {
			headers[3][1] = requestId;
		}
		
		if(appId.length() != 0 && !isOauth) {
			headers[0][1] = appId;
		}
		
		if(isOauth || isinvalidOauth) {
			response = JsonTestClient.doPost(url, headers, serializeToStringEntity(mapBody),consumer);
		}else {
			response = JsonTestClient.doPost(url, headers, serializeToStringEntity(mapBody));
		}
		
		return response;
	}
	
	/**
	 * This will do the JSON post request with invalid request id
	 * 
	 * 
	 */
	public static HttpResponse postInvalidReq(String url, String[][] headerParams, HashMap<String,Object> mapBody,String[][] req) throws Exception {
		
		headers = setHeaders(headerParams);	
		HttpResponse response;
		
		for (int i=0; i< req.length ; i++) {
			if(isOauth) {
				headers[2][1] = req[i][1];
			}else {
				headers[3][1] = req[i][1];
		   }
		}
		
		if(	isOauth || isinvalidOauth) {
			response = JsonTestClient.doPost(url, headers, serializeToStringEntity(mapBody),consumer);
		}else {
			response = JsonTestClient.doPost(url, headers, serializeToStringEntity(mapBody));
		}
		
		return response;
	}
	
	
	/**
	 * This will do the JSON post request with Oauth authorization
	 * @param url
	 * @param headerParams
	 * @param mapBody
	 * @return
	 * @throws Exception
	 */
	/*public static HttpResponse postOauth(String url, String[][] headerParams, StringEntity body) throws Exception {
		TestUtil.auth = "oauth";
		request_Id = UUID.randomUUID().toString();
		intuit_tid = UUID.randomUUID().toString();
		headers = setHeaders(headerParams);
		HttpPost request = new HttpPost(url);
		// sign the request      
		consumer.sign(request);
		StringEntity input = new StringEntity(body);
		input.setContentType("application/json");
		request.setEntity(input);
		if(ifPassCompanyId){
			request.addHeader("Company-Id", "1019017762");
		}
		request.addHeader("Request-Id",request_Id );
		request.addHeader("intuit_tid",intuit_tid );
		request.addHeader("accept", "application/json");
		// send the request
		@SuppressWarnings({ "deprecation", "resource" })
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		HttpResponse response = JsonTestClient.doPost(url, headerParams, body, consumer);
		return response;
	}*/
	
	/**
	 * This will do the JSON post request with invalid Oauth authorization should throw 401
	 * @param url
	 * @param headerParams
	 * @param mapBody
	 * @return
	 * @throws Exception
	 */
	/*public static HttpResponse postInvalidOauth(String url, String[][] headerParams, String body) throws Exception {
		
		headers = setHeaders(headerParams);
		
		HttpResponse response = JsonTestClient.doPost(url, headerParams, body, consumer);
		
		return response;
	}*/
	
	/**
	 * This will do the JSON post request with Json string  
	 * @param url
	 * @param headerParams
	 * @param mapBody
	 * @return
	 * @throws Exception
	 */
	/*public static HttpResponse post(String url, String[][] headerParams, String body) throws Exception {
		headers = setHeaders(headerParams);		
		HttpResponse response = JsonTestClient.doPost(url, headers, new StringEntity(body, ContentType.create("application/json", "UTF-8")));
		return response;
	}*/
	
	/**
	 * This will do the Json get request
	 * @param url
	 * @param headerParams
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse get(String url, String[][] headerParams) throws Exception {
		headers = setHeaders(headerParams);		
		HttpResponse response;
		
		if(isOauth || isinvalidOauth) {
			response = JsonTestClient.doGet(url, headerParams, consumer);
		}else {
			response = JsonTestClient.doGet(url, headers);
		}
			
		return response;
	}
	
	/**
	 * This will do the Json get request with Oauth authorization
	 * @param url
	 * @param headerParams
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse getOauth(String url, String[][] headerParams) throws Exception {
		TestUtil.auth = "oauth";
		headers = setHeaders(headerParams);
		HttpGet request = new HttpGet(url);
		// sign the request      
		consumer.sign(request);
		request.addHeader("Company-Id", "1019017762");
		request.addHeader("accept", "application/json");
		// send the request
		@SuppressWarnings({ "deprecation", "resource" })
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		return response;
	}

	/**
	 * This will do the Json put request
	 * @param url
	 * @param headerParams
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse put(String url,String[][] headerParams,HashMap<String,Object> mapBody) throws Exception {
		headers = setHeaders(headerParams);	
		HttpResponse response;
		
		if(	isOauth || isinvalidOauth) {
			response = JsonTestClient.doPut(url, headers, serializeToStringEntity(mapBody),consumer);
		}else {
			response = JsonTestClient.doPut(url, headers, serializeToStringEntity(mapBody));
		}
		
		return response;
	}
	
	/**
	 * This will do the Json delete request
	 * @param url
	 * @param headerParams
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse delete(String url,String[][] headerParams ) throws Exception {
		headers = setHeaders(headerParams);		
		HttpResponse response = JsonTestClient.doDelete(url, headers);
		return response;
	}

	
	/**
	 * This will set the headers for the request
	 * @param headerParams
	 * @return
	 * @throws Exception
	 */
	public static String[][] setHeaders(String[][] headerParams) throws Exception{
		int j = (emulate !=null && emulate.equalsIgnoreCase("false"))? 3: 2;
		String[][] headers = null ;
		if(auth != null && (auth.equals("oauth") || auth.equals("invalidOauth")))  j = j - 1;
			
		if(headerParams == null){
			 headers = new String[j][2];
		}else{
			 headers = new String[headerParams.length+j][2];
		}
		
		//set the authorization in the header
		
		headers = setAuthorization(headers);
		
		if(isOauth || isinvalidOauth) {
			
			//set the intuit tid 
			headers[0][0] = "intuit_tid";
			intuit_tid = UUID.randomUUID().toString();
			headers[0][1] = intuit_tid;
		}else {
			//set the intuit tid 
			headers[1][0] = "intuit_tid";
			intuit_tid = UUID.randomUUID().toString();
			headers[1][1] = intuit_tid;
		}
		
		//populate the header parameters
		int i = (isOauth || isinvalidOauth)? 1 : 2;
		if(headerParams!=null){
			for(int l=0; l< headerParams.length; l++,i++){
				headers[i][0] = headerParams[l][0];
				if(headers[i][0].equalsIgnoreCase("Request-Id")){
					request_Id= UUID.randomUUID().toString();
					headers[i][1] = request_Id;
				}else{
					headers[i][1] = headerParams[l][1];
				}
					
			}
		}	
		
		//if emulate is false, then add emulate tag in the header to go through the test env instead of the emulator
		if(emulate !=null && emulate.equalsIgnoreCase("false")){
			headers[headers.length-1][0] = "emulation";
			headers[headers.length-1][1] = "emulate=no";

		}
		
		return headers;
	}
	
	/**
	 * This will read from the file and return the string
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String fileName) throws IOException {
		URL url = TestUtil.class.getClassLoader().getResource(fileName);
		byte[] buffer = new byte[(int) new File(url.getFile()).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(url.getFile()));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}
	
	/**
	 * This will take the map of map and serialize to string and then convert to StringEntity
	 * @param mapBody
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private static StringEntity serializeToStringEntity(HashMap<String, Object> mapBody) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		String body = objectMapper.writeValueAsString(mapBody);
		return new StringEntity(body, ContentType.create("application/json", "UTF-8"));
		
	}
	
	
	
	/**
	 * This will set the authorization in the header
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private static String[][] setAuthorization(String[][] headers) throws Exception{
		if(auth == null){
				headers[0][0] = "Authorization";
				headers[0][1] = "Intuit_IAM_Authentication intuit_appid="
						+ PropertyUtil.getValue(env
								+ ".appid")
						+ ", intuit_app_secret="
						+ PropertyUtil.getValue(env
								+ ".appsecret");
			
		}else if(auth.equalsIgnoreCase("apikey")){
			headers[0][0] = "Authorization";
			headers[0][1] = "Intuit_APIKey intuit_apikey="
					+ PropertyUtil.getValue(env
							+ ".apikey");
		}else if(auth.equalsIgnoreCase("invalidapikey")){
			headers[0][0] = "Authorization";
			headers[0][1] = "Intuit_APIKey intuit_apikey="
					+ PropertyUtil.getValue(env
							+ ".invalidapikey");
		}else if(auth.equalsIgnoreCase("oauth")) {
			isOauth = true;
			
			consumer = new CommonsHttpOAuthConsumer(PropertyUtil.getValue(env + ".consumerkey"),PropertyUtil.getValue(env + ".consumerSecret"));
		    consumer.setTokenWithSecret(PropertyUtil.getValue(env + ".token"), PropertyUtil.getValue(env + ".secret"));
			
		}else if(auth.equalsIgnoreCase("invalidOauth")) {
			isinvalidOauth = true;
			
			consumer = new CommonsHttpOAuthConsumer(PropertyUtil.getValue(env + ".invalidconsumerkey"),PropertyUtil.getValue(env + ".consumerSecret"));
		    consumer.setTokenWithSecret(PropertyUtil.getValue(env + ".token"), PropertyUtil.getValue(env + ".secret"));
			
		}else if (auth.equalsIgnoreCase("IAM")){
			throw new Exception("Auth type: "+auth +" is not supported. Please check TestUtil.setAuthorization for available auth types.");
		}else{
			throw new Exception("Auth type: "+auth +" is not supported. Please check TestUtil.setAuthorization for available auth types.");
		}
		
		return headers;
	}
	
	public static boolean compareJsonMap(HashMap<String, Object> map1, HashMap<String, Object> map2) throws JsonGenerationException, Exception, IOException{

		if(map1.size()!= map2.size()) return false;
		for(String s: map1.keySet()){
			if(!((map1.get(s)).toString()).equalsIgnoreCase((map2.get(s)).toString())) return false;
		}
		
		return true;
	}
	
	public static String serializeMap(HashMap<String, Object> map) throws Exception, JsonMappingException, IOException{
		
		return new ObjectMapper().writeValueAsString(map);
	}
	
	
	
	
}
