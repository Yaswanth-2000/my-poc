package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class RegistrationRequest {
	
	private String pan;
	private String name;
	private String dob;
	@JsonAlias({"mobileNumber","number"})
	private String mobileNumber;
	private Boolean loginSuspended;
	private Boolean suspensionExpired;
	private String type;
	private String journeyId;
	private String taskId;
	private String currentStep;
	private String applicationId;
	private String errorMessage;
}
