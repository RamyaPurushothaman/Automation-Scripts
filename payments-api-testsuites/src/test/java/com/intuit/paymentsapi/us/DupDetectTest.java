package com.intuit.paymentsapi.us;

import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.PropertyUtil;
import com.intuit.paymentsapi.util.TestUtil;
import com.intuit.tame.ws.JsonTestClient;

public class DupDetectTest extends BaseTest {

	String CCChargeUrl = buildUrl(CHARGE_CC);

	/**
	 * This will test same request under same intuit_appid, the second one
	 * should return the exact same response.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDupRequestSameApp() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		String req_id = UUID.randomUUID().toString();
		String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", req_id }

		};

		HttpResponse response1 = TestUtil.postDupReq(CCChargeUrl,
				CCChargeHeaderParams, mapBody, req_id, "");
		HashMap<String, Object> resp1 = validateChargeRes(response1, 201, Status.CAPTURED.toString());

		mapBody.put("amount", 2.00);
		HttpResponse response2 = TestUtil.postDupReq(CCChargeUrl,
				CCChargeHeaderParams, mapBody, req_id, "");
		HashMap<String, Object> resp2 = JsonTestClient.convertToMap(response2);
		// Assert.assertEquals(true, TestUtil.compareJsonMap(resp1, resp2));
		Assert.assertEquals("1.00", resp2.get("amount").toString());
		Assert.assertEquals(resp1.get("id").toString(), resp2.get("id")
				.toString());
	}

	/**
	 * This will test same request under different intuit_appid, the second one
	 * should return a new response.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDupRequestDiffApp() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		String req_id = UUID.randomUUID().toString();
		String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", req_id }

		};

		HttpResponse response1 = TestUtil.postDupReq(CCChargeUrl,
				CCChargeHeaderParams, mapBody, req_id, "");
		HashMap<String, Object> resp1 = validateChargeRes(response1, 201, Status.CAPTURED.toString());

		mapBody.put("amount", 2.00);
		String newAppId = null;
		if (TestUtil.env.startsWith("cto")) {
			newAppId = "Intuit_IAM_Authentication intuit_appid="
					+ PropertyUtil.getValue(TestUtil.env + ".newappid")
					+ ", intuit_app_secret="
					+ PropertyUtil.getValue(TestUtil.env + ".newappsecret");
		} else {
			newAppId = "Intuit_IAM_Authentication intuit_appid="
					+ PropertyUtil.getValue("v2.appid")
					+ ", intuit_app_secret="
					+ PropertyUtil.getValue("v2.appsecret");
		}
		HttpResponse response2 = TestUtil.postDupReq(CCChargeUrl,
				CCChargeHeaderParams, mapBody, req_id, newAppId);
		HashMap<String, Object> resp2 = validateChargeRes(response2, 201, Status.CAPTURED.toString());
		Assert.assertEquals(false, TestUtil.compareJsonMap(resp1, resp2));
		Assert.assertEquals("2.00", resp2.get("amount").toString());
		Assert.assertEquals(false,
				resp1.get("id").toString().equals(resp2.get("id").toString()));
	}

	/**
	 * This will charge first with unique Request-Id then refund second time
	 * with the same Request-Id within the same appid should return an error.
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDupRequestDiffTxnType() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		String req_id = UUID.randomUUID().toString();
		String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", req_id }

		};

		HttpResponse chargeResponse = TestUtil.postDupReq(CCChargeUrl,
				CCChargeHeaderParams, mapBody, req_id, "");
		HashMap<String, Object> resp1 = validateChargeRes(chargeResponse, 201, Status.CAPTURED.toString());
		String chargeid = (String) resp1.get("id");

		String[][] CCRefundHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", UUID.randomUUID().toString() }, };
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeid);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		HttpResponse refundResponse = TestUtil.postDupReq(CCRefundUrl,
				CCRefundHeaderParams, refundBody, req_id, "");
		validateErrorRes(refundResponse,400,"invalid_request","PMT-4000", "requestId is invalid.", "requestId","https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will auth first with unique Request-Id then capture
	 * with the same Request-Id within the same appid should return error code 10312.
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDupRequestAuthCapture() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		mapBody.put("capture", false);

		String req_id = UUID.randomUUID().toString();
		String[][] CCChargeHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", req_id }

		};

		HttpResponse authResponse = TestUtil.postDupReq(CCChargeUrl,
				CCChargeHeaderParams, mapBody, req_id, "");
		HashMap<String, Object> resp1 = validateAuthRes(authResponse, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) resp1.get("id");

		String[][] CCCaptureHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", req_id }};
		String CCCaptureUrl = buildUrl(CAPTURE_CC, realmid, chargeid);
		HashMap<String, Object> capBody = new HashMap<String, Object>();
		capBody.put("amount", 2.00);
		HttpResponse capResponse = TestUtil.postDupReq(CCCaptureUrl,
				CCCaptureHeaderParams, capBody, req_id, "");
		
		validateErrorRes(capResponse,400,"invalid_request","PMT-4000", "requestId is invalid.", "requestId","https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

}
