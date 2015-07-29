package com.intuit.outboundpayments;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.intuit.outboundpayments.jsonobject.BankAccountCreateRequest;
import com.intuit.outboundpayments.jsonobject.BankAccountCreateResponse;
import com.intuit.outboundpayments.jsonobject.BankAccountUpdateRequest;
import com.intuit.outboundpayments.jsonobject.BankAccountUpdateResponse;
import com.intuit.outboundpayments.jsonobject.ClaimTransferRequest;
import com.intuit.outboundpayments.jsonobject.Errors;
import com.intuit.outboundpayments.jsonobject.From;
import com.intuit.outboundpayments.jsonobject.HoldTransferRequest;
import com.intuit.outboundpayments.jsonobject.ReclaimTransferRequest;
import com.intuit.outboundpayments.jsonobject.To;
import com.intuit.outboundpayments.jsonobject.TransferResponse;

public class TransferServiceUtil {
	
	final static Logger logger = Logger.getLogger(TransferServiceUtil.class);
	static ObjectMapper mapper = new ObjectMapper();
	
			
	public static String createHoldRequestJsonBody(String routingNo,
			String accountNo) {
		//FIXME: Hard coded fields removal
		HoldTransferRequest transfer = new HoldTransferRequest();
		transfer.setAmount(10.51);
		transfer.setDescription("Outbound payment");
		transfer.setStatementName("Jinnius");

		From from = new From();
		from.setName("Jinnius Solutions Inc.");
		from.setRoutingNumber(routingNo);
		from.setAccountNumber(accountNo);
		from.setAccountType("BusinessChecking");
		transfer.setFrom(from);

		try {
			return mapper.writeValueAsString(transfer);

		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException occured for createHoldJsonBody() ");
			return null;

		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for createHoldJsonBody() ");
			return null;

		} catch (IOException exception) {
			logger.error("IOException occured for createHoldJsonBody() ");
			return null;
		}
	}
	
	public static String createHoldRequestJsonBodyWithToFields(String routingNo,
			String accountNo) {
		//FIXME: Hard coded fields removal
		HoldTransferRequest transfer = new HoldTransferRequest();
		transfer.setAmount(10.51);
		transfer.setDescription("Outbound payment");
		transfer.setStatementName("Jinnius");

		From from = new From();
		from.setName("Jinnius Solutions Inc.");
		from.setRoutingNumber(routingNo);
		from.setAccountNumber(accountNo);
		from.setAccountType("BusinessChecking");
		transfer.setFrom(from);
		
		To to = new To();
		to.setName("Katya Vaddadi");
		to.setRoutingNumber(routingNo);
		to.setAccountNumber(accountNo);
		to.setAccountType("BusinessChecking");
		transfer.setTo(to);

		try {
			return mapper.writeValueAsString(transfer);

		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException occured for createHoldJsonBody() ");
			return null;

		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for createHoldJsonBody() ");
			return null;

		} catch (IOException exception) {
			logger.error("IOException occured for createHoldJsonBody() ");
			return null;
		}
	}
	
	public static String createClaimRequestJsonBody(String routingNo,
			String accountNo) {
		//FIXME: Hard coded fields removal
		ClaimTransferRequest transfer = new ClaimTransferRequest();
		transfer.setStatementName("Jinnius");

		To to = new To();
		to.setName("Arash Ramin");
		to.setRoutingNumber(routingNo);
		to.setAccountNumber(accountNo);
		to.setAccountType("BusinessChecking");
		transfer.setTo(to);

		try {
			return mapper.writeValueAsString(transfer);

		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException occured for createClaimJsonBody() ");
			return null;

		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for createClaimJsonBody() ");
			return null;

		} catch (IOException exception) {
			logger.error("IOException occured for createClaimJsonBody() ");
			return null;
		}
	}

	public static String createReclaimRequestJsonBody(String statementName) {

		ReclaimTransferRequest transfer = new ReclaimTransferRequest();
		transfer.setStatementName(statementName);

		try {
			return mapper.writeValueAsString(transfer);

		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException occured for createReclaimJsonBody() ");
			return null;

		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for createReclaimJsonBody() ");
			return null;

		} catch (IOException exception) {
			logger.error("IOException occured for createReclaimJsonBody() ");
			return null;
		}
	}

	public static Errors getErrorResponseObject(String returnedJsonString) {
		try {
			return mapper.readValue(returnedJsonString, Errors.class);
		} catch (JsonParseException e) {
			logger.error("JsonParseException occured for getErrorResponseObject() ");
			return null;
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for getErrorResponseObject() ");
			return null;
		} catch (IOException e) {
			logger.error("IOException occured for getErrorResponseObject() ");
			return null;
		}
	}
	
	public static TransferResponse getTransferResponseObject(String returnedJsonString) {
		try {
			return mapper.readValue(returnedJsonString, TransferResponse.class);
		} catch (JsonParseException e) {
			logger.error("JsonParseException occured for getTransferResponseObject() ");
			return null;
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for getTransferResponseObject() ");
			return null;
		} catch (IOException e) {
			logger.error("IOException occured for getTransferResponseObject() ");
			return null;
		}
	}
	public static BankAccountCreateResponse getBankAccountCreateResponseObject(String returnedJsonString) {
		try {
			return mapper.readValue(returnedJsonString, BankAccountCreateResponse.class);
		} catch (JsonParseException e) {
			logger.error("JsonParseException occured for getBankAccountResponseObject() ");
			return null;
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for getBankAccountResponseObject() ");
			return null;
		} catch (IOException e) {
			logger.error("IOException occured for getBankAccountResponseObject() ");
			return null;
		}
	}
	
	public static BankAccountUpdateResponse getBankAccountUpdateResponseObject(String returnedJsonString) {
		try {
			return mapper.readValue(returnedJsonString, BankAccountUpdateResponse.class);
		} catch (JsonParseException e) {
			logger.error("JsonParseException occured for getBankAccountResponseObject() ");
			return null;
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for getBankAccountResponseObject() ");
			return null;
		} catch (IOException e) {
			logger.error("IOException occured for getBankAccountResponseObject() ");
			return null;
		}
	}
	
	

	
	public static String createBankAccountRequestJsonBody(String routingNo,
			String accountNo) {
		BankAccountCreateRequest bankAccountRequest = new BankAccountCreateRequest();
		BankAccountCreateRequest.BankAccount bankAccount =  bankAccountRequest.new BankAccount();
		bankAccount.setAccountNumber(accountNo);
		bankAccount.setRoutingNumber(routingNo);
		bankAccount.setAccountType("Checking");
		bankAccount.setName("Jinnius Solutions Inc.");
		bankAccount.setPhoneNumber("6047296480");
		bankAccountRequest.setBankAccount(bankAccount);
		
		try {
			return mapper.writeValueAsString(bankAccountRequest);

		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException occured for createBankAccountRequestJsonBody() ");
			return null;

		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for createBankAccountRequestJsonBody() ");
			return null;

		} catch (IOException exception) {
			logger.error("IOException occured for createBankAccountRequestJsonBody() ");
			return null;
		}

	}
	
	public static String updateBankAccountRequestJsonBody(String routingNo,
			String accountNo) {
		BankAccountUpdateRequest bankAccountRequest = new BankAccountUpdateRequest();
		BankAccountUpdateRequest.BankAccount bankAccount =  bankAccountRequest.new BankAccount();
		bankAccount.setRoutingNumber(routingNo);
		bankAccount.setAccountType("Checking");
		bankAccount.setName("Jinnius Solutions Inc.");
		bankAccount.setPhoneNumber("6047296480");
		bankAccountRequest.setBankAccount(bankAccount);
		
		try {
			return mapper.writeValueAsString(bankAccountRequest);

		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException occured for createBankAccountRequestJsonBody() ");
			return null;

		} catch (JsonMappingException e) {
			logger.error("JsonMappingException occured for createBankAccountRequestJsonBody() ");
			return null;

		} catch (IOException exception) {
			logger.error("IOException occured for createBankAccountRequestJsonBody() ");
			return null;
		}

	}


}
