package com.intuit.paymentsapi.us;

import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class InvalidFieldTest extends BaseTest {

	public static String[][] CCChargeHeaderParams = {
			{ "Company-Id", realmid },
			{ "Request-Id", UUID.randomUUID().toString() }

	};

	public static String CCChargeUrl = buildUrl(CHARGE_CC);

	@DataProvider(name = "invalidReq")
	public Object[][] invalidReqData() throws Exception {
		return new Object[][] {

				{ new String[][] { { "Request-Id",
						UUID.randomUUID().toString() + "." },

				} },

				{ new String[][] {

				{ "Request-Id", UUID.randomUUID().toString() + "!" },

				} },

		};
	}

	/**
	 * 
	 * This will test invalid path such as
	 * https://transaction-qa.payments.intuit.com/v2/charges_invalid should
	 * throw 404
	 * 
	 */
	@Test
	public void testInvalidPath() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		String invalidUrl = buildUrl(CHARGE_CC_INVALID);
		HttpResponse response = TestUtil.post(invalidUrl, CCChargeHeaderParams,
				mapBody);
		validateErrorRes(response, 404, "invalid_request", "PMT-4000",
				"uri is invalid.", "uri",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");

	}

	/**
	 * This test using invalid request id for the transaction should fail.
	 * 
	 * @throws Exception
	 */

	@Test(dataProvider = "invalidReq")
	public void testInvalidRequestId(String[][] testData) throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		String[][] invalidReqHeaderParams = { { "Company-Id", realmid },
				{ "Request-Id", "" }

		};

		HttpResponse response = TestUtil.postInvalidReq(CCChargeUrl,
				invalidReqHeaderParams, mapBody, testData);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000",
				"request is invalid.", "request",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

	/**
	 * This test will test with invalid company id(e.g. " "1019017762" ") for the
	 * transaction should fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvalidCompanyId() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 0.01);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		String[][] invalidCompanyId = {
				{ "Company-Id", "\"" + realmid + "\"" },
				{ "Request-Id", UUID.randomUUID().toString() }

		};
		HttpResponse response = TestUtil.post(CCChargeUrl, invalidCompanyId,
				mapBody);
		validateErrorRes(response, 400, "account_error", "PMT-3000",
				"The merchant account could not be validated.", "",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

}
