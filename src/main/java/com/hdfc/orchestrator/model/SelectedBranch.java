package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SelectedBranch {

	 @JsonProperty("branch_name")
	    private String branchName;

	    @JsonProperty("branch_address")
	    private String branchAddress;

	    @JsonProperty("branch_city")
	    private String branchCity;

	    @JsonProperty("branch_id")
	    private String branchId;

	    @JsonProperty("branch_pincode")
	    private String branchPincode;
}
