package qbmssdk.connectionTicket;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import utils.BaseTest;
import utils.Configuration;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

public class ConnectionTicketValidation extends BaseTest {

	@Test
	public void testInvalidConnectionTicketCCCharge() throws Exception {

		String[][] testData = new String[][] { { "//ConnectionTicket",
				"In-Valid-Connection-Ticket" } , {"//TransRequestID",UUID.randomUUID().toString()}};
		InputStream is = getClass()
				.getResourceAsStream("/xml/cc/charge-cc.xml");
		Response resp = xmlClient.doPost(Configuration.test_env,
				getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();

		XPathAsserts.assertXPath(res, "//SignonDesktopRs/@statusCode", "2000");

	}

	@Test
	public void testValidConnectionTicketCCCharge() throws Exception {
		InputStream is = getClass()
				.getResourceAsStream("/xml/cc/charge-cc.xml");
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		Response resp = xmlClient.doPost(Configuration.test_env,
				getHttpHeader(), testUtil.normalXMLInputStream(is),testData);
		String res = resp.getResponseContentAsString();

		XPathAsserts.assertXPath(res,
				"//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res,
				"//CustomerCreditCardChargeRs/@statusCode", "0");
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
