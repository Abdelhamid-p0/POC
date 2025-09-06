package com.service.payement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class BmceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmceApplication.class, args);
	}

}
