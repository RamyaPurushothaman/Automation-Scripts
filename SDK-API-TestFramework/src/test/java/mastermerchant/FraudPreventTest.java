package mastermerchant;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class FraudPreventTest extends BaseTest {
	
	protected String[][] getCVVData(String cc, String transReqId) {
		String[][] fData = {	{"//ConnectionTicket", connTicket},
								{"//TransRequestID", transReqId},
								{"//CreditCardNumber", cc},
		};
		return fData;
	}
	
	protected String[][] generateAVSData(String cc, String reqId, String amt, String addr, String zip) {
		String[][] avsData = {	{"//ConnectionTicket", connTicket},
								{"//TransRequestID", reqId},
								{"//CreditCardNumber", cc},
								{"//Amount", amt},
								{"//CreditCardAddress", addr},
								{"//CreditCardPostalCode", zip}
		};
		return avsData;
	}
	
	@Test
	public void testFraudPreventionInvalidPlus4() throws Exception {
		String transReqId = testUtil.getIntuitId();
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMTicket(transReqId));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testFraudInvalidPlus4Locale() throws Exception {
	
		String[][] fData = {	{"//ConnectionTicket", connTicket},
								{"//TransRequestID", UUID.randomUUID().toString()},
								{"//CreditCardAddress", "557 MAPLE ST" },
								{"//CreditCardPostalCode", "03104"},
							};

		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), fData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Fail");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testFraudInvalidZipPlus4Locale() throws Exception {
	
		String[][] fData = {	{"//ConnectionTicket", connTicket},
								{"//TransRequestID", UUID.randomUUID().toString()},
								{"//CreditCardAddress", "PO BOX 5973" },
								{"//CreditCardPostalCode", "03108"},
							};

		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), fData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "10409");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");						
	}
	
	//@Test
	public void testFraudInvalidStreetZip() throws Exception {
	
		String[][] fData = {	{"//TransRequestID", UUID.randomUUID().toString()},
								{"//CreditCardAddress", "486 CHESTNUT ST" },
								{"//CreditCardPostalCode", "3109"},
								{"//ConnectionTicket", connTicket},
							};

		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), fData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Fail");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	//@Test
	public void testCVVNoMatch() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "411"));
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, getCVVData("4444584332100774", UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", "Fail");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testCVVUnsupportedByIssuer() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "613"));
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, getCVVData("6011000990139424", UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", "NotAvailable");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}

	@Test
	public void testCVVMatch() throws Exception {
		String xml = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "111"));
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), xml, getCVVData("5405107001767865", UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", "Pass");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testCVVInvalidVisa() throws Exception {
		String xml = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "412"));
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), xml, getCVVData("4112344112344113",UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", "NotAvailable");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	// MM ML certification test
	
	@Test
	public void testMLCertAvsPassVisaCVD() throws Exception {
		
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "111"));
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "557 Any ST";
		
		String[][] avsData = generateAVSData("4112344112344113", reqId, "1.00", addr, "10451");
				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsVisaNoCVD() throws Exception {
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "3 MAIN ST";
		
		String[][] avsData = generateAVSData("4112344112344113", reqId, "2.00", addr, "10453");

		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvs() throws Exception {
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "3 MAIN ST";
		
		String[][] avsData = generateAVSData("4112344112344113", reqId, "3.00", addr, "10453");

		InputStream is = getClass().getResourceAsStream("/xml/charge-cc-mm.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsCVD233() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "233"));
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "3 MAIN ST";
		
		String[][] avsData = generateAVSData("4112344112344113", reqId, "4.00", addr, "10453");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassMCCVD222() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "222"));
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "557 Any ST";
		
		String[][] avsData = generateAVSData("5500005555555559", reqId, "3.00", addr, "10454");
	
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassMCCVD231() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "231"));
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "77 WEB BLVD";
		
		String[][] avsData = generateAVSData("5500005555555559", reqId, "4.00", addr, "10467");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassMCCVD232() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "232"));
		String reqId = UUID.randomUUID().toString();		
		String addr = "33 CIRCLE WAY";
		
		String[][] avsData = generateAVSData("5500005555555559", reqId, "5.00", addr, "10463");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassMCNoCVSHasCVD() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "233"));
		String reqId = UUID.randomUUID().toString();		
		String addr = "557 Any ST";
		
		String[][] avsData = generateAVSData("5500005555555559", reqId, "6.00", addr, "10454");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassDcCVD() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "399"));
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "557 Any ST";
		
		String[][] avsData = generateAVSData("36110361103612", reqId, "7.00", addr, "10451");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassDcCVD231() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "388"));
		
		String reqId = UUID.randomUUID().toString();		
		String addr = "77 WEB BLVD";
		
		String[][] avsData = generateAVSData("36110361103612", reqId, "8.00", addr, "10467");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassDcNoCVD232() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "381"));

		String reqId = UUID.randomUUID().toString();		
		String addr = "33 CIRCLE WAY";
		
		String[][] avsData = generateAVSData("36110361103612", reqId, "9.00", addr, "10457");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
	
	@Test
	public void testMLCertAvsPassDcCVD388() throws Exception {
		String is = testUtil.insertXmlTree("/xml/charge-cc-mm.xml", "//CustomerCreditCardChargeRq", testUtil.updateXmlNodeValue("CardSecurityCode", "388"));

		String reqId = UUID.randomUUID().toString();		
		String addr = "7 BROADWAY AVENUE";
		
		String[][] avsData = generateAVSData("36110361103612", reqId, "4.00", addr, "10457");
			
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, avsData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", "[PD].+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res, "//AVSStreet", "Pass");
		XPathAsserts.assertXPath(res, "//AVSZip", "Pass");
		XPathAsserts.assertXPath(res, "//CardSecurityCodeMatch", ".+");
		XPathAsserts.assertXPath(res, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");								
	}
}
