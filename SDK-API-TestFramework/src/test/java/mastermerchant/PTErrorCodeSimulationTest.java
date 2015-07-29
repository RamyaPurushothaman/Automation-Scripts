package mastermerchant;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

/**
 * This test the paymentTech error code. To generate specific error code, put in a specific amount.
 * See https://wiki.intuit.com/display/psdpd/MM+SDK+Test+Strategy+and+Coverage for more details
 *
 */
public class PTErrorCodeSimulationTest extends BaseTest {
	
	public String[][] getTestData(String s) {
		String[][] testData = {	{"//TransRequestID", UUID.randomUUID().toString()},
								{"//Amount", s},
								{"//ConnectionTicket", connTicket},
							  };		
		return testData;		
	}
	
	@Test
	public void testInvalidAccountNum() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getTestData("201.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10301");							
	}
	
	@Test
	public void testInvalidMCC() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getTestData("249.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");							
	}
	
	@Test
	public void testInvalidTransactionType() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("253.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");							
	}
	
	@Test
	public void testNotOnFile() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("301.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");							
	}
	
	@Test
	public void testCreditFloor() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("302.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10400");							
	}
	
	@Test
	public void testdefaultCall() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("402.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10404");							
	}
	
	@Test
	public void testPickup() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("501.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10401");							
	}
	
	@Test
	public void testLost() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("502.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10401");							
	}
	
	@Test
	public void testInsufficientFunds() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("521.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10400");							
	}
	
	@Test
	public void testBadAmount() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is), getTestData("592.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10300");							
	}
	
	@Test
	public void testInvalidCCNumber() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("201.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10301");							
	}
	
	@Test
	public void testInvalidExpirationDate() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("605.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10302");							
	}
	
	@Test
	public void testExpiredCard() throws Exception {	
		
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("522.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10302");							
	}
	
	@Test
	public void testCVV2Failure() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("531.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10401");							
	}
	
	@Test
	public void testAccountClosed() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("754.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10401");							
	}
	
	@Test
	public void testCall() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("401.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10404");							
	}
	
	@Test
	public void testAuthroizationFail() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("307.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10405");					
	}
	
	@Test
	public void testAgressivePINTry() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("508.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10414");							
	}
	
	@Test
	public void testRestraint() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("806.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10401");							
	}
	
	@Test
	public void testNoAccount() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("825.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10401");							
	}
	
	@Test
	public void testInvlaidMerchant() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("833.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10403");							
	}
	
	@Test
	public void testExceededLimitAmount() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("1100000.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10300");							
	}
	
	/**
	 * The test case that charge individual transaction for 99999.99(SDK transaction amount limit) should succeed and
	 * return 0. 
	 * @throws Exception
	 */
	//@Test disabling the test since the new division id we used for pin debit testing is diff config for this test case
	public void test99KLimit() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("99999.99"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "0");							
	}
	
	/**
	 * The test case that charge individual transaction for more than 99999.99 should fail and
	 * return error code 10300. 
	 * @throws Exception
	 */
	@Test
	public void testExceed99KLimit() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("100000.00"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10300");							
	}
	
	@Test
	public void testSmallAmount() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is),  getTestData("0.01"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "0");							
	}
}
