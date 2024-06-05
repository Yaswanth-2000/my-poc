package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class PostCustomerRequest {

	private String applicationId;
	private String customerId;
	private String journey;
	private CscDetails CscDetails;
	private String currentStep;
	private NomineeDetails nomineeDetails;
	private SelectedBranch selectedBranch;
	private SelectedProduct selectedProduct;
	private OdDetails odDetails;
	private String skippedModules;
	private String[] consentDetails;
	private CustomerDetails CustomerDetails;
	private AmlDetails amlDetails;
	private String journeyId;
	private String taskId;
}

