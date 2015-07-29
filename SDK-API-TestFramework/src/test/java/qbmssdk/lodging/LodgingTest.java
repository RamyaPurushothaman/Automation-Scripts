package qbmssdk.lodging;

import java.io.InputStream;
import java.util.UUID;

import com.intuit.tame.common.wsclient.Response;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;

import utils.BaseTest;
import utils.Configuration;

public class LodgingTest extends BaseTest{

	@Test
	public void testLodgingCharge() throws Exception{
		
		String lodging_xml = buildLodgingXML("/xml/cc/charge-cc.xml", "//CustomerCreditCardChargeRq");
		
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml,testData);
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
	public void testLodgingRefund() throws Exception{
		String lodging_xml = buildLodgingXML("/xml/cc/refund-cc.xml", "//CustomerCreditCardRefundRq");
		
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml,testData);
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
	public void testLodgingVoicAuth() throws Exception{
		String lodging_xml = buildLodgingXML("/xml/cc/cc-voice-auth.xml", "//CustomerCreditCardVoiceAuthRq");
		
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml,testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardVoiceAuthRs/@statusMessage", "Status OK");
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
	public void testLodgingIncrmentalAuthCapture() throws Exception{
		String lodging_xml = buildLodgingXML("/xml/cc/auth.xml", "//CustomerCreditCardAuthRq");
		
		String[][] data = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString() }	};
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodging_xml, data);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res, "//AuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//ClientTransID", ".+");	
		
		String ccTransId = XPathUtils.applyXpath(res, "//CreditCardTransID");
		
		String lodgingExtend = buildLodgingXML("/xml/cc/incremental-auth.xml", "//CustomerCreditCardTxnIncrementalAuthRq");	
		String[][] testData = new String[][] {	{ "//CreditCardTransID", ccTransId },{"//TransRequestID", UUID.randomUUID().toString()}	};
		
		Response resp_ia = xmlClient.doPost(Configuration.test_env, getHttpHeader(), lodgingExtend, testData);
		String res_ia = resp_ia.getResponseContentAsString();
		XPathAsserts.assertXPath(res_ia, "//CustomerCreditCardTxnIncrementalAuthRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_ia, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res_ia, "//ClientTransID", ".+");
		XPathAsserts.assertXPath(res_ia, "//AuthorizationCode", ".+");
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/capture.xml");
		String[][] testCap = new String[][] { { "//CreditCardTransID", ccTransId },
				                              { "//TransRequestID", UUID.randomUUID().toString() }
		
		}; 
				
		Response resp_cap = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testCap);
		String res_cap = resp_cap.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res_cap, "//CustomerCreditCardCaptureRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res_cap, "//CreditCardTransID", ".+");
		XPathAsserts.assertXPath(res_cap, "//ReconBatchID", ".+");
		XPathAsserts.assertXPath(res_cap, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(res_cap, "//PaymentGroupingCode", "5");
		XPathAsserts.assertXPath(res_cap, "//PaymentStatus", "Completed");
		XPathAsserts.assertXPath(res_cap, "//TxnAuthorizationTime", ".+");
		XPathAsserts.assertXPath(res_cap, "//TxnAuthorizationStamp", ".+");
		XPathAsserts.assertXPath(res_cap, "//ClientTransID", ".+");			
	}

}
