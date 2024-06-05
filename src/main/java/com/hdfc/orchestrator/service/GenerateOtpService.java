//package com.hdfc.orchestrator.service;
//
//import org.camunda.bpm.engine.delegate.DelegateExecution;
//import org.camunda.bpm.engine.delegate.JavaDelegate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import com.hdfc.orchestrator.config.ConfigUtil;
//import com.hdfc.orchestrator.model.RegistrationRequest;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@Slf4j
//public class GenerateOtpService implements JavaDelegate{
//
//	@Value("${orchestrator.url}")
//	private String orchestratorUrl;
//	
//	@Autowired
//	private ConfigUtil configUtil;
//	
//	@Override
//	public void execute(DelegateExecution execution) throws Exception {
//		log.info("Calling the Delegate method from GenerateOtpService");
//		RestTemplate restTemplate = new RestTemplate();
//		String url = orchestratorUrl + "/api/v1/gigasa/generate-otp";
//		HttpHeaders headers = configUtil.getHttpHeaders();
//		log.info("Business Key is - "+ execution.getBusinessKey());
//		Object variable = execution.getVariable("");
//		
//		HttpEntity<Object> httpEntity = new HttpEntity<>(variable, headers);
//		ResponseEntity<RegistrationRequest> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, RegistrationRequest.class);
//		
//		log.info("Response - "+ response.getBody());
//		
//	}
//
//}
