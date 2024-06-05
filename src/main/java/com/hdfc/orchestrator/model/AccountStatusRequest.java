package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class AccountStatusRequest {

	private String customerId;
	private String journey;
	private String journeyId;
//	private String taskId;
}
