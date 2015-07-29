package com.intuit.paymentsapi.us;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class ChargeListTest extends BaseTest {

	private String[][] CCRetrievalHeaderParams = { { "Company-Id", realmid } };

	/**
	 * 
	 * Happy Path
	 * 
	 * 
	 */

	/**
	 * This will list the 200 records of charges without the count number.
	 * 
	 * @throws Exception
	 */
	@Test(groups = {"chargeListTest"})
	public void testListCharges() throws Exception {
		String CCRetrievalUrl = buildUrl(RETRIEVAL_LIST_CC, realmid);
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				CCRetrievalHeaderParams);
		Assert.assertEquals(retrievalResponse.getStatusLine().getStatusCode(),
				200);
		Assert.assertEquals(convertRespToJsonArray(retrievalResponse).length(),
				200);
	}

	/**
	 * This will return no record for non-exist company.
	 * 
	 * @throws Exception
	 */
	@Test(groups = {"chargeListTest"})
	public void testListNonExistCompany() throws Exception {
		String CCRetrievalUrl = buildUrl(RETRIEVAL_LIST_CC, realmid);
		String[][] nonExistCompanyHeaderParams = { { "Company-Id", "1019017761" } };
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				nonExistCompanyHeaderParams);
		Assert.assertEquals(retrievalResponse.getStatusLine().getStatusCode(),
				200);
		Assert.assertEquals(convertRespToJsonArray(retrievalResponse).length(),
				0);
	}

	/**
	 * This will return no records without the company id.
	 * 
	 * @throws Exception
	 */
	@Test(groups = {"chargeListTest"})
	public void testListEmptyCompany() throws Exception {
		String CCRetrievalUrl = buildUrl(RETRIEVAL_LIST_CC, realmid);
		String[][] emptyCompanyHeaderParams = { { "Company-Id", "" } };
		HttpResponse retrievalResponse = TestUtil.get(CCRetrievalUrl,
				emptyCompanyHeaderParams);
		Assert.assertEquals(retrievalResponse.getStatusLine().getStatusCode(),
				200);
		Assert.assertEquals(convertRespToJsonArray(retrievalResponse).length(),
				0);
	}

	/***
	 * 
	 * This will query for 10 transactions will return 10 transactions.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test(groups = {"chargeListTest"})
	public void testListTenCharges() throws Exception {
		String CCRetrievalUrl = buildUrl(RETRIEVAL_LIST_CC, realmid);
		String retrievalWithCount = CCRetrievalUrl + "?count=10";
		HttpResponse retrievalResponse = TestUtil.get(retrievalWithCount,
				CCRetrievalHeaderParams);
		Assert.assertEquals(retrievalResponse.getStatusLine().getStatusCode(),
				200);
		Assert.assertEquals(convertRespToJsonArray(retrievalResponse).length(),
				10);
	}

	/***
	 * 
	 * This will query for 300 transactions will return 300 transactions.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test(groups = {"chargeListTest"})
	public void testListMoreCharges() throws Exception {
		String CCRetrievalUrl = buildUrl(RETRIEVAL_LIST_CC, realmid);
		String retrievalWithCount = CCRetrievalUrl + "?count=300";
		HttpResponse retrievalResponse = TestUtil.get(retrievalWithCount,
				CCRetrievalHeaderParams);
		Assert.assertEquals(retrievalResponse.getStatusLine().getStatusCode(),
				200);
		Assert.assertEquals(convertRespToJsonArray(retrievalResponse).length(),
				300);
	}

	/**
	 * 
	 * 
	 * Unhappy Path
	 * 
	 * 
	 */

}
