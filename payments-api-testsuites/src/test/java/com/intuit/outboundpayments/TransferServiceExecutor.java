package com.intuit.outboundpayments;

import static com.jayway.restassured.RestAssured.given;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransferServiceExecutor {
	
	protected String performHoldLeg(int expectedStatusCode, String holdJsonBody) {
		assertFalse(holdJsonBody == null || holdJsonBody.isEmpty(),
				"Failed to create holdJsonBodyValidFields");
		String returnedJsonString = 
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
				.body(holdJsonBody)
			.when()
			.	post("/v2/transfers/bankaccount")
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
				.asString();
		return returnedJsonString;
	}
	
	protected String performClaimLeg(String xferId, int expectedStatusCode, String claimJsonBody) {
		String returnedJsonString;
		assertFalse(claimJsonBody == null
				|| claimJsonBody.isEmpty(),
				"Failed to create claimJsonBodyValidFields");
		returnedJsonString = 
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
				.body(claimJsonBody)
			.when()
				.post("/v2/transfers/bankaccount/{xferId}/claim", xferId)
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
				.asString();
		return returnedJsonString;
	}
	
	protected String performReclaimLeg(String xferId, int expectedStatusCode, String reclaimJsonBody) {
		String returnedJsonString;
		assertFalse(reclaimJsonBody == null
				|| reclaimJsonBody.isEmpty(),
				"Failed to create reclaimJsonBodyValidFields");
		returnedJsonString = 
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
				.body(reclaimJsonBody)
			.when()
				.post("/v2/transfers/bankaccount/{xferId}/reclaim", xferId)
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
				.asString();
		return returnedJsonString;
	}
	
	protected String performCreateBankAccount(String userId, int expectedStatusCode, String bankAcctJsonBody) {
		assertFalse(bankAcctJsonBody == null || bankAcctJsonBody.isEmpty(),
				"Failed to create bankAcctJsonBody");
		String returnedJsonString = 
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
				.body(bankAcctJsonBody)
			.when()
			.	post("/v2/{userId}/bankaccounts", userId)
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
				.asString();
		return returnedJsonString;
	}
	
	protected String performUpdateBankAccount(String userId, int expectedStatusCode, String bankAcctJsonBody, String bankAcctId) {
		assertFalse(bankAcctJsonBody == null || bankAcctJsonBody.isEmpty(),
				"Failed to create bankAcctJsonBody");
		
		String returnedJsonString = 
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
				.body(bankAcctJsonBody)
			.when()
			.	put("/v2/{userId}/bankaccounts/{bankAcctId}", userId, bankAcctId)
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
				.asString();
		return returnedJsonString;
	}
	
	protected void performDeleteBankAccount(String userId, int expectedStatusCode, String bankAcctJsonBody, String bankAcctId) {
		assertFalse(bankAcctJsonBody == null || bankAcctJsonBody.isEmpty(),
				"Failed to create bankAcctJsonBody");
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
				.body(bankAcctJsonBody)
			.when()
			.	delete("/v2/{userId}/bankaccounts/{bankAcctId}", userId, bankAcctId)
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
			.asString();
	}

	

	protected String performRetrieveBankAccount(String userId, int expectedStatusCode, String bankAcctId) {		
		String returnedJsonString = 
			given()
				.contentType(BaseSetup.CONTENT_TYPE)
				.header("Authorization", BaseSetup.authorizationHeader)
				.header("Company-Id", BaseSetup.companyIdHeader)
				.header("Request-Id", UUID.randomUUID().toString())
			.when()
			.	get("/v2/{userId}/bankaccounts/{bankAcctId}", userId, bankAcctId)
			.then()
				.statusCode(expectedStatusCode)
			.extract().response().getBody()
				.asString();
		return returnedJsonString;
	}


	

}
