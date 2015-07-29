package com.intuit.paymentsapi.us;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.intuit.paymentsapi.util.TestUtil;


public class RevelIntegrationTest extends BaseTest {
	private String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() } };

	private String[][] CCRefundRetrieveHeader = { { "Company-Id", realmid } };
	private String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() } };

	private String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() } };

	private String CCChargeUrl = buildUrl(CHARGE_CC);
	// private String track1 =
	// "FCF733EA4D10009FD335681E361297394DE9A623523141D7DE7EBB1DA37F5414752785573296E46DCA1BE4341D7734538A9BCD4CC0C5B1F31A895FBAFE29B57373D104E9A5D3427F3B3A4F3DBB9D0D79763E6E14D95DC7D3AEF89D633FCB25AF";
	private String track1 = "878D2608D6D42F687FD25081EDBF0183D9AB3EB9783FFF53A55A801525996E52921B23199EBEC968A0860AAD256F96199CDC64B44C9073FD3AB833D8A0FDBD7BAA538E35E29B5C525F4056FA005666BFC4A9690F25B5BAFACBEB69E8396620A7D640DAD87CB1D53E56AA547CF14A43EA";
	private String track2 = "";
	// private String ksn = "FFFF490036149D400001";
	private String ksn = "FFFF9876540005000001";
	private String pinBlock = "D1B28DCFE795A7A9";

	@BeforeMethod
	public void beforeMethod(Method m) {
		System.out.println("RUNNING this test method  :  " + m.getName());
	}

	/**
	 * Map Device info for Revel tests
	 */

	private HashMap<String, Object> constructMapDeviceInfoRevel() {
		HashMap<String, Object> mapDeviceInfo = new HashMap<String, Object>();
		mapDeviceInfo.put("longitude", "-122.098203");
		mapDeviceInfo.put("latitude", "37.430377");
		mapDeviceInfo.put("id", "00001");
		mapDeviceInfo.put("type", "IPP350");
		mapDeviceInfo.put("phoneNumber", "6509446000");
		mapDeviceInfo.put("macAddress", "12-34-56-78-9A-BC");
		mapDeviceInfo.put("ipAddress", "10.10.10.10");
		mapDeviceInfo.put("encrypted", true);
		return mapDeviceInfo;
	}

	/**
	 * This will construct map of 'card present' with track 1 and track2
	 * 
	 * @return
	 */
	private HashMap<String, Object> constructRevelCardPresent() {

		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("track1", track1);
		cardPresent.put("ksn", ksn);
		return cardPresent;
	}

	/**
	 * This will construct map of 'card present' with track 1 and track2 for
	 * Revel
	 * 
	 * @return
	 */
	private HashMap<String, Object> returnMapCardWithRevelCardPresent() {
		HashMap<String, Object> mapCard = new HashMap<String, Object>();
		mapCard.put("cardPresent", constructRevelCardPresent());
		return mapCard;
	}

	/**
	 * This will Validate the successful capture with revel track 1 and track2
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> validateSuccessfulTrack1CaptureRes(
			HttpResponse response) throws Exception {
		HashMap<String, Object> map = validateSuccessfulCaptureRes(response);
		HashMap<String, Object> device = (HashMap<String, Object>) ((HashMap<String, Object>) map
				.get("context")).get("deviceInfo");
		Assert.assertNotNull(device.get("type"));
		if ((HashMap<String, Object>) map.get("card") != null) {
			HashMap<String, Object> card = (HashMap<String, Object>) ((HashMap<String, Object>) map
					.get("card"));

			Assert.assertNull(card.get("track1"));
			Assert.assertNull(card.get("track2"));
			Assert.assertNull(card.get("ksn"));
			if (card.get("number") != null)
				Assert.assertTrue(card.get("number").toString()
						.contains("xxxxxxxxxxxx"));

		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private void validateMaskTrackData(HashMap<String, Object> map) {
		if ((HashMap<String, Object>) map.get("card") != null) {
			HashMap<String, Object> cardPresent = (HashMap<String, Object>) ((HashMap<String, Object>) map
					.get("card")).get("cardPresent");
			if (cardPresent != null) {
				Assert.assertTrue(cardPresent.get("track1").toString()
						.matches("x+"));
				Assert.assertTrue(cardPresent.get("track2").toString()
						.matches("x+"));
				Assert.assertTrue(cardPresent.get("ksn").toString()
						.matches("x+"));
			}
		}
	}

	/**
	 * This will construct map of request body for revel specs with track 1 and
	 * track2
	 * 
	 * @return
	 */

	private HashMap<String, Object> constructRevelRequestBody() {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		// device info in card context
		revelCardContext.put("deviceInfo", constructMapDeviceInfoRevel());
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", returnMapCardWithRevelCardPresent());
		return revelRequestMap;

	}

	/**
	 * This will construct map of request body for revel specs for pindebit
	 * 
	 * @return
	 */

	private HashMap<String, Object> constructRevelRequestBodyForPinDebit() {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		// device info in card context
		revelCardContext.put("deviceInfo", constructMapDeviceInfoRevel());
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card",
				returnMapCardWithRevelCardPresentForPinDebit());
		return revelRequestMap;

	}

	/**
	 * This will construct map of 'card present' with track 1 and track2 for
	 * Revel
	 * 
	 * @return
	 */

	private HashMap<String, Object> returnMapCardWithRevelCardPresentForPinDebit() {
		HashMap<String, Object> mapCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("track1", track1);
		cardPresent.put("ksn", ksn);
		cardPresent.put("pinBlock", pinBlock);
		mapCard.put("cardPresent", cardPresent);
		return mapCard;
	}

	/**********************************************************************************************************************
	 * ******** REVEL TEST CASES*******************
	 * https://wiki.intuit.com/display/~schilukuri1/Revel+PayAPI+IntegrationTest+cases
	 * 
	 * 
	 **********************************************************************************************************************/
	
	 /** 
	 * Valid track1 data with a valid Device Type (IPP350) and
	 * encrypted=false in device info [Outcome- Should not call decryption code
	 * and pass track1 data to downstream systems as is]
	 */

	@Test
	public void testCCCaptureRevelWithEncryptedFalse() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", false);
		mapDeviceInfo.put("type", "IPP350");
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", returnMapCardWithRevelCardPresent());
		revelRequestMap.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateIfIsError(response);

	}

	/**
	 * encrypted=true and with Device Type missing.[Outcome-Should throw a
	 * proper error indicating that device type is needed when is_encrypted=1]
	 * 
	 * 
	 */
	@Test
	public void testMissingDeviceType() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", returnMapCardWithRevelCardPresent());
		revelRequestMap.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"deviceInfoType is invalid.", "deviceInfoType",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * encrypted=true and track1 data is missing.[Outcome-Should throw a proper
	 * error indicating that track1 is needed when encrypted=true]
	 * 
	 */

	@Test
	public void testMissingTrackData() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		// missing track1 data
		// revelRequestMap.put("card", returnMapCard());
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		// Missing track1/track2 data
		// cardPresent.put("track1", track1);
		// cardPresent.put("track2", "4111111111111111");
		cardPresent.put("ksn", ksn);
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		// do auth first
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"cardPresent is invalid.", "cardPresent",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * encrypted=true and Device Type is a value other than IPP350.
	 * [Outcome-Should throw a proper error indicating that device type is
	 * unsupported]
	 */

	@Test
	public void testUnSupportedDeviceType() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "ISC250");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", returnMapCardWithRevelCardPresent());
		// do auth first
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"deviceInfoType is invalid.", "deviceInfoType",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * Bad track1/track2 data.[Outcome-Should throw a proper error indicating
	 * that track1/track2 data is corrupted]
	 * 
	 */

	@Test
	public void testInvalidTrackData() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		revelCard.put("track1", "%64545dfj&&&");
		// revelCard.put("track2", "%78596$&&&&&&&&&&&");
		revelRequestMap.put("card", revelCard);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"track1 is invalid.", "track1",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * track1 and track2 data both present.[Outcome-Should be successful]
	 * 
	 */

	@Test
	public void testBothTrackDataPresent() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("track1", track1);
		cardPresent.put("ksn", ksn);
		cardPresent.put("track2", "4111111111111111");
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.CAPTURED.toString());
		validateMaskTrackData(map);

	}

	/**
	 * track1 missing and track2 data present.[Outcome-Should be error]
	 * 
	 */

	@Test
	public void testTrack2ValidData() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		// cardPresent.put("track1", track1);
		cardPresent.put("ksn", ksn);
		//replace track2 with the right data once we get the data.
		cardPresent.put("track2", track1);
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
//		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
//				"cardPresent is invalid.", "cardPresent",
//				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
		//The error will no longer be seen as we made enhancements to accept track2 only
		
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.CAPTURED.toString());
		validateMaskTrackData(map);

	}

	/**
	 * This will capture first without doing an Auth should be successful.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeCardRevel() throws Exception {
		HashMap<String, Object> mapBody = constructRevelRequestBody();
		// do capture without auth
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		// validate device info
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.CAPTURED.toString());
		validateMaskTrackData(map);

	}

	/**
	 * This will auth first then capture the exact auth amount should be
	 * successful.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	// (enabled=false)
	public void testCCCaptureCardPresentRevel() throws Exception {
		HashMap<String, Object> mapBody = constructRevelRequestBody();
		mapBody.put("capture", false);
		// do auth first
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		// HashMap<String, Object> map = validateAuthResRevel(response, 201,
		// Status.AUTHORIZED.toString());
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.AUTHORIZED.toString());
		validateMaskTrackData(map);
		String chargeid = (String) map.get("id");
		// build capture endpoint with the charge id from Auth response
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		String Description = "Capture 1.00 US Dollar";
		captureBody.put("amount", 1.00);
		captureBody.put("description", Description);
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulTrack1CaptureRes(capture_response);
		Assert.assertEquals("1.00", captureResponse.get("amount").toString());
		Assert.assertEquals(
				Description,
				((HashMap<String, Object>) captureResponse.get("captureDetail"))
						.get("description").toString());
	}

	/**
	 * This will auth first then capture the exact auth amount and refund the
	 * full amount should be successful.
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	@Test
	public void testCCRefundDeviceTypeRevel() throws Exception {

		HashMap<String, Object> mapBody = constructRevelRequestBody();
		mapBody.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.AUTHORIZED.toString());
		validateMaskTrackData(map);
		// Assert.assertEquals(201, response.getStatusLine().getStatusCode());
		String chargeid = (String) map.get("id");
		// do capture
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 1.00);
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		// Assert.assertEquals(201, response.getStatusLine().getStatusCode());
		HashMap<String, Object> captureMap = validateSuccessfulTrack1CaptureRes(capture_response);
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, captureMap.get("id")
				.toString());
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		HashMap<String, Object> mapContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = new HashMap<String, Object>();
		deviceInfo.put("encrypted", true);
		deviceInfo.put("type", "IPP350");
		refundBody.put("amount", 1.00);
		mapContext.put("deviceInfo", deviceInfo);
		refundBody.put("context", mapContext);
		HttpResponse resRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(
				resRefund, 201, Status.ISSUED.toString());
		HashMap<String, Object> actualDeviceInfo = ((HashMap<String, Object>) ((HashMap<String, Object>) refundResponseMap
				.get("context")).get("deviceInfo"));
		Assert.assertEquals("IPP350", actualDeviceInfo.get("type"));
		String refundId = refundResponseMap.get("id").toString();
		String CCRetrieveRefundUrl = buildUrl(RETRIEVAL_REFUND_CC, realmid,
				captureMap.get("id").toString(), refundId);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrieveRefundUrl,
				CCRefundRetrieveHeader);
		HashMap<String, Object> refundMap = validateSuccessfulRefundRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.ISSUED.getText(), refundMap.get("status")
				.toString());

	}

	@Test
	public void testEmptyTrack2AndPinBlock() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		// cardPresent.put("track1", track1);
		cardPresent.put("ksn", ksn);
		cardPresent.put("track1", track1);
		cardPresent.put("track2", "");
		cardPresent.put("pinBlock", "");
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.CAPTURED.toString());
		validateMaskTrackData(map);

	}

	// *****************Pin Block tests *****

	@SuppressWarnings("unchecked")
	@Test
	// (enabled=false)
	public void testCCCaptureCardPinDebit() throws Exception {
		HashMap<String, Object> mapBody = constructRevelRequestBodyForPinDebit();
		mapBody.put("capture", false);
		// do auth first
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.AUTHORIZED.toString());
		validateMaskTrackData(map);
		String chargeid = (String) map.get("id");
		// build capture endpoint with the charge id from Auth response
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		String Description = "Capture 1.00 US Dollar";
		captureBody.put("amount", 1.00);
		captureBody.put("description", Description);
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		HashMap<String, Object> captureResponse = validateSuccessfulTrack1CaptureRes(capture_response);
		Assert.assertEquals(201, response.getStatusLine().getStatusCode());
		Assert.assertEquals("1.00", captureResponse.get("amount").toString());
		Assert.assertEquals(
				Description,
				((HashMap<String, Object>) captureResponse.get("captureDetail"))
						.get("description").toString());
	}

	/**
	 * This will auth first then capture the exact auth amount and refund the
	 * full amount should be successful.
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	@Test
	public void testCCRefundDeviceTypeRevelPinDebit() throws Exception {

		HashMap<String, Object> mapBody = constructRevelRequestBodyForPinDebit();
		mapBody.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201,
				Status.AUTHORIZED.toString());
		validateMaskTrackData(map);
		Assert.assertEquals(201, response.getStatusLine().getStatusCode());
		String chargeid = (String) map.get("id");
		// do capture without auth
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 1.00);
		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		Assert.assertEquals(201, response.getStatusLine().getStatusCode());
		HashMap<String, Object> captureMap = validateSuccessfulTrack1CaptureRes(capture_response);
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, captureMap.get("id")
				.toString());
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		HashMap<String, Object> mapContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = new HashMap<String, Object>();
		deviceInfo.put("encrypted", true);
		deviceInfo.put("type", "IPP350");
		refundBody.put("amount", 1.00);
		mapContext.put("deviceInfo", deviceInfo);
		refundBody.put("context", mapContext);
		HttpResponse resRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(
				resRefund, 201, Status.ISSUED.toString());
		HashMap<String, Object> actualDeviceInfo = ((HashMap<String, Object>) ((HashMap<String, Object>) refundResponseMap
				.get("context")).get("deviceInfo"));
		Assert.assertEquals("IPP350", actualDeviceInfo.get("type"));
		String refundId = refundResponseMap.get("id").toString();
		String CCRetrieveRefundUrl = buildUrl(RETRIEVAL_REFUND_CC, realmid,
				captureMap.get("id").toString(), refundId);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrieveRefundUrl,
				CCRefundRetrieveHeader);
		HashMap<String, Object> refundMap = validateSuccessfulRefundRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.ISSUED.getText(), refundMap.get("status")
				.toString());

	}

	/**
	 * This will capture first without doing an Auth should be successful.
	 * 
	 * @throws Exception
	 */
	// @Test(enabled=false,description="enable when correct test data available for pin debit")
	// public void testCCChargePinDebit() throws Exception {
	// HashMap<String, Object> mapBody = constructRevelRequestBodyForPinDebit();
	// // do capture without auth
	// HttpResponse response = TestUtil.post(CCChargeUrl,
	// CCChargeHeaderParams, mapBody);
	// HashMap<String, Object>
	// map=validateChargeRes(response,201,Status.CAPTURED.toString());
	// validateMaskTrackData(map);
	//
	//
	// }

//	@Test(enabled=false,description="enable when correct test data available for pin debit")
//	public void testTrack2ValidDataPinDebit() throws Exception {
//		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
//		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
//		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
//		mapDeviceInfo.put("encrypted", true);
//		mapDeviceInfo.put("type", "IPP350");
//
//		// device info in card context
//		revelCardContext.put("deviceInfo", mapDeviceInfo);
//		revelRequestMap.put("context", revelCardContext);
//		revelRequestMap.put("amount", 1.00);
//		revelRequestMap.put("currency", "USD");
//		HashMap<String, Object> revelCard = new HashMap<String, Object>();
//		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
//		// cardPresent.put("track1", track1);
//		cardPresent.put("ksn", ksn);
//		cardPresent.put("track2", track1);
//		cardPresent.put("pinBlock", pinBlock);
//		revelCard.put("cardPresent", cardPresent);
//		revelRequestMap.put("card", revelCard);
//		HttpResponse response = TestUtil.post(CCChargeUrl,
//				CCChargeHeaderParams, revelRequestMap);
//		//error will no longer be seen as we made enhancements to support track2 only.
////		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
////				"cardPresent is invalid.", "cardPresent",
////				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
//		HashMap<String, Object> map = validateChargeRes(response, 201,
//				Status.CAPTURED.toString());
//		validateMaskTrackData(map);
//
//	}

	/**
	 * track1 and track2 data both present.[Outcome-Should be successful]test
	 * case no 8
	 * 
	 */

	@Test
	public void testInValidPinBlock() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("track1", track1);
		cardPresent.put("ksn", ksn);
		cardPresent.put("track2", "4111111111111111");
		cardPresent.put("pinBlock", "TYUIOPYUUYTYT");
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		// validateSuccessfulTrack1CaptureRes(response);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"PINBlock is invalid.", "PINBlock",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * Valid track1 data with a valid Device Type (IPP350) and encrypted=false
	 * in device info [Outcome- Should not call decryption code and pass track1
	 * data to downstream systems as is]
	 */

	@Test
	public void testCCCaptureWithEncryptedFalseWithPinBlock()
			throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", false);
		mapDeviceInfo.put("type", "IPP350");
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		HashMap<String, Object> pinDebit = constructCardPresent();
		pinDebit.put("track1", track1);
		pinDebit.put("ksn", ksn);
		pinDebit.put("pinBlock", pinBlock);

		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", pinDebit);
		revelRequestMap.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"track1 is invalid.", "track1",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * encrypted=true and with Device Type missing.[Outcome-Should throw a
	 * proper error indicating that device type is needed when is_encrypted=1]
	 * 
	 * 
	 */
	@Test
	public void testMissingDeviceTypePinDebit() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", returnMapCardWithRevelCardPresent());
		revelRequestMap.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"deviceInfoType is invalid.", "deviceInfoType",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * Test with no amount in pin debit throws error
	 * 
	 */
	@Test
	public void testNoAmountPinDebit() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		// revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		revelRequestMap.put("card", returnMapCardWithRevelCardPresent());
		revelRequestMap.put("capture", false);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4002",
				"amount is required.", "amount",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * encrypted=true and track1 data is missing.[Outcome-Should throw a proper
	 * error indicating that track1 is needed when encrypted=true] 
	 * 
	 */

	@Test
	public void testMissingTrackDataPinDebit() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");
		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		// missing track1 data
		// revelRequestMap.put("card", returnMapCard());
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("ksn", ksn);
		cardPresent.put("pinBlock", pinBlock);
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		// do auth first
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"cardPresent is invalid.", "cardPresent",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * encrypted=true and Device Type is a value other than IPP350.
	 * [Outcome-Should throw a proper error indicating that device type is
	 * unsupported]
	 */

	@Test
	public void testUnSupportedDeviceTypePinDebit() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "ISC250");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("ksn", ksn);
		cardPresent.put("pinBlock", pinBlock);
		cardPresent.put("track1", track1);
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);

		// do auth first
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"deviceInfoType is invalid.", "deviceInfoType",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * Bad track1/track2 data.[Outcome-Should throw a proper error indicating
	 * that track1/track2 data is corrupted]
	 * 
	 */

	@Test
	public void testRevelInvalidTrackDataPinDebit() throws Exception {
		HashMap<String, Object> revelRequestMap = new HashMap<String, Object>();
		HashMap<String, Object> revelCardContext = new HashMap<String, Object>();
		HashMap<String, Object> mapDeviceInfo = constructMapDeviceInfo();
		mapDeviceInfo.put("encrypted", true);
		mapDeviceInfo.put("type", "IPP350");

		// device info in card context
		revelCardContext.put("deviceInfo", mapDeviceInfo);
		revelRequestMap.put("context", revelCardContext);
		revelRequestMap.put("amount", 1.00);
		revelRequestMap.put("currency", "USD");
		HashMap<String, Object> revelCard = new HashMap<String, Object>();
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("ksn", ksn);
		// cardPresent.put("pinBlock",pinBlock);
		// cardPresent.put("track1",track1);
		cardPresent.put("track1",
				"ABCDGSTULKJKJHLIUYTUIOUYIUYTUIYTUIYTUYTUYTUYYTUTUERSEREDFVCC");
		revelCard.put("cardPresent", cardPresent);
		revelRequestMap.put("card", revelCard);
		revelRequestMap.put("card", revelCard);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, revelRequestMap);
		// validateErrorRes(response, 400, "invalid_request", "PMT-4000",
		// "track1 is invalid.", "track1",
		// "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
		validateIfIsError(response);

	}

}