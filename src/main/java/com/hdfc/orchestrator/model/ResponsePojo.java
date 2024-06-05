package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class ResponsePojo {
	
	private Object data;
	private String message;
	private String responseCode;
	private String bpmnStatus;
	private String journeyId;
	
}
