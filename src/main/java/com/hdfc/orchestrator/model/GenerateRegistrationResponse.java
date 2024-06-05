package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class GenerateRegistrationResponse {
	
	private int numberOfAttemptsRemaining;
	private String otpTransactionId;
	private String journeyId;
	private String taskId;
}
