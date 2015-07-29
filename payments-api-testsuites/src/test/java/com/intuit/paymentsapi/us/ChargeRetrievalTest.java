package com.intuit.paymentsapi.us;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class ChargeRetrievalTest extends BaseTest {

	private String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }

	};

	private String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }, };

	private String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() } };

	private String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid }, };

	private String CCChargeUrl = buildUrl(CHARGE_CC);

	/**
	 * 
	 * Happy Path
	 * 
	 */

	@SuppressWarnings("unchecked")
	/**
	 * This will charge first then get the charge detail should success.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCGetCharge() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> chargeResMap = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());
		String chargeid = (String) chargeResMap.get("id");

		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.CAPTURED.getText(), retrievalMap.get("status"));
		Assert.assertEquals("5.00", retrievalMap.get("amount").toString());
		Assert.assertEquals(true, retrievalMap.get("capture"));
		Assert.assertEquals(chargeid, retrievalMap.get("id"));
		Assert.assertEquals(true, ((HashMap<String,Object>)retrievalMap.get("card")).get("number").toString().matches("x{11,12}\\d{4}"));
		Assert.assertNull(((HashMap<String,Object>)retrievalMap.get("card")).get("cvs"));
	}

	/**
	 * This will auth first then get the auth detail should success.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCGetAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse authResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResMap = validateAuthRes(authResponse, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) authResMap.get("id");

		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(Status.AUTHORIZED.getText(), retrievalMap.get("status"));
		Assert.assertEquals("5.00", retrievalMap.get("amount").toString());
		Assert.assertEquals(false, retrievalMap.get("capture"));
		Assert.assertEquals(true, retrievalMap.get("authCode").toString().matches(".{6}"));
		Assert.assertEquals(chargeid, retrievalMap.get("id"));
		Assert.assertEquals(true, ((HashMap<String,Object>)retrievalMap.get("card")).get("number").toString().matches("x{11,12}\\d{4}"));
	}

	/**
	 * This will auth first then capture and get the captured auth should
	 * success.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCGetCapturedAuth() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 5);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		HttpResponse authResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> authResMap = validateAuthRes(authResponse, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) authResMap.get("id");
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);

		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", 5);

		HttpResponse captureResponse = TestUtil.post(CCCaptureUrl,
				CCCaptureHeaderParams, captureBody);
		validateSuccessfulCaptureRes(captureResponse);

		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		//Assert.assertEquals(Status.CAPTURED.getText(), retrievalMap.get("status"));
		Assert.assertTrue(retrievalMap.get("status").toString().equals(Status.CAPTURED.getText()) || retrievalMap.get("status").toString().equals(Status.SETTLED.getText()));
		Assert.assertEquals(false, retrievalMap.get("capture"));
		Assert.assertEquals(true, retrievalMap.get("authCode").toString().matches(".{6}"));
		Assert.assertNotNull(retrievalMap.get("captureDetail"));
	}

	/**
	 * This will charge first then refund and get the refunded charge should
	 * success.
	 * 
	 * @throws Exception
	 */
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCCGetRefundCharge() throws Exception {
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
		validateRefundRes(refundResponse, 201, Status.ISSUED.toString());

		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(true, retrievalMap.get("capture"));
		Assert.assertEquals(true,((ArrayList<Object>)retrievalMap.get("refundDetail")).size() > 0);
		Assert.assertEquals(chargeid, retrievalMap.get("id"));
	}

	/**
	 * 
	 * 
	 * Unhappy Path
	 * 
	 * 
	 */

	/**
	 * This will retrieve with invalid charge_id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRetrievalInvalidChargeId() throws Exception {
		String chargeid = "EMM000000000";
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		validateErrorRes(retrievalResponse, "PMT-4000");
	}

	/**
	 * This will retrieve with invalid Company-Id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRetrievalInvalidRealmId() throws Exception {
		String invalidRealmId = "216214100";
		String chargeid = "YY1001313329";
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, invalidRealmId, chargeid);
		String[][] CCRetrievalInvalidRealmHeader = { { "Company-Id",
				invalidRealmId } };
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalInvalidRealmHeader);
		validateErrorRes(retrievalResponse, "PMT-4000");
	}

	/**
	 * This will retrieve without Company-Id should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCRetrievalNoRealmId() throws Exception {
		String noRealmId = "";
		String chargeid = "YY1001313329";
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, noRealmId, chargeid);
		String[][] CCRetrievalNoRealmHeader = { { "Company-Id", noRealmId } };
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalNoRealmHeader);
		validateErrorRes(retrievalResponse, "PMT-4000");
	}
	
	

	
}
