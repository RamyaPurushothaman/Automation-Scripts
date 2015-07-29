package com.intuit.outboundpayments;

import static org.testng.Assert.*;

import com.intuit.outboundpayments.jsonobject.*;
import com.intuit.outboundpayments.jsonobject.Error;

public class TransferServiceValidators {
	
	protected String validateSuccessfulHoldLeg(String returnedJsonString) {

		// Validate that hold response is not empty
		assertFalse(returnedJsonString == null || returnedJsonString.isEmpty(),
				"Hold response is Empty");
		TransferResponse holdResponse = TransferServiceUtil.getTransferResponseObject(returnedJsonString);
		// TODO: Validate that the holdDetail status has resulted in
		// "positive status"

		// Validate that XferId is not null
		String xferId = (holdResponse != null) ? holdResponse.getTransferId()
				: null;
		assertNotNull(xferId, "XferId is null");
		return xferId;
	}
	
	protected void validateSuccessfulClaimLeg(String xferId,
			 String returnedJsonString) {

		// Validate that claim response is not empty
		assertFalse(returnedJsonString == null || returnedJsonString.isEmpty(),
				"Claim response is Empty");

		TransferResponse claimResponse = TransferServiceUtil.getTransferResponseObject(returnedJsonString);
		assertNotNull(claimResponse, "Claim response.");
		// TODO: Validate that the claimDetail status has resulted in
		// "positive status"

		// Validate that XferId is the same as the one received in the hold
		// response
		assertEquals(xferId, claimResponse.getTransferId(),
				"XferId is not the same");
	}
	
	protected void validateSuccessfulReclaimLeg(String xferId,
			String returnedJsonString) {

		// Validate that reclaim response is not empty
		assertFalse(returnedJsonString == null || returnedJsonString.isEmpty(),
				"Reclaim response is Empty");

		TransferResponse reclaimResponse = TransferServiceUtil.getTransferResponseObject(returnedJsonString);
		assertNotNull(reclaimResponse);
		// TODO: Validate that the reclaimDetail status has resulted in
		// "positive status"

		// Validate that XferId is the same as the one received in the hold
		// response
		assertEquals(xferId, reclaimResponse.getTransferId(),
				"XferId is not the same");
	}

	protected void validateErrorResponse(String returnedJsonString, String paymentsErrorCode){
		assertFalse(returnedJsonString == null || returnedJsonString.isEmpty(),
				"Error response is Empty");
		Errors errors = TransferServiceUtil.getErrorResponseObject(returnedJsonString);
		assertNotNull(errors);
		//TODO: Currently there is always only one error in the returned array of errors. For future changes, modify this method 
		//to validate all errors returned.
		for(Error error: errors.getErrors()){	//currently has only one element
			if(error!=null){
				assertEquals(paymentsErrorCode, error.getCode());
				//TODO: Validate more fields like type, message, and detail once the text is finalized.
			}
		}
			
	}
	
	protected String validateSuccessCreateBankAccountResponse(String returnedJsonString) {

		assertFalse(returnedJsonString == null || returnedJsonString.isEmpty(),
				"Create BankAccount response is Empty");
		BankAccountCreateResponse bankAccountResponse = TransferServiceUtil.getBankAccountCreateResponseObject(returnedJsonString);
		
		assertTrue(bankAccountResponse!=null 
				&& bankAccountResponse.getBankAccount()!=null		
				&& bankAccountResponse.getBankAccount().getBankAccountId()!=null);
		return bankAccountResponse.getBankAccount().getBankAccountId();

	}
	
	protected String validateSuccessUpdateBankAccountResponse(String returnedJsonString) {

		assertFalse(returnedJsonString == null || returnedJsonString.isEmpty(),
				"Update BankAccount response is Empty");
		BankAccountUpdateResponse bankAccountResponse = TransferServiceUtil.getBankAccountUpdateResponseObject(returnedJsonString);
		
		assertTrue(bankAccountResponse!=null 
				&& bankAccountResponse.getBankAccount()!=null		
				&& bankAccountResponse.getBankAccount().getBankAccountId()!=null);
		return bankAccountResponse.getBankAccount().getBankAccountId();

	}

	

}
