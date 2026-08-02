package com.devsquad.payment_processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentProcessingApplication {

	public static void main(String[] args) {
        System.out.println("Before SpringApplication.run");
		SpringApplication.run(PaymentProcessingApplication.class, args);
        System.out.println("After SpringApplication.run");
	}

}
