package com.intuit.paymentsapi.util;

/** CONSTANTS
 */
public abstract class Constants {

	public final static String REALM_ID = "realmid";
	public final static String SBX_REALM_ID = "sbxrealmid";
	public final static String USER_AUTH_ID = "consumer.auth.id";
	public final static String WALLET_ENTRY_ID = "wallet.entry.id";
	
	//default OBS fields
	public static final String DEFAULT_USER_EMAIL_ID = "user.email.id";
	public static final String DEFAULT_USER_EMAIL_DOMAIN = "user.email.domain";
	public static final String DEFAULT_USER_EMAIL_PWD = "user.email.pwd";
	public static final String DEFAULT_CHANNEL = "channel";
	public static final String DEFAULT_SOURCE = "src";
	public static final String DEFAULT_OFFER_ID = "offer.id";
	public static final String DEFAULT_BRAND_ID = "brand.id";
	public static final String DEFAULT_DECISION = "decision";
	
	// Credit card prefixes
	public final String[] VISA_PREFIX_LIST = new String[] { "4539", "4556",
			"4916", "4532", "4929", "40240071", "4485", "4716", "4" };
	public final String[] MASTERCARD_PREFIX_LIST = new String[] { "51", "52",
			"53", "54", "55" };
	public final String[] AMEX_PREFIX_LIST = new String[] { "34", "37" };
	public final String[] DISCOVER_PREFIX_LIST = new String[] { "6011" };
	public final String[] JCB_PREFIX_LIST = new String[] { "3528" };
	
	// Creditcard Resource URL keys
	public final static String CHARGE_CC = "charge.cc";
	public final static String CAPTURE_CC = "capture.cc";
	public final static String REFUND_CC = "refund.cc";
	public final static String RETRIEVAL_CC = "retrieval.cc";
	public final static String CREATE_TOKEN = "create.token";
	public final static String RETRIEVAL_LIST_CC = "listRetrieval.cc";
	public final static String RETRIEVAL_REFUND_CC = "refundRetrieval.cc";
	public final static String CHARGE_CC_INVALID = "invalidCharge.cc";
	
	//Cards URL
	public final static String CREATE_CARDS = "create.card";
	public final static String LIST_CARDS = "list.card";
	public final static String PUT_CARDS = "put.card";
	public final static String GET_CARDS = "get.card";
	public final static String DELETE_CARDS = "delete.card";
	
	//US Template paths
	public final static String CC_CHARGE_TEMPLATE_PATH = "creditcard/cc-charge.txt";
	public final static String CC_CAPTURE_TEMPLATE_PATH = "creditcard/cc-capture.txt";

}
