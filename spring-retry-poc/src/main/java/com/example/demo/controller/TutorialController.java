package com.example.demo.controller;

import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.RestService;
import com.example.demo.entity.Tutorial;
import com.example.demo.repo.TutorialRepository;

import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;


//@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

	Logger log = LoggerFactory.getLogger(TutorialController.class);
	
	int attempts = 0;
	
	@Autowired
	TutorialRepository tutorialRepository;
	
	@Autowired
	RestService restService;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@GetMapping("/product")
	public ResponseEntity<String> getProduct() {
		log.info("product service called at:" + attempts++);
		String response = restService.testRetrywithYml();
		return new ResponseEntity<String>(response, HttpStatus.OK);
		
	}
	
	@GetMapping("/productTime")
	public CompletableFuture<String> productTime() throws InterruptedException, ExecutionException {
		log.info("productTime service called at:" + attempts++);
		CompletableFuture<String> response = restService.futreTime();
		return CompletableFuture.completedFuture("Test");
		
	}
	

    @GetMapping("/tl")
    public CompletableFuture<String> timeLimiter() {
    	log.info("tl..");
    	CompletableFuture<String> str = null;
        try {
        	str =  restService.timeLimiter();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
        
        return str;
    }
    
    @GetMapping("/tlClient")
    public CompletableFuture<String> tlClient() {
        return restService.timeLimiter();
    }
    
	/*
	 * @GetMapping("/tlStr") public String timeLimiterString() { return
	 * restService.timeLimiterString(); }
	 */
	
	@GetMapping("/bulkHeadApi")
	public String bulkHeadApi() throws InterruptedException, ExecutionException {
		log.info("productTime service called at:" + attempts++);
		String response = restService.getProductBulkHead();
		return response;
		
	}
	
	
	@GetMapping("/resiliencecDemo")
	public ResponseEntity<String> getProductDetails() {
		log.info("getProductDetails service called at:" + attempts++);
		
		RetryConfig retryConfig = RetryConfig
									.custom()
									.maxAttempts(5)
									.waitDuration(Duration.ofMillis(500))
									//.intervalFunction(IntervalFunction.ofExponentialBackoff(200, 2))
									.retryOnResult(b -> b.toString().contains("I"))
									.retryOnException(e -> e instanceof RuntimeException)
									.failAfterMaxAttempts(true)
									//.retryExceptions(ConnectException.class)
									//.retryOnResult((s) -> s != null)
									.build();
		
		RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
		
		io.github.resilience4j.retry.Retry retry = retryRegistry.retry("restService", retryConfig);
		
		//String executeSupplier = retry.executeSupplier(() -> restTemplate().getForObject("http://localhost:8082/v1/product/1", String.class));
		String test = restService.test(retry);
		//Supplier<String> supp = () -> restTemplate().getForObject("http://localhost:8082/v1/product/1", String.class);
		
		//Supplier<String> decorateSupplier = io.github.resilience4j.retry.Retry.decorateSupplier(retry, supp);
		
		String response = test;
		
		log.info("response:" + response);
		return new ResponseEntity<String>(response, HttpStatus.OK);
		
	}
	
	@GetMapping("/intervalFunction")
	public ResponseEntity<String> intervalFunction() {
		log.info("getProductDetails service called at:" + attempts++);
		
		RetryConfig retryConfig = RetryConfig
				.custom()
				.maxAttempts(2)
				.waitDuration(Duration.ofSeconds(2))
				//.intervalBiFunction(2)
				.failAfterMaxAttempts(true)
				.retryOnResult(b -> b.toString().contains("I"))

				//.retryOnException(e -> e instanceof RuntimeException)
				//.failAfterMaxAttempts(true)
				//.retryExceptions(ConnectException.class)
				//.retryOnResult((s) -> s != null)
				.build();

		RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);

		io.github.resilience4j.retry.Retry retry = retryRegistry.retry("testIntervalFunction", retryConfig);
		
		String response = restService.testIntervalFunction(retry);
		
		log.info("response:" + response);
		
		return new ResponseEntity<String>(response, HttpStatus.OK);
		
	}
	
	/*
	 * public ResponseEntity<String> fallbackRetry(Exception e) { attempts = 1;
	 * return new ResponseEntity<String>("Product Service Is Down", HttpStatus.OK);
	 * }
	 */
	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {
			List<Tutorial> tutorials = new ArrayList<Tutorial>();

			if (title == null)
				tutorialRepository.findAll().forEach(tutorials::add);
			else
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
		try {
			Tutorial _tutorial = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			Tutorial _tutorial = tutorialData.get();
			_tutorial.setTitle(tutorial.getTitle());
			_tutorial.setDescription(tutorial.getDescription());
			_tutorial.setPublished(tutorial.isPublished());
			return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}