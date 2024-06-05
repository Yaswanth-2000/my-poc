package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class ValidateOTPResponses {

	private String accessToken;
	private String customerId;
	private String language;
	private String name;
	private String applicationId;
//	private String journeyId;
//	private String taskId;
}
