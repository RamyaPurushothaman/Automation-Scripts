package com.intuit.paymentsapi.us;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class TokenTest extends BaseTest{
	
	
	static String origAuth;
	
	@BeforeMethod(alwaysRun=true)
	public void setup(){
		origAuth = TestUtil.auth;
		TestUtil.auth = "apikey";
	}
	
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
	
	
	public static String[][] tokenHeaderParams = { /*{ "company_id", realmid },
												   { "request_id", UUID.randomUUID().toString()}*/
												 };
	
	public static String tokenUrl = buildUrl(CREATE_TOKEN);
	
	
	/**
	 * Test that creating a token with card number should suceed
	 * @throws Exception
	 */
	@Test
	public void testCreateCCToken() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("card", returnMapCard());
		HttpResponse response = TestUtil.post(tokenUrl,
				tokenHeaderParams, mapBody);
		validateSuccessfulTokenRes(response);
	}
	
	/**
	 * Test that creating a token with bank account should succeed.
	 * Comment out the test because token with bank account has been removed 
	 * @throws Exception
	 */
	//@Test
	public void testCreateBankAcctToken() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("bankAccount", constructBankAcct());
		HttpResponse response = TestUtil.post(tokenUrl,
				tokenHeaderParams, mapBody);
		validateSuccessfulTokenRes(response);
	}
	
	/**
	 * Test that creating a token with same card twice should succeed with two different token values
	 * @throws Exception
	 */
	@Test
	public void testCreateCCTokenTwice() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("card", returnMapCard());
		HttpResponse response = TestUtil.post(tokenUrl,
				tokenHeaderParams, mapBody);
		HashMap<String, Object> result = validateSuccessfulTokenRes(response);
		String token1 = (String)result.get("value");
		response = TestUtil.post(tokenUrl,
				tokenHeaderParams, mapBody);
		result = validateSuccessfulTokenRes(response);
		Assert.assertNotEquals(token1, (String)result.get("value"));
	}
	
	/**
	 * This will test that creating a token using both CC and bank account will throw error
	 * @throws Exception
	 */
	//@Test
	public void testCreateCCBankActToken() throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("card", returnMapCard());
		mapBody.put("bankAccount", constructBankAcct());
		HttpResponse response = TestUtil.post(tokenUrl,
				tokenHeaderParams, mapBody);
		
		HashMap<String, Object> result = validateErrorRes(response, "PMT-4000");
		Assert.assertEquals(result.get("message"), "Validation error");
		Assert.assertEquals(result.get("detail"), "Either Card or Bank Account\n");
		Assert.assertEquals(result.get("infoLink"), "https://developer.intuit.com/docs/030_qbms/0060_documentation/error_handling");

	}
	
	
}
