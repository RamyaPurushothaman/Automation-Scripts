package com.intuit.outboundpayments.jsonobject;
public class BankAccountUpdate {
		private String name;
		private String routingNumber;
		private String accountType;
		private String phoneNumber;
		private String bankAccountId;

		public String getBankAccountId() {
			return bankAccountId;
		}
		public void setBankAccountId(String bankAccountId) {
			this.bankAccountId = bankAccountId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getRoutingNumber() {
			return routingNumber;
		}
		public void setRoutingNumber(String routingNumber) {
			this.routingNumber = routingNumber;
		}
		public String getAccountType() {
			return accountType;
		}
		public void setAccountType(String accountType) {
			this.accountType = accountType;
		}
		public String getPhoneNumber() {
			return phoneNumber;
		}
		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}
	}