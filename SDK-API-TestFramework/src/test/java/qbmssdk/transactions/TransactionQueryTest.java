package qbmssdk.transactions;

import java.io.InputStream;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class TransactionQueryTest extends BaseTest {
	
	/**
	 * This method tests that query transaction should succeed.
	 * @throws Exception
	 */
	@Test
	public void testQueryTxns() throws Exception {
				
		InputStream is = getClass().getResourceAsStream("/xml/query/query-transaction.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//MerchantAccountTxnQueryRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//MerchantAccountTxnQueryRs/@statusCode", "0");
	}

	/**
	 * This method tests that query the transaction and get the query result over 1000 should fail on QA
	 * and return the error code; But not fail on PTC and should throw exceptions because
	 * there aren't enough transaction results stored in PTC DB.
	 * @throws Exception
	 */
	@Test (groups = {"qa-only"})
	public void testQueryTxnsOver1000() throws Exception {
				
			InputStream is = getClass().getResourceAsStream("/xml/query/query-over-1000-txns.xml");
			
			Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
			String res = resp.getResponseContentAsString();
			
			XPathAsserts.assertXPath(res, "//MerchantAccountTxnQueryRs/@statusCode", "10102");
			
	}
}
