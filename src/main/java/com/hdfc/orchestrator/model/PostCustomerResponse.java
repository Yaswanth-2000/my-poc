package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class PostCustomerResponse {
	
	private String applicationId;
	private String currentStatus;
	private int currentStep;
	private String customerId;
	private String status;
	private Boolean isAadharVerified;
	private Boolean isDedupeVerified;
//	private String journeyId;
//	private String taskId;

}
