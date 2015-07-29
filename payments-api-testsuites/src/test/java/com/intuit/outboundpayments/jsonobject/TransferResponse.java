package com.intuit.outboundpayments.jsonobject;

public class TransferResponse {
	private String created;
	private String status;
	private double amount;
	private From from;
	private To to;
	private HoldDetail holdDetail;
	private ClaimDetail claimDetail;
	private ReclaimDetail reclaimDetail;
	private String description;
	private String transferId;
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public From getFrom() {
		return from;
	}
	public void setFrom(From from) {
		this.from = from;
	}
	public To getTo() {
		return to;
	}
	public void setTo(To to) {
		this.to = to;
	}
	public HoldDetail getHoldDetail() {
		return holdDetail;
	}
	public void setHoldDetail(HoldDetail holdDetail) {
		this.holdDetail = holdDetail;
	}
	public ClaimDetail getClaimDetail() {
		return claimDetail;
	}
	public void setClaimDetail(ClaimDetail claimDetail) {
		this.claimDetail = claimDetail;
	}
	public ReclaimDetail getReclaimDetail() {
		return reclaimDetail;
	}
	public void setReclaimDetail(ReclaimDetail reclaimDetail) {
		this.reclaimDetail = reclaimDetail;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTransferId() {
		return transferId;
	}
	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}
	
	
	
}
