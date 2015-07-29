package mastermerchant;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import utils.BaseTest;
import utils.Configuration;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

public class CCTransactionValidationTest extends BaseTest{
	
	private String branchToAdd = "<BatchID>2360</BatchID>";

	@Test
	public void testChargeMMHasBatchId() throws Exception {	
		String uid = testUtil.getIntuitId();
		String xml = testUtil.insertXmlTree("/xml/cc/charge-cc.xml", "//CustomerCreditCardChargeRq", branchToAdd);

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), xml, getMMTicket(uid));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "0");
		
	}
	
	/**
	 * This tests that charging amex disabled merchants will return appropriate error 
	 * @throws Exception
	 * Disable this test case since not able to create connection ticket for the merchant in PTC
	 */
	//@Test
	public void testChargeAmexDisabledMerchants() throws Exception {	
		String[][] amexData = {
								{"//CreditCardNumber", "345674726530781"},
								{"//ConnectionTicket", "SDK-TGT-212-NT_fodF_Px8AfTa9Zj5dzg"},
								//{"//ConnectionTicket", "SDK-TGT-13-BlGacwbegEUbAYvxraIX1A"},
								{ "//TransRequestID", UUID.randomUUID().toString()}, 
							  };		
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), amexData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10402");
	}
	
	/**
	 * This tests that authing amex disabled merchants will return appropriate error 
	 * @throws Exception
	 * Disable this test case since not able to create connection ticket for the merchant in PTC
	 */
	
	//@Test
	public void testAuthAmex() throws Exception {
		String[][] mmData = getMMData(UUID.randomUUID().toString(),mmCCCardNum);

		String[][] mmDataNew = mmData.clone();
		mmDataNew[3][1] = "345674726530781";
		String s = Configuration.test_env;
		if (s.equals("https://merchantaccount.qa.quickbooks.com/j/AppGateway")||s.equals("https://merchantaccount2.qa.quickbooks.com/j/AppGateway")) 
			mmDataNew[1][1] = "SDK-TGT-212-NT_fodF_Px8AfTa9Zj5dzg";
		
		//mmDataNew[1][1] = "SDK-TGT-13-BlGacwbegEUbAYvxraIX1A";

		
		InputStream is = getClass().getResourceAsStream("/xml/cc/auth.xml");					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), mmDataNew);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusCode", "10402");
	}
	
	@Test
	public void testLodgingIncrmentalAuthCapture() throws Exception {
		String lodging_xml = buildLodgingXML("/xml/cc/auth.xml", "//CustomerCreditCardAuthRq");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml, getMMData(UUID.randomUUID().toString(), mmCCCardNum));
		String res = resp.getResponseContentAsString();		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
		
		String lodgingExtend = buildLodgingXML("/xml/cc/incremental-auth.xml", "//CustomerCreditCardTxnIncrementalAuthRq");	
		String[][] testData = new String[][] {	{"//ApplicationLogin", "mm.intuit.com"},
												{"//ConnectionTicket", connTicket},
												{ "//CreditCardTransID", ccTransId },
												{"//CreditCardNumber", "371449635398431"},
												{ "//TransRequestID", UUID.randomUUID().toString()},
											 };
		
		Response resp_ia = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodgingExtend, testData);
		String res_ia = resp_ia.getResponseContentAsString();
		XPathAsserts.assertXPath(res_ia, "//CustomerCreditCardTxnIncrementalAuthRs/@statusCode", "10501");	
	}
	
	@Test
	public void testChargePINDebitCard() throws Exception {	
		String uid = testUtil.getIntuitId();		
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMTicket(uid));
		String res = resp.getResponseContentAsString();
		
		//XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10402");
	}
	
}
