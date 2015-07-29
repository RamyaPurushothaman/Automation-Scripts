package qbmssdk.wallet;

import java.io.InputStream;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import utils.BaseTest;
import utils.Configuration;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

public class CreateUpdateDeleteCCWalletTest extends BaseTest {
	
	@BeforeClass
	public void beforeClass() {}
	
	@BeforeMethod
	public void beforeMethod() {}
	
	@Test
	public void testCreateDeleteCCWallet() throws Exception {
		
		deleteCCWallet();
		
		InputStream is = getClass().getResourceAsStream("/xml/create-cc-wallet.xml");

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();
		System.out.println("Response:\n" + resp.getResponseContentAsString());
		
		XPathAsserts.assertXPathExists(res, "/QBMSXML/SignonMsgsRs");
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//IsDuplicate", "false");			
	}
	
	@Test
	public void testCreateCCWalletExsit() throws Exception {

		InputStream is = getClass().getResourceAsStream("/xml/create-cc-wallet.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();
		System.out.println("Response:\n" + resp.getResponseContentAsString());
		
		XPathAsserts.assertXPathExists(res, "/QBMSXML/SignonMsgsRs");
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//IsDuplicate", "true");	
	}
	
	// Negative Tests
	
	@Test
	public void testDeleteNonExsitCCWallet() throws Exception {

		InputStream is = getClass().getResourceAsStream("/xml/delete-cc-wallet.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));
		String res = resp.getResponseContentAsString();
		System.out.println("Response:\n" + resp.getResponseContentAsString());
		
		XPathAsserts.assertXPathExists(res, "/QBMSXML/SignonMsgsRs");
		XPathAsserts.assertXPath(res, "//CustomerCreditCardWalletDelRs/@statusCode", "10307");
		
	}

}
