package com.intuit.outboundpayments;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/*
 * @author katyayani_vaddadi@intuit.com
 */

public class TransfersServiceTest{
	final static Logger logger = Logger.getLogger(TransfersServiceTest.class);
	ObjectMapper mapper = new ObjectMapper();
	TransferServiceValidators validator = new TransferServiceValidators();
	TransferServiceExecutor executor = new TransferServiceExecutor();
	
	@BeforeClass
	protected void setup() {
		BaseSetup.setup();
	}
/**********************************************************************************************************************
 * POSITIVE TEST FLOWS
 * 
 **********************************************************************************************************************/
	/*
	 * Test 1: To test successful hold flow followed by claim flow 
	 * See {@url  https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 1
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldClaim_positive() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000358", "9876");
		returnedJsonString = executor.performClaimLeg(xferId, 200, claimJsonBody);
		validator.validateSuccessfulClaimLeg(xferId, returnedJsonString);
	}

	/*
	 * Test 2: To test successful hold flow followed by reclaim flow 
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 2
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldReclaim_positive() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** RECLAIM ******************/
		String reclaimJsonBody = TransferServiceUtil.createReclaimRequestJsonBody("Jinnius");
		returnedJsonString = executor.performReclaimLeg(xferId,
				200, reclaimJsonBody);
		validator.validateSuccessfulReclaimLeg(xferId, returnedJsonString);
	}
	/**********************************************************************************************************************
	 * POSITIVE TEST FLOWS--TO BODY IN HOLD/CLAIM REQUEST
	 * 
	 **********************************************************************************************************************/

	/*
	 * Test : To test successful hold flow followed by claim flow with "To" body in both 
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 2
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldClaim_with_ToBody_positive() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBodyWithToFields(
				"121000358", "37924535");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000358", "9876");
		returnedJsonString = executor.performClaimLeg(xferId, 200, claimJsonBody);
		validator.validateSuccessfulClaimLeg(xferId, returnedJsonString);
	}
	
	/**********************************************************************************************************************
	 * NEGATIVE TEST FLOWS--INCORRECT ROUTING NO. 
	 * 
	 **********************************************************************************************************************/

	/*
	 * Test 3: To test unsuccessful hold flow with incorrect less than length for routing Number
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 3
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testHoldFlow_IncorrectRoutingNoLessThanLength_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"12100035", "35834534");	//Note incorrect length of routing no
		String jsonResponse = executor.performHoldLeg(400, holdJsonBodyValidFields);
		validator.validateErrorResponse(jsonResponse, "PMT-4000");  //NOTE: this error code is subject to change
	}
	
	/*
	 * Test 4: To test unsuccessful hold flow with incorrect greater than length for routing Number
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 4
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testHoldFlow_IncorrectRoutingNoGreaterThanLength_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"1210003587", "35834534");	//Note incorrect length of routing no
		String jsonResponse = executor.performHoldLeg(400, holdJsonBodyValidFields);
		validator.validateErrorResponse(jsonResponse, "PMT-4000");  //NOTE: this error code is subject to change
	}

	
	/*
	 * Test 5: To test unsuccessful hold flow with incorrect for routing Number not found in system
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 5
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testHoldFlow_IncorrectRoutingNo_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000359", "35834534");	//Note incorrect length of routing no
		String jsonResponse = executor.performHoldLeg(400, holdJsonBodyValidFields);
		validator.validateErrorResponse(jsonResponse, "PMT-4000");  //NOTE: this error code is subject to change
	}
	
	/*
	 * Test 6: To test successful hold flow followed by unsuccessful claim flow due to incorrect routing no
	 * See {@url  https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 6
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldClaim_incorrectRoutingNo_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000359", "9876");
		returnedJsonString = executor.performClaimLeg(xferId, 400, claimJsonBody);
		validator.validateErrorResponse(returnedJsonString, "PMT-4000"); //NOTE: this error code is subject to change
	}
	/**********************************************************************************************************************
	 * NEGATIVE TEST FLOWS--MULTIPLE CLAIM/RECLAIM W.R.T SAME XFER ID
	 * 
	 **********************************************************************************************************************/
	/*
	 * Test 7: To test successful hold flow followed by 2 claim flows with same XferId (negative test) 
	 * See {@url  https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 7
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldClaimClaim_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000358", "9876");
		returnedJsonString = executor.performClaimLeg(xferId, 200, claimJsonBody);
		validator.validateSuccessfulClaimLeg(xferId, returnedJsonString);
		/************** CLAIM ******************/
		returnedJsonString = executor.performClaimLeg(xferId, 400, claimJsonBody);
		validator.validateErrorResponse(returnedJsonString, "PMT-4000"); //NOTE: this error code is subject to change
	}
	/*
	 * Test 8: To test successful hold flow followed by successful reclaim flow followed by claim flow (negative test) 
	 * See {@url  https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 8
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldReclaimClaim_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** RECLAIM ******************/		
		String reclaimJsonBody = TransferServiceUtil.createReclaimRequestJsonBody("Jinnius");
		returnedJsonString = executor.performReclaimLeg(xferId,
				200, reclaimJsonBody);
		validator.validateSuccessfulReclaimLeg(xferId, returnedJsonString);
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000359", "9876");
		returnedJsonString = executor.performClaimLeg(xferId, 400, claimJsonBody);
		validator.validateErrorResponse(returnedJsonString, "PMT-4000"); //NOTE: this error code is subject to change
	}
	/*
	 * Test 9: To test successful hold flow followed by successful claim flow followed by reclaim flow (negative test) 
	 * See {@url  https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 9
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldClaimReclaim_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000358", "9876");
		returnedJsonString = executor.performClaimLeg(xferId, 200, claimJsonBody);
		validator.validateSuccessfulClaimLeg(xferId, returnedJsonString);
		/************** RECLAIM ******************/
		String reclaimJsonBody = TransferServiceUtil.createReclaimRequestJsonBody("Jinnius");
		returnedJsonString = executor.performClaimLeg(xferId, 400, reclaimJsonBody);
		validator.validateErrorResponse(returnedJsonString, "PMT-4000"); //NOTE: this error code is subject to change
	}
	/**********************************************************************************************************************
	 * NEGATIVE TEST FLOWS--INCORRECT XFER ID
	 * 
	 **********************************************************************************************************************/
	/*
	 * Test 10: To test successful hold flow followed by claim flow with incorrect XferId
	 * See {@url  https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 10
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldClaimIncorrectXFerID_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** CLAIM ******************/
		String claimJsonBody = TransferServiceUtil.createClaimRequestJsonBody(
				"121000358", "9876");
		returnedJsonString = executor.performClaimLeg("9999999999999", 404, claimJsonBody); //NOTE: the incorrect Xfer ID
		validator.validateErrorResponse(returnedJsonString, "PMT-4000"); //NOTE: this error code is subject to change
	}
	
	/*
	 * Test 11: To test successful hold flow followed by reclaim flow  with incorrect XferId
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 11
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testTransferFlow_HoldReclaimIncorrectXFerID_negative() {
		/************** HOLD ********************/
		String holdJsonBodyValidFields = TransferServiceUtil.createHoldRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performHoldLeg(201, holdJsonBodyValidFields);
		String xferId = validator.validateSuccessfulHoldLeg(returnedJsonString);
		/************** RETRIEVE ******************/
		// TODO: make a retrieve request and validate fields
		/************** RECLAIM ******************/
		String reclaimJsonBody = TransferServiceUtil.createReclaimRequestJsonBody("Jinnius");
		returnedJsonString = executor.performReclaimLeg("9999999999999",
				404, reclaimJsonBody);
		validator.validateErrorResponse(returnedJsonString, "PMT-4000"); //NOTE: this error code is subject to change
	}
	
	/**********************************************************************************************************************
	 * POSITIVE TEST FLOW--BANKACCOUNT TESTS
	 * 
	 **********************************************************************************************************************/
	/*
	 * Test 12: To test successful hold flow followed by reclaim flow  with incorrect XferId
	 * See {@url https://wiki.intuit.com/display/psd/Payments+API+test+cases+for+Outbound+payments} Test Case 12
	 */
	@Test(groups = {"outboundPaymentsTest"})
	public void testBankAccountsFlow_CreateUpdateDelete() {
		/************** CREATE ********************/
		String bankAccountsJsonBodyValidFields = TransferServiceUtil.createBankAccountRequestJsonBody(
				"121000358", "35834534");
		String returnedJsonString = executor.performCreateBankAccount("1086469995", 200, bankAccountsJsonBodyValidFields);
		String bankAccountId = validator.validateSuccessCreateBankAccountResponse(returnedJsonString);
		/************** UPDATE ********************/
		bankAccountsJsonBodyValidFields = TransferServiceUtil.updateBankAccountRequestJsonBody(
				"121000358", "55555534");
		returnedJsonString = executor.performUpdateBankAccount("1086469995", 200, bankAccountsJsonBodyValidFields, bankAccountId);
		validator.validateSuccessUpdateBankAccountResponse(returnedJsonString);
		/************** RETRIEVE ********************/
		returnedJsonString = executor.performRetrieveBankAccount("1086469995", 200, bankAccountId);
		validator.validateSuccessCreateBankAccountResponse(returnedJsonString);		
		/************** DELETE ********************/
		bankAccountsJsonBodyValidFields = TransferServiceUtil.createBankAccountRequestJsonBody(
				"121000358", "55555534");
//		executor.performDeleteBankAccount("1086469995", 200, bankAccountsJsonBodyValidFields, bankAccountId);
	}


	/**********************************************************************************************************************
	 * NEGATIVE TEST FLOWS--CONCURRENCY TESTS
	 * 
	 **********************************************************************************************************************/


	
}