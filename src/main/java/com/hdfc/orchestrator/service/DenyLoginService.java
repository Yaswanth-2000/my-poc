//package com.hdfc.orchestrator.service;
//
//import org.camunda.bpm.engine.delegate.DelegateExecution;
//import org.camunda.bpm.engine.delegate.JavaDelegate;
//import org.springframework.stereotype.Component;
//
//import com.hdfc.orchestrator.model.RegistrationRequest;
//import com.hdfc.orchestrator.model.StatusENUM;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@Slf4j
//public class DenyLoginService implements JavaDelegate{
//
//	@Override
//	public void execute(DelegateExecution execution) throws Exception {
//		RegistrationRequest rr = new RegistrationRequest();
//		rr.setCurrentStep(StatusENUM.LOGIN_SUSPENDED.name());
//		log.info("The Login has been denied for the journey:"+rr.getJourneyId());
//		
//	}
//
//}
