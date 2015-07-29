package qbmssdk.transactions;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class chargeVoidRefund extends BaseTest{
	
	String ccTransId;
	private String branchToAdd = "<BatchID>2358</BatchID>";
	
	/**
	 * This method tests that will run before the whole test cases and 
	 * set up the credit card charge request should succeed.
	 * @throws Exception
	 */
	@BeforeMethod
	public void beforeMethod() throws Exception {
		
		String charge_xml = testUtil.insertXmlTree("/xml/cc/charge-cc.xml", "//CustomerCreditCardChargeRq", branchToAdd);
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), charge_xml, testData);
		String res = resp.getResponseContentAsString();		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		
		ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
	}
	
	/**
	 * This method tests that void the credit card charge should succeed and
	 * return the credit card transaction id.
	 * @throws Exception
	 */
	@Test
	public void testVoid() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/cc/void-cc.xml");	
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, { "//CreditCardTransID", ccTransId} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardTxnVoidRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");		
	}
	
	/**
	 * This method tests that void or refund the credit card charge should succeed.
	 * @throws Exception
	 */
	@Test
	public void testVoidOrRefund() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/cc/void-or-refund-cc.xml");	
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, { "//CreditCardTransID", ccTransId} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardTxnVoidOrRefundRs/@statusMessage", "Status OK");
	}

	/**
	 * This method tests that charge first and follow 2 void-or-refund operations should succeed and
	 * return the credit card transaction id for each void-or-refund.
	 * @throws Exception
	 */
	@Test
	public void testMultiRefundOrVoid() throws Exception {
		
		String chargeData[][] = {	{ "//TransRequestID", UUID.randomUUID().toString()},
									{"//Amount", "124.41"}	};
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), chargeData);
		String res = resp.getResponseContentAsString();	
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		
		String CCTransID= XPathUtils.applyXpath(res, "//CreditCardTransID");
		
		String[][] ref1Data = {		{ "//TransRequestID", UUID.randomUUID().toString()},
									{"//CreditCardTransID", CCTransID},
									{"//Amount", "8.73"},
			 				  };
		
		InputStream is_ref = getClass().getResourceAsStream("/xml/cc/void-or-refund-cc.xml");
					
		Response resp1 = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is_ref), ref1Data);
		String res1 = resp1.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res1, "//CustomerCreditCardTxnVoidOrRefundRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res1, "//CreditCardTransID", ".+");
		
		InputStream is_ref2 = getClass().getResourceAsStream("/xml/cc/void-or-refund-cc.xml");
		
		String[][] ref2Data = {	{ "//TransRequestID", UUID.randomUUID().toString()},
								{"//CreditCardTransID", CCTransID},
								{"//Amount", "115.68"},
							  };
		
		Response resp2 = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is_ref2), ref2Data);
		String res2 = resp2.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res2, "//CustomerCreditCardTxnVoidOrRefundRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res2, "//CreditCardTransID", ".+");
				
	}

}
