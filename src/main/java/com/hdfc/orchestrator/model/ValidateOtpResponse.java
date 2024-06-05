package com.hdfc.orchestrator.model;


import lombok.Data;

@Data
public class ValidateOtpResponse {

	private String accessToken;
	private String customerId;
	private String language;
	private String name;
	private String journeyId;
	private String taskId;
	private int retries=3;
	private boolean isValid;
	private String errorStatus;
	private String applicationId;
}
