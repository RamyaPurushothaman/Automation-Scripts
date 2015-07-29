package mastermerchant;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import junit.framework.Assert;

import org.jdom2.JDOMException;
import org.testng.annotations.Test;

import utils.BaseTest;
import utils.Configuration;
import utils.DbUtil;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

public class CCTransactionVerificationTest extends BaseTest {
	
	
	private String[][] getCardData(String s) {
		String[][] CardData = { {"//ConnectionTicket", connTicket},
								{ "//TransRequestID", UUID.randomUUID().toString()},
								{"//CreditCardNumber", s} };
		return CardData;
	}
	
	protected String getMMChargeCC() throws Exception {	
		String uid = testUtil.getIntuitId();
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");	
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMData(uid, mmCCCardNum));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		return XPathUtils.applyXpath(res, "//CreditCardTransID");							
	}
	
	@Test
	public void testChargeMM() throws Exception {	
		String uid = testUtil.getIntuitId();		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMData(uid, mmCCCardNum));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");		
		
		//Validate the new added column in database
		//SENumber is from CSR search result
		ResultSet ret;
		Connection conn;
		Statement statment;
		String SENumber = "2201126851";
		String sql = "select * from mas_txn where CLIENT_TXN_ID = " + "'" 
				+ XPathUtils.applyXpath(res, "//ClientTransID") + "'";
		
		conn = DbUtil.connectDB(); 
		statment = conn.createStatement();
		ret = statment.executeQuery(sql);
		
		Assert.assertEquals(ret.next(),true);
		Assert.assertNotNull(ret.getString("CARD_INFO_DURBIN"));
		Assert.assertNotNull(ret.getString("CARD_INFO_COMMERCIAL"));
		Assert.assertNotNull(ret.getString("CARD_INFO_PREPAID"));
		Assert.assertNotNull(ret.getString("CARD_INFO_PAYROLL"));
		Assert.assertNotNull(ret.getString("CARD_INFO_HEALTHCARE"));
		Assert.assertNotNull(ret.getString("CARD_INFO_AFFLUENT"));
		Assert.assertNotNull(ret.getString("CARD_INFO_SIGNATUREDEBIT"));
		Assert.assertNotNull(ret.getString("CARD_INFO_PINLESSDEBIT"));
		Assert.assertNotNull(ret.getString("CARD_INFO_LEVEL3"));
		Assert.assertNotNull(ret.getString("CARD_INFO_ISSUING_COUNTRY"));
		Assert.assertNotNull(ret.getString("IPS_AMEX_SENUM"));
		Assert.assertEquals(SENumber,ret.getString("IPS_AMEX_SENUM"));
		
		ret.close();
		statment.close();
		conn.close();
	}
	
	@Test
	public void testAmexType() throws Exception {
		String uid = testUtil.getIntuitId();		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMData(uid, mmCCCardNum));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
		ResultSet ret;
		Connection conn;
		Statement statment;
		
		//In QBN,add attribute "amexType" to one of the following value : Direct/ESA/OnePoint
		String amexType = "ESA";
		String sql = "select * from mas_txn where CLIENT_TXN_ID = " + "'" 
				+ XPathUtils.applyXpath(res, "//ClientTransID") + "'";
		
		conn = DbUtil.connectDB(); 
		statment = conn.createStatement();
		ret = statment.executeQuery(sql);
		
		Assert.assertEquals(ret.next(),true);
		Assert.assertEquals(amexType,ret.getString("MERCHANT_AMEX_TYPE"));
		
		ret.close();
		statment.close();
		conn.close();
		
	}
	
	@Test
	public void testCCChargeTrack1Data() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-track1data.xml");
		
		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//Track1Data", "%B5500005555555559^Grace/Xu ^2209961767676hghghghghg123456?"},
								{"//CreditCardAddress", "6 WILLOW ST"},
								{"//CreditCardCity", "Manchester"},
								{"//CreditCardState", "NH"},
								{"//CreditCardPostalCode", "03103"}
							  };
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);

		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "4");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	//@Test
	public void testCCChargeAmexTrack1Data() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-track1data.xml");
		
		String connTicket = "SDK-TGT-142-rlQSGhtiQmkxfTYpVa7P$w";
		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//Track1Data", "%B371016057771005^Grace/Xu ^2209961767676hghghghghg123456?"},
							  };
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);

		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "4");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	// on PTC E2E, used as 2nd MerchantAccountNumber 5247710015523798
	
	@Test
	public void testChargeCC2ndAccnt() throws Exception {	
		String uid = testUtil.getIntuitId();		
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMTicket(uid));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");					
	}

	@Test
	public void testRefundMM() throws Exception {
		String uid = testUtil.getIntuitId();		
		InputStream is = getClass().getResourceAsStream("/xml/cc/refund-cc.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMData(uid, mmCCCardNum));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardRefundRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testVoidVisa() throws Exception {
		String ccTransId = getMMChargeCC();		
		InputStream is = getClass().getResourceAsStream("/xml/cc/void-cc.xml");
					
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, 
												{ "//CreditCardTransID", ccTransId},
												{"//ApplicationLogin", "mm.intuit.com"},
												{"//ConnectionTicket", connTicket},
											 };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardTxnVoidRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");								
	}
	
	@Test
	public void testVoidOrRefundMM() throws Exception {
		String ccTransId = getMMChargeCC();
		InputStream is = getClass().getResourceAsStream("/xml/cc/void-or-refund-cc.xml");	
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, 
												{ "//CreditCardTransID", ccTransId},
												{"//ApplicationLogin", "mm.intuit.com"},
												{"//ConnectionTicket", connTicket},
			 								  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardTxnVoidOrRefundRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");		
	}
	
	@Test
	public void testCCVoiceAuthWithComment() throws Exception {
		String uid = testUtil.getIntuitId();	
		InputStream is = getClass().getResourceAsStream("/xml/cc/cc-voice-auth.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMData(uid, mmCCCardNum));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardVoiceAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".*");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");						
	}
	
	@Test
	public void testAuthCapture() throws Exception{

		String lodging_xml = buildLodgingXML("/xml/cc/auth.xml", "//CustomerCreditCardAuthRq");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml, getMMData(UUID.randomUUID().toString(), mmCCCardNum) );
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
		ccTransId = "<CreditCardTransID> " + ccTransId + "</CreditCardTransID>";

		String is = testUtil.replaceXmlTree("/xml/cc/capture.xml", "//CreditCardTransID", ccTransId);
		
		try {
		    Thread.sleep(2000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		Response resp_charge = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, getMMData(UUID.randomUUID().toString(), mmCCCardNum));
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
	}
	
	@Test
	public void testAuthCaptureZero() throws Exception{
		
		String lodging_xml = buildLodgingXML("/xml/cc/auth-zero.xml", "//CustomerCreditCardAuthRq");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml, getMMData(UUID.randomUUID().toString(), mmCCCardNum) );
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
		ccTransId = "<CreditCardTransID> " + ccTransId + "</CreditCardTransID>";

		String is = testUtil.replaceXmlTree("/xml/cc/capture-zero.xml", "//CreditCardTransID", ccTransId);
		
		try {
		    Thread.sleep(2000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		Response resp_charge = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, getMMData(UUID.randomUUID().toString(), mmCCCardNum));
		String res_charge = resp_charge.getResponseContentAsString();

		XPathAsserts.assertXPath(res_charge, "//CustomerCreditCardCaptureRs/@statusCode", "10300");	
		
	}
	
	@Test
	public void testChargeRestaurant() throws Exception {
		String transReqId = testUtil.getIntuitId();
		String new_xml = testUtil.insertXmlTree("/xml/cc/charge-cc.xml","//CustomerCreditCardChargeRq", addRestaurant());
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), new_xml, getMMData(transReqId, mmCCCardNum));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");				
	}
	
	@Test
	public void testChargeVoidDiscoverCard() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("6011016011016011"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "3");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");		
		
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
	
		InputStream void_xml = getClass().getResourceAsStream("/xml/cc/void-cc.xml");
					
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, 
												{ "//CreditCardTransID", ccTransId},
												{"//ApplicationLogin", "mm.intuit.com"},
												{"//ConnectionTicket", connTicket},
											 };
		
		Response resp_void = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(void_xml), testData);
		String res_void = resp_void.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res_void, "//CustomerCreditCardTxnVoidRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_void, "//CreditCardTransID", ".+");								
	}
	
	@Test
	public void testChargeMasterCard() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("5111005111051128"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "4");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	/**
	 * This will test that charging Amex for amex enabled master merchants should succeed
	 * @throws Exception
	 */
	@Test
	public void testChargeAmex() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		String[][] testdata = getCardData("373953192351004");
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testdata);

		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "2");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");					
	}
	
	@Test
	public void testChargeJCB() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("3566003566003566"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "7");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testChargeDiscoverDiners() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("36110361103612"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "7");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testChargeVisaCorporateCard() throws Exception {	
				
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("4114360123456785"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testChargeVisaCommercialCard() throws Exception {	

		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("4110144110144115"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");				
			
	}
	
	@Test
	public void testChargeVisaCommercialCardHas25CommercialCode() throws Exception {	
		
		String new_branch = buildXmlBranch("CommercialCardCode", "abcdefgHIJKLMNOPQrstuvwxy");
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", new_branch);				
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, getCardData("4110144110144115"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");							
	}
	
	@Test
	public void testChargeVisaCommercialCardCommercialCodeOver25() throws Exception {	
		
		String new_branch = buildXmlBranch("CommercialCardCode", "abcdefgHIJKLMNOPQrstuvwxyz");
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", new_branch);				
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, getCardData("4110144110144115"));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10304");					
	}
	
	@Test
	public void testChargeDiners() throws Exception {	
					
			InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
			Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getCardData("36438936438936"));
			String res = resp.getResponseContentAsString();
			
			XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
			XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
			XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
			XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
			XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
			XPathAsserts.assertXPath(res, "//AVSZip", ".+");
			XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
			XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
			XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "7");
			XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
			XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
			XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
			XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
		}
	
	
}
