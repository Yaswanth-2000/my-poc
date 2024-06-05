package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class StartProcessModel {
	
	private String projectId;
	
	private String templateId;
	
	private String requestBody;

}
