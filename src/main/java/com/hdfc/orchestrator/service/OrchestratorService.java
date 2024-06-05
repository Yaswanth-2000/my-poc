package com.hdfc.orchestrator.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hdfc.orchestrator.config.ConfigUtil;
import com.hdfc.orchestrator.model.AadharDetails;
import com.hdfc.orchestrator.model.AccountResponse;
import com.hdfc.orchestrator.model.AccountStatusRequest;
import com.hdfc.orchestrator.model.AccountStatusResponse;
import com.hdfc.orchestrator.model.BpmnResponse;
import com.hdfc.orchestrator.model.CreateRequest;
import com.hdfc.orchestrator.model.CreateResponse;
import com.hdfc.orchestrator.model.GenerateOtpModel;
import com.hdfc.orchestrator.model.MasterDataRequest;
import com.hdfc.orchestrator.model.OtpRegistration;
import com.hdfc.orchestrator.model.PostCustomerResponse;
import com.hdfc.orchestrator.model.RegisterResponse;
import com.hdfc.orchestrator.model.RegistrationRequest;
import com.hdfc.orchestrator.model.ResponsePojo;
import com.hdfc.orchestrator.model.StatusENUM;
import com.hdfc.orchestrator.model.ValidateOTPResponses;
import com.hdfc.orchestrator.model.ValidateOtpResponse;
import com.hdfc.orchestrator.model.ValidateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrchestratorService {

	@Autowired
	Environment env;

	@Value("${simulator.url}")
	private String SIMULATOR_HOST;

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private ConfigUtil configUtil;

	ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	public ResponseEntity<ResponsePojo> validateCustomerDetails(RegistrationRequest cd, String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		try {

			if (cd.getPan() == null || cd.getMobileNumber() == null || cd.getDob() == null) {

				if (cd.getPan() == null) {
					log.info("PAN is mandatory. Please enter and try again");
				} else if (cd.getMobileNumber() == null) {
					log.info("Mobile Number is mandatory. Please enter and try again");
				} else if (cd.getDob() == null) {
					log.info("DOB is mandatory. Please enter and try again");
				}

				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("PAN number, dob, channel is mandatory. Please enter and try again");
				rp.setResponseCode("0001");
				return ResponseEntity.accepted().body(rp);
			} else if (Channel_partner_key == null || Channel_partner_key.isEmpty()) {
				log.info("Channel_partner_key is missing");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Channel_partner_key is missing");
				rp.setResponseCode("0010");
//						return ResponseEntity.internalServerError().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			}

			if (configUtil.getPanDb().contains(cd.getPan())) {
				log.info("Login is Declined as the Account with PAN " + cd.getPan() + " or MobileNumber "
						+ cd.getMobileNumber() + " already exists");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Login is Declined as the Account with PAN " + cd.getPan() + " or MobileNumber "
						+ cd.getMobileNumber() + " already exists");
				rp.setResponseCode("0005");
				return ResponseEntity.accepted().body(rp);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String writeValueAsString = objectMapper.writeValueAsString(cd);
			HttpEntity<String> httpEntity = new HttpEntity<>(writeValueAsString, headers);
			JSONObject json = new JSONObject(cd);
			log.info("Request to /v2/customer_otpservice/check-existing-customer sent :\n{} ", json.toString(4));
			String SIMULATOR_URL = SIMULATOR_HOST + "/v2/customer_otpservice/check-existing-customer";
			log.info(
					"Invoking /v2/customer_otpservice/check-existing-customer for checking if the user is existing or not");
			log.info("BPM next step to perform: Generating Otp");
			ResponseEntity<Boolean> res1 = restTemplate.exchange(SIMULATOR_URL, HttpMethod.POST, httpEntity,
					Boolean.class);
			Boolean body = res1.getBody();
			log.info("Response from /v2/customer_otpservice/check-existing-customer received:{}",
					body + " states that the customer " + (body ? "is a new customer" : "is an existing customer"));
			if (!body) {
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Login is Declined as the Account with PAN " + cd.getPan() + " or MobileNumber "
						+ cd.getMobileNumber() + " already exists");
				rp.setResponseCode("0005");
				return ResponseEntity.accepted().body(rp);
			} else {

				GenerateOtpModel generateOtpModel = new GenerateOtpModel();
				cd.setCurrentStep(StatusENUM.MOBILE_VERIFICATION_PENDING.name());
				ConfigUtil.setBPMNStep(StatusENUM.MOBILE_VERIFICATION_PENDING.name());
				generateOtpModel.setRegistrationRequest(cd);
				generateOtpModel.setOtpRegistration(new OtpRegistration());
				GenerateOtpModel generateOTP = generateOTP(generateOtpModel);
				RegistrationRequest registrationRequest = generateOTP.getRegistrationRequest();
				rp.setBpmnStatus(registrationRequest.getCurrentStep());
//				rp.setJourneyId(registrationRequest.getJourneyId());

				if (registrationRequest.getErrorMessage().equals("NO_ERROR")) {
					log.info("Success:OTP is sent to the registered mobile number");
					rp.setMessage("OTP is sent to the registered mobile number");
					rp.setResponseCode("200");
					RegisterResponse registerResponse = new RegisterResponse();
					registerResponse
							.setNoOfAttemptsRemaining(generateOTP.getOtpRegistration().getNoOfAttemptsRemaining());
					registerResponse.setOtpTransactionId(generateOTP.getOtpRegistration().getOtpTransactionId());
//					registerResponse.setOtpCreatedAt(generateOTP.getOtpRegistration().getOtpCreatedAt());
//					registerResponse.setJourneyId(generateOTP.getRegistrationRequest().getJourneyId());
//					registerResponse.setTaskId(generateOTP.getOtpRegistration().getTaskId());
					rp.setJourneyId(generateOTP.getRegistrationRequest().getJourneyId());
					rp.setData(registerResponse);
					return ResponseEntity.ok().body(rp);
				} else {
					log.info("Number of retries exceeded for this mobile number");
					rp.setMessage("Number of retries exceeded for this mobile number");
					RegisterResponse registerResponse = new RegisterResponse();
//					registerResponse.setJourneyId(generateOTP.getRegistrationRequest().getJourneyId());
					registerResponse
							.setNoOfAttemptsRemaining(generateOTP.getOtpRegistration().getNoOfAttemptsRemaining());
					registerResponse.setOtpTransactionId(generateOTP.getOtpRegistration().getOtpTransactionId());
//					registerResponse.setOtpCreatedAt(generateOTP.getOtpRegistration().getOtpCreatedAt());
					rp.setJourneyId(generateOTP.getRegistrationRequest().getJourneyId());
					rp.setBpmnStatus(StatusENUM.LOGIN_SUSPENDED.name());
					ConfigUtil.setBPMNStep(StatusENUM.LOGIN_SUSPENDED.name());
					rp.setData(registerResponse);
					rp.setResponseCode("0005");
					return ResponseEntity.accepted().body(rp);
				}

			}
		} catch (Exception e) {
			log.error("Exception raised during callCustomerIntroductionProcess:" + e.getMessage());
			rp.setMessage("The server encountered an unexpected error.");
			rp.setResponseCode("0003");
			rp.setBpmnStatus(StatusENUM.LOGIN_SUSPENDED.name());
			ConfigUtil.setBPMNStep(StatusENUM.LOGIN_SUSPENDED.name());
			rp.setData(null);
			return ResponseEntity.internalServerError().body(rp);
		}
	}

	public GenerateOtpModel generateOTP(GenerateOtpModel cd) {
		log.info("Invoking the /v2/customer_otpservice/generate-registration-otp for OTP generation");
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String writeValueAsString = objectMapper.writeValueAsString(cd);
			JSONObject json = new JSONObject(cd);
			log.info("Request to /v2/customer_otpservice/generate-registration-otp sent:\n" + json.toString(4));
			HttpEntity<String> httpEntity = new HttpEntity<>(writeValueAsString, headers);
			String SIMULATOR_URL = SIMULATOR_HOST + "/v2/customer_otpservice/generate-registration-otp";
			ResponseEntity<GenerateOtpModel> res1 = restTemplate.exchange(SIMULATOR_URL, HttpMethod.POST, httpEntity,
					GenerateOtpModel.class);
			GenerateOtpModel cd1 = res1.getBody();
			JSONObject json1 = new JSONObject(cd1);
			log.info("Response from /v2/customer_otpservice/generate-registration-otp received:\n{}",
					json1.toString(4));
			configUtil.getCustomerData().put(cd1.getRegistrationRequest().getJourneyId(), cd1);
			return cd1;
		} catch (Exception e) {
			cd.getRegistrationRequest().setErrorMessage("Unable to reach otp generation service");
			log.error("0003-Exception raised during generateOTP:" + e.getMessage());
			return cd;
		}
	}

	public ResponseEntity<ResponsePojo> validateOtp(ValidateRequest validateRequest, String journeyId,
			String Channel_partner_key) {
		log.info("Invoking the /v2/customer_otpservice/verify-registration-otp for OTP validation");
		ResponsePojo rp = new ResponsePojo();
		try {
			if (journeyId == null) {
				log.info("Journey-Id is mandatory. Please enter and try again");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Journey-Id is missing");
				rp.setResponseCode("0011");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			} else if (Channel_partner_key == null) {
				rp.setJourneyId(journeyId);
				rp.setMessage("Channel_partner_key is missing, please check");
				rp.setResponseCode("0010");
				// return ResponseEntity.internalServerError().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);

			}
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			GenerateOtpModel generateOtpModel = configUtil.getCustomerData().get(journeyId);
			HttpEntity<GenerateOtpModel> httpEntity = new HttpEntity<>(generateOtpModel, headers);
			RegistrationRequest registrationRequest = generateOtpModel.getRegistrationRequest();
			OtpRegistration otpRegistration = generateOtpModel.getOtpRegistration();
			otpRegistration.setOtp(validateRequest.getOtp());
			otpRegistration.setJourneyId(journeyId);
			otpRegistration.setOtpTransactionId(validateRequest.getOtpTransactionId());
			generateOtpModel.setOtpRegistration(otpRegistration);
			if (!generateOtpModel.getRegistrationRequest().getJourneyId().equals(journeyId)) {
				rp.setJourneyId(journeyId);
				rp.setMessage("Journey ID is different, please check");
				rp.setResponseCode("0001");
				return ResponseEntity.internalServerError().body(rp);
			} else if (validateRequest.getOtpTransactionId() == null && validateRequest.getOtp() == null) {

				if (validateRequest.getOtpTransactionId() == null) {
					log.info("otp_transaction_id is mandatory. Please enter and try again.");
				} else if (validateRequest.getOtp() == null) {
					log.info("otp is mandatory. Please enter and try again.");
				}
				log.info("otp, otp_transaction_id is mandatory. Please enter and try again.");
				rp.setMessage("otp, otp_transaction_id is mandatory. Please enter and try again.");
				return ResponseEntity.internalServerError().body(rp);
			}
			JSONObject json = new JSONObject(generateOtpModel);
			log.info("Request to /v2/customer_otpservice/verify-registration-otp sent:\n" + json.toString(4));

			ValidateOtpResponse validateOtp;

			validateOtp = configUtil.getValidateMap().get(journeyId);
			if (validateOtp == null) {
				validateOtp = new ValidateOtpResponse();
			}
			LocalDateTime plusMinutes = LocalDateTime.now().plusMinutes(10);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM hh:mm a");
			if (validateOtp.getRetries() == 0) {
				log.info("There are no retries left, Login is Denied. Please wait for sometime and retry");

				rp.setMessage("There are no retries left, Login is Denied. Please wait for 10 minutes and retry after: "
						+ plusMinutes.format(formatter));
				rp.setResponseCode("0005");
				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.MOBILE_VERIFICATION_FAILED.name());
				ConfigUtil.setBPMNStep(StatusENUM.MOBILE_VERIFICATION_FAILED.name());
				return ResponseEntity.accepted().body(rp);
			}

			String SIMULATOR_URL = SIMULATOR_HOST + "/v2/customer_otpservice/verify-registration-otp";
			log.info("Invoking /v2/customer_otpservice/verify-registration-otp for validating the OTP");
			log.info("BPM next step to perform: Validating OTP");
			ResponseEntity<ValidateOtpResponse> res1 = restTemplate.exchange(SIMULATOR_URL, HttpMethod.POST, httpEntity,
					ValidateOtpResponse.class);

			validateOtp = res1.getBody();
			configUtil.getValidateMap().put(journeyId, validateOtp);
			JSONObject json1 = new JSONObject(validateOtp);
			log.info("Response from /v2/customer_otpservice/verify-registration-otp received:\n{}", json1.toString(4));
			if (!validateOtp.getErrorStatus().equals("NO_ERROR")) {
				if (validateOtp.getRetries() > 0 && validateOtp.getRetries() <= 3) {
					log.info(validateOtp.getErrorStatus());
					rp.setMessage(validateOtp.getErrorStatus());
					generateNew(headers, generateOtpModel);
					rp.setResponseCode("0002");
				} else {
					log.info("There are no retries left, Login is Denied. Please wait for sometime and retry");
					rp.setMessage(
							"There are no retries left, Login is Denied. Please wait for 10 minutes and retry after:"
									+ plusMinutes.format(formatter));
					rp.setResponseCode("0005");
				}

				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.MOBILE_VERIFICATION_FAILED.name());
				ConfigUtil.setBPMNStep(StatusENUM.MOBILE_VERIFICATION_FAILED.name());
				return ResponseEntity.accepted().body(rp);
			}

			if (!registrationRequest.getLoginSuspended() && validateOtp.isValid()) {
				registrationRequest.setCurrentStep(StatusENUM.MOBILE_VERIFICATION_COMPLETE.name());
				ConfigUtil.setBPMNStep(StatusENUM.MOBILE_VERIFICATION_COMPLETE.name());
				generateOtpModel.setRegistrationRequest(registrationRequest);

				ConfigUtil.setCurrentStep(registrationRequest.getCurrentStep());
				String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/fixed_deposit/fd-state";

				Map<String, Object> pathParams = new HashMap<>();
				pathParams.put("currentStep", ConfigUtil.getCurrentStep());
				log.info("Invoking /v2/fixed_deposit/fd-state for ETB/NTB Check");
				log.info("BPM next step to perform: ETB/NTB check");

				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1)
						.queryParam("currentStep", ConfigUtil.getCurrentStep());
				String pan = generateOtpModel.getRegistrationRequest().getPan();
				HttpEntity<String> httpEntity1 = new HttpEntity<>(pan, headers);
				log.info("Request to /v2/fixed_deposit/fd-state sent:" + pan);

				ResponseEntity<String> res2 = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, httpEntity1,
						String.class);
				log.info("Response from /v2/fixed_deposit/fd-state received:{}", res2.getBody());
				if (res2.getBody().equals(StatusENUM.NEW_CUSTOMER.name())) {
					log.info("The customer with {} seems to be a new customer", pan);
					ConfigUtil.setCustomerStatus(StatusENUM.NEW_CUSTOMER.name());
					ConfigUtil.setCurrentStep(StatusENUM.NEW_CUSTOMER.name());
					ConfigUtil.setBPMNStep(StatusENUM.NEW_CUSTOMER.name());
					generateOtpModel.getRegistrationRequest().setCurrentStep(StatusENUM.NEW_CUSTOMER.name());
				} else {
					log.info("The customer with {} seems to be a existing customer", pan);
					ConfigUtil.setCustomerStatus(StatusENUM.EXISTING_CUSTOMER.name());
					ConfigUtil.setCurrentStep(StatusENUM.EXISTING_CUSTOMER.name());
					ConfigUtil.setBPMNStep(StatusENUM.EXISTING_CUSTOMER.name());
					generateOtpModel.getRegistrationRequest().setCurrentStep(StatusENUM.EXISTING_CUSTOMER.name());
				}
//				validateOtp.setAccessToken(ConfigUtil.generateAccessToken(registrationRequest.getPan()));
				validateOtp.setAccessToken(ConfigUtil.generateAccessToken(registrationRequest));
				validateOtp.setLanguage("English");
				configUtil.getApplicationMap().put(validateOtp.getApplicationId(),
						generateOtpModel.getRegistrationRequest());

				ValidateOTPResponses finalRes = new ValidateOTPResponses();
				finalRes.setAccessToken(validateOtp.getAccessToken());
				finalRes.setCustomerId(validateOtp.getCustomerId());
//				finalRes.setJourneyId(validateOtp.getJourneyId());
				finalRes.setLanguage(validateOtp.getLanguage());
				finalRes.setName(validateOtp.getName());
//				finalRes.setTaskId(validateOtp.getTaskId());
				finalRes.setApplicationId(validateOtp.getApplicationId());

				rp.setData(finalRes);
				rp.setMessage("Success");
				rp.setResponseCode("200");
				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.MOBILE_VERIFICATION_COMPLETE.name());
				ConfigUtil.setBPMNStep(StatusENUM.MOBILE_VERIFICATION_COMPLETE.name());
				return ResponseEntity.ok().body(rp);
			} else {
				if (registrationRequest.getSuspensionExpired()
						&& (validateOtp.getRetries() > 0 && validateOtp.getRetries() <= 3)) {
					log.info(
							"Invoking /v2/customer_otpservice/generate-registration-otp for OTP regeneration as the retries available is {}",
							validateOtp.getRetries());

					log.info("Request to /v2/customer_otpservice/generate-registration-otp sent:\n"
							+ new JSONObject(generateOtpModel).toString(4));
					ResponseEntity<GenerateOtpModel> res3 = generateNew(headers, generateOtpModel);
					GenerateOtpModel generateOTP = res3.getBody();
					log.info("Response from /v2/customer_otpservice/generate-registration-otp received:\n{}",
							new JSONObject(generateOTP).toString(4));
					RegisterResponse registerResponse = new RegisterResponse();
					registerResponse
							.setNoOfAttemptsRemaining(generateOTP.getOtpRegistration().getNoOfAttemptsRemaining());
					registerResponse.setOtpTransactionId(generateOTP.getOtpRegistration().getOtpTransactionId());
//					registerResponse.setOtpCreatedAt(generateOTP.getOtpRegistration().getOtpCreatedAt());
					rp.setData(registerResponse);
					rp.setMessage("Invoked generation regestration otp API as retry count is available ");
					rp.setResponseCode("0004");
					rp.setBpmnStatus(StatusENUM.SUSPENSION_REVOKED.name());
					ConfigUtil.setBPMNStep(StatusENUM.SUSPENSION_REVOKED.name());
					rp.setJourneyId(journeyId);
					return ResponseEntity.accepted().body(rp);
				} else if (!registrationRequest.getSuspensionExpired()) {
					log.info("Login is Declined as the suspension is expired for the customer:"
							+ registrationRequest.getPan());
					rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
					ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
					rp.setMessage("Login is Declined as the suspension is expired for the customer:"
							+ registrationRequest.getPan());
					rp.setResponseCode("0005");
					rp.setJourneyId(journeyId);
					return ResponseEntity.accepted().body(rp);
				} else {
					log.error("Login is suspended as the retries count is not available, please try after 10minutes");
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.MINUTE, 15);
					rp.setData(c);
					rp.setMessage("Maximum number of attempts reached. Please try after some time");
					rp.setResponseCode("0005");
					rp.setBpmnStatus(StatusENUM.LOGIN_SUSPENDED.name());
					ConfigUtil.setBPMNStep(StatusENUM.LOGIN_SUSPENDED.name());
					rp.setJourneyId(journeyId);
					return ResponseEntity.accepted().body(rp);
				}

			}

		} catch (Exception e) {
			log.error("Exception occured during validateOtp:" + e.getMessage());
			rp.setMessage("Server encountered an unexpected error");
			rp.setData(null);
			rp.setResponseCode("500");
			rp.setJourneyId(journeyId);
			return ResponseEntity.internalServerError().body(rp);
		}
	}

	private ResponseEntity<GenerateOtpModel> generateNew(HttpHeaders headers, GenerateOtpModel generateOtpModel) {
		String regenerate_URL = SIMULATOR_HOST + "/v2/customer_otpservice/generate-registration-otp";
		log.info("Invoking /v2/customer_otpservice/generate-registration-otp for generating new API");
		log.info("Invoking {} to generate the OTP as the retries are left:" + regenerate_URL);
		HttpEntity<GenerateOtpModel> httpEntity1 = new HttpEntity<>(generateOtpModel, headers);
		ResponseEntity<GenerateOtpModel> res3 = restTemplate.exchange(regenerate_URL, HttpMethod.POST, httpEntity1,
				GenerateOtpModel.class);
		return res3;
	}

	public ResponseEntity<ResponsePojo> postCustomerDetails(AccountStatusResponse asr, String journeyId,
			String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		try {

			if (journeyId == null) {
				log.info("Journey-Id is mandatory. Please enter and try again");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Journey-Id is missing");
				rp.setResponseCode("0011");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			} else if (Channel_partner_key == null || Channel_partner_key.isEmpty()) {
				log.info("Channel_partner_key is missing");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Channel_partner_key is missing");
				rp.setResponseCode("0010");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			}
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// UIDAI Status check
			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/fixed_deposit/fd-state";

			log.info("Checking for BPM steps to perform:\n {}" + "\n",
					"\n 1. Aadhar Name match,\n 2. Aadhar deduplication,\n "
							+ "3. Save customer details,\n 4. Save Address details,\n "
							+ "5. Save Nominee details,\n 6. Save location details \n");
//			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
//					StatusENUM.NEW_CUSTOMER_POST.name());
//
////			GenerateOtpModel generateOtpModel = configUtil.getCustomerData().get(journeyId);
//
//			HttpEntity<String> httpEntity2 = new HttpEntity<>(objectMapper.writeValueAsString(asr),
//					headers);
//
//			// req and res log
//
//			restTemplate.exchange(builder.toUriString(), HttpMethod.POST, httpEntity2, String.class);

			ConfigUtil.setCurrentStep(StatusENUM.UIDAI_VERIFICATION_COMPLETE.name());
			ConfigUtil.setBPMNStep(StatusENUM.UIDAI_VERIFICATION_COMPLETE.name());
			HttpEntity<String> httpEntity1 = new HttpEntity<>(objectMapper.writeValueAsString("No_data"), headers);
			// Aadhar deduplication
			log.info("BPM next step: Aadhar deduplication:");
			log.info("Invoking /v2/ekyc/aadhaar-dedupe-check for Aadhar deduplication");
			String SIMULATOR_URL2 = SIMULATOR_HOST + "/v2/ekyc/aadhaar-dedupe-check";

//		
			log.info("Request to /v2/ekyc/aadhaar-dedupe-check sent:" + ConfigUtil.getCurrentStep());

			HttpEntity<String> httpEntity3 = new HttpEntity<>(headers);
			UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL2).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			ResponseEntity<AadharDetails> exchange = restTemplate.exchange(builder2.toUriString(), HttpMethod.GET,
					httpEntity3, AadharDetails.class);
			log.info("Response from /v2/ekyc/aadhaar-dedupe-check received:\n{}",
					new JSONObject(exchange.getBody()).toString(4));

			ConfigUtil.setCurrentStep(StatusENUM.PROFILE_IN_PROGRESS.name());
			ConfigUtil.setBPMNStep(StatusENUM.PROFILE_IN_PROGRESS.name());
			// Save customer details
			UriComponentsBuilder builder3 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			log.info("Invoking /v2/fixed_deposit/fd-state for Saving customer details");
			log.info("BPM next step: Save customer details");
			log.info("Request to /v2/fixed_deposit/fd-state sent:" + new JSONObject(asr).toString(4));
			restTemplate.exchange(builder3.toUriString(), HttpMethod.POST, httpEntity1, String.class);
			log.info("Response from /v2/fixed_deposit/fd-state received:{}", "Saved customer details");
			// Save Address
			log.info("BPM next step: Save Address details");
			log.info("Invoking /v2/fixed_deposit/fd-state for Saving Address details");
			UriComponentsBuilder builder4 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			log.info("Request to /v2/fixed_deposit/fd-state sent:" + new JSONObject(asr).toString(4));
			restTemplate.exchange(builder4.toUriString(), HttpMethod.POST, httpEntity1, String.class);
			log.info("Response from /v2/fixed_deposit/fd-state received:{}", "Saved Address details");

			// Save Nominee
			log.info("BPM next step: Save Nominee details");
			log.info("Invoking /v2/fixed_deposit/fd-state for Saving Nominee details");
			UriComponentsBuilder builder5 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			log.info("Request to /v2/fixed_deposit/fd-state sent:" + new JSONObject(asr).toString(4));
			restTemplate.exchange(builder5.toUriString(), HttpMethod.POST, httpEntity1, String.class);
			log.info("Response from /v2/fixed_deposit/fd-state received:{}", "Saved Nominee details");

			ConfigUtil.setCurrentStep(StatusENUM.PROFILE_COMPLETED.name());
			ConfigUtil.setBPMNStep(StatusENUM.PROFILE_COMPLETED.name());
			// Save Location
			log.info("Invoking /v2/fixed_deposit/fd-state for Saving Location details");
			log.info("BPM next step: Save location details");
			UriComponentsBuilder builder6 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());

			String asrString = objectMapper.writeValueAsString(asr);
			HttpEntity<String> httpEntity7 = new HttpEntity<>(asrString, headers);
			log.info("Request to /v2/fixed_deposit/fd-state sent:" + new JSONObject(asr).toString(4));
			ResponseEntity<String> res7 = restTemplate.exchange(builder6.toUriString(), HttpMethod.POST, httpEntity7,
					String.class);

			log.info("Response from /v2/fixed_deposit/fd-state received:\n{}", new JSONObject(res7).toString(4));

			if (res7.getBody() != null) {
				log.info("Customer details saved successfully..");
				ConfigUtil.setCurrentStep(StatusENUM.PROFILE_COMPLETED.name());
				ConfigUtil.setBPMNStep(StatusENUM.PROFILE_COMPLETED.name());
				PostCustomerResponse psr = objectMapper.readValue(res7.getBody(), PostCustomerResponse.class);
//				psr.setJourneyId(journeyId);
				rp.setData(psr);
//				rp.setData(psr);
				rp.setMessage("Success");
				rp.setResponseCode("200");
				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.PROFILE_COMPLETED.name());
				ConfigUtil.setBPMNStep(StatusENUM.PROFILE_COMPLETED.name());
				return ResponseEntity.ok().body(rp);
			} else {
				log.info("Unable to save Customer details!");
				ConfigUtil.setCurrentStep(StatusENUM.PROFILE_FAILED.name());
				ConfigUtil.setBPMNStep(StatusENUM.PROFILE_FAILED.name());
				String resp = res7.getBody();
				rp.setData(null);
				rp.setMessage(resp);
				rp.setResponseCode("0004");
				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.PROFILE_FAILED.name());
				ConfigUtil.setBPMNStep(StatusENUM.PROFILE_FAILED.name());
				return ResponseEntity.accepted().body(rp);
			}
		} catch (Exception e) {
			rp.setData(null);
			rp.setMessage("Unable to reach create saving account AP");
			rp.setResponseCode("500");
			rp.setJourneyId(journeyId);
			return ResponseEntity.internalServerError().body(rp);
		}
	}

	public ResponseEntity<ResponsePojo> postAccountStatus(CreateRequest cm, String journeyId,
			String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		try {
			if (cm.getApplicationId() == null || cm.getJourney() == null) {
				if (cm.getApplicationId() == null) {
					log.info("ApplicationId is mandatory. Please enter and try again");
				} else if (cm.getJourney() == null) {
					log.info("Journey is mandatory. Please enter and try again");
				}
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("ApplicationId, Journey is mandatory. Please enter and try again");
				rp.setResponseCode("0001");
				return ResponseEntity.accepted().body(rp);
			} else if (Channel_partner_key == null || Channel_partner_key.isEmpty()) {
				log.info("Channel_partner_key is missing");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Channel_partner_key is missing");
				rp.setResponseCode("0010");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			} else if (journeyId == null) {
				log.info("Journey-Id is missing");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Journey-Id is mandatory. Please enter and try again");
				rp.setResponseCode("0011");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			}

			log.info("Request to /v2/ekyc/factiva sent:\n" + new JSONObject(cm).toString(4));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CreateRequest> httpEntity = new HttpEntity<>(cm, headers);

			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/ekyc/factiva";
			log.info("Invoking /v2/ekyc/factiva for factiva check on customer status");
			log.info("BPM next step to perform: verify factiva blacklisted check");

			ResponseEntity<String> res1 = restTemplate.exchange(SIMULATOR_URL1, HttpMethod.POST, httpEntity,
					String.class);
			log.info("Response from /v2/ekyc/factiva received:\n{}", new JSONObject(res1).toString(4));
			if (res1.getBody().equals(StatusENUM.WHITELISTED_CUSTOMER.name())) {
				log.info("The customer is a " + StatusENUM.WHITELISTED_CUSTOMER.name());
				String SIMULATOR_URL2 = SIMULATOR_HOST + "/v2/fixed_deposit/create-savings-account";
				log.info("Invoking /v2/fixed_deposit/create-savings-account for creating savings account");
				log.info("BPM next step to perform: Create Savings Account");
				log.info("Request to /v2/fixed_deposit/create-savings-account sent:" + new JSONObject(cm).toString(4));

				UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL2).queryParam("journeyId",
						journeyId);
				ResponseEntity<CreateResponse> res2 = restTemplate.exchange(builder2.toUriString(), HttpMethod.POST,
						httpEntity, CreateResponse.class);
				CreateResponse cr = res2.getBody();
				log.info("Response from /v2/fixed_deposit/create-savings-account received:\n{}", cr.toString());
//				cr.setJourneyId(journeyId);
				rp.setData(cr);
				rp.setData(res2.getBody());
				rp.setMessage("Success");
				rp.setResponseCode("200");
				rp.setBpmnStatus(StatusENUM.ACCOUNT_CREATED.name());
				ConfigUtil.setCurrentStep(StatusENUM.ACCOUNT_CREATED.name());
				ConfigUtil.setBPMNStep(StatusENUM.ACCOUNT_CREATED.name());

				RegistrationRequest registrationRequest = configUtil.getApplicationMap().get(cm.getApplicationId());
				configUtil.getPanDb().add(registrationRequest.getPan());

				rp.setJourneyId(journeyId);
				return ResponseEntity.ok().body(rp);
			} else {
				log.info("The customer is a " + StatusENUM.BLACKLISTED_CUSTOMER.name());
				rp.setData(null);
				rp.setMessage("Customer is blacklisted");
				rp.setResponseCode("0002");
				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.BLACKLISTED_CUSTOMER.name());
				ConfigUtil.setBPMNStep(StatusENUM.BLACKLISTED_CUSTOMER.name());
				return ResponseEntity.accepted().body(rp);
			}

		} catch (Exception e) {
			log.error("Exception raised on createSavingsAccount:" + e.getMessage());
			rp.setData(null);
			rp.setMessage("The server encountered an unexpected error.");
			rp.setResponseCode("500");
//			rp.setBpmnStatus(StatusENUM.BLACKLISTED_CUSTOMER.name());
			return ResponseEntity.internalServerError().body(rp);
		}
	}

	@SuppressWarnings("unused")
	public ResponseEntity<ResponsePojo> getCustomerDetails(AccountStatusRequest asr, String journeyId,
			String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		try {
			if (journeyId == null) {
				log.info("Journey-Id is mandatory. Please enter and try again");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Journey-Id is missing");
				rp.setResponseCode("0011");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			} else if (Channel_partner_key == null) {
				rp.setJourneyId(journeyId);
				rp.setMessage("Channel_partner_key is missing, please check");
				rp.setResponseCode("0010");
				// return ResponseEntity.internalServerError().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);

			}
			log.info("BPM next step to perform: Aadhar validation");
			if (asr.getCustomerId() == null || asr.getJourney() == null) {

				if (asr.getCustomerId() == null) {
					log.info("CustomerId is mandatory. Please enter and try again");
				} else if (asr.getJourney() == null) {
					log.info("Journey is mandatory. Please enter and try again");
				}
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("CustomerId, Journey is mandatory. Please enter and try again");
				rp.setResponseCode("0001");
				return ResponseEntity.accepted().body(rp);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/fixed_deposit/fd-state";
			asr.setJourneyId(journeyId);
			HttpEntity<String> httpEntity1 = new HttpEntity<>(objectMapper.writeValueAsString(asr), headers);
			// Aadhar deduplication
			log.info("Invoking /v2/fixed_deposit/fd-state for Aadhar validation");
//			UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
//					StatusENUM.NEW_CUSTOMER_GET.name());
			UriComponentsBuilder builder2 = null;
			if (ConfigUtil.getCurrentStep().equals(StatusENUM.NEW_CUSTOMER.name())) {
				builder2 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
						StatusENUM.NEW_CUSTOMER_GET.name());
			} else if (ConfigUtil.getCurrentStep().equals(StatusENUM.PROFILE_COMPLETED.name())) {
				builder2 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
						StatusENUM.PROFILE_COMPLETED_GET.name());
			} else if (ConfigUtil.getCurrentStep().equals(StatusENUM.ACCOUNT_CREATED.name())
					|| ConfigUtil.getCurrentStep().equals(StatusENUM.ACCOUNT_ACTIVATED.name())) {
				builder2 = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
						ConfigUtil.getCurrentStep());
			}
			log.info("Request to /v2/fixed_deposit/fd-state sent:" + new JSONObject(asr).toString(4));

			ResponseEntity<String> res = restTemplate.exchange(builder2.toUriString(), HttpMethod.POST, httpEntity1,
					String.class);

//			String SIMULATOR_URL = SIMULATOR_HOST + "/v2/ekyc/aadhaarValidation";

//			HttpHeaders headers1 = new HttpHeaders();
//			
//			HttpEntity<AccountStatusRequest> httpEntity2 = new HttpEntity<>(asr, headers1);
//			log.info("Request to /v2/ekyc/aadhaarValidation sent:"+asr.toString());
//
//			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL).queryParam("journeyId",
//					journeyId);
//			ResponseEntity<AccountStatusResponse> res = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
//					httpEntity2, AccountStatusResponse.class);

			AccountStatusResponse asre = objectMapper.readValue(res.getBody(), AccountStatusResponse.class);

			if (asre != null) {
				log.info("Response from /v2/fixed_deposit/fd-state received:\n{}", new JSONObject(asre).toString(4));
				log.info("UIDAI verification has completed for this customer");
				rp.setData(asre);
				rp.setMessage("Success");
				rp.setJourneyId(journeyId);
				if(ConfigUtil.getCurrentStep().equals(StatusENUM.PROFILE_COMPLETED.name())) {
					rp.setBpmnStatus(StatusENUM.PROFILE_COMPLETED.name());
					ConfigUtil.setBPMNStep(StatusENUM.PROFILE_COMPLETED.name());
				}else if(ConfigUtil.getCurrentStep().equals(StatusENUM.ACCOUNT_CREATED.name())
						|| ConfigUtil.getCurrentStep().equals(StatusENUM.ACCOUNT_ACTIVATED.name())) {
					rp.setBpmnStatus(StatusENUM.ACCOUNT_CREATED.name());
					ConfigUtil.setBPMNStep(StatusENUM.ACCOUNT_CREATED.name());
				}else {
				rp.setBpmnStatus(StatusENUM.UIDAI_VERIFICATION_COMPLETE.name());
				ConfigUtil.setBPMNStep(StatusENUM.UIDAI_VERIFICATION_COMPLETE.name());
				}
				rp.setResponseCode("200");
				return ResponseEntity.ok().body(rp);
			} else {
				log.info("Response from /v2/fixed_deposit/fd-state received:{}", "UIDAI_AUTH_FAILED");
				log.info("UIDAI verification has failed for this customer");
				rp.setData(null);
				rp.setMessage("UIDAI Verification failed");
				rp.setJourneyId(journeyId);
				rp.setBpmnStatus(StatusENUM.UIDAI_AUTH_FAILED.name());
				ConfigUtil.setBPMNStep(StatusENUM.UIDAI_AUTH_FAILED.name());
				rp.setResponseCode("0002");
				return ResponseEntity.accepted().body(rp);
			}

		} catch (Exception e) {
			rp.setData(null);
			log.error("Exception occured during getCustomerDetails:" + e.getMessage());
			rp.setMessage("The server encountered an unexpected error");
			rp.setResponseCode("500");
			return ResponseEntity.internalServerError().body(rp);
		}
	}

//	public ResponseEntity<ResponsePojo> createSavingsAccount(CreateRequest cm, String journeyId) {
//		ResponsePojo rp = new ResponsePojo();
//		try {
//			HttpHeaders headers = configUtil.getHttpHeaders();
//
//			headers.setContentType(MediaType.APPLICATION_JSON);
//
//			HttpEntity<CreateRequest> httpEntity = new HttpEntity<>(cm, headers);
//
//			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/ekyc/factiva";
//			HttpEntity<String> httpEntity1 = new HttpEntity<>(headers);
//			
//			log.info("BPM step to perform: Verify Active");
//			ResponseEntity<String> res1 = restTemplate.exchange(SIMULATOR_URL1, HttpMethod.GET, httpEntity1,
//					String.class);
//
//			if (res1.getBody().equals(StatusENUM.WHITELISTED_CUSTOMER.name())) {
//				String SIMULATOR_URL2 = SIMULATOR_HOST + "/v2/fixed-deposit/create-savings-account";
//				log.info("BPM step to perform: Create Savings Account");
//				ResponseEntity<CreateResponse> res2 = restTemplate.exchange(SIMULATOR_URL2, HttpMethod.POST, httpEntity,
//						CreateResponse.class);
//				if (res2.getStatusCode().equals(HttpStatus.OK)) {
//
//					rp.setData(objectMapper.writeValueAsString(res2.getBody()));
//					rp.setMessage("Customer is not blacklisted and created savings account");
//					rp.setResponseCode("200");
//					return ResponseEntity.ok().body(rp);
//				} else {
//					rp.setData(null);
//					rp.setMessage("Customer is not blacklisted and unable to create savings account");
//					rp.setResponseCode("0002");
//					return ResponseEntity.accepted().body(rp);
//				}
//
//			} else {
//				rp.setData(null);
//				rp.setMessage("Customer is blacklisted");
//				rp.setResponseCode("0002");
//				return ResponseEntity.accepted().body(rp);
//			}
//
//		} catch (Exception e) {
//			log.error("Exception raised on createSavingsAccount:" + e.getMessage());
//			rp.setData(null);
//			rp.setMessage("Unable to reach servers");
//			rp.setResponseCode("0002");
//			return ResponseEntity.internalServerError().body(rp);
//		}
//	}
//
//	public Object getPaymentStatus(PaymentStatusRequest psr) {
//		try {
//			HttpHeaders headers = configUtil.getHttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//
//			JSONObject requestBody = new JSONObject(psr);
//			HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);
//
//			String SIMULATOR_URL = SIMULATOR_HOST + "/v1/payment-gateway/get-status";
//			
//			 log.info("BPM step to perform: get-Status");
//			ResponseEntity<String> res1 = restTemplate.exchange(SIMULATOR_URL, HttpMethod.GET, httpEntity,
//					String.class);
//
//			ResponsePojo rp = new ResponsePojo();
//
//			PaymentStatusResponse ps = new PaymentStatusResponse();
//
//			rp.setData(objectMapper.writeValueAsString(ps));
//			rp.setMessage(res1.getBody());
//			rp.setResponseCode("");
//
//			return rp;
//		} catch (Exception e) {
//
//		}
//		return null;
//	}
//
//	public ResponsePojo initiateVkyc(CreateRequest cm) {
//		try {
//			HttpHeaders headers = configUtil.getHttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//
//			JSONObject requestBody = new JSONObject(cm);
//			HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);
//
//			String SIMULATOR_URL = SIMULATOR_HOST + "/v2/video-kyc/initiate-vkyc-experience";
////			log.info("SIMULATOR_URL:" + SIMULATOR_URL);
//			log.info("BPM step to perform: initiate-vkyc-experience");
//			ResponseEntity<String> res1 = restTemplate.exchange(SIMULATOR_URL, HttpMethod.GET, httpEntity,
//					String.class);
//
//			ResponsePojo rp = new ResponsePojo();
//
//			VkycResponse vr = new VkycResponse();
//
//			rp.setData(objectMapper.writeValueAsString(vr));
//			rp.setMessage(res1.getBody());
//			rp.setResponseCode("");
//
//			return rp;
//		} catch (Exception e) {
//
//		}
//		return null;
//	}

	public Object getAccountStatus(String journeyId, String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		try {
			if (journeyId == null) {
				log.info("Journey-Id is mandatory. Please enter and try again");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Journey-Id is missing");
				rp.setResponseCode("0011");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			} else if (Channel_partner_key == null || Channel_partner_key.isEmpty()) {
				log.info("Channel_partner_key is missing");
				rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
				ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
				rp.setMessage("Channel_partner_key is missing");
				rp.setResponseCode("0010");
//				return ResponseEntity.accepted().body(rp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
			}
			HttpHeaders headers = new HttpHeaders();// new httppheaders
			headers.setContentType(MediaType.APPLICATION_JSON);
			log.info("Request to /v2/fixed_deposit/fd-state sent:" + ConfigUtil.getCurrentStep());
			HttpEntity<String> httpEntity = new HttpEntity<>(journeyId, headers);
			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/fixed_deposit/fd-state";
			log.info("Invoking /v2/fixed_deposit/fd-state for retrieving the account details");
			ConfigUtil.setCurrentStep(StatusENUM.ACTIVATION_PENDING.name());
			ConfigUtil.setBPMNStep(StatusENUM.ACTIVATION_PENDING.name());
			log.info("BPM step to perform :fd-state");
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			ResponseEntity<String> res1 = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, httpEntity,
					String.class);

			AccountResponse as = objectMapper.readValue(res1.getBody(), AccountResponse.class);
			log.info("Response from /v2/fixed_deposit/fd-state received:\n{}", new JSONObject(as).toString(4));
			log.info("Account Activated for the given customer");
			rp.setData(as);// as
			rp.setMessage("Success");
			rp.setResponseCode("200");
			rp.setJourneyId(journeyId);
			rp.setData(as);
			rp.setBpmnStatus(StatusENUM.ACCOUNT_ACTIVATED.name());
			ConfigUtil.setCurrentStep(StatusENUM.ACCOUNT_ACTIVATED.name());
			ConfigUtil.setBPMNStep(StatusENUM.ACCOUNT_ACTIVATED.name());
			return ResponseEntity.ok().body(rp);

		} catch (Exception e) {
			log.info("Server encountered an unexpected error:" + e.getMessage());
			rp.setData(null);
			rp.setMessage("Server encountered an unexpected error");
			rp.setResponseCode("500");
			rp.setJourneyId(journeyId);
			rp.setBpmnStatus(StatusENUM.ACCOUNT_FAILED.name());
			ConfigUtil.setBPMNStep(StatusENUM.ACCOUNT_FAILED.name());
			return ResponseEntity.internalServerError().body(rp);
		}
	}

	public Object getDetailes(MasterDataRequest masterDto) {
		ResponsePojo rp = new ResponsePojo();
		try {
			HttpHeaders headers = configUtil.getHttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JSONObject requestBody = new JSONObject(masterDto);
			HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);

			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/master";
			log.info("Invoking SIMULATOR_URL:{} for Getting the account details:", SIMULATOR_URL1);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			ResponseEntity<Object> res1 = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, httpEntity,
					Object.class);

			rp.setData(res1.getBody());
			rp.setMessage("fetched master detailes");
			rp.setResponseCode("200");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rp;
	}

	public Object getDetailes() {
		ResponsePojo rp = new ResponsePojo();
		try {
			HttpHeaders headers = configUtil.getHttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> httpEntity = new HttpEntity<>(headers);

			String SIMULATOR_URL1 = SIMULATOR_HOST + "/v2/master";
			log.info("Invoking SIMULATOR_URL:{} for Getting the account details:", SIMULATOR_URL1);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIMULATOR_URL1).queryParam("currentStep",
					ConfigUtil.getCurrentStep());
			ResponseEntity<Object> res1 = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity,
					Object.class);

			rp.setData(res1.getBody());
			rp.setMessage("fetched all master detailes");
			rp.setResponseCode("200");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rp;
	}

	public Object getCurrentBpmnStatus(String journeyId, String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		if (journeyId == null) {
			log.info("JourneyId is mandatory. Please enter and try again");
			rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
			ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
			rp.setMessage("Journey-Id is missing");
			rp.setResponseCode("0011");
//			return ResponseEntity.accepted().body(rp);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
		} else if (Channel_partner_key == null || Channel_partner_key.isEmpty()) {
			log.info("Channel_partner_key is missing");
			rp.setBpmnStatus(StatusENUM.LOGIN_DENIED.name());
			ConfigUtil.setBPMNStep(StatusENUM.LOGIN_DENIED.name());
			rp.setMessage("Channel_partner_key is missing");
			rp.setResponseCode("0010");
//			return ResponseEntity.accepted().body(rp);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
		}

		ValidateOtpResponse validateOtp;

		validateOtp = configUtil.getValidateMap().get(journeyId);

		BpmnResponse rp1 = new BpmnResponse();
		if (validateOtp == null) {
			rp1.setCustomerId(null);
			rp1.setApplicationId(null);
		} else {
			rp1.setCustomerId(validateOtp.getCustomerId());
			rp1.setApplicationId(validateOtp.getApplicationId());
		}

		rp1.setJourneyId(journeyId);
		rp1.setBpmnStatus(ConfigUtil.getBPMNStep());

		return rp1;
	}

}
