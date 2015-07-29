package mastermerchant;

import org.testng.annotations.Test;
import java.io.InputStream;
import java.util.UUID;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

/**
 * Intuit MerchantLink PaymentTech certification test 
 * To test a specific error, put a specific amount.
 * https://wiki.intuit.com/display/psdpd/MM+SDK+Test+Strategy+and+Coverage
 */
public class IMPCertificationTest extends BaseTest{
	
	// select vendor_result_code from mas_txn where amount='100.99' and request_id = '33d09639-1f64-4c51-9903-30327912e122'
	
	public String[][] getTrack2Data(String reqId, String cardNum, String amt) {
		String txnData[][] = { {"//TransRequestID", reqId},
							   {"//ApplicationLogin", "mm.intuit.com"},
							   {"//ConnectionTicket", connTicket},
							   {"//Track2Data", generateTrack2Data(cardNum)},
							   {"//Amount", amt} };
		return txnData;
	}
	
	@Test(dataProvider="cardSet")
	public void testAuthCaptureWithTrack2data(String type, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/auth-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();		
		String amt = "1.00";
		
		if (type=="MC") {
			System.out.println("MC");
			amt = "3.00";
			reqId = UUID.randomUUID().toString();
		}
		else if (type=="DC") {
			amt = "7.00";
			reqId = UUID.randomUUID().toString();
		}
		else if (type=="JCB") {
			amt = "9.00";
			reqId = UUID.randomUUID().toString();
		}
		
		String testData[][] = { {"//TransRequestID", reqId},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//Track2Data", generateTrack2Data(card)},
								{"//Amount", amt} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();	
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		
		InputStream cap_xml = getClass().getResourceAsStream("/xml/cc/capture.xml");
		
		reqId = UUID.randomUUID().toString();
		String capData[][] = {  {"//TransRequestID", reqId},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//CreditCardTransID", ccTransId},
								{"//Amount", amt} };
		
		Response resp_charge = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(cap_xml), capData);
		String res_charge = resp_charge.getResponseContentAsString();

		XPathAsserts.assertXPath(res_charge, "//CustomerCreditCardCaptureRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_charge, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res_charge, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res_charge, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res_charge, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res_charge, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res_charge, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res_charge, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res_charge, "//ClientTransID", ".+");				
		
	}
	
	/**
	 * This tests the merchant link response code 202 
	 * @param c
	 * @param card
	 * @throws Exception
	 */
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode202(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "0.00");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10300");
	}
	
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode100(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "100.99");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
	}
	
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode201(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "201.77");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10301");
	}
	
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode204(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "204.44");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");
	}

	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode249(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "249.55");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");
	}
	
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode253(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "253.66");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");
	}
	
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode257(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "257.22");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		if (c == "MC")
				XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10200");
		else 
				XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
	}
		
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode301(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "301.11");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10500");			
	}
	
	@Test(dataProvider="cardSet")
	public void testChargeWithTrack2dataResCode302(String c, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/charge-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();
		
		String[][] testData = getTrack2Data(reqId, card, "302.00");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10400");			
	}
	
	@Test(dataProvider="cardSet")
	public void testCCVoiceAuth(String type, String card) throws Exception {
			
		InputStream is = getClass().getResourceAsStream("/xml/cc/cc-voice-auth.xml");
		String uid = UUID.randomUUID().toString();
		String amt = "1.00";
		String auth = "111111";
		
		if (type=="MC") {
			amt = "3.00";
			auth = "222222";
			uid = UUID.randomUUID().toString();
		}
		else if (type=="DC") {
			amt = "7.00";
			auth = "444444";
			uid = UUID.randomUUID().toString();
		}
		else if (type=="JCB") {
			amt = "9.00";
			uid = UUID.randomUUID().toString();
		}
		
		uid = UUID.randomUUID().toString();
		String testData[][] = { {"//TransRequestID", uid},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//CreditCardNumber", card},
								{"//Amount", amt},
								{"//AuthorizationCode", auth}};
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardVoiceAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".*");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");						
	}
	
	@Test(dataProvider="cardSet")
	public void testAuthVoidWithTrack2data(String type, String card) throws Exception{

		InputStream xml = getClass().getResourceAsStream("/xml/cc/auth-track2data.xml");
		
		String reqId = UUID.randomUUID().toString();		
		String amt = "1.00";
		
		if (type=="MC") {
			System.out.println("MC");
			amt = "3.00";
			reqId = UUID.randomUUID().toString();
		}
		else if (type=="DC") {
			amt = "7.00";
			reqId = UUID.randomUUID().toString();
		}
		else if (type=="JCB") {
			amt = "9.00";
			reqId = UUID.randomUUID().toString();
		}
		
		String testData[][] = { {"//TransRequestID", reqId},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//Track2Data", generateTrack2Data(card)},
								{"//Amount", amt} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(xml), testData);
		String res = resp.getResponseContentAsString();	
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		
		reqId = UUID.randomUUID().toString();
		InputStream cap_xml = getClass().getResourceAsStream("/xml/cc/void-cc.xml");
		
		String capData[][] = {  {"//TransRequestID", reqId},
								{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{"//CreditCardTransID", ccTransId},
								{"//Amount", amt} };
		
		Response resp_void = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(cap_xml), capData);
		String res_void = resp_void.getResponseContentAsString();

		XPathAsserts.assertXPath(res_void, "//CustomerCreditCardTxnVoidRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_void, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res_void, "//ClientTransID", ".+");				
		
	}
}
