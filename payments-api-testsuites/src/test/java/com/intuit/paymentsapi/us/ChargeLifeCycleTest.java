package com.intuit.paymentsapi.us;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;
import com.intuit.tame.ws.JsonTestClient;

@Test(groups = {"nonPTC"})
public class ChargeLifeCycleTest extends BaseTest {

	
	private String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid }, };

	private String CCChargeUrl = buildUrl(CHARGE_CC);
	String amt = "5.00";
	
	@DataProvider(name = "refund-b4-settlement-data")
	public Object[][] refundData1() throws Exception {
		return new Object[][] {
				// Full Refund
				{ new String[][] {{ "amt", amt  } , {"chargeStatus", Status.CANCELLED.toString()}

				} },
				// Partial Refund
				{ new String[][] {

				{ "amt", "1.00"  },{"chargeStatus", Status.REFUNDED.toString()}

				} }};
	}

	@DataProvider(name = "refund-after-settlement-data")
	public Object[][] refundData2() throws Exception {
		return new Object[][] {
				// Full Refund
				{ new String[][] {{ "amt", amt  } , {"chargeStatus", Status.REFUNDED.toString()}

				} },
				// Partial Refund
				{ new String[][] {

				{ "amt", "1.00"  },{"chargeStatus", Status.REFUNDED.toString()}

				}}
		};
	}
	
	@DataProvider(name = "refund-data")
	public Object[][] refundData3() throws Exception {
		return new Object[][] {
				// Full Refund
				{ new String[][] {{ "amt", amt  } }

				} ,
				// Partial Refund
				{ new String[][] {

				{ "amt", "1.00"  }

				}}
		};
	}
	
	
	/**
	 * 1. Auth -> Authorized -> Capture -> Captured ->  Full Refund -> Cancelled. Refund status will be issued
	 * 2. Auth -> Authorized -> Capture -> Captured ->  Partial Refund -> Refunded. Refund status will be issued
	 * @throws Exception
	 */
	@Test(dataProvider = "refund-b4-settlement-data")
	public void testAuthCaptureRefundBeforeSettlement(String[][] testData) throws Exception {
		String refundAmt = testData[0][1];
		String chargeStatusAfterRefund = testData[1][1];
		
		//Auth 
		HttpResponse response = auth(Double.valueOf(amt), null);
		HashMap<String, Object> authResMap = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) authResMap.get("id");

		//validate retrieving the auth
		HttpResponse retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		String authCode = validateAuthRetrieval(retrievalMap,amt, chargeid, Status.AUTHORIZED.toString() );
		
		
		//Capture
		response = capture(chargeid, amt, getCCChargeHeaderParams());
		
		//validate retrieving the capture
		retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateCaptureRetrieval(retrievalMap, authCode, amt);
		
		
		
		// Refund
		response = refund(chargeid, refundAmt, getCCChargeHeaderParams());
		String refundId = (String)validateRefundRes(response, 201, Status.ISSUED.toString()).get("id");
		
		//validate retrieving the refund
		retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid, chargeStatusAfterRefund);
		validateCaptureDetail((HashMap<String, Object>) retrievalMap.get("captureDetail"), amt);
		List<HashMap<String, Object>> list = (List<HashMap<String, Object>>)  retrievalMap.get("refundDetail");
		validateRefundDetail((HashMap<String, Object>)list.get(0), refundAmt, refundId, Status.ISSUED.toString());
		
		
	}
	
	/**
	 * 3.Charge -> Captured with no capture detail -> X min later in preprod then recon insert records into settlement db -> Settled -> Full Refund -> Refunded  Refund detail should be issued  -> 1 min later in preprod then recon insert records into settlement db  -> Refund status will be Settled
	 * 4.Charge -> Captured with no capture detail -> X min later in preprod then recon insert records into settlement db -> Settled -> Partial Refund -> Refunded Refund detail should be issued  -> 1 min later in preprod then recon insert records into settlement db  -> Refund status will be Settled
	 * @param testData
	 * @throws Exception
	 */
	@Test(dataProvider="refund-after-settlement-data")
	public void testChargeRefundAfterSettlement(String[][] testData) throws Exception{
		String refundAmt = testData[0][1];
		String chargeStatusAfterRefund = testData[1][1];

		// Charge
		HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams() );
		HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) resMap.get("id");
		String authCode = (String) resMap.get("authCode");
		
		//wait for recon service to settle
		System.out.println("Waiting "+recon_emulator_frequency+" ms for recon emulator to settle");
		Thread.sleep(recon_emulator_frequency);

		// validate retrieving the charge
		HttpResponse retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid, Status.SETTLED.toString());

		
		// Refund
		response = refund(chargeid, refundAmt, getCCChargeHeaderParams());
		String refundId = (String) validateRefundRes(response, 201, Status.ISSUED.toString()).get(
				"id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) retrievalMap.get("refundDetail");
		validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
				refundId);

		//wait for recon service to settle
		System.out.println("Waiting "+recon_emulator_frequency+" ms for recon emulator to settle");
		Thread.sleep(recon_emulator_frequency);
		
		// validate retrieving the refund
				retrievalResponse = TestUtil.get(
						buildUrl(RETRIEVAL_CC, realmid, chargeid),
						CCRetrievalHeaderParams);
				retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
				validateRefundRetrieval(retrievalMap, amt, chargeid,
						chargeStatusAfterRefund);
				list = (List<HashMap<String, Object>>) retrievalMap.get("refundDetail");
				validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
						refundId, Status.SETTLED.toString());
	}
	
	
	/*****************
	 * Negative Tests
	 * ***************
	 */
	
	/**
	 * 1.Auth  fail at ML side-> Declined
	 * @throws Exception
	 */
	@Test()
	public void testAuthDeclined() throws Exception {

		
		String[][] params = {
				{ "name", "emulate=10401" } };
		//Auth 
		HttpResponse response = auth(Double.valueOf(amt), params);
		HashMap<String, Object> authResMap = validateAuthRes(response, 201, Status.DECLINED.toString());
		String chargeid = (String) authResMap.get("id");

		//validate retrieving the auth
		HttpResponse retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		String authCode = validateAuthRetrieval(retrievalMap,amt, chargeid, Status.DECLINED.toString() );
		
	}
	
	/**
	 * 2. Auth -> Authorized -> Capture fail at ML side -> Get an error back when doing retrieval it's Authorized and hide capture detail 
	 */
	@Test
	public void testCaptureFailAtML() throws Exception {
		
		
		//Auth 
		HttpResponse response = auth(Double.valueOf(amt), null);
		HashMap<String, Object> authResMap = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) authResMap.get("id");

		//validate retrieving the auth
		HttpResponse retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		String authCode = validateAuthRetrieval(retrievalMap,amt, chargeid, Status.AUTHORIZED.toString() );
		
		
		//Capture
		response = capture(chargeid, amt, getCCChargeHeaderParamsWithEmulatedFlag("10406") );
		validateErrorRes(response, "PMT-5000");
		
		//validate retrieving the capture
		retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateAuthRetrieval(retrievalMap,amt, chargeid, Status.AUTHORIZED.toString() );
		Assert.assertEquals(false, retrievalMap.containsKey("captureDetail"));
		
		
	}
	
	/**
	 * 3. Auth -> Authorized -> Capture fail at validation before reaching ML -> Get an error back when doing retrieval it's Authorized and hide capture detail
	 */
	@Test
	public void testCaptureFailAtValidation() throws Exception {
		
		
		//Auth 
		HttpResponse response = auth(Double.valueOf(amt), null);
		HashMap<String, Object> authResMap = validateAuthRes(response, 201, Status.AUTHORIZED.toString());
		String chargeid = (String) authResMap.get("id");

		//validate retrieving the auth
		HttpResponse retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		String authCode = validateAuthRetrieval(retrievalMap,amt, chargeid, Status.AUTHORIZED.toString() );
		
		
		//Capture
		response = capture(chargeid, "-200", getCCChargeHeaderParams() );
		validateErrorRes(response, "PMT-4000");
		
		//validate retrieving the capture
		retrievalResponse = TestUtil.get(buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateAuthRetrieval(retrievalMap,amt, chargeid, Status.AUTHORIZED.toString() );
		Assert.assertEquals(false, retrievalMap.containsKey("captureDetail"));
		
		
	}
	
	
	/**
	 * 4. Charge -> Captured ->  Full Refund fail at ML side-> Captured. But the refund detail will say it's declined
	 * 5. Charge -> Captured ->  Partial Refund fail at ML side-> Captured. But the refund detail will say it's declined
	 */
	@Test(dataProvider = "refund-data")
	public void testRefundDeclined(String[][] testData) throws Exception {

		String refundAmt = testData[0][1];
		String chargeStatusAfterRefund = Status.CAPTURED.toString();

		// Charge
		HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams() );
		HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) resMap.get("id");
		String authCode = (String) resMap.get("authCode");

		// validate retrieving the charge
		HttpResponse retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
				Status.CAPTURED.toString());

		// Refund
		String[][] headers = { { "Company-Id",realmid }, {"Request-Id", UUID.randomUUID().toString()}, {"emulation", "emulate=10401"}};
		response = refund(chargeid, refundAmt, headers);
		String refundId = (String) validateRefundRes(response, 201, Status.DECLINED.toString()).get(
				"id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) retrievalMap
				.get("refundDetail");
		validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
				refundId, Status.DECLINED.toString());
		
		

	}
	
	/**
	 * 6. Charge -> Captured ->  Full Refund fail at validation before reaching to ML side-> Captured. No Refund detail in the response
	   7, Charge -> Captured ->  Partial Refund fail at validation before reaching to ML side-> Captured. No Refund detail in the response
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	@Test(dataProvider = "refund-data")
	public void testRefundVadlidationFailures(String[][] testData) throws NumberFormatException, Exception{
		String refundAmt = testData[0][1];
		String chargeStatusAfterRefund = Status.CAPTURED.toString();

		// Charge
		HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams());
		HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) resMap.get("id");
		String authCode = (String) resMap.get("authCode");

		// validate retrieving the charge
		HttpResponse retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
				Status.CAPTURED.toString());

		// Refund
		String[][] headers = { { "Company-Id",realmid }, {"Request-Id", UUID.randomUUID().toString()}, {"emulation", "emulate=10401"}};
		response = refund(chargeid, refundAmt, headers);
		String refundId = (String) validateRefundRes(response, 201, Status.DECLINED.toString()).get(
				"id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
				Status.CAPTURED.toString());
		Assert.assertEquals(false, retrievalMap.containsKey("refundDetail"));
		
	}
	
	/**
	 * 8. Charge -> Captured ->  X min later in preprod then recon insert records into settlement db -> Settled -> Full Refund fail at ML side-> Settled. But the refund detail will say it's declined
	 */
	@Test(dataProvider="refund-data")
		public void testRefundDeclineAfterSettlement(String[][] testData) throws Exception{
			String refundAmt = testData[0][1];
			String chargeStatusAfterRefund = Status.SETTLED.toString();

			// Charge
			HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams() );
			HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
			String chargeid = (String) resMap.get("id");
			String authCode = (String) resMap.get("authCode");
			
			//wait for recon service to settle
			System.out.println("Waiting "+recon_emulator_frequency+" ms for recon emulator to settle");
			Thread.sleep(recon_emulator_frequency);

			// validate retrieving the charge
			HttpResponse retrievalResponse = TestUtil.get(
					buildUrl(RETRIEVAL_CC, realmid, chargeid),
					CCRetrievalHeaderParams);
			HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
			validateChargeRetrieval(retrievalMap, amt, authCode, chargeid, Status.SETTLED.toString());

			
			// Refund
			response = refund(chargeid, refundAmt, getCCChargeHeaderParamsWithEmulatedFlag("10401"));
			String refundId = (String) validateRefundRes(response, 201, Status.DECLINED.toString()).get(
					"id");

			// validate retrieving the refund
			retrievalResponse = TestUtil.get(
					buildUrl(RETRIEVAL_CC, realmid, chargeid),
					CCRetrievalHeaderParams);
			retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
			validateRefundRetrieval(retrievalMap, amt, chargeid,
					chargeStatusAfterRefund);
			List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) retrievalMap.get("refundDetail");
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refundId, Status.DECLINED.toString());
	}
		
		/**
		 * 9. Charge -> Captured -> Partial Refund -> Refunded -> Partial Refund fail -> Refunded But the refund details will have two refund details (list will have two refund that is issued and then decined. Out of scope for this part)
		 */
		@Test()
		public void testChargeTwoPartialRefund() throws Exception{
		String refundAmt = "1.50";
		String chargeStatusAfterRefund = Status.REFUNDED.toString();

		// Charge
		HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams());
		HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) resMap.get("id");
		String authCode = (String) resMap.get("authCode");

		// validate retrieving the charge
		HttpResponse retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
				Status.CAPTURED.toString());

		// Partial Refund Succeed
		response = refund(chargeid, refundAmt, getCCChargeHeaderParams());
		String refundId_succ = (String) validateRefundRes(response, 201,
				Status.ISSUED.toString()).get("id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) retrievalMap
				.get("refundDetail");
		validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
				refundId_succ, Status.ISSUED.toString());

		// Partial Refund 2nd time Failed
		response = refund(chargeid, refundAmt, getCCChargeHeaderParamsWithEmulatedFlag("10401"));
		String refundId_fail = (String) validateRefundRes(response, 201,
				Status.DECLINED.toString()).get("id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		list = (List<HashMap<String, Object>>) retrievalMap
				.get("refundDetail");
		
		/*if(((HashMap<String, Object>) list.get(0)).get("status").toString().equals(Status.ISSUED.toString())) {
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refundId_succ, Status.ISSUED.toString());
			validateRefundDetail((HashMap<String, Object>) list.get(1), refundAmt,
					refundId_fail, Status.DECLINED.toString());
		}else {
			validateRefundDetail((HashMap<String, Object>) list.get(1), refundAmt,
					refundId_succ, Status.ISSUED.toString());
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refundId_fail, Status.DECLINED.toString());
		}*/
		
		Assert.assertNotSame(((HashMap<String, Object>) list.get(0)).get("status").toString(), ((HashMap<String, Object>) list.get(1)).get("status").toString());
		
		}
		
		/**
		 * 10. Charge -> Captured -> Partial Refund fail -> Captured -> Partial Refund succeed -> Refunded But list will have two refund that is declined and then issued.
		 */
	@Test()
	public void testChargeTwoPartialRefund2() throws Exception {
		String refundAmt = "1.50";
		String chargeStatusAfterRefund = Status.REFUNDED.toString();

		// Charge
		HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams() );
		HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) resMap.get("id");
		String authCode = (String) resMap.get("authCode");

		// validate retrieving the charge
		HttpResponse retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
				Status.CAPTURED.toString());

		// Partial Refund 1st time Failed
		response = refund(chargeid, refundAmt, getCCChargeHeaderParamsWithEmulatedFlag("10401"));
		String refund_fail = (String) validateRefundRes(response, 201,
				Status.DECLINED.toString()).get("id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) retrievalMap.get("refundDetail");
		validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
				refund_fail, Status.DECLINED.toString());

		// Partial Refund 2nd time Succeed
		response = refund(chargeid, refundAmt, getCCChargeHeaderParams());
		String refund_succ = (String) validateRefundRes(response, 201,
				Status.ISSUED.toString()).get("id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		list = (List<HashMap<String, Object>>) retrievalMap
				.get("refundDetail");
		
		
		/*if(((HashMap<String, Object>) list.get(0)).get("status").toString().equals(Status.ISSUED.toString())) {
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refund_succ, Status.ISSUED.toString());
			validateRefundDetail((HashMap<String, Object>) list.get(1), refundAmt,
					refund_fail, Status.DECLINED.toString());
		}else {
			validateRefundDetail((HashMap<String, Object>) list.get(1), refundAmt,
					refund_succ, Status.ISSUED.toString());
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refund_fail, Status.DECLINED.toString());
		}*/
		
		Assert.assertNotSame(((HashMap<String, Object>) list.get(0)).get("status").toString(), ((HashMap<String, Object>) list.get(1)).get("status").toString());
	}
	
	/**
	 * 11. Charge -> Captured ->  X min later in preprod then recon insert records into settlement db -> Settled ->Partial Refund -> Refunded -> Partial Refund fail -> Refunded But list will have two refund that is issued and then declined.
	 */
	@Test
	public void testChargeTwoPartialRefundAfterSettlement() throws Exception{
		String refundAmt = "1.50";
		String chargeStatusAfterRefund = Status.REFUNDED.toString();

		// Charge
		HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParams() );
		HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) resMap.get("id");
		String authCode = (String) resMap.get("authCode");
	
		//wait for recon service to settle
		System.out.println("Waiting "+recon_emulator_frequency+" ms for recon emulator to settle");
		Thread.sleep(recon_emulator_frequency);

		//validate it's settled
		HttpResponse retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
				Status.SETTLED.toString());
				
		// Partial Refund Succeed
		response = refund(chargeid, refundAmt, getCCChargeHeaderParams());
		String refund_succ = (String) validateRefundRes(response, 201,
				Status.ISSUED.toString()).get("id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) retrievalMap
				.get("refundDetail");
		validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
				refund_succ, Status.ISSUED.toString());

		// Partial Refund 2nd time Failed
		String[][] headers = { { "Company-Id",realmid }, {"Request-Id", UUID.randomUUID().toString()}, {"emulation", "emulate=10401"}};
		response = refund(chargeid, refundAmt, headers);
		String refund_fail = (String) validateRefundRes(response, 201,
				Status.DECLINED.toString()).get("id");

		// validate retrieving the refund
		retrievalResponse = TestUtil.get(
				buildUrl(RETRIEVAL_CC, realmid, chargeid),
				CCRetrievalHeaderParams);
		retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		validateRefundRetrieval(retrievalMap, amt, chargeid,
				chargeStatusAfterRefund);
		list = (List<HashMap<String, Object>>) retrievalMap
				.get("refundDetail");
		
		/*if(((HashMap<String, Object>) list.get(0)).get("status").toString().equals(Status.ISSUED.toString())) {
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refund_succ);
			validateRefundDetail((HashMap<String, Object>) list.get(1), refundAmt,
					refund_fail, Status.DECLINED.toString());
		}else {
			validateRefundDetail((HashMap<String, Object>) list.get(1), refundAmt,
					refund_succ);
			validateRefundDetail((HashMap<String, Object>) list.get(0), refundAmt,
					refund_fail, Status.DECLINED.toString());
		}*/
		
		Assert.assertNotSame(((HashMap<String, Object>) list.get(0)).get("status").toString(), ((HashMap<String, Object>) list.get(1)).get("status").toString());
	}
	
	/**
	 * This will charge first then get the charge detail should success.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCGetChargeDeclined() throws Exception {
		// Charge
				HttpResponse response = charge(Double.valueOf(amt),getCCChargeHeaderParamsWithEmulatedFlag("10401") );
				HashMap<String, Object> resMap = validateChargeRes(response, 201, Status.DECLINED.toString());
				String chargeid = (String) resMap.get("id");
				String authCode = (String) resMap.get("authCode");
				
				// validate retrieving the charge
				HttpResponse retrievalResponse = TestUtil.get(
						buildUrl(RETRIEVAL_CC, realmid, chargeid),
						CCRetrievalHeaderParams);
				HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
				validateChargeRetrieval(retrievalMap, amt, authCode, chargeid,
						Status.DECLINED.toString());
		
				
	}
		
}