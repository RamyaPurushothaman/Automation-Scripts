package com.intuit.paymentsapi.us;

import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.intuit.paymentsapi.util.TestUtil;

public class PinDebitTest extends BaseTest {
	
	public String CCChargeUrl = buildUrl(CHARGE_CC);
	
	public String[][] CCChargeHeaderParams = { { "Company-Id", realmid }, 
		{"Request-Id", UUID.randomUUID().toString()}

	};
	
	/**
	 * This will test that charging pin debit should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCard() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCardWithCardPresent());
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateChargeRes(response, 201, Status.CAPTURED.toString());
	}
	
	/**
	 * This will test that charging pin debit with device info should succeed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardWithDeviceInfo() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCardWithCardPresent());
		
		HashMap<String, Object> mapCardContext = new HashMap<String, Object>();
		mapCardContext.put("deviceInfo", constructMapDeviceInfo());
		mapBody.put("context", mapCardContext);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		HashMap<String, Object> result = validateChargeRes(response, 201, Status.CAPTURED.toString());
	}
	
	/**
	 * This will test that charging pin debit without track2 will throw error
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testChargePinDebitCardWithoutTrack2() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("track2", "");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4002", "number is required.", "number", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit without ksn will throw error
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testChargePinDebitCardWithoutKsn() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("ksn", "");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4002", "SMID is required.", "SMID", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit without pinBlock will throw error
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testChargePinDebitCardWithoutPinBlock() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("pinBlock", "");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4002", "pinBlock is required.", "pinBlock", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit with invalid track2 will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardInvalidTrack2() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("track2", "invalidTrack2");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000", "track2 is invalid.", "track2", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit with invalid ksn will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardInvalidKsn() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("ksn", "invalidKsn");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000", "SMID is invalid.", "SMID", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit with invalid ksn will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardInvalidPinBlock() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("pinBlock", "invalidPin");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000", "PINBlock is invalid.", "PINBlock", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit with ksn which exceeding max length will throw error
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardLongKsn() throws Exception {	
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		HashMap<String, Object> cardPresent = (HashMap<String, Object>) card.get("cardPresent");
		cardPresent.put("ksn", "123456789012345678901234");
		card.put("cardPresent", cardPresent);
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000", "SMID is invalid.", "SMID", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit without amount should throw error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardWithoutAmount() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", "");mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCardWithCardPresent());
		
		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(chargeResponse, 400, "invalid_request", "PMT-4000", "Amount. is invalid.", "Amount.", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit without amount should throw error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardInvalidAmount() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", "-1.00");mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCardWithCardPresent());
		
		HttpResponse chargeResponse = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(chargeResponse, 400, "invalid_request", "PMT-4000", "Amount. is invalid.", "Amount.", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
	/**
	 * This will test that charging pin debit without card name should throw error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChargePinDebitCardWithOutName() throws Exception {
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", 1.00);mapBody.put("currency", "USD");
		HashMap<String, Object> card = returnMapCardWithCardPresent();
		card.put("name", "");
		mapBody.put("card", card);
		
		HttpResponse response = TestUtil.post(CCChargeUrl,
				CCChargeHeaderParams, mapBody);
		validateErrorRes(response, 400, "invalid_request", "PMT-4000", "card.name is invalid.", "card.name", "https://developer.intuit.com/v2/docs?redirectID=PayErrors");
	}
	
}
