package com.ipho4ticket.clienteventfeign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClientEventFeignApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientEventFeignApplication.class, args);
	}

}
