package com.example.demo.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.ComputeResponse;
import com.example.demo.dto.ResponseType;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class ComputeController {

	Logger log = LoggerFactory.getLogger(ComputeController.class);
	@Autowired
	RestTemplate restTemplate;
	
    @GetMapping("/double/{input}")
    public String doubleValue(@PathVariable int input){
        return new String(2*input+"");
    }

    @GetMapping("/square/{input}")
    @RateLimiter(name = "sqaureLimit", fallbackMethod = "squareErrorResponse")
    public String getValue(@PathVariable int input){
    	
    	log.info("getValue called");
    	String response = "Service Called";
    	//String response = restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
    	
        return response;
    }
    
    @GetMapping("/rateLimiterTest")
    public String rateLimiterTest(){
    	
    	log.info("@@inside rateLimiterTest");
    	String response = null;
    	
    	ExecutorService executor = Executors.newFixedThreadPool(20);

		Callable<String> call1 = () -> restTemplate.getForObject("http://localhost:8080/square/10", String.class);
		Callable<String> call2 = () -> restTemplate.getForObject("http://localhost:8080/square/10", String.class);
		Callable<String> call3 = () -> restTemplate.getForObject("http://localhost:8080/square/10", String.class);
		Callable<String> call4 = () -> restTemplate.getForObject("http://localhost:8080/square/10", String.class);

		
		Future<String> fut1 = executor.submit(call1);
		Future<String> fut2 = executor.submit(call2);
		Future<String> fut3 = executor.submit(call3);
		Future<String> fut4 = executor.submit(call4);
		
		try {
			String futStr1 = fut1.get();
			String futStr2 = fut2.get();
			String futStr3 = fut3.get();
			String fut4Str4 = fut4.get();
			
			response = futStr1;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return response;

    }

    public String squareErrorResponse(int input, Throwable throwable){
        return new String(input + " " +  -1 + " " +  throwable.getMessage());
    }

}