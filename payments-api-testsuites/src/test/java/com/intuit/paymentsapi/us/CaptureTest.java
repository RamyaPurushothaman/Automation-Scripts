package com.intuit.paymentsapi.us;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class CaptureTest extends BaseTest {

	private String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }

	};

	private String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }, };

	private String CCChargeUrl = buildUrl(CHARGE_CC);

	// /////////////////////Happy Path//////////////////////////////

	/**
	 * This will auth first then capture the auth should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCCapture() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		String Description = "Capture 1.00 US Dollar";
		captureBody.put("amount", 1.00);
		captureBody.put("description", Description);

		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulCaptureRes(capture_response);
		Assert.assertEquals("1.00", captureResponse.get("amount").toString());
		Assert.assertEquals(Description, ((HashMap<String,Object>)captureResponse.get("captureDetail")).get("description").toString());
	}

	/**
	 * This will auth first then capture more amount should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCCaptureMoreThanAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 2.00);

		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulCaptureRes(capture_response);
		Assert.assertEquals("2.00", ((HashMap<String,Object>)captureResponse.get("captureDetail")).get("amount").toString());
	}

	/**
	 * This will test capture with card context should succeed
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCCaptureWithContext() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResult = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) chargeResult.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapContext = new HashMap<String, Object>();
		//mapContext.put("tax", 0.5);
		//mapContext.put("sender_account_id", "John@intuit.com");

		captureBody.put("amount", 1.00);
		captureBody.put("context", mapContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponseMap = validateSuccessfulCaptureRes(captureResponse);

		Assert.assertEquals("1.00", ((HashMap<String,Object>)captureResponseMap.get("captureDetail")).get("amount").toString());
		//Assert.assertEquals("0.50", (((HashMap<String,Object>)((HashMap<String,Object>)captureResponseMap.get("captureDetail")).get("context"))).get("tax").toString());
		/*Assert.assertEquals("John@intuit.com",
				((HashMap<String, Object>) captureResponseMap.get("context"))
						.get("sender_account_id"));*/

	}

	/**
	 * This will test that Capture CC with device info should succeed
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	@Test
	public void testCCCaptureWithDeviceInfo() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) result.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 1.00);
		captureBody.put("context", mapCardContext);

		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResult = validateSuccessfulCaptureRes(capture_response);

		HashMap<String, Object> actualDeviceInfo = (HashMap<String,Object>)((((HashMap<String,Object>)((HashMap<String,Object>)captureResult.get("captureDetail")).get("context"))).get("deviceInfo"));
		
		deviceInfo.remove("encrypted");
		Assert.assertEquals(true,
				TestUtil.compareJsonMap(actualDeviceInfo, deviceInfo));

	}

	/**
	 * This test that capturing twice after one auth should succeed.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCMultipleCapture() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");

		// First Capture
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 3.00);

		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureMap = validateSuccessfulCaptureRes(captureResponse);
		Assert.assertEquals("3.00", ((HashMap<String,Object>)captureMap.get("captureDetail")).get("amount").toString());

		// Second Capture
		captureBody.put("amount", 2.00);
		captureResponse = TestUtil.post(CCCaptureUrl, CCCaptureHeaderParams,
				captureBody);
		
		//Should throw error after code change
		validateErrorRes(captureResponse,400,"invalid_request","PMT-4000","chargeId is invalid.", "chargeId", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

	/**
	 * Capture with blank amount should succeed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureNull() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", "");

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateSuccessfulCaptureRes(captureResponse);
	}
	
	/**
	 * Auth for $100 and do partial capture of $10 
	 * Expected Response - Capture of $10 success
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCCaptureLessThanAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 100.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 10.00);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureMap = validateSuccessfulCaptureRes(captureResponse);
		Assert.assertEquals("10.00", ((HashMap<String, Object>) captureMap
				.get("captureDetail")).get("amount").toString());
	}

	// //////////////Unhappy Path////////////////////////
	/**
	 * Capturing with invalid charge id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureInvalidChargeId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		// Invalid charge id
		String chargeid = "EMUInvalid";
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 1);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);

		validateErrorRes(captureResponse, "PMT-4000");
	}

	/**
	 * Capture the zero dollar should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureZero() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 0);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);

		validateErrorRes(captureResponse, "PMT-4000");
	}

	/**
	 * Capture the negative dollar should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureNegtive() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", -1);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);

		validateErrorRes(captureResponse, "PMT-4000");
	}

	/**
	 * Capture with the already charged transaction should fail.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testCCDupCapture() throws Exception {
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201, Status.CAPTURED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 1.00);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse,400,"invalid_request","PMT-4000","chargeId is invalid.", "chargeId", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

	/**
	 * Capture with the long description which is longer than 4000 characters
	 * should fail
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongDescription() throws Exception {

		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 1.00);

		String Description = null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4001; i++) {
			sb.append("a");
		}
		Description = sb.toString();
		captureBody.put("description", Description);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}
	
	/**
	 * Auth $10 --> Failed Auth --> Capture $10
	 * Expected Response - PMT-6000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureWithFailedAuth() throws Exception {
		String[][] emulatedHeaderParams = {{"Company-Id", realmid},
				{"Request-Id", UUID.randomUUID().toString()},
				{"emulation", "emulate=10401"}};

		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl, emulatedHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.DECLINED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 10.00);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl, emulatedHeaderParams,
				captureBody);
		validateErrorRes(captureResponse,400,"invalid_request","PMT-4000","chargeId is invalid.", "chargeId", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * Auth $10 --> Capture $10 (add deviceId greater than 40 characters)
	 * Expected Response - PMT-4000 
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureWithLongDeviceId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("id",
				"00000000000000000000000000000000000000000000001234");
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}
	
	/**
	 * Auth $10 --> Capture $10 (add deviceType greater than 200 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongDeviceType() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		String deviceType = null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 201; i++) {
			sb.append("a");
		}
		deviceType = sb.toString();
		deviceInfo.put("type", deviceType);
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}
	
	/**
	 * Auth $10 --> Capture $10 (add macAddress greater than 60 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongMacAddress() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();

		String macAddress = null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 61; i++) {
			sb.append("a");
		}
		macAddress = sb.toString();
		deviceInfo.put("macAddress", macAddress);
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");

	}
	
	/**
	 * Auth $10 --> Capture $10 (add latitude greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongLatitude() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("latitude",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}
	
	/**
	 * Auth $10 --> Capture $10 (add longitude greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongLongitude() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("longitude",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}
	
	/**
	 * Auth $10 --> Capture $10 (add phoneNumber greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongPhoneNumber() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("phoneNumber",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}
	
	/**
	 * Auth $10 --> Capture $10 (add ipAddress greater than 40 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCCaptureLongIPAddress() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201,
				Status.AUTHORIZED.toString());

		String chargeid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("ipAddress",
				"123456789012345678901234567890123456789012");
		mapCardContext.put("deviceInfo", deviceInfo);

		captureBody.put("amount", 10.00);
		captureBody.put("context", mapCardContext);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateErrorRes(captureResponse, "PMT-4000");
	}

	/**
	 * Auth for $100 and two or more thread should trying to Capture at the same
	 * time <br/>
	 * Exception: only one should succeed and other two should fail to capture
	 * it
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test(enabled = false)
	public void testCCCaptureMoreThanTwoThread() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 100.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);

		HashMap<String, Object> authResponseMap = validateAuthRes(response,
				201, Status.AUTHORIZED.toString());

		// Refund execution with multiple thread
		String chargeId = (String) authResponseMap.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeId);
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 100.00);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		List<Future<HttpResponse>> captureResponseList = new ArrayList<Future<HttpResponse>>();

		Capture capture01 = new Capture(captureBody, CCCaptureUrl,
				CCCaptureHeaderParams);
		Capture capture02 = new Capture(captureBody, CCCaptureUrl,
				CCCaptureHeaderParams);
		Capture capture03 = new Capture(captureBody, CCCaptureUrl,
				CCCaptureHeaderParams);

		Future<HttpResponse> res01 = executor.submit(capture01);
		Future<HttpResponse> res02 = executor.submit(capture02);
		Future<HttpResponse> res03 = executor.submit(capture03);

		// Wait for all thread completes its execution
		executor.shutdown();
		executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

		captureResponseList.add(res01);
		captureResponseList.add(res02);
		captureResponseList.add(res03);

		int count = 0;
		for (Future<HttpResponse> future : captureResponseList) {
			HttpResponse captureResponse = future.get();

			if (captureResponse.getStatusLine().getStatusCode() == 200) {
				HashMap<String, Object> captureResponseMap = validateSuccessfulCaptureRes(captureResponse);
				Assert.assertEquals("100.00",
						((HashMap<String, Object>) captureResponseMap
								.get("captureDetail")).get("amount").toString());
				count++;
			} else {
				validateErrorRes(captureResponse, "PMT-4000");
			}
		}
		// Verify that only one thread should success to validate
		Assert.assertEquals(Integer.valueOf(count), Integer.valueOf(1));
	}
}

/**
 * Thread for invoke Capture operation
 */
class Capture implements Callable<HttpResponse> {

	private HashMap<String, Object> captureBody;
	private String CCCaptureUrl;
	private String[][] CCCaptureHeaderParams;

	public Capture(HashMap<String, Object> captureBody, String CCCaptureUrl,
			String[][] CCCaptureHeaderParams) {
		this.captureBody = captureBody;
		this.CCCaptureUrl = CCCaptureUrl;
		this.CCCaptureHeaderParams = CCCaptureHeaderParams;
	}

	@Override
	public HttpResponse call() throws Exception {
		HttpResponse captureResponse;
		captureResponse = TestUtil.post(CCCaptureUrl, CCCaptureHeaderParams,
				captureBody);
		return captureResponse;
	}
}
