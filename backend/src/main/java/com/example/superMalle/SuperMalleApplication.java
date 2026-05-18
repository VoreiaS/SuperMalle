package com.example.superMalle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
public class SuperMalleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuperMalleApplication.class, args);
	}

}
