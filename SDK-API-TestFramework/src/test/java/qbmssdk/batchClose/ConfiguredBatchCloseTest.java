package qbmssdk.batchClose;

import java.io.InputStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class ConfiguredBatchCloseTest extends BaseTest {
	
	@BeforeClass
	public void beforeClass() throws Exception {
		InputStream is = getClass().getResourceAsStream("/xml/batchclose/batch-close-delete.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//MerchantAccountModRs/@statusCode", "0");
	}
	
	@BeforeMethod
	public void beforeMethod() {}
	
	@Test
	public void tesCreateBatchCloseConfigured() throws Exception {
		
		InputStream is = getClass().getResourceAsStream("/xml/batchclose/batch-close-cfg.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//MerchantAccountModRs/@statusCode", "0");
		
		String query_resp = queryMerchantAccount("imsqadesktoptest1", "TGT-230-PLwtnwqvzP78yHJigVo$XA");		
		XPathAsserts.assertXPath(query_resp, "//BatchCloseHour", "10");
		
		// update configured batch close 
		
		String[][] bc_data = new String[][] {	{ "//BatchCloseHour", "8" },	};					
		InputStream updated_is = getClass().getResourceAsStream("/xml/batchclose/batch-close-cfg.xml");
		
		Response resp_update = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(updated_is), bc_data);
		String res_update = resp_update.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res_update, "//MerchantAccountModRs/@statusCode", "0");
		
		query_resp = queryMerchantAccount("imsqadesktoptest1", "TGT-230-PLwtnwqvzP78yHJigVo$XA");
		XPathAsserts.assertXPath(query_resp, "//BatchCloseHour", "8");
	}
}
