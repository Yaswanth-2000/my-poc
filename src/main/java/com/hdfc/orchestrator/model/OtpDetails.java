package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class OtpDetails {

	private long otp;
	private int retryCount;
	private boolean otpValidate;
	private String otpTransactionId;
}
