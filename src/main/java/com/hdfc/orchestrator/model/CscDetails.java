package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CscDetails {

	@JsonProperty("vle_name")
    private String vleName;

    @JsonProperty("lg_code")
    private String lgCode;}
