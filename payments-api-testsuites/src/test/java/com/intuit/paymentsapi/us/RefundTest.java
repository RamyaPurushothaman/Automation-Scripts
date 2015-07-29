package com.intuit.paymentsapi.us;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class RefundTest extends BaseTest {

	private String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }

	};
	
	private String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }, };

	private String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }, /* { "id", "" } */};
	
	private String[][] CCRefundRetrieveHeader = {	
			{ "Company-Id", realmid }
	};
	
	private String[][] CCChargeRetrievalHeader = { { "Company-Id", realmid }, };

	private String CCChargeUrl = buildUrl(CHARGE_CC);
	
	public static String[][] tokenHeaderParams = {};
	
	public static String tokenUrl = buildUrl(CREATE_TOKEN);

	/**
	 * This will charge first then partial refund should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCPatialRefund() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 2);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("2.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals(Status.ISSUED.getText(),
				refundResponseMap.get("status").toString());
		
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCChargeRetrievalHeader);
		
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(1, ((ArrayList<LinkedHashMap<String, Object>>)retrievalMap.get("refundDetail")).size());
	}

	/**
	 * This will charge first then full refund should success.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCFullRefund() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals(Status.ISSUED.getText(),
				refundResponseMap.get("status").toString());
	}

	/**
	 * This will charge first then refund with tax should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCRefundWithTax() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("tax", 0.05);
		refundBody.put("context", contextMap);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals("0.05",
				((HashMap<String, Object>) refundResponseMap.get("context"))
						.get("tax").toString());
	}

	/**
	 * This will charge first then refund with device_info should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCRefundWithDeviceInfo() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		contextMap.put("deviceInfo", deviceInfo);
		refundBody.put("context", contextMap);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		HashMap<String, Object> actualDeviceInfo = ((HashMap<String, Object>) ((HashMap<String, Object>) refundResponseMap
				.get("context")).get("deviceInfo"));
		deviceInfo.remove("encrypted");
		Assert.assertEquals(true,
				TestUtil.compareJsonMap(actualDeviceInfo, deviceInfo));
	}

	/**
	 * This will charge first then refund with sender_account should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	// @Test
	public void testCCRefundWithSenderAccount() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("sender_account_id", "John@intuit.com");
		refundBody.put("context", contextMap);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals("John@intuit.com",
				((HashMap<String, Object>) refundResponseMap.get("context"))
						.get("sender_account_id").toString());
	}

	/**
	 * This will charge first then refund with recurring should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCRefundWithRecur() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("recurring", false);
		refundBody.put("context", contextMap);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals(false, ((HashMap<String, Object>) refundResponseMap
				.get("context")).get("recurring"));
	}

	/**
	 * This will charge first then refund with description should success.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundWithDescription() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		String descript = "Refund 5 dollar.";
		refundBody.put("amount", 5);
		refundBody.put("description", descript);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals(descript, refundResponseMap.get("description")
				.toString());
	}

	/**
	 * This will charge first then refund without the amount should do full
	 * refund.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundNoAmount() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", "");
		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
	}

	/**
	 * This will charge first then refund with batch_id should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	// @Test
	public void testCCRefundWithBatchId() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("batch_id", "1234");
		refundBody.put("context", contextMap);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("5.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals("1234",
				((HashMap<String, Object>) refundResponseMap.get("context"))
						.get("batch_id").toString());
	}

	/**
	 * This will test refund settled status: after 5 min of successful refund,
	 * the status will change to "SETTLED".
	 */
	@SuppressWarnings("unchecked")
	@Test(groups = {"nonPTC"})
	public void testCCRefundSettledStatus() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeId = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 2);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("2.00", refundResponseMap.get("amount").toString());
		Assert.assertEquals(Status.ISSUED.getText(),
				refundResponseMap.get("status").toString());
		String refundId = refundResponseMap.get("id").toString();
		String CCRetrieveRefundUrl = buildUrl(RETRIEVAL_REFUND_CC, realmid, chargeId,refundId);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrieveRefundUrl,
				CCRefundRetrieveHeader);
		HashMap<String, Object> refundMap = validateSuccessfulRefundRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.ISSUED.getText(), refundMap.get("status").toString());
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeId);
		HttpResponse chargeResult = TestUtil.get(CCRetrievalUrl,
				CCChargeRetrievalHeader);
		ArrayList<LinkedHashMap<String, Object>> refundDetail = (ArrayList<LinkedHashMap<String, Object>>)validateSuccessfulRetrievalRes(chargeResult).get("refundDetail");
		LinkedHashMap<String, Object> refundDetailMap = refundDetail.get(0);
		Assert.assertEquals(Status.ISSUED.getText(), refundDetailMap.get("status").toString());
		
		//Wait for 90s
		System.out.println("Waiting "+recon_emulator_frequency+" ms for recon emulator to settle");
		Thread.sleep(recon_emulator_frequency);
		
		HttpResponse retrievalSettledResponse = TestUtil.get(CCRetrieveRefundUrl,
				CCRefundRetrieveHeader);
		refundMap = validateSuccessfulRefundRetrievalRes(retrievalSettledResponse);
		Assert.assertEquals(Status.SETTLED.getText(), refundMap.get("status").toString());
		
		HttpResponse chargeResult2 = TestUtil.get(CCRetrievalUrl,
				CCChargeRetrievalHeader);
		refundDetail = (ArrayList<LinkedHashMap<String, Object>>)validateSuccessfulRetrievalRes(chargeResult2).get("refundDetail");
		refundDetailMap = refundDetail.get(0);
		Assert.assertEquals(Status.SETTLED.getText(), refundDetailMap.get("status").toString());
		
	}

	
	/**
	 * This will test refund declined status,the status should be "DECLINED" and will never change to "SETTLED".
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(groups = {"nonPTC"})
	public void testCCRefundDeclinedStatus() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		
		//Emulate ML error code
		String[][] refundEmulatedMLError = {{ "Company-Id", realmid },
											{ "Request-Id", UUID.randomUUID().toString() },
											{ "emulation",  "emulate=10401"}
		
		};
		
		HttpResponse refundFail = TestUtil.post(CCRefundUrl,
				refundEmulatedMLError, refundBody);
	    
		//validateIfIsError(refundFail);
		validateRefundRes(refundFail, 201, Status.DECLINED.toString());
		
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCChargeRetrievalHeader);
		
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		ArrayList<LinkedHashMap<String, Object>> refundArr = ((ArrayList<LinkedHashMap<String, Object>>)retrievalMap.get("refundDetail"));
		Assert.assertEquals(1, refundArr.size());
		
		LinkedHashMap<String, Object> refundMap = refundArr.get(0);
		Assert.assertEquals(Status.DECLINED.getText(), refundMap.get("status").toString());
		String refundId = refundMap.get("id").toString();
		
		//Wait for 90s
		System.out.println("Waiting "+recon_emulator_frequency+" ms for recon emulator to settle");
		Thread.sleep(recon_emulator_frequency);
				
		String CCRetrieveRefundUrl = buildUrl(RETRIEVAL_REFUND_CC, realmid, chargeid,refundId);
		HttpResponse retrievalDeclinedResponse = TestUtil.get(CCRetrieveRefundUrl,
						CCRefundRetrieveHeader);
	    HashMap<String,Object> refundMap2 = validateSuccessfulRefundRetrievalRes(retrievalDeclinedResponse);
	    Assert.assertEquals(Status.DECLINED.getText(), refundMap2.get("status").toString());
	}
	
	/**
	 * This will test first auth, then capture.
	 * And then refund with auth id should work.
	 * 
	 */
	@Test
	public void testCCRefundCapturedAuthId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String authid = (String) map.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, authid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 1.00);

		HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateSuccessfulCaptureRes(capture_response);
		
		//Refund with auth id
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, authid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 1.00);
		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateRefundRes(refundResponse, 201, Status.ISSUED.toString());
	}
	 
	
	/**
	 * This will test first auth, then capture.
	 * And then refund with capture id should work.
	 * 
	 */
	 @Test
	 public void testCCRefundCaptureId() throws Exception {
		 HashMap<String, Object> mapBody = new HashMap<String, Object>();
			mapBody.put("amount", 1.00);
			mapBody.put("currency", "USD");
			mapBody.put("card", returnMapCard());
			mapBody.put("capture", false);

			HttpResponse response = TestUtil.post(CCChargeUrl,
					CCChargeHeaderParams, mapBody);
			HashMap<String, Object> map = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
			String authid = (String) map.get("id");
			String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, authid);

			HashMap<String, Object> captureBody = new HashMap<String, Object>();
			captureBody.put("amount", 1.00);

			HttpResponse capture_response = TestUtil.post(CCCaptureUrl,
					CCCaptureHeaderParams, captureBody);
			HashMap<String,Object> captureMap = validateSuccessfulCaptureRes(capture_response);
			
			//Refund with capture id
			String CCRefundUrl = buildUrl(REFUND_CC, realmid, captureMap.get("id").toString());
			HashMap<String, Object> refundBody = new HashMap<String, Object>();
			refundBody.put("amount", 1.00);
			HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
					CCRefundHeaderParams, refundBody);
			validateRefundRes(refundResponse, 201, Status.ISSUED.toString());
	 }
	 
	 /**
	  *  This will create a token, then using this token to charge.
	  *  And then refund should work.
	  *  
	  */
	 @Test
	 public void testCCRefundToken() throws Exception {
		    //Create token
			HashMap<String, Object> mapBody = new HashMap<String, Object>();
			mapBody.put("card", returnMapCard());
			
			TestUtil.auth = "apikey";
			
			HttpResponse response = TestUtil.post(tokenUrl,
					tokenHeaderParams, mapBody);
			HashMap<String,Object> tokenMap = validateSuccessfulTokenRes(response);
			String token = tokenMap.get("value").toString();
			
			TestUtil.auth = null;
			
			//Charge with token
			HashMap<String, Object> chargeBody = new HashMap<String, Object>();
			chargeBody.put("amount", 1.00);
			chargeBody.put("token", token);
			chargeBody.put("currency", "USD");
			HttpResponse chargeTokenRes = TestUtil.post(ChargeTest.CCChargeUrl,
					CCChargeHeaderParams, chargeBody);
			HashMap<String,Object> captureMap = validateChargeRes(chargeTokenRes, 201, Status.CAPTURED.toString());
			
			//Refund the charge
			String CCRefundUrl = buildUrl(REFUND_CC, realmid, captureMap.get("id").toString());
			HashMap<String, Object> refundBody = new HashMap<String, Object>();
			refundBody.put("amount", 1.00);
			HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
					CCRefundHeaderParams, refundBody);
			validateRefundRes(refundResponse, 201, Status.ISSUED.toString());
	 }
	 
	
	/**
	 * 
	 * 
	 * 
	 * Unhappy Path
	 * 
	 * 
	 * 
	 */

	/**
	 * This will charge first then refund with invalid charge id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundInvalidChargeId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = "EMU000000000";

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
	}

	/**
	 * This will charge first then refund without charge id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundNoChargeId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = "";

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-6000");
	}

	/**
	 * This will charge first then refund with too long charge id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongChargeId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = "EMU0000000000000";

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
	}

	/**
	 * This will charge first then refund with more amount should fail.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCRefundAmountExceed() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 10);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
		
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCChargeRetrievalHeader);
		
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(null, (ArrayList<LinkedHashMap<String, Object>>)retrievalMap.get("refundDetail"));
	}

	/**
	 * This will charge first then refund with short batch_id should fail.
	 * 
	 * @throws Exception
	 */
	// @Test
	public void testCCRefundShortBatchId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		// contextMap.put("batch_id", "123");
		refundBody.put("context", contextMap);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
	}

	/**
	 * This will auth first then refund should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("capture", false);
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResMap = validateAuthRes(chargeResponse, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) authResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 3);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
	}

	/**
	 * This will charge first then multiple refund which amount exceed charged
	 * amount should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCMultiRefundAmountExceed() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 3);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("3.00", refundResponseMap.get("amount").toString());
		refundBody.put("amount", 3);

		refundResponse = TestUtil.post(CCRefundUrl, CCRefundHeaderParams,
				refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
		
		
	}

	/**
	 * This will charge first then refund with too long description should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongDescription() throws Exception {
		/*
		 * String[][] testData = { { "number",
		 * generateCreditCardNumber(VISA_PREFIX_LIST) } };
		 */
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);
		String longDescrip = "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4001; i++) {
			sb.append("a");
		}
		longDescrip = sb.toString();
		refundBody.put("description", longDescrip);

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
	}

	/**
	 * This will charge first then refund with invalid realm_id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRefundInvalidRealmId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String invalidRealm = "1019017111";
		String CCRefundUrl = buildUrl(REFUND_CC, invalidRealm, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 5);

		String[][] InvalidRefundHeader = { { "Company-Id", invalidRealm },
				{ "Request-Id", UUID.randomUUID().toString() } };

		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				InvalidRefundHeader, refundBody);
		validateErrorRes(refundResponse, "PMT-4000");
	}
	
	/**
	 * Charge $10 --> Full Refund $10 --> Refund $10
	 * Expected Response- Error code - PMT-4000 
	 */
	@Test
	public void testCCRefundAfterFullRefund() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");
		
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 10.00);
		HttpResponse refundResponse = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		HashMap<String, Object> refundResponseMap = validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		Assert.assertEquals("10.00", refundResponseMap.get("amount").toString());
		
		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");
		
	}

	/**
	 * Charge $10 --> Refund $10 (add deviceId greater than 40 characters)
	 * Expected Response - Error code --> PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCRefundWithLongDeviceId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");
		
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("id",
				"00000000000000000000000000000000000000000000001234");
		mapCardContext.put("deviceInfo", deviceInfo);

		refundBody.put("amount", 10.00);
		refundBody.put("context", mapCardContext);

		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");
	}
	
	/**
	 * Charge $10 --> Refund $10 (add deviceType greater than 200 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongDeviceType() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");
		
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		
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

		refundBody.put("amount", 10.00);
		refundBody.put("context", mapCardContext);
		
		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");
	}
	
	/**
	 * Capture $10 --> Refund $10 (add macAddress greater than 60 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongMacAddress() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(
				chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();

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

		refundBody.put("amount", 10.00);
		refundBody.put("context", mapCardContext);
		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");

	}
	
	/**
	 * Capture $10 --> Refund $10 (add latitude greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongLatitude() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(
				chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();		
		deviceInfo.put("latitude",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);
		
		refundBody.put("amount", 10.00);
		refundBody.put("context", mapCardContext);
		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");
	}
	
	/**
	 * Capture $10 --> Refund $10 (add longitude greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongLongitude() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(
				chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();		
		deviceInfo.put("longitude",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);
		
		refundBody.put("amount", 10.00);
		refundBody.put("context", mapCardContext);
		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");
	}
	
	/**
	 * Capture $10 --> Refund $10 (add IP address greater than 40 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCRefundLongIPAddress() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(
				chargeResponse, 201, Status.CAPTURED.toString());

		String chargeid = (String) chargeResMap.get("id");

		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();		
		deviceInfo.put("ipAddress",
				"123456789012345678901234567890123456789012");
		mapCardContext.put("deviceInfo", deviceInfo);
		
		refundBody.put("amount", 10.00);
		refundBody.put("context", mapCardContext);
		HttpResponse doubleRefund = TestUtil.post(CCRefundUrl,
				CCRefundHeaderParams, refundBody);
		validateErrorRes(doubleRefund, "PMT-4000");
	}
}
