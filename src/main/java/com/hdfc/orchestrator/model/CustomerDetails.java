package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CustomerDetails {

	 @JsonProperty("communication_address")
	    private CommunicationAddress communicationAddress;
	 @JsonProperty("permanent_address")
	    private PermenantAddress permanentAddress;
	 @JsonProperty("personal_details")
	    private PersonalDetails personalDetails;
	 @JsonProperty("tax_address")
	    private CommunicationAddress taxAddress;
}
