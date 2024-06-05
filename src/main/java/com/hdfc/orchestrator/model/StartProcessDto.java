package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class StartProcessDto {

	private Object links;
	private String id;
	private String definitionId;
	private String businessKey;
	private String contractId;
	private String tenantId;
	private Boolean ended;
	private Boolean suspended;
	private Object variables;
	private String caseInstanceId;
	
}
