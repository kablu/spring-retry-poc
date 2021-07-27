package com.example.demo;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Service
public class RestService {

	Logger log = LoggerFactory.getLogger(RestService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	int cnt = 0;
	int attempts = 1;
	
	@Retry(name = "restService", fallbackMethod = "retryfallback")
	public String test(io.github.resilience4j.retry.Retry retry) {
		log.info("test service called:" + cnt++);
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
	
	
	@Retry(name = "PRODUCTSERVICE", fallbackMethod = "fallbackRetry")
	@TimeLimiter(name = "PRODUCTSERVICE")
	public String testRetrywithYml() {
		String response = null;
		log.info("testRetrywithYml attempts:" + attempts++);
		response = restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
		return response;
	}
	
	public String fallbackRetry(Throwable ex) {	
		log.info("fallbackRetry attempts:" + attempts++);
		return new String("Product Service Is Down");
	}
	
	public String fallbackRetry(RuntimeException ex) {
		log.info("fallbackRetry attempts:" + attempts++);
		return new String("Product Service Is Down");
	}
	
	//@Retry(name = "PRODUCTSERVICE", fallbackMethod = "fallbackRetryWithCompletion")
	@TimeLimiter(name = "TIMELIMITER", fallbackMethod = "fallbackRetryWithCompletion")
	public CompletableFuture<String> futreTime() {
		String res = restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
		CompletableFuture<String> completedFuture = CompletableFuture.completedFuture(res);
		return completedFuture;
	}
	
	 
	 @Retry(name = "PRODUCTSERVICE", fallbackMethod = "fallbackProdService")
	 @TimeLimiter(name = "PRODUCTSERVICE")
	    public CompletableFuture<String> timeLimiter() {
	        return CompletableFuture.supplyAsync(this::timeLimiterRemoteCall);
	    }
	 
	   public CompletableFuture<String> fallbackProdService(Throwable ex) {		
			log.info("Retry Fallback:" + attempts++);
			return CompletableFuture.completedFuture("Retry Fallback");
		}
	   
	   public CompletableFuture<String> fallbackTimeLimiter(Throwable ex) {			
			log.info("fallbackTimeLimiter:" + attempts++);
			return CompletableFuture.completedFuture("TimeLimiter Fallback");
		}
		private String timeLimiterRemoteCall() {
			log.info("timeLimiterRemoteCall..");
			String response = null;
			response = restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
			// Thread.sleep(3000);

			//log.info("response:" + response);
			//response = doException();
			return response;
		}
		
		public String doException() {
			log.info("doException");
			if(true) throw new RuntimeException("This is a runtime exception");
			return "List Of Product";
		}
	
	public CompletableFuture<String> fallbackRetryWithCompletion(RuntimeException ex) {
		
		log.info("fallbackRetryWithCompletion attempts:" + attempts++);	
		CompletableFuture<String> str = CompletableFuture.completedFuture("Futre service is down");
		return str;
	}
	
	/*
	 * @TimeLimiter(name = "TIMELIMITER", fallbackMethod = "fallbackTimeLimiter")
	 * public String timeLimiterString() { return "OK"; }
	 */
	
	 	@RateLimiter(name = "PRODUCTSERVICERATEL", fallbackMethod = "rateLimiterFallback")
	    public String rateLimiter() {
	        return rateLimiterRemoteCall();
	    }
	 	
	 	public String rateLimiterFallback(Throwable ex) {		
				log.info("RateLimiter Fallback:" + attempts++);
				return "RateLimiter Fallback";
		}
	 
	 	private String rateLimiterRemoteCall() {
			log.info("rateLimiterRemoteCall..");
			return "OK";
		}
	 
	@Bulkhead(name = "productBulkHeadService", fallbackMethod = "getDefault")
    public String getProductBulkHead(){
		
		String res = null;
		ExecutorService executor = Executors.newFixedThreadPool(5);
		Callable<String> call1 = () ->  restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
		Callable<String> call2 = () ->  restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
		Callable<String> call3 = () ->  restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
		
		Future<String> submit1 = executor.submit(call1);
		Future<String> submit2 = executor.submit(call2);
		Future<String> submit3 = executor.submit(call3);
		//String res = restTemplate.getForObject("http://localhost:8082/v1/product/1", String.class);
		try {
			submit1.get();
			submit2.get();
			res =  submit3.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
    }
	
    public String getDefault(RuntimeException ex) {
		
		log.info("fallbackRetry Bulkhead attempts:" + attempts++);
		return "Product Service Is Down from bulk-head";
	}
	
}
