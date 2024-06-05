package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class AccountResponse {

	private String accountNo;
	private String bankCustomerId;
	private String crmLeadId;
	private String ifscCode;
	
}
