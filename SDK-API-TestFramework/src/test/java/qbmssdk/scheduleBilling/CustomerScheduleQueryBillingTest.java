package qbmssdk.scheduleBilling;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utils.*;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

public class CustomerScheduleQueryBillingTest extends BaseTest {
	
	private String walletEntryId;
	private String startDate;

	private String getStartDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		
		return sdf.format(c.getTime()); 	
	}
	
	@BeforeClass
	public void beforeClass() throws Exception {
		walletEntryId = createCCWallet(new String[][] {	{ "//CustomerID", "RebillCCWalletAmex2" }, });
		startDate = getStartDate();
	}
	
	//@AfterClass
	public void afterClass() throws Exception {
		deleteCCWallet(walletEntryId);		
	}	
	
	@Test
	public void testScheduleCCWalletBillingQueryByCustomerId() throws Exception {
		
		String walletEId = createCCWallet();
		String[][] testData = new String[][] {	{ "//WalletEntryID", walletEId }, };
				
		InputStream is = getClass().getResourceAsStream("/xml/schedule-billing.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");
		
		InputStream query = getClass().getResourceAsStream("/xml/query-customer-scheduled-billing.xml");
		
		Response query_rs = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(query));
		String rs = query_rs.getResponseContentAsString();
		
		XPathAsserts.assertXPath(rs, "//CustomerScheduledBillingQueryRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(rs, "//CustomerID", "RebillCCWalletVisa1");
		XPathAsserts.assertXPath(rs, "//ScheduledBillingID", ".+");
		
		String schedBID = XPathUtils.applyXpath(res, "//ScheduledBillingID");	
		deleteScheduledBilling(schedBID, "RebillCCWalletVisa1");
	}
	
	/**
	 * 1. This success of this test depends on the cron job execution at midnight.
	   2. This test supposes to be triggered by scheduled automation job, not manual run
	 */
	
	//@Test
	public void testScheduleCCWalletBillingPmtQueryByCustomerId() throws Exception {	
		
		String schedBillId = getScheduledBillingID("RebillCCWalletAmex2", walletEntryId);
		System.out.println("ddddd" + schedBillId);
		
		InputStream query = getClass().getResourceAsStream("/xml/query-customer-scheduled-billing-payment.xml");

		Response query_rs = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(query));
		String rs = query_rs.getResponseContentAsString();

	    XPathAsserts.assertXPath(rs, "//CustomerScheduledBillingPaymentQueryRs/@statusCode", "0");		
		XPathAsserts.assertXPath(rs, "//ScheduledBillingPaymentID", ".+");
		XPathAsserts.assertXPath(rs, "//ScheduledBillingID", ".+");
		XPathAsserts.assertXPath(rs, "//CustomerID", "RebillCCWalletAmex2");
		XPathAsserts.assertXPath(rs, "//WalletEntryID", walletEntryId);
		XPathAsserts.assertXPath(rs, "//MerchantAccountNumber", ".+");
		XPathAsserts.assertXPath(rs, "//PaymentType", "CreditCard");
		XPathAsserts.assertXPath(rs, "//Amount", "1.00");
		XPathAsserts.assertXPath(rs, "//PaymentDate", ".+");
		XPathAsserts.assertXPath(rs, "//ResultStatusCode", "0");
		
		if (Calendar.getInstance().equals(startDate))
			deleteScheduledBilling(schedBillId, "RebillCCWalletAmex2");
				
		String[][] scheData = new String[][] {	{ "//CustomerID", "RebillCCWalletAmex2" },
												{ "//WalletEntryID", walletEntryId }, 											
												{ "//FrequencyExpression", "28" },
												{ "//StartDate", startDate },	};			
		scheduleBilling(scheData);
	}
	
	// Negative test
	
	@Test
	public void testDeleteScheduleCCWalletBillingNonExsitInDB() throws Exception {

		String schedBID = "10017945";
		String[][] schBillIdIdVal = new String[][] {{ "//ScheduledBillingID", schedBID}, };
		
		InputStream schBillDel = getClass().getResourceAsStream("/xml/delete-schedule-billing.xml");
		
		Response res_delBill = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(schBillDel), schBillIdIdVal);
		
		XPathAsserts.assertXPath(res_delBill.getResponseContentAsString(), "//CustomerScheduledBillingDelRs/@statusCode", "10321");		
	}

}
