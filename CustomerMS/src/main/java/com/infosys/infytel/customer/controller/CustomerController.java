package com.infosys.infytel.customer.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infosys.infytel.customer.dto.CustomerDTO;
import com.infosys.infytel.customer.dto.LoginDTO;
import com.infosys.infytel.customer.dto.PlanDTO;
import com.infosys.infytel.customer.service.CustomerCircuitBreakerService;
import com.infosys.infytel.customer.service.CustomerService;

import io.vavr.concurrent.Future;

@RestController
@CrossOrigin
@RefreshScope
public class CustomerController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	CustomerService custService;
	
	@Autowired
	private CustomerCircuitBreakerService custCircuitService;
	
	// Create a new customer
	@RequestMapping(value = "/customers", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCustomer(@RequestBody CustomerDTO custDTO) {
		logger.info("Creation request for customer {}", custDTO);
		custService.createCustomer(custDTO);
	}

	// Login
	@RequestMapping(value = "/login", method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean login(@RequestBody LoginDTO loginDTO) {
		logger.info("Login request for customer {} with password {}", loginDTO.getPhoneNo(),loginDTO.getPassword());
		return custService.login(loginDTO);
	}

	// Fetches full profile of a specific customer
	@RequestMapping(value = "/customers/{phoneNo}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public CustomerDTO getCustomerProfile(@PathVariable Long phoneNo) {
		
		logger.info("Profile request for customer {}", phoneNo);
		CustomerDTO custDTO=custService.getCustomerProfile(phoneNo);
		
		Future<PlanDTO> futurePlanDTO=custCircuitService.getSpecificPlan(custDTO.getCurrentPlan().getPlanId());
		Future<List<Long>> futureFriends=custCircuitService.getSpecificFriends(phoneNo);
		
		custDTO.setCurrentPlan(futurePlanDTO.get());
		custDTO.setFriendAndFamily(futureFriends.get());
		
		return custDTO;
	}


}
