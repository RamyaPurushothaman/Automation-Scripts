package qbmssdk.check;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.jdom2.JDOMException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import utils.BaseTest;
import utils.Configuration;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

public class checkVerificationTest extends BaseTest {
	
	private String xmlPath = "/xml/check/check-verification.xml";
	private String pxml = "";
	
	@BeforeClass
	public void setup() throws JDOMException, IOException {		
		String tree = "<PersonalPaymentInfo>" +
	                      "<PersonalDebitAccountType>Savings</PersonalDebitAccountType>" +
	                      "<PayorFirstName>TestFName</PayorFirstName>" +
	                      "<PayorLastName>TestLName</PayorLastName>" +
                       "</PersonalPaymentInfo>";
		
	    pxml  = testUtil.replaceXmlTree(xmlPath, "//BusinessPaymentInfo", tree);
	}
	
	
	@DataProvider(name="checkPaymentModeSet")
	public Object[][] checkDataProvider() {	
		return new Object[][] {	 {"Telephone"}, { "Internet"}	};		
	}
	
	@Test(dataProvider="checkPaymentModeSet",enabled = false)
	public void testCheckVerificationPaymentMode(String pMode) throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PaymentMode", pMode}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CheckTransID", ".+");
		XPathAsserts.assertXPath(res, "//CheckAuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");						
	}
	
	@Test(dataProvider="checkPaymentModeSet",enabled = false)
	public void testCheckVerificationUsePersonalPaymentInfo(String pMode) throws Exception {
				
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PaymentMode", pMode}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), pxml, testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CheckTransID", ".+");
		XPathAsserts.assertXPath(res, "//CheckAuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");						
	}
	
	// Validation tests
	
	@Test
	public void testCheckVerificationWithInvalidAccountNumber() throws Exception {
				
		InputStream is = getClass().getResourceAsStream("/xml/check/check-verification.xml");		
		String[][] accntData = new String[][] {{"//TransRequestID", UUID.randomUUID().toString()}, {"//AccountNumber","11.0"}};
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), accntData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10309");
	}
	
	@Test
	public void testCheckVerificationNoAccountNumber() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);		
		String[][] accntData = new String[][] {{"//TransRequestID", UUID.randomUUID().toString()}, {"//AccountNumber",""}};
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), accntData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10303");
	}
	
	@Test
	public void testCheckInvalidPaymentMode() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PaymentMode", "Xyz"}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10310");					
	}
	
	@Test
	public void testCheckNoPaymentMode() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PaymentMode", ""}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10303");					
	}
	
	@Test
	public void testCheckNoPayorPhone() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PayorPhoneNumber", ""}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10303");					
	}
	
	// it does not take 123-123-1234, should be a bug?
	@Test
	public void testCheckInvalidPayorPhone() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PayorPhoneNumber", "12312312345"}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10304");					
	}
	
	@Test
	public void testCheckNoRoutingNum() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//RoutingNumber", ""}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10303");					
	}
	
	@Test
	public void testCheckInvalidRoutingNum() throws Exception {
				
		InputStream is = getClass().getResourceAsStream(xmlPath);
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//RoutingNumber", "123456789"}};	
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10312");					
	}
	
	@Test
	public void testCheckUsePersonalInfoNoPayorLastname() throws Exception {
				
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}, {"//PayorLastName", ""}};

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), pxml, testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckVerificationRs/@statusCode", "10303");					
	}
	
}
