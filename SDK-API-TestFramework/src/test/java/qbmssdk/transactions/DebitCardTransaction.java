package qbmssdk.transactions;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class DebitCardTransaction extends BaseTest{
	
	@DataProvider(name="debitCardSet")
	public Object[][] dcDataProviderTest(){
		return new Object[][]{								
								{"Visa", new String[][] { 			 
									  { "//TransRequestID", UUID.randomUUID().toString() },	
									  { "//Track2Data", ";4217658964923292=12061019536190200000?" },
									}
								},
								{"Master DebitCard", new String[][] { 
										  { "//TransRequestID", UUID.randomUUID().toString() },	
										  { "//Track2Data", ";5555555555554444=13021011234566?" },
									}
								},
								{"JCB DebitCard", new String[][] { 
										  { "//TransRequestID", UUID.randomUUID().toString() },	
										  { "//Track2Data", ";3530111333300000=14021011234545?" },
									}
								},
								{"Discover DebitCard", new String[][] { 
										 { "//TransRequestID", UUID.randomUUID().toString() },	
										  { "//Track2Data", ";5555555555554444=13021011234566?" },
									}
								},
								{"Amex", new String[][] { 
										 { "//TransRequestID", UUID.randomUUID().toString() },	
										  { "//Track2Data", ";371449635398431=160810112341557?" },
									}
								}
							};
	}
	
	/**
	 * This method tests that will charge the debit card should succeed.
	 * @throws Exception
	 */
	@Test(dataProvider = "debitCardSet")
	public void testDebitCardCharge(String dcType, String[][] testData) throws Exception {
	
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-dc.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//DebitCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//NetworkName", ".+");
		XPathAsserts.assertXPath(res, "//NetworkNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "8");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");
	}
	
	/**
	 * This method tests that will charge the debit card with no PIN number and should 
	 * fail and return the error code.
	 * @throws Exception
	 */
	@Test
	public void testDebitCardNoPIN() throws Exception {
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-dc.xml");
		String[][] testData = { { "//TransRequestID", UUID.randomUUID().toString() },	
				  				{ "//PINBlock", "" }};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10303");
	}
	
	/**
	 * This method tests that will charge the debit card with PIN number less than 16 digit should 
	 * fail and return the error code.
	 * @throws Exception
	 */
	@Test
	public void testDebitPINLess16digit() throws Exception {
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-dc.xml");
		String[][] testData = { { "//TransRequestID", UUID.randomUUID().toString() },	
				  				{ "//PINBlock", "1789809890" }};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10307");
	}
	
	/**
	 * This test that using transaction id to void or refund the debit card charge should fail.
	 * @throws Exception
	 */
	@Test
	public void testDebitVoidOrRefundByTxnId() throws Exception {
		
		String[][] testData = { 
								{"//TransRequestID", UUID.randomUUID().toString()},
							    {"//Track2Data", ";4217658964923292=12061019536190200000?"}
		};
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-dc.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//DebitCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//NetworkName", ".+");
		XPathAsserts.assertXPath(res, "//NetworkNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "8");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");
		
		String debitCardTransID = XPathUtils.applyXpath(res,"//DebitCardTransID");
		
		//Then refund should fail
		is = getClass().getResourceAsStream("/xml/cc/void-or-refund-cc.xml");
		String[][] voidorRefundData = {
				{ "//TransRequestID", UUID.randomUUID().toString()}, 
				{ "//CreditCardTransID", debitCardTransID}
		};
		resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),voidorRefundData);
		res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardTxnVoidOrRefundRs/@statusCode", "10312");
	}
	
	/**
	 * This test that using debit card number to refund the debit charge should fail.
	 * @throws Exception
	 */
	@Test
	public void testDebitRefundByCardNumber() throws Exception {
		
		String ccNum = generateCreditCardNumber(VISA_PREFIX_LIST);
		String track2data = generateTrack2Data(ccNum);
		
		String[][] testData = { 
								{"//TransRequestID", UUID.randomUUID().toString()},
							    {"//Track2Data", track2data}
		};
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-dc.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//DebitCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//NetworkName", ".+");
		XPathAsserts.assertXPath(res, "//NetworkNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode","\\d");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");
		
		//Then refund should fail
		is = getClass().getResourceAsStream("/xml/cc/refund-cc.xml");
		String[][] refundData = {
				{ "//TransRequestID", UUID.randomUUID().toString()}, 
				{ "//CreditCardNumber", ccNum}
		};
		resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),refundData);
		res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardRefundRs/@statusCode", "10305");
	}
	
	/**
	 * This test that using transaction id to void the debit card charge should fail.
	 * @throws Exception
	 */
	@Test
	public void testDebitVoid() throws Exception {
		
		String[][] testData = { 
								{"//TransRequestID", UUID.randomUUID().toString()},
							    {"//Track2Data", ";4217658964923292=12061019536190200000?"}
		};
		InputStream is = getClass().getResourceAsStream("/xml/cc/charge-dc.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//DebitCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//NetworkName", ".+");
		XPathAsserts.assertXPath(res, "//NetworkNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", "[0-9]+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "8");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");
		
		String debitCardTransID = XPathUtils.applyXpath(res,"//DebitCardTransID");
		
		//Then refund should fail
		is = getClass().getResourceAsStream("/xml/cc/void-cc.xml");
		String[][] voidorRefundData = {
				{ "//TransRequestID", UUID.randomUUID().toString()}, 
				{ "//CreditCardTransID", debitCardTransID}
		};
		resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),voidorRefundData);
		res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardTxnVoidRs/@statusCode", "10405");
	}
	
	
}
