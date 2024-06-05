package com.hdfc.orchestrator.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RegisterResponse {
	
	private int noOfAttemptsRemaining;
	private String otpTransactionId;
//	private Long otp;
//	private String otpCreatedAt;
//	private String taskId;
//	private String journeyId;
}
