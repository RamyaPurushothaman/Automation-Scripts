package com.intuit.paymentsapi.us;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

@Test(groups = {"cto-simpleauth"})
public class AuthorizationTest extends BaseTest{
	
	static String origAuth;
	private String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() } };
	private String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }};
	private String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid }};
	public static String[][] CCChargeHeaderParams = { { "Company-Id", realmid }, 
		{"Request-Id", UUID.randomUUID().toString()}

};
	
	@AfterMethod(alwaysRun=true)
	public void tearDown(ITestResult result, Method method) throws Exception {
		TestUtil.auth = null;
		TestUtil.isOauth = false;
		
		String host="";
	   if (result.getStatus() == ITestResult.FAILURE) {
		  
		  if(TestUtil.env.contains("qa")){
			  host = "qa";
			  
		  }else if(TestUtil.env.contains("ptc")){
			  host = "ptc";
		  }else if(TestUtil.env.contains("sbx")) {
			  host = "sbx";
		  }else{
			  throw new Exception("Env not supported.");
		  }

		  System.out.println("mvn clean install -Dtest="+this.getClass().getName().split("us.")[1]+"#"+method.getName()+" -P "+TestUtil.env);
		  System.out.println("SPLUNK URL is:");
		  System.out.println("https://splunk.qbms.intuit.com/en-US/app/qbms_search/search?q=search "+URLEncoder.encode("index=\"preprod-vhost-transaction\" AND tag::host="+host+" "+TestUtil.intuit_tid)+"&earliest=-15m&latest=now");      		  

	   }  
	}
	
	/**
	 * This will test that using simple auth for token api should work both CTO and transaction vhost
	 * @throws Exception
	 */
	@Test
	public void testApiKeyToken() throws Exception {
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("card", returnMapCard());
		HttpResponse response = TestUtil.post(TokenTest.tokenUrl,
				TokenTest.tokenHeaderParams, mapBody);
		
		//validateSuccessfulTokenRes(response);
		
		if(TestUtil.env.startsWith("cto")){
			HashMap<String,Object> tokenRes = validateSuccessfulTokenRes(response);
			
			TestUtil.auth = "oauth";
			
			HashMap<String, Object> chargeBody = new HashMap<String, Object>();
			chargeBody.put("amount", 1.00);
			chargeBody.put("token",tokenRes.get("value").toString());
			chargeBody.put("currency", "USD");
			HttpResponse chargeTokenRes = TestUtil.post(ChargeTest.CCChargeUrl,
					CCChargeHeaderParams, chargeBody);
			validateChargeRes(chargeTokenRes, 201, Status.CAPTURED.toString());
			
		}else{
			validateIfIsAuthorizationError(response);
		}
		
	}
	
	/**
	 * This test that using simple auth for charge endpoint should fail with 403 unsupported authentication error.
	 * @throws Exception
	 */
	@Test
	public void testApiKeyCharge() throws Exception{
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		HttpResponse response = TestUtil.post(ChargeTest.CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
	}
	
	
	/**
	 * This test that using simple auth for auth endpoint should fail with 403 authorization error
	 * @throws Exception
	 */
	@Test
	public void testApiKeyAuth() throws Exception{
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);
		HttpResponse response = TestUtil.post(ChargeTest.CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());

	}
	
	/**
	 * This test that using simple auth for capture endpoint should fail with 403 authorization error
	 * @throws Exception
	 */
	@Test
	public void testApiKeyCapture() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);
		HttpResponse response = TestUtil.post(ChargeTest.CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) map.get("id");
		
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
		
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 0.01);

		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);	
		
		Assert.assertEquals(403, capture_response.getStatusLine().getStatusCode());

	}
	
	/**
	 * This test that using simple auth for refund endpoint should fail with 403 authorization error
	 * @throws Exception
	 */
	@Test
	public void testApiKeyRefund() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse response = TestUtil.post(ChargeTest.CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201, Status.CAPTURED.toString());

		String chargeid = (String) map.get("id");
		
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
		
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 0.01);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);	
		
		Assert.assertEquals(403, refundResponse.getStatusLine().getStatusCode());

	}
	
	/**
	 * This test that using simple auth for retrieval endpoint should fail with 403 authorization error
	 * @throws Exception
	 */
	@Test
	public void testApiKeyRetrieval() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse response = TestUtil.post(ChargeTest.CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201, Status.CAPTURED.toString());

		String chargeid = (String) map.get("id");
		
		TestUtil.auth = "apikey";
		
		String CCRetrieveUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrieveResponse = TestUtil.get(CCRetrieveUrl,
				CCRetrievalHeaderParams);	
		
		Assert.assertEquals(403, retrieveResponse.getStatusLine().getStatusCode());

	}
	
	/**
	 * This test that using simple auth for retrieval endpoint should fail with 403 Unsupported authorization error
	 * @throws Exception
	 */
	@Test
	public void testApiKeyLists() throws Exception{
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
		
		String CCRetrievalUrl = buildUrl(RETRIEVAL_LIST_CC, realmid);
		HttpResponse listResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		
		Assert.assertEquals(403, listResponse.getStatusLine().getStatusCode());

	}
	
	
	/**
	 * This test that using invalid api key for token api should throw 401 
	 * @throws Exception
	 */
	@Test
	public void testInvalidApiKeyToken() throws Exception {
		origAuth = TestUtil.auth;
		TestUtil.auth = "invalidapikey";
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("card", returnMapCard());
		HttpResponse response = TestUtil.post(TokenTest.tokenUrl,
				TokenTest.tokenHeaderParams, mapBody);
		
		Assert.assertEquals(401, response.getStatusLine().getStatusCode());
	}

}
