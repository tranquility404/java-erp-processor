package com.tranquility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class Application {

	@Autowired
	@Value("${flask.server.url}")
	private static String flask;

	@Autowired
	@Value("${spring.data.mongodb.uri}")
	private static String mongo;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("Flask: " + flask);
		System.out.println("Mongo: " + mongo);
	}

}
