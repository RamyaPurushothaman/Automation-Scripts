package com.intuit.paymentsapi.us;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class ChargeTest extends BaseTest {
	static String origAuth =TestUtil.auth;
	
	@AfterMethod(alwaysRun=true)
	public void tearDown(ITestResult result, Method method) throws Exception {
		TestUtil.auth= origAuth;
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

	public static String[][] CCChargeHeaderParams = { { "Company-Id", realmid }, 
			{"Request-Id", UUID.randomUUID().toString()}

	};
	
	public static String CCChargeUrl = buildUrl(CHARGE_CC);


	@DataProvider(name = "cc-data")
	public Object[][] creditCardChargeData() throws Exception {
		return new Object[][] {
				// Charge Visa CreditCard
				{ new String[][] { { "number",
						generateCreditCardNumber(VISA_PREFIX_LIST) },

				} },
				// Charge Master CreditCard. PTS test env only take this specific card number
				{ new String[][] {

				{ "number", "5111005111051128" },

				} },
				// Charge JCB CreditCard
				{ new String[][] {

				{ "number", generateCreditCardNumber(JCB_PREFIX_LIST) },

				} },
				// Charge Discover CreditCard
				{ new String[][] {

				{ "number", "6559906559906557" },

				} } };
	}

	

	/**
	 * This will test that charging with credit card with card object should
	 * pass
	 * 
	 * @throws Exception
	 */
	@Test(dataProvider = "cc-data")
	public void testCCCharge(String[][] testData) throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("card", updateMapCard(testData));

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateChargeRes(response, 201, Status.CAPTURED.toString());
		Assert.assertEquals(result.get("currency"), "USD");
		Assert.assertEquals(result.get("amount").toString(), "0.01");
		
		
	}

	/**
	 * This will test that charging credit card with non USD will throw error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeNonUSD() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "TWD");
		mapBody.put("card", returnMapCard());

	/*	String body = TestUtil.readFile(CC_CHARGE_TEMPLATE_PATH);
		System.out.println("body " + body);
		body = body.replaceFirst("USD", "TWD");*/
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateIfIsError(response);

		
	}

	/**
	 * This will test that charging CC with device info should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithDeviceInfo() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		mapCardContext.put("deviceInfo", deviceInfo);
		
		mapBody.put("context", mapCardContext);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateChargeRes(response, 201, Status.CAPTURED.toString());

		HashMap<String, Object> actualDeviceInfo = ((HashMap<String, Object>) ((HashMap<String, Object>) result
				.get("context")).get("deviceInfo"));
		
		deviceInfo.remove("encrypted");
		Assert.assertEquals(TestUtil.compareJsonMap(actualDeviceInfo,
				deviceInfo), true);
	}

	/**
	 * This will test charging with cardcontext including tax and
	 * senderaccountid should succeed
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCCChargeWithCardContext() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		mapCardContext.put("tax", "0.02");
		//mapCardContext.put("sender_account_id", "sender_account_id");

		mapBody.put("context", mapCardContext);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateChargeRes(response, 201, Status.CAPTURED.toString());

		String actualTax =  ((HashMap<String, Object>) result
				.get("context")).get("tax").toString();
		Assert.assertEquals(actualTax, "0.02");
		//String actualSenderAccountid = ((HashMap<String, Object>) result.get("context")).get("sender_account_id").toString();
		//Assert.assertEquals(actualSenderAccountid,"sender_account_id" );

	}
	
	/**
	 * This will test charge with description should succeed
	 * 
	 * 
	 */
	@Test
	public void testCCChargeDescription() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		
		String Description = "Charge 1.00 US Dollar";
		mapBody.put("description", Description);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> map = validateChargeRes(response, 201, Status.CAPTURED.toString());
		String chargeid = (String) map.get("id");
		
		String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid }};
		String CCRetrievalUrl = buildUrl(RETRIEVAL_CC, realmid, chargeid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		HashMap<String, Object> retrievalMap = validateSuccessfulRetrievalRes(retrievalResponse);
		Assert.assertEquals(retrievalMap.get("description").toString(),Description);
	}

	/**
	 * This will test that charging with vaultid should succeed
	 * 
	 * @throws Exception
	 */
	// @Test
	public void testCCChargewithVaultId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.05);mapBody.put("currency", "USD");
		mapBody.put("card_on_file", "");
		// TODO: token service is down. run when it's fixed
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(response, 201, Status.CAPTURED.toString());

	}

	
	/**
	 * This will test that charging with credit card with card object for
	 * recurring should pass
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeRecurring() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		mapCardContext.put("recurring", true);
		mapBody.put("context", mapCardContext);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		System.out.println(Status.AUTHORIZED.getText());

		validateChargeRes(response, 201, Status.CAPTURED.toString());

		


	}
	
	/**
	 * This will test charging with one time CC token should succeed
	 * @throws Exception
	 */
	@Test(groups = { "token" })
	public void testChargeOneTimeTokenCC() throws Exception{
		TestUtil.auth = "apikey";
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("token", generateOneTimeToken(returnMapCard()));
		TestUtil.auth= origAuth;
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(response, 201, Status.CAPTURED.toString());
	}
	
	/**
	 * This will test charging with one time CC token should succeed after 14 min of creating the token
	 * @throws Exception
	 */
	@Test(groups = { "token", "time-consuming" })
	public void testChargeOneTimeTokenCCAfter14min() throws Exception{
		TestUtil.auth = "apikey";
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("token", generateOneTimeToken(returnMapCard()));
		TestUtil.auth= origAuth;
		
		System.out.println("Waiting for 14 min...");
		Thread.sleep(60*14*1000);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(response, 201, Status.CAPTURED.toString());
	}
	

	
	/*
	 * *****************************************************
	 * CC Charge Negative Test Cases
	 * *****************************************************
	 */
	
	/**
	 * This will test charging with invalid one time CC token should throw error
	 * @throws Exception
	 */
	@Test(groups = { "token" })
	public void testChargeInvalidOneTimeTokenCC() throws Exception{
		String token = "lucy1234";
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("token", token);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response,400,"invalid_request","PMT-4000","Token is invalid.", "Token", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test charging with same one time CC token twice should throw error at 2nd time
	 * @throws Exception
	 */
	@Test(groups = { "token" })
	public void testChargeOneTimeTokenCCTwice() throws Exception{
		TestUtil.auth = "apikey";
		String token = generateOneTimeToken(returnMapCard());
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("token",token );
		TestUtil.auth= origAuth;
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateChargeRes(response, 201, Status.CAPTURED.toString());
		
		response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response,400,"invalid_request","PMT-4000","Token is invalid.", "Token", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}
	
	/**
	 * This will test charging with one time CC token should throw error after 15 min of creating the token
	 * @throws Exception
	 */
	@Test(groups = { "token", "time-consuming" })
	public void testChargeOneTimeTokenCCAfter15min() throws Exception{
		TestUtil.auth = "apikey";
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);mapBody.put("currency", "USD");
		mapBody.put("token", generateOneTimeToken(returnMapCard()));
		TestUtil.auth= origAuth;
		
		System.out.println("Waiting for token to expire after 15 min...");
		Thread.sleep(60*15*1000);
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response,400,"invalid_request","PMT-4000","Token is invalid.", "Token", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	
	/**
	 * This will test that charging with no amount should return error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithNoExpYear() throws Exception {
		String[][] testData = new String[][] {
				{"expYear",""}
				
		};
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1);
		mapBody.put("card", updateMapCard(testData));

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateIfIsError(response);
	}
	
	/**
	 * This will test that charging with field exceeding max length should return error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithExceedMaxLength() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", "1");
		mapBody.put("card", returnMapCard());
		HashMap<String, Object> mapContext = new HashMap<String, Object>();
		mapContext.put("batch_id", "1234*");
		mapBody.put("context", mapContext);
		

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateIfIsError(response);
	}
	
	/**
	 * This will test that charging with no amount should return error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithNoAmt() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", "");
		mapBody.put("card", returnMapCard());

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateIfIsError(response);
	}
	
	/**
	 * This will test that charging with no amount should return error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithNoCCNum() throws Exception {
		String[][] testData = new String[][] {
				{"number",""}
				
		};
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1);
		mapBody.put("card", updateMapCard(testData));

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateIfIsError(response);
	}

	/**
	 * This will test that charging with no amount should return error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithZeroAmt() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0);
		mapBody.put("card", returnMapCard());

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateIfIsError(response);
	}
	
	/**
	 * Charge $10 (add deviceId greater than 40 characters)
	 * Expected Response - PMT-4000 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeWithLongDeviceId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();

		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		
		deviceInfo.put("id",
				"00000000000000000000000000000000000000000000001234");
		mapCardContext.put("deviceInfo", deviceInfo);
		
		mapBody.put("context", mapCardContext);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, "PMT-4000");
	}
	
	/**
	 * Charge $10 (add deviceType greater than 200 characters)
	 * Expected Response - PMT-4000 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeLongDeviceType() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

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
		mapBody.put("context", mapCardContext);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, "PMT-4000");
	}
	
	/**
	 * Charge $10 (add macAddress greater than 60 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCChargeMacAddress() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

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
		mapBody.put("context", mapCardContext);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, "PMT-4000");
	}
	
	/**
	 * Charge $10 (add latitude greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCChargeLongLatitude() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();

		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("latitude",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);
		mapBody.put("context", mapCardContext);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, "PMT-4000");
	}
	
	/**
	 * Charge $10 (add longitude greater than 20 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCChargeLongLongitude() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();

		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("longitude",
				"123456789012345678901");
		mapCardContext.put("deviceInfo", deviceInfo);
		mapBody.put("context", mapCardContext);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, "PMT-4000");
	}
	
	/**
	 * Charge $10 (add ipAddress greater than 40 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	@Test
	public void testCCChargeLongIPAddress() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();

		HashMap<String, Object> deviceInfo = constructMapDeviceInfo();
		deviceInfo.put("ipAddress",
				"123456789012345678901234567890123456789012");
		mapCardContext.put("deviceInfo", deviceInfo);
		mapBody.put("context", mapCardContext);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, "PMT-4000");
	}
	
	
	/**
	 * Charge $10 (add Long Postal code greater than 10 characters)
	 * Expected Response - PMT-4000
	 * @throws Exception
	 */
	
	@Test
	public void testCCChargeLongPostalCode() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCard();
		HashMap<String, Object> address = returnMapAddress();
		address.put("postalCode", "00000777777777777777755555555555555");
		
		card.put("address", address);
		
		mapBody.put("card", card);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		
		validateErrorRes(response, "PMT-4000");
	}
	
	
	@Test
	public void testCCChargeLongAddressField() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 10.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCard();
		HashMap<String, Object> address = returnMapAddress();
		address.put("streetAdress", "1234567891234567890123456789012");
		
		card.put("address", address);
		
		mapBody.put("card", card);

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		
		validateErrorRes(response, "PMT-4000");
		
		
		
	}
	
	/**
	 * Charge for $200 and two or more thread should trying to Refund at the
	 * same time
	 * 
	 * Exception: only one should succeed and other two should fail to refund it
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCCChargeMoreThanTwoThread() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 200.00);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);

		HashMap<String, Object> chargeResponseMap = validateChargeRes(response,
				201, Status.CAPTURED.toString());

		// Refund execution with multiple thread
		String chargeId = (String) chargeResponseMap.get("id");
		String CCRefundUrl = buildUrl(REFUND_CC, realmid, chargeId);
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", 200.00);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		List<Future<HttpResponse>> refundResponseList = new ArrayList<Future<HttpResponse>>();

		Refund refund01 = new Refund(refundBody, CCRefundUrl,
				CCChargeHeaderParams);
		Refund refund02 = new Refund(refundBody, CCRefundUrl,
				CCChargeHeaderParams);
		Refund refund03 = new Refund(refundBody, CCRefundUrl,
				CCChargeHeaderParams);

		Future<HttpResponse> res01 = executor.submit(refund01);
		Future<HttpResponse> res02 = executor.submit(refund02);
		Future<HttpResponse> res03 = executor.submit(refund03);

		// Wait for all thread completes its execution
		executor.shutdown();
		executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

		refundResponseList.add(res01);
		refundResponseList.add(res02);
		refundResponseList.add(res03);

		int count = 0;
		for (Future<HttpResponse> future : refundResponseList) {
			HttpResponse refundResponse = future.get();
			if (refundResponse.getStatusLine().getStatusCode() == 201) {
				HashMap<String, Object> refundResponseMap = validateRefundRes(
						refundResponse, 201, Status.ISSUED.toString());
				Assert.assertEquals("200.00",
						(String) refundResponseMap.get("amount"));
				count++;
			} else {
				validateErrorRes(refundResponse, "PMT-4000");
			}
		}
		// Verify that only one thread should success to validate
		Assert.assertEquals(Integer.valueOf(count), Integer.valueOf(1));
	}
	 
	private static String getResultBody(HttpResponse response) {
		BufferedReader rd = null;

		StringBuffer result = new StringBuffer();
		String line = null;

		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("result is " + result);
		return result.toString();
	}
}

/**
 * Thread for invoke Refund operation
 */
class Refund implements Callable<HttpResponse> {

	private HashMap<String, Object> refundBody;
	private String CCRefundUrl;
	private String[][] CCRefundHeaderParams;

	public Refund(HashMap<String, Object> refundBody, String CCRefundUrl,
			String[][] CCRefundHeaderParams) {
		this.refundBody = refundBody;
		this.CCRefundUrl = CCRefundUrl;
		this.CCRefundHeaderParams = CCRefundHeaderParams;
	}

	@Override
	public HttpResponse call() throws Exception {
		HttpResponse refundResponse;
		refundResponse = TestUtil.post(CCRefundUrl, CCRefundHeaderParams,
				refundBody);
		return refundResponse;
	}
}
