package com.example.demo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringRetryPocApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringRetryPocApplication.class, args);
	}
	
	 
	
	@Override
	public void run(String... args) throws Exception {
		
		System.out.println("run method called..");
		
}
}
