package com.intuit.paymentsapi.us;

import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class FraudTest extends BaseTest {
	
	String[][] CCChargeHeaderParams = { { "Company-Id", realmid }, 
		{"Request-Id", UUID.randomUUID().toString()}

	};

	String CCChargeUrl = buildUrl(CHARGE_CC);
	
	@DataProvider(name = "cc-avs-error")
	public Object[][] ccAvsErrorData() throws Exception {
		return new Object[][] { 
				 { new String[][] { { "name", "emulate=10000.avscvdfail" },
			} }
		};
	}
	
	@Test(dataProvider = "cc-avs-error")
	public void testCCAvsCvdFail(String[][] testData) throws Exception {
		
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", updateMapCard(testData));

		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response,400,"fraud_error","PMT-2002","Incorrect address information.", "", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}
	
}
