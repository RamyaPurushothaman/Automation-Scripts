package com.intuit.paymentsapi.us;

public enum Status {
	
	AUTHORIZED("AUTHORIZED"),
	DECLINED("DECLINED"),
	VOIDED("VOIDED"), 
	CAPTURED("CAPTURED"),
	CANCELLED("CANCELLED"),
	REFUNDED("REFUNDED"),
	SETTLED("SETTLED"),
	ISSUED("ISSUED");
	
	private String text;

	Status(String text) {
	    this.text = text;
	  }

	  public String getText() {
	    return this.text;
	  }

}
