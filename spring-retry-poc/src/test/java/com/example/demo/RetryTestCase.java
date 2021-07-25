package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.github.resilience4j.retry.Retry;

import static org.mockito.BDDMockito.*;

class RetryTestCase {

	RestService restService = null;
	@Test
	void test() {
		fail("Not yet implemented");
	}

	@Test
	public void shouldNotretry() {
	 
		restService = mock(RestService.class);
		Retry retry = Retry.ofDefaults("id");
		
		String supp = restService.test(retry);
		
		Supplier<String> s = () -> supp;
		  Supplier<String> supplier = Retry
		            .decorateSupplier(retry, s);
		  
		System.out.println(supplier.get());  
		given(restService.test(retry)).willReturn("p");
		 
	}
}
