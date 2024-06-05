package com.hdfc.orchestrator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hdfc.orchestrator.config.ConfigUtil;
import com.hdfc.orchestrator.model.AccountStatusRequest;
import com.hdfc.orchestrator.model.AccountStatusResponse;
import com.hdfc.orchestrator.model.CreateRequest;
import com.hdfc.orchestrator.model.MasterDataRequest;
import com.hdfc.orchestrator.model.RegistrationRequest;
import com.hdfc.orchestrator.model.ResponsePojo;
import com.hdfc.orchestrator.model.StatusENUM;
import com.hdfc.orchestrator.model.ValidateRequest;
import com.hdfc.orchestrator.service.OrchestratorService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
@CrossOrigin
public class OrchestratorController {

	@Autowired
	private OrchestratorService customerService;

	@PostMapping("/gigsa/register")
	public ResponseEntity<ResponsePojo> customerIntroductionProcess(@Valid @RequestBody RegistrationRequest cd,
			@RequestHeader(name ="Channel_partner_key", required = false)  String Channel_partner_key) {
		ResponsePojo rp = new ResponsePojo();
		try {
			
			log.info("Invoking /api/v1/gigsa/register from Orchestrator");
			return customerService.validateCustomerDetails(cd, Channel_partner_key);
			
		} catch (Exception e) {
			log.error("Exception occured during Invoking customer-introduction API:" + e.getMessage());
		}
		return null;
	}

	@PostMapping("/gigsa/validate")
	public ResponseEntity<ResponsePojo> validateOtp(@Valid @RequestBody ValidateRequest validateRequest,
			@RequestHeader(name = "journey-id",required = false ) String journeyId,@RequestHeader(name ="Channel_partner_key", required = false) String Channel_partner_key) {
		log.info("Invoking /api/v1/gigsa/validate from Orchestrator");
		return customerService.validateOtp(validateRequest, journeyId,Channel_partner_key);
	}

	@PostMapping("/gigsa/customer")
	public ResponseEntity<ResponsePojo> postCustomerDetails(@RequestBody AccountStatusResponse asr,
			@RequestHeader(name = "journey-id",required = false) String journeyId,@RequestHeader(name ="Channel_partner_key", required = false) String Channel_partner_key) {
		log.info("Invoking POST /api/v1/gigsa/customer from Orchestrator");
		return customerService.postCustomerDetails(asr, journeyId, Channel_partner_key);
	}

	@GetMapping("/gigsa/customer")
	public ResponseEntity<ResponsePojo> getCustomerDetails(@RequestParam String customerId,
			@RequestParam String journey, @RequestHeader(name = "journey-id", required = false ) String journeyId,
			@RequestHeader(name ="Channel_partner_key", required = false) String Channel_partner_key) {
		log.info("Invoking GET /api/v1/gigsa/customer from Orchestrator");
		AccountStatusRequest asr = new AccountStatusRequest();
		asr.setCustomerId(customerId);
		asr.setJourney(journey);
		return customerService.getCustomerDetails(asr, journeyId,Channel_partner_key );
	}

	@PostMapping("/gigsa/account")
	public ResponseEntity<ResponsePojo> createSavingsAccount(@RequestBody CreateRequest cm,
			@RequestHeader(name = "journey-id",required = false ) String journeyId,@RequestHeader(name ="Channel_partner_key", required = false) String Channel_partner_key) {
		log.info("Invoking POST /api/v1/gigsa/account from Orchestrator");
		return customerService.postAccountStatus(cm, journeyId,Channel_partner_key);
	}

	@GetMapping("/gigsa/account")
	public Object getAccountStatus(@RequestHeader(name = "journey-id",required = false) String journeyId,@RequestHeader(name ="Channel_partner_key", required = false) String Channel_partner_key) {
		log.info("Invoking GET /api/v1/gigsa/account from Orchestrator");
		return customerService.getAccountStatus(journeyId, Channel_partner_key);
	}

	@PostMapping("/gigsa/master")
	public Object getMasterDetails(@RequestBody MasterDataRequest masterDto) {
		log.info("Invoking GET /api/v1/gigsa/master from Orchestrator");
		return customerService.getDetailes(masterDto);
	}
	
	@GetMapping("/gigsa/master")
	public Object getMasterDetails() {
		log.info("Invoking GET /api/v1/gigsa/master from Orchestrator");
		return customerService.getDetailes();
	}
	@GetMapping("/gigsa/bpmn")
	public Object getBpmbStatus( @RequestHeader(name ="journey-id",required = false) String journeyId ,@RequestHeader(name ="Channel_partner_key", required = false) String Channel_partner_key) {
		log.info("Invoking GET /api/v1/gigsa/master from Orchestrator");
//		return ConfigUtil.getBPMNStep();
		return customerService.getCurrentBpmnStatus(journeyId,Channel_partner_key);
	}
}
