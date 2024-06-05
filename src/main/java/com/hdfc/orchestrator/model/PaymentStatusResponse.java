package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class PaymentStatusResponse {

	private String status;
	private String orderId;
	private String orderBankRefNo;
	private String journeyId;
	private String taskId;
	private Double amount;
}
