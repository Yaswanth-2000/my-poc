package com.hdfc.orchestrator.model;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PermenantAddress {

	@JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("address3")
    private String address3;

    @JsonProperty("city")
    private String city;

    @JsonProperty("city_id")
    private String cityId;

    @JsonProperty("pin_code")
    private String pinCode;

    @JsonProperty("state_name")
    private String stateName;

    @JsonProperty("country")
    private String country;

    @JsonProperty("nearest_landmark")
    private String nearestLandmark;

    @JsonProperty("state")
    private String State;

    @JsonProperty("state_id")
    private String stateId;
	
}