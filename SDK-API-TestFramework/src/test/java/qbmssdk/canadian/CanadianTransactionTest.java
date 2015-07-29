package qbmssdk.canadian;

import java.util.UUID;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class CanadianTransactionTest extends BaseTest {
	
	String cnData =  "<CanadianPaymentInfo>"
			       +   "<SystemTrace>567321</SystemTrace>"
			       +   "<TerminalID>01089999</TerminalID>"
			       +   "<LocalPOSTimestamp>2013-11-17T13:51:13</LocalPOSTimestamp>"
			       +   "<RetrievalRefNumber>000099567321</RetrievalRefNumber>"
	               + "</CanadianPaymentInfo>";
	
	@Test
	public void testCanadianCCCharge() throws Exception{
		String new_xml = testUtil.insertXmlTree("/xml/cc/charge-cc.xml","//CustomerCreditCardChargeRq", cnData);
		
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXML(new_xml),testData);
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
}
