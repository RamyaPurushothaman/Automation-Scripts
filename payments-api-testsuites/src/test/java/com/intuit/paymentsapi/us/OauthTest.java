package com.intuit.paymentsapi.us;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.PropertyUtil;
import com.intuit.paymentsapi.util.TestUtil;

@Test(groups = {"cto"})
public class OauthTest extends BaseTest {

	static String origAuth;
	//Passby CTO and hit our QA env
	String uri = buildUrl(CHARGE_CC);
	
	String chargeBody = "{\"amount\":\"10.55\",\"card\":{\"expYear\":\"2020\",\"expMonth\":\"02\",\"address\":{\"region\":\"CA\",\"postalCode\":\"94086\",\"streetAddress\":\"1130 Kifer Rd\",\"country\":\"US\",\"city\":\"Sunnyvale\"},\"name\":\"emulate=0\",\"cvc\":\"123\",\"number\":\"4111111111111111\"},\"currency\":\"USD\"}";
	
	public String[][] CCChargeHeaderParams = { { "Company-Id", realmid }, 
		{"Request-Id", UUID.randomUUID().toString()}

	};
	
	String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid }};
	
	String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
										{ "Request-Id", UUID.randomUUID().toString() }
	};

	@AfterMethod(alwaysRun=true)
	public void tearDown(ITestResult result, Method method) throws Exception {
		TestUtil.auth = null;
		TestUtil.isinvalidOauth = false;
		TestUtil.ifPassCompanyId = false;
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

	public static String tokenUrl = buildUrl(CREATE_TOKEN);

	/**
	 * This will test charge using Oauth authorization should succeed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCharge() throws Exception {

		TestUtil.auth = "oauth";
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse response = TestUtil.post(uri, CCChargeHeaderParams, mapBody);
		validateChargeRes(response, 201, Status.CAPTURED.toString());

	}

	/**
	 * This will test list charge records using Oauth authorization should
	 * succeed.
	 * 
	 * @throws Exception
	 */
	@Test(groups = {"chargeListTest"})
	public void testListCharges() throws Exception {
		
		TestUtil.auth = "oauth";
		
		String[][] header = {{"Company-Id",""}};
		if(TestUtil.isSBX) {
			header[0][1] = PropertyUtil.getValue(SBX_REALM_ID);;
		}else {
			header[0][1] = PropertyUtil.getValue(REALM_ID);;
		}
		HttpResponse response = TestUtil.get(uri, header);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
	}

	/**
	 * This will test retrieval one charge record based on charge id using Oauth
	 * authorization should succeed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargeRetrieval() throws Exception {
		
		TestUtil.auth = "oauth";
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse response = TestUtil.post(uri, CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeRes = validateChargeRes(response, 201, Status.CAPTURED.toString());

		
		String chargeId = chargeRes.get("id").toString();
		String retrieUrl = uri + "/" + chargeId;
		
		response = TestUtil.get(retrieUrl, CCRetrievalHeaderParams);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
	}

	/**
	 * This will test auth and capture using Oauth authorization should succeed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAuthCapture() throws Exception {
		
		TestUtil.auth = "oauth";
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);
		
		HttpResponse response = TestUtil.post(uri, CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) map.get("id");

		// Capture the auth
		String captureUrl = uri + "/" + chargeid + "/capture";
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 0.01);
		
		response = TestUtil.post(captureUrl, CCChargeHeaderParams, captureBody);
		validateSuccessfulCaptureRes(response);
	}

	/**
	 * This will test refund using Oauth authorization should succeed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRefund() throws Exception {
		
		TestUtil.auth = "oauth";
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse response = TestUtil.post(uri, CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201, Status.CAPTURED.toString());

		String chargeid = (String) map.get("id");
		
		//Refund the charge
		String refundUrl = uri + "/" + chargeid + "/refunds";
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 0.01);
		
		response = TestUtil.post(refundUrl, CCRefundHeaderParams, refundBody);
		validateRefundRes(response, 201, Status.ISSUED.toString());
	}
	
	/**
	 * Test that creating a token with card number should suceed
	 * @throws Exception
	 */
	@Test(enabled = false)
	public void testCreateCCToken() throws Exception{
		
		TestUtil.auth = "oauth";
		String[][] tokenHeaderParams = {};
		
		HashMap<String,Object> card = new HashMap<String,Object>();
		card.put("card", returnMapCard());
		
		HttpResponse response = TestUtil.post(tokenUrl,
				tokenHeaderParams, card);
		validateSuccessfulTokenRes(response);
	}
	
	
	/**
	 * This will test charge using a invalid Oauth authorization should fail and throw 401.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargeInvalidOauth() throws Exception {
		
		TestUtil.auth = "invalidOauth";
		
		HashMap<String, Object> chargeBody = new HashMap<String, Object>();
		chargeBody.put("amount", 0.01);
		chargeBody.put("currency", "USD");
		chargeBody.put("card", returnMapCard());
		
		HttpResponse response = TestUtil.post(uri, CCChargeHeaderParams, chargeBody);
		Assert.assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	/**
	 * This will test that charging with company_id should succeed
	 * @throws Exception
	 */
	@Test
	public void testChargeWithCompanyId() throws Exception{
		
		TestUtil.auth = "oauth";
		TestUtil.ifPassCompanyId = true;
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse response = TestUtil.post(uri, CCChargeHeaderParams, mapBody);
		validateChargeRes(response, 201, Status.CAPTURED.toString());


	}

}
