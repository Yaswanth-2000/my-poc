package com.hdfc.orchestrator.model;



import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccountStatusResponse {

	private AadharDetails aadharDetails;
	@JsonProperty("csc_details")
	private CscDetails cscDetails;
	@JsonProperty("account_number")
	private String accountNumber;
	@JsonProperty("amb_values")
	private String ambValues;
	 @JsonProperty("aml_details")
	    private AmlDetails amlDetails;
	@JsonProperty("application_id")
	private String applicationId;
	@JsonProperty("consent_details")
    private ArrayList<ConsentDetails> consentDetails;
	private String crmLeadId;
	private String currentStatus;
	@JsonProperty("current_step")
	private int currentStep;
	@JsonProperty("customer_details")
    private CustomerDetails customerDetails;

	@JsonProperty("customer_id")
	private String customerId;

	private String fdReferenceNumber;
	private FixedDeposit fixedDeposit;
	private String journey;
	private String neftDetails;
	
	@JsonProperty("nominee_details")
    private NomineeDetails nomineeDetails;
	 @JsonProperty("od_details")
	    private String odDetails;
	private String pgTxnId;
	private String savingsAmount;
	@JsonProperty("selected_branch")
    private SelectedBranch selectedBranch;
	 @JsonProperty("selected_product")
	    private SelectedProduct selectedProduct;
	 @JsonProperty("skipped_modules")
	    private String skippedModules;
	private Long totalamount;
	private String channel;
	
	@JsonProperty("selected_product_id")
    private String selectedProductId;
	
}
