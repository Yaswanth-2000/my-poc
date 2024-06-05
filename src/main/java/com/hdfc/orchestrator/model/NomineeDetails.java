package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NomineeDetails {
	
	@JsonProperty("guardian_address1")
    private String guardianAddress1;

    @JsonProperty("guardian_address2")
    private String guardianAddress2;

    @JsonProperty("guardian_address3")
    private String guardianAddress3;

    @JsonProperty("guardian_city_id")
    private String guardianCityId;

    @JsonProperty("guardian_city_name")
    private String guardianCityName;

    @JsonProperty("guardian_dob")
    private String guardianDob;

    @JsonProperty("guardian_name")
    private String guardianName;

    @JsonProperty("guardian_pin_code")
    private String guardianPinCode;

    @JsonProperty("guardian_relationship_id")
    private String guardianRelationshipId;

    @JsonProperty("guardian_state_id")
    private String guardianStateId;

    @JsonProperty("guardian_state_name")
    private String guardianStateName;

    @JsonProperty("is_edit")
    private Boolean isEdit;

    @JsonProperty("is_minor")
    private Boolean isMinor;

    @JsonProperty("nominee_address1")
    private String nomineeAddress1;

    @JsonProperty("nominee_address2")
    private String nomineeAddress2;

    @JsonProperty("nominee_address3")
    private String nomineeAddress3;

    @JsonProperty("nominee_city_id")
    private String nomineeCityId;

    @JsonProperty("nominee_city_name")
    private String nomineeCityName;

    @JsonProperty("nominee_dob")
    private String nomineeDob;

    @JsonProperty("nominee_id")
    private String nomineeId;

    @JsonProperty("nominee_name")
    private String nomineeName;

    @JsonProperty("nominee_pin_code")
    private String nomineePinCode;

    @JsonProperty("nominee_relationship_id")
    private int nomineeRelationshipId;

    @JsonProperty("nominee_state_id")
    private String nomineeStateId;

    @JsonProperty("nominee_state_name")
    private String nomineeStateName;
}
