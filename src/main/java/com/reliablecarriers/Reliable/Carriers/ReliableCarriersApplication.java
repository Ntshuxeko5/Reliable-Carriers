package com.reliablecarriers.Reliable.Carriers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReliableCarriersApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReliableCarriersApplication.class, args);
	}

}
