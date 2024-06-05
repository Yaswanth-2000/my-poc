package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CommunicationAddress {

	@JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("address3")
    private String address3;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("pin_code")
    private String pinCode;

    @JsonProperty("state_name")
    private String stateName;
}
