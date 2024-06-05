package com.hdfc.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AmlDetails {
	

	 @JsonProperty("employment_type")
	    private int employmentType;

	    @JsonProperty("source_of_funds")
	    private int sourceOfFunds;

	    @JsonProperty("annual_income")
	    private int annualIncome;

	    @JsonProperty("residence_type")
	    private int residenceType;

	    @JsonProperty("organization_type")
	    private int organizationType;

	    @JsonProperty("self_employee_category")
	    private Long selfEmployeeCategory;

	    @JsonProperty("self_employed_since")
	    private String selfEmployedSince;

	    @JsonProperty("incorporation_date")
	    private String incorporationDate;

	    @JsonProperty("nature_of_business")
	    private Long natureOfBusiness;

	    @JsonProperty("type_of_firm")
	    private Long typeOfFirm;

	    @JsonProperty("type_of_firm_description")
	    private String typeOfFirmDescription;

	    @JsonProperty("source_income_description")
	    private String sourceIncomeDescription;

	    @JsonProperty("self_employee_category_description")
	    private String selfEmployeeCategoryDescription;

	    @JsonProperty("residence_type_description")
	    private String residenceTypeDescription;

	    @JsonProperty("organization_type_description")
	    private String organizationTypeDescription;

	    @JsonProperty("nature_of_business_description")
	    private String natureOfBusinessDescription;

	    @JsonProperty("employment_type_description")
	    private String employmentTypeDescription;

	    @JsonProperty("annual_income_description")
	    private String annualIncomeDescription;


}
