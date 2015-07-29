package qbmssdk.batchClose;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;

public class ManualBatchCloseTest extends BaseTest {
	
	private String branchToAdd = "<BatchID>2358</BatchID>";
	
	
	@Test
	public void testManualBatchClose() throws Exception {
		
		String[][] testData1 = {{"//TransRequestID",UUID.randomUUID().toString()}};
		String charge_xml = testUtil.insertXmlTree("/xml/cc/charge-cc.xml", "//CustomerCreditCardChargeRq", branchToAdd);
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), charge_xml,testData1);
		String res = resp.getResponseContentAsString();

		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusCode", "0");
		
		InputStream is = getClass().getResourceAsStream("/xml/batchclose/batch-close.xml");
		
		String[][] testData2 = {{"//TransRequestID",UUID.randomUUID().toString()}};
		Response resb = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is),testData2);
		String resbc = resb.getResponseContentAsString();
		
		XPathAsserts.assertXPath(resbc, "//MerchantBatchCloseRs/@statusCode", "0");

	}

}
