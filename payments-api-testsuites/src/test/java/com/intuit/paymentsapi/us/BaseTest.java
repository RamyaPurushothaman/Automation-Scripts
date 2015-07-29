package com.intuit.paymentsapi.us;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.testng.Assert;

import com.intuit.paymentsapi.util.Constants;
import com.intuit.paymentsapi.util.OnboardingManagerUtil;
import com.intuit.paymentsapi.util.PropertyUtil;
import com.intuit.paymentsapi.util.TestUtil;
import com.intuit.tame.ws.JsonTestClient;


/**
 * REST API credit card wallet test
 * 
 * This class is called at the beginning. It builds all of the URLs first.
 * 
 * URL constants are defined in the constant file
 * 
 */
public class BaseTest extends Constants {

	protected static String host = getHost();
	protected static String realmid = setRealmId();
	protected static String walletEntryId = PropertyUtil
			.getValue(WALLET_ENTRY_ID);
	protected static int recon_emulator_frequency = (TestUtil.isSBX) ? 300*1000 : 90*1000;

	protected static String transactionId = "";
	protected static String ccChargeUrl = buildUrl(CHARGE_CC, realmid);

	protected static String getHost() {
		return PropertyUtil.getValue(System.getenv("vhost") + "." + "url");
	}
	
	protected static String setRealmId(){
		String realmId = null;
		try {
			realmId = (new OnboardingManagerUtil()).getRealmId();
		} catch (Exception e) {			
		}
		
		if(System.getenv("vhost").equals("ctosbx")) {
			return PropertyUtil.getValue(SBX_REALM_ID);
		}
		
		//If realmid is not generated thorough OBS due to some failure, then default back to the hardcoded realmId in the properties file.
		if(realmId == null || realmId.isEmpty()){
			realmId = PropertyUtil.getValue(REALM_ID);
		}
		return realmId;
	}

	protected static String buildUrl(String baseUrlkey, Object... pathParams) {
		Object[] params = new Object[pathParams.length + 1];
		params[0] = host;
		for (int i = 0, j = 1; i <= pathParams.length - 1; i++, j++) {
			params[j] = pathParams[i];
		}
		return PropertyUtil.getValue(baseUrlkey, params);
	}

	protected String[] generateCreditCardNumbers(String[] prefix, int howMany) {
		return credit_card_number(prefix, 16, howMany);
	}

	protected String generateCreditCardNumber(String[] prefix) {
		return credit_card_number(prefix, 16, 1)[0];
	}

	protected String generateAmexCreditCardNumber(String[] prefix) {
		return credit_card_number(prefix, 15, 1)[0];
	}

	protected String[] generateMasterCardNumbers(int howMany) {
		return credit_card_number(MASTERCARD_PREFIX_LIST, 16, howMany);
	}

	protected String generateMasterCardNumber() {
		return credit_card_number(MASTERCARD_PREFIX_LIST, 16, 1)[0];
	}

	protected String[] credit_card_number(String[] prefixList, int length,
			int howMany) {

		Stack<String> result = new Stack<String>();
		for (int i = 0; i < howMany; i++) {
			int randomArrayIndex = (int) Math.floor(Math.random()
					* prefixList.length);
			String ccnumber = prefixList[randomArrayIndex];
			result.push(completed_number(ccnumber, length));
		}

		return result.toArray(new String[result.size()]);
	}

	/*
	 * 'prefix' is the start of the CC number as a string, any number of digits.
	 * 'length' is the length of the CC number to generate. Typically 13 or 16
	 */

	protected String completed_number(String prefix, int length) {

		String ccnumber = prefix;

		// generate digits

		while (ccnumber.length() < (length - 1)) {
			ccnumber += new Double(Math.floor(Math.random() * 10)).intValue();
		}

		// reverse number and convert to int

		String reversedCCnumberString = strrev(ccnumber);

		List<Integer> reversedCCnumberList = new ArrayList<Integer>();
		for (int i = 0; i < reversedCCnumberString.length(); i++) {
			reversedCCnumberList.add(new Integer(String
					.valueOf(reversedCCnumberString.charAt(i))));
		}

		// calculate sum

		int sum = 0;
		int pos = 0;

		Integer[] reversedCCnumber = reversedCCnumberList
				.toArray(new Integer[reversedCCnumberList.size()]);
		while (pos < length - 1) {

			int odd = reversedCCnumber[pos] * 2;
			if (odd > 9) {
				odd -= 9;
			}

			sum += odd;

			if (pos != (length - 2)) {
				sum += reversedCCnumber[pos + 1];
			}
			pos += 2;
		}

		// calculate check digit

		int checkdigit = new Double(
				((Math.floor(sum / 10) + 1) * 10 - sum) % 10).intValue();
		ccnumber += checkdigit;

		return ccnumber;

	}

	protected String strrev(String str) {
		if (str == null)
			return "";
		String revstr = "";
		for (int i = str.length() - 1; i >= 0; i--) {
			revstr += str.charAt(i);
		}

		return revstr;
	}
	
	
	public String generateOneTimeToken(HashMap<String, Object> map) throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		
		if(map.containsKey("number")){
			mapBody.put("card", map);
		}else if(map.containsKey("routingNumber")){
			mapBody.put("bankAccount", map);
		}else{
			throw new Exception("Please pass map of card or map of bank account");
		}
		
		HttpResponse response = TestUtil.post(TokenTest.tokenUrl,
				TokenTest.tokenHeaderParams, mapBody);
		HashMap<String, Object> result = validateSuccessfulTokenRes(response);
		return (String)result.get("value");
	}

	/**
	 * This will construct a map of card and return
	 * 
	 * @return
	 */
	protected HashMap<String, Object> returnMapCard() {

		HashMap<String, Object> mapCard = new HashMap<String, Object>();
		mapCard.put("number", "4112344112344113");
		mapCard.put("expMonth", "02");
		mapCard.put("expYear", "2020");
		mapCard.put("cvc", "123");
		mapCard.put("address", returnMapAddress());
		mapCard.put("name", "emulate=0");

		return mapCard;
	}
	
	protected HashMap<String, Object> returnMapCardWithCardPresent() {

		HashMap<String, Object> card = new HashMap<String, Object>();
		card.put("name", "Fefe Fefe");
		card.put("cardPresent", constructCardPresent());

		return card;
	}
	
	protected HashMap<String, Object> returnMapAddress(){
		HashMap<String, Object> mapAddress = new HashMap<String, Object>();
		mapAddress.put("streetAddress", "1130 Kifer Rd");
		mapAddress.put("city", "Sunnyvale");
		mapAddress.put("region", "CA");
		mapAddress.put("country", "US");
		mapAddress.put("postalCode", "94086");
		
		return mapAddress;
	}

	
	/**
	 * This will construct map of device info
	 * 
	 * @return
	 */
	protected HashMap<String, Object> constructMapDeviceInfo() {
		HashMap<String, Object> mapDeviceInfo = new HashMap<String, Object>();
		mapDeviceInfo.put("longitude", "100");
		mapDeviceInfo.put("latitude", "200");
		mapDeviceInfo.put("id", "7654321");
		mapDeviceInfo.put("type", "Type2");
		mapDeviceInfo.put("encrypted", false);
		mapDeviceInfo.put("phoneNumber", "4084088090");
		mapDeviceInfo.put("macAddress", "12-34-56-78-9A-BC");
		mapDeviceInfo.put("ipAddress", "10.10.10.10");

		return mapDeviceInfo;
	}

	/**
	 * This will construct map of card present
	 * 
	 * @return
	 */
	protected HashMap<String, Object> constructCardPresent() {
		HashMap<String, Object> cardPresent = new HashMap<String, Object>();
		cardPresent.put("track1", "");
		cardPresent.put("track2", "9999999800002773=20121015432112345678");
		cardPresent.put("ksn", "00000E0040000005");
		cardPresent.put("pinBlock", "A5F84A01C8EC5684");

		return cardPresent;
	}
	
	/**
	 * This will create map of bank account
	 * @return
	 */
	protected HashMap<String, Object> constructBankAcct(){
		HashMap<String, Object> bankAcct = new HashMap<String, Object>();
		bankAcct.put("name", "emulate=0");
		bankAcct.put("routingNumber", "1234567");
		bankAcct.put("accountNumber", "1234567");
		bankAcct.put("accountType", "checking");
		bankAcct.put("phoneNumber", "4088888888");
		
		return bankAcct;
	}

	/**
	 * This will validate a charge response is successful
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> validateChargeRes(
			HttpResponse response ,int statusCode, String status) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), statusCode );
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("id"));
		if(TestUtil.emulate == null) {
			Assert.assertTrue(map.get("id").toString().matches("E[A-Z0-9]{11}"));
		}else if(TestUtil.emulate.equals("false")) {
			Assert.assertTrue(map.get("id").toString().matches("P.{11}"));
		}
		Assert.assertEquals(map.get("created") != null, true);
		Assert.assertEquals(map.get("created").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") , true);
		Assert.assertEquals(map.get("status"), status );
		Assert.assertNotNull(map.get("authCode"));
		if((HashMap<String,Object>)map.get("card") != null) {
			HashMap<String,Object> address = (HashMap<String, Object>) ((HashMap<String,Object>)map.get("card")).get("address");
			if(address != null) {
				validateMaskAddress(address);
			}
		}
		return map;
	}
	
	
	
	/**
	 * This will validate a auth response
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> validateAuthRes(
			HttpResponse response, int statusCode, String status) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), statusCode );
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("id") );
		if(TestUtil.emulate == null) {
			Assert.assertTrue(map.get("id").toString().matches("E[A-Z0-9]{11}"));
		}else if(TestUtil.emulate.equals("false")) {
			Assert.assertTrue(map.get("id").toString().matches("P.{11}"));
		}
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals(map.get("created").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") , true);
		Assert.assertNotNull(map.get("authCode"));
		Assert.assertEquals(map.get("status"), status );
		if((HashMap<String,Object>)map.get("card") != null) {
			HashMap<String,Object> address = (HashMap<String, Object>) ((HashMap<String,Object>)map.get("card")).get("address");
			if(address != null) {
				validateMaskAddress(address);
			}
		}
		return map;
	}
	
	
	/**
	 * This will validate a token creation response is successful
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected HashMap<String, Object> validateSuccessfulTokenRes(
			HttpResponse response) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(),201 );
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("value") );
		
		return map;
	}
	
	/**
	 * This will update the value of the Map Card 
	 * @param params
	 * @return
	 */
	protected HashMap<String, Object> updateMapCard(String[][] testData){
		HashMap<String, Object> mapCard = returnMapCard();
		
		for(int i=0; i< testData.length ; i++){
			mapCard.put(testData[i][0], testData[i][1]);
		}
	
		return mapCard;
	}
	
	

	/**
	 * This will validate a capture response is successful
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> validateSuccessfulCaptureRes(
			HttpResponse response) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("id"));
		if(TestUtil.emulate == null) {
			Assert.assertTrue(map.get("id").toString().matches("E[A-Z0-9]{11}"));
		}else if(TestUtil.emulate.equals("false")) {
			Assert.assertTrue(map.get("id").toString().matches("P.{11}"));
		}
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals( map.get("created").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") , true);
		//Validate Status
		//Assert.assertEquals(map.get("status").toString(), Status.CAPTURED.getText());
		Assert.assertTrue(map.get("status").toString().equals(Status.CAPTURED.getText()) || map.get("status").toString().equals(Status.SETTLED.getText()));
		Assert.assertNotNull(map.get("amount"));
		Assert.assertNotNull(map.get("currency"));
		Assert.assertNotNull(map.get("card"));
		Assert.assertNotNull(map.get("capture"));
		Assert.assertNotNull(map.get("authCode"));
		Assert.assertNotNull(map.get("id"));
		Assert.assertNotNull(map.get("context"));
		Assert.assertNotNull(map.get("captureDetail"));
		if((HashMap<String,Object>)map.get("card") != null) {
			HashMap<String,Object> address = (HashMap<String, Object>) ((HashMap<String,Object>)map.get("card")).get("address");
			if(address != null) {
				validateMaskAddress(address);
			}
		}
		return map;
	}
	
	/**
	 * This will validate if a refund response is successful
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected HashMap<String, Object> validateRefundRes(
			HttpResponse response, int statusCode, String status) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), statusCode);
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("id"));
		if(TestUtil.emulate == null) {
			Assert.assertTrue(map.get("id").toString().matches("E[A-Z0-9]{11}"));
		}else if(TestUtil.emulate.equals("false")) {
			Assert.assertTrue(map.get("id").toString().matches("P.{11}"));
		}
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals( map.get("created").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") , true);
		//Validate refund Status
		Assert.assertEquals(map.get("status").toString(), status);
		Assert.assertNotNull(map.get("amount"));
		return map;
	}
	
	protected HashMap<String, Object> validateSuccessfulRefundRetrievalRes(
			HttpResponse response) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("id"));
		if(TestUtil.emulate == null) {
			Assert.assertTrue(map.get("id").toString().matches("E[A-Z0-9]{11}"));
		}else if(TestUtil.emulate.equals("false")) {
			Assert.assertTrue(map.get("id").toString().matches("P.{11}"));
		}
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals( map.get("created").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") , true);
		//Validate refund Status
		Assert.assertNotNull(map.get("status"));
		Assert.assertNotNull(map.get("amount"));
		Assert.assertEquals(map.containsKey("context"), true);
		return map;
	}
	
	
	/**
	 * This will validate if a retrieval response is successful
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> validateSuccessfulRetrievalRes(
			HttpResponse response) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals( map.get("created").toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") , true);
		Assert.assertNotNull(map.get("status"));
		Assert.assertNotNull(map.get("amount"));
		Assert.assertNotNull(map.get("currency"));
		Assert.assertNotNull(map.get("card"));
		Assert.assertNotNull(map.get("capture"));
		Assert.assertNotNull(map.get("authCode"));
		Assert.assertNotNull(map.get("id"));
		if(TestUtil.emulate == null) {
			Assert.assertTrue(map.get("id").toString().matches("E[A-Z0-9]{11}"));
		}else if(TestUtil.emulate.equals("false")) {
			Assert.assertTrue(map.get("id").toString().matches("P.{11}"));
		}
		Assert.assertNotNull(map.get("context"));
		if((HashMap<String,Object>)map.get("card") != null) {
			HashMap<String,Object> address = (HashMap<String, Object>) ((HashMap<String,Object>)map.get("card")).get("address");
			if(address != null) {
				validateMaskAddress(address);
			}
		}
		return map;
	}
	
	/**
	 * This will validate the error format
	 * @param response
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void validateErrorFormat(HashMap<String, Object> map) throws Exception{
		
		ArrayList<LinkedHashMap<String,Object>> errArray = ((ArrayList<LinkedHashMap<String,Object>>)map.get("errors"));
		LinkedHashMap<String,Object> errMap = errArray.get(0);
		Assert.assertEquals(errMap.containsKey("code"), true);
		Assert.assertEquals(errMap.containsKey("type"), true);
		Assert.assertEquals(errMap.containsKey("message"), true);
		//Assert.assertEquals(errMap.containsKey("detail"), true);
		//Assert.assertEquals(errMap.containsKey("moreInfo"), true);
		Assert.assertEquals(errMap.containsKey("infoLink"), true);
	}
	
	/**
	 * This will validate the response is error
	 * @param response
	 * @throws Exception
	 */
	protected void validateIfIsError(HttpResponse response) throws Exception{
		Assert.assertNotEquals(200, response.getStatusLine().getStatusCode());
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		validateErrorFormat(map);
	}
	
	/**
	 * This will validate the response is authorization error
	 * @param response
	 * @throws Exception
	 */
	protected void validateIfIsAuthorizationError(HttpResponse response) throws Exception{
		Assert.assertEquals(response.getStatusLine().getStatusCode(),401 );
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		validateErrorFormat(map);
	}
	
	/**
	 * This will validate the error response with the fields
	 * @param res
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> validateErrorRes(HttpResponse res,String error) throws Exception{
		Assert.assertEquals(true, res.getStatusLine().getStatusCode()!=200 );
		Assert.assertEquals(true, res.getStatusLine().getStatusCode()!=201 );
		HashMap<String, Object> map = JsonTestClient.convertToMap(res);
		
		ArrayList<LinkedHashMap<String,Object>> errArray = ((ArrayList<LinkedHashMap<String,Object>>)map.get("errors"));
		LinkedHashMap<String,Object> errMap = errArray.get(0);
		
		if(error!=null){
			Assert.assertEquals(errMap.get("code").toString().equalsIgnoreCase(error), true);
		}else{
			Assert.assertNotNull(errMap.get("code"));
		}
		
		Assert.assertEquals(errMap.containsKey("type"), true);
		Assert.assertEquals(errMap.containsKey("message"), true);
		//Assert.assertEquals(errMap.containsKey("detail"), true);
		//Assert.assertEquals(errMap.containsKey("moreInfo"), true);
		Assert.assertEquals(errMap.containsKey("infoLink"), true);
		
		return errMap;
	}
	
	/**
	 * This will validate the error response with the fields
	 * @param res
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> validateErrorRes(HttpResponse res, int statusCode,String type,String code, String message, String detail,String infolink) throws Exception{
		Assert.assertEquals(res.getStatusLine().getStatusCode(), statusCode );
		HashMap<String, Object> map = JsonTestClient.convertToMap(res);
		ArrayList<LinkedHashMap<String,Object>> errArray = ((ArrayList<LinkedHashMap<String,Object>>)map.get("errors"));
		LinkedHashMap<String,Object> errMap = errArray.get(0);
		
		Assert.assertEquals((errMap.get("code")).toString().equalsIgnoreCase(code), true);	
		Assert.assertEquals((errMap.get("type")).toString().equalsIgnoreCase(type), true);	
		Assert.assertEquals((errMap.get("message")).toString().equalsIgnoreCase(message), true);
		//Assert.assertEquals(errMap.containsKey("detail"), true);
		//Assert.assertEquals((errMap.get("moreInfo")).toString().equalsIgnoreCase(moreinfo), true);
		Assert.assertEquals((errMap.get("infoLink")).toString().equalsIgnoreCase(infolink), true);
		
		return map;
	}
	
	/**
	 * This will convert HTTP response to JSON array.
	 * @param response
	 * @throws Exception
	 */
	public static JSONArray convertRespToJsonArray(HttpResponse res) throws Exception{
		BufferedReader br = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
		StringBuilder content = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
		    content.append(line);
		}
		JSONArray finalResult = new JSONArray(content.toString());
		return finalResult;
	}
	
	public String[][] getCCChargeHeaderParams(){
		String[][] headers = { { "Company-Id",realmid }, {"Request-Id", UUID.randomUUID().toString()}};
		return headers;
	}
	
	public String[][] getCCChargeHeaderParamsWithEmulatedFlag(String errorCode){
		String[][] headers = { { "Company-Id",realmid }, {"Request-Id", UUID.randomUUID().toString()}, {"emulation", "emualte="+errorCode}};
		return headers;
	}

	
	/**
	 * Charge CC with 5 USD and return http response
	 * @return
	 * @throws Exception
	 */
	public HttpResponse charge(Double amt, String[][] headers) throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", amt);
		mapBody.put("currency", "USD");
		mapBody.put("card", returnMapCard());

		return TestUtil.post(buildUrl(CHARGE_CC),
				headers, mapBody);
	}
	
	/**
	 * Auth with x USD and return http response
	 * @return
	 * @throws Exception
	 */
	public HttpResponse auth(Double amt, String[][] testData) throws Exception{
		HashMap<String, Object> mapBody = new HashMap<String, Object>();
		mapBody.put("amount", amt);
		mapBody.put("currency", "USD");
		if(testData != null){
			mapBody.put("card", updateMapCard(testData));
		}else{
			mapBody.put("card", returnMapCard());
		}
		mapBody.put("capture", false);
		return TestUtil.post(buildUrl(CHARGE_CC),
				getCCChargeHeaderParams(), mapBody);
	}
	
	public HttpResponse capture(String chargeid, String amt , String[][] headers) throws Exception{
		HashMap<String, Object> captureBody = new HashMap<String, Object>();
		captureBody.put("amount", Double.valueOf(amt));

		return TestUtil.post(buildUrl(CAPTURE_CC, realmid, chargeid),
				headers, captureBody);
	}
	
	public HttpResponse refund(String chargeid, String amt , String[][] headers) throws Exception {
		HashMap<String, Object> refundBody = new HashMap<String, Object>();
		refundBody.put("amount", Double.valueOf(amt));
		return TestUtil.post(buildUrl(REFUND_CC, realmid, chargeid),
				headers, refundBody);
	}
	
	public String validateAuthRetrieval(HashMap<String, Object>  retrievalMap, String amt, String chargeid, String status){
		Assert.assertEquals(status, retrievalMap.get("status"));
		Assert.assertEquals(amt, retrievalMap.get("amount").toString());
		Assert.assertEquals(false, retrievalMap.get("capture"));
		Assert.assertEquals(true, retrievalMap.get("authCode").toString().matches(".{6}"));
		Assert.assertEquals(chargeid, retrievalMap.get("id"));
		Assert.assertEquals(true, ((HashMap<String,Object>)retrievalMap.get("card")).get("number").toString().matches("x{11,12}\\d{4}"));	
		return retrievalMap.get("authCode").toString();
	}
	
	public void validateCaptureRetrieval(HashMap<String, Object> retrievalMap, String authCode, String amt){
		Assert.assertEquals(Status.CAPTURED.getText(), retrievalMap.get("status"));
		Assert.assertEquals(false, retrievalMap.get("capture"));
		Assert.assertEquals(retrievalMap.get("authCode"), authCode);
		Assert.assertNotNull(retrievalMap.get("captureDetail"));
		validateCaptureDetail((HashMap<String, Object>)retrievalMap.get("captureDetail"), amt);
	}
	
	public void validateChargeRetrieval(HashMap<String, Object> retrievalMap, String amt, String authCode,String chargeid, String status ){
		Assert.assertEquals(retrievalMap.get("status"), status);
		Assert.assertEquals(retrievalMap.get("capture"), true);
		Assert.assertEquals(retrievalMap.get("authCode"), authCode);
		Assert.assertEquals(retrievalMap.get("id"), chargeid);
		
	}
	
	public String validateRefundRetrieval(HashMap<String, Object> retrievalMap,String amt ,String refundId, String status){
		//Assert.assertEquals(status, retrievalMap.get("status"));
		Assert.assertNotNull(retrievalMap.get("created"));
		Assert.assertEquals(amt, retrievalMap.get("amount").toString());
		Assert.assertEquals(refundId, retrievalMap.get("id"));
		
		return (String)retrievalMap.get("id");

	}
	
	public void validateListTxn(HashMap<String, Object> map, String authCode, String chargeid, String status){
		Assert.assertEquals(authCode, map.get("authCode"));
	    Assert.assertEquals(chargeid, map.get("id"));
		Assert.assertEquals(status, map.get("status"));
	}
	
	
	public void validateCaptureDetail(HashMap<String, Object> map, String amt){
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals(amt, map.get("amount").toString());
	}
	
	public void validateRefundDetail(HashMap<String, Object> map, String amt, String refundId, String status){
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals(amt, map.get("amount").toString());
		Assert.assertEquals(refundId, map.get("id"));
		Assert.assertEquals(status, map.get("status"));
	}
	
	public void validateRefundDetail(HashMap<String, Object> map, String amt, String refundId){
		Assert.assertNotNull(map.get("created"));
		Assert.assertEquals(amt, map.get("amount").toString());
		Assert.assertEquals(refundId, map.get("id"));
		Assert.assertTrue(map.get("status").toString().equals(Status.ISSUED.getText()) || map.get("status").toString().equals(Status.SETTLED.getText()));
	}
	
	public HashMap<String,Object> validateCardsDetail(HttpResponse response, int statusCode) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), statusCode);
		HashMap<String, Object> map = JsonTestClient.convertToMap(response);
		
		Assert.assertNotNull(map.get("id"));
		Assert.assertTrue(map.get("id").toString().matches("[0-9]{24}"));
		Assert.assertNotNull(map.get("number"));
		Assert.assertNotNull(map.get("expMonth"));
		Assert.assertNotNull(map.get("expYear"));
		
		return map;
	}
	
	public void validateListCards(HttpResponse response) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
		List<HashMap<String, Object>> cards = getJsonList(response);
		Assert.assertTrue(cards.size() != 0);
		
		for(int i = 0; i < cards.size(); i++) {
			HashMap<String,Object> card = cards.get(i);
			Assert.assertNotNull(card.get("id"));
			Assert.assertTrue(card.get("id").toString().matches("[0-9]{24}"));
			Assert.assertNotNull(card.get("number"));
			Assert.assertNotNull(card.get("expMonth"));
			Assert.assertNotNull(card.get("expYear"));
		}
	}
	
	public void validateEmptyCard(HttpResponse response) throws Exception {
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
		List<HashMap<String, Object>> cards = getJsonList(response);
		Assert.assertTrue(cards.size() == 0);
	}
	
	public void validateMaskAddress(HashMap<String, Object> address) {
		
		for(String key : address.keySet()) {
			if(address.get(key).toString().length() != 0) {
				Assert.assertTrue(address.get(key).toString().matches("x+"));
			}
		}
	}
	
	public List<HashMap<String, Object>> getJsonList(HttpResponse listResponse) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();	
	    return objectMapper.readValue(
	    		getResultBody(listResponse),
	            objectMapper.getTypeFactory().constructCollectionType(
	                    List.class, HashMap.class));
	}
	
	private static String getResultBody(HttpResponse response) {
		BufferedReader rd = null;

		StringBuffer result = new StringBuffer();
		String line = null;

		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("result is " + result);
		return result.toString();
	}
	
}
