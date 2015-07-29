package com.intuit.outboundpayments.jsonobject;

public class BankAccountCreateRequest {
	private BankAccount BankAccount;

	public BankAccount getBankAccount() {
		return BankAccount;
	}

	public void setBankAccount(BankAccount bankAccount) {
		BankAccount = bankAccount;
	}
	
	public class BankAccount {
		private String name;
		private String routingNumber;
		private String accountNumber;
		private String accountType;
		private String phoneNumber;

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
		public String getAccountNumber() {
			return accountNumber;
		}
		public void setAccountNumber(String accountNumber) {
			this.accountNumber = accountNumber;
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
	
}
