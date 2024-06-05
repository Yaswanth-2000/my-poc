//package com.hdfc.orchestrator.service;
//
//import static java.lang.String.format;
//import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.hdfc.orchestrator.model.RegistrationRequest;
//import com.hdfc.orchestrator.model.StartProcessDto;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Service
//@Slf4j
//public class CamundaRestService {
//
//	@Value("${camunda.endpoint}")
//	private String CAMUNDA_URL;
//
//	public StartProcessDto startProcess(RegistrationRequest spm, String workflowName) throws Exception {
//		ResponseEntity<String> response = doStartProcess(spm, workflowName);
//		JSONObject json = new JSONObject(response.getBody());
//		StartProcessDto readValue = new ObjectMapper().readValue(response.getBody(), StartProcessDto.class);
//		if (response.getStatusCode() == HttpStatus.OK) {
//			log.info(json.toString());
//			return readValue;
//		} else {
//			return null;
//		}
//	}
//
//	public ResponseEntity<String> doStartProcess(RegistrationRequest spm, String workflowName) throws Exception {
//		try {
//			String START_PROCESS_URI = CAMUNDA_URL + "/process-definition/key/{key}/start";
//			log.info("START_PROCESS_URI :: {}", START_PROCESS_URI);
//
//			// check if the process already exists to avoid saving duplicate ones
////			checkIfProcessAlreadyExists(workflowName);
//
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//
//			// set request body as json
//			JSONObject requestBody = new JSONObject(spm);
////			requestBody.put("businessKey", spm.generateBusinessKey());
//			HttpEntity<String> request = new HttpEntity<String>(requestBody.toString(), headers);
//
//			// set path parameters
//			Map<String, Object> pathParams = new HashMap<>();
////			pathParams.put("tenant-id", spm.getTenantId());
//			pathParams.put("key", workflowName);
//			log.info("pathParams :: " + pathParams);
//
//			RestTemplate restTemplate = new RestTemplate();
//			ResponseEntity<String> response = restTemplate.postForEntity(START_PROCESS_URI, request, String.class,
//					pathParams);
//			log.info("ResponseStatusCodeValue :: {}", response.getStatusCode().value());
//			return response;
//		} catch (Exception e) {
//			e.printStackTrace();
//			log.error(e.getMessage());
//			throw e;
//		}
//	}
//
//	private void checkIfProcessAlreadyExists(String workflowName) throws Exception {
//		ResponseEntity<String> response = processInstances(workflowName);
//		if (response.getStatusCode() == HttpStatus.OK) {
//			JSONArray jsonArr = new JSONArray(response.getBody());
//			if (jsonArr.length() != 0) {
//				String definitionId = jsonArr.getJSONObject(0).getString("definitionId");
//				String errMsg = format("Process already exists with definitionId - %s", definitionId);
//				log.error("No resource found"+errMsg);
//			}
//		} else {
//			throw new Exception(response.getBody());
//		}
//	}
//
//	public ResponseEntity<String> processInstances(String workflowName) throws JSONException {
//		String GET_PROCESS_INSTANCE_DETAILS_URI = CAMUNDA_URL + "/process-instance";
//		log.info("GET_PROCESS_INSTANCE_DETAILS_URI :: {}", GET_PROCESS_INSTANCE_DETAILS_URI);
//		// set query parameters
//		UriComponentsBuilder builder = fromHttpUrl(GET_PROCESS_INSTANCE_DETAILS_URI)
////				.queryParam("tenantIdIn", tenantId)
//				.queryParam("processDefinitionKey", workflowName);
////				.queryParam("businessKey", businessKey);
//
//		RestTemplate restTemplate = new RestTemplate();
//		ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
//		log.info("ResponseStatusCodeValue :: {}", response.getStatusCode().value());
//		return response;
//	}
//
//}
