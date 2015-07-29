package qbmssdk.transactions;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class AuthorizeChargeTest extends BaseTest{
	
	private String branchToAdd = "<Comment>Voice Auth</Comment>";
	
	/**
	 * This method tests that Credit Card Voice Authorization with the comment 
	 * should succeed and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testCCVoiceAuthWithComment() throws Exception {
		
		String xml = testUtil.insertXmlTree("/xml/cc/cc-voice-auth.xml", "//CustomerCreditCardVoiceAuthRq", branchToAdd);
		
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), xml, testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardVoiceAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", "4269283000000102");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");						
	}
	
	/**
	 * This method tests that Credit Card Voice Authorization with the token case 
	 * should succeed and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testCCVoiceAuthWithToken() throws Exception {
		String ccToken = "<CreditCardToken>99949944415411111</CreditCardToken>";
		
		String is = testUtil.replaceXmlTree("/xml/cc/cc-voice-auth.xml", "//CreditCardNumber", ccToken);
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};							
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardVoiceAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", "4269283000000102");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");						
	}
	
	/**
	 * This method tests that authorize the credit card then capture
	 * should succeed and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testAuthCapture() throws Exception{
		String lodging_xml = buildLodgingXML("/xml/cc/auth.xml", "//CustomerCreditCardAuthRq");
		String[][] testData1 = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml, testData1);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");

		ccTransId = "<CreditCardTransID> " + ccTransId + "</CreditCardTransID>";
		
		String is = testUtil.replaceXmlTree("/xml/cc/capture.xml", "//CreditCardTransID", ccTransId);
		String[][] testData2 = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp_charge = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData2);
		String res_charge = resp_charge.getResponseContentAsString();

		XPathAsserts.assertXPath(res_charge, "//CustomerCreditCardCaptureRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_charge, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res_charge, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res_charge, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res_charge, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res_charge, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res_charge, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res_charge, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res_charge, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res_charge, "//ClientTransID", ".+");	
		
/**		Fail in Emulator
		String ccTransId_capture = XPathUtils.applyXpath(res, "//CreditCardTransID");
		
		InputStream is_void = getClass().getResourceAsStream("/xml/void-cc.xml");	
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, { "//CreditCardTransID", ccTransId_capture} };
		
		Response resp_v = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is_void, testData);
		String res_v = resp_v.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res_v, "//CustomerCreditCardTxnVoidRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_v, "//CreditCardTransID", ".+");		*/
	}
	
	/**
	 * This method tests that authorize the credit card with MTS token
	 * should succeed and return the charge detail.
	 * @throws Exception
	 */
	//@Test		we don't support MTS token now.
	public void testAuthWithToken() throws Exception{
		InputStream xml = getClass().getResourceAsStream("/xml/cc/auth-token.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
	}
	
	/**
	 * This method tests that Credit Card Authorization with PTS token
	 * should succeed and return the authorization detail.
	 * @throws Exception
	 */
	@Test
	public void testAuthWithPTSToken() throws Exception{
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()},
								{"//CreditCardToken", "99949944415411111"},
							  };
		InputStream xml = getClass().getResourceAsStream("/xml/cc/auth-token.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
	}
	
	/**
	 * This method tests that Credit Card Authorization with Track2data case
	 * should succeed and return the authorization detail.
	 * @throws Exception
	 */
	@Test
	public void testAuthWithTrack2data() throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/auth-track2data.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
	}
	
	
}
