package mastermerchant;

import java.io.InputStream;
import java.util.UUID;

import org.testng.annotations.Test;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.wsclient.Response;

import utils.BaseTest;
import utils.Configuration;


public class PinDebitTest extends BaseTest{
	
	/**
	 * This should test that charging with pin debit should succeed
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCard() throws Exception {	
		String uid = testUtil.getIntuitId();		
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), getMMTicket(uid));
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusMessage", "Status OK");
	}
	
	
	/**
	 * This should test that charging with pin debit with optional fields should succeed
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCardWithOptionalFields() throws Exception {	
		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//BatchID", "1234"},
				{"//InvoiceID", "12345"},
				{"//UserID", "123456"},
				{"//GeoLocationInfo/IPAddress", "123456789"},
				{"//GeoLocationInfo/Latitude", "	37.373"},
				{"//GeoLocationInfo/Longitude", "-121.999368"},
				{"//DeviceInfo/DeviceID", "123456"},
				{"//DeviceInfo/DeviceType", "123456"},
				{"//DeviceInfo/DevicePhoneNumber", "123456"},
				{"//DeviceInfo/DeviceMACAddress", "123456"},

			  };
			
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusMessage", "Status OK");
	}
	
	
	/**
	 * This will test that charging pin debit without track2 data/ pin block/ smid will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCardWithoutTrack2PINBlockSMID() throws Exception {	
	
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//Track2Data", ""},
				{"//PINBlock", ""},
				{"//SMID", ""}
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10303");
	}
	
	/**
	 * This will test that charging pin debit without amount will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCardWithoutAmt() throws Exception {	
	
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//Amount", ""},
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10300");
	}
	
	/**
	 * This will test that charging pin debit with invalid track2 data will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCardWithInvalidTrack2() throws Exception {	
	
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//Track2Data", "lucytest"},
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10307");
	}
	
	/**
	 * This will test that charging pin debit with invalid pin block will throw error
	 * @throws Exception
	 */
	@Test 
	public void testChargePINDebitCardWithInvalidPINBlock() throws Exception {	
	
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//PINBlock", "lucytestlucytest"},
				{"//NameOnCard", "emulate=10401"},
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10401");
	}
	
	/**
	 * This will test that charging pin debit with invalid amount will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCardWithInvalidAmt() throws Exception {	
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//Amount", "-10.00"},
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10300");
	}
	
	/**
	 * This will test that charging pin debit with invalid smid will throw error
	 * @throws Exception
	 */
	@Test 
	public void testChargePINDebitCardWithInvalidSmid() throws Exception {	
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//SMID", "LUCYTESTLUCYTEST"},
				{"//NameOnCard", "emulate=10401"},
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10401");
	}
	
	/**
	 * This will test that charging pin debit with smid exceeding max length will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePINDebitCardWithExceedingMaxLengthSmid() throws Exception {	
		
		InputStream is = getClass().getResourceAsStream("/xml/cc/debitcard-charge.xml");	

		String testData[][] = { {"//TransRequestID", UUID.randomUUID().toString()},
				{"//ApplicationLogin", "mm.intuit.com"},
				{"//ConnectionTicket", connTicket},
				{"//SMID", "123456789012345678901234"},
			  };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), testUtil.normalXMLInputStream(is), testData);
		String res = resp.getResponseContentAsString();
		
		

		XPathAsserts.assertXPath(res, "//CustomerDebitCardChargeRs/@statusCode", "10304");
	}
	
}
