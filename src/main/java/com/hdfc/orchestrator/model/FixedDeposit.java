package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class FixedDeposit {
	
	private Long depositAmount;
	private Long tenureMonth;
	private Long tenureDays;
	private int maturityInstructionCode;
	private String interestMode;
	private String maturityInstructionDescription;
	private String maturityDate;
	private String interestRate;
	

}
