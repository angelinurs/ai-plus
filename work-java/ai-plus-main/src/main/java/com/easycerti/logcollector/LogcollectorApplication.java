package com.easycerti.logcollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogcollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogcollectorApplication.class, args);
	}

}
