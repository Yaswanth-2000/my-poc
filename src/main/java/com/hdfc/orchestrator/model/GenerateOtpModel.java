package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class GenerateOtpModel {

	private RegistrationRequest registrationRequest;
	private OtpRegistration otpRegistration;
//	private ValidateOtpResponse validateOtpResponse;
}
