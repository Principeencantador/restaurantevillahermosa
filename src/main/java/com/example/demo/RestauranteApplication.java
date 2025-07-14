package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class RestauranteApplication {

	public static void main(String[] args) {

		System.setProperty("java.awt.headless", "true");
		System.setProperty("java.awt.headless", "true");
		SpringApplication.run(RestauranteApplication.class, args);
	}

}
