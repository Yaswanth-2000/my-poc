package com.hdfc.orchestrator.model;

import lombok.Data;

@Data
public class AadharDetails {

	private Byte aadharPhoto;
//	private Long aadharReferenceNumber;
	private String dateOfBirth;
	private String gender;
	private Boolean isAadharVerified;
	private Boolean isDedupeVerified;
	private String name;

}
