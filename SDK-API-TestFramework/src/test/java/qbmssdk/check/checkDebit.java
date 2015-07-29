package qbmssdk.check;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;
import utils.DbUtil;

public class checkDebit extends BaseTest {
	
	/**
	 * This test that charging check should succeed
	 * @throws Exception
	 */
	@Test
	public void testCheckDebit() throws Exception {
				
		InputStream is = getClass().getResourceAsStream("/xml/check/check-debit.xml");
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckDebitRs/@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//CheckTransID", ".+");
		XPathAsserts.assertXPath(res, "//CheckAuthorizationCode", ".+");
		XPathAsserts.assertXPath(res, "//TxnAuthorizationTime", ".+");	
		
		ResultSet ret;
		Connection conn;
		Statement statment;
		String sql = "select * from mas_check_txn where TXN_ID = " + "'" 
				+ XPathUtils.applyXpath(res, "//CheckTransID") + "'";
		
		conn = DbUtil.connectDB(); 
		statment = conn.createStatement();
		ret = statment.executeQuery(sql);
		
		Assert.assertEquals(ret.next(),true);
		Assert.assertEquals(ret.getString("VENDOR_RESULT_CODE"),"0");
		Assert.assertEquals(ret.getString("RESULT_CODE"),"0");
		Assert.assertEquals(ret.getString("AMOUNT"),"185");
		
		ret.close();
		statment.close();
		conn.close();
	}
	
	/**
	 * This will test that non-subscribed merchants will receive error when charging check
	 * @throws Exception
	 */
	@Test(groups ={"unapproved-nonreopened-merchants"})
	public void testCheckDebitNonSubscribed() throws Exception {
				
		InputStream is = getClass().getResourceAsStream("/xml/check/check-debit.xml");
		String[][] testData = {{"//TransRequestID",UUID.randomUUID().toString()}};
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),testData);
		String res = resp.getResponseContentAsString();
		XPathAsserts.assertXPath(res, "//CustomerCheckDebitRs/@statusCode", "10309");

					
	}
	
	/**
	 * This test that charging check with invalid account number will fail
	 * @throws Exception
	 */
	@Test
	public void testCheckDebitWithInvalidAccountNumber() throws Exception {
				
		InputStream is = getClass().getResourceAsStream("/xml/check/check-debit.xml");		
		String[][] accntData = new String[][] {{"//AccountNumber","11.0"},{"//TransRequestID",UUID.randomUUID().toString()}};
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), accntData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCheckDebitRs/@statusCode", "10309");
	}

}
