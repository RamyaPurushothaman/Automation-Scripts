package qbmssdk.transactions;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;
import utils.DbUtil;

public class ChargeRefundCCTest extends BaseTest {
	
	private String transRqID = "testRefundMultiCharge";
	
	
	/**
	 * This method tests that charge credit card should succeed and
	 * return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testChargeCC() throws Exception {
						
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),testData);
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
		
		//Validate for non Master Merchant column value, should be null
		ResultSet ret;
		Connection conn;
		Statement statment;
		String sql = "select * from mas_txn where CLIENT_TXN_ID = " + "'" 
				+ XPathUtils.applyXpath(res, "//ClientTransID") + "'";
		
		conn = DbUtil.connectDB(); 
		statment = conn.createStatement();
		ret = statment.executeQuery(sql);
		
		Assert.assertEquals(ret.next(),true);
		Assert.assertNull(ret.getString("CARD_INFO_DURBIN"));
		Assert.assertNull(ret.getString("CARD_INFO_COMMERCIAL"));
		Assert.assertNull(ret.getString("CARD_INFO_PREPAID"));
		Assert.assertNull(ret.getString("CARD_INFO_PAYROLL"));
		Assert.assertNull(ret.getString("CARD_INFO_HEALTHCARE"));
		Assert.assertNull(ret.getString("CARD_INFO_AFFLUENT"));
		Assert.assertNull(ret.getString("CARD_INFO_SIGNATUREDEBIT"));
		Assert.assertNull(ret.getString("CARD_INFO_PINLESSDEBIT"));
		Assert.assertNull(ret.getString("CARD_INFO_LEVEL3"));
		Assert.assertNull(ret.getString("CARD_INFO_ISSUING_COUNTRY"));
		Assert.assertNull(ret.getString("IPS_AMEX_SENUM"));
		
		ret.close();
		statment.close();
		conn.close();
	}
	
	/**
	 * This method tests that charge credit card should succeed and verify the Amex type in DB.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAmexType() throws Exception {
						
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),testData);
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
		
		//Validate for non Master Merchant column value, should be null
		ResultSet ret;
		Connection conn;
		Statement statment;
		String amexType = "Direct";
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
	
	/**
	 * This method tests that charge all credit card types should succeed.
	 * @throws Exception
	 */
	//@Test
	public void testChargeAllCCTypes() throws Exception {
				
		InputStream is = getClass().getResourceAsStream("/xml/cc-charge-all-types.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();

		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='100 MasterCard']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.hasXPath(xPath);
		XPathAsserts.assertXPathExists(res, "//CustomerCreditCardChargeRs[@requestID='100 MasterCard'] and CustomerCreditCardChargeRs/[@statusMessage='Status OK'");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='100 MasterCard'] and CustomerCreditCardChargeRs/[@statusMessage='Status OK'");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs[@requestID='103 Amex']/../CustomerCreditCardChargeRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
//		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
//		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
//		XPathAsserts.assertXPath(res, "//AVSStreet", ".+");
//		XPathAsserts.assertXPath(res, "//AVSZip", ".+");
//		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
//		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
//		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
//		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
//		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
//		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");

	}
	
	/**
	 * This method tests that refund from the credit card should succeed and return the 
	 * refund info.
	 * @throws Exception
	 */
	@Test
	public void testRefundCC() throws Exception {
		String walletEntryId = createCCWallet();
		String[][] testData = new String[][] {	{ "//WalletEntryID", walletEntryId },{ "//TransRequestID", UUID.randomUUID().toString() } };
				
		InputStream is = getClass().getResourceAsStream("/xml/cc/refund-cc.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
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
	
	/**
	 * This method tests that two credit card charges with same transaction request id should return same CC Transaction ID.
	 * And only first charge works.Then refund should succeed with the first charge amount.
	 * return the refund information.
	 * @throws Exception
	 */
	@Test
	public void testRefundMultiCharges() throws Exception {
		
		String creditcard = "4024007133502137";
		String chargeData1[][] = {	{"//TransRequestID", transRqID},
									{"//Amount", "8.73"},
									{"//CreditCardNumber",creditcard}
								 };
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		
		Response resp1 = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), chargeData1);
		String res1 = resp1.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res1, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		String CCTransID1= XPathUtils.applyXpath(res1, "//CreditCardTransID");
		
		String chargeData2[][] = {	{"//TransRequestID", transRqID},
									{"//Amount", "115.68"},
									{"//CreditCardNumber",creditcard}
			 					 };
		
		InputStream is2 = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		
		Response resp2 = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is2), chargeData2);
		String res2 = resp2.getResponseContentAsString();
		XPathAsserts.assertXPath(res2, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		
		String CCTransID2= XPathUtils.applyXpath(res2, "//CreditCardTransID");
		
		if (CCTransID1.equals(CCTransID2)) {
			
			String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, 
													{ "//CreditCardNumber", creditcard}
			};
		
			InputStream is_ref = getClass().getResourceAsStream("/xml/cc/refund-cc.xml");
						
			Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is_ref), testData);
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
		else {
			Assert.fail("There is an issue with TransRequestID returned ");
		}
	}
	
	/**
	 * This method tests that two charges with same transaction id and only first charge works. 
	 * So the refund amount should consider amount of the first charge.
	 * @throws Exception
	 */
	@Test
	public void testRefundMultiChargesSameTxnIdAmountExceed() throws Exception {
		
		//Randomly generate the credit card number
		String creditcard = generateCreditCardNumber(VISA_PREFIX_LIST);
		String txnId = UUID.randomUUID().toString();
		
		String chargeData1[][] = {	{"//TransRequestID", txnId},
									{"//Amount", "10.00"},
									{"//CreditCardNumber",creditcard}
								 };
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		
		Response resp1 = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), chargeData1);
		String res1 = resp1.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res1, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		String CCTransID1 = XPathUtils.applyXpath(res1, "//CreditCardTransID");
		
		String chargeData2[][] = {	{"//TransRequestID", txnId},
									{"//Amount", "100.00"},
									{"//CreditCardNumber",creditcard}
			 					 };
		
		InputStream is2 = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");
		
		Response resp2 = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is2), chargeData2);
		String res2 = resp2.getResponseContentAsString();
		XPathAsserts.assertXPath(res2, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		
		String CCTransID2 = XPathUtils.applyXpath(res2, "//CreditCardTransID");
		
		if (CCTransID1.equals(CCTransID2)) {
			
			String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()}, 
													{ "//Amount","20.00" },
													{ "//CreditCardNumber", creditcard}
			};
		
			InputStream is_ref = getClass().getResourceAsStream("/xml/cc/refund-cc.xml");
						
			Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(),  testUtil.normalXMLInputStream(is_ref), testData);
			String res = resp.getResponseContentAsString();
			
			XPathAsserts.assertXPath(res, "//CustomerCreditCardRefundRs/@statusCode", "10305");
		}
		else {
			Assert.fail("There is an issue with TransRequestID returned ");
		}
	}
	
	/**
	 * This method tests that restaurant credit card charge should succeed
	 * and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testCCChargeRestaurant() throws Exception {
		String new_xml = testUtil.insertXmlTree("/xml/cc/charge-cc.xml","//CustomerCreditCardChargeRq", addRestaurant());				
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), new_xml, testData);
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
	
	/**
	 * This method tests that credit card charge with track1data should succeed
	 * and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testCCChargeTrack1Data() throws Exception {
		String new_xml = testUtil.insertXmlTree("/xml/cc/charge-track1data.xml","//CustomerCreditCardChargeRq", addRestaurant());				
		
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}};
				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), new_xml, testData);
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
	
	/**
	 * This method tests that credit card charge with private app token should succeed
	 * and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testCCChargePrivateAppToken() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc-token.xml");				
		
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()}};
				
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
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	/**
	 * This method tests that credit card charge refund with private app token should succeed
	 * and return the charge detail.
	 * @throws Exception
	 */
	@Test
	public void testRefundWithPrivateAppToken() throws Exception {	
	    String refund_xml = testUtil.replaceXmlTree("/xml/cc/refund-cc.xml", "//CreditCardNumber", getccToken("9747883270747775"));
	    
	    String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()},
	    						{"//ApplicationLogin", "imsqadesktoptest2"},
	    						{"//ConnectionTicket", "TGT-72-ufUU_avjxYulsjmHRIxLcQ"},
	    					  };
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXML(refund_xml), testData);
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
	
	/**
	 * This method tests that charge credit card should not succeed for non-subscribed merchants and
	 * return the charge detail.
	 * @throws Exception
	 */
	@Test(groups = {"unapproved-nonreopened-merchants"})
	public void testChargeCCNonSubscribed() throws Exception {
		
				
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-cc.xml");			
		String[][] testData = { {"//TransRequestID", UUID.randomUUID().toString()} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10202");
						
	}
	
}
