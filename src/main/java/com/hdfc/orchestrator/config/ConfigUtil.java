package com.hdfc.orchestrator.config;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfc.orchestrator.model.CreateResponse;
import com.hdfc.orchestrator.model.GenerateOtpModel;
import com.hdfc.orchestrator.model.OtpDetails;
import com.hdfc.orchestrator.model.RegistrationRequest;
import com.hdfc.orchestrator.model.StatusENUM;
import com.hdfc.orchestrator.model.ValidateOtpResponse;

@Component
public class ConfigUtil implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2267571762913267617L;

	Map<String, GenerateOtpModel> customerData = new LinkedHashMap<>();
	
	Map<String, OtpDetails> otpObj = new HashMap<String, OtpDetails>();
	
	HttpHeaders headers = new HttpHeaders();
	Map<String, CreateResponse> createResponseMap= new HashMap<>();
	Map<String,RegistrationRequest > applicationMap= new HashMap<>();
	Map<String, ValidateOtpResponse> validateMap = new HashMap<>();
	
	public Map<String, ValidateOtpResponse> getValidateMap() {
		return validateMap;
	}

	public void setValidateMap(Map<String, ValidateOtpResponse> validateMap) {
		this.validateMap = validateMap;
	}

	Set<String> panDb = new HashSet<>();
	
	public Map<String, RegistrationRequest> getApplicationMap() {
		return applicationMap;
	}

	public void setApplicationMap(Map<String, RegistrationRequest> applicationMap) {
		this.applicationMap = applicationMap;
	}

	public void setHttpHeaders(String channel, String transactionId, String apikey, String scope) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Channel", "");
		headers.add("transactionId", "");
		headers.add("apikey", "");
		headers.add("Scope", "");
	}

	public HttpHeaders getHttpHeaders() {
		return headers;
	}
	
//	public static ThreadLocal<String> currentStep = new ThreadLocal<>();
//	public static void setCurrentStep(String step) {
//		currentStep.set(step);
//	}
// 
//	public static String getCurrentStep() {
//		return currentStep.get();
//	}
	
	private static String currentStep = StatusENUM.MOBILE_VERIFICATION_PENDING.name();
	private static String BPMNStep;
	public static void setBPMNStep(String step) {
		ConfigUtil.BPMNStep = step;
	}
 
	public static String getBPMNStep() {
		return ConfigUtil.BPMNStep;
	}
	
	public static void setCurrentStep(String step) {
		ConfigUtil.currentStep = step;
	}
 
	public static String getCurrentStep() {
		return ConfigUtil.currentStep;
	}
	
//	private static ThreadLocal<String> customerStatus = new ThreadLocal<>();
//	
//	public static void setCustomerStatus(String status) {
//		customerStatus.set(status);
//	}
// 
//	public static String getCustomerStatus() {
//		return customerStatus.get();
//	}
	
	private static String customerStatus = StatusENUM.MOBILE_VERIFICATION_PENDING.name();
	public static void setCustomerStatus(String status) {
		ConfigUtil.customerStatus = status;
	}
 
	public static String getCustomerStatus() {
		return ConfigUtil.customerStatus;
	}

	public Map<String, GenerateOtpModel> getCustomerData() {
		return customerData;
	}

	public void setCustomerData(Map<String, GenerateOtpModel> customerData) {
		this.customerData = customerData;
	}

	public Map<String, OtpDetails> getOtpObj() {
		return otpObj;
	}

	public void setOtpObj(Map<String, OtpDetails> otpObj) {
		this.otpObj = otpObj;
	}
	
	public static String generateAccessToken(String pan) {
		return new String(Base64.getEncoder().encode(pan.getBytes()));
	}
	public String generateRandomString() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public Set<String> getPanDb() {
		return panDb;
	}

	public void setPanDb(Set<String> panDb) {
		this.panDb = panDb;
	}
	public static String generateAccessToken(RegistrationRequest registrationRequest) throws JsonProcessingException {
		String payload = new ObjectMapper().writeValueAsString(registrationRequest);
		 byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
		
		return Base64.getEncoder().encodeToString(payloadBytes);
	}
	
	
	
}
