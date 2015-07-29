package com.intuit.paymentsapi.us;

import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class CardsTest extends BaseTest {

	String[][] CardsHeaderParams = { { "Company-Id", realmid } };
	String userid = "12345";

	// HTTP status codes
	public static final int SUCCESS = 200;
	public static final int CREATED = 201;
	public static final int SUCCESS_PUT_DELETE = 204;
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;

	/**
	 * This will test create cards for userid 12345 for that CC should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateCards() throws Exception {

		// Create a card
		String postCardsUrl = buildUrl(CREATE_CARDS, realmid, userid);
		HashMap<String, Object> cardBody = returnMapCard();
		HttpResponse response = TestUtil.post(postCardsUrl, CardsHeaderParams,
				cardBody);
		HashMap<String, Object> card = validateCardsDetail(response, CREATED);
		String card_id = card.get("id").toString();

		// Query the card,should exist
		String getCardsUrl = buildUrl(GET_CARDS, realmid, userid, card_id);
		HttpResponse getResp = TestUtil.get(getCardsUrl, CardsHeaderParams);
		validateCardsDetail(getResp, SUCCESS);
	}

	/**
	 * This will list all the cards for the user 12345 should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListCards() throws Exception {

		// Create another cards for user 12345
		String postCardsUrl = buildUrl(CREATE_CARDS, realmid, userid);
		HashMap<String, Object> cardBody = returnMapCard();
		cardBody.put("number", "4111111111111111");
		cardBody.put("name", "testUser");

		HttpResponse response = TestUtil.post(postCardsUrl, CardsHeaderParams,
				cardBody);
		validateCardsDetail(response, CREATED);

		// List all cards for user 12345
		String getCardsUrl = buildUrl(LIST_CARDS, realmid, userid);
		HttpResponse listResp = TestUtil.get(getCardsUrl, CardsHeaderParams);
		validateListCards(listResp);
	}

	/**
	 * This will update one card for the user 12345 and query it should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateCards() throws Exception {

		// Create a card
		String postCardsUrl = buildUrl(CREATE_CARDS, realmid, userid);
		HashMap<String, Object> cardBody = returnMapCard();
		HttpResponse response = TestUtil.post(postCardsUrl, CardsHeaderParams,
				cardBody);
		HashMap<String, Object> card = validateCardsDetail(response, CREATED);
		String card_id = card.get("id").toString();
		
		// Update the card
		String putCardsUrl = buildUrl(PUT_CARDS, realmid, userid, card_id);
		HashMap<String,Object> newCard = new HashMap<String,Object>();
		String newName = "TestUser";
		newCard.put("name", newName);
		
		HttpResponse putResp = TestUtil.put(putCardsUrl, CardsHeaderParams,newCard);
		Assert.assertEquals(putResp.getStatusLine().getStatusCode(),
				SUCCESS_PUT_DELETE);
		
		// Query the updated card
		String getCardsUrl = buildUrl(GET_CARDS, realmid, userid, card_id);
		HttpResponse getResp = TestUtil.get(getCardsUrl, CardsHeaderParams);
		card = validateCardsDetail(getResp,SUCCESS);
		
		Assert.assertEquals(card.get("name").toString(),newName) ;
	}

	/**
	 * This will delete one card for the user 12345 and query it should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteCards() throws Exception {

		// Create a card for user 12345
		String postCardsUrl = buildUrl(CREATE_CARDS, realmid, userid);
		HashMap<String, Object> cardBody = returnMapCard();
		cardBody.put("number", "4111111111111111");
		cardBody.put("name", "testUser");

		HttpResponse response = TestUtil.post(postCardsUrl, CardsHeaderParams,
				cardBody);
		HashMap<String, Object> card = validateCardsDetail(response, CREATED);
		String card_id = card.get("id").toString();

		// Delete that card
		String deleteCardsUrl = buildUrl(DELETE_CARDS, realmid, userid, card_id);
		HttpResponse deleteResp = TestUtil.delete(deleteCardsUrl,
				CardsHeaderParams);
		Assert.assertEquals(deleteResp.getStatusLine().getStatusCode(),
				SUCCESS_PUT_DELETE);

		// Query that card, the card shouldn't exist
		String getCardsUrl = buildUrl(GET_CARDS, realmid, userid, card_id);
		HttpResponse getResp = TestUtil.get(getCardsUrl, CardsHeaderParams);
		// validateEmptyCard(getResp);
		validateErrorRes(getResp, BAD_REQUEST, "invalid_request", "PMT-4000",
				"card is invalid.", "card",
				"https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}

}
