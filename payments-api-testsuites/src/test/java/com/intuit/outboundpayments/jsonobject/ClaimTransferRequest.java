package com.intuit.outboundpayments.jsonobject;

public class ClaimTransferRequest{
	private String statementName;
	private To to;

	public String getStatementName() {
		return statementName;
	}
	public void setStatementName(String statementName) {
		this.statementName = statementName;
	}

	public To getTo() {
		return to;
	}
	public void setTo(To to) {
		this.to = to;
	}

}
