package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class RestService {

	Logger log = LoggerFactory.getLogger(RestService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	int cnt = 0;
	
	@Retry(name = "restService", fallbackMethod = "retryfallback")
	public String test(io.github.resilience4j.retry.Retry retry) {
		System.out.println("test service called:" + cnt++);
		String executeSupplier = retry.executeSupplier(() -> restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class));
		return executeSupplier;
	}
	
	
	@Retry(name = "testIntervalFunction", fallbackMethod = "retryfallback")
	public String testIntervalFunction(io.github.resilience4j.retry.Retry retry) {
		System.out.println("test service called:" + cnt++);
		String executeSupplier = retry.executeSupplier(() -> restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class));
		return executeSupplier;
	}
	
	public String retryfallback(Exception t) {
		log.error("Inside retryfallback, cause – {}", t.toString());
        return "Inside retryfallback method. Some error occurred while calling service for user registration";
    }
}
