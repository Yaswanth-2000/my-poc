package com.hdfc.orchestrator.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OtpRegistration {

	private Long otp;
	@JsonProperty("otp_transaction_id")
	private String otpTransactionId;
	private int noOfAttemptsRemaining;
	private String journeyId;
//	private String taskId;
	private String otpCreatedAt;
	
}
