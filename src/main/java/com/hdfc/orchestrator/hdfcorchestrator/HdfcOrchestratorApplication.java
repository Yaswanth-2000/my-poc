package com.hdfc.orchestrator.hdfcorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.hdfc.orchestrator.config.ConfigUtil;
import com.hdfc.orchestrator.controller.OrchestratorController;
import com.hdfc.orchestrator.service.OrchestratorService;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan(basePackageClasses = { OrchestratorController.class, OrchestratorService.class,
		ConfigUtil.class })
@EnableWebMvc
@CrossOrigin
public class HdfcOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(HdfcOrchestratorApplication.class, args);
	}
	
//	@Bean
//	protected UrlBasedCorsConfigurationSource corsConfigurationSource() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		CorsConfiguration config = new CorsConfiguration();
//		config.addAllowedOrigin("*");
//	    config.addAllowedHeader("*");
//	    config.addAllowedMethod("OPTIONS");
//	    config.addAllowedMethod("HEAD");
//	    config.addAllowedMethod("GET");
//	    config.addAllowedMethod("POST");
//		source.registerCorsConfiguration("/**", config);
////		return new CorsFilter(source);
//	}
	
	

}
