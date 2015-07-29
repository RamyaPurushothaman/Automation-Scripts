package qbmssdk.transactions;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.Test;

import utils.BaseTest;
import utils.Configuration;
import utils.TestUtil;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

public class GiftCardTransaction extends BaseTest{
	
	/**
	 * This method that cancel the gift card transaction should succeed
	 * and the transaction should be canceled.
	 * @throws Exception
	 */
	protected void cancelGC(String s) throws Exception {
		String[][] testData = {{"//GCTransID", s}};
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-cancel.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardCancelRs/@statusMessage", "Status OK");
	}
	
	/**
	 * This method that query the gift card balance should succeed and return the gift card balance.
	 * @throws Exception
	 */
	protected double inquiryGCbalance() throws Exception {
		double balance = 0.00;
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-balance-inquiry.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardBalanceInquiryRs/@statusMessage", "Status OK");
		balance = Float.parseFloat(XPathUtils.applyXpath(res, "//GCCertificateBalance"));
		
		return balance;
	}
	
// Disabling all tests in this class due to Gateway issue 
// These tests should be enabled upon closing this ticket:QSDK-533
	
	/**
	 * This method tests that activate a gift card and then cancel the transaction should succeed.
	 * @throws Exception
	 */
//	@Test 
	@Test (enabled=false)
	public void testGCActivateThenCancel() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-activate.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardActivateRs/@statusMessage", "Status OK");
		
		String gcTransId = XPathUtils.applyXpath(res, "//GCTransID");
		cancelGC(gcTransId);
//		cancelGC("A11969877000");
	}
	
	/**
	 * This method tests that increase gift card balance should succeed and 
	 * return the balance of gift card.
	 * @throws Exception
	 */
	@Test (enabled=false)
	public void testGCAdjustIncrease() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-adjust.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardAdjustRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//GCTransID", ".+");
		XPathAsserts.assertXPath(res, "//GCAuthCode", ".+");
		XPathAsserts.assertXPath(res, "//GCCertificateBalance", "\\d+(\\.\\d{2})");
	}
	
	/**
	 * This method tests that query gift card balance should succeed and 
	 * return the balance with currency.
	 * @throws Exception
	 */
	@Test (enabled=false)
	public void testGCBalanceInquiry() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-balance-inquiry.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardBalanceInquiryRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//GCTransID", ".+");
		XPathAsserts.assertXPath(res, "//GCCertificateBalance", "\\d+(\\.\\d{2})");
		XPathAsserts.assertXPath(res, "//GCCurrencyCode", "USD");
	}
	
	/**
	 * This method tests that decrease gift card balance should succeed and 
	 * return the new balance.
	 * @throws Exception
	 */
	@Test (enabled=false)
	public void testGCAdjustDecrease() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-adjust.xml");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("//TransRequestID", UUID.randomUUID().toString());
		map.put("//GCAdjustmentAmount", "-10.00");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), TestUtil.mapToTwoDarray(map));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardAdjustRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//GCTransID", ".+");
		XPathAsserts.assertXPath(res, "//GCAuthCode", ".+");
		XPathAsserts.assertXPath(res, "//GCCertificateBalance", "\\d+(\\.\\d{2})");
	}
	
	/**
	 * This method tests that force gift card redeem should succeed and 
	 * then cancel the transaction.
	 * @throws Exception
	 */
	@Test (enabled=false)
	public void testGCForceRedeem() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-force-redeem.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardForcedRedeemRs/@statusMessage", "Status OK");
		
		String gcTransId = XPathUtils.applyXpath(res, "//GCTransID");
		cancelGC(gcTransId);		
	}
	
	/**
	 * This method tests that securely redeem the gift card should succeed and 
	 * then cancel the transaction.
	 * @throws Exception
	 */
	@Test (enabled=false)
	public void testGCSecureRedeem() throws Exception {

//		double redeem_amount = inquiryGCbalance();
//		NumberFormat formatter = new DecimalFormat("0.00");
//	    String s = formatter.format(redeem_amount); 

		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-secure-redeem.xml");		
		
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("//TransRequestID", UUID.randomUUID().toString());
//		map.put("//GCAmount", s);

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getUID(UUID.randomUUID().toString()));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardSecureRedeemRs/@statusMessage", "Status OK");
		
		String gcTransId = XPathUtils.applyXpath(res, "//GCTransID");
		cancelGC(gcTransId);		
	}
	
	/**
	 * This method tests that will redeem the amount which is larger than gift card's balance should fail and
	 * return an error code.
	 * @throws Exception
	 */
	@Test (enabled=false)
	public void testGCSecureRedeemExceededBalance() throws Exception {

		double redeem_amount = inquiryGCbalance() + 1;
		NumberFormat formatter = new DecimalFormat("0.00");
	    String s = formatter.format(redeem_amount); 

		InputStream is = getClass().getResourceAsStream("/xml/giftcard/gc-secure-redeem.xml");		
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("//TransRequestID", UUID.randomUUID().toString());
		map.put("//GCAmount", s);

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), TestUtil.mapToTwoDarray(map));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerGiftCardSecureRedeemRs/@statusCode", "10401");	
	}
	
	
	
}
