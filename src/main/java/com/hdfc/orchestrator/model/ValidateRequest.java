package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ValidateRequest {

	private Long otp;
	@JsonProperty("otp_transaction_id")
	private String otpTransactionId;
}
