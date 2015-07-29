package com.intuit.paymentsapi.us;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;
import com.intuit.paymentsapi.us.BaseTest;

public class AuthTest extends BaseTest {

	static String origAuth = TestUtil.auth;

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result, Method method) throws Exception {
		TestUtil.auth = origAuth;

		String host = "";
		if (result.getStatus() == ITestResult.FAILURE) {

			if (TestUtil.env.contains("qa")) {
				host = "qa";

			} else if (TestUtil.env.contains("ptc")) {
				host = "ptc";
			} else if (TestUtil.env.contains("sbx")) {
				host = "sbx";
			} else {
				throw new Exception("Env not supported.");
			}

			System.out.println("mvn clean install -Dtest="
					+ this.getClass().getName().split("us.")[1] + "#"
					+ method.getName() + " -P " + TestUtil.env);
			System.out.println("SPLUNK URL is:");
			System.out
					.println("https://splunk.qbms.intuit.com/en-US/app/qbms_search/search?q=search "
							+ URLEncoder
									.encode("index=\"preprod-vhost-transaction\" AND tag::host="
											+ host + " " + TestUtil.intuit_tid)
							+ "&earliest=-15m&latest=now");

		}
	}

	private String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", "" }

	};
	
	String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }, };

	String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() } };

	private String CCChargeUrl = buildUrl(CHARGE_CC);

	@DataProvider(name = "cc-auth-error-codes")
	public Object[][] ccAuthErrorData() throws Exception {
		return new Object[][] { { new String[][] { { "name", "emulate=10200" },

		} }, { new String[][] { { "name", "emulate=10201" },

		} }, { new String[][] { { "name", "emulate=10500" },

		} } };
	}

	/**
	 * This will test that cc auth with more than zero dollars should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		Assert.assertEquals(result.get("capture"), false);
		Assert.assertEquals(result.get("currency"), "USD");
		Assert.assertEquals(result.get("amount").toString(), "0.01");
	}

	/**
	 * This will test that cc auth with zero dollars should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCAuthZeroDollars() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);
		mapBody.put("description", "description for cc auth test");

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateAuthRes(response, 201, Status.AUTHORIZED.toString());

	}

	/**
	 * This will test that CC auth with vault should succeed
	 * 
	 * @throws Exception
	 */
	// @Test
	public void testCCAuthwithVaultid() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.05);
		mapBody.put("currency", "USD");
		mapBody.put("card_on_file", "");
		mapBody.put("capture", false);

		// TODO: token service is down. run when it's fixed
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateAuthRes(response, 201, Status.AUTHORIZED.toString());

	}

	/**
	 * This will test CC auth with cardcontext including tax should succeed
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCAuthWithCardContext() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		mapCardContext.put("tax", "0.02");
		// mapCardContext.put("sender_account_id", "sender_account_id");

		mapBody.put("context", mapCardContext);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String actualTax = ((HashMap<String, Object>) result.get("context"))
				.get("tax").toString();
		Assert.assertEquals(actualTax, "0.02");
		// String actualSenderAccountid = ((HashMap<String, Object>)
		// result.get("context")).get("sender_account_id").toString();
		// Assert.assertEquals(actualSenderAccountid,"sender_account_id" );
	}

	/**
	 * This will test that CC auth with device info should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCAuthWithDeviceInfo() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		mapCardContext.put("deviceInfo", deviceInfo);
		mapBody.put("context", mapCardContext);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());
		HashMap<String, Object> actualDeviceInfo = ((HashMap<String, Object>) ((HashMap<String, Object>) result
				.get("context")).get("deviceInfo"));
		deviceInfo.remove("encrypted");
		Assert.assertEquals(TestUtil.compareJsonMap(actualDeviceInfo,
				deviceInfo), true);

	}
	
	/**
	 * This will test that CC auth with description should succeed
	 * @throws Exception
	 */
	@Test
	public void testCCAuthDescription() throws Exception {
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);
		
		String description = "Auth 1.00 US Dollar";
		mapBody.put("description", description);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String authId = (String) result.get("id");
		
		String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid }};
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, authId);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(retrievalMap.get("description").toString(),description);
	}

	/**
	 * This will test that cc auth with one time CC token should succeed
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "token" })
	public void testCCAuthOneTimeTokenCC() throws Exception {
		TestUtil.auth = "apikey";

		String token = generateOneTimeToken(returnMapCard());
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("token", token);
		mapBody.put("capture", false);
		TestUtil.auth = origAuth;

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		Assert.assertEquals(result.get("token"), token);
		Assert.assertEquals(result.get("capture"), false);
		Assert.assertEquals(result.get("currency"), "USD");
		Assert.assertEquals(result.get("amount").toString(), "0.01");
	}

	/**
	 * This will test that cc auth with one time CC token that is created 14 min
	 * should succeed
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "token", "time-consuming" })
	public void testCCAuthOneTimeTokenCCAfter14min() throws Exception {

		TestUtil.auth = "apikey";

		String token = generateOneTimeToken(returnMapCard());
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("token", token);
		mapBody.put("capture", false);
		TestUtil.auth = origAuth;

		System.out.println("Waiting for 14 min...");
		Thread.sleep(60 * 14 * 1000);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		Assert.assertEquals(result.get("token"), token);
		Assert.assertEquals(result.get("capture"), false);
		Assert.assertEquals(result.get("currency"), "USD");
		Assert.assertEquals(result.get("amount").toString(), "0.01");
	}

	/*
	 * *****************************************************
	 * CC Auth Negative Test Cases
	 * *****************************************************
	 */

	/**
	 * This will test that cc auth with same one time CC token twice should
	 * throw error
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "token" })
	public void testCCAuthOneTimeTokenCCTwice() throws Exception {
		TestUtil.auth = "apikey";

		String token = generateOneTimeToken(returnMapCard());
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("token", token);
		mapBody.put("capture", false);
		TestUtil.auth = origAuth;

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		response = TestUtil.post(CCChargeUrl, CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"Token is invalid.", "Token",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

	/**
	 * This will test that cc auth with invalid one time CC token should throw
	 * error
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "token" })
	public void testCCAuthInvalidOneTimeTokenCC() throws Exception {
		String token = "lucy1234";

		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("token", token);
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"Token is invalid.", "Token",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * This will test that cc auth with one time CC token that is expired after
	 * 15 min should throw error
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "token", "time-consuming" })
	public void testCCAuthOneTimeTokenCCAfter15min() throws Exception {
		TestUtil.auth = "apikey";

		String token = generateOneTimeToken(returnMapCard());
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("token", token);
		mapBody.put("capture", false);
		TestUtil.auth = origAuth;

		System.out.println("Waiting for token to expire after 15 min...");
		Thread.sleep(60 * 15 * 1000);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"Token is invalid.", "Token",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

	/**
	 * This will test that CC Auth fail for long string for any field Error code
	 * is 10304
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCAuthErrorCode10304() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();

		String[][] params = { { "name", "emulate=10304" }, { "cvc", "123456" } };

		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", updateMapCard(params));
		HashMap<String, Object> mapContext = new HashMap<String, Object>();
		mapBody.put("context", mapContext);
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);

		validateErrorRes(response, "PMT-4000");
	}

	/**
	 * This will test that CC Auth fail for passing wrong type into the field
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCAuthErrorCode10306() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();

		String[][] params = { { "name", "emulate=10306" } };

		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", updateMapCard(params));
		mapBody.put("capture", "string");

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);

		validateErrorRes(response, "PMT-4000");
	}

	/**
	 * This will test that CC Auth fail for special char like *
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCAuthErrorCode10309() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();

		String[][] params = { { "name", "emulate=10309" }, { "number", "****" } };

		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", updateMapCard(params));
		mapBody.put("capture", false);

		HashMap<String, Object> mapContext = new HashMap<String, Object>();

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);

		validateErrorRes(response, "PMT-4000");
	}

	/**
	 * This will test all the error codes reproducible only by emulator for CC
	 * Auth
	 * 
	 * @throws Exception
	 */
	@Test(dataProvider = "cc-auth-error-codes")
	public void testCCAuthErrorCodes(String[][] testData) throws Exception {
		if (TestUtil.emulate == null
				|| TestUtil.emulate.equalsIgnoreCase("true")) {
			HashMap<String, Object> mapBody = new HashMap<String, Object>();
			mapBody.put("amount", 0.01);
			mapBody.put("currency", "USD");
			mapBody.put("card", updateMapCard(testData));
			mapBody.put("capture", false);

			HttpResponse response = TestUtil.post(CCChargeUrl,
					CCChargeHeaderParams, mapBody);
			validateErrorRes(response, "PMT-6000");
		}

	}

	/**
	 * 
	 * This will test void(full refund) auth should success.
	 * 
	 */
	@Test
	public void testCCVoidAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		String CCChargeUrl = buildUrl(CHARGE_CC);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResp = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());
		Assert.assertEquals(authResp.get("currency"), "USD");
		Assert.assertEquals(authResp.get("amount").toString(), "10.00");

		// Then void this charge transaction
		String authId = (String) authResp.get("id");

		String voidUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 10.00);

		HttpResponse voidResponse = TestUtil.post(voidUrl,
				CCRefundHeaderParams, refundBody);
		validateRefundRes(voidResponse, 201, Status.ISSUED.toString());

		// Auth status should be CANCELLED
		String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid } };
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, authId);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.CANCELLED.getText(),
				retrievalMap.get("status"));
	}

	/**
	 * 
	 * This will test refund auth should fail.
	 * 
	 */
	@Test
	public void testCCRefundAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		String CCChargeUrl = buildUrl(CHARGE_CC);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResp = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());
		Assert.assertEquals(authResp.get("currency"), "USD");
		Assert.assertEquals(authResp.get("amount").toString(), "10.00");

		// Then void this charge transaction
		String authId = (String) authResp.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5.00);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, 400, "invalid_request", "PMT-4000",
				"chargeId is invalid.", "chargeId",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

	/**
	 * 
	 * This will test auth then void it twice should fail.
	 * 
	 */
	@Test
	public void testCCVoidAuthTwice() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		String CCChargeUrl = buildUrl(CHARGE_CC);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResp = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());
		Assert.assertEquals(authResp.get("currency"), "USD");
		Assert.assertEquals(authResp.get("amount").toString(), "10.00");

		// Then void this charge transaction
		String authId = (String) authResp.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 10.00);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		// Auth status should be CANCELLED
		String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid } };
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, authId);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.CANCELLED.getText(),
				retrievalMap.get("status"));

		refundResponse = TestUtil.post(CCRefundUrl, CCRefundHeaderParams,
				refundBody);
		validateErrorRes(refundResponse, 400, "invalid_request", "PMT-4000",
				"chargeId is invalid.", "chargeId",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * This will test the workflow: Auth->Capture->Call void auth->refund should fail
	 * 
	 * 
	 */
	@Test
	public void testCCRefundAfterVoidAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String authId = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, authId);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 10.00);
		
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulCaptureRes(capture_response);
		String captureId = captureResponse.get("id").toString();
		
		//Void the auth
		String CCVoidAuthUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> voidAuthBody = new HashMap<String, Object>();
		voidAuthBody.put("amount", 10.00);

		HttpResponse voidAuthResponse = TestUtil.post(CCVoidAuthUrl,
				CCRefundHeaderParams, voidAuthBody);
		validateRefundRes(voidAuthResponse, 201, Status.ISSUED.toString());
		
		//Refund the capture
		String refundUrl = buildUrl(REFUND_CC, realmid, captureId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 2.00);

		HttpResponse refundResponse = TestUtil.post(refundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, 400, "invalid_request", "PMT-4000",
				"chargeId is invalid.", "chargeId",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * 
	 * This will test the workflow: Auth->Capture->refund using
	 * capture id->Call void auth->should fail
	 * 
	 */
	@Test
	public void testCCVoidAuthAfterRefund() throws Exception {
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String authId = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, authId);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 10.00);
		
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulCaptureRes(capture_response);
		String captureId = captureResponse.get("id").toString();
		
		//Refund using capture id
		String refundUrl = buildUrl(REFUND_CC, realmid, captureId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 2.00);

		HttpResponse refundResponse = TestUtil.post(refundUrl,
				CCRefundHeaderParams, refundBody);
		
		validateRefundRes(refundResponse, 201, Status.ISSUED.toString());
		
		//Void auth should fail
		String CCVoidAuthUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> voidAuthBody = new HashMap<String, Object>();
		voidAuthBody.put("amount", 10.00);

		HttpResponse voidAuthResponse = TestUtil.post(CCVoidAuthUrl,
				CCRefundHeaderParams, voidAuthBody);
		validateErrorRes(voidAuthResponse, 400, "invalid_request", "PMT-4000",
				"request is invalid.", "request",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * 
	 * This will test the workflow: Auth->Capture->Void capture id->void auth->should fail
	 * 
	 */
	@Test
	public void testCCVoidAuthAfterVoidCapture() throws Exception {
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String authId = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, authId);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 10.00);
		
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulCaptureRes(capture_response);
		String captureId = captureResponse.get("id").toString();
		
		//Void using capture id
		String refundUrl = buildUrl(REFUND_CC, realmid, captureId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 10.00);

		HttpResponse refundResponse = TestUtil.post(refundUrl,
				CCRefundHeaderParams, refundBody);
		
		validateRefundRes(refundResponse, 201, Status.ISSUED.toString());
		
		//Void auth should fail
		String CCVoidAuthUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> voidAuthBody = new HashMap<String, Object>();
		voidAuthBody.put("amount", 10.00);

		HttpResponse voidAuthResponse = TestUtil.post(CCVoidAuthUrl,
				CCRefundHeaderParams, voidAuthBody);
		validateErrorRes(voidAuthResponse, 400, "invalid_request", "PMT-4000",
				"chargeId is invalid.", "chargeId",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test the workflow: Auth->Void auth->Capture should fail
	 * 
	 * 
	 */
	@Test
	public void testCCCaptureVoidAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		String CCChargeUrl = buildUrl(CHARGE_CC);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResp = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());
		Assert.assertEquals(authResp.get("currency"), "USD");
		Assert.assertEquals(authResp.get("amount").toString(), "10.00");

		// Then void this auth transaction
		String authId = (String) authResp.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, authId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 10.00);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateRefundRes(refundResponse, 201, Status.ISSUED.toString());
		
		//Then capture the auth should fail
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, authId);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 10.00);

		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(capture_response, 400, "invalid_request", "PMT-4000",
				"chargeId is invalid.", "chargeId",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}
}
