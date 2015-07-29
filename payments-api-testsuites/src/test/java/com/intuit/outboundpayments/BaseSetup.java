
package com.intuit.outboundpayments;

import com.intuit.paymentsapi.us.BaseTest;
import com.intuit.paymentsapi.util.PropertyUtil;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

/*
 * @author katyayani_vaddadi@intuit.com
 */

public class BaseSetup extends BaseTest {

	protected static String baseUrl;
	protected static String appId;
	protected static String appSecret;
	protected static String CONTENT_TYPE = ContentType.JSON + ";charset=UTF-8";
	protected static String authorizationHeader;
	protected static String companyIdHeader = realmid;
	
	public static void setup(){
		String env = System.getenv("vhost");
		//FIXME: once we find a common set of urls for ctogateway, we should refactor this
		if(env.contains("cto")){
			baseUrl = PropertyUtil.getValue(env + "." + "swaggerurl");
		}else{
			baseUrl = PropertyUtil.getValue(System.getenv("vhost") + "." + "url");
		}
		appId = PropertyUtil.getValue(env + "." + "appid");
		appSecret = PropertyUtil.getValue(env + "." + "appsecret");
		authorizationHeader = "Intuit_IAM_Authentication intuit_appid="+appId+", intuit_app_secret="+appSecret;
		RestAssured.baseURI = baseUrl;
	}
}