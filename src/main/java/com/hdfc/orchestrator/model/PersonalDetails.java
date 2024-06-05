package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PersonalDetails {

	@JsonProperty("mothers_name")
    private String mothersName;

    @JsonProperty("fathers_name")
    private String fathersName;

    @JsonProperty("marital_status")
    private Integer maritalStatus;

    @JsonProperty("spouse_name")
    private String spouseName;

    @JsonProperty("city_of_birth_id")
    private String cityOfBirthId;

    @JsonProperty("email_id")
    private String emailId;

    @JsonProperty("nearest_landmark")
    private String nearestLandmark;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("city_of_birth")
    private String cityOfBirth;
	
}
