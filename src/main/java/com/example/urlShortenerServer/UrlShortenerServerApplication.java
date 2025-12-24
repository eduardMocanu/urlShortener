package com.example.urlShortenerServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UrlShortenerServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerServerApplication.class, args);
	}

}
