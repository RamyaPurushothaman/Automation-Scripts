package qbmssdk;

import java.io.InputStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import utils.*;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;
import com.intuit.tame.ws.XmlAssert;

public class SignOnTest extends BaseTest{
	
	SignOnTest(){	super();	}

	@BeforeClass
	public void beforeClass() {}
	
	@BeforeMethod
	public void beforeMethod() {}
	
	@Test
	public void testSignOn() throws Exception {

		InputStream is = getClass().getResourceAsStream("/xml/qbms.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is));

		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPathExists(res, "/QBMSXML/SignonMsgsRs");
		XmlAssert.verifyXPathNodeCount(resp, "//CreditCardType", 6);
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");
		XPathAsserts.assertXPath(res, "//SessionTicket", ".*");
		XPathAsserts.assertXPath(res, "//ServerDateTime", ".*");
		XPathAsserts.assertXPath(res, "//IsCheckAccepted", "true");	
		
		String val = XPathUtils.applyXpath(res, "//CreditCardType");
		
		
		System.out.println(resp.getXPathNodeList("//WalletEntryID"));
//		System.out.println(resp.getXPathValueArray("//QBMSXMLMsgsRs").getClass().);
	}
	
	@Test
	public void testSignOnInvalidAppLogin() throws Exception {
		String[][] appLogin = { {"//ApplicationLogin", "123qwe"} };
		InputStream is = getClass().getResourceAsStream("/xml/qbms.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), appLogin);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//@statusCode", "2040");		
	}
	
	@Test
	public void testSignOnMissingAppLogin() throws Exception {
		String[][] appLogin = { {"//ApplicationLogin", ""} };
		InputStream is = getClass().getResourceAsStream("/xml/qbms.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), appLogin);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//@statusCode", "2040");		
	}
	
	@Test
	public void testSignOnInvalidConnTicket() throws Exception {
		String[][] appLogin = { {"//ConnectionTicket", "TGT-72-ufUU_avjxYulsjmHRIxLc"} };
		InputStream is = getClass().getResourceAsStream("/xml/qbms.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), appLogin);

		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//@statusCode", "2000");		
	}
	
	@Test(enabled = false)
	public void testSignOnMissingConnTicket() throws Exception {
		String[][] appLogin = { {"//ConnectionTicket", ""} };
		InputStream is = getClass().getResourceAsStream("/xml/qbms.xml");
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), appLogin);

		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//@statusCode", "2000");		
	}

}
