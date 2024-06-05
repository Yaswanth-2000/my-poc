package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class BpmnResponse {

	private String customerId;
	private String applicationId;
	private String bpmnStatus;
	private String journeyId;
}
