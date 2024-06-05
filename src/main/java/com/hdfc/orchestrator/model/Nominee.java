package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class Nominee {
	
	private String nomineeName;
	private String nomineeAddress1;
	private String nomineeAddress2;
	private String nomineeAddress3;
	private String nomineeCityId;
	private String nomineeStateId;
	private String nomineeCountry;
	private String nomineePinCode;
	private String nomineeDob;
	private String nomineeRelationShipId;
	private String nomineeGuardianName;
	private String nomineeGuardianAddress1;
	private String nomineeGuardianAddress2;
	private String nomineeGuardianAddress3;
	private String nomineeGuardianCityId;
	private String nomineeGuardianStateId;
	private String nomineeGuardianPinCode;
	private String nomineeGuardianCountry;
	private String nomineeGuardianRelationShipId;
	private Boolean isEdit;
	private Boolean isMinor;
	

}
