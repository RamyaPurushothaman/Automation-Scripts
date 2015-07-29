package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import org.jdom2.JDOMException;
import org.testng.annotations.DataProvider;

import com.intuit.tame.common.utility.XPathAsserts;
import com.intuit.tame.common.utility.XPathUtils;
import com.intuit.tame.common.wsclient.Response;
import com.intuit.tame.ws.XmlTestClient;

public class BaseTest {
	
	public XmlTestClient xmlClient;
	public TestUtil testUtil;
	public String connTicket;
	public static int build = 0;

	public BaseTest() {
		Configuration.instance();
		connTicket = getTicket(Configuration.test_env);
		
		xmlClient = new XmlTestClient(true);
		xmlClient.setConnectionTimeout(120000);
		testUtil = new TestUtil();		
	}
	
	// Credit card prefixes
	public final String[] VISA_PREFIX_LIST = new String[] { "4539", "4556",
			"4916", "4532", "4929", "40240071", "4485", "4716", "4" };
	public final String[] MASTERCARD_PREFIX_LIST = new String[] { "51", "52",
			"53", "54", "55" };
	public final String[] AMEX_PREFIX_LIST = new String[] { "34", "37" };
	public final String[] DISCOVER_PREFIX_LIST = new String[] { "6011" };
	public final String[] JCB_PREFIX_LIST = new String[] { "3528" };
	
	String mmTicketQA = "SDK-TGT-159-KoE63t5u0KeSl7YTbupnbQ";
	//String mmTicketQA = "SDK-TGT-115-q5SsdwmxqU4X$RRuYQ$QXg";
	String mmTicketPTC = "SDK-TGT-13-BlGacwbegEUbAYvxraIX1A";
	String mmTicketPTCE2E = "SDK-TGT-13-BlGacwbegEUbAYvxraIX1A";
	
	String qaEndpoint = "https://merchantaccount.qa.quickbooks.com/j/AppGateway";
	String qa2Endpoint = "https://merchantaccount2.qa.quickbooks.com/j/AppGateway";
	String ciEndpoint = "https://merchantaccount.ipc.intuit.com/j/AppGateway";
	String ptcEndpoint = "https://merchantaccount.ptc.quickbooks.com/j/AppGateway";
    String transactionqaEndpoint = "https://transaction-qa.payments.intuit.com/j/AppGateway";
    String transactionptcEndpoint = "https://transaction-ptc.payments.intuit.com/j/AppGateway";

	public String mmCCCardNum = "4112344112344113";
	
	private String getTicket(String s){
		String ticket;
		if (s.equals(qaEndpoint)||s.equals(qa2Endpoint)||s.equals(ciEndpoint)||s.equals(transactionqaEndpoint)) 
		{
			ticket = mmTicketQA;
		}else if(s.equals(ptcEndpoint)||s.equals(transactionptcEndpoint)) {
			ticket = mmTicketPTC;
		}else {
			ticket = mmTicketPTCE2E;
			build = 1;
		}
		return ticket;
	}
	
	public boolean isQA() {
		return true;	
	}
	
	public String[][] getHttpHeader() {
		return new String[][] { { "Content-Type", "application/x-qbmsxml" }, {"emulation","emulate=no"} };		
	}
	
	// wallet methods
	
	public String createCCWallet() throws Exception {

		InputStream is = getClass().getResourceAsStream("/xml/create-cc-wallet.xml");

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is);
		String res = resp.getResponseContentAsString();	
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");	
		
		return XPathUtils.applyXpath(res, "//WalletEntryID");
	}
	
	public String createCCWallet(String[][] testData) throws Exception {

		InputStream is = getClass().getResourceAsStream("/xml/create-cc-wallet.xml");

		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData);
		String res = resp.getResponseContentAsString();		
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");		
		
		return XPathUtils.applyXpath(res, "//WalletEntryID");
	}
	
	public void deleteCCWallet() throws Exception {
		
		String walletEntryId = createCCWallet();	
		String[][] testData = new String[][] {	{ "//WalletEntryID", walletEntryId }, };

		InputStream is = getClass().getResourceAsStream("/xml/delete-cc-wallet.xml");					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData);	
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardWalletDelRs/@statusMessage", "Status OK");		
	}
	
	public void deleteCCWallet(String walletEntryId) throws Exception {
		
		String[][] testData = new String[][] {	{ "//WalletEntryID", walletEntryId }, };
		
		InputStream is = getClass().getResourceAsStream("/xml/delete-cc-wallet.xml");				
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardWalletDelRs/@statusMessage", "Status OK");		
	}
	
	// billing methods
	
	public String scheduleBilling(String[][] testData) throws Exception {
		
		InputStream is = getClass().getResourceAsStream("/xml/schedule-billing.xml");
					
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData);
		String res = resp.getResponseContentAsString();
		
		XPathAsserts.assertXPath(res, "//@statusMessage", "Status OK");
		return XPathUtils.applyXpath(res, "//ScheduledBillingID");
	}
	
	public void deleteScheduledBilling(String sbid, String cid) throws Exception {
		String[][] schBillIdIdVal = new String[][] {{ "//ScheduledBillingID", sbid}, { "//CustomerID", cid}, };
		
		InputStream schBillDel = getClass().getResourceAsStream("/xml/delete-schedule-billing.xml");	
		Response res_delBill = xmlClient.doPost(Configuration.test_env, getHttpHeader(), schBillDel, schBillIdIdVal);	
		
		XPathAsserts.assertXPath(res_delBill.getResponseContentAsString(), "//CustomerScheduledBillingDelRs/@statusCode", "0");	
	}
	
	public String getScheduledBillingID(String cid, String wid) throws Exception {
		
		String[][] testData = new String[][] {	{ "//CustomerID", cid }, { "//WalletEntryID", wid }, };
		InputStream query = getClass().getResourceAsStream("/xml/query-customer-scheduled-billing.xml");

		Response query_rs = xmlClient.doPost(Configuration.test_env, getHttpHeader(), query, testData);
		String rs = query_rs.getResponseContentAsString();
		XPathAsserts.assertXPath(rs, "//CustomerScheduledBillingQueryRs/@statusMessage", "Status OK");
//		XPathAsserts.assertXPath(rs, "//CustomerID", cid);
		
		return XPathUtils.applyXpath(rs, "//ScheduledBillingID");
	}
	
	public String queryMerchantAccount(String appLogin, String connectionTicket) {
		
		String[][] signon = new String[][] { { "//ApplicationLogin", appLogin },	
											 { "//ConnectionTicket", connectionTicket },	};
	
		InputStream account_query = getClass().getResourceAsStream("/xml/qbms.xml");
		
		Response rs_query = xmlClient.doPost(Configuration.test_env, getHttpHeader(), account_query, signon);
		String res = rs_query.getResponseContentAsString();
		
		try {
			XPathAsserts.assertXPath(res, "//MerchantAccountQueryRs/@statusCode", "0");
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return res;		
	}
	
	public String getCCTransID() throws Exception {
		
		InputStream is = getClass().getResourceAsStream("/xml/charge-cc.xml");
		String[][] testData = new String[][] {	{ "//TransRequestID", UUID.randomUUID().toString()} };
		
		Response resp = xmlClient.doPost(Configuration.test_env, getHttpHeader(), is, testData);
		String res = resp.getResponseContentAsString();		
		XPathAsserts.assertXPath(res, "//CustomerCreditCardChargeRs/@statusMessage", "Status OK");
		
		return XPathUtils.applyXpath(res, "//CreditCardTransID");		
	}
	
	public String updateTicket(String xml, String appLogin, String ticket) throws JDOMException, IOException {
		
		String new_ticket = "<ApplicationLogin>" + appLogin + "</ApplicationLogin>\n"
			              + "<ConnectionTicket>" + ticket + "</ConnectionTicket>";
		
		return testUtil.replaceXmlTree(xml, "", new_ticket);
	}
	
	public String addLodging() {
		String s = "<Lodging>\n"
		         + "<FolioID>1245667347347</FolioID>\n"
		         + "<ChargeType>Golf</ChargeType>\n"
		         + "<CheckInDate>2012-12-13</CheckInDate>\n"
		         + "<CheckOutDate>2012-12-18</CheckOutDate>\n"
		         + "<RoomRate>400.00</RoomRate>\n"
		         + "<ExtraCharge>GiftShop</ExtraCharge>\n"
		         + "<ExtraCharge>Laundry</ExtraCharge>\n"
		         + "<SpecialProgram>AssuredReservation</SpecialProgram>\n"
		         + "</Lodging>";
		return s;		
	}
	
	public String buildLodgingXML(String basexml, String target) throws JDOMException, IOException {
		return testUtil.insertXmlTree(basexml, target, addLodging());				
	}
	
	public String addRestaurant(){
		String s = "<Restaurant>\n" 
				 + "<ServerID>12</ServerID>\n"
				 + "<FoodAmount>30.00</FoodAmount>\n"
				 + "<BeverageAmount>8.00</BeverageAmount>\n"
				 + "<TaxAmount>3.20</TaxAmount>\n" 
				 + "<TipAmount>5.00</TipAmount>\n"
				 + "</Restaurant>\n";
		return s;
	}
	
	public String buildRestaurantXML(String basexml, String target) throws JDOMException, IOException {
		return testUtil.insertXmlTree(basexml, target, addRestaurant());				
	}


	// MM 
	
	@DataProvider(name="cardSet")
	public String[][] dataProviderTest(){
		return new String[][]{
								{"DC", "36110361103612"},
								{"Visa", "4112344112344113"},
								{"MC","5500005555555559"},
					//			{"Amex", "371449635398431"},
								{"JCB", "3566003566003566"},
								
							};
	}
	
	public String[][] getMMTicket(String s){

		String[][]  mmTicket= {	{"//ApplicationLogin", "mm.intuit.com"},
							  {"//ConnectionTicket", connTicket},
							  { "//TransRequestID", s},
			  };
		return mmTicket;	
	}
	
	public String[][] getMMData(String s, String cc) {
		String[][]  mmData= {	{"//ApplicationLogin", "mm.intuit.com"},
								{"//ConnectionTicket", connTicket},
								{ "//TransRequestID", s},
								{"//CreditCardNumber", cc}
							};
		return mmData;
	}
	
	
	public String[][] getUID(String s) {
		 String[][] uid = { {"//TransRequestID", s}};
		 return uid;
	}
	
	public String getccToken(String s) {
		 return "<CreditCardToken>" + s + "</CreditCardToken>";
	}
	
	protected String generateTrack1Data(String s){
		return "%B"+s+"^Grace/Xu^2209961767676hghghghghg123456?";
	}
	
	protected String generateTrack2Data(String s){
		return ";"+s+"=250610100039300000?";
	}
	
	protected String buildXmlBranch(String node, String value){		
		return "<" + node + ">" + value + "</" + node + " >";
	}
	
	protected String generateCreditCardNumber(String[] prefix) {
		return credit_card_number(prefix, 16, 1)[0];
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
		
}
